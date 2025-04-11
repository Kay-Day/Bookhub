package com.example.bookhub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookhub.R;
import com.example.bookhub.database.DatabaseHelper;
import com.example.bookhub.models.User;
import com.example.bookhub.utils.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerText, forgotPasswordText;
    private ProgressBar progressBar;

    private DatabaseHelper databaseHelper;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo DatabaseHelper và PreferenceManager
        databaseHelper = DatabaseHelper.getInstance(this);
        preferenceManager = PreferenceManager.getInstance(this);

        // Ánh xạ view
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        registerText = findViewById(R.id.register_text);
        forgotPasswordText = findViewById(R.id.forgot_password_text);
        progressBar = findViewById(R.id.progressBar);

        // Thiết lập sự kiện click cho nút đăng nhập với hiệu ứng ripple
        loginButton.setBackground(getResources().getDrawable(R.drawable.button_ripple_effect));
        loginButton.setOnClickListener(v -> loginUser());

        // Thiết lập sự kiện click cho text đăng ký
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Thiết lập sự kiện click cho text quên mật khẩu
        forgotPasswordText.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void loginUser() {
        // Lấy giá trị từ EditText
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Kiểm tra dữ liệu nhập vào
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Vui lòng nhập email");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Vui lòng nhập mật khẩu");
            passwordEditText.requestFocus();
            return;
        }

        // Hiển thị ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        // Kiểm tra thông tin đăng nhập trong cơ sở dữ liệu
        User user = databaseHelper.getUserByEmail(email);

        if (user != null && user.getPassword().equals(password)) {
            // Đăng nhập thành công
            // Lưu thông tin đăng nhập vào SharedPreferences
            preferenceManager.saveUserLoginSession(user.getId(), user.getUsername(), user.getEmail());

            // Hiển thị thông báo
            Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();

            // Chuyển đến MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            // Hiệu ứng chuyển cảnh
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            // Đăng nhập thất bại
            Toast.makeText(LoginActivity.this, getString(R.string.error_login), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }
}