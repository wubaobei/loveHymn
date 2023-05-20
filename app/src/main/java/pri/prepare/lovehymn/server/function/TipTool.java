package pri.prepare.lovehymn.server.function;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.TipStruct;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.server.entity.Setting;

public class TipTool {
    //20220111 V1.5.6
    //20220302 添加tip14
    //20220311 添加tip15
    //20220430 添加tip16，部分文字修改
    //20220513 添加tip18 标签组
    //20220521 添加tip19 shortcut;更新tip15
    private static TipStruct[] commonTig = new TipStruct[]{
            new TipStruct(R.drawable.tip1, "点击标题栏的标题可收藏、分享等"),
            new TipStruct(R.drawable.tip2, "目录中可搜索歌词，作者，序号等"),
            new TipStruct(R.drawable.tip3, "搜索用空格隔开可以搜索多关键词，例如'十架 医治'"),
            new TipStruct(R.drawable.tip4, "如果要看和弦，请下载白版附加包，对司琴的同伴很有帮助"),
            new TipStruct(R.drawable.tip7, "详情中的文字如果有下划线,可以点击获取更多内容"),
            new TipStruct(R.drawable.tip8, "详情中的歌词、背景等按钮可以长按复制内容"),
            new TipStruct(R.drawable.tip5, "去设置中看看吧"),
            new TipStruct(R.drawable.tip6, "如有建议快点告诉作者"),
            new TipStruct(R.drawable.tip9, "如有你觉得标题栏影响你看诗歌歌词，可以在设置中关闭"),
            new TipStruct(R.drawable.tip11, "这里也可以搜旋律（目前仅限大本）"),
            new TipStruct(R.drawable.tip12, "这里可以进入MP3播放器（如果你下载过MP3附加包）"),
            new TipStruct(R.drawable.tip13, "来这里获取最新版本或者分享链接给同伴"),
            new TipStruct(R.drawable.tip14, "长按标签显示该标签下的所有诗歌"),
            new TipStruct(R.drawable.tip15, "还有别的手势操作，在设置中改成自己喜欢的吧"),
            new TipStruct(R.drawable.tip18, "标签组功能目前可用于播放列表分组(长按播放列表-XX诗歌)"),
            new TipStruct(R.drawable.tip19, "桌面上长按可以最近播放列表和三旧一新功能"),
            new TipStruct(R.drawable.tip20, "重磅推出三旧一新功能,可以播放音频!!!"),
            new TipStruct("左右滑动可上一首,下一首"),
            new TipStruct(R.drawable.tip16, "长按pdf可显示历史、分享等功能"),
            new TipStruct(R.drawable.tip17, "播放列表长按诗歌本有更多选项"),
            new TipStruct("双指左右滑动可以查看快速查看浏览历史"),
            new TipStruct("注意了，" + Constant.TIPS + "内容可能和当前版本有一定的出入，当前整理自1.6.5版本"),
            new TipStruct("可以在设置中关闭此‘" + Constant.TIPS + "’", true)};

    public static TipStruct[] getAll(boolean hideSpecial) {
        int[] arr = Tool.randomList(commonTig.length);
        List<TipStruct> res = new ArrayList<>();
        for (int i = 0; i < arr.length; i++)
            if (!commonTig[arr[i]].hideInReadMe || !hideSpecial)
                res.add(commonTig[arr[i]]);
        return res.toArray(new TipStruct[0]);
    }

    private static Random random = null;

    public static TipStruct getTig() {
        if (!Setting.getValueB(Setting.SHOW_TIG))
            return null;
        if (random == null)
            random = new Random();
        if (random.nextInt(3) != 0)
            return null;
        return commonTig[random.nextInt(commonTig.length)];
    }

    public static void addTips(Context context, TipStruct[] str, LinearLayout ll) {
        boolean isF = true;
        for (TipStruct tip : str) {
            if (isF)
                isF = false;
            else {
                ImageView iv = new ImageView(context);
                iv.setPadding(10, 10, 10, 10);
                iv.setImageResource(R.mipmap.line);
                iv.setAdjustViewBounds(true);
                ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                iv.setLayoutParams(lp);
                ll.addView(iv);
            }

            if (tip.resId != null) {
                ImageView iv = new ImageView(context);
                iv.setPadding(10, 10, 10, 10);
                iv.setImageResource(tip.resId);
                iv.setAdjustViewBounds(true);
                ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                iv.setLayoutParams(lp);
                ll.addView(iv);
            }
            if (tip.text != null && tip.text.length() > 0) {
                TextView tv = new TextView(context);
                tv.setText(tip.text);
                ll.addView(tv);
            }
        }
    }
}
