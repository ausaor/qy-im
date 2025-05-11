package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.qy.implatform.entity.Talk;
import xyz.qy.implatform.entity.TalkComment;
import xyz.qy.implatform.entity.TalkStar;

@Data
@ApiModel("动态消息VO")
public class TalkMessageVO {

    @ApiModelProperty(value = "数据类型：动态，评论，点赞")
    private Integer type;

    private Talk talk;

    private TalkComment talkComment;

    private TalkStar talkStar;
}
