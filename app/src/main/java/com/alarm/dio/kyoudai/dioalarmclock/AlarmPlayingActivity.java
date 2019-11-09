package com.alarm.dio.kyoudai.dioalarmclock;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;

@SuppressWarnings("ALL")
public class AlarmPlayingActivity extends AppCompatActivity {

    TextView alarmNameView;
    TextView alarmTimeView;
    Button stopAlarmButton;
    String sound;
    MediaPlayer soundPlayer;
    private InterstitialAd advert;
    boolean showing;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StartAppSDK.init(this, "210746585", true);
        setContentView(R.layout.activity_alarm_playing);
        alarmNameView = findViewById(R.id.playingTitle);
        alarmTimeView = findViewById(R.id.playingTime);
        stopAlarmButton = findViewById(R.id.stopAlarmButton);

        StartAppSDK.setUserConsent (this,
                "pas",
                System.currentTimeMillis(),
                false);
        //Variable used for if advertistment is showing
        showing = false;

        //Set UI elements corresponding texts
        alarmNameView.setText(getIntent().getStringExtra("alarmName"));
        alarmTimeView.setText(getIntent().getStringExtra("alarmTime"));
        sound = getIntent().getStringExtra("alarmSound");

        //Adverts temporarily not showing as account disabled
      //  MobileAds.initialize(getApplicationContext(), "ca-app-pub-5483591282248570~7107432706");
//        advert = new InterstitialAd(this);
//        advert.setAdUnitId("ca-app-pub-5483591282248570/7033789821");
//        advert.setAdListener(new AdListener() {
//            @Override
//            public void onAdClosed() {
//                finish();
//                startActivity(new Intent(getApplicationContext(),MainActivity.class));
//            }
//
//        });
//
//        advert.loadAd(new AdRequest.Builder().build());


        //Wake up the screen if it is locked
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (pm != null) {
            wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
        }
        if (wakeLock != null) {
            wakeLock.acquire(10*60*1000L /*10 minutes*/);
        }

        //Lock the keyboard
        KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = null;
        if (keyguardManager != null) {
            keyguardLock = keyguardManager.newKeyguardLock("TAG");
        }
        if (keyguardLock != null) {
            keyguardLock.disableKeyguard();
        }



        //Ensures device is now at full volume
        AudioManager am =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (am != null) {
            am.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    0);
        }


        //Switch for setting soundPlayer based on the sound selected
        switch (sound) {
            case "ZA WARUDO":
                soundPlayer = MediaPlayer.create(getApplicationContext(), R.raw.zawarudo);
                break;
            case "IT WAS ME, DIO!":
                soundPlayer = MediaPlayer.create(getApplicationContext(), R.raw.itwasmedio);
                break;
            case "MUDA MUDA":
                soundPlayer = MediaPlayer.create(getApplicationContext(), R.raw.mudamuda);
                break;
            case "ROAD ROLLER":
                soundPlayer = MediaPlayer.create(getApplicationContext(), R.raw.dio);
                break;
            default:
                soundPlayer = MediaPlayer.create(getApplicationContext(), R.raw.wryy);
                break;
        }

        //Start playing the sound repetitively
        if (soundPlayer != null) {
            soundPlayer.start();
            soundPlayer.setLooping(true);
        }


        stopAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (advert.isLoaded() && !showing) {

                //Stop sound playing
                    soundPlayer.stop();
                    showing = true;
                    //advert.show();
                StartAppAd.showAd(getApplicationContext());
                //End Activity
                finish();
                //}
            }
        });




    }



}
