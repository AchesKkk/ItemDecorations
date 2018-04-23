package com.sheldonchen.itemdecorations.decorations;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.sheldonchen.itemdecorations.annotation.DecorationOrientType;
import com.sheldonchen.itemdecorations.provider.ColorIntProvider;
import com.sheldonchen.itemdecorations.provider.DrawableProvider;
import com.sheldonchen.itemdecorations.provider.base.BaseProvider;

/**
 * 适用于RecyclerView网格布局以及瀑布流布局下的ItemDecoration
 * 完美支持网格布局设置了SpanSizeLookup的情况
 * Created by cxd on 2018/3/7
 */

public class GridLayoutDivider extends RecyclerView.ItemDecoration {
    
    public static final class Builder {
        
        /**
         * RecyclerView布局方向.
         */
        @DecorationOrientType int mOrientation = GridLayoutManager.VERTICAL;

        /**
         * 列表布局方向上分割线的厚度.
         */
        int mDividerThickness = 0;

        /**
         * 侧边分割线的厚度.
         */
        int mSideDividerThickness = 0;

        /**
         * 是否画顶部分割线.
         */
        boolean mDrawTopSideDivider = false;

        /**
         * 是否画底部分割线.
         */
        boolean mDrawBottomSideDivider = false;

        /**
         * 是否画两侧边分割线.
         */
        boolean mDrawTwoSidesDivider = false;

        /**
         * Provider: 支持Drawable和ColorInt.
         */
        BaseProvider<?> mProvider = null;

        /**
         * Side Provider: 支持Drawable和ColorInt.
         */
        BaseProvider<?> mSideProvider = null;

        public Builder setOrientation(@DecorationOrientType int orientation) {
            this.mOrientation = orientation;
            return this;
        }

        public Builder setDividerThickness(int dividerThickness) {
            this.mDividerThickness = dividerThickness;
            return this;
        }

        public Builder setSideDividerThickness(int sideDividerThickness) {
            this.mSideDividerThickness = sideDividerThickness;
            return this;
        }

        public Builder drawTopSideDivider(boolean drawTopSideDivider) {
            this.mDrawTopSideDivider = drawTopSideDivider;
            return this;
        }

        public Builder drawBottomSideDivider(boolean drawBottomSideDivider) {
            this.mDrawBottomSideDivider = drawBottomSideDivider;
            return this;
        }

        public Builder drawTwoSidesDivider(boolean drawTwoSidesDivider) {
            this.mDrawTwoSidesDivider = drawTwoSidesDivider;
            return this;
        }

        public Builder setProvider(@NonNull BaseProvider<?> provider, @NonNull BaseProvider<?> sideProvider) {
            this.mProvider = provider;
            this.mSideProvider = sideProvider;
            return this;
        }
        
        public Builder setDividerColor(@ColorInt int dividerColor) {
            this.mProvider = this.mSideProvider
                    = new ColorIntProvider(dividerColor);
            return this;
        }

        public Builder setSideDividerColor(@ColorInt int dividerColor) {
            this.mSideProvider = new ColorIntProvider(dividerColor);
            return this;
        }

        public Builder setDividerDrawable(@NonNull Drawable drawable, Drawable sideDrawable) {
            this.mProvider = new DrawableProvider(drawable);
            this.mSideProvider = new DrawableProvider(sideDrawable);
            return this;
        }

        public GridLayoutDivider build() {
            return new GridLayoutDivider(this);
        }
    }

    private final Builder mBuilder;

    private GridLayoutDivider(Builder builder) {
        if(builder == null) {
            throw new NullPointerException("GridLayoutDivider: mBuilder can't be null.");
        }
        this.mBuilder = builder;
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(canvas, parent, state);
        if(mBuilder.mProvider == null
                || mBuilder.mSideProvider == null) return;

        if (mBuilder.mOrientation == GridLayoutManager.VERTICAL) {
            drawOrientVerticalDivider(canvas, parent);
        } else {
            drawOrientHorizontalDivider(canvas, parent);
        }
    }

    private void drawOrientVerticalDivider(Canvas canvas, RecyclerView parent) {
        final int spanCount = getSpanCount(parent);
        final int childSize = parent.getChildCount();
        
        for (int i = 0; i < childSize; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();

            // 画水平分隔线.
            int left = child.getLeft();
            int right = child.getRight();
            int top = child.getBottom() + layoutParams.bottomMargin;
            int bottom = top + mBuilder.mDividerThickness;
            if(!isLastRaw(parent, i , spanCount, childSize) || mBuilder.mDrawBottomSideDivider) {
                mBuilder.mProvider.drawDivider(canvas, left, top, right, bottom);
            }
            if(mBuilder.mDrawTopSideDivider && isFirstRaw(parent, i, spanCount)) {
                bottom = child.getTop() - layoutParams.topMargin;
                top = bottom - mBuilder.mDividerThickness;
                mBuilder.mProvider.drawDivider(canvas, left, top, right, bottom);
            }

            // 画竖直分隔线.
            top = child.getTop();
            bottom = child.getBottom();
            left = child.getRight() + layoutParams.rightMargin;
            right = left + mBuilder.mSideDividerThickness;
            if(!isLastColumn(parent, i, spanCount, childSize) || mBuilder.mDrawTwoSidesDivider) {
                mBuilder.mSideProvider.drawDivider(canvas, left, top, right, bottom);
            }
            if(isFirstColumn(parent, i, spanCount) && mBuilder.mDrawTwoSidesDivider) {
                right = child.getLeft() - layoutParams.leftMargin;
                left = right - mBuilder.mSideDividerThickness;
                mBuilder.mSideProvider.drawDivider(canvas, left, top, right, bottom);
            }
        }
    }

    private void drawOrientHorizontalDivider(Canvas canvas, RecyclerView parent) {
        final int spanCount = getSpanCount(parent);
        final int childSize = parent.getChildCount();

        for (int i = 0; i < childSize; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();

            // 画竖直分隔线.
            int top = child.getTop();
            int bottom = child.getBottom();
            int left = child.getRight() + layoutParams.rightMargin;
            int right = left + mBuilder.mDividerThickness;
            if(!isLastColumn(parent, i, spanCount, childSize) || mBuilder.mDrawBottomSideDivider) {
                mBuilder.mProvider.drawDivider(canvas, left, top, right, bottom);
            }
            if(mBuilder.mDrawTopSideDivider && isFirstColumn(parent, i, spanCount)) {
                right = child.getLeft() - layoutParams.leftMargin;
                left = right - mBuilder.mDividerThickness;
                mBuilder.mProvider.drawDivider(canvas, left, top, right, bottom);
            }

            // 画水平分隔线.
            left = child.getLeft();
            right = child.getRight();
            top = child.getBottom() + layoutParams.bottomMargin;
            bottom = top + mBuilder.mSideDividerThickness;
            if(!isLastRaw(parent, i, spanCount, childSize) || mBuilder.mDrawTwoSidesDivider) {
                mBuilder.mSideProvider.drawDivider(canvas, left, top, right, bottom);
            }
            if(isFirstRaw(parent, i, spanCount) && mBuilder.mDrawTwoSidesDivider) {
                bottom = child.getTop() - layoutParams.topMargin;
                top = bottom - mBuilder.mSideDividerThickness;
                mBuilder.mSideProvider.drawDivider(canvas, left, top, right, bottom);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // 在notifyDataSetChanged()之后并不能马上获取Adapter中的position, 要等布局结束之后才能获取到.
        // 而对于getChildLayoutPosition(), 在notifyItemInserted()之后, Layout不能马上获取到新的position
        // ,因为布局还没更新(需要<16ms的时间刷新视图), 所以只能获取到旧的, 但是Adapter中的position就可以马上获取到最新的position.
        final int itemPosition = parent.getChildLayoutPosition(view);

        final int spanCount = getSpanCount(parent);
        final int childCount = state.getItemCount();

        // 每个item分配到的offset总量.
        int dividerCount = spanCount - 1;
        if (mBuilder.mDrawTwoSidesDivider) dividerCount = dividerCount + 2;

        // 确保每个item分配到的offset总量相等
        int eachItemOffsetWidth = dividerCount * mBuilder.mSideDividerThickness / spanCount;
        int dc = eachItemOffsetWidth - mBuilder.mSideDividerThickness;

        int left = 0;
        int top = 0;
        int right;
        int bottom;

        if (mBuilder.mOrientation == GridLayoutManager.VERTICAL) {
            if (isFirstRaw(parent, itemPosition, spanCount) && mBuilder.mDrawTopSideDivider) {
                top = mBuilder.mDividerThickness;
            }
            bottom = mBuilder.mDividerThickness;
            if (isLastRaw(parent, itemPosition, spanCount, childCount) && !mBuilder.mDrawBottomSideDivider) {
                bottom = 0;
            }

            int a1 = 0;
            if(mBuilder.mDrawTwoSidesDivider) a1 = mBuilder.mSideDividerThickness;

            int spanIndex = getSpanIndex(parent, itemPosition, spanCount);
            int spanLastIndex = spanIndex + getSpanSize(parent, itemPosition) - 1;
            left = a1 - dc * spanIndex;
            right = eachItemOffsetWidth - a1 + dc * spanLastIndex;
        } else {
            if(isFirstColumn(parent, itemPosition, spanCount) && mBuilder.mDrawTopSideDivider) {
                left = mBuilder.mDividerThickness;
            }
            right = mBuilder.mDividerThickness;
            if(isLastColumn(parent, itemPosition, spanCount, childCount) && !mBuilder.mDrawBottomSideDivider) {
                right = 0;
            }

            int a1 = 0;
            if(mBuilder.mDrawTwoSidesDivider) a1 = mBuilder.mSideDividerThickness;

            int spanIndex = getSpanIndex(parent, itemPosition, spanCount);
            int spanLastIndex = spanIndex + getSpanSize(parent, itemPosition) - 1;
            top = a1 - dc * spanIndex;
            bottom = eachItemOffsetWidth - a1 + dc * spanLastIndex;
        }

        outRect.set(left, top, right, bottom);
    }

    private int getSpanIndex(RecyclerView parent, int pos, int spanCount) {
        RecyclerView.LayoutManager manager = parent.getLayoutManager();
        if(manager instanceof GridLayoutManager) {
            GridLayoutManager.SpanSizeLookup spanSizeLookup =
                    ((GridLayoutManager) manager).getSpanSizeLookup();
            return spanSizeLookup.getSpanIndex(pos, spanCount);
        } else {
            return pos % spanCount;
        }
    }

    private int getSpanSize(RecyclerView parent, int pos) {
        RecyclerView.LayoutManager manager = parent.getLayoutManager();
        if(manager instanceof GridLayoutManager) {
            GridLayoutManager.SpanSizeLookup spanSizeLookup =
                    ((GridLayoutManager) manager).getSpanSizeLookup();
            return spanSizeLookup.getSpanSize(pos);
        }
        return 1;
    }

    // utils.

    /**
     * 判断是否是第一行.
     */
    private boolean isFirstRaw(RecyclerView parent, int pos, int spanCount){
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if(layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            if(mBuilder.mOrientation == GridLayoutManager.VERTICAL
                    && pos < getGridFirstDividerOffset(parent, gridLayoutManager)) {
                return true;
            } else if(mBuilder.mOrientation == GridLayoutManager.HORIZONTAL
                    && isFirstRowInHorizontalGridLayout(gridLayoutManager, pos)) {
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            if (mBuilder.mOrientation == GridLayoutManager.VERTICAL
                    && pos < spanCount) {
                return true;
            }else if(mBuilder.mOrientation == GridLayoutManager.HORIZONTAL
                    && pos % spanCount == 0){
                return true;
            }
        }

        return false;
    }

    private static int getGridFirstDividerOffset(RecyclerView parent, GridLayoutManager manager) {
        GridLayoutManager.SpanSizeLookup spanSizeLookup = manager.getSpanSizeLookup();
        spanSizeLookup.setSpanIndexCacheEnabled(true);

        int spanCount = manager.getSpanCount();
        int itemCount = parent.getAdapter().getItemCount();
        for (int i = 1; i < itemCount; i++) {
            if (spanSizeLookup.getSpanIndex(i, spanCount) == 0) {
                return i;
            }
        }

        return spanCount;
    }

    private static boolean isFirstRowInHorizontalGridLayout(GridLayoutManager manager, int pos) {
        GridLayoutManager.SpanSizeLookup spanSizeLookup = manager.getSpanSizeLookup();
        spanSizeLookup.setSpanIndexCacheEnabled(true);

        int spanCount = manager.getSpanCount();
        if(spanSizeLookup.getSpanIndex(pos, spanCount) == 0) {
            return true;
        }
        return false;
    }


    /**
     * 判断是否是第一列.
     */
    private boolean isFirstColumn(RecyclerView parent, int pos, int spanCount){
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if(layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            if(mBuilder.mOrientation == GridLayoutManager.VERTICAL
                    && isFirstRowInHorizontalGridLayout(gridLayoutManager, pos)) {
                return true;
            } else if(mBuilder.mOrientation == GridLayoutManager.HORIZONTAL
                    && pos < getGridFirstDividerOffset(parent, gridLayoutManager)) {
                return true;
            }

        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            if (mBuilder.mOrientation == GridLayoutManager.VERTICAL
                    && pos % spanCount == 0) {
                return true;
            }else if(mBuilder.mOrientation == GridLayoutManager.HORIZONTAL
                    && pos < spanCount){
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否是最后一列.
     */
    private boolean isLastColumn(RecyclerView parent, int pos, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();

            if(mBuilder.mOrientation == GridLayoutManager.VERTICAL) {
                if(pos == childCount - 1) {
                    return spanSizeLookup.getSpanSize(pos) == spanCount;
                } else if(spanSizeLookup.getSpanIndex(pos + 1, spanCount) == 0){
                    return true;
                }
            } else if(mBuilder.mOrientation == GridLayoutManager.HORIZONTAL
                    && pos >= getGridLastDividerOffset(parent, gridLayoutManager)){
                return true;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            if (mBuilder.mOrientation == StaggeredGridLayoutManager.VERTICAL
                    && (pos + 1) % spanCount == 0) {
                return true;
            } else {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount) {
                    return true;
                }
            }
        }

        return false;
    }

    private int getGridLastDividerOffset(RecyclerView parent, GridLayoutManager manager) {
        GridLayoutManager.SpanSizeLookup spanSizeLookup = manager.getSpanSizeLookup();

        int spanCount = manager.getSpanCount();
        int itemCount = parent.getAdapter().getItemCount();
        for (int i = itemCount - 1; i >= 0; i--) {
            if (spanSizeLookup.getSpanIndex(i, spanCount) == 0) {
                return i;
            }
        }

        return itemCount - 1;
    }

    /**
     * 是否是最后一行.
     */
    private boolean isLastRaw(RecyclerView parent, int pos, int spanCount, int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();

            if(mBuilder.mOrientation == GridLayoutManager.VERTICAL
                    && pos >= getGridLastDividerOffset(parent, gridLayoutManager)) {
                return true;
            } else if(mBuilder.mOrientation == GridLayoutManager.HORIZONTAL){
                if(pos == childCount - 1) {
                    return spanSizeLookup.getSpanSize(pos) == spanCount;
                } else if(spanSizeLookup.getSpanIndex(pos + 1, spanCount) == 0){
                    return true;
                }
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            if (mBuilder.mOrientation == StaggeredGridLayoutManager.VERTICAL) {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount) {
                    return true;
                }
            } else if(mBuilder.mOrientation == StaggeredGridLayoutManager.HORIZONTAL
                    && (pos + 1) % spanCount == 0){
                return true;
            }
        }

        return false;
    }

    private int getSpanCount(RecyclerView parent) {
        int spanCount = 1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }

        return spanCount;
    }

}