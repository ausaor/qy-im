package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.PushTaskReceiver;
import xyz.qy.implatform.mapper.PushTaskReceiverMapper;
import xyz.qy.implatform.service.IPushTaskReceiverService;

/**
 * 推送任务的接收用户
 *
 * @author Polaris
 * @since 2024-12-28
 */
@Slf4j
@Service
public class PushTaskReceiverServiceImpl extends ServiceImpl<PushTaskReceiverMapper, PushTaskReceiver> implements IPushTaskReceiverService {
}
