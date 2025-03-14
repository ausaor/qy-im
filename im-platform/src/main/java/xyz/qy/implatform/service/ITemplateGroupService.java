package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.TemplateGroup;
import xyz.qy.implatform.vo.ReviewVO;
import xyz.qy.implatform.vo.TemplateGroupVO;

import java.util.List;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-12 11:11
 **/
public interface ITemplateGroupService extends IService<TemplateGroup> {
    void addOrModify(TemplateGroupVO vo);

    void delete(Long id);

    List<TemplateGroupVO> findTemplateGroups(TemplateGroupVO templateGroupVO);

    List<TemplateGroupVO> findMyTemplateGroups();

    List<TemplateGroupVO> pageAllTemplateGroups();

    int countUserTemplateGroup();

    void submitForApproval(Long id);

    void withdrawalOfApproval(Long id);

    /**
     * 查询待审批的模板群聊
     *
     * @return 模板群聊
     */
    List<TemplateGroupVO> findReviewingTemplateGroups();

    /**
     * 提交审核结论
     *
     * @param reviewVO
     */
    void submitAuditConclusion(ReviewVO reviewVO);

    /**
     * 查询已发布的群聊模板
     *
     * @param id id
     * @return 已发布群聊模板
     */
    TemplateGroup findPublishedById(Long id);
}
