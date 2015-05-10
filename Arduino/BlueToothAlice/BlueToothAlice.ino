#include <Wire.h>
#include <Smartcar.h>
#include <Smartcar_sensors.h>
#include <SoftwareSerial.h>

SoftwareSerial bluetooth(50,51);
Odometer encoder;
Smartcar alice;
Sonar sonar;

const int trig_pin = 43;
const int echo_pin = 42;
int counter = 0;

void setup() 
{
  alice.begin();
  Serial2.begin(9600);
  sonar.attach(trig_pin, echo_pin);
}

void loop() 
{
  if(Serial2.available() > 0)
  { 
    //initialize a queue as accommodation
    int arraylength = Serial2.readStringUntil('!').toInt();
    String queue[arraylength];
    int i = 0;
           
    //store instructions in queue
    while(i<arraylength)
    {
      if(Serial2.available() > 0)
      {
        queue[i] = Serial2.readStringUntil('*');
        i++;
      }
    }
    
    //interpret and excute instructions
    for(int i = 0; i<arraylength; i++)
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
  
  while(encoder.getDistance() < desiredDistance && !frontIsClear())
  {
    //bob.goForward();
    Serial.println("bob going forward.");
  }
  
  //change when manual mode available
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
