package pri.prepare.lovehymn.client;

import android.app.Dialog;
import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.server.function.Constant;

public class SimpleTextDialog extends Dialog implements IShowDialog {
    public static final int AD = 1;
    public static final int DOWNLOAD = 2;

    public SimpleTextDialog(@NonNull Context context, HashMap<String, String> map, int i) {
        super(context);
        setContentView(R.layout.download_layout);

        EditText tv = findViewById(R.id.download_et);
        TextView title = findViewById(R.id.textView);

        if (i == DOWNLOAD) {
            if (map.containsKey(Constant.LINK) && map.containsKey(Constant.PWD)) {
                ArrayList<String> list = new ArrayList<>();
                list.add("请通过百度网盘app下载");
                list.add("可以长按复制");
                list.add("下载链接");
                list.add(map.get(Constant.LINK));
                list.add("提取码");
                list.add(map.get(Constant.PWD));
                list.add("如果你发现链接失效了，请联系作者更新");
                list.add("可以截图扫描加网盘好友，或查看说明文档");
                //list.add("这个");
                tv.setText(String.join("\r\n", list));
            }
        } else if (i == AD) {
            title.setText(map.get(Constant.AD));
            tv.setText(map.get(Constant.AD_TEXT));
        }
    }

    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(),Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(true);
        Tool.DialogSet(this);
        show();
    }
}
