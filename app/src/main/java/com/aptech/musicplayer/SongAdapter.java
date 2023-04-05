package com.aptech.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //members
    Context context;
    List<Song> songs;
    ExoPlayer player;
    ConstraintLayout playerView;

    public SongAdapter(Context context, List<Song> songs,ExoPlayer player,ConstraintLayout playerView) {
        this.context = context;
        this.songs = songs;
        this.player = player;
        this.playerView = playerView;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate song row item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row_item,parent,false);
        return new SongViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        //current song view holder
        Song song = songs.get(position);
        SongViewHolder viewHolder = (SongViewHolder) holder;


        //set values to view
        viewHolder.titleHolder.setText(song.getTitle());
        viewHolder.durationHolder.setText(getDuration(song.getDuration()));
        viewHolder.sizeHolder.setText(getSize(song.getSize()));
        viewHolder.artistHolder.setText(song.getArtist());

        //artwork
        Uri artworkUri = song.getArtworkUri();

        if (artworkUri != null){
            //set the uri to image view
            //viewHolder.artworkHolder.setImageURI(artworkUri);
            Picasso.get().load(artworkUri).into(viewHolder.artworkHolder);

            //make sure that uri has an work
            if (viewHolder.artworkHolder.getDrawable() == null){
               viewHolder.artworkHolder.setImageResource(R.drawable.song_bg);
            }
        }

        //player song on item click
        viewHolder.itemView.setOnClickListener(view -> {
            //start player service
            context.startService(new Intent(context.getApplicationContext(),PlayerService.class));
            //playing song
            if (!player.isPlaying()){
                player.setMediaItems(getMediaItem(),position,0);
            }else {
                player.pause();
                player.seekTo(position,0);
            }
            //prepare and play
            player.prepare();
            player.play();

            Toast.makeText(context,song.getTitle(),Toast.LENGTH_SHORT).show();

            //show player view
            playerView.setVisibility(View.VISIBLE);

            //check if the record audio permission is granted
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                //request the record audio perm
                ((MainActivity)context).recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            };
        });
    }

    private List<MediaItem> getMediaItem() {
        //define a list of media items
        List<MediaItem> mediaItems = new ArrayList<>();

        for (Song song: songs){
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getUri())
                    .setMediaMetadata(getMetadata(song))
                    .build();


            //add the media item to media items list
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    private MediaMetadata getMetadata(Song song) {
        return new MediaMetadata.Builder()
                .setTitle(song.getTitle())
                .setArtist(song.getArtist())
                .setArtworkUri(song.getArtworkUri())
                .build();
    }

    //View holder
    public static class SongViewHolder extends RecyclerView.ViewHolder{
        //members
        ImageView artworkHolder;
        TextView titleHolder,durationHolder,sizeHolder,artistHolder;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            artworkHolder = itemView.findViewById(R.id.artworkView);
            titleHolder = itemView.findViewById(R.id.titleView);
            durationHolder = itemView.findViewById(R.id.durationView);
            sizeHolder = itemView.findViewById(R.id.sizeView);
            artistHolder = itemView.findViewById(R.id.artistView);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }


    //filter song /search result
    @SuppressLint("NotifyDataSetChanged")
    public void filter(List<Song> filteredList){
        songs = filteredList;
        notifyDataSetChanged();
    }

    @SuppressLint("DefaultLocale")
    private String getDuration(int totalDuration){
        String totalDurationText;

        int hrs = totalDuration/(1000*60*60);
        int min = (totalDuration%(1000*60*60))/(1000*60);
        int secs = ((totalDuration%(1000*60*60))%(1000*60))/1000;

        if (hrs < 1){
            totalDurationText = String.format("%02d:%02d",min,secs);
            
        }else {
            totalDurationText = String.format("%1d:%02d:%02d",hrs,min,secs);
        }
        return totalDurationText;
    }

    //size
    private String getSize(long bytes){
        String hrSize;

        double k = bytes/1024.0;
        double m = (k/1024.0);
        double g = (m/1024.0);
        double t = (g/1024.0);

        //the format
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        if (t>1){
            hrSize = decimalFormat.format(t).concat(" TB");
        } else if (g>1) {
            hrSize = decimalFormat.format(g).concat(" GB");
        }else if (m>1) {
            hrSize = decimalFormat.format(m).concat(" MB");
        }else if (k>1) {
            hrSize = decimalFormat.format(k).concat(" KB");
        }else {
            hrSize = decimalFormat.format(g).concat(" Bytes");
        }
        return hrSize;
    }
}
