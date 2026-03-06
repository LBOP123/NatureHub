package com.naturalhub.system.service.impl;

import com.naturalhub.system.domain.QaConversation;
import com.naturalhub.system.domain.QaHistory;
import com.naturalhub.system.mapper.QaHistoryMapper;
import com.naturalhub.system.service.IQaHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("qaHistorySystemServiceImpl")
public class QaHistoryServiceImpl implements IQaHistoryService {
    
    @Autowired
    private QaHistoryMapper qaHistoryMapper;
    
    @Override
    public int saveQaHistory(QaHistory qaHistory) {
        return qaHistoryMapper.insertQaHistory(qaHistory);
    }
    
    @Override
    public List<QaHistory> getUserQaHistory(Long userId) {
        return qaHistoryMapper.selectUserQaHistory(userId);
    }
    
    @Override
    public List<QaHistory> selectQaHistoryList(QaHistory qaHistory) {
        return qaHistoryMapper.selectQaHistoryList(qaHistory);
    }
    
    @Override
    public QaHistory selectQaHistoryById(Long id) {
        return qaHistoryMapper.selectQaHistoryById(id);
    }
    
    @Override
    public int deleteQaHistoryById(Long id) {
        return qaHistoryMapper.deleteQaHistoryById(id);
    }
    
    @Override
    public int deleteQaHistoryByIds(Long[] ids) {
        return qaHistoryMapper.deleteQaHistoryByIds(ids);
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQa", qaHistoryMapper.countTotalQa());
        stats.put("hotSpecies", qaHistoryMapper.selectHotSpecies(10));
        stats.put("qaStatsByDate", qaHistoryMapper.selectQaStatsByDate(7));
        return stats;
    }
    
    @Override
    public List<QaConversation> getUserConversations(String username, Integer qaType) {
        return qaHistoryMapper.selectUserConversations(username, qaType);
    }
    
    @Override
    public Long createConversation(String username, String title, Integer qaType) {
        QaConversation conversation = new QaConversation();
        conversation.setUsername(username);
        conversation.setTitle(title);
        conversation.setQaType(qaType);
        qaHistoryMapper.insertConversation(conversation);
        return conversation.getId();
    }
    
    @Override
    public List<QaHistory> getConversationMessages(Long conversationId) {
        return qaHistoryMapper.selectConversationMessages(conversationId);
    }
    
    @Override
    public int deleteConversation(Long conversationId) {
        qaHistoryMapper.deleteConversationMessages(conversationId);
        return qaHistoryMapper.deleteConversation(conversationId);
    }
}
