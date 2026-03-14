<template>
  <div class="user-index-container">
    <!-- 背景轮播 -->
    <div class="background-carousel" :style="{ backgroundImage: `url(${currentBgImage})` }"></div>

    <!-- 内容层 -->
    <div class="content-wrapper">
      <div class="hero-section">
        <h1 class="hero-title">探索自然，记录生命的美好</h1>
        <p class="hero-subtitle">专业的自然观察与物种识别平台，连接每一位自然爱好者</p>
      </div>

      <!-- 功能卡片 -->
      <div class="features-grid">
        <!-- 物种库 -->
        <div class="feature-card" @click="navigateTo('/user/knowledge')">
          <div class="card-header">
            <h3 class="card-title">物种知识图谱</h3>
            <div class="title-underline"></div>
          </div>
          <p class="card-desc">我国首个权威发布且具有完全自主知识产权的中国生物物种全信息数据库，收录了1...</p>
          <div class="card-stats">
            <div class="stat-item">
              <span class="stat-number">443,153</span>
              <span class="stat-label">物种条目</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">225,992</span>
              <span class="stat-label">物种图片</span>
            </div>
          </div>
          <div class="card-images">
            <img src="https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=300&h=150&fit=crop" alt="species1">
          </div>
        </div>

        <!-- 知识库 -->
        <div class="feature-card" @click="navigateTo('/user/qa')">
          <div class="card-header">
            <h3 class="card-title">科普问答</h3>
            <div class="title-underline"></div>
          </div>
          <p class="card-desc">汇集生命科学类、生态学、专业科普等多个学科领域的权威知识，同时收录《中国物...</p>
          <div class="card-stats">
            <div class="stat-item">
              <span class="stat-number">4,939</span>
              <span class="stat-label">独立章节</span>
            </div>
          </div>
          <div class="card-images">
            <img src="https://images.unsplash.com/photo-1505142468610-359e7d316be0?w=300&h=150&fit=crop"
                 alt="knowledge1">
          </div>
        </div>

        <!-- AI问答 -->
        <div class="feature-card" @click="navigateTo('/user/bioRecognition')">
          <div class="card-header">
            <h3 class="card-title">生物识别</h3>
            <div class="title-underline"></div>
          </div>
          <p class="card-desc">生态科学术语库提供生态科学领域常用、基础性重要的名词术语，提供权威、准...</p>
          <div class="card-stats">
            <div class="stat-item">
              <span class="stat-number">135,334</span>
              <span class="stat-label">术语条目</span>
            </div>
          </div>
          <div class="card-images">
            <img src="https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=300&h=150&fit=crop" alt="term1">
          </div>
        </div>

        <!-- 图像库 -->
        <div class="feature-card" @click="navigateTo('/user/bioRecognition')">
          <div class="card-header">
            <h3 class="card-title">3D数字标本</h3>
            <div class="title-underline"></div>
          </div>
          <p class="card-desc">多媒体资源库中的图片、视频、音频等多媒体资源，包括图片、视频、音频等一站式...</p>
          <div class="card-stats">
            <div class="stat-item">
              <span class="stat-number">244,757</span>
              <span class="stat-label">图像数</span>
            </div>
          </div>
          <div class="card-images">
            <img src="https://images.unsplash.com/photo-1469022563149-aa64dbd37dae?w=300&h=150&fit=crop" alt="image1">
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "UserIndex",
  data() {
    return {
      backgroundImages: [
        'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=1920&h=1080&fit=crop',
        'https://images.unsplash.com/photo-1469022563149-aa64dbd37dae?w=1920&h=1080&fit=crop',
        'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=1920&h=1080&fit=crop',
        'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1920&h=1080&fit=crop',
        'https://images.unsplash.com/photo-1505142468610-359e7d316be0?w=1920&h=1080&fit=crop'
      ],
      currentBgIndex: 0,
      currentBgImage: '',
      bgCarouselTimer: null
    }
  },
  mounted() {
    this.initBackground()
    this.startBgCarousel()
  },
  beforeDestroy() {
    if (this.bgCarouselTimer) {
      clearInterval(this.bgCarouselTimer)
    }
  },
  methods: {
    initBackground() {
      // 随机选择初始背景
      this.currentBgIndex = Math.floor(Math.random() * this.backgroundImages.length)
      this.currentBgImage = this.backgroundImages[this.currentBgIndex]
    },
    startBgCarousel() {
      // 每3分钟轮播一次背景
      this.bgCarouselTimer = setInterval(() => {
        this.currentBgIndex = (this.currentBgIndex + 1) % this.backgroundImages.length
        this.currentBgImage = this.backgroundImages[this.currentBgIndex]
      }, 3 * 60 * 1000) // 3分钟
    },
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
  position: relative;
  width: 100%;
  overflow: visible;
}

// 背景轮播
.background-carousel {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: 0;
  transition: background-image 1s ease-in-out;
  pointer-events: none;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.35);
    z-index: 1;
  }
}

// 内容层
.content-wrapper {
  position: relative;
  z-index: 2;
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px 20px;
}

// 英雄区
.hero-section {
  text-align: center;
  margin-bottom: 80px;
  animation: fadeInDown 0.8s ease-out;

  .hero-title {
    font-size: 56px;
    font-weight: 700;
    color: #ffffff;
    margin: 0 0 20px 0;
    letter-spacing: 2px;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
    line-height: 1.2;
  }

  .hero-subtitle {
    font-size: 20px;
    color: rgba(255, 255, 255, 0.9);
    margin: 0;
    text-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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

// 功能卡片网格
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  max-width: 1200px;
  width: 100%;
  margin-bottom: 100px;
  animation: slideUp 0.8s ease-out 0.2s both;
}

.feature-card {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 12px;
  padding: 28px 24px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-12px);
    box-shadow: 0 16px 48px rgba(0, 0, 0, 0.2);
  }

  .card-header {
    margin-bottom: 16px;

    .card-title {
      font-size: 22px;
      font-weight: 700;
      color: #2c3e50;
      margin: 0 0 8px 0;
      letter-spacing: 0.5px;
    }

    .title-underline {
      width: 60px;
      height: 3px;
      background: linear-gradient(90deg, #43cea2 0%, #185a9d 100%);
      border-radius: 2px;
    }
  }

  .card-desc {
    font-size: 13px;
    color: #7f8c8d;
    line-height: 1.6;
    margin: 0 0 16px 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .card-stats {
    display: flex;
    gap: 20px;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;

    .stat-item {
      display: flex;
      flex-direction: column;

      .stat-number {
        font-size: 18px;
        font-weight: 700;
        color: #2c3e50;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 12px;
        color: #95a5a6;
        margin-top: 4px;
      }
    }
  }

  .card-images {
    display: flex;
    gap: 8px;
    margin-top: auto;

    img {
      width: 100%;
      height: 100px;
      object-fit: cover;
      border-radius: 8px;
      transition: transform 0.3s ease;

      &:hover {
        transform: scale(1.05);
      }
    }
  }
}

// 探索导航栏
.explore-section {
  width: 100%;
  max-width: 1000px;
  margin-bottom: 60px;
  animation: fadeIn 0.8s ease-out 0.4s both;

  .explore-title {
    text-align: center;
    font-size: 32px;
    font-weight: 700;
    color: #ffffff;
    margin: 0 0 40px 0;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  }

  .explore-nav {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 20px;
  }
}

.explore-item {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
    background: rgba(255, 255, 255, 1);

    .explore-icon {
      transform: scale(1.2) rotate(10deg);
    }
  }

  .explore-icon {
    font-size: 40px;
    margin-bottom: 12px;
    transition: transform 0.3s ease;
    display: block;
  }

  span {
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
    display: block;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

// 社群卡片
.community-section {
  width: 100%;
  max-width: 1000px;
  animation: fadeIn 0.8s ease-out 0.6s both;
}

.community-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 40px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 16px 48px rgba(0, 0, 0, 0.2);

    .community-icon {
      transform: scale(1.15);
    }
  }

  .community-icon {
    font-size: 56px;
    margin-bottom: 16px;
    transition: transform 0.3s ease;
    display: block;
  }

  h3 {
    font-size: 24px;
    font-weight: 700;
    color: #2c3e50;
    margin: 0 0 12px 0;
  }

  p {
    font-size: 16px;
    color: #7f8c8d;
    margin: 0;
  }
}

@media (max-width: 768px) {
  .content-wrapper {
    padding: 40px 16px;
  }

  .hero-section {
    margin-bottom: 60px;

    .hero-title {
      font-size: 36px;
      letter-spacing: 1px;
    }

    .hero-subtitle {
      font-size: 16px;
    }
  }

  .features-grid {
    grid-template-columns: 1fr;
    gap: 16px;
    margin-bottom: 60px;
  }

  .explore-nav {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .explore-item {
    padding: 20px;

    .explore-icon {
      font-size: 32px;
    }

    span {
      font-size: 14px;
    }
  }

  .community-card {
    padding: 32px 20px;

    .community-icon {
      font-size: 44px;
    }

    h3 {
      font-size: 20px;
    }

    p {
      font-size: 14px;
    }
  }
}
</style>
