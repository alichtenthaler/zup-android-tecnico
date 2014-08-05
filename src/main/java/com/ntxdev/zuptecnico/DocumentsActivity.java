package com.ntxdev.zuptecnico;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TabHost;
import android.widget.TextView;

import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.Document;
import com.ntxdev.zuptecnico.ui.SingularTabHost;
import com.ntxdev.zuptecnico.ui.UIHelper;

import java.util.Iterator;

public class DocumentsActivity extends ActionBarActivity implements SingularTabHost.OnTabChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);

        Zup.getInstance().initStorage(getApplicationContext());

        if (savedInstanceState == null) {
            //getSupportFragmentManager().beginTransaction()
            //        .add(R.id.container, new PlaceholderFragment())
            //        .commit();
        }

        if(Build.VERSION.SDK_INT >= 11)
        {
            //this.getActionBar().hide();

        }

        UIHelper.initActivity(this, true);
        UIHelper.setTitle(this, "Todos documentos");

        android.support.v7.widget.PopupMenu menu = UIHelper.initMenu(this);
        menu.getMenu().add(Menu.NONE, 1, 0, "Todos documentos");
        menu.getMenu().add(Menu.NONE, 2, 1, "Árvores");
        menu.getMenu().add(Menu.NONE, 3, 2, "Bocas de lobo");
        menu.getMenu().add(Menu.NONE, 4, 3, "Entulho");
        menu.getMenu().add(Menu.NONE, 5, 4, "Sinalização de trânsito");

        final ActionBarActivity activity = this;
        menu.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                UIHelper.setTitle(activity, menuItem.getTitle().toString());
                return true;
            }
        });

        SingularTabHost tabHost = (SingularTabHost)findViewById(R.id.tabhost_documents);
        tabHost.setOnTabChangeListener(this);

        tabHost.addTab("all", "Todos status");
        tabHost.addTab("pending", "Pendentes");
        tabHost.addTab("running", "Em andamento");
        tabHost.addTab("finished", "Concluídos");
    }

    public void onTabChange(SingularTabHost tabHost, String oldIdentifier, String newIdentifier)
    {
        Document.State state = null;
        if(newIdentifier.equals("all"))
        {
            state = null;
        }
        else if(newIdentifier.equals("pending"))
        {
            state = Document.State.Pending;
        }
        else if(newIdentifier.equals("running"))
        {
            state = Document.State.Running;
        }
        else if(newIdentifier.equals("finished"))
        {
            state = Document.State.Finished;
        }

        Iterator<Document> results;
        if(state == null)
        {
            results = Zup.getInstance().getDocuments();
        }
        else
        {
            results = Zup.getInstance().getDocuments(state);
        }

        ((ViewGroup)findViewById(R.id.documents_container)).removeAllViews();
        while(results.hasNext())
        {
            Document document = results.next();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.documents_container, new DocumentFragment(document))
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items_list to the action bar if it is present.
        getMenuInflater().inflate(R.menu.documents, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class DocumentFragment extends Fragment implements View.OnClickListener {
        private Document document;

        public DocumentFragment() {

        }

        public DocumentFragment(Document document) {
            this.document = document;
        }

        public void onClick(View view)
        {
            Intent intent = new Intent(getActivity().getApplicationContext(), DocumentDetailsActivity.class);
            intent.putExtra("document_id", this.document.getId());
            getActivity().startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_documents, container, false);
            rootView.setOnClickListener(this);


            ImageView stateIcon = (ImageView)rootView.findViewById(R.id.fragment_document_stateicon);
            TextView title = (TextView)rootView.findViewById(R.id.fragment_document_title);
            TextView state = (TextView)rootView.findViewById(R.id.fragment_document_statedesc);
            TextView type = (TextView)rootView.findViewById(R.id.fragment_document_type);
            TextView description = (TextView)rootView.findViewById(R.id.fragment_document_desc);

            if(document != null)
            {
                title.setText("Documento #" + document.getId());
                type.setText("Árvore");

                stateIcon.setImageDrawable(getResources().getDrawable(Zup.getInstance().getDocumentStateDrawable(document.getState())));
                state.setBackgroundColor(Zup.getInstance().getDocumentStateColor(document.getState()));

                if(document.getState() == Document.State.Pending)
                {
                    rootView.setBackgroundDrawable(getResources().getDrawable(R.drawable.document_list_item_pending));
                }
            }

            return rootView;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_documents, container, false);

            return rootView;
        }
    }

}
