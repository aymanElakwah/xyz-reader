package com.example.xyzreader.ui.textrecyclerview;

import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.ui.ArticleListActivity;

public class HeaderViewHolder extends ArticleListActivity.ViewHolder {
    private View mView;
    public HeaderViewHolder(View view, String title, Spanned by) {
        super(view);
        mView = view.findViewById(R.id.meta_bar);
        ((TextView) view.findViewById(R.id.article_title)).setText(title);
        ((TextView) view.findViewById(R.id.article_byline)).setText(by);
    }

    public void setColor(int color) {
        mView.setBackgroundColor(color);

    }
}
