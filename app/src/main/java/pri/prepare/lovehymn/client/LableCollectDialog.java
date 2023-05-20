package pri.prepare.lovehymn.client;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.I4LC;
import pri.prepare.lovehymn.client.tool.I4Set;
import pri.prepare.lovehymn.client.tool.IRefresh;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.client.tool.enuCm;
import pri.prepare.lovehymn.databinding.LabelCollectionLayoutBinding;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.UpdateHistory;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.Label;
import pri.prepare.lovehymn.server.entity.LabelType;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.entity.PersonRemark;
import pri.prepare.lovehymn.server.entity.Setting;
import pri.prepare.lovehymn.server.function.CollectTool;
import pri.prepare.lovehymn.server.function.SdCardTool;

/**
 * 标签，足迹，收藏，分享页（下拉）
 */
public class LableCollectDialog extends Dialog implements IShowDialog {
    private final I4LC _i4lc;
    private I4Set _i4Set;
    private MyFile _file;
    private Hymn hymn = null;

    private LabelCollectionLayoutBinding binding = null;

    public LableCollectDialog(@NonNull Context context, MyFile file, I4LC i4LC, I4Set i4Set) {
        super(context);
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.label_collection_layout, null, false);
        setContentView(binding.getRoot());
        getWindow().setBackgroundDrawable(new BitmapDrawable());

        _i4lc = i4LC;
        _file = file;
        _i4Set = i4Set;
        hymn = file.getHymn();
        iRefresh.refresh();

        boolean haveCollect = Setting.getValueS(Setting.COLLECT).contains(file.getAbsolutePath());
        //足迹
        //Tool.drawableRightSet(binding.addStepBtn, context, R.drawable.step); //改到下面
        binding.addStepBtn.setOnClickListener(v -> {
            String dt = hymn.addStep();
            try {
                hymn.update();
                SdCardTool.writeToFile(SdCardTool.getResPath() + File.separator + SdCardTool.STEP_FILE_NAME, hymn.toString() + " " + dt, SdCardTool.FILE_APPEND);
                mod = I4LC.ADD_STEP;
                dismiss();
            } catch (Exception e) {
                Logger.info("update hymn error");
                Logger.exception(e);
            }
        });
        binding.myStep.setOnClickListener(v -> {
            AllStepDialog sd=new AllStepDialog(context);
            sd.showDialog();
            dismiss();
        });
        binding.myStep.setOnLongClickListener(v -> binding.myStep.callOnClick());
        //收藏
        Tool.drawableLeftSet(binding.collectBtn, context, haveCollect ? R.drawable.collect_yes : R.drawable.collect_no);
        binding.collectBtn.setOnClickListener(v -> {
            CollectTool.modCollect(file);
            mod = I4LC.MOD_COLLECT;
            dismiss();
        });

        Tool.drawableRightSet(binding.lmAddBtn, context, R.drawable.add_i);
        binding.lmAddBtn.setOnClickListener(v -> {
            CommonDialog cd = new CommonDialog(getContext(), enuCm.ADD_LABEL, iRefresh, null);
            cd.showDialog();
        });

        Tool.drawableLeftSet(binding.shareToWX, context, R.drawable.wx);
        binding.shareToWX.setOnClickListener(v -> i4Set.Share(file, false));

        String sp = "\r\n";
        String[] steps = hymn.getSteps();
        if (steps.length == 0)
            binding.hymnStep.setText("你还未在这首诗歌留下足迹");
        else if (steps.length <= 3)
            binding.hymnStep.setText(String.join(sp, steps));
        else {
            binding.hymnStep.setText(steps[0] + sp + steps[1] + sp + steps[2] + sp + "点击查看所有");
            binding.hymnStep.setOnClickListener(v -> {
                Tool.ShowDialog(context, hymn.getShowName() + " 足迹(" + steps.length + ")", String.join(sp, steps));
            });
        }
        if (steps.length > 0 && steps[0].startsWith("今天"))
            Tool.drawableRightSet(binding.addStepBtn, context, R.drawable.step_g);
        else
            Tool.drawableRightSet(binding.addStepBtn, context, R.drawable.step);
        //remark
        String remark = PersonRemark.getRemark(hymn);
        if (remark.length() > 0)
            binding.remark.setText(remark);
        else
            binding.remark.setText(PersonRemark.NO_REMARK);
        binding.editRemark.setOnClickListener(v -> {
            CommonDialog cd = new CommonDialog(getContext(), enuCm.EDIT_REMARK, remarkRefresh, null, hymn.toString());
            cd.showDialog();
        });
    }

    private IRefresh remarkRefresh = () -> {
        String s = PersonRemark.getRemark(hymn);
        if (s.trim().length() == 0)
            s = PersonRemark.NO_REMARK;
        binding.remark.setText(s);
    };

    private IRefresh iRefresh = () -> {
        LabelType[] lts = LabelType.getAll();
        binding.lll1.removeAllViews();
        binding.lll2.removeAllViews();
        if (lts.length == 0) {
            binding.hymnLabel.setText("没有标签，去其他设置里添加标签吧");
        } else {
            binding.hymnLabel.setVisibility(View.GONE);
            int ind = 0;
            for (LabelType lt : lts) {
                Button tv = new Button(getContext());
                tv.setText(lt.getName());
                tv.setTextSize(20);
                tv.setBackground(null);
                tv.setGravity(Gravity.LEFT);
                boolean hasL = Label.hasLabel(_file.getHymn(), lt.getId());
                Tool.drawableLeftSet(tv, getContext(), hasL ? R.drawable.label_yes : R.drawable.label_no);
                tv.setOnClickListener(v -> {
                    boolean hasL2 = Label.hasLabel(_file.getHymn(), lt.getId());
                    Label.mod(_file.getHymn(), lt.getId(), !hasL2);
                    Tool.drawableLeftSet(tv, getContext(), !hasL2 ? R.drawable.label_yes : R.drawable.label_no);

                    Service.getC().bakLabel(true);
                });
                tv.setOnLongClickListener(v -> {
                    LabelDetailDialog dialog = new LabelDetailDialog(getContext(), lt.getId(), _i4Set);
                    dialog.showDialog();
                    dismiss();
                    return true;
                });

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 0);
                tv.setLayoutParams(lp);

                if (ind % 2 == 0)
                    binding.lll1.addView(tv);
                else
                    binding.lll2.addView(tv);
                ind++;
            }
        }
    };

    @Override
    public void showDialog() {
        Window window = getWindow(); //得到对话框
        if (window != null) {
            Tool.setAnim(window, Tool.ANIM_TOP);
            window.setGravity(Gravity.TOP);
            window.setBackgroundDrawableResource(R.drawable.mydialogup);
        }
        //设置触摸对话框以外的地方取消对话框
        setCanceledOnTouchOutside(true);
        show();
    }

    private int mod = 0;

    @Override
    protected void onStop() {
        super.onStop();
        _i4lc.updateTitle(_file, mod);
    }
}