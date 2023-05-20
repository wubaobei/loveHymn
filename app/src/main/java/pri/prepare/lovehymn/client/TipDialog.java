package pri.prepare.lovehymn.client;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.TipStruct;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.TipDialogBinding;

public class TipDialog extends Dialog implements IShowDialog {
    private final TipDialogBinding binding;

    public TipDialog(@NonNull Context context, TipStruct tip) {
        super(context);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.tip_dialog, null, false);
        setContentView(binding.getRoot());

        if (tip.resId != null) {
            ImageView iv = new ImageView(getContext());
            iv.setPadding(10, 10, 10, 10);
            iv.setImageResource(tip.resId);
            iv.setAdjustViewBounds(true);
            ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            iv.setLayoutParams(lp);
            binding.tipLl.addView(iv);
        }
        if (tip.text != null && tip.text.length() > 0) {
            TextView tv = new TextView(getContext());
            tv.setText(tip.text);
            binding.tipLl.addView(tv);
        }

        binding.closeTigBtn.setOnClickListener(v -> dismiss());
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
