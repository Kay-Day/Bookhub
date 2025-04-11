package com.example.bookhub.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.example.bookhub.R;
import com.example.bookhub.database.DatabaseHelper;
import com.example.bookhub.models.Book;
import com.example.bookhub.utils.Constants;
import com.example.bookhub.utils.PreferenceManager;

public class BookReaderActivity extends AppCompatActivity {

    private TextView bookContent;
    private NestedScrollView contentScrollView;
    private ImageButton btnDecreaseFont, btnNightMode, btnIncreaseFont;

    private DatabaseHelper databaseHelper;
    private PreferenceManager preferenceManager;
    private Book book;
    private int bookId;
    private int fontSize;
    private boolean isNightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reader);

        // Khởi tạo DatabaseHelper và PreferenceManager
        databaseHelper = DatabaseHelper.getInstance(this);
        preferenceManager = PreferenceManager.getInstance(this);

        // Lấy cài đặt người dùng
        fontSize = preferenceManager.getFontSize();
        isNightMode = preferenceManager.isNightMode();

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Ánh xạ view
        bookContent = findViewById(R.id.book_content);
        contentScrollView = findViewById(R.id.content_scroll_view);
        btnDecreaseFont = findViewById(R.id.btn_decrease_font);
        btnNightMode = findViewById(R.id.btn_night_mode);
        btnIncreaseFont = findViewById(R.id.btn_increase_font);

        // Thiết lập icons ban đầu
        btnDecreaseFont.setImageResource(R.drawable.ic_font_decrease);
        btnNightMode.setImageResource(R.drawable.ic_night_mode);
        btnIncreaseFont.setImageResource(R.drawable.ic_font_increase);

        // Lấy thông tin sách từ Intent
        bookId = getIntent().getIntExtra(Constants.EXTRA_BOOK_ID, -1);

        if (bookId != -1) {
            // Tải thông tin sách từ cơ sở dữ liệu
            loadBookContent(bookId);

            // Áp dụng cài đặt hiện tại
            applyFontSize();
            applyNightMode();

            // Thiết lập sự kiện click cho các nút điều khiển
            btnDecreaseFont.setOnClickListener(v -> {
                decreaseFontSize();
            });

            btnNightMode.setOnClickListener(v -> {
                toggleNightMode();
            });

            btnIncreaseFont.setOnClickListener(v -> {
                increaseFontSize();
            });
        } else {
            Toast.makeText(this, getString(R.string.error_loading_book), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadBookContent(int bookId) {
        book = databaseHelper.getBookById(bookId);

        if (book != null) {
            // Thiết lập tiêu đề
            getSupportActionBar().setTitle(book.getTitle());

            // Thiết lập nội dung sách
            if (book.getContent() != null && !book.getContent().isEmpty()) {
                bookContent.setText(book.getContent());
            } else {
                bookContent.setText("Không có nội dung");
            }
        } else {
            Toast.makeText(this, getString(R.string.error_loading_book), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void applyFontSize() {
        bookContent.setTextSize(fontSize);
    }

    private void applyNightMode() {
        if (isNightMode) {
            // Chế độ ban đêm
            contentScrollView.setBackgroundColor(getResources().getColor(R.color.text_primary));
            bookContent.setTextColor(getResources().getColor(R.color.background));
            btnNightMode.setImageResource(R.drawable.ic_day_mode);
            btnNightMode.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.background)));
            btnDecreaseFont.setImageResource(R.drawable.ic_font_decrease);
            btnDecreaseFont.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.background)));
            btnIncreaseFont.setImageResource(R.drawable.ic_font_increase);
            btnIncreaseFont.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.background)));
        } else {
            // Chế độ ban ngày
            contentScrollView.setBackgroundColor(getResources().getColor(R.color.background));
            bookContent.setTextColor(getResources().getColor(R.color.text_primary));
            btnNightMode.setImageResource(R.drawable.ic_night_mode);
            btnNightMode.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_primary)));
            btnDecreaseFont.setImageResource(R.drawable.ic_font_decrease);
            btnDecreaseFont.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_primary)));
            btnIncreaseFont.setImageResource(R.drawable.ic_font_increase);
            btnIncreaseFont.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_primary)));
        }
    }

    private void decreaseFontSize() {
        if (fontSize > Constants.FONT_SIZE_SMALL) {
            fontSize -= 2;
            applyFontSize();
            preferenceManager.setFontSize(fontSize);
        } else {
            Toast.makeText(this, "Đã đạt kích thước nhỏ nhất", Toast.LENGTH_SHORT).show();
        }
    }

    private void increaseFontSize() {
        if (fontSize < Constants.FONT_SIZE_EXTRA_LARGE) {
            fontSize += 2;
            applyFontSize();
            preferenceManager.setFontSize(fontSize);
        } else {
            Toast.makeText(this, "Đã đạt kích thước lớn nhất", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleNightMode() {
        isNightMode = !isNightMode;
        applyNightMode();
        preferenceManager.setNightMode(isNightMode);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Animation khi quay lại
        overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
    }
}