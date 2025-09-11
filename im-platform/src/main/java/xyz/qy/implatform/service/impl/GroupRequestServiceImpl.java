package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.GroupRequest;
import xyz.qy.implatform.mapper.GroupRequestMapper;
import xyz.qy.implatform.service.IGroupRequestService;

/**
 * 群组请求表 服务实现类
 *
 * @author Polaris
 * @since 2025-09-11
 */
@Service
public class GroupRequestServiceImpl extends ServiceImpl<GroupRequestMapper, GroupRequest> implements IGroupRequestService {

}
