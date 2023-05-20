package pri.prepare.lovehymn.client;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;

import pri.prepare.lovehymn.R;
import pri.prepare.lovehymn.databinding.ActicityAddPdfBinding;
import pri.prepare.lovehymn.server.Service;
import pri.prepare.lovehymn.server.UpdateHistory;
import pri.prepare.lovehymn.server.entity.Author;
import pri.prepare.lovehymn.server.entity.Book;
import pri.prepare.lovehymn.server.entity.Hymn;
import pri.prepare.lovehymn.server.entity.Logger;
import pri.prepare.lovehymn.server.entity.MyFile;
import pri.prepare.lovehymn.server.function.CharConst;
import pri.prepare.lovehymn.server.function.SdCardTool;

/**
 * 特殊功能：添加自定义pdf
 */
public class AddPdfActivity extends AppCompatActivity {
    ActicityAddPdfBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.acticity_add_pdf);

        choosePdfBtnSet();
        addNowBtnSet();
        otherBtnSet();
    }

    private String path;

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private boolean isInitSp = true;
    private boolean isInitSp2 = true;

    private static final String NULL = "无作者";

    private void otherBtnSet() {
        Author[] as = Author.getAll();
        String[] ans = new String[as.length + 1];
        ans[0] = NULL;
        for (int i = 1; i <= as.length; i++)
            ans[i] = as[i - 1].getName();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ans);
        binding.authorSpinner.setAdapter(adapter);
        binding.authorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSp)
                    isInitSp = false;
                else {
                    String s = (String) binding.authorSpinner.getSelectedItem();
                    if (s.equals(NULL))
                        binding.musicAuthor.setText("");
                    else
                        binding.musicAuthor.setText(s);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.musicAuthor.setText("");

        binding.authorSpinner2.setAdapter(adapter);
        binding.authorSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSp2)
                    isInitSp2 = false;
                else {
                    String s = (String) binding.authorSpinner2.getSelectedItem();
                    if (s.equals(NULL))
                        binding.lyricAuthor.setText("");
                    else
                        binding.lyricAuthor.setText(s);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.lyricAuthor.setText("");
    }

    private void addNowBtnSet() {
        binding.addNow.setOnClickListener(v -> {
            File f = new File(binding.filePath.getText().toString());
            if (!f.isFile()) {
                toast("pdf不存在或未选择：" + f.getAbsolutePath());
                return;
            }
            String sind = binding.fileIndex.getText().toString();
            int ind;
            try {
                ind = Integer.parseInt(sind);
                if (ind < 1 || ind > 999) {
                    toast("序号要在1-999之间");
                    return;
                }
            } catch (Exception e) {
                toast("未填写序号或序号填写错误");
                return;
            }
            String title = binding.fileTitle.getText().toString().trim();
            if (title.length() == 0) {
                toast("未填写标题");
                return;
            }
            try {
                Hymn h = Hymn.search(Book.Other, ind, 1);
                if (h != null) {
                    toast("该序号已存在：" + h.getTitle());
                    return;
                }
                String newName = ind > 99 ? (ind + ".pdf") : (ind > 9 ? ("0" + ind + ".pdf") : ("00" + ind + ".pdf"));
                String newPath = SdCardTool.getLbPath() + File.separator + Book.Other.FullName + File.separator + (ind / 100) + File.separator + newName;
                if (new File(newPath).exists()) {
                    toast("已存在pdf:" + newPath);
                    return;
                }

                if (copyFileUsingFileStreams(f, new File(newPath))) {
                    h = new Hymn();
                    h.setBookId(Book.Other.id);
                    h.setIndex1(ind);
                    h.setIndex2(1);
                    h.setTitle(title);
                    //author set
                    String musicAuthorName = binding.musicAuthor.getText().toString().trim();
                    if (musicAuthorName.length() > 0) {
                        Author author = Author.search(musicAuthorName);
                        if (author == null) {
                            author = new Author();
                            author.setName(musicAuthorName);
                            author.addOrUpdateByName();
                            author = Author.search(musicAuthorName);
                            Logger.info("新增曲作者：" + musicAuthorName);
                        }
                        h.addMusicAuthorId(author.getId());
                    }
                    String lyricAuthorName = binding.lyricAuthor.getText().toString().trim();
                    if (lyricAuthorName.length() > 0) {
                        Author author = Author.search(lyricAuthorName);
                        if (author == null) {
                            author = new Author();
                            author.setName(lyricAuthorName);
                            author.addOrUpdateByName();
                            author = Author.search(lyricAuthorName);
                            Logger.info("新增词作者：" + lyricAuthorName);
                        }
                        h.addLyricAuthorId(author.getId());
                    }
                    //
                    h.addOrUpdateByIndex();

                    String resFile = SdCardTool.getResPath() + File.separator + (UpdateHistory.MIN_RES_INDEX + 99) + ".qita.txt";
                    try (FileWriter fw = new FileWriter(resFile, true); BufferedWriter bw = new BufferedWriter(fw)) {
                        bw.write("序号：O" + newName.substring(0, 3));
                        bw.write("\r\n");
                        bw.write("标题：" + title);
                        bw.write("\r\n");
                    }
                    toast("添加成功");
                    binding.musicAuthor.setText("");
                    binding.fileIndex.setText("");
                    binding.fileTitle.setText("");
                    binding.filePath.setText("pdf路径");
                } else {
                    toast("复制文件错误");
                }

            } catch (Exception e) {
                Logger.exception(e);
            }
        });
    }

    private static final int REQUEST_MANAGER_PERMISSION = 12342;

    private void requestManagerPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 没文件管理权限时申请权限
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, REQUEST_MANAGER_PERMISSION);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MANAGER_PERMISSION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //用户拒绝权限，重新申请
            if (!Environment.isExternalStorageManager()) {
                requestManagerPermission();
            }
        }
    }

    private boolean copyFileUsingFileStreams(File source, File dest) {
        requestManagerPermission();
        dest.getParentFile().mkdirs();
        try (InputStream input = new FileInputStream(source); OutputStream output = new FileOutputStream(dest)) {
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, bytesRead);
            }
            return true;
        } catch (Exception e) {
            Logger.info("复制或移动文件错误：" + e.getMessage());
            return false;
        }
    }

    private void choosePdfBtnSet() {
        binding.choosePdf.setOnClickListener(v -> {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
            updateDF();
        });
    }

    private static final String UP = "……";

    /**
     * 刷新文件列表
     */
    private void updateDF() {
        if (path == null || path.length() == 0) {
            binding.fileList.setVisibility(View.GONE);
            return;
        }
        binding.fileList.setVisibility(View.VISIBLE);
        binding.fileList.removeAllViews();
        if (!path.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            Button btn = new Button(this);
            btn.setText(UP);
            btn.setOnClickListener(listener);
            binding.fileList.addView(btn);
        }
        MyFile file = MyFile.from(path);
        for (MyFile f : file.listFiles())
            if (f.isPdf()) {
                Button btn = new Button(this);
                btn.setText(CharConst.BOOK + f.getName());
                btn.setOnClickListener(listener);
                btn.setAllCaps(false);
                btn.setGravity(Gravity.CENTER_VERTICAL);
                binding.fileList.addView(btn);
            }
        for (MyFile f : file.listFilesOrders())
            if (f.isDirectory() && !f.isHiddenDirectory()) {
                Button btn = new Button(this);
                btn.setText(f.getName());
                btn.setAllCaps(false);
                btn.setGravity(Gravity.CENTER_VERTICAL);
                btn.setOnClickListener(listener);
                binding.fileList.addView(btn);
            }
    }

    View.OnClickListener listener = v -> {
        if (((Button) v).getText().toString().equals(UP)) {
            path = new File(path).getParentFile().getAbsolutePath();
            updateDF();
            return;
        }

        File f = new File(path + File.separator + ((Button) v).getText().toString().replace(CharConst.BOOK, ""));
        if (f.isFile()) {
            if (MyFile.from(f.getAbsolutePath()).getHymn() != null) {
                Toast.makeText(this, "该pdf已存在", Toast.LENGTH_SHORT).show();
                return;
            } else {
                binding.filePath.setText(f.getAbsolutePath());
                path = "";
                binding.fileTitle.setText(f.getName().replace(".pdf", ""));
                binding.fileIndex.setText(Service.getC().getNewOtherBookIndex());
            }
        } else {
            path = f.getAbsolutePath();
        }
        updateDF();
    };
}
