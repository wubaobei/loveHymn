package pri.prepare.lovehymn.client.tool;

import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;

import pri.prepare.lovehymn.server.entity.Logger;

public class TouchUtil {
    public TouchUtil(WindowManager wm1) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm1.getDefaultDisplay().getMetrics(outMetrics);
        width = outMetrics.widthPixels;
        height = outMetrics.heightPixels;
    }

    private int width, height;

    public void set(WindowManager windowManager) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        width = outMetrics.widthPixels;
        height = outMetrics.heightPixels;
    }

    public void set(int height1, int height2) {
        h1 = height1;
        h2 = height2;
    }

    /**
     * 还未初始化高 宽
     */
    public boolean notSet() {
        return h1 == 0 && h2 == 0;
    }

    private long getMaxHW() {
        return Math.max(height, width);
    }

    private long getMinHW() {
        return Math.min(height, width);
    }

    //region 触摸中间位置
    private static final int dxy = 10;
    private static final double td = 300L;
    private boolean isCenter = false;
    private long time = 0L;
    private int h1 = 0, h2 = 0;
    private float x = 0f, y = 0f;

    private boolean getCenter() {
        return y > h1 && y < height - h2;
    }

    public boolean clickCenter(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //tn++;
            time = System.currentTimeMillis();
            x = ev.getX();
            y = ev.getY();
            isCenter = getCenter();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            //tn--;
            if (isCenter && System.currentTimeMillis() - time < td) {
                time = System.currentTimeMillis();

                if (Math.abs(x - ev.getX()) > dxy || Math.abs(y - ev.getY()) > dxy)
                    return false;

                x = ev.getX();
                y = ev.getY();
                isCenter = getCenter();
                return isCenter;
            }
        }
        return false;
    }

    //endregion

    //region 上一首 下一首
    private static final double MinK = 0.34d;
    private static final double MaxK = 0.1d;
    private static final long PNTime = 500;

    private static final double MinK2 = 0.22d;
    private static final long PNTime2 = 300;


    private float x2 = 0f, y2 = 0f;
    private long time2 = 0L;

    public int PreviewOrNext(MotionEvent ev) {
        maxN = Math.max(maxN, ev.getPointerCount());
        if (maxN > 1) {
            if (ev.getPointerCount() == 1 && ev.getAction() == MotionEvent.ACTION_UP)
                maxN = 0;
            return 0;
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            jumpFlag = false;
            time2 = System.currentTimeMillis();
            x2 = ev.getX();
            y2 = ev.getY();
            return 0;
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (jumpFlag)
                return 0;
            maxN = 0;
            if (Math.abs(ev.getY() - y2) > getMaxHW() * MaxK)
                return 0;
            if ((System.currentTimeMillis() - time2 <= PNTime && Math.abs(ev.getX() - x2) >= getMinHW() * MinK)
                    || (System.currentTimeMillis() - time2 <= PNTime2 && Math.abs(ev.getX() - x2) >= getMinHW() * MinK2))
                return ev.getX() < x2 ? 1 : -1;
        }
        return 0;
    }
    //endregion

    //region 三指触控

    private static final double t3K = 0.2;
    private boolean firstThree = false;
    private boolean threeAct = false;
    private double xt, yt;
    private int maxN = 0;

    public static final int THREE_UP = 10;
    public static final int THREE_DOWN = 11;
    public static final int THREE_LEFT = 12;
    public static final int THREE_RIGHT = 13;

    /**
     * 三指上滑
     *
     * @param ev
     * @return
     */
    public int PressThree(MotionEvent ev) {
        maxN = Math.max(maxN, ev.getPointerCount());
        if (ev.getPointerCount() < 3) {
            firstThree = false;
            threeAct = false;
        } else if (ev.getPointerCount() == 3) {
            if (!firstThree) {
                firstThree = true;
                xt = ev.getX(0) + ev.getX(1) + ev.getX(2);
                yt = ev.getY(0) + ev.getY(1) + ev.getY(2);
            } else if (!threeAct) {
                double xTemp = ev.getX(0) + ev.getX(1) + ev.getX(2);
                double yTemp = ev.getY(0) + ev.getY(1) + ev.getY(2);
                if (xTemp - xt > getMinHW() * t3K * 3) {
                    threeAct = true;
                    firstThree = false;
                    return THREE_RIGHT;
                } else if (xt - xTemp > getMinHW() * t3K * 3) {
                    threeAct = true;
                    firstThree = false;
                    return THREE_LEFT;
                } else if (yt - yTemp > getMinHW() * t3K * 3) {
                    threeAct = true;
                    firstThree = false;
                    return THREE_UP;
                } else if (yTemp - yt > getMinHW() * t3K * 3) {
                    threeAct = true;
                    firstThree = false;
                    return THREE_DOWN;
                }
            }
        }

        return 0;
    }
    //endregion

    //region 双指触控

    //private static final double t3K = 0.2;
    private boolean firstDouble = false;
    private boolean doubleAct = false;
    private double xtD, ytD;

    public static final int DOUBLE_UP = 210;
    public static final int DOUBLE_DOWN = 211;
    public static final int DOUBLE_LEFT = 212;
    public static final int DOUBLE_RIGHT = 213;

    /**
     * 双指滑动
     *
     * @param ev
     * @return
     */
    public int PressDouble(MotionEvent ev) {
        maxN = Math.max(maxN, ev.getPointerCount());
        if (ev.getPointerCount() < 2) {
            firstDouble = false;
            doubleAct = false;
        } else if (ev.getPointerCount() > 2) {
            doubleAct = true;
        } else if (ev.getPointerCount() == 2) {
            if (!firstDouble) {
                firstDouble = true;
                xtD = ev.getX(0) + ev.getX(1);
                ytD = ev.getY(0) + ev.getY(1);
            } else if (!doubleAct) {
                double xTemp = ev.getX(0) + ev.getX(1);
                double yTemp = ev.getY(0) + ev.getY(1);
                if (xTemp - xtD > getMinHW() * t3K * 3) {
                    doubleAct = true;
                    firstDouble = false;
                    return DOUBLE_RIGHT;
                } else if (xtD - xTemp > getMinHW() * t3K * 3) {
                    doubleAct = true;
                    firstDouble = false;
                    return DOUBLE_LEFT;
                } else if (ytD - yTemp > getMinHW() * t3K * 3) {
                    doubleAct = true;
                    firstDouble = false;
                    return DOUBLE_UP;
                } else if (yTemp - ytD > getMinHW() * t3K * 3) {
                    doubleAct = true;
                    firstDouble = false;
                    return DOUBLE_DOWN;
                }
            }
        }

        return 0;
    }
    //endregion

    //region 长按
    private long downT = 0;
    private float ldx, ldy;
    //超过该时间认为是长按
    private static final int LONG_PRESS_TIME = 500;
    private static final int LONG_PRESS_TIME2 = 1000;

    public boolean isLongPress() {
        boolean r = downT > 0 && System.currentTimeMillis() - downT > LONG_PRESS_TIME && System.currentTimeMillis() - downT < LONG_PRESS_TIME2;
        if (r) {
            downT = 0;
            Logger.info("long press");
        }
        return r;
    }

    public boolean LongPress(MotionEvent ev) {
        if (ev.getPointerCount() > 1) {
            downT = 0;
            return false;
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            downT = System.currentTimeMillis();
            ldx = ev.getX();
            ldy = ev.getY();
            return false;
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            downT = 0;
        } else {
            if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                float cx = ev.getX();
                float cy = ev.getY();
                if ((cx - ldx) * (cx - ldx) + (cy - ldy) * (cy - ldy) > getMinHW() * getMinHW() / 900)
                    downT = 0;
            } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                downT = 0;
            }

            if (downT == 0)
                return false;
            if (System.currentTimeMillis() - downT > LONG_PRESS_TIME && System.currentTimeMillis() - downT < LONG_PRESS_TIME2) {
                downT = 0;
                return true;
            }
        }

        return false;
    }

    private boolean jumpFlag = false;

    public void jumpThis() {
        jumpFlag = true;
    }
    //endregion
}
