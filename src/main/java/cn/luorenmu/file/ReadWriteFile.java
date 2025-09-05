package cn.luorenmu.file;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author LoMu
 * Date 2023.11.22 23:03
 */
public class ReadWriteFile {
    public static String CURRENT_PATH;


    /**
     * 获取jar文件所在目录
     *
     * @param clazz main类
     * @return path str
     */
    protected static String scanFilePath(Class<?> clazz) {
        URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
        String path = location.getPath();
        if (path.contains("jar")) {
            int i = path.indexOf("jar");
            path = path.substring(0, i);
        }
        String filePath = path.substring(path.indexOf("/"), path.lastIndexOf("/") + 1);

        filePath = URLDecoder.decode(filePath, StandardCharsets.UTF_8);
        return filePath;
    }


    public static String readFileJson(String path) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }


    public static String readCurrentFileJson(String fileName) {
        return readFileJson(currentPathFileName(fileName));
    }


    /**
     * 如果init中存在fileName文件 则将其拷贝至outputPath(输出包括文件名)
     * 否则创建一个为空的文件
     *
     * @param fileName   init文件
     * @param outputPath 输出路径.含文件名
     */
    private static void getInitFileStreamThenCreate(String fileName, String outputPath) {
        InputStream resourceAsStream = InitializeFile.class.getResourceAsStream(fileName);
        if (resourceAsStream == null) {
            return;
        }
        writeStreamFile(outputPath, resourceAsStream);
    }

    public static File writeStreamFile(String fileName, InputStream inputStream) {
        File file = new File(fileName);
        if (file.exists()) {
            return file;
        }
        OutputStream currentFile = createFile(fileName);
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(currentFile)) {
            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                bufferedOutputStream.write(bytes, 0, len);
                bufferedOutputStream.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public static File writeCurrentStreamFile(String fileName, InputStream inputStream) {
        return writeStreamFile(currentPathFileName(fileName), inputStream);
    }


    /**
     * 将对象写入json文件
     *
     * @param t        对象
     * @param filePath 当前文件目录
     * @param <T>      object
     * @return 如果成功写入返回true 如果 写入失败后文件已存在则返回false
     */
    public static <T> Boolean checkFileThenCreateObjectJson(T t, String filePath) {
        String outputFilePath = currentPathFileName(filePath);
        File file = new File(outputFilePath);
        if (!file.exists()) {
            entityWriteFile(filePath, t);
            return true;
        }
        return false;

    }

    public static String checkFileThenCreate(String fileName) {
        String outputFilePath = currentPathFileName(fileName);
        File file = new File(outputFilePath);
        if (!file.exists()) {
            getInitFileStreamThenCreate(fileName, outputFilePath);
        }
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        return fileName;
    }

    public static void createCurrentDirs(String fileName) {
        fileName = currentPathFileName(fileName);
        if (fileName.contains("/") || fileName.contains("\\")) {
            fileName = fileName.replaceAll("\\\\", "/");
        }
        int indexOf = fileName.lastIndexOf("/");
        String substring = fileName.substring(0, indexOf);
        File file = new File(substring);
        if (!file.exists()) {
            boolean ignored = file.mkdirs();
        }
    }

    public static OutputStream createFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                int i = path.lastIndexOf("/");
                if (i == -1) {
                    i = path.lastIndexOf("\\");
                }
                String dirStr = path.substring(0, i);
                File dir = new File(dirStr);
                boolean ignored1 = dir.mkdirs();
                boolean ignored2 = file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * @param fileName name
     * @return 输出流
     */
    public static OutputStream createCurrentFile(String fileName) {
        createCurrentDirs(fileName);
        String path = currentPathFileName(fileName);
        return createFile(path);
    }

    public static String currentPathFileName(String fileName) {
        return CURRENT_PATH + fileName;
    }

    public static <T> void entityWriteFileToCurrentDir(String name, T t) {
        entityWriteFile(currentPathFileName(name), t);
    }

    public static <T> void entityWriteFile(String path, T t) {
        try (OutputStream outputStream = createFile(path)) {
            String jsonString = JSON.toJSONString(t, JSONWriter.Feature.PrettyFormat);
            outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean fileExists(String fileName) {
        return new File(currentPathFileName(fileName)).exists();
    }

    public static InputStream readCurrentFile(String fileName) throws FileNotFoundException {
        String path = CURRENT_PATH + fileName;
        File file = new File(path);
        return new FileInputStream(file);
    }

    public static File[] currentDirs(String fileName) {
        File file = new File(currentPathFileName(fileName));
        return file.listFiles();
    }

    public static String streamToString(InputStream inputStream) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
        return result.toString();
    }

    public static JSONObject readFiles(File[] files) {
        JSONObject jsonObject = new JSONObject();
        for (File file : files) {
            try (FileInputStream fis = new FileInputStream(file)) {
                String s = streamToString(fis);
                String name = file.getName();
                if (name.contains("."))
                    name = name.substring(0, name.lastIndexOf("."));
                jsonObject.put(name, JSON.parseObject(s));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return jsonObject;
    }
}
