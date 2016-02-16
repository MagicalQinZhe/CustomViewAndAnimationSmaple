package zhe.qin.viewandanimation.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import zhe.qin.viewandanimation.R;

/**
 * Created by qinzhe on 15/10/29.
 */
public class SpannableGridLayout extends ViewGroup implements ITestAnimation {

    private int columnCount;
    private int rowCount;
    private Drawable columnDivider;
    private Drawable rowDivider;
    private int columnDividerSize;
    private int rowDividerSize;
    private int columnWidth;
    private int rowHeight;
    private List<Divider> columnDividers;
    private List<Divider> rowDividers;

    class Divider {
        Rect divierBounds;
        List<Rect> checkedBounds;

        void exclude(int orientation, int start, int end) {
            if (checkedBounds == null) {
                checkedBounds = new ArrayList<Rect>();
                checkedBounds.add(divierBounds);
            }

            if (orientation == LinearLayout.HORIZONTAL) {
                for (int i=0; i<checkedBounds.size(); ++i) {
                    Rect rc = checkedBounds.get(i);
                    if (rc.left <= start && rc.right >= end) {
                        checkedBounds.remove(i);
                        //split
                        if (rc.left < start) {
                            checkedBounds.add(new Rect(rc.left, rc.top, start, rc.bottom));
                        }
                        if (rc.right > end) {
                            checkedBounds.add(new Rect(end, rc.top, rc.right, rc.bottom));
                        }
                        break;
                    }
                }

            } else if (orientation == LinearLayout.VERTICAL) {
                for (int i=0; i<checkedBounds.size(); ++i) {
                    Rect rc = checkedBounds.get(i);
                    if (rc.top <= start && rc.bottom >= end) {
                        checkedBounds.remove(i);
                        //split
                        if (rc.top < start) {
                            checkedBounds.add(new Rect(rc.left, rc.top, rc.right, start));
                        }
                        if (rc.bottom > end) {
                            checkedBounds.add(new Rect(rc.left, end, rc.right, rc.bottom));
                        }
                        break;
                    }
                }
            }
        }
    }

    public SpannableGridLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public SpannableGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public SpannableGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr){
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpannableGridLayout, defStyleAttr, 0);
            columnCount = a.getInteger(R.styleable.SpannableGridLayout_column_count, 1);
            rowCount = a.getInteger(R.styleable.SpannableGridLayout_row_count, 1);
            columnDivider = a.getDrawable(R.styleable.SpannableGridLayout_column_divider);
            rowDivider = a.getDrawable(R.styleable.SpannableGridLayout_row_divider);
            if (columnDivider != null) {
                columnDividerSize = columnDivider.getIntrinsicWidth();
            }
            columnDividerSize = a.getDimensionPixelSize(R.styleable.SpannableGridLayout_column_divider_size, columnDividerSize);
            if (rowDivider != null) {
                rowDividerSize = rowDivider.getIntrinsicHeight();
            }
            rowDividerSize = a.getDimensionPixelSize(R.styleable.SpannableGridLayout_row_divider_size, rowDividerSize);

            a.recycle();
        }

        setWillNotDraw(false);
    }

    private void initDividers(int totalWidth, int totalHeight) {
        if (columnDivider != null) {
            if (columnDividers != null) {
                columnDividers.clear();
            } else {
                columnDividers = new ArrayList<Divider>(columnCount-1);
            }
            for (int i=1; i<columnCount; ++i) {
                Divider divider = new Divider();
                final int x = columnWidth*i+columnDividerSize*(i-1);
                divider.divierBounds = new Rect(x, 0, x+columnDividerSize, totalHeight);
                columnDividers.add(divider);
            }
        } else if (columnDividers != null) {
            columnDividers.clear();
        }

        if (rowDivider != null) {
            if (rowDividers != null) {
                rowDividers.clear();
            } else {
                rowDividers = new ArrayList<Divider>(rowCount-1);
            }
            for (int i=1; i<rowCount; ++i) {
                Divider divider = new Divider();
                final int y = rowHeight*i+rowDividerSize*(i-1);
                divider.divierBounds = new Rect(0, y, totalWidth, y+rowDividerSize);
                rowDividers.add(divider);
            }
        } else if (rowDividers != null) {
            rowDividers.clear();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if ((widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) &&
                (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST)) {
            setMeasuredDimension(widthSpecSize, heightSpecSize);
            columnWidth = (widthSpecSize - (columnCount-1)*columnDividerSize)/columnCount;
            rowHeight = (heightSpecSize- (rowCount-1)*rowDividerSize)/rowCount;
            initDividers(widthSpecSize, heightSpecSize);
            int childCount = getChildCount();
            for (int i=0;i <childCount; ++i) {
                View v = getChildAt(i);
                LayoutParams lp = (LayoutParams) v.getLayoutParams();
                final int column = lp.column;
                final int row = lp.row;
                final int colSpan = lp.columnSpan;
                final int rowSpan = lp.rowSpan;

                v.measure(MeasureSpec.makeMeasureSpec(colSpan*columnWidth+(colSpan-1)*columnDividerSize, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(rowSpan*rowHeight+(rowSpan-1)*rowDividerSize, MeasureSpec.EXACTLY));

                if (rowSpan > 1 && rowDividers != null) {
                    for (int j=1; j<rowSpan; ++j) {
                        int excludeRow = row+j;
                        Divider divider = rowDividers.get(excludeRow-1);
                        final int x = columnWidth*column+columnDividerSize*column;
                        divider.exclude(LinearLayout.HORIZONTAL, x, x + colSpan * (columnWidth+columnDividerSize));
                    }
                }

                if (colSpan > 1 && columnDividers != null) {
                    for (int j=1; j<colSpan; ++j) {
                        int excludeCol = column+j;
                        Divider divider = columnDividers.get(excludeCol-1);
                        int y = rowHeight*row+rowDividerSize*row;

                        divider.exclude(LinearLayout.VERTICAL, y, y+rowSpan*(rowHeight+rowDividerSize));
                    }
                }
            }

        } else {
            setMeasuredDimension(0, 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i=0;i <childCount; ++i) {
            View v = getChildAt(i);
            LayoutParams lp = (LayoutParams) v.getLayoutParams();
            final int col = lp.column;
            final int row = lp.row;
            final int w = v.getMeasuredWidth();
            final int h = v.getMeasuredHeight();
            int x = col*columnWidth+col*columnDividerSize;
            int y = row*rowHeight+row*rowDividerSize;
            v.layout(x, y, x+w, y+h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDividers(canvas, columnDividers, columnDivider);
        drawDividers(canvas, rowDividers, rowDivider);
    }

    private void drawDividers(Canvas canvas, List<Divider> dividers, Drawable dividerDrawable) {
        if (dividers != null && dividerDrawable != null) {
            for (Divider divider:dividers) {
                if (divider.checkedBounds != null) {
                    for (Rect rc:divider.checkedBounds) {
                        dividerDrawable.setBounds(rc);
                        dividerDrawable.draw(canvas);
                    }
                } else if (divider.divierBounds != null) {
                    dividerDrawable.setBounds(divider.divierBounds);
                    dividerDrawable.draw(canvas);
                }
            }
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    class LayoutParams extends MarginLayoutParams {

        int column;
        int row;
        int columnSpan = 1;
        int rowSpan = 1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            init(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams() {
            super(MATCH_PARENT, MATCH_PARENT);
        }

        private void init(Context c, AttributeSet attrs) {
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.SpannableGridLayout);
            column = a.getInteger(R.styleable.SpannableGridLayout_column, 0);
            row = a.getInteger(R.styleable.SpannableGridLayout_row, 0);
            columnSpan = a.getInteger(R.styleable.SpannableGridLayout_column_span, 1);
            rowSpan = a.getInteger(R.styleable.SpannableGridLayout_row_span, 1);
            a.recycle();
        }
    }

    private boolean isAnimating;
    private long animateStartTime;
    private static final long DURATION = 2000;
    private static final long DURATION_HALF = 1000;

    @Override
    public void testAnimation() {
        setStaticTransformationsEnabled(true);
        startAnimation();
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {

        if (isAnimating) {
            try {
                int index = indexOfChild(child);
                long startTime = animateStartTime + index * 500;
                long now = SystemClock.uptimeMillis();

                if (now < startTime) {
                    return false;
                }

                long extra = (now - startTime) % DURATION;

                t.clear();
                t.setTransformationType(Transformation.TYPE_ALPHA);
                if (extra <= DURATION_HALF) {
                    t.setAlpha(extra / (float)DURATION_HALF);
                } else {
                    t.setAlpha((DURATION_HALF - (extra-DURATION_HALF)) / (float)DURATION_HALF);
                }

                return true;
            } finally {
                child.postInvalidateDelayed(25);
            }
        }
        return super.getChildStaticTransformation(child, t);
    }

    public void startAnimation() {
        isAnimating = true;
        animateStartTime = SystemClock.uptimeMillis();
    }
}
