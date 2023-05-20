package pri.prepare.lovehymn.client;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import java.util.ArrayList;
import java.util.List;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.I4StopMp3;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.IntentHelper;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.CommonListDialogBinding;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.Label;
import pri.prepare.lovehymn.server.entity.LabelType;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;
import pri.prepare.lovehymn.server.function.CharConst;

public class CommonListDialog extends Dialog implements IShowDialog {
    private final CommonListDialogBinding binding;

    private int _type;

    public CommonListDialog(@NonNull Context context, int type, List<String> contents, I4StopMp3 i4StopMp3) {
        this(context, type, contents, i4StopMp3, -1, null);
    }

    public CommonListDialog(@NonNull Context context, int type, List<String> contents, I4StopMp3 i4StopMp3, Hymn currentHymn) {
        this(context, type, contents, i4StopMp3, -1, currentHymn);
    }

    public CommonListDialog(@NonNull Context context, int type, List<String> contents, I4StopMp3 i4StopMp3, int res) {
        this(context, type, contents, i4StopMp3, res, null);
    }

    public CommonListDialog(@NonNull Context context, int type, List<String> contents, I4StopMp3 i4StopMp3, int res, Hymn currentHymn) {
        super(context);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.common_list_dialog, null, false);
        setContentView(binding.getRoot());
        createTime = System.currentTimeMillis();
        _type = type;
        if (type == 1) {
            //currentHymn.getMp3File()
            binding.title.setText("播放列表：");
            if (!Setting.getValueB(Setting.USE_ASYNC))
                binding.title2.setText("注意：由于关闭了异步功能，点击后后卡一会");
            int n = 0;
            for (String c : contents) {
                String path = currentHymn == null ? "" : currentHymn.getMp3File().getAbsolutePath();
                Button tv = new Button(context);
                tv.setGravity(Gravity.CENTER_VERTICAL);
                tv.setBackgroundColor(Color.argb(0, 0, 0, 0));
                tv.setTextSize(18.0f);
                String temp;
                String sid;
                sid = c.split(";")[0];
                temp = (sid.startsWith("-") ? CharConst.LABEL : CharConst.BOOK) + c.split(";")[1];

                tv.setText(temp);
                Activity act = (Activity) context;
                Intent intent = IntentHelper.create(act, Mp3ListActivity.class, IntentHelper.TYPE_NORMAL, Math.abs(Integer.parseInt(sid)), Integer.parseInt(sid) < 0, "a", path);
                loadCount(tv, new IntentHelper(intent).key());
                if (n++ % 2 == 0)
                    binding.listLl1.addView(tv);
                else
                    binding.listLl2.addView(tv);
                tv.setOnClickListener(v -> {
                    try {
                        i4StopMp3.Stop();
                        act.startActivity(intent);
                        CommonListDialog.this.dismiss();
                    } catch (Exception e) {
                        Logger.exception(e);
                    }
                });
                if (!sid.startsWith("-")) {
                    tv.setOnLongClickListener(v -> {
                        SpecialMp3ListDialog dialog = new SpecialMp3ListDialog((Activity) context, Integer.parseInt(sid), i4StopMp3);
                        dialog.showDialog();
                        dismiss();
                        return true;
                    });
                }
            }
            //binding.refreshBtn.setBackground(null);
        } else if (type == 2) {
            Hymn hymn = Hymn.getById(Integer.parseInt(contents.get(0)));
            binding.title.setText(hymn.getShortShowName() + "标签：");
            int n = 0;
            LabelType[] types = LabelType.getAll();
            for (LabelType t : types) {
                Button tv = new Button(context);
                tv.setGravity(Gravity.CENTER_VERTICAL);
                tv.setBackgroundColor(Color.argb(0, 0, 0, 0));
                tv.setTextSize(18.0f);
                tv.setText(t.getName());
                boolean hasL = Label.hasLabel(hymn, t.getId());
                Tool.drawableLeftSet(tv, getContext(), hasL ? R.drawable.label_yes : R.drawable.label_no);
                if (n++ % 2 == 0)
                    binding.listLl1.addView(tv);
                else
                    binding.listLl2.addView(tv);
                tv.setOnClickListener(v -> {
                    boolean hasL2 = Label.hasLabel(hymn, t.getId());
                    Tool.drawableLeftSet(tv, getContext(), !hasL2 ? R.drawable.label_yes : R.drawable.label_no);
                    Label.mod(hymn, t.getId(), !hasL2);
                });
            }
            new Thread(timerR).start();
        } else if (type == 3) {
            //显示多行文本
            binding.title.setText(contents.get(0));
            for (int i = 1; i < contents.size(); i++) {
                CharSequence cs = Tool.toColorSpan(contents.get(i), Color.RED);
                TextView tv = new TextView(context);
                tv.setText(cs);
                tv.setPadding(10, 10, 10, 10);
                binding.listLl1.addView(tv);
            }
            if (res != -1) {
                ImageView iv = new ImageView(getContext());
                iv.setAdjustViewBounds(true);
                iv.setImageResource(res);
                binding.listLl1.addView(iv);
            }
        }
    }


    private static final String UNKNOWN_COUNT = "(?)";

    private void loadCount(Button tv, String key) {
        String txt = tv.getText().toString();
        String cache = Setting.getValueS(Setting.MP3_COUNT_CACHE);
        for (String c : cache.split(";")) {
            String[] c2 = c.split(":");
            if (c2[0].equals(key)) {
                tv.setText(txt + "(" + c2[1] + ")");
                return;
            }
        }
        tv.setText(txt + UNKNOWN_COUNT);
    }

    private static long createTime;
    final Runnable timerR = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Logger.exception(e);
                }
                if (System.currentTimeMillis() - createTime > 10000)
                    if (_type == 2) {
                        dismiss();
                    }
            }
        }
    };

    @Override
    public void showDialog() {
        if (_type == 3) {
            Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
            //设置触摸对话框以外的地方取消对话框
            setCanceledOnTouchOutside(true);
            Tool.DialogSet(this);
            show();
            return;
        }

        Window window = getWindow();
        if (window != null) {
            Tool.setAnim(window, Tool.ANIM_BOTTOM);
            window.setGravity(Gravity.BOTTOM);
            window.setBackgroundDrawableResource(R.drawable.mydialogdown);
        }
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(true);
        //Tool.DialogSet(this);

        show();
    }
}
