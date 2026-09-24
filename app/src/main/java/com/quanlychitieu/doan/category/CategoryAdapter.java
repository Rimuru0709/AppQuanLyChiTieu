package com.quanlychitieu.doan.category;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.quanlychitieu.doan.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter
        extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryModel category);
    }

    private final List<CategoryModel> categories =
            new ArrayList<>();

    private final OnCategoryClickListener listener;

    public CategoryAdapter(
            OnCategoryClickListener listener
    ) {
        this.listener = listener;
    }

    public void setCategories(
            List<CategoryModel> newCategories
    ) {
        categories.clear();
        categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(
                R.layout.item_category,
                parent,
                false
        );

        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CategoryViewHolder holder,
            int position
    ) {
        CategoryModel category = categories.get(position);
        Context context = holder.itemView.getContext();

        holder.tvCategoryName.setText(
                category.getName()
        );

        int iconResource;

        if (category.isCreateButton()) {
            iconResource =
                    android.R.drawable.ic_input_add;
        } else {
            iconResource =
                    context.getResources().getIdentifier(
                            category.getIcon(),
                            "drawable",
                            context.getPackageName()
                    );

            if (iconResource == 0) {
                iconResource = R.drawable.ic_dot;
            }
        }

        holder.imgCategoryIcon.setImageResource(
                iconResource
        );

        ImageViewCompat.setImageTintList(
                holder.imgCategoryIcon,
                ColorStateList.valueOf(Color.WHITE)
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setShape(
                GradientDrawable.OVAL
        );

        try {
            background.setColor(
                    Color.parseColor(
                            category.getColor()
                    )
            );
        } catch (IllegalArgumentException exception) {
            background.setColor(
                    Color.parseColor("#ADB5BD")
            );
        }

        holder.imgCategoryIcon.setBackground(
                background
        );

        holder.itemView.setOnClickListener(
                view -> listener.onCategoryClick(
                        category
                )
        );
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder
            extends RecyclerView.ViewHolder {

        private final ImageView imgCategoryIcon;
        private final TextView tvCategoryName;

        public CategoryViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            imgCategoryIcon = itemView.findViewById(
                    R.id.imgItemCategoryIcon
            );

            tvCategoryName = itemView.findViewById(
                    R.id.tvItemCategoryName
            );
        }
    }
}