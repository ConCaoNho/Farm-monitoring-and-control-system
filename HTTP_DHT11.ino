#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <DHT.h> 
#include "BluetoothSerial.h"

BluetoothSerial SerialBT;

#define DHTPIN 4
#define DHTTYPE DHT11 
DHT dht11(DHTPIN, DHTTYPE); 
#define LED_PIN  5
#define FAN_PIN 18

String URL = "http://172.20.10.8/BTL_DHT11/send_data.php";
const char* ssid = "Duong Tran"; 
const char* password = "11111111"; 

int temperature = 0;
int humidity = 0;

void setup() {
  Serial.begin(115200);

  dht11.begin(); 
  pinMode(LED_PIN, OUTPUT);
  pinMode(FAN_PIN, OUTPUT);
  // Bắt đầu Bluetooth với tên thiết bị
  SerialBT.begin("Duong_Tran_ESP"); 
  Serial.println("Bluetooth đã bật. Đang chờ kết nối...");
  connectWiFi();
}

void loop() {
  Load_DHT11_Data();
// Nếu WiFi chưa kết nối, thử kết nối lại
  if (WiFi.status() != WL_CONNECTED) {
    connectWiFi();
  }
  // Nếu có WiFi thì mới gửi dữ liệu và lấy lệnh
  if (WiFi.status() == WL_CONNECTED) {
    String postData = "temperature=" + String(temperature) + "&humidity=" + String(humidity);
    
    HTTPClient http;
    http.begin(URL);
    http.addHeader("Content-Type", "application/x-www-form-urlencoded");
    
    int httpCode = http.POST(postData);
    String payload = http.getString();

    Serial.print("URL : "); Serial.println(URL); 
    Serial.print("Data: "); Serial.println(postData);
    Serial.print("httpCode: "); Serial.println(httpCode);
    Serial.print("payload : "); Serial.println(payload);
    Serial.println("--------------------------------------------------");

    getControlCommand(); // lấy lệnh điều khiển từ server
    http.end();
  } else {
    Serial.println("Không có kết nối WiFi. Đang chỉ hiển thị dữ liệu cảm biến...");
  }
  // Gửi dữ liệu qua Bluetooth để app Android nhận
  String btData = "Temp:" + String(temperature) + "*C, Humi:" + String(humidity) + "%";
  SerialBT.println(btData);
  delay(500); // Delay để tránh đọc quá nhanh
}



void Load_DHT11_Data() {
  temperature = dht11.readTemperature(); //Celsius
  humidity = dht11.readHumidity();
  // Check if any reads failed.
  if (isnan(temperature) || isnan(humidity)) {
    Serial.println("Failed to read from DHT sensor!");
    temperature = 0;
    humidity = 0;
  }
  Serial.printf("Temperature: %d °C\n", temperature);
  Serial.printf("Humidity: %d %%\n", humidity);
}

void connectWiFi() {
  WiFi.mode(WIFI_OFF);
  delay(1000);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  Serial.println("Đang kết nối WiFi...");

  unsigned long startTime = millis();
  const unsigned long timeout = 5000; // 5 giây

  while (WiFi.status() != WL_CONNECTED && millis() - startTime < timeout) {
    delay(500);
    Serial.print(".");
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n Đã kết nối WiFi!");
    Serial.print("IP: "); Serial.println(WiFi.localIP());
  } else {
    Serial.println("\n Kết nối WiFi thất bại.");
  }
}
void getControlCommand() {
  HTTPClient http;
  http.begin("http://172.20.10.8/BTL_DHT11/get_control.php"); 
  int httpCode = http.GET();

  if (httpCode == 200) {
  String payload = http.getString();
  Serial.print("Control Payload: ");
  Serial.println(payload);

  // Phân tích JSON
  DynamicJsonDocument doc(256);  //512
  DeserializationError error = deserializeJson(doc, payload);

  if (!error) {
    int ledState = doc["led"];
    int fanState = doc["fan"];

    digitalWrite(LED_PIN, ledState == 1 ? HIGH : LOW);
    digitalWrite(FAN_PIN, fanState == 1 ? HIGH : LOW);

    Serial.printf("LED: %d, FAN: %d\n", ledState, fanState);
  } else {
    Serial.println("Lỗi phân tích JSON");
  }
}
  http.end();
} 