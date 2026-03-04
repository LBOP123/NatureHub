package com.naturalhub.web.controller.common;

import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.system.domain.dto.BioRecognitionRequest;
import com.naturalhub.system.domain.dto.BioRecognitionResponse;
import com.naturalhub.system.service.IBioRecognitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.naturalhub.common.utils.baidu.Base64Util;

import java.io.File;
import java.io.FileInputStream;

/**
 * 生物识别Controller
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
@RestController
@RequestMapping("/common/bio")
public class BioRecognitionController {
    
    private static final Logger log = LoggerFactory.getLogger(BioRecognitionController.class);
    
    @Autowired
    private IBioRecognitionService bioRecognitionService;
    
    @Value("${naturalhub.profile:D:/naturalhub/uploadPath}")
    private String uploadPath;
    
    /**
     * 生物识别（Base64或URL）
     * 
     * @param request 识别请求
     * @return 识别结果
     */
    @PostMapping("/recognize")
    public AjaxResult recognize(@RequestBody BioRecognitionRequest request) {
        try {
            log.info("收到生物识别请求");
            log.info("请求参数 - image: {}, url: {}", 
                     request.getImage() != null ? "有值" : "null",
                     request.getUrl() != null ? request.getUrl() : "null");
            
            // 如果是URL，转换为Base64
            if (request.getUrl() != null && !request.getUrl().isEmpty()) {
                String imageBase64 = convertUrlToBase64(request.getUrl());
                if (imageBase64 != null) {
                    log.info("URL转Base64成功，长度: {}", imageBase64.length());
                    request.setImage(imageBase64);
                    request.setUrl(null); // 清空URL，使用Base64
                }
            }
            
            BioRecognitionResponse response = bioRecognitionService.recognize(request);
            return AjaxResult.success("识别成功", response);
        } catch (IllegalArgumentException e) {
            log.error("参数错误: {}", e.getMessage());
            return AjaxResult.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("识别失败", e);
            return AjaxResult.error("识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 将图片URL转换为Base64
     * 支持本地路径和HTTP URL
     * 
     * @param url 图片URL
     * @return Base64字符串
     */
    private String convertUrlToBase64(String url) {
        try {
            log.info("开始转换URL为Base64: {}", url);
            
            // 如果是本地路径（相对路径或绝对路径）
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                // 处理相对路径
                String filePath = url;
                if (url.startsWith("/profile")) {
                    // 若依框架的上传路径
                    filePath = uploadPath + url.replace("/profile", "");
                } else if (!new File(url).isAbsolute()) {
                    // 相对路径，拼接上传目录
                    filePath = uploadPath + File.separator + url;
                }
                
                log.info("本地文件路径: {}", filePath);
                
                File file = new File(filePath);
                if (!file.exists()) {
                    log.error("文件不存在: {}", filePath);
                    return null;
                }
                
                // 读取文件并转Base64
                try (FileInputStream fis = new FileInputStream(file)) {
                    String base64 = Base64Util.encodeStreamToBase64(fis);
                    log.info("本地文件转Base64成功");
                    return base64;
                }
            } else {
                // HTTP URL，使用Base64Util的URL转换方法
                log.info("HTTP URL，使用URL转Base64");
                return Base64Util.encodeUrlToBase64(url);
            }
        } catch (Exception e) {
            log.error("URL转Base64失败", e);
            return null;
        }
    }
    
    /**
     * 生物识别（文件上传）
     * 
     * @param file 图片文件
     * @return 识别结果
     */
    @PostMapping("/recognizeFile")
    public AjaxResult recognizeFile(@RequestParam("file") MultipartFile file) {
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
    public AjaxResult quickRecognize(@RequestBody BioRecognitionRequest request) {
        try {
            log.info("收到快速识别请求");
            
            // 如果是URL，转换为Base64
            if (request.getUrl() != null && !request.getUrl().isEmpty()) {
                String imageBase64 = convertUrlToBase64(request.getUrl());
                if (imageBase64 != null) {
                    request.setImage(imageBase64);
                    request.setUrl(null);
                }
            }
            
            BioRecognitionResponse response = bioRecognitionService.recognize(request);
            
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
            return AjaxResult.error("识别失败: " + e.getMessage());
        }
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
