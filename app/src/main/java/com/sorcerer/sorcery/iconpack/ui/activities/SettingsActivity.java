package com.sorcerer.sorcery.iconpack.ui.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sorcerer.sorcery.iconpack.R;
import com.sorcerer.sorcery.iconpack.models.CheckSettingsItem;
import com.sorcerer.sorcery.iconpack.models.SettingsItem;
import com.sorcerer.sorcery.iconpack.ui.activities.base.BaseActivity;
import com.sorcerer.sorcery.iconpack.ui.adapters.recyclerviewAdapter.SettingsAdapter;
import com.sorcerer.sorcery.iconpack.util.PermissionsHelper;
import com.sorcerer.sorcery.iconpack.xposed.XposedUtils;
import com.sorcerer.sorcery.iconpack.xposed.theme.IconReplacementItem;
import com.sorcerer.sorcery.iconpack.xposed.theme.Util;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends BaseActivity {

    private static final String SHARED_PREFERENCE_NAME = "SIP_XPOSED";
    private RecyclerView mRecyclerView;
    private SharedPreferences mPrefs;
    private static final String TAG = "SIP/Settings";
    private Context mContext;
    private CheckSettingsItem global;

    @Override
    protected int provideLayoutId() {
        return R.layout.activity_settings;
    }

    @Override
    protected void init() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_universal);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mContext = this;
        mPrefs = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_WORLD_READABLE);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_settings);

        List<SettingsItem> list = new ArrayList<>();
        global = new CheckSettingsItem(
                getString(R.string.pref_global_load_title),
                getString(R.string.pref_global_load_summary),
                mPrefs.getBoolean(getString(R.string.pref_global_load_key), false));
        global.setOnCheckListener(new CheckSettingsItem.OnCheckListener() {
            @Override
            public void onChecked() {
                Toast.makeText(SettingsActivity.this,
                        getString(R.string.pref_global_hint),
                        Toast.LENGTH_SHORT).show();
                mPrefs.edit().putBoolean(getString(R.string.pref_global_load_key), true).commit();
                boolean ok = tryAndApplyIcon(getApplication().getApplicationInfo());
                global.setChecked(ok);
            }

            @Override
            public void onUnchecked() {
                mPrefs.edit().putBoolean(getString(R.string.pref_global_load_key), false).commit();
            }
        });
        list.add(global);

        SettingsAdapter adapter = new SettingsAdapter(this, list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager
                .VERTICAL, false));

    }

    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            PermissionsHelper.requestWriteExternalStorage(this);
        }
    }

    private boolean appIsInstalledInMountASEC() {
        return getApplicationInfo().sourceDir.contains("asec/");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return false;
    }

    private boolean tryAndApplyIcon(final ApplicationInfo themePackage) {
        if (!RootTools.isAccessGiven()) {
            try {
                Toast.makeText(this, "acquiring root...", Toast.LENGTH_SHORT).show();
                RootTools.getShell(true).add(new CommandCapture(0, "echo Hello"));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        if (!RootTools.isAccessGiven()) {
            Toast.makeText(this, "need root access!", Toast.LENGTH_SHORT).show();
            return false;
        }
//        if (this.mInstallStep < 2 && appIsInstalledInMountASEC()) {
//            this.mInstallStep = 2;
//        }
//        if (!(this.mInstallStep >= 3 || appIsInstalledInMountASEC() || checkForXposedInstaller())) {
//            this.mInstallStep = 3;
//        }
//        if (this.mInstallStep < 4 && checkForXposedInstaller() && !Utils
// .checkIfModuleIsActivated(getPackageName())) {
//            this.mInstallStep = 4;
//        }
//        if (!appIsInstalledInMountASEC() && checkForXposedInstaller() && Utils.checkIfModuleIsActivated(getPackageName())) {
//            this.mInstallStep = 5;
//        }
        if (RootTools.isAccessGiven()) {
//            if (this.mInstallStepBeforePause != this.mInstallStep) {
//                switch (this.mInstallStep) {
//                    case TestHandler.ACTION_HIDE /*2*/:
//                        PromptMoveAppDialog.newInstance().show(getFragmentManager(), "dialog");
//                        break;
//                    case TestHandler.ACTION_DISPLAY /*3*/:
//                        PromptInstallXposedDialog.newInstance().show(getFragmentManager(), "dialog");
//                        break;
//                    case TestHandler.ACTION_PDISPLAY /*4*/:
//                        PromptActivateModuleDialog.newInstance(true).show(getFragmentManager(), "dialog");
//                        break;
//                    case 5:
//                        try {
//                            Utils.copyAsset(this, "XposedBridge.jar", getExternalCacheDir().getAbsolutePath() + "/XposedBridge.jar.newversion");
//                            RootTools.getShell(true).add(new CommandCapture(0, "cat " +
//                                    getExternalCacheDir().getAbsolutePath() + "/XposedBridge.jar.newversion > /data/xposed/XposedBridge.jar.newversion", "chmod 655 /data/xposed/XposedBridge.jar.newversion", "cat " + getExternalCacheDir().getAbsolutePath() + "/XposedBridge.jar.newversion > /data/xposed/XposedBridge.jar", "chmod 655 /data/xposed/XposedBridge.jar"));
//                            break;
//                        } catch (Exception e2) {
//                            e2.printStackTrace();
//                            break;
//                        }
//                }
//            }

//            try {
//                Utils.copyAsset(this,
//                        "XposedBridge.jar",
//                        getExternalCacheDir().getAbsolutePath() + "/XposedBridge.jar.newversion");
//                RootTools.getShell(true).add(new CommandCapture(0,
//                        "cat " +
//                                getExternalCacheDir().getAbsolutePath() +
//                                "/XposedBridge.jar.newversion > /data/xposed/XposedBridge.jar.newversion",
//                        "chmod 655 /data/xposed/XposedBridge.jar.newversion",
//                        "cat " + getExternalCacheDir().getAbsolutePath() +
//                                "/XposedBridge.jar.newversion > /data/xposed/XposedBridge.jar",
//                        "chmod 655 /data/xposed/XposedBridge.jar"));
//            } catch (Exception e2) {
//                e2.printStackTrace();
//            }
            if (!new File(getCacheDir().getAbsolutePath() + "/icons").exists()) {
                try {
                    RootTools.getShell(true).add(new CommandCapture(0,
                            "mkdir " + getCacheDir().getAbsolutePath() + "/icons",
                            "chmod 777 " + getCacheDir().getAbsolutePath() + "/icons"));
                } catch
                        (Exception e22) {
                    e22.printStackTrace();
                }
            }
            if (!appIsInstalledInMountASEC()) {
                try {
                    Runtime.getRuntime()
                            .exec("rm " + getExternalCacheDir().getAbsolutePath() + "/tmp.apk");
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }

            apply(themePackage);
            return true;
        }
        return false;
    }

    private void apply(final ApplicationInfo themePackage) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    XmlPullParser xrp;
                    XmlPullParser xrp2;
                    ArrayList<IconReplacementItem> items;
                    Resources origPkgRes;
                    SharedPreferences.Editor editor = mPrefs.edit();
                    Gson gson = new Gson();
                    ArrayList<String> mIconPackages = new ArrayList();
                    HashMap<String, ArrayList<IconReplacementItem>> mIconReplacementsHashMap =
                            new HashMap();
                    String themePackagePath = themePackage.sourceDir;
                    if (themePackage.sourceDir.contains("/data/app/")) {
                        Command tmp = RootTools.getShell(true).add(new CommandCapture(0,
                                "rm /data/data/" + getPackageName() + "/cache/icons/*",
                                "rm " + getExternalCacheDir().getAbsolutePath()
                                        + "/current_theme.apk"));
                        Log.d(TAG, "contains");
//                        Log.d(TAG, String.valueOf(tmp.isExecuting()));
//                        synchronized (tmp) {
//                            while (!tmp.isFinished()) {
//                                tmp.wait();
//                            }
//                        }
                    } else {
                        Log.d(TAG,
                                "Original Theme APK is at " + themePackage.sourceDir);
                        Command commandCapture = new CommandCapture(0,
                                "rm /data/data/" + getPackageName() + "/cache/icons/*",
                                "rm " + getExternalCacheDir().getAbsolutePath()
                                        + "/current_theme.apk",
                                "cat \"" + themePackage.sourceDir + "\" > " + getExternalCacheDir()
                                        .getAbsolutePath() + "/current_theme.apk",
                                "chmod 644 " + getExternalCacheDir().getAbsolutePath()
                                        + "/current_theme.apk");
                        Command tmp = RootTools.getShell(true).add(commandCapture);

//                        synchronized (tmp) {
//                            while (!tmp.isFinished()) {
//                                tmp.wait();
//                            }
//                        }
                        Log.d(TAG, "not contains");
//                        Log.d(TAG, String.valueOf(tmp.isExecuting()));

                        themePackagePath = getExternalCacheDir() + "/current_theme.apk";
                        Log.d(TAG, "Copied Theme APK is at " + themePackagePath);
                    }
                    PackageManager pm = getPackageManager();
                    Resources r = getPackageManager()
                            .getResourcesForApplication(themePackage.packageName);
                    if (r.getIdentifier("appfilter", "xml", themePackage.packageName) == 0) {
                        InputStream istr = r.getAssets().open("appfilter.xml");
                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        factory.setNamespaceAware(true);
                        xrp = factory.newPullParser();
                        xrp.setInput(istr, "UTF-8");
                        InputStream istr2 = r.getAssets().open("appfilter.xml");
                        XmlPullParserFactory factory2 = XmlPullParserFactory.newInstance();
                        factory2.setNamespaceAware(true);
                        xrp2 = factory2.newPullParser();
                        xrp2.setInput(istr2, "UTF-8");
                    } else {
                        xrp = r.getXml(r
                                .getIdentifier("appfilter", "xml", themePackage.packageName));
                        xrp2 = r.getXml(r
                                .getIdentifier("appfilter", "xml", themePackage.packageName));
                    }
//                    editor.putString("theme_icon_mask", null);
                    for (Map.Entry<String, ?> entry : mPrefs.getAll()
                            .entrySet()) {
                        if (((String) entry.getKey()).contains("theme_icon_for_")) {
                            editor.remove((String) entry.getKey());
                        }
                    }
                    editor.commit();
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay()
                            .getMetrics(metrics);
                    if (metrics.densityDpi == 213) {
                        metrics.densityDpi = 320;
                    }
                    editor.putInt("display_dpi", metrics.densityDpi);
                    Iterator i$ = Util.ParseIconReplacements(themePackage.packageName, r, xrp)
                            .iterator();
                    while (i$.hasNext()) {
                        IconReplacementItem item = (IconReplacementItem) i$.next();
                        try {
                            ActivityInfo activityInfo = pm.getActivityInfo(new ComponentName(
                                    item.getPackageName(),
                                    item.getActivityName()), 128);
                            Log.d(TAG, "activity: " + item.getActivityName());
                            Log.d(TAG, "orig res name: " + item.getOrigResName());
                            Log.d(TAG, "component: " + item.getComponent());
                            Log.d(TAG, "orig res: " + item.getOrigRes());
                            Log.d(TAG, "replacement res: " + item.getReplacementRes());
                            Log.d(TAG, "replacement res name: " + item.getReplacementResName());
                            Log.d(TAG, "package: " + item.getPackageName());
                            if (activityInfo != null) {
                                if (mIconReplacementsHashMap.get(item.getPackageName()) == null) {
                                    mIconReplacementsHashMap
                                            .put(item.getPackageName(), new ArrayList());
                                }
                                items = (ArrayList) mIconReplacementsHashMap
                                        .get(item.getPackageName());
                                origPkgRes =
                                        pm.getResourcesForApplication(item.getPackageName());
                                if (activityInfo.getIconResource() != 0) {
                                    try {
                                        item.setPackageName(origPkgRes
                                                .getResourcePackageName(activityInfo
                                                        .getIconResource()));
                                    } catch (Exception e) {
                                    }
                                }
                                item.setOrigRes(activityInfo.getIconResource());

                                if (!items.contains(item)) {
                                    items.add(item);

                                    XposedUtils.cacheDrawable(item.getPackageName(),
                                            item.getOrigRes(),
                                            (BitmapDrawable) new BitmapDrawable(origPkgRes,
                                                    XposedUtils.getBitmapForDensity(r,
                                                            metrics.densityDpi,
                                                            item.getReplacementRes())));
                                }
                            }
                        } catch (Exception e2) {
                        }
                    }
                    editor.putString("theme_package_name", themePackage.packageName);
                    editor.putString("theme_package_path", themePackagePath);
                    for (Map.Entry<String, ArrayList<IconReplacementItem>> entry2 : mIconReplacementsHashMap
                            .entrySet()) {
                        mIconPackages.add(entry2.getKey());
                        editor.putString("theme_icon_for_" + ((String) entry2.getKey()),
                                gson.toJson(((ArrayList) entry2.getValue()).toArray()));
                    }
                    editor.putString("theme_icon_packages",
                            gson.toJson(mIconPackages.toArray()));
                    editor.commit();
                } catch (Exception e6) {
                    e6.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(mContext, "Failed to apply icons!", Toast.LENGTH_SHORT)
                                    .show();
//                            tryAndApplyIcon(null);
                        }
                    });
                }
            }
        }).start();
    }
}

