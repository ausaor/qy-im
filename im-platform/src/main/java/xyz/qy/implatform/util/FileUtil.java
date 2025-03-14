package xyz.qy.implatform.util;

public final class FileUtil {
    public FileUtil() {
    }

    /**
     * 获取文件后缀
     *
     * @param fileName 文件名
     * @return boolean
     */
    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 判断文件是否图片类型
     *
     * @param fileName 文件名
     * @return boolean
     */
    public static boolean isImage(String fileName) {
        String extension = getFileExtension(fileName);
        String[] imageExtension = new String[]{"jpeg", "jpg", "bmp", "png", "webp", "gif"};
        for (String e : imageExtension) {
            if (extension.toLowerCase().equals(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断文件是否视频类型
     *
     * @param fileName 文件名
     * @return boolean
     */
    public static boolean isVideo(String fileName) {
        String extension = getFileExtension(fileName);
        String[] videoExtension = new String[]{"mp4", "rmvb", "ogg", "flv", "avi", "wmv", "mov"};
        for (String e : videoExtension) {
            if (extension.toLowerCase().equals(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断文件是否音频类型
     *
     * @param fileName 文件名
     * @return boolean
     */
    public static boolean isAudio(String fileName) {
        String extension = getFileExtension(fileName);
        String[] videoExtension = new String[]{"mpeg","mp3", "ape", "wav", "flac", "m4a", "kgm", "ncm", "mgg"};
        for (String e : videoExtension) {
            if (extension.toLowerCase().equals(e)) {
                return true;
            }
        }
        return false;
    }
}
