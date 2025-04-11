package com.example.bookhub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookhub.R;
import com.example.bookhub.adapters.BookAdapter;
import com.example.bookhub.adapters.CategoryAdapter;
import com.example.bookhub.database.DatabaseHelper;
import com.example.bookhub.models.Book;
import com.example.bookhub.models.Category;
import com.example.bookhub.utils.Constants;
import com.example.bookhub.utils.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BookAdapter.OnBookClickListener, CategoryAdapter.OnCategoryClickListener {

    private RecyclerView categoryRecyclerView, newBooksRecyclerView, allBooksRecyclerView;
    private CategoryAdapter categoryAdapter;
    private BookAdapter newBooksAdapter, allBooksAdapter;
    private List<Category> categoryList;
    private List<Book> bookList, allBooksList;
    private DatabaseHelper databaseHelper;
    private PreferenceManager preferenceManager;
    private FloatingActionButton fabAddBook;
    private CardView searchCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo DatabaseHelper và PreferenceManager
        databaseHelper = DatabaseHelper.getInstance(this);
        preferenceManager = PreferenceManager.getInstance(this);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Ánh xạ view
        categoryRecyclerView = findViewById(R.id.category_recycler_view);
        newBooksRecyclerView = findViewById(R.id.new_books_recycler_view);
        allBooksRecyclerView = findViewById(R.id.all_books_recycler_view);
        fabAddBook = findViewById(R.id.fab_add_book);
        searchCard = findViewById(R.id.search_card);

        // Khởi tạo danh sách
        categoryList = new ArrayList<>();
        bookList = new ArrayList<>();
        allBooksList = new ArrayList<>();

        // Thiết lập adapter và layout manager cho RecyclerView
        setupRecyclerViews();

        // Tải dữ liệu từ cơ sở dữ liệu
        loadData();

        // Thiết lập icon cho FAB
        fabAddBook.setImageResource(R.drawable.ic_add);

        // Thiết lập sự kiện click cho FAB thêm sách
        fabAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditBookActivity.class);
            startActivityForResult(intent, Constants.REQUEST_ADD_BOOK);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Thiết lập sự kiện click cho card tìm kiếm
        searchCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void setupRecyclerViews() {
        // Thiết lập RecyclerView cho danh sách thể loại
        categoryAdapter = new CategoryAdapter(this, categoryList, this);
        LinearLayoutManager categoryLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        categoryRecyclerView.setLayoutManager(categoryLayoutManager);
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Thiết lập RecyclerView cho danh sách sách mới
        newBooksAdapter = new BookAdapter(this, bookList, 0, this);
        LinearLayoutManager newBooksLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        newBooksRecyclerView.setLayoutManager(newBooksLayoutManager);
        newBooksRecyclerView.setAdapter(newBooksAdapter);

        // Thiết lập RecyclerView cho tất cả sách
        allBooksAdapter = new BookAdapter(this, allBooksList, 1, this);
        GridLayoutManager allBooksLayoutManager = new GridLayoutManager(this, 2);
        allBooksRecyclerView.setLayoutManager(allBooksLayoutManager);
        allBooksRecyclerView.setAdapter(allBooksAdapter);
    }

    private void loadData() {
        // Tải danh sách thể loại
        categoryList.clear();
        categoryList.addAll(databaseHelper.getAllCategories());
        categoryAdapter.notifyDataSetChanged();

        // Tải danh sách sách (giới hạn 10 cho danh sách sách mới)
        List<Book> allBooks = databaseHelper.getAllBooks();

        // Đảm bảo danh sách sách mới không vượt quá 10
        bookList.clear();
        for (int i = 0; i < Math.min(10, allBooks.size()); i++) {
            bookList.add(allBooks.get(i));
        }
        newBooksAdapter.notifyDataSetChanged();

        // Tải tất cả sách
        allBooksList.clear();
        allBooksList.addAll(allBooks);
        allBooksAdapter.notifyDataSetChanged();
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
    public void onCategoryClick(Category category) {
        Intent intent = new Intent(this, BookListActivity.class);
        intent.putExtra(Constants.EXTRA_CATEGORY_ID, category.getId());
        intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getName());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        } else if (id == R.id.action_logout) {
            // Đăng xuất
            preferenceManager.clearUserLoginSession();

            // Hiển thị thông báo
            Toast.makeText(MainActivity.this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();

            // Chuyển đến LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            // Hiệu ứng chuyển cảnh
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_ADD_BOOK) {
                // Tải lại dữ liệu sau khi thêm sách
                loadData();

                // Hiển thị thông báo
                Toast.makeText(this, getString(R.string.book_added), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi quay lại từ màn hình khác
        loadData();
    }
}