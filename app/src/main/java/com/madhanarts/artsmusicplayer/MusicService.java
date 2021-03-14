package com.madhanarts.artsmusicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

import static com.madhanarts.artsmusicplayer.ApplicationClass.ACTION_DELETE;
import static com.madhanarts.artsmusicplayer.ApplicationClass.ACTION_NEXT;
import static com.madhanarts.artsmusicplayer.ApplicationClass.ACTION_PLAY;
import static com.madhanarts.artsmusicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.madhanarts.artsmusicplayer.ApplicationClass.CHANNEL_ID_2;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    MyBinder myBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    static ArrayList<MusicFile> musicFiles;
    Uri uri;

    int position = -1;

    ActionPlaying actionPlaying;
    private MediaSessionCompat mediaSessionCompat;

    Notification notification;

    boolean isStopped = false;

    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String MUSIC_NAME = "MUSIC_NAME";
    public static final String MUSIC_ARTIST_NAME = "MUSIC_ARTIST_NAME";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("music_service", "on create");
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("music_service", "on bind");
        return myBinder;
    }

    public class MyBinder extends Binder
    {
        MusicService getService()
        {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int startPosition = intent.getIntExtra("service_position", -1);

        String actionName = intent.getStringExtra("action_name");

        if (startPosition != -1)
        {
            playMedia(startPosition);
        }

        if (actionName != null)
        {
            switch (actionName)
            {
                case ACTION_PLAY:
                    Toast.makeText(this, "Play Pause", Toast.LENGTH_SHORT).show();

                    if (actionPlaying != null)
                    {
                        Log.d("music_service", "Play pause clicked");
                        actionPlaying.playPauseButtonClicked();
                    }
                    break;

                case ACTION_NEXT:
                    Toast.makeText(this, "Next", Toast.LENGTH_SHORT).show();
                    if (actionPlaying != null)
                    {
                        Log.d("music_service", "next clicked");
                        actionPlaying.nextButtonClicked();
                    }
                    break;

                case ACTION_PREVIOUS:
                    Toast.makeText(this, "Previous", Toast.LENGTH_SHORT).show();
                    if (actionPlaying != null)
                    {
                        Log.d("music_service", "previous clicked");
                        actionPlaying.previousButtonClicked();
                    }
                    break;

                case ACTION_DELETE:
                    Toast.makeText(this, "Delete", Toast.LENGTH_SHORT).show();
                    if (actionPlaying != null)
                    {
                        Log.d("music_service", "delete clicked");
                        //stopService(new Intent(this, MusicService.class));
                        isStopped = true;

                        if (mediaPlayer.isPlaying())
                        {
                            mediaPlayer.stop();
                        }
                        mediaPlayer.reset();
                        release();
                        mediaPlayer = null;

                        Intent i = new Intent(getBaseContext(), MusicService.class);
                        stopService(i);

                    }
                    break;

            }
        }

        return START_STICKY;
    }

    void playMedia(int startPosition)
    {
        musicFiles = PlayerActivity.musicFiles;
        position = startPosition;
        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (musicFiles != null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else
        {
            createMediaPlayer(position);
            mediaPlayer.start();
        }

    }

    void start()
    {
        mediaPlayer.start();
    }

    boolean isPlaying()
    {
        return mediaPlayer.isPlaying();
    }

    void stop()
    {
        mediaPlayer.stop();
    }

    void release()
    {
        mediaPlayer.release();
    }

    int getDuration()
    {
        return mediaPlayer.getDuration();
    }

    void seekTo(int position)
    {
        mediaPlayer.seekTo(position);
    }

    int getCurrentPosition()
    {
        return mediaPlayer.getCurrentPosition();
    }


    void pause() {
        mediaPlayer.pause();
    }

    void createMediaPlayer(int positionInner)
    {
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, uri.toString());
        //editor.putString(MUSIC_NAME, )
        editor.apply();


        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    void onCompleted()
    {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (actionPlaying != null)
        {
            actionPlaying.nextButtonClicked();
            if (mediaPlayer != null)
            {
                createMediaPlayer(position);
                start();
                onCompleted();
            }
        }

    }

    void setCallBack(ActionPlaying actionPlaying)
    {
        this.actionPlaying = actionPlaying;
    }

    void showNotification(int playPauseButton)
    {
        Intent contentIntent = new Intent(this, PlayerActivity.class);
        contentIntent.putExtra("sender", "notification_action");
        contentIntent.putExtra("position", position);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this, 0, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_DELETE);
        PendingIntent deletePending = PendingIntent.getBroadcast(this, 0, deleteIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);


        byte[] picture;
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb;
        if (picture != null)
        {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        }
        else
        {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
        }

        notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseButton)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_skip_previous, "Previous", prevPending)
                .addAction(playPauseButton, "Pause", pausePending)
                .addAction(R.drawable.ic_skip_next, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentPendingIntent)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(deletePending)
                .build();

//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.notify(0, notification);

        startForeground(2, notification);


    }


    private byte[] getAlbumArt(String uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] musicImage = retriever.getEmbeddedPicture();
        retriever.release();
        return musicImage;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("music_service", "On unbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("music_service", "on task removed");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.d("music_service", "on destroy");
        super.onDestroy();
    }
}
