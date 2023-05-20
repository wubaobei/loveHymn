package pri.prepare.lovehymn.client.tool;

import pri.prepare.lovehymn.server.entity.Logger;

public class TimeStatTool {
    /**
     * @param maxStatTime 最大统计时间间隔 单位ms
     * @param warnTime    提醒时间 单位s
     */
    public TimeStatTool(int maxStatTime, int warnTime) {
        totTime = 0;
        thisTime = System.currentTimeMillis();
        this.maxStatTime = maxStatTime;
        this.warnTime = warnTime * 1000;
        isPause = false;
        hasShow = false;
    }

    public void Restart() {
        totTime = 0;
        thisTime = System.currentTimeMillis();
        //this.maxStatTime = maxStatTime;
        //this.warnTime = warnTime * 1000;
        isPause = false;
        hasShow = false;
    }

    private long totTime;
    private long thisTime;
    private int maxStatTime;
    private int warnTime;
    private boolean isPause;
    private boolean hasShow;

    public void Stat() {
        if (!isPause) {
            long t = System.currentTimeMillis();
            if (t - thisTime < maxStatTime) {
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
