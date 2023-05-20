package pri.prepare.lovehymn.client.tool;

import android.content.Context;
import android.util.TypedValue;

public class ThemeManager {
    public static int getMipmap(Context context, int attrId){
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrId, typedValue, true);
        return typedValue.resourceId;
    }
}
