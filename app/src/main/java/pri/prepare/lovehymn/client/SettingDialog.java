package pri.prepare.lovehymn.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.I4Set;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.SettingLayoutBinding;
import pri.prepare.lovehymn.server.UpdateHistory;
import pri.prepare.lovehymn.server.entity.Book;
import pri.prepare.lovehymn.server.function.Constant;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;

public class SettingDialog extends Dialog implements IShowDialog {
    private final Context ct;
    private final I4Set iTell;
    private final WindowManager _wm;

    private final SettingLayoutBinding binding;
    private final Activity activity;

    @SuppressLint("SetTextI18n")
    public SettingDialog(@NonNull Activity activity, I4Set tell, WindowManager wm) {
        super(activity);
        this.activity = activity;
        ct = activity;
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.setting_layout, null, false);
        setContentView(binding.getRoot());
        TextView tvv = binding.tvVersion;
        tvv.setText("当前版本：" + Service.getC().getVersionStr(ct));
        Service.getC().checkVersion(activity);
        iTell = tell;
        _wm = wm;
        setStatisticBtn();
        setLoadResBtn();
        setUpdateHistoryBtn();
        setAuthorTV();
        setSettingIcons();
        setDisappearBtn();
        setUpdateBtn();
        setSpecialSetting();
        setLabelSetting();
        setAllRead();
        setShowToolBarSetting();
        setStatusBarSetting();
        setScreenCastingModeSetting();
        setDictSetting();
        setTigSetting();
        setSignSetting();
    }

    private void commonSwitchSet(ImageButton img, int setId) {
        if (Setting.getValueB(setId)) {
            img.setImageResource(R.drawable.on);
        } else {
            img.setImageResource(R.drawable.off);
        }

        img.setOnClickListener(v -> {
            Logger.info("set " + setId);
            boolean b = Setting.getValueB(setId);
            b = !b;
            Setting.updateSetting(setId, b);

            img.setImageResource(b ? R.drawable.on : R.drawable.off);
        });
    }

    private static final int[] disappearTimeArr = new int[]{9999000, 2000, 4000, 6000};

    public static int getInitDisappearTime() {
        return disappearTimeArr[disappearTimeArr.length - 1];
    }

    private String getText(int t) {
        if (t == disappearTimeArr[0])
            return "不自动隐藏";
        return (t / 1000d) + "秒";
    }

    /**
     * 设置图标
     */
    private void setSettingIcons() {
        int[] btnId = new int[]{R.id.resStatBtn, R.id.all_read,
                R.id.dispearTime, R.id.downloadAddressBtn,
                R.id.specialSettingBtn,
                R.id.lableBtn, R.id.showstatusbar, R.id.screenCastingMode,
                R.id.showtoolbar, R.id.dict_show, R.id.close_tig_btn, R.id.signSettingBtn, R.id.loadResBtn};
        int[] dId = new int[]{R.drawable.s_2, R.drawable.s_4,
                R.drawable.s_5, R.drawable.s_6,
                R.drawable.special_setting,
                R.drawable.label_icon, R.drawable.statuslan, R.drawable.screen_casting_mode,
                R.drawable.lan, R.drawable.spz, R.drawable.gth2, R.drawable.finger, R.drawable.load};

        for (int i = 0; i < btnId.length; i++) {
            Button btn1 = findViewById(btnId[i]);
            Tool.drawableLeftSet(btn1, ct, dId[i]);
        }
    }

    /**
     * 渐隐时间设置
     */
    private void setDisappearBtn() {
        Button btn = binding.dispearTimeBtn;
        int v = Setting.getValueI(Setting.DISAPPEAR_TIME);
        btn.setText(getText(v));

        btn.setOnClickListener(v1 -> {
            int v2 = Setting.getValueI(Setting.DISAPPEAR_TIME);
            int ind = 0;
            for (int i = 0; i < disappearTimeArr.length; i++)
                if (disappearTimeArr[i] == v2) {
                    ind = i;
                    break;
                }
            ind++;
            if (ind >= disappearTimeArr.length)
                ind = 0;
            v2 = disappearTimeArr[ind];
            Setting.updateSetting(Setting.DISAPPEAR_TIME, v2);
            btn.setText(getText(v2));
        });
    }

    /**
     * 默认显示工具栏
     */
    private void setShowToolBarSetting() {
        commonSwitchSet(binding.showtoolbarSw, Setting.SHOW_TOOL_BAR_ON_LOAD);
        binding.showtoolbarHelp.setOnClickListener(v -> toast("在打开pdf和切换pdf时是否显示工具栏"));
    }

    private void setStatusBarSetting() {
        commonSwitchSet(binding.showstatusbarSw, Setting.STATUS_BAR_SHOW);
    }

    private void setScreenCastingModeSetting() {
        commonSwitchSet(binding.screenCastingModeSw, Setting.SCREEN_CASTING_MODE);
        binding.screenCastingModeHelp.setOnClickListener(v -> toast("一般模式下，pdf是拖动上滑下滑；投影模式下，pdf是点击左右两边而上一页下一页"));
    }

    private void toast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
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
     * 生僻字拼音
     */
    private void setDictSetting() {
        commonSwitchSet(binding.dictShowSw, Setting.SHOW_DICT);
    }

    /**
     * 资源统计
     */
    private void setStatisticBtn() {
        binding.resStatBtn.setOnClickListener(v -> Tool.ShowDialog(ct, "资源统计", Service.getC().getResStatString(), -1));
    }

    /**
     * 加载资源按钮
     */
    private void setLoadResBtn() {
        binding.loadResBtn.setOnClickListener(v -> { //搜索资源
            //由于一般的附加包在打开时已经加载了，所以这里只加载其他的一些资源
//            try {
//                String t = Service.getC().autoLoadOtherRes();
//                Tool.ShowDialog(getContext(), "加载完成", t);
//            } catch (Exception e) {
//                Logger.exception(e);
//                Tool.ShowDialog(getContext(), "加载异常", e.getMessage());
//            }
            LoadResDialog dialog=new LoadResDialog(getContext());
            dialog.showDialog();
        });
//        binding.loadResBtn.setOnLongClickListener(v -> {
//            for (Book privateBook : Book.getPrivateBooks()) {
//                privateBook.delete();
//            }
//            Tool.ShowDialog(getContext(), "提示", "已删除附加的诗歌本。如果是误删除，请再次点击‘加载资源按钮’");
//            return true;
//        });
    }

    /**
     * 下载（更新）地址
     */
    private void setUpdateBtn() {
        Button btn = binding.downloadAddressBtn;

        btn.setEnabled(true);

        btn.setOnClickListener(v -> {
            try {
                SimpleTextDialog cl = new SimpleTextDialog(ct, new HashMap<>(), SimpleTextDialog.DOWNLOAD);
                cl.showDialog();

            } catch (Exception e) {
                Logger.exception(e);
                Tool.toastRestart(getContext(), e);
            }
        });
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
            final AlertDialog.Builder alterDialog = new AlertDialog.Builder(getContext());
            alterDialog.setTitle("警告");
            alterDialog.setMessage("由于种种原因，该app的许多功能用户都不会使用，因此添加了" + Constant.TIPS + "功能。如果你确定你会使用大部分功能，可以关闭自动弹窗，" +
                    "使app更简洁清爽。关闭后，除非你清空app数据或重装app，这些" + Constant.TIPS + "不会再自动弹出。另外，可以在'" + Constant.READ_ME + "'中查看所有" + Constant.TIPS);
            alterDialog.setPositiveButton("我知道了，确定关闭", (dialog, which) -> {
                Setting.updateSetting(Setting.SHOW_TIG, false);
                binding.closeTigBtn.setVisibility(View.GONE);
            });
            alterDialog.show();
        });
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
        textView.setOnClickListener(v -> Tool.ShowDialog(ct, "更新历史", UpdateHistory.getVersionHistory(activity), -1));
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
