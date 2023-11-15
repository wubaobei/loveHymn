package pri.prepare.lovehymn.client;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.BuildConfig;
import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.DisplayStat;
import pri.prepare.lovehymn.client.tool.HistoryTool;
import pri.prepare.lovehymn.client.tool.I4Intro;
import pri.prepare.lovehymn.client.tool.I4Set;
import pri.prepare.lovehymn.client.tool.I4Catalog;
import pri.prepare.lovehymn.client.tool.I4LC;
import pri.prepare.lovehymn.client.tool.I4StopMp3;
import pri.prepare.lovehymn.client.tool.ThemeManager;
import pri.prepare.lovehymn.client.tool.TimeStatTool;
import pri.prepare.lovehymn.client.tool.TipStruct;
import pri.prepare.lovehymn.client.tool.TouchUtil;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.client.tool.VolumeUtil;
import pri.prepare.lovehymn.client.tool.enuCm;
import pri.prepare.lovehymn.databinding.ActivityMainBinding;
import pri.prepare.lovehymn.server.UpdateHistory;
import pri.prepare.lovehymn.server.dal.SettingD;
import pri.prepare.lovehymn.server.entity.Book;
import pri.prepare.lovehymn.server.entity.Label;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.PersonRemark;
import pri.prepare.lovehymn.server.function.CharConst;
import pri.prepare.lovehymn.server.function.CollectTool;
import pri.prepare.lovehymn.server.function.Constant;
import pri.prepare.lovehymn.server.function.DBHelper;
import pri.prepare.lovehymn.server.function.MusicManager;
import pri.prepare.lovehymn.server.function.ScreenKTool;
import pri.prepare.lovehymn.server.function.SdCardTool;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;
import pri.prepare.lovehymn.server.function.TipTool;
import pri.prepare.lovehymn.server.function.WebHelper;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private VolumeUtil volumeUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            DBHelper.init(MainActivity.this);
            Service.getC().setTheme(this);
            binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
            setScreenK();

            Logger.info("onCreate " + Service.getC().getVersionStr(this));
            volumeUtil = new VolumeUtil(this);

            createTime = System.currentTimeMillis();

            tt = new TouchUtil(this.getWindowManager());

            boolean showHis = showVersionUpdateHistory();
            if (!showHis && Setting.getValueB(Setting.SHOW_TIG))
                showTips();
            timeTool = new TimeStatTool(300, 300);
        } catch (Exception e) {
            Logger.info("onCreate 出现bug");
            Logger.exception(e);
        }
    }

    private void showTips() {
        TipStruct tip = TipTool.getTig();
        if (tip != null) {
            TipDialog td = new TipDialog(this, tip);
            td.showDialog();
        }
    }

    private long createTime;
    public static TimeStatTool timeTool;

    public static boolean showAd = false;

    /**
     * 显示版本更新历史（如果更新了版本）
     */
    private boolean showVersionUpdateHistory() {
        try {
            if (showAd) {
                showAd = false;
                String k = Setting.getValueS(Setting.AD_TITLE);
                String v = Setting.getValueS(Setting.AD_CONTENT);
                Tool.ShowDialog(this, k, v);
                return true;
            }
            String[] vs = UpdateHistory.getUpdate(this);
            if (vs.length > 0) {
                if (vs[0].equals(UpdateHistory.WELCOME)) {
                    //第一次打开
                    try {
                        new QucikSettingDialog(this, vs[1]).showDialog();
                    } catch (Exception e) {
                        Logger.info("qucik errrp");
                        Logger.exception(e);
                    }
                } else
                    Tool.ShowDialog(this, vs[0], vs[1]);
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.exception(e);
            return false;
        }
    }

    //region 状态栏设置
    //全屏并且隐藏状态栏
    private void hideStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    //endregion

    private TouchUtil tt;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        try {
            tt.set(this.getWindowManager());
            tt.set(0, 0);

            setScreenK();
        } catch (Exception e) {
            Logger.exception(e);
        }
        super.onConfigurationChanged(newConfig);
    }

    private float pdfPro = 0f;
    private boolean isPdf0 = true;
    private boolean isSwitchPdf = false;

    /**
     * 当前pdfView
     */
    private PDFView getPdfV0() {
        return isPdf0 ? binding.pdfv0 : binding.pdfv1;
    }

    /**
     * 要切换的pdfView
     */
    private PDFView getPdfV1() {
        return isPdf0 ? binding.pdfv1 : binding.pdfv0;
    }

    /**
     * 过渡动画完成，交换pdfView
     */
    private void switchPdfvFinish() {
        isPdf0 = !isPdf0;
    }

    /**
     * 调整长宽比
     */
    private void setScreenK() {
        try {
            ScreenKTool sk = new ScreenKTool(this.getWindowManager());
            PDFView pdfView = getPdfV0();
            ViewGroup.LayoutParams lp = pdfView.getLayoutParams();
            if (lp != null) {
                lp.width = sk.getWidth();
                pdfView.setLayoutParams(lp);
                getPdfV1().setLayoutParams(lp);
            }
            boolean showTime = Setting.getValueB(Setting.AUTO_STEP_TIME);
            if (Setting.getValueB(Setting.STATUS_BAR_SHOW) && !showTime && lp.width<lp.height)
                Tool.showStatusBar(getWindow(), this);
            else
                hideStatusBar();

        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    /**
     * 持续多少刷新间隔（60HZ）
     */
    public static int lastInToast = 10;

    public static String msgWait = "";
    /**
     * 用于更新bgMsg（配合msgWait使用）
     */
    public static String bgMsg = "";

    /**
     * 持续(time/60)s
     */
    public void ToastInTimerH(String s, int time) {
        try {
            lastInToast = time;
            if (time > 60 * 10) {
                s += "(点击关闭)";
            }
            Bundle bd = new Bundle();
            bd.putString("a", s);
            Message msg = new Message();
            msg.setData(bd);
            toastH.sendMessage(msg);
            Logger.info(s + " Toast show");
        } catch (Exception e) {
            Logger.info("ToastInTimerH 出现bug");
            Logger.exception(e);
        }
    }

    /**
     * 持续3s
     */
    public void ToastInTimerH(String s) {
        ToastInTimerH(s, 180);
    }

    boolean toastHasValue = false;

    @SuppressLint("HandlerLeak")
    final Handler toastH = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                String v = msg.getData().getString("a");
                MyFile f = MyFile.from(v);
                if (f.isPdf() || f.isMp3()) {
                    loadPdf(f);
                    return;
                }

                TextView tv = binding.tvInToast;
                tv.setText(v);
                tv.setTextSize(getTextSize(v));
                tv.setBackgroundColor(Color.WHITE);
                tv.setBackgroundResource(R.drawable.btn_shape);
                toastHasValue = true;

            } catch (Exception e) {
                Logger.exception(e);
            }
            super.handleMessage(msg);
        }
    };

    private float getTextSize(String s) {
        float f = 22f - s.length() / 5f;
        if (f < 13)
            f = 13;
        else if (f > 20)
            f = 20;
        return f;
    }

    @SuppressLint("ShowToast")
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            //更新时屏蔽触摸
            if (DisplayStat.getC().isUpdateRes)
                return true;

            int pn = tt.PreviewOrNext(ev);
            int tr = tt.PressThree(ev);
            int dr = tt.PressDouble(ev);
            boolean lp = tt.LongPress(ev);

            if (tt.clickCenter(ev)) {
                hideBtnClickEvent();
                return true;
            }

            if (tr == TouchUtil.THREE_LEFT) {
                ToastInTimerH("请使用双指滑动代替三指滑动");
            } else if (tr == TouchUtil.THREE_RIGHT) {
                ToastInTimerH("请使用双指滑动代替三指滑动");
            } else if (dr == TouchUtil.DOUBLE_LEFT) {
                MyFile f = HistoryTool.getNext();
                if (f != null) {
                    loadPdf(f, false);
                } else
                    ToastInTimerH("没有下一首");
            } else if (dr == TouchUtil.DOUBLE_RIGHT) {
                MyFile f = HistoryTool.getPreview();
                if (f != null) {
                    loadPdf(f, false);
                } else
                    ToastInTimerH("没有上一首");
            } else if (dr == TouchUtil.DOUBLE_UP) {
                quickSign(Setting.getValueI(Setting.DOUBLE_FINGER_UP));
            } else if (dr == TouchUtil.DOUBLE_DOWN) {
                quickSign(Setting.getValueI(Setting.DOUBLE_FINGER_DOWN));
            } else if (pn != 0) {
                if (pn == 1) {
                    if (fn != null)
                        loadPdf(fn);
                    else {
                        ToastInTimerH("没有下一首");
                    }
                } else {
                    if (fp != null)
                        loadPdf(fp);
                    else {
                        ToastInTimerH("没有上一首");
                    }
                }
            } else if (lp) {
                //长按
                CollectHistoryDialog dialog = new CollectHistoryDialog(this, lastFile, lastHymn, i4Set, i4Lc);
                dialog.showDialog();
            }
        } catch (Exception e) {
            Logger.exception(e);
            //当加载失败时，可能需要通过长按查看教程，屏蔽此时的错误提示
            if (loadPdfSuccess)
                ToastInTimerH("出了点小问题：" + e.getMessage());
        }
        return super.dispatchTouchEvent(ev);
    }

    private void mp3StateSet() {
        try {
            binding.mp3PlayerBtn.setOnClickListener(v -> {
                startMp3();
            });
            binding.mp3PlayerTv.setOnClickListener(v -> binding.mp3PlayerBtn.callOnClick());

            if (musicManager == null) {
                return;
            }

            ImageButton playBtn = binding.playBtn;
            TextView playT = binding.playBtnT;
            ImageButton repeatBtn = binding.repeatBtn;
            TextView repeatT = binding.repeatBtnT;

            boolean repeat = Setting.getValueB(Setting.MP3_LOOP);
            if (repeat != musicManager.getRepeat())
                musicManager.changeSimpleRepeat();
            if (musicManager.getRepeat()) {
                repeatBtn.setImageResource(ThemeManager.getMipmap(this, R.attr.mapRepeat));
                repeatT.setText(getResources().getString(R.string.repeat));
            } else {
                repeatBtn.setImageResource(ThemeManager.getMipmap(this, R.attr.mapRepeatNo));
                repeatT.setText(getResources().getString(R.string.repeat_no));
            }

            if (musicManager.isPlaying()) {
                playBtn.setImageResource(ThemeManager.getMipmap(this, R.attr.mapPause));
                playT.setText(getResources().getString(R.string.pause));
            } else {
                playBtn.setImageResource(ThemeManager.getMipmap(this, R.attr.mapPlay));
                playT.setText(getResources().getString(R.string.play));
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    final I4StopMp3 i4StopMp3 = new I4StopMp3() {
        @Override
        public void Stop() {
            if (musicManager.isPlaying())
                musicManager.pause();
        }
    };

    private void startMp3() {
        try {
            List<String> mp3List = Service.getC().getMp3C();
            if (mp3List.size() > 0) {
                CommonListDialog cl = new CommonListDialog(MainActivity.this, 1, mp3List, i4StopMp3, lastFile.getHymn());
                cl.showDialog();
            } else {
                ToastInTimerH("未找到mp3资源");
                binding.trList.removeView(binding.cataMp3);
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private void hide1Set() {
        try {
            RelativeLayout iv = binding.rLayout;
            iv.setY(0f);
            RelativeLayout iv2 = binding.rLayout2;
            iv2.setY(0f);
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private void hideBtnClickEvent() {
        RelativeLayout iv = binding.rLayout;
        TextView bigIv = binding.tvback;
        RelativeLayout iv2 = binding.rLayout2;
        //
        if (iv.getY() < 0.001f) {
            DisplayStat.getC().beginHide();
        } else if (bigIv.getHeight() < iv2.getHeight()) {
            DisplayStat.getC().resetToolBar();
            hide1Set();
        }
    }

    private int resumeToSetPdfFlag = -1;

    @Override
    protected void onResume() {
        try {
            resumeToSetPdfFlag = 50;
            Logger.info("onResume");

            LocalDateTime ldt = Setting.getValueT(Setting.PAUSE_TIME);

            DisplayStat.getC().resetToolBar();
            isRun = true;
            new Thread(timerR).start();

            String s = Setting.getValueS(Setting.LAST_OPEN);
            loadPdf(s);
            catelogBtnSet();
            settingBtnSet();

            checkWhenOpen();
            mp3StateSet();

            setCataLL();

            binding.bgMsg.setText("加载pdf失败，请尝试下列方法，如果仍有问题联系作者\r\n" + Service.getC().getFixFunctions() + "\r\n"
                    + Service.getC().getDebugMsg(this)
                    + "\r\n在'设置'-'" + SettingDialog.ALL_READ + "'中看到《加百度好友教程》《附加包下载及使用教程》");

            timeTool.Resume();

            binding.tvInToast.setOnClickListener(v -> {
                if (lastInToast > 10)
                    lastInToast = 10;
            });
        } catch (Exception e) {
            Logger.info("onResume 出现bug");
            Logger.exception(e);

            Toast.makeText(this, "app出了点问题", Toast.LENGTH_LONG).show();
            this.onDestroy();
        }

        if (DBHelper.updateStr.length() > 0) {
            final AlertDialog.Builder alterDiaglog = new AlertDialog.Builder(MainActivity.this);
            alterDiaglog.setTitle("警告");
            alterDiaglog.setMessage("由于数据库版本更新：" + DBHelper.updateStr +
                    "，可能导致错误数据。如果发现数据异常，例如标题显示错误等，请清除app数据。\r\n"
                    + DBHelper.getCurrentHistory());
            alterDiaglog.setPositiveButton("现在不清除", (dialog, which) -> {
            });
            alterDiaglog.setNegativeButton("清除app数据", (dialog, which) -> Tool.openInfo(this));
            alterDiaglog.show();
            DBHelper.updateStr = "";
        }

        super.onResume();
    }

    private boolean jumpCheck = false;

    /**
     * 目录放最前边，而非中间，可以改为可设置的
     */
    private void setCataLL() {
        TableRow tr = binding.trList;
        LinearLayout ll = binding.cataLl;
        tr.removeView(ll);
        tr.addView(ll, 0);
    }

    @Override
    protected void onPause() {
        isRun = false;
        recordPdfProcess();
        timeTool.Pause();
        Logger.info("onPause");
        Logger.writeToFile();
        super.onPause();
    }

    private void recordPdfProcess() {
        try {
            PDFView pdfv = getPdfV0();
            Setting.updateSetting(Setting.PDF_Y_OFFSET, (int) (pdfv.getPositionOffset() * 10000));
        } catch (Exception e) {
            Logger.info("记录pdf进度出现异常");
            Logger.exception(e);
            Tool.toastRestart(this, e);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            DBHelper.current.closeWritableDB(true);
            Logger.info("onDestroy");
            if (musicManager != null) {
                musicManager.release();
                musicManager = null;
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
        super.onDestroy();
    }

    private boolean isRun = true;
    private MusicManager musicManager = null;
    /**
     * 仅用于从MP3ListActivity跳转时传递参数
     */
    public static String jumpFromMp3List = "";

    /**
     * 每次打开时的检查（更新等）
     */
    private void checkWhenOpen() {
        if (jumpCheck) {
            return;
        }
        try {
            new Thread(checkResAble).start();
            new Thread(getAd).start();
            DBHelper.current.check();
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private boolean loadPdfSuccess = false;

    /**
     * 计时器
     */
    final Runnable timerR = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - createTime > 1000)
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Logger.exception(e);
                }

            try {
                while (isRun) {
                    try {
                        int n = 1000 / DisplayStat.HZ;
                        Thread.sleep(n);
                    } catch (InterruptedException e) {
                        Logger.exception(e);
                    }
                    if (msgWait.length() > 0) {
                        if (msgWait.startsWith("解压")) {
                            ToastInTimerH(msgWait, 60 * 60);
                        } else {
                            ToastInTimerH(msgWait);
                        }
                        msgWait = "";
                        //if (bgMsg.length() > 0)
                        //    binding.bgMsg.setText(bgMsg);
                    }

                    //播放列表跳转到pdf
                    if (jumpFromMp3List.length() > 0) {
                        msgWait = jumpFromMp3List;
                        jumpFromMp3List = "";
                        continue;
                    }

                    if (resumeToSetPdfFlag >= 0) {
                        PDFView pdfView = isSwitchPdf ? getPdfV1() : getPdfV0();
                        try {
                            resumeToSetPdfFlag--;
                            pdfView.setPositionOffset(Setting.getValueI(Setting.PDF_Y_OFFSET) / 10000f);
                            resumeToSetPdfFlag = -1;
                            loadPdfSuccess = true;
                        } catch (Exception e) {
                            //Logger.exception(e);
                        }
                    }

                    if (DisplayStat.getC().updateResInFirst) {
                        updateEndH.sendEmptyMessage(0);
                        DisplayStat.getC().updateResInFirst = false;
                    }

                    if (DisplayStat.getC().isUpdateRes) {
                        DisplayStat.getC().updateProcess++;
                        updateH.sendEmptyMessage(DisplayStat.getC().updateProcess);
                    } else {
                        DisplayStat.getC().addToolBarPercent();
                        timerH.sendEmptyMessage(0);
                    }

                    if (musicManager != null && musicManager.needUpdatePlayBtn) {
                        mp3StateSet();
                    }

                }
            } catch (Exception e) {
                Logger.info("timerR 出现bug");
                Logger.exception(e);
            }
        }
    };

    TextView tvU = null;

    @SuppressLint("HandlerLeak")
    final Handler updateH = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            try {
                if (tvU == null) {
                    RelativeLayout rl = binding.all;
                    tvU = new TextView(MainActivity.this);
                    tvU.setText(Constant.UPDATING + "\r\n0s");
                    tvU.setBackgroundResource(R.drawable.btn_shape);
                    tvU.setTextSize(20f);
                    tvU.setGravity(Gravity.CENTER);

                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

                    rl.addView(tvU, layoutParams);
                } else {
                    tvU.setText(Constant.UPDATING + "\r\n" + (msg.what / 60) + "s");
                }
            } catch (Exception e) {
                Logger.exception(e);
            }
        }
    };

    @SuppressLint("HandlerLeak")
    final Handler updateEndH = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                if (tvU != null) {
                    RelativeLayout rl = binding.all;
                    rl.removeView(tvU);
                    tvU = null;
                }
            } catch (Exception e) {
                Logger.exception(e);
            }
        }
    };


    /**
     * 计时器响应事件
     */
    @SuppressLint({"HandlerLeak", "CutPasteId"})
    final Handler timerH = new Handler() {
        @SuppressLint("SimpleDateFormat")
        @Override
        public void handleMessage(Message msg) {
            try {
                timeTool.Stat();
                //当满足条件时 弹出添加足迹提示（自动足迹）
                if (timeTool.WarnOnce() && (Setting.getValueI(Setting.AUTO_STEP) == 1) && (!lastHymn.hasStepToday())) {
                    CommonDialog cd = new CommonDialog(MainActivity.this, enuCm.LEAVE_STEP, () -> setTitleText(lastFile)
                            , MainActivity.this, String.valueOf(lastHymn.getId()));
                    cd.showDialog();
                }

                boolean showStepTime = Setting.getValueB(Setting.AUTO_STEP_TIME);
                binding.timeTv.setVisibility(showStepTime ? View.VISIBLE : View.INVISIBLE);
                if (showStepTime) {
                    binding.timeTv.setText(timeTool.getTime());
                }

                if (tt.notSet()) {
                    int h1 = binding.tvbackTitle.getHeight() +
                            (viv == View.VISIBLE ? binding.mp3Layout.getHeight() : 0);
                    int h2 = binding.rLayout2.getHeight();

                    tt.set(h1, h2);
                }

                //长按显示收藏历史
                if (tt.isLongPress()) {
                    CollectHistoryDialog dialog = new CollectHistoryDialog(MainActivity.this, lastFile, lastHymn, i4Set, i4Lc);
                    dialog.showDialog();
                }

                //pdf渐隐效果
                if (isSwitchPdf) {
                    pdfPro += 0.1f;
                    if (pdfPro >= 1f) {
                        getPdfV1().setAlpha(1f);
                        getPdfV0().setAlpha(0f);
                        switchPdfvFinish();
                        isSwitchPdf = false;
                    } else {
                        getPdfV1().setAlpha(pdfPro);
                    }
                }

                TextView progressText = binding.progressText;
                if (musicManager != null) {
                    ProgressBar pb = binding.seekBar;
                    int pro = (int) (musicManager.getProgressPercent() * pb.getMax());
                    pb.setProgress(pro);
                    progressText.setText(musicManager.getProgressString());

                    if (musicManager.overEventAp) {
                        musicManager.stop();
                        mp3StateSet();
                        musicManager.overEventAp = false;
                    }
                }

                RelativeLayout iv = binding.rLayout;
                TextView back1 = binding.tvback;
                TextView tvhide = binding.tvShapeHide;
                TableLayout cal = binding.tableLayout;

                if (lastInToast <= 0 && toastHasValue) {
                    TextView tv = binding.tvInToast;
                    tv.setText("");
                    toastHasValue = false;
                    tv.setBackgroundColor(Color.argb(0, 0, 0, 0));
                }
                lastInToast--;

                TextView back2 = binding.tvback2;
                RelativeLayout iv2 = binding.rLayout2;
                if (DisplayStat.getC().resetLast()) {
                    iv2.setY(0);
                    iv.setY(0);
                }

                if (back1.getHeight() < cal.getHeight())
                    back1.setHeight(cal.getHeight());

                int h = binding.tableLayout2.getHeight() + binding.seekBarLayout.getHeight();
                if (back2.getHeight() < h)
                    back2.setHeight(h);

                //关闭标题栏和工具栏
                if (DisplayStat.getC().isClosingToolBar()) {
                    int height = back1.getHeight() + tvhide.getHeight();
                    int in2h = viv == View.VISIBLE ? iv2.getHeight() : (binding.tvbackTitle.getHeight() + binding.dpdp.getHeight());

                    if (iv.getY() < iv.getHeight()) {
                        iv2.setY(-DisplayStat.getC().getToolBarPercent() * in2h);
                        iv.setY(DisplayStat.getC().getToolBarPercent() * height);
                    }
                }

                //加载pdf成功时，关闭加载失败的说明
                if (loadPdfSuccess) {
                    binding.bgMsg.setText("");
                }
            } catch (Exception e) {
                Logger.exception(e);
            }
        }
    };

    /**
     * 更新广告内容
     */
    final Runnable getAd = () -> {
        HashMap<String, String> ads = WebHelper.getAds();
        if (ads == null) return;
        if (ads.containsKey(WebHelper.AD_TITLE)) {
            Setting.updateSetting(Setting.AD_TITLE, ads.get(WebHelper.AD_TITLE));
            Setting.updateSetting(Setting.AD_CONTENT, ads.get(WebHelper.AD_CONTENT));
        }

        String lt = Setting.getValueS(Setting.LAST_BD);
        if (ads.containsKey(WebHelper.BD_URL) && ads.containsKey(WebHelper.BD_PW) && ads.containsKey(WebHelper.NEW_VERSION)) {
            Logger.info("获取下载地址" + ads.get(WebHelper.BD_URL));
            if (lt.length() == 0) {
                if (ads.containsKey(WebHelper.BD_URL) && ads.containsKey(WebHelper.BD_PW)) {
                    Setting.updateSetting(Setting.LAST_BD, ads.get(WebHelper.BD_URL));
                    Setting.updateSetting(Setting.LAST_BD_PWD, ads.get(WebHelper.BD_PW));
                    Setting.updateSetting(Setting.NEW_VERSION, ads.get(WebHelper.NEW_VERSION));
                    Logger.info("首次获取下载地址");
                    lt = ads.get(WebHelper.BD_URL);
                }
            }
            if (!ads.get(WebHelper.BD_URL).equals(lt)) {
                ToastInTimerH("发现新版本：" + ads.get(WebHelper.NEW_VERSION) + "\r\n点击'更多'-'下载与更新'查看");

                Setting.updateSetting(Setting.LAST_BD, ads.get(WebHelper.BD_URL));
                Setting.updateSetting(Setting.LAST_BD_PWD, ads.get(WebHelper.BD_PW));
                Setting.updateSetting(Setting.NEW_VERSION, ads.get(WebHelper.NEW_VERSION));
            }
        }
    };

    /**
     * 检查新的附加包
     */
    final Runnable checkResAble = () -> {
        try {
            checkRes();
            jumpCheck = true;
        } catch (Exception e) {
            Logger.exception(e);
        }
    };

    private void checkRes() {
        long t1 = System.currentTimeMillis();
        String mp3N = Service.getC().scanAddedFile();

        if (mp3N.length() > 0) {
            Logger.info("AddedFile " + mp3N);
        }
        String[] arr = SdCardTool.getResFileList();
        String res = "";
        for (String a : arr) {
            if (a.contains("标签") && (!Setting.getValueB(Setting.LOAD_LABEL))) {
                Service.getC().loadResFileByName(a);
                Setting.updateSetting(Setting.LOAD_LABEL, true);
                res += "标签,";
            } else if (a.contains("足迹") && (!Setting.getValueB(Setting.LOAD_STEP))) {
                Service.getC().loadResFileByName(a);
                Setting.updateSetting(Setting.LOAD_STEP, true);
                res += "足迹,";
            } else if (a.contains("qita") && (!Setting.getValueB(Setting.LOAD_QITA))) {
                Service.getC().loadResFileByName(a);
                Setting.updateSetting(Setting.LOAD_QITA, true);
                res += "其他诗歌信息,";
            }
        }
        if (res.length() > 0) {
            MainActivity.msgWait = "自动加载资源" + res.substring(0, res.length() - 1);
        }
        for (Book bk : Book.getAll())
            bk.renamePinYin();
        Logger.info("自动扫描耗时：" + (System.currentTimeMillis() - t1));
    }

    /**
     * 设置（更多）按钮设置
     */
    private void settingBtnSet() {
        try {
            ImageButton btn = binding.moreBtn;
            btn.setOnClickListener(v -> openSettingDialog());
            TextView tv = binding.moreTv;
            tv.setOnClickListener(v -> btn.callOnClick());
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private void openSettingDialog() {
        try {
            SettingDialog cl = new SettingDialog(this, i4Set, this.getWindowManager());
            cl.showDialog();
        } catch (Exception e) {
            Logger.exception(e);
            MainActivity.msgWait = e.getMessage();
        }
    }

    /**
     * 目录按钮设置
     */
    private void catelogBtnSet() {
        try {
            ImageButton btn = binding.cataBtn;
            btn.setOnClickListener(v -> openCatalogDialog(""));

            TextView tv = binding.cataTv;
            tv.setOnClickListener(v -> btn.callOnClick());
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private void openCatalogDialog(String searchString) {
        try {
            CatalogDialog cl = new CatalogDialog(this, i4Catalog, searchString, i4StopMp3, lastFile.getHymn());
            cl.showDialog();
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    final I4Set i4Set = new I4Set() {
        @SuppressLint("QueryPermissionsNeeded")
        @Override
        public void Share(MyFile file, boolean isMp3) {
            try {
                Logger.info("share " + file.getName());
                final Uri uri;
                String newPath = SdCardTool.getSharePath() + File.separator + Service.getC().getNameAfterDeal(file, true);
                if (isMp3) {
                    newPath = newPath.replace("pdf", "mp3");
                    if (!Service.getC().copyFile(file.getMp3(), MyFile.from(newPath)))
                        return;
                } else if (!Service.getC().copyFile(file, MyFile.from(newPath))) {
                    Logger.info("复制文件出错");
                    return;
                }

                uri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".preparewu", new File(newPath));

                Logger.info("uri: " + uri);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_STREAM, uri);
                if (isMp3)
                    share.setType("audio/x-mpeg");
                else
                    share.setType("application/pdf");
                share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                share.addCategory(Intent.CATEGORY_DEFAULT);
                share.setPackage("com.tencent.mm");

                if (share.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                    startActivity(share);
                    Logger.info("分享文件成功");
                } else
                    Logger.info("分享文件出错");
            } catch (Exception e) {
                Logger.exception(e);
            }
        }

        @Override
        public void RefreshScreenK() {
            setScreenK();
        }

        @Override
        public void loadPdfCall(String text) {
            loadPdf(text);
        }
    };

    private final I4Catalog i4Catalog = text -> loadPdf(text);

    private final I4Intro i4Intro = s -> openCatalogDialog(s);

    public static MyFile lastFile = null;
    public static Hymn lastHymn = null;

    /**
     * 将其他控件置顶
     */
    private void bringOtherToFront() {
        binding.otherToolLl.bringToFront();
    }

    private void setTitleText(MyFile file) {
        TextView tv = binding.tvbackTitle;
        String sb = "";
        String sbEnd = "";
        if (file.getHymn() != null) {
            Label[] ls = Label.getByHymnId(file.getHymn().getId());
            if (ls.length > 0)
                sb += CharConst.LABEL;

            String[] st = file.getHymn().getSteps();
            if (st.length > 0) {
                int id = st.length == 1 ? R.drawable.step1 : (st.length == 2 ? R.drawable.step2 : R.drawable.step3);
                if (file.getHymn().hasStepToday())
                    id = st.length == 1 ? R.drawable.step1_g : (st.length == 2 ? R.drawable.step2_g : R.drawable.step3_g);
                Tool.drawableRightSet(tv, this, id);
            } else
                Tool.drawableRightSet(tv, this, -1);
        }
        tv.setTextColor(Color.GRAY);
        if (CollectTool.hasFile(file)) {
            sbEnd += CharConst.STAR;
        }
        tv.setText(sb + Service.getC().getNameAfterDeal(file, false) + sbEnd);
    }

    private final I4LC i4Lc = (file, mod) -> {
        try {
            setTitleText(file);
            if (mod == I4LC.MOD_COLLECT) {
                if (CollectTool.hasFile(file))
                    ToastInTimerH("已加入收藏夹");
                else
                    ToastInTimerH("已移出收藏夹");
            } else if (mod == I4LC.ADD_STEP) {
                ToastInTimerH("已留下足迹");
            } else if (mod == I4LC.CLEAR_COLLECT) {
                ToastInTimerH("已清空收藏夹");
            } else if (mod == I4LC.CLEAR_HISTORY) {
                ToastInTimerH("已清空历史记录");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    /**
     * mp3工具栏是否可见
     */
    private int viv;

    private void mp3BtnSet(MyFile f) {
        try {
            File mp3;
            if (f == null)
                mp3 = null;
            else
                mp3 = f.getMp3();
            viv = mp3 == null ? View.INVISIBLE : View.VISIBLE;
            //
            RelativeLayout mp3Ll = binding.mp3Layout;
            mp3Ll.setVisibility(viv);
            TextView tvdpdp = binding.dpdp;
            tvdpdp.setVisibility(4 - viv);
            //

            //pause btn
            ImageButton playBtn = binding.playBtn;
            TextView playTv = binding.playBtnT;
            //repeat btn
            ImageButton repeatBtn = binding.repeatBtn;
            TextView repeatTv = binding.repeatBtnT;
            SeekBar progressBar = binding.seekBar;
            ImageButton randomBtn = binding.randomBtn;
            TextView randomTv = binding.randomBtnT;
            //progress text
            //final TextView progressTV = binding.progressTV);
            if (musicManager != null) {
                if (!(musicManager.isPlaying() && musicManager.isFile(mp3))) {
                    musicManager.release();
                    musicManager = null;
                }
            }

            if (mp3 != null) {
                if (musicManager == null)
                    musicManager = new MusicManager(mp3);
                mp3StateSet();
                playBtn.setOnClickListener(v -> {
                    DisplayStat.getC().resetToolBar();
                    if (musicManager.isPlaying())
                        musicManager.pause();
                    else {
                        volumeUtil.setMediaVolumeIfZero();
                        musicManager.Play();
                    }

                    mp3StateSet();
                });
                playTv.setOnClickListener(v -> playBtn.callOnClick());
                repeatBtn.setOnClickListener(v -> {
                    DisplayStat.getC().resetToolBar();
                    musicManager.changeSimpleRepeat();
                    Setting.updateSetting(Setting.MP3_LOOP, musicManager.getRepeat());
                    mp3StateSet();
                });
                repeatTv.setOnClickListener(v -> repeatBtn.callOnClick());
                progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        Logger.info("点击进度条");
                        tt.jumpThis();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        musicManager.setProgress(((double) seekBar.getProgress()) / seekBar.getMax());
                        DisplayStat.getC().resetToolBar();
                    }
                });
                randomBtn.setOnClickListener(v -> {
                    Logger.info("click random");
                    MyFile f1 = Service.getC().getRandomMp3File();
                    if (f1 == null)
                        ToastInTimerH("出了点小问题，找不到随机MP3");
                    else
                        loadPdf(f1);
                });
                randomTv.setOnClickListener(v -> randomBtn.callOnClick());
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    MyFile fp = null;
    MyFile fn = null;

    private void setPreviousNext(final MyFile f) {
        try {
            fp = Service.getC().previousFile(f);
            fn = Service.getC().nextFile(f);
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private void detailBtnSet(final MyFile f) {
        try {
            final Hymn hymn = f.getHymn();
            ImageButton dt = binding.introBtn;
            TextView tv = binding.introTv;
            if (hymn == null) {
                Logger.info("找不到该pdf对应诗歌信息：" + f.getAbsolutePath());
                dt.setImageResource(R.drawable.detail_no);
                tv.setText("无资料");
                dt.setClickable(false);
                tv.setClickable(false);
                return;
            }
            lastHymn = hymn;
            int count = hymn.hasContentCount();
            if (count == 0) {
                dt.setImageResource(R.drawable.detail_no);
                tv.setText("无资料");
                dt.setClickable(false);
                tv.setClickable(false);
                return;
            }

            int n = hymn.hasContentCount();
            if (n == 0) {
                dt.setImageResource(R.drawable.detail_no);
            } else if (n == 1) {
                dt.setImageResource(ThemeManager.getMipmap(this, R.attr.mapC1));
            } else if (n == 2) {
                dt.setImageResource(ThemeManager.getMipmap(this, R.attr.mapC2));
            } else {
                dt.setImageResource(ThemeManager.getMipmap(this, R.attr.mapC3));
            }
            if (n == 1 && hymn.getLyric().length() > 0) {
                tv.setText("歌词");
            } else {
                tv.setText("详情");
            }

            dt.setClickable(true);
            dt.setOnClickListener(v -> introBtnClick(hymn));
            tv.setClickable(true);
            tv.setOnClickListener(v -> dt.callOnClick());
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private void introBtnClick(Hymn h) {
        try {
            IntroDialog dialog = new IntroDialog(this, h, i4Intro);
            dialog.showDialog();
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private void loadPdf(int res) {
        PDFView pdfView = getPdfV1();
        pdfView.bringToFront();
        bringOtherToFront();
        isSwitchPdf = true;
        pdfPro = 0f;
        pdfView.fromStream(getResources().openRawResource(res)).load();

        binding.tvbackTitle.setText("其他");
        mp3BtnSet(null);
    }

    private void loadPdf(String path) {
        if (Service.isInteger(path)) {
            loadPdf(Integer.parseInt(path));
        } else
            loadPdf(MyFile.from(path));
    }

    private void loadPdf(MyFile f) {
        loadPdf(f, true);
    }

    private void loadPdf(final MyFile f, boolean modifyHis) {
        if (f.isMp3()) {
            loadPdf(f.getPdf(), modifyHis);
            return;
        }
        if ((lastFile != null && (!lastFile.getAbsolutePath().equals(f.getAbsolutePath())))) {
            timeTool.Restart();
        }
        //is pdf
        try {
            String remark = PersonRemark.getRemark(f.getHymn());
            if (remark.length() > 0)
                ToastInTimerH(remark);

            Logger.info("lodfpdf:" + f.getAbsolutePath());
            HistoryTool.add(f, modifyHis);
            if (tt != null)
                tt.set(0, 0);

            if (Setting.getValueB(Setting.SHOW_TOOL_BAR_ON_LOAD))
                DisplayStat.getC().resetToolBar();
            else
                DisplayStat.getC().hideToolBarNow();
            if (!f.isFile()) {
                Logger.info(f.getAbsolutePath() + "不存在或不是pdf文件");
                return;
            }
            PDFView pdfView = getPdfV1();
            pdfView.bringToFront();
            bringOtherToFront();
            isSwitchPdf = true;
            pdfPro = 0f;
            pdfView.fromFile(f.getfile()).load();
            Setting.updateSetting(Setting.LAST_OPEN, f.getAbsolutePath());
            detailBtnSet(f);
            mp3BtnSet(f);
            lastFile = f;
            setPreviousNext(f);
            setTitleText(f);
            binding.tvbackTitle.setOnClickListener(v -> {
                try {
                    LableCollectDialog dialog = new LableCollectDialog(this, f, i4Lc, i4Set);
                    dialog.showDialog();
                } catch (Exception e) {
                    Logger.exception(e);
                    ToastInTimerH("错误：该pdf缺失必要的信息");
                }
            });
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private void quickSign(int type) {
        switch (type) {
            case Setting.CATELOG_QUICK:
                binding.cataBtn.callOnClick();
                break;
            case Setting.COLEECT_QUICK:
                CollectTool.modCollect(lastFile);
                setTitleText(lastFile);
                if (CollectTool.hasFile(lastFile))
                    ToastInTimerH("已加入收藏夹");
                else
                    ToastInTimerH("已移出收藏夹");
                break;
            case Setting.DETAIL_QUICK:
                if (binding.introBtn.isClickable())
                    binding.introBtn.callOnClick();
                else
                    ToastInTimerH("没有详情内容");
                break;
            case Setting.LABLE_COLLECT_QUICK:
                binding.tvbackTitle.callOnClick();
                break;
            case Setting.MP3_PLAY_QUICK:
                binding.mp3PlayerBtn.callOnClick();
                break;
            default:
                ToastInTimerH("未能识别手势内容");
        }
    }
}
