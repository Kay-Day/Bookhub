package com.example.bookhub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookhub.R;
import com.example.bookhub.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    private final Context context;
    private final List<Category> categoryList;
    private final OnCategoryClickListener listener;

    public CategoryAdapter(Context context, List<Category> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        // Set category name
        holder.categoryName.setText(category.getName());

        // Set category icon (thiết lập icon khác nhau dựa trên tên thể loại)
        String categoryName = category.getName().toLowerCase();
        if (categoryName.contains("tiểu thuyết") || categoryName.contains("văn học")) {
            holder.categoryIcon.setImageResource(R.drawable.ic_book);
        } else if (categoryName.contains("khoa học")) {
            holder.categoryIcon.setImageResource(R.drawable.ic_category);
        } else if (categoryName.contains("thiếu nhi")) {
            holder.categoryIcon.setImageResource(R.drawable.ic_book);
            holder.categoryIcon.setColorFilter(context.getResources().getColor(R.color.secondary));
        } else if (categoryName.contains("kỹ năng")) {
            holder.categoryIcon.setImageResource(R.drawable.ic_profile);
        } else {
            // Mặc định
            holder.categoryIcon.setImageResource(R.drawable.ic_book);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void updateData(List<Category> newCategoryList) {
        this.categoryList.clear();
        this.categoryList.addAll(newCategoryList);
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIcon;
        TextView categoryName;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.category_icon);
            categoryName = itemView.findViewById(R.id.category_name);
        }
    }
}