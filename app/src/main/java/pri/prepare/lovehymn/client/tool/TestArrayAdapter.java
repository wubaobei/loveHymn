package pri.prepare.lovehymn.client.tool;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import pri.prepare.lovehymn.R;

/**
 * 自定义spinner
 * https://www.jianshu.com/p/803a0b7c5f90?tn=96100419_hao_pg
 */
public class TestArrayAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private String[] mStringArray;

    public TestArrayAdapter(Context context, String[] stringArray) {
        super(context, android.R.layout.simple_spinner_item, stringArray);
        mContext = context;
        mStringArray = stringArray;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        //修改Spinner展开后的字体颜色
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        //此处text1是Spinner默认的用来显示文字的TextView
        TextView tv = convertView.findViewById(android.R.id.text1);
        tv.setText(mStringArray[position]);
        tv.setTextColor(Color.BLACK);
        tv.setBackgroundColor(cgColor(position));
        tv.setLayoutParams(Tool.getMarginLayout());

        return convertView;
    }

    private int cgColor(int index) {
        int n1 = 120;
        int n2 = 140;
        if (index % 2 == 0)
            return Color.argb(100, n1, n1, n1);
        return Color.argb(100, n2, n2, n2);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 修改Spinner选择后结果的字体颜色
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        //此处text1是Spinner默认的用来显示文字的TextView
        TextView tv = convertView.findViewById(android.R.id.text1);
        tv.setText(mStringArray[position]);
        //tv.setTextSize(20);
        tv.setTextColor(Color.BLACK);
        tv.setBackgroundResource(R.drawable.jianbian_bg);
        tv.setPadding(20,10,20,10);

        return convertView;
    }
}
