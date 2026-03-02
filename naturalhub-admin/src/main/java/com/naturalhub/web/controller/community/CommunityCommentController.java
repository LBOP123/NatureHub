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
import com.naturalhub.system.domain.CommunityComment;
import com.naturalhub.system.service.ICommunityCommentService;
import com.naturalhub.common.utils.poi.ExcelUtil;
import com.naturalhub.common.core.page.TableDataInfo;

/**
 * 社群评论管理Controller
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
@RestController
@RequestMapping("/community/comment")
public class CommunityCommentController extends BaseController
{
    @Autowired
    private ICommunityCommentService communityCommentService;

    /**
     * 查询社群评论列表
     */
    @PreAuthorize("@ss.hasPermi('community:comment:list')")
    @GetMapping("/list")
    public TableDataInfo list(CommunityComment communityComment)
    {
        startPage();
        List<CommunityComment> list = communityCommentService.selectCommunityCommentList(communityComment);
        return getDataTable(list);
    }

    /**
     * 导出社群评论列表
     */
    @PreAuthorize("@ss.hasPermi('community:comment:export')")
    @Log(title = "社群评论", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CommunityComment communityComment)
    {
        List<CommunityComment> list = communityCommentService.selectCommunityCommentList(communityComment);
        ExcelUtil<CommunityComment> util = new ExcelUtil<CommunityComment>(CommunityComment.class);
        util.exportExcel(response, list, "社群评论数据");
    }

    /**
     * 获取社群评论详细信息
     */
    @PreAuthorize("@ss.hasPermi('community:comment:query')")
    @GetMapping(value = "/{commentId}")
    public AjaxResult getInfo(@PathVariable("commentId") Long commentId)
    {
        return success(communityCommentService.selectCommunityCommentByCommentId(commentId));
    }

    /**
     * 删除社群评论
     */
    @PreAuthorize("@ss.hasPermi('community:comment:remove')")
    @Log(title = "社群评论", businessType = BusinessType.DELETE)
	@DeleteMapping("/{commentIds}")
    public AjaxResult remove(@PathVariable Long[] commentIds)
    {
        return toAjax(communityCommentService.deleteCommunityCommentByCommentIds(commentIds));
    }

    /**
     * 屏蔽评论
     */
    @PreAuthorize("@ss.hasPermi('community:comment:edit')")
    @Log(title = "屏蔽评论", businessType = BusinessType.UPDATE)
    @PutMapping("/block/{commentId}")
    public AjaxResult blockComment(@PathVariable Long commentId)
    {
        CommunityComment comment = new CommunityComment();
        comment.setCommentId(commentId);
        comment.setStatus("2"); // 2-已屏蔽
        return toAjax(communityCommentService.updateCommunityComment(comment));
    }

    /**
     * 恢复评论
     */
    @PreAuthorize("@ss.hasPermi('community:comment:edit')")
    @Log(title = "恢复评论", businessType = BusinessType.UPDATE)
    @PutMapping("/restore/{commentId}")
    public AjaxResult restoreComment(@PathVariable Long commentId)
    {
        CommunityComment comment = new CommunityComment();
        comment.setCommentId(commentId);
        comment.setStatus("0"); // 0-正常
        return toAjax(communityCommentService.updateCommunityComment(comment));
    }
}
