package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("im_music")
public class Music extends Model<Music> {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String singer;

    private String name;

    private String url;

    private String cover;

    private Integer duration;

    private Integer playCount;

    private String category;

    private Long groupId;

    private String regionCode;

    private Long groupTemplateId;

    private Long characterId;

    private Boolean deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}
