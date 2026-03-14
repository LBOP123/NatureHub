package com.naturalhub.web.controller.admin;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.naturalhub.common.annotation.Log;
import com.naturalhub.common.core.controller.BaseController;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.enums.BusinessType;
import com.naturalhub.common.utils.poi.ExcelUtil;
import com.naturalhub.common.core.page.TableDataInfo;
import com.naturalhub.system.domain.SpeciesIdentification;
import com.naturalhub.system.domain.IdentificationAnswer;
import com.naturalhub.system.service.ISpeciesIdentificationService;
import com.naturalhub.system.service.IIdentificationAnswerService;

/**
 * 物种鉴定求助管理端Controller
 * 
 * @author NaturalHub
 * @date 2026-03-09
 */
@RestController
@RequestMapping("/admin/identification")
public class IdentificationAdminController extends BaseController
{
    @Autowired
    private ISpeciesIdentificationService speciesIdentificationService;

    @Autowired
    private IIdentificationAnswerService identificationAnswerService;

    /**
     * 查询物种鉴定求助列表
     */
    @PreAuthorize("@ss.hasPermi('admin:identification:list')")
    @GetMapping("/list")
    public TableDataInfo list(SpeciesIdentification speciesIdentification)
    {
        startPage();
        List<SpeciesIdentification> list = speciesIdentificationService.selectSpeciesIdentificationList(speciesIdentification);
        return getDataTable(list);
    }

    /**
     * 导出物种鉴定求助列表
     */
    @PreAuthorize("@ss.hasPermi('admin:identification:export')")
    @Log(title = "物种鉴定求助", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SpeciesIdentification speciesIdentification)
    {
        List<SpeciesIdentification> list = speciesIdentificationService.selectSpeciesIdentificationList(speciesIdentification);
        ExcelUtil<SpeciesIdentification> util = new ExcelUtil<SpeciesIdentification>(SpeciesIdentification.class);
        util.exportExcel(response, list, "物种鉴定求助数据");
    }

    /**
     * 获取物种鉴定求助详细信息
     */
    @PreAuthorize("@ss.hasPermi('admin:identification:query')")
    @GetMapping(value = "/{identificationId}")
    public AjaxResult getInfo(@PathVariable("identificationId") Long identificationId)
    {
        SpeciesIdentification identification = speciesIdentificationService.selectSpeciesIdentificationByIdentificationId(identificationId);
        
        // 获取回答列表
        List<IdentificationAnswer> answers = identificationAnswerService.selectAnswersByIdentificationId(identificationId);
        
        AjaxResult result = AjaxResult.success(identification);
        result.put("answers", answers);
        return result;
    }

    /**
     * 新增物种鉴定求助
     */
    @PreAuthorize("@ss.hasPermi('admin:identification:add')")
    @Log(title = "物种鉴定求助", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SpeciesIdentification speciesIdentification)
    {
        return toAjax(speciesIdentificationService.insertSpeciesIdentification(speciesIdentification));
    }

    /**
     * 修改物种鉴定求助
     */
    @PreAuthorize("@ss.hasPermi('admin:identification:edit')")
    @Log(title = "物种鉴定求助", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SpeciesIdentification speciesIdentification)
    {
        return toAjax(speciesIdentificationService.updateSpeciesIdentification(speciesIdentification));
    }

    /**
     * 删除物种鉴定求助
     */
    @PreAuthorize("@ss.hasPermi('admin:identification:remove')")
    @Log(title = "物种鉴定求助", businessType = BusinessType.DELETE)
	@DeleteMapping("/{identificationIds}")
    public AjaxResult remove(@PathVariable Long[] identificationIds)
    {
        return toAjax(speciesIdentificationService.deleteSpeciesIdentificationByIdentificationIds(identificationIds));
    }

    /**
     * 审核物种鉴定求助
     */
    @PreAuthorize("@ss.hasPermi('admin:identification:audit')")
    @Log(title = "审核物种鉴定求助", businessType = BusinessType.UPDATE)
    @PostMapping("/{identificationId}/audit")
    public AjaxResult audit(@PathVariable Long identificationId, @RequestBody AuditRequest request)
    {
        return toAjax(speciesIdentificationService.audit(identificationId, request.getAuditStatus(), request.getAuditRemark()));
    }

    /**
     * 批量审核
     */
    @PreAuthorize("@ss.hasPermi('admin:identification:audit')")
    @Log(title = "批量审核物种鉴定求助", businessType = BusinessType.UPDATE)
    @PostMapping("/audit/batch")
    public AjaxResult batchAudit(@RequestBody BatchAuditRequest request)
    {
        int count = 0;
        for (Long id : request.getIds()) {
            count += speciesIdentificationService.audit(id, request.getAuditStatus(), request.getAuditRemark());
        }
        return toAjax(count);
    }

    /**
     * 获取统计数据
     */
    @PreAuthorize("@ss.hasPermi('admin:identification:list')")
    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        // 待审核数量
        SpeciesIdentification pending = new SpeciesIdentification();
        pending.setAuditStatus(1); // 1=待审核
        int pendingCount = speciesIdentificationService.selectSpeciesIdentificationList(pending).size();
        
        // 待鉴定数量
        SpeciesIdentification pendingIdentify = new SpeciesIdentification();
        pendingIdentify.setStatus(0); // 0=待鉴定
        pendingIdentify.setAuditStatus(2); // 2=已通过
        int pendingIdentifyCount = speciesIdentificationService.selectSpeciesIdentificationList(pendingIdentify).size();
        
        // 已解决数量
        SpeciesIdentification resolved = new SpeciesIdentification();
        resolved.setStatus(2); // 2=已解决
        int resolvedCount = speciesIdentificationService.selectSpeciesIdentificationList(resolved).size();
        
        // 总数
        int totalCount = speciesIdentificationService.selectSpeciesIdentificationList(new SpeciesIdentification()).size();
        
        AjaxResult result = AjaxResult.success();
        result.put("pendingCount", pendingCount);
        result.put("pendingIdentifyCount", pendingIdentifyCount);
        result.put("resolvedCount", resolvedCount);
        result.put("totalCount", totalCount);
        return result;
    }

    /**
     * 审核请求对象
     */
    public static class AuditRequest {
        private Integer auditStatus;
        private String auditRemark;

        public Integer getAuditStatus() {
            return auditStatus;
        }

        public void setAuditStatus(Integer auditStatus) {
            this.auditStatus = auditStatus;
        }

        public String getAuditRemark() {
            return auditRemark;
        }

        public void setAuditRemark(String auditRemark) {
            this.auditRemark = auditRemark;
        }
    }

    /**
     * 批量审核请求对象
     */
    public static class BatchAuditRequest {
        private Long[] ids;
        private Integer auditStatus;
        private String auditRemark;

        public Long[] getIds() {
            return ids;
        }

        public void setIds(Long[] ids) {
            this.ids = ids;
        }

        public Integer getAuditStatus() {
            return auditStatus;
        }

        public void setAuditStatus(Integer auditStatus) {
            this.auditStatus = auditStatus;
        }

        public String getAuditRemark() {
            return auditRemark;
        }

        public void setAuditRemark(String auditRemark) {
            this.auditRemark = auditRemark;
        }
    }
}
