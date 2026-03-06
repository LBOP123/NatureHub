package com.naturalhub.system.mapper;

import com.naturalhub.system.domain.BioRecognition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface BioRecognitionMapper {
    
    int insertBioRecognition(BioRecognition bioRecognition);
    
    List<BioRecognition> selectBioRecognitionList(BioRecognition bioRecognition);
    
    BioRecognition selectBioRecognitionById(Long id);
    
    int deleteBioRecognitionById(Long id);
    
    int deleteBioRecognitionByIds(Long[] ids);
    
    int countTotal();
    
    int countToday();
    
    Map<String, Object> selectSuccessRate();
    
    String selectTopSpecies();
    
    List<Map<String, Object>> selectTrendData(@Param("days") int days);
    
    List<Map<String, Object>> selectSpeciesRank(@Param("limit") int limit);
}
