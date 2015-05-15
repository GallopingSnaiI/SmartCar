#include <Wire.h>
#include <Smartcar_sensors.h>

//sonar and its pin
Sonar sonar;
const int trig_pin = 43;
const int echo_pin = 42;

int counter = 0;
boolean frontIsClear = true;
String mode = "Idle";
//String str = "Enter mode: ";

void setup() {
  Serial.begin(4800);
  sonar.attach(trig_pin, echo_pin);
}

void loop() {
  //int distance = sonar.getDistance();
  //Serial.println(distance);
  //Serial.print("counter: ");
  //Serial.println(counter);
  Serial.println(mode);
  
  if(mode == "Idle")
  {
    if(Serial.available() > 0)
    {
      reset();
      
      if(Serial.peek() == '$')
      {
        Serial.readStringUntil('$');
        mode = "Manual";
      }
      else
      mode = "Auto";
    }
  
  if(mode == "Auto")
  {
    String s = Serial.readString();
    
    while(frontIsClear)
    {
      scan();
      
      Serial.println(": Going forward.\n");
    }
    
    Serial.println(": Obstacle detected. Car stop.\n");
    mode = "Idle";
  }
  else if(mode == "Manual")
  {    
    while(true)
    {
      if(Serial.available() > 0)
      {
        if(Serial.peek() == '@')
        {
          Serial.readStringUntil('@');
          Serial.println("Manual mode end, turn back into Idle mode.");
          mode = "Idle";
          break;
        }
        else
        Serial.readString();
      }
      Serial.println(": Waiting for manual control.");
    }
  }
}
}
  
void scan()
{
  int distance = sonar.getDistance();
//  Serial.println("hej");
  if(distance < 25 && distance != 0)
  {
    Serial.print("Obstacle detected at: ");
    Serial.println(distance);
    counter++;
  }
  
  if(counter >= 3){
    frontIsClear = false;
    counter = 0;
  }
}

void reset()
{
  counter = 0;
  frontIsClear = true;
}
