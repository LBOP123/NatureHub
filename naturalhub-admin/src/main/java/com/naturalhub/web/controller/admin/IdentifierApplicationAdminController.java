package com.naturalhub.web.controller.admin;

import com.naturalhub.common.annotation.Log;
import com.naturalhub.common.core.controller.BaseController;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.core.page.TableDataInfo;
import com.naturalhub.common.enums.BusinessType;
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.system.domain.IdentifierApplication;
import com.naturalhub.system.service.IIdentifierApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端 - 鉴定者申请审核
 *
 * @author NaturalHub
 */
@RestController
@RequestMapping("/admin/identifier/application")
public class IdentifierApplicationAdminController extends BaseController
{
    @Autowired
    private IIdentifierApplicationService applicationService;

    /**
     * 查询申请列表
     * GET /admin/identifier/application/list
     */
    @PreAuthorize("@ss.hasPermi('admin:identifier:list')")
    @GetMapping("/list")
    public TableDataInfo list(IdentifierApplication application)
    {
        startPage();
        List<IdentifierApplication> list = applicationService.listApplications(application);
        return getDataTable(list);
    }

    /**
     * 获取申请详情
     * GET /admin/identifier/application/{id}
     */
    @PreAuthorize("@ss.hasPermi('admin:identifier:list')")
    @GetMapping("/{id}")
    public AjaxResult getDetail(@PathVariable Long id)
    {
        return success(applicationService.listApplications(
                new IdentifierApplication()).stream()
                .filter(a -> a.getId().equals(id))
                .findFirst().orElse(null));
    }

    /**
     * 审核通过
     * POST /admin/identifier/application/{id}/approve
     */
    @PreAuthorize("@ss.hasPermi('admin:identifier:audit')")
    @Log(title = "鉴定者申请", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/approve")
    public AjaxResult approve(@PathVariable Long id)
    {
        applicationService.approve(id, SecurityUtils.getUsername());
        return success("审核通过，已为用户分配鉴定者角色");
    }

    /**
     * 审核拒绝
     * POST /admin/identifier/application/{id}/reject
     * body: { "rejectReason": "xxx" }
     */
    @PreAuthorize("@ss.hasPermi('admin:identifier:audit')")
    @Log(title = "鉴定者申请", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/reject")
    public AjaxResult reject(@PathVariable Long id, @RequestBody Map<String, String> body)
    {
        String rejectReason = body.getOrDefault("rejectReason", "");
        applicationService.reject(id, rejectReason, SecurityUtils.getUsername());
        return success("已拒绝该申请");
    }
}
