package com.example.bookhub.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.bookhub.models.Book;
import com.example.bookhub.models.Category;
import com.example.bookhub.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // Database Info
    private static final String DATABASE_NAME = "BookDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_BOOKS = "books";
    private static final String TABLE_CATEGORIES = "categories";

    // User Table Columns
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USER_NAME = "username";
    private static final String KEY_USER_EMAIL = "email";
    private static final String KEY_USER_PASSWORD = "password";

    // Book Table Columns
    private static final String KEY_BOOK_ID = "id";
    private static final String KEY_BOOK_TITLE = "title";
    private static final String KEY_BOOK_AUTHOR = "author";
    private static final String KEY_BOOK_DESCRIPTION = "description";
    private static final String KEY_BOOK_CONTENT = "content";
    private static final String KEY_BOOK_COVER = "cover";
    private static final String KEY_BOOK_CATEGORY_ID = "category_id";
    private static final String KEY_BOOK_USER_ID = "user_id";

    // Category Table Columns
    private static final String KEY_CATEGORY_ID = "id";
    private static final String KEY_CATEGORY_NAME = "name";

    private static DatabaseHelper sInstance;

    // Singleton pattern implementation
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS +
                "(" +
                KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_USER_NAME + " TEXT," +
                KEY_USER_EMAIL + " TEXT UNIQUE," +
                KEY_USER_PASSWORD + " TEXT" +
                ")";

        String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_CATEGORIES +
                "(" +
                KEY_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_CATEGORY_NAME + " TEXT UNIQUE" +
                ")";

        String CREATE_BOOKS_TABLE = "CREATE TABLE " + TABLE_BOOKS +
                "(" +
                KEY_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_BOOK_TITLE + " TEXT," +
                KEY_BOOK_AUTHOR + " TEXT," +
                KEY_BOOK_DESCRIPTION + " TEXT," +
                KEY_BOOK_CONTENT + " TEXT," +
                KEY_BOOK_COVER + " BLOB," +
                KEY_BOOK_CATEGORY_ID + " INTEGER," +
                KEY_BOOK_USER_ID + " INTEGER," +
                "FOREIGN KEY (" + KEY_BOOK_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + KEY_CATEGORY_ID + ")," +
                "FOREIGN KEY (" + KEY_BOOK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ")" +
                ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_CATEGORIES_TABLE);
        db.execSQL(CREATE_BOOKS_TABLE);

        // Thêm một số thể loại sách mặc định
        insertDefaultCategories(db);
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        String[] defaultCategories = {"Tiểu thuyết", "Khoa học", "Lịch sử", "Kỹ năng sống", "Văn học", "Thiếu nhi"};

        for (String category : defaultCategories) {
            ContentValues values = new ContentValues();
            values.put(KEY_CATEGORY_NAME, category);
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        }
    }

    // USER METHODS
    public long addUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_USER_NAME, user.getUsername());
            values.put(KEY_USER_EMAIL, user.getEmail());
            values.put(KEY_USER_PASSWORD, user.getPassword());

            userId = db.insertOrThrow(TABLE_USERS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while adding user");
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return userId;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        User user = null;

        String USERS_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = ?",
                        TABLE_USERS, KEY_USER_EMAIL);

        Cursor cursor = db.rawQuery(USERS_SELECT_QUERY, new String[]{email});

        try {
            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndex(KEY_USER_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(KEY_USER_NAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(KEY_USER_EMAIL)));
                user.setPassword(cursor.getString(cursor.getColumnIndex(KEY_USER_PASSWORD)));
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while getting user by email");
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return user;
    }

    public User getUserById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        User user = null;

        String USERS_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = ?",
                        TABLE_USERS, KEY_USER_ID);

        Cursor cursor = db.rawQuery(USERS_SELECT_QUERY, new String[]{String.valueOf(id)});

        try {
            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndex(KEY_USER_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(KEY_USER_NAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(KEY_USER_EMAIL)));
                user.setPassword(cursor.getString(cursor.getColumnIndex(KEY_USER_PASSWORD)));
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while getting user by id");
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return user;
    }

    // CATEGORIES METHODS
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();

        String CATEGORIES_SELECT_QUERY =
                String.format("SELECT * FROM %s ORDER BY %s ASC",
                        TABLE_CATEGORIES, KEY_CATEGORY_NAME);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(CATEGORIES_SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    Category category = new Category();
                    category.setId(cursor.getInt(cursor.getColumnIndex(KEY_CATEGORY_ID)));
                    category.setName(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY_NAME)));
                    categories.add(category);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while getting categories");
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return categories;
    }

    public Category getCategoryById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Category category = null;

        String CATEGORY_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = ?",
                        TABLE_CATEGORIES, KEY_CATEGORY_ID);

        Cursor cursor = db.rawQuery(CATEGORY_SELECT_QUERY, new String[]{String.valueOf(id)});

        try {
            if (cursor.moveToFirst()) {
                category = new Category();
                category.setId(cursor.getInt(cursor.getColumnIndex(KEY_CATEGORY_ID)));
                category.setName(cursor.getString(cursor.getColumnIndex(KEY_CATEGORY_NAME)));
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while getting category by id");
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return category;
    }

    // BOOKS METHODS
    public long addBook(Book book) {
        SQLiteDatabase db = getWritableDatabase();
        long bookId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_BOOK_TITLE, book.getTitle());
            values.put(KEY_BOOK_AUTHOR, book.getAuthor());
            values.put(KEY_BOOK_DESCRIPTION, book.getDescription());
            values.put(KEY_BOOK_CONTENT, book.getContent());
            values.put(KEY_BOOK_COVER, book.getCoverImage());
            values.put(KEY_BOOK_CATEGORY_ID, book.getCategoryId());
            values.put(KEY_BOOK_USER_ID, book.getUserId());

            bookId = db.insertOrThrow(TABLE_BOOKS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while adding book");
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return bookId;
    }

    public int updateBook(Book book) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_BOOK_TITLE, book.getTitle());
            values.put(KEY_BOOK_AUTHOR, book.getAuthor());
            values.put(KEY_BOOK_DESCRIPTION, book.getDescription());
            values.put(KEY_BOOK_CONTENT, book.getContent());
            if (book.getCoverImage() != null) {
                values.put(KEY_BOOK_COVER, book.getCoverImage());
            }
            values.put(KEY_BOOK_CATEGORY_ID, book.getCategoryId());

            rowsAffected = db.update(TABLE_BOOKS, values, KEY_BOOK_ID + " = ?",
                    new String[]{String.valueOf(book.getId())});

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while updating book");
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return rowsAffected;
    }

    public int deleteBook(int bookId) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            rowsAffected = db.delete(TABLE_BOOKS, KEY_BOOK_ID + " = ?",
                    new String[]{String.valueOf(bookId)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while deleting book");
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return rowsAffected;
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();

        String BOOKS_SELECT_QUERY =
                String.format("SELECT * FROM %s ORDER BY %s DESC",
                        TABLE_BOOKS, KEY_BOOK_ID);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(BOOKS_SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    Book book = new Book();
                    book.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_ID)));
                    book.setTitle(cursor.getString(cursor.getColumnIndex(KEY_BOOK_TITLE)));
                    book.setAuthor(cursor.getString(cursor.getColumnIndex(KEY_BOOK_AUTHOR)));
                    book.setDescription(cursor.getString(cursor.getColumnIndex(KEY_BOOK_DESCRIPTION)));
                    book.setContent(cursor.getString(cursor.getColumnIndex(KEY_BOOK_CONTENT)));
                    book.setCoverImage(cursor.getBlob(cursor.getColumnIndex(KEY_BOOK_COVER)));
                    book.setCategoryId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_CATEGORY_ID)));
                    book.setUserId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_USER_ID)));
                    books.add(book);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while getting books");
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return books;
    }

    public Book getBookById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Book book = null;

        String BOOK_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = ?",
                        TABLE_BOOKS, KEY_BOOK_ID);

        Cursor cursor = db.rawQuery(BOOK_SELECT_QUERY, new String[]{String.valueOf(id)});

        try {
            if (cursor.moveToFirst()) {
                book = new Book();
                book.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_ID)));
                book.setTitle(cursor.getString(cursor.getColumnIndex(KEY_BOOK_TITLE)));
                book.setAuthor(cursor.getString(cursor.getColumnIndex(KEY_BOOK_AUTHOR)));
                book.setDescription(cursor.getString(cursor.getColumnIndex(KEY_BOOK_DESCRIPTION)));
                book.setContent(cursor.getString(cursor.getColumnIndex(KEY_BOOK_CONTENT)));
                book.setCoverImage(cursor.getBlob(cursor.getColumnIndex(KEY_BOOK_COVER)));
                book.setCategoryId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_CATEGORY_ID)));
                book.setUserId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_USER_ID)));
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while getting book by id");
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return book;
    }

    public List<Book> getBooksByCategory(int categoryId) {
        List<Book> books = new ArrayList<>();

        String BOOKS_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = ? ORDER BY %s DESC",
                        TABLE_BOOKS, KEY_BOOK_CATEGORY_ID, KEY_BOOK_ID);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(BOOKS_SELECT_QUERY, new String[]{String.valueOf(categoryId)});

        try {
            if (cursor.moveToFirst()) {
                do {
                    Book book = new Book();
                    book.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_ID)));
                    book.setTitle(cursor.getString(cursor.getColumnIndex(KEY_BOOK_TITLE)));
                    book.setAuthor(cursor.getString(cursor.getColumnIndex(KEY_BOOK_AUTHOR)));
                    book.setDescription(cursor.getString(cursor.getColumnIndex(KEY_BOOK_DESCRIPTION)));
                    book.setContent(cursor.getString(cursor.getColumnIndex(KEY_BOOK_CONTENT)));
                    book.setCoverImage(cursor.getBlob(cursor.getColumnIndex(KEY_BOOK_COVER)));
                    book.setCategoryId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_CATEGORY_ID)));
                    book.setUserId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_USER_ID)));
                    books.add(book);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while getting books by category");
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return books;
    }

    public List<Book> searchBooks(String query) {
        List<Book> books = new ArrayList<>();

        String BOOKS_SEARCH_QUERY =
                String.format("SELECT * FROM %s WHERE %s LIKE ? OR %s LIKE ? ORDER BY %s DESC",
                        TABLE_BOOKS, KEY_BOOK_TITLE, KEY_BOOK_AUTHOR, KEY_BOOK_ID);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(BOOKS_SEARCH_QUERY, new String[]{"%" + query + "%", "%" + query + "%"});

        try {
            if (cursor.moveToFirst()) {
                do {
                    Book book = new Book();
                    book.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_ID)));
                    book.setTitle(cursor.getString(cursor.getColumnIndex(KEY_BOOK_TITLE)));
                    book.setAuthor(cursor.getString(cursor.getColumnIndex(KEY_BOOK_AUTHOR)));
                    book.setDescription(cursor.getString(cursor.getColumnIndex(KEY_BOOK_DESCRIPTION)));
                    book.setContent(cursor.getString(cursor.getColumnIndex(KEY_BOOK_CONTENT)));
                    book.setCoverImage(cursor.getBlob(cursor.getColumnIndex(KEY_BOOK_COVER)));
                    book.setCategoryId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_CATEGORY_ID)));
                    book.setUserId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_USER_ID)));
                    books.add(book);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while searching books");
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return books;
    }

    public List<Book> getBooksByUser(int userId) {
        List<Book> books = new ArrayList<>();

        String BOOKS_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = ? ORDER BY %s DESC",
                        TABLE_BOOKS, KEY_BOOK_USER_ID, KEY_BOOK_ID);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(BOOKS_SELECT_QUERY, new String[]{String.valueOf(userId)});

        try {
            if (cursor.moveToFirst()) {
                do {
                    Book book = new Book();
                    book.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_ID)));
                    book.setTitle(cursor.getString(cursor.getColumnIndex(KEY_BOOK_TITLE)));
                    book.setAuthor(cursor.getString(cursor.getColumnIndex(KEY_BOOK_AUTHOR)));
                    book.setDescription(cursor.getString(cursor.getColumnIndex(KEY_BOOK_DESCRIPTION)));
                    book.setContent(cursor.getString(cursor.getColumnIndex(KEY_BOOK_CONTENT)));
                    book.setCoverImage(cursor.getBlob(cursor.getColumnIndex(KEY_BOOK_COVER)));
                    book.setCategoryId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_CATEGORY_ID)));
                    book.setUserId(cursor.getInt(cursor.getColumnIndex(KEY_BOOK_USER_ID)));
                    books.add(book);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while getting books by user");
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return books;
    }
}