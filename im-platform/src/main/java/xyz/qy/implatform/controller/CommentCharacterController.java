package xyz.qy.implatform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ICommentCharacterService;
import xyz.qy.implatform.vo.CommentCharacterVO;

import javax.annotation.Resource;

@RequestMapping("/commentCharacter")
@RestController
public class CommentCharacterController {
    @Resource
    private ICommentCharacterService commentCharacterService;

    @GetMapping("/getCommentCharacter")
    public Result<CommentCharacterVO> getCommentCharacter(@RequestParam("targetId") Long targetId,
                                                          @RequestParam("targetType") String targetType) {
        return ResultUtils.success(commentCharacterService.getCommentCharacter(targetId, targetType));
    }
}
