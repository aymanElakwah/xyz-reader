package com.example.xyzreader.ui.textrecyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;

public class TextViewHolder extends RecyclerView.ViewHolder {
    private TextView block;
    public TextViewHolder(@NonNull View view) {
        super(view);
        block = view.findViewById(R.id.single_text_block);
    }

    public void bind(String text) {
        block.setText(text.trim());
    }
}
