package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_character_word")
public class CharacterWord extends BaseEntity{
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "template_group_id")
    private Long templateGroupId;

    @TableField(value = "character_id")
    private Long characterId;

    @TableField(value = "character_name")
    private String characterName;

    @TableField(value = "word")
    private String word;

    @TableField(value = "voice")
    private String voice;

    @TableField("status")
    private String status;
}
