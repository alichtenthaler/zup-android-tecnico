package com.ntxdev.zuptecnico.fragments.reports;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.adapters.ReportCategoriesAdapter;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.ReportItem;

import org.w3c.dom.Text;

/**
 * Created by igorlira on 7/20/15.
 */
public class ReportItemCommentDialog extends DialogFragment implements TextWatcher {
    public interface OnCommentListener {
        void onComment(int type, String text);
    }
    OnCommentListener listener;
    boolean hasType = true;

    public void setListener(OnCommentListener listener) {
        this.listener = listener;
    }

    public void setHasType(boolean value) {
        this.hasType = value;
        if(getTypeView() != null)
            getTypeView().setVisibility(value ? View.VISIBLE : View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_comment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View confirmBtn = view.findViewById(R.id.confirm);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });

        TextView title = (TextView) view.findViewById(R.id.textView31);
        if(!hasType){
            title.setText(getActivity().getString(R.string.internal_comments_title));
        }

        EditText textView = (EditText) view.findViewById(R.id.comment_text);
        textView.addTextChangedListener(this);

        getTypeView().setVisibility(hasType ? View.VISIBLE : View.GONE);

        view.findViewById(R.id.comment_type_private_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPrivate();
            }
        });

        view.findViewById(R.id.comment_type_public_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPublic();
            }
        });

        setPrivate();
        getConfirmButton().setEnabled(false);
    }

    void setPrivate() {
        if(getView() == null)
            return;

        RadioButton btnPrivate = (RadioButton) getView().findViewById(R.id.comment_type_private);
        RadioButton btnPublic = (RadioButton) getView().findViewById(R.id.comment_type_public);

        btnPrivate.setChecked(true);
        btnPublic.setChecked(false);
    }

    void setPublic() {
        if(getView() == null)
            return;

        RadioButton btnPrivate = (RadioButton) getView().findViewById(R.id.comment_type_private);
        RadioButton btnPublic = (RadioButton) getView().findViewById(R.id.comment_type_public);

        btnPrivate.setChecked(false);
        btnPublic.setChecked(true);
    }

    View getConfirmButton() {
        if(getView() == null)
            return null;

        return getView().findViewById(R.id.confirm);
    }

    View getTypeView() {
        if(getView() == null)
            return null;

        return getView().findViewById(R.id.comment_type);
    }

    TextView getTextView() {
        if(getView() == null)
            return null;

        return (TextView) getView().findViewById(R.id.comment_text);
    }

    int getCommentType() {
        RadioButton btnPrivate = (RadioButton) getView().findViewById(R.id.comment_type_private);
        RadioButton btnPublic = (RadioButton) getView().findViewById(R.id.comment_type_public);

        if(!hasType)
            return ReportItem.CommentType.TYPE_INTERNAL;
        else if(btnPrivate.isChecked())
            return ReportItem.CommentType.TYPE_PRIVATE;
        else
            return ReportItem.CommentType.TYPE_PUBLIC;
    }

    void confirm() {
        TextView textView = getTextView();

        if(this.listener != null)
            this.listener.onComment(getCommentType(), textView.getText().toString());

        dismiss();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        getConfirmButton().setEnabled(charSequence.length() > 0);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
