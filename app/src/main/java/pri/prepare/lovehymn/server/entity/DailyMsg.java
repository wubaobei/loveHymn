package pri.prepare.lovehymn.server.entity;

import pri.prepare.lovehymn.server.dal.DailyMsgD;

public class DailyMsg {
    private DailyMsg() {
        dao = new DailyMsgD();
    }

    public DailyMsg(DailyMsgD d) {
        dao = d;
    }

    private final DailyMsgD dao;

    public static String get(String name) {
        try {
            return DailyMsgD.getByName(name).msg;
        } catch (IllegalAccessException e) {
            return "出错了，未找到" + name + "的每日信息";
        }
    }

    public static void save(String shortName,String fullName, String msg) throws Exception {
        try {
            DailyMsgD msgD = DailyMsgD.getByName(shortName);
            if (msgD == null) {
                DailyMsgD d = new DailyMsgD();
                d.msg = msg;
                d.shortName = shortName;
                d.fullName=fullName;
                d.insert(false);
            } else {
                msgD.msg = msg;
                msgD.update();
            }
        } catch (Exception e) {
            Logger.exception(e);
            //return "插入/更新" + shortName + "每日信息失败";
            throw e;
        }
    }
}
