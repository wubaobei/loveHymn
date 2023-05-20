package pri.prepare.lovehymn.client;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashMap;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.databinding.AllStepDialogBinding;
import pri.prepare.lovehymn.databinding.CommonListDialogBinding;
import pri.prepare.lovehymn.server.entity.Book;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.Setting;
import pri.prepare.lovehymn.server.function.SdCardTool;

public class AllStepDialog extends Dialog implements IShowDialog {
    private final AllStepDialogBinding binding;

    public AllStepDialog(@NonNull Context context) {
        super(context);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.all_step_dialog, null, false);
        setContentView(binding.getRoot());
        try {
            String[] cts = MyFile.from(SdCardTool.getStepPath()).getContent();

            binding.title.setText("最近足迹");
            binding.textView.setText(getContent(10, cts));
            statSet();
            allSet(cts);
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private String getContent(int count, String[] cts) {
        StringBuilder sb = new StringBuilder();
        for (int i = cts.length - 1; i >= 0 && cts.length - i <= count; i--) {
            int ind = cts[i].indexOf(" ");
            Hymn h = Hymn.search(cts[i].substring(0, ind));
            String s = cts[i].substring(ind);
            if (h != null) {
                sb.append(h.getShowName()).append("\r\n\t\t").append(s.substring(0, s.length() - 3)).append("\r\n");
            } else {
                sb.append(cts[i].substring(0, ind)).append("\r\n\t\t").append(s.substring(0, s.length() - 3)).append("\r\n");
            }
        }
        return sb.toString().trim();
    }

    private void allSet(String[] cts) {
        binding.allStep.setOnClickListener(v -> {
            binding.title.setText("所有足迹(" + cts.length + ")");
            binding.textView.setText(getContent(99999, cts));

            binding.allStep.setEnabled(false);
            binding.stepStat.setEnabled(true);
        });
    }

    private void statSet() {
        binding.stepStat.setOnClickListener(v -> {
            Hymn[] hymns = Hymn.getHymnsHasStepSortDesc();
            String[] cts = MyFile.from(SdCardTool.getStepPath()).getContent();

            StringBuilder sbw = new StringBuilder();
            sbw.append("共留下" + cts.length + "次足迹\r\n");
            int[] week = new int[8];
            for (String s : cts) {
                int ind = s.indexOf(" ");
                String dateString = s.substring(ind).trim();
                LocalDateTime dt = LocalDateTime.parse(dateString, Setting.getDefaultTimeFormatter());
                week[dt.getDayOfWeek().getValue()]++;
            }
            int[] arr = new int[]{DayOfWeek.SUNDAY.getValue(), DayOfWeek.MONDAY.getValue(), DayOfWeek.TUESDAY.getValue(),
                    DayOfWeek.WEDNESDAY.getValue(), DayOfWeek.THURSDAY.getValue(), DayOfWeek.FRIDAY.getValue(), DayOfWeek.SATURDAY.getValue()};
            String[] arrS = new String[]{"主日", "周一", "周二", "周三", "周四", "周五", "周六"};

            sbw.append("按时间分类:\r\n");
            for (int i = 0; i < arr.length; i++)
                if (week[arr[i]] > 0)
                    sbw.append("\t" + arrS[i] + ":" + week[arr[i]]).append("次\r\n");

            StringBuilder sb = new StringBuilder();
            sb.append("留下足迹最多的几首是：\r\n");
            for (int i = 0; i < hymns.length && i < 5; i++) {
                sb.append(hymns[i].getShowName()).append("(").append(hymns[i].getSteps().length).append("次)\r\n");
            }

            HashMap<String, Integer> bookN = new HashMap<>();
            for (Book b : Book.getAll()) {
                bookN.put(b.SimpleName, 0);
            }
            for (String ct : cts) {
                int bn = bookN.get(ct.substring(0, 1));
                bookN.put(ct.substring(0, 1), bn + 1);
            }
            sbw.append("按诗歌本分类:\r\n");
            for (Book b : Book.getAll()) {
                if (bookN.get(b.SimpleName) > 0) {
                    sbw.append("\t" + b.FullName + ":" + bookN.get(b.SimpleName)).append("\r\n");
                }
            }

            String[] stat = new String[]{"有" + hymns.length + "首诗歌留下了足迹", sbw.toString().trim(), sb.toString().trim()};

            binding.title.setText("足迹统计");
            binding.textView.setText(String.join("\r\n\r\n", stat));


            binding.allStep.setEnabled(true);
            binding.stepStat.setEnabled(false);
        });
    }

    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(true);
        Tool.DialogSet(this);
        show();
    }
}
