package com.naturalhub.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.naturalhub.common.annotation.Excel;
import com.naturalhub.common.core.domain.BaseEntity;

/**
 * 社群板块分类对象 nh_community_category
 * 
 * @author NaturalHub
 * @date 2026-03-02
 */
public class CommunityCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 板块ID */
    private Long categoryId;

    /** 板块代码 */
    @Excel(name = "板块代码")
    private String categoryCode;

    /** 板块名称 */
    @Excel(name = "板块名称")
    private String categoryName;

    /** 板块图标 */
    @Excel(name = "板块图标")
    private String categoryIcon;

    /** 板块描述 */
    @Excel(name = "板块描述")
    private String categoryDesc;

    /** 排序顺序 */
    @Excel(name = "排序顺序")
    private Integer sortOrder;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }

    public void setCategoryCode(String categoryCode) 
    {
        this.categoryCode = categoryCode;
    }

    public String getCategoryCode() 
    {
        return categoryCode;
    }

    public void setCategoryName(String categoryName) 
    {
        this.categoryName = categoryName;
    }

    public String getCategoryName() 
    {
        return categoryName;
    }

    public void setCategoryIcon(String categoryIcon) 
    {
        this.categoryIcon = categoryIcon;
    }

    public String getCategoryIcon() 
    {
        return categoryIcon;
    }

    public void setCategoryDesc(String categoryDesc) 
    {
        this.categoryDesc = categoryDesc;
    }

    public String getCategoryDesc() 
    {
        return categoryDesc;
    }

    public void setSortOrder(Integer sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Integer getSortOrder() 
    {
        return sortOrder;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("categoryId", getCategoryId())
            .append("categoryCode", getCategoryCode())
            .append("categoryName", getCategoryName())
            .append("categoryIcon", getCategoryIcon())
            .append("categoryDesc", getCategoryDesc())
            .append("sortOrder", getSortOrder())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
