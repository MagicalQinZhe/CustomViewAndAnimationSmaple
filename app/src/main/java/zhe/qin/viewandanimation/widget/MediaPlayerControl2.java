package zhe.qin.viewandanimation.widget;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import zhe.qin.viewandanimation.R;

/**
 * Created by qinzhe on 16/2/15.
 */
public class MediaPlayerControl2 extends FrameLayout implements ITestLayout, ITestAnimation {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MediaPlayerControl2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public MediaPlayerControl2(Context context) {
        super(context);
        init(context);
    }

    public MediaPlayerControl2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MediaPlayerControl2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private TextView currentTimeView;
    private TextView durationTimeView;
    private SeekBar seekBar;

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.mediaplayer_control, this);
        currentTimeView = (TextView) findViewById(R.id.left);
        durationTimeView = (TextView) findViewById(R.id.right);
        seekBar = (SeekBar) findViewById(R.id.seekbar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setCurrent(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setDuraion(int duraionSec) {
        durationTimeView.setText(formatTime(duraionSec));
        seekBar.setMax(duraionSec);
    }

    public void setCurrent(int timeSec) {
        currentTimeView.setText(formatTime(timeSec));
    }

    private String formatTime(int second) {
        int minute = 0;
        if (second >= 60) {
            minute = second / 60;
            second = second % 60;
        }

        return String.format("%02d:%02d", minute, second);
    }

    @Override
    public void testLayout() {
        setDuraion(2345);
    }

    @Override
    public void testAnimation() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "scaleX", 0, 1);
        animator.setDuration(10000);
        animator.start();
    }
}
