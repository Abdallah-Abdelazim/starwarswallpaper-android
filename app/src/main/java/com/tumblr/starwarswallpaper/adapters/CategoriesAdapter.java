package com.tumblr.starwarswallpaper.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tumblr.starwarswallpaper.R;
import com.tumblr.starwarswallpaper.models.Category;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private List<Category> categoriesList;
    private int selectedPos;
    private CategoryItemClickListener itemClickListener;

    public CategoriesAdapter(List<Category> categoriesList, int currentSelectedPosition
            , CategoryItemClickListener itemClickListener) {
        this.categoriesList = categoriesList;
        this.selectedPos = currentSelectedPosition;
        this.itemClickListener = itemClickListener;
    }

    public CategoriesAdapter(int currentSelectedPosition, CategoryItemClickListener itemClickListener) {
        this.selectedPos = currentSelectedPosition;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nav_category, parent, false);

        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.categoryNameTextView.setText(categoriesList.get(position).getName());

        holder.itemView.setSelected(position == selectedPos);
    }

    @Override
    public int getItemCount() {
        if (categoriesList == null) return 0;
        return categoriesList.size();
    }

    public List<Category> swapCategoriesList(List<Category> categories) {
        List<Category> oldCategories = categoriesList;

        categoriesList = categories;
        notifyDataSetChanged();

        return oldCategories;
    }

    public int getSelectedPosition() {
        return this.selectedPos;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_category_name) TextView categoryNameTextView;

        /**
         * @param itemView root View (ViewGroup) of the RecyclerView item
         */
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int oldPos = selectedPos;
            selectedPos = getLayoutPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPos);

            int clickedItemIndex = getAdapterPosition();
            itemClickListener.onCategoryItemClicked(clickedItemIndex);
        }
    }

    /**
     * Used in handling items clicks.
     */
    public interface CategoryItemClickListener {
        void onCategoryItemClicked(int clickedItemIndex);
    }

}
