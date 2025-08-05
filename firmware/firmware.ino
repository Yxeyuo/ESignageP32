#include <WiFi.h>                  // WiFi support for ESP32
#include <WiFiClient.h>            // TCP client for HTTP
#include <WiFiClientSecure.h>      // Secure (TLS) client
#include <HTTPClient.h>            // HTTP requests
#include <ArduinoJson.h>           // JSON parsing
#include <SPIFFS.h>                // Filesystem on flash
#include <Wire.h>                  // I²C communication
#include <Adafruit_GFX.h>          // Graphics core library
#include <Adafruit_SSD1306.h>      // OLED display driver
#include <time.h>                  // Time functions (NTP)
#include <vector>                  // Dynamic array container

#define FW_VERSION     "0.0.6"
#define SCREEN_WIDTH   128
#define SCREEN_HEIGHT  64
#define OLED_RESET     -1           // No hardware reset pin
#define SDA_PIN        21
#define SCL_PIN        19

// OLED display instance over I²C
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

// Single message with text, font size and scroll flag
struct Msg {
  String text;
  int    fontSize;
  bool   scroll;
};

// Configuration loaded from JSON (local or remote)
struct Config {
  String           wifiSsid, wifiPassword, ntpServer, serverDomain;
  long             updateInterval, rotateInterval;
  int              deviceId;
  String           deviceToken;
  std::vector<Msg> messages;
} cfg;

// State variables for message rotation and scrolling
size_t        currentIdx      = 0;
unsigned long lastUpdate      = 0;
unsigned long lastRotate      = 0;
unsigned long lastScroll      = 0;
int16_t       scrollX         = SCREEN_WIDTH;
bool          staticNeedsDraw = true;

// Display a non-scrolling message
void showStatic(const Msg &m) {
  display.clearDisplay();
  display.setTextWrap(true);
  display.setTextSize(m.fontSize);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.print(m.text);
  display.display();
}

// Display a scrolling message
void showScroll(const Msg &m) {
  display.clearDisplay();
  display.setTextWrap(false);
  display.setTextSize(m.fontSize);
  display.setTextColor(SSD1306_WHITE);
  // center text vertically
  int16_t y = (SCREEN_HEIGHT - 8 * m.fontSize) / 2;
  display.setCursor(scrollX, y);
  display.print(m.text);
  display.display();
}

// Load configuration from SPIFFS (/config.json)
void loadLocalConfig() {
  SPIFFS.begin(true);
  File f = SPIFFS.open("/config.json", "r");
  if (!f) {
    Serial.println("[CFG] No local config found");
    return;
  }
  StaticJsonDocument<8192> doc;
  deserializeJson(doc, f);
  f.close();

  // Read fields, use defaults if missing
  cfg.wifiSsid       = doc["wifiSsid"]       | "";
  cfg.wifiPassword   = doc["wifiPassword"]   | "";
  cfg.ntpServer      = doc["ntpServer"]      | "";
  cfg.serverDomain   = doc["serverDomain"]   | "";
  cfg.deviceId       = doc["deviceId"]       | 0;
  cfg.deviceToken    = doc["deviceToken"]    | "";
  cfg.updateInterval = doc["updateIntervalSeconds"] | 30;
  cfg.rotateInterval = doc["rotateIntervalSeconds"] | 10;

  // Parse array of messages
  cfg.messages.clear();
  if (doc.containsKey("messages") && doc["messages"].is<JsonArray>()) {
    for (JsonVariant v : doc["messages"].as<JsonArray>()) {
      JsonObject o = v.as<JsonObject>();
      Msg m;
      m.text     = o["text"].as<const char*>();
      m.fontSize = o["fontSize"] | 1;
      m.scroll   = o["scroll"]   | false;
      cfg.messages.push_back(m);
    }
  }

  // Print out loaded config for debugging
  Serial.println("[CFG] --- Initial Configuration ---");
  Serial.printf("  SSID:               %s\n", cfg.wifiSsid.c_str());
  Serial.printf("  Password:           %s\n", cfg.wifiPassword.c_str());
  Serial.printf("  NTP Server:         %s\n", cfg.ntpServer.c_str());
  Serial.printf("  Server Domain:      %s\n", cfg.serverDomain.c_str());
  Serial.printf("  Device ID:          %d\n", cfg.deviceId);
  Serial.printf("  Device Token:       %s\n", cfg.deviceToken.c_str());
  Serial.printf("  Update Interval:    %ld s\n", cfg.updateInterval);
  Serial.printf("  Rotate Interval:    %ld s\n", cfg.rotateInterval);
  Serial.printf("  Message Count:      %u\n", (unsigned)cfg.messages.size());
  for (unsigned i = 0; i < cfg.messages.size(); ++i) {
    const Msg &m = cfg.messages[i];
    Serial.printf("    [%u] \"%s\", fontSize=%d, scroll=%s\n",
                  i,
                  m.text.c_str(),
                  m.fontSize,
                  m.scroll ? "true" : "false");
  }
  Serial.println("[CFG] ------------------------------");
}

// Connect to Wi-Fi with timeout
void connectWifi() {
  Serial.printf("[WIFI] Connecting to SSID '%s'...\n", cfg.wifiSsid.c_str());
  WiFi.begin(cfg.wifiSsid.c_str(), cfg.wifiPassword.c_str());
  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - start < 15000) {
    delay(200);
    Serial.print(".");
  }
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println();
    Serial.printf("[WIFI] Connected! IP address: %s\n",
                  WiFi.localIP().toString().c_str());
  } else {
    Serial.println();
    Serial.println("[WIFI] Connection failed after 15s");
  }
}

// Parse JSON response and update cfg.messages, intervals
void handleResponse(HTTPClient& http) {
  String body = http.getString();
  StaticJsonDocument<8192> doc;
  auto err = deserializeJson(doc, body);
  if (err) {
    Serial.printf("[JSON] Deserialization failed: %s\n", err.c_str());
    return;
  }
  cfg.updateInterval = doc["updateIntervalSeconds"] | cfg.updateInterval;
  cfg.rotateInterval = doc["rotateIntervalSeconds"]  | cfg.rotateInterval;
  cfg.messages.clear();
  for (JsonVariant v : doc["messages"].as<JsonArray>()) {
    JsonObject o = v.as<JsonObject>();
    Msg m;
    m.text     = o["text"].as<const char*>();
    m.fontSize = o["fontSize"] | 1;
    m.scroll   = o["scroll"]   | false;
    cfg.messages.push_back(m);
  }
  Serial.printf("[CONFIG] %u messages loaded\n", (unsigned)cfg.messages.size());
}

// Follow HTTP redirect to new location
void fetchConfigFromLocation(const String& location) {
  HTTPClient http;
  http.setFollowRedirects(HTTPC_STRICT_FOLLOW_REDIRECTS);
  WiFiClientSecure clientSecure;
  clientSecure.setInsecure();

  Serial.printf("[HTTP] FOLLOW-GET %s\n", location.c_str());
  bool begun = false;
  if (location.startsWith("https://")) begun = http.begin(clientSecure, location);
  else if (location.startsWith("http://")) begun = http.begin(location);
  if (!begun) return;

  http.addHeader("X-Device-Token", cfg.deviceToken);
  int code = http.GET();
  if (code == HTTP_CODE_OK) {
    handleResponse(http);
  } else {
    Serial.printf("[HTTP] Follow-up request failed: %d\n", code);
  }
  http.end();
}

// Fetch configuration from server, try HTTPS then HTTP, handle redirects
void fetchRemoteConfig() {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("[HTTP] Skipping fetch: no Wi-Fi connection");
    return;
  }

  HTTPClient http;
  http.setFollowRedirects(HTTPC_STRICT_FOLLOW_REDIRECTS);
  WiFiClientSecure clientSecure;
  clientSecure.setInsecure();

  String endpoint = "/api/config/" + String(cfg.deviceId);
  String urls[2] = {
    "https://" + cfg.serverDomain + endpoint,
    "http://"  + cfg.serverDomain + endpoint
  };

  for (int i = 0; i < 2; ++i) {
    const String& url = urls[i];
    Serial.printf("[HTTP] GET %s\n", url.c_str());

    bool begun = (url.startsWith("https://"))
                 ? http.begin(clientSecure, url)
                 : http.begin(url);
    if (!begun) {
      Serial.println("[HTTP] begin() failed");
      continue;
    }

    http.addHeader("X-Device-Token", cfg.deviceToken);
    int httpCode = http.GET();
    Serial.printf("[HTTP] Response code: %d\n", httpCode);

    if (httpCode == HTTP_CODE_OK) {
      handleResponse(http);
      http.end();
      return;
    }
    else if (httpCode == HTTP_CODE_MOVED_PERMANENTLY
          || httpCode == HTTP_CODE_FOUND) {
      String location = http.getLocation();
      Serial.printf("[HTTP] Redirect to %s\n", location.c_str());
      http.end();
      fetchConfigFromLocation(location);
      return;
    }

    http.end();
    Serial.println("[HTTP] Request failed, trying next protocol");
  }

  Serial.println("[HTTP] All attempts failed");
}

// Initialize NTP time synchronization
void setupTime() {
  configTime(0, 0, cfg.ntpServer.c_str());
}

// Initialize I²C and OLED display
void initDisplay() {
  Wire.begin(SDA_PIN, SCL_PIN);
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
  display.clearDisplay();
}

void setup() {
  Serial.begin(115200);
  delay(100);
  Serial.printf("[FW] Version %s\n", FW_VERSION);

  loadLocalConfig();
  if (cfg.wifiSsid.isEmpty()) while (true) delay(1000);  // halt if no config

  connectWifi();
  setupTime();
  fetchRemoteConfig();
  initDisplay();

  // initialize timers
  lastUpdate      = millis();
  lastRotate      = millis();
  lastScroll      = millis();
  scrollX         = SCREEN_WIDTH;
  staticNeedsDraw = true;
}

void loop() {
  if (cfg.messages.empty()) return;  // nothing to display
  unsigned long now = millis();

  // periodically fetch new config
  if (now - lastUpdate >= cfg.updateInterval * 1000UL) {
    fetchRemoteConfig();
    lastUpdate = now;
  }

  Msg &cur = cfg.messages[currentIdx];

  if (cur.scroll) {
    // scrolling text: update position and redraw
    if (now - lastScroll >= 50) {
      scrollX--;
      showScroll(cur);
      lastScroll = now;
    }
    // once text has fully scrolled off-screen, move to next
    int textW = cur.text.length() * 6 * cur.fontSize;
    if (scrollX < -textW) {
      currentIdx      = (currentIdx + 1) % cfg.messages.size();
      scrollX         = SCREEN_WIDTH;
      lastRotate      = now;
      staticNeedsDraw = true;
    }
  } else {
    // static text: draw once per rotation
    if (staticNeedsDraw) {
      showStatic(cur);
      staticNeedsDraw = false;
      lastRotate      = now;
    }
    // rotate to next message after interval
    if (now - lastRotate >= cfg.rotateInterval * 1000UL) {
      currentIdx      = (currentIdx + 1) % cfg.messages.size();
      staticNeedsDraw = true;
      scrollX         = SCREEN_WIDTH;
      lastScroll      = now;
    }
  }
}
