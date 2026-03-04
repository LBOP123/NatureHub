package com.naturalhub.system.service;

import com.naturalhub.system.domain.dto.BioRecognitionRequest;
import com.naturalhub.system.domain.dto.BioRecognitionResponse;

/**
 * 生物识别Service接口
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
public interface IBioRecognitionService {
    
    /**
     * 智能识别生物
     * 自动判断是植物、动物还是其他物体
     * 
     * @param request 识别请求
     * @return 识别结果
     * @throws Exception 异常
     */
    BioRecognitionResponse recognize(BioRecognitionRequest request) throws Exception;
}
