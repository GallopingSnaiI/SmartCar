#include <Smartcar_sensors.h>
#include <Wire.h>

Sonar sonar;

const int trig_pin = 43;
const int echo_pin = 42;
int counter = 0;

void setup() 
{
  Serial.begin(4800);
  sonar.attach(trig_pin, echo_pin);
}

void loop()
{  
  if(detectObstacle())
  {
    Serial.println("Car stop.");
  }
  else
  {
    Serial.println("Going forward.");
  }
}

boolean detectObstacle()
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
