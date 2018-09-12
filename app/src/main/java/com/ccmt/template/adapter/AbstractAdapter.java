package com.ccmt.template.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class AbstractAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    protected Context mContext;
    @SuppressWarnings("WeakerAccess")
    protected List<T> mList;
    @SuppressWarnings("WeakerAccess")
    protected OnRecyclerViewItemClickListener mOnItemClickListener;
    @SuppressWarnings("WeakerAccess")
    protected OnCheckNotAllNotify mNotify;

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
    private int mFooterCount = 0;

    @SuppressWarnings("WeakerAccess")
    protected LayoutInflater mLayoutInflater;

    @SuppressWarnings("unused")
    protected Typeface mTypeface;

    protected AbstractAdapter(Context context, List<T> datas, int headerCount, int footerCount) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mList = datas;
        mHeaderCount = headerCount;
        mFooterCount = footerCount;
//        mTypeface = CommonUtil.getSourceTypeFont(mContext);
    }

    protected AbstractAdapter(Context context, List<T> datas) {
        this(context, datas, 0, 0);
    }

    /**
     * 在Activity和Fragment的onDestory()方法中调用,确保不会造成该类对象的内存泄露.
     */
    @SuppressWarnings("unused")
    public void release() {
        mContext = null;
        mLayoutInflater = null;
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
        return mHeaderCount + getContentItemCount() + mFooterCount;
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
        return mFooterCount != 0 && position >= (mHeaderCount + getContentItemCount());
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
            return new HeaderViewHolder(getHeaderView());
//            return null;
        } else if (viewType == ITEM_TYPE_CONTENT) {
            View view = getContentView();
            //            if (Build.VERSION.SDK_INT >= 21) {
//                view.setBackgroundResource(R.drawable.recycleritem_bg);
//            }
            return new ContentViewHolder(view);
        } else if (viewType == ITEM_TYPE_BOTTOM) {
            return new FooterViewHolder(getBottomView());
//            return null;
        }
//        return new BottomViewHolderNone(mLayoutInflater.inflate(R.layout.processcolum_item_bottom_none, viewGroup, false));
        return null;
    }

    protected abstract View getContentView();

    protected abstract View getHeaderView();

    protected abstract View getBottomView();

    /**
     * 将数据与界面进行绑定的操作
     *
     * @param viewHolder
     * @param position
     */
    @SuppressWarnings({"JavaDoc", "InfiniteRecursion"})
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        viewHolder.itemView.setTag(position);

        if (viewHolder instanceof ContentViewHolder) {
//            // 将数据保存在button的Tag中,以便点击时进行获取.
//            viewHolder.mBtnStop.setTag(position - 1);
//
//            // 将创建的View注册点击事件
//            viewHolder.mBtnStop.setOnClickListener(this);
//
//            viewHolder.mTextViewSize.setTypeface(mTypeface);

//            viewHolder.mTextView.setText(mList.get(position).description);

            onBindViewHolderContent((ContentViewHolder) viewHolder, position);
        } else if (viewHolder instanceof HeaderViewHolder) {
//            HeaderViewHolder viewHolder = (HeaderViewHolder) viewHolder_Recycler;
//            String coutstring = mContext.getResources().getString(R.string.activity_detect_network_app);
//            String result = String.format(coutstring, mList.size());
//            viewHolder.mTextView_cout.setText(result);
            onBindViewHolderHeader((HeaderViewHolder) viewHolder, position);
        } else if (viewHolder instanceof FooterViewHolder) {
            onBindViewHolderFooter((FooterViewHolder) viewHolder, position);
        }
    }

    protected abstract void onBindViewHolderHeader(HeaderViewHolder viewHolder, int position);

    protected abstract void onBindViewHolderContent(ContentViewHolder viewHolder, int position);

    protected abstract void onBindViewHolderFooter(FooterViewHolder viewHolder, int position);

    /**
     * 自定义的ViewHolder,持有每个Item的的所有界面元素.
     */
    static class ContentViewHolder extends RecyclerView.ViewHolder {
        ContentViewHolder(View view) {
            super(view);
        }
    }

    /**
     * 头部ViewHolder
     */
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * 底部ViewHolder
     */
    static class FooterViewHolder extends RecyclerView.ViewHolder {
        FooterViewHolder(View itemView) {
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
