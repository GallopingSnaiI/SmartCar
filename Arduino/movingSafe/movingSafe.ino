#include <Smartcar_sensors.h>
#include <Smartcar.h>
#include <Wire.h>

Smartcar bob;
Odometer encoder;
Sonar sonar;

const int trig_pin = 43;
const int echo_pin = 42;
const int odo_pin = 19;
volatile int counter = 0;
  
void setup() 
{
   Serial.begin(4800);
   bob.begin();
   sonar.attach(trig_pin, echo_pin);
   encoder.attach(odo_pin);
}

void loop() 
{
  if(Serial.available() > 0)
  { int dis = Serial.readStringUntil('*').toInt();
    goForwardSafe(dis);
  }
}

void goForwardSafe(int desiredDistance)
{
  encoder.begin();
  
  while(encoder.getDistance() < desiredDistance && isFrontClear())
  {
    bob.goForward();
  }
  
  brake();
}

boolean isFrontClear()
{
  int distance = sonar.getDistance();
  
  if(distance < 25 && distance != 0)
  {
    counter++;
  }
  
  if(counter >= 3)
  {
    Serial.println("Obstacle detected");
    counter = 0;
    return false;
  }
  
  return true;
}

void brake()
{
  bob.stop();
  delay(50);
  bob.goBackward();
  delay(75);
  bob.stop();
}
