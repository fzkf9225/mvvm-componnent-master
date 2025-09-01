package pers.fz.mvvm.util.common;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import pers.fz.mvvm.listener.FileUploadProgressListener;
import pers.fz.mvvm.util.encode.MD5Util;
import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.util.upload.ProgressRequestBody;


public final class FileUtil {
    private static final String TAG = "FileUtils";

    private FileUtil() {
        throw new Error("error");
    }

    /**
     * 分隔符.
     */
    public final static String FILE_EXTENSION_SEPARATOR = ".";

    /**
     * 获取临时文件
     * @param url 文件地址
     * @param saveBasePath 保存基础路径
     * @return 临时文件
     */
    public static File getTempFile(String url, String saveBasePath) {
        String tempFileName = null;
        try {
            tempFileName = "TEMP" + MD5Util.md5Encode(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new File(saveBasePath, tempFileName + ".temp.download");
    }

    public static String getUrlFileExtensionName(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        String[] tmp = url.split("/"); // 按照"/"分割
        String pp = tmp[tmp.length - 1]; // 获取最后一部分，即文件名和参数
        tmp = pp.split("\\?"); // 把参数和文件名分割开
        if (tmp.length == 0) {
            return "";
        }

        String[] nameTemp = tmp[0].split("\\."); // 按照"."分割
        System.out.println("nameTemp: " + Arrays.toString(nameTemp));
        return nameTemp.length >= 2 ? nameTemp[1] : "";
    }

    public static byte[] getFileToByte(File file) {
        byte[] by = new byte[(int) file.length()];
        try {
            InputStream is = new FileInputStream(file);
            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
            byte[] bb = new byte[1024 * 1024];
            int ch;
            ch = is.read(bb);
            while (ch != -1) {
                bytestream.write(bb, 0, ch);
                ch = is.read(bb);
            }
            by = bytestream.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return by;
    }

    /**
     * 判断SD卡是否可用
     *
     * @return SD卡可用返回true
     */
    public static boolean hasSdcard() {
        String status = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(status);
    }

    public static File getFile(Bitmap bitmap, String fileName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        File file = new File(Environment.getExternalStorageDirectory() + fileName);
        try {
            boolean result = file.createNewFile();
            if (!result) {
                return null;
            }
            FileOutputStream fos = new FileOutputStream(file);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            int x = 0;
            byte[] b = new byte[1024 * 100];
            while ((x = is.read(b)) != -1) {
                fos.write(b, 0, x);
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File saveFile(String filePath, ResponseBody body) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        File file = null;
        try {
            if (filePath == null) {
                return null;
            }
            file = new File(filePath);
            if (!file.exists()) {
                boolean result = file.createNewFile();
            }
            long fileSizeDownloaded = 0;
            byte[] fileReader = new byte[4096];

            inputStream = body.byteStream();
            outputStream = new FileOutputStream(file);

            while (true) {
                int read = inputStream.read(fileReader);
                if (read == -1) {
                    break;
                }
                outputStream.write(fileReader, 0, read);
                fileSizeDownloaded += read;

            }

            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return file;
    }

    /**
     * @param filePath
     * @param start    起始位置
     * @param body
     */
    public static File saveFile(String filePath, long start, ResponseBody body) {
        InputStream inputStream = null;
        RandomAccessFile raf = null;
        File file = null;
        try {
            file = new File(filePath);

            raf = new RandomAccessFile(filePath, "rw");
            inputStream = body.byteStream();
            byte[] fileReader = new byte[4096];

            raf.seek(start);

            while (true) {
                int read = inputStream.read(fileReader);
                if (read == -1) {
                    break;
                }
                raf.write(fileReader, 0, read);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return file;

    }

    public static String saveBitmap(String path, Bitmap image) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File appDir = new File(path);
        if (!appDir.exists()) {
            boolean isCreated = appDir.mkdirs();
        }
        //设置图片路径
        String fileName = FileUtil.getNoRepeatFileName(appDir, "IMG_", ".jpg");
        File imageDir = new File(appDir, fileName + ".jpg");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imageDir);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageDir.getAbsolutePath();
    }

    /**
     * 重命名文件：
     * oldPath 和 newPath必须是新旧文件的绝对路径
     */
    public static boolean renameFile(String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath)) {
            return false;
        }
        if (TextUtils.isEmpty(newPath)) {
            return false;
        }
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        if (newFile.exists()) {
            return false;
        }
        //重命名
        return oldFile.renameTo(newFile);
    }

    /**
     * 获取basePath下不重复的文件名
     *
     * @param basePath  基础目录
     * @param prefix    默认前缀
     * @param extension 扩展名
     * @return 文件名，不带后缀名的
     */
    public static String getNoRepeatFileName(String basePath, String prefix, String extension) {
        File baseFile = new File(basePath);
        if (!baseFile.exists()) {
            boolean isCreated = baseFile.mkdirs();
        }
        String fileName = prefix + DateUtil.dateFormat(new Date(), DateUtil.DATE_TIME_FORMAT) + "_" + new Random().nextInt(1000);
        File file = new File(baseFile, fileName + extension);
        int index = 0;
        //防止重名
        while (file.exists()) {
            index += 1;
            file = new File(baseFile, fileName + "_" + index + extension);
        }
        return fileName;
    }

    /**
     * 获取basePath下不重复的文件名
     *
     * @return 如果存储路径中有重复的则自动+1，如果没有则返回文件名
     */
    public static String autoRenameFileName(String baseSavePath, String oldName) {
        try {
            // 检查文件是否有后缀名
            if (!oldName.contains(".")) {
                oldName += "." + oldName.split("\\.")[oldName.split("\\.").length - 1];
            }

            // 拼接完整的文件路径
            String filePath = baseSavePath + File.separator + oldName;

            // 判断文件是否存在
            File file = new File(filePath);
            if (!file.exists()) {
                return oldName;
            }

            // 文件已存在，查找可用的文件名
            int count = 1;
            while (true) {
                String newFileName = oldName.split("\\.")[0] + count + "." + oldName.split("\\.")[oldName.split("\\.").length - 1];
                String newFilePath = baseSavePath + File.separator + newFileName;
                File newFile = new File(newFilePath);
                if (!newFile.exists()) {
                    return newFileName;
                }
                count++;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return oldName;
    }

    /**
     * 获取basePath下不重复的文件名
     *
     * @param basePath  基础目录
     * @param prefix    默认前缀
     * @param extension 扩展名
     * @return 文件名，不带后缀名的
     */
    public static String getNoRepeatFileName(File basePath, String prefix, String extension) {
        if (!basePath.exists()) {
            boolean isCreated = basePath.mkdirs();
        }
        String fileName = prefix + DateUtil.dateFormat(new Date(), DateUtil.DATE_TIME_FORMAT) + "_" + new Random().nextInt(1000);
        File file = new File(basePath, fileName + extension);
        int index = 0;
        //防止重名
        while (file.exists()) {
            index += 1;
            file = new File(basePath, fileName + "_" + index + extension);
        }
        return fileName;
    }

    /**
     * 读取文件的内容
     * <br>
     * 默认utf-8编码
     *
     * @param filePath 文件路径
     * @return 字符串
     * @throws IOException
     */
    public static String readFile(String filePath) throws IOException {
        return readFile(filePath, "utf-8");
    }

    /**
     * 读取文件的内容
     *
     * @param filePath    文件目录
     * @param charsetName 字符编码
     * @return String字符串
     */
    public static String readFile(String filePath, String charsetName)
            throws IOException {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        if (TextUtils.isEmpty(charsetName)) {
            charsetName = "utf-8";
        }
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder();
        if (!file.isFile()) {
            return null;
        }
        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(
                    file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().isEmpty()) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            return fileContent.toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取文本文件到List字符串集合中(默认utf-8编码)
     *
     * @param filePath 文件目录
     * @return 文件不存在返回null，否则返回字符串集合
     * @throws IOException
     */
    public static List<String> readFileToList(String filePath)
            throws IOException {
        return readFileToList(filePath, "utf-8");
    }

    /**
     * 读取文本文件到List字符串集合中
     *
     * @param filePath    文件目录
     * @param charsetName 字符编码
     * @return 文件不存在返回null，否则返回字符串集合
     */
    public static List<String> readFileToList(String filePath,
                                              String charsetName) throws IOException {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        if (TextUtils.isEmpty(charsetName)) {
            charsetName = "utf-8";
        }
        File file = new File(filePath);
        List<String> fileContent = new ArrayList<String>();
        if (!file.isFile()) {
            return null;
        }
        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(
                    file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                fileContent.add(line);
            }
            return fileContent;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 向文件中写入数据
     *
     * @param filePath 文件目录
     * @param content  要写入的内容
     * @param append   如果为 true，则将数据写入文件末尾处，而不是写入文件开始处
     * @return 写入成功返回true， 写入失败返回false
     * @throws IOException
     */
    public static boolean writeFile(String filePath, String content,
                                    boolean append) throws IOException {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        FileWriter fileWriter = null;
        try {
            createFile(filePath);
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            fileWriter.flush();
            return true;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 向文件中写入数据<br>
     * 默认在文件开始处重新写入数据
     *
     * @param filePath 文件目录
     * @param stream   字节输入流
     * @return 写入成功返回true，否则返回false
     * @throws IOException
     */
    public static boolean writeFile(String filePath, InputStream stream)
            throws IOException {
        return writeFile(filePath, stream, false);
    }

    /**
     * 向文件中写入数据
     *
     * @param filePath 文件目录
     * @param stream   字节输入流
     * @param append   如果为 true，则将数据写入文件末尾处；
     *                 为false时，清空原来的数据，从头开始写
     * @return 写入成功返回true，否则返回false
     * @throws IOException
     */
    public static boolean writeFile(String filePath, InputStream stream,
                                    boolean append) throws IOException {
        if (TextUtils.isEmpty(filePath)) {
            throw new NullPointerException("filePath is Empty");
        }
        if (stream == null) {
            throw new NullPointerException("InputStream is null");
        }
        return writeFile(new File(filePath), stream,
                append);
    }

    /**
     * 向文件中写入数据
     * 默认在文件开始处重新写入数据
     *
     * @param file   指定文件
     * @param stream 字节输入流
     * @return 写入成功返回true，否则返回false
     * @throws IOException
     */
    public static boolean writeFile(File file, InputStream stream)
            throws IOException {
        return writeFile(file, stream, false);
    }

    /**
     * 向文件中写入数据
     *
     * @param file   指定文件
     * @param stream 字节输入流
     * @param append 为true时，在文件开始处重新写入数据；
     *               为false时，清空原来的数据，从头开始写
     * @return 写入成功返回true，否则返回false
     * @throws IOException
     */
    public static boolean writeFile(File file, InputStream stream,
                                    boolean append) throws IOException {
        if (file == null) {
            throw new NullPointerException("file = null");
        }
        OutputStream out = null;
        try {
            createFile(file.getAbsolutePath());
            out = new FileOutputStream(file, append);
            byte[] data = new byte[1024];
            int length = -1;
            while ((length = stream.read(data)) != -1) {
                out.write(data, 0, length);
            }
            out.flush();
            return true;
        } finally {
            if (out != null) {
                try {
                    out.close();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 复制文件
     *
     * @param sourceFilePath 源文件目录（要复制的文件目录）
     * @param destFilePath   目标文件目录（复制后的文件目录）
     * @return 复制文件成功返回true，否则返回false
     * @throws IOException
     */
    public static boolean copyFile(String sourceFilePath, String destFilePath)
            throws IOException {
        InputStream inputStream = null;
        inputStream = new FileInputStream(sourceFilePath);
        return writeFile(destFilePath, inputStream);
    }


    /**
     * 获取某个目录下的文件名
     *
     * @param dirPath    目录
     * @param fileFilter 过滤器
     * @return 某个目录下的所有文件名
     */
    public static List<String> getFileNameList(String dirPath,
                                               FilenameFilter fileFilter) {
        if (fileFilter == null) {
            return getFileNameList(dirPath);
        }
        if (TextUtils.isEmpty(dirPath)) {
            return Collections.emptyList();
        }
        File dir = new File(dirPath);

        File[] files = dir.listFiles(fileFilter);
        if (files == null)
            return Collections.emptyList();

        List<String> conList = new ArrayList<String>();
        for (File file : files) {
            if (file.isFile()) {
                conList.add(file.getName());
            }
        }
        return conList;
    }

    /**
     * 获取某个目录下的文件名
     *
     * @param dirPath 目录
     * @return 某个目录下的所有文件名
     */
    public static List<String> getFileNameList(String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            return Collections.emptyList();
        }
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        List<String> conList = new ArrayList<String>();
        for (File file : files) {
            if (file.isFile()) {
                conList.add(file.getName());
            }
        }
        return conList;
    }

    /**
     * 获取某个目录下的指定扩展名的文件名称
     *
     * @param dirPath 目录
     * @return 某个目录下的所有文件名
     */
    public static List<String> getFileNameList(String dirPath,
                                               final String extension) {
        if (TextUtils.isEmpty(dirPath)) {
            return Collections.emptyList();
        }
        File dir = new File(dirPath);
        File[] files = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.indexOf("." + extension) > 0;
            }
        });
        if (files == null) {
            return Collections.emptyList();
        }
        List<String> conList = new ArrayList<String>();
        for (File file : files) {
            if (file.isFile()) {
                conList.add(file.getName());
            }
        }
        return conList;
    }

    /**
     * 获得文件的扩展名
     *
     * @param filePath 文件路径
     * @return 如果没有扩展名，返回""
     */
    public static String getFileExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (extenPosi == -1) {
            return "";
        }
        return (filePosi >= extenPosi) ? "" : filePath.substring(extenPosi + 1);
    }

    public static String getLastPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        String[] pathArr = path.split(File.separator);
        if (pathArr.length == 0) {
            return null;
        }
        if (pathArr.length == 1) {
            return pathArr[0];
        }
        if (File.separator.equals(pathArr[pathArr.length - 1])) {
            return pathArr[pathArr.length - 2];
        }
        return pathArr[pathArr.length - 1];
    }

    public static String getLastPath(String path, String defaultPath) {
        if (TextUtils.isEmpty(path)) {
            return defaultPath;
        }
        String[] pathArr = path.split(File.separator);
        if (pathArr.length == 0) {
            return defaultPath;
        }
        if (pathArr.length == 1) {
            return pathArr[0];
        }
        if (File.separator.equals(pathArr[pathArr.length - 1])) {
            return TextUtils.isEmpty(pathArr[pathArr.length - 2]) ? defaultPath : pathArr[pathArr.length - 2];
        }
        return TextUtils.isEmpty(pathArr[pathArr.length - 1]) ? defaultPath : pathArr[pathArr.length - 1];
    }

    public static String getDefaultBasePath(Context mContext) {
        String packageName = mContext.getPackageName();
        String[] packageArr = packageName.split("\\.");
        if (packageArr.length == 0) {
            return "";
        }
        if (packageArr.length == 1) {
            return packageArr[0];
        }
        return packageArr[1];
    }

    /**
     * 创建文件
     *
     * @param path 文件的绝对路径
     * @return
     */
    public static boolean createFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        return createFile(new File(path));
    }

    /**
     * 创建文件
     *
     * @param file
     * @return 创建成功返回true
     */
    public static boolean createFile(File file) {
        if (file == null || !makeDirs(getFolderName(file.getAbsolutePath()))) {
            return false;
        }
        if (!file.exists()) {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * 创建目录（可以是多个）
     *
     * @param filePath 目录路径
     * @return 如果路径为空时，返回false；如果目录创建成功，则返回true，否则返回false
     */
    public static boolean makeDirs(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File folder = new File(filePath);
        return (folder.exists() && folder.isDirectory()) || folder
                .mkdirs();
    }

    /**
     * 创建目录（可以是多个）
     *
     * @param dir 目录
     * @return 如果目录创建成功，则返回true，否则返回false
     */
    public static boolean makeDirs(File dir) {
        if (dir == null) {
            return false;
        }
        return dir.exists() && dir.isDirectory() || dir.mkdirs();
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @return 如果路径为空或者为空白字符串，就返回false；如果文件存在，且是文件，
     * 就返回true；如果不是文件或者不存在，则返回false
     */
    public static boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }

    /**
     * 获得不带扩展名的文件名称
     *
     * @param filePath 文件路径
     * @return
     */
    public static String getFileNameWithoutExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (filePosi == -1) {
            return (extenPosi == -1 ? filePath : filePath.substring(0,
                    extenPosi));
        }
        if (extenPosi == -1) {
            return filePath.substring(filePosi + 1);
        }
        return (filePosi < extenPosi ? filePath.substring(filePosi + 1,
                extenPosi) : filePath.substring(filePosi + 1));
    }

    /**
     * 获得文件名
     *
     * @param filePath 文件路径
     * @return 如果路径为空或空串，返回路径名；不为空时，返回文件名
     */
    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? filePath : filePath.substring(filePosi + 1);
    }

    public static String getFileNameByUrl(String url) {
        // 获取文件路径部分
        URL fileUrl = null;
        try {
            if (TextUtils.isEmpty(url)) {
                return url;
            }
            fileUrl = new URL(url);
            String filePath = fileUrl.getPath();
            // 找到最后一个斜杠的位置
            int lastIndex = filePath.lastIndexOf("/");

            // 截取文件名部分
            String fileName = filePath.substring(lastIndex + 1);

            // 去除参数部分
            int questionMarkIndex = fileName.indexOf("?");
            if (questionMarkIndex != -1) {
                fileName = fileName.substring(0, questionMarkIndex);
            }
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis() + ".apk";
    }

    /**
     * 获得所在目录名称
     *
     * @param filePath 文件的绝对路径
     * @return 如果路径为空或空串，返回路径名；不为空时，如果为根目录，返回"";
     * 如果不是根目录，返回所在目录名称，格式如：C:/Windows/Boot
     */
    public static String getFolderName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }

    /**
     * 判断目录是否存在
     *
     * @param
     * @return 如果路径为空或空白字符串，返回false；如果目录存在且，确实是目录文件夹，
     * 返回true；如果不是文件夹或者不存在，则返回false
     */
    public static boolean isFolderExist(String directoryPath) {
        if (TextUtils.isEmpty(directoryPath)) {
            return false;
        }
        File dire = new File(directoryPath);
        return (dire.exists() && dire.isDirectory());
    }

    /**
     * 删除指定文件或指定目录内的所有文件
     *
     * @param path 文件或目录的绝对路径
     * @return 路径为空或空白字符串，返回true；文件不存在，返回true；文件删除返回true；
     * 文件删除异常返回false
     */
    public static boolean deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return true;
        }
        return deleteFile(new File(path));
    }

    /**
     * 删除指定文件或指定目录内的所有文件
     *
     * @param file
     * @return 路径为空或空白字符串，返回true；文件不存在，返回true；文件删除返回true；
     * 文件删除异常返回false
     */
    public static boolean deleteFile(File file) {
        if (file == null)
            throw new NullPointerException("file is null");
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }

        File[] files = file.listFiles();
        if (files == null)
            return true;
        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    /**
     * 删除指定目录中特定的文件
     *
     * @param dir
     * @param filter
     */
    public static void delete(String dir, FilenameFilter filter) {
        if (TextUtils.isEmpty(dir)) {
            return;
        }
        File file = new File(dir);
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        }
        if (!file.isDirectory()) {
            return;
        }

        File[] lists = null;
        if (filter != null) {
            lists = file.listFiles(filter);
        } else {
            lists = file.listFiles();
        }

        if (lists == null) {
            return;
        }
        for (File f : lists) {
            if (f.isFile()) {
                f.delete();
            }
        }
    }

    /**
     * 获得文件或文件夹的大小
     *
     * @param path 文件或目录的绝对路径
     * @return 返回当前目录的大小 ，注：当文件不存在，为空，或者为空白字符串，返回 -1
     */
    public static long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }
        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    public static long getFileSize(Context context, Uri uri) {
        if (uri == null) {
            return -1;
        }

        Cursor cursor = context.getContentResolver().query(uri, new String[]{
                MediaStore.Files.FileColumns.SIZE
        }, null, null, null);
        long size = cursor.getLong(0);
        LogUtil.show(TAG, "size:" + size);
        return size;
    }

    /**
     * 保存文件到本地
     *
     * @param b
     * @param path 路径
     * @return
     */
    public static File saveFileFromBytes(byte[] b, String path, String fileName) {
        File rootFile = new File(path);
        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }
        String outputFile = path + String.valueOf(System.currentTimeMillis()) + fileName;
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = new File(outputFile);
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return file;
    }

    public static final Long K_SIZE = 1024L;
    public static final Long M_SIZE = K_SIZE * K_SIZE;
    public static final Long G_SIZE = M_SIZE * K_SIZE;
    public static final String B = "B";
    public static final String K = "KB";
    public static final String M = "MB";
    public static final String G = "GB";

    /**
     * 将字节数据转为带单位的字符串
     *
     * @param fileSize 文件字节大小
     * @return 带单位的字符串
     */
    public static String formatFileSize(long fileSize) {
        String fileSizeStr = "";
        if (fileSize == 0L) {
            return "0".concat(B);
        }
        // 小数位数自定义
        DecimalFormat df = new DecimalFormat("#.##");
        if (fileSize < K_SIZE) {
            fileSizeStr = df.format((double) fileSize) + B;
        } else if (fileSize < M_SIZE) {
            fileSizeStr = df.format((double) fileSize / K_SIZE) + K;
        } else if (fileSize < G_SIZE) {
            fileSizeStr = df.format((double) fileSize / M_SIZE) + M;
        } else {
            fileSizeStr = df.format((double) fileSize / G_SIZE) + G;
        }
        return fileSizeStr;
    }

    @SuppressLint("Range")
    public static Map<String, RequestBody> createFormDataRequestBody(Context mContext, Uri uri) {
        Map<String, RequestBody> formDataMap = new HashMap<>(0);
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            formDataMap.put("name", RequestBody.create(MediaType.parse("multipart/form-data"), TextUtils.isEmpty(name) ? "unknown" : name));
            cursor.close();
        } else {
            throw new RuntimeException("无附件操作权限");
        }
        return formDataMap;
    }

    @SuppressLint("Range")
    public static List<MultipartBody.Part> createFilePart(Context mContext, List<Uri> uriList, FileUploadProgressListener uploadListener) {
        if (uriList == null || uriList.isEmpty()) {
            return null;
        }
        List<MultipartBody.Part> multiList = new ArrayList<>(uriList.size());
        IntStream.range(0, uriList.size()).forEach(i ->
                multiList.add(createFilePart(mContext, uriList.get(i), i, uriList.size(), uploadListener))
        );
        return multiList;
    }

    public static MultipartBody.Part createFilePart(Context mContext, Uri uri, FileUploadProgressListener uploadListener) {
        return createFilePart(mContext, uri, 0, 1, uploadListener);
    }

    @SuppressLint("Range")
    public static MultipartBody.Part createFilePart(Context mContext, Uri uri, int currentPos, int totalCount, FileUploadProgressListener uploadListener) {
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            String fileName = null;
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
            }
            ParcelFileDescriptor pdf = contentResolver.openFileDescriptor(uri, "r");
            if (pdf == null) {
                throw new RuntimeException("读取文件失败");
            }
            FileDescriptor fileDescriptor = pdf.getFileDescriptor();
            RequestBody requestFile = RequestBody.create(
                    fileDescriptor,
                    MediaType.parse("multipart/form-data")
            );
            if (uploadListener == null) {
                pdf.close();
                return MultipartBody.Part.createFormData("file", TextUtils.isEmpty(fileName) ? "file" : fileName, requestFile);
            } else {
                ProgressRequestBody progressRequestBody = new ProgressRequestBody(requestFile, uri, currentPos, totalCount, uploadListener);
                pdf.close();
                return MultipartBody.Part.createFormData("file", TextUtils.isEmpty(fileName) ? "file" : fileName, progressRequestBody);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("无附件操作权限");
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败");
        }
    }

    @SuppressLint("Range")
    public static List<MultipartBody.Part> createAssetsFilePart(Context mContext, List<Uri> uriList, FileUploadProgressListener uploadListener) {
        if (uriList == null || uriList.isEmpty()) {
            return null;
        }
        List<MultipartBody.Part> multiList = new ArrayList<>(uriList.size());
        IntStream.range(0, uriList.size()).forEach(i ->
                multiList.add(createAssetsFilePart(mContext, uriList.get(i), i, uriList.size(), uploadListener))
        );
        return multiList;
    }

    public static MultipartBody.Part createAssetsFilePart(Context mContext, Uri uri, FileUploadProgressListener uploadListener) {
        return createAssetsFilePart(mContext, uri, 0, 1, uploadListener);
    }

    @SuppressLint("Range")
    public static MultipartBody.Part createAssetsFilePart(Context mContext, Uri uri, int currentPos, int totalCount, FileUploadProgressListener uploadListener) {
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            String fileName = null;
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
            }
            AssetFileDescriptor afd = contentResolver.openAssetFileDescriptor(uri, "r");
            if (afd == null) {
                throw new RuntimeException("读取文件失败");
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4 * 1024];
            int bytesRead;
            while ((bytesRead = afd.createInputStream().read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            RequestBody requestFile = RequestBody.create(
                    byteArrayOutputStream.toByteArray(),
                    MediaType.parse("multipart/form-data")
            );
            if (uploadListener == null) {
                afd.close();
                return MultipartBody.Part.createFormData("file", TextUtils.isEmpty(fileName) ? "file" : fileName, requestFile);
            } else {
                ProgressRequestBody progressRequestBody = new ProgressRequestBody(requestFile, uri, currentPos, totalCount, uploadListener);
                afd.close();
                return MultipartBody.Part.createFormData("file", TextUtils.isEmpty(fileName) ? "file" : fileName, progressRequestBody);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("无附件操作权限");
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败");
        }
    }

    @SuppressLint("Range")
    public static List<MultipartBody.Part> createTempFilePart(Context mContext, List<Uri> uriList, FileUploadProgressListener uploadListener) {
        if (uriList == null || uriList.isEmpty()) {
            return null;
        }
        List<MultipartBody.Part> multiList = new ArrayList<>(uriList.size());
        IntStream.range(0, uriList.size()).forEach(i ->
                multiList.add(createTempFilePart(mContext, uriList.get(i), i, uriList.size(), uploadListener))
        );
        return multiList;
    }

    public static MultipartBody.Part createTempFilePart(Context mContext, Uri uri, FileUploadProgressListener uploadListener) {
        return createTempFilePart(mContext, uri, 0, 1, uploadListener);
    }

    @SuppressLint("Range")
    public static MultipartBody.Part createTempFilePart(Context mContext, Uri uri, int currentPos, int totalCount, FileUploadProgressListener uploadListener) {
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            String fileName = null;
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
            }
            // 创建临时文件
            File tempFile = new File(mContext.getCacheDir(), TextUtils.isEmpty(fileName) ? "file" : fileName);
            tempFile.deleteOnExit();//删除旧文件
            InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            if (inputStream == null) {
                return null;
            }
            byte[] buffer = new byte[4 * 1024]; // 4K buffer
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            if (!tempFile.exists()) {
                throw new RuntimeException("临时文件生成失败");
            }
            RequestBody requestFile = RequestBody.create(tempFile, MediaType.parse("multipart/form-data"));

            if (uploadListener == null) {
                inputStream.close();
                return MultipartBody.Part.createFormData("file", TextUtils.isEmpty(fileName) ? "file" : fileName, requestFile);
            } else {
                ProgressRequestBody requestBody = new ProgressRequestBody(requestFile, uri, currentPos, totalCount, uploadListener);
                inputStream.close();
                return MultipartBody.Part.createFormData("file", TextUtils.isEmpty(fileName) ? "file" : fileName, requestBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("无附件操作权限");
        }
    }

    @SuppressLint("Range")
    public static File copyFileToCacheDir(Context mContext, Uri uri) throws IOException {
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        String fileName = null;
        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
        }
        // 创建临时文件
        File tempFile = new File(mContext.getCacheDir(), TextUtils.isEmpty(fileName) ? "file" : fileName);
        tempFile.deleteOnExit();//删除旧文件
        InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        if (inputStream == null) {
            return null;
        }
        byte[] buffer = new byte[4 * 1024]; // 4K buffer
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        outputStream.flush();
        inputStream.close();

        return tempFile;
    }

}

