package com.ntxdev.zuptecnico.fragments.reports;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.adapters.ReportItemCommentsAdapter;
import com.ntxdev.zuptecnico.api.PublishReportCommentSyncAction;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.entities.requests.CreateReportItemCommentRequest;
import com.ntxdev.zuptecnico.entities.responses.CreateReportItemCommentResponse;
import com.ntxdev.zuptecnico.ui.ScrollLessListView;
import com.ntxdev.zuptecnico.util.Utilities;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportItemCommentsFragment extends Fragment implements ReportItemCommentDialog.OnCommentListener {
    ReportItemCommentsAdapter adapter;

    ReportItem getItem() {
        return (ReportItem) getArguments().getParcelable("item");
    }

    int getFilterType() {
        return getArguments().getInt("filter_type", ReportItemCommentsAdapter.FILTER_COMMENTS);
    }

    public void refresh(ReportItem item) {
        this.adapter.updateComments(item);
        hideCreatingProgress();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_report_details_comments, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView txtTitle = (TextView) view.findViewById(R.id.comments_title);
        if(getFilterType() == ReportItemCommentsAdapter.FILTER_INTERNAL) {
            txtTitle.setText(getActivity().getString(R.string.internal_comments_title));
        }

        View createBtn = view.findViewById(R.id.comment_create);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateDialog();
            }
        });
        fillData(view);

        this.hideCreatingProgress();
    }

    private boolean canCreate() {
        boolean canCreate;
        switch (getFilterType()) {
            case ReportItemCommentsAdapter.FILTER_COMMENTS:
            default:
                canCreate = Zup.getInstance().getAccess().canCreateCommentOnReportItem(getItem().category_id);
                break;

            case ReportItemCommentsAdapter.FILTER_INTERNAL:
                canCreate = Zup.getInstance().getAccess().canCreateInternalCommentOnReportItem(getItem().category_id);
                break;
        }

        return canCreate;
    }

    void showCreateDialog() {
        ReportItemCommentDialog dialog = new ReportItemCommentDialog();
        dialog.setListener(this);
        dialog.setHasType(this.getFilterType() != ReportItemCommentsAdapter.FILTER_INTERNAL);
        dialog.show(getChildFragmentManager(), "create_dialog");
    }

    void fillData(View root) {
        if(getItem() == null)
            return;

        this.adapter = new ReportItemCommentsAdapter(getActivity(), getItem(), this.getFilterType());

        ScrollLessListView listView = (ScrollLessListView) root.findViewById(R.id.report_comments_listview);
        listView.setAdapter(this.adapter);
    }

    void showCreatingProgress() {
        if(getView() == null)
            return;

        View progress = getView().findViewById(R.id.comment_create_progress);
        View button = getView().findViewById(R.id.comment_create);

        progress.setVisibility(View.VISIBLE);
        button.setVisibility(View.GONE);
    }

    void hideCreatingProgress() {
        if(getView() == null)
            return;

        View progress = getView().findViewById(R.id.comment_create_progress);
        View button = getView().findViewById(R.id.comment_create);

        progress.setVisibility(View.GONE);
        button.setVisibility(canCreate() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onComment(int type, String text) {
        if(getItem() == null)
            return;

        showCreatingProgress();
        CreateReportItemCommentRequest request = new CreateReportItemCommentRequest();
        request.message = text;
        request.visibility = type;

        PublishReportCommentSyncAction syncAction = new PublishReportCommentSyncAction(
                getItem().id,
                type,
                text
        );
        Zup.getInstance().addSyncAction(syncAction);
        Zup.getInstance().sync();
    }
}
