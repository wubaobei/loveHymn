package pri.prepare.lovehymn.server;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;

import java.util.ArrayList;

import pri.prepare.lovehymn.client.SettingDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.Setting;

public class UpdateHistory {
    public static final int MIN_RES_INDEX = 190;

    /**
     * 是否为内测版
     *
     * @return
     */
    public static boolean isTestMode(Activity activity) {
        return getVersionHistory(activity)[0].contains(" temp ");
    }

    public static  String[] versionHistory=null;
    public String[] getVersionHistory(Activity activity){
        if(versionHistory==null){
            versionHistory=  MyFile.readStream(activity.getResources().openRawResource(R.raw.history));
        }
        return versionHistory;
    }

    private static final String[] ASK_ANSWER = {
            //"问:\r\n答:",
            "问:设置里一直提示获取不到下载地址怎么办？\r\n答:可能是wifi造成的,换成数据试试",
            "问:app解压压缩包失败怎么办?\r\n答:请尝试到文件管理器中手动解压压缩包,并移动到'诗歌蓝版'的相应位置;如果文件管理器也解压失败,尝试换个解压app或者用电脑解压",
            //"问:搜索很慢怎么办?\r\n答:第一次搜索时搜索结果的诗歌会去建立诗歌和资源的对应关系,因此比较慢,第二次搜索同样关键字就快了",
            "问:诗人介绍怎么那么多英文和奇怪翻译?\r\n答:很多是从英文网站https://hymnary.org/谷歌翻译来的,如果你有合适的诗人介绍资源或网站,请告诉作者",
            "问:下载链接怎么失效了?\r\n答:可能是链接又被百度封了,请联系作者来解决",
            "问:诗歌标题变成数字怎么办?\r\n答:可能是版本升级造成的错误。请通过'设置'-'其他设置'-'清除app数据'重置",
            "问:手机的状态栏可以隐藏吗?\r\n答:如果你的手机是挖空/水滴/刘海屏,不建议隐藏;如果是非全面屏或弹出式前摄/屏下摄像头,可以在其他设置中隐藏状态栏,以获取更好的阅读体验",
            "问:歌词显示异常怎么办?\r\n答:可能是版本升级造成的错误。请通过'设置'-'其他设置'-'清除app数据'重置",
            "问:百度网盘下载太慢,能不能换其他网盘?\r\n答:暂时还是使用百度网盘,没有修改网盘的计划",
            "问:为什么搜索下拉会一直'加载中...'?\r\n答:可能是下拉太快导致的bug,可以往回拉一些,重新下拉进行加载",
            "问:mp3哪里下载?\r\n答:加作者的百度网盘好友私发(参考'设置'-'说明文档'-'加百度网盘好友教程')",
            "问:白版去哪了?\r\n答:现在长按pdf会显示白版,分享,历史等内容,可参考'" + SettingDialog.ALL_READ + "'-'使用说明'"
    };

    public static String[] getAskAnswer() {
        int[] inds = Tool.randomList(ASK_ANSWER.length);
        String[] res = new String[ASK_ANSWER.length];
        for (int i = 0; i < res.length; i++)
            res[i] = ASK_ANSWER[inds[i]];
        return res;
    }

    public static final String WELCOME = "欢迎使用";

    /**
     * 获取更新信息
     */
    public static String[] getUpdate(Context context) throws PackageManager.NameNotFoundException {
        String currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        String lastVersion = Setting.getValueS(Setting.LAST_VERSION_NAME);
        if (lastVersion.length() == 0) {
            Setting.updateSetting(Setting.LAST_VERSION_NAME, currentVersion);
            return new String[]{WELCOME, "欢迎使用唱诗歌app，如果你是第一次使用，请阅读'设置'-'" + SettingDialog.ALL_READ + "'-'使用说明'，以了解更多的隐藏功能"};
        }
        if (lastVersion.equals(currentVersion))
            return new String[0];
        ArrayList<String> res = new ArrayList<>();
        for (String s : VERSION_HISTORY) {
            String t = getVersionStr(s);
            if (t.equals(lastVersion)) {
                Setting.updateSetting(Setting.LAST_VERSION_NAME, currentVersion);
                res.add("{注意：}");
                res.add("如果更新中有{蓝版pdf}更新，可以清除数据重新加载，使{蓝版pdf}得更新");
                res.add("如果更新后无法搜索，点击详情无反应，则需要清除数据重新加载");
                return new String[]{"更新历史", String.join("\r\n\r\n", res)};
            }
            res.add(s);
        }
        throw new RuntimeException("更新历史异常");
    }

    private static String getVersionStr(String s) {
        String[] va = s.split(" ");
        if (va.length <= 2)
            throw new RuntimeException("更新历史异常");
        if (!va[1].equals("temp")) {
            return va[1];
        }
        return "";
    }
}
