FROM gradle:jdk24 AS builder

WORKDIR /home/gradle/project

USER root
RUN apt-get update \
 && apt-get install -y curl unzip \
 && rm -rf /var/lib/apt/lists/* \
 && curl -fsSL https://downloads.arduino.cc/arduino-cli/arduino-cli_latest_Linux_64bit.tar.gz \
      | tar -xz -C /usr/local/bin \
 && arduino-cli config init --overwrite \
 && arduino-cli core update-index \
      --additional-urls https://espressif.github.io/arduino-esp32/package_esp32_index.json \
 && arduino-cli core install esp32:esp32 \
 && arduino-cli lib install "ArduinoJson@7.4.2" \
 && arduino-cli lib install "Adafruit GFX Library@1.12.0" \
 && arduino-cli lib install "Adafruit SSD1306@2.5.13" \
 && arduino-cli lib install "Adafruit BusIO@1.17.0"

COPY . .

WORKDIR /home/gradle/project/firmware
RUN arduino-cli compile --fqbn esp32:esp32:esp32 --export-binaries . \
 && mv build/esp32.esp32.esp32/firmware.ino.bin firmware.bin \
 && mv build/esp32.esp32.esp32/firmware.ino.bootloader.bin bootloader.bin \
 && mv build/esp32.esp32.esp32/firmware.ino.partitions.bin partitions.bin \
 && mkdir -p /home/gradle/project/src/main/resources/static/esp-web \
 && cp firmware.bin bootloader.bin partitions.bin /home/gradle/project/src/main/resources/static/esp-web/

WORKDIR /home/gradle/project
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:24-jdk

RUN apt-get update \
 && apt-get install -y curl unzip \
 && rm -rf /var/lib/apt/lists/* \
 && curl -fsSL https://downloads.arduino.cc/arduino-cli/arduino-cli_latest_Linux_64bit.tar.gz \
      | tar -xz -C /usr/local/bin

RUN arduino-cli config init --overwrite \
 && arduino-cli core update-index \
      --additional-urls https://espressif.github.io/arduino-esp32/package_esp32_index.json \
 && arduino-cli core install esp32:esp32

RUN ln -s /root/.arduino15/packages/esp32/tools/mkspiffs/*/mkspiffs /usr/local/bin/mkspiffs

ENV PATH="/usr/local/bin:${PATH}"

WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
