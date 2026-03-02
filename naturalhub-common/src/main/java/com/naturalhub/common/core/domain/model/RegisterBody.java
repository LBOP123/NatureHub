package com.naturalhub.common.core.domain.model;

/**
 * 用户注册对象
 * 
 * @author NaturalHub
 */
public class RegisterBody extends LoginBody
{
    /** 用户类型 */
    private String userType;
    
    /** 邮箱 */
    private String email;
    
    /** 手机号 */
    private String phonenumber;

    public String getUserType()
    {
        return userType;
    }

    public void setUserType(String userType)
    {
        this.userType = userType;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }
}
