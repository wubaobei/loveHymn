package pri.prepare.lovehymn.client;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import java.util.HashMap;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.SignSettingLayoutBinding;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;

public class SignSettingDialog extends Dialog implements IShowDialog {
    private final SignSettingLayoutBinding binding;

    public SignSettingDialog(@NonNull Context context) {
        super(context);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.sign_setting_layout, null, false);
        setContentView(binding.getRoot());

        Tool.drawableLeftSet(binding.specialSettingTitle, context, R.drawable.finger);

        binding.oneFingerLr.setEnabled(false);
        binding.twoFingerLr.setEnabled(false);
        binding.longClick.setEnabled(false);
        final int[] options1 = new int[]{Setting.CATELOG_QUICK, Setting.DETAIL_QUICK, Setting.MP3_PLAY_QUICK};
        final int[] options2 = new int[]{Setting.COLEECT_QUICK, Setting.LABLE_COLLECT_QUICK};
        HashMap<Integer, String> os = new HashMap<>();
        os.put(Setting.CATELOG_QUICK, "快捷方式：目录");
        os.put(Setting.COLEECT_QUICK, "快捷方式：收藏");
        os.put(Setting.DETAIL_QUICK, "快捷方式：详情");
        os.put(Setting.LABLE_COLLECT_QUICK, "标签收藏栏");
        os.put(Setting.MP3_PLAY_QUICK, "播放列表");
        //final String[] os = new String[]{"快捷方式：目录", "快捷方式：收藏", "快捷方式：详情","标签收藏栏"};
        int tu = Setting.getValueI(Setting.DOUBLE_FINGER_UP);
        binding.twoFingerUpText.setText(os.get(tu));
        int td = Setting.getValueI(Setting.DOUBLE_FINGER_DOWN);
        binding.twoFingerDownText.setText(os.get(td));
        binding.twoFingerUp.setOnClickListener(v -> {
            try {
                int c = Setting.getValueI(Setting.DOUBLE_FINGER_UP);
                int ci = indOfArr(options1, c);
                ci = (ci + 1) % options1.length;
                c = options1[ci];
                Setting.updateSetting(Setting.DOUBLE_FINGER_UP, c);
                binding.twoFingerUpText.setText(os.get(c));
            } catch (Exception e) {
                Logger.exception(e);
            }
        });
        binding.twoFingerDown.setOnClickListener(v -> {
            try {
                int c = Setting.getValueI(Setting.DOUBLE_FINGER_DOWN);
                int ci = indOfArr(options2, c);
                ci = (ci + 1) % options2.length;
                c = options2[ci];
                Setting.updateSetting(Setting.DOUBLE_FINGER_DOWN, c);
                binding.twoFingerDownText.setText(os.get(c));
            } catch (Exception e) {
                Logger.exception(e);
            }
        });
    }

    private int indOfArr(int[] arr, int v) {
        for (int i = 0; i < arr.length; i++)
            if (v == arr[i]) {
                return i;
            }
        return -1;
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
