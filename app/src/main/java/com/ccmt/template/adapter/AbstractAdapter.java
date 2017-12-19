package com.ccmt.template.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * @author myx
 *         by 2017-08-21
 */
public abstract class AbstractAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private List<T> mList;
    private OnRecyclerViewItemClickListener mOnItemClickListener;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private OnCheckNotAllNotify mNotify;

    /**
     * item类型
     */
    private static final int ITEM_TYPE_HEADER = 0;
    private static final int ITEM_TYPE_CONTENT = 1;
    private static final int ITEM_TYPE_BOTTOM = 2;

    /**
     * 头部View个数
     */
    private int mHeaderCount = 0;

    /**
     * 底部View个数
     */
    private int mBottomCount = 0;

    private LayoutInflater mLayoutInflater;

    @SuppressWarnings("unused")
    private Typeface mTypeface;

    @SuppressWarnings("unused")
    public AbstractAdapter(Context context, List<T> datas) {
        mLayoutInflater = LayoutInflater.from(context);
        mList = datas;
//        mTypeface = CommonUtil.getSourceTypeFont(mContext);
    }

    public void setList(List<T> datas) {
        mList = datas;
    }

    public void remove(int position) {
        mList.remove(position);
    }

//    public void addItem(String content, int position) {
////        datas.add(position, content);
////        notifyItemInserted(position);
//    }

//    public void removeItem(String model) {
////        int position = datas.indexOf(model);
////        datas.remove(position);
////        notifyItemRemoved(position);//Attention!
//    }

    /**
     * 获取数据的数量
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    @Override
    public int getItemCount() {
//        return mList.size();
        return mHeaderCount + getContentItemCount() + mBottomCount;
    }

    /**
     * 内容长度
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private int getContentItemCount() {
        return mList.size();
    }

    /**
     * 判断当前item是否是HeadView
     *
     * @param position
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private boolean isHeaderView(int position) {
        return mHeaderCount != 0 && position < mHeaderCount;
    }

    /**
     * 判断当前item是否是FooterView
     *
     * @param position
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private boolean isBottomView(int position) {
        return mBottomCount != 0 && position >= (mHeaderCount + getContentItemCount());
    }

    @Override
    public int getItemViewType(int position) {
//        int dataItemCount = getContentItemCount();
        if (isHeaderView(position)) {
            // 头部View
            return ITEM_TYPE_HEADER;
        } else if (isBottomView(position)) {
            // 底部View
            return ITEM_TYPE_BOTTOM;
        } else {
            // 内容View
            return ITEM_TYPE_CONTENT;
        }
//        return super.getItemViewType(position);
    }

    /**
     * 创建新View,被LayoutManager所调用.
     *
     * @param viewGroup
     * @param viewType
     * @return
     */
    @SuppressWarnings("JavaDoc")
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == ITEM_TYPE_HEADER) {
            return new HeaderViewHolder(mLayoutInflater.inflate(getHeaderLayoutResourceId(), viewGroup, false));
//            return null;
        } else if (viewType == ITEM_TYPE_CONTENT) {
            View view = mLayoutInflater.inflate(getContentLayoutResourceId(), viewGroup, false);
            //            if (Build.VERSION.SDK_INT >= 21) {
//                view.setBackgroundResource(R.drawable.recycleritem_bg);
//            }
            return new ViewHolder(view);
        } else if (viewType == ITEM_TYPE_BOTTOM) {
            return new BottomViewHolder(mLayoutInflater.inflate(getBottomLayoutResourceId(), viewGroup, false));
//            return null;
        }
//        return new BottomViewHolderNone(mLayoutInflater.inflate(R.layout.processcolum_item_bottom_none, viewGroup, false));
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    protected abstract int getContentLayoutResourceId();

    @SuppressWarnings("WeakerAccess")
    protected abstract int getHeaderLayoutResourceId();

    @SuppressWarnings("WeakerAccess")
    protected abstract int getBottomLayoutResourceId();

    /**
     * 将数据与界面进行绑定的操作
     *
     * @param viewHolderRecycler
     * @param position
     */
    @SuppressWarnings({"JavaDoc", "InfiniteRecursion"})
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolderRecycler, int position) {
        if (viewHolderRecycler instanceof ViewHolder) {
//            // 将数据保存在button的Tag中,以便点击时进行获取.
//            viewHolder.mBtnStop.setTag(position - 1);
//
//            // 将创建的View注册点击事件
//            viewHolder.mBtnStop.setOnClickListener(this);
//
//            viewHolder.mTextViewSize.setTypeface(mTypeface);

//            viewHolder.mTextView.setText(mList.get(position).description);

            onBindViewHolderNew((ViewHolder) viewHolderRecycler, position);
        }
//        else if (viewHolder_Recycler instanceof HeaderViewHolder) {
//            HeaderViewHolder viewHolder = (HeaderViewHolder) viewHolder_Recycler;
//            String coutstring = mContext.getResources().getString(R.string.activity_detect_network_app);
//            String result = String.format(coutstring, mList.size());
//            viewHolder.mTextView_cout.setText(result);
//        }
    }

    @SuppressWarnings("WeakerAccess")
    protected abstract void onBindViewHolderNew(ViewHolder viewHolder, int position);

    /**
     * 自定义的ViewHolder,持有每个Item的的所有界面元素.
     */
    private static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View view) {
            super(view);
        }
    }

    /**
     * 头部ViewHolder
     */
    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * 底部ViewHolder
     */
    private static class BottomViewHolder extends RecyclerView.ViewHolder {
        BottomViewHolder(View itemView) {
            super(itemView);
        }
    }

//    private static class BottomViewHolderNone extends RecyclerView.ViewHolder {
//        BottomViewHolderNone(View itemView) {
//            super(itemView);
//        }
//    }

    interface OnCheckNotAllNotify {
        @SuppressWarnings("unused")
        void onNotifyFalse(boolean[] ischks);
    }

    interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int pos);
    }

    @SuppressWarnings("unused")
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @SuppressWarnings("unused")
    public void setOnNotifyNotAll(OnCheckNotAllNotify notify) {
        mNotify = notify;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            // 注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, (int) v.getTag());
        }
    }

}
