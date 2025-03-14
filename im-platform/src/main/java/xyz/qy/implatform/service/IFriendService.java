package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.Friend;
import xyz.qy.implatform.vo.FriendVO;

import java.util.List;


public interface IFriendService extends IService<Friend> {

    Boolean isFriend(Long userId1, Long userId2);

    List<Friend> findFriendByUserId(Long UserId);

    List<Long> getFriendIdsByUserId(Long userId);

    void addFriend(Long friendId);

    void addFriend(Long userId, Long friendId);

    void delFriend(Long friendId);

    void update(FriendVO vo);

    void updateMyInfoToFriend(FriendVO vo);

    FriendVO findFriend(Long friendId);
}
