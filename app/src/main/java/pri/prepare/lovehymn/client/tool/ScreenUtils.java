package pri.prepare.lovehymn.client.tool;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ScreenUtils {

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth( WindowManager wm) {
        if (wm == null) return -1;
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //Andoird 4.0时，引入了虚拟导航键
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight(WindowManager wm) {
        if (wm == null) return -1;
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.y;
    }

    /**
     * 获取宽度（排除系统装饰元素，如底部导航栏等），此方法是不准确的
     */
    public static int getAppScreenWidth(WindowManager wm) {
        if (wm == null) return -1;
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        return point.x;
    }

    /**
     * 获取高度（排除系统装饰元素，如底部导航栏等），此方法是不准确的
     */
    public static int getAppScreenHeight( WindowManager wm) {
        if (wm == null) return -1;
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        return point.y;
    }

    /**
     * 获取ContentView宽度
     */
    public static int getContentViewWidth(Activity activity) {
        View contentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        return contentView.getWidth();
    }

    /**
     * 获取ContentView高度
     */
    public static int getContentViewHeight(Activity activity) {
        View contentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        return contentView.getHeight();
    }
}
