<template>
  <div class="user-login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">🌿</div>
        <h2>自然汇 · 用户登录</h2>
        <p class="subtitle">探索自然，记录生命</p>
      </div>

      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            type="text"
            auto-complete="off"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
            size="large"
          >
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            auto-complete="off"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            size="large"
            @keyup.enter.native="handleLogin"
          >
          </el-input>
        </el-form-item>

        <el-form-item prop="code" v-if="captchaEnabled">
          <el-input
            v-model="loginForm.code"
            auto-complete="off"
            placeholder="验证码"
            size="large"
            style="width: 63%"
            @keyup.enter.native="handleLogin"
          >
            <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon"/>
          </el-input>
          <div class="login-code">
            <img :src="codeUrl" @click="getCode" class="login-code-img"/>
          </div>
        </el-form-item>

        <el-form-item style="margin-bottom: 10px;">
          <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
<!--          <router-link to="/user/forgot" class="link-type" style="float: right;">忘记密码?</router-link>-->
        </el-form-item>

        <el-form-item style="width:100%;">
          <el-button
            :loading="loading"
            size="large"
            type="primary"
            style="width:100%;"
            @click.native.prevent="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
        </el-form-item>

        <div class="register-link">
          还没有账号？
          <router-link to="/user/register" class="link-type">立即注册</router-link>
        </div>
      </el-form>
    </div>

    <div class="footer">
      <p>© 2026 NaturalHub 自然汇 · 让每一次观察都有意义</p>
    </div>
  </div>
</template>

<script>
import {getCodeImg} from "@/api/login"
import Cookies from "js-cookie"
import {encrypt, decrypt} from '@/utils/jsencrypt'

export default {
  name: "UserLogin",
  data() {
    return {
      codeUrl: "",
      loginForm: {
        username: "",
        password: "",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [
          {required: true, trigger: "blur", message: "请输入您的用户名"}
        ],
        password: [
          {required: true, trigger: "blur", message: "请输入您的密码"}
        ],
        code: [{required: true, trigger: "change", message: "请输入验证码"}]
      },
      loading: false,
      captchaEnabled: true,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function (route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, {expires: 30})
            Cookies.set("password", encrypt(this.loginForm.password), {expires: 30})
            Cookies.set('rememberMe', this.loginForm.rememberMe, {expires: 30})
          } else {
            Cookies.remove("username")
            Cookies.remove("password")
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({path: this.redirect || "/user/index"}).catch(() => {
            })
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
          })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.user-login-container {
  min-height: 100vh;
  background: url('../../../assets/loginBackground/login-background.webp') center/cover no-repeat fixed;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
}

/* 🔥 仅登录框内部模糊 + 白边框 + 高透明 */
.login-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px); /* 只有这里模糊 */
  border: 1px solid rgba(255, 255, 255, 1);
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  padding: 35px 30px;
  width: 100%;
  max-width: 360px;
  position: relative;
  z-index: 1;
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 28px;

  .logo {
    font-size: 48px;
    margin-bottom: 12px;
    animation: rotate 3s ease-in-out infinite;
  }

  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #000000;
    margin: 0 0 8px 0;
  }

  .subtitle {
    color: #F2F6FC;
    font-size: 13px;
    margin: 0;
  }
}

@keyframes rotate {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(10deg); }
}

/* 🔥 输入框透明统一 */
.login-form {
  ::v-deep .el-input__inner {
    height: 42px;
    line-height: 42px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.4);
    color: #333;
    transition: all 0.3s;

    &:focus {
      border-color: #4CAF50;
      background: rgba(255, 255, 255, 0.3);
      box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
    }
  }

  ::v-deep .el-form-item {
    margin-bottom: 20px;
  }

  ::v-deep .el-button--primary {
    background: linear-gradient(135deg, #4CAF50 0%, #2E7D32 100%);
    border: none;
    height: 42px;
    border-radius: 10px;
    font-size: 16px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(76, 175, 80, 0.3);
    }
  }
}

.login-code {
  width: 33%;
  height: 42px;
  float: right;

  img {
    cursor: pointer;
    vertical-align: middle;
    height: 42px;
    border-radius: 8px;
  }
}

.link-type {
  color: #4CAF50;
  text-decoration: none;
  font-size: 13px;

  &:hover {
    color: #2E7D32;
    text-decoration: underline;
  }
}

.register-link {
  text-align: center;
  margin-top: 18px;
  color: #F2F6FC;
  font-size: 13px;
}

.footer {
  margin-top: 30px;
  text-align: center;
  color: rgba(255, 255, 255, 0.85);
  font-size: 12px;
  position: relative;
  z-index: 1;

  p {
    margin: 0;
  }
}

@media (max-width: 768px) {
  .login-card {
    padding: 30px 25px;
    max-width: 320px;
  }
}
</style>
