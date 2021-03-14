package com.madhanarts.artsmusicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.madhanarts.artsmusicplayer.ApplicationClass.ACTION_DELETE;
import static com.madhanarts.artsmusicplayer.ApplicationClass.ACTION_NEXT;
import static com.madhanarts.artsmusicplayer.ApplicationClass.ACTION_PLAY;
import static com.madhanarts.artsmusicplayer.ApplicationClass.ACTION_PREVIOUS;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String actionName = intent.getAction();
        Intent serviceIntent = new Intent(context, MusicService.class);

        if (actionName != null)
        {
            serviceIntent.putExtra("action_name", actionName);
            context.startService(serviceIntent);
        }

    }
}
