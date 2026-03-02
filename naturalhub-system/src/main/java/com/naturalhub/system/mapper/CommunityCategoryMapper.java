package com.naturalhub.system.mapper;

import java.util.List;
import com.naturalhub.system.domain.CommunityCategory;

/**
 * 社群板块分类Mapper接口
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public interface CommunityCategoryMapper 
{
    /**
     * 查询社群板块分类
     * 
     * @param categoryId 社群板块分类主键
     * @return 社群板块分类
     */
    public CommunityCategory selectCommunityCategoryByCategoryId(Long categoryId);

    /**
     * 根据板块代码查询
     * 
     * @param categoryCode 板块代码
     * @return 社群板块分类
     */
    public CommunityCategory selectCommunityCategoryByCode(String categoryCode);

    /**
     * 查询社群板块分类列表
     * 
     * @param communityCategory 社群板块分类
     * @return 社群板块分类集合
     */
    public List<CommunityCategory> selectCommunityCategoryList(CommunityCategory communityCategory);

    /**
     * 新增社群板块分类
     * 
     * @param communityCategory 社群板块分类
     * @return 结果
     */
    public int insertCommunityCategory(CommunityCategory communityCategory);

    /**
     * 修改社群板块分类
     * 
     * @param communityCategory 社群板块分类
     * @return 结果
     */
    public int updateCommunityCategory(CommunityCategory communityCategory);

    /**
     * 删除社群板块分类
     * 
     * @param categoryId 社群板块分类主键
     * @return 结果
     */
    public int deleteCommunityCategoryByCategoryId(Long categoryId);

    /**
     * 批量删除社群板块分类
     * 
     * @param categoryIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCommunityCategoryByCategoryIds(Long[] categoryIds);
}
