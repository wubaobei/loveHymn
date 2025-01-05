package pri.prepare.lovehymn.server.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import pri.prepare.lovehymn.server.dal.WarnDateD;

public class WarnDate {

    private static DateTimeFormatter df0 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static boolean hasWarnToday(String name) throws IllegalAccessException {
        WarnDateD dao = WarnDateD.getByName(name);
        if (dao == null) {
            return false;
        }
        LocalDateTime ldt = LocalDateTime.parse(dao.lastTime, df0);
        return ldt.toLocalDate().toEpochDay() == LocalDate.now().toEpochDay();
    }

    public static void addOrUpdateToday(String name) {
        try {
            WarnDateD dao = WarnDateD.getByName(name);
            if (dao == null) {
                dao = new WarnDateD();
                dao.lastTime = df0.format(LocalDateTime.now());
                dao.name = name;
                dao.insert(false);
            } else {
                dao.lastTime = df0.format(LocalDateTime.now());
                dao.update();
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }
}
