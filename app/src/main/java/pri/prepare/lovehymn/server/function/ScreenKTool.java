package pri.prepare.lovehymn.server.function;

import android.util.DisplayMetrics;
import android.view.WindowManager;

import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;

public class ScreenKTool {
    public ScreenKTool(WindowManager wm1) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm1.getDefaultDisplay().getMetrics(outMetrics);
        width = outMetrics.widthPixels;
        height = outMetrics.heightPixels;
    }

    private final int width;
    private final int height;

    public String[] getKs() {
        double k;
        if (width > height)
            k = width * 1.0D / height;
        else
            k = height * 1.0D / width;
        if (k > 1.8D)
            return new String[]{def, k916, k34};
        if (k > 1.4D)
            return new String[]{def, k34};
        return new String[]{def};
    }

    public String getK() {
        int v = Setting.getValueI(Setting.SCREEN_K);
        return v == 0 ? def : (v == 1 ? k34 : k916);
    }

    public void setK(String s) {
        int v = s.equals(def) ? 0 : (s.equals(k34) ? 1 : 2);
        Setting.updateSetting(Setting.SCREEN_K, v);
    }

    public int getWidth() {
        int v = Setting.getValueI(Setting.SCREEN_K);
        if (v == 0)
            return width;
        if (v == 1)
            return height * 4 / 3;
        if (v == 2)
            return height * 16 / 9;
        Logger.info("width error, return default");
        return width;
    }

    public int getHeight(){
        return height;
    }

    private final String def = "全屏";
    private final String k34 = "4:3";
    private final String k916 = "16:9";
}
