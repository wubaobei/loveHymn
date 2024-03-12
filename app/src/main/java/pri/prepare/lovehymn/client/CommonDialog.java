package pri.prepare.lovehymn.client;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.IRefresh;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.client.tool.enuCm;
import pri.prepare.lovehymn.databinding.CommonYnDialogBinding;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.LabelType;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.PersonRemark;
import pri.prepare.lovehymn.server.entity.Setting;
import pri.prepare.lovehymn.server.function.SdCardTool;
import pri.prepare.lovehymn.server.function.WebHelper;

public class CommonDialog extends Dialog implements IShowDialog {
    private final CommonYnDialogBinding binding;
    private Activity activity;
    private enuCm cm;

    public CommonDialog(@NonNull Context context, enuCm c, IRefresh iRefresh, Activity activity, String... params) {
        super(context);
        this.activity = activity;
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.common_yn_dialog, null, false);
        setContentView(binding.getRoot());
        iconSet();
        cm = c;
        binding.cmIo.setVisibility(View.GONE);
        if (c == enuCm.ADD_LABEL) {
            ADD_LABEL_init(iRefresh);
        } else if (c == enuCm.DELETE_LABEL) {
            DELETE_LABEL_init(iRefresh, params[0]);
        } else if (c == enuCm.RENAME_LABEL) {
            RENAME_LABEL_init(iRefresh, params[0]);
        } else if (c == enuCm.SECRET_CODE) {
            SECRET_CODE_init();
            binding.cmIo.setVisibility(View.VISIBLE);
        } else if (c == enuCm.LEAVE_STEP) {
            LEAVE_STEP_init(iRefresh, params[0]);
        } else if (c == enuCm.EDIT_REMARK) {
            EDIT_REMARK_init(iRefresh, params[0]);
        } else if (c == enuCm.WARM) {
            WARN_init(iRefresh, params[0]);
        }

        binding.cmNo.setOnClickListener(v -> {
            dismiss();
        });
    }

    private void WARN_init(IRefresh iRefresh, String param) {
        binding.cmIo.setVisibility(View.VISIBLE);
        binding.cmNo.setVisibility(View.VISIBLE);
        binding.cmTitle.setText("解压完成");
        binding.cmEdit.setText(param);
        binding.cmEdit.setEnabled(false);
        binding.cmYes.setOnClickListener(v -> {
            dismiss();
        });
    }

    private void ADD_LABEL_init(IRefresh iRefresh) {
        binding.cmTitle.setText(" 新增标签");
        Tool.drawableLeftSet(binding.cmTitle, getContext(), R.drawable.add_i);

        groupInit("0");
        binding.cmEdit.setEnabled(true);
        binding.cmEdit.setText("");
        binding.cmYes.setOnClickListener(v -> {
            try {
                String s = binding.cmEdit.getText().toString().trim();
                if (s.length() > 0 && !s.contains(":")) {
                    LabelType.add(s, groupName);
                    iRefresh.refresh();
                }

                dismiss();
            } catch (Exception e) {
                Logger.exception(e);
            }
        });
    }

    private void DELETE_LABEL_init(IRefresh iRefresh, String param) {
        Tool.drawableLeftSet(binding.cmTitle, getContext(), R.drawable.delete_i);
        binding.cmEdit.setText(param);
        binding.cmEdit.setEnabled(false);
        binding.cmYes.setOnClickListener(v -> {
            String ltName = binding.cmEdit.getText().toString().trim();
            LabelType lt = LabelType.getByShowName(ltName);
            if (lt != null) {
                lt.delete(true);
                iRefresh.refresh();
            }
            Service.getC().bakLabel(true);
            dismiss();
        });
    }

    private void DELETE_RES_FILE_init(String param) {
        binding.cmTitle.setText("删除资源文件");
        binding.cmEdit.setText(param);
        binding.cmEdit.setEnabled(false);
        binding.cmYes.setOnClickListener(v -> {
            if (new File(SdCardTool.getResPath() + File.separator + param).delete()) {
            }
            dismiss();
        });
    }

    private void groupInit(String current) {
        TextView tv = new TextView(getContext());
        tv.setText("标签组");
        binding.groupLl.addView(tv, 0);
        for (int i = 0; i < LabelType.MAX_NUM; i++) {
            RadioButton rb = new RadioButton(getContext());
            rb.setText(i + "");
            binding.rg.addView(rb);
            if (rb.getText().toString().equals(current))
                rb.setChecked(true);
            rb.setOnClickListener(view -> groupName = ((RadioButton) view).getText().toString());
        }
    }

    private String groupName = "0";

    private void RENAME_LABEL_init(IRefresh iRefresh, String param) {
        LabelType lt = LabelType.getByShowName(param);
        binding.cmTitle.setText(" 重命名：" + lt.getName());
        binding.cmEdit.setText(lt.getName());
        groupInit(lt.getGroup());

        Tool.drawableLeftSet(binding.cmTitle, getContext(), R.drawable.rename_i);
        binding.cmYes.setOnClickListener(v -> {
            String ltName2 = binding.cmEdit.getText().toString().trim();
            if (ltName2.length() == 0) {
                return;
            }
            LabelType lt2 = LabelType.getByName(ltName2);
            if (lt2 != null && lt2.getId() != lt.getId()) {
                toast("已存在标签：" + ltName2);
                return;
            }

            if (lt.rename(ltName2, groupName)) {
                iRefresh.refresh();
            }
            dismiss();
        });
    }

    private void SECRET_CODE_init() {
        binding.cmTitle.setText("神秘代码");
        binding.cmEdit.setEnabled(true);
        binding.cmEdit.setText("");
        binding.cmEdit.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        binding.cmYes.setOnClickListener(v -> {
            try {
                int s = Integer.parseInt(binding.cmEdit.getText().toString());
                if (s == 0) {
                    String sp = "\r\n";
                    String content = CODE_AUTHOR + ":所有作者名" + sp
                            + CODE_STATUS + ":手机及app状态" + sp
                            + ENCODE + ":加密" + sp
                            + DECODE + ":解密" + sp
                            + QING_HIDE + ":显示/隐藏《青年诗歌》"
                            + CODE_OUTPUT_RES + ":导出资源文件" + sp
                            + CODE_STEP_TIME + ":显示/隐藏自动足迹时间";
                    Tool.ShowDialog(getContext(), "代码编号", content);
                } else if (s == CODE_AUTHOR) {
                    Tool.ShowDialog(getContext(), "所有作者名", Service.getC().allAuthorNames());
                } else if (s == CODE_STATUS) {
                    Tool.ShowDialog(getContext(), "状态", Service.getC().getDebugMsg(activity));
                } else if (s == CODE_STEP_TIME) {
                    boolean st = Setting.getValueB(Setting.AUTO_STEP_TIME);
                    Setting.updateSetting(Setting.AUTO_STEP_TIME, !st);
                } else if (s == CODE_OUTPUT_RES) {
                    String path = SdCardTool.getSharePath() + File.separator + "资源文件导出";
                    Service.getC().outputRes(path);
                    toast("导出完成，请到" + path + "查看");
                } else if (s == ENCODE) {
                    binding.cmIo.setText(WebHelper.encode(binding.cmIo.getText().toString()));
                    return;
                } else if (s == DECODE) {
                    binding.cmIo.setText(WebHelper.decode(binding.cmIo.getText().toString()));
                    return;
                } else if (s == QING_HIDE) {
                    boolean b = Setting.getValueB(Setting.HIDE_QING);
                    if (b) {
                        toast("显示青年诗歌");
                    } else {
                        toast("隐藏青年诗歌");
                    }
                    Setting.updateSetting(Setting.HIDE_QING, !b);
                } else {
                    toast("未识别的代码");
                    return;
                }
                dismiss();
            } catch (Exception e) {
                toast("输入错误 " + e.getMessage());
                Logger.exception(e);
            }
        });
    }

    private void LEAVE_STEP_init(IRefresh iRefresh, String param) {
        Hymn hymn = Hymn.getById(Integer.parseInt(param));

        binding.cmTitle.setText("留下足迹");
        binding.cmEdit.setEnabled(false);
        binding.cmEdit.setText("是否在" + hymn.getShortShowName() + "留下足迹");
        binding.cmYes.setOnClickListener(v -> {
            String newStep = hymn.addStep();
            try {
                hymn.update();
                SdCardTool.writeToFile(SdCardTool.getResPath() + File.separator + SdCardTool.STEP_FILE_NAME, hymn.toString() + " " + newStep, SdCardTool.FILE_APPEND);
                iRefresh.refresh();
                dismiss();
            } catch (Exception e) {
                Logger.exception(e);
            }
        });
        binding.cmYes.setText("是");
        binding.cmNo.setText("否");
    }

    private void EDIT_REMARK_init(IRefresh iRefresh, String param) {
        binding.cmTitle.setText("个人备注");
        binding.cmEdit.setEnabled(true);
        Hymn hymn = Hymn.search(param);
        if (hymn == null)
            return;
        binding.cmEdit.setText(PersonRemark.getRemark(hymn));
        binding.cmYes.setOnClickListener(v -> {
            PersonRemark.updateAndSave(hymn, binding.cmEdit.getText().toString());
            iRefresh.refresh();
            dismiss();
        });
        binding.cmYes.setText("确定");
        binding.cmNo.setText("取消");
    }

    //神秘代码编号
    //0-99 显示
    private static final int CODE_AUTHOR = 1;
    private static final int CODE_STATUS = 2;
    private static final int ENCODE = 3;
    private static final int DECODE = 4;
    private static final int QING_HIDE = 5;
    //100-199 输出
    private static final int CODE_OUTPUT_RES = 100;
    //200-299 调试信息
    private static final int CODE_STEP_TIME = 200;

    @Override
    public void showDialog() {
        Tool.setAnim(getWindow(), Tool.ANIM_NORMAL);
        //设置触摸对话框以外的地方取消对话框
        if (cm == enuCm.LEAVE_STEP)
            setCanceledOnTouchOutside(false);
        else
            setCanceledOnTouchOutside(true);
        Tool.DialogSet(this);
        show();
    }

    private static final String SPACE = " ";

    private void iconSet() {
        Button[] btns = new Button[]{binding.cmYes, binding.cmNo};
        int[] dId = new int[]{R.drawable.ok_i, R.drawable.cancel_i};

        for (int i = 0; i < btns.length; i++) {
            btns[i].setText(SPACE + btns[i].getText().toString());
            Tool.drawableLeftSet(btns[i], getContext(), dId[i]);
        }
    }

    private void toast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

    private String easySchedule(String source) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");

        String[] sp = source.split(" ");
        LocalTime lt = null;
        boolean isTime = true;
        int min = 0;
        for (String s : sp) {
            s = s.trim();
            Logger.info("deal " + s);
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

