package cn.luorenmu.file;


/**
 * @author LoMu
 * Date 2023.11.06 19:12
 */

public class InitializeFile {


    public static void initConfig(Class<?> mainClazz) {
        String path = ReadWriteFile.scanFilePath(mainClazz);
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            ReadWriteFile.CURRENT_PATH = path.substring(1);
        }

    }


    public static void run(Class<?> clazz) {
        initConfig(clazz);
    }

}
