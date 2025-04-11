package com.example.bookhub.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.example.bookhub.R;
import com.example.bookhub.database.DatabaseHelper;
import com.example.bookhub.models.Book;
import com.example.bookhub.models.Category;
import com.example.bookhub.utils.Constants;
import com.example.bookhub.utils.ImageUtils;
import com.example.bookhub.utils.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddEditBookActivity extends AppCompatActivity {

    private ImageView bookCover;
    private Button btnChooseImage;
    private TextInputEditText titleEditText, authorEditText, descriptionEditText, contentEditText;
    private Spinner categorySpinner;
    private FloatingActionButton fabSave;
    private ProgressBar progressBar;

    private DatabaseHelper databaseHelper;
    private PreferenceManager preferenceManager;
    private List<Category> categoryList;
    private Book book;
    private int bookId = -1;
    private boolean isEditMode = false;
    private byte[] coverImageBytes;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_book);

        // Khởi tạo DatabaseHelper và PreferenceManager
        databaseHelper = DatabaseHelper.getInstance(this);
        preferenceManager = PreferenceManager.getInstance(this);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Ánh xạ view
        bookCover = findViewById(R.id.book_cover);
        btnChooseImage = findViewById(R.id.btn_choose_image);
        titleEditText = findViewById(R.id.title_edit_text);
        authorEditText = findViewById(R.id.author_edit_text);
        descriptionEditText = findViewById(R.id.description_edit_text);
        contentEditText = findViewById(R.id.content_edit_text);
        categorySpinner = findViewById(R.id.category_spinner);
        fabSave = findViewById(R.id.fab_save);
        progressBar = findViewById(R.id.progressBar);

        // Khởi tạo danh sách thể loại
        loadCategories();

        // Kiểm tra xem là thêm mới hay chỉnh sửa sách
        bookId = getIntent().getIntExtra(Constants.EXTRA_BOOK_ID, -1);
        isEditMode = (bookId != -1);

        if (isEditMode) {
            // Chỉnh sửa sách
            getSupportActionBar().setTitle(getString(R.string.edit_book));
            loadBookDetails(bookId);
        } else {
            // Thêm sách mới
            getSupportActionBar().setTitle(getString(R.string.add_book));
        }

        // Thiết lập sự kiện click cho nút chọn ảnh
        btnChooseImage.setOnClickListener(v -> showImagePickerOptions());

        // Thiết lập icon cho FAB lưu
        fabSave.setImageResource(R.drawable.ic_save);

        // Thiết lập sự kiện click cho nút lưu
        fabSave.setOnClickListener(v -> saveBook());
    }

    private void loadCategories() {
        categoryList = databaseHelper.getAllCategories();

        // Thiết lập adapter cho Spinner thể loại
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void loadBookDetails(int bookId) {
        book = databaseHelper.getBookById(bookId);

        if (book != null) {
            // Thiết lập các trường thông tin
            titleEditText.setText(book.getTitle());
            authorEditText.setText(book.getAuthor());
            descriptionEditText.setText(book.getDescription());
            contentEditText.setText(book.getContent());

            // Thiết lập thể loại
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).getId() == book.getCategoryId()) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }

            // Thiết lập hình ảnh bìa sách
            if (book.getCoverImage() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(book.getCoverImage(), 0, book.getCoverImage().length);
                bookCover.setImageBitmap(bitmap);
                btnChooseImage.setVisibility(View.GONE);
                coverImageBytes = book.getCoverImage();
            }
        } else {
            Toast.makeText(this, getString(R.string.error_loading_book), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showImagePickerOptions() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện", "Hủy"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh bìa");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Chụp ảnh
                captureImage();
                btnChooseImage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_camera, 0, 0, 0);
            } else if (which == 1) {
                // Chọn từ thư viện
                pickImageFromGallery();
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                // Tạo file tạm thời để lưu ảnh
                File storageDir = getExternalFilesDir("images");
                photoFile = File.createTempFile("book_cover", ".jpg", storageDir);

                // Lấy Uri từ file
                String authorities = getPackageName() + ".fileprovider";
                imageUri = FileProvider.getUriForFile(this, authorities, photoFile);

                // Gửi Uri đến camera app
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, Constants.REQUEST_IMAGE_CAPTURE);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể tạo file ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Constants.REQUEST_PICK_IMAGE);
    }

    private void saveBook() {
        // Lấy giá trị từ các trường nhập liệu
        String title = titleEditText.getText().toString().trim();
        String author = authorEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        // Kiểm tra trường bắt buộc
        if (TextUtils.isEmpty(title)) {
            titleEditText.setError("Vui lòng nhập tiêu đề sách");
            titleEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(author)) {
            authorEditText.setError("Vui lòng nhập tên tác giả");
            authorEditText.requestFocus();
            return;
        }

        // Hiển thị ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        // Lấy ID thể loại đã chọn
        Category selectedCategory = (Category) categorySpinner.getSelectedItem();
        int categoryId = selectedCategory.getId();

        // Lấy ID người dùng hiện tại
        int userId = preferenceManager.getUserId();

        // Tạo đối tượng Book
        if (isEditMode) {
            // Cập nhật sách đã tồn tại
            book.setTitle(title);
            book.setAuthor(author);
            book.setDescription(description);
            book.setContent(content);
            book.setCategoryId(categoryId);

            if (coverImageBytes != null) {
                book.setCoverImage(coverImageBytes);
            }

            // Cập nhật sách vào cơ sở dữ liệu
            int result = databaseHelper.updateBook(book);

            if (result > 0) {
                // Cập nhật thành công
                setResult(RESULT_OK);
                finish();
            } else {
                // Cập nhật thất bại
                Toast.makeText(this, getString(R.string.error_updating_book), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        } else {
            // Thêm sách mới
            Book newBook = new Book(title, author, description, content, coverImageBytes, categoryId, userId);

            // Thêm sách vào cơ sở dữ liệu
            long bookId = databaseHelper.addBook(newBook);

            if (bookId != -1) {
                // Thêm thành công
                setResult(RESULT_OK);
                finish();
            } else {
                // Thêm thất bại
                Toast.makeText(this, getString(R.string.error_adding_book), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_IMAGE_CAPTURE) {
                // Xử lý ảnh từ camera
                if (imageUri != null) {
                    try {
                        coverImageBytes = ImageUtils.convertImageToByteArray(this, imageUri);
                        displaySelectedImage();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == Constants.REQUEST_PICK_IMAGE) {
                // Xử lý ảnh từ thư viện
                if (data != null && data.getData() != null) {
                    imageUri = data.getData();
                    try {
                        coverImageBytes = ImageUtils.convertImageToByteArray(this, imageUri);
                        displaySelectedImage();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void displaySelectedImage() {
        if (coverImageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(coverImageBytes, 0, coverImageBytes.length);
            bookCover.setImageBitmap(bitmap);
            btnChooseImage.setVisibility(View.GONE);
        }
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