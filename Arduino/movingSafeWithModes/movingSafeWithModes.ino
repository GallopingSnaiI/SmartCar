#include <Smartcar_sensors.h>
#include <Smartcar.h>
#include <Wire.h>

Smartcar bob;
Odometer encoder;
Sonar sonar;

const int trig_pin = 43;
const int echo_pin = 42;
const int odo_pin = 19;
volatile int counter = 0;

int i;

String mode = "Idle";
  
void setup() 
{
   Serial.begin(4800);
   bob.begin();
   sonar.attach(trig_pin, echo_pin);
   encoder.attach(odo_pin);
}

void loop() 
{
  
  if (mode.equals("Idle")){
    //Serial.read();
  if(Serial.available() > 0){
    if (Serial.peek() != '$') {
      mode = "Auto";
      Serial.println(mode);
      autoMode();
    }
    else if (Serial.peek() == '$'){
      mode = "Manual";
      Serial.println(mode);
      manualMode();
    }
  }
  }
      
}

void manualMode() {
  Serial.readStringUntil('$');
  delay(1000);
  mode = "Idle";
  
}
  

void autoMode() {
  if(Serial.available() > 0){ 
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
      
      if(instr.equals("goForward")){
          goForwardSafe(value);
      }else if(instr=="rotateClockwise"){
          bob.rotateClockwise(value);
      }else if(instr=="rotateCounterClockwise"){
          bob.rotateCounterClockwise(value);
      }
    }
    mode = "Idle";
    Serial.println(mode);
}
}

void goForwardSafe(int desiredDistance)
{
  encoder.begin();
  
  while(encoder.getDistance() < desiredDistance && isFrontClear())
  {
    bob.goForward();
  }
  
  brake();
}

boolean isFrontClear()
{
  int distance = sonar.getDistance();
  
  if(distance < 25 && distance != 0)
  {
    counter++;
  }
  
  if(counter >= 3)
  {
    Serial.println("Obstacle detected");
    counter = 0;
    mode = "Idle";
    Serial.println(mode);
    return false;
  }
  
  return true;
}

void brake()
{
  bob.stop();
  delay(50);
  bob.goBackward();
  delay(75);
  bob.stop();
}
