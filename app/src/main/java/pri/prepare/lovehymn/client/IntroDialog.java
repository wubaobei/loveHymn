package pri.prepare.lovehymn.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.client.tool.I4Intro;
import pri.prepare.lovehymn.client.tool.IShowDialog;
import pri.prepare.lovehymn.client.tool.Tool;
import pri.prepare.lovehymn.server.entity.Dict;
import pri.prepare.lovehymn.server.function.BibleTool;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.dal.AuthorRelatedD;
import pri.prepare.lovehymn.server.dal.ContentTypeD;
import pri.prepare.lovehymn.server.entity.Author;
import pri.prepare.lovehymn.server.entity.Content;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.Logger;

/**
 * 详情
 */
public class IntroDialog extends Dialog implements IShowDialog {
    private final Activity ct;
    final I4Intro _listener;

    public IntroDialog(@NonNull Activity activity, Hymn hymn, I4Intro listener) throws Exception {
        super(activity);
        ct = activity;
        setContentView(R.layout.intro_layout);
        _listener = listener;

        LinearLayout layout = findViewById(R.id.detLayout);

        EditText et = findViewById(R.id.etTitle);
        et.setText(hymn.getShowName());
        et.setEnabled(false);

        Tool.drawableLeftSet(et, ct, R.drawable.music);

        LinearLayout layoutAuthor = findViewById(R.id.detLayoutA);
        if (hymn.getLyricAuthors().length + hymn.getMusicAuthors().length > 0) {
            for (Author author : hymn.getLyricAuthors()) {
                addAuthor(author, AuthorRelatedD.LyricAuthor, layoutAuthor);
            }
            for (Author author : hymn.getMusicAuthors()) {
                addAuthor(author, AuthorRelatedD.MusicAuthor, layoutAuthor);
            }
        } else
            layout.removeView(layoutAuthor);

        if (hymn.getLyric() != null && hymn.getLyric().length() > 0) {
            addContent(Content.fromLyric(Dict.updateContent(hymn.getLyric(), activity)), layout);
        }

        for (Content contentEn : hymn.getContents()) {
            addContent(contentEn, layout);
        }
    }

    private void addAuthor(final Author author, int type, LinearLayout layoutAuthor) throws Exception {
        LinearLayout ll = new LinearLayout(ct);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setGravity(Gravity.CENTER_VERTICAL);

        if (AuthorRelatedD.getByAuthorId(author.getId()).length > 1) {
            final ImageButton ibtn = new ImageButton(ct);
            ibtn.setBackgroundResource(R.drawable.sc_search);
            ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(80, 80);
            ibtn.setLayoutParams(lp);
            Tool.setSearchLink(ibtn, author.getName(), _listener, this);
            ll.addView(ibtn);
        }

        TextView tv2 = new TextView(ct);
        ll.addView(tv2);

        layoutAuthor.addView(ll, Tool.getMarginLayout());
        String s = AuthorRelatedD.getTypeShortStr(type) + "：" + author.getName();
        tv2.setText(s);
        if ((author.getIntroduction() != null && author.getIntroduction().length() > 0)
                || (author.getAge() != null && author.getAge().length() > 0)) {
            String it = "";
            if (author.getAge() != null && author.getAge().length() > 0) {
                it += author.getAge() + "\r\n";
            }
            if (author.getIntroduction() != null && author.getIntroduction().length() > 0) {
                it += author.getIntroduction();
            }
            it = it.trim();

            tv2.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            final String finalIt = it;
            tv2.setOnClickListener(v -> Tool.ShowDialog(ct, author.getName() + "简介", finalIt));
            tv2.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Service.getC().CB(author.getName() + "简介：\r\n" + finalIt, ct);
                    Toast.makeText(ct, "已复制作者简介", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void addContent(Content contentEn, LinearLayout layout) throws Exception {
        final EditText tvi = new EditText(ct);
        tvi.setText(contentEn.getValue());
        tvi.setKeyListener(null);
        boolean hasLink = false;
        tvi.setBackgroundResource(R.drawable.btn_shape_down);
        tvi.setTextSize(14f);

        if (contentEn.isType(ContentTypeD.getSameMusicType()) || contentEn.isType(ContentTypeD.getSameSongType())) {
            if (Service.getC().correctOrders(contentEn.getValue()) != null) {
                Tool.setSearchLink(tvi, contentEn.getValue(), _listener, this);
                if (contentEn.isType(ContentTypeD.getSameSongType()))
                    tvi.setText(contentEn.getValue());
                else
                    tvi.setText(contentEn.getOtherShowString());
            }
            hasLink = true;
        } else if (contentEn.isType(ContentTypeD.getRelatedBibleType())) {
            tvi.setText(BibleTool.dealStr(contentEn.getValue(), ct));
        }
        if (!hasLink) {
            tvi.setTextIsSelectable(true);
        }

        final String btnT = contentEn.getTypeString();
        final Button btni = new Button(ct);
        btni.setGravity(Gravity.CENTER_VERTICAL);
        layout.addView(btni, Tool.getMarginLayoutTop());
        btni.setText("+ " + btnT);
        btni.setBackgroundResource(R.drawable.btn_shape2);

        btni.setOnClickListener(v -> {
            try {
                LinearLayout layout1 = findViewById(R.id.detLayout);
                int ind = layout1.indexOfChild(btni);
                if (tvi.getParent() == null) {
                    layout1.addView(tvi, ind + 1, Tool.getMarginLayoutBottom());
                    btni.setText("- " + btnT);
                    btni.setBackgroundResource(R.drawable.btn_shape_up_gray);
                } else {
                    layout1.removeView(tvi);
                    btni.setText("+ " + btnT);
                    btni.setBackgroundResource(R.drawable.btn_shape2);
                }
            } catch (Exception e) {
                Logger.exception(e);
            }
        });
        btni.setOnLongClickListener(v -> {
            Service.getC().CB(tvi.getText().toString(), ct);
            Toast.makeText(ct, "已复制" + btnT, Toast.LENGTH_SHORT).show();
            return true;
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
