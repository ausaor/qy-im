package xyz.qy.imserver.netty.processor;

import xyz.qy.imcommon.enums.IMCmdType;
import xyz.qy.imserver.util.SpringContextHolder;

public class ProcessorFactory {
    public static AbstractMessageProcessor createProcessor(IMCmdType cmd) {
        AbstractMessageProcessor processor = null;
        switch (cmd) {
            case LOGIN:
                processor = SpringContextHolder.getApplicationContext().getBean(LoginProcessor.class);
                break;
            case HEART_BEAT:
                processor = SpringContextHolder.getApplicationContext().getBean(HeartbeatProcessor.class);
                break;
            case PRIVATE_MESSAGE:
                processor = SpringContextHolder.getApplicationContext().getBean(PrivateMessageProcessor.class);
                break;
            case GROUP_MESSAGE:
                processor = SpringContextHolder.getApplicationContext().getBean(GroupMessageProcessor.class);
                break;
            case REGION_GROUP_MESSAGE:
                processor = SpringContextHolder.getApplicationContext().getBean(RegionGroupMessageProcessor.class);
                break;
            case SYSTEM_MESSAGE:
                processor = SpringContextHolder.getApplicationContext().getBean(SystemMessageProcessor.class);
                break;
            case TALK_MESSAGE:
                processor = SpringContextHolder.getApplicationContext().getBean(TalkMessageProcessor.class);
                break;
            default:
                break;
        }
        return processor;
    }
}
