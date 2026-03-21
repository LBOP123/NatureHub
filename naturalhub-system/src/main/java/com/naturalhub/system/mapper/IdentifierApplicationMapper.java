package com.naturalhub.system.mapper;

import com.naturalhub.system.domain.IdentifierApplication;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 鉴定者申请 Mapper 接口
 *
 * @author NaturalHub
 */
public interface IdentifierApplicationMapper
{
    /**
     * 新增申请
     */
    int insertApplication(IdentifierApplication application);

    /**
     * 根据ID查询申请
     */
    IdentifierApplication selectApplicationById(Long id);

    /**
     * 查询指定用户的最新一条申请
     */
    IdentifierApplication selectLatestByUserId(Long userId);

    /**
     * 查询申请列表（管理端，支持按状态/用户名筛选）
     */
    List<IdentifierApplication> selectApplicationList(IdentifierApplication application);

    /**
     * 更新申请（审核时使用）
     */
    int updateApplication(IdentifierApplication application);
}
