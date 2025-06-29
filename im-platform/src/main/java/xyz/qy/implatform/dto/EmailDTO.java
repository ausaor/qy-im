package xyz.qy.implatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.qy.implatform.enums.EmailCategoryEnum;
import xyz.qy.implatform.enums.ValidEnum;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDTO {
    @Email(message = "邮箱格式错误")
    @NotBlank(message = "邮箱不可为空")
    private String toEmail;

    @NotBlank(message = "邮件分类不可为空")
    @ValidEnum(enumClass = EmailCategoryEnum.class, message = "无效的邮件分类")
    private String category;
}
