package com.madhanarts.artsmusicplayer;

import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static com.madhanarts.artsmusicplayer.MainActivity.PATH;
import static com.madhanarts.artsmusicplayer.MainActivity.SHOW_MINI_PLAYER;

public class BottomPlayerFragment extends Fragment {

    ImageView bottomPlayerAlbumArt, bottomPlayerNextButton;
    TextView bottomPlayerMusicName, bottomPlayerMusicArtistName;
    FloatingActionButton bottomPlayerPlayPauseButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bottom_player, container, false);

        bottomPlayerAlbumArt = view.findViewById(R.id.bottom_player_album_art);
        bottomPlayerNextButton = view.findViewById(R.id.bottom_player_play_next);
        bottomPlayerMusicName = view.findViewById(R.id.bottom_player_music_name);
        bottomPlayerMusicArtistName = view.findViewById(R.id.bottom_player_music_artist);
        bottomPlayerPlayPauseButton = view.findViewById(R.id.bottom_player_play_pause);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SHOW_MINI_PLAYER)
        {
            if (PATH != null)
            {
                byte[] art = getAlbumArt(PATH);
                bottomPlayerAlbumArt.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
                bottomPlayerMusicName.setText(PATH);
            }
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