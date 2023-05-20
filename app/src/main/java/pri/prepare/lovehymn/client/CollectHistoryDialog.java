package pri.prepare.lovehymn.client;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.HistoryTool;
import pri.prepare.lovehymn.client.tool.I4LC;
import pri.prepare.lovehymn.client.tool.I4Set;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.DialogGnBinding;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.Setting;
import pri.prepare.lovehymn.server.function.CollectTool;
import pri.prepare.lovehymn.server.function.Constant;

/**
 * 收藏与浏览历史(长按)
 */
public class CollectHistoryDialog extends Dialog implements IShowDialog {
    private final DialogGnBinding binding;
    private final I4LC _i4lc;
    MyFile myFile;
    private boolean loadPdf = false;

    public CollectHistoryDialog(@NonNull Context context, MyFile file, Hymn hymn, I4Set i4Set, I4LC i4LC) {
        super(context);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_gn, null, false);
        _i4lc = i4LC;
        myFile = file;
        setContentView(binding.getRoot());
        //白版
        if (hymn != null && hymn.getBook() != null)
            setButtonEnable(hymn.getBook().getWhiteFile(hymn.getWhitePdf()) != null && hymn.getWhitePage() > 0, binding.button7);
        else
            setButtonEnable(false, binding.button7);
        //分享MP3
        setButtonEnable(file.getMp3() != null, binding.button9);

        binding.button7.setOnClickListener(v -> {
            try {
                File ff = hymn.getBook().getWhiteFile(hymn.getWhitePdf());
                if (ff != null && hymn.getWhitePage() > 0) {
                    Intent intent = new Intent(getContext(), WhiteActivity.class);
                    intent.putExtra("path", ff.getAbsolutePath());
                    intent.putExtra("page", hymn.getWhitePage() - 1);
                    getContext().startActivity(intent);
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "未找到白版", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.i("ppp", e.getMessage());
            }
        });

        binding.button8.setOnClickListener(v -> i4Set.Share(file, false));
        binding.button9.setOnClickListener(v -> i4Set.Share(file, true));
        binding.textView7.setOnClickListener(v -> {
            isCollect = !isCollect;
            Setting.updateSetting(Setting.COLLECT_HISTORY_LAST_CHOOSE, isCollect ? 0 : 1);
            showCH();
        });
        binding.buttonCH.setOnClickListener(v -> {
            isCollect = !isCollect;
            Setting.updateSetting(Setting.COLLECT_HISTORY_LAST_CHOOSE, isCollect ? 0 : 1);
            showCH();
        });
        isCollect = Setting.getValueI(Setting.COLLECT_HISTORY_LAST_CHOOSE) == 0;
        showCH();
        MyFile[] fs = CollectTool.getCollects();
        for (int i = fs.length - 1; i >= 0; i--) {
            MyFile f = fs[i];
            Hymn h = f.getHymn();
            TextView tv = new TextView(getContext());
            tv.setText(h.getShowName() + "\r\n" + h.getShortLyric());
            tv.setTextColor(Color.valueOf(1f, 1f, 1f).toArgb());
            tv.setBackgroundColor(cgColor(i));
            binding.colLl.addView(tv);
            tv.setOnClickListener(v -> {
                i4Set.loadPdfCall(f.getAbsolutePath());
                loadPdf = true;
                CollectHistoryDialog.this.dismiss();
            });
        }
        if (fs.length == 0) {
            TextView tv = new TextView(getContext());
            tv.setText(Constant.COLLECT_TIG);
            tv.setTextColor(Color.valueOf(1f, 1f, 1f).toArgb());
            tv.setBackgroundColor(cgColor(0));
            binding.colLl.addView(tv);
        }

        fs = HistoryTool.getHistories();
        for (int i = fs.length - 1; i >= 0; i--) {
            MyFile f = fs[i];
            Hymn h = f.getHymn();
            if (h == null || (hymn != null && h.getShowName().equals(hymn.getShowName())))
                continue;
            TextView tv = new TextView(getContext());
            tv.setText(h.getShowName() + "\r\n" + h.getShortLyric());
            tv.setTextColor(Color.valueOf(1f, 1f, 1f).toArgb());
            tv.setBackgroundColor(cgColor(i));
            binding.hisLl.addView(tv);
            tv.setOnClickListener(v -> {
                i4Set.loadPdfCall(f.getAbsolutePath());
                loadPdf = true;
                CollectHistoryDialog.this.dismiss();
            });
        }

        binding.deleteAllBtn.setOnLongClickListener(v -> {
            if (isCollect) {
                if (CollectTool.getCollects().length > 0) {
                    CollectTool.clearAll();
                    mod = I4LC.CLEAR_COLLECT;
                    dismiss();
                } else
                    Toast.makeText(getContext(), "没有任何收藏", Toast.LENGTH_SHORT).show();
            } else {
                if (HistoryTool.getHistories().length > 0) {
                    HistoryTool.clearAll();
                    mod = I4LC.CLEAR_HISTORY;
                    dismiss();
                } else
                    Toast.makeText(getContext(), "没有任何历史记录", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void setButtonEnable(boolean bool, Button btn) {
        btn.setEnabled(bool);
        if (!bool)
            btn.setTextColor(Color.GRAY);
    }

    private int cgColor(int index) {
        if (index % 2 == 0)
            return Color.argb(100, 70, 70, 70);
        return Color.argb(100, 130, 130, 130);
    }

    private void showCH() {
        binding.textView7.setText(isCollect ? "收藏夹" : "浏览历史");
        binding.gnllC.setVisibility(isCollect ? View.VISIBLE : View.GONE);
        binding.gnllH.setVisibility(isCollect ? View.GONE : View.VISIBLE);
    }

    private boolean isCollect = true;

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

    private int mod = 0;

    @Override
    protected void onStop() {
        super.onStop();
        if (!loadPdf)
            _i4lc.updateTitle(myFile, mod);
    }
}
