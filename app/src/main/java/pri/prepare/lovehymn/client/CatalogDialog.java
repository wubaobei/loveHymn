package pri.prepare.lovehymn.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.I4Catalog;
import pri.prepare.lovehymn.client.tool.I4StopMp3;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.ImmTool;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.CatelogLayoutBinding;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.Setting;
import pri.prepare.lovehymn.server.function.CharConst;
import pri.prepare.lovehymn.server.function.Constant;
import pri.prepare.lovehymn.server.result.ShowResult;
import pri.prepare.lovehymn.server.function.SdCardTool;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.Book;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.Logger;

/**
 * 目录
 */
public class CatalogDialog extends Dialog implements IShowDialog {

    private CatelogLayoutBinding binding;
    private final LinearLayout ll;
    private final LinearLayout numLL;
    private final I4Catalog listener;
    private final Activity activity;
    private final Hymn currentHymn;
    private I4StopMp3 i4StopMp3;

    private static String hintRecord = "";

    public CatalogDialog(@NonNull Context context, I4Catalog lis, String searchString, I4StopMp3 i4StopMp3, Hymn hymn) {
        super(context);
        currentHymn = hymn;
        activity = (Activity) context;
        this.i4StopMp3 = i4StopMp3;
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.catelog_layout, null, false);
        setContentView(binding.getRoot());
        if (searchString != null && searchString.length() > 0)
            Logger.info("searchString " + searchString);
        ll = binding.detLayout;
        numLL = binding.numDirShow;

        String hintV = null;
        if (hintRecord != null && hintRecord.length() > 0)
            hintV = hintRecord;

        setOkBtnOrSearch(searchString, hintV);
        setDetLayout(MyFile.from(SdCardTool.getLbPath()));

        if (searchString != null && searchString.length() > 0)
            setReturnBtn(false);

        this.listener = lis;
        setSearchView();
        setBookId(-1);

        ImageButton btn = binding.returnBtn;
        ViewGroup.LayoutParams lp = btn.getLayoutParams();
        if (lp != null) {
            lp.width = 70;
            lp.height = 70;
            btn.setLayoutParams(lp);
        }
    }

    private int _bookId;

    private int page = 0;
    private boolean beginLoad = false;
    private TextView lastView = null;
    private ShowResult[] pageRes = null;

    private ImmTool immTool;

    /**
     * 滚动条设置
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setSearchView() {
        final ScrollView sv = binding.showSearchView;
        sv.setOnTouchListener((v, event) -> {
            try {
                immTool.closeImmIfOpen();

                if (page < 0)
                    return false;
                LinearLayout ll = binding.detLayout;
                if (page < 1000 && !beginLoad && sv.getScrollY() + sv.getHeight() + Constant.SEARCH_RESULT_MORE_Y > ll.getHeight()) {
                    beginLoad = true;
                    page++;
                    new Thread(searchPageR).start();
                }
                return false;
            } catch (Exception e) {
                Logger.exception(e);
                return false;
            }
        });
    }

    final Runnable searchPageR = new Runnable() {
        @Override
        public void run() {
            try {
                EditText et = binding.etSearch;
                pageRes = SdCardTool.search(et.getText().toString(), page, getBookId());
                if (pageRes.length > 0) {
                    searchPageH.sendEmptyMessage(page);
                } else {
                    page = 1000;
                }
            } catch (Exception e) {
                Logger.exception(e);
            }
        }
    };
    @SuppressLint("HandlerLeak")
    final Handler searchPageH = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LinearLayout ll = binding.detLayout;
            if (lastView != null)
                ll.removeView(lastView);
            for (ShowResult sr : pageRes) {
                setSearchBtn(sr, ll);
            }
            pageRes = null;
            new Thread(loadR).start();
            super.handleMessage(msg);
        }
    };

    final Runnable loadR = () -> {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        beginLoad = false;
    };

    private static final String LONG_CLICK_TIG = "长按搜索:";

    boolean hitPas = false;

    private void setHint(TextView tv, String value) {
        if (hitPas) {
            hitPas = false;
            return;
        }
        if (tv == null)
            tv = binding.etSearch;
        tv.setHint(LONG_CLICK_TIG + (value == null ? Constant.getRandomSearchStr() : value));
    }

    private void setOkBtnOrSearch(String searchString, String hintV) {
        immTool = new ImmTool(getContext(), getWindow());
        EditText et = binding.etSearch;
        et.setText(searchString);
        et.setEnabled(true);
        setHint(et, hintV);
        Tool.drawableLeftSet(et, getContext(), R.drawable.sc_search);

        et.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        page = 0;
                        setHint(null, null);
                        setDetLayout(MyFile.from(SdCardTool.getLbPath()));
                        setReturnBtn(et.getText().toString().isEmpty());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
        et.setOnLongClickListener(v -> {
            EditText et1 = binding.etSearch;
            if (et1.getText() == null || et1.getText().length() == 0) {
                if (et1.getHint().toString().startsWith(LONG_CLICK_TIG)) {
                    et1.setText(et1.getHint().toString().substring(LONG_CLICK_TIG.length()));
                    return true;
                }
            }
            return false;
        });
    }

    private void setDetLayout(MyFile f) {
        setDetLayout(f, false);
    }

    private String lastSearchS = "";

    private void setReturnBtn(boolean isRoot) {
        ImageButton btn = binding.returnBtn;
        if (isRoot) {
            btn.setBackgroundResource(R.drawable.music);
            btn.setOnClickListener(mp3Listener);
        } else {
            btn.setBackgroundResource(R.drawable.clear);
            btn.setOnClickListener(backListener);
        }
    }

    View.OnClickListener backListener = v -> {
        EditText et = binding.etSearch;
        et.setText("");
    };

    List<String> mp3List = null;

    private void startMp3() {
        try {
            mp3List = Service.getC().getMp3C();
            if (mp3List != null && mp3List.size() > 0) {
                CommonListDialog cl = new CommonListDialog(activity, 1, mp3List, i4StopMp3, currentHymn);
                cl.showDialog();
            } else {
                Toast.makeText(getContext(), "未找到mp3资源", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    View.OnClickListener mp3Listener = v -> {
        try {
            CatalogDialog.this.dismiss();
            startMp3();
        } catch (Exception e) {
            Logger.exception(e);
        }
    };

    private void setDetLayout(MyFile f, boolean autoOpen) {
        long t1 = System.currentTimeMillis();
        String ss = binding.etSearch.getText().toString().trim();
        if (lastSearchS.equals(ss) && ss.length() > 0) {
            return;
        }
        lastSearchS = ss;

        ll.removeAllViews();

        if (f == null || f.length() == 0)
            return;

        if (ss.length() > 0) {
            numLL.removeAllViews();
            try {
                ShowResult[] temp = SdCardTool.search(ss, 0, getBookId());
                if (temp.length == 0) {
                    Logger.info("找不到任何匹配的诗歌：'" + ss + "' " + getBookId() + " " + SdCardTool.searchType);
                    ShowResult r = new ShowResult("找不到任何匹配的诗歌");
                    setSearchBtn(r, ll);
                    return;
                }
                if (temp.length == 1 && temp[0].file == null) {
                    Logger.info("找不到任何匹配的诗歌：'" + ss + "' " + getBookId() + " " + SdCardTool.searchType);
                }
                setReturnBtn(false);

                binding.showSearchView.scrollTo(0, 0);
                Logger.info("search '" + ss + "' res count:" + temp.length);
                for (ShowResult sr : temp) {
                    setSearchBtn(sr, ll);
                }
                return;
            } catch (Exception e) {
                Logger.exception(e);
                return;
            }
        }

        page = -1;
        LinearLayout lt;
        boolean isRoot = f.getAbsolutePath().equals(SdCardTool.getLbPath());
        boolean isNumberBtn = f.isDirectory() && Service.isInteger(f.getName());
        if (isRoot) {
            setBookId(-1);
            numLL.removeAllViews();
            setReturnBtn(isRoot);

            lt = ll;
        } else {
            ImageButton btn = binding.returnBtn;
            btn.setBackgroundResource(R.drawable.home);
            btn.setOnClickListener(v -> setDetLayout(MyFile.from(SdCardTool.getLbPath())));
            if (isNumberBtn)
                lt = ll;
            else
                lt = numLL;
        }
        MyFile[] fileList = f.listFiles();
        ArrayList<MyFile> showFileList = new ArrayList<>();
        for (MyFile file : Service.getC().orderFiles(fileList)) {
            if (Setting.getValueB(Setting.HIDE_QING) && file.getName().equals(Book.Qing.FullName)) {
                continue;
            }
            //跳过资源文件 隐藏文件 MP3 白版
            if (!file.getName().equals(Constant.RES_NAME) && !(file.getName().startsWith("."))
                    && !(file.getName().endsWith("mp3")) && !(file.getName().equals(Constant.WHITE))
                    && (!isRoot || file.getName().length() > 1) && (!file.getName().contains("hide"))) {
                showFileList.add(file);
            }
        }
        int n = 0;
        Button btnF = null;

        lazyLoadData = new ArrayList<>();
        lazyLoadFlag = true;
        for (int i = Constant.FIRST_LOAD_COUNT; i < showFileList.size(); i++)
            lazyLoadData.add(showFileList.get(i));

        MyFile.loadHymnBat(showFileList);
        for (int i = 0; i < Constant.FIRST_LOAD_COUNT && i < showFileList.size(); i++) {
            MyFile fn = showFileList.get(i);
            ShowResult sr = new ShowResult(fn);
            if (n == 0)
                btnF = setSearchBtn(sr, lt, TOP, isRoot);
            else if (n == showFileList.size() - 1)
                setSearchBtn(sr, lt, BOTTOM, isRoot);
            else
                setSearchBtn(sr, lt, NORMAL, isRoot);
            n++;
        }

        //自动加载第一个文件夹
        if (autoOpen && showFileList.size() > 0) {
            lastChooseDirBtn = btnF;
            btnF.setBackgroundResource(getBkRes(TOP, true));
            setDetLayout(showFileList.get(0));
        }

        if (System.currentTimeMillis() - t1 > 100)
            Logger.info("load dir " + f.getName() + " cost " + (System.currentTimeMillis() - t1));
        if (lazyLoadData.size() > 0 && lazyLoadFlag) {
            lazyLoadFlag = false;
            new Thread(lazyLoadFileR).start();
        }
    }

    private ArrayList<MyFile> lazyLoadData = null;
    @SuppressWarnings("FieldCanBeLocal")
    private boolean lazyLoadFlag = false;

    final Runnable lazyLoadFileR = new Runnable() {
        @Override
        public void run() {
            lazyLoadFileH.sendEmptyMessage(0);
        }
    };

    @SuppressLint("HandlerLeak")
    final Handler lazyLoadFileH = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (lazyLoadData == null) return;
            long t1 = System.currentTimeMillis();
            for (int i = 0; i < lazyLoadData.size(); i++) {
                MyFile fn = lazyLoadData.get(i);
                ShowResult sr = new ShowResult(fn);
                setSearchBtn(sr, ll, BOTTOM, false);
            }
            Logger.info("lazy load cost: " + (System.currentTimeMillis() - t1));
        }
    };

    private int getBkRes(int position, boolean choose) {
        if (!choose) {
            if (position == TOP)
                return R.drawable.btn_num_top;
            if (position == NORMAL)
                return R.drawable.btn_num_normal;
            return R.drawable.btn_num_bottom;
        }
        if (position == TOP)
            return R.drawable.btn_num_top_choose;
        if (position == NORMAL)
            return R.drawable.btn_num_normal_choose;
        return R.drawable.btn_num_bottom_choose;
    }

    private Button lastChooseDirBtn = null;

    private final HashMap<String, Integer> numMap = new HashMap<>();

    private void setSearchBtn(ShowResult sr, LinearLayout ll) {
        setSearchBtn(sr, ll, 0, false);
    }

    /**
     * 设置按钮样式，包括书名按钮，数字按钮，诗歌按钮及说明
     */
    @SuppressLint("SetTextI18n")
    private Button setSearchBtn(ShowResult sr, LinearLayout ll, int tcb, final boolean autoOpen) {
        if (sr.file == null) {
            TextView tv = new TextView(getContext());
            ll.addView(tv, Tool.getMarginLayout());
            tv.setTextColor(Color.RED);
            tv.setText(sr.showStr);
            lastView = tv;
            //Logger.info("setSearchBtn:sr.file == null");
            return null;
        }

        final MyFile f = sr.file;
        if (f.isFile() && !f.isPdf()) {
            Logger.info("setSearchBtn:f.isFile() && !f.isPdf()" + f.getAbsolutePath());
            return null;
        }
        if (!f.exists()) {
            Logger.info("setSearchBtn:!f.exists()");
            return null;
        }

        boolean showText = sr.showStr != null && sr.showStr.length() > 0;

        final MyFile ff = sr.file;
        if (!showText) {
            Hymn hymn = ff.getHymn();
            if (hymn != null) {
                String sl = hymn.getShortLyric();
                if (sl.length() > 0) {
                    showText = true;
                    sr.showStr = sl;
                }
            }
        }

        //平均耗时0.5ms
        final Button btn = new Button(getContext());
        btn.setAllCaps(false);
        if (f.isDirectory() && Service.isInteger(f.getName())) {
            btn.setGravity(Gravity.CENTER);
            Tool.setBtnWidth(btn, Constant.btnMinWidth);
            numMap.put(f.getName(), tcb);
            btn.setBackgroundResource(getBkRes(tcb, false));
            ll.addView(btn, Tool.getMarginLayoutOnlyLeft(tcb));
        } else {
            btn.setGravity(Gravity.CENTER_VERTICAL);
            ll.addView(btn, showText ? Tool.getMarginLayoutTop() : Tool.getMarginLayout());
            btn.setBackgroundResource(showText ? R.drawable.btn_shape_up : R.drawable.btn_shape);
        }
        if (f.isDirectory()) {
            if (Service.isInteger(f.getName())) {
                if (f.getName().equals(Constant.SUBJOIN_DIR_NAME))
                    btn.setText(Constant.SUBJOIN_DIR_RENAME);
                else
                    btn.setText(f.getName());
            } else
                btn.setText(CharConst.DIR + f.getName());

            btn.setOnClickListener(v -> {
                try {
                    if (autoOpen) {
                        for (Book b : Book.getAll())
                            if (b.FullName.equals(ff.getName()))
                                setBookId(b.id);
                    }
                    if (Service.isInteger(f.getName())) {
                        if (lastChooseDirBtn != null) {
                            int r = numMap.getOrDefault(lastChooseDirBtn.getText(), NORMAL);
                            if (lastChooseDirBtn.getText().equals(Constant.SUBJOIN_DIR_RENAME))
                                r = BOTTOM;
                            lastChooseDirBtn.setBackgroundResource(getBkRes(r, false));
                        }
                        lastChooseDirBtn = btn;

                        int r2 = numMap.getOrDefault(f.getName(), NORMAL);
                        btn.setBackgroundResource(getBkRes(r2, true));
                    }
                    setDetLayout(ff, autoOpen);
                } catch (Exception e) {
                    Logger.exception(e);
                }
            });
            return btn;
        }
        btn.setText((sr.highLight ? "⭐" : "") + Service.getC().getNameAfterDeal(ff, false));
        btn.setOnClickListener(v -> {
            TextView tv = binding.etSearch;
            String et = tv.getText().toString();
            if (!et.equals(Constant.STR_COLLECT) && !et.equals(Constant.STR_HISTORY))
                hintRecord = et;
            listener.loadPdfCall(ff.getAbsolutePath());
            CatalogDialog.this.dismiss();
        });
        if (showText) {
            TextView tv = new TextView(getContext());
            tv.setBackgroundResource(R.drawable.btn_shape_down);
            ll.addView(tv, Tool.getMarginLayoutBottom());
            tv.setText(Tool.getSpannableString(sr.showStr, sr.lightStr));
            tv.setOnClickListener(v -> btn.callOnClick());
        }
        return btn;
    }

    public static final int TOP = 1;
    public static final int NORMAL = 2;
    public static final int BOTTOM = 3;

    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(true);
        Tool.DialogSet(this);
        show();
    }

    public int getBookId() {
        return _bookId;
    }

    public void setBookId(int _bookId) {
        this._bookId = _bookId;
        if (_bookId == -1)
            binding.bookIn.setText("");
        else
            binding.bookIn.setText(Book.getById(_bookId).FullName);
    }
}
