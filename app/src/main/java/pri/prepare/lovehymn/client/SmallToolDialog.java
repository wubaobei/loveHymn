package pri.prepare.lovehymn.client;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.SmallToolDialogBinding;
import pri.prepare.lovehymn.server.entity.Logger;

public class SmallToolDialog extends Dialog implements IShowDialog {
    private final SmallToolDialogBinding binding;

    public SmallToolDialog(@NonNull Context context, Activity activity) {
        super(context);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.small_tool_dialog, null, false);
        setContentView(binding.getRoot());

        Tool.setListDialogLayout(binding.toolDialog, context, R.drawable.small_tool, binding.easySchedule, binding.dailyBible);

        binding.easySchedule.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(activity, EasyScheduleActivity.class);
                activity.startActivity(intent);
                SmallToolDialog.this.dismiss();
            } catch (Exception e) {
                Logger.exception(e);
            }
        });

        binding.dailyBible.setOnClickListener(view -> {
            try {
                Logger.info("db");
                Intent intent = new Intent(activity, DailyBibleActivity.class);
                activity.startActivity(intent);
                SmallToolDialog.this.dismiss();
            } catch (Exception e) {
                Logger.exception(e);
            }
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
