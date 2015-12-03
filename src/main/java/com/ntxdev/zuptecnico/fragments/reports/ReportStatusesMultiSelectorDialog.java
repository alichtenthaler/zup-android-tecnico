package com.ntxdev.zuptecnico.fragments.reports;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.adapters.ReportStatusesAdapter;
import com.ntxdev.zuptecnico.entities.ReportCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 25/08/2015.
 */
public class ReportStatusesMultiSelectorDialog extends DialogFragment implements AdapterView.OnItemClickListener{
    View confirmButton;
    List<ReportCategory.Status> selectedStatuses;
    TextView headerTextView;

    public interface OnReportStatusesSetListener {
        void onReportStatusSet(List<ReportCategory.Status> selectedStatuses);
    }

    OnReportStatusesSetListener listener;
    ReportStatusesAdapter adapter;

    public ReportStatusesMultiSelectorDialog() {
        adapter = new ReportStatusesAdapter();
        selectedStatuses = new ArrayList<ReportCategory.Status>();
    }

    public void setListener(OnReportStatusesSetListener listener) {
        this.listener = listener;
    }

    public int getSelectedStatusesCount() {
        return selectedStatuses.size();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setView(createView(getActivity().getLayoutInflater(), null, savedInstanceState));

        return builder.create();
    }

    View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_status_picker, container, false);
        ListView listView = (ListView) view.findViewById(R.id.listView);
        confirmButton = view.findViewById(R.id.confirm);

        confirmButton.setVisibility(View.INVISIBLE);

        listView.setAdapter(adapter);
        listView.setDividerHeight(0);
        listView.setOnItemClickListener(this);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
        view.findViewById(R.id.confirm).setVisibility(View.VISIBLE);
        headerTextView = (TextView) view.findViewById(R.id.header_multiselection_clear);
        headerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.clearSelection();
                selectedStatuses.clear();
                hideHeaderView();
            }
        });
        if (selectedStatuses == null)
            selectedStatuses = new ArrayList<ReportCategory.Status>();
        updateAdapter();
        return view;
    }

    public void setSelectedStatuses(String[] statusesId){
        if(statusesId == null || statusesId.length == 0 || statusesId[0].isEmpty()){
            return;
        }
        List<Integer> statusesList = new ArrayList<>();
        for(int index=0;index<statusesId.length;index++) {
            statusesList.add(Integer.parseInt(statusesId[index]));
        }
        adapter.setSelectedStatusesId(statusesList);
        selectedStatuses = adapter.getSelectedStatuses();
        showHeaderView(statusesList.size());
    }

    private void updateAdapter() {
        if (selectedStatuses != null) {
            ArrayList<Integer> statusesId = new ArrayList<Integer>();
            for (int index = 0; index < selectedStatuses.size(); index++) {
                statusesId.add(selectedStatuses.get(index).getId());
            }
            if (adapter != null) {
                adapter.setSelectedStatusesId(statusesId);
            }
            if(statusesId.size() > 0){
                showHeaderView(statusesId.size());
            }
        }
    }

    private void showHeaderView(int selectedUsersCount) {
        if (headerTextView != null) {
            headerTextView.setText(getActivity().getString(R.string.clear_selected_items) + " (" + selectedUsersCount + ")");
            headerTextView.setVisibility(View.VISIBLE);
        }
    }

    private void hideHeaderView() {
        if (headerTextView != null) {
            headerTextView.setVisibility(View.GONE);
        }
    }

    void confirm() {
        if (listener != null)
            listener.onReportStatusSet(selectedStatuses);
        this.dismiss();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ReportCategory.Status selectedStatus = adapter.getItem(i);
        toggleSelectedStatus(selectedStatus);
        adapter.setSelectedStatusId((Integer) selectedStatus.getId());
        if (adapter.getSelectedStatusesCount() > 0) {
            showHeaderView(adapter.getSelectedStatusesCount());
        } else {
            hideHeaderView();
        }

    }

    private void toggleSelectedStatus(ReportCategory.Status reportStatus) {
        if (selectedStatuses.contains(reportStatus)) {
            selectedStatuses.remove(reportStatus);
        } else {
            selectedStatuses.add(reportStatus);
        }
    }


}
