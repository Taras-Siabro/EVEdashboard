package com.couriertracker;
import android.Manifest;import android.app.*;import android.content.*;import android.content.pm.PackageManager;import android.os.*;import android.webkit.*;
public class MainActivity extends Activity{
 WebView web; static final int REQ=42;
 public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xff111318);web=new WebView(this);web.setBackgroundColor(0xff0d0f13);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);web.setWebViewClient(new WebViewClient());web.addJavascriptInterface(new Bridge(this),"Android");setContentView(web);web.loadUrl("file:///android_asset/index.html");if(Build.VERSION.SDK_INT>=33)requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.POST_NOTIFICATIONS},REQ);else requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQ);}
 void startTracking(){if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)return;Intent i=new Intent(this,TrackingService.class).setAction("START");if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);}
 void act(String a){startService(new Intent(this,TrackingService.class).setAction(a));}
 String state(){return getSharedPreferences("track",0).getString("state","{}");}
 class Bridge{Context c;Bridge(Context c){this.c=c;}@JavascriptInterface public void start(){startTracking();}@JavascriptInterface public void pause(){act("PAUSE");}@JavascriptInterface public void resume(){act("RESUME");}@JavascriptInterface public void stop(){act("STOP");}@JavascriptInterface public String state(){return getSharedPreferences("track",0).getString("state","{}");}}
}
