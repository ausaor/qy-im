package xyz.qy.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.qy.implatform.entity.SensitiveWord;

@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {
	
}