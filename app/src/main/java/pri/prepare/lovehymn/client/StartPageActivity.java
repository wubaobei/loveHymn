package pri.prepare.lovehymn.client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import java.util.Random;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.LoadProcess;
import pri.prepare.lovehymn.client.tool.SPBGManager;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.StartpageLayoutLoadBinding;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.UpdateHistory;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MusicSearch;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.PersonRemark;
import pri.prepare.lovehymn.server.entity.Setting;
import pri.prepare.lovehymn.server.function.BibleTool;
import pri.prepare.lovehymn.server.function.Constant;
import pri.prepare.lovehymn.server.function.DBHelper;
import pri.prepare.lovehymn.server.function.SdCardTool;
import pri.prepare.lovehymn.server.function.TipTool;

/**
 * 启动页（欢迎或加载资源和说明）
 */
public class StartPageActivity extends AppCompatActivity {
    private StartpageLayoutLoadBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DBHelper.init(this);
        if (Service.getC().noPer(this)) {
            binding = DataBindingUtil.setContentView(this, R.layout.startpage_layout_load);

            LoadProcess.init(this);
            ts = System.currentTimeMillis();

            new Thread(() -> {
                while (ts >= 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    timerH.sendEmptyMessage((int) (System.currentTimeMillis() - ts));
                }
            }).start();
            new Thread(rb).start();
        } else {
            int load = 0;
            try {
                load = Setting.getValueI(Setting.RES_VERSION);
            } catch (Exception e) {
                Logger.exception(e);
            }
            if (load < Setting.CURRENT_RES_VERSION) {
                //版本不兼容提示界面
                //211220注：一般是加载一般出问题（例如强制退出）导致的错误
                setContentView(R.layout.startpage_layout_failed);
                //记录一些系统信息，用于方便开发者判断情况
                TextView msgTv = findViewById(R.id.other_msg);
                msgTv.setText(Service.getC().getDebugMsg(this));
                //
                Button btn = findViewById(R.id.clear_app_info);
                btn.setOnClickListener(v -> Tool.openInfo(this));
            } else {
                isNormal = true;
                setContentView(R.layout.startpage_layout);
                int gd = Setting.getValueI(Setting.STARTPAGE_BACKGROUND);

                if(gd==0){
                    Random rd=new Random();
                    gd=rd.nextInt(SPBGManager.bname.length-1)+1;
                }
                findViewById(R.id.spbg_ll).setBackgroundResource(SPBGManager.ids[gd]);
                ((TextView) findViewById(R.id.sa1)).setText(SPBGManager.j0[gd]);
                ((TextView) findViewById(R.id.sa2)).setText(SPBGManager.j1[gd]);

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                TextView tvs = findViewById(R.id.sa3);
                tvs.setText(Constant.getStartPageAd());
                TextView mode = findViewById(R.id.testMode);
                if (UpdateHistory.isTestMode()) {
                    mode.setText("内测版");
                } else {
                    String c = Service.getC().getVersionStr(this);
                    String ver = Setting.getValueS(Setting.NEW_VERSION);
                    if (ver.length() != 0 && (!ver.equals(c))) {
                        if (Service.getC().compareVersion(ver, c)) {
                            mode.setText("新版本：" + ver);
                        }
                    }
                }
                new Thread(rb).start();
            }
        }
    }

    private boolean isNormal = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isNormal) {
            MainActivity.showAd = true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onPause() {
        Logger.writeToFile();
        super.onPause();
    }

    final Runnable rb = new Runnable() {
        @Override
        public void run() {
            Logger.info("开始获取权限，并尝试更新资源");
            try {
                //请求文件管理权限
                requestPermission();
                //请求一般读写权限
                verifyStoragePermissions(StartPageActivity.this);
                //等待用户给与读写权限
                if (Service.getC().noPer(StartPageActivity.this)) {
                    while (Service.getC().noPer(StartPageActivity.this)) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Logger.info("获取权限成功");
                    Service.getC().forceUpdate(StartPageActivity.this);
                    Setting.updateSetting(Setting.RES_VERSION, 1);
                }else{
                    Logger.info("已有权限？？");
                }

                String s = Service.getC().checkSelf();
                if (s.length() > 0) {
                    MainActivity.msgWait = s;
                    MainActivity.bgMsg = s;
                }
                try {
                    long t1 = System.currentTimeMillis();
                    MusicSearch.init(StartPageActivity.this);
                    BibleTool.loadRes(StartPageActivity.this);
                    PersonRemark.init();
                    long t2 = System.currentTimeMillis() - t1;
                    Logger.info("初始化耗时:" + t2);
                    if (t2 < 800)
                        Thread.sleep(800 - t2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ts = -1;
                toMain();
            } catch (Exception e) {
                //记录异常信息，方便定位问题
                Logger.exception(e);
                throw new RuntimeException(e.getMessage());
            }
        }
    };

    final int initPs = 3000;
    int ps = initPs;
    final Handler timerH = new Handler() {
        @SuppressLint({"HandlerLeak", "SetTextI18n"})
        @Override
        public void handleMessage(Message msg) {
            if (msg.what - ps > 5000) {
                if (ps == initPs) {
                    TextView textView = findViewById(R.id.sp_intro);
                    textView.setText("");
                    textView.setVisibility(View.INVISIBLE);

                    binding.title1.setText("隐藏功能介绍");
                    binding.title2.setText("你也可以在app的'设置'-'" + Constant.READ_ME + "'中找到它");
                    TipTool.addTips(StartPageActivity.this, TipTool.getAll(true), binding.readme);
                }

                ps = msg.what;
            }
            TextView tv = findViewById(R.id.sa3);
            tv.setText("已耗时：" + msg.what / 1000 + "秒");

            TextView tv2 = findViewById(R.id.sa1);
            tv2.setText("欢迎使用唱诗歌app\r\n" + LoadProcess.getText());
        }
    };
    private long ts;

    private void toMain() {
        Intent intent = new Intent(StartPageActivity.this, MainActivity.class);
        startActivity(intent);
        StartPageActivity.this.finish();
    }

    /**
     * 申请读写权限（如果没有）
     */
    public void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.MANAGE_EXTERNAL_STORAGE"};

    private static final int REQUEST_CODE = 123;

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, 123);
            }
        } else {
            // 先判断有没有权限
            if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                Logger.info("存储权限获取失败");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Logger.info("存储权限获取失败");
            }
        }
    }
}
