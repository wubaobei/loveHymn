package pri.prepare.lovehymn.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.DisplayStat;
import pri.prepare.lovehymn.databinding.ActivityWhiteBinding;
import pri.prepare.lovehymn.server.entity.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

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
        PDFView pdfv = binding.pdfv2;
        pdfv.fromFile(new File(path)).defaultPage(page).load();
        handler.post(runnable);
        lockBtnSet();
        new Thread(r).start();
        new Thread(timerR).start();
        MainActivity.timeTool.Resume();
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
            btnAlpha = 1f;
            binding.lockBtn.setImageResource(isLock ? R.drawable.lock_foreground : R.drawable.lock_open_foreground);
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isLock && (ev.getX() < lx || ev.getY() > ly))
            return true;
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
        isRun = false;
        t1 = System.currentTimeMillis() - t1;
    }

    private boolean isLock = false;
    private float btnAlpha = 1f;
    private boolean isRun = true;
    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                this.update();
                int n = 1000 / DisplayStat.HZ;
                if (isRun)
                    handler.postDelayed(this, n);
            } catch (Exception e) {
                Logger.exception(e);
            }
        }

        void update() {
            btnAlpha -= 0.01f;
            if (btnAlpha < 0f)
                btnAlpha = 0f;
            binding.lockBtn.setAlpha(btnAlpha);
        }
    };

    final Runnable timerR = () -> {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MainActivity.timeTool.Stat();
        }
    };
}