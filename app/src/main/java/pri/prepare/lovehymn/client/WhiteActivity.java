package pri.prepare.lovehymn.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.ActivityWhiteBinding;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

/**
 * 白版或帮助文档
 */
public class WhiteActivity extends AppCompatActivity {
    private ActivityWhiteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_white);
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        int page = intent.getIntExtra("page", 0);
        PDFView pdfView = binding.pdfv2;
        pdfView.fromFile(new File(path)).defaultPage(page).load();
        setPdfViewZoom();
        binding.lockBtn.setAnimation(lockBtnHideAnimation());
        lockBtnSet();
        new Thread(r).start();
        new Thread(timerR).start();
        MainActivity.timeTool.Resume();
        if (!Setting.getValueB(Setting.STATUS_BAR_SHOW)) {
            hideStatusBar(this);
        } else {
            Tool.showStatusBar(getWindow(), this);
        }
    }

    private Animation lockBtnHideAnimation() {
        Animation res = AnimationUtils.loadAnimation(this, R.anim.alpha_out_slow);
        res.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.lockBtn.setAlpha(1f);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.lockBtn.setAlpha(0f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return res;
    }

    private boolean isLandscape() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        return rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270;
    }

    private void setPdfViewZoom() {
        PDFView pdfView = binding.pdfv2;
        if (isLandscape()) {
            Tool.hideStatusBar(this);
            pdfView.setMinZoom(0.6f);
        } else {
            pdfView.setMinZoom(1f);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        setPdfViewZoom();
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 竖屏时隐藏状态栏
     */
    public void hideStatusBar(Activity activity) {
        if (activity == null) return;
        Window window = activity.getWindow();
        if (window == null) return;
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        WindowManager.LayoutParams lp = window.getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        window.setAttributes(lp);
    }

    /**
     * 目前（211214）仅用于获取按钮的坐标，用于坐标判断
     */
    Runnable r = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lx = binding.lockBtn.getX();
            ly = binding.lockBtn.getHeight();
        }
    };

    private float lx, ly;

    private void lockBtnSet() {
        binding.lockBtn.setOnClickListener(v -> {
            isLock = !isLock;
            binding.lockBtn.setImageResource(isLock ? R.drawable.lock_foreground : R.drawable.lock_open_foreground);
            binding.lockBtn.startAnimation(lockBtnHideAnimation());
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isLock && (ev.getX() < lx || ev.getY() > ly)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    private static long t1;

    @Override
    protected void onResume() {
        super.onResume();
        t1 = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        t1 = System.currentTimeMillis() - t1;
    }

    private boolean isLock = false;
    private final Handler handler = new Handler();

    private boolean runF = true;

    @Override
    protected void onDestroy() {
        runF = false;
        super.onDestroy();
    }

    final Runnable timerR = () -> {
        while (runF) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MainActivity.timeTool.Stat();
        }
    };
}