package ramt57.infotrench.com.callrecorder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;

import ramt57.infotrench.com.callrecorder.SqliteDatabase.ContactsDatabase;
import ramt57.infotrench.com.callrecorder.SqliteDatabase.DatabaseHelper;
import ramt57.infotrench.com.callrecorder.adapter.FavouriteAdapter;
import ramt57.infotrench.com.callrecorder.pojo_classes.Contacts;
import ramt57.infotrench.com.callrecorder.utils.StringUtils;

/**
 * Created by sandhya on 29-Aug-17.
 */

public class Favourite  extends AppCompatActivity{
    FavouriteAdapter recyclerAdapter;
    RecyclerView recyclerView;
    ArrayList<Contacts> recordedContacts=new ArrayList<>();
    ArrayList<Contacts> realContacts=new ArrayList<>();
    private AdView mAdView;
    LinearLayout message;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourite_layout);
        Toolbar toolbar=findViewById(R.id.action_bar);
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        toolbar.setTitle("Favourite");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        DatabaseHelper db=new DatabaseHelper(getApplicationContext());
        recordedContacts.clear();
        recordedContacts=db.getAllContacts();
        message=findViewById(R.id.hidemessage);
        for(Contacts contacts:recordedContacts){
            boolean hascontact = false;
            ContactsDatabase database=new ContactsDatabase(this);
            ArrayList<Contacts> goContacts=database.AllContacts();
            for (Contacts contacts1:goContacts){
                if(StringUtils.prepareContacts(this,contacts.getNumber()).equals(StringUtils.prepareContacts(this,contacts1.getNumber()))){
                    realContacts.add(contacts1);
                    hascontact = true;
                    break;
                }
            }
            if(!hascontact){
                realContacts.add(contacts);
            }
        }
        if(realContacts.isEmpty()){
            message.setVisibility(View.VISIBLE);
        }else{
            message.setVisibility(View.GONE);
        }
        recyclerAdapter.setContacts(realContacts);
        recyclerAdapter.notifyDataSetChanged();
    }
    private void init() {
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getApplicationContext())
                        .color(Color.parseColor("#dadde2"))
                        .sizeResId(R.dimen.divider)
                        .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                        .build());
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter=new  FavouriteAdapter();
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.setListener(new FavouriteAdapter.OnitemClickListener() {

            @Override
            public void onClick(View v, int position) {
                Intent intent=new Intent(v.getContext(),ListenActivity.class);
                intent.putExtra("NUMBER",StringUtils.prepareContacts(getApplicationContext(),realContacts.get(position).getNumber()));
                startActivity(intent);
            }
        });
    }
}
