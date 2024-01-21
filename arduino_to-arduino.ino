int incomingByte = 0;

int output_pin1 = 6;
int output_pin2 = 5;
int output_pin3 = 4;
int output_pin4 = 3;


void setup() {
  Serial.begin(9600);

  pinMode(output_pin1, OUTPUT);
  pinMode(output_pin2, OUTPUT);
  pinMode(output_pin3, OUTPUT);
  pinMode(output_pin4, OUTPUT);

}

void loop() {
  if (Serial.available() > 0) {
    incomingByte = Serial.read();

    // Serial.print(incomingByte);
    // 4 =52
    // 3 =51
    // 2 =50
    // 1 =49
    if (incomingByte == 52) {          // left on
      Serial.print("Foward");
      digitalWrite(output_pin1, HIGH);
      digitalWrite(output_pin2, LOW);
      digitalWrite(output_pin3, LOW);
      digitalWrite(output_pin4, LOW);

     
    } else if (incomingByte == 51) {   // right off
      Serial.print("left"); 
      digitalWrite(output_pin1, LOW);
      digitalWrite(output_pin2, HIGH);
      digitalWrite(output_pin3, LOW);
      digitalWrite(output_pin4, LOW); 
     
    } else if (incomingByte == 50) {   // both on
      Serial.print("Right");
      digitalWrite(output_pin1, LOW);
      digitalWrite(output_pin2, LOW);
      digitalWrite(output_pin3, HIGH);
      digitalWrite(output_pin4, LOW);
     
    } else if (incomingByte == 49) {   // both off
      Serial.print("both off");
      digitalWrite(output_pin1, LOW);
      digitalWrite(output_pin2, LOW);
      digitalWrite(output_pin3, LOW);
      digitalWrite(output_pin4, HIGH);
     
    }
  }
}
