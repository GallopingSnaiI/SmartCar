#include <Smartcar.h>
#include <Smartcar_sensors.h>
#include <Wire.h>
#include <SoftwareSerial.h>

Smartcar alice;

Odometer encoder;
const int odo_pin = 19;

Sonar sonar;
const int trig_pin = 43;
const int echo_pin = 42;

//Global variables
volatile int counter;
boolean obstacle;
int index;
String mode;
  
void setup(){
   Serial2.begin(9600);   
   alice.begin();
   sonar.attach(trig_pin, echo_pin);
   encoder.attach(odo_pin);
}

void loop(){
  
  //Idle state.
  while(true){    
    if(Serial2.available() > 0){
      if (Serial2.peek() == '$'){
        mode = "Manual";
        break;
      }else{
        mode = "Auto";
        reset();
        break;
      }
    }
  }
  
  //switch between manual mode and auto mode according to data received.
  if(mode.equals("Auto")){
    autoMode();
  }else if(mode.equals("Manual")){
    manualMode();
  }   
}

//manual mode
void manualMode(){
  
  alice.setAutomationRotationSpeed(150);
  
  String instr;
  
  //main loop
  while(true){    
    
    if(Serial2.available() > 0){
      
      //validate exit condition and filter out trash information  
      if(Serial2.peek() == '@'){
          Serial2.readStringUntil('@');
          break;
        }else if(Serial2.peek() == '$'){
          Serial2.readStringUntil('$');
          instr = Serial2.readStringUntil('*');
        }else{
          Serial2.read();
        }
      }
      
      //execute manual commandes.
      if(instr.equals("goForward")){
        alice.goForward();
      }else if(instr.equals("goBackward")){
        alice.goBackward();
      }else if(instr.equals("rotateClockwise")){
        alice.rotateClockwise(1);
      }else if(instr.equals("rotateCounterClockwise")){
        alice.rotateCounterClockwise(1);
      }else{
        alice.stop();
      }
    }
  }

//path following mode  
void autoMode(){
  
  alice.setAutomationRotationSpeed(100);
  
  if(Serial2.available() > 0){ 
    
    //initialize a queue as accommodation
    int arraylength = Serial2.readStringUntil('!').toInt();
    String queue[arraylength];
    int k = 0;
        
    //store instructions in array
    while(k<arraylength){
      if(Serial2.available() > 0){
        queue[k] = Serial2.readStringUntil('*');
        k++;
      }
    }
    
    //intepret and excute instructions
    for(index = 0; index<arraylength && !obstacle; index++){
      
      //intrpret  
      String instr = "";
      String parameter = "";
      boolean spaceFound = false;
      
      for(int j = 0; j<queue[index].length(); j++){
        if(queue[index].charAt(j)==' '){
          spaceFound = true;
        }else if(spaceFound){
          parameter += queue[index].charAt(j);
        }else{
          instr += queue[index].charAt(j);
        }
      }
      
      //excute
      int value = parameter.toInt();
      
      if(instr.equals("goForward")){
        goForwardSafe(value);
      }else if(instr=="rotateClockwise"){
        alice.rotateClockwise(value);
      }else if(instr=="rotateCounterClockwise"){
        alice.rotateCounterClockwise(value);
      }
    }
  }
}

//go forward with detect obstacle function working
void goForwardSafe(int desiredDistance){
  encoder.begin();
  
  while(encoder.getDistance() < desiredDistance && isFrontClear()){
    alice.goForward();
  }
  
  brake();
}

//detect obstacle function.
boolean isFrontClear(){
  int distance = sonar.getDistance();
  
  if(distance < 25 && distance != 0){
    counter++;
  }
  
  if(counter >= 3){
    String str = "Obstacle ";
    str += index;
    Serial2.println(str);
    
    obstacle = true;
    return false;
  }
  
  return true;
}

//brake safe in front of the obstacle.
void brake(){
  alice.stop();
  delay(50);
  alice.goBackward();
  delay(75);
  alice.stop();
}

//reset variables.
void reset(){
  counter = 0;
  obstacle = false;
}
