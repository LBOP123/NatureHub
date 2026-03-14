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
import com.naturalhub.system.domain.CommunityTopic;
import com.naturalhub.system.service.ICommunityTopicService;
import com.naturalhub.common.utils.poi.ExcelUtil;
import com.naturalhub.common.core.page.TableDataInfo;

/**
 * 社群话题Controller（管理端）
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
@RestController
@RequestMapping("/community/topic")
public class CommunityTopicController extends BaseController
{
    @Autowired
    private ICommunityTopicService communityTopicService;

    /**
     * 查询社群话题列表
     */
    @PreAuthorize("@ss.hasPermi('community:topic:list')")
    @GetMapping("/list")
    public TableDataInfo list(CommunityTopic communityTopic)
    {
        startPage();
        List<CommunityTopic> list = communityTopicService.selectCommunityTopicList(communityTopic);
        return getDataTable(list);
    }

    /**
     * 导出社群话题列表
     */
    @PreAuthorize("@ss.hasPermi('community:topic:export')")
    @Log(title = "社群话题", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CommunityTopic communityTopic)
    {
        List<CommunityTopic> list = communityTopicService.selectCommunityTopicList(communityTopic);
        ExcelUtil<CommunityTopic> util = new ExcelUtil<CommunityTopic>(CommunityTopic.class);
        util.exportExcel(response, list, "社群话题数据");
    }

    /**
     * 获取社群话题详细信息
     */
    @PreAuthorize("@ss.hasPermi('community:topic:query')")
    @GetMapping(value = "/{topicId}")
    public AjaxResult getInfo(@PathVariable("topicId") Long topicId)
    {
        return success(communityTopicService.selectCommunityTopicByTopicId(topicId));
    }

    /**
     * 新增社群话题
     */
    @PreAuthorize("@ss.hasPermi('community:topic:add')")
    @Log(title = "社群话题", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CommunityTopic communityTopic)
    {
        return toAjax(communityTopicService.insertCommunityTopic(communityTopic));
    }

    /**
     * 修改社群话题
     */
    @PreAuthorize("@ss.hasPermi('community:topic:edit')")
    @Log(title = "社群话题", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CommunityTopic communityTopic)
    {
        return toAjax(communityTopicService.updateCommunityTopic(communityTopic));
    }

    /**
     * 删除社群话题
     */
    @PreAuthorize("@ss.hasPermi('community:topic:remove')")
    @Log(title = "社群话题", businessType = BusinessType.DELETE)
	@DeleteMapping("/{topicIds}")
    public AjaxResult remove(@PathVariable Long[] topicIds)
    {
        return toAjax(communityTopicService.deleteCommunityTopicByTopicIds(topicIds));
    }

    /**
     * 置顶话题
     */
    @PreAuthorize("@ss.hasPermi('community:topic:edit')")
    @Log(title = "置顶话题", businessType = BusinessType.UPDATE)
    @PutMapping("/top/{topicId}/{isTop}")
    public AjaxResult setTop(@PathVariable Long topicId, @PathVariable String isTop)
    {
        return toAjax(communityTopicService.setTopicTop(topicId, isTop));
    }

    /**
     * 设置精华话题
     */
    @PreAuthorize("@ss.hasPermi('community:topic:edit')")
    @Log(title = "设置精华话题", businessType = BusinessType.UPDATE)
    @PutMapping("/essence/{topicId}/{isEssence}")
    public AjaxResult setEssence(@PathVariable Long topicId, @PathVariable String isEssence)
    {
        return toAjax(communityTopicService.setTopicEssence(topicId, isEssence));
    }

    /**
     * 关闭/开启话题
     */
    @PreAuthorize("@ss.hasPermi('community:topic:edit')")
    @Log(title = "关闭/开启话题", businessType = BusinessType.UPDATE)
    @PutMapping("/status/{topicId}/{status}")
    public AjaxResult setStatus(@PathVariable Long topicId, @PathVariable String status)
    {
        return toAjax(communityTopicService.setTopicStatus(topicId, status));
    }

    /**
     * 审核话题
     */
    @PreAuthorize("@ss.hasPermi('community:topic:audit')")
    @Log(title = "审核话题", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{topicId}/{auditStatus}")
    public AjaxResult auditTopic(@PathVariable Long topicId, @PathVariable Integer auditStatus, String auditRemark)
    {
        return toAjax(communityTopicService.auditTopic(topicId, auditStatus, auditRemark));
    }
}
