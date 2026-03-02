package com.naturalhub.web.controller.user;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.core.domain.entity.SysUser;
import com.naturalhub.system.service.ISysUserService;

/**
 * 用户端公开个人信息Controller
 *
 * 仅返回公开字段，用于在评论/话题中查看他人个人页。
 *
 * @author NaturalHub
 */
@RestController
@RequestMapping("/user/profile")
public class UserProfileController
{
    @Autowired
    private ISysUserService userService;

    /**
     * 获取指定用户的公开资料
     */
    @GetMapping("/{userId}")
    public AjaxResult getPublicProfile(@PathVariable("userId") Long userId)
    {
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            return AjaxResult.error("用户不存在");
        }
        PublicUserProfile profile = new PublicUserProfile();
        profile.setUserId(user.getUserId());
        profile.setUserName(user.getUserName());
        profile.setNickName(user.getNickName());
        profile.setAvatar(user.getAvatar());
        profile.setCreateTime(user.getCreateTime());
        profile.setUserType(user.getUserType());
        profile.setRemark(user.getRemark());
        return AjaxResult.success(profile);
    }

    /**
     * 公开资料DTO（避免返回敏感字段）
     */
    public static class PublicUserProfile
    {
        private Long userId;
        private String userName;
        private String nickName;
        private String avatar;
        private Date createTime;
        private String userType;
        private String remark;

        public Long getUserId()
        {
            return userId;
        }

        public void setUserId(Long userId)
        {
            this.userId = userId;
        }

        public String getUserName()
        {
            return userName;
        }

        public void setUserName(String userName)
        {
            this.userName = userName;
        }

        public String getNickName()
        {
            return nickName;
        }

        public void setNickName(String nickName)
        {
            this.nickName = nickName;
        }

        public String getAvatar()
        {
            return avatar;
        }

        public void setAvatar(String avatar)
        {
            this.avatar = avatar;
        }

        public Date getCreateTime()
        {
            return createTime;
        }

        public void setCreateTime(Date createTime)
        {
            this.createTime = createTime;
        }

        public String getUserType()
        {
            return userType;
        }

        public void setUserType(String userType)
        {
            this.userType = userType;
        }

        public String getRemark()
        {
            return remark;
        }

        public void setRemark(String remark)
        {
            this.remark = remark;
        }
    }
}

