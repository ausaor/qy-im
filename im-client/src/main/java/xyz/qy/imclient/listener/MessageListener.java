package xyz.qy.imclient.listener;


import xyz.qy.imcommon.model.IMSendResult;

import java.util.List;

public interface MessageListener<T> {

     void process(List<IMSendResult<T>> result);

}
