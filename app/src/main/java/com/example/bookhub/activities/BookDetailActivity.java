package com.example.bookhub.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bookhub.R;
import com.example.bookhub.database.DatabaseHelper;
import com.example.bookhub.models.Book;
import com.example.bookhub.models.Category;
import com.example.bookhub.utils.Constants;
import com.example.bookhub.utils.PreferenceManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView bookCover;
    private TextView bookTitle, bookAuthor, bookCategory, bookDescription, bookContentPreview;
    private FloatingActionButton fabReadBook;

    private DatabaseHelper databaseHelper;
    private PreferenceManager preferenceManager;
    private Book book;
    private int bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Khởi tạo DatabaseHelper và PreferenceManager
        databaseHelper = DatabaseHelper.getInstance(this);
        preferenceManager = PreferenceManager.getInstance(this);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Ánh xạ view
        bookCover = findViewById(R.id.book_cover);
        bookTitle = findViewById(R.id.book_title);
        bookAuthor = findViewById(R.id.book_author);
        bookCategory = findViewById(R.id.book_category);
        bookDescription = findViewById(R.id.book_description);
        bookContentPreview = findViewById(R.id.book_content_preview);
        fabReadBook = findViewById(R.id.fab_read_book);

        // Lấy thông tin sách từ Intent
        bookId = getIntent().getIntExtra(Constants.EXTRA_BOOK_ID, -1);

        if (bookId != -1) {
            // Tải thông tin sách từ cơ sở dữ liệu
            loadBookDetails(bookId);

            // Thiết lập sự kiện click cho FAB đọc sách
            fabReadBook.setOnClickListener(v -> {
                Intent intent = new Intent(BookDetailActivity.this, BookReaderActivity.class);
                intent.putExtra(Constants.EXTRA_BOOK_ID, bookId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        } else {
            Toast.makeText(this, getString(R.string.error_loading_book), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadBookDetails(int bookId) {
        book = databaseHelper.getBookById(bookId);

        if (book != null) {
            // Thiết lập tiêu đề cho CollapsingToolbarLayout
            CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
            collapsingToolbar.setTitle(book.getTitle());

            // Thiết lập thông tin sách
            bookTitle.setText(book.getTitle());
            bookAuthor.setText(book.getAuthor());
            bookDescription.setText(book.getDescription());

            // Hiển thị nội dung xem trước (5 dòng đầu)
            String content = book.getContent();
            bookContentPreview.setText(content != null ? content : "");

            // Thiết lập thể loại
            Category category = databaseHelper.getCategoryById(book.getCategoryId());
            if (category != null) {
                bookCategory.setText(category.getName());
            }

            // Thiết lập hình ảnh bìa sách
            if (book.getCoverImage() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(book.getCoverImage(), 0, book.getCoverImage().length);
                bookCover.setImageBitmap(bitmap);
            } else {
                bookCover.setImageResource(R.drawable.ic_book);
            }
        } else {
            Toast.makeText(this, getString(R.string.error_loading_book), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_detail_menu, menu);

        // Chỉ hiển thị nút chỉnh sửa và xóa nếu người dùng hiện tại là người tạo sách
        int currentUserId = preferenceManager.getUserId();
        if (book != null && book.getUserId() != currentUserId) {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_edit) {
            // Chỉnh sửa sách
            Intent intent = new Intent(BookDetailActivity.this, AddEditBookActivity.class);
            intent.putExtra(Constants.EXTRA_BOOK_ID, bookId);
            startActivityForResult(intent, Constants.REQUEST_EDIT_BOOK);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        } else if (id == R.id.action_delete) {
            // Xóa sách
            showDeleteConfirmationDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_book);
        builder.setMessage(R.string.delete_book_confirm);
        builder.setPositiveButton(R.string.delete, (dialog, which) -> {
            deleteBook();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    private void deleteBook() {
        int result = databaseHelper.deleteBook(bookId);

        if (result > 0) {
            // Hiển thị thông báo
            Toast.makeText(this, getString(R.string.book_deleted), Toast.LENGTH_SHORT).show();

            // Quay lại màn hình trước
            finish();
        } else {
            Toast.makeText(this, getString(R.string.error_deleting_book), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_EDIT_BOOK) {
                // Tải lại thông tin sách sau khi chỉnh sửa
                loadBookDetails(bookId);

                // Hiển thị thông báo
                Toast.makeText(this, getString(R.string.book_updated), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Animation khi quay lại
        overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
    }
}