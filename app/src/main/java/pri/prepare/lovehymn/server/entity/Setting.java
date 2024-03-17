package pri.prepare.lovehymn.server.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import pri.prepare.lovehymn.client.SettingDialog;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.dal.SettingD;
import pri.prepare.lovehymn.server.function.DBHelper;
import pri.prepare.lovehymn.server.function.DBUtil;

public class Setting {
    /**
     * 上次打开文件
     */
    public static final int LAST_OPEN = 1;
    /**
     * 蓝版诗歌所在目录
     */
    //public static final int FILE_PATH = 2;
    /**
     * 最近打开（列表）
     */
    public static final int OPEN_RECENT = 3;
    /**
     * 自动隐藏的时间
     */
    public static final int DISPEAR_TIME = 4;
    /**
     * MP3是否单曲循环
     */
    public static final int MP3_LOOP = 5;
    /**
     * 目录界面是否显示歌词
     */
    //public static final int SHOW_LYRIC = 6;
    /**
     * 相关经节中英显示
     */
    public static final int SHOW_CHINESE_ENGLISH = 7;
    /**
     * 段落间是否加空格
     */
//    public static final int LINE_SHOW = 8;
    /**
     * 作者显示英文名
     */
    //public static final int AUTHOR_ENGLISH_NAME = 9;
    /**
     * 经节连续显示
     */
    public static final int SECTION_CT = 10;
    /**
     * 屏幕比例
     */
    public static final int SCREEN_K = 11;
    /**
     * 收藏夹
     */
    public static final int COLLECT = 12;
    /**
     * 更新url
     */
    public static final int LAST_BD = 13;
    /**
     * 已加载的资源文件
     */
    //public static final int RES_UPDATE_RECORD = 14;
    /**
     * pdf阅读进度
     */
    public static final int PDF_Y_OFFSET = 15;
    /**
     * 主题
     */
    public static final int APP_THEME = 16;
    /**
     * 搜索结果的分隔符
     */
    public static final int SEARCH_RESULT_SPLIT = 17;
    public static final String[] SEARCH_RESULT_SPLIT_Arr = new String[]{"...", "++++++++", "——————朴素的分割线——————"};
    /**
     * 显示状态栏（针对真全面屏）
     */
    public static final int STATUS_BAR_SHOW = 18;
    /**
     * 初始化成功
     */
    public static final int RES_VERSION = 19;
    public static final int CURRENT_RES_VERSION = 1;
    /**
     * 下载地址提取码缓存
     */
    public static final int LAST_BD_PWD = 21;
    /**
     * 版本缓存（用于控制版本历史显示）
     */
    public static final int LAST_VERSION_NAME = 22;
    /**
     * 上一次使选择的收藏夹还是历史
     */
    public static final int COLLECT_HISTORY_LAST_CHOOSE = 23;
    /**
     * 足迹时间格式化类型
     */
    public static final int STEP_FORMAT_TYPE = 24;
    /**
     * MP3播放器的循环类型
     */
    public static final int MP3_PLAYER_MODE = 25;
    /**
     * 默认显示或隐藏工具栏
     */
    public static final int SHOW_TOOL_BAR_ON_LOAD = 26;
    /**
     * 显示生僻字拼音
     */
    public static final int SHOW_DICT = 27;
    /**
     * 显示小提示
     */
    public static final int SHOW_TIG = 28;
    /**
     * mp3播放器显示歌词
     */
    public static final int SHOW_LYRIC = 29;
    /**
     * 歌词字体
     */
    public static final int LYRIC_SIZE = 30;
    /**
     * 双指上海
     */
    public static final int DOUBLE_FINGER_UP = 31;
    /**
     * 双指下滑
     */
    public static final int DOUBLE_FINGER_DOWN = 32;
    /**
     * 自动足迹（提示）
     */
    public static final int AUTO_STEP = 33;
    /**
     * 自动足迹时间（特殊代码）
     */
    public static final int AUTO_STEP_TIME = 34;
    /**
     * 调用pause时间
     */
    public static final int PAUSE_TIME = 35;
    /**
     * pdfTimer时间
     */
    public static final int PDF_TIME = 36;
    /**
     * MP3数量缓存
     */
    public static final int MP3_COUNT_CACHE = 37;
    /**
     * 广告标题
     */
    public static final int AD_TITLE = 38;
    /**
     * 广告内容
     */
    public static final int AD_CONTENT = 39;
    /**
     * 最新版本（用于更新app的提醒显示）
     */
    public static final int NEW_VERSION = 40;
    /**
     * 最近播放列表缓存
     */
    public static final int SHORT_CUT1 = 41;
    /**
     * 简易时间表记录
     */
    public static final int EASY_SCHEDULE_RECORD = 42;
    /**
     * 三旧一新字体大小
     */
    public static final int DAILY_BIBLE_TEXT_SIZE = 43;
    /**
     * 三旧一新连续显示
     */
    public static final int DAILY_BIBLE_CT = 44;
    /**
     * 三旧一新播放速度
     */
    public static final int DAILY_BIBLE_SPEECH = 45;
    /**
     * 三旧一新播放音色
     */
    public static final int DAILY_BIBLE_YINSE = 46;
    /**
     * 三旧一新播放自动滚动
     */
    public static final int DAILY_BIBLE_AUTO_SCR = 47;
    /**
     * 是否加载其他
     */
    public static final int LOAD_QITA = 48;
    /**
     * 是否加载标签
     */
    public static final int LOAD_LABEL = 49;
    /**
     * 是否加载足迹
     */
    public static final int LOAD_STEP = 50;
    /**
     * 默认使用网页版三旧一新
     */
    public static final int USE_WEB_DAILY_BIBLE = 51;
    /**
     * 关闭异步功能
     */
    public static final int USE_ASYNC = 52;
    /**
     * 启动页背景
     */
    public static final int STARTPAGE_BACKGROUND = 53;
    /**
     * 隐藏青年诗歌
     */
    public static final int HIDE_QING = 54;

    public static final int CATELOG_QUICK = 1;
    public static final int DETAIL_QUICK = 3;
    public static final int MP3_PLAY_QUICK = 5;
    public static final int double_finger_up_default = CATELOG_QUICK;
    public static final int COLEECT_QUICK = 2;
    public static final int LABLE_COLLECT_QUICK = 4;
    public static final int double_finger_down_default = LABLE_COLLECT_QUICK;
    public static final int lyric_default_text_size = 15;
    public static final boolean section_ct_default = true;
    //public static final boolean show_lyric_default = true;

    public static final boolean line_show_default = true;
    //public static final boolean author_english_name_default = true;
    public static final int show_chinese_english_default = 1;

    public static final String TABLE = SettingD.class.getSimpleName();
    public final int Key;
    public int ValueInt;
    public String ValueStr;

    public Setting(Cursor c) {
        Key = c.getInt(1);
        ValueInt = c.getInt(2);
        ValueStr = c.getString(3);
    }

    private static Setting getByKey(int k) {
        SQLiteDatabase db = DBHelper.current.getWritableDB();
        Setting res = null;

        Cursor c = db.rawQuery(DBUtil.getC().getSelectSql(SettingD.class) + " where keyI= ?", new String[]{k + ""});
        if (c != null && c.getCount() >= 1) {
            while (c.moveToNext()) {
                res = new Setting(c);
            }
        }

        return res;
    }

    private static Object getDefault(int key) {
        if (defaultMap == null) {
            defaultMap = new HashMap<>();
            defaultMap.put(AUTO_STEP_TIME, false);
            defaultMap.put(SHOW_TIG, true);
            defaultMap.put(STATUS_BAR_SHOW, true);
            defaultMap.put(MP3_LOOP, false);
            defaultMap.put(SHOW_TOOL_BAR_ON_LOAD, true);
            defaultMap.put(SHOW_LYRIC, false);
            defaultMap.put(SECTION_CT, section_ct_default);
            defaultMap.put(SHOW_DICT, true);
            defaultMap.put(SEARCH_RESULT_SPLIT, 0);
            defaultMap.put(STEP_FORMAT_TYPE, 1);
            defaultMap.put(COLLECT_HISTORY_LAST_CHOOSE, 0);
            defaultMap.put(AUTO_STEP, 1);
            defaultMap.put(DOUBLE_FINGER_UP, double_finger_up_default);
            defaultMap.put(DOUBLE_FINGER_DOWN, double_finger_down_default);
            defaultMap.put(PDF_Y_OFFSET, 0);
            defaultMap.put(MP3_PLAYER_MODE, 3);
            defaultMap.put(LYRIC_SIZE, Setting.lyric_default_text_size);
            defaultMap.put(PDF_TIME, 0);
            defaultMap.put(DISPEAR_TIME, SettingDialog.getInitDisappearTime());
            defaultMap.put(SHOW_CHINESE_ENGLISH, show_chinese_english_default);
            defaultMap.put(APP_THEME, 0);
            defaultMap.put(RES_VERSION, 0);
            defaultMap.put(SCREEN_K, 0);
            //defaultMap.put(RES_UPDATE_RECORD, "");
            defaultMap.put(MP3_COUNT_CACHE, "");
            defaultMap.put(COLLECT, "");
            defaultMap.put(LAST_OPEN, Service.getC().getFirstHymnPath());
            defaultMap.put(LAST_BD, "https://pan.baidu.com/s/1VFuigoViOR63QDBQcAVfgw");
            defaultMap.put(SHORT_CUT1, "99]false]1]a");//默认显示所有诗歌
            defaultMap.put(LAST_BD_PWD, "2iof");
            defaultMap.put(NEW_VERSION, "");
            defaultMap.put(OPEN_RECENT, "");
            defaultMap.put(AD_TITLE, "广告位招租");
            defaultMap.put(AD_CONTENT, "联系作者详谈，作者也从没干过这活(滑稽)");
            defaultMap.put(LAST_VERSION_NAME, "");
            defaultMap.put(PAUSE_TIME, "");
            defaultMap.put(EASY_SCHEDULE_RECORD, "");
            defaultMap.put(DAILY_BIBLE_TEXT_SIZE, 20);
            defaultMap.put(DAILY_BIBLE_CT, false);
            defaultMap.put(DAILY_BIBLE_SPEECH, 2);
            defaultMap.put(DAILY_BIBLE_YINSE, 2);
            defaultMap.put(DAILY_BIBLE_AUTO_SCR, true);
            defaultMap.put(LOAD_LABEL, false);
            defaultMap.put(LOAD_QITA, false);
            defaultMap.put(LOAD_STEP, false);
            defaultMap.put(USE_WEB_DAILY_BIBLE, false);
            defaultMap.put(USE_ASYNC, true);
            defaultMap.put(STARTPAGE_BACKGROUND, 0);
            defaultMap.put(HIDE_QING, true);
        }
        return defaultMap.get(key);
    }

    private static HashMap<Integer, Object> defaultMap = null;

    //region get/set boolean
    private static final HashMap<Integer, Boolean> bMap = new HashMap<>();

    public static boolean getValueB(int key) {
        if (!bMap.containsKey(key)) {
            Setting s = getByKey(key);
            if (s != null) {
                bMap.put(key, s.ValueInt == 1);
            } else
                bMap.put(key, (Boolean) getDefault(key));
        }
        return bMap.get(key);
    }

    public static void updateSetting(int key, boolean value) {
        Setting s = getByKey(key);
        if (s == null) {
            add(key, value ? 1 : 0, "");
        } else {
            s.ValueInt = value ? 1 : 0;
            s.update();
        }
        bMap.put(key, value);
    }

    //endregion
    //region get/set int
    private static final HashMap<Integer, Integer> iMap = new HashMap<>();

    public static int getValueI(int key) {
        if (!iMap.containsKey(key)) {
            Setting s = getByKey(key);
            if (s != null) {
                iMap.put(key, s.ValueInt);
            } else
                iMap.put(key, (Integer) getDefault(key));
        }
        return iMap.get(key);
    }

    public static void updateSetting(int key, int value) {
        Setting s = getByKey(key);
        if (s == null) {
            add(key, value, "");
        } else {
            s.ValueInt = value;
            s.update();
        }
        iMap.put(key, value);
    }

    //endregion
    //region get/set String
    private static final HashMap<Integer, String> sMap = new HashMap<>();

    public static String getValueS(int key) {
        if (!sMap.containsKey(key)) {
            Setting s = getByKey(key);
            if (s != null)
                sMap.put(key, s.ValueStr);
            else
                sMap.put(key, (String) getDefault(key));
        }
        return sMap.get(key);
    }

    public static void updateSetting(int key, String value) {
        Setting s = getByKey(key);
        if (s == null) {
            add(key, -1, value);
        } else {
            s.ValueStr = value;
            s.update();
        }
        sMap.put(key, value);
    }

    //endregion

    //region get/set LocalDateTime
    public static DateTimeFormatter getDefaultTimeFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    public static LocalDateTime getValueT(int key) {
        String t = getValueS(key);
        if (t.length() == 0)
            return LocalDateTime.now();
        return LocalDateTime.parse(t, getDefaultTimeFormatter());
    }

    public static void updateSetting(int key, LocalDateTime value) {
        String sv = getDefaultTimeFormatter().format(value);
        updateSetting(key, sv);
    }
    //endregion

    private static void add(int key, int valueI, String valueS) {
        SQLiteDatabase db = DBHelper.current.getWritableDB();
        ContentValues contentValues = new ContentValues();
        contentValues.put("keyI", key);
        contentValues.put("valueInt", valueI);
        contentValues.put("valueStr", valueS);
        db.insert(TABLE, null, contentValues);
    }

    private void update() {
        SQLiteDatabase db = DBHelper.current.getWritableDB();
        ContentValues contentValues = new ContentValues();
        contentValues.put("keyI", Key);
        contentValues.put("valueInt", ValueInt);
        contentValues.put("valueStr", ValueStr);
        db.update(TABLE, contentValues, "keyI=?", new String[]{Key + ""});
    }
}
