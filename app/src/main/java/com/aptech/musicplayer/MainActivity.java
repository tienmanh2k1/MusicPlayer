package com.aptech.musicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aptech.musicplayer.Adapter.SongAdapter;
import com.chibde.visualizer.BarVisualizer;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.jgabrielfreitas.core.BlurImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    SongAdapter songAdapter;
    List<Song> allSong = new ArrayList<>();
    ActivityResultLauncher<String> storagePermissionLauncher;
    final String permission = android.Manifest.permission.READ_EXTERNAL_STORAGE;

    //today
    ExoPlayer player;
    public ActivityResultLauncher<String> recordAudioPermissionLauncher; //to be accessed in the song adapter
    final String recordAudioPermission = Manifest.permission.RECORD_AUDIO;
    ConstraintLayout playerView;
    //controls
    TextView songNameView,skipPreviousBtn,skipNextBtn,playPauseBtn,repeatModeBtn,playListBtn,playerCloseBtn;
    TextView homeSongNameView,homeSkipPreviousBtn,homeSkipNextBtn,homePlayPauseBtn;
    //wrappers
    ConstraintLayout homeControlWrapper,headWrapper,artWorkWrapper,seekBarWrapper,controlWrapper,audioVisualizerWrapper;
    //artwork
    CircleImageView artworkView;
    //seek bar
    SeekBar seekBar;
    TextView processView,durationView;
    //audio visualizer
    BarVisualizer audioVisualizer;
    //blur image view
    BlurImageView blurImageView;
    //status bar  & navigation color
    int defaultStatusColor;
    //repeat mode
    int repeatMode = 1;
    //is the act. bound?
    boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //save the status color
        defaultStatusColor = getWindow().getStatusBarColor();
        //set the navigation color
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));// 0-255

        //set tool bar,and app title
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));

        //recyclerview
        recyclerView = findViewById(R.id.recyclerView);
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted-> {
            if (granted){
                //fetch song
                fetchSong();
            }else {
                userResponses();
            }
        });


        //record audio permission
        recordAudioPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted->{
            if (granted && player.isPlaying()){
                activateAudioVisualizer();
            }else {

                userResponsesOnRecordAudioPerm();
            }
        });

        //view
        //player = new ExoPlayer.Builder(this).build();
        playerView = findViewById(R.id.playerView);
        playerCloseBtn = findViewById(R.id.playerCloseBtn);
        songNameView = findViewById(R.id.songNameView);
        skipPreviousBtn = findViewById(R.id.skipPreviousBtn);
        skipNextBtn = findViewById(R.id.skipNextBtn);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        repeatModeBtn = findViewById(R.id.repeatModeBtn);
        playListBtn = findViewById(R.id.playListBtn);

        homeSongNameView = findViewById(R.id.homeSongNameView);
        homeSkipPreviousBtn = findViewById(R.id.homeSkipPreviousBtn);
        homeSkipNextBtn = findViewById(R.id.homeSkipNextBtn);
        homePlayPauseBtn = findViewById(R.id.homePLayPauseBtn);

        //wrappers
        homeControlWrapper = findViewById(R.id.homeControllerWrapper);
        headWrapper = findViewById(R.id.headWrapper);
        artWorkWrapper = findViewById(R.id.artworkWrapper);
        seekBarWrapper = findViewById(R.id.seekBarWrapper);
        controlWrapper = findViewById(R.id.controlWrapper);
        audioVisualizerWrapper = findViewById(R.id.audioVisualizerWrapper);

        //artwork
        artworkView = findViewById(R.id.artworkView);
        //seek bar
        seekBar = findViewById(R.id.seekbar);
        processView = findViewById(R.id.progressView);
        durationView = findViewById(R.id.durationView);

        //audio visualizer
        audioVisualizer = findViewById(R.id.visualizer);

        //blur image view
        blurImageView = findViewById(R.id.blurImageView);




        //launch storage permission on create
        //storagePermissionLauncher.launch(permission);

        //player controls method
        //playerControls();

        //bind to the player service , and do every thing after binding
        doBinderService();
    }

    private void doBinderService() {
            Intent playerServiceIntent = new Intent(this,PlayerService.class);
        bindService(playerServiceIntent,playerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    ServiceConnection playerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            //get the service instance
            PlayerService.ServiceBinder serviceBinder = (PlayerService.ServiceBinder) iBinder;
            player = serviceBinder.getPlayerService().player;
            isBound = true;

            //ready to show song
            storagePermissionLauncher.launch(permission);
            //call player control method
            playerControls();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onBackPressed() {
        //we say if the player view is visible ,close it
        if (playerView.getVisibility() == View.VISIBLE){
            exitPlayerView();
        }else {
            super.onBackPressed();
        }
    }

    private void playerControls() {
        //song name marquee
        songNameView.setSelected(true);
        homeSongNameView.setSelected(true);

        //exit the player view
        playerCloseBtn.setOnClickListener(view -> exitPlayerView());
        playListBtn.setOnClickListener(view -> exitPlayerView());
        //open player view on home control wrapper click
        homeControlWrapper.setOnClickListener(view -> showPlayerView());

        //player listener
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                //show the playing song title
                assert mediaItem != null;
                songNameView.setText(mediaItem.mediaMetadata.title);
                homeSongNameView.setText(mediaItem.mediaMetadata.title);

                processView.setText(getReadableTime((int)player.getCurrentPosition()));
                seekBar.setProgress((int) player.getCurrentPosition());
                seekBar.setMax((int) player.getDuration());
                durationView.setText(getReadableTime((int) player.getDuration()));
                playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_circle_outline,0,0,0);
                homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);

                //show the current art work
                showCurrentArtWork();

                //update the process position of a current playing song
                updatePlayerPositionProcess();

                //load artwork animation
                artworkView.setAnimation(loadRotation());

                //set audio visualizer
                activateAudioVisualizer();

                //update player view color
                updatePlayerColors();

                if (player.isPlaying()){
                    player.play();
                }

            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == ExoPlayer.STATE_READY){
                    //set values to player view
                    songNameView.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    homeSongNameView.setText(player.getCurrentMediaItem().mediaMetadata.title);
                    processView.setText(getReadableTime((int) player.getCurrentPosition()));
                    durationView.setText(getReadableTime((int) player.getDuration()));
                    seekBar.setMax((int) player.getDuration());
                    seekBar.setProgress((int) player.getCurrentPosition());

                    playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_circle_outline,0,0,0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);

                    //show the current art work
                    showCurrentArtWork();

                    //update the process position of a current playing song
                    updatePlayerPositionProcess();

                    //load artwork animation
                    artworkView.setAnimation(loadRotation());

                    //set audio visualizer
                    activateAudioVisualizer();

                    //update player view color
                    updatePlayerColors();
                }
                else {
                    playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_circle_outline,0,0,0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play,0,0,0);
                }
            }
        });

        //skip to next track
        skipNextBtn.setOnClickListener(view -> skipToNextSong());
        homeSkipNextBtn.setOnClickListener(view -> skipToNextSong());
        //skip to previous track
        skipPreviousBtn.setOnClickListener(view -> skipToPreviousSong());
        homeSkipPreviousBtn.setOnClickListener(view -> skipToPreviousSong());

        //play or pause the player
        playPauseBtn.setOnClickListener(view -> playOrPausePlayer());
        homePlayPauseBtn.setOnClickListener(view -> playOrPausePlayer());

        //seekbar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int processValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                processValue = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player.getPlaybackState() == ExoPlayer.STATE_READY){
                    seekBar.setProgress(processValue);
                    processView.setText(getReadableTime(processValue));
                    player.seekTo(processValue);
                }
            }
        });

        //repeat mode
        repeatModeBtn.setOnClickListener(view -> {
            if (repeatMode == 1){
                //repeat one
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
                repeatMode = 2;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_one,0,0,0);
            }else if(repeatMode == 2) {
                //shuffle all
                player.setShuffleModeEnabled(true);
                player.setRepeatMode(Player.REPEAT_MODE_ALL);
                repeatMode = 3;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shuffle,0,0,0);
            }else if(repeatMode == 3){
                //repeat all
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                player.setShuffleModeEnabled(false);
                repeatMode = 1;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat,0,0,0);
            }

            //update color
            updatePlayerColors();
        });
    }

    public void playOrPausePlayer() {
        if (player.isPlaying()){
            player.pause();
            playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_circle_outline,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play,0,0,0);
            artworkView.clearAnimation();
        }else {
            player.play();
            playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_circle_outline,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);
            artworkView.startAnimation(loadRotation());
        }

        //update player color
        updatePlayerColors();
    }

    private void skipToNextSong() {
        if (player.hasNextMediaItem()){
            player.seekToNext();
        }
    }

    private void skipToPreviousSong() {
        if (player.hasPreviousMediaItem()){
            player.seekToPrevious();
        }
    }

    private Animation loadRotation() {
        RotateAnimation rotateAnimation = new RotateAnimation(0,360,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        return rotateAnimation;
    }

    private void updatePlayerPositionProcess() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player.isPlaying()){
                    processView.setText(getReadableTime((int) player.getCurrentPosition()));
                    seekBar.setProgress((int) player.getCurrentPosition());

                    //repeat calling the method
                    updatePlayerPositionProcess();
                }
            }
        },1000);
    }

    private void showCurrentArtWork() {
       // artworkView.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);
        Picasso.get().load(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri).into(artworkView);
        if (artworkView.getDrawable() == null){
            artworkView.setImageResource(R.drawable.default_bg);
        }
    }

    private String getReadableTime(int totalDuration) {

        String time;

        int hrs = totalDuration/(1000*60*60);
        int min = (totalDuration%(1000*60*60))/(1000*60);
        int secs = ((totalDuration%(1000*60*60))%(1000*60))/1000;

        if (hrs < 1){time = min + " : " + secs;}
        else {time = hrs + " : " + min + " : " + secs;}

        return time;
    }

    private void showPlayerView() {
        playerView.setVisibility(View.VISIBLE);
        updatePlayerColors();
    }

    private void updatePlayerColors() {
        //only player view visible
        if (playerView.getVisibility() == View.GONE )
            return;

        BitmapDrawable bitmapDrawable = (BitmapDrawable) artworkView.getDrawable();
        if (bitmapDrawable == null){
            bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(this,R.drawable.artwork);

        }
        assert bitmapDrawable != null;
        Bitmap bitmap = bitmapDrawable.getBitmap();

        //set bitmap to blur image view
        blurImageView.setImageBitmap(bitmap);
        blurImageView.setBlur(5);

        //player control color
        Palette.from(bitmap).generate(palette -> {
           if (palette != null){
               Palette.Swatch swatch = palette.getDarkVibrantSwatch();
               if (swatch == null){
                   swatch = palette.getMutedSwatch();
                   if (swatch == null){
                       swatch = palette.getDominantSwatch();
                   }
               }

               //extract text colors
               assert swatch != null;
               int titleTextColor = swatch.getTitleTextColor();
               int bodyTextColor = swatch.getBodyTextColor();
               int rgbColor = swatch.getRgb();

               //setColor to player view
               //status & navigation bar colors
               getWindow().setStatusBarColor(rgbColor);
               getWindow().setNavigationBarColor(rgbColor);

               //more view color
               songNameView.setTextColor(titleTextColor);
               playerCloseBtn.getCompoundDrawables()[0].setTint(titleTextColor);
               processView.setTextColor(bodyTextColor);
               durationView.setTextColor(bodyTextColor);

               repeatModeBtn.getCompoundDrawables()[0].setTint(rgbColor);
               skipPreviousBtn.getCompoundDrawables()[0].setTint(rgbColor);
               skipNextBtn.getCompoundDrawables()[0].setTint(rgbColor);
               playPauseBtn.getCompoundDrawables()[0].setTint(rgbColor);
               playListBtn.getCompoundDrawables()[0].setTint(rgbColor);
           }
        });
    }

    private void exitPlayerView() {
        playerView.setVisibility(View.GONE);
        getWindow().setStatusBarColor(defaultStatusColor);
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199)); //0 and 255
    }

    private void userResponsesOnRecordAudioPerm() {
        if (shouldShowRequestPermissionRationale(recordAudioPermission)){
            //show an educational UI explaining why we need this permission
             //use alert dialog
            new AlertDialog.Builder(this)
                    .setTitle("Requesting to show Audio visualizer")
                    .setMessage("Allow tis app to do display audio visualizer when music is playing")
                    .setPositiveButton("allow", (dialog, which) -> {
                        //request the perm
                        recordAudioPermissionLauncher.launch(recordAudioPermission);
                    })
                    .setNegativeButton("No",(dialogInterface,i)->{
                        Toast.makeText(getApplicationContext(),"you denied to show the audio visualizer",Toast.LENGTH_SHORT).show();
                    })
                    .show();
        }else {
            Toast.makeText(getApplicationContext(),"you denied to show the audio visualizer",Toast.LENGTH_SHORT).show();
        }
    }

    //audio visualizer
    private void activateAudioVisualizer() {
        //check if we have record audio permission to show an audio visualizer
        if (ContextCompat.checkSelfPermission(this,recordAudioPermission) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        //set color the audio visualizer
        audioVisualizer.setColor(ContextCompat.getColor(this,R.color.secondary_color));
        //set number of visualizer btn 10 & 256
        audioVisualizer.setDensity(20);
        //set the audio session id from the player
        audioVisualizer.setPlayer(player.getAudioSessionId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //release the player
//        if (player.isPlaying()){
//            player.stop();
//        }
//        player.release();

        doUnbindService();
    }

    private void doUnbindService() {
        if (isBound){
            unbindService(playerServiceConnection);
            isBound = false;
        }
    }

    private void userResponses() {
        if (ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED){
            //fetch song
            fetchSong();
        } else {
            if (shouldShowRequestPermissionRationale(permission)){
                //show an education UI to user explaining why we need this permission
                //user alert dialog
                new AlertDialog.Builder(this)
                        .setTitle("Requesting permission")
                        .setMessage("Allow us to fetch song on your device")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //request permission
                                storagePermissionLauncher.launch(permission);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(),"You denied us to show songs",Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }).show();
            }
        }
    }

    private void fetchSong() {

        //define a list cary songs
        List<Song> songs = new ArrayList<>();
        Uri mediaStoreUri ;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            mediaStoreUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        }else {
            mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        //define projection
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST,
        };

        //oder
        String sortOder = MediaStore.Audio.Media.DATE_ADDED + " DESC";
        String selection = MediaStore.Audio.Media.DATA + " like ?";
        String[] selectionArgs = new String[]{"%Music%"};

        //get the songs
        try (Cursor cursor = getContentResolver().query(mediaStoreUri,projection,selection,selectionArgs,sortOder)) {
            //cache cursor indices
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);

            //clear the previous loaded before adding loading again
            while (cursor.moveToNext()){
                   //get the values of a column for a given audio file
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);
                long albumId = cursor.getLong(albumColumn);
                String artist = cursor.getString(artistColumn);

                //song uri
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id);

                //album artwork uri
                Uri albumArtworkUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);

                //remove .mp3 extension from the song name
                name = name.substring(0,name.lastIndexOf("."));

                //song item
                Song song = new Song(name,uri,albumArtworkUri,duration,size,artist);

                //add song to list
                songs.add(song);
            }
                //display song
                showSongs(songs);
        }catch (Exception e){
            System.out.println(e);
        }

    }

    private void showSongs(List<Song> songs) {
        if (songs.size() == 0){
            Toast.makeText(this,"No Songs",Toast.LENGTH_SHORT).show();
            return;
        }

        //save song
        allSong.clear();
        allSong.addAll(songs);

        //update the tool bar title
        String title = getResources().getString(R.string.app_name) + " - " + songs.size();
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);

        //layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        //song adapter
        songAdapter = new SongAdapter(this,songs,player,playerView);

        //set the adapter recycleView
        recyclerView.setAdapter(songAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_btn,menu);
        //search btn item
        MenuItem menuItem = menu.findItem(R.id.search_btn);
        SearchView searchView = (SearchView) menuItem.getActionView();

        //search song method
        SearchSong(searchView);

        return super.onCreateOptionsMenu(menu);
    }

    private void SearchSong(SearchView searchView) {

        //search view listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //filler song
                fillerSongs(newText.toLowerCase());
                return true;
            }
        });
    }

    private void fillerSongs(String query) {
        List<Song> fillerListSong = new ArrayList<>();

        if (allSong.size() > 0){
            for (Song song : allSong){
                if (song.getTitle().toLowerCase().contains(query)){
                    fillerListSong.add(song);
                }
            }

            if (songAdapter != null){
                songAdapter.filter(fillerListSong);
            }
        }
    }

}