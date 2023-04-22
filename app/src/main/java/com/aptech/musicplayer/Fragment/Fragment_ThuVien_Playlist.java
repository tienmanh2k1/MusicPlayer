package com.aptech.musicplayer.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aptech.musicplayer.Activity.HomeActivity;
import com.aptech.musicplayer.Adapter.ThuVienPlayListAdapter;
import com.aptech.musicplayer.R;

public class Fragment_ThuVien_Playlist extends Fragment {
    View view;
    ThuVienPlayListAdapter thuVienPlayListAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerViewThuVienPlayList;
    TextView tenThuVienPlayList;
    HomeActivity hm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_thuvien_playlist, container, false);
        recyclerViewThuVienPlayList = view.findViewById(R.id.recyclerviewthuvienplaylist);
        tenThuVienPlayList = view.findViewById(R.id.textviewthuvienplaylist);
        hm = (HomeActivity) getActivity();
        //GetData(hm.getTaikhoan());
        swipeRefreshLayout = view.findViewById(R.id.swipethuvien);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //GetData(hm.getTaikhoan());
                thuVienPlayListAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        return view;
    }
//    public void GetData(String id) {
//        Dataservice dataservice = APIService.getService();
//        Call<List<ThuVienPlayListModel>> callback = dataservice.GetBangThuVienPlayList(id);
//        callback.enqueue(new Callback<List<ThuVienPlayListModel>>() {
//            @Override
//            public void onResponse(Call<List<ThuVienPlayListModel>> call, Response<List<ThuVienPlayListModel>> response) {
//                ArrayList<ThuVienPlayListModel> mangthuvienplaylist = (ArrayList<ThuVienPlayListModel>) response.body();
//                thuVienPlayListAdapter = new ThuVienPlayListAdapter(getActivity(), mangthuvienplaylist, hm.getName());
//                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
//                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//                recyclerViewThuVienPlayList.setLayoutManager(linearLayoutManager);
//                recyclerViewThuVienPlayList.setAdapter(thuVienPlayListAdapter);
//            }
//
//            @Override
//            public void onFailure(Call<List<ThuVienPlayListModel>> call, Throwable t) {
//
//            }
//
//        });
//    }
}
