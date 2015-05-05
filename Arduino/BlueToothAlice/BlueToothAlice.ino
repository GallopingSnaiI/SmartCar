#include <Smartcar.h>
#include <Wire.h>
#include <SoftwareSerial.h>

SoftwareSerial bluetooth(50,51); 
Smartcar alice;
//int arraylength;

void setup() 
{
 alice.begin();
 Serial2.begin(9600);
}

void loop() 
{
  if(Serial2.available() > 0)
  { //initialize a queue as accommodation
    int arraylength = Serial2.readStringUntil('!').toInt();
    String queue[arraylength];
    int i = 0;
    
    //store instructions in queue
    while(Serial2.available() > 0 && i<arraylength)
    {
      //if(){
        queue[i] = Serial2.readStringUntil('*');
        i++;
      //}
    }
    for(int i = 0; i<arraylength; i++)
    {
        String instr = "";
        String parameter = "";
        boolean spaceFound = false;
        
        //interpret instruction
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
      int value = parameter.toInt();
      
      //excute instruction
      if(instr.equals("goForward")){
          alice.goForward(value);
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
