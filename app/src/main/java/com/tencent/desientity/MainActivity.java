package com.tencent.desientity;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(isRootAvailable()) {

            if(!isRootGiven()){
                setContentView(R.layout.provide_root);
                return;
            }


            permission();
            bitmap();
            setContentView(R.layout.activity_main);
            final String[] vers = {"Global", "Korea", "Vietnam", "Taiwan"};
            final Spinner spin = findViewById(R.id.spinner);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vers);

            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin.setAdapter(dataAdapter);



            final Button btn = findViewById(R.id.button);



            final CharSequence activate = "Activate ESP";
            final CharSequence stop = "Stop ESP";

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeSock();
                    if (btn.getText() == stop) {
                        finish();
                        btn.setText(activate);
                        btn.setBackgroundColor(Color.parseColor("#673AB7"));
                        spin.setVisibility(View.VISIBLE);
                       // hideFloatJNI();
                        Overlay.HideFloat();
                        FloatingView.HideFloat();
                    } else {

                        int version=0;
                        if((spin.getSelectedItem().toString().equals("Global")))
                            version=1;
                        else if(spin.getSelectedItem().toString().equals("Korea"))
                            version=2;
                        else if(spin.getSelectedItem().toString().equals("Vietnam") )
                            version=3;
                        else if(spin.getSelectedItem().toString().equals("Taiwan"))
                            version=4;
                            stringFromJNI(version,1);
                        btn.setText(stop);
                        btn.setBackgroundColor(Color.RED);
                        spin.setVisibility(View.INVISIBLE);

                        start(spin);
                        Overlay.ShowFloat(getApplicationContext());
                        stDm();
                        FloatingView.ShowFloat(getApplicationContext());
                    }
                }
            });


        }
        else {
            setContentView(R.layout.no_root);

        }
    }



    void start(final Spinner sp){

        String toWrite;
        if(sp.getSelectedItem().toString().equals("Global"))
            toWrite="com.tencent.ig";
        else if(sp.getSelectedItem().toString().equals("Korea"))
            toWrite="com.pubg.krmobile";
        else if(sp.getSelectedItem().toString().equals("Vietnam"))
            toWrite="com.vng.pubgmobile";
        else
            toWrite="com.rekoo.pubgm";

       //start pubg

       Shell("am start -n "+toWrite+"/com.epicgames.ue4.SplashActivity");


    }
    

    void bitmap(){
        InputStream in = getResources().openRawResource(R.raw.view);
        FileOutputStream out ;
        try {
            out = new FileOutputStream(getApplicationContext().getFilesDir()+"/bitmap.so");
            byte[] buff = new byte[1024];
            int read ;

            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Runtime.getRuntime().exec("chmod +x "+getApplicationContext().getFilesDir()+"/bitmap.so");
        } catch (IOException e) {
            System.out.println("done");
        }
        Shell("chmod +x "+getApplicationContext().getFilesDir()+"/bitmap.so");

    }

    void permission(){

        if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {   //Android M Or Over
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 69);

        }

    }


    private void Shell(String shell){
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("su -c "+shell);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataOutputStream dos = null;
        if (p != null) {
            dos = new DataOutputStream(p.getOutputStream());
        }
        try {
            assert dos != null;
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isRootAvailable(){
        for(String pathDir : Objects.requireNonNull(System.getenv("PATH")).split(":")){
            if(new File(pathDir, "su").exists()) {
                return true;
            }
        }
        return false;
    }
    public void stDm(){
       Thread tr=new Thread(new Runnable() {
           @Override
           public void run() {
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               Shell(getApplicationContext().getFilesDir()+"/bitmap.so");
           }
       });
       tr.start();
    }
    public static boolean isRootGiven(){
        if (isRootAvailable()) {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = in.readLine();
                if (output != null && output.toLowerCase().contains("uid=0"))
                    return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (process != null)
                    process.destroy();
            }
        }

        return false;
    }


    public native void stringFromJNI(int virzn,int vechon);
    public native void closeSock();

}


