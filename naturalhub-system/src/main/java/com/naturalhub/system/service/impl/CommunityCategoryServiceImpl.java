package com.naturalhub.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.naturalhub.system.mapper.CommunityCategoryMapper;
import com.naturalhub.system.domain.CommunityCategory;
import com.naturalhub.system.service.ICommunityCategoryService;

/**
 * 社群板块分类Service业务层处理
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
@Service
public class CommunityCategoryServiceImpl implements ICommunityCategoryService 
{
    @Autowired
    private CommunityCategoryMapper communityCategoryMapper;

    /**
     * 查询社群板块分类
     * 
     * @param categoryId 社群板块分类主键
     * @return 社群板块分类
     */
    @Override
    public CommunityCategory selectCommunityCategoryByCategoryId(Long categoryId)
    {
        return communityCategoryMapper.selectCommunityCategoryByCategoryId(categoryId);
    }

    /**
     * 根据板块代码查询
     * 
     * @param categoryCode 板块代码
     * @return 社群板块分类
     */
    @Override
    public CommunityCategory selectCommunityCategoryByCode(String categoryCode)
    {
        return communityCategoryMapper.selectCommunityCategoryByCode(categoryCode);
    }

    /**
     * 查询社群板块分类列表
     * 
     * @param communityCategory 社群板块分类
     * @return 社群板块分类
     */
    @Override
    public List<CommunityCategory> selectCommunityCategoryList(CommunityCategory communityCategory)
    {
        return communityCategoryMapper.selectCommunityCategoryList(communityCategory);
    }

    /**
     * 新增社群板块分类
     * 
     * @param communityCategory 社群板块分类
     * @return 结果
     */
    @Override
    public int insertCommunityCategory(CommunityCategory communityCategory)
    {
        return communityCategoryMapper.insertCommunityCategory(communityCategory);
    }

    /**
     * 修改社群板块分类
     * 
     * @param communityCategory 社群板块分类
     * @return 结果
     */
    @Override
    public int updateCommunityCategory(CommunityCategory communityCategory)
    {
        return communityCategoryMapper.updateCommunityCategory(communityCategory);
    }

    /**
     * 批量删除社群板块分类
     * 
     * @param categoryIds 需要删除的社群板块分类主键
     * @return 结果
     */
    @Override
    public int deleteCommunityCategoryByCategoryIds(Long[] categoryIds)
    {
        return communityCategoryMapper.deleteCommunityCategoryByCategoryIds(categoryIds);
    }

    /**
     * 删除社群板块分类信息
     * 
     * @param categoryId 社群板块分类主键
     * @return 结果
     */
    @Override
    public int deleteCommunityCategoryByCategoryId(Long categoryId)
    {
        return communityCategoryMapper.deleteCommunityCategoryByCategoryId(categoryId);
    }
}
