package pri.prepare.lovehymn.client;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.client.tool.enuCm;
import pri.prepare.lovehymn.databinding.SpecialSettingLayoutBinding;
import pri.prepare.lovehymn.server.Service;

public class SpecialSettingDialog extends Dialog implements IShowDialog {
    private final Context ct;

    private final SpecialSettingLayoutBinding binding;
    private final Activity activity;

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

        binding.clearZip.setOnClickListener(v -> Toast.makeText(getContext(), Service.getC().clearZip(), Toast.LENGTH_LONG).show());

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
