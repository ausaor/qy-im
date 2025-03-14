package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-05-02 16:44
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_dict_data")
public class DictData extends Model<DictData> {
    private static final long serialVersionUID = 1L;

    /** 字典编码 */
    @TableId(value = "dict_code", type = IdType.AUTO)
    private Long dictCode;

    /** 字典排序 */
    @TableField("dict_sort")
    private Long dictSort;

    /** 字典标签 */
    @TableField("dict_label")
    private String dictLabel;

    /** 字典键值 */
    @TableField("dict_value")
    private String dictValue;

    /** 字典类型 */
    @TableField("dict_type")
    private String dictType;

    /** 状态（0正常 1停用） */
    @TableField("status")
    private String status;

    @Override
    protected Serializable pkVal() {
        return this.dictCode;
    }
}
