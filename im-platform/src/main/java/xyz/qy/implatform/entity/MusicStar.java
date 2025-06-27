package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("im_music_star")
public class MusicStar extends Model<MusicStar> {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long musicId;

    private Boolean deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}
