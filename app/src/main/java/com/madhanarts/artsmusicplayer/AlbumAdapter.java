package com.madhanarts.artsmusicplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private Context context;
    private ArrayList<MusicFile> albumFiles;

    public AlbumAdapter(Context context, ArrayList<MusicFile> albumFiles)
    {
        this.context = context;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false);

        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, final int position) {

        holder.albumName.setText(albumFiles.get(position).getAlbum());
        byte[] image = getAlbumArt(albumFiles.get(position).getPath());
        if (image != null)
        {
//            Glide.with(context).asBitmap()
//                    .load(image)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .into(holder.albumImage);

            holder.albumImage.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));

        }
        else
        {
//            Glide.with(context)
//                    .load(R.drawable.ic_launcher_background)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .into(holder.albumImage);

            holder.albumImage.setImageResource(R.drawable.ic_launcher_background);

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AlbumDetailsActivity.class);
                intent.putExtra("album_name", albumFiles.get(position).getAlbum());
                context.startActivity(intent);

            }
        });



    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {

        ImageView albumImage;
        TextView albumName;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);

            albumImage = itemView.findViewById(R.id.album_image);
            albumName = itemView.findViewById(R.id.album_name);

        }
    }

    private byte[] getAlbumArt(String uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] musicImage = retriever.getEmbeddedPicture();
        retriever.release();
        return musicImage;
    }

}
