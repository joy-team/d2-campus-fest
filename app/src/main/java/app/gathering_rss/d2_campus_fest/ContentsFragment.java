package app.gathering_rss.d2_campus_fest;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import static android.view.View.VISIBLE;

public class ContentsFragment extends Fragment {
    private String contentDate = "";
    private String contentPlace = "";
    private ArrayList<String> contentImg = new ArrayList<>();
    private ArrayList<String> contentVid = new ArrayList<>();
    private String contentDes = "";

    private TabLayout tabLayout;
    private View tabView;
    private ImageView tabImg;

    private TextView view_contentDate;
    private TextView view_contentDes;
    private TextView view_contentPlace;
    private ImageView view_contentRes;

    private String str_feed;

    private PlayerView playerView;
    private SimpleExoPlayer player = null;
    private Boolean playReady = true;
    private int curWindow = 0;
    private long playBackPos = 0;


    public ContentsFragment() {
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            FragmentManager.updateFocus_hor(str_feed, this);
        }else{

        }
    }

    public ContentsFragment newInstance(Bundle content_bundle) {
        ContentsFragment fragment = new ContentsFragment();
        fragment.setArguments(content_bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_contents,container,false);

        tabLayout = view.findViewById(R.id.tabLayout);
        view_contentDate = view.findViewById(R.id.content_date);
        view_contentDes = view.findViewById(R.id.content_des);
        view_contentPlace = view.findViewById(R.id.content_place);
        view_contentRes = view.findViewById(R.id.content_res);
        playerView = view.findViewById(R.id.video_view);

        if(getArguments()!=null){
            str_feed = getArguments().getString("RSS");
            contentDate = getArguments().getString("DATE");
            contentDes = getArguments().getString("DESCRIPTION");
            contentImg = getArguments().getStringArrayList("RESOURCE_IMG");
            contentVid = getArguments().getStringArrayList("RESOURCE_VID");

            if(FragmentManager.activeFragment.get(str_feed)==null) {
                FragmentManager.activeFragment.put(str_feed,this);
                Log.d("now playing", "put fragment");
            }

            if(contentVid.size()>0){
                initPlayer(contentVid.get(0));
            }else{
                try {
                    Glide.with(getActivity().getApplicationContext()).load(contentImg.get(0)).into(view_contentRes);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }


        }else{
            Log.d("get_rss","no arg");
        }

        view_contentDate.setText(contentDate);
        view_contentDes.setText(contentDes);

        //set bottom tab
        for(int i=0;i<contentVid.size();i++){
            TabLayout.Tab tab = tabLayout.newTab();

            tabView = getLayoutInflater().inflate(R.layout.contents_tab,null);
            tabImg = tabView.findViewById(R.id.tab_img);
            tabView.setMinimumHeight(50);
            tabView.setMinimumWidth(50);
            try{
                Glide.with(getActivity().getApplicationContext()).asBitmap().load(contentVid.get(i)).into(tabImg);
                tab.setCustomView(tabView);
            }catch (Exception e){

            }
            tabLayout.addTab(tab);
        }
        for(int i=0;i<contentImg.size();i++){
            TabLayout.Tab tab = tabLayout.newTab();

            tabView = getLayoutInflater().inflate(R.layout.contents_tab,null);
            tabImg = tabView.findViewById(R.id.tab_img);
            tabView.setMinimumHeight(50);
            tabView.setMinimumWidth(50);
            try{
                Glide.with(getActivity().getApplicationContext()).load(contentImg.get(i)).into(tabImg);
                tab.setCustomView(tabView);
            }catch (Exception e){

            }
            tabLayout.addTab(tab);
        }

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int selected = tabLayout.getSelectedTabPosition();
                Log.d("tab_select", new Integer(selected).toString());
                try {
                    Glide.with(getActivity().getApplicationContext()).load(contentImg.get(selected)).into(view_contentRes);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return view;
    }


    public void startPlaying(){
        if(FragmentManager.playing_fragment!=null && FragmentManager.playing_fragment!=this)
            //stop previous focused feed
            FragmentManager.playing_fragment.stopPlaying();

        if(FragmentManager.playing_fragment!=this){
            //start playing
            FragmentManager.playing_fragment = this;
            /// TODO: 2019-07-28 현재 프래그먼트 재생하는 스레드 실행
            if(contentVid.size()>0)
                initPlayer(contentVid.get(0));
            Log.d("now playing","feed : "+str_feed+" fragment : "+this.toString()+" / date : "+contentDate);
        }

    }

    public void stopPlaying(){
        Log.d("now playing","stop previous playing");
        /// TODO: 2019-07-28 현재 프래그먼트의 재생 스레드 멈춤
        releasePlayer();
    }


    private void initPlayer(String videoUrl) {
        playerView.setVisibility(VISIBLE);
        playerView.setUseController(false);

        /// TODO: 2019-07-30 nullpointerexception 고치기 
        player = ExoPlayerFactory.newSimpleInstance(getActivity().getApplicationContext());
        player.setVolume(0f);

        playerView.setPlayer(player);
        player.setPlayWhenReady(playReady);
        player.seekTo(curWindow, playBackPos);

        Uri uri = Uri.parse(videoUrl);
        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource);
    }

    private void releasePlayer() {
        if (player != null) {
            playBackPos = player.getCurrentPosition();
            curWindow = player.getCurrentWindowIndex();
            playReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("Mozilla/5.0"))
                .createMediaSource(uri);
    }
}
