package net.starwarswallpaper.app.android.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.starwarswallpaper.app.android.R;
import net.starwarswallpaper.app.android.models.Wallpaper;
import net.starwarswallpaper.app.android.utils.glide.GlideApp;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WallpapersAdapter extends RecyclerView.Adapter<WallpapersAdapter.ViewHolder> {

    private Context context;
    private List<Wallpaper> wallpapers;
    private WallpaperItemClickListener itemClickListener;

    public WallpapersAdapter(Context context, List<Wallpaper> wallpaperList
            , WallpaperItemClickListener itemClickListener) {
        this.context = context;
        this.wallpapers = wallpaperList;
        this.itemClickListener = itemClickListener;
    }

    public WallpapersAdapter(Context context, WallpaperItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wallpaper, parent, false);

        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wallpaper w = wallpapers.get(position);

        StorageReference thumbnailStorageRef =
                FirebaseStorage.getInstance().getReference(w.getThumbnailPath());
        GlideApp.with(context)
                .load(thumbnailStorageRef)
                .placeholder(R.drawable.placeholder_wallpaper_thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.wallpaperThumbnailImageView);
    }

    @Override
    public int getItemCount() {
        if (wallpapers == null) return 0;
        return wallpapers.size();
    }

    public void addWallpaper(Wallpaper wallpaper) {
        wallpapers.add(wallpaper);
        notifyDataSetChanged();
    }

    public List<Wallpaper> swapWallpapers(List<Wallpaper> wallpaperList) {
        List<Wallpaper> oldWallpapers = this.wallpapers;
        this.wallpapers = wallpaperList;
        notifyDataSetChanged();
        return oldWallpapers;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.iv_wallpaper_thumbnail) public ImageView wallpaperThumbnailImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedItemIndex = getAdapterPosition();
            itemClickListener.onWallpaperItemClicked(clickedItemIndex);
        }
    }

    /**
     * Used in handling items clicks
     */
    public interface WallpaperItemClickListener {
        void onWallpaperItemClicked(int clickedWallpaperIndex);
    }
}
