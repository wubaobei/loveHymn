package pri.prepare.lovehymn.client;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.I4Set;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.AllReadDialogBinding;

public class AllReadDialog extends Dialog implements IShowDialog {
    private final AllReadDialogBinding binding;

    public AllReadDialog(@NonNull Context context, Activity activity, I4Set i4Set) {
        super(context);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.all_read_dialog, null, false);
        setContentView(binding.getRoot());

        Tool.setListDialogLayout(binding.allReadTitle, context, R.drawable.s_4, binding.readMeBtn, binding.askanswerBtn, binding.addfriend, binding.addpackage);

        binding.askanswerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReadMeActivity.class);
            intent.putExtra("type", 2);
            getContext().startActivity(intent);
            dismiss();
        });

        binding.readMeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReadMeActivity.class);
            intent.putExtra("type", 1);
            getContext().startActivity(intent);
            dismiss();
        });

        binding.addfriend.setOnClickListener(v -> {
            i4Set.loadPdfCall(R.raw.addfriend + "");
            dismiss();
        });
        binding.addpackage.setOnClickListener(v -> {
            i4Set.loadPdfCall(R.raw.addpackage + "");
            dismiss();
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
