# ShoreSweep: Beach Cleaning with Color Detection and Distance Tracking  
Submitted to **UTRA HACKS 2024**, a 26-hour hackathon  
[Devpost Project Page](https://devpost.com/software/shoresweep)  

---

## Overview  
ShoreSweep is an innovative solution designed to automate beach cleaning using color detection and distance tracking. By leveraging OpenCV and Arduino integration, the system detects target objects (e.g., litter) based on their color, calculates their distance from the camera, and autonomously navigates to sweep them away.  

---

## Requirements  
- **Android OS**: Version < 11  
- **OpenCV**: Version 3.9  
- **Physicaloid Library**: [GitHub Repository](https://github.com/ksksue/PhysicaloidLibrary)  

---

## How It Works  

1. **Color Detection**  
   - The system uses OpenCV to perform color-based detection.  
   - A mask is generated to isolate target colors, which is translated into points representing the object’s location.  
   - Distance to the object is calculated from its size and position in the frame.  

   *User-friendly GUI for HSV color adjustment is included to fine-tune detection accuracy.*  

2. **Autonomous Controls**  
   - Based on the proximity of the detected object, preprogrammed motor controls are triggered.  

3. **Arduino Communication**  
   - **Arduino 1**: Receives commands from the Android device via a serial port using the Physicaloid library.  
   - **Arduino 2**: Acts as a motor controller, tethered to Arduino 1, to execute commands for object sweeping.  

---

## Key Features  
- Real-time color detection with adjustable HSV parameters.  
- Object distance tracking for proximity-based navigation.  
- Seamless integration of Android devices with Arduino hardware.  
- Efficient motor control for autonomous sweeping actions.  

---

## Demo  
Visit our [Devpost project page](https://devpost.com/software/shoresweep) for more details, code, and demo videos.
