# ESignageP32

A Spring Boot application that lets you flash an ESP32 in your browser and remotely manage the messages displayed on its connected OLED screen.

## Features

* Web interface with secure login
* Browser-based firmware flashing for the ESP32
* Remotely update and rotate text messages shown on the display

## Supported Hardware

* **Display**: Only the ADA1306 (128×64) OLED module is supported at this time.

## Requirements

* Docker
* Docker Compose

## Installation & Deployment

1. **Clone the repository**:

   ```bash
   git clone https://github.com/Yxeyuo/ESignageP32.git
   cd ESignageP32
   ```
2. **Configure environment variables**:

    * Create or open the `.env` file and set the following variables:

      ```dotenv
      # Postgres DB
      POSTGRES_USER=my_pg_user
      POSTGRES_PASSWORD=my_pg_pass
      POSTGRES_DB=meine_db
 
      # Host path for persistent data storage
      DB_DATA_PATH=./data/postgres
 
      # Default application login (change for production)
      APP_DEFAULT_USER_USERNAME=username
      APP_DEFAULT_USER_PASSWORD=password
      ```
3. **Start the backend service** using Docker Compose:

   ```bash
   docker-compose up -d
   ```
4. **Display wiring**:

    * The OLED display must use:

      ```c
      #define SDA_PIN 21  // I²C data line
      #define SCL_PIN 19  // I²C clock line
      ```

## Usage

1. **Initial settings**:

    * In your browser, navigate to `http://<your-domain>/settings`.
    * Enter your Wi‑Fi SSID, password, and the server domain or IP.
2. **Flash the ESP32 firmware**:

    * Connect the ESP32 to your PC via USB.
    * Open `http://<your-domain>/flash`.
    * Upload the firmware binary and device configuration to flash the ESP32.
3. **Manage messages**:

    * In the web UI, add, edit, or remove messages to be shown on the OLED display.
    * Configure the rotation order and polling frequency for message updates.

## Recommended Deployment

* **HTTPS Proxy**: Run behind an HTTPS proxy (e.g., NGINX, Traefik) to secure all endpoints.

## Known Issues & Security Warnings

* This project is under active development and may contain bugs or security vulnerabilities.
* Use caution when exposing network interfaces to untrusted environments.

