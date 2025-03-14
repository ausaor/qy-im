package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.TalkStarDTO;
import xyz.qy.implatform.entity.TalkStar;
import xyz.qy.implatform.vo.TalkStarVO;

/**
 * @description: 动态赞星
 * @author: Polaris
 * @create: 2023-12-24 15:37
 **/
public interface ITalkStarService extends IService<TalkStar> {
    /**
     * 点赞动态
     *
     * @param talkStarDTO 参数
     */
    TalkStarVO like(TalkStarDTO talkStarDTO);

    /**
     * 取消点赞
     *
     * @param talkStarDTO 参数
     */
    void cancelLike(TalkStarDTO talkStarDTO);
}
