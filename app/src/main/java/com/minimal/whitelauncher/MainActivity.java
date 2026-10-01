package com.minimal.whitelauncher;
import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
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
        
        // Background mein naye apps install/delete hone par screen update karne ka logic
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
            paint.setColor(Color.WHITE); // White text
            paint.setTextAlign(Paint.Align.CENTER);
            setBackgroundColor(Color.BLACK); // Pitch black background
            loadApps();
        }

        public void loadApps() {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            apps = pm.queryIntentActivities(intent, 0);
            Collections.sort(apps, new ResolveInfo.DisplayNameComparator(pm));
            invalidate(); // Screen ko turant redraw karega
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (apps == null || apps.isEmpty()) return;

            int total = apps.size();
            // Total apps ke hisaab se row/column divide karna
            cols = (int) Math.ceil(Math.sqrt(total));
            rows = (int) Math.ceil((double) total / cols);

            cellWidth = (float) getWidth() / cols;
            cellHeight = (float) getHeight() / rows;

            // Apps badhne par automatically font size chota hoga
            float fontSize = Math.min(cellWidth / 5, cellHeight / 3);
            paint.setTextSize(fontSize);

            int index = 0;
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    if (index >= total) break;
                    String name = apps.get(index).loadLabel(pm).toString();
                    
                    // Naam bahut lamba ho toh cut kar do taaki overlap na ho
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
                // X aur Y touch coordinates se exact app detect karna
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
        for (ResolveInfo ri : apps) names.add(ri.loadLabel(pm).toString());

        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(pm.getLaunchIntentForPackage(apps.get(position).activityInfo.packageName));
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.setData(Uri.parse("package:" + apps.get(position).activityInfo.packageName));
                startActivity(i);
                return true;
            }
        });
    }
    @Override
    public void onBackPressed() {
        // Back button disable taaki launcher close na ho
    }
}
