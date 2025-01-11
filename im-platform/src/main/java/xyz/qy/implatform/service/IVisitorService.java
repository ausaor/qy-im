package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.Visitor;

/**
 * @description: 访客信息
 * @author: Polaris
 * @create: 2023-04-29 09:38
 **/
public interface IVisitorService extends IService<Visitor> {
    /**
     * 记录访客信息
     */
    void report();
}
