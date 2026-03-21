<template>
  <div class="user-register-container">
    <div class="register-card">
      <div class="register-header">
        <div class="logo">🌿</div>
        <h2>加入自然汇</h2>
        <p class="subtitle">开启你的自然探索之旅</p>
      </div>

      <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form">
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            type="text"
            placeholder="用户名（4-20个字符）"
            prefix-icon="el-icon-user"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码（6-20个字符）"
            prefix-icon="el-icon-lock"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            prefix-icon="el-icon-lock"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            type="text"
            placeholder="邮箱地址"
            prefix-icon="el-icon-message"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="phonenumber">
          <el-input
            v-model="registerForm.phonenumber"
            type="text"
            placeholder="手机号码"
            prefix-icon="el-icon-phone"
            size="large"
            maxlength="11"
          />
        </el-form-item>

<!--        <el-form-item prop="userType">
          <div class="role-selector">
            <div
              class="role-card"
              :class="{ active: registerForm.userType === 'explorer' }"
              @click="registerForm.userType = 'explorer'"
            >
              <div class="role-icon">🔍</div>
              <div class="role-name">探索者</div>
              <div class="role-desc">记录观察、识别物种</div>
            </div>
            <div
              class="role-card"
              :class="{ active: registerForm.userType === 'supervisor' }"
              @click="registerForm.userType = 'supervisor'"
            >
              <div class="role-icon">👨‍🔬</div>
              <div class="role-name">监督者</div>
              <div class="role-desc">审核鉴定、科普指导</div>
            </div>
          </div>
        </el-form-item>-->

        <el-form-item prop="code" v-if="captchaEnabled">
          <el-input
            v-model="registerForm.code"
            placeholder="验证码"
            size="large"
            style="width: 63%"
          >
            <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
          </el-input>
          <div class="register-code">
            <img :src="codeUrl" @click="getCode" class="register-code-img"/>
          </div>
        </el-form-item>
        <el-form-item prop="agreement">
          <el-checkbox v-model="registerForm.agreement" style="color: #F2F6FC; font-size: 13px;">
            我已阅读并同意
            <a href="#" class="link-type">《用户协议》</a>
            和
            <a href="#" class="link-type">《隐私政策》</a>
          </el-checkbox>
        </el-form-item>

        <el-form-item style="width:100%;">
          <el-button
            :loading="loading"
            size="large"
            type="primary"
            style="width:100%;"
            @click.native.prevent="handleRegister"
          >
            <span v-if="!loading">立即注册</span>
            <span v-else>注册中...</span>
          </el-button>
        </el-form-item>

        <div class="login-link">
          已有账号？
          <router-link to="/user/login" class="link-type">立即登录</router-link>
        </div>
      </el-form>
    </div>

    <div class="footer">
      <p>© 2026 NaturalHub 自然汇 · 让每一次观察都有意义</p>
    </div>
  </div>
</template>

<script>
import { getCodeImg, register } from "@/api/login"

export default {
  name: "UserRegister",
  data() {
    const equalToPassword = (rule, value, callback) => {
      if (this.registerForm.password !== value) {
        callback(new Error("两次输入的密码不一致"))
      } else {
        callback()
      }
    }
    return {
      codeUrl: "",
      registerForm: {
        username: "",
        password: "",
        confirmPassword: "",
        email: "",
        phonenumber: "",
        userType: "explorer",
        code: "",
        uuid: "",
        agreement: false
      },
      registerRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入用户名" },
          { min: 4, max: 20, message: '用户名长度必须介于 4 和 20 之间', trigger: 'blur' }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入密码" },
          { min: 6, max: 20, message: '密码长度必须介于 6 和 20 之间', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, trigger: "blur", message: "请再次输入密码" },
          { required: true, validator: equalToPassword, trigger: "blur" }
        ],
        email: [
          { required: true, message: "请输入邮箱地址", trigger: "blur" },
          {
            type: "email",
            message: "请输入正确的邮箱地址",
            trigger: ["blur", "change"]
          }
        ],
        phonenumber: [
          { required: true, trigger: "blur", message: "请输入手机号码" },
          { pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur" }
        ],
        userType: [
          { required: true, message: "请选择用户类型", trigger: "change" }
        ],
        code: [
          { required: true, trigger: "change", message: "请输入验证码" }
        ],
        agreement: [
          {
            validator: (rule, value, callback) => {
              if (!value) {
                callback(new Error("请阅读并同意用户协议"))
              } else {
                callback()
              }
            },
            trigger: "change"
          }
        ]
      },
      loading: false,
      captchaEnabled: true
    }
  },
  created() {
    this.getCode()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.registerForm.uuid = res.uuid
        }
      })
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true
          register(this.registerForm).then(res => {
            const username = this.registerForm.username
            this.$alert("<font color='red'>恭喜你，您的账号 " + username + " 注册成功！</font>", '系统提示', {
              dangerouslyUseHTMLString: true,
              type: 'success',
            }).then(() => {
              this.$router.push("/user/login")
            }).catch(() => {})
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
.user-register-container {
  min-height: 100vh;
  background: url('../../../assets/loginBackground/login-background.webp') center/cover no-repeat fixed;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
}

/* 🔥 完全对齐登录页：毛玻璃 + 白边框 + blur模糊 */
.register-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 1);
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  padding: 35px 30px;
  width: 100%;
  max-width: 420px;
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

.register-header {
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

/* 🔥 输入框完全统一透明样式 */
.register-form {
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

  /* 🔥 统一绿色渐变按钮 */
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

/* 角色选择器适配毛玻璃风格 */
.role-selector {
  display: flex;
  gap: 12px;
  width: 100%;
}

.role-card {
  flex: 1;
  padding: 16px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;

  &:hover {
    border-color: #4CAF50;
    background: rgba(255, 255, 255, 0.25);
  }

  &.active {
    border-color: #4CAF50;
    background: rgba(76, 175, 80, 0.2);
    box-shadow: 0 4px 10px rgba(76, 175, 80, 0.2);
  }

  .role-icon {
    font-size: 32px;
    margin-bottom: 6px;
  }

  .role-name {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 4px;
  }

  .role-desc {
    font-size: 12px;
    color: #f0f0f0;
  }
}

.register-code {
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

.login-link {
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
  .register-card {
    padding: 30px 25px;
    max-width: 320px;
  }
  .role-selector {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
