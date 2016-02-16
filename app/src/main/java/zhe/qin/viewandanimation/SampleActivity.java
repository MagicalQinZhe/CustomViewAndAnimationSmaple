package zhe.qin.viewandanimation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import zhe.qin.viewandanimation.widget.ITestAnimation;
import zhe.qin.viewandanimation.widget.ITestLayout;

/**
 * Created by qinzhe on 16/2/15.
 */
public class SampleActivity extends AppCompatActivity {

    public static final String EXTRA_LAYOUT_ID = "layout_id";
    public static final String EXTRA_USE_ANIM = "use_anim";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getIntent().getIntExtra(EXTRA_LAYOUT_ID, 0);
        if (layoutId != 0) {
            View v = getLayoutInflater().inflate(layoutId, null);
            setContentView(v);
            if (v instanceof ITestLayout) {
                ((ITestLayout) v).testLayout();
            }

            boolean useAnim = getIntent().getBooleanExtra(EXTRA_USE_ANIM, false);

            if (useAnim && v instanceof ITestAnimation) {
                ((ITestAnimation) v).testAnimation();
            }
        } else {
            finish();
        }
    }
}
