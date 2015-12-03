package com.ntxdev.zuptecnico.fragments.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.util.Utilities;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportItemGeneralInfoFragment extends Fragment {
    ReportItem getItem() {
        return (ReportItem) getArguments().getParcelable("item");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_report_details_general, container, false);
        fillData(root);
        return root;
    }

    public void refresh() {
        fillData((ViewGroup) getView());
    }

    void fillData(ViewGroup root) {
        if(getItem() == null)
            return;

        ReportItem item = getItem();

        ReportCategory category = Zup.getInstance().getReportCategoryService().getReportCategory(item.category_id);
        ReportCategory.Status status = null;
        if(item.status_id != -1)
            status = category.getStatus(item.status_id);

        TextView txtProtocol = (TextView) root.findViewById(R.id.protocol);
        TextView txtAddress = (TextView) root.findViewById(R.id.full_address);
        TextView txtReference = (TextView) root.findViewById(R.id.reference);
        TextView txtDescription = (TextView) root.findViewById(R.id.description);
        TextView txtCategory = (TextView) root.findViewById(R.id.category_name);
        TextView txtCreation = (TextView) root.findViewById(R.id.creation_date);
        TextView txtStatus = (TextView) root.findViewById(R.id.status);
        TextView txtGroup = (TextView) root.findViewById(R.id.responsible_group_name);
        TextView txtUser = (TextView) root.findViewById(R.id.responsible_user_name);

        txtProtocol.setText(item.protocol);
        txtAddress.setText(item.getFullAddress());
        txtReference.setText(notInformedIfBlank(item.reference));
        txtDescription.setText(notInformedIfBlank(item.description));
        txtCategory.setText(category.title);
        txtCreation.setText(Utilities.formatIsoDateAndTime(item.created_at));
        if(status != null)
            txtStatus.setText(status.getTitle());
        else
            txtStatus.setText("Sem status.");

        if(item.assignedUser != null)
            txtUser.setText(item.assignedUser.name);
        else
            txtUser.setText("Não há um usuário responsável pelo relato.");

        if(item.assignedGroup != null)
            txtGroup.setText(item.assignedGroup.getName());
        else
            txtGroup.setText("Não há um grupo responsável pelo relato.");
    }

    String notInformedIfBlank(String value) {
        if(value == null || value.isEmpty())
            return "Não informado.";
        else
            return value;
    }
}
