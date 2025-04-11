package com.example.bookhub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookhub.R;
import com.example.bookhub.database.DatabaseHelper;
import com.example.bookhub.models.User;
import com.example.bookhub.utils.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginText;
    private ImageView backButton;
    private ProgressBar progressBar;

    private DatabaseHelper databaseHelper;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo DatabaseHelper và PreferenceManager
        databaseHelper = DatabaseHelper.getInstance(this);
        preferenceManager = PreferenceManager.getInstance(this);

        // Ánh xạ view
        usernameEditText = findViewById(R.id.username_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        registerButton = findViewById(R.id.register_button);
        loginText = findViewById(R.id.login_text);
        backButton = findViewById(R.id.back_button);
        progressBar = findViewById(R.id.progressBar);

        // Thiết lập sự kiện click cho nút đăng ký với hiệu ứng ripple
        registerButton.setBackground(getResources().getDrawable(R.drawable.button_ripple_effect));
        registerButton.setOnClickListener(v -> registerUser());

        // Thiết lập sự kiện click cho text đăng nhập
        loginText.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
        });

        // Thiết lập sự kiện click cho nút back
        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
        });
    }

    private void registerUser() {
        // Lấy giá trị từ EditText
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Kiểm tra dữ liệu nhập vào
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Vui lòng nhập tên người dùng");
            usernameEditText.requestFocus();
            return;
        }

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

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Vui lòng xác nhận mật khẩu");
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.error_password_match));
            confirmPasswordEditText.requestFocus();
            return;
        }

        // Hiển thị ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        // Kiểm tra xem email đã tồn tại trong cơ sở dữ liệu chưa
        User existingUser = databaseHelper.getUserByEmail(email);

        if (existingUser != null) {
            // Email đã tồn tại
            Toast.makeText(RegisterActivity.this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Tạo user mới
        User newUser = new User(username, email, password);

        // Thêm user vào cơ sở dữ liệu
        long userId = databaseHelper.addUser(newUser);

        if (userId != -1) {
            // Đăng ký thành công
            // Lưu thông tin đăng nhập vào SharedPreferences
            preferenceManager.saveUserLoginSession((int) userId, username, email);

            // Hiển thị thông báo
            Toast.makeText(RegisterActivity.this, getString(R.string.register_success), Toast.LENGTH_SHORT).show();

            // Chuyển đến MainActivity
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            // Hiệu ứng chuyển cảnh
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            // Đăng ký thất bại
            Toast.makeText(RegisterActivity.this, getString(R.string.error_register), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }
}