package com.example.bookhub.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookhub.R;
import com.example.bookhub.database.DatabaseHelper;
import com.example.bookhub.models.Book;
import com.example.bookhub.models.Category;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public interface OnBookClickListener {
        void onBookClick(Book book, View view);
    }

    private final Context context;
    private final List<Book> bookList;
    private final int viewType; // 0 for horizontal list, 1 for grid
    private final OnBookClickListener listener;
    private final DatabaseHelper databaseHelper;

    public BookAdapter(Context context, List<Book> bookList, int viewType, OnBookClickListener listener) {
        this.context = context;
        this.bookList = bookList;
        this.viewType = viewType;
        this.listener = listener;
        this.databaseHelper = DatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (this.viewType == 0) {
            // Horizontal list item
            view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        } else {
            // Grid item
            view = LayoutInflater.from(context).inflate(R.layout.item_book_grid, parent, false);
        }
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        // Set book title and author
        holder.bookTitle.setText(book.getTitle());
        holder.bookAuthor.setText(book.getAuthor());

        // Set book category
        Category category = databaseHelper.getCategoryById(book.getCategoryId());
        if (category != null) {
            holder.bookCategory.setText(category.getName());
            book.setCategoryName(category.getName());
        }

        // Set book cover image
        if (book.getCoverImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(book.getCoverImage(), 0, book.getCoverImage().length);
            holder.bookCover.setImageBitmap(bitmap);
        } else {
            // Load placeholder image if no cover is available
            Glide.with(context)
                    .load(R.drawable.ic_book)
                    .into(holder.bookCover);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book, holder.bookCover);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void updateData(List<Book> newBookList) {
        this.bookList.clear();
        this.bookList.addAll(newBookList);
        notifyDataSetChanged();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookTitle, bookAuthor, bookCategory;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookTitle = itemView.findViewById(R.id.book_title);
            bookAuthor = itemView.findViewById(R.id.book_author);
            bookCategory = itemView.findViewById(R.id.book_category);
        }
    }
}