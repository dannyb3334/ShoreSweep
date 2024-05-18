# Requirements
- Required < Android v.11
- Opencv 3.9
- Physicaloid [https://github.com/ksksue/PhysicaloidLibrary]

## How it works?
1. Color detection perfomed with opencv. Resultant mask is translated into points and recognized as a distance to the camera.
    *Developed a user GUI HSV color adjustment tool to adjust color detection accuracy.
2. Based on the closeness of an object, preprogrammed controlls will be executed.
3. Arduino 1 listens for Android command via serial port. 
4. Arduino 2 executes commands via tether to Arduino 1 and motor controllers.
