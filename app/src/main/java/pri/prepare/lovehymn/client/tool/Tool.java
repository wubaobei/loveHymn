package pri.prepare.lovehymn.client.tool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.CatalogDialog;
import pri.prepare.lovehymn.client.CommonListDialog;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MyFile;

public class Tool {

    public static SpannableString getSpannableString(String s, String[] aims) {
        SpannableString res = new SpannableString(s);
        if (aims == null || aims.length == 0)
            return res;
        for (String aim : aims) {
            int ind = s.indexOf(aim);
            if (ind >= 0)
                res.setSpan(new ForegroundColorSpan(Color.parseColor("#ff3c2a")), ind, ind + aim.length(), 0);
        }
        return res;
    }

    public static void setSearchLink(View tv, final String searchStr, final I4Intro listener, final Dialog dialog) {
        if (tv instanceof TextView) {
            ((TextView) tv).getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        }

        tv.setOnFocusChangeListener((v, hasFocus) -> {
            Logger.info("click foc");
            listener.searchStringCall(searchStr);
            dialog.dismiss();
        });
        tv.setOnClickListener(v -> {
            Logger.info("click sl");
            listener.searchStringCall(searchStr);
            dialog.dismiss();
        });
    }

    /**
     * 目录 设置 详情界面dialog
     */
    public static void DialogSet(Dialog dialog) {
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.mydialog);
    }

    /**
     * 纯文本dialog
     */
    public static void ShowDialog(Context context, String title, String value) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        Window window = alertDialog.getWindow();
        window.setBackgroundDrawable(new BitmapDrawable());
        window.setBackgroundDrawableResource(R.drawable.simpledialog);
        alertDialog.setTitle(title);
        alertDialog.setMessage(toColorSpan(value, Color.RED));
        alertDialog.show();
    }
    public static void ShowDialog(Context context, String title, String[] values){
        ShowDialog(context,title,values,-1);
    }
    /**
     * 纯文本dialog
     */
    public static void ShowDialog(Context context, String title, String[] values,int res) {
        List<String> c = new ArrayList<>();
        c.add(title);
        Collections.addAll(c, values);
        CommonListDialog dialog = new CommonListDialog(context, 3, c,null,res);
        dialog.showDialog();
    }

    public static LinearLayout.LayoutParams getMarginLayoutTop() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
        int lr = 20;
        lp.setMargins(lr, 10, lr, 0);
        return lp;
    }

    public static LinearLayout.LayoutParams getMarginLayoutBottom() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
        int lr = 20;
        lp.setMargins(lr, 0, lr, 10);
        return lp;
    }

    public static LinearLayout.LayoutParams getMarginLayout() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
        int lr = 20;
        int tb = 10;
        lp.setMargins(lr, tb, lr, tb);
        return lp;
    }

    public static LinearLayout.LayoutParams getMarginLayout(int lr, int tb) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
        lp.setMargins(lr, tb, lr, tb);
        return lp;
    }

    public static LinearLayout.LayoutParams getMarginLayoutOnlyLeft(int tcb) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
        int lr = 20;
        int t = tcb == CatalogDialog.TOP ? 10 : 0;
        int b = tcb == CatalogDialog.BOTTOM ? 10 : 0;
        lp.setMargins(lr, t, 0, b);
        return lp;
    }

    public static void setBtnWidth(Button btn, int width) {
        btn.setMinimumWidth(width);
        btn.setMinWidth(width);
        btn.setWidth(width);
    }

    private static final int ICON_SIZE = 70;

    /**
     * 左边加图标
     */
    public static void drawableLeftSet(TextView tv, Context ct, int i) {
        drawableLeftRightSet(tv, ct, i, -1);
    }

    /**
     * 右边加图标
     */
    public static void drawableRightSet(TextView tv, Context ct, int i) {
        drawableLeftRightSet(tv, ct, -1, i);
    }

    /**
     * 左右两边都加上图标，目前仅广告使用
     *
     * @param tv
     * @param ct
     */
    public static void drawableLeftRightSet(TextView tv, Context ct, int i1, int i2) {
        Drawable drawable1 = i1 == -1 ? null : ct.getResources().getDrawable(i1);
        Drawable drawable2 = i2 == -1 ? null : ct.getResources().getDrawable(i2);
        if (drawable1 != null)
            drawable1.setBounds(0, 0, ICON_SIZE, ICON_SIZE);
        if (drawable2 != null)
            drawable2.setBounds(0, 0, ICON_SIZE, ICON_SIZE);
        tv.setCompoundDrawables(drawable1, null, drawable2, null);
    }

    /**
     * 出现空指针时提示重启app
     */
    public static void toastRestart(Context context, Exception e) {
        if (e.getMessage().contains("null"))
            Toast.makeText(context, "出了点小问题，请重启app", Toast.LENGTH_LONG);
    }

    private static Random random = null;

    public static int[] randomList(int n) {
        if (random == null)
            random = new Random();
        int[] arr = new int[n];
        for (int i = 0; i < arr.length; i++)
            arr[i] = i;
        for (int i = 0; i < 1000; i++) {
            int r1 = random.nextInt(n);
            int r2 = random.nextInt(n);
            if (r1 != r2) {
                int t = arr[r1];
                arr[r1] = arr[r2];
                arr[r2] = t;
            }
        }
        return arr;
    }

    public static void openInfo(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivity(intent);
    }

    public static final int ANIM_NORMAL = 0;
    public static final int ANIM_TOP = 1;
    public static final int ANIM_BOTTOM = 2;

    public static void setAnim(Window window, int ind) {
        if (window == null) {
            Logger.info("window==null exit");
            return;
        }
        if (ind == ANIM_NORMAL)
            window.setWindowAnimations(R.style.DialogIOSAnim);
        else if (ind == ANIM_TOP)
            window.setWindowAnimations(R.style.DialogTopAnim);
        else window.setWindowAnimations(R.style.DialogBottomAnim);
    }

    //全屏并且状态栏透明显示
    public static void showStatusBar(Window window, Activity activity) {
        WindowManager.LayoutParams attrs = window.getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setAttributes(attrs);
        setStatusBarColor(activity, Color.argb(255, 0xcc, 0xcc, 0xcc));
    }

    private static void setStatusBarColor(Activity activity, int statusColor) {
        Window window = activity.getWindow();
        //取消状态栏透明
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(statusColor);
        //设置系统状态栏处于可见状态
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        //让view不根据系统窗口来调整自己的布局
        ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
            ViewCompat.requestApplyInsets(mChildView);
        }
    }


    /**
     * 变色
     */
    public static CharSequence toColorSpan(String charSequence, int color) {
        int k = 0;
        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        for (int i = 0; i < charSequence.length(); i++) {
            if (charSequence.charAt(i) == '{') {
                starts.add(i - k);
                k++;
            } else if (charSequence.charAt(i) == '}') {
                ends.add(i - k);
                k++;
            }
        }
        SpannableString spannableString = new SpannableString(charSequence.replace("{", "").replace("}", ""));

        if (starts.size() == ends.size()) {
            for (int i = 0; i < starts.size(); i++)
                spannableString.setSpan(
                        new ForegroundColorSpan(color),
                        starts.get(i),
                        ends.get(i),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

    public static void setListDialogLayout(TextView title, Context context, int r, Button... buttons) {
        title.setTextSize(28f);
        drawableLeftSet(title, context, r);

//        int n = 0;
//        for (Button btn : buttons) {
//            if (n % 2 == 0)
//                btn.setTextColor(Color.BLUE);
//            else
//                btn.setTextColor(Color.RED);
//            n++;
//        }
    }
}
