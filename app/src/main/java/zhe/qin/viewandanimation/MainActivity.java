package zhe.qin.viewandanimation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.list);
        String[] samples = {"组合view", "自己布局", "自己绘制", "Animation Api", "Transformation", "Animator Api"};
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, samples));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        gotoLayout(R.layout.composite, false);
                        break;
                    case 1:
                        gotoLayout(R.layout.self_layout, false);
                        break;
                    case 2:
                        gotoLayout(R.layout.selfdraw, false);
                        break;
                    case 3:
                        gotoLayout(R.layout.composite, true);
                        break;
                    case 4:
                        gotoLayout(R.layout.self_layout, true);
                        break;
                    case 5:
                        gotoLayout(R.layout.selfdraw, true);
                        break;
                }
            }
        });
    }

    private void gotoLayout(int layoutId, boolean useAnim) {
        Intent intent = new Intent();
        intent.setClass(this, SampleActivity.class);
        intent.putExtra(SampleActivity.EXTRA_LAYOUT_ID, layoutId);
        intent.putExtra(SampleActivity.EXTRA_USE_ANIM, useAnim);
        startActivity(intent);
    }
}
