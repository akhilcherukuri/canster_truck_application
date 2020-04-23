Resources that helped:
- Youtube Coding with Mitch: https://www.youtube.com/channel/UCoNZZLhPuuRteu02rh7bzsw
- Android Developers Github repo: https://github.com/googlearchive/android-BluetoothChat
- Android Developers Website: https://developer.android.com/guide/topics/connectivity/bluetooth
- Udemy Course: https://www.udemy.com/course/the-complete-android-oreo-developer-course/

As of now, the application consists of:
1. Main Activity: 
- This is the app launch display screen
- Has Search Button for connecting from a paired device list view
- Once Device is clicked, a intent will start the Debug Activity // Later stages it will directly start the Maps Activity.

2. Debug Activity:
- Main Function of this activity is to checked all RX and TX Messages
- For the Demo: Predefined Transmit Buttons [START], [STOP], Textbox[ENTER]

3. Bluetooth Connection Service :
- Has 3 Running Threads (Accept, Connect, Connected).
- Accept Thread - Listens to <pre><code>BluetoothServerSocket</code></pre> using <pre><code>listenUsingRfcommWithServiceRecord</code></pre>
- Connect Thread - Creates a bluetooth socket using <pre><code>createRfcommSocketToServiceRecord</code></pre>
- Connected Thread - Creates <pre><code>socket.getInputStream();</code></pre> and <pre><code>socket.getOutputStream();</code></pre> when bytes are available in inputstream it will read them into a buffer.
- All messages for activities are done by Handler <pre><code>mHandler.obtainMessage();
mHandler.sendMessage();</code></pre>

Input string:

<pre><code>$canster,lat,lng,compass heading,speed,battery\n

Example: $canster,37.339312,-121.881111,180.77,7,5\n
</code></pre>

string needs to end with new line character to prase the message.



