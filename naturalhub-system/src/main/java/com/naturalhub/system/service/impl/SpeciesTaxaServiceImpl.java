package com.naturalhub.system.service.impl;

import java.util.List;
import com.naturalhub.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.naturalhub.system.mapper.SpeciesTaxaMapper;
import com.naturalhub.system.domain.SpeciesTaxa;
import com.naturalhub.system.service.ISpeciesTaxaService;

/**
 * 物种分类基础Service业务层处理
 * 
 * @author Kairos
 * @date 2025-09-08
 */
@Service
public class SpeciesTaxaServiceImpl implements ISpeciesTaxaService 
{
    @Autowired
    private SpeciesTaxaMapper speciesTaxaMapper;

    /**
     * 查询物种分类基础
     * 
     * @param taxonId 物种分类基础主键
     * @return 物种分类基础
     */
    @Override
    public SpeciesTaxa selectSpeciesTaxaByTaxonId(Long taxonId)
    {
        return speciesTaxaMapper.selectSpeciesTaxaByTaxonId(taxonId);
    }

    /**
     * 查询物种分类基础列表
     * 
     * @param speciesTaxa 物种分类基础
     * @return 物种分类基础
     */
    @Override
    public List<SpeciesTaxa> selectSpeciesTaxaList(SpeciesTaxa speciesTaxa)
    {
        return speciesTaxaMapper.selectSpeciesTaxaList(speciesTaxa);
    }

    /**
     * 新增物种分类基础
     * 
     * @param speciesTaxa 物种分类基础
     * @return 结果
     */
    @Override
    public int insertSpeciesTaxa(SpeciesTaxa speciesTaxa)
    {
        speciesTaxa.setCreateTime(DateUtils.getNowDate());
        return speciesTaxaMapper.insertSpeciesTaxa(speciesTaxa);
    }

    /**
     * 修改物种分类基础
     * 
     * @param speciesTaxa 物种分类基础
     * @return 结果
     */
    @Override
    public int updateSpeciesTaxa(SpeciesTaxa speciesTaxa)
    {
        speciesTaxa.setUpdateTime(DateUtils.getNowDate());
        return speciesTaxaMapper.updateSpeciesTaxa(speciesTaxa);
    }

    /**
     * 批量删除物种分类基础
     * 
     * @param taxonIds 需要删除的物种分类基础主键
     * @return 结果
     */
    @Override
    public int deleteSpeciesTaxaByTaxonIds(Long[] taxonIds)
    {
        return speciesTaxaMapper.deleteSpeciesTaxaByTaxonIds(taxonIds);
    }

    /**
     * 删除物种分类基础信息
     * 
     * @param taxonId 物种分类基础主键
     * @return 结果
     */
    @Override
    public int deleteSpeciesTaxaByTaxonId(Long taxonId)
    {
        return speciesTaxaMapper.deleteSpeciesTaxaByTaxonId(taxonId);
    }
}
