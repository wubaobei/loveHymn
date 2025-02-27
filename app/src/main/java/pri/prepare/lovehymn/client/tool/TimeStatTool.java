package pri.prepare.lovehymn.client.tool;

public class TimeStatTool {
    /**
     * @param warnTime    提醒时间 单位s
     */
    public TimeStatTool( int warnTime) {
        totTime = 0;
        thisTime = System.currentTimeMillis();
        this.warnTime = warnTime * 1000;
        isPause = false;
        hasShow = false;
    }

    public void Restart() {
        totTime = 0;
        thisTime = System.currentTimeMillis();
        isPause = false;
        hasShow = false;
    }

    private long totTime;
    private long thisTime;
    private final int warnTime;
    private boolean isPause;
    private boolean hasShow;

    public void Stat() {
        if (!isPause) {
            long t = System.currentTimeMillis();
            if (t - thisTime < 300) {
                totTime += t - thisTime;
            }
            thisTime = t;
        }
    }

    public void Pause() {
        isPause = true;
    }

    public void Resume() {
        isPause = false;
    }

    public boolean WarnOnce() {
        if (totTime > warnTime && (!hasShow)) {
            hasShow = true;
            return true;
        }
        return false;
    }

    public String getTime() {
        long t = totTime / 1000;
        return (t / 60) + "分" + (t % 60) + "秒";
    }
}
