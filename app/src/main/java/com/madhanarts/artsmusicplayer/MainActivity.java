package com.madhanarts.artsmusicplayer;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import javax.crypto.ShortBufferException;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final int REQUEST_CODE = 1;
    static ArrayList<MusicFile> musicFiles;
    static ArrayList<MusicFile> albums = new ArrayList<>();

    SongsFragment songsFragment;
    AlbumFragment albumFragment;

    static boolean shuffleBoolean = false, repeatBoolean = false;

    private String MY_SORT_PREF = "sort_order";

    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String MUSIC_NAME = "MUSIC_NAME";
    public static final String MUSIC_ARTIST_NAME = "MUSIC_ARTIST_NAME";
    public static boolean SHOW_MINI_PLAYER = false;

    public static String PATH = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_options_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.ic_menu_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        SharedPreferences.Editor editor = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE).edit();
        switch (item.getItemId())
        {
            case R.id.ic_menu_sort_name:
                editor.putString("sorting", "sort_by_name");
                editor.apply();
                this.recreate();
                break;

            case R.id.ic_menu_sort_date:
                editor.putString("sorting", "sort_by_date");
                editor.apply();
                this.recreate();
                break;


            case R.id.ic_menu_sort_size:
                editor.putString("sorting", "sort_by_size");
                editor.apply();
                this.recreate();
                break;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permission();

    }

    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        }
        else
        {
            musicFiles = getAllAudio(this);
            initViewPager();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // Permission granted
                musicFiles = getAllAudio(this);
                initViewPager();
            }
            else
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE);
            }
        }

    }

    public ArrayList<MusicFile> getAllAudio(Context context)
    {

        SharedPreferences preferences = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE);
        String sortOrder = preferences.getString("sorting", "sort_by_name");
        String order = null;


        ArrayList<String> duplicate = new ArrayList<>();
        ArrayList<MusicFile> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        albums.clear();

        switch (sortOrder)
        {
            case "sort_by_name":
                order = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                break;

            case "sort_by_date":
                order = MediaStore.MediaColumns.DATE_ADDED + " ASC";
                break;

            case "sort_by_size":
                order = MediaStore.MediaColumns.SIZE + " DESC";
                break;
        }

        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, order);
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);

                MusicFile musicFile = new MusicFile(id, path, title, artist, album, duration);

                Log.d("music_file", "\n" + id + "\n" + album + "\n" +
                        title +"\n" + duration + "\n" + path + "\n" + artist);
                tempAudioList.add(musicFile);

                if (!duplicate.contains(album))
                {
                    albums.add(musicFile);
                    duplicate.add(album);
                }

            }
            cursor.close();
        }
        return tempAudioList;
    }

    private void initViewPager() {
        ViewPager viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        songsFragment = new SongsFragment();
        albumFragment = new AlbumFragment();
        viewPagerAdapter.addFragment(songsFragment, "Songs");
        viewPagerAdapter.addFragment(albumFragment, "Album");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter
    {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();

        }

        void addFragment(Fragment fragment, String title)
        {
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String searchText = newText.toLowerCase();
        ArrayList<MusicFile> filteredMusicFiles = new ArrayList<>();
        for (MusicFile musicFile: musicFiles)
        {
            if (musicFile.getTitle().toLowerCase().contains(searchText))
            {
                filteredMusicFiles.add(musicFile);
            }
        }
        songsFragment.musicAdapter.updateMusicFileList(filteredMusicFiles);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE);
        String value = preferences.getString(MUSIC_FILE, null);
        if (value != null)
        {
            SHOW_MINI_PLAYER = true;
            PATH = value;
        }
        else
        {
            SHOW_MINI_PLAYER = false;
            PATH = null;
        }

    }
}