package pri.prepare.lovehymn.client;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.LoadResLayoutBinding;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.function.SdCardTool;

public class LoadResDialog extends Dialog implements IShowDialog {
    private LoadResLayoutBinding binding;

    public LoadResDialog(@NonNull Context context) {
        super(context);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.load_res_layout, null, false);
        setContentView(binding.getRoot());
        getWindow().setBackgroundDrawable(new BitmapDrawable());

        try {
            loadResList();
        } catch (IOException e) {
            binding.textView4.setText("出错了，请联系开发者：" + e.getMessage());
        }
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
                    new ReadMeDialog(getContext(), re).showDialog();
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
}
