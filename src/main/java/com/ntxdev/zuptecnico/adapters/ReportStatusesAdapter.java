package com.ntxdev.zuptecnico.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.ReportCategory.Status;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Renan on 25/08/2015.
 */
public class ReportStatusesAdapter extends BaseAdapter {
    private List<Integer> selectedStatusesId;
    private Status[] statusList;

    public ReportStatusesAdapter() {
        selectedStatusesId = new ArrayList<>();
        Set<Status> statusesList = new HashSet<>();
        ReportCategory[] categoryList = Zup.getInstance().getReportCategoryService().getReportCategories();
        if (categoryList != null) {
            for (int index = 0; index < categoryList.length; index++) {
                ReportCategory category = categoryList[index];
                if (category.statuses != null) {
                    for (int auxIndex = 0; auxIndex < category.statuses.length; auxIndex++) {
                        statusesList.add(category.statuses[auxIndex]);
                    }
                }
            }
        }
        statusList = new Status[statusesList.size()];
        Iterator<Status> iterator = statusesList.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            statusList[i] = iterator.next();
            i++;
        }
    }

    public List<Integer> getSelectedStatusesId() {
        return selectedStatusesId;
    }

    public void clearSelection() {
        selectedStatusesId.clear();
        notifyDataSetInvalidated();
    }

    public void setSelectedStatusesId(List<Integer> usersId) {
        selectedStatusesId.clear();
        selectedStatusesId.addAll(usersId);
        notifyDataSetInvalidated();
    }

    public List<Status> getSelectedStatuses(){
        List<Status> statusesList = new ArrayList<Status>();
        if(selectedStatusesId != null) {
            for (int index = 0; index < selectedStatusesId.size(); index++) {
                for (int j = 0; j < getCount(); j++) {
                    if (getItemId(j) == selectedStatusesId.get(index)) {
                        statusesList.add(getItem(j));
                        break;
                    }
                }
            }
        }
        return statusesList;
    }

    public void setSelectedStatusId(Integer selectedUserId) {
        if (selectedStatusesId.contains(selectedUserId)) {
            selectedStatusesId.remove(selectedUserId);
        } else {
            selectedStatusesId.add(selectedUserId);
        }
        notifyDataSetInvalidated();
    }

    public int getSelectedStatusesCount() {
        return selectedStatusesId.size();
    }

    @Override
    public int getCount() {
        return statusList == null ? 0 : statusList.length;
    }

    @Override
    public Status getItem(int position) {
        return statusList == null ? null : statusList[position];
    }

    @Override
    public long getItemId(int position) {
        return getItem(position) == null ? 0 : getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.statuses_list_item, parent, false);
        Status status = getItem(position);
        TextView statusName = (TextView) root.findViewById(R.id.status_name);
        CheckBox checkBox = (CheckBox) root.findViewById(R.id.status_selected_checkbox);
        checkBox.setChecked(selectedStatusesId.contains(status.getId()) ? true : false);
        statusName.setText(status.getTitle());
        return root;
    }
}
