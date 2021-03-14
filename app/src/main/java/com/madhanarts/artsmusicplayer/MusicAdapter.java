package com.madhanarts.artsmusicplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private Context context;
    static ArrayList<MusicFile> musicFiles;

    public MusicAdapter(Context context, ArrayList<MusicFile> musicFiles)
    {
        this.context = context;
        MusicAdapter.musicFiles = musicFiles;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.music_item, parent, false);

        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, final int position) {

        holder.musicFileName.setText(musicFiles.get(position).getTitle());
        byte[] image = getAlbumArt(musicFiles.get(position).getPath());
        if (image != null)
        {
//            Glide.with(context).asBitmap()
//                    .load(image)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .into(holder.musicMetaImage);
            holder.musicMetaImage.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
        }
        else
        {
//            Glide.with(context)
//                    .load(R.drawable.ic_launcher_background)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .into(holder.musicMetaImage);
            holder.musicMetaImage.setImageResource(R.drawable.ic_launcher_background);

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("sender", "music_fragment");
                intent.putExtra("position", position);
                context.startActivity(intent);

            }
        });

        holder.musicMenuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())
                        {
                            case R.id.ic_menu_delete:
                                Toast.makeText(context, "Delete Clicked", Toast.LENGTH_SHORT).show();
                                deleteFile(position);
                                break;
                        }
                        return true;
                    }
                });
            }
        });

    }

    private void deleteFile(int position) {

//        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                Long.parseLong(musicFiles.get(position).getId()));  // content://
//
//        File file = new File(musicFiles.get(position).getPath());
        boolean isDeleted = true; //file.delete();  // delete your file

        if (isDeleted)
        {
            //context.getContentResolver().delete(contentUri, null, null);
            musicFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, musicFiles.size());
            Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // May be in SD card
            Toast.makeText(context, "Can't be deleted", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public int getItemCount() {
        return musicFiles.size();
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder {

        TextView musicFileName;
        ImageView musicMetaImage, musicMenuMore;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);

            musicFileName = itemView.findViewById(R.id.music_file_name);
            musicMetaImage = itemView.findViewById(R.id.music_image);
            musicMenuMore = itemView.findViewById(R.id.music_menu_more);

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

    void updateMusicFileList(ArrayList<MusicFile> filteredMusicFiles)
    {
        musicFiles = new ArrayList<>();
        musicFiles.addAll(filteredMusicFiles);
        notifyDataSetChanged();

    }

}
