package com.naturalhub.system.service;

import java.util.List;
import com.naturalhub.system.domain.CommunityCategory;

/**
 * 社群板块分类Service接口
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public interface ICommunityCategoryService 
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
     * 批量删除社群板块分类
     * 
     * @param categoryIds 需要删除的社群板块分类主键集合
     * @return 结果
     */
    public int deleteCommunityCategoryByCategoryIds(Long[] categoryIds);

    /**
     * 删除社群板块分类信息
     * 
     * @param categoryId 社群板块分类主键
     * @return 结果
     */
    public int deleteCommunityCategoryByCategoryId(Long categoryId);
}
