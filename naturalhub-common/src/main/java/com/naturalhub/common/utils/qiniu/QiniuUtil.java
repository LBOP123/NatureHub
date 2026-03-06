package com.naturalhub.common.utils.qiniu;

import com.google.gson.Gson;
import com.naturalhub.common.config.QiniuConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 七牛云上传工具类
 * 
 * @author naturalhub
 * @date 2025-03-05
 */
@Component
public class QiniuUtil {
    
    private static final Logger log = LoggerFactory.getLogger(QiniuUtil.class);
    
    @Autowired
    private QiniuConfig qiniuConfig;
    
    /**
     * 上传文件到七牛云
     * 
     * @param file 文件
     * @return 文件访问URL
     * @throws IOException IO异常
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // 获取文件原始名称
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IOException("文件名不能为空");
        }
        
        // 获取文件扩展名
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        
        // 生成唯一文件名：upload/年/月/日/UUID.扩展名
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String datePath = sdf.format(new Date());
        String fileName = "upload/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + extension;
        
        // 上传文件
        return uploadFile(file.getInputStream(), fileName);
    }
    
    /**
     * 上传文件流到七牛云
     * 
     * @param inputStream 文件流
     * @param fileName 文件名（包含路径）
     * @return 文件访问URL
     * @throws IOException IO异常
     */
    public String uploadFile(InputStream inputStream, String fileName) throws IOException {
        // 构造一个带指定Region对象的配置类
        Configuration cfg = new Configuration(getRegion());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        
        // 创建上传管理器
        UploadManager uploadManager = new UploadManager(cfg);
        
        // 生成上传凭证
        Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
        String upToken = auth.uploadToken(qiniuConfig.getBucket());
        
        try {
            // 上传文件
            Response response = uploadManager.put(inputStream, fileName, upToken, null, null);
            
            // 解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            
            // 拼接访问URL
            String url = qiniuConfig.getDomain() + "/" + putRet.key;
            
            log.info("文件上传成功，URL: {}", url);
            return url;
            
        } catch (QiniuException ex) {
            Response r = ex.response;
            log.error("七牛云上传失败: {}", r.toString());
            try {
                log.error("错误详情: {}", r.bodyString());
            } catch (QiniuException ex2) {
                // ignore
            }
            throw new IOException("七牛云上传失败: " + ex.getMessage());
        }
    }
    
    /**
     * 根据配置获取Region
     * 
     * @return Region对象
     */
    private Region getRegion() {
        String zone = qiniuConfig.getZone();
        if (zone == null || zone.isEmpty()) {
            zone = "huadong";
        }
        
        switch (zone.toLowerCase()) {
            case "huadong":
            case "z0":
                return Region.huadong();
            case "huabei":
            case "z1":
                return Region.huabei();
            case "huanan":
            case "z2":
                return Region.huanan();
            case "beimei":
            case "na0":
                return Region.beimei();
            case "xinjiapo":
            case "as0":
                return Region.xinjiapo();
            default:
                log.warn("未知的区域配置: {}，使用默认华东区域", zone);
                return Region.huadong();
        }
    }
    
    /**
     * 删除七牛云文件
     * 
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL中提取文件key
            String key = fileUrl.replace(qiniuConfig.getDomain() + "/", "");
            
            Configuration cfg = new Configuration(getRegion());
            Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
            com.qiniu.storage.BucketManager bucketManager = new com.qiniu.storage.BucketManager(auth, cfg);
            
            bucketManager.delete(qiniuConfig.getBucket(), key);
            log.info("文件删除成功: {}", key);
            return true;
            
        } catch (QiniuException ex) {
            log.error("文件删除失败: {}", ex.getMessage());
            return false;
        }
    }
}
