package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.ModifyPwdDTO;
import xyz.qy.implatform.dto.UserBanDTO;
import xyz.qy.implatform.dto.UserQueryDTO;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.OnlineTerminalVO;
import xyz.qy.implatform.vo.PasswordVO;
import xyz.qy.implatform.vo.UserVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "用户")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @GetMapping("/online")
    @ApiOperation(value = "判断用户是否在线", notes = "返回在线的用户id集合")
    public Result checkOnline(@NotEmpty @RequestParam("userIds") String userIds) {
        List<Long> onlineIds = userService.checkOnline(userIds);
        return ResultUtils.success(onlineIds);
    }

    @GetMapping("/terminal/online")
    @ApiOperation(value = "判断用户哪个终端在线",notes="返回在线的用户id的终端集合")
    public Result<List<OnlineTerminalVO>> getOnlineTerminal(@NotEmpty @RequestParam("userIds") String userIds){
        return ResultUtils.success(userService.getOnlineTerminals(userIds));
    }

    @GetMapping("/self")
    @ApiOperation(value = "获取当前用户信息", notes = "获取当前用户信息")
    public Result<UserVO> findSelfInfo() {
        UserSession session = SessionContext.getSession();
        User user = userService.getById(session.getUserId());
        UserVO userVO = BeanUtils.copyProperties(user, UserVO.class);
        return ResultUtils.success(userVO);
    }

    @GetMapping("/find/{id}")
    @ApiOperation(value = "查找用户", notes = "根据id查找用户")
    public Result<UserVO> findById(@NotEmpty @PathVariable("id") Long id){
        return ResultUtils.success(userService.findUserById(id));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改用户信息", notes = "修改用户信息，仅允许修改登录用户信息")
    public Result update(@Valid @RequestBody UserVO vo) {
        userService.update(vo);
        return ResultUtils.success();
    }

    @GetMapping("/pageFindByNickName")
    @ApiOperation(value = "查找用户", notes = "根据昵称查找用户")
    public Result findByNickName(@NotEmpty(message = "用户昵称不可为空") @RequestParam("nickName") String nickName) {
        return ResultUtils.success(userService.pageFindUserByNickName(nickName));
    }

    @GetMapping("/findByName")
    @ApiOperation(value = "查找用户",notes="根据用户名或昵称查找用户")
    public Result<List<UserVO>> findByName(@NotEmpty(message = "用户名称不可为空") @RequestParam("name") String name){
        return ResultUtils.success( userService.findUserByName(name));
    }

    @ApiOperation(value = "修改密码", notes = "修改密码")
    @PostMapping("/modifyPassword")
    public Result modifyPassword(@Valid @RequestBody PasswordVO passwordVO) {
        userService.modifyPassword(passwordVO);
        return ResultUtils.success();
    }

    @PutMapping("/modifyPwd")
    @ApiOperation(value = "修改密码",notes="修改用户密码")
    public Result update(@Valid @RequestBody ModifyPwdDTO dto){
        userService.modifyPassword(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "分页查询用户",notes="分页查询用户")
    @PostMapping ("/page")
    public Result page(@RequestBody UserQueryDTO dto){
        return ResultUtils.success(userService.page(dto));
    }

    @ApiOperation(value = "禁言用户",notes="禁言用户")
    @PostMapping ("/bandUser")
    public Result banUser(@Valid @RequestBody UserBanDTO dto){
        userService.bandUser(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "解禁用户",notes="解禁用户")
    @PostMapping ("/unBandUser")
    public Result unBandUser(@Valid @RequestBody UserBanDTO dto){
        userService.unBandUser(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "封禁账号",notes="封禁账号")
    @GetMapping ("/banAccount")
    public Result banAccount(@RequestParam Long userId) {
        userService.banAccount(userId);
        return ResultUtils.success();
    }

    @ApiOperation(value = "解封账号",notes="解封账号")
    @GetMapping ("/unBanAccount")
    public Result unBanAccount(@RequestParam Long userId) {
        userService.unBanAccount(userId);
        return ResultUtils.success();
    }
}

