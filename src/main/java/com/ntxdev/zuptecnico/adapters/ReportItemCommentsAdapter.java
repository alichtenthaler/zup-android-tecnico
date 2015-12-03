package com.ntxdev.zuptecnico.adapters;

import android.content.Context;
import android.os.Build;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.util.Utilities;

import java.util.ArrayList;

/**
 * Created by igorlira on 7/24/15.
 */
public class ReportItemCommentsAdapter extends BaseAdapter {
    public static final int FILTER_COMMENTS = 1;
    public static final int FILTER_INTERNAL = 2;

    Context context;
    ReportItem.Comment[] items;
    SparseArray<View> viewCache;
    int filter;

    public ReportItemCommentsAdapter(Context context, ReportItem item, int filter) {
        this.context = context;
        this.viewCache = new SparseArray<>();
        this.filter = filter;

        this.updateComments(item);
    }

    public void updateComments(ReportItem item) {
        this.items = filterComments(item.comments);
        this.notifyDataSetInvalidated();
    }

    ReportItem.Comment[] filterComments(ReportItem.Comment[] comments) {
        ArrayList<ReportItem.Comment> result = new ArrayList<>();

        for(int i = 0; i < comments.length; i++) {
            if(comments[i].visibility == ReportItem.CommentType.TYPE_INTERNAL) {
                if(this.filter == FILTER_INTERNAL)
                    result.add(comments[i]);
            }
            else if(comments[i].visibility == ReportItem.CommentType.TYPE_PUBLIC
                    || comments[i].visibility == ReportItem.CommentType.TYPE_PRIVATE) {
                if(this.filter == FILTER_COMMENTS)
                    result.add(comments[i]);
            }
        }

        ReportItem.Comment[] resultArray = new ReportItem.Comment[result.size()];
        result.toArray(resultArray);

        return resultArray;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public ReportItem.Comment getItem(int i) {
        return items[i];
    }

    @Override
    public long getItemId(int i) {
        return getItem(i).id;
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {
        ReportItem.Comment comment = getItem(i);

        if(viewCache.get(comment.id) != null)
            return viewCache.get(comment.id);
        else {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.report_comment_item, viewGroup, false);
            fillData(view, comment);

            return view;
        }
    }

    void fillData(View view, ReportItem.Comment comment) {
        view.findViewById(R.id.fake_comment_indicator).setVisibility(comment.isFake ? View.VISIBLE : View.INVISIBLE);

        TextView txtUsername = (TextView) view.findViewById(R.id.comment_username);
        TextView txtDate = (TextView) view.findViewById(R.id.comment_date);
        TextView txtText = (TextView) view.findViewById(R.id.comment_text);

        String extraDate = "";
        if(comment.visibility == ReportItem.CommentType.TYPE_PRIVATE) {
            extraDate = " - " + context.getString(R.string.comment_private);
        }
        else if(comment.visibility == ReportItem.CommentType.TYPE_INTERNAL) {
            if(Build.VERSION.SDK_INT >= 21) {
                txtText.setBackground(context.getDrawable(R.drawable.report_internal_comment_bg));
            } else {
                txtText.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.report_internal_comment_bg));
            }
        }

        txtUsername.setText(comment.author.name);
        txtDate.setText(Utilities.formatIsoDateAndTime(comment.created_at) + extraDate);
        txtText.setText(comment.message);
    }
}
