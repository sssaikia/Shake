package shake.sstudio.com.shake;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service implements SensorEventListener{
    MainActivity mainActivity;
    MediaPlayer mp;
    float x,y,z,last_x,last_y,last_z;
    long lastUpdate,curTime;
    int threshold;
    SensorManager sensorManager;
    Sensor acc;
    Uri uri;
    int rID;
    PowerManager.WakeLock wakeLock;
    String START="RESTART_SERVICE";
    BroadcastReceiver receiver;
    boolean playing=false;
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mainActivity=new MainActivity();
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipboardManager.OnPrimaryClipChangedListener mPrimaryChangeListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                Toast.makeText(getApplicationContext(), "Text copied.. (service)", Toast.LENGTH_SHORT).show();
                // this will be called whenever you copy something to the clipboard
            }
        };
        clipboard.addPrimaryClipChangedListener(mPrimaryChangeListener);
        PowerManager mgr = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();
        //threshold=mainActivity.threshold;

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        acc=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, acc,SensorManager.SENSOR_DELAY_NORMAL);
        curTime=lastUpdate=(long)0.0;
        x=y=z=last_x=last_y=last_z=(float)0.0;
        //rID = mainActivity.selectedId;
        receiver=new MyReceiver();
        IntentFilter intentFilter= new IntentFilter(START);
        registerReceiver(receiver,intentFilter);
       /* SharedPreferences pre=getSharedPreferences("seekVal", Activity.MODE_PRIVATE);
        threshold = pre.getInt("thres",0);
        rID=pre.getInt("rID",0);*/
        Log.d("logs", "on create");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

       /* rID=intent.getIntExtra("rID",0);
        threshold=intent.getIntExtra("value",800);*/
        Log.d("Logs", "on start command");

       // return START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        SharedPreferences pre=getSharedPreferences("seekVal", Activity.MODE_PRIVATE);
        threshold = pre.getInt("thres",800);
        rID=pre.getInt("rID",0);
        long curTime = System.currentTimeMillis();
        // only allow one update every 100ms.
        if ((curTime - lastUpdate) > 100) {//was 100
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];

            float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

            if (speed > threshold) {
                Log.d("sensor", "shake detected w/ speed: " + speed);
                if (!playing)
                { play();}
                //Toast.makeText(this, "shake detected w/ speed: " + speed+ "threshold"+threshold, Toast.LENGTH_SHORT).show();
            }
            last_x = x;
            last_y = y;
            last_z = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public void play(){

        mp = new MediaPlayer();
            if (rID!=0) {
           // Toast.makeText(getApplicationContext(),"rID"+rID,Toast.LENGTH_SHORT).show();
                switch (rID){
                case R.id.radioButton:
                    mp=MediaPlayer.create(this,R.raw.you_suck);
                    break;
                case R.id.radioButton2:
                    mp=MediaPlayer.create(this,R.raw.ha_ha);
                    break;
                case R.id.radioButton3:
                    mp=MediaPlayer.create(this,R.raw.shut_up);
                    break;
                case R.id.radioButton4:
                    uri = Uri.parse(Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/" + "AudioRecording.3gp");
                    mp=MediaPlayer.create(this, uri);
                    break;
            }
                mp.start();
                playing=true;
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mp.release();
                playing=false;
            }
        });}else {
                Toast.makeText(this,"Select a track",Toast.LENGTH_SHORT).show();
            }

    }

    @Override
    public void onDestroy() {
        Log.d("Logs", "unregistering reciever");
        unregisterReceiver(receiver);
        wakeLock.release();
        super.onDestroy();
    }
/* @Override
    public void onTaskRemoved(Intent rootIntent) {
        //super.onTaskRemoved(rootIntent);
        Intent intent=new Intent(START);
        sendBroadcast(intent);
    }*/
}
