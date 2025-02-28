package com.example.hostel;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.hostel.loginpage;
import com.example.hostel.R;

public class SplashActivity extends AppCompatActivity {

    private ImageView logoImage;
    private TextView appTitle;
    private TextView appSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.splash);

        logoImage = findViewById(R.id.splash_logo);
        appTitle = findViewById(R.id.splashtitle);
        appSubtitle = findViewById(R.id.splashsubtitle);


        startAnimations();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, loginpage.class);
                startActivity(intent);
                finish();
            }
        }, 4000);
    }
    public void startAnimations() {
        // Logo slide down and fade in
        logoImage.setTranslationY(-200f);
        ObjectAnimator logoSlideDown = ObjectAnimator.ofFloat(logoImage, "translationY", -200f, 0f);
        ObjectAnimator logoFadeIn = ObjectAnimator.ofFloat(logoImage, "alpha", 0f, 1f);

        // Title fade in and slide up
        ObjectAnimator titleFadeIn = ObjectAnimator.ofFloat(appTitle, "alpha", 0f, 1f);
        ObjectAnimator titleSlideUp = ObjectAnimator.ofFloat(appTitle, "translationY", 50f, 0f);

        // Subtitle fade in and slide up
        ObjectAnimator subtitleFadeIn = ObjectAnimator.ofFloat(appSubtitle, "alpha", 0f, 1f);
        ObjectAnimator subtitleSlideUp = ObjectAnimator.ofFloat(appSubtitle, "translationY", 50f, 0f);

        // Create animation set
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(logoSlideDown, logoFadeIn);
        animatorSet.play(titleFadeIn).with(titleSlideUp).after(500); // Delay title animation
        animatorSet.play(subtitleFadeIn).with(subtitleSlideUp).after(titleFadeIn); // Chain after title

        // Configure timing
        logoSlideDown.setDuration(3000);
        logoFadeIn.setDuration(3000);
        titleFadeIn.setDuration(1200);
        titleSlideUp.setDuration(1200);
        subtitleFadeIn.setDuration(1200);
        subtitleSlideUp.setDuration(1200);

        // Start animations
        animatorSet.start();
    }
}