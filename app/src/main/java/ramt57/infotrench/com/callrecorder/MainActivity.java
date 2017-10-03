package ramt57.infotrench.com.callrecorder;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

import ramt57.infotrench.com.callrecorder.SqliteDatabase.ContactsDatabase;
import ramt57.infotrench.com.callrecorder.Transformer.ZoomOutPageTransformer;
import ramt57.infotrench.com.callrecorder.adapter.ScreenSlidePagerAdapter;
import ramt57.infotrench.com.callrecorder.contacts.ContactProvider;
import ramt57.infotrench.com.callrecorder.fragments.AllFragment;
import ramt57.infotrench.com.callrecorder.fragments.Incomming;
import ramt57.infotrench.com.callrecorder.fragments.Outgoing;
import ramt57.infotrench.com.callrecorder.pojo_classes.Contacts;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private  ViewPager viewPager;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ScreenSlidePagerAdapter adapter;
    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;
    static querySearch queylistener;
    static querySearch2 queylistener2;
    static querySearch3 queylistener3;
    static refreshstener refreshlistenerobj;
    ArrayList<Contacts> phoneContacts=new ArrayList<>();
    ArrayList<String> recordinglist=new ArrayList<>();
    private InterstitialAd mInterstitialAd;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 2001;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    SharedPreferences prefofsync;

    private AdView mAdView;
    ProgressDialog bar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar=findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        bar=new ProgressDialog(this);
        bar.setMessage("Fetching Contacts..");
        prefofsync=getSharedPreferences("SYNC",MODE_PRIVATE);
        MobileAds.initialize(this, "ca-app-pub-8475322962539552~2909231737");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8475322962539552/4790834204");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        boolean Auth=getIntent().getBooleanExtra("AUTH",false);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        SharedPreferences SP1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean b1=SP1.getBoolean("LOCK",false);
        if(b1&&!Auth){
            Intent intent=new Intent(getApplicationContext(),NewPinLock.class);
            finish();
            startActivity(intent);
        }
//        initAdmin();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
            checkAndRequestPermissions();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager=findViewById(R.id.viewpager);
        viewPager.setPageTransformer(true,new ZoomOutPageTransformer());
        adapter=new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout=findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        toolbar.setTitle("Call Recorder");
        Bundle bundles=new Bundle();
        bundles.putStringArrayList("RECORDING",recordinglist);
        AllFragment allFragment=new AllFragment();
        allFragment.setArguments(bundles);
        Incomming fr=new Incomming();
        fr.setArguments(bundles);
        Outgoing outgoing=new Outgoing();
        outgoing.setArguments(bundles);
        adapter.addFrag(allFragment,"All");
        adapter.addFrag(fr,"Received");
        adapter.addFrag(outgoing,"Outgoing");
        adapter.notifyDataSetChanged();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            if(prefofsync.getBoolean("RED",true)){
                new AsyncAdapter1().execute(); //logic here
            }
        }
               //ask permission
        //navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    changeColorOfStatusAndActionBar();
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        final SharedPreferences pref=getSharedPreferences("TOGGLE",MODE_PRIVATE);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        final Switch toggleRecord=navigationView.getHeaderView(0).findViewById(R.id.switch1);
        boolean sie=pref.getBoolean("STATE",true);
        if(sie){
            toggleRecord.setChecked(true);
            toggleRecord.setText("Call Recording On");
        }else{
            toggleRecord.setChecked(false);
            toggleRecord.setText("Call Recording Off");
        }

        toggleRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                Toast.makeText(MainActivity.this, "status"+b, Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor=pref.edit();
                if(b){
                    editor.putBoolean("STATE",b);
                    editor.apply();
                    toggleRecord.setText("Call Recording On");
                }else{
                    editor.putBoolean("STATE",b);
                    editor.apply();
                    toggleRecord.setText("Call Recording Off");
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        ContactProvider.deletelistener(new ContactProvider.deleterefresh() {
            @Override
            public void deleterefreshList(boolean var) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private boolean storeToDatabase(ArrayList<Contacts> phoneContacts) {
        ContactsDatabase datbaseObj=new ContactsDatabase(this);
        for (Contacts con:phoneContacts){
           //photo uri got here
            if(datbaseObj.isContact(con.getNumber()).getNumber()!=null){
               datbaseObj.updateContact(con);
            }else{
                datbaseObj.addContact(con);
            }
        }
        return true;
    }
    private void changeColorOfStatusAndActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
             switch (viewPager.getCurrentItem()) {
                case 0:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    break;
                case 1:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.cyan));
                    window.setStatusBarColor(getResources().getColor(R.color.cyan_dark));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.cyan));
                    break;
                case 2:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.smooth_red));
                    window.setStatusBarColor(getResources().getColor(R.color.smooth_red_dark));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.smooth_red));
                    break;
                default:
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                    tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_resourse_file,menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
                searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                    queylistener.Search_name(newText+"");
                    queylistener2.Search_name2(newText+"");
                try {
                    queylistener3.Search_name3(newText+"");
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                if(!newText.isEmpty()){
                    tabLayout.setVisibility(View.GONE);
                }else{
                    tabLayout.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.setting){
            Intent intent= new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(viewPager.getCurrentItem()==0){
           super.onBackPressed();
        }else{
            viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.setting) {
            // Handle the setting action
            Intent intent= new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        } else if(id==R.id.pin_lock){
//                    Intent intent=new Intent(MainActivity.this,PinLock.class);
//                    intent.putExtra("SET",true);
//                    startActivity(intent);
            Intent intent= new Intent(MainActivity.this,NewPinLock.class);
            intent.putExtra("SET",true);
            startActivity(intent);
        } else if (id == R.id.fav) {
            //open favourite activity
            Intent intent= new Intent(MainActivity.this,Favourite.class);
            intent.putStringArrayListExtra("RECORD",recordinglist);
            startActivity(intent);
        }else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Best Call recorder app download now.https://play.google.com/store/apps/details?id=ramt57.infotrench.com.callrecorder&hl=en";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share App");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }else if (id == R.id.rate_us) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ramt57.infotrench.com.callrecorder")));
            }catch (Exception e){
                Toast.makeText(this, "Play store not found.", Toast.LENGTH_SHORT).show();
            }
        }else if(id==R.id.recording_issue){
            Intent intent= new Intent(MainActivity.this,Recording_issue.class);
            startActivity(intent);
        }else if(id==R.id.contact){
            new AsyncAdapter1().execute();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public static  void setQueylistener(querySearch quey){
        queylistener=quey;
    }
    public interface querySearch{
        public void Search_name(String name1);
    }
    public static  void setQueylistener2(querySearch2 quey1){
        queylistener2=quey1;
    }
    public interface querySearch2{
        public void Search_name2(String name1);
    }
    public static  void setQueylistener3(querySearch3 quey3){
        queylistener3=quey3;
    }
    public interface querySearch3{
        public void Search_name3(String name1);
    }
    public static  void setrefreshlistener(refreshstener quey3){
        refreshlistenerobj=quey3;
    }
    public interface refreshstener{
        public void refresh(boolean b);
    }
    private  boolean checkAndRequestPermissions() {

        List<String> listPermissionsNeeded = new ArrayList<>();
        listPermissionsNeeded.clear();
        int recordaudio=ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);//
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);//
        int call= ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);//
        int read_phonestate= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);//
        int Capture_audio_output= ContextCompat.checkSelfPermission(this, Manifest.permission.CAPTURE_AUDIO_OUTPUT);
        int process_outgoing_call= ContextCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS);//
        int modify_audio_setting= ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS);//
        int read_contacts= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);//

        if (read_contacts != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (modify_audio_setting != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
        }
        if (process_outgoing_call != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        }
        if (read_phonestate != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (call != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }
        if (recordaudio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (Capture_audio_output!=PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.CAPTURE_AUDIO_OUTPUT);
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0){

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Please Allow All Permission To Continue..", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            break;
            case PERMISSIONS_REQUEST_READ_CONTACTS:
                // Permission is granted
                if(prefofsync.getBoolean("RED",true)){
                    new AsyncAdapter1().execute();
                }
                break;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        //placed interterstial ads here
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }
    private class AsyncAdapter1 extends AsyncTask<Void,Integer,ArrayList<Contacts>> {

        @Override
        protected void onPostExecute(ArrayList<Contacts> contactses) {
            refreshlistenerobj.refresh(true);
            try {
                if(bar!=null&&bar.isShowing()){
                    bar.dismiss();
                }
            }catch (Exception e1){
                e1.printStackTrace();
            }

            if(prefofsync.getBoolean("RED",true)){
                SharedPreferences.Editor editor=prefofsync.edit();
                editor.putBoolean("RED",false);
                editor.apply();
            }
        }

        @Override
        protected ArrayList<Contacts> doInBackground(Void... voids) {
            ArrayList<Contacts> backphone=ContactProvider.getContacts(getApplicationContext());
            storeToDatabase(backphone);
            return backphone;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try{
                if(bar!=null){
                    bar.show();
                }
            }catch (Exception e2){
                e2.printStackTrace();
            }


        }
    }
}
