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

public class AlbumDetailsAdapter extends RecyclerView.Adapter<AlbumDetailsAdapter.AlbumDetailsViewHolder> {

    private Context context;
    private ArrayList<MusicFile> albumFiles;

    public AlbumDetailsAdapter(Context context, ArrayList<MusicFile> albumFiles)
    {
        this.context = context;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public AlbumDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.music_item, parent, false);

        return new AlbumDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumDetailsViewHolder holder, final int position) {

        holder.albumMusicName.setText(albumFiles.get(position).getTitle());
        byte[] image = getAlbumArt(albumFiles.get(position).getPath());
        if (image != null)
        {
//            Glide.with(context).asBitmap()
//                    .load(image)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .into(holder.albumMusicImage);
            holder.albumMusicImage.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
        }
        else
        {
//            Glide.with(context)
//                    .load(R.drawable.ic_launcher_background)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .into(holder.albumMusicImage);
            holder.albumMusicImage.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, PlayerActivity.class);

                intent.putExtra("sender", "album_details");
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public static class AlbumDetailsViewHolder extends RecyclerView.ViewHolder {

        ImageView albumMusicImage;
        TextView albumMusicName;

        public AlbumDetailsViewHolder(@NonNull View itemView) {
            super(itemView);

            albumMusicImage = itemView.findViewById(R.id.music_image);
            albumMusicName = itemView.findViewById(R.id.music_file_name);

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
