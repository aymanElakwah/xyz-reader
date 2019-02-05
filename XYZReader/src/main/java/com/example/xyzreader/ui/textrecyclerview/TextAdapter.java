package com.example.xyzreader.ui.textrecyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.xyzreader.R;

public class TextAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private String mLines[];

    private static final int HEADER = 0;
    private static final int ITEM = 1;
    private boolean hasHeader;
    private String mTitle;
    private Spanned mBy;
    private int mColor;

    public TextAdapter(Context context) {
        mContext = context;
    }

    public TextAdapter(Context context, String title, Spanned by) {
        mContext = context;
        hasHeader = true;
        mTitle = title;
        mBy = by;
    }

    public void setData(String lines[]) {
        mLines = lines;
    }

    public void setColor(int color) {
        mColor = color;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        if(type == HEADER) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.article_title, viewGroup, false);
            return new HeaderViewHolder(view, mTitle, mBy);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.line_of_text, viewGroup, false);
            return new TextViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if(viewHolder instanceof TextViewHolder) {
            i -= hasHeader ? 1 : 0;
            ((TextViewHolder) viewHolder).bind(mLines[i]);
        } else {
            ((HeaderViewHolder) viewHolder).setColor(mColor);
        }
    }

    @Override
    public int getItemCount() {
        int count = (mLines == null? 0 : mLines.length);
        count += hasHeader ? 1 : 0;
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if(hasHeader && position == 0)
            return HEADER;
        return ITEM;
    }
}
