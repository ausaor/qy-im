package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.dto.ComplaintRecordDTO;
import xyz.qy.implatform.entity.ComplaintRecord;
import xyz.qy.implatform.enums.ComplaintStatusEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.ComplaintRecordMapper;
import xyz.qy.implatform.service.IComplaintRecordService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;

@Slf4j
@Service
public class ComplaintRecordServiceImpl extends ServiceImpl<ComplaintRecordMapper, ComplaintRecord> implements IComplaintRecordService {

    @Override
    public void submitComplaint(ComplaintRecordDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        LambdaQueryWrapper<ComplaintRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ComplaintRecord::getUserId, userId)
                .eq(ComplaintRecord::getTargetId, dto.getTargetId())
                .eq(ComplaintRecord::getTargetType, dto.getTargetType())
                .eq(ComplaintRecord::getStatus, ComplaintStatusEnum.SUBMITTED.getCode());
        if (this.count(queryWrapper) > 0) {
            throw new GlobalException("已存在对目标对象的投诉!");
        }

        ComplaintRecord complaintRecord = BeanUtils.copyProperties(dto, ComplaintRecord.class);
        complaintRecord.setUserId(userId);
        complaintRecord.setStatus(ComplaintStatusEnum.SUBMITTED.getCode());
        complaintRecord.setCreateBy(userId);
        if (CollectionUtils.isNotEmpty(dto.getEvidenceImgList())) {
            complaintRecord.setEvidenceImg(String.join(",", dto.getEvidenceImgList()));
        }
        this.save(complaintRecord);
    }
}
