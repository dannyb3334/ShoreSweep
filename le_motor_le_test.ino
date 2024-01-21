int motorpin1 = 9;
int motorpin2 = 10;
int motorpin3 = 11;
int motorpin4 = 12;

int recieve_data = 2;
int recieve_data1 = 3;
int recieve_data2 = 4;
int recieve_data3 = 5;


int value;
int value1;
int value2;
int value3;




void setup() {
  pinMode (motorpin1, OUTPUT);
  pinMode (motorpin2, OUTPUT);
  pinMode (motorpin3, OUTPUT);
  pinMode (motorpin4, OUTPUT);

  pinMode(recieve_data, INPUT);
  pinMode(recieve_data1, INPUT);
  pinMode(recieve_data2, INPUT);
  pinMode(recieve_data3, INPUT);

  Serial.begin(9600);
}

void loop() {
  
  value = digitalRead(recieve_data);
  value1 = digitalRead(recieve_data1);
  value2 = digitalRead(recieve_data2);
  value3 = digitalRead(recieve_data3);

  Serial.println(value);

  if (value == HIGH){


   Serial.println("Right");

   digitalWrite (motorpin1, LOW);
   digitalWrite (motorpin2, HIGH);
   digitalWrite (motorpin3, LOW);
   digitalWrite (motorpin4, HIGH);

   

  }



  if (value1 == HIGH){
    Serial.println("left");
    digitalWrite (motorpin1, HIGH);
    digitalWrite (motorpin2, LOW);
    digitalWrite (motorpin3, HIGH);
    digitalWrite (motorpin4, LOW);

    
 }

  if (value2 == HIGH){
   Serial.println("Foward");
   digitalWrite (motorpin1, HIGH);
   digitalWrite (motorpin2, LOW);
   digitalWrite (motorpin3, LOW);
   digitalWrite (motorpin4, HIGH);

   
  }

  if (value3 == HIGH){

   Serial.println("off");

   digitalWrite (motorpin1, LOW);
   digitalWrite (motorpin2, LOW);
   digitalWrite (motorpin3, LOW);
   digitalWrite (motorpin4, LOW);

  
  }



  

  
}
