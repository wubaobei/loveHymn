package pri.prepare.lovehymn.client;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.DisplayStat;
import pri.prepare.lovehymn.client.tool.IntentHelper;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.client.tool.VolumeUtil;
import pri.prepare.lovehymn.databinding.ActivityMp3ListBinding;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.Book;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.LabelType;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.Setting;
import pri.prepare.lovehymn.server.function.CharConst;
import pri.prepare.lovehymn.server.function.DBHelper;
import pri.prepare.lovehymn.server.function.MusicManager;

public class Mp3ListActivity extends AppCompatActivity {
    private ActivityMp3ListBinding binding;
    private MusicManager musicManager = null;
    IntentHelper helper;
    /**
     * 加载中标记
     */
    private boolean isLoadingFlag = true;
    private String lastString = "";
    private VolumeUtil volumeUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBHelper.init(Mp3ListActivity.this);
        Service.getC().setTheme(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mp3_list);
        Tool.showStatusBar(getWindow(), this);
        volumeUtil = new VolumeUtil(this);

        helper = new IntentHelper(getIntent());
        if (helper.error) {
            binding.mp3Title.setText("无最近播放列表");
            return;
        }

        TextView tv = new TextView(this);
        tv.setText("加载中...\r\n如果长时间卡在这个界面，请到‘设置’-‘其他设置’中关闭异步功能");
        binding.mp3List.addView(tv);

        if (Setting.getValueB(Setting.USE_ASYNC))
            new Thread(runnableInit).start();
        else
            run0();

        btnSet();
        btnUpdate();
        lyricBtnSet();
        jumpPdfSet();
        updateLyric();
        plusDecSet();

        binding.lyricLl.setVisibility(INVISIBLE);
        binding.lyricLs.setVisibility(INVISIBLE);

        listSvSet();
    }

    /**
     * lyricLl设置
     */
    @SuppressLint("ClickableViewAccessibility")
    private void listSvSet() {
        binding.listSv.setOnTouchListener((view, motionEvent) -> {
            binding.hideNow.callOnClick();
            return false;
        });
    }

    private static final int LABEL = 0;
    private static final int BOOK = 1;

    private int getType() {
        if (helper.isLabel())
            return LABEL;
        return BOOK;
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void plusDecSet() {
        lyricShowSetTextSize(Setting.getValueI(Setting.LYRIC_SIZE));
        binding.sizeD.setOnClickListener(v -> {
            int s = Setting.getValueI(Setting.LYRIC_SIZE);
            if (s > 13) {
                s--;
                Setting.updateSetting(Setting.LYRIC_SIZE, s);
                lyricShowSetTextSize(s);
            }
        });
        binding.sizeP.setOnClickListener(v -> {
            int s = Setting.getValueI(Setting.LYRIC_SIZE);
            if (s < 35) {
                s++;
                Setting.updateSetting(Setting.LYRIC_SIZE, s);
                lyricShowSetTextSize(s);
            }
        });
        binding.hideNow.setOnClickListener(v -> lyricVisible(HIDE_NOW));
        binding.showNow.setOnClickListener(v -> lyricVisible(VISIBLE));
    }

    @SuppressLint("SetTextI18n")
    private void lyricBtnSet() {
        binding.lyricTv.setText(CharConst.MUSIC + binding.lyricTv.getText().toString());
        binding.lyricTv.setOnClickListener(lyricListener);
    }

    @SuppressLint("SetTextI18n")
    private void jumpPdfSet() {
        binding.jumpTv.setText(CharConst.BOOK + binding.jumpTv.getText().toString());
        binding.jumpTv.setOnClickListener(v -> {
            if (musicManager != null) {
                MainActivity.jumpFromMp3List = musicManager.getMp3Path();
                this.finish();
            } else {
                toast("还未选择MP3，无法跳转");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateLabelCount(String s) {
        String t = s.substring(0, s.indexOf(" ")).trim();
        Hymn h = Hymn.search(t);
        if (h.getLabels().length == 0) {
            binding.addToLable.setText(CharConst.LABEL + "标签");
        } else {
            SpannableString ss = new SpannableString(CharConst.LABEL + "标签x" + h.getLabels().length);
            ForegroundColorSpan bc = new ForegroundColorSpan(Color.parseColor("#ff0000"));
            ss.setSpan(bc, ss.length() - 2, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            binding.addToLable.setText(ss);
        }
    }

    private static final int VISIBLE = View.VISIBLE;
    private static final int INVISIBLE = View.INVISIBLE;
    private static final int HIDE_NOW = 333;
    private int lyricStatus = INVISIBLE;

    private void lyricVisible(int hide) {
        if (lyricStatus == hide)
            return;

        if (hide == HIDE_NOW) {
            if (lyricStatus == VISIBLE) {
                binding.lyricLl.setAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
                binding.lyricLl.setVisibility(INVISIBLE);
                binding.lyricLs.setAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
                binding.lyricLs.setVisibility(VISIBLE);
            }
        } else if (hide == VISIBLE) {
            if (lyricStatus == INVISIBLE) {
                binding.lyricLl.setAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
                binding.lyricLl.setVisibility(VISIBLE);
            } else if (lyricStatus == HIDE_NOW) {
                binding.lyricLl.setAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
                binding.lyricLl.setVisibility(VISIBLE);
                binding.lyricLs.setAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
                binding.lyricLs.setVisibility(INVISIBLE);
            }
        } else if (hide == INVISIBLE) {
            if (lyricStatus == HIDE_NOW) {
                binding.lyricLs.setAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
                binding.lyricLs.setVisibility(INVISIBLE);
            } else if (lyricStatus == VISIBLE) {
                binding.lyricLl.setAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
                binding.lyricLl.setVisibility(INVISIBLE);
            }
        }

        lyricStatus = hide;
    }

    private void updateLyric() {
        boolean sl = Setting.getValueB(Setting.SHOW_LYRIC);
        if (sl) {
            if (musicManager == null) {
                lyricVisible(INVISIBLE);
                return;
            }
            lyricVisible(VISIBLE);
            MyFile file = MyFile.from(musicManager.getMp3Path());
            Hymn hymn = file.getHymn();
            if (hymn != null) {
                lyricShowSetText(hymn.getLyric());
                Logger.info("show lyric:" + hymn.getLyric().length());
            } else
                lyricShowSetText("未找到对应诗歌");
        } else {
            lyricVisible(INVISIBLE);
        }
    }

    View.OnClickListener lyricListener = v -> {
        if (musicManager == null) {
            toast("还未选择MP3，无法切换");
            return;
        }
        boolean sl = Setting.getValueB(Setting.SHOW_LYRIC);
        Setting.updateSetting(Setting.SHOW_LYRIC, !sl);
        updateLyric();
        if (!sl)
            toast("显示歌词");
        else
            toast("隐藏歌词");
    };

    private static final String IS_LOADING = "(加载中)";

    @SuppressLint("SetTextI18n")
    private void run0() {
        try {
            if (getType() == LABEL) {
                LabelType lt = LabelType.getById(helper.getId());
                binding.mp3Title.setText(CharConst.LABEL + lt.getName() + IS_LOADING);
                binding.addToLable.setText(CharConst.LABEL + binding.addToLable.getText().toString());
                for (Hymn h : lt.getHymns()) {
                    if (h.getMp3File() != null) {
                        addMp3(h.getShowName(), h.getMp3File().getAbsolutePath());
                    }
                }
            } else if (getType() == BOOK) {
                Book book = Book.getById(helper.getId());
                if (book == null) {
                    throw new RuntimeException("找不到书 " + helper.getId());
                }
                binding.addToLable.setText(CharConst.LABEL + binding.addToLable.getText().toString());
                HashMap<String, String> map = Service.getC().getHymnNameBat(book);

                List<MyFile> list = new ArrayList<>();
                if (book == Book.ALL) {
                    list = new ArrayList<>();
                    Book[] bks = Book.getAll();
                    for (int i = bks.length - 1; i >= 0; i--) {
                        Book b = bks[i];
                        MyFile dir = b.getMp3Directory();
                        if (dir != null)
                            list.addAll(dir.getMp3List());
                    }
                } else {
                    MyFile file = book.getMp3Directory();
                    if (file != null)
                        list = file.getMp3List();
                }

                switch (helper.getType()) {
                    case IntentHelper.TYPE_NORMAL:
                        binding.mp3Title.setText(CharConst.BOOK + book.FullName + IS_LOADING);
                        break;
                    case IntentHelper.TYPE_BOOK_NO_LABEL:
                        binding.mp3Title.setText(CharConst.BOOK + book.FullName + " 无标签" + IS_LOADING);
                        break;
                    case IntentHelper.TYPE_BOOK_LABEL:
                        binding.mp3Title.setText(CharConst.BOOK + book.FullName + " 有标签" + IS_LOADING);
                        break;
                    case IntentHelper.TYPE_BOOK_LABEL_GROUP:
                        binding.mp3Title.setText(CharConst.BOOK + book.FullName + " 标签组:" + helper.getParam() + IS_LOADING);
                        break;
                }

                for (int i = list.size() - 1; i >= 0; i--) {
                    MyFile mp3File = list.get(i);
                    Hymn hymn = mp3File.getHymn();
                    if (hymn == null) {
                        continue;
                    }
                    if (helper.getType() == IntentHelper.TYPE_BOOK_NO_LABEL) {
                        if (hymn.getLabels().length > 0)
                            continue;
                    } else if (helper.getType() == IntentHelper.TYPE_BOOK_LABEL) {
                        if (hymn.getLabels().length == 0)
                            continue;
                    } else if (helper.getType() == IntentHelper.TYPE_BOOK_LABEL_GROUP) {
                        initGroupMap(helper.getParam());
                        if (!groupMap.contains(hymn.getId())) {
                            continue;
                        }
                    }
                    String path = mp3File.getAbsolutePath();
                    if (!map.containsKey(path))
                        Logger.info("找不到MP3对应的诗歌信息：" + path);
                    addMp3(map.getOrDefault(path, book.SimpleName + mp3File.getName() + " (未知)"), path);
                }
            }
            loadOver = true;
            File f = new File(helper.getPath());
            if (f.isFile() && playOnStart != null) {
                playOnStart.callOnClick();
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    /**
     * 异步加载所有MP3
     */
    private final Runnable runnableInit = this::run0;

    /**
     * 加载MP3完成
     */
    private boolean loadOver = false;
    private HashSet<Integer> groupMap = null;

    private void initGroupMap(String groupName) {
        if (groupMap != null)
            return;
        if (groupName == null || groupName.length() == 0)
            groupName = "0";
        groupMap = new HashSet<>();
        LabelType[] types = LabelType.getAll();
        for (LabelType lt : types) {
            if (lt.getGroup().equals(groupName)) {
                groupMap.addAll(lt.getHymnIds());
            }
        }
    }

    private void btnSet() {
        binding.playBtn.setImageResource(R.drawable.random_play);
        repeatBtnUpdate(Setting.getValueI(Setting.MP3_PLAYER_MODE));
        binding.previewBtn.setImageResource(R.drawable.preview);
        binding.nextBtn.setImageResource(R.drawable.next);

        binding.playBtnT.setOnClickListener(playListener);
        binding.playBtn.setOnClickListener(playListener);

        binding.repeatBtnT.setOnClickListener(repeatListener);
        binding.repeatBtn.setOnClickListener(repeatListener);

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicManager.setProgress(((double) seekBar.getProgress()) / seekBar.getMax());
                DisplayStat.getC().resetToolBar();
            }
        });

        binding.previewBtn.setOnClickListener(previewListener);
        binding.previewBtnT.setOnClickListener(previewListener);

        binding.nextBtn.setOnClickListener(nextListener);
        binding.nextBtnT.setOnClickListener(nextListener);

        binding.addToLable.setOnClickListener(v -> {
            if (musicManager == null) {
                toast("还未选择诗歌");
                return;
            }
            String path = musicManager.getMp3Path();
            Hymn hymn = MyFile.from(path).getHymn();
            try {
                List<String> arr = new ArrayList<>();
                arr.add(hymn.getId() + "");
                CommonListDialog cl = new CommonListDialog(Mp3ListActivity.this, 2, arr, null);
                cl.setOnDismissListener(dialogInterface -> updateLabelFlag = true);
                cl.showDialog();
            } catch (Exception e) {
                Logger.exception(e);
            }
        });
    }

    private boolean updateLabelFlag = false;
    private final List<String> nameList = new ArrayList<>();
    private final List<String> pathList = new ArrayList<>();
    private final List<TextView> mp3Added = new ArrayList<>();
    private int mp3AddIndex = 0;

    View.OnClickListener playListener = v -> {
        volumeUtil.setMediaVolumeIfZero();

        if (musicManager == null) {
            if (nameList.size() > 0 && mp3Added.size() == mp3AddIndex) {
                int n = new Random().nextInt(nameList.size());
                String path = pathList.get(n);
                String name = nameList.get(n);
                musicManager = new MusicManager(new File(path), name, nameList, pathList);
                musicManager.Play();
                btnUpdate();
                updateLyric();
            }
            return;
        }
        if (musicManager.isPlaying())
            musicManager.pause();
        else
            musicManager.Play();
        btnUpdate();
    };
    View.OnClickListener repeatListener = v -> {
        try {
            if (musicManager == null)
                return;
            musicManager.changeComplexRepeat();
            btnUpdate();
        } catch (Exception e) {
            Logger.exception(e);
        }
    };
    View.OnClickListener previewListener = v -> {
        if (musicManager == null)
            return;
        musicManager.previewMp3();
    };
    View.OnClickListener nextListener = v -> {
        if (musicManager == null)
            return;
        musicManager.nextMp3();
    };

    private void repeatBtnUpdate(int mode) {
        switch (mode) {
            case MusicManager.MODE_NO_REPEAT:
                binding.repeatBtnT.setText("不循环");
                binding.repeatBtn.setImageResource(R.drawable.one2);
                break;
            case MusicManager.MODE_REPEAT:
                binding.repeatBtnT.setText("单曲循环");
                binding.repeatBtn.setImageResource(R.drawable.repeat);
                break;
            case MusicManager.MODE_ORDER:
                binding.repeatBtnT.setText("顺序播放");
                binding.repeatBtn.setImageResource(R.drawable.loop);
                break;
            case MusicManager.MODE_RANDOM:
                binding.repeatBtnT.setText("随机播放");
                binding.repeatBtn.setImageResource(R.drawable.rand);
                break;
        }
    }

    private void btnUpdate() {
        if (musicManager == null)
            return;
        binding.playBtnT.setText(musicManager.isPlaying() ? "暂停" : "播放");
        binding.playBtn.setImageResource(musicManager.isPlaying() ? R.drawable.pause : R.drawable.play);
        repeatBtnUpdate(musicManager.getMode());
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(runnable);
    }

    private final HashMap<String, TextView> nameTextViewMap = new HashMap<>();

    private TextView playOnStart = null;

    private void addMp3(String name, String path) {
        nameList.add(name);
        pathList.add(path);

        TextView tv = new TextView(this);
        tv.setText(name);
        tv.setTextSize(17f);
        tv.setOnClickListener(v -> {
            try {
                if (musicManager == null) {
                    musicManager = new MusicManager(new File(path), name, nameList, pathList);
                }

                if (musicManager.getMp3Path().equals(path)) {
                    if (!musicManager.isPlaying())
                        musicManager.Play();
                    btnUpdate();
                    updateLyric();
                    return;
                }

                musicManager.changeMusic(path, name);
                musicManager.Play();
                btnUpdate();
                updateLyric();
            } catch (Exception e) {
                Logger.exception(e);
            }
        });
        if (helper.getPath().equalsIgnoreCase(path)) {
            playOnStart = tv;
        }
        mp3Added.add(tv);
        nameTextViewMap.put(name, tv);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRun = false;
        if (musicManager != null) {
            musicManager.release();
            musicManager = null;
        }
    }

    private boolean firstOverSetFlag = false;

    @SuppressLint("SetTextI18n")
    private void firstOverSet() {
        if (firstOverSetFlag)
            return;
        if (!loadOver)
            return;
        firstOverSetFlag = true;
        if (isLoadingFlag) {
            binding.mp3List.removeAllViews();
            TextView tv = new TextView(Mp3ListActivity.this.getApplicationContext());
            tv.setText("无诗歌mp3");
            binding.mp3List.addView(tv);
        }
        String title = binding.mp3Title.getText().toString();
        binding.mp3Title.setText(title.replace(IS_LOADING, "") + "(" + mp3Added.size() + ")");
        String cache = Setting.getValueS(Setting.MP3_COUNT_CACHE);
        StringBuilder newCache = new StringBuilder();
        for (String c : cache.split(";")) {
            String[] c2 = c.split(":");
            if (!c2[0].equals(helper.key())) {
                newCache.append(c).append(";");
            }
        }
        newCache.append(helper.key()).append(":").append(mp3Added.size());
        Setting.updateSetting(Setting.MP3_COUNT_CACHE, newCache.toString());
    }

    private boolean isRun = true;

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                this.update();
                int n = 1000 / DisplayStat.HZ;
                if (isRun)
                    handler.postDelayed(this, n);
            } catch (Exception e) {
                Logger.exception(e);
            }
        }

        @SuppressLint("SetTextI18n")
        void update() {
            try {
                int n = 1000 / DisplayStat.HZ;
                Thread.sleep(n);
            } catch (InterruptedException e) {
                Logger.exception(e);
            }

            int n = 0;

            while (mp3AddIndex < mp3Added.size() && n++ < 30) {
                if (isLoadingFlag) {
                    isLoadingFlag = false;
                    binding.mp3List.removeAllViews();
                }
                TextView tv = mp3Added.get(mp3AddIndex);
                mp3AddIndex++;
                if (tv.getParent() != null) {
                    Logger.info("发现异常：有parent " + tv.getText().toString());
                    break;
                }
                binding.mp3List.addView(tv);
            }

            firstOverSet();

            if (musicManager != null) {
                binding.progressText.setText(musicManager.getProgressString());
                ProgressBar pb = binding.seekBar;
                int pro = (int) (musicManager.getProgressPercent() * pb.getMax());
                pb.setProgress(pro);

                if (musicManager.isPlaying()) {
                    binding.mp3Title.setText("正在播放：" + musicManager.getMusicName());
                    if (musicManager.needUpdateLyric) {
                        updateLyric();
                        musicManager.needUpdateLyric = false;
                    }
                } else
                    binding.mp3Title.setText("暂停播放：" + musicManager.getMusicName());
            }

            if (musicManager != null && !musicManager.getMusicName().equals(lastString)) {
                TextView tv1 = nameTextViewMap.getOrDefault(lastString, null);
                if (tv1 != null) {
                    tv1.setTextColor(Color.BLACK);
                }
                lastString = musicManager.getMusicName();
                TextView tv2 = nameTextViewMap.getOrDefault(lastString, null);
                if (tv2 != null) {
                    tv2.setTextColor(Color.RED);
                }
                updateLabelCount(musicManager.getMusicName());
            } else if (musicManager != null && updateLabelFlag) {
                updateLabelFlag = false;
                updateLabelCount(musicManager.getMusicName());
            }
        }
    };

    //region lyricShow set
    private float defaultSize = -1f;
    List<TextView> lsTvs = new ArrayList<>();

    private void lyricShowSetTextSize(float f) {
        defaultSize = f;
        for (TextView t : lsTvs)
            t.setTextSize(f);
    }

    private void lyricShowSetText(String s) {
        binding.lyricShowList.removeAllViews();
        lsTvs.clear();
        for (String a : s.split("\n")) {
            if (a.trim().length() == 0) continue;
            TextView tv = new TextView(this);
            tv.setBackgroundColor(Color.parseColor("#ffffff"));
            if (defaultSize > 0)
                tv.setTextSize(defaultSize);
            tv.setPadding(10, 10, 10, 10);
            tv.setText(a.trim());
            binding.lyricShowList.addView(tv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            lsTvs.add(tv);
        }
    }

    //endregion
}
