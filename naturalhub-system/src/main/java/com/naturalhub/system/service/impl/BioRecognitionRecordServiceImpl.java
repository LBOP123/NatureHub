package com.naturalhub.system.service.impl;

import com.naturalhub.system.domain.BioRecognition;
import com.naturalhub.system.mapper.BioRecognitionMapper;
import com.naturalhub.system.service.IBioRecognitionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生物识别记录Service实现
 */
@Service("bioRecognitionRecordSystemServiceImpl")
public class BioRecognitionRecordServiceImpl implements IBioRecognitionRecordService {
    
    @Autowired
    private BioRecognitionMapper bioRecognitionMapper;
    
    @Override
    public int saveBioRecognition(BioRecognition bioRecognition) {
        return bioRecognitionMapper.insertBioRecognition(bioRecognition);
    }
    
    @Override
    public List<BioRecognition> selectBioRecognitionList(BioRecognition bioRecognition) {
        return bioRecognitionMapper.selectBioRecognitionList(bioRecognition);
    }
    
    @Override
    public BioRecognition selectBioRecognitionById(Long id) {
        return bioRecognitionMapper.selectBioRecognitionById(id);
    }
    
    @Override
    public int deleteBioRecognitionById(Long id) {
        return bioRecognitionMapper.deleteBioRecognitionById(id);
    }
    
    @Override
    public int deleteBioRecognitionByIds(Long[] ids) {
        return bioRecognitionMapper.deleteBioRecognitionByIds(ids);
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 识别总次数
        stats.put("totalRecognition", bioRecognitionMapper.countTotal());
        
        // 今日识别
        stats.put("todayRecognition", bioRecognitionMapper.countToday());
        
        // 成功率
        Map<String, Object> successRate = bioRecognitionMapper.selectSuccessRate();
        if (successRate != null && successRate.get("success_rate") != null) {
            stats.put("successRate", successRate.get("success_rate"));
        } else {
            stats.put("successRate", 0);
        }
        
        // 最常识别物种
        String topSpecies = bioRecognitionMapper.selectTopSpecies();
        stats.put("topSpecies", topSpecies != null ? topSpecies : "-");
        
        // 趋势数据（最近7天）
        stats.put("trendData", bioRecognitionMapper.selectTrendData(7));
        
        // 物种排行（Top10）
        stats.put("speciesRank", bioRecognitionMapper.selectSpeciesRank(10));
        
        return stats;
    }
}
