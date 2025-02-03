package pri.prepare.lovehymn.client;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.DisplayStat;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.LoadResLayoutBinding;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.Logger;

public class LoadResDialog extends Dialog implements IShowDialog {
    private final LoadResLayoutBinding binding;
    private final Activity activity;

    public LoadResDialog(@NonNull Activity activity) {
        super(activity);
        this.activity = activity;
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.load_res_layout, null, false);
        setContentView(binding.getRoot());
        getWindow().setBackgroundDrawable(new BitmapDrawable());

        try {
            if (hasZipUnzip()) {
                TextView tv = new TextView(getContext());
                tv.setText("发现有未解压的综合包，点击下面按钮开始解压并耐心等待");
                binding.resList.addView(tv);
                Button btn = new Button(getContext());
                btn.setText("开始解压");
                binding.resList.addView(btn);

                btn.setOnClickListener(v -> {
                    btn.setText("解压中");
                    btn.setEnabled(false);
                    isUnZipping = true;
                    needUpdate = true;
                    new Thread(unZip).start();
                });

                //检查解压状态
                if (handlerSend == null) {
                    handlerSend = new MyHandler(this);
                }
                handlerSend.sendEmptyMessageDelayed(1, 250);
            } else {
                loadResList();
            }
        } catch (IOException e) {
            binding.textView4.setText("出错了，请联系开发者：" + e.getMessage());
        }
    }

    private boolean needUpdate = false;
    private boolean isUnZipping = false;

    final Runnable unZip = () -> {
        try {
            List<LoadRes> res = Service.getC().loadResList();
            for (LoadRes re : res) {
                if (!re.isDir && !re.load) {
                    //覆盖性解压，为了覆盖原先的青年诗歌白板
                    Service.getC().unzipToLb(new File(re.path), false, true);
                }
            }
            isUnZipping = false;
        } catch (Exception e) {
            Logger.exception(e);
        }
    };

    /**
     * 发现有压缩包未解压
     */
    private boolean hasZipUnzip() throws IOException {
        List<LoadRes> res = Service.getC().loadResList();
        return res.stream().anyMatch(x -> !x.isDir && !x.load);
    }

    private void loadResList() throws IOException {
        List<LoadRes> res = Service.getC().loadResList();
        for (LoadRes re : res) {
            LinearLayout l = new LinearLayout(getContext());
            binding.resList.addView(l);
            TextView tv = new TextView(getContext());
            tv.setAllCaps(false);
            if (re.isDir) {
                tv.setText("发现资源" + re.shortName + "《" + re.fullNane + "》");
            } else {
                tv.setText("发现压缩包资源" + re.shortName + "《" + re.fullNane + "》");
            }
            l.addView(tv);
            Button btn = new Button(getContext());
            l.addView(btn);
            if (re.load) {
                btn.setText("已加载");
                btn.setEnabled(false);
            } else {
                if (re.isDir) {
                    btn.setText("开始加载");
                } else {
                    btn.setText("解压并加载");
                }
                btn.setOnClickListener(v -> {
                    new ReadMeDialog(activity, re).showDialog();
                    dismiss();
                });
            }
        }
    }

    private void toast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(true);
        Tool.DialogSet(this);
        show();
    }

    private MyHandler handlerSend = null;

    private static class MyHandler extends Handler {
        private final WeakReference<LoadResDialog> reference;

        @SuppressWarnings("deprecation")
        public MyHandler(LoadResDialog activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            LoadResDialog t = reference.get();
            if (t != null) {
                try {
                    super.handleMessage(msg);

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
        if (needUpdate && !isUnZipping) {
            //解压完成了
            binding.resList.removeAllViews();
            try {
                loadResList();
                needUpdate = false;
            } catch (IOException e) {
                binding.textView4.setText("出错了，请联系开发者：" + e.getMessage());
            }
        }
    }
}
