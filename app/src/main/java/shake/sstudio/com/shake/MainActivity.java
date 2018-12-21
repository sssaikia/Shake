package shake.sstudio.com.shake;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    String AudioSavePathInDevice;
    int threshold=800;
    TextView text;
    MediaRecorder rec;
    ImageButton button;
    boolean state=true,broadcast;
    RadioButton radio;
    CountDownTimer mCountdowntimer;
    RadioGroup radiogroup;
    int selectedId,READ_PERMISSION,WRITE_PERMISSION,RECORD;
    ToggleButton toggleButton;
    private static final String[]requests={READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE,RECORD_AUDIO};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toggleButton=(ToggleButton)findViewById(R.id.toggleButton);
        SeekBar seek=(SeekBar)findViewById(R.id.seekBar);
        toggleButton.setChecked(true);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                toggleButton.setChecked(b);
                if (!b){

                    broadcast=false;
                }else {
                    broadcast=true;
                    Intent im=new Intent(MainActivity.this,MyService.class);
                    startService(im);
                }
            }
        });

        text=(TextView)findViewById(R.id.tv);

        radiogroup=(RadioGroup)findViewById(R.id.radioGroup);

        AudioSavePathInDevice =
                Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/" + "AudioRecording.3gp";

        button= (ImageButton) findViewById(R.id.button);

        SharedPreferences pre=getSharedPreferences("seekVal",Activity.MODE_PRIVATE);
        threshold = pre.getInt("thres",800);
        selectedId=pre.getInt("rID",0);
        radio=(RadioButton)findViewById(selectedId);
        radio.setChecked(true);
        seek.setMax(3000);
        seek.setProgress(threshold);
        //seek.setLeft(threshold);

        text.setText("Shake Intensity:"+threshold);
        selectedId=radiogroup.getCheckedRadioButtonId();

        Intent im=new Intent(MainActivity.this,MyService.class);
        /*im.putExtra("value",threshold);
        im.putExtra("rID", selectedId);*/
        startService(im);
        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                selectedId=radiogroup.getCheckedRadioButtonId();
                SharedPreferences pre =getSharedPreferences("seekVal", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor =pre.edit();
                editor.putInt("rID",selectedId);
                editor.apply();
                /*Intent im=new Intent(MainActivity.this,MyService.class);
                startService(im);*/
            }
        });



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this,
                        READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    read();
                }else

                if (state) {
                    MediaRecorderReady();
                    try {
                        rec.prepare();
                        timeC();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    state = false;
                } else {
                    mCountdowntimer.cancel();
                    TextView textView = (TextView) findViewById(R.id.textView);
                    textView.setText("Done");
                    button.setImageResource(R.drawable.rec_off);
                    button.setScaleType(ImageView.ScaleType.FIT_XY);
                    rec.stop();
                    rec.release();
                    state = true;
                }

            }

        });

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                threshold=i;
                text.setText("Shake Intensity:"+threshold);
                SharedPreferences pre =getSharedPreferences("seekVal", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor =pre.edit();
                editor.putInt("thres",threshold);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }
    public void read(){
        ActivityCompat.requestPermissions(MainActivity.this,
                requests, READ_PERMISSION);
    }

    public void MediaRecorderReady(){
        rec=new MediaRecorder();
        rec.reset();
        rec.setAudioSource(MediaRecorder.AudioSource.MIC);
        rec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        rec.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        rec.setOutputFile(AudioSavePathInDevice);
    }
    public void timeC(){
        rec.start();
        button.setImageResource(R.drawable.rec_on);
        button.setScaleType(ImageView.ScaleType.FIT_XY);
       mCountdowntimer=new CountDownTimer(5000, 1000) {//countdown Period =5000
           TextView textView=(TextView)findViewById(R.id.textView);
            public void onTick(long millisUntilFinished) {


                textView.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                textView.setText("Done");
               rec.stop();
                rec.release();
            button.setImageResource(R.drawable.rec_off);
                button.setScaleType(ImageView.ScaleType.FIT_XY);
            }

        }.start();

    }

    @Override
    protected void onDestroy() {
        if (toggleButton.isChecked()) {
            Intent sstart = new Intent("RESTART_SERVICE");
            sendBroadcast(sstart);
        }else {
            Log.d("Logs", "stop service called");
            Intent im=new Intent(MainActivity.this,MyService.class);
            stopService(im);
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    }
    public void diag(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Shake N Mock");
        builder.setMessage("While hanging with friends, " +
                "when someone says a stupid stuff shake your phone to mock them." +
                " Tease, annoy or make fun of them, you know. :)" +
                "\n-Simo");
        builder.setNegativeButton("Got it.",null);
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu_item,menu);
        return true;
        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.about:
                diag();
                return true;
            case R.id.share:
                Intent intent= new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Shake N Mock");
                intent.putExtra(Intent.EXTRA_TEXT,"text");
                startActivity(Intent.createChooser(intent,"Send via."));
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
