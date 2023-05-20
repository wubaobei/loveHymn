package pri.prepare.lovehymn.client;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.TipStruct;
import pri.prepare.lovehymn.databinding.ActivityEmpty4readmeBinding;
import pri.prepare.lovehymn.server.UpdateHistory;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.function.Constant;
import pri.prepare.lovehymn.server.function.TipTool;

public class ReadMeActivity extends AppCompatActivity {
    private ActivityEmpty4readmeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_empty4readme);

        int type = getIntent().getIntExtra("type", 1);
        TipStruct[] str;
        String h1 = "";
        String h2 = "";
        if (type == 2) {
            h1 = "常见问题";
            h2 = "如果有其他问题，可以问作者，微信：prepareWu";
            String[] ss = UpdateHistory.getAskAnswer();
            str = new TipStruct[ss.length];
            for (int i = 0; i < ss.length; i++)
                str[i] = new TipStruct(ss[i]);
        } else {
            h1 = Constant.READ_ME;
            h2 = "其实是'" + Constant.TIPS + "'集合";
            str = TipTool.getAll(true);
        }
        binding.title1.setText(h1);
        binding.title2.setText(h2);

        TipTool.addTips(this, str, binding.readLl);
        binding.readLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.info("click readLL");
            }
        });
    }
}
