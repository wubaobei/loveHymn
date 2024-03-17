package pri.prepare.lovehymn.client;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.SpeckBibleStruct;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.client.tool.VolumeUtil;
import pri.prepare.lovehymn.databinding.ActivityDailyBibleBinding;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.Setting;
import pri.prepare.lovehymn.server.function.BibleTool;
import pri.prepare.lovehymn.server.function.DBHelper;
import pri.prepare.lovehymn.server.function.WebHelper;

public class DailyBibleActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    ActivityDailyBibleBinding binding;

    TextToSpeech tts = null;
    private static final int COLOR_DEFAULT = Color.BLACK;
    private static final int COLOR_HAS_READ = Color.GRAY;
    private static final int COLOR_READING = Color.BLUE;
    private static final int COLOR_NOT_READ = Color.BLACK;

    private VolumeUtil volumeUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_daily_bible);
        DBHelper.init(this);
        Tool.showStatusBar(getWindow(), this);

        tts = new TextToSpeech(this, this);
        volumeUtil = new VolumeUtil(this);

        InputStream is = getResources().openRawResource(R.raw.dailybible);
        allContent = MyFile.readStream(is);
        binding.addts.setOnClickListener(v -> {
            int size = Setting.getValueI(Setting.DAILY_BIBLE_TEXT_SIZE);
            if (size < 40) {
                size++;
                Setting.updateSetting(Setting.DAILY_BIBLE_TEXT_SIZE, size);
                for (int i = 0; i < cts.size(); i++) {
                    TextView tv = tvs.get(i);
                    tv.setTextSize(Setting.getValueI(Setting.DAILY_BIBLE_TEXT_SIZE));
                    if (cts.get(i).type != 0)
                        tv.setTextSize(Setting.getValueI(Setting.DAILY_BIBLE_TEXT_SIZE) * 1.5f);
                }
            }
        });
        binding.dects.setOnClickListener(v -> {
            int size = Setting.getValueI(Setting.DAILY_BIBLE_TEXT_SIZE);
            if (size > 5) {
                size--;
                Setting.updateSetting(Setting.DAILY_BIBLE_TEXT_SIZE, size);

                for (int i = 0; i < cts.size(); i++) {
                    TextView tv = tvs.get(i);
                    tv.setTextSize(Setting.getValueI(Setting.DAILY_BIBLE_TEXT_SIZE));
                    if (cts.get(i).type != 0)
                        tv.setTextSize(Setting.getValueI(Setting.DAILY_BIBLE_TEXT_SIZE) * 1.5f);
                }
            }
        });
        binding.ps.setOnClickListener(v -> {
            try {
                if (!ttsSuccess) {
                    Toast.makeText(this, "初始化语音引擎失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!tts.isSpeaking()) {
                    volumeUtil.setMediaVolumeIfZero();
                    play(Math.max(currentInd, 0));
                    Logger.info("开始播放语音");
                    binding.ps.setText("暂停");
                } else {
                    tts.stop();
                    binding.ps.setText("播放");
                }
            } catch (Exception e) {
                Logger.exception(e);
                Toast.makeText(this, "播放异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        binding.pause.setOnClickListener(v -> {
            try {
                if (!ttsSuccess) {
                    Toast.makeText(this, "初始化语音引擎失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (tts.isSpeaking()) {
                    tts.stop();
                }
                currentInd = 0;
                binding.seekBar2.setProgress(0);
                for (int i = 0; i < tvs.size(); i++)
                    tvs.get(i).setTextColor(COLOR_NOT_READ);
                binding.ps.setText("播放");
            } catch (Exception e) {
                Logger.exception(e);
                Toast.makeText(this, "播放异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                int ind = Integer.parseInt(utteranceId);
                if (ind < 0) {
                    if (nextP > 0)
                        scrollTo(nextP);
                    return;
                }
                if (ind < tvs.size()) {
                    currentInd = ind;
                    tvs.get(ind).setTextColor(COLOR_READING);
                    scrollTo(ind);
                }
            }

            @Override
            public void onDone(String utteranceId) {
                int ind = Integer.parseInt(utteranceId);
                if (ind < 0)
                    return;
                for (int i = 0; i <= ind && i < tvs.size(); i++)
                    tvs.get(i).setTextColor(COLOR_HAS_READ);
                for (int i = ind + 1; i < tvs.size(); i++)
                    tvs.get(i).setTextColor(COLOR_NOT_READ);
                binding.seekBar2.setProgress(ind);
                if (ind == tvs.size() - 1) {
                    binding.pause.callOnClick();
                }
            }

            @Override
            public void onError(String utteranceId) {

            }
        });
        //for (dt = 0; dt < 366; dt++)
        load();

        binding.previewDay.setOnClickListener(v -> {
            dt--;
            load();
        });
        binding.nextDay.setOnClickListener(v -> {
            dt++;
            load();
        });

        SeekBar progressBar = binding.seekBar2;
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // tt.jumpThis();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                play(seekBar.getProgress());
            }
        });

        binding.speech.setOnClickListener(v -> {
            int t = Setting.getValueI(Setting.DAILY_BIBLE_SPEECH);
            t = (t + 1) % 5;
            Setting.updateSetting(Setting.DAILY_BIBLE_SPEECH, t);
            setSpeechRate(true);
        });
        binding.yinse.setOnClickListener(v -> {
            int t = Setting.getValueI(Setting.DAILY_BIBLE_YINSE);
            t = (t + 1) % 5;
            Setting.updateSetting(Setting.DAILY_BIBLE_YINSE, t);
            setYinSe(true);
        });
        binding.setBtn.setOnClickListener(v -> {
            if (binding.setList.getVisibility() == View.VISIBLE)
                binding.setList.setVisibility(View.GONE);
            else
                binding.setList.setVisibility(View.VISIBLE);
        });
        binding.autoscr.setOnClickListener(v -> {
            boolean t = Setting.getValueB(Setting.DAILY_BIBLE_AUTO_SCR);
            t = !t;
            Setting.updateSetting(Setting.DAILY_BIBLE_AUTO_SCR, t);
            setAuto();
        });
        binding.readme.setOnClickListener(v -> Tool.ShowDialog(this, binding.readme.getText().toString(), new String[]{
                "1:播放功能使用的是TTS(文字转语音)引擎,可能会使用少量流量",
                "2:如果无法播放,请检查手机是否有安装tts(一般手机会自带该功能)",
                "3:如果不满意当前的语音质量,可以下载第三方的tts来替换"
        }));
        //自动跳转
        boolean useWeb = Setting.getValueB(Setting.USE_WEB_DAILY_BIBLE);
        binding.checkBox.setChecked(useWeb);
        binding.checkBox.setOnClickListener(v -> {
            boolean f = binding.checkBox.isChecked();
            Setting.updateSetting(Setting.USE_WEB_DAILY_BIBLE, f);
        });
        if (useWeb) {
            WebHelper.OpenByUrl(this, "http://mana.stmn1.com/1n1bB/index.html");
        }
    }

    private void scrollTo(int ind) {
        boolean t = Setting.getValueB(Setting.DAILY_BIBLE_AUTO_SCR);
        if (!t)
            return;
        int height = binding.scrollview.getHeight();
        int sum = binding.ft.getHeight();
        for (TextView tv : tvs)
            sum += tv.getHeight();
        int cut = binding.ft.getHeight();
        for (int i = 0; i <= ind; i++) {
            if (i == ind)
                cut += tvs.get(i).getHeight() / 2;
            else
                cut += tvs.get(i).getHeight();
        }
        cut -= height / 2;

        int aim;
        if (cut > sum - height)
            aim = sum - height;
        else aim = Math.max(cut, 0);
        if (binding.scrollview.getScrollY() != aim)
            binding.scrollview.smoothScrollTo(0, aim);
    }

    private int currentInd = -1;

    private int nextP = -1;

    private void play(int ind) {
        try {
            if (!ttsSuccess) {
                Toast.makeText(this, "初始化语音引擎失败", Toast.LENGTH_SHORT).show();
                return;
            }
            Logger.info("set play progress: " + ind);
            tts.stop();
            binding.ps.setText("暂停");
            SpeckBibleStruct[] txts = getTTSTexts();
            for (int j = 0; j < ind && j < tvs.size(); j++) {
                tvs.get(j).setTextColor(COLOR_HAS_READ);
            }
            for (int j = ind + 1; j < tvs.size(); j++) {
                tvs.get(j).setTextColor(COLOR_NOT_READ);
            }
            if (ind != 0) {
                SpeckBibleStruct ss = txts[ind];
                if (ss.content.trim().length() != 0) {
                    if (ss.content.contains(" ") && ss.content.contains("第") && ss.content.contains("章")) {
                        //Logger.info("正好是书名");
                    } else if (ss.content.contains("第") && ss.content.contains("章")) {
                        //Logger.info("正好是章节");
                        tts.speak(scv(ss.section.letter.getFullName()), TextToSpeech.QUEUE_ADD, null, -1 + "");
                    } else {
                        if (ss.section.letter.getFullName().equals("诗篇"))
                            tts.speak(scv(ss.section.letter.getFullName() + " 第" + ss.section.ChapterNum + "篇"), TextToSpeech.QUEUE_ADD, null, -1 + "");
                        else
                            tts.speak(scv(ss.section.letter.getFullName() + " 第" + ss.section.ChapterNum + "章"), TextToSpeech.QUEUE_ADD, null, -1 + "");
                    }
                    nextP = ind;
                }
            }

            for (int j = ind; j < txts.length; j++) {
                if (txts[j].content.trim().length() > 0) {
                    tts.speak(txts[j].content, TextToSpeech.QUEUE_ADD, null, j + "");
                }
            }
        } catch (Exception e) {
            Logger.exception(e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private SpeckBibleStruct[] getTTSTexts() {
        List<SpeckBibleStruct> res = new ArrayList<>();
        for (SpeckBibleStruct c : cts) {
            res.add(new SpeckBibleStruct(scv(c.content), c.section, c.type));
        }
        return res.toArray(new SpeckBibleStruct[0]);
    }

    private static String[] cvList = null;

    private String scv(String s) {
        if (cvList == null) {
            cvList = MyFile.readStream(getResources().openRawResource(R.raw.replaceword));
        }
        for (String r : cvList) {
            String[] sp = r.split(" ");
            if (sp.length == 2)
                s = s.replace(sp[0], sp[1]);
        }
        return s;
    }

    private int dt = 0;
    private String[] allContent;

    @SuppressLint("SetTextI18n")
    private void load() {
        binding.setList.setVisibility(View.GONE);
        if (tts.isSpeaking()) {
            tts.stop();
            binding.ps.setText("播放");
        }
        binding.scrollview.setScrollY(0);
        LocalDate ld = LocalDate.now().plusDays(dt);
        int a = ld.getMonthValue();
        int b = ld.getDayOfMonth();
        String ds = nn[a] + "月" + nn[b] + "日";
        if (dt == 0)
            ds += "(今天)";
        else if (dt == -1)
            ds += "(昨天)";
        else if (dt == -2)
            ds += "(前天)";
        else if (dt == 1)
            ds += "(明天)";
        else if (dt == 2)
            ds += "(后天)";

        try {
            binding.todayTv.setText(ds);
            Logger.info(ds);
            if (a == 2 && b == 29)
                b = 28;
            ds = nn[a] + "月" + nn[b] + "日";
            String o = null;
            String n = null;
            for (int i = 0; i < allContent.length - 2; i++)
                if (allContent[i].equals(ds)) {
                    o = allContent[i + 1];
                    n = allContent[i + 2];
                }
            if (n == null || o == null) {
                Logger.info("未找到对应经节");
                return;
            }
            //Logger.info(o + " " + n);
            binding.bibleContents.removeAllViews();

            binding.ft.setText(n + " \r\n" + o);
            String str = n.replace("新约：", "").trim() + ";" + o.replace("旧约：", "");
            cts = BibleTool.dailyShow(str, this);
            binding.seekBar2.setProgress(0);
            binding.seekBar2.setMax(cts.size());
            tvs.clear();

            for (SpeckBibleStruct s : cts) {
                if (s == null) continue;
                TextView tv = new TextView(this);
                tv.setText(s.content);
                tv.setTextColor(COLOR_DEFAULT);
                binding.bibleContents.addView(tv);
                tv.setTextSize(Setting.getValueI(Setting.DAILY_BIBLE_TEXT_SIZE));
                if (s.type != 0)
                    tv.setTextSize(Setting.getValueI(Setting.DAILY_BIBLE_TEXT_SIZE) * 1.5f);
                tvs.add(tv);
            }
        } catch (Exception e) {
            Logger.info(ds);
            Logger.exception(e);
        }
    }

    List<SpeckBibleStruct> cts = new ArrayList<>();
    List<TextView> tvs = new ArrayList<>();
    private static final String[] nn = new String[]{"", "一", "二", "三", "四", "五",
            "六", "七", "八", "九", "十",
            "十一", "十二", "十三", "十四", "十五",
            "十六", "十七", "十八", "十九", "二十",
            "二一", "二二", "二三", "二四", "二五",
            "二六", "二七", "二八", "二九", "三十", "三一"};

    private boolean ttsSuccess = false;

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            Logger.info("初始化成功");
            int r = tts.setLanguage(Locale.CHINA);
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                Logger.info("语言不支持");
            } else {
                ttsSuccess = true;
                setSpeechRate(false);
                setYinSe(false);
                setAuto();
            }
        } else {
            Logger.info("初始化失败");
        }
    }

    private void setSpeechRate(boolean replay) {
        int i = Setting.getValueI(Setting.DAILY_BIBLE_SPEECH);
        if (i == 0) {
            tts.setSpeechRate(0.3f);
            binding.speech.setText("语速:慢");
        } else if (i == 1) {
            tts.setSpeechRate(0.7f);
            binding.speech.setText("语速:较慢");
        } else if (i == 2) {
            tts.setSpeechRate(1f);
            binding.speech.setText("语速:中");
        } else if (i == 3) {
            tts.setSpeechRate(1.3f);
            binding.speech.setText("语速:较快");
        } else if (i == 4) {
            tts.setSpeechRate(1.8f);
            binding.speech.setText("语速:快");
        }
        if (replay && currentInd >= 0) {
            play(currentInd);
        }
    }

    private void setYinSe(boolean replay) {
        int i = Setting.getValueI(Setting.DAILY_BIBLE_YINSE);
        if (i == 0) {
            tts.setPitch(0.3f);
            binding.yinse.setText("音色:0");
        } else if (i == 1) {
            tts.setPitch(0.5f);
            binding.yinse.setText("音色:1");
        } else if (i == 2) {
            tts.setPitch(0.7f);
            binding.yinse.setText("音色:2");
        } else if (i == 3) {
            tts.setPitch(1f);
            binding.yinse.setText("音色:3");
        } else if (i == 4) {
            tts.setPitch(1.6f);
            binding.yinse.setText("音色:4");
        }
        if (replay && currentInd >= 0) {
            play(currentInd);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setAuto() {
        boolean b = Setting.getValueB(Setting.DAILY_BIBLE_AUTO_SCR);
        binding.autoscr.setText("自动滚动:" + (b ? "开" : "关"));
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        super.onDestroy();
    }
}