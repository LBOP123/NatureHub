package com.naturalhub.web.controller.species;

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
import com.naturalhub.system.domain.SpeciesTaxa;
import com.naturalhub.system.service.ISpeciesTaxaService;
import com.naturalhub.common.utils.poi.ExcelUtil;
import com.naturalhub.common.core.page.TableDataInfo;

/**
 * 物种分类基础Controller
 * 
 * @author Kairos
 * @date 2025-09-08
 */
@RestController
@RequestMapping("/species/taxa")
public class SpeciesTaxaController extends BaseController
{
    @Autowired
    private ISpeciesTaxaService speciesTaxaService;

    /**
     * 查询物种分类基础列表
     */
    @PreAuthorize("@ss.hasPermi('species:taxa:list')")
    @GetMapping("/list")
    public TableDataInfo list(SpeciesTaxa speciesTaxa)
    {
        startPage();
        List<SpeciesTaxa> list = speciesTaxaService.selectSpeciesTaxaList(speciesTaxa);
        return getDataTable(list);
    }

    /**
     * 导出物种分类基础列表
     */
    @PreAuthorize("@ss.hasPermi('species:taxa:export')")
    @Log(title = "物种分类基础", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SpeciesTaxa speciesTaxa)
    {
        List<SpeciesTaxa> list = speciesTaxaService.selectSpeciesTaxaList(speciesTaxa);
        ExcelUtil<SpeciesTaxa> util = new ExcelUtil<SpeciesTaxa>(SpeciesTaxa.class);
        util.exportExcel(response, list, "物种分类基础数据");
    }

    /**
     * 获取物种分类基础详细信息
     */
    @PreAuthorize("@ss.hasPermi('species:taxa:query')")
    @GetMapping(value = "/{taxonId}")
    public AjaxResult getInfo(@PathVariable("taxonId") Long taxonId)
    {
        return success(speciesTaxaService.selectSpeciesTaxaByTaxonId(taxonId));
    }

    /**
     * 新增物种分类基础
     */
    @PreAuthorize("@ss.hasPermi('species:taxa:add')")
    @Log(title = "物种分类基础", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SpeciesTaxa speciesTaxa)
    {
        return toAjax(speciesTaxaService.insertSpeciesTaxa(speciesTaxa));
    }

    /**
     * 修改物种分类基础
     */
    @PreAuthorize("@ss.hasPermi('species:taxa:edit')")
    @Log(title = "物种分类基础", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SpeciesTaxa speciesTaxa)
    {
        return toAjax(speciesTaxaService.updateSpeciesTaxa(speciesTaxa));
    }

    /**
     * 删除物种分类基础
     */
    @PreAuthorize("@ss.hasPermi('species:taxa:remove')")
    @Log(title = "物种分类基础", businessType = BusinessType.DELETE)
	@DeleteMapping("/{taxonIds}")
    public AjaxResult remove(@PathVariable Long[] taxonIds)
    {
        return toAjax(speciesTaxaService.deleteSpeciesTaxaByTaxonIds(taxonIds));
    }
}
