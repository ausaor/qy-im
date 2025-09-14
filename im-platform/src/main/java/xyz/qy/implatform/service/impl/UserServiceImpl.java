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
import xyz.qy.imclient.IMClient;
import xyz.qy.imcommon.enums.IMTerminalType;
import xyz.qy.imcommon.model.IMSystemMessage;
import xyz.qy.implatform.config.JwtProperties;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.EmailBindDTO;
import xyz.qy.implatform.dto.EmailDTO;
import xyz.qy.implatform.dto.LoginDTO;
import xyz.qy.implatform.dto.ModifyPwdDTO;
import xyz.qy.implatform.dto.RegisterDTO;
import xyz.qy.implatform.dto.ResetPwdDTO;
import xyz.qy.implatform.dto.UserBanDTO;
import xyz.qy.implatform.dto.UserQueryDTO;
import xyz.qy.implatform.entity.Friend;
import xyz.qy.implatform.entity.FriendRequest;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.EmailCategoryEnum;
import xyz.qy.implatform.enums.FriendRequestStatusEnum;
import xyz.qy.implatform.enums.LoginTypeEnum;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.UserMapper;
import xyz.qy.implatform.service.IFriendRequestService;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.strategy.context.UploadStrategyContext;
import xyz.qy.implatform.util.BeanUtils;
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
import xyz.qy.implatform.vo.SystemMessageVO;
import xyz.qy.implatform.vo.UserVO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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

    @Resource
    private EmailService emailService;

    @Resource
    private IFriendRequestService friendRequestService;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = null;
        if (LoginTypeEnum.USERNAME.getType().equals(dto.getLoginType())) {
            if (StringUtils.isAnyBlank(dto.getUserName(), dto.getPassword(), dto.getUuid(), dto.getCode())) {
                throw new GlobalException("参数异常");
            }
            // 验证码校验
            validateCaptcha(dto.getCode(), dto.getUuid());
            user = findUserByUserName(dto.getUserName());
            if (null == user) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "用户不存在");
            }
            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                throw new GlobalException(ResultCode.PASSWORD_ERROR);
            }
        } else if (LoginTypeEnum.EMAIL.getType().equals(dto.getLoginType())) {
            if (StringUtils.isAnyBlank(dto.getEmail(), dto.getEmailCode())) {
                throw new GlobalException("参数异常");
            }
            // 校验邮箱验证码
            validateEmailCode(dto.getEmail(), EmailCategoryEnum.LOGIN.name(), dto.getEmailCode());
            user = findUserByEmail(dto.getEmail());
            if (null == user) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "用户不存在");
            }
        }

        assert user != null;
        if (user.getIsDisable()) {
            throw new GlobalException("您的账号已被管理员封禁!");
        }

        user.setLoginType(dto.getLoginType());
        recordLoginInfo(user);
        redisCache.deleteObject(RedisKey.CAPTCHA_CODE_KEY + dto.getUuid());
        // 生成token
        return jwtUtil.createToken(user, dto.getTerminal());
    }

    private void recordLoginInfo(User user) {
        String ipAddress = IpUtils.getIp(request);
        String ipSource = IpUtils.getIp2region(ipAddress);
        user.setIpAddress(ipAddress);
        user.setIpSource(ipSource);
        IpGeoInfoVO ipGeoInfo = locationServicesUtil.getIpGeoInfoByIp2Region(ipAddress);
        if (ObjectUtil.isNotNull(ipGeoInfo)) {
            user.setProvince(ipGeoInfo.getPro());
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
        validateEmailCode(vo.getEmail(), EmailCategoryEnum.REGISTER.name(), vo.getEmailCode());
        validateSpecialChar(vo);
        if (!Validator.isGeneral(vo.getUserName())) {
            throw new GlobalException("用户名只能包含数字，字母，下划线");
        }
        User user = findUserByUserName(vo.getUserName());
        if (null != user) {
            throw new GlobalException(ResultCode.USERNAME_ALREADY_REGISTER);
        }
        User user1 = findUserByEmail(vo.getEmail());
        if (null != user1) {
            throw new GlobalException(ResultCode.EMAIL_ALREADY_REGISTER);
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

        String ipAddress = IpUtils.getIp(request);
        String ipSource = IpUtils.getIp2region(ipAddress);
        user.setIpAddress(ipAddress);
        user.setIpSource(ipSource);
        IpGeoInfoVO ipGeoInfo = locationServicesUtil.getIpGeoInfoByIp2Region(ipAddress);
        if (ObjectUtil.isNotNull(ipGeoInfo)) {
            user.setProvince(ipGeoInfo.getPro());
            user.setCity(ipGeoInfo.getCity());
        }
        user.setNickName(SensitiveUtil.filter(user.getNickName()));
        this.save(user);
        redisCache.deleteObject(RedisKey.CAPTCHA_CODE_KEY + vo.getUuid());

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

    private void validateEmailCode(String email, String category, String emailCode) {
        String emailCodeCache = redisCache.getCacheObject(RedisKey.IM_CACHE_MAIL_CODE + category + ":" + email);
        if (StringUtils.isBlank(emailCodeCache)) {
            throw new GlobalException(ResultCode.VERITY_CODE_NOT_EXIST, "邮箱验证码已过期");
        }
        if (!emailCode.equals(emailCodeCache)) {
            throw new GlobalException(ResultCode.VERITY_CODE_ERROR, "邮箱验证码错误");
        }
        redisCache.deleteObject(RedisKey.IM_CACHE_MAIL_CODE + category + ":" + email);
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

    @Override
    public User findUserByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getEmail, email);
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
        if (!user.getHeadImage().equals(vo.getHeadImage())) {
            List<GroupMember> members = groupMemberService.findByUserId(session.getUserId());
            for (GroupMember member : members) {
                // 模板角色不修改聊天头像
                if (member.getIsTemplate()) {
                    continue;
                }
                member.setHeadImage(vo.getHeadImage());
            }
            groupMemberService.updateBatchById(members);
        }
        // 更新用户信息
        user.setNickName(SensitiveUtil.filter(vo.getNickName()));
        user.setSex(vo.getSex());
        user.setSignature(SensitiveUtil.filter(vo.getSignature()));
        user.setHeadImage(vo.getHeadImage());
        user.setHeadImageThumb(vo.getHeadImageThumb());
        user.setAutoPlay(vo.getAutoPlay());
        user.setSoundPlay(vo.getSoundPlay());
        user.setFriendReview(vo.getFriendReview());
        user.setGroupReview(vo.getGroupReview());
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
            vo.setHeadImage(friend.getFriendHeadImage());
            vo.setFriendRemark(friend.getFriendRemark());
        }
        Friend myInfoToFriend = findMyInfoToFriend(id);
        if (myInfoToFriend != null) {
            vo.setMyHeadImageToFriend(myInfoToFriend.getFriendHeadImage());
        }
        vo.setOnline(imClient.isOnline(id));
        return vo;
    }

    @Override
    public List<User> findUserByIds(List<Long> ids) {
        return this.listByIds(ids);
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

    private Friend findMyInfoToFriend(Long friendId) {
        UserSession session = SessionContext.getSession();
        if (session.getUserId().equals(friendId)) {
            return null;
        }
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(Friend::getUserId, friendId)
                .eq(Friend::getFriendId, session.getUserId());
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

        // 查询当前用户发起的好友申请列表
        List<Long> recvIds = friendRequestService.lambdaQuery()
                .eq(FriendRequest::getSendId, userId)
                .eq(FriendRequest::getStatus, FriendRequestStatusEnum.APPLYING.getCode())
                .select(FriendRequest::getRecvId).list()
                .stream().map(FriendRequest::getRecvId).collect(Collectors.toList());


        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        List<Long> onlineUserIds = imClient.getOnlineUser(userIds);
        List<UserVO> vos = users.stream().map(u -> {
            UserVO vo = BeanUtils.copyProperties(u, UserVO.class);
            vo.setOnline(onlineUserIds.contains(u.getId()));
            vo.setIsManualApprove(recvIds.contains(u.getId()));
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
            throw new GlobalException("旧" + ResultCode.PASSWORD_ERROR);
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

    @Override
    public void resetPassword(ResetPwdDTO dto) {
        UserSession session = SessionContext.getSession();
        User user = this.getById(session.getUserId());
        validateEmailCode(user.getEmail(), EmailCategoryEnum.RESET_PASSWORD.name(), dto.getEmailCode());
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(user);
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

        Long userId = SessionContext.getSession().getUserId();
        // 查询当前用户发起的好友申请列表
        List<Long> recvIds = friendRequestService.lambdaQuery()
                .eq(FriendRequest::getSendId, userId)
                .eq(FriendRequest::getStatus, FriendRequestStatusEnum.APPLYING.getCode())
                .select(FriendRequest::getRecvId).list()
                .stream().map(FriendRequest::getRecvId).collect(Collectors.toList());
        return users.stream().map(u -> {
            UserVO vo = BeanUtils.copyProperties(u, UserVO.class);
            vo.setOnline(onlineUserIds.contains(u.getId()));
            vo.setIsManualApprove(recvIds.contains(u.getId()));
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

    @Override
    public PageResultVO page(UserQueryDTO dto) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(StringUtils.isNotBlank(dto.getUserName()), User::getUserName, dto.getUserName());
        queryWrapper.like(StringUtils.isNotBlank(dto.getNickName()), User::getNickName, dto.getNickName());
        queryWrapper.eq(dto.getSex() != null, User::getSex, dto.getSex());
        queryWrapper.orderByDesc(User::getId);

        Page<User> page = this.page(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), queryWrapper);
        List<User> users = page.getRecords();
        if (CollectionUtils.isEmpty(users)) {
            return PageResultVO.builder().data(Collections.EMPTY_LIST).build();
        }
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        List<Long> onlineUserIds = imClient.getOnlineUser(userIds);
        List<UserVO> vos = users.stream().map(u -> {
            UserVO vo = BeanUtils.copyProperties(u, UserVO.class);
            vo.setIsBanned(redisCache.hasKey(RedisKey.IM_USER_MSG_SWITCH + u.getId()));
            vo.setOnline(onlineUserIds.contains(u.getId()));
            return vo;
        }).collect(Collectors.toList());

        return PageResultVO.builder().data(vos).total(page.getTotal()).build();
    }

    @Override
    public void bandUser(UserBanDTO dto) {
        UserSession session = SessionContext.getSession();
        if (!session.getUserId().equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("只有系统管理员有权限操作");
        }
        if (dto.getUserId().equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("系统管理员不能被禁言");
        }
        User user = this.getById(dto.getUserId());
        if (ObjectUtil.isNull(user)) {
            throw new GlobalException("用户不存在");
        }
        if (redisCache.hasKey(RedisKey.IM_USER_MSG_SWITCH + dto.getUserId())) {
            throw new GlobalException("用户已禁言");
        }

        if (dto.getBanDuration() != null && dto.getBanDuration() > 0) {
            redisCache.setCacheObject(RedisKey.IM_USER_MSG_SWITCH + dto.getUserId(), dto.getUserId(), dto.getBanDuration(), TimeUnit.MINUTES);
        } else {
            redisCache.setCacheObject(RedisKey.IM_USER_MSG_SWITCH + dto.getUserId(), dto.getUserId());
        }
    }

    @Override
    public void unBandUser(UserBanDTO dto) {
        UserSession session = SessionContext.getSession();
        if (!session.getUserId().equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("只有系统管理员有权限操作");
        }
        redisCache.deleteObject(RedisKey.IM_USER_MSG_SWITCH + dto.getUserId());
    }

    @Override
    public void banAccount(Long userId) {
        UserSession session = SessionContext.getSession();
        if (!session.getUserId().equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("只有系统管理员有权限操作");
        }
        if (userId.equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("系统管理员不能被封禁");
        }
        User user = this.getById(userId);
        if (ObjectUtil.isNull(user)) {
            throw new GlobalException("用户不存在");
        }
        if (user.getIsDisable()) {
            throw new GlobalException("用户已封禁");
        }

        user.setIsBanned(true);
        user.setIsDisable(true);
        redisCache.setCacheObject(RedisKey.IM_USER_BAN_ACCOUNT + userId, userId);
        this.updateById(user);

        SystemMessageVO msgInfo = new SystemMessageVO();
        msgInfo.setType(MessageType.USER_BANNED.code());
        msgInfo.setContent("账号违规");

        IMSystemMessage<SystemMessageVO> sendMessage = new IMSystemMessage<>();
        sendMessage.setRecvIds(Collections.singletonList(userId));
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendSystemMessage(sendMessage);
    }

    @Override
    public void unBanAccount(Long userId) {
        UserSession session = SessionContext.getSession();
        if (!session.getUserId().equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("只有系统管理员有权限操作");
        }

        User user = this.getById(userId);
        if (ObjectUtil.isNull(user)) {
            throw new GlobalException("用户不存在");
        }

        user.setIsBanned(false);
        user.setIsDisable(false);
        this.updateById(user);
        redisCache.deleteObject(RedisKey.IM_USER_BAN_ACCOUNT + userId);
    }

    @Override
    public void bindEmail(EmailBindDTO dto) {
        // 查询邮箱是否存在
        User userByEmail = this.findUserByEmail(dto.getEmail());
        if (ObjectUtil.isNotNull(userByEmail)) {
            throw new GlobalException("要绑定的邮箱已存在");
        }

        validateEmailCode(dto.getEmail(), EmailCategoryEnum.BIND_EMAIL.name(), dto.getEmailCode());

        UserSession session = SessionContext.getSession();
        User user = this.getById(session.getUserId());
        user.setEmail(dto.getEmail());
        this.updateById(user);
    }

    @Override
    public void getEmailCode(String emailCategory) {
        if (Arrays.stream(EmailCategoryEnum.values()).noneMatch(item -> item.name().equals(emailCategory))) {
            throw new GlobalException("邮件分类错误");
        }

        UserSession session = SessionContext.getSession();
        User user = this.getById(session.getUserId());
        if (StringUtils.isBlank(user.getEmail())) {
            throw new GlobalException("请先绑定邮箱");
        }
        emailService.getEmailCode(new EmailDTO(user.getEmail(), emailCategory));
    }
}
