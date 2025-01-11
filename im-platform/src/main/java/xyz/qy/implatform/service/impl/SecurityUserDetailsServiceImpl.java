//package xyz.qy.implatform.service.impl;
//
//
//import cn.hutool.core.util.ObjectUtil;
//import xyz.qy.implatform.entity.User;
//import xyz.qy.implatform.enums.LoginTypeEnum;
//import xyz.qy.implatform.service.IUserService;
//import xyz.qy.implatform.session.UserSession;
//import xyz.qy.implatform.util.IpUtils;
//import xyz.qy.implatform.vo.IpGeoInfoVO;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class SecurityUserDetailsServiceImpl implements UserDetailsService {
//
//    @Autowired
//    private IUserService userService;
//
//    @Resource
//    private HttpServletRequest request;
//
//    /**
//     * 加载用户数据，用户登录时，由spring security调用
//     *
//     * @param username 用户名
//     * @return
//     * @throws UsernameNotFoundException 用户不存在时抛出
//     */
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user = userService.findUserByName(username);
//        if (user == null) {
//            throw new UsernameNotFoundException("用户不存在");
//        }
//        //定义权限列表.
//        List<GrantedAuthority> authorities = new ArrayList();
//        // 用户可以访问的资源名称（或者说用户所拥有的权限） 注意：必须"ROLE_"开头
//        authorities.add(new SimpleGrantedAuthority("ROLE_XX"));
//
//        String ipAddress = IpUtils.getIpAddress(request);
//        String ipSource = IpUtils.getIpSource(ipAddress);
//        user.setIpAddress(ipAddress);
//        user.setIpSource(ipSource);
//        user.setLoginType(LoginTypeEnum.USERNAME.getType());
//        IpGeoInfoVO ipGeoInfo = IpUtils.getIpGeoInfo(ipAddress);
//        if (ObjectUtil.isNotNull(ipGeoInfo)) {
//            user.setProvince(ipGeoInfo.getPro());
//            user.setCity(ipGeoInfo.getCity());
//        }
//        user.setLastLoginTime(LocalDateTime.now());
//        userService.updateById(user);
//        return new UserSession(user.getUserName(),
//                user.getPassword(),
//                authorities,
//                user.getId(),
//                user.getNickName(),
//                user.getIsDisable());
//    }
//}
