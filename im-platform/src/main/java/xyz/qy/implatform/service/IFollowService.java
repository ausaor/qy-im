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
     * 查询当前用户的所有关注
     *
     * @return 关注列表
     */
    Set<String> findAllFollows();

    /**
     * 查询当前用户某个类别的所有关注id
     *
      * @param type 类别
      * @return 关注id集合
     */
    Set<Long> findFollowsTargetIds(String type);

    /**
     * 判断当前用户是否关注了某个目标对象
     *
     * @param userId 当前用户id
     * @param targetId 目标对象id
     * @param type 类别
     * @return 是否关注
     */
    Boolean isFollow(Long userId, Long targetId, String type);
}
