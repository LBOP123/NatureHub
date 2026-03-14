<template>
  <div class="user-layout-wrapper">
    <header class="user-header" :class="{ 'home-page': isHomePage }">
      <div class="header-content">
        <div class="brand" @click="$router.push('/user/index')">
          <span class="logo">🌿</span>
          <span class="brand-name">自然汇</span>
        </div>

        <nav class="user-nav">
          <a @click.prevent="$router.push('/user/index')" :class="{ active: isActive('/user/index') }">
            <i class="el-icon-s-home"></i> 首页
          </a>

          <!-- 探索下拉菜单 -->
          <el-dropdown @command="handleExploreCommand" trigger="hover">
            <a :class="{ active: isActive('/user/observation') || isActive('/user/survey') || isActive('/user/identification') || isActive('/user/diary') }">
              <i class="el-icon-compass"></i> 探索
              <i class="el-icon-arrow-down"></i>
            </a>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="observation">
                <i class="el-icon-view"></i> 观察记录
              </el-dropdown-item>
              <el-dropdown-item command="survey">
                <i class="el-icon-data-analysis"></i> 野外调查
              </el-dropdown-item>
              <el-dropdown-item command="identification">
                <i class="el-icon-search"></i> 物种鉴定
              </el-dropdown-item>
              <el-dropdown-item command="diary">
                <i class="el-icon-notebook-2"></i> 观察日记
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>

          <a @click.prevent="$router.push('/user/community')" :class="{ active: isActive('/user/community') }">
            <i class="el-icon-chat-dot-round"></i> 社群
          </a>
          <a @click.prevent="$router.push('/user/myProfile')" :class="{ active: isActive('/user/myProfile') }">
            <i class="el-icon-user"></i> 个人中心
          </a>
        </nav>

        <div class="user-actions">
          <el-dropdown v-if="isLoggedIn" @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userAvatar" icon="el-icon-user-solid"></el-avatar>
              <span class="username">{{ userName }}</span>
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile">
                <i class="el-icon-user"></i> 个人中心
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <i class="el-icon-setting"></i> 设置
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <i class="el-icon-switch-button"></i> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>

          <div v-else class="auth-buttons">
            <el-button size="small" @click="$router.push('/user/login')">登录</el-button>
            <el-button size="small" type="primary" @click="$router.push('/user/register')">注册</el-button>
          </div>
        </div>
      </div>
    </header>

    <main class="user-main">
      <router-view />
    </main>

    <footer class="user-footer">
      <div class="footer-content">
        <div class="footer-info">
          <p class="footer-brand">🌿 自然汇 NaturalHub</p>
          <p class="footer-slogan">让每一次观察都有意义</p>
        </div>
        <div class="footer-links">
          <a href="#">关于我们</a>
          <span class="divider">|</span>
          <a href="#">联系方式</a>
          <span class="divider">|</span>
          <a href="#">用户协议</a>
          <span class="divider">|</span>
          <a href="#">隐私政策</a>
        </div>
        <p class="copyright">© 2026 NaturalHub. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'UserLayout',
  data() {
    return {
      isHomePage: false
    }
  },
  watch: {
    $route(to) {
      this.isHomePage = to.path === '/user/index'
    }
  },
  mounted() {
    this.isHomePage = this.$route.path === '/user/index'
  },
  computed: {
    isLoggedIn() {
      return this.$store.getters.token
    },
    userName() {
      return this.$store.getters.name || '用户'
    },
    userAvatar() {
      return this.$store.getters.avatar
    }
  },
  methods: {
    isActive(path) {
      return this.$route.path.startsWith(path)
    },
    handleExploreCommand(command) {
      const routes = {
        observation: '/user/observation/list',
        survey: '/user/survey/list',
        identification: '/user/identification/list',
        diary: '/user/diary/list'
      }
      this.$router.push(routes[command])
    },
    handleCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push({ path: '/user/myProfile'})
          break
        case 'settings':
          this.$router.push('/user/settings')
          break
        case 'logout':
          this.$confirm('确定要退出登录吗?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.$store.dispatch('LogOut').then(() => {
              this.$router.push('/user/login')
            })
          }).catch(() => {})
          break
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.user-layout-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f5f5;
}

.user-header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 1000;
  transition: all 0.3s ease;

  &.home-page {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .header-content {
    max-width: 1600px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 70px;
    padding: 0 40px;
  }
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.3s ease;

  &:hover {
    transform: scale(1.05);
  }

  .logo {
    font-size: 32px;
  }

  .brand-name {
    font-size: 22px;
    font-weight: 700;
    color: #2c3e50;
    letter-spacing: 1px;
    transition: color 0.3s ease;
  }
}

.user-header.home-page .brand-name {
  color: #ffffff;
}

.user-nav {
  display: flex;
  gap: 8px;
  align-items: center;

  a {
    padding: 8px 20px;
    color: #606266;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.3s ease;
    font-size: 16px;
    display: flex;
    align-items: center;
    gap: 6px;

    i {
      font-size: 16px;
    }

    &:hover {
      color: #43cea2;
      background: rgba(67, 206, 162, 0.1);
    }

    &.active {
      color: #43cea2;
      background: rgba(67, 206, 162, 0.15);
      font-weight: 600;
    }
  }
}

.user-header.home-page .user-nav a {
  color: rgba(255, 255, 255, 0.8);

  &:hover {
    color: #ffffff;
    background: rgba(255, 255, 255, 0.2);
  }

  &.active {
    color: #ffffff;
    background: rgba(255, 255, 255, 0.25);
    font-weight: 600;
  }
}

::v-deep .el-dropdown {
  display: flex;
  align-items: center;

  a {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

::v-deep .el-dropdown-menu {
  min-width: 140px;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);

  .el-dropdown-menu__item {
    padding: 10px 16px;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: all 0.3s ease;

    i {
      font-size: 14px;
    }

    &:hover {
      color: #43cea2;
      background: rgba(67, 206, 162, 0.1);
    }
  }
}

.user-actions {
  .user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    padding: 6px 12px;
    border-radius: 8px;
    transition: background 0.3s ease;
    color: #606266;

    &:hover {
      background: #f5f7fa;
    }

    .username {
      font-size: 14px;
      color: #606266;
      font-weight: 500;
    }
  }

  .auth-buttons {
    display: flex;
    gap: 12px;
  }
}

.user-header.home-page .user-info {
  color: rgba(255, 255, 255, 0.8);

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    color: #ffffff;
  }

  .username {
    color: rgba(255, 255, 255, 0.8);
  }
}

.user-main {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
  position: relative;
  z-index: 10;
}

.user-footer {
  background: #2c3e50;
  color: white;
  padding: 40px 24px 24px;
  margin-top: 60px;
  position: relative;
  z-index: 10;

  .footer-content {
    max-width: 1400px;
    margin: 0 auto;
    text-align: center;
  }

  .footer-info {
    margin-bottom: 24px;

    .footer-brand {
      font-size: 20px;
      font-weight: 600;
      margin: 0 0 8px 0;
    }

    .footer-slogan {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.7);
      margin: 0;
    }
  }

  .footer-links {
    margin-bottom: 20px;

    a {
      color: rgba(255, 255, 255, 0.8);
      text-decoration: none;
      font-size: 14px;
      transition: color 0.3s ease;

      &:hover {
        color: #43cea2;
      }
    }

    .divider {
      margin: 0 12px;
      color: rgba(255, 255, 255, 0.3);
    }
  }

  .copyright {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.5);
    margin: 0;
  }
}

@media (max-width: 768px) {
  .user-header .header-content {
    padding: 0 16px;
  }

  .brand .brand-name {
    display: none;
  }

  .user-nav {
    gap: 4px;

    a {
      padding: 6px 12px;
      font-size: 14px;

      span {
        display: none;
      }
    }
  }

  .user-main {
    padding: 16px;
  }

  .user-footer {
    padding: 30px 16px 20px;

    .footer-links {
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 8px;

      .divider {
        display: none;
      }
    }
  }
}
</style>
