**CLICK HERE -->** **[WIKI PAGE](http://socialledge.com/sjsu/index.php/S20:_Canster_Truck#Android_Application)**

**1. Main Activity**
* This is the app launch display screen
* It will check for all the required permission onStart().
* Has search button for connecting from a paired device list view
* Once device is clicked, an intent will start the Map Activity 

**2. Map Activity**
* For this requirement we are using the Google Maps Fragment, for the map fragment to function we must request an API key from Google and set the key in the manifest of the application.
* Destination marker(Red Marker) is set using <code>setOnMapClickListener</code> to which the car will navigate to, the destination can be changed by clicking elsewhere on the Map Fragment. The marker can be removed using <code>setOnMarkerClickListener</code>.
* When the start button is pressed, the start command identifier along with destination latitude and longitude is sent to the car via the write thread. A background thread is started along with it to indicate the current car's position(Green Marker) along with a plotline between the current position and destination.
* When the stop button is pressed, the stop command identifier is sent to the car, to stop and turn off the car.


**3. Info Activity**
* Messages from Geological, Driver, Motor coming from CAN bus are decoded by the Sensor and Bridge controller and sent to the Android application as a string. 
* The received string is parsed and categorizes the data to store it in the required textView to be displayed. 
* Bluetooth status, Lidar Values, Ultrasonic Sensor Values, Motor Speed, Motor RPM, Motor PWM, Cars' current location, Compass Heading, Distance till Destination, Checkpoint Index is displayed. This was useful for debugging purposes and allowed us to avoid scanning the mounted LCD or CAN Busmaster on PC during drives.

**4. Debug Activity**
* The main function of this activity is to check all RAW RX and RAW TX messages and create a log of all the data received and sent.


**5. Bluetooth Connection Service Activity**
* This is the background activity that handles all the threads required for transmission and receiving data using Bluetooth connections. It has 3 running threads which is called inside other activities using a handler:
* Accept Thread - Listens to BluetoothServerSocket using listenUsingRfcommWithServiceRecord. In order for the RF communication socket to connect to the HC-05, we used the following <code>UUID: 00001101-0000-1000-8000-00805F9B34FB</code>. This is a generic SSP Bluetooth UUID that enables the socket to directly connected to HC-05 and maintain the connection.
* Connect Thread - Creates a Bluetooth socket using createRfcommSocketToServiceRecord
* Connected Thread - Creates socket.getInputStream(); and socket.getOutputStream(); when bytes are available in input stream it will read them into a buffer. 
**All messages for activities are done by <code> Handler mHandler.obtainMessage(); mHandler.sendMessage(); </code>

**Receieved string:**
* A String is sent to the Bluetooth app from HC-05 and when it receives a string with identifier "$canster", the message is prased accordingly by using the string delimiter ',' and is ended by the newline character '\n' which will remove the data from StringBuffer.
* <code>Example: $canster,37.339334,-121.881123,37.338713,-121.880685,10.123,20.133,30.123,10.5,88.1,99.2,-2,1,44,7,11,22,33,-3,23\n</code>

**Manifest:**
<pre><code>
    uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" 
    uses-permission android:name="android.permission.BLUETOOTH" 
    uses-permission android:name="android.permission.BLUETOOTH_ADMIN" 
    uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"
    uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" 
    uses-permission android:name="android.permission.INTERNET"
</code></pre>

**Dependencies:**
<pre><code>
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.google.android.gms:play-services-maps:17.0.0'
    compile 'com.google.maps.android:android-maps-utils:0.4+'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'com.google.android.gms:play-services-location:17.0.0'
    implementation 'androidx.appcompat:appcompat:1.1.0'
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    implementation 'com.android.support:design:28.0.0'
    implementation 'androidx.navigation:navigation-fragment:2.2.1'
    implementation 'androidx.navigation:navigation-ui:2.2.1'
    implementation 'androidx.lifecycle:lifecycle-extensions:2.2.0'
</code></pre>

**Resources:**

- [Youtube Coding with Mitch](https://www.youtube.com/channel/UCoNZZLhPuuRteu02rh7bzsw)
- [Android Developers Github repo](https://github.com/googlearchive/android-BluetoothChat)
- [Android Developers Website](https://developer.android.com/guide/topics/connectivity/bluetooth)
- [Udemy Course](https://www.udemy.com/course/the-complete-android-oreo-developer-course/)

**Technical Challenges:**

* Bluetooth Discovery: While creating the android application, we were not able to see any Bluetooth devices in the paired device list. The problem was that for the Android versions above Lillipop we would also need location access. We have solved it by writing a function to check whether to check for permissions or not.
<pre><code>
private void checkBluetoothPermissions() {
    Log.d(TAG, "checkBTPermissions: Checking permissions");
    int permission_check = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
    permission_check += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
    if (permission_check != 0) {
        this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
    }
    else{
        Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
    }
    }
</code></pre>

* Catching NULL in a StringTokenizer: When sending data using Bluetooth sometimes thee app crashes. Using debug Run log in Android Studio IDE, we have found out that this is because we get <code> NullPointerException, java.util.StringTokenizer </code>  error. This is mainly due to passing a null value to the StringTokenizer constructor. This problem can be avoided by after reading a line from the Bluetooth StringBuffer, we check whether it is null, before passing it to the StringTokenizer, but later on, we have preferred to use <code>split();</code> to prase the received string as StringTokenizer is now a legacy class that is retained for compatibility reasons and its use is discouraged in new code.

* XML Layouts: Generating layouts is difficult for new android code developers, the design layouts made should be compatible for a wide range of mobile devices but using the new android libraries we have found that, the older phones then tend to have difficulty in showing the layouts correctly. We have solved this problem by learning the design commercial XML design layouts by following tutorials on Udemy and Youtube. 

* Auto Connect: During the first stages we have implemented Bluetooth connection via searching for all devices and clicking on the device from Listpair to connect. This has a lot of time to process all the discovered devices near range. To solve this we have hardcoded the HC-05 device address <code>00:14:03:06:02:83</code> to <code>mBluetoothAdapter.getRemoteDevice("device address")</code>.

<BR/>

