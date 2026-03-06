package com.naturalhub.web.controller.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.common.utils.baidu.Base64Util;
import com.naturalhub.system.domain.BioRecognition;
import com.naturalhub.system.domain.dto.BioRecognitionRequest;
import com.naturalhub.system.domain.dto.BioRecognitionResponse;
import com.naturalhub.system.service.IBioRecognitionService;
import com.naturalhub.system.service.IBioRecognitionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * 生物识别Controller
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
@RestController("bioRecognitionCommonController")
@RequestMapping("/common/bio")
public class BioRecognitionController {
    
    private static final Logger log = LoggerFactory.getLogger(BioRecognitionController.class);
    
    @Autowired
    private IBioRecognitionService bioRecognitionService;
    
    @Autowired
    private IBioRecognitionRecordService bioRecognitionRecordService;

    /**
     * 生物识别（Base64或URL）
     * 
     * @param request 识别请求
     * @return 识别结果
     */
    @PostMapping("/recognize")
    public AjaxResult recognize(@RequestBody BioRecognitionRequest request, HttpServletRequest httpRequest) {
        try {
            log.info("收到生物识别请求");
            log.info("请求参数 - image: {}, url: {}", 
                     request.getImage() != null ? "有值" : "null",
                     request.getUrl() != null ? request.getUrl() : "null");
            
            // 直接调用识别服务，优先使用URL
            BioRecognitionResponse response = bioRecognitionService.recognize(request);
            
            // 保存识别记录
            saveRecognitionRecord(request, response, httpRequest);
            
            return AjaxResult.success("识别成功", response);
        } catch (IllegalArgumentException e) {
            log.error("参数错误: {}", e.getMessage());
            // 保存失败记录
            saveFailedRecord(request, e.getMessage(), httpRequest);
            return AjaxResult.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("识别失败", e);
            // 保存失败记录
            saveFailedRecord(request, e.getMessage(), httpRequest);
            return AjaxResult.error("识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 生物识别（文件上传）
     * 
     * @param file 图片文件
     * @return 识别结果
     */
    @PostMapping("/recognizeFile")
    public AjaxResult recognizeFile(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {
        try {
            log.info("收到文件上传识别请求，文件名: {}", file.getOriginalFilename());
            
            // 验证文件
            if (file.isEmpty()) {
                return AjaxResult.error("文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return AjaxResult.error("只支持图片文件");
            }
            
            // 验证文件大小（4MB）
            if (file.getSize() > 4 * 1024 * 1024) {
                return AjaxResult.error("图片大小不能超过4MB");
            }
            
            // 转换为Base64
            String base64 = Base64Util.encodeStreamToBase64(file.getInputStream());
            log.info("文件转Base64成功，长度: {}", base64.length());
            
            // 调用识别服务
            BioRecognitionRequest request = new BioRecognitionRequest();
            request.setImage(base64);
            
            BioRecognitionResponse response = bioRecognitionService.recognize(request);
            
            // 保存识别记录
            saveRecognitionRecord(request, response, httpRequest);
            
            return AjaxResult.success("识别成功", response);
            
        } catch (Exception e) {
            log.error("文件识别失败", e);
            return AjaxResult.error("识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 快速识别（仅返回最佳结果）
     * 
     * @param request 识别请求
     * @return 最佳识别结果
     */
    @PostMapping("/quickRecognize")
    public AjaxResult quickRecognize(@RequestBody BioRecognitionRequest request, HttpServletRequest httpRequest) {
        try {
            log.info("收到快速识别请求");
            
            BioRecognitionResponse response = bioRecognitionService.recognize(request);
            
            // 保存识别记录
            saveRecognitionRecord(request, response, httpRequest);
            
            // 只返回第一个结果
            if (response.getResults() != null && !response.getResults().isEmpty()) {
                BioRecognitionResponse.RecognitionResult bestResult = response.getResults().get(0);
                AjaxResult result = AjaxResult.success("识别成功");
                result.put("type", response.getType());
                result.put("name", bestResult.getName());
                result.put("score", bestResult.getScore());
                result.put("category", bestResult.getCategory());
                result.put("baikeInfo", bestResult.getBaikeInfo());
                return result;
            } else {
                return AjaxResult.error("未识别到任何内容");
            }
        } catch (Exception e) {
            log.error("快速识别失败", e);
            saveFailedRecord(request, e.getMessage(), httpRequest);
            return AjaxResult.error("识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存识别记录
     */
    private void saveRecognitionRecord(BioRecognitionRequest request, BioRecognitionResponse response, HttpServletRequest httpRequest) {
        try {
            BioRecognition record = new BioRecognition();
            
            // 用户信息
            try {
                record.setUsername(SecurityUtils.getUsername());
                record.setUserId(SecurityUtils.getUserId());
            } catch (Exception e) {
                record.setUsername("anonymous");
            }
            
            // 图片URL
            record.setImageUrl(request.getUrl() != null ? request.getUrl() : "base64");
            
            // 识别结果
            if (response.getResults() != null && !response.getResults().isEmpty()) {
                BioRecognitionResponse.RecognitionResult bestResult = response.getResults().get(0);
                record.setRecognitionResult(bestResult.getName());
                record.setConfidence(BigDecimal.valueOf(bestResult.getScore()));
                
                // 百科信息
                if (bestResult.getBaikeInfo() != null) {
                    record.setBaikeInfo(JSON.toJSONString(bestResult.getBaikeInfo()));
                }
            }
            
            // 识别类型
            record.setRecognitionType(response.getType());
            
            // 所有结果
            if (response.getResults() != null) {
                record.setAllResults(JSON.toJSONString(response.getResults()));
            }
            
            // 状态
            record.setStatus("success");
            
            // IP地址
            record.setIpAddress(getIpAddress(httpRequest));
            
            // 保存
            bioRecognitionRecordService.saveBioRecognition(record);
            log.info("识别记录保存成功，ID: {}", record.getId());
            
        } catch (Exception e) {
            log.error("保存识别记录失败", e);
        }
    }
    
    /**
     * 保存失败记录
     */
    private void saveFailedRecord(BioRecognitionRequest request, String errorMsg, HttpServletRequest httpRequest) {
        try {
            BioRecognition record = new BioRecognition();
            
            // 用户信息
            try {
                record.setUsername(SecurityUtils.getUsername());
                record.setUserId(SecurityUtils.getUserId());
            } catch (Exception e) {
                record.setUsername("anonymous");
            }
            
            // 图片URL
            record.setImageUrl(request.getUrl() != null ? request.getUrl() : "base64");
            
            // 状态和备注
            record.setStatus("fail");
            record.setRemark(errorMsg);
            
            // IP地址
            record.setIpAddress(getIpAddress(httpRequest));
            
            // 保存
            bioRecognitionRecordService.saveBioRecognition(record);
            
        } catch (Exception e) {
            log.error("保存失败记录失败", e);
        }
    }
    
    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * 健康检查
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public AjaxResult health() {
        return AjaxResult.success("生物识别服务运行正常");
    }
}
