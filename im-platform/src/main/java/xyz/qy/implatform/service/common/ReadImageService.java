package xyz.qy.implatform.service.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.entity.CharacterAvatar;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.service.ICharacterAvatarService;
import xyz.qy.implatform.service.ITemplateCharacterService;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 从磁盘读取图片
 *
 * @author Polaris
 */
@Slf4j
@Component
public class ReadImageService {
    @Resource
    private ITemplateCharacterService templateCharacterService;

    @Resource
    private ICharacterAvatarService characterAvatarService;

    /**
     * 读取模板角色图片
     *
     * @param avatarPath 存放头像目录 例：E:/image/wzry/avatar/，E:/image/lolm/avatar/
     * @param skinAvatarPath 存放皮肤头像目录 例：E:/image/wzry/skinAvatar/，E:/image/lolm/skinAvatar/
     * @param urlPre 链接前缀 例：http://localhost:8300/wzry/,http://localhost:8300/lolm/
     * @param templateGroupId 群聊模板id
     */
    public void readTemplateCharacterImageFromDisk(String avatarPath, String skinAvatarPath, String urlPre, Long templateGroupId) {
        readTemplateCharacterImage(avatarPath, skinAvatarPath, urlPre, templateGroupId);
    }

    private void readTemplateCharacterImage(String avatarPath, String skinAvatarPath, String urlPre, Long templateGroupId) {
        File file = new File(avatarPath);

        if (file.isDirectory()) { //是目录
            String[] list = file.list();
            for (String str : list) {
                readTemplateCharacterImage(avatarPath + "/" + str, skinAvatarPath, urlPre, templateGroupId);
            }
        } else {
            String fileName = file.getName();
            log.info("fileName:::>{}", fileName);
            String[] arr = fileName.split("\\.");

            String name = arr[0];
            TemplateCharacter templateCharacter = new TemplateCharacter();
            templateCharacter.setTemplateGroupId(templateGroupId);
            // 角色名称
            templateCharacter.setName(name);

            // 角色头像链接
            templateCharacter.setAvatar(urlPre + "avatar/" + fileName);
            templateCharacter.setStatus(ReviewEnum.REVIEWED.getCode());
            templateCharacter.setCreateBy(String.valueOf(Constant.ADMIN_USER_ID));
            templateCharacter.setUpdateBy(String.valueOf(Constant.ADMIN_USER_ID));
            templateCharacterService.save(templateCharacter);
            List<CharacterAvatar> characterAvatars = new ArrayList<>();

            this.readCharacterAvatarFile(skinAvatarPath + templateCharacter.getName(),
                    urlPre,
                    templateCharacter,
                    characterAvatars);

            if (CollectionUtils.isNotEmpty(characterAvatars)) {
                characterAvatarService.saveBatch(characterAvatars, 100);
            }
        }
    }

    private void readCharacterAvatarFile(String filePath,
                                         String urlPre,
                                         TemplateCharacter templateCharacter,
                                         List<CharacterAvatar> characterAvatars) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) { //是目录
            String[] list = file.list();
            for (String str : list) {
                readCharacterAvatarFile(filePath  + "/" + str, urlPre, templateCharacter, characterAvatars);
            }
        } else {
            String fileName = file.getName();
            File parentFile = file.getParentFile();
            String parentName = parentFile.getName();
            log.info("parentName:::>{}", parentName);
            log.info("fileName:::>{}", fileName);
            String[] arr = fileName.split("\\.");
            String characterAvatarName = arr[0];

            CharacterAvatar characterAvatar = new CharacterAvatar();
            characterAvatar.setTemplateCharacterId(templateCharacter.getId());
            characterAvatar.setTemplateCharacterName(templateCharacter.getName());
            // 头像名称
            characterAvatar.setName(characterAvatarName);
            // 头像链接
            characterAvatar.setAvatar(urlPre + "skinAvatar/" + parentName + "/" + fileName);
            characterAvatar.setLevel(1);
            characterAvatar.setStatus(ReviewEnum.REVIEWED.getCode());
            characterAvatar.setCreateBy(String.valueOf(Constant.ADMIN_USER_ID));
            characterAvatar.setUpdateBy(String.valueOf(Constant.ADMIN_USER_ID));

            characterAvatars.add(characterAvatar);
        }
    }
}
