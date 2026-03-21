package com.naturalhub.system.service;

import com.naturalhub.system.domain.IdentifierApplication;

import java.util.List;

/**
 * 鉴定者申请 Service 接口
 *
 * @author NaturalHub
 */
public interface IIdentifierApplicationService
{
    /**
     * 用户提交申请
     *
     * @param application 申请信息
     * @return 申请ID
     */
    Long submitApplication(IdentifierApplication application);

    /**
     * 查询当前用户的最新申请状态
     *
     * @param userId 用户ID
     * @return 申请对象，null 表示从未申请
     */
    IdentifierApplication getMyApplication(Long userId);

    /**
     * 管理端：查询申请列表
     *
     * @param application 查询条件
     * @return 列表
     */
    List<IdentifierApplication> listApplications(IdentifierApplication application);

    /**
     * 管理端：审核通过
     *
     * @param id       申请ID
     * @param reviewBy 审核人
     */
    void approve(Long id, String reviewBy);

    /**
     * 管理端：审核拒绝
     *
     * @param id           申请ID
     * @param rejectReason 拒绝原因
     * @param reviewBy     审核人
     */
    void reject(Long id, String rejectReason, String reviewBy);
}
