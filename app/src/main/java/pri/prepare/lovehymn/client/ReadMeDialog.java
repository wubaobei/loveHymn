package pri.prepare.lovehymn.client;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import java.io.File;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.ReadMeDialogBinding;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.function.SdCardTool;

public class ReadMeDialog extends Dialog implements IShowDialog {
    private ReadMeDialogBinding binding;
    private LoadRes re;

    public ReadMeDialog(@NonNull Context context, LoadRes loadRes) {
        super(context);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.read_me_dialog, null, false);
        setContentView(binding.getRoot());

        binding.readText.setText(loadRes.readMe);

        re = loadRes;

        binding.okBtn.setOnClickListener(v -> load());
    }

    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(false);
        Tool.DialogSet(this);
        show();
    }

    private void load() {
        try {
            if (!re.isDir) {
                Service.getC().unzipToLb(new File(re.path));
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
}
