package xyz.qy.implatform.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.core.lang.Validator;
import xyz.qy.implatform.entity.CharacterWord;
import xyz.qy.implatform.exception.GlobalException;

/**
 * 消息类型工具类
 *
 * @author Polaris
 * @since 2025-11-22
 */
public class MsgTypeUtil {

    public static boolean checkMediaMsgContent(Integer msgType, String content) {
        // Check if content is blank
        if (StrUtil.isBlank(content)) {
            return false;
        }

        // Parse JSON content
        JSONObject jsonObject;
        try {
            jsonObject = JSONUtil.parseObj(content);
        } catch (Exception e) {
            return false;
        }

        // Check required properties based on message type codes
        // 1: IMAGE, 2: FILE, 3: AUDIO, 4: VIDEO
        switch (msgType) {
            case 1:
                // For image type, check if originUrl exists and is a valid URL
                String originUrl = jsonObject.getStr("originUrl");
                return StrUtil.isNotBlank(originUrl) && Validator.isUrl(originUrl);
                
            case 2:
                // For file type, check if url exists and is a valid URL
                String fileUrl = jsonObject.getStr("url");
                return StrUtil.isNotBlank(fileUrl) && Validator.isUrl(fileUrl);
                
            case 3:
                // For audio type, check if url exists and is a valid URL
                String audioUrl = jsonObject.getStr("url");
                return StrUtil.isNotBlank(audioUrl) && Validator.isUrl(audioUrl);
                
            case 4:
                // For video type, check if videoUrl exists and is a valid URL
                String videoUrl = jsonObject.getStr("videoUrl");
                return StrUtil.isNotBlank(videoUrl) && Validator.isUrl(videoUrl);
                
            default:
                // For other message types, return false
                return false;
        }
    }

    public static Long getWordIdFromContent(String content) {
        if (StrUtil.isBlank(content)) {
            throw new GlobalException("消息内容不能为空");
        }

        // Parse JSON content
        JSONObject jsonObject;
        try {
            jsonObject = JSONUtil.parseObj(content);
        } catch (Exception e) {
            throw new GlobalException("消息内容格式错误");
        }

        Long wordId = jsonObject.getLong("id");
        if (ObjectUtil.isNull(wordId)) {
            throw new GlobalException("消息内容格式错误");
        }

        return wordId;
    }

    public static String formatContent(CharacterWord characterWord) {
        // 抽取id, templateGroupId, characterId, characterName, word, voice, status属性，拼接成json字符串
        JSONObject jsonObject = JSONUtil.createObj()
                .set("id", characterWord.getId())
                .set("templateGroupId", characterWord.getTemplateGroupId())
                .set("characterId", characterWord.getCharacterId())
                .set("characterName", characterWord.getCharacterName())
                .set("word", characterWord.getWord())
                .set("voice", characterWord.getVoice())
                .set("status", characterWord.getStatus());
        return jsonObject.toString();
    }
}