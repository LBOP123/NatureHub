package com.naturalhub.system.mapper;

import java.util.List;
import com.naturalhub.system.domain.IdentificationAnswer;
import org.apache.ibatis.annotations.Param;

/**
 * 鉴定回答Mapper接口
 * 
 * @author NaturalHub
 * @date 2026-03-09
 */
public interface IdentificationAnswerMapper 
{
    /**
     * 查询鉴定回答
     * 
     * @param answerId 鉴定回答主键
     * @return 鉴定回答
     */
    public IdentificationAnswer selectIdentificationAnswerByAnswerId(Long answerId);

    /**
     * 查询鉴定回答列表
     * 
     * @param identificationAnswer 鉴定回答
     * @return 鉴定回答集合
     */
    public List<IdentificationAnswer> selectIdentificationAnswerList(IdentificationAnswer identificationAnswer);

    /**
     * 根据鉴定ID查询回答列表
     * 
     * @param identificationId 鉴定ID
     * @return 鉴定回答集合
     */
    public List<IdentificationAnswer> selectAnswersByIdentificationId(Long identificationId);

    /**
     * 新增鉴定回答
     * 
     * @param identificationAnswer 鉴定回答
     * @return 结果
     */
    public int insertIdentificationAnswer(IdentificationAnswer identificationAnswer);

    /**
     * 修改鉴定回答
     * 
     * @param identificationAnswer 鉴定回答
     * @return 结果
     */
    public int updateIdentificationAnswer(IdentificationAnswer identificationAnswer);

    /**
     * 删除鉴定回答
     * 
     * @param answerId 鉴定回答主键
     * @return 结果
     */
    public int deleteIdentificationAnswerByAnswerId(Long answerId);

    /**
     * 批量删除鉴定回答
     * 
     * @param answerIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteIdentificationAnswerByAnswerIds(Long[] answerIds);

    /**
     * 设置最佳答案
     * 
     * @param identificationId 鉴定ID
     * @param answerId 答案ID
     * @return 结果
     */
    public int setBestAnswer(@Param("identificationId") Long identificationId, @Param("answerId") Long answerId);

    /**
     * 取消最佳答案
     * 
     * @param identificationId 鉴定ID
     * @return 结果
     */
    public int cancelBestAnswer(Long identificationId);

    /**
     * 增加点赞数
     * 
     * @param answerId 答案ID
     * @return 结果
     */
    public int incrementLikeCount(Long answerId);
}
