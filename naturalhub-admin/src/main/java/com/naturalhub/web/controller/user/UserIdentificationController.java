package com.naturalhub.web.controller.user;

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
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.system.domain.SpeciesIdentification;
import com.naturalhub.system.domain.IdentificationAnswer;
import com.naturalhub.system.service.ISpeciesIdentificationService;
import com.naturalhub.system.service.IIdentificationAnswerService;
import com.naturalhub.common.core.page.TableDataInfo;

/**
 * 物种鉴定求助用户端Controller
 * 
 * @author NaturalHub
 * @date 2026-03-09
 */
@RestController
@RequestMapping("/user/identification")
public class UserIdentificationController extends BaseController
{
    @Autowired
    private ISpeciesIdentificationService speciesIdentificationService;

    @Autowired
    private IIdentificationAnswerService identificationAnswerService;

    /**
     * 查询物种鉴定求助列表（我的求助）
     */
    @GetMapping("/list")
    public TableDataInfo list(SpeciesIdentification speciesIdentification)
    {
        startPage();
        // 只查询当前用户的记录
        speciesIdentification.setUserId(SecurityUtils.getUserId());
        List<SpeciesIdentification> list = speciesIdentificationService.selectSpeciesIdentificationList(speciesIdentification);
        return getDataTable(list);
    }

    /**
     * 查询所有鉴定求助列表（广场）
     */
    @GetMapping("/square")
    public TableDataInfo square(SpeciesIdentification speciesIdentification)
    {
        startPage();
        // 只查询审核通过的记录（2=已通过）
        speciesIdentification.setAuditStatus(2);
        List<SpeciesIdentification> list = speciesIdentificationService.selectSpeciesIdentificationList(speciesIdentification);
        return getDataTable(list);
    }

    /**
     * 获取物种鉴定求助详细信息
     */
    @GetMapping(value = "/{identificationId}")
    public AjaxResult getInfo(@PathVariable("identificationId") Long identificationId)
    {
        // 增加浏览次数
        speciesIdentificationService.incrementViewCount(identificationId);
        
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
    @Log(title = "物种鉴定求助", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SpeciesIdentification speciesIdentification)
    {
        speciesIdentification.setUserId(SecurityUtils.getUserId());
        speciesIdentification.setCreateBy(SecurityUtils.getUsername());
        return toAjax(speciesIdentificationService.insertSpeciesIdentification(speciesIdentification));
    }

    /**
     * 修改物种鉴定求助
     */
    @Log(title = "物种鉴定求助", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SpeciesIdentification speciesIdentification)
    {
        // 验证是否是本人的记录
        SpeciesIdentification original = speciesIdentificationService.selectSpeciesIdentificationByIdentificationId(speciesIdentification.getIdentificationId());
        if (original == null || !original.getUserId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("无权修改此记录");
        }
        
        speciesIdentification.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(speciesIdentificationService.updateSpeciesIdentification(speciesIdentification));
    }

    /**
     * 删除物种鉴定求助
     */
    @Log(title = "物种鉴定求助", businessType = BusinessType.DELETE)
	@DeleteMapping("/{identificationIds}")
    public AjaxResult remove(@PathVariable Long[] identificationIds)
    {
        // 验证是否是本人的记录
        for (Long id : identificationIds) {
            SpeciesIdentification identification = speciesIdentificationService.selectSpeciesIdentificationByIdentificationId(id);
            if (identification == null || !identification.getUserId().equals(SecurityUtils.getUserId())) {
                return AjaxResult.error("无权删除此记录");
            }
        }
        
        return toAjax(speciesIdentificationService.deleteSpeciesIdentificationByIdentificationIds(identificationIds));
    }

    /**
     * 提交回答
     */
    @Log(title = "提交鉴定回答", businessType = BusinessType.INSERT)
    @PostMapping("/{identificationId}/answer")
    public AjaxResult submitAnswer(@PathVariable Long identificationId, @RequestBody IdentificationAnswer answer)
    {
        // 验证鉴定记录是否存在
        SpeciesIdentification identification = speciesIdentificationService.selectSpeciesIdentificationByIdentificationId(identificationId);
        if (identification == null) {
            return AjaxResult.error("鉴定记录不存在");
        }
        
        if (!Integer.valueOf(2).equals(identification.getAuditStatus())) {
            return AjaxResult.error("该鉴定求助尚未审核通过");
        }
        
        answer.setIdentificationId(identificationId);
        answer.setUserId(SecurityUtils.getUserId());
        answer.setUserName(SecurityUtils.getUsername());
        
        return toAjax(identificationAnswerService.insertIdentificationAnswer(answer));
    }

    /**
     * 采纳最佳答案
     */
    @Log(title = "采纳最佳答案", businessType = BusinessType.UPDATE)
    @PutMapping("/{identificationId}/best/{answerId}")
    public AjaxResult setBestAnswer(@PathVariable Long identificationId, @PathVariable Long answerId)
    {
        // 验证是否是本人的记录
        SpeciesIdentification identification = speciesIdentificationService.selectSpeciesIdentificationByIdentificationId(identificationId);
        if (identification == null || !identification.getUserId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("只有提问者才能采纳最佳答案");
        }
        
        return toAjax(speciesIdentificationService.setBestAnswer(identificationId, answerId));
    }

    /**
     * 点赞回答
     */
    @Log(title = "点赞回答", businessType = BusinessType.UPDATE)
    @PostMapping("/answer/{answerId}/like")
    public AjaxResult likeAnswer(@PathVariable Long answerId)
    {
        return toAjax(identificationAnswerService.likeAnswer(answerId));
    }

    /**
     * 分享到社群
     */
    @Log(title = "分享鉴定求助到社群", businessType = BusinessType.UPDATE)
    @PostMapping("/{identificationId}/share")
    public AjaxResult shareToCommunity(@PathVariable Long identificationId)
    {
        try {
            // 验证是否是本人的记录
            SpeciesIdentification identification = speciesIdentificationService.selectSpeciesIdentificationByIdentificationId(identificationId);
            if (identification == null || !identification.getUserId().equals(SecurityUtils.getUserId())) {
                return AjaxResult.error("只能分享自己的记录");
            }
            
            Long topicId = speciesIdentificationService.shareToCommunity(identificationId);
            return AjaxResult.success("分享成功", topicId);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 修改回答
     */
    @Log(title = "修改鉴定回答", businessType = BusinessType.UPDATE)
    @PutMapping("/answer")
    public AjaxResult editAnswer(@RequestBody IdentificationAnswer answer)
    {
        // 验证是否是本人的回答
        IdentificationAnswer original = identificationAnswerService.selectIdentificationAnswerByAnswerId(answer.getAnswerId());
        if (original == null || !original.getUserId().equals(SecurityUtils.getUserId())) {
            return AjaxResult.error("无权修改此回答");
        }
        
        return toAjax(identificationAnswerService.updateIdentificationAnswer(answer));
    }

    /**
     * 删除回答
     */
    @Log(title = "删除鉴定回答", businessType = BusinessType.DELETE)
    @DeleteMapping("/answer/{answerIds}")
    public AjaxResult removeAnswer(@PathVariable Long[] answerIds)
    {
        // 验证是否是本人的回答
        for (Long id : answerIds) {
            IdentificationAnswer answer = identificationAnswerService.selectIdentificationAnswerByAnswerId(id);
            if (answer == null || !answer.getUserId().equals(SecurityUtils.getUserId())) {
                return AjaxResult.error("无权删除此回答");
            }
        }
        
        return toAjax(identificationAnswerService.deleteIdentificationAnswerByAnswerIds(answerIds));
    }
}
