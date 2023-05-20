package pri.prepare.lovehymn.client;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import java.util.List;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.I4StopMp3;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.IntentHelper;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.SpecialMp3ListLayoutBinding;
import pri.prepare.lovehymn.server.entity.Book;
import pri.prepare.lovehymn.server.entity.LabelType;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;
import pri.prepare.lovehymn.server.function.CharConst;

public class SpecialMp3ListDialog extends Dialog implements IShowDialog {
    private final SpecialMp3ListLayoutBinding binding;

    public SpecialMp3ListDialog(@NonNull Activity activity, int bookSid, I4StopMp3 i4StopMp3) {
        super(activity);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.special_mp3_list_layout, null, false);
        setContentView(binding.getRoot());

        Book book = Book.getById(bookSid);
        binding.titleTv.setText(CharConst.BOOK + book.FullName);
        binding.buttonAll.setBackground(null);
        binding.buttonLabel.setBackground(null);
        binding.buttonNoLabel.setBackground(null);
        Intent intentAll = IntentHelper.create(activity, Mp3ListActivity.class, IntentHelper.TYPE_NORMAL, bookSid, false);
        setButtonText(binding.buttonAll, intentAll);
        binding.buttonAll.setOnClickListener(v -> {
            i4StopMp3.Stop();
            activity.startActivity(intentAll);
            dismiss();
        });
        Intent intentNoLabel = IntentHelper.create(activity, Mp3ListActivity.class, IntentHelper.TYPE_BOOK_NO_LABEL, bookSid, false);
        setButtonText(binding.buttonNoLabel, intentNoLabel);
        binding.buttonNoLabel.setOnClickListener(v -> {
            i4StopMp3.Stop();
            activity.startActivity(intentNoLabel);
            dismiss();
        });
        Intent intentLabel = IntentHelper.create(activity, Mp3ListActivity.class, IntentHelper.TYPE_BOOK_LABEL, bookSid, false);
        setButtonText(binding.buttonLabel, intentLabel);
        binding.buttonLabel.setOnClickListener(v -> {
            i4StopMp3.Stop();
            activity.startActivity(intentLabel);
            dismiss();
        });
        List<String> groups = LabelType.getAllGroups();
        if (groups.size() > 1) {
            for (String g : groups) {
                Button bt = new Button(getContext());
                bt.setText("标签组:" + g);
                bt.setBackground(null);
                binding.slLl.addView(bt);
                Intent intent = IntentHelper.create(activity, Mp3ListActivity.class, IntentHelper.TYPE_BOOK_LABEL_GROUP, bookSid, false, g,"");
                setButtonText(bt, intent);
                bt.setOnClickListener(v -> {
                    i4StopMp3.Stop();
                    activity.startActivity(intent);
                    dismiss();
                });
            }
        }
    }

    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(true);
        Tool.DialogSet(this);
        show();
    }

    private void setButtonText(Button btn, Intent intent) {
        IntentHelper helper = new IntentHelper(intent);
        String key = helper.key();

        String txt = btn.getText().toString();
        String cache = Setting.getValueS(Setting.MP3_COUNT_CACHE);
        for (String c : cache.split(";")) {
            String[] c2 = c.split(":");
            if (c2[0].equals(key)) {
                btn.setText(txt + "(" + c2[1] + ")");
                return;
            }
        }
        btn.setText(txt + UNKNOWN_COUNT);
    }

    private static final String UNKNOWN_COUNT = "(?)";
}
