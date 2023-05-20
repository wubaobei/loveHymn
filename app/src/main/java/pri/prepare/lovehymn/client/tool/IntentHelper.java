package pri.prepare.lovehymn.client.tool;

import android.content.Context;
import android.content.Intent;

import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;

public class IntentHelper {
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_BOOK_NO_LABEL = 2;
    public static final int TYPE_BOOK_LABEL = 3;
    public static final int TYPE_BOOK_LABEL_GROUP = 4;

    private static final String ID = "ID";
    private static final String TYPE = "TYPE";
    private static final String ISLABEL = "ISLABEL";
    private static final String OTHERPARAM = "OTHERPARAM";
    private static final String PATH = "PATH";

    public static Intent create(Context packageContext, Class<?> cls, int type, int id, boolean isLabel, String otherParam, String path) {
        Intent intent = new Intent(packageContext, cls);
        intent.putExtra(TYPE, type);
        intent.putExtra(ID, id);
        intent.putExtra(ISLABEL, isLabel);
        intent.putExtra(OTHERPARAM, otherParam);
        intent.putExtra(PATH, path);
        return intent;
    }

    public static Intent create(Context packageContext, Class<?> cls, int type, int id, boolean isLabel) {
        Intent intent = new Intent(packageContext, cls);
        intent.putExtra(TYPE, type);
        intent.putExtra(ID, id);
        intent.putExtra(ISLABEL, isLabel);
        intent.putExtra(OTHERPARAM, "a");
        intent.putExtra(PATH, "");
        return intent;
    }

    private int id;
    private boolean isLabel;
    private int type;
    private String param;
    private String path;

    public boolean error = false;

    public IntentHelper(Intent intent) {
        String sp = "]";
        if (!intent.hasExtra(ID)) {
            String s = Setting.getValueS(Setting.SHORT_CUT1);
            if (s.length() == 0) {
                Logger.info("无最近播放列表");
                error = true;
            } else {
                String[] arr = s.split(sp);
                id = Integer.parseInt(arr[0]);
                isLabel = Boolean.parseBoolean(arr[1]);
                type = Integer.parseInt(arr[2]);
                param = arr[3];
                return;
            }
        }

        id = intent.getIntExtra(ID, 0);
        isLabel = intent.getBooleanExtra(ISLABEL, true);
        type = intent.getIntExtra(TYPE, TYPE_NORMAL);
        param = intent.getStringExtra(OTHERPARAM);
        path = intent.getStringExtra(PATH);

        Setting.updateSetting(Setting.SHORT_CUT1, id + sp + isLabel + sp + type + sp + param);
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public boolean isLabel() {
        return isLabel;
    }

    public String getParam() {
        return param;
    }

    public String key() {
        return type + "," + id + "," + isLabel + "," + param;
    }

    public String getPath(){return path;}
}
