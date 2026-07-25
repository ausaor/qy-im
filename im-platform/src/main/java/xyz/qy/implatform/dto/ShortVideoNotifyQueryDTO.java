package xyz.qy.implatform.dto;

import lombok.Data;
import xyz.qy.implatform.enums.RecordTypeEnum;
import xyz.qy.implatform.enums.ValidEnum;

@Data
public class ShortVideoNotifyQueryDTO {
    @ValidEnum(enumClass = RecordTypeEnum.class, property = "code", message = "记录类型错误")
    private Integer recordType;
}
