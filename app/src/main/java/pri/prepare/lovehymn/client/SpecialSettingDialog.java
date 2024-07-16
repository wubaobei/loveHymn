package pri.prepare.lovehymn.client;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.client.tool.enuCm;
import pri.prepare.lovehymn.databinding.SpecialSettingLayoutBinding;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.Setting;

public class SpecialSettingDialog extends Dialog implements IShowDialog {
    private final Context ct;

    private final SpecialSettingLayoutBinding binding;
    private Activity activity;

    public SpecialSettingDialog(@NonNull Context context, Activity activity) {
        super(context);

        this.activity = activity;
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.special_setting_layout, null, false);
        setContentView(binding.getRoot());

        Tool.setListDialogLayout(binding.specialSettingTitle, context, R.drawable.special_setting,binding.button3, binding.button4,
                binding.clearData, binding.addPdfBtn, binding.clearCache, binding.secretCode);

        ct = context;
        btnSet();
        setYbBtn();

        setAppInfoSetting((Activity) context);
    }

    /**
     * 清除app数据入口
     */
    private void setAppInfoSetting(Activity activity) {
        binding.clearData.setOnClickListener(v -> Tool.openInfo(activity));
    }

    private void btnSet() {
        //region 切换搜索结果的分隔符
        final Button b2 = binding.button3;
        int setting = Setting.getValueI(Setting.SEARCH_RESULT_SPLIT);
        final String setStr = Setting.SEARCH_RESULT_SPLIT_Arr[setting];
        b2.setText(Tool.getSpannableString("切换搜索的结果的分隔符 当前：" + setStr, new String[]{setStr}));
        b2.setOnClickListener(v -> {
            int setting1 = Setting.getValueI(Setting.SEARCH_RESULT_SPLIT);
            setting1 = (setting1 + 1) % Setting.SEARCH_RESULT_SPLIT_Arr.length;
            String setStr1 = Setting.SEARCH_RESULT_SPLIT_Arr[setting1];
            b2.setText(Tool.getSpannableString("切换搜索的结果的分隔符 当前：" + setStr1, new String[]{setStr1}));
            Setting.updateSetting(Setting.SEARCH_RESULT_SPLIT, setting1);
        });
        //endregion
        //region 隐藏状态栏
        final Button b3 = binding.button4;
        boolean setting2 = Setting.getValueB(Setting.STATUS_BAR_SHOW);
        b3.setText(Tool.getSpannableString("竖屏时的状态栏展示 当前：" + (setting2 ? "显示" : "隐藏"), new String[]{"显示", "隐藏"}));
        b3.setOnClickListener(v -> {
            boolean setting21 = Setting.getValueB(Setting.STATUS_BAR_SHOW);
            setting21 = !setting21;
            b3.setText(Tool.getSpannableString("竖屏时的状态栏展示 当前：" + (setting21 ? "显示" : "隐藏"), new String[]{"显示", "隐藏"}));
            Setting.updateSetting(Setting.STATUS_BAR_SHOW, setting21);
        });
        //endregion
        //region 自定义pdf
        binding.addPdfBtn.setOnClickListener(v -> {
            Activity act = (Activity) ct;
            Intent intent = new Intent(act, AddPdfActivity.class);
            act.startActivity(intent);
            SpecialSettingDialog.this.dismiss();
        });
        //endregion
        //region 清理缓存及过期文件
        binding.clearCache.setOnClickListener(v -> Toast.makeText(getContext(), Service.getC().clearCache(), Toast.LENGTH_LONG).show());
        //endregion
        //region 神秘代码
        binding.secretCode.setOnClickListener(v -> {
            CommonDialog cd = new CommonDialog(getContext(), enuCm.SECRET_CODE, null, activity);
            cd.showDialog();
        });
        //endregion
    }

    /**
     * 异步功能
     */
    private void setYbBtn() {
        final Button btn = binding.closeYb;
        boolean set = Setting.getValueB(Setting.USE_ASYNC);
        String[] oc=new String[]{"开启","关闭"};
        btn.setText(Tool.getSpannableString("异步功能 当前："+(set?oc[0]:oc[1]) ,oc));

        btn.setOnClickListener(v -> {
            boolean set1 = Setting.getValueB(Setting.USE_ASYNC);
            set1 = !set1;
            btn.setText(Tool.getSpannableString("异步功能 当前："+(set1?oc[0]:oc[1]) ,oc));
            Setting.updateSetting(Setting.USE_ASYNC, set1);
        });
    }
    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(true);
        Tool.DialogSet(this);
        show();
    }
}
