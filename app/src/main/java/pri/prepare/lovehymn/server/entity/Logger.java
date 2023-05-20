package pri.prepare.lovehymn.server.entity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import pri.prepare.lovehymn.server.function.SdCardTool;

public class Logger {
    public static String getCurrentLogFileName() {
        File file = new File(SdCardTool.getLogPath());
        if (!file.exists())
            file.mkdirs();
        return file.getAbsolutePath() + "/" + getName();
    }

    private static String getName() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(new Date()) + ".txt";
    }

    private static final ArrayList<String> temp = new ArrayList<>();

    private static void output(String flag, String s) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.");
        temp.add(sdf.format(new Date()) + " " + flag + ":" + s + "\r\n");
    }

    public static void info(String s) {
        if (s == null || s.length() == 0)
            s = "啥都没有";

        output("info", s);
        Log.i("pppre", s);
    }

    public static void exception(Exception e, Context ct) {
        StringBuilder res = new StringBuilder();
        res.append(e.getMessage());
        for (StackTraceElement t : e.getStackTrace()) {
            res.append("\r\n").append(t.getClassName()).append(" ").append(t.getMethodName()).append(" ").append(t.getLineNumber());
        }

        output("exception", res.toString());
        Log.i("pppre", res.toString());
        Toast.makeText(ct,res.toString(),Toast.LENGTH_LONG).show();
        writeToFile();
    }
    public static void exception(Exception e) {
        StringBuilder res = new StringBuilder();
        res.append(e.getMessage());
        for (StackTraceElement t : e.getStackTrace()) {
            res.append("\r\n").append(t.getClassName()).append(" ").append(t.getMethodName()).append(" ").append(t.getLineNumber());
        }

        output("exception", res.toString());
        Log.i("pppre", res.toString());
        writeToFile();
    }

    /**
     * 持久化日志
     */
    public static void writeToFile() {
        String newPath = getCurrentLogFileName();
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(newPath, true), StandardCharsets.UTF_8)) {
            for (String s : temp)
                out.append(s);
            temp.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
