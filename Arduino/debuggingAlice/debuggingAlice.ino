#include <Smartcar_sensors.h>
#include <Smartcar.h>
#include <Wire.h>

Smartcar alice;

Odometer encoder;
const int odo_pin = 19;

Sonar sonar;
const int trig_pin = 43;
const int echo_pin = 42;

volatile int counter = 0;
int i;
boolean obstacle;

String mode;
  
void setup(){
   Serial.begin(9600);
   alice.begin();
   alice.setAutomationRotationSpeed(100);
   sonar.attach(trig_pin, echo_pin);
   encoder.attach(odo_pin);
}

void loop(){
  Serial.println("Car waiting");
  while(true){    
    if(Serial.available() > 0){
      if (Serial.peek() == '$'){
        mode = "Manual";
        break;
      }else{
        mode = "Auto";
        break;
      }
    }
  }
  if(mode.equals("Auto")){
    autoMode();
  }else if(mode.equals("Manual")){
    manualMode();
  }   
}

void manualMode() {
  Serial.println("Manual mode entered, waiting for manual control.");
  Serial.readStringUntil('$');
  
  while(true){
    if(Serial.available() > 0){
        if(Serial.peek() == '@'){
          Serial.readStringUntil('@');
          Serial.println("Manual mode end, turn back into idle mode.");
          //mode = "Idle";
          break;
        }
        else
        Serial.readString();
      }
    }
  }
  

void autoMode(){
  Serial.println("Auto mode entered.");
  if(Serial.available() > 0){ 
    //initialize a queue as accommodation
    int arraylength = Serial.readStringUntil('!').toInt();
    String queue[arraylength];
    int k = 0;
        
    //store instructions in queue
    while(k<arraylength){
      if(Serial.available() > 0){
        queue[k] = Serial.readStringUntil('*');
        k++;
      }
    }
    
    //interpret and excute instructions
    obstacle = false;
    for(i = 0; i<arraylength && !obstacle; i++)
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
      
      if(instr.equals("goForward")){
          goForwardSafe(value);
      }else if(instr=="rotateClockwise"){
          alice.rotateClockwise(value);
      }else if(instr=="rotateCounterClockwise"){
          alice.rotateCounterClockwise(value);
      }
    }
    //mode = "Idle";
    //Serial.println(mode);
    //Serial.read();
}
}

void goForwardSafe(int desiredDistance){
  encoder.begin();
  
  while(encoder.getDistance() < desiredDistance && isFrontClear()){
    alice.goForward();
  }
  
  brake();
}

boolean isFrontClear(){
  int distance = sonar.getDistance();
  
  if(distance < 25 && distance != 0){
    counter++;
  }
  
  if(counter >= 3){
    String str = "Obstacle ";
    str += i;
    Serial.println(str);
    
    counter = 0;
    obstacle = true;
    //mode = "Idle";
    //Serial.println(mode);
    //Serial.read();
    return false;
  }
  
  return true;
}

void brake(){
  alice.stop();
  delay(50);
  alice.goBackward();
  delay(75);
  alice.stop();
}
