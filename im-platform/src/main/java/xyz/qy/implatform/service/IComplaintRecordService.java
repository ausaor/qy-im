package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.ComplaintRecordDTO;
import xyz.qy.implatform.entity.ComplaintRecord;

/**
 * 投诉记录服务类
 *
 * @author Lingma
 * @since 2026-05-05
 */
public interface IComplaintRecordService extends IService<ComplaintRecord> {
    /**
     * 新增投诉
     *
     * @param dto 投诉记录
     */
    void submitComplaint(ComplaintRecordDTO dto);
}