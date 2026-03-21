package com.naturalhub.web.controller.user;

import com.naturalhub.common.core.controller.BaseController;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.core.domain.entity.SysUser;
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.system.domain.IdentifierApplication;
import com.naturalhub.system.service.IIdentifierApplicationService;
import com.naturalhub.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 申请成为鉴定者
 *
 * @author NaturalHub
 */
@RestController
@RequestMapping("/user/identifier/application")
public class UserIdentifierApplicationController extends BaseController
{
    @Autowired
    private IIdentifierApplicationService applicationService;

    @Autowired
    private ISysUserService userService;

    /**
     * 查询当前用户的申请状态
     * GET /user/identifier/application/my
     */
    @GetMapping("/my")
    public AjaxResult myApplication()
    {
        Long userId = SecurityUtils.getUserId();
        IdentifierApplication app = applicationService.getMyApplication(userId);
        if (app == null)
        {
            return success(null);
        }
        return success(app);
    }

    /**
     * 提交申请
     * POST /user/identifier/application/submit
     */
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody IdentifierApplication application)
    {
        Long userId = SecurityUtils.getUserId();
        // 查询当前用户类型，管理员不允许申请
        SysUser user = userService.selectUserById(userId);
        if (user != null && "0".equals(user.getUserType()))
        {
            return error("管理员账号无需申请鉴定者身份");
        }
        if (user != null && "2".equals(user.getUserType()))
        {
            return error("您已是鉴定者，无需重复申请");
        }
        application.setUserId(userId);
        application.setUserName(SecurityUtils.getUsername());
        Long id = applicationService.submitApplication(application);
        return AjaxResult.success("申请提交成功，请等待管理员审核", id);
    }
}
