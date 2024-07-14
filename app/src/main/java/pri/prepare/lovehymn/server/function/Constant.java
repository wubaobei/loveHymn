package pri.prepare.lovehymn.server.function;

import java.util.Random;

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
    public static final String AD_TEXT = "ad_text";
    public static final String COLLECT_TIG = "还没有任何收藏，点击标题栏进行收藏";

    public static String getStartPageMsg() {
        return "By PrepareWu";
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

    public static final String[] AUTHOR = new String[]{"作者：吴预备-杭州召会\r\n" +
            "他很懒，只留下了联系方式\r\n" +
            "QQ：843439261\r\n" +
            "微信：prepareWu",
            "感谢所有搜集整理蓝版和MP3等资料的同伴们和家人的支持",
            "如果你觉得这个APP不错，也可以给作者一点捐赠\uD83D\uDE00"};
    public static final String ADURL = "https://www.jianshu.com/p/7964f2b8de07";

    public static final String READ_ME = "使用说明";
    public static final String TIPS = "小贴士";
}
