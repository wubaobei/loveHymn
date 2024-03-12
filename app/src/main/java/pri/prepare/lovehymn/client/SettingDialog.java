package pri.prepare.lovehymn.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.I4Set;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.SPBGManager;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.SettingLayoutBinding;
import pri.prepare.lovehymn.server.UpdateHistory;
import pri.prepare.lovehymn.server.function.Constant;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.function.ScreenKTool;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;

public class SettingDialog extends Dialog implements IShowDialog {
    private final Context ct;
    private final I4Set iTell;
    private final WindowManager _wm;

    private final SettingLayoutBinding binding;
    private Activity activity;

    public SettingDialog(@NonNull Activity activity, I4Set tell, WindowManager wm) {
        super(activity);
        this.activity = activity;
        ct = activity;
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.setting_layout, null, false);
        setContentView(binding.getRoot());
        TextView tvv = binding.tvVersion;
        tvv.setText("当前版本：" + Service.getC().getVersionStr(ct));
        Service.getC().checkVersion(ct);
        iTell = tell;
        _wm = wm;
        setStasticsBtn();
        setUpdateHistoryBtn();
        setAuthorTV();
        setSettingIcons();
        setDispearBtn();
        setCEBtn();
        setUpdateBtn();
        setAdBtn();
        setSectionEnclises();
        setScreenKBtn();
        setSpecialSetting();
        setLabelSetting();
        setAllRead();
        setShowToolBarSetting();
        setDictSetting();
        setTigSetting();
        setSignSetting();
        setSmallToolSetting();
        setSPBGSetting();
    }

    private void commonSwitchSet(ImageButton img, int setId) {
        if (Setting.getValueB(setId))
            img.setImageResource(R.drawable.on);
        else
            img.setImageResource(R.drawable.off);

        img.setOnClickListener(v -> {
            boolean b = Setting.getValueB(setId);
            b = !b;
            Setting.updateSetting(setId, b);

            img.setImageResource(b ? R.drawable.on : R.drawable.off);
        });
    }

    private static final int[] dispearTimeArr = new int[]{999000, 2000, 4000, 6000};

    public static int getInitDispearTime() {
        return dispearTimeArr[dispearTimeArr.length - 1];
    }

    private String getText(int t) {
        if (t == dispearTimeArr[0])
            return "不自动隐藏";
        return (t / 1000d) + "秒";
    }

    /**
     * 设置图标
     */
    private void setSettingIcons() {
        int[] btnId = new int[]{R.id.resStatBtn, R.id.all_read,
                R.id.dispearTime, R.id.downloadAddressBtn, R.id.bibleShowChineseEnglish,
                R.id.screenK, R.id.specialSettingBtn,
                R.id.sectionCt, R.id.lableBtn,
                R.id.showtoolbar, R.id.dict_show, R.id.close_tig_btn, R.id.signSettingBtn, R.id.small_tool_btn, R.id.spgb};//R.id.alpha,
        int[] dId = new int[]{R.drawable.s_2, R.drawable.s_4,
                R.drawable.s_5, R.drawable.s_6, R.drawable.book,
                R.drawable.screen, R.drawable.special_setting,
                R.drawable.ct, R.drawable.label_icon,
                R.drawable.lan, R.drawable.spz, R.drawable.gth2, R.drawable.finger, R.drawable.small_tool, R.drawable.pngicon};//R.drawable.theme,

        for (int i = 0; i < btnId.length; i++) {
            Button btn1 = findViewById(btnId[i]);
            Tool.drawableLeftSet(btn1, ct, dId[i]);
        }
    }

    /**
     * 渐隐时间设置
     */
    private void setDispearBtn() {
        Button btn = binding.dispearTimeBtn;
        int v = Setting.getValueI(Setting.DISPEAR_TIME);
        btn.setText(getText(v));

        btn.setOnClickListener(v1 -> {
            int v2 = Setting.getValueI(Setting.DISPEAR_TIME);
            int ind = 0;
            for (int i = 0; i < dispearTimeArr.length; i++)
                if (dispearTimeArr[i] == v2) {
                    ind = i;
                    break;
                }
            ind++;
            if (ind >= dispearTimeArr.length)
                ind = 0;
            v2 = dispearTimeArr[ind];
            Setting.updateSetting(Setting.DISPEAR_TIME, v2);
            btn.setText(getText(v2));
        });
    }

    /**
     * 横屏的pdf比例
     */
    private void setScreenKBtn() {
        final ScreenKTool st = new ScreenKTool(_wm);
        Button btn = binding.screenKBtn;
        btn.setText(st.getK());

        btn.setOnClickListener(v -> {
            String[] kvs = st.getKs();
            String kv = st.getK();
            int ind = 0;
            for (int i = 0; i < kvs.length; i++)
                if (kvs[i].equals(kv)) {
                    ind = i;
                    break;
                }

            ind = (ind + 1) % kvs.length;
            st.setK(kvs[ind]);
            btn.setText(st.getK());
            iTell.RefreshScreenK();
        });
    }

    /**
     * 启动页背景
     */
    private void setSPBGSetting() {
        int bg = Setting.getValueI(Setting.STARTPAGE_BACKGROUND);
        Button btn = binding.spbgBtn;
        btn.setText(SPBGManager.bname[bg]);

        btn.setOnClickListener(v -> {
            int bg0 = Setting.getValueI(Setting.STARTPAGE_BACKGROUND);
            bg0 = (bg0 + 1) % SPBGManager.bname.length;
            Setting.updateSetting(Setting.STARTPAGE_BACKGROUND, bg0);
            btn.setText(SPBGManager.bname[bg0]);
        });
    }

    /**
     * 默认显示工具栏
     */
    private void setShowToolBarSetting() {
        commonSwitchSet(binding.showtoolbarSw, Setting.SHOW_TOOL_BAR_ON_LOAD);
    }

    /**
     * 经节中英显示设置
     */
    private void setCEBtn() {
        Button btn = binding.bibleShowChineseEnglishBtn;
        int v = Setting.getValueI(Setting.SHOW_CHINESE_ENGLISH);
        btn.setText(getCEText(v));

        btn.setOnClickListener(v1 -> {
            int v2 = Setting.getValueI(Setting.SHOW_CHINESE_ENGLISH);
            v2++;
            if (v2 > 3)
                v2 = 1;
            Setting.updateSetting(Setting.SHOW_CHINESE_ENGLISH, v2);
            btn.setText(getCEText(v2));
        });
    }

    /**
     * 经节中英显示
     */
    private String getCEText(int t) {
        if (t == 1)
            return "中文";
        if (t == 2)
            return "英文";
        if (t == 3)
            return "中英";
        return "中文";
    }

    /**
     * 显示经节连续显示
     */
    private void setSectionEnclises() {
        commonSwitchSet(binding.sectionCtBtn, Setting.SECTION_CT);
    }

    /**
     * 生僻字拼音
     */
    private void setDictSetting() {
        commonSwitchSet(binding.dictShowSw, Setting.SHOW_DICT);
    }

    /**
     * 资源统计
     */
    private void setStasticsBtn() {
        binding.resStatBtn.setOnClickListener(v -> Tool.ShowDialog(ct, "资源统计", Service.getC().getResStatString(), -1));
    }

    /**
     * 下载（更新）地址
     */
    private void setUpdateBtn() {
        Button btn = binding.downloadAddressBtn;
        String link = Setting.getValueS(Setting.LAST_BD);
        String pwd = Setting.getValueS(Setting.LAST_BD_PWD);

        btn.setEnabled(true);

        final String finalLink = link;
        final String finalPwd = pwd;
        btn.setOnClickListener(v -> {
            try {
                //总是回显示链接和提取码作为保底
                String str = "链接：" + finalLink + "  提取码：" + finalPwd;

                HashMap<String, String> map = new HashMap<>();
                map.put(Constant.LINK, link);
                map.put(Constant.PWD, pwd);
                SimpleTextDialog cl = new SimpleTextDialog(ct, map, SimpleTextDialog.DOWNLOAD);
                cl.showDialog();

                String disk = "com.baidu.netdisk";
                if (isExistApp(disk)) {
                    Service.getC().CB(str, ct);
                    try {
                        Intent intent = ct.getPackageManager().getLaunchIntentForPackage(disk);
                        ct.startActivity(intent);
                    } catch (Exception e) {
                        Logger.exception(e);
                    }
                } else {
                    Service.getC().openByIE(ct, map.get(Constant.LINK));
                    Service.getC().CB(map.get(Constant.PWD), ct);
                }
            } catch (Exception e) {
                Logger.exception(e);
                Tool.toastRestart(getContext(), e);
            }
        });
    }

    /**
     * 方法：是否存在app
     */
    private boolean isExistApp(String app) {
        try {
            ct.getPackageManager().getApplicationInfo(app, PackageManager.MATCH_UNINSTALLED_PACKAGES);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static final String ALL_READ = "说明文档";

    /**
     * 说明、帮助集合
     */
    private void setAllRead() {
        binding.allRead.setOnClickListener(v -> {
            AllReadDialog cl = new AllReadDialog(ct, activity, iTell);
            cl.showDialog();
            dismiss();
        });
    }

    /**
     * 其他设置
     */
    private void setSpecialSetting() {
        binding.specialSettingBtn.setOnClickListener(v -> {
            SpecialSettingDialog cl = new SpecialSettingDialog(ct, activity);
            cl.showDialog();
            dismiss();
        });
    }

    /**
     * 标签管理
     */
    private void setLabelSetting() {
        binding.lableBtn.setOnClickListener(v -> {
            LabelStepManagerDialog dialog = new LabelStepManagerDialog(ct);
            dialog.showDialog();
            dismiss();
        });
    }

    /**
     * 小工具
     */
    private void setSmallToolSetting() {
        binding.smallToolBtn.setOnClickListener(v -> {
            SmallToolDialog cl = new SmallToolDialog(ct, activity);
            cl.showDialog();
            dismiss();
        });
    }

    /**
     * 手势设置
     */
    private void setSignSetting() {
        binding.signSettingBtn.setOnClickListener(v -> {
            SignSettingDialog cl = new SignSettingDialog(ct);
            cl.showDialog();
            dismiss();
        });
    }

    /**
     * 关闭小贴士
     */
    private void setTigSetting() {
        boolean tig = Setting.getValueB(Setting.SHOW_TIG);
        if (!tig) {
            binding.closeTigBtn.setVisibility(View.GONE);
            return;
        }
        binding.closeTigBtn.setOnClickListener(v -> {
            final AlertDialog.Builder alterDiaglog = new AlertDialog.Builder(getContext());
            alterDiaglog.setTitle("警告");
            alterDiaglog.setMessage("由于种种原因，该app的许多功能用户都不会使用，因此添加了" + Constant.TIPS + "功能。如果你确定你会使用大部分功能，可以关闭自动弹窗，" +
                    "使app更简洁清爽。关闭后，除非你清空app数据或重装app，这些" + Constant.TIPS + "不会再自动弹出。另外，可以在'" + Constant.READ_ME + "'中查看所有" + Constant.TIPS);
            alterDiaglog.setPositiveButton("我知道了，确定关闭", (dialog, which) -> {
                Setting.updateSetting(Setting.SHOW_TIG, false);
                binding.closeTigBtn.setVisibility(View.GONE);
            });
            alterDiaglog.show();
        });
    }

    /**
     * 广告
     */
    private void setAdBtn() {
        HashMap<String, String> map = new HashMap<>();
        if (!map.containsKey(Constant.AD)) {
            map = new HashMap<>();
            map.put(Constant.AD, Constant.getRandomAd());
            map.put(Constant.AD_TEXT, Constant.getDefaultAdValue());
        }


        if (map.containsKey(Constant.AD)) {
            LinearLayout ll = binding.detLayout;
            RelativeLayout rl = binding.rLayout;
            int ind = ll.indexOfChild(rl);
            Button btn = new Button(ct);
            btn.setText(map.get(Constant.AD));
            btn.setBackgroundResource(R.drawable.btn_white);
            btn.setCompoundDrawablePadding(10);
            Tool.drawableLeftRightSet(btn, ct, R.drawable.ad_1, R.drawable.ad_2);
            ll.addView(btn, ind);

            if (map.containsKey(Constant.AD_ADDRESS)) {
                final HashMap<String, String> finalMap = map;
                btn.setOnClickListener(v -> Service.getC().openByIE(ct, finalMap.get(Constant.AD_ADDRESS)));

            } else if (map.containsKey(Constant.AD_TEXT)) {
                final HashMap<String, String> finalMap1 = map;
                btn.setOnClickListener(v -> {
                    SimpleTextDialog cl = new SimpleTextDialog(ct, finalMap1, SimpleTextDialog.AD);
                    cl.showDialog();
                });
            } else {
                btn.setEnabled(false);
            }
        }
    }

    /**
     * 作者及感谢
     */
    private void setAuthorTV() {
        TextView textView = binding.authorTV;
        textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        textView.setOnClickListener(v -> Tool.ShowDialog(ct, "作者及感谢", Constant.AUTHOR, R.drawable.wx2));
    }

    /**
     * 版本及更新历史
     */
    private void setUpdateHistoryBtn() {
        TextView textView = binding.tvVersion;
        textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        textView.setOnClickListener(v -> Tool.ShowDialog(ct, "更新历史", UpdateHistory.VERSION_HISTORY, -1));
    }
    //

    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(true);
        Tool.DialogSet(this);
        show();
    }
}
