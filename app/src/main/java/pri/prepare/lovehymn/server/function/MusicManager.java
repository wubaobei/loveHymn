package pri.prepare.lovehymn.server.function;

import android.media.MediaPlayer;

import java.io.File;
import java.util.List;
import java.util.Random;

import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;


public class MusicManager {
    MediaPlayer mp;
    private String _name;
    private List<String> _nameList;
    private List<String> _pathList;
    private int index = -1;

    public MusicManager(File file, String name, List<String> nameList, List<String> pathList) {
        try {
            _name = name;
            ft = file;
            _nameList = nameList;
            _pathList = pathList;

            mp = new MediaPlayer();
            mp.setDataSource(file.getAbsolutePath());
            mp.prepare();
            mp.setOnCompletionListener(listener);

            if (nameList != null) {
                index = nameList.indexOf(name);
                mode = Setting.getValueI(Setting.MP3_PLAYER_MODE);
                mp.setLooping(mode == MODE_REPEAT);
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    public MusicManager(File file) {
        this(file, null, null, null);
    }

    public void changeMusic(String path, String name) {
        try {
            _name = name;
            mp.release();
            mp = new MediaPlayer();
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
            ft = new File(path);
            mp.setOnCompletionListener(listener);
            index = _nameList.indexOf(name);
            Logger.info("播放 " + name);
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private File ft;
    public boolean overEventAp = false;

    public boolean isFile(File f) {
        return ft != null && f != null && f.getAbsolutePath().equals(ft.getAbsolutePath());
    }

    public void Play() {
        if (mp != null)
            mp.start();
    }

    public void stop() {
        if (mp != null) {
            mp.seekTo(0);
            mp.pause();
        }
    }

    public void pause() {
        if (mp != null) {
            mp.pause();
        }
    }

    public void release() {
        if (mp != null)
            mp.release();
    }

    public boolean isPlaying() {
        return mp.isPlaying();
    }

    public String getProgressString() {
        if (mp == null) return "";
        try {
            boolean showZero = mp.getDuration() / 1000 / 60 > 10;
            return dp2String(mp.getCurrentPosition(), showZero) + "/" + dp2String(mp.getDuration(), showZero);
        } catch (Exception e) {
            return "";
        }
    }

    private String dp2String(int pd, boolean showZero) {
        pd = pd / 1000;
        String res = (pd / 60) + (pd % 60 < 10 ? ":0" : ":") + (pd % 60);
        if (showZero && pd / 60 < 10)
            return "0" + res;
        return res;
    }

    public double getProgressPercent() {
        if (mp == null) return 0d;
        try {
            return (double) mp.getCurrentPosition() / mp.getDuration();
        } catch (Exception e) {
            return 0d;
        }
    }

    public void setProgress(double v) {
        if (mp != null) {
            mp.seekTo((int) (v * mp.getDuration()));
        }
    }

    /**
     * 切换单曲循环-不循环
     */
    public void changeSimpleRepeat() {
        try {
            if (mp != null)
                mp.setLooping(!mp.isLooping());
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    /**
     * 切换 不循环-单曲循环-顺序播放-随机播放
     */
    public void changeComplexRepeat() {
        if (_nameList.size() == 1) {
            //只有一首MP3时，就在不循环-单曲循环中切换
            mode = (mode + 1) % 2;
            return;
        }
        mode = (mode + 1) % 4;
        mp.setLooping(mode == MODE_REPEAT);
        Setting.updateSetting(Setting.MP3_PLAYER_MODE, mode);
    }

    public int getMode() {
        return mode;
    }

    private int mode = 0;
    public static final int MODE_NO_REPEAT = 0;
    public static final int MODE_REPEAT = 1;
    public static final int MODE_ORDER = 2;
    public static final int MODE_RANDOM = 3;

    public boolean needUpdateLyric = false;
    public boolean needUpdatePlayBtn = false;

    private MediaPlayer.OnCompletionListener listener = mp -> {
        try {
            switch (mode) {
                case MODE_NO_REPEAT:
                    setProgress(0d);
                    needUpdatePlayBtn=true;
                    break;
                case MODE_REPEAT:
                    //setProgress(0d);
                    break;
                case MODE_ORDER:
                    //下一首
                    index = (index + 1) % _nameList.size();
                    changeMusic(_pathList.get(index), _nameList.get(index));
                    needUpdateLyric = true;
                    break;
                case MODE_RANDOM:
                    //随机
                    index = new Random().nextInt(_nameList.size());
                    changeMusic(_pathList.get(index), _nameList.get(index));
                    needUpdateLyric = true;
                    break;
            }
        } catch (Exception e) {
            Logger.exception(e);
            throw new RuntimeException(e.getMessage());
        }
    };

    public boolean getRepeat() {
        try {
            if (mp != null)
                return mp.isLooping();
            return false;
        } catch (Exception e) {
            Logger.exception(e);
            return false;
        }
    }

    public String getMp3Path() {
        return ft.getAbsolutePath();
    }

    public String getMusicName() {
        return _name;
    }

    public void nextMp3() {
        index = (index + 1) % _nameList.size();
        changeMusic(_pathList.get(index), _nameList.get(index));
        needUpdateLyric=true;
    }

    public void previewMp3() {
        index = (index + _nameList.size() - 1) % _nameList.size();
        changeMusic(_pathList.get(index), _nameList.get(index));
        needUpdateLyric=true;
    }
}
