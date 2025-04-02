package nfccapstone.nfc;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class ViewVideoFragment extends Fragment {
    private View view;
    private Uri uri;
    private VideoView videoView;
    private int video;
    private int duration;
    private int position = 0;
    private int currentVol;
    private int maxVol;
    public  MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private MediaController mediaController;
    private PowerManager powerManager;
    private Activity videoPlay;
    private TabbedMainActivity activity;

    public static final String debug = "DEBUG_ViewVideoFragment";
    private static final int wait = 500;
    private static final int seekDur = 15000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // obtain the resource ID of the video in the bundle added in newInstance static method
        Bundle bundle = getArguments();
        int video = bundle.getInt("video");
        this.video = video;

        // change orientation of the activity to landscape
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);

        // declare and initalise power manager accordingly
        powerManager = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);

        // refer current activity
        videoPlay = this.getActivity();

        // allow video to be played fullscreen
        getActivity(). getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Log.d(debug, "View video fragment created");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_video, container, false);

        // identify the VideoView widget in the view of the fragment
        videoView = view.findViewById(R.id.videoView);

        // Generate the Uri of the video to be played from the resource ID of the video
        uri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + video);

        // Set the Uri of the video to be played by the VideoView
        videoView.setVideoURI(uri);

        // Add a MediaController at the bottom of the VideoView to handle play/pause/fast forward/backward
        mediaController = new MediaController(getActivity());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // obtain audio service
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        // obtain current volume
        currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // obtain max volume
        Log.d(debug, "current volume: " + currentVol);
        maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.d(debug, "Max volume: " + maxVol);

        // automatically direct back to previous activity when video finished playing
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                getActivity(). getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getActivity().onBackPressed();
            }
        });

        Log.d(debug, "Start video");
        return view;
    }

    // newInstance allows resource ID of the video to be played to be sent to the ViewVideoFragment by
    // putting resource ID of video to be played (in bundle) into ViewVideoFragment
    public static ViewVideoFragment newInstance(int video) {
        ViewVideoFragment fragment = new ViewVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("video", video);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onStart(){
        super.onStart();
        // obtain duration of video when video view is set up
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer = mp;

                duration = videoView.getDuration();
                Log.d(debug, "Duration: " + duration);
            }
        });

        // ensure screen will not blackout while video is playing
        videoView.requestFocus();
        // Start the video
        videoView.start();
    }

    @Override
    public void onPause(){
        Log.d(debug, "pause video");
        Log.d(debug, "screen on? " + powerManager.isScreenOn());
        Log.d(debug, "window focus? " + videoPlay.hasWindowFocus());

        super.onPause();
    }

    @Override
    public void onResume(){
        Log.d(debug, "resume video");
        Log.d(debug, "screen on? " + powerManager.isInteractive());
        Log.d(debug, "window focus? " + videoPlay.hasWindowFocus());

        super.onResume();
    }

    public void pauseVideo(){
        if (mediaPlayer != null){
            Log.d(debug, "media player playing state: " + mediaPlayer.isPlaying());

            // pause video when it is being played, continue playing when it is paused
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }
            else{
                mediaPlayer.start();
            }
            // show media controller for 1s
            mediaController.show(wait);
        }
    }

    public void stopVideo(){
        Log.d(debug, "Stop video");
        if(videoView != null){
            videoView.stopPlayback();
            videoView = null;
        }

        // exit fullscreen mode
        getActivity(). getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // display the mode selection bar
        activity.hideBars();
        activity.mode.setVisibility(View.VISIBLE);
        // go back to activity when Back is pressed
        getActivity().onBackPressed();
    }

    public void fastForwardVideo(){
        Log.d(debug, "Fast forward video");

        // seek the video forward by 10s
        if(videoView.canSeekForward()){
            position = videoView.getCurrentPosition();
            videoView.seekTo(position + seekDur);
            //videoView.start();

            // show media controller
            mediaController.show(wait);
        }
    }

    public void rewindVideo() {
        Log.d(debug, "Rewind video");

        // seek the video backward by 10s
        if (videoView.canSeekBackward()) {
            position = videoView.getCurrentPosition();
            videoView.seekTo(position - seekDur);
            //videoView.start();

            // show media controller
            mediaController.show(wait);
        }
    }

    public void volUpVideo(){
        // increase volume of video
        increaseVolume();
        Log.d(debug, "+volume, new volume = " + currentVol);
    }

    public void volDownVideo(){
        // decrease volume of video
        decreaseVolume();
        Log.d(debug, "-volume, new volume = " + currentVol);
    }

    private void increaseVolume() {
        //if no video playing, return
        if (mediaPlayer == null) return;

        audioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        // obtain current volume
        currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d(debug, "current volume: " + currentVol);

        //if max volume, inform user and return
        if (currentVol == maxVol){
            Toast.makeText(getActivity(), "Max volume", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            if (currentVol == maxVol-1){
                currentVol+=1;
            }
            else{
                currentVol += 2;

                if(currentVol == 0){
                    unmuteAudio();
                }
            }

            // Set media volume level
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVol, AudioManager.FLAG_SHOW_UI);
        }
    }

    private void decreaseVolume() {
        //if no video playing, return
        if (mediaPlayer == null) return;

        audioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        // obtain current volume
        currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d(debug, "current volume: " + currentVol);

        //if muted, inform user and return
        if (currentVol == 0) {
            Toast.makeText(getActivity(), "Muted", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            if (currentVol == 1){
                currentVol -= 1;
                muteAudio();
            }
            else{
                currentVol -= 2;

                // Set media volume level
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVol, AudioManager.FLAG_SHOW_UI);
            }
        }
    }

    private void muteAudio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            audioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            audioManager.setStreamMute(AudioManager.STREAM_RING, true);
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
    }

    private void unmuteAudio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE,0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            audioManager.setStreamMute(AudioManager.STREAM_RING, false);
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
        }
    }
}
