package xyz.qy.implatform.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.qy.imclient.IMClient;
import xyz.qy.imcommon.enums.IMTerminalType;
import xyz.qy.implatform.config.JwtProperties;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.LoginDTO;
import xyz.qy.implatform.dto.ModifyPwdDTO;
import xyz.qy.implatform.dto.RegisterDTO;
import xyz.qy.implatform.entity.Friend;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.FilePathEnum;
import xyz.qy.implatform.enums.LoginTypeEnum;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.UserMapper;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.strategy.context.UploadStrategyContext;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.FileUtils;
import xyz.qy.implatform.util.ImageUtil;
import xyz.qy.implatform.util.IpUtils;
import xyz.qy.implatform.util.JwtUtil;
import xyz.qy.implatform.util.LocationServicesUtil;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.util.SensitiveUtil;
import xyz.qy.implatform.util.SysStringUtils;
import xyz.qy.implatform.vo.IpGeoInfoVO;
import xyz.qy.implatform.vo.LoginVO;
import xyz.qy.implatform.vo.OnlineTerminalVO;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.PasswordVO;
import xyz.qy.implatform.vo.UploadImageVO;
import xyz.qy.implatform.vo.UserVO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IGroupMemberService groupMemberService;

    @Autowired
    private IFriendService friendService;

    @Autowired
    private UploadStrategyContext uploadStrategyContext;

    @Autowired
    private RedisCache redisCache;

    @Resource
    private HttpServletRequest request;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private IMClient imClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Resource
    private LocationServicesUtil locationServicesUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 验证码校验
        validateCaptcha(dto.getCode(), dto.getUuid());
        User user = findUserByUserName(dto.getUserName());
        if (null == user) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "用户不存在");
        }
        if (user.getIsBanned()) {
            throw new GlobalException("您的账号已被管理员封禁!");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new GlobalException(ResultCode.PASSWOR_ERROR);
        }
        recordLoginInfo(user);
        redisCache.deleteObject(RedisKey.CAPTCHA_CODE_KEY + dto.getUuid());
        // 生成token
        return jwtUtil.createToken(user, dto.getTerminal());
    }

    private void recordLoginInfo(User user) {
        String ipAddress = IpUtils.getIpAddress(request);
        String ipSource = IpUtils.getIpSource(ipAddress);
        user.setIpAddress(ipAddress);
        user.setIpSource(ipSource);
        user.setLoginType(LoginTypeEnum.USERNAME.getType());
        IpGeoInfoVO ipGeoInfo = locationServicesUtil.getIpGeoInfoByTencentApi(ipAddress);
        if (ObjectUtil.isNotNull(ipGeoInfo)) {
            user.setProvince(StringUtils.isBlank(ipGeoInfo.getPro()) ? ipSource : ipGeoInfo.getPro());
            user.setCity(ipGeoInfo.getCity());
        }
        user.setLastLoginTime(LocalDateTime.now());
        baseMapper.updateById(user);
    }

    /**
     * 用refreshToken换取新 token
     *
     * @param refreshToken
     * @return 用户信息
     */
    @Override
    public LoginVO refreshToken(String refreshToken) {
        //验证 token
        if (!JwtUtil.checkSign(refreshToken, jwtProperties.getRefreshTokenSecret())) {
            throw new GlobalException("您的登陆信息已过期，请重新登陆");
        }
        String strJson = JwtUtil.getInfo(refreshToken);
        Long userId = JwtUtil.getUserId(refreshToken);
        User user = this.getById(userId);
        if (Objects.isNull(user)) {
            throw new GlobalException("用户不存在");
        }
        if (user.getIsBanned()) {
            throw new GlobalException("您的账号已被管理员封禁");
        }
        String accessToken = JwtUtil.sign(userId, strJson, jwtProperties.getAccessTokenExpireIn(), jwtProperties.getAccessTokenSecret());
        String newRefreshToken = JwtUtil.sign(userId, strJson, jwtProperties.getRefreshTokenExpireIn(), jwtProperties.getRefreshTokenSecret());
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(newRefreshToken);
        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        return vo;
    }

    /**
     * 用户注册
     *
     * @param vo 注册vo
     */
    @Override
    public void register(RegisterDTO vo) {
        // 验证码校验
        validateCaptcha(vo.getCode(), vo.getUuid());
        validateSpecialChar(vo);
        if (!Validator.isGeneral(vo.getUserName())) {
            throw new GlobalException("用户名只能包含数字，字母，下划线");
        }
        User user = findUserByUserName(vo.getUserName());
        if (null != user) {
            throw new GlobalException(ResultCode.USERNAME_ALREADY_REGISTER);
        }
        user = BeanUtils.copyProperties(vo, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        try (InputStream inputStream = ImageUtil.getRandomAvatar()) {
//            if (inputStream != null) {
//                MultipartFile multipartFile = FileUtils.inputStreamToMultipartFile(System.currentTimeMillis() + ".png", inputStream);
//                UploadImageVO uploadImageVO = uploadStrategyContext.executeUploadImageStrategy(multipartFile, FilePathEnum.AVATAR.getPath());
//                user.setHeadImageThumb(uploadImageVO.getOriginUrl());
//                user.setHeadImage(uploadImageVO.getOriginUrl());
//            }
//        } catch (Exception e) {
//            log.error("get avatar error:{}", e.getMessage());
//        }

        String ipAddress = IpUtils.getIpAddress(request);
        String ipSource = IpUtils.getIpSource(ipAddress);
        user.setIpAddress(ipAddress);
        user.setIpSource(ipSource);
        IpGeoInfoVO ipGeoInfo = locationServicesUtil.getIpGeoInfoByTencentApi(ipAddress);
        if (ObjectUtil.isNotNull(ipGeoInfo)) {
            user.setProvince(StringUtils.isBlank(ipGeoInfo.getPro()) ? ipSource : ipGeoInfo.getPro());
            user.setCity(ipGeoInfo.getCity());
        }
        user.setNickName(SensitiveUtil.filter(user.getNickName()));
        this.save(user);
        redisCache.deleteObject(RedisKey.CAPTCHA_CODE_KEY + vo.getUuid());

        groupMemberService.newRegisteredUserHandle(user);
        log.info("注册用户，用户id:{},用户名:{},昵称:{}", user.getId(), vo.getUserName(), vo.getNickName());
    }

    private void validateCaptcha(String code, String uuid) {
        String verifyKey = xyz.qy.implatform.contant.RedisKey.CAPTCHA_CODE_KEY + uuid;
        String captcha = redisCache.getCacheObject(verifyKey);
        if (StringUtils.isBlank(captcha)) {
            throw new GlobalException(ResultCode.VERITY_CODE_NOT_EXIST, "验证码已过期");
        }

        if (!code.equals(captcha)) {
            throw new GlobalException(ResultCode.VERITY_CODE_ERROR, "验证码错误");
        }
    }

    private void validateSpecialChar(RegisterDTO vo) {
        if (SysStringUtils.checkSpecialChar(vo.getUserName())) {
            throw new GlobalException("用户名不能包含特殊字符");
        }
        if (SysStringUtils.checkSpecialChar(vo.getNickName())) {
            throw new GlobalException("昵称不能包含特殊字符");
        }
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public User findUserByUserName(String username) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getUserName, username);
        return this.getOne(queryWrapper);
    }

    /**
     * 更新用户信息，好友昵称和群聊昵称等冗余信息也会更新
     *
     * @param vo 用户信息vo
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(UserVO vo) {
        UserSession session = SessionContext.getSession();
        if (!session.getUserId().equals(vo.getId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "不允许修改其他用户的信息!");
        }
        User user = this.getById(vo.getId());
        if (Objects.isNull(user)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "用户不存在");
        }
        // 更新好友昵称和头像
        if (!user.getNickName().equals(vo.getNickName()) || !user.getHeadImage().equals(vo.getHeadImage())) {
            QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Friend::getFriendId, session.getUserId());
            List<Friend> friends = friendService.list(queryWrapper);
            for (Friend friend : friends) {
                friend.setFriendNickName(vo.getNickName());
                friend.setFriendHeadImage(vo.getHeadImage());
            }
            friendService.updateBatchById(friends);
        }
        // 更新群聊中的头像
//        if (!user.getHeadImage().equals(vo.getHeadImage())) {
//            List<GroupMember> members = groupMemberService.findByUserId(session.getUserId());
//            for (GroupMember member : members) {
//                // 模板群聊不能修改用户聊天头像
//                if (member.getIsTemplate()) {
//                    continue;
//                }
//                member.setHeadImage(vo.getHeadImage());
//            }
//            groupMemberService.updateBatchById(members);
//        }
        // 更新用户信息
        user.setNickName(SensitiveUtil.filter(vo.getNickName()));
        user.setSex(vo.getSex());
        user.setSignature(SensitiveUtil.filter(vo.getSignature()));
        user.setHeadImage(vo.getHeadImage());
        user.setHeadImageThumb(vo.getHeadImageThumb());
        this.updateById(user);
        log.info("用户信息更新，用户:{}", user);
    }

    /**
     * 根据用户昵id查询用户以及在线状态
     *
     * @param id 用户id
     * @return 用户信息
     */
    @Override
    public UserVO findUserById(Long id) {
        User user = this.getById(id);
        UserVO vo = BeanUtils.copyProperties(user, UserVO.class);
        Friend friend = findFriend(id);
        if (friend != null) {
            vo.setFriendRemark(friend.getFriendRemark());
        }
        vo.setOnline(imClient.isOnline(id));
        return vo;
    }

    private Friend findFriend(Long friendId) {
        UserSession session = SessionContext.getSession();
        if (session.getUserId().equals(friendId)) {
            return null;
        }
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(Friend::getUserId, session.getUserId())
                .eq(Friend::getFriendId, friendId);
        return friendService.getOne(wrapper);
    }

    /**
     * 根据用户昵称查询用户，最多返回20条数据
     *
     * @param nickname 用户昵称
     * @return PageResultVO
     */
    @Override
    public PageResultVO pageFindUserByNickName(String nickname) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper
                .ne(User::getId, userId)
                .and(StringUtils.isNotBlank(nickname),
                        qw -> qw.like(User::getNickName, nickname)
                                .or()
                                .like(User::getUserName, nickname))
                .orderByDesc(User::getCreateTime);
        Page<User> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), queryWrapper);
        List<User> users = page.getRecords();
        if (CollectionUtils.isEmpty(users)) {
            return PageResultVO.builder().data(Collections.EMPTY_LIST).build();
        }
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        List<Long> onlineUserIds = imClient.getOnlineUser(userIds);
        List<UserVO> vos = users.stream().map(u -> {
            UserVO vo = BeanUtils.copyProperties(u, UserVO.class);
            vo.setOnline(onlineUserIds.contains(u.getId()));
            return vo;
        }).collect(Collectors.toList());
        return PageResultVO.builder().data(vos).total(page.getTotal()).build();
    }

    /**
     * 判断用户是否在线，返回在线的用户id列表
     *
     * @param userIds 用户id，多个用‘,’分割
     * @return 在线用户id列表
     */
    @Override
    public List<Long> checkOnline(String userIds) {
        List<Long> userIdList = Arrays.stream(userIds.split(","))
                .map(Long::parseLong).collect(Collectors.toList());
        return imClient.getOnlineUser(userIdList);
    }

    @Override
    public String generateRandomUsername() {
        boolean flag = true;
        String username = null;
        int count = 0;
        while (flag) {
            username = RandomUtil.randomString(6);
            count++;
            List<User> users = baseMapper.selectList(new LambdaQueryWrapper<User>()
                    .eq(User::getUserName, username));
            if (CollectionUtils.isEmpty(users)) {
                break;
            }
            if (count > 20) {
                username = UUID.randomUUID().toString(true);
                flag = false;
            }
        }
        return username;
    }

    @Override
    public void modifyPassword(PasswordVO passwordVO) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        User user = baseMapper.selectById(userId);
        if (!passwordEncoder.matches(passwordVO.getOldPassword(), user.getPassword())) {
            throw new GlobalException("旧" + ResultCode.PASSWOR_ERROR);
        }
        user.setPassword(passwordEncoder.encode(passwordVO.getNewPassWord()));
        baseMapper.updateById(user);
    }

    @Override
    public void modifyPassword(ModifyPwdDTO dto) {
        UserSession session = SessionContext.getSession();
        User user = this.getById(session.getUserId());
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new GlobalException("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(user);
        log.info("用户修改密码，用户id:{},用户名:{},昵称:{}", user.getId(), user.getUserName(), user.getNickName());
    }

    /**
     * 根据用户昵称查询用户，最多返回20条数据
     *
     * @param name 用户名或昵称
     * @return 用户列表
     */
    @Override
    public List<UserVO> findUserByName(String name) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(User::getUserName, name)
                .or()
                .like(User::getNickName, name)
                .last("limit 20");
        List<User> users = this.list(queryWrapper);
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        List<Long> onlineUserIds = imClient.getOnlineUser(userIds);
        return users.stream().map(u -> {
            UserVO vo = BeanUtils.copyProperties(u, UserVO.class);
            vo.setOnline(onlineUserIds.contains(u.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取用户在线的终端类型
     *
     * @param userIds 用户id，多个用‘,’分割
     * @return 在线用户终端
     */
    @Override
    public List<OnlineTerminalVO> getOnlineTerminals(String userIds) {
        List<Long> userIdList = Arrays.stream(userIds.split(","))
                .map(Long::parseLong).collect(Collectors.toList());
        // 查询在线的终端
        Map<Long, List<IMTerminalType>> terminalMap = imClient.getOnlineTerminal(userIdList);
        // 组装vo
        List<OnlineTerminalVO> vos = new LinkedList<>();
        terminalMap.forEach((userId, types) -> {
            List<Integer> terminals = types.stream().map(IMTerminalType::code).collect(Collectors.toList());
            vos.add(new OnlineTerminalVO(userId, terminals));
        });
        return vos;
    }

    @Override
    public List<Long> getAllUserIds() {
        return lambdaQuery().select(User::getId).list().stream().map(User::getId).collect(Collectors.toList());
    }
}
