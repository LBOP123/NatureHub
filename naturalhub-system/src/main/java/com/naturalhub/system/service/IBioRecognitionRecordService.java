package com.naturalhub.system.service;

import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.system.domain.BioRecognition;
import java.util.List;
import java.util.Map;

/**
 * 生物识别记录Service接口
 */
public interface IBioRecognitionRecordService {
    
    /**
     * 保存识别记录
     */
    int saveBioRecognition(BioRecognition bioRecognition);
    
    /**
     * 查询识别记录列表
     */
    List<BioRecognition> selectBioRecognitionList(BioRecognition bioRecognition);
    
    /**
     * 根据ID查询识别记录
     */
    BioRecognition selectBioRecognitionById(Long id);
    
    /**
     * 删除识别记录
     */
    int deleteBioRecognitionById(Long id);
    
    /**
     * 批量删除识别记录
     */
    int deleteBioRecognitionByIds(Long[] ids);
    
    /**
     * 获取统计数据
     */
    Map<String, Object> getStatistics();

    /**
     * 获取用户识别历史记录（分页）
     */
    AjaxResult getUserRecognitionHistory(String username, Integer pageNum, Integer pageSize);
}
