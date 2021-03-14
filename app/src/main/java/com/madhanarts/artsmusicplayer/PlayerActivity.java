package com.madhanarts.artsmusicplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

import static com.madhanarts.artsmusicplayer.ApplicationClass.ACTION_NEXT;
import static com.madhanarts.artsmusicplayer.ApplicationClass.ACTION_PLAY;
import static com.madhanarts.artsmusicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.madhanarts.artsmusicplayer.ApplicationClass.CHANNEL_ID_2;


public class PlayerActivity extends AppCompatActivity implements ActionPlaying {

    private TextView playerMusicName, playerMusicArtist, playerDurationPlayed, playerDurationTotal;
    private ImageView playerMusicImage, playerShuffleButton, playerRepeatButton, playerBackButton,
            playerMenuButton, playerPreviousButton, playerNextButton;
    private FloatingActionButton playerPlayPauseButton;
    private SeekBar playerSeekBar;

    int position = -1;


    public static ArrayList<MusicFile> musicFiles = new ArrayList<>();

    Uri uri;

    //MediaPlayer mediaPlayer;

    private Handler handler = new Handler();
    Runnable runnable;

    private Thread playThread, previousThread, nextThread;

    MusicService musicService;

    boolean isBounded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreenMode();
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();

        initViews();
        getIntentMethod();

        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser)
                {
                    musicService.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        updateRunnable();
        handler.postDelayed(runnable, 0);
//        playerDurationPlayed.setText(formattedTime(currentPosition));

        playerShuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.shuffleBoolean)
                {
                    MainActivity.shuffleBoolean = false;
                    playerShuffleButton.setImageResource(R.drawable.ic_shuffle_off);
                }
                else
                {
                    MainActivity.shuffleBoolean = true;
                    playerShuffleButton.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });

        playerRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.repeatBoolean)
                {
                    MainActivity.repeatBoolean = false;
                    playerRepeatButton.setImageResource(R.drawable.ic_repeat_off);
                }
                else
                {
                    MainActivity.repeatBoolean = true;
                    playerRepeatButton.setImageResource(R.drawable.ic_repeat_on);
                }
            }
        });


    }

    private void setFullScreenMode() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private String formattedTime(int currentPosition) {

        String playedTime;

        String seconds = String.valueOf(currentPosition % 60);
        String minutes = String.valueOf(currentPosition / 60);

        if (seconds.length() == 1)
        {
            playedTime = minutes + ":" + "0" + seconds;
        }
        else
        {
            playedTime = minutes + ":" + seconds;
        }
        return playedTime;

    }

    private void getIntentMethod()
    {
        String sender = getIntent().getStringExtra("sender");
        if (sender != null)
        {
            if (sender.equals("album_details"))
            {
                musicFiles = AlbumDetailsActivity.albumMusicFiles;
            }
            else if (sender.equals("notification_action"))
            {
                Log.d("notification", "getting music files");
                musicFiles = MusicService.musicFiles;
            }
            else
            {
                musicFiles = MusicAdapter.musicFiles;
            }
        }

        position = getIntent().getIntExtra("position", -1);

        if (musicFiles != null)
        {
            playerPlayPauseButton.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(musicFiles.get(position).getPath());
        }

        if (!sender.equals("notification_action"))
        {
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra("service_position", position);

            startService(intent);
        }



    }

    private void initViews()
    {
        playerMusicName = findViewById(R.id.player_music_name);
        playerMusicArtist = findViewById(R.id.player_music_artist);
        playerDurationPlayed = findViewById(R.id.player_seekbar_duration_played);
        playerDurationTotal = findViewById(R.id.player_seekbar_duration_total);

        playerMusicImage = findViewById(R.id.player_music_image);
        playerShuffleButton = findViewById(R.id.player_bottom_shuffle);
        playerRepeatButton = findViewById(R.id.player_bottom_repeat);
        playerBackButton = findViewById(R.id.player_back_button);
        playerMenuButton = findViewById(R.id.player_menu_button);
        playerPreviousButton = findViewById(R.id.player_bottom_skip_previous);
        playerNextButton = findViewById(R.id.player_bottom_skip_next);

        playerPlayPauseButton = findViewById(R.id.player_bottom_play_pause);
        playerSeekBar = findViewById(R.id.player_seekbar_seekbar);

    }

    private void setMetaData(Uri uri)
    {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] music_image = retriever.getEmbeddedPicture();

        Bitmap bitmap;

        if (music_image != null)
        {
            bitmap = BitmapFactory.decodeByteArray(music_image, 0, music_image.length);
            imageAnimation(this, playerMusicImage, bitmap);

            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {

                    Palette.Swatch swatch = palette.getDominantSwatch();

                    if (swatch != null)
                    {
                        ImageView gradient = findViewById(R.id.image_view_gradient);
                        RelativeLayout container = findViewById(R.id.player_container);

                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        container.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), 0x00000000});
                        gradient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawableBackground = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), swatch.getRgb()});
                        container.setBackground(gradientDrawableBackground);

                        playerMusicName.setTextColor(swatch.getTitleTextColor());
                        playerMusicArtist.setTextColor(swatch.getBodyTextColor());
                    }
                    else
                    {
                        ImageView gradient = findViewById(R.id.image_view_gradient);
                        RelativeLayout container = findViewById(R.id.player_container);

                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        container.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0x00000000});
                        gradient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawableBackground = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0xff000000});
                        container.setBackground(gradientDrawableBackground);

                        playerMusicName.setTextColor(Color.WHITE);
                        playerMusicArtist.setTextColor(Color.DKGRAY);
                    }
                }
            });

        }
        else
        {
//            Glide.with(this)
//                    .asBitmap()
//                    .load(R.drawable.ic_launcher_background)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .into(playerMusicImage);

            playerMusicImage.setImageResource(R.drawable.ic_launcher_background);

            ImageView gradient = findViewById(R.id.image_view_gradient);
            RelativeLayout container = findViewById(R.id.player_container);

            gradient.setBackgroundResource(R.drawable.gradient_bg);
            container.setBackgroundResource(R.drawable.main_bg);

            playerMusicName.setTextColor(Color.WHITE);
            playerMusicArtist.setTextColor(Color.DKGRAY);

        }

        int durationTotal = Integer.parseInt(musicFiles.get(position).getDuration()) / 1000;
        playerDurationTotal.setText(formattedTime(durationTotal));

    }

    public void imageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap)
    {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);

        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                Glide.with(context)
//                        .load(bitmap)
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
//                        .skipMemoryCache(true)
//                        .into(imageView);

                imageView.setImageBitmap(bitmap);


                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                imageView.startAnimation(animIn);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imageView.startAnimation(animOut);

    }


    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        playThreadButton();
        previousThreadButton();
        nextThreadButton();

        updateRunnable();
        handler.postDelayed(runnable, 0);

    }

    @Override
    protected void onPause() {
        super.onPause();

//        Intent intent = new Intent(this, MusicService.class);
//        stopService(intent);
        unbindService(connection);

        if (runnable != null)
        {
            handler.removeCallbacks(runnable);
        }


    }

    private void previousThreadButton() {
//        previousThread = new Thread()
//        {
//            @Override
//            public void run() {
//                playerPreviousButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        previousButtonClicked();
//                    }
//                });
//            }
//        };
//
//        previousThread.start();

        playerPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousButtonClicked();
            }
        });
    }

    public void previousButtonClicked() {
        if (musicService.isPlaying())
        {
            musicService.stop();
            musicService.release();

            if (MainActivity.shuffleBoolean && !MainActivity.repeatBoolean)
            {
                position = getRandom(musicFiles.size() - 1);
            }
            else if(!MainActivity.shuffleBoolean && !MainActivity.repeatBoolean)
            {
                position = ((position - 1) < 0 ? (musicFiles.size() - 1) : (position - 1));
            }
            // else position will be position

            uri = Uri.parse(musicFiles.get(position).getPath());
            musicService.createMediaPlayer(position);

            if (runnable != null)
            {
                setMetaData(uri);
                playerMusicName.setText(musicFiles.get(position).getTitle());
                playerMusicArtist.setText(musicFiles.get(position).getArtist());
                playerSeekBar.setMax(musicService.getDuration() / 1000);
                playerPlayPauseButton.setBackgroundResource(R.drawable.ic_pause);

                updateRunnable();
                handler.postDelayed(runnable, 0);
            }

            musicService.onCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            musicService.start();

        }
        else
        {
            musicService.stop();
            musicService.release();

            if (MainActivity.shuffleBoolean && !MainActivity.repeatBoolean)
            {
                position = getRandom(musicFiles.size() - 1);
            }
            else if(!MainActivity.shuffleBoolean && !MainActivity.repeatBoolean)
            {
                position = ((position - 1) < 0 ? (musicFiles.size() - 1) : (position - 1));
            }
            // else position will be position

            uri = Uri.parse(musicFiles.get(position).getPath());
            musicService.createMediaPlayer(position);

            if (runnable != null)
            {
                setMetaData(uri);
                playerMusicName.setText(musicFiles.get(position).getTitle());
                playerMusicArtist.setText(musicFiles.get(position).getArtist());
                playerSeekBar.setMax(musicService.getDuration() / 1000);
                playerPlayPauseButton.setBackgroundResource(R.drawable.ic_play);

                updateRunnable();
                handler.postDelayed(runnable, 0);
            }

            musicService.onCompleted();
            musicService.showNotification(R.drawable.ic_play);


        }
    }

    private void playThreadButton() {

//        playThread = new Thread()
//        {
//            @Override
//            public void run() {
//                playerPlayPauseButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        playPauseButtonClicked();
//                    }
//                });
//            }
//        };
//
//        playThread.start();

        playerPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseButtonClicked();
            }
        });

    }

    public void playPauseButtonClicked() {
        if (musicService.isPlaying())
        {
            musicService.showNotification(R.drawable.ic_play);
            musicService.stopForeground(false);

            musicService.pause();

            if (runnable != null)
            {
                playerPlayPauseButton.setImageResource(R.drawable.ic_play);
                playerSeekBar.setMax(musicService.getDuration() / 1000);

                updateRunnable();
                handler.postDelayed(runnable, 0);
            }

        }
        else
        {
            musicService.showNotification(R.drawable.ic_pause);
            musicService.start();
            //musicService.startForeground(2, musicService.notification);
            if (runnable != null)
            {
                playerPlayPauseButton.setImageResource(R.drawable.ic_pause);

                playerSeekBar.setMax(musicService.getDuration() / 1000);

                //handler.removeCallbacks(runnable);
                updateRunnable();
                handler.postDelayed(runnable, 0);
            }
        }

    }

    private void nextThreadButton() {

//        nextThread = new Thread()
//        {
//            @Override
//            public void run() {
//                playerNextButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        nextButtonClicked();
//                    }
//                });
//            }
//        };
//
//        nextThread.start();
        playerNextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextButtonClicked();
                    }
                });

    }

    public void nextButtonClicked() {

        if (musicService.isPlaying())
        {
            musicService.stop();
            musicService.release();

            if (MainActivity.shuffleBoolean && !MainActivity.repeatBoolean)
            {
                position = getRandom(musicFiles.size() - 1);
            }
            else if(!MainActivity.shuffleBoolean && !MainActivity.repeatBoolean)
            {
                position = ((position + 1) % musicFiles.size());
            }
            // else position will be position
            uri = Uri.parse(musicFiles.get(position).getPath());
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            musicService.createMediaPlayer(position);


            if (runnable != null)
            {
                setMetaData(uri);
                playerMusicName.setText(musicFiles.get(position).getTitle());
                playerMusicArtist.setText(musicFiles.get(position).getArtist());
                playerSeekBar.setMax(musicService.getDuration() / 1000);
                playerPlayPauseButton.setBackgroundResource(R.drawable.ic_pause);

                updateRunnable();
                handler.postDelayed(runnable, 0);
            }


            musicService.onCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            musicService.start();

        }
        else
        {
            musicService.stop();
            musicService.release();

            if (MainActivity.shuffleBoolean && !MainActivity.repeatBoolean)
            {
                position = getRandom(musicFiles.size() - 1);
            }
            else if(!MainActivity.shuffleBoolean && !MainActivity.repeatBoolean)
            {
                position = ((position + 1) % musicFiles.size());
            }
            // else position will be position

            uri = Uri.parse(musicFiles.get(position).getPath());
            // mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            musicService.createMediaPlayer(position);

            if (runnable != null)
            {
                setMetaData(uri);
                playerMusicName.setText(musicFiles.get(position).getTitle());
                playerMusicArtist.setText(musicFiles.get(position).getArtist());
                playerSeekBar.setMax(musicService.getDuration() / 1000);
                playerPlayPauseButton.setBackgroundResource(R.drawable.ic_play);

                updateRunnable();
                handler.postDelayed(runnable, 0);
            }

            musicService.onCompleted();
            musicService.showNotification(R.drawable.ic_play);

        }
    }


    private int getRandom(int size)
    {
        Random random = new Random();
        return random.nextInt(size + 1);
    }

    void updateRunnable()
    {
        if (runnable != null)
        {
            handler.removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("music_player", "update runnable");

                if (musicService != null && musicService.isStopped)
                {
                    musicService = null;
                    handler.removeCallbacks(this);
                }

                if (musicService != null)
                {
                    int currentPosition = musicService.getCurrentPosition() / 1000;
                    playerSeekBar.setProgress(currentPosition);
                    playerDurationPlayed.setText(formattedTime(currentPosition));
                }
                handler.postDelayed(this, 500);
            }
        };
    }


    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
            musicService = myBinder.getService();
            musicService.setCallBack(PlayerActivity.this);
            Log.d("music_service", "Service Connected...");

            playerSeekBar.setMax(musicService.getDuration() / 1000);
            setMetaData(uri);

            playerMusicName.setText(musicFiles.get(position).getTitle());
            playerMusicArtist.setText(musicFiles.get(position).getArtist());
            musicService.onCompleted();
            musicService.showNotification(R.drawable.ic_pause);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("music_service", "Service Disconnected...");
            musicService = null;
        }
    };

//    @Override
//    public void onServiceConnected(ComponentName name, IBinder service) {
//
//        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
//        musicService = myBinder.getService();
//        musicService.setCallBack(this);
//        Log.d("music_service", "Service Connected...");
//
//        playerSeekBar.setMax(musicService.getDuration() / 1000);
//        setMetaData(uri);
//
//        playerMusicName.setText(musicFiles.get(position).getTitle());
//        playerMusicArtist.setText(musicFiles.get(position).getArtist());
//        musicService.onCompleted();
//        musicService.showNotification(R.drawable.ic_pause);
//    }
//
//    @Override
//    public void onServiceDisconnected(ComponentName name) {
//        Log.d("music_service", "Service Disconnected...");
//        musicService = null;
//    }

    @Override
    protected void onStop() {
        Toast.makeText(this, "App closed...", Toast.LENGTH_SHORT).show();
        handler.removeCallbacks(runnable);
        runnable = null;
        super.onStop();
    }
}