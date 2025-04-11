package com.example.bookhub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookhub.R;
import com.example.bookhub.adapters.BookAdapter;
import com.example.bookhub.database.DatabaseHelper;
import com.example.bookhub.models.Book;
import com.example.bookhub.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class BookListActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private RecyclerView bookRecyclerView;
    private TextView emptyView;
    private ProgressBar progressBar;

    private DatabaseHelper databaseHelper;
    private BookAdapter bookAdapter;
    private List<Book> bookList;

    private int categoryId;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        // Khởi tạo DatabaseHelper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Lấy thông tin thể loại từ Intent
        categoryId = getIntent().getIntExtra(Constants.EXTRA_CATEGORY_ID, -1);
        categoryName = getIntent().getStringExtra(Constants.EXTRA_CATEGORY_NAME);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (categoryName != null) {
            getSupportActionBar().setTitle(categoryName);
        }

        // Ánh xạ view
        bookRecyclerView = findViewById(R.id.book_recycler_view);
        emptyView = findViewById(R.id.empty_view);
        progressBar = findViewById(R.id.progressBar);

        // Khởi tạo danh sách sách
        bookList = new ArrayList<>();

        // Thiết lập adapter và layout manager cho RecyclerView
        setupRecyclerView();

        // Tải dữ liệu
        loadBooks();
    }

    private void setupRecyclerView() {
        bookAdapter = new BookAdapter(this, bookList, 1, this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        bookRecyclerView.setLayoutManager(layoutManager);
        bookRecyclerView.setAdapter(bookAdapter);
    }

    private void loadBooks() {
        progressBar.setVisibility(View.VISIBLE);

        if (categoryId != -1) {
            // Tải sách theo thể loại
            bookList.clear();
            bookList.addAll(databaseHelper.getBooksByCategory(categoryId));
        } else {
            // Tải tất cả sách nếu không có thể loại cụ thể
            bookList.clear();
            bookList.addAll(databaseHelper.getAllBooks());
        }

        // Cập nhật UI
        progressBar.setVisibility(View.GONE);
        bookAdapter.notifyDataSetChanged();

        // Hiển thị emptyView nếu không có sách
        if (bookList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            bookRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            bookRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBookClick(Book book, View view) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra(Constants.EXTRA_BOOK_ID, book.getId());

        // Animation chuyển đổi giữa các activity với ảnh bìa sách
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                view,
                "book_cover_transition"
        );

        startActivity(intent, options.toBundle());
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

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi quay lại từ màn hình khác
        loadBooks();
    }
}