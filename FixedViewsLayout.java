import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 支持对子元素自动换行显示排版
 * @author Z.P.F created on 2015年12月24日
 *
 */
public class FixedViewsLayout extends FrameLayout {

    private int mLeftRightMargin;
    private int mTopBottomMargin;
    private int[] mWidthArray;
    private int[] mHeightArray;

    public FixedViewsLayout(Context context) {
        super(context);
    }

    public FixedViewsLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FixedViewsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 固定高宽
     * @param width
     * @param height
     */
    public void setFixedSizeArrays(int[] width, int[] height) {
        mWidthArray = width;
        mHeightArray = height;
    }

    /**
     * 设置每个子View的左右间隔
     * @param px
     */
    public void setChildLeftRightMargin(float px) {
        mLeftRightMargin = Float.valueOf(px).intValue();
    }

    /**
     * 设置每个子View的上下间隔
     * @param px
     */
    public void setChildTopBottomMargin(float px) {
        mTopBottomMargin = Float.valueOf(px).intValue();
    }

    private int mDesireSize = 0;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        for (int index = 0, count = getChildCount(); index < count; index++) {
            View childView = getChildAt(index);
            if (null != mWidthArray && null != mHeightArray && mWidthArray.length == mHeightArray.length
                && count == mWidthArray.length && index < mWidthArray.length) {
                childView.measure(MeasureSpec.makeMeasureSpec(mWidthArray[index], MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mHeightArray[index], MeasureSpec.EXACTLY));
            } else {
                childView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            }
        }
        int rows = layoutChildren(measuredWidth);
        final int margin = (rows > 0) ? mTopBottomMargin : 0;
        final int desiredSize = rows * mTopBottomMargin + margin + mDesireSize;
        final int resolvedSize = resolveSizeAndState(desiredSize, heightMeasureSpec, 0);
        setMeasuredDimension(measuredWidth, resolvedSize);
    }

    private int layoutChildren(int width) {
        mDesireSize = 0;
        int x = 0;
        int y = 0;
        final int count = getChildCount();
        int rows = count > 0 ? 1 : 0;
        for (int index = 0; index < count; index++) {
            final View child = getChildAt(index);

            LayoutParams params = (LayoutParams) child.getLayoutParams();
            final int childLeftMargin = params.leftMargin;
            final int childRightMargin = params.rightMargin;
            final int childTopMargin = params.topMargin;
            final int childBottomMargin = params.bottomMargin;

            int w = child.getMeasuredWidth();
            final int h = child.getMeasuredHeight();
            final int left = x + mLeftRightMargin + childLeftMargin;
            final int top = y + mTopBottomMargin + childTopMargin;

            // 如果单行宽度超出控件的宽度
            if (left + w >= width) {
                w = w - (left + w - width) - mLeftRightMargin - childLeftMargin;
                if ((child instanceof TextView) || (child instanceof Button)) {
                    // FIXME 如果是TextView等文本控件，必须再measure一次，才能使"单行且超出部分在末尾添加省略号"的功能生效
                    child.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
                }
            }
            child.layout(left, top, left + w, top + h);
            x = left + w + childRightMargin;

            // 是否下一个元素在下一行中显示
            boolean advanceToNextRow = false;
            if (index + 1 < count) {
                final View nextView = getChildAt(index + 1);
                LayoutParams params1 = (LayoutParams) nextView.getLayoutParams();
                final int nextLeftMargin = params1.leftMargin;

                final int nextViewMeasuredWidth = nextView.getMeasuredWidth();
                final int plusNextViewTotalWidth = x + mLeftRightMargin + nextViewMeasuredWidth + nextLeftMargin;
                // x + (mLeftRightMargin * 3) + nextViewMeasuredWidth;
                advanceToNextRow = plusNextViewTotalWidth > width;
            }
            if (advanceToNextRow) {
                x = 0;
                y += (h + mTopBottomMargin + childBottomMargin);
                rows++;
                mDesireSize += (h + childTopMargin + childBottomMargin);
            }
        }

        View lastView = getChildAt(count - 1);
        LayoutParams lastParams = (LayoutParams) lastView.getLayoutParams();
        mDesireSize += (lastView.getMeasuredHeight() + lastParams.topMargin + lastParams.bottomMargin);

        return rows;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }
}
