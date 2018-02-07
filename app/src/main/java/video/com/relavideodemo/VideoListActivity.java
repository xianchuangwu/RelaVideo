package video.com.relavideodemo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {


//    private VideoListView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_list);

//        mRecyclerView = findViewById(R.id.list);
        VideoListView2 videoListView2 = findViewById(R.id.videoList);

        String basePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0)
                list.add("https://relamov.rela.me/gyx.mp4");
            else {
//                list.add(basePath + "/square.mp4");
                list.add("http://video.rela.me/app/timeline/103351547/4f8c27caa4aa57194f742b72a107dbb1.mp4");
            }
        }
//        mRecyclerView.setData(list);
        videoListView2.setData(list);
    }


}
