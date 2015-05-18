#include <Wire.h>
#include <Smartcar.h>
#include <Smartcar_sensors.h>
#include <SoftwareSerial.h>

Smartcar alice;

//sonar and its pin
Sonar sonar;
const int trig_pin = 43;
const int echo_pin = 42;

//odometer and its pin
Odometer encoder;
const int odo_pin = 19;

//global variables
int counter = 0;
int i;
String mode;

void setup() 
{
  Serial.begin(9600);
  sonar.attach(trig_pin, echo_pin);
  encoder.attach(odo_pin);
  mode = "Idle";
}

void loop() 
{
  /*Idle state
   *switch modes between auto and manual according to serial input.
   */
  if(mode == "Idle")
  {
    if(Serial.available() > 0)
    {      
      if(Serial.peek() == '$')
      {
        Serial.readStringUntil('$');
        mode = "Manual";
      }
      else
      mode = "Auto";
    }
  }
  
  //Auto mode.
  if(mode == "Auto")
  {
    Serial.println("Anto mode entered.");
    if(Serial.available() > 0)
    { 
    //initialize a queue as accommodation
    int arraylength = Serial.readStringUntil('!').toInt();
    Serial.print("length = ");
    Serial.println(arraylength);
    String queue[arraylength];
    int k = 0;
        
    //store instructions in queue
    while(k<arraylength){
      if(Serial.available() > 0)
      {
        queue[k] = Serial.readStringUntil('*');
        Serial.print(k);
        Serial.print(": ");
        Serial.println(queue[k]);
        k++;
      }
    }
    
    //interpret and excute instructions
    for(i = 0; i<arraylength; i++)
    {
      String instr = "";
      String parameter = "";
      boolean spaceFound = false;
      
      for(int j = 0; j<queue[i].length(); j++)
      {
          if(queue[i].charAt(j)==' '){
            spaceFound = true;
          }else if(spaceFound){
            parameter += queue[i].charAt(j);
          }else{
            instr += queue[i].charAt(j); 
          }
        }
      
      //excute instruction
      int value = parameter.toInt();
      Serial.print(instr);
      Serial.println(value);
      
      if(instr.equals("goForward")){
        Serial.println("hej!");
        goForwardSafe(value);
      }else if(instr=="rotateClockwise"){
        alice.rotateClockwise(value);
      }else if(instr=="rotateCounterClockwise"){
        alice.rotateCounterClockwise(value);
      }
    }
   }
}

else if(mode == "Manual")
  {
    Serial.println("Manual mode entered, waiting for manual control.");
    
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
    }
  }
}

void goForwardSafe(int desiredDistance){
  encoder.begin();
  
  while(encoder.getDistance() < desiredDistance && isFrontClear()){
    alice.goForward();
  }
  brake();  
  mode = "Idle";  
}

boolean isFrontClear(){
  int distance = sonar.getDistance();
  
  if(distance < 25 && distance != 0){
    counter++;
  }
  
  if(counter >= 3){
    Serial.print("Obstacle Detected at ");
    Serial.println(++i);
    counter = 0;
    return false;
  }
  return true;
}

void brake()
{
  alice.stop();
  delay(50);
  alice.goBackward();
  delay(75);
  alice.stop();
}
