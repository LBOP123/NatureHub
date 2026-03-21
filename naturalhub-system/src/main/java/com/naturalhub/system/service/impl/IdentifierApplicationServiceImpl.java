package com.naturalhub.system.service.impl;

import com.naturalhub.common.core.domain.entity.SysRole;
import com.naturalhub.common.core.domain.entity.SysUser;
import com.naturalhub.common.exception.ServiceException;
import com.naturalhub.common.utils.DateUtils;
import com.naturalhub.system.domain.IdentifierApplication;
import com.naturalhub.system.domain.SysUserRole;
import com.naturalhub.system.mapper.IdentifierApplicationMapper;
import com.naturalhub.system.mapper.SysRoleMapper;
import com.naturalhub.system.mapper.SysUserMapper;
import com.naturalhub.system.mapper.SysUserRoleMapper;
import com.naturalhub.system.service.IIdentifierApplicationService;
import com.naturalhub.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 鉴定者申请 Service 实现
 *
 * @author NaturalHub
 */
@Service
public class IdentifierApplicationServiceImpl implements IIdentifierApplicationService
{
    /** 鉴定者角色的 role_key，与 sys_role 表中一致 */
    private static final String IDENTIFIER_ROLE_KEY = "identifier";

    /** userType 常量 */
    private static final String USER_TYPE_ADMIN      = "0";
    private static final String USER_TYPE_EXPLORER   = "1";
    private static final String USER_TYPE_IDENTIFIER = "2";

    @Autowired
    private IdentifierApplicationMapper applicationMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public Long submitApplication(IdentifierApplication application)
    {
        // 只有探索者（userType='1'）可以申请
        SysUser user = userMapper.selectUserById(application.getUserId());
        if (user != null && USER_TYPE_ADMIN.equals(user.getUserType()))
        {
            throw new ServiceException("管理员账号无需申请鉴定者");
        }
        if (user != null && USER_TYPE_IDENTIFIER.equals(user.getUserType()))
        {
            throw new ServiceException("您已是鉴定者，无需重复申请");
        }

        // 检查是否已有待审核的申请
        IdentifierApplication latest = applicationMapper.selectLatestByUserId(application.getUserId());
        if (latest != null && latest.getStatus() == 0)
        {
            throw new ServiceException("您已有一条待审核的申请，请勿重复提交");
        }
        // status==2（拒绝）允许重新申请

        applicationMapper.insertApplication(application);
        return application.getId();
    }

    @Override
    public IdentifierApplication getMyApplication(Long userId)
    {
        return applicationMapper.selectLatestByUserId(userId);
    }

    @Override
    public List<IdentifierApplication> listApplications(IdentifierApplication application)
    {
        return applicationMapper.selectApplicationList(application);
    }

    @Override
    @Transactional
    public void approve(Long id, String reviewBy)
    {
        IdentifierApplication application = applicationMapper.selectApplicationById(id);
        if (application == null)
        {
            throw new ServiceException("申请不存在");
        }
        if (application.getStatus() != 0)
        {
            throw new ServiceException("该申请已审核，无法重复操作");
        }

        // 1. 更新申请状态
        application.setStatus(1);
        application.setReviewBy(reviewBy);
        application.setReviewTime(DateUtils.getNowDate());
        applicationMapper.updateApplication(application);

        // 2. 更新用户 userType 为 '2'（鉴定者）
        userMapper.updateUserType(application.getUserId(), USER_TYPE_IDENTIFIER);

        // 3. 查找鉴定者角色
        SysRole identifierRole = roleMapper.checkRoleKeyUnique(IDENTIFIER_ROLE_KEY);
        if (identifierRole == null)
        {
            throw new ServiceException("系统中不存在 role_key=" + IDENTIFIER_ROLE_KEY + " 的鉴定者角色，请先在系统管理-角色管理中创建");
        }

        // 4. 为用户分配鉴定者角色（先删再插，避免重复）
        SysUserRole existingRelation = new SysUserRole();
        existingRelation.setUserId(application.getUserId());
        existingRelation.setRoleId(identifierRole.getRoleId());
        userRoleMapper.deleteUserRoleInfo(existingRelation);

        List<SysUserRole> list = new ArrayList<>();
        SysUserRole ur = new SysUserRole();
        ur.setUserId(application.getUserId());
        ur.setRoleId(identifierRole.getRoleId());
        list.add(ur);
        userRoleMapper.batchUserRole(list);
    }

    @Override
    @Transactional
    public void reject(Long id, String rejectReason, String reviewBy)
    {
        IdentifierApplication application = applicationMapper.selectApplicationById(id);
        if (application == null)
        {
            throw new ServiceException("申请不存在");
        }
        if (application.getStatus() != 0)
        {
            throw new ServiceException("该申请已审核，无法重复操作");
        }

        application.setStatus(2);
        application.setRejectReason(rejectReason);
        application.setReviewBy(reviewBy);
        application.setReviewTime(DateUtils.getNowDate());
        applicationMapper.updateApplication(application);
    }
}
