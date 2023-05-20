package pri.prepare.lovehymn.client;

import android.app.Dialog;
import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.IRefresh;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.TestArrayAdapter;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.client.tool.enuCm;
import pri.prepare.lovehymn.databinding.LabelStepManagerLayoutBinding;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.UpdateHistory;
import pri.prepare.lovehymn.server.entity.Book;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.Label;
import pri.prepare.lovehymn.server.entity.LabelType;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.Setting;
import pri.prepare.lovehymn.server.function.SdCardTool;

/**
 * 设置-标签足迹管理页
 */
public class LabelStepManagerDialog extends Dialog implements IShowDialog {
    private final LabelStepManagerLayoutBinding binding;

    public LabelStepManagerDialog(@NonNull Context context) {
        super(context);
        try {
            binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.label_step_manager_layout, null, false);
            setContentView(binding.getRoot());

            Tool.drawableLeftSet(binding.lmTitle, context, R.drawable.label_icon);
            Tool.drawableLeftSet(binding.lmTitle2, context, R.drawable.step);

            iconSet();
            init();
            btnSet();
            stepBtnSet();
        } catch (Exception e) {
            Logger.exception(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(true);
        Tool.DialogSet(this);
        show();
    }

    private IRefresh iRefresh = () -> init();
    private static final String SPACE = " ";
    private boolean isFirst = true;

    private void iconSet() {
        Button[] btns = new Button[]{binding.lmAddBtn, binding.lmDeleteBtn, binding.lmRenameBtn};
        int[] dId = new int[]{R.drawable.add_i, R.drawable.delete_i, R.drawable.rename_i};

        for (int i = 0; i < btns.length; i++) {
            btns[i].setText(SPACE + btns[i].getText().toString());
            Tool.drawableLeftSet(btns[i], getContext(), dId[i]);
        }
    }

    /**
     * 控件初始值和显示隐藏控制
     */
    private void init() {
        binding.lmDeleteBtn.setEnabled(false);
        binding.lmRenameBtn.setEnabled(false);

        LabelType[] lts = LabelType.getAll();
        if (lts.length > 0) {
            LabelType lt = lts[0];
            List<String> s = Label.getHymnIndexsByTypeId(lt.getId());
            if (s.size() == 0)
                binding.textView6.setText("该标签下没有诗歌");
            else
                binding.textView6.setText(String.join(";", s) + "(共" + s.size() + "首)");
        }

        String[] ltNameList = new String[lts.length];
        for (int i = 0; i < lts.length; i++)
            ltNameList[i] = lts[i].getShowName();
        ArrayAdapter<String> starAdapter = new TestArrayAdapter(getContext(), ltNameList);
        binding.spinner.setAdapter(starAdapter);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String ltName = (String) binding.spinner.getSelectedItem();
                LabelType lt = LabelType.getByShowName(ltName);
                binding.lmDeleteBtn.setEnabled(lt != null);
                binding.lmRenameBtn.setEnabled(lt != null);
                if (lt != null) {
                    List<String> s = Label.getHymnIndexsByTypeId(lt.getId());
                    if (s.size() == 0)
                        binding.textView6.setText("该标签下没有诗歌");
                    if (isFirst)
                        isFirst = false;
                    else
                        binding.textView6.setText(String.join(";", s) + "(共" + s.size() + "首)");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 标签按钮动作
     */
    private void btnSet() {
        //进入新增页面
        binding.lmAddBtn.setOnClickListener(v -> {
            CommonDialog cd = new CommonDialog(getContext(), enuCm.ADD_LABEL, iRefresh, null);
            cd.showDialog();
        });

        //删除
        binding.lmDeleteBtn.setOnClickListener(v -> {
            String s = binding.spinner.getSelectedItem().toString();
            if (s.length() == 0)
                return;
            LabelType lt = LabelType.getByShowName(s);
            if (lt == null)
                return;
            if (lt.getLabels().size() == 0) {
                lt.delete(false);
                init();
            } else {
                CommonDialog cd = new CommonDialog(getContext(), enuCm.DELETE_LABEL, iRefresh, null, s);
                cd.showDialog();
            }
        });

        //重命名
        binding.lmRenameBtn.setOnClickListener(v -> {
            String s = binding.spinner.getSelectedItem().toString();
            if (s.length() > 0) {
                CommonDialog cd = new CommonDialog(getContext(), enuCm.RENAME_LABEL, iRefresh, null, s);
                cd.showDialog();
            }
        });

        binding.lmLabelStatBtn.setOnClickListener(v -> {
            int lnumber = LabelType.getAll().length;
            StringBuilder sb = new StringBuilder();
            HashMap<String, Integer> bookN = new HashMap<>();
            for (Book b : Book.getAll()) {
                bookN.put(b.SimpleName, 0);
            }
            for (LabelType lt : LabelType.getAll()) {
                sb.append(lt.getName()).append(" 有").append(lt.getHymns().size()).append("首诗歌\r\n");
                for (String n : Label.getHymnIndexsByTypeId(lt.getId())) {
                    int bn = bookN.get(n.substring(0, 1));
                    bookN.put(n.substring(0, 1), bn + 1);
                }
            }
            StringBuilder bsb = new StringBuilder();
            for (Book b : Book.getAll()) {
                if (bookN.get(b.SimpleName) > 0) {
                    bsb.append(b.FullName + " 共" + bookN.get(b.SimpleName) + "个标签").append("\r\n");
                }
            }

            String[] stat = new String[]{"共" + lnumber + "个标签类型", sb.toString().trim(), bsb.toString()};
            Tool.ShowDialog(getContext(), "标签统计", stat);
        });
    }

    /**
     * 足迹设置
     */
    private void stepBtnSet() {
        int type = Setting.getValueI(Setting.STEP_FORMAT_TYPE);

        SpannableString ss = Tool.getSpannableString("时间格式：" + getFmt(type), new String[]{getFmt(0), getFmt(1), getFmt(2)});
        binding.lmStepFmtBtn.setText(ss);

        binding.lmStepFmtBtn.setOnClickListener(v -> {
            int t = Setting.getValueI(Setting.STEP_FORMAT_TYPE);
            t = (t + 1) % 3;

            SpannableString ss2 = Tool.getSpannableString("时间格式：" + getFmt(t), new String[]{getFmt(0), getFmt(1), getFmt(2)});
            binding.lmStepFmtBtn.setText(ss2);
            Setting.updateSetting(Setting.STEP_FORMAT_TYPE, t);
        });

        int as = Setting.getValueI(Setting.AUTO_STEP);
        SpannableString ss2 = Tool.getSpannableString("自动足迹：" + (as == 0 ? "否" : "是"), new String[]{"否", "是"});
        binding.autoStepBtn.setText(ss2);
        binding.autoStepBtn.setOnClickListener(v -> {
            int as2 = Setting.getValueI(Setting.AUTO_STEP);
            as2 = 1 - as2;

            SpannableString ss3 = Tool.getSpannableString("自动足迹：" + (as2 == 0 ? "否" : "是"), new String[]{"否", "是"});
            binding.autoStepBtn.setText(ss3);
            Setting.updateSetting(Setting.AUTO_STEP, as2);
        });

        binding.lmStepStatBtn.setOnClickListener(v -> {
            AllStepDialog sd=new AllStepDialog(getContext());
            sd.showDialog();
            dismiss();

        });
    }

    private String getFmt(int t) {
        if (t == 1)
            return "2021年11月21日 主日下午";
        if (t == 0)
            return "2021-11-21 16:31";
        return "2021年11月21日 主日";
    }
}