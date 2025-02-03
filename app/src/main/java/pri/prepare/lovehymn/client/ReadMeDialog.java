package pri.prepare.lovehymn.client;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import java.io.File;
import java.lang.ref.WeakReference;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.DisplayStat;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.ReadMeDialogBinding;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.function.SdCardTool;

public class ReadMeDialog extends Dialog implements IShowDialog {
    private final ReadMeDialogBinding binding;
    private final LoadRes re;
    private final int MAXTIME;

    private Activity activity;

    public ReadMeDialog(@NonNull Activity activity, LoadRes loadRes) {
        super(activity);
        this.activity = activity;
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.read_me_dialog, null, false);
        setContentView(binding.getRoot());
        int len=loadRes.readMe.length();
        if(len>1000){
            MAXTIME=30000;
        }else if(len>500){
            MAXTIME=10000;
        }else{
            MAXTIME=3000;
        }

        binding.readText.setText(loadRes.readMe.replace("\\n", "\r\n"));

        re = loadRes;

        binding.okBtn.setOnClickListener(v -> load());

        st = System.currentTimeMillis();
        if (handlerSend == null) {
            handlerSend = new MyHandler(this);
        }
        handlerSend.sendEmptyMessageDelayed(1, 250);
    }

    private final long st;

    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(false);
        Tool.DialogSet(this);
        show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 设置宽度为屏宽, 靠近屏幕底部。
        Window win = this.getWindow();
        // 一定要设置Background，如果不设置，window属性设置无效
        //win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = win.getAttributes();
        //params.gravity = Gravity.BOTTOM;
        // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        win.setAttributes(params);
    }

    private void load() {
        try {
            //按照设计，到这里应该已经解压了
            if (!re.isDir) {
                Service.getC().unzipToLb(new File(re.path), false, true);
            }
            Service.getC().loadPrivateResFromLoadFile(new File(SdCardTool.getResPath() + File.separator + "load-" + re.shortName + ".txt"));
            toast("加载《" + re.fullNane + "》完成");
            dismiss();
        } catch (Exception e) {
            Logger.exception(e);
            toast("加载《" + re.fullNane + "》失败：" + e.getMessage());
        }
    }

    private void toast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

    private MyHandler handlerSend = null;

    private static class MyHandler extends Handler {
        private final WeakReference<ReadMeDialog> reference;

        @SuppressWarnings("deprecation")
        public MyHandler(ReadMeDialog activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            ReadMeDialog t = reference.get();
            if (t != null) {
                try {
                    super.handleMessage(msg);
                    //todo
                    t.updateInHandle();

                    if (msg.what == 1) {
                        // 再次使用handler发送信息
                        int n = 1000 / DisplayStat.HZ;
                        t.handlerSend.sendEmptyMessageDelayed(1, n);
                    }
                } catch (Exception e) {
                    Logger.exception(e);
                }
            }
        }
    }

    private void updateInHandle() {
        if (binding.okBtn.isEnabled()) {
            return;
        }
        long t = System.currentTimeMillis() - st;
        if (t > MAXTIME) {
            binding.okBtn.setEnabled(true);
            binding.okBtn.setText("确定");
        } else {
            binding.okBtn.setText("已阅读" + (t / 1000) + "秒");
        }
    }
}