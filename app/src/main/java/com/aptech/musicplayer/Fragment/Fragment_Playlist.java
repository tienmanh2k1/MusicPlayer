package com.aptech.musicplayer.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.aptech.musicplayer.Activity.HomeActivity;
import com.aptech.musicplayer.Adapter.PlaylistAdapter;
import com.aptech.musicplayer.R;
import com.squareup.picasso.Picasso;

public class Fragment_Playlist extends Fragment implements Dialog_update_user.ExampleDialogListenerUpdateUser{

    HomeActivity hm;
    de.hdodenhof.circleimageview.CircleImageView imguser;
    View view;
    PlaylistAdapter playlistAdapter;
    RecyclerView recyclerViewplaylist;
    TextView tenPlaylist;
    String sql = "";
    private SQLiteDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_playlist, container, false);
        db = getActivity().openOrCreateDatabase("NguoiDung.db", MODE_PRIVATE, null);
        recyclerViewplaylist = view.findViewById(R.id.recyclerviewplaylist);
        tenPlaylist = view.findViewById(R.id.txtplaylist);
        hm = (HomeActivity) getActivity();
        imguser = view.findViewById(R.id.imageviewuser);
        Picasso.get().load(hm.getUrl()).into(imguser);
        //GetData();
        imguser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialogUpdate();
            }
        });
        return view;
    }

//    private void GetData() {
//        Dataservice dataservice = APIService.getService();
//        Call<List<PlaylistModel>> callback = dataservice.GetPlaylistCurrentDay();
//        callback.enqueue(new Callback<List<PlaylistModel>>() {
//            @Override
//            public void onResponse(Call<List<PlaylistModel>> call, Response<List<PlaylistModel>> response) {
//                ArrayList<PlaylistModel> mangplaylist = (ArrayList<PlaylistModel>) response.body();
//                playlistAdapter = new PlaylistAdapter(getActivity(), mangplaylist);
//
//                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
//                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//                recyclerViewplaylist.setLayoutManager(linearLayoutManager);
//                recyclerViewplaylist.setAdapter(playlistAdapter);
//            }
//
//            @Override
//            public void onFailure(Call<List<PlaylistModel>> call, Throwable t) {
//
//            }
//        });
//    }

    private void ShowDialogUpdate(){
        Dialog_update_user dialog_update_user = new Dialog_update_user(hm.getTaikhoan(), hm.getName(), hm.getUrl());
        dialog_update_user.show(getParentFragmentManager(), "");
    }

    @Override
    public void apply(String tenUser, Bitmap bitmap) {
        if (bitmap != null){
            imguser.setImageBitmap(bitmap);
            sql = "UPDATE tbNguoiDung SET Ten ='"+tenUser+"', ImageURL =  'https://music4b.000webhostapp.com/HinhAnh/NguoiDung/"+hm.getTaikhoan()+".jpg' WHERE TaiKhoan = '"+hm.getTaikhoan()+"'";
        }else {
            sql = "UPDATE tbNguoiDung SET Ten ='"+tenUser+"' WHERE TaiKhoan = '"+hm.getTaikhoan()+"'";
        }
        db.execSQL(sql);
        hm.updateHome();

    }
}
