package com.ccmt.template.adapter;

import android.content.Context;
import android.view.View;

import java.util.List;

public class MyAdapter extends AbstractAdapter<Object> {

    private static final int ITEM_TYPE_STRING = 3;
    private static final int ITEM_TYPE_CONTENT = 4;

    protected MyAdapter(Context context, List<Object> datas, int headerCount, int footerCount) {
        super(context, datas, headerCount, footerCount);
    }

    protected MyAdapter(Context context, List<Object> datas) {
        super(context, datas);
    }

//    @Override
//    public int getItemViewType(int position) {
//        int itemViewType = super.getItemViewType(position);
//        if (itemViewType==ITEM_TYPE_CONTENT) {
//            Object obj = mList.get(position);
//            if (obj instanceof String) {
//                return ITEM_TYPE_STRING;
//            }
//            if (obj instanceof Content) {
//                return ITEM_TYPE_CONTENT;
//            }
//        }
//        return itemViewType;
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
//        RecyclerView.ViewHolder viewHolder = super.onCreateViewHolder(viewGroup, viewType);
//        if (viewHolder==null) {
//            if (viewType==ITEM_TYPE_STRING) {
//                return TextViewHolder;
//            } else if (viewType==ITEM_TYPE_CONTENT) {
//                return ContentViewHolder;
//            }
//        }
//        return viewHolder;
//    }

    @Override
    protected View getContentView() {
        return null;
    }

    @Override
    protected View getHeaderView() {
        return null;
    }

    @Override
    protected View getBottomView() {
        return null;
    }

    @Override
    protected void onBindViewHolderHeader(HeaderViewHolder viewHolder, int position) {

    }

    @Override
    protected void onBindViewHolderContent(ContentViewHolder viewHolder, int position) {

    }

    @Override
    protected void onBindViewHolderFooter(FooterViewHolder viewHolder, int position) {

    }

}
