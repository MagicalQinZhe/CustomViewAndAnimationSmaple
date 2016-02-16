package zhe.qin.viewandanimation.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import zhe.qin.viewandanimation.R;

/**
 * Created by qinzhe on 15/3/9.
 */
public class MagneticDropIndicator extends View implements ITestLayout, ITestAnimation {
    public MagneticDropIndicator(Context context) {
        super(context);
        init(context, null, 0);
    }

    public MagneticDropIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public MagneticDropIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private int dropWidthNormal;
    private int dropWidthSelected;
    private int dropSpace;

    private int normalColor;
    private int selectedColor;

    private int indicatorCount;
    private int currentSelected;
    private int nextSelect = -1;
    private float indicatorProgress;


    private Path path = new Path();
    private int extraWidth;
    private int extraHeight;

    private final Paint circlePaint = new Paint();

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.FILL);
        
        setWillNotDraw(false);
        
		TypedArray a = attrs != null ? context.obtainStyledAttributes(attrs,
				R.styleable.MagneticDropIndicator, defStyleAttr, 0) : context
				.obtainStyledAttributes(R.styleable.MagneticDropIndicator);
		
		dropWidthNormal = a.getDimensionPixelSize(R.styleable.MagneticDropIndicator_normalDropWidth, 0);
		dropWidthSelected = a.getDimensionPixelSize(R.styleable.MagneticDropIndicator_selectedDropWidth, 0);
		normalColor = a.getColor(R.styleable.MagneticDropIndicator_normalDropColor, 0);
		selectedColor = a.getColor(R.styleable.MagneticDropIndicator_selectedDropColor, 0);
		dropSpace = a.getDimensionPixelSize(R.styleable.MagneticDropIndicator_dropSpace, 0);
		
		a.recycle();
    }

    public void setIndicatorProgress(float progress) {
        this.indicatorProgress = progress;
        invalidate();
    }

    public void setIndicatorCount(int count, int defaultSelected) {
        indicatorCount = count;
        currentSelected = defaultSelected;
        requestLayout();
        invalidate();
    }

    public void setCurrentSelected(int index) {
        currentSelected = index;
        nextSelect = -1;
        indicatorProgress = 0f;
        invalidate();
    }
    
    public void setNextSelected(int index) {
    	nextSelect = index;
    }
    
    public int getCount() {
    	return indicatorCount;
    }
    
    public int getCurrentSelected() {
    	return currentSelected;
    }

    private void moveDot(float cx, float cy, float r, boolean isMoveToRight, Canvas canvas) {

        float indicatorProgress = this.indicatorProgress;

        if (indicatorProgress <= 0.001f) {
            canvas.drawCircle(cx, cy , r, circlePaint);
        } else if (indicatorProgress >= 0.999f) {
            canvas.drawCircle(cx+(isMoveToRight ? dropSpace : -dropSpace), cy , r, circlePaint);
        } else if (indicatorProgress < 1 && indicatorProgress > 0) {
            boolean isPreHalf = indicatorProgress<=0.5f;
            float halfProgress = indicatorProgress;
            if (!isPreHalf) {
                halfProgress = indicatorProgress-0.5f;
            }
            halfProgress /= 0.5f;

            if (!isMoveToRight)
                indicatorProgress = 1-indicatorProgress;

            float rProgressLeft = r*(1-indicatorProgress);
            float rProgressRight = r*indicatorProgress;

            float leftX;
            float rightX;

            if (isMoveToRight) {
                float startX = cx+r;
                float terminalX = cx+dropSpace-r;

                if (isPreHalf) {
                    leftX = cx;
                    rightX = startX + (cx+dropSpace-startX)*halfProgress;
                } else {
                    leftX = cx + (terminalX-cx)*halfProgress;
                    rightX = cx + dropSpace;
                }
            } else {
                float startX = cx-r;
                float terminalX = cx-dropSpace+r;

                if (isPreHalf) {
                    leftX = startX + (cx-dropSpace-startX)*halfProgress;
                    rightX = cx;
                } else {
                    leftX = cx-dropSpace;
                    rightX = cx + (terminalX-cx)*halfProgress;
                }
            }


            float rOtherX = leftX + (rightX-leftX)*(1-indicatorProgress);
            float d1 = rOtherX-leftX;
            float d2 = rightX-rOtherX;
            float rTangentialCircle = (d1*d1-d2*d2-rProgressLeft*rProgressLeft+rProgressRight*rProgressRight)/(2*(rProgressLeft-rProgressRight));
            float h = (float) Math.sqrt((rProgressLeft+rTangentialCircle)*(rProgressLeft+rTangentialCircle)-d1*d1);
            float leftRadius = (float) Math.asin(h/(rProgressLeft+rTangentialCircle));
            float leftDegree = (float) (leftRadius*180/Math.PI);

            float rightRadius = (float) Math.asin(h/(rProgressRight+rTangentialCircle));
            float rightDegree = (float) (rightRadius*180/Math.PI);


            path.reset();
            // left circle
            path.addArc(new RectF(leftX - rProgressLeft, cy - rProgressLeft, leftX + rProgressLeft, cy + rProgressLeft), leftDegree, 360 - leftDegree * 2);
            // up circle
            path.arcTo(new RectF(rOtherX-rTangentialCircle, cy-h-rTangentialCircle, rOtherX+rTangentialCircle, cy-h+rTangentialCircle), 180-leftDegree, -(180-leftDegree-rightDegree));
            
            // right circle
            path.arcTo(new RectF(rightX-rProgressRight, cy-rProgressRight, rightX+rProgressRight, cy+rProgressRight), -(180-rightDegree), 360-rightDegree*2);
            // bottom circle
            path.arcTo(new RectF(rOtherX-rTangentialCircle, cy+h-rTangentialCircle, rOtherX+rTangentialCircle, cy+h+rTangentialCircle), -rightDegree, -(180-leftDegree-rightDegree));

            path.close();
            canvas.drawPath(path, circlePaint);
        }

    }

    private void moveToRight(float cx, float cy, float r, Canvas canvas) {
        moveDot(cx, cy, r, true, canvas);
    }

    private void moveToLeft(float cx, float cy, float r, Canvas canvas) {
        moveDot(cx, cy, r, false, canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (indicatorCount <= 0)
            return;
        float drawStartX = getPaddingLeft()+extraWidth/2f;
        float drawStartY = getPaddingTop()+extraHeight/2f;

        float indicatorWidth = Math.max(dropWidthNormal, dropWidthSelected)/2f;
        float cx = drawStartX + indicatorWidth;
        float cy = drawStartY + indicatorWidth;

//        Bitmap bmp = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.RGB_565);
//        Canvas org = canvas;
//        Canvas bmpCanvas = new Canvas(bmp);
//        canvas = bmpCanvas;

        for (int i=0; i<indicatorCount; ++i) {
            float r;
            if (i == currentSelected) {
                circlePaint.setColor(selectedColor);
                r = dropWidthSelected/2f;

            } else {
                circlePaint.setColor(normalColor);
                r = dropWidthNormal/2f;
            }

            if (nextSelect != -1) {
                if (nextSelect > currentSelected) {
                    if (i == currentSelected) {
                        moveToRight(cx, cy, r, canvas);
                    } else if (i == nextSelect) {
                        moveToLeft(cx, cy, r, canvas);
                    } else {
                        canvas.drawCircle(cx, cy , r, circlePaint);
                    }
                } else {
                    if (i == currentSelected) {
                        moveToLeft(cx, cy, r, canvas);
                    } else if (i == nextSelect) {
                        moveToRight(cx, cy, r, canvas);
                    } else {
                        canvas.drawCircle(cx, cy , r, circlePaint);
                    }
                }
            } else {
                canvas.drawCircle(cx, cy , r, circlePaint);
            }

            cx += dropSpace;
        }
//        try {
//            if (nextSelect >= 0) {
//                File file = new File("/sdcard/a/" + indicatorProgress + ".jpg");
//                file.getParentFile().mkdirs();
//                bmp.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

//        canvas = org;
//        canvas.drawBitmap(bmp, 0, 0, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (indicatorCount > 0) {
            int minHeight = Math.max(dropWidthSelected, dropWidthNormal)+getPaddingTop()+getPaddingBottom();
            int minWidth = minHeight+dropSpace*(indicatorCount-1)+getPaddingLeft()+getPaddingRight();

            int widthMeasureMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightMeasureMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            setMinimumWidth(minWidth);
            setMinimumHeight(minHeight);

            if (widthMeasureMode == MeasureSpec.AT_MOST) {
                setMinimumWidth(Math.min(minWidth, widthSpecSize));
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, 0);
            }
            if (heightMeasureMode == MeasureSpec.AT_MOST) {
                setMinimumHeight(Math.min(minHeight, heightSpecSize));
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, 0);
            }

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            int measuredWidth = getMeasuredWidth();
            int measuredHeight = getMeasuredHeight();
            if (measuredWidth > minWidth)
                extraWidth = measuredWidth-minWidth;

            if (measuredHeight > minHeight)
                extraHeight = measuredHeight-minHeight;

        } else {
            setMeasuredDimension(0, 0);
        }
    }


    @Override
    public void testLayout() {
        setIndicatorCount(5, 2);
    }

    @Override
    public void testAnimation() {
        setIndicatorCount(5, 0);
        startAnimate();
    }

    int i = 0;
    void startAnimate() {
        i++;
        if (i == 5) {
            i=4;
            reverseAnimate();
            return;
        }
        setNextSelected(i);
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "indicatorProgress", 0, 1);
        animator.setDuration(2000);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setCurrentSelected(i);
                startAnimate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    void reverseAnimate() {
        i--;
        if (i == -1) {
            i=0;
            startAnimate();
            return;
        }
        setNextSelected(i);
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "indicatorProgress", 0, 1);
        animator.setDuration(2000);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setCurrentSelected(i);
                reverseAnimate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
}
