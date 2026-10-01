package com.minimal.whitelauncher;

import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;i
import android.os.Bundle;
import android.view.*;
import java.util.*;

public class MainActivity extends Activity {
    private AppDrawerView drawerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawerView = new AppDrawerView(this);
        setContentView(drawerView);
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                drawerView.loadApps();
            }
        }, filter);
    }

    @Override
    public void onBackPressed() {
        // Home screen par back button kuch na kare
    }

    class AppDrawerView extends View {
        private List<ResolveInfo> apps;
        private PackageManager pm;
        private Paint paint;
        private int cols, rows;
        private float cellWidth, cellHeight;

        public AppDrawerView(Context context) {
            super(context);
            pm = context.getPackageManager();
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE); 
            paint.setTextAlign(Paint.Align.CENTER);
            setBackgroundColor(Color.BLACK); 
            loadApps();
        }

        public void loadApps() {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            apps = pm.queryIntentActivities(intent, 0);
            Collections.sort(apps, new ResolveInfo.DisplayNameComparator(pm));
            invalidate(); 
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (apps == null || apps.isEmpty()) return;

            int total = apps.size();
            cols = (int) Math.ceil(Math.sqrt(total));
            rows = (int) Math.ceil((double) total / cols);

            cellWidth = (float) getWidth() / cols;
            cellHeight = (float) getHeight() / rows;

            float fontSize = Math.min(cellWidth / 5, cellHeight / 3);
            paint.setTextSize(fontSize);

            int index = 0;
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    if (index >= total) break;
                    String name = apps.get(index).loadLabel(pm).toString();
                    
                    if(name.length() > 9) name = name.substring(0, 7) + "..";

                    float textX = (x * cellWidth) + (cellWidth / 2);
                    float textY = (y * cellHeight) + (cellHeight / 2) + (fontSize / 3);
                    
                    canvas.drawText(name, textX, textY, paint);
                    index++;
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int col = (int) (event.getX() / cellWidth);
                int row = (int) (event.getY() / cellHeight);
                int index = (row * cols) + col;

                if (index < apps.size()) {
                    ActivityInfo activity = apps.get(index).activityInfo;
                    ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    i.setComponent(name);
                    try {
                        getContext().startActivity(i);
                    } catch (Exception e) {}
                }
                return true;
            }
            return super.onTouchEvent(event);
        }
    }
}
