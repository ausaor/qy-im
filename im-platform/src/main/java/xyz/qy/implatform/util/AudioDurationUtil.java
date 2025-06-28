package xyz.qy.implatform.util;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.*;
import org.jaudiotagger.tag.TagException;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class AudioDurationUtil {

    public static int getAudioDuration(MultipartFile multipartFile)
            throws IOException, CannotReadException {

        // 1. 确保有原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new CannotReadException("文件名无效");
        }

        // 2. 提取扩展名（带点号，如 ".mp3"）
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        // 3. 生成带随机UUID和正确扩展名的临时文件
        String tempFileName = "audio-" + UUID.randomUUID() + extension;
        Path tempPath = Path.of(System.getProperty("java.io.tmpdir"), tempFileName);

        try {
            // 4. 复制文件内容
            Files.copy(
                    multipartFile.getInputStream(),
                    tempPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // 5. 读取音频时长
            AudioFile audioFile = AudioFileIO.read(tempPath.toFile());
            return audioFile.getAudioHeader().getTrackLength();

        } catch (TagException | IOException | ReadOnlyFileException | InvalidAudioFrameException e) {
            throw new CannotReadException("解析失败: " + e.getMessage());
        } finally {
            // 6. 确保删除临时文件
            Files.deleteIfExists(tempPath);
        }
    }
}