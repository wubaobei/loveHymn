package pri.prepare.lovehymn.client;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.chip.Chip;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.QuickSettingLayoutBinding;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;

/**
 * 第一次进入app的快捷设置
 */
public class QucikSettingDialog extends Dialog implements IShowDialog {

    private final QuickSettingLayoutBinding binding;
    private final View.OnClickListener l = v -> {
        try {
            Chip c = (Chip) v;
            if (c.isChecked()) {
                c.setChecked(false);
                c.setText("否");
            } else {
                c.setChecked(true);
                c.setText("是");
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    };

    public QucikSettingDialog(@NonNull Context context, String msg) {
        super(context);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.quick_setting_layout, null, false);
        setContentView(binding.getRoot());
        try {
            binding.welcome.setText(msg);


            binding.okBtn.setOnClickListener(v -> {
                Setting.updateSetting(Setting.STATUS_BAR_SHOW, binding.radioButton.isChecked());

                Setting.updateSetting(Setting.SHOW_TIG, !binding.radioButton3.isChecked());

                Setting.updateSetting(Setting.SHOW_TOOL_BAR_ON_LOAD, binding.radioButton5.isChecked());

                this.dismiss();
            });
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(false);
        Tool.DialogSet(this);
        show();
    }
}
