package com.naturalhub.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.system.mapper.IdentificationAnswerMapper;
import com.naturalhub.system.mapper.SpeciesIdentificationMapper;
import com.naturalhub.system.domain.IdentificationAnswer;
import com.naturalhub.system.service.IIdentificationAnswerService;

/**
 * 鉴定回答Service业务层处理
 * 
 * @author NaturalHub
 * @date 2026-03-09
 */
@Service
public class IdentificationAnswerServiceImpl implements IIdentificationAnswerService 
{
    @Autowired
    private IdentificationAnswerMapper identificationAnswerMapper;

    @Autowired
    private SpeciesIdentificationMapper speciesIdentificationMapper;

    /**
     * 查询鉴定回答
     * 
     * @param answerId 鉴定回答主键
     * @return 鉴定回答
     */
    @Override
    public IdentificationAnswer selectIdentificationAnswerByAnswerId(Long answerId)
    {
        return identificationAnswerMapper.selectIdentificationAnswerByAnswerId(answerId);
    }

    /**
     * 查询鉴定回答列表
     * 
     * @param identificationAnswer 鉴定回答
     * @return 鉴定回答
     */
    @Override
    public List<IdentificationAnswer> selectIdentificationAnswerList(IdentificationAnswer identificationAnswer)
    {
        return identificationAnswerMapper.selectIdentificationAnswerList(identificationAnswer);
    }

    /**
     * 根据鉴定ID查询回答列表
     * 
     * @param identificationId 鉴定ID
     * @return 鉴定回答集合
     */
    @Override
    public List<IdentificationAnswer> selectAnswersByIdentificationId(Long identificationId)
    {
        return identificationAnswerMapper.selectAnswersByIdentificationId(identificationId);
    }

    /**
     * 新增鉴定回答
     * 
     * @param identificationAnswer 鉴定回答
     * @return 结果
     */
    @Override
    @Transactional
    public int insertIdentificationAnswer(IdentificationAnswer identificationAnswer)
    {
        identificationAnswer.setCreateTime(new Date());
        identificationAnswer.setDelFlag("0");
        identificationAnswer.setLikeCount(0);
        identificationAnswer.setIsBest(0);
        
        // 插入回答
        int result = identificationAnswerMapper.insertIdentificationAnswer(identificationAnswer);
        
        // 更新鉴定记录的回答数量和状态
        if (result > 0) {
            speciesIdentificationMapper.updateAnswerCount(identificationAnswer.getIdentificationId());
            // 如果是第一次回答，更新状态为已回答
            identificationAnswerMapper.selectAnswersByIdentificationId(identificationAnswer.getIdentificationId());
        }
        
        return result;
    }

    /**
     * 修改鉴定回答
     * 
     * @param identificationAnswer 鉴定回答
     * @return 结果
     */
    @Override
    public int updateIdentificationAnswer(IdentificationAnswer identificationAnswer)
    {
        identificationAnswer.setUpdateTime(new Date());
        return identificationAnswerMapper.updateIdentificationAnswer(identificationAnswer);
    }

    /**
     * 批量删除鉴定回答
     * 
     * @param answerIds 需要删除的鉴定回答主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteIdentificationAnswerByAnswerIds(Long[] answerIds)
    {
        int result = identificationAnswerMapper.deleteIdentificationAnswerByAnswerIds(answerIds);
        
        // 更新相关鉴定记录的回答数量
        if (result > 0 && answerIds.length > 0) {
            IdentificationAnswer answer = identificationAnswerMapper.selectIdentificationAnswerByAnswerId(answerIds[0]);
            if (answer != null) {
                speciesIdentificationMapper.updateAnswerCount(answer.getIdentificationId());
            }
        }
        
        return result;
    }

    /**
     * 删除鉴定回答信息
     * 
     * @param answerId 鉴定回答主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteIdentificationAnswerByAnswerId(Long answerId)
    {
        IdentificationAnswer answer = identificationAnswerMapper.selectIdentificationAnswerByAnswerId(answerId);
        int result = identificationAnswerMapper.deleteIdentificationAnswerByAnswerId(answerId);
        
        // 更新鉴定记录的回答数量
        if (result > 0 && answer != null) {
            speciesIdentificationMapper.updateAnswerCount(answer.getIdentificationId());
        }
        
        return result;
    }

    /**
     * 点赞回答
     * 
     * @param answerId 答案ID
     * @return 结果
     */
    @Override
    public int likeAnswer(Long answerId)
    {
        return identificationAnswerMapper.incrementLikeCount(answerId);
    }
}
