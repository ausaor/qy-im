package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("用户信息VO")
public class UserVO {
    @NotNull(message = "用户id不能为空")
    @ApiModelProperty(value = "id")
    private Long id;

    @NotEmpty(message = "用户名不能为空")
    @Length(max = 64, message = "用户名不能大于64字符")
    @ApiModelProperty(value = "用户名")
    private String userName;

    @NotEmpty(message = "用户昵称不能为空")
    @Length(max = 64, message = "昵称不能大于64字符")
    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "性别")
    private Integer sex;

    @ApiModelProperty(value = "用户类型 1:普通用户 2:审核账户")
    private Integer type;

    @Length(max = 64, message = "个性签名不能大于1024个字符")
    @ApiModelProperty(value = "个性签名")
    private String signature;

    @ApiModelProperty(value = "头像")
    private String headImage;

    @ApiModelProperty(value = "头像缩略图")
    private String headImageThumb;

    @ApiModelProperty(value = "是否在线")
    private Boolean online;

    @ApiModelProperty(value = "IP地址")
    private String ipAddress;

    @ApiModelProperty(value = "省份")
    private String province;

    @ApiModelProperty(value = "城市")
    private String city;

    @ApiModelProperty(value = "好友备注")
    private String friendRemark;

    @ApiModelProperty(value = "与好友聊天头像")
    private String myHeadImageToFriend;

    @ApiModelProperty(value = "语音自动播放")
    private Boolean autoPlay;

    @ApiModelProperty(value = "消息提示音")
    private Boolean soundPlay;

    private Boolean isDisable;

    private Boolean isBanned;
}
