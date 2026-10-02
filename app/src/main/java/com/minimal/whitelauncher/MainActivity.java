package com.minimal.whitelauncher;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {
    private GridView gridView;
    private PackageManager pm;
    private AppAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Pure white background
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        pm = getPackageManager();
        
        gridView = new GridView(this);
        gridView.setNumColumns(4); 
        gridView.setVerticalSpacing(80);
        gridView.setHorizontalSpacing(10);
        gridView.setPadding(20, 100, 20, 20);
        gridView.setClipToPadding(false);
        gridView.setVerticalScrollBarEnabled(false);
        gridView.setSelector(android.R.color.transparent);

        setContentView(gridView);
        loadApps();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadApps();
            }
        }, filter);
    }

    private void loadApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableActivities = pm.queryIntentActivities(intent, 0);
        Collections.sort(availableActivities, new ResolveInfo.DisplayNameComparator(pm));
        
        adapter = new AppAdapter(this, availableActivities);
        gridView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        // Home screen par back button se bahar na nikle
    }

    private class AppAdapter extends BaseAdapter {
        private Context context;
        private List<ResolveInfo> apps;

        public AppAdapter(Context c, List<ResolveInfo> apps) {
            this.context = c;
            this.apps = apps;
        }

        public int getCount() { return apps.size(); }
        public Object getItem(int position) { return apps.get(position); }
        public long getItemId(int position) { return position; }

        public View getView(int position, View convertView, ViewGroup parent) {
            FrameLayout frame = new FrameLayout(context);
            
            TextView textView = new TextView(context);
            final ResolveInfo info = apps.get(position);
            String label = info.loadLabel(pm).toString();
            
            textView.setText(label);
            textView.setTextColor(Color.BLACK);
            textView.setSingleLine(true);
            textView.setGravity(Gravity.CENTER);
            
            // System Font Settings se scale hoga (SP unit use kiya hai)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            
            // FrameLayout mein wrap_content taaki click margin par kaam na kare
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, 
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            textView.setLayoutParams(params);
            
            // Click sirf exact text par hi register hoga
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityInfo activity = info.activityInfo;
                    ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    i.setComponent(name);
                    
                    try {
                        // Launch karte waqt Native scale-up animation
                        int[] location = new int[2];
                        v.getLocationOnScreen(location);
                        ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 
                                0, 0, v.getWidth(), v.getHeight());
                        context.startActivity(i, options.toBundle());
                    } catch (Exception e) {}
                }
            });

            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    try {
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.setData(Uri.parse("package:" + info.activityInfo.packageName));
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                    } catch (Exception e) {}
                    return true;
                }
            });

            frame.addView(textView);
            return frame;
        }
    }
                         }
