package com.example.bookhub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookhub.R;
import com.example.bookhub.adapters.BookAdapter;
import com.example.bookhub.database.DatabaseHelper;
import com.example.bookhub.models.Book;
import com.example.bookhub.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private RecyclerView searchRecyclerView;
    private TextView emptyView;
    private ProgressBar progressBar;
    private SearchView searchView;

    private DatabaseHelper databaseHelper;
    private BookAdapter bookAdapter;
    private List<Book> bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Khởi tạo DatabaseHelper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Ánh xạ view
        searchRecyclerView = findViewById(R.id.search_recycler_view);
        emptyView = findViewById(R.id.empty_view);
        progressBar = findViewById(R.id.progressBar);
        searchView = findViewById(R.id.search_view);

        // Khởi tạo danh sách sách
        bookList = new ArrayList<>();

        // Thiết lập adapter và layout manager cho RecyclerView
        setupRecyclerView();

        // Thiết lập sự kiện cho SearchView
        setupSearchView();

        // Kiểm tra xem có query từ Intent không
        String searchQuery = getIntent().getStringExtra(Constants.EXTRA_SEARCH_QUERY);
        if (searchQuery != null && !searchQuery.isEmpty()) {
            searchView.setQuery(searchQuery, true);
        }
    }

    private void setupRecyclerView() {
        bookAdapter = new BookAdapter(this, bookList, 0, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        searchRecyclerView.setLayoutManager(layoutManager);
        searchRecyclerView.setAdapter(bookAdapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    performSearch(newText);
                } else if (newText.isEmpty()) {
                    clearSearchResults();
                }
                return true;
            }
        });

        // Focus SearchView
        searchView.requestFocus();
    }

    private void performSearch(String query) {
        progressBar.setVisibility(View.VISIBLE);

        // Tìm kiếm trong cơ sở dữ liệu
        List<Book> searchResults = databaseHelper.searchBooks(query);

        // Cập nhật danh sách
        bookList.clear();
        bookList.addAll(searchResults);
        bookAdapter.notifyDataSetChanged();

        // Cập nhật UI
        progressBar.setVisibility(View.GONE);

        // Hiển thị emptyView nếu không có kết quả
        if (bookList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            searchRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            searchRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void clearSearchResults() {
        bookList.clear();
        bookAdapter.notifyDataSetChanged();
        emptyView.setVisibility(View.GONE);
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
}