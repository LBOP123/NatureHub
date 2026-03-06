package com.naturalhub.system.mapper;

import com.naturalhub.system.domain.QaConversation;
import com.naturalhub.system.domain.QaHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface QaHistoryMapper {
    
    int insertQaHistory(QaHistory qaHistory);
    
    List<QaHistory> selectUserQaHistory(@Param("userId") Long userId);
    
    List<QaHistory> selectQaHistoryList(QaHistory qaHistory);
    
    QaHistory selectQaHistoryById(Long id);
    
    int deleteQaHistoryById(Long id);
    
    int deleteQaHistoryByIds(Long[] ids);
    
    int countTotalQa();
    
    List<Map<String, Object>> selectHotSpecies(@Param("limit") int limit);
    
    List<Map<String, Object>> selectQaStatsByDate(@Param("days") int days);
    
    List<QaConversation> selectUserConversations(@Param("username") String username, @Param("qaType") Integer qaType);
    
    int insertConversation(QaConversation conversation);
    
    List<QaHistory> selectConversationMessages(@Param("conversationId") Long conversationId);
    
    int deleteConversation(@Param("conversationId") Long conversationId);
    
    int deleteConversationMessages(@Param("conversationId") Long conversationId);
}
