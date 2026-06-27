package com.example.finalyearproject.activites;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearproject.R;

public class VideoAds extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.property_ads_ui_design);

            // 1. VideoView ko ID se connect karein
            VideoView videoView = findViewById(R.id.adsvideoView);

            // 2. Video ka Path batayein (res/raw folder se)
            // Yahan 'ads_video' wahi naam hai jo aapne raw folder mein rakha hai
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.video1;
            Uri uri = Uri.parse(videoPath);
            videoView.setVideoURI(uri);

            // 3. Video jab load ho jaye (Ready ho jaye) tab start ho
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Video automatically start ho jayegi
                    videoView.start();

                    // Agar aap chahte hain video baar baar chale (Loop)
                    mp.setLooping(true);
                }
            });

            // 4. Agar video load karne mein error aaye (Optional par acha hai)
            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // Yahan aap error handle kar sakte hain
                    return false;
                }
            });
        }
    }
