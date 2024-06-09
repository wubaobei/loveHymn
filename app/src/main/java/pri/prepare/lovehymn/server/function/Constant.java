package pri.prepare.lovehymn.server.function;

import java.util.Random;

import pri.prepare.lovehymn.server.entity.Setting;

public class Constant {

    /**
     * 附歌的文件夹名
     */
    public static final String SUBJOIN_DIR_NAME = "-";
    public static final String STR_COLLECT = "收藏夹";
    public static final String STR_HISTORY = "历史记录";
    public static final String SUBJOIN_DIR_RENAME = "附";
    public static final String UPDATING = "正在更新资源，请耐心等待";
    public static final String AD = "ad";
    public static final String AD_ADDRESS = "ad_url";
    public static final String AD_TEXT = "ad_text";
    public static final String COLLECT_TIG = "还没有任何收藏，点击标题栏进行收藏";
    private static String[] DEFAULT_AD_ARRAY = new String[]{
            "如果你觉得好用\r\n把这个APP推荐给同伴吧"
    };
    private static String[] DEFAULT_AD_CONTENT_ARRAY = new String[]{
            "如果你觉得哪些地方需要改进或有什么建议，联系作者。"
    };
    private static int adN = 0;

    public static String getStartPageAd() {
        String s;
        if ((s = Setting.getValueS(Setting.AD_TITLE)).length() > 0) {
            String s2 = Setting.getValueS(Setting.AD_CONTENT);
            if (s2.length() > 30)
                s2 = s2.substring(0, 28) + "...";
            return s + "\r\n" + s2;
        }
        return "By PrepareWu";
    }

    public static String getRandomAd() {
        String t1 = Setting.getValueS(Setting.AD_TITLE);
        String t2 = Setting.getValueS(Setting.AD_CONTENT);
        if (!t1.equals("广告位招租")) {
            DEFAULT_AD_ARRAY = new String[]{t1};
            DEFAULT_AD_CONTENT_ARRAY = new String[]{t2};
        }
        Random rd = new Random();
        adN = rd.nextInt(Constant.DEFAULT_AD_ARRAY.length);
        return DEFAULT_AD_ARRAY[adN];
    }

    public static String getDefaultAdValue() {
        return DEFAULT_AD_CONTENT_ARRAY[adN];
    }

    public static final String[] DEFAULT_SEARCH_STRING_ARRAY = new String[]{
            "李常受",
            "达秘",
            "耶稣 十字架",
            "你不在此",
            "330",
            "1233335321665"
    };

    public static String getRandomSearchStr() {
        Random rd = new Random();
        int n = rd.nextInt(Constant.DEFAULT_SEARCH_STRING_ARRAY.length);
        return DEFAULT_SEARCH_STRING_ARRAY[n];
    }

    public static final int btnMinWidth = 80;
    public static final String WHITE = "white";
    public static final String ADD_FILE_NAME = "附加包";
    public static final String ADDED_FILE_FLAG = "已加载";

    /**
     * 标点符号（包括中英）
     */
    public static final String ChineseChar = "，。；：？、！‘’”“!?,.;:'\"—-";
    /**
     * 即时加载的数量（快速显示目录，后续的再加载，减少卡顿感）
     */
    public static final int FIRST_LOAD_COUNT = 15;
    /**
     * 目录中显示的歌词一般长度
     */
    public static final int LYRIC_SHOW_LENGTH = 30;
    /**
     * 显示的最大长度（强行截取）
     */
    public static final int LYRIC_SHOW_LENGTH_MAX = 40;
//    public static final String OPEN_RECENT = "历史";
    /**
     * 搜索一次显示的数量
     */
    public static final int SEARCH_RESULT_SHOW_COUNT = 20;
    public static final String SHOW_MORE = "加载中...";
    public static final int SEARCH_RESULT_SHOW_MAX_COUNT = 300;
    public static final String TOO_MUCH_WARN = "换个关键词试试吧";
    public static final String NO_RESULT = "没有找到任何匹配的诗歌";

    public static final int SEARCH_RESULT_MORE_Y = 2000;
    public static final String LB_DIR_NAME = "诗歌蓝版";
    public static final String RES_NAME = "res";

    public static final String[] AUTHOR = new String[]{"作者：吴预备-杭州召会\r\n他很懒，只留下了联系方式\r\nQQ：843439261\r\n微信：prepareWu",
            "感谢所有搜集整理蓝版和MP3等资料的同伴们",
            "感谢@heetisn @canlinkj @yls863699912 @ymll31 @weixinyedelu提供的pdf和mp3资源",
            "感谢@qpzmhyn8 @x03140501 @ymll31 的测试反馈和建议",
            "感谢@C18857836876 设计的界面",
            "感谢@Daniel198717 @miaomiao1989127 @伟www @火烧荆棘 整理的‘思路’‘背景’等资料",
            "特别感谢姊妹@马晓可 和家人的支持",
            "如果你觉得这个APP不错，也可以给作者一点捐赠\uD83D\uDE00"};
    public static final String ADURL = "https://www.jianshu.com/p/7964f2b8de07";
    /**
     * 百度网盘分享链接
     */
    public static final String LINK = "link";
    /**
     * 提取码
     */
    public static final String PWD = "password";

    public static final String READ_ME = "使用说明";
    public static final String TIPS = "小贴士";
}
