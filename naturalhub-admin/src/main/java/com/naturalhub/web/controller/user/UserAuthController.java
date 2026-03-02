package com.naturalhub.web.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.naturalhub.common.constant.Constants;
import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.core.domain.entity.SysUser;
import com.naturalhub.common.core.domain.model.LoginBody;
import com.naturalhub.common.core.domain.model.RegisterBody;
import com.naturalhub.common.utils.SecurityUtils;
import com.naturalhub.framework.web.service.SysLoginService;
import com.naturalhub.framework.web.service.SysRegisterService;
import com.naturalhub.system.service.ISysConfigService;

/**
 * 用户端登录注册Controller
 * 
 * @author NaturalHub
 */
@RestController
@RequestMapping("/user/auth")
public class UserAuthController
{
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 用户端登录
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌（添加user前缀区分）
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, "user_" + token);
        return ajax;
    }

    /**
     * 用户端注册
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return AjaxResult.error("当前系统没有开启注册功能！");
        }
        
        // 设置用户类型
        if (user.getUserType() == null || user.getUserType().isEmpty()) {
            user.setUserType("explorer"); // 默认为探索者
        }
        
        // 验证用户类型
        if (!"explorer".equals(user.getUserType()) && !"supervisor".equals(user.getUserType())) {
            return AjaxResult.error("用户类型只能是explorer（探索者）或supervisor（监督者）");
        }
        
        String msg = registerService.register(user);
        return AjaxResult.success(msg);
    }
}
