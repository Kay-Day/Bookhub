package com.example.bookhub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.bookhub.R;
import com.example.bookhub.utils.PreferenceManager;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 3000; // Tăng thời gian để animation hiển thị đầy đủ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_splash);

        // Hiệu ứng fade-in cho text
        TextView appNameText = findViewById(R.id.app_name_text);
        TextView appSloganText = findViewById(R.id.app_slogan);

        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1500);
        fadeIn.setStartOffset(500);

        appNameText.startAnimation(fadeIn);
        appSloganText.startAnimation(fadeIn);

        // Lottie animation
        LottieAnimationView animationView = findViewById(R.id.animation_view);
        animationView.setAnimation(R.raw.book_animation);
        animationView.playAnimation();

        // Chuyển đến màn hình tiếp theo sau khoảng thời gian
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextScreen, SPLASH_DURATION);
    }

    private void navigateToNextScreen() {
        // Kiểm tra trạng thái đăng nhập
        PreferenceManager preferenceManager = PreferenceManager.getInstance(this);
        Intent intent;

        if (preferenceManager.isLoggedIn()) {
            // Nếu đã đăng nhập thì vào MainActivity
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // Nếu chưa đăng nhập thì vào LoginActivity
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);

        // Animation chuyển cảnh
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // Đóng SplashActivity
        finish();
    }
}