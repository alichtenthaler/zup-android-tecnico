package com.ntxdev.zuptecnico.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.adapters.UsersAdapter;
import com.ntxdev.zuptecnico.entities.User;

/**
 * Created by igorlira on 7/23/15.
 */
public class UserPickerDialog extends DialogFragment implements AdapterView.OnItemClickListener {
    UsersAdapter adapter;
    User selectedUser;
    private EditText searchText;

    public interface OnUserPickedListener {
        void onUserPicked(User user);
    }

    private OnUserPickedListener listener;

    public void setListener(OnUserPickedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(DialogFragment.STYLE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_userpicker, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hideConfirmButton();
        ListView listView = (ListView) view.findViewById(R.id.listView);

        listView.setDividerHeight(0);
        listView.setOnItemClickListener(this);

        searchText = (EditText) view.findViewById(R.id.search_edit);
        searchText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchTextChanged(charSequence.toString());
            }

            public void afterTextChanged(Editable editable) {

            }
        });

        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });

        loadAdapter(listView);
    }

    void loadAdapter(ListView listView){
        adapter = new UsersAdapter(this.getActivity());
        listView.setAdapter(adapter);
        adapter.load();
    }

    void showConfirmButton() {
        getView().findViewById(R.id.confirm).setVisibility(View.VISIBLE);
    }

    void hideConfirmButton() {
        getView().findViewById(R.id.confirm).setVisibility(View.INVISIBLE);
    }

    void searchTextChanged(String newQuery) {
        adapter.setQuery(newQuery);
    }

    void confirm() {
        if(this.selectedUser == null)
            return;

        if(this.listener != null)
            this.listener.onUserPicked(this.selectedUser);

        this.dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (searchText != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        this.selectedUser = adapter.getItem(i);
        adapter.setSelectedUserId(this.selectedUser.id);
        showConfirmButton();
    }
}
