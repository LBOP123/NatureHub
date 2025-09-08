package com.naturalhub.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.naturalhub.common.annotation.Excel;
import com.naturalhub.common.core.domain.BaseEntity;

/**
 * 物种分类基础对象 species_taxa
 * 
 * @author Kairos
 * @date 2025-09-08
 */
public class SpeciesTaxa extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分类单元ID */
    private Long taxonId;

    /** 物种学名 */
    @Excel(name = "物种学名")
    private String scientificName;

    /** 物种中文名 */
    @Excel(name = "物种中文名")
    private String chineseName;

    /** 俗名 */
    @Excel(name = "俗名")
    private String commonNames;

    /** 分类阶元级别 */
    @Excel(name = "分类阶元级别")
    private Long rankLevel;

    /** 父分类单元ID */
    @Excel(name = "父分类单元ID")
    private Long parentTaxonId;

    /** 分类单元编码 */
    @Excel(name = "分类单元编码")
    private String taxonCode;

    /** 是否有效 */
    @Excel(name = "是否有效")
    private String isValid;

    /** 同义词 */
    @Excel(name = "同义词")
    private String synonyms;

    public void setTaxonId(Long taxonId) 
    {
        this.taxonId = taxonId;
    }

    public Long getTaxonId() 
    {
        return taxonId;
    }

    public void setScientificName(String scientificName) 
    {
        this.scientificName = scientificName;
    }

    public String getScientificName() 
    {
        return scientificName;
    }

    public void setChineseName(String chineseName) 
    {
        this.chineseName = chineseName;
    }

    public String getChineseName() 
    {
        return chineseName;
    }

    public void setCommonNames(String commonNames) 
    {
        this.commonNames = commonNames;
    }

    public String getCommonNames() 
    {
        return commonNames;
    }

    public void setRankLevel(Long rankLevel) 
    {
        this.rankLevel = rankLevel;
    }

    public Long getRankLevel() 
    {
        return rankLevel;
    }

    public void setParentTaxonId(Long parentTaxonId) 
    {
        this.parentTaxonId = parentTaxonId;
    }

    public Long getParentTaxonId() 
    {
        return parentTaxonId;
    }

    public void setTaxonCode(String taxonCode) 
    {
        this.taxonCode = taxonCode;
    }

    public String getTaxonCode() 
    {
        return taxonCode;
    }

    public void setIsValid(String isValid) 
    {
        this.isValid = isValid;
    }

    public String getIsValid() 
    {
        return isValid;
    }

    public void setSynonyms(String synonyms) 
    {
        this.synonyms = synonyms;
    }

    public String getSynonyms() 
    {
        return synonyms;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("taxonId", getTaxonId())
            .append("scientificName", getScientificName())
            .append("chineseName", getChineseName())
            .append("commonNames", getCommonNames())
            .append("rankLevel", getRankLevel())
            .append("parentTaxonId", getParentTaxonId())
            .append("taxonCode", getTaxonCode())
            .append("isValid", getIsValid())
            .append("synonyms", getSynonyms())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
