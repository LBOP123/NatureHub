package com.naturalhub.system.service;

import com.naturalhub.system.domain.QaConversation;
import com.naturalhub.system.domain.QaHistory;
import java.util.List;
import java.util.Map;

public interface IQaHistoryService {
    
    int saveQaHistory(QaHistory qaHistory);
    
    List<QaHistory> getUserQaHistory(Long userId);
    
    List<QaHistory> selectQaHistoryList(QaHistory qaHistory);
    
    QaHistory selectQaHistoryById(Long id);
    
    int deleteQaHistoryById(Long id);
    
    int deleteQaHistoryByIds(Long[] ids);
    
    Map<String, Object> getStatistics();
    
    List<QaConversation> getUserConversations(String username, Integer qaType);
    
    Long createConversation(String username, String title, Integer qaType);
    
    List<QaHistory> getConversationMessages(Long conversationId);
    
    int deleteConversation(Long conversationId);
}
