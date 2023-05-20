package pri.prepare.lovehymn.client;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.ImmTool;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.ActivityEasyScheduleBinding;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.Setting;

public class EasyScheduleActivity extends AppCompatActivity {
    private ActivityEasyScheduleBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_easy_schedule);
        Tool.showStatusBar(getWindow(),this);
        binding.cmIo.setText(Setting.getValueS(Setting.EASY_SCHEDULE_RECORD));

        immTool = new ImmTool(getApplicationContext(), getWindow());
        binding.exampleSchedule.setOnClickListener(view -> {
            String ex = "6:30 0 起床 15 晨兴 45 洗漱、内务 30 早操 30 早餐 45 第一节课 15 休息 45 第二节课 15 休息 45 申言 15 休息 30 *午餐 120 午休";
            binding.cmIo.setText(ex);
            immTool.closeImmIfOpen();
        });
        binding.create.setOnClickListener(view -> {
            String input = binding.cmIo.getText().toString();
            if (input.trim().length() == 0)
                return;
            try {
                res = easySchedule(input);
                Setting.updateSetting(Setting.EASY_SCHEDULE_RECORD, input);
            } catch (Exception e) {
                Logger.exception(e);
            }
            showSchedule(res);
            immTool.closeImmIfOpen();
        });
        binding.copySchedule.setOnClickListener(view -> {
            Service.getC().CB(res, EasyScheduleActivity.this);
            toast("已复制到剪切板");
            immTool.closeImmIfOpen();
        });
        binding.sv.setOnTouchListener((view, motionEvent) -> {
            immTool.closeImmIfOpen();
            return false;
        });
    }

    private ImmTool immTool;

    private String res = "";

    private void showSchedule(String s) {
        binding.svList.removeAllViews();
        binding.svList1.removeAllViews();
        binding.svList2.removeAllViews();
        try {
            String[] sp = s.split("\n");
            for (String line : sp) {
                String[] arr = line.split("\t");

                TextView tv = new TextView(getApplicationContext());
                tv.setText(arr[0]);
                binding.svList.addView(tv);
                TextView tv1 = new TextView(getApplicationContext());
                tv1.setText(arr[1]);
                binding.svList1.addView(tv1);

                TextView tv2 = new TextView(getApplicationContext());
                if (arr.length > 2) {
                    tv2.setText(arr[2]);
                    binding.svList2.addView(tv2);
                } else {
                    tv2.setText("");
                    binding.svList2.addView(tv2);
                }

                if(arr[1].contains("*")){
                    int color=Color.rgb(100,100,255);
                    int tcolor=Color.rgb(240,240,240);
                    tv.setBackgroundColor(color);
                    tv.setTextColor(tcolor);
                    tv1.setBackgroundColor(color);
                    tv1.setTextColor(tcolor);
                    tv1.setTextColor(tcolor);
                    tv2.setBackgroundColor(color);
                    tv2.setTextColor(tcolor);
                    tv1.setText(tv1.getText().toString().replace("*",""));
                }
            }
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private String easySchedule(String source) {
        if (source.contains("  ")) {
            source = source.replace("  ", " ");
        }

        StringBuilder sb = new StringBuilder();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

        String[] sp = source.split(" ");
        LocalTime lt = null;
        boolean isTime = true;
        int min = 0;
        for (String s : sp) {
            s = s.trim();
            if (lt == null) {
                if (s.length() == 4)
                    s = "0" + s;
                lt = LocalTime.parse(s, df);
                continue;
            }
            if (isTime) {
                min = Integer.parseInt(s);
                if (s.equals("0")) {
                    sb.append(df.format(lt)).append("\t");
                } else {
                    sb.append(df.format(lt)).append("-");
                    lt = lt.plusMinutes(min);
                    sb.append(df.format(lt)).append("\t");
                }
            } else {
                if (min == 0)
                    sb.append(s).append("\r\n");
                else
                    sb.append(s).append("\t").append(min).append("\r\n");
            }
            isTime = !isTime;
        }

        return sb.toString().trim();
    }
}

