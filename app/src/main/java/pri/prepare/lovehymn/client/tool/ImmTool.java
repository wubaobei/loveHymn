package pri.prepare.lovehymn.client.tool;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class ImmTool {
    public ImmTool(Context context, Window window) {
        this.window = window;
        this.context = context;
        windowRect = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(windowRect);
    }

    Rect windowRect;
    Window window;
    Context context;

//    public void closeImmIfOpen(Activity activity, View view) {
//        //if (immIsOpen()) {
//            ((InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(),0);
//        //}
//    }
    public void closeImmIfOpen() {
        if (immIsOpen()) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private boolean immIsOpen() {
        Rect rect2 = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rect2);
        return rect2.bottom < windowRect.bottom;
    }

}
