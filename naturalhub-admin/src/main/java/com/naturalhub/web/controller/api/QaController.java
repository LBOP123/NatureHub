package com.naturalhub.web.controller.api;

import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.system.domain.QaConversation;
import com.naturalhub.system.domain.QaHistory;
import com.naturalhub.system.service.IQaHistoryService;
import com.naturalhub.common.utils.wiki.WikiRagChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/qa")
public class QaController {
    
    @Autowired
    private IQaHistoryService qaHistoryService;
    
    @PostMapping("/ask")
    public AjaxResult ask(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String question = (String) params.get("question");
            Long conversationId = params.get("conversationId") != null ? 
                Long.valueOf(params.get("conversationId").toString()) : null;
            
            if (question == null || question.trim().isEmpty()) {
                return AjaxResult.error("问题不能为空");
            }
            
            String answer = WikiRagChatUtil.qwen(question);
            
            QaHistory history = new QaHistory();
            history.setConversationId(conversationId);
            history.setQuestion(question);
            history.setAnswer(answer);
            history.setQaType(1);
            
            try {
                history.setUsername(SecurityUtils.getUsername());
            } catch (Exception e) {
                history.setUsername("anonymous");
            }
            history.setIpAddress(getIpAddress(request));
            
            qaHistoryService.saveQaHistory(history);
            
            // 修复：明确指定msg和data
            return AjaxResult.success("查询成功", answer);
        } catch (Exception e) {
            return AjaxResult.error("AI调用失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/knowledge")
    public AjaxResult knowledgeAsk(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String question = (String) params.get("question");
            Long conversationId = params.get("conversationId") != null ? 
                Long.valueOf(params.get("conversationId").toString()) : null;
            
            if (question == null || question.trim().isEmpty()) {
                return AjaxResult.error("问题不能为空");
            }
            
            String answer = WikiRagChatUtil.chat(question);
            String relatedSpecies = extractSpecies(question);
            
            QaHistory history = new QaHistory();
            history.setConversationId(conversationId);
            history.setQuestion(question);
            history.setAnswer(answer);
            history.setQaType(2);
            history.setRelatedSpecies(relatedSpecies);
            
            try {
                history.setUsername(SecurityUtils.getUsername());
            } catch (Exception e) {
                history.setUsername("anonymous");
            }
            history.setIpAddress(getIpAddress(request));
            
            qaHistoryService.saveQaHistory(history);
            
            // 修复：明确指定msg和data
            return AjaxResult.success("查询成功", answer);
        } catch (Exception e) {
            return AjaxResult.error("知识库查询失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/conversations")
    public AjaxResult getConversations(@RequestParam(required = false) Integer qaType) {
        try {
            String username = SecurityUtils.getUsername();
            List<QaConversation> conversations = qaHistoryService.getUserConversations(username, qaType);
            return AjaxResult.success(conversations);
        } catch (Exception e) {
            return AjaxResult.error("查询失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/conversation")
    public AjaxResult createConversation(@RequestBody Map<String, Object> params) {
        try {
            String title = (String) params.get("title");
            Integer qaType = (Integer) params.get("qaType");
            String username = SecurityUtils.getUsername();
            
            Long conversationId = qaHistoryService.createConversation(username, title, qaType);
            return AjaxResult.success(conversationId);
        } catch (Exception e) {
            return AjaxResult.error("创建失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/conversation/{conversationId}/messages")
    public AjaxResult getConversationMessages(@PathVariable Long conversationId) {
        try {
            List<QaHistory> messages = qaHistoryService.getConversationMessages(conversationId);
            return AjaxResult.success(messages);
        } catch (Exception e) {
            return AjaxResult.error("查询失败：" + e.getMessage());
        }
    }
    
    @DeleteMapping("/conversation/{conversationId}")
    public AjaxResult deleteConversation(@PathVariable Long conversationId) {
        try {
            qaHistoryService.deleteConversation(conversationId);
            return AjaxResult.success("删除成功");
        } catch (Exception e) {
            return AjaxResult.error("删除失败：" + e.getMessage());
        }
    }
    
    private String extractSpecies(String question) {
        return question.length() > 20 ? question.substring(0, 20) : question;
    }
    
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
}
