#include <Smartcar_sensors.h>
#include <Smartcar.h>
#include <Wire.h>

Smartcar bob;
Odometer encoder;
Sonar sonar;

const int trig_pin = 43;
const int echo_pin = 42;
int counter = 0;
  
void setup() 
{
   Serial.begin(4800);
   bob.begin();
   sonar.attach(trig_pin, echo_pin);
}

void loop() 
{
  goForwardSafe(1000);
}

void goForwardSafe(int desiredDistance)
{
  encoder.begin();
  
  while(encoder.getDistance() < desiredDistance && !frontIsClear())
  {
    //bob.goForward();
    Serial.println("bob going forward.");
  }
  
  while(true)
  {
    //bob.stop();
    Serial.println("bob has stopped.");
  }
}

boolean frontIsClear()
{
  int distance = sonar.getDistance();
  
  if(distance < 30 && distance != 0)
  {
    counter++;
  }
  
  if(counter >= 5)
  {
    Serial.println("Obstacle detected");
    counter = 0;
    return true;
  }
  
  return false;
}

