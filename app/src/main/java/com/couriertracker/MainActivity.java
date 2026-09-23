package com.couriertracker;
import android.Manifest;import android.app.*;import android.content.*;import android.content.pm.PackageManager;import android.location.LocationManager;import android.net.Uri;import android.os.*;import android.provider.Settings;import android.view.*;import android.webkit.*;
public class MainActivity extends Activity{
 WebView web; static final int REQ=42;
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xff111318);getWindow().setNavigationBarColor(0xff0d0f13);
  web=new WebView(this);web.setBackgroundColor(0xff0d0f13);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setCacheMode(WebSettings.LOAD_DEFAULT);web.setWebViewClient(new WebViewClient());web.addJavascriptInterface(new Bridge(),"Android");setContentView(web);
  web.setOnApplyWindowInsetsListener((v,in)->{int top,bottom;if(Build.VERSION.SDK_INT>=30){android.graphics.Insets x=in.getInsets(WindowInsets.Type.systemBars());top=x.top;bottom=x.bottom;}else{top=in.getSystemWindowInsetTop();bottom=in.getSystemWindowInsetBottom();}v.setPadding(0,top,0,bottom);return in;});web.loadUrl("file:///android_asset/index.html");
  web.postDelayed(()->ensureSetup(),700);
 }
 boolean fine(){return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED;}
 boolean background(){return Build.VERSION.SDK_INT<29||checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)==PackageManager.PERMISSION_GRANTED;}
 boolean notif(){return Build.VERSION.SDK_INT<33||checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED;}
 boolean gps(){LocationManager m=(LocationManager)getSystemService(LOCATION_SERVICE);return m.isProviderEnabled(LocationManager.GPS_PROVIDER);}
 boolean battery(){if(Build.VERSION.SDK_INT<23)return true;return ((PowerManager)getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName());}
 void ensureSetup(){if(!fine()){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQ);return;}if(Build.VERSION.SDK_INT>=33&&!notif()){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQ);return;}if(Build.VERSION.SDK_INT>=30&&!background()){openLocationSettings();return;}if(!gps()){openGpsSettings();return;}if(!battery()){requestBattery();return;}notifyWeb();}
 void openLocationSettings(){try{startActivity(new Intent(Settings.ACTION_MANAGE_APP_LOCATION_SETTINGS,Uri.parse("package:"+getPackageName())));}catch(Exception e){startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));}}
 void openGpsSettings(){startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));}
 void requestBattery(){try{startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,Uri.parse("package:"+getPackageName())));}catch(Exception e){startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));}}
 void notifyWeb(){if(web!=null)web.evaluateJavascript("window.refreshSetup&&refreshSetup();",null);}
 @Override public void onResume(){super.onResume();if(web!=null)web.postDelayed(()->{notifyWeb();ensureSetup();},250);}
 void startTracking(){if(!fine()){ensureSetup();return;}Intent i=new Intent(this,TrackingService.class).setAction("START");if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);}
 void act(String a){startService(new Intent(this,TrackingService.class).setAction(a));}
 String state(){return getSharedPreferences("track",0).getString("state","{}");}
 class Bridge{
  @JavascriptInterface public void start(){startTracking();}@JavascriptInterface public void pause(){act("PAUSE");}@JavascriptInterface public void resume(){act("RESUME");}@JavascriptInterface public void stop(){act("STOP");}@JavascriptInterface public String state(){return MainActivity.this.state();}
  @JavascriptInterface public boolean locationOk(){return fine();}@JavascriptInterface public boolean backgroundOk(){return background();}@JavascriptInterface public boolean gpsOk(){return gps();}@JavascriptInterface public boolean batteryOk(){return battery();}@JavascriptInterface public boolean notificationsOk(){return notif();}
  @JavascriptInterface public void fixLocation(){openLocationSettings();}@JavascriptInterface public void fixGps(){openGpsSettings();}@JavascriptInterface public void fixBattery(){requestBattery();}
 }
}