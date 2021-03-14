package com.madhanarts.artsmusicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class AlbumDetailsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView albumImage;
    String albumName;
    static ArrayList<MusicFile> albumMusicFiles;
    AlbumDetailsAdapter albumDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);

        recyclerView = findViewById(R.id.album_detail_recycler_view);
        albumImage = findViewById(R.id.album_detail_image);
        albumName = getIntent().getStringExtra("album_name");

        albumMusicFiles = new ArrayList<>();

        int j = 0;
        for (int i = 0; i < MainActivity.musicFiles.size(); i++)
        {
            if (albumName.equals(MainActivity.musicFiles.get(i).getAlbum()))
            {
                albumMusicFiles.add(j, MainActivity.musicFiles.get(i));
                j++;
            }
        }

        byte[] image = getAlbumArt(albumMusicFiles.get(0).getPath());

        if (image != null)
        {
//            Glide.with(this)
//                    .load(image)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .into(albumImage);
            albumImage.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
        }
        else
        {
//            Glide.with(this)
//                    .load(R.drawable.ic_launcher_background)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .into(albumImage);
            albumImage.setImageResource(R.drawable.ic_launcher_background);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (albumMusicFiles.size() > 0)
        {
            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumMusicFiles);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            recyclerView.setAdapter(albumDetailsAdapter);

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