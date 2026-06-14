package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.FollowDTO;
import xyz.qy.implatform.entity.Follow;
import xyz.qy.implatform.vo.FollowVO;

import java.util.List;
import java.util.Set;

public interface IFollowService extends IService<Follow> {

    /**
     * 添加关注
     *
     * @param dto 关注参数
     */
    void addFollow(FollowDTO dto);

    /**
     * 取消关注（物理删除）
     *
     * @param targetId 目标id
     * @param type     类别
     */
    void cancelFollow(Long targetId, String type);

    /**
     * 查询当前用户的所有关注
     *
     * @param type 类别
     * @return 关注列表
     */
    List<FollowVO> findFollows(String type);

    /**
     * 查询当前用户某个类别的所有关注id
     *
      * @param type 类别
      * @return 关注id集合
     */
    Set<Long> findFollowsTargetIds(String type);
}
