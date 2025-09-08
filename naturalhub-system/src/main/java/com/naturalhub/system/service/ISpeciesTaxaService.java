package com.naturalhub.system.service;

import java.util.List;
import com.naturalhub.system.domain.SpeciesTaxa;

/**
 * 物种分类基础Service接口
 * 
 * @author Kairos
 * @date 2025-09-08
 */
public interface ISpeciesTaxaService 
{
    /**
     * 查询物种分类基础
     * 
     * @param taxonId 物种分类基础主键
     * @return 物种分类基础
     */
    public SpeciesTaxa selectSpeciesTaxaByTaxonId(Long taxonId);

    /**
     * 查询物种分类基础列表
     * 
     * @param speciesTaxa 物种分类基础
     * @return 物种分类基础集合
     */
    public List<SpeciesTaxa> selectSpeciesTaxaList(SpeciesTaxa speciesTaxa);

    /**
     * 新增物种分类基础
     * 
     * @param speciesTaxa 物种分类基础
     * @return 结果
     */
    public int insertSpeciesTaxa(SpeciesTaxa speciesTaxa);

    /**
     * 修改物种分类基础
     * 
     * @param speciesTaxa 物种分类基础
     * @return 结果
     */
    public int updateSpeciesTaxa(SpeciesTaxa speciesTaxa);

    /**
     * 批量删除物种分类基础
     * 
     * @param taxonIds 需要删除的物种分类基础主键集合
     * @return 结果
     */
    public int deleteSpeciesTaxaByTaxonIds(Long[] taxonIds);

    /**
     * 删除物种分类基础信息
     * 
     * @param taxonId 物种分类基础主键
     * @return 结果
     */
    public int deleteSpeciesTaxaByTaxonId(Long taxonId);
}
