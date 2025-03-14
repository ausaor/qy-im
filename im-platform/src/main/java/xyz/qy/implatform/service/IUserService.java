package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.LoginDTO;
import xyz.qy.implatform.dto.LogoutDTO;
import xyz.qy.implatform.dto.ModifyPwdDTO;
import xyz.qy.implatform.dto.RegisterDTO;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.vo.LoginVO;
import xyz.qy.implatform.vo.OnlineTerminalVO;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.PasswordVO;
import xyz.qy.implatform.vo.UserVO;

import java.util.List;


public interface IUserService extends IService<User> {

    LoginVO login(LoginDTO dto);

    LoginVO refreshToken(String refreshToken);

    void register(RegisterDTO registerDTO);

    User findUserByUserName(String username);

    void update(UserVO vo);

    UserVO findUserById(Long id);

    PageResultVO pageFindUserByNickName(String nickname);

    List<Long> checkOnline(String userIds);

    /**
     * 生成随机用户名
     *
     * @return 用户名
     */
    String generateRandomUsername();

    /**
     * 修改密码
     *
     * @param passwordVO 密码信息
     */
    void modifyPassword(PasswordVO passwordVO);

    void modifyPassword(ModifyPwdDTO dto);

    List<UserVO> findUserByName(String name);

    List<OnlineTerminalVO> getOnlineTerminals(String userIds);

    List<Long> getAllUserIds();
}
