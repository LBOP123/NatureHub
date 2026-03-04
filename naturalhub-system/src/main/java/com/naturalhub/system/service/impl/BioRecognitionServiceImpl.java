package com.naturalhub.system.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.naturalhub.common.utils.baidu.BaiduAiUtil;
import com.naturalhub.system.domain.dto.BioRecognitionRequest;
import com.naturalhub.system.domain.dto.BioRecognitionResponse;
import com.naturalhub.system.domain.dto.BioRecognitionResponse.BaikeInfo;
import com.naturalhub.system.domain.dto.BioRecognitionResponse.RecognitionResult;
import com.naturalhub.system.service.IBioRecognitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 生物识别Service实现类
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
@Service
public class BioRecognitionServiceImpl implements IBioRecognitionService {
    
    private static final Logger log = LoggerFactory.getLogger(BioRecognitionServiceImpl.class);
    
    @Autowired
    private BaiduAiUtil baiduAiUtil;
    
    /** 植物相关关键词 */
    private static final List<String> PLANT_KEYWORDS = Arrays.asList(
        "植物", "花", "树", "草", "叶", "果", "种子", "根", "茎",
        "plant", "flower", "tree", "grass", "leaf", "fruit", "seed"
    );
    
    /** 动物相关关键词 */
    private static final List<String> ANIMAL_KEYWORDS = Arrays.asList(
        "动物", "鸟", "鱼", "昆虫", "哺乳", "爬行", "两栖",
        "animal", "bird", "fish", "insect", "mammal", "reptile"
    );
    
    @Override
    public BioRecognitionResponse recognize(BioRecognitionRequest request) throws Exception {
        log.info("开始生物识别，请求参数: {}", request);
        
        // 参数校验
        if ((request.getImage() == null || request.getImage().isEmpty()) 
            && (request.getUrl() == null || request.getUrl().isEmpty())) {
            throw new IllegalArgumentException("图片Base64和URL不能同时为空");
        }
        
        // 第一步：通用物体识别，判断类型
        log.info("步骤1: 调用通用物体识别接口");
        JSONObject generalResult = baiduAiUtil.generalRecognition(request.getImage(), request.getUrl());
        
        String type = determineType(generalResult);
        log.info("识别类型: {}", type);
        
        BioRecognitionResponse response = new BioRecognitionResponse();
        response.setType(type);
        response.setLogId(generalResult.getLong("log_id"));
        
        // 第二步：根据类型调用专业识别接口
        if ("plant".equals(type)) {
            log.info("步骤2: 调用植物识别接口");
            JSONObject plantResult = baiduAiUtil.plantRecognition(request.getImage(), request.getUrl());
            response.setResults(parsePlantResult(plantResult));
        } else if ("animal".equals(type)) {
            log.info("步骤2: 调用动物识别接口");
            JSONObject animalResult = baiduAiUtil.animalRecognition(request.getImage(), request.getUrl());
            response.setResults(parseAnimalResult(animalResult));
        } else {
            log.info("步骤2: 使用通用识别结果");
            response.setResults(parseGeneralResult(generalResult));
        }
        
        log.info("识别完成，结果数量: {}", response.getResults().size());
        return response;
    }
    
    /**
     * 判断识别类型
     * 
     * @param generalResult 通用识别结果
     * @return 类型（plant/animal/other）
     */
    private String determineType(JSONObject generalResult) {
        JSONArray results = generalResult.getJSONArray("result");
        if (results == null || results.isEmpty()) {
            return "other";
        }
        
        // 获取第一个结果
        JSONObject firstResult = results.getJSONObject(0);
        String keyword = firstResult.getString("keyword");
        String root = firstResult.getString("root");
        
        // 判断是否为植物
        for (String plantKeyword : PLANT_KEYWORDS) {
            if (keyword.contains(plantKeyword) || root.contains(plantKeyword)) {
                return "plant";
            }
        }
        
        // 判断是否为动物
        for (String animalKeyword : ANIMAL_KEYWORDS) {
            if (keyword.contains(animalKeyword) || root.contains(animalKeyword)) {
                return "animal";
            }
        }
        
        return "other";
    }
    
    /**
     * 解析植物识别结果
     * 
     * @param plantResult 植物识别结果
     * @return 识别结果列表
     */
    private List<RecognitionResult> parsePlantResult(JSONObject plantResult) {
        List<RecognitionResult> results = new ArrayList<>();
        JSONArray resultArray = plantResult.getJSONArray("result");
        
        if (resultArray != null) {
            for (int i = 0; i < resultArray.size(); i++) {
                JSONObject item = resultArray.getJSONObject(i);
                RecognitionResult result = new RecognitionResult();
                result.setName(item.getString("name"));
                result.setScore(item.getDouble("score"));
                result.setCategory("植物");
                
                // 解析百科信息
                JSONObject baikeInfo = item.getJSONObject("baike_info");
                if (baikeInfo != null) {
                    BaikeInfo baike = new BaikeInfo();
                    baike.setBaikeUrl(baikeInfo.getString("baike_url"));
                    baike.setImageUrl(baikeInfo.getString("image_url"));
                    baike.setDescription(baikeInfo.getString("description"));
                    result.setBaikeInfo(baike);
                }
                
                results.add(result);
            }
        }
        
        return results;
    }
    
    /**
     * 解析动物识别结果
     * 
     * @param animalResult 动物识别结果
     * @return 识别结果列表
     */
    private List<RecognitionResult> parseAnimalResult(JSONObject animalResult) {
        List<RecognitionResult> results = new ArrayList<>();
        JSONArray resultArray = animalResult.getJSONArray("result");
        
        if (resultArray != null) {
            for (int i = 0; i < resultArray.size(); i++) {
                JSONObject item = resultArray.getJSONObject(i);
                RecognitionResult result = new RecognitionResult();
                result.setName(item.getString("name"));
                result.setScore(item.getDouble("score"));
                result.setCategory("动物");
                
                // 解析百科信息
                JSONObject baikeInfo = item.getJSONObject("baike_info");
                if (baikeInfo != null) {
                    BaikeInfo baike = new BaikeInfo();
                    baike.setBaikeUrl(baikeInfo.getString("baike_url"));
                    baike.setImageUrl(baikeInfo.getString("image_url"));
                    baike.setDescription(baikeInfo.getString("description"));
                    result.setBaikeInfo(baike);
                }
                
                results.add(result);
            }
        }
        
        return results;
    }
    
    /**
     * 解析通用识别结果
     * 
     * @param generalResult 通用识别结果
     * @return 识别结果列表
     */
    private List<RecognitionResult> parseGeneralResult(JSONObject generalResult) {
        List<RecognitionResult> results = new ArrayList<>();
        JSONArray resultArray = generalResult.getJSONArray("result");
        
        if (resultArray != null) {
            for (int i = 0; i < resultArray.size() && i < 3; i++) {
                JSONObject item = resultArray.getJSONObject(i);
                RecognitionResult result = new RecognitionResult();
                result.setName(item.getString("keyword"));
                result.setScore(item.getDouble("score"));
                result.setCategory(item.getString("root"));
                
                // 解析百科信息
                JSONObject baikeInfo = item.getJSONObject("baike_info");
                if (baikeInfo != null) {
                    BaikeInfo baike = new BaikeInfo();
                    baike.setBaikeUrl(baikeInfo.getString("baike_url"));
                    baike.setImageUrl(baikeInfo.getString("image_url"));
                    baike.setDescription(baikeInfo.getString("description"));
                    result.setBaikeInfo(baike);
                }
                
                results.add(result);
            }
        }
        
        return results;
    }
}
