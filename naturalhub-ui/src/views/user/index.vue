<template>
  <div class="user-index-container">
    <div class="hero-section">
      <h1 class="hero-title">🌿 欢迎来到自然汇</h1>
      <p class="hero-subtitle">记录每一次与自然的相遇，探索生命的奥秘</p>
    </div>
    
    <div class="features-grid">
      <div 
        v-for="feature in features" 
        :key="feature.id"
        class="feature-card"
        @click="navigateTo(feature.path)"
      >
        <div class="feature-icon">{{ feature.icon }}</div>
        <h3 class="feature-title">{{ feature.title }}</h3>
        <p class="feature-desc">{{ feature.description }}</p>
        <div class="feature-arrow">→</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "UserIndex",
  data() {
    return {
      features: [
        {
          id: 1,
          icon: "🔍",
          title: "生物识别上传",
          description: "拍照上传，AI智能识别物种信息",
          path: "/user/upload"
        },
        {
          id: 2,
          icon: "📝",
          title: "我的观察记录",
          description: "查看和管理你的所有观察记录",
          path: "/user/observations"
        },
        {
          id: 3,
          icon: "🔎",
          title: "物种鉴定求助",
          description: "不认识的物种？专家帮你鉴定",
          path: "/user/identify"
        },
        {
          id: 4,
          icon: "📊",
          title: "野外调查记录",
          description: "系统化记录野外调查数据",
          path: "/user/survey"
        },
        {
          id: 5,
          icon: "🧠",
          title: "AI科普问答",
          description: "智能问答，解答你的自然疑问",
          path: "/user/qa"
        },
        {
          id: 6,
          icon: "🌍",
          title: "物种知识图谱",
          description: "探索物种间的关系与演化",
          path: "/user/knowledge"
        },
        {
          id: 7,
          icon: "💬",
          title: "社群交流",
          description: "与自然爱好者分享交流",
          path: "/user/community"
        },
        {
          id: 8,
          icon: "📔",
          title: "个人观察日志",
          description: "记录你的自然探索历程",
          path: "/user/diary"
        }
      ]
    }
  },
  methods: {
    navigateTo(path) {
      this.$router.push(path).catch(() => {
        this.$message.info('该功能正在开发中，敬请期待')
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.user-index-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.hero-section {
  text-align: center;
  margin-bottom: 60px;
  animation: fadeIn 0.8s ease-out;
  
  .hero-title {
    font-size: 42px;
    font-weight: 700;
    color: #2c3e50;
    margin: 0 0 16px 0;
    letter-spacing: 1px;
  }
  
  .hero-subtitle {
    font-size: 18px;
    color: #7f8c8d;
    margin: 0;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  animation: slideUp 0.8s ease-out 0.2s both;
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

.feature-card {
  background: white;
  border-radius: 16px;
  padding: 32px 24px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid #f0f0f0;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #43cea2 0%, #185a9d 100%);
    transform: scaleX(0);
    transition: transform 0.3s ease;
  }
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
    border-color: #43cea2;
    
    &::before {
      transform: scaleX(1);
    }
    
    .feature-icon {
      transform: scale(1.1) rotate(5deg);
    }
    
    .feature-arrow {
      transform: translateX(8px);
      opacity: 1;
    }
  }
  
  .feature-icon {
    font-size: 48px;
    margin-bottom: 16px;
    transition: transform 0.3s ease;
  }
  
  .feature-title {
    font-size: 20px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0 0 12px 0;
  }
  
  .feature-desc {
    font-size: 14px;
    color: #7f8c8d;
    line-height: 1.6;
    margin: 0;
  }
  
  .feature-arrow {
    position: absolute;
    bottom: 24px;
    right: 24px;
    font-size: 24px;
    color: #43cea2;
    opacity: 0;
    transition: all 0.3s ease;
  }
}

@media (max-width: 768px) {
  .hero-section {
    margin-bottom: 40px;
    
    .hero-title {
      font-size: 32px;
    }
    
    .hero-subtitle {
      font-size: 16px;
    }
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .feature-card {
    padding: 24px 20px;
  }
}
</style>
