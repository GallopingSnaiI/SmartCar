#include <Smartcar.h>
#include <Wire.h>

Smartcar alice;

void setup() 
{
 // put your setup code here, to run once:
 alice.begin();
 Serial.begin(9000);
}

void loop() 
{
  // put your main code here, to run repeatedly:
  if(Serial && Serial.available() > 0)
  {
    int arraylength = Serial.readStringUntil('!').toInt();
    int a = arraylength * 2;
    String queue[a];
    
    int i = 0;
    while(Serial.available() > 0)
    {
      queue[i] = Serial.readStringUntil(' ');
      i++;
      queue[i] = Serial.readStringUntil('*');
      i++;
    }
    
    for(int j = 0; j < a; j += 2)
    {
      if(queue[j].equals("goForward"))
       alice.goForward(queue[j+1].toInt());
        
      else if(queue[j].equals("rotateClockwise"))
       alice.rotateClockwise(queue[j+1].toInt());
        
      else if(queue[j].equals("rotateCounterClocewise"))
       alice.rotateCounterClockwise(queue[j+1].toInt());
    }
}
}



