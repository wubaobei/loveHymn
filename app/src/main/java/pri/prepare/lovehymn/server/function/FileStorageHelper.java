package pri.prepare.lovehymn.server.function;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import pri.prepare.lovehymn.client.tool.LoadProcess;
import pri.prepare.lovehymn.server.entity.Logger;

/**
 * 复制assets到sd卡
 */
public class FileStorageHelper {
    public static int assetsCount = 0;

    public static void getAssetsCount(Context context, String assetsPath) throws IOException {
        AssetManager assetManager = context.getAssets();
        String[] fileNames = assetManager.list(assetsPath);
        String temp = "";
        if (fileNames.length > 0) {//如果是目录 apk
            for (String fileName : fileNames) {
                if (!TextUtils.isEmpty(assetsPath)) {
                    temp = assetsPath + File.separator + fileName;//补全assets资源路径
                }

                String[] childFileNames = assetManager.list(temp);
                if (!TextUtils.isEmpty(temp) && childFileNames.length > 0) {//判断是文件还是文件夹：如果是文件夹
                    getAssetsCount(context, temp);
                } else {//如果是文件
                    assetsCount++;
                }
            }
        }
    }

    /**
     * 复制assets中的文件到指定目录
     *
     * @param context     上下文
     * @param assetsPath  assets资源路径
     * @param storagePath 目标文件夹的路径
     */
    public static void copyFilesFromAssets(Context context, String assetsPath, String storagePath, boolean forceUpdate) {
        String temp = "";

        File f = new File(SdCardTool.getResPath());
        if (!f.exists())
            f.mkdirs();

        if (TextUtils.isEmpty(storagePath)) {
            return;
        } else if (storagePath.endsWith(File.separator)) {
            storagePath = storagePath.substring(0, storagePath.length() - 1);
        }
        if (TextUtils.isEmpty(assetsPath) || assetsPath.equals(File.separator)) {
            assetsPath = "";
        } else if (assetsPath.endsWith(File.separator)) {
            assetsPath = assetsPath.substring(0, assetsPath.length() - 1);
        }

        AssetManager assetManager = context.getAssets();
        try {
            File file = new File(storagePath);
            if (!file.exists()) {//如果文件夹不存在，则创建新的文件夹
                file.mkdirs();
            }

            // 获取assets目录下的所有文件及目录名
            String[] fileNames = assetManager.list(assetsPath);
            if (fileNames.length > 0) {//如果是目录 apk
                for (String fileName : fileNames) {
                    if (!TextUtils.isEmpty(assetsPath)) {
                        temp = assetsPath + File.separator + fileName;//补全assets资源路径
                    }

                    String[] childFileNames = assetManager.list(temp);
                    if (!TextUtils.isEmpty(temp) && childFileNames.length > 0) {//判断是文件还是文件夹：如果是文件夹
                        copyFilesFromAssets(context, temp, storagePath + File.separator + fileName, forceUpdate);
                    } else {//如果是文件
                        InputStream inputStream = assetManager.open(temp);
                        readInputStream(storagePath + File.separator + fileName, inputStream, forceUpdate);
                    }
                }
            } else {//如果是文件 doc_test.txt或者apk/app_test.apk
                InputStream inputStream = assetManager.open(assetsPath);
                if (assetsPath.contains(File.separator)) {//apk/app_test.apk
                    assetsPath = assetsPath.substring(assetsPath.lastIndexOf(File.separator));
                }
                readInputStream(storagePath + File.separator + assetsPath, inputStream, forceUpdate);
            }
        } catch (IOException e) {
            Logger.exception(e);
        }
    }

    private static int fileCopyCount = 0;

    /**
     * 读取输入流中的数据写入输出流
     *
     * @param storagePath 目标文件路径
     * @param inputStream 输入流
     */
    private static void readInputStream(String storagePath, InputStream inputStream, boolean forceUpdate) {
        fileCopyCount++;
        if (fileCopyCount % 100 == 0)
            Logger.info("加载" + fileCopyCount);
        File file = new File(storagePath);
        try {
            if (forceUpdate && file.exists()) {
                if (!file.delete()) {
                    Logger.info("删除" + file.getAbsolutePath() + "失败");
                }
            }

            if (!file.exists()) {
                // 1.建立通道对象
                FileOutputStream fos = new FileOutputStream(file);
                // 2.定义存储空间
                byte[] buffer = new byte[inputStream.available()];
                // 3.开始读文件
                int lenght;
                while ((lenght = inputStream.read(buffer)) > 0) {// 循环从输入流读取buffer字节
                    // 将Buffer中的数据写到outputStream对象中
                    fos.write(buffer, 0, lenght);
                }
                fos.flush();// 刷新缓冲区
                // 4.关闭流
                fos.close();
                inputStream.close();
            }
            LoadProcess.FILE_COUNT++;
        } catch (Exception e) {
            Logger.info("复制 " + storagePath + " 失败");
            //Logger.exception(e);
        }
    }
}
