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

        Tool.setListDialogLayout(binding.specialSettingTitle, context, R.drawable.special_setting
        );

        ct = context;
        btnSet();

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

    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(true);
        Tool.DialogSet(this);
        show();
    }
}
