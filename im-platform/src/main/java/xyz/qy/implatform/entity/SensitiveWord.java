package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@TableName("im_sensitive_word")
public class SensitiveWord {
	/**
	* id
	*/
	@TableId
	private Long id;

	/**
	* 敏感词内容
	*/
	private String content;

	/**
	* 是否启用
	*/
	private Boolean enabled;

	/**
	 * 创建者
	 */
	private Long creator;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;
}