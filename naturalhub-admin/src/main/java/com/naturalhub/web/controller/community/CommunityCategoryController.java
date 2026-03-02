package com.naturalhub.web.controller.community;

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
import com.naturalhub.system.domain.CommunityCategory;
import com.naturalhub.system.service.ICommunityCategoryService;
import com.naturalhub.common.utils.poi.ExcelUtil;
import com.naturalhub.common.core.page.TableDataInfo;

/**
 * 社群板块分类Controller
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
@RestController
@RequestMapping("/community/category")
public class CommunityCategoryController extends BaseController
{
    @Autowired
    private ICommunityCategoryService communityCategoryService;

    /**
     * 查询社群板块分类列表
     */
    @PreAuthorize("@ss.hasPermi('community:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(CommunityCategory communityCategory)
    {
        startPage();
        List<CommunityCategory> list = communityCategoryService.selectCommunityCategoryList(communityCategory);
        return getDataTable(list);
    }

    /**
     * 查询所有板块（不分页）
     */
    @GetMapping("/listAll")
    public AjaxResult listAll()
    {
        CommunityCategory communityCategory = new CommunityCategory();
        communityCategory.setStatus("0");
        List<CommunityCategory> list = communityCategoryService.selectCommunityCategoryList(communityCategory);
        return success(list);
    }

    /**
     * 导出社群板块分类列表
     */
    @PreAuthorize("@ss.hasPermi('community:category:export')")
    @Log(title = "社群板块分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CommunityCategory communityCategory)
    {
        List<CommunityCategory> list = communityCategoryService.selectCommunityCategoryList(communityCategory);
        ExcelUtil<CommunityCategory> util = new ExcelUtil<CommunityCategory>(CommunityCategory.class);
        util.exportExcel(response, list, "社群板块分类数据");
    }

    /**
     * 获取社群板块分类详细信息
     */
    @PreAuthorize("@ss.hasPermi('community:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId)
    {
        return success(communityCategoryService.selectCommunityCategoryByCategoryId(categoryId));
    }

    /**
     * 新增社群板块分类
     */
    @PreAuthorize("@ss.hasPermi('community:category:add')")
    @Log(title = "社群板块分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CommunityCategory communityCategory)
    {
        // 检查板块代码是否已存在
        CommunityCategory existCategory = communityCategoryService.selectCommunityCategoryByCode(communityCategory.getCategoryCode());
        if (existCategory != null)
        {
            return error("板块代码已存在");
        }
        return toAjax(communityCategoryService.insertCommunityCategory(communityCategory));
    }

    /**
     * 修改社群板块分类
     */
    @PreAuthorize("@ss.hasPermi('community:category:edit')")
    @Log(title = "社群板块分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CommunityCategory communityCategory)
    {
        return toAjax(communityCategoryService.updateCommunityCategory(communityCategory));
    }

    /**
     * 删除社群板块分类
     */
    @PreAuthorize("@ss.hasPermi('community:category:remove')")
    @Log(title = "社群板块分类", businessType = BusinessType.DELETE)
	@DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(communityCategoryService.deleteCommunityCategoryByCategoryIds(categoryIds));
    }
}
