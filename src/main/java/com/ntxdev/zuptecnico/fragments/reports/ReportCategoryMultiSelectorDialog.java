package com.ntxdev.zuptecnico.fragments.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.adapters.ReportCategoriesMultiSelectAdapter;
import com.ntxdev.zuptecnico.adapters.UsersMultiSelectAdapter;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 24/08/2015.
 */
public class ReportCategoryMultiSelectorDialog extends ReportCategorySelectorDialog {
    private OnReportCategoryMultiSelectPickedListener multiSelectPickedListener;
    List<ReportCategory> selectedCategories;
    TextView headerTextView;

    public interface OnReportCategoryMultiSelectPickedListener {
        void onReportCategoriesSet(List<ReportCategory> categories);
    }

    public void setListener(OnReportCategoryMultiSelectPickedListener listener) {
        multiSelectPickedListener = listener;
    }

    public ReportCategoryMultiSelectorDialog() {
        adapter = new ReportCategoriesMultiSelectAdapter();
        selectedCategories = new ArrayList<ReportCategory>();
    }

    public int getSelectedCategoriesCount() {
        return selectedCategories.size();
    }

    @Override
    View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.createView(inflater, container, savedInstanceState);
        view.findViewById(R.id.confirm).setVisibility(View.VISIBLE);
        headerTextView = (TextView) view.findViewById(R.id.header_multiselection_clear);
        headerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReportCategoriesMultiSelectAdapter) adapter).clearSelection();
                selectedCategories.clear();
                hideHeaderView();
            }
        });
        if (selectedCategories == null)
            selectedCategories = new ArrayList<ReportCategory>();
        updateAdapter();
        return view;
    }

    public void setSelectedCategories(String[] categories){
        if(categories == null || categories.length == 0 || categories[0].isEmpty()){
            return;
        }
        List<Integer> categoriesIdList = new ArrayList<>();
        for(int index=0;index<categories.length;index++) {
            categoriesIdList.add(Integer.parseInt(categories[index]));
        }
        ((ReportCategoriesMultiSelectAdapter) adapter).setSelectedCategoriesId(categoriesIdList);
        selectedCategories = ((ReportCategoriesMultiSelectAdapter) adapter).getSelectedCategories();
        showHeaderView(categoriesIdList.size());
    }

    private void updateAdapter() {
        if (selectedCategories != null) {
            ArrayList<Integer> categoriesId = new ArrayList<Integer>();
            for (int index = 0; index < selectedCategories.size(); index++) {
                categoriesId.add(selectedCategories.get(index).id);
            }
            if (adapter != null) {
                ((ReportCategoriesMultiSelectAdapter) adapter).setSelectedCategoriesId(categoriesId);
            }
            if (categoriesId.size() > 0) {
                showHeaderView(categoriesId.size());
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


    @Override
    void confirm(int id) {
        if (multiSelectPickedListener != null)
            multiSelectPickedListener.onReportCategoriesSet(selectedCategories);
        this.dismiss();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ReportCategory selectedCategory = adapter.getItem(i);
        ((ReportCategoriesMultiSelectAdapter) adapter).setSelectedCategoryId((Integer) selectedCategory.id);
        toggleSelectedCategory(selectedCategory);
        if (((ReportCategoriesMultiSelectAdapter) adapter).getSelectedCategoriesCount() > 0) {
            showHeaderView(((ReportCategoriesMultiSelectAdapter) adapter).getSelectedCategoriesCount());
        } else {
            hideHeaderView();
        }

    }

    private void toggleSelectedCategory(ReportCategory reportCategory) {
        boolean isToRemove = false;
        boolean isRoot = reportCategory.parent_id == null || (reportCategory.subcategories != null && reportCategory.subcategories.length > 0);
        if (selectedCategories.contains(reportCategory)) {
            selectedCategories.remove(reportCategory);
            isToRemove = true;
        } else {
            selectedCategories.add(reportCategory);
        }
        if (isRoot) {
            //Se uma categoria pai é selecionada ou desselecionada, todas as categorias filho devem seguir o comportamento.
            for (int index = 0; index < reportCategory.subcategories.length; index++) {
                selectedCategories.remove(reportCategory.subcategories[index]);
                ((ReportCategoriesMultiSelectAdapter) adapter).removeSelectedCategoryId(reportCategory.subcategories[index].id);
                if (!isToRemove) {
                    if (!selectedCategories.contains(reportCategory.subcategories[index])) {
                        selectedCategories.add(reportCategory.subcategories[index]);
                    }
                    ((ReportCategoriesMultiSelectAdapter) adapter).insertSelectedCategoryId(reportCategory.subcategories[index].id);
                }
            }
        } else {
            //Caso toda a categoria pai esteja selecionada e o usuário desseleciona uma categoria filho, a categoria pai é desselecionada  tb
            ReportCategory parentCategory = new ReportCategory();
            parentCategory.id = reportCategory.parent_id;
            if (isToRemove) {
                selectedCategories.remove(parentCategory);
                ((ReportCategoriesMultiSelectAdapter) adapter).removeSelectedCategoryId(reportCategory.parent_id);
            }
        }

    }
}
