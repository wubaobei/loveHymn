package pri.prepare.lovehymn.client.tool;

import java.util.ArrayList;

import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.Setting;

public class HistoryTool {
    private static ArrayList<MyFile> _history;
    private static int _ind = -1;

    public static void add(MyFile f, boolean modify) {
        if (init())
            return;
        if (f == null || !f.exists())
            return;

        if (!modify) {
            save();
            return;
        }

        if (_ind > 0 && _history.get(_ind - 1).getAbsolutePath().equals(f.getAbsolutePath())) {
            _ind--;
        } else if (_ind < _history.size() - 1 && _history.get(_ind + 1).getAbsolutePath().equals(f.getAbsolutePath())) {
            _ind++;
        } else {
            for (int i = 0; i < _history.size(); i++) {
                if (_history.get(i).getAbsolutePath().equals(f.getAbsolutePath())) {
                    _history.remove(i);
                    break;
                }
            }
            _history.add(f);
            if (_history.size() > MAX) {
                _history.remove(0);
            }
            _ind = _history.size() - 1;
        }
        save();
    }

    private static boolean init() {
        if (_history == null) {
            String s = Setting.getValueS(Setting.OPEN_RECENT);
            String[] arr = s.split(";");

            if (arr == null || arr.length == 0 || arr[0].length() == 0) {
                _ind = -1;
                _history = new ArrayList<>();
                return true;
            }

            if (Service.isInteger(arr[0])) {
                _ind = Integer.parseInt(arr[0]);
                _history = new ArrayList<>();
                for (int i = 1; i < arr.length; i++)
                    _history.add(MyFile.from(arr[i]));
            } else {
                _history = new ArrayList<>();
                for (int i = 1; i < arr.length; i++)
                    _history.add(MyFile.from(arr[i]));
                _ind = _history.size() - 1;
            }
            return true;
        }
        return false;
    }

    private static void save() {
        if (_history == null) return;
        MyFile[] fs = getHistories();
        StringBuilder sb = new StringBuilder();
        sb.append(_ind).append(";");
        for (MyFile f : fs) {
            sb.append(f.getAbsolutePath()).append(";");
        }
        Setting.updateSetting(Setting.OPEN_RECENT, sb.toString());
    }

    public static MyFile getNext() {
        init();
        if (_ind < _history.size() - 1) {
            _ind++;
            return _history.get(_ind);
        }
        return null;
    }

    public static MyFile getPreview() {
        init();
        if (_ind > 0) {
            _ind--;
            return _history.get(_ind);
        }
        return null;
    }

    private static final int MAX = 20;

    public static MyFile[] getHistories() {
        init();
        if (_history.size() <= MAX) {
            return _history.toArray(new MyFile[0]);
        } else {
            MyFile[] fs = new MyFile[MAX];
            for (int i = 0; i < MAX; i++)
                fs[i] = _history.get(_history.size() - MAX + i);
            return fs;
        }
    }

    /**
     * 清空浏览历史
     */
    public static void clearAll() {
        Setting.updateSetting(Setting.OPEN_RECENT, "");
        _history = null;
    }
}
