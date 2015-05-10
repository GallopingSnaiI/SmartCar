#include <Wire.h>
#include <Smartcar_sensors.h>

//sonar and its pin
Sonar sonar;
const int trig_pin = 43;
const int echo_pin = 42;

int counter = 0;
boolean frontIsClear = true;
char mode = 'I';

void setup() {
  Serial.begin(4800);
  sonar.attach(trig_pin, echo_pin);
}

void loop() {
  String str = "Enter mode: " + mode;
  Serial.println(str);
  
  switch(mode){
    default:
    mode = 'A';
    break;
    
    case 'A':
    scan();
    
    if(frontIsClear)
    Serial.println("Going forward.");
    else{
      Serial.println("Obstacle detected. Car stop.");
      mode = 'M';
    }
    
    break;
    
    case 'M':
    while(true){}
    break;
       
 }
}

void scan()
{
  int distance = sonar.getDistance();
  
  if(distance < 25 && distance != 0)
    counter++;
  
  if(counter >= 3)
    frontIsClear = false;
}
