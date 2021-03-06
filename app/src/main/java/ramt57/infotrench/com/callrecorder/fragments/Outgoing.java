package ramt57.infotrench.com.callrecorder.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import ramt57.infotrench.com.callrecorder.MainActivity;
import ramt57.infotrench.com.callrecorder.R;
import ramt57.infotrench.com.callrecorder.adapter.OutgoingAdapter;
import ramt57.infotrench.com.callrecorder.contacts.ContactProvider;
import ramt57.infotrench.com.callrecorder.pojo_classes.Contacts;
import ramt57.infotrench.com.callrecorder.utils.StringUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class Outgoing extends Fragment {
    private OutgoingAdapter recyclerAdapter;
    RecyclerView recyclerView;
    int temp;
    ArrayList<String> recording2=new ArrayList<>();
    ArrayList<Contacts> recordedContacts=new ArrayList<>();
    ArrayList<Object> searchPeople=new ArrayList<>();
    ArrayList<Integer> integers=new ArrayList<>();
    ArrayList<Object> realrecordingcontact=new ArrayList<>();
    TreeMap<String ,ArrayList<Contacts>> headerevent=new TreeMap<>();
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    boolean mensu=false;
    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayout message;
    Context ctx;
    public Outgoing() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.outgoing_fragment,container,false);
        ctx=view.getContext();
        recyclerView=view.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getContext())
                        .color(Color.parseColor("#dadde2"))
                        .sizeResId(R.dimen.divider)
                        .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                        .build());
        recyclerView.setHasFixedSize(true);
        message=view.findViewById(R.id.hidemessage);
        LinearLayoutManager layoutManager=new LinearLayoutManager(view.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter=new OutgoingAdapter();
        recyclerView.setAdapter(recyclerAdapter);
        Bundle bundle;
        bundle=getArguments();
        swipeRefreshLayout=view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });
        recording2=bundle.getStringArrayList("RECORDING");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ctx.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            showContact();
        }
        if(realrecordingcontact.isEmpty()){
            message.setVisibility(View.VISIBLE);
        }else{
            message.setVisibility(View.GONE);
        }
        recyclerAdapter.setContacts(realrecordingcontact);
        recyclerAdapter.setListener(new OutgoingAdapter.ItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                if(mensu){
                    Contacts contacts1= (Contacts) searchPeople.get(position);
                    String records=ContactProvider.getRecordsList(v.getContext(),recording2,"OUT",contacts1);
                    if(Build.VERSION.SDK_INT>18){
                        ContactProvider.openMaterialSheetDialog(getLayoutInflater(),position,records, StringUtils.prepareContacts(ctx,contacts1.getNumber()));
                    }else{
                        ContactProvider.showDialog(v.getContext(),records,contacts1);
                    }
                }else {
//                    Contacts contacts= (Contacts) realrecordingcontact.get(position);
//                    String records=ContactProvider.getRecordsList(v.getContext(),recording2,"OUT",contacts);
//                    if(Build.VERSION.SDK_INT>18){
//                        ContactProvider.openMaterialSheetDialog(getLayoutInflater(),position,records,StringUtils.prepareContacts(ctx,contacts.getNumber()));
//                    }else{
//                        ContactProvider.showDialog(v.getContext(),records,contacts);
//                    }

                    try{
                        Contacts contacts = (Contacts) realrecordingcontact.get(position);
                        String records = ContactProvider.getRecordsList(v.getContext(), recording2, "", contacts);
                        if (Build.VERSION.SDK_INT > 18) {
                            try {
                                ContactProvider.openMaterialSheetDialog(getLayoutInflater(), position, records, StringUtils.prepareContacts(ctx, contacts.getNumber()));
                            }catch (Exception e1){
                                e1.printStackTrace();
                            }
                        } else {
                            ContactProvider.showDialog(v.getContext(), records, contacts);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                ContactProvider.setItemrefresh(new ContactProvider.refresh() {
                    @Override
                    public void refreshList(boolean var) {
                        if(var)
                            showContact();
                    }
                });
            }
        });
        MainActivity.setQueylistener3(new MainActivity.querySearch3() {
            @Override
            public void Search_name3(String name) {
                if(name.length()>2){
                    mensu=true;
                    searchPeople.clear();
                    for(Contacts contacts:recordedContacts){
                        if(contacts.getNumber().contains(name)){
                            searchPeople.add(contacts);
                            continue;
                        }
                        if(contacts.getName()!=null&&contacts.getName().toLowerCase().contains(name.toLowerCase())){
                            searchPeople.add(contacts);
                        }

                    }
                            recyclerAdapter.setContacts(searchPeople);
                            recyclerAdapter.notifyDataSetChanged();

                }else{
                    mensu=false;
                    if(realrecordingcontact.isEmpty()){
                        message.setVisibility(View.VISIBLE);
                    }else{
                        message.setVisibility(View.GONE);
                    }
                    recyclerAdapter.setContacts(realrecordingcontact);
                    recyclerAdapter.notifyDataSetChanged();
                }
            }
        });

        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContact();
            } else {
                Toast.makeText(getContext(), "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void refreshItems() {
        recording2=ContactProvider.showlistfiles(ctx);
        showContact();
        if(realrecordingcontact.isEmpty()){
            message.setVisibility(View.VISIBLE);
        }else{
            message.setVisibility(View.GONE);
        }
        recyclerAdapter.setContacts(realrecordingcontact);
        recyclerAdapter.notifyDataSetChanged();
    }
    private void showContact() {
        headerevent.clear();
        recording2=ContactProvider.showlistfiles(ctx);
        ArrayList<Contacts> contactses = new ArrayList<>();
        if(!realrecordingcontact.isEmpty()){
            realrecordingcontact.clear();
        }
        if(!recordedContacts.isEmpty()){
            recordedContacts.clear();
        }
        recordedContacts=ContactProvider.getCallList(getContext(),recording2,"OUT");
        for (Contacts contacts:recordedContacts){
            if(contacts.getView()==1){
                if(!headerevent.containsKey("1")){
                    headerevent.put("1",new ArrayList<Contacts>());
                }
                headerevent.get("1").add(contacts);
            }else if(contacts.getView()==2){
                if(!headerevent.containsKey("2")){
                    headerevent.put("2",new ArrayList<Contacts>());
                }
                headerevent.get("2").add(contacts);
            }else {
                if(!headerevent.containsKey(contacts.getDate())){
                    headerevent.put(contacts.getDate(),new ArrayList<Contacts>());
                }
                headerevent.get(contacts.getDate()).add(contacts);
            }
        }
        for (String date2:headerevent.keySet()){
            if(date2.equals("1")){
                if(headerevent.keySet().contains("2")){
                    date2="2";
                }
            }else if(date2.equals("2")){
                if(headerevent.keySet().contains("1")){
                    date2="1";
                }
            }
            contactses.clear();
            for (Contacts contacts : headerevent.get(date2)) {
                contactses.add(contacts);
            }
            for (Contacts contacts : sorts(contactses)) {
                realrecordingcontact.add(contacts);
            }
            realrecordingcontact.add(date2);
        }
        if(realrecordingcontact.isEmpty()){
            message.setVisibility(View.VISIBLE);
        }else{
            message.setVisibility(View.GONE);
        }
        recyclerAdapter.notifyDataSetChanged();
        if(swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(ctx, "Records refreshed.", Toast.LENGTH_SHORT).show();
        }
    }
    private ArrayList<Contacts> sorts(ArrayList<Contacts> contactses) {
        Collections.sort(contactses);
        return contactses;
    }
}
