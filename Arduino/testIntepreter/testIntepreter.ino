#include <Wire.h>

int arraylength;
int k;

void setup() {
  Serial.begin(9600);
}

void loop(){
  
  //initialize accommodation
  if(Serial.available() > 0){
    arraylength = Serial.readStringUntil('!').toInt();
    
    String instr[arraylength];
    int para[arraylength];
    
    inteprete(instr,para);
    execute(instr,para);
    }}
    
 void inteprete(String instr[], int para[]){
   String ins;
   int par;
   int k = 0;
    //Serial.println(arraylength);
    while(k < arraylength){
      if(Serial.available() > 0){
        
      //inteprete instruction
      ins = Serial.readStringUntil(' ');
        
      if(ins.equals("goForward")){
        instr[k] = "F";
      }else if(ins.equals("rotateClockwise")){
        instr[k] = "R";
      }else if(ins.equals("rotateCounterClockwise")){
        instr[k] = "L";
      }else{
        Serial.print("Oops, it seems that some unexpected instruction at ");
        Serial.print(k);
        Serial.println(" has been send.");
      }
      //store patameter
      par = Serial.readStringUntil('*').toInt();
      para[k] = par;
    }
  }
}
    
 void execute(String instr[], int para[]){
  String i= instr[0];
         Serial.print(i);
         int p = para[0];
         Serial.println(p); 
 }
