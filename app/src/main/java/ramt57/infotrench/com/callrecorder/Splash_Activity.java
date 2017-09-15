package ramt57.infotrench.com.callrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ramt57.infotrench.com.callrecorder.SqliteDatabase.ContactsDatabase;
import ramt57.infotrench.com.callrecorder.pojo_classes.Contacts;

public class Splash_Activity extends Activity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 2001;
    ArrayList<Contacts> phoneContacts=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences SP1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean b1=SP1.getBoolean("LOCK",false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
            checkAndRequestPermissions();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            checkAndRequestPermissions();
        } else {
            if(b1){
                Intent intent=new Intent(getApplicationContext(),PinLock.class);
                startActivity(intent);
                finish();
            }else{
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0&&grantResults[0]==PackageManager.PERMISSION_GRANTED
                       ){
//                    phoneContacts= ContactProvider.getContacts(getApplicationContext());//ask permission here
//                    storeToDatabase(phoneContacts);
                    SharedPreferences SP1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    boolean b1=SP1.getBoolean("LOCK",false);
                    if(b1){
                        Intent intent=new Intent(getApplicationContext(),PinLock.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Please Allow All Permission To Continue..", Toast.LENGTH_SHORT).show();
//                    finish();
                }
            }
            break;
        }
    }
    private void storeToDatabase(ArrayList<Contacts> phoneContacts) {
        ContactsDatabase datbaseObj=new ContactsDatabase(this);
        for (Contacts con:phoneContacts){
            //photo uri got here
            if(datbaseObj.isContact(con.getNumber()).getNumber()!=null){
                datbaseObj.updateContact(con);
            }else{
                datbaseObj.addContact(con);
            }
        }
    }

    private  boolean checkAndRequestPermissions() {

        List<String> listPermissionsNeeded = new ArrayList<>();
        listPermissionsNeeded.clear();
        int recordaudio= ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);//
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);//
        int call= ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);//
        int read_phonestate= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);//
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
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
}
