package com.tencent.desientity;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageView;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;


public class Overlay extends Service
{
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    WindowManager manager;
    private View mFloatingView;


    Paint linePaint=new Paint();
    Paint rectPaint=new Paint();
    Paint textPaint=new Paint();
    Paint namePaint=new Paint();
    Paint vechilePaint=new Paint();
    Paint headEnemyPaint=new Paint();
    Paint espBoxPaint=new Paint();
    Paint distancePaint=new Paint();
    Paint backCirclePaint=new Paint();
    Paint sideDistancePaint=new Paint();
    Paint healthPaint=new Paint();
    Paint healthBoxPaint=new Paint();
    Canvas canvas;
    int height,width;
    ImageView imgv;
    String amit="DesiEsp by amits2249";

    @SuppressLint("StaticFieldLeak")
    private static Overlay Instance;

    static {
}
    @Override
    public void onCreate()
    {
        super.onCreate();
        Instance = this;
        SetFloatView();
        Display display = manager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        final int screenX = size.x;
        int screenY = size.y;
        if(screenX>screenY) {
            height = screenY;
            width = screenX;
        }
        else {
            height = screenX;
            width = screenY;
        }

        init();

        final Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas=new Canvas(mBitmap );
        imgv=mFloatingView.findViewById(R.id.imageView);
        imgv.setImageBitmap(mBitmap);
        //final TextView eText=mFloatingView.findViewById(R.id.enemy_text);
//updateOverChk();

        final Thread t = new Thread(new Runnable() {
            @Override
            public void run(){
                stringFromJNI(height,width);
                System.out.println("Sssssssstttooooooopppppppeeemmmmmmmmmmmmmmmmmmmmmmmmmmmm");
            }
        });
        t.start();

    }
    boolean isRun(){
        if(Instance == null)
            return false;
        return true;
    }
    boolean updateOverChk(String s){
        System.out.println(">"+s+"<");

        return true;

    }
    static int xem=224;
    boolean updateOver(String enemyCords){

        // System.out.println("'"+enemyCords+"'");
        int eCount=0;
        float x=0f,y=0f,fov,distance,type,health;
        String addr="",nameByte;


        if(enemyCords.length()<1){
            Hide();
            return false;
        }
        else
            clearCanvas();

        // canvas.drawRect(0,0,width,height,rectPaint);


        String[] splitEnm=enemyCords.split("/");
        // eCount=splitEnm.length;

        for (String s : splitEnm) {
            String[] splitEnmXY = s.split(",");
            try {
                type = Float.parseFloat(splitEnmXY[0]);
            } catch (Exception e) {
                continue;
            }

            try {
                x = Float.parseFloat(splitEnmXY[1]);
            } catch (Exception e) {
                continue;
            }
            try {
                y = Float.parseFloat(splitEnmXY[2]);
            } catch (Exception e) {
                continue;
            }
            try {
                distance = Float.parseFloat(splitEnmXY[3]);
            } catch (Exception e) {
                continue;
            }
            if(distance<1.0f)
                continue;

            try {
                health = Float.parseFloat(splitEnmXY[4]);
            } catch (Exception e) {
                continue;
            }
            try {
                fov = Float.parseFloat(splitEnmXY[5]);
            } catch (Exception e) {
                continue;
            }
            try {
                addr = splitEnmXY[6];
            } catch (Exception e) {

            }
            if (type == 1 || type == 2 || type==0)
                eCount++;

            if (x == 6969.69f) {
                continue;
            }
            int vDistance = Math.round(distance);


            float mx = ((width / 4) / (distance * fov));
            float my = (width / 1.38f) / (distance * fov);
            //System.out.println(mx+"  ---  "+z);
            float top = y - my + (width / 1.7f) / (distance * fov);
            if(type==4) {
                String itemName = getItemName(Integer.parseInt(addr));
                if (itemName.equals(""))
                    continue;
                canvas.drawText(itemName, x + 10f, y, distancePaint);
                canvas.drawCircle(x, y, 4f, namePaint);
            } else if(type==1 || type==2) {
                if(type==2) {
                    linePaint.setARGB(200,30, 232, 222);
                    espBoxPaint.setARGB(200,30, 232, 222);
                }else{
                    linePaint.setARGB(255,255,102,179);
                    espBoxPaint.setARGB(200,255,0,0);
                }

                String name = getName(addr);
                int healthLength = width / 40;
                if (health == 0.0f){
                    if(getConfig("Health"))
                        canvas.drawText("Knocked", x - 30, top - 5, distancePaint);
                    if(getConfig("Name"))
                        canvas.drawText(name, x - name.length() * 4, top - 28, namePaint);
                }
                else {
                    if(getConfig("Name"))
                        canvas.drawText(name, x - name.length() * 8, top - 14, namePaint);

                    if(getConfig("Health")) {
                        if (health < 25)
                            healthPaint.setColor(Color.RED);
                        else if (health < 50)
                            healthPaint.setARGB(255, 255, 165, 0);
                        else if (health < 75f)
                            healthPaint.setColor(Color.YELLOW);
                        else
                            healthPaint.setColor(Color.GREEN);


                        if (distance < 12f) {
                            canvas.drawLine(x - mx, top - 8, x + (-mx) + 2 * mx * health / 100, top - 8, healthPaint);
                            canvas.drawRect(x - mx, top - 11, x + mx, top - 5, healthBoxPaint);
                        } else {
                            canvas.drawLine(x - healthLength, top - 8, x + (-healthLength) + 2 * healthLength * health / 100, top - 8, healthPaint);
                            canvas.drawRect(x - healthLength, top - 11, x + healthLength, top - 5, healthBoxPaint);
                        }
                    }
                }
            }
            float backRadius = 100f;
            if (height < 1000)
                backRadius = 80f;
            float leftGap = 4f;

            float rightGap = 80f;
            if (height < 1000)
                rightGap = 65f;
            if (type == 3) {
                if (getConfig("Vehicle") && distance > 10.0f)
                    canvas.drawText("\uD83D\uDE97:" + vDistance + " m", x, y, vechilePaint);
            } else if (type == 0) {
                if (getConfig("Back Mark") ) {
                    //start Semi circle
                    if (y >= 120 && y <= height - 150) {
                        if (x < (float) (width / 2)) {
                            canvas.drawCircle(width, y, backRadius, backCirclePaint);
                            canvas.drawText(vDistance + " m", width - rightGap, y + 15, sideDistancePaint);
                        } else {
                            canvas.drawCircle(0, y, backRadius, backCirclePaint);
                            canvas.drawText(vDistance + " m", leftGap, y + 15, sideDistancePaint);
                        }
                    } else {

                        //up
                        if (y < 120) {
                            if (x < (float) (width / 2)) {
                                canvas.drawCircle(width, 120, backRadius, backCirclePaint);
                                canvas.drawText(vDistance + " m", width - rightGap, 120 + 15, sideDistancePaint);
                            } else {
                                canvas.drawCircle(0, 120, backRadius, backCirclePaint);
                                canvas.drawText(vDistance + " m", leftGap, 120 + 15, sideDistancePaint);
                            }
                        }
                        //down
                        else {
                            if (x < (float) (width / 2)) {
                                canvas.drawCircle(width, height - 120, backRadius, backCirclePaint);
                                canvas.drawText(vDistance + " m", width - rightGap, height - 120 + 15, sideDistancePaint);
                            } else {
                                canvas.drawCircle(0, height - 120, backRadius, backCirclePaint);
                                canvas.drawText(vDistance + " m", leftGap, height - 120 + 15, sideDistancePaint);
                            }
                        }

                    }
                }

                //end Semi circle
            } else if (getConfig("Back Mark") && (x < -width / 4 || x > width + width / 3)) {
                //start Semi circle
                if (y >= 120 && y <= height - 150) {
                    if (x > (float) (width / 2)) {
                        canvas.drawCircle(width, y, backRadius, backCirclePaint);
                        canvas.drawText(vDistance + " m", width - rightGap, y, sideDistancePaint);
                    } else {
                        canvas.drawCircle(0, y, backRadius, backCirclePaint);
                        canvas.drawText(vDistance + " m", leftGap, y, sideDistancePaint);
                    }
                } else {

                    //up
                    if (y < 120) {
                        if (x > (float) (width / 2)) {
                            canvas.drawCircle(width, 120, backRadius, backCirclePaint);
                            canvas.drawText(vDistance + " m", width - rightGap, 120, sideDistancePaint);
                        } else {
                            canvas.drawCircle(0, 120, backRadius, backCirclePaint);
                            canvas.drawText(vDistance + " m", leftGap, 120, sideDistancePaint);
                        }
                    }
                    //down
                    else {
                        if (x > (float) (width / 2)) {
                            canvas.drawCircle(width, height - 120, backRadius, backCirclePaint);
                            canvas.drawText(vDistance + " m", width - rightGap, height - 120, sideDistancePaint);
                        } else {
                            canvas.drawCircle(0, height - 120, backRadius, backCirclePaint);
                            canvas.drawText(vDistance + " m", leftGap, height - 120, sideDistancePaint);
                        }
                    }


                }

                //end Semi circle
            } else {
                float radi = (height / 7.5f) / (distance * fov);
                if (getConfig("Head Position"))
                    canvas.drawCircle(x, y, radi, headEnemyPaint);
                if (getConfig("Box"))
                    canvas.drawRect(x - mx, top, x + mx, y + my + height / 3 / (distance * fov), espBoxPaint);
                if (getConfig("Line")) {
                    if (height < 1000)
                        canvas.drawLine(width / 2, 85, x, top, linePaint);
                    else
                        canvas.drawLine(width / 2, 115, x, top, linePaint);
                }
                if (getConfig("Distance"))
                    canvas.drawText(vDistance + "  m", x - 25, y + my + ((height / 3) - 20) / (distance * fov) + 30, distancePaint);
            }

        }

        if(eCount!=0) {
            if (height < 1000) {
                canvas.drawRect(width / 2 - 30, 20 + 30, width / 2 + 30, 85, rectPaint);
                canvas.drawText("" + eCount, width / 2 - 15, 82, textPaint);
            } else {
                canvas.drawRect(width / 2 - 30, 75, width / 2 + 30, 115, rectPaint);
                canvas.drawText("" + eCount, width / 2 - 20, 112, textPaint);
            }
        }
        imgv.postInvalidate();

        return true;
    }

 /*   @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bndl=intent.getExtras();
        assert bndl != null;

        //System.out.println("mmmmmmmmmmmmmmmmmmmmmmmmmmmm"+virzn);
        isbox=Integer.parseInt(Objects.requireNonNull(bndl.get("isBox")).toString());
        isBack=Integer.parseInt(Objects.requireNonNull(bndl.get("isBack")).toString());
        isDist=Integer.parseInt(Objects.requireNonNull(bndl.get("isDist")).toString());
        ishead=Integer.parseInt(Objects.requireNonNull(bndl.get("isHead")).toString());
        isline=Integer.parseInt(Objects.requireNonNull(bndl.get("isLine")).toString());
        isName=Integer.parseInt(Objects.requireNonNull(bndl.get("isName")).toString());
        isHeath=Integer.parseInt(Objects.requireNonNull(bndl.get("isHealth")).toString());

        return super.onStartCommand(intent, flags, startId);
    }*/

    public void clearCanvas(){
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawText(amit,30,height-30,distancePaint);
    }

    public void clearCanvasNative(){
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawText(amit,30,height-30,distancePaint);
        imgv.postInvalidate();
    }

    public void lobby(){
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawText(amit,30,height-30,distancePaint);
        if(height<1000) {
            canvas.drawText("Lobby", width / 2 - 25, 82, textPaint);
        }
        else{
            canvas.drawText("Lobby", width / 2 - 30, 112, textPaint);
        }
        imgv.postInvalidate();

    }

    public static void HideFloat()
    {

        if (Instance != null)
        {
            Instance.Hide();
        }
    }


    public void Hide()
    {
        Instance = null;
      try {
          manager.removeView(mFloatingView);
      }catch (Exception e){
          System.out.println(e);
      }
        this.stopSelf();
        this.onDestroy();
    }

    void init(){

        //Paints
        textPaint.setARGB(255,255,0,0);
        if(height<1000)
            textPaint.setTextSize(30f);
        else
            textPaint.setTextSize(40f);

        //rectPaint
        rectPaint.setARGB(40,0,200,0);

        //healthBoxPaint
        healthBoxPaint.setColor(Color.BLACK);
        healthBoxPaint.setStyle(Paint.Style.STROKE);
        healthBoxPaint.setStrokeWidth(1f);

        //healthPaint
        healthPaint.setStrokeWidth(6);

        //linePaint

        linePaint.setStrokeWidth(3f);
        linePaint.setAntiAlias(true);

        //vechilePaint
        vechilePaint.setTextSize(30f);
        vechilePaint.setColor(Color.YELLOW);
        vechilePaint.setAlpha(150);

        //distancePaint
        distancePaint.setColor(Color.RED);
        if(height<1000)
            distancePaint.setTextSize(25f);
        else
            distancePaint.setTextSize(30f);

        //namePaint
        namePaint.setColor(Color.WHITE);
        if(height<1000)
            namePaint.setTextSize(25f);
        else
            namePaint.setTextSize(30f);
        namePaint.setAntiAlias(true);


        //headPaint
        headEnemyPaint.setColor(Color.GREEN);
        headEnemyPaint.setAlpha(150);

        //espboxPaint
        espBoxPaint.setStyle(Paint.Style.STROKE);
        espBoxPaint.setStrokeWidth(2f);


        //backCircle
        backCirclePaint.setColor(Color.MAGENTA);
        backCirclePaint.setColor(Color.MAGENTA);
        backCirclePaint.setAlpha(100);

        //side distance
        sideDistancePaint.setColor(Color.GREEN);
        if(height<1000)
            sideDistancePaint.setTextSize(25f);
        else
            sideDistancePaint.setTextSize(30f);
        //Paint End

    }

    public static void ShowFloat(Context context)
    {
        if (Instance == null)
        {
            Intent intent = new Intent(context, Overlay.class);
            context.startService(intent);
        }
    }



    public static void ShowFloat(Context context,int box,int line, int head,int dist,int back,int name,int health)
    {
        if (Instance == null)
        {

            Intent intent = new Intent(context, Overlay.class);

            if(box==1)
                intent.putExtra("isBox","1");
            else
                intent.putExtra("isBox","0");

            if(line==1)
                intent.putExtra("isLine","1");
            else
                intent.putExtra("isLine","0");

            if(head==1)
                intent.putExtra("isHead","1");
            else
                intent.putExtra("isHead","0");

            if(dist==1)
                intent.putExtra("isDist","1");
            else
                intent.putExtra("isDist","0");

            if(name==1)
                intent.putExtra("isName","1");
            else
                intent.putExtra("isName","0");

            if(health==1)
                intent.putExtra("isHealth","1");
            else
                intent.putExtra("isHealth","0");

            if(back==1)
                intent.putExtra("isBack","1");
            else
                intent.putExtra("isBack","0");
            context.startService(intent);
        }
    }


    private void SetFloatView()
    {

        mFloatingView = LayoutInflater.from(this).inflate(R.layout.float_view, null);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = LayoutParams.TYPE_APPLICATION_OVERLAY;

        } else {
            LAYOUT_FLAG = LayoutParams.TYPE_SYSTEM_OVERLAY;
    }




        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,0,getNavigationBarHeight(),
                LAYOUT_FLAG,
                //LayoutParams.TYPE_APPLICATION_OVERLAY,
                LayoutParams.FLAG_NOT_FOCUSABLE
                        | LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.RGBA_8888);
        /*final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                LayoutParams.TYPE_APPLICATION_OVERLAY, // TYPE_SYSTEM_ALERT is denied in apiLevel >=19
                LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
        );*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            params.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;


        params.gravity = Gravity.TOP | Gravity.START;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 0;
        manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        manager.addView(mFloatingView, params);


    }




String getName(String nametxt){
    String[] namesp=nametxt.split(":");
    char[] nameint = new char[namesp.length];
    for (int i=0; i<namesp.length;i++)
        nameint[i]=(char)Integer.parseInt(namesp[i]);
    String reetname=new String(nameint);
    return reetname;
}

    public int getNavigationBarHeight(){
        boolean hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey();
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0 && !hasMenuKey)
        {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }








    private String getItemName(int id){
        //Scopes
        if(id==203005 && getConfig("8x"))
            return "8x";

        if(id==203003 && getConfig("2x"))
            return "2x";

        if(id==203001 && getConfig("Red Dot"))
            return "Red Dot";

        if(id==203014 && getConfig("3x"))
            return "3X";

        if(id==203002 && getConfig("Hollow"))
            return "Hollow Sight";

        if(id==203015 && getConfig("6x"))
            return "6x";

        if(id==203004 && getConfig("4x"))
            return "4x";

        if(id==203018 && getConfig("Canted"))
            return "Canted Sight";


        //AR and smg
        if(id==101006  && getConfig("AUG"))
            return "AUG";

        if(id==101008 && getConfig("M762"))
            return "M762" ;

        if(id==101003 && getConfig("SCAR-L"))
            return "SCAR-L";

        if(id==101004 && getConfig("M416"))
            return "M416";

        if(id==101002 && getConfig("M16A4"))
            return "M16A-4";

        if(id==101009 && getConfig("Mk47 Mutant"))
            return "Mk47 Mutant";

        if(id==101010 && getConfig("G36C"))
            return "G36C";

        if(id==101007 && getConfig("QBZ"))
            return "QBZ";

        if(id==101001 && getConfig("AKM"))
            return "AKM";

        if(id==101005 && getConfig("Groza"))
            return "Groza";

        if(id==102005 && getConfig("Bizon"))
            return "Bizon";

        if(id==102004 && getConfig("TommyGun"))
            return "TommyGun";

        if(id==102007 && getConfig("MP5K"))
            return "MP5K";

        if(id==102002 && getConfig("UMP"))
            return "UMP";

        if(id==102003 && getConfig("Vector"))
            return "Vector";

        if(id==102001 && getConfig("Uzi"))
            return "Uzi";

        if(id==105002 && getConfig("DP28"))
            return "DP28";

        if(id==105001 && getConfig("M249"))
            return "M249";

        //snipers

        if(id==103003 && getConfig("AWM"))
            return "AWM";

        if(id==103010 && getConfig("QBU"))
            return "QBU";

        if(id==103009 && getConfig("SLR"))
            return "SLR";

        if(id==103004 && getConfig("SKS"))
            return "SKS";

        if(id==103006 && getConfig("Mini14"))
            return "Mini14";

        if(id==103002 && getConfig("M24"))
            return "M24";

        if(id==103001 && getConfig("Kar98k"))
            return "Kar98k";

        if(id==103005 && getConfig("VSS"))
            return "VSS";

        if(id==103008 && getConfig("Win94"))
            return "Win94";

        if(id==103007 && getConfig("Mk14"))
            return "Mk14";

//shotguns and hand weapons
        if(id==104003 && getConfig("S12K"))
            return "S12K";

        if(id==104004 && getConfig("DBS"))
            return "DBS";

        if(id==104001 && getConfig("S686"))
            return "S686";

        if(id==104002 && getConfig("S1897"))
            return "S1897";

        if(id==108003 && getConfig("Sickle"))
            return "Sickle";

        if(id==108001 && getConfig("Machete"))
            return "Machete";

        if(id==108002 && getConfig("Crowbar"))
            return "Crowbar";

        if(id==107001 && getConfig("CrossBow"))
            return "CrossBow";

        if(id==108004 && getConfig("Pan"))
            return "Pan";

        //pistols

        if(id==106006 && getConfig("SawedOff"))
            return "SawedOff";

        if(id==106003 && getConfig("R1895"))
            return "R1895";

        if(id==106008 && getConfig("Vz61"))
            return "Vz61";

        if(id==106001 && getConfig("P92"))
            return "P92";

        if(id==106004 && getConfig("P18C"))
            return "P18C";

        if(id==106005 && getConfig("R45"))
            return "R45";

        if(id==106002 && getConfig("P1911"))
            return "P1911";

        if(id==106010 && getConfig("Desert Eagle"))
            return "DesertEagle";




        //Ammo
        if(id==302001 && getConfig("7.62"))
            return "7.62";

        if(id==305001 && getConfig("45ACP"))
            return "45ACP";

        if(id==303001 && getConfig("5.56"))
            return "5.56";

        if(id==301001 && getConfig("9mm"))
            return "9mm";

        if(id==306001 && getConfig("300Magnum"))
            return "300Magnum";

        if(id==304001 && getConfig("12 Guage"))
            return "12 Guage";

        if(id==307001 && getConfig("Arrow"))
            return "Arrow";

        //bag helmet vest
        if((id==501006 || id==501003)  && getConfig("Bag L 3"))
            return "Bag lvl 3";

        if((id==501004 || id==501001) && getConfig("Bag L 1"))
            return "Bag lvl 1";

        if((id==501005 || id==501002) && getConfig("Bag L 2"))
            return "Bag lvl 2";

        if(id==503002 && getConfig("Vest L 2"))
            return "Vest lvl 2";


        if(id==503001 && getConfig("Vest L 1"))
            return "Vest lvl 1";


        if(id==503003 && getConfig("Vest L 3"))
            return "Vest lvl 3";


        if(id==502002 && getConfig("Helmet 2"))
            return "Helmet lvl 2";

        if(id==502001 && getConfig("Helmet 1"))
            return "Helmet lvl 1";

        if(id==502003 && getConfig("Helmet 3"))
            return "Helmet lvl 3";

        //Healthkits
        if(id==601003 && getConfig("PainKiller"))
            return "Painkiller";

        if(id==601002 && getConfig("Adrenaline"))
            return "Adrenaline";

        if(id==601001 && getConfig("Energy Drink"))
            return "Energy Drink";

        if(id==601005 && getConfig("FirstAidKit"))
            return "FirstAidKit";

        if(id==601004 && getConfig("Bandage"))
            return "Bandage";

        if(id==601006 && getConfig("Medkit"))
            return "Medkit";

        //Throwables
        if(id==602001 && getConfig("Stung"))
            return "Stung";

        if(id==602004 && getConfig("Grenade"))
            return "Grenade";

        if(id==602002 && getConfig("Smoke"))
            return "Smoke";

        if(id==602003 && getConfig("Molotov"))
            return "Molotov";


        //others
        if(id==201010 && getConfig("Flash Hider Ar"))
            return "Flash Hider Ar";

        if(id==201009 && getConfig("Ar Compensator"))
            return "Ar Compensator";

        if(id==201004 && getConfig("Flash Hider SMG"))
            return "Flash Hider SMG";

        if(id==205002 && getConfig("Tactical Stock"))
            return "Tactical Stock";

        if(id==201012 && getConfig("Duckbill"))
            return "Duckbill";

        if(id==201005 && getConfig("Flash Hider Snp"))
            return "Flash Hider Sniper";

        if(id==201006 && getConfig("Suppressor SMG"))
            return "Suppressor SMG";

        if(id==205003 && getConfig("Half Grip"))
            return "Half Grip";

        if(id==202005 && getConfig("Half Grip"))
            return "Half Grip";

        if(id==201001 && getConfig("Choke"))
            return "Choke";

        if(id==205001 && getConfig("Stock Micro UZI"))
            return "Stock Micro UZI";

        if(id==201003 && getConfig("SniperCompensator"))
            return "Sniper Compensator";

        if(id==201007 && getConfig("Sup Sniper"))
            return "Suppressor Sniper";

        if(id==201011 && getConfig("Suppressor Ar"))
            return "Suppressor Ar";


        if(id==204009 && getConfig("Ex.Qd.Sniper"))
            return "Ex.Qd.Sniper";

        if(id==204005 && getConfig("Qd.SMG"))
            return "Qd.SMG";

        if(id==204004 && getConfig("Ex.SMG"))
            return "Ex.SMG";

        if(id==204008 && getConfig("Qd.Sniper"))
            return "Qd.Sniper";

        if(id==204007 && getConfig("Ex.Sniper"))
            return "Ex.Sniper";

        if(id==204011 && getConfig("Ex.Ar"))
            return "Ex.Ar";

        if(id==204013 && getConfig("Ex.Qd.Ar"))
            return "Ex.Qd.Ar";

        if(id==204012 && getConfig("Qd.Ar"))
            return "Qd.Ar";

        if(id==204006 && getConfig("Ex.Qd.SMG"))
            return "Ex.Qd.SMG";

        if(id==205004 && getConfig("Quiver CrossBow"))
            return "Quiver CrossBow";

        if(id==204014 && getConfig("Bullet Loop"))
            return "Bullet Loop";


        if(id==202006 && getConfig("Thumb Grip"))
            return "Thumb Grip";

        if(id==202007 && getConfig("Laser Sight"))
            return "Laser Sight";

        if(id==202001 && getConfig("Angled Grip"))
            return "Angled Grip";

        if(id==202004 && getConfig("Light Grip"))
            return "Light Grip";

        if(id==202002 && getConfig("Vertical Grip"))
            return "Vertical Grip";

        if(id==603001 && getConfig("Gas Can"))
            return "Gas Can";

        if(id==201002 && getConfig("Compensator SMG"))
            return "Compensator SMG";



        //special
        if(id==106007  || id==308001 && getConfig("Flare Gun"))
            return "Flare Gun";

        if((id==403989 || id==403045 || id==403187 || id==403188)  && getConfig("Gullie Suit"))
            return "Gullie Suit";

        return "";
        //return String.valueOf(id);

    }

    boolean getConfig(String key){
        SharedPreferences sp=this.getSharedPreferences("espValue",Context.MODE_PRIVATE);
        return  sp.getBoolean(key,false);
       // return !key.equals("");
    }

    public native void stringFromJNI(int height,int width);


}

