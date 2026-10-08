# Android Sensor Parallax

An experimental Android app I built to play around with hardware sensors and Jetpack Compose. It takes a 2D image and turns it into a 3D floating card that tilts in real-time as you physically move your phone.

https://github.com/user-attachments/assets/6fa15a49-861f-4fff-903b-41411a7d9f60

### What it does
* **Hardware-tracked tilting:** Uses the phone's gravity sensor to calculate pitch and roll, mapped directly to the UI.
* **Custom reflection shader:** I wrote an AGSL (Android Graphics Shading Language) runtime shader that calculates a light source based on the phone's angle, creating a metallic/glass gloss effect on the card.
* **Live physics tweaking:** I built a bottom sheet control panel so you can adjust the shadow drop, blur, parallax depth, and gloss intensity on the fly.
* **Smooth rendering:** Added a basic low-pass filter to the raw sensor data so the 60fps Compose animations don't jitter.

### Tech Stack
* Kotlin & Jetpack Compose
* Android `SensorManager` (Gravity)
* AGSL (RuntimeShader) for custom lighting
* Coil for image loading
* MVVM architecture to separate the sensor listeners from the UI state

### To Run It
Clone the repo and open it in Android Studio. **Note:** You'll need to run this on a physical Android device to actually see the tilt effect, as the emulator doesn't simulate gravity sensors properly without extra setup.



