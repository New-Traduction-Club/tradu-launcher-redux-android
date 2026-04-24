package org.renpy.android;

import org.libsdl.app.SDLActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;

// runtime bridge for renpy 7.4 through 7.8.x (family 2, monolithic librenpython.so)
public class PythonSDLActivity extends SDLActivity {

    // static reference for python code access
    public static PythonSDLActivity mActivity = null;

    // layout containing the sdl view, used by videoplayer to overlay its own view
    public FrameLayout mFrameLayout;

    // root vertical layout wrapping mFrameLayout
    public LinearLayout mVbox;

    // store interface for iap, set externally if available
    public StoreInterface mStore = null;

    ResourceManager resourceManager;

    protected String[] getLibraries() {
        return new String[] {
            "renpython",
        };
    }

    // intent extra keys for launcher communication
    public static final String EXTRA_GAME_PATH = "game_path";
    public static final String EXTRA_ORIENTATION = "orientation";

    public void addView(View view, int index) {
        mVbox.addView(view, index, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, (float) 0.0));
    }

    public void removeView(View view) {
        mVbox.removeView(view);
    }

    @Override
    public void setContentView(View view) {
        mFrameLayout = new FrameLayout(this);
        mFrameLayout.addView(view);

        mVbox = new LinearLayout(this);
        mVbox.setOrientation(LinearLayout.VERTICAL);
        mVbox.addView(mFrameLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, (float) 1.0));

        super.setContentView(mVbox);
    }

    // override to make sdl respect the orientation from the intent or manifest
    @Override
    public void setOrientationBis(int w, int h, boolean resizable, String hint) {
        return;
    }

    public void recursiveDelete(File f) {
        if (f.isDirectory()) {
            for (File r : f.listFiles()) {
                recursiveDelete(r);
            }
        }
        f.delete();
    }

    // unpacks a tar.gz asset disguised as .mp3 into the target directory
    public void unpackData(final String resource, File target) {
        boolean shouldUnpack = false;

        String data_version = resourceManager.getString(resource + "_version");
        String disk_version = null;

        String filesDir = target.getAbsolutePath();
        String disk_version_fn = filesDir + "/" + resource + ".version";

        if (data_version != null) {
            File versionFile = new File(disk_version_fn);
            if (versionFile.exists()) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(versionFile));
                    disk_version = br.readLine();
                    br.close();
                } catch (Exception e) {
                    disk_version = "";
                }
            } else {
                disk_version = "";
            }

            if (!data_version.equals(disk_version)) {
                shouldUnpack = true;
            }
        }

        if (shouldUnpack) {
            Log.v("python", "Extracting " + resource + " assets.");

            new File(target, "main.pyo").delete();

            recursiveDelete(new File(target, "lib"));
            recursiveDelete(new File(target, "renpy"));

            target.mkdirs();

            AssetExtract ae = new AssetExtract(this);
            if (!ae.extractTar(resource + ".mp3", target.getAbsolutePath())) {
                toastError("Could not extract " + resource + " data.");
            }

            try {
                new File(target, ".nomedia").createNewFile();

                FileOutputStream os = new FileOutputStream(disk_version_fn);
                os.write(data_version.getBytes());
                os.close();
            } catch (Exception e) {
                Log.w("python", e);
            }
        }
    }

    public void toastError(final String msg) {
        final Activity thisActivity = this;

        runOnUiThread(new Runnable () {
            public void run() {
                Toast.makeText(thisActivity, msg, Toast.LENGTH_LONG).show();
            }
        });

        synchronized (this) {
            try {
                this.wait(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public native void nativeSetEnv(String variable, String value);

    public void preparePython() {
        long startTime = System.currentTimeMillis();
        Log.v("python", "Starting preparePython. Time: " + startTime);

        mActivity = this;

        resourceManager = new ResourceManager(this);

        File oldExternalStorage = new File(Environment.getExternalStorageDirectory(), getPackageName());
        File externalStorage = getExternalFilesDir(null);

        if (externalStorage == null) {
            externalStorage = oldExternalStorage;
        }

        unpackData("private", getFilesDir());

        nativeSetEnv("ANDROID_PRIVATE", getFilesDir().getAbsolutePath());
        nativeSetEnv("ANDROID_PUBLIC",  externalStorage.getAbsolutePath());
        nativeSetEnv("ANDROID_OLD_PUBLIC", oldExternalStorage.getAbsolutePath());

        String apkFilePath;
        ApplicationInfo appInfo;
        PackageManager packMgmr = getApplication().getPackageManager();

        try {
            appInfo = packMgmr.getApplicationInfo(getPackageName(), 0);
            apkFilePath = appInfo.sourceDir;
        } catch (NameNotFoundException e) {
            apkFilePath = "";
        }

        nativeSetEnv("ANDROID_APK", apkFilePath);

        // if a custom game path was provided via intent, override the default
        String gamePath = getIntent().getStringExtra(EXTRA_GAME_PATH);
        if (gamePath != null && !gamePath.isEmpty()) {
            nativeSetEnv("ANDROID_PRIVATE", gamePath);
        }

        Log.v("python", "Finished preparePython. Duration: " + (System.currentTimeMillis() - startTime) + "ms");
    };

    // presplash support
    public ImageView mPresplash = null;
    ProgressBar mProgressBar = null;

    Bitmap getBitmap(String assetName) {
        try {
            InputStream is = getAssets().open(assetName);
            Bitmap rv = BitmapFactory.decodeStream(is);
            is.close();
            return rv;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("python", "PythonSDLActivity onCreate()");

        // apply orientation from intent extra or default to sensor landscape
        int orientation = getIntent().getIntExtra(EXTRA_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setRequestedOrientation(orientation);

        super.onCreate(savedInstanceState);

        if (mLayout == null) {
            return;
        }

        Bitmap presplashBitmap = getBitmap("android-presplash.png");
        if (presplashBitmap == null) {
            presplashBitmap = getBitmap("android-presplash.jpg");
        }

        if (presplashBitmap != null) {
            mPresplash = new ImageView(this);
            mPresplash.setBackgroundColor(presplashBitmap.getPixel(0, 0));
            mPresplash.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mPresplash.setImageBitmap(presplashBitmap);
            mLayout.addView(mPresplash, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        }
    }

    // called by renpy to hide the presplash after start
    public void hidePresplash() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mActivity.mPresplash != null) {
                    mActivity.mLayout.removeView(mActivity.mPresplash);
                    mActivity.mPresplash = null;
                }
                if (mActivity.mProgressBar != null) {
                    mActivity.mLayout.removeView(mActivity.mProgressBar);
                    mActivity.mProgressBar = null;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.v("python", "onDestroy()");
        super.onDestroy();

        if (mStore != null) {
            mStore.destroy();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.v("python", "onNewIntent()");
        setIntent(intent);
    }

    public boolean mStopDone = true;

    @Override
    public void onStop() {
        Log.v("python", "onStop() start.");
        super.onStop();

        long startTime = System.currentTimeMillis();

        synchronized (this) {
            while (true) {
                if (mStopDone) {
                    break;
                }
                if (startTime + 8000 < System.currentTimeMillis()) {
                    break;
                }
                try {
                    this.wait(100);
                } catch (InterruptedException e) { /* pass */ }
            }
        }

        Log.v("python", "onStop() done.");
    }

    public void armOnStop () {
        Log.v("python", "armOnStop()");
        mStopDone = false;
    }

    public void finishOnStop() {
        Log.v("python", "finishOnStop()");

        synchronized (this) {
            mStopDone = true;
            this.notifyAll();
        }
    }

    // public api for python code
    public void openUrl(String url) {
        openURL(url);
    }

    public void vibrate(double s) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot((int) (1000 * s), VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate((int) (1000 * s));
            }
        }
    }

    public int getDPI() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.densityDpi;
    }

    public PowerManager.WakeLock wakeLock = null;

    public void setWakeLock(boolean active) {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Screen On");
            wakeLock.setReferenceCounted(false);
        }

        if (active) {
            wakeLock.acquire();
        } else {
            wakeLock.release();
        }
    }

    // activity result support for python jni calls
    public int mActivityResultRequestCode = -1;
    public int mActivityResultResultCode = -1;
    public Intent mActivityResultResultData = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (mStore != null && mStore.onActivityResult(requestCode, resultCode, resultData)) {
            return;
        }

        mActivityResultRequestCode = requestCode;
        mActivityResultResultCode = resultCode;
        mActivityResultResultData = resultData;

        super.onActivityResult(requestCode, resultCode, resultData);
    }
}
