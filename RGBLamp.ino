#include <stdint.h>
#include <SoftwareSerial.h>
#include <Adafruit_NeoPixel.h>

#define ON_START 'O'
#define OFF_START 'F'
#define COLOR_START 'C'
#define END_CHAR '!'

SoftwareSerial Bluetooth(10, 9);

uint8_t data[15];            //Variable for storing received data

int red = 128;
int green = 128;
int blue = 128;

int LEDPin = 6;

Adafruit_NeoPixel strip = Adafruit_NeoPixel(60, LEDPin, NEO_GRB + NEO_KHZ800);

void setup()
{
  Bluetooth.begin(9600);

  Serial.begin(9600);

  pinMode(LEDPin, OUTPUT);
}


void loop()
{
  bool dataRead = false;
  int index = 0;
  uint8_t chr = '`';
  while (Bluetooth.available() > 0)
  {
    data[index++] = Bluetooth.read();
    dataRead = true;
    delay(1);
  }

  if (dataRead)
  {
    index = 0;
    switch(data[0])
    {
    case ON_START:
      for(int ii = 0; ii < strip.numPixels(); ii++)
      {
        strip.setPixelColor(ii, red, green, blue);
      }
      strip.show();
      break;
    case OFF_START:
      for(int ii = 0; ii < strip.numPixels(); ii++)
      {
        strip.setPixelColor(ii, 0, 0, 0);
      }
      strip.show();
      break;
    case COLOR_START:
      red = data[1];
      green = data[2];
      blue = data[3];
      for(int ii = 0; ii < strip.numPixels(); ii++)
      {
        strip.setPixelColor(ii, red, green, blue);
      }
      strip.show();
      break;
    }

    while (data[index] != 0)
    {
      Serial.print(data[index++]);
      Serial.print(" ");
    }

    Bluetooth.write("d");
    Serial.print("\n");
    memset(data, 0, sizeof(uint8_t) * 15);
  }
}













