#include <Wire.h>
#include <Smartcar.h>
#include <Smartcar_sensors.h>
#include <SoftwareSerial.h>

//SoftwareSerial bluetooth(50,51);
Odometer encoder;
Smartcar alice;
Sonar sonar;

const int trig_pin = 43;
const int echo_pin = 42;
const int odo_pin = 19;
int counter = 0;
static int i;
boolean frontIsClear = true;

void setup() 
{
  alice.begin();
  Serial2.begin(9600);
  sonar.attach(trig_pin, echo_pin);
  encoder.attach(odo_pin);
}

void loop() 
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

void goForwardSafe(int desiredDistance)
{
  encoder.begin();
  
  while(encoder.getDistance() < desiredDistance)
  {
    scan();
    int dis = encoder.getDistance();
    Serial2.print(dis);
    
    if(!frontIsClear)
    {
      brake();
      Serial2.print("Obstacle Detected ");
      Serial2.println(++i);
      
      //make alice stand still, just for test.
      while(true)
        alice.stop();
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
  delay(100);
}
