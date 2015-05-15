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
int counter;
boolean frontIsClear;
static int i;
String mode;

void setup() 
{
  alice.begin();
  Serial2.begin(9600);
  sonar.attach(trig_pin, echo_pin);
  encoder.attach(odo_pin);
  reset();
  mode = "Idle";
}

void loop() 
{
  //Idle state
  if(mode == "Idle")
  {
    if(Serial2.available() > 0)
    {
      reset();
      
      if(Serial2.peek() == '$')
      {
        Serial2.readStringUntil('$');
        mode = "Manual";
      }
      else
      mode = "Auto";
    }
  }
  
  //Auto mode.
  if(mode == "Auto")
  {
    if(Serial2.available() > 0)
   { 
    //initialize a queue as accommodation
    int arraylength = Serial2.readStringUntil('!').toInt();
    String queue[arraylength];
    int k = 0;
        
    //store instructions in queue
    while(k<arraylength){
      if(Serial2.available() > 0)
      {
        queue[k] = Serial2.readStringUntil('*');
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
      
      //print instruction
      Serial2.println(instr);
      
      //excute instruction
      int value = parameter.toInt();
      
      if(instr.equals("goForward")){
          goForwardSafe(value);
      }else if(instr=="rotateClockwise"){
          alice.rotateClockwise(value);
      }else if(instr=="rotateCounterClockwise"){
          alice.rotateCounterClockwise(value);
      }else{
          //r2d2.rotateClockwise(360);
      }
    }
   }
}

else if(mode == "Manual")
  {
    Serial2.println("Manual mode entered, waiting for manual control.");
    
    while(true)
    {
      if(Serial2.available() > 0)
      {
        if(Serial2.peek() == '@')
        {
          Serial2.readStringUntil('@');
          Serial2.println("Manual mode end, turn back into Idle mode.");
          mode = "Idle";
          break;
        }
        else
        Serial2.readString();
      }
    }
  }
}

//reset the global variables
void reset()
{
 counter = 0;
 frontIsClear = true; 
}

void goForwardSafe(int desiredDistance)
{
  encoder.begin();
  
  while(encoder.getDistance() < desiredDistance)
  {
    scan();
    int dis = encoder.getDistance();
    //Serial2.print(dis);
    
    if(!frontIsClear)
    {
      brake();
      mode = "Idle";
      Serial2.print("Obstacle Detected ");
      Serial2.println(++i);
      break;
    }
    
    alice.goForward();
  }
}

void scan()
{
  int distance = sonar.getDistance();
  
  if(distance < 25 && distance != 0)
    counter++;
  
  if(counter >= 3)
  {
    //counter = 0;
    frontIsClear = false;
  }
}

void brake()
{
  alice.stop();
  delay(50);
  alice.goBackward();
  delay(50);
  alice.stop();
}

