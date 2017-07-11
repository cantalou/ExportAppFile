package com.cantalou.export;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private GridView mAppGridView;
    private ArrayList<ResolveInfo> mApps;
    private PackageManager pm;
    private MyAdapter adapter;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pm = getPackageManager();
        mAppGridView = (GridView) findViewById(R.id.appGridView);
        initApp();
        adapter = new MyAdapter(this, mApps);
        mAppGridView.setAdapter(adapter);
        mAppGridView.setOnItemClickListener(this);
    }

    /**
     * 初始化app列表
     */
    private void initApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = (ArrayList<ResolveInfo>) pm.queryIntentActivities(intent, 0);
        final Collator collator = Collator.getInstance(Locale.CHINESE);
        Collections.sort(mApps, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo a, ResolveInfo b) {
                PackageManager pm = getPackageManager();
                return collator.compare(a.loadLabel(pm).toString(), b.loadLabel(pm).toString());
            }
        });

    }

    private class MyAdapter extends ArrayAdapter<ResolveInfo> {

        public MyAdapter(Context context, ArrayList<ResolveInfo> apps) {
            super(context, 0, apps);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_gridview, null);
                holder = new ViewHolder();
                holder.appImageView = (ImageView) convertView.findViewById(R.id.appImageView);
                holder.appNameTextView = (TextView) convertView.findViewById(R.id.appNameTextView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ResolveInfo app = mApps.get(position);
            holder.appNameTextView.setText(app.loadLabel(pm));
            holder.appImageView.setImageDrawable(app.loadIcon(pm));
            return convertView;
        }

        private class ViewHolder {
            public ImageView appImageView;
            public TextView appNameTextView;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        copyAppFile(mApps.get(position));
    }

    private void copyAppFile(ResolveInfo info) {
        try {
            String packageName = info.activityInfo.packageName;
            String appDir = getPackageManager().getApplicationInfo(packageName, 0).sourceDir;
            File out = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!out.exists()) {
                out.mkdir();
            }
            IOUtils.copyLarge(new FileInputStream(appDir), new FileOutputStream(out.getAbsolutePath() + File.separator + packageName + ".apk"));
            Toast.makeText(this, "Copy to " + out.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
