package pri.prepare.lovehymn.client.tool;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Context;
import android.graphics.Rect;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

public class ImmTool {
    public ImmTool(Context context, Window window) {
        this.window = window;
        this.context = context;
        windowRect = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(windowRect);
    }

    private final Rect windowRect;
    private final Window window;
    private final Context context;

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
