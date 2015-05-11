#include <Wire.h>
#include <Smartcar_sensors.h>

//sonar and its pin
Sonar sonar;
const int trig_pin = 43;
const int echo_pin = 42;

int counter = 0;
boolean frontIsClear = true;
char mode = 'I';
//String str = "Enter mode: ";

void setup() {
  Serial.begin(4800);
  sonar.attach(trig_pin, echo_pin);
}

void loop() {
  int distance = sonar.getDistance();
  Serial.println(distance);
  Serial.print("counter: ");
  Serial.println(counter);
  Serial.print(mode);
  
  switch(mode){
    default:
    mode = 'A';
    break;
    
    case 'A':
    scan();
    
    if(frontIsClear)
    Serial.println(": Going forward.\n");
    else{
      Serial.println(": Obstacle detected. Car stop.\n");
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
//  Serial.println("hej");
  if(distance < 25 && distance != 0){
    Serial.print("Obstacle detected at: ");
    Serial.println(distance);
    counter++;
  }
  
  if(counter >= 3){
    frontIsClear = false;
  }
}
