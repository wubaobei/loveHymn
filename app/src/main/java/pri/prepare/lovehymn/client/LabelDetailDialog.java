package pri.prepare.lovehymn.client;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import java.util.List;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.I4Set;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.LabelDetailLayoutBinding;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.LabelType;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.function.CharConst;

public class LabelDetailDialog extends Dialog implements IShowDialog {
    private final LabelDetailLayoutBinding binding;

    public LabelDetailDialog(@NonNull Context context, int labelTypeSid, I4Set i4Set) {
        super(context);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.label_detail_layout, null, false);
        setContentView(binding.getRoot());
        getWindow().setBackgroundDrawable(new BitmapDrawable());

        try {
            LabelType lt = LabelType.getById(labelTypeSid);
            List<Hymn> hymnList = lt.getHymns();
            binding.labelNameTv.setText(CharConst.LABEL + lt.getName() + "(" + hymnList.size() + ")");
            Logger.info("ltName:" + lt.getName());
            int i = 0;
            for (Hymn h : hymnList) {
                TextView tv = new TextView(getContext());
                tv.setText(h.getShowName() + "\r\n" + h.getShortLyric());
                tv.setTextColor(Color.valueOf(1f, 1f, 1f).toArgb());
                tv.setBackgroundColor(cgColor(i++));
                binding.lableList.addView(tv);
                tv.setOnClickListener(v -> {
                    i4Set.loadPdfCall(h.getFile().getAbsolutePath());
                    dismiss();
                });
            }
            if (hymnList.size() == 0) {
                TextView tv = new TextView(getContext());
                tv.setText("该标签下还没有诗歌");
                tv.setTextColor(Color.valueOf(1f, 1f, 1f).toArgb());
                tv.setBackgroundColor(cgColor(i++));
                binding.lableList.addView(tv);
                binding.deleteLabel.setBackground(null);
                binding.deleteLabel.setOnLongClickListener(view -> {
                    Logger.info("deleteLabel "+lt.getName());
                    lt.delete(false);
                    dismiss();
                    return true;
                });
            } else {
                binding.deleteLabel.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private int cgColor(int index) {
        if (index % 2 == 0)
            return Color.argb(100, 70, 70, 70);
        return Color.argb(100, 130, 130, 130);
    }

    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(true);
        //Tool.DialogSet(this);
        //透明背景
        getWindow().setBackgroundDrawable(new ColorDrawable());
        show();
    }
}
