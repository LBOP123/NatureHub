package com.naturalhub.system.domain.dto;

import java.util.List;

/**
 * 生物识别响应DTO
 * 
 * @author naturalhub
 * @date 2025-03-03
 */
public class BioRecognitionResponse {
    
    /** 识别类型（plant-植物，animal-动物，other-其他） */
    private String type;
    
    /** 识别结果列表 */
    private List<RecognitionResult> results;
    
    /** 日志ID */
    private Long logId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<RecognitionResult> getResults() {
        return results;
    }

    public void setResults(List<RecognitionResult> results) {
        this.results = results;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    /**
     * 识别结果
     */
    public static class RecognitionResult {
        
        /** 名称 */
        private String name;
        
        /** 置信度（0-1） */
        private Double score;
        
        /** 分类（根节点） */
        private String category;
        
        /** 百科信息 */
        private BaikeInfo baikeInfo;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public BaikeInfo getBaikeInfo() {
            return baikeInfo;
        }

        public void setBaikeInfo(BaikeInfo baikeInfo) {
            this.baikeInfo = baikeInfo;
        }
    }
    
    /**
     * 百科信息
     */
    public static class BaikeInfo {
        
        /** 百科URL */
        private String baikeUrl;
        
        /** 图片URL */
        private String imageUrl;
        
        /** 描述 */
        private String description;

        public String getBaikeUrl() {
            return baikeUrl;
        }

        public void setBaikeUrl(String baikeUrl) {
            this.baikeUrl = baikeUrl;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
