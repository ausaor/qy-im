package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.annotation.RepeatSubmit;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.vo.FriendVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Api(tags = "好友")
@RestController
@RequestMapping("/friend")
public class FriendController {

    @Resource
    private IFriendService friendService;

    @GetMapping("/list")
    @ApiOperation(value = "好友列表", notes = "获取好友列表")
    public Result<List<FriendVO>> findFriends() {
        return ResultUtils.success(friendService.findFriends());
    }

    @RepeatSubmit
    @PostMapping("/add")
    @ApiOperation(value = "添加好友", notes = "双方建立好友关系")
    public Result addFriend(@NotEmpty(message = "好友id不可为空") @RequestParam("friendId") Long friendId) {
        friendService.addFriend(friendId);
        return ResultUtils.success();
    }

    @GetMapping("/find/{friendId}")
    @ApiOperation(value = "查找好友信息", notes = "查找好友信息")
    public Result<FriendVO> findFriend(@NotEmpty(message = "好友id不可为空") @PathVariable("friendId") Long friendId) {
        return ResultUtils.success(friendService.findFriend(friendId));
    }


    @DeleteMapping("/delete/{friendId}")
    @ApiOperation(value = "删除好友", notes = "解除好友关系")
    public Result delFriend(@NotEmpty(message = "好友id不可为空") @PathVariable("friendId") Long friendId) {
        friendService.delFriend(friendId);
        return ResultUtils.success();
    }

    @PutMapping("/update")
    @ApiOperation(value = "更新好友信息", notes = "更新好友头像或昵称")
    public Result modifyFriend(@Valid @RequestBody FriendVO vo) {
        friendService.update(vo);
        return ResultUtils.success();
    }

    @PutMapping("/updateMyInfoToFriend")
    @ApiOperation(value = "更新好友关于我的信息", notes = "更新好友关于我的信息")
    public Result updateMyInfoToFriend(@Valid @RequestBody FriendVO vo) {
        friendService.updateMyInfoToFriend(vo);
        return ResultUtils.success();
    }
}

