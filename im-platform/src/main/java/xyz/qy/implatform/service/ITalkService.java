package xyz.qy.implatform.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.TalkAddDTO;
import xyz.qy.implatform.dto.TalkDelDTO;
import xyz.qy.implatform.dto.TalkQueryDTO;
import xyz.qy.implatform.dto.TalkUpdateDTO;
import xyz.qy.implatform.dto.UserDataAuthDTO;
import xyz.qy.implatform.entity.Talk;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.TalkVO;

/**
 * @description: 动态
 * @author: Polaris
 * @create: 2023-11-19 21:38
 **/
public interface ITalkService extends IService<Talk> {

    void addTalk(TalkAddDTO talkAddDTO);

    void updateTalk(TalkUpdateDTO talkUpdateDTO);

    PageResultVO pageQueryTalkList(TalkQueryDTO queryDTO);

    void delTalk(TalkDelDTO talkDelDTO);

    TalkVO getTalkDetail(Long talkId);

    /**
     * 验证用户评论角色是否异常
     *
     * @param talkId
     * @param characterId
     * @return
     */
    boolean verifyTalkCommentCharacter(Long talkId, Long characterId, Long avatarId);

    /**
     * 拉取离线未读动态数量
     *
     * @param minId 最小id
     */
    JSONObject pullOfflineTalks(Long minId);

    /**
     * 校验用户是否具有数据操作权限
     *
     * @param dto 数据权限
     * @return true:有权限，false:无权限
     */
    boolean verifyUserDataAuth(UserDataAuthDTO dto);
}
