package com.madhanarts.artsmusicplayer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.madhanarts.artsmusicplayer.MainActivity.albums;
import static com.madhanarts.artsmusicplayer.MainActivity.musicFiles;


public class AlbumFragment extends Fragment {

    RecyclerView recyclerView;
    AlbumAdapter albumAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        recyclerView = view.findViewById(R.id.album_recycler_view);
        recyclerView.setHasFixedSize(true);

        if (albums.size() > 0)
        {
            albumAdapter = new AlbumAdapter(getContext(), albums);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            recyclerView.setAdapter(albumAdapter);
        }


        return view;
    }
}