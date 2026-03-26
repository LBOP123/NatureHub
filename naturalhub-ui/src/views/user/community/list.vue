<template>
  <div class="container">
    <!-- 标签导航（简约胶囊样式） -->
    <div class="nav-tabs">
      <button :class="{ active: queryParams.category === '' }" @click="queryParams.category = ''; handleQuery()">全部话题</button>
      <button v-for="item in categoryList" :key="item.categoryCode" :class="{ active: queryParams.category === item.categoryCode }" @click="queryParams.category = item.categoryCode; handleQuery()">
        {{ item.categoryName }}
      </button>
    </div>

    <!-- 搜索+筛选栏（简约风格） -->
    <div class="search-filter-bar">
      <div class="search-box">
        <input type="text" v-model="queryParams.title" placeholder="搜索话题标题..." @keyup.enter="handleQuery">
      </div>
      <div class="filter-options">
        <label class="filter-item">
          <input
            type="checkbox"
            :checked="filterOptions.includes('top')"
            @change="toggleFilter('top')"
          > 置顶
        </label>
        <label class="filter-item">
          <input
            type="checkbox"
            :checked="filterOptions.includes('essence')"
            @change="toggleFilter('essence')"
          > 精华
        </label>
      </div>
    </div>

    <!-- 空状态提示（简约） -->
    <div class="empty-tip" v-show="topicList.length === 0">
      暂无话题内容
    </div>

    <!-- 话题列表（简约布局，解决内容拥挤问题） -->
    <div class="topic-list" v-show="topicList.length > 0">
      <div class="topic-card" v-for="topic in topicList" :key="topic.topicId" @click="handleView(topic)">
        <div class="topic-content">
          <!-- 标签组（简约样式） -->
          <div class="badge-group" v-if="topic.isTop === '1' || topic.isEssence === '1' || topic.sourceType">
            <span class="badge badge-top" v-if="topic.isTop === '1'">置顶</span>
            <span class="badge badge-good" v-if="topic.isEssence === '1'">精华</span>
            <span class="badge badge-source" :class="'badge-source--' + getSourceClass(topic.sourceType)" v-if="topic.sourceType">{{ getSourceName(topic.sourceType) }}</span>
          </div>

          <h3 class="topic-title">{{ topic.title }}</h3>

          <p class="topic-desc">{{ topic.content || '暂无描述' }}</p>

          <div class="topic-meta">
            <span class="meta-author" @click.stop="goToUserProfile(topic.userId)">{{ topic.userName }}</span>
            <span class="meta-sep">|</span>
            <span class="meta-item">{{ parseTime(topic.createTime, '{y}-{m}-{d}') }}</span>
            <span class="meta-sep">|</span>
            <span class="meta-item">阅读 {{ topic.viewCount }}</span>
            <span class="meta-item" :class="{ active: topic.isLiked }" @click.stop="handleLike(topic)">点赞 {{ topic.likeCount }}</span>
            <span class="meta-item">评论 {{ topic.commentCount }}</span>
            <span class="meta-item" :class="{ active: topic.isCollected }" @click.stop="handleCollect(topic)">收藏 {{ topic.collectCount }}</span>
          </div>
        </div>

        <!-- 缩略图（简约样式，可选隐藏） -->
        <div class="topic-thumbnail" v-if="topic.coverImage">
          <img :src="topic.coverImage" alt="封面">
        </div>
      </div>
    </div>

    <!-- 分页（简约样式） -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" class="pagination-container" />

    <!-- 回到顶部（简约） -->
    <div class="back-top" @click="backToTop">↑</div>
  </div>
</template>

<script>
import { listTopic, likeTopic, unlikeTopic, collectTopic, uncollectTopic } from '@/api/user/community'
import { listAllCategory } from '@/api/community/category'
import { getDicts } from '@/api/system/dict/data'

export default {
  name: 'CommunityList',
  data() {
    return {
      queryParams: { pageNum: 1, pageSize: 10, category: '', title: '', auditStatus: '1' },
      filterOptions: [],
      topicList: [],
      communityCategoryOptions: [],
      sourceTypeOptions: [],
      categoryList: [],
      total: 0
    }
  },
  created() {
    this.getDicts('nh_community_category_type').then(res => { this.communityCategoryOptions = res.data || [] })
    this.getDicts('nh_community_source_type').then(res => { this.sourceTypeOptions = res.data || [] })
    this.getList()
    this.getCategoryList()
  },
  methods: {
    // 获取字典数据
    getDicts(dictType) {
      return getDicts(dictType)
    },
    // 获取分类列表
    getCategoryList() {
      listAllCategory().then(response => {
        this.categoryList = response.data
      })
    },
    // 获取话题列表
    getList() {
      const params = { ...this.queryParams }
      if (this.filterOptions.includes('essence')) params.isEssence = '1'
      if (this.filterOptions.includes('top')) params.isTop = '1'
      listTopic(params).then(response => {
        this.topicList = response.rows.map(topic => {
          // 从 images 字段提取第一张图片作为缩略图
          if (topic.images) {
            try {
              let images = topic.images
              // 如果是字符串，尝试解析
              if (typeof images === 'string') {
                // 先检查是否是 JSON 数组格式
                if (images.startsWith('[')) {
                  images = JSON.parse(images)
                } else {
                  // 否则用逗号分割（适用于野外调查等）
                  images = images.split(',').map(s => s.trim()).filter(Boolean)
                }
              }
              // 获取第一张图片
              topic.coverImage = Array.isArray(images) && images.length > 0 ? images[0] : null
            } catch (e) {
              topic.coverImage = null
            }
          }
          return topic
        })
        this.total = response.total
      })
    },
    // 搜索查询
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList()
    },
    // 切换筛选条件
    toggleFilter(key) {
      const idx = this.filterOptions.indexOf(key)
      if (idx > -1) {
        this.filterOptions.splice(idx, 1)
      } else {
        this.filterOptions.push(key)
      }
      this.handleQuery()
    },
    // 查看话题详情
    handleView(topic) {
      this.$router.push({ path: '/user/community/detail', query: { id: topic.topicId } })
    },
    // 跳转到用户主页
    goToUserProfile(userId) {
      if (!userId) return;
      this.$router.push({ path: '/user/myProfile', query: { userId } })
    },
    // 点赞/取消点赞
    handleLike(topic) {
      if (topic.isLiked) {
        unlikeTopic(topic.topicId).then(() => {
          topic.isLiked = false;
          topic.likeCount--;
          this.$message.success('已取消点赞')
        })
      } else {
        likeTopic(topic.topicId).then(() => {
          topic.isLiked = true;
          topic.likeCount++;
          this.$message.success('点赞成功')
        })
      }
    },
    // 收藏/取消收藏
    handleCollect(topic) {
      if (topic.isCollected) {
        uncollectTopic(topic.topicId).then(() => {
          topic.isCollected = false;
          topic.collectCount--;
          this.$message.success('已取消收藏')
        })
      } else {
        collectTopic(topic.topicId).then(() => {
          topic.isCollected = true;
          topic.collectCount++;
          this.$message.success('收藏成功')
        })
      }
    },
    // 回到顶部
    backToTop() {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      })
    },
    // 获取来源类型名称
    getSourceName(type) {
      const item = this.sourceTypeOptions.find(d => d.dictValue == type)
      return item ? item.dictLabel : ''
    },
    // 获取来源类型颜色class
    getSourceClass(type) {
      const item = this.sourceTypeOptions.find(d => d.dictValue == type)
      return item ? (item.listClass || 'default') : 'default'
    }
  }
}
</script>

<style lang="scss" scoped>
// 基础重置 - 极简风格
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: "PingFang SC", "Microsoft Yahei", sans-serif;
}

// 容器 - 加宽内边距，解决内容拥挤
.container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 30px; // 增加左右内边距，避免内容贴边
}

// 标签栏 - 简约胶囊样式
.nav-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;

  button {
    padding: 8px 20px;
    border: 1px solid #e5e5e5;
    border-radius: 24px;
    background: #fff;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s ease;

    &.active {
      background: #2f54eb;
      color: #fff;
      border-color: #2f54eb;
    }

    &:hover:not(.active) {
      border-color: #d0d0d0;
    }
  }
}

// 搜索筛选栏 - 极简布局，增加间距
.search-filter-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px 0;
  margin-bottom: 25px;
  flex-wrap: wrap;

  .search-box {
    flex: 1;
    min-width: 280px;

    input {
      width: 100%;
      padding: 10px 15px;
      border: 1px solid #e5e5e5;
      border-radius: 6px;
      font-size: 14px;
      outline: none;
      transition: border 0.2s;

      &:focus {
        border-color: #2f54eb;
        box-shadow: 0 0 0 2px rgba(47, 84, 235, 0.1);
      }
    }
  }

  .filter-options {
    display: flex;
    gap: 15px;
    align-items: center;

    .filter-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: #666;
      cursor: pointer;

      input {
        width: 16px;
        height: 16px;
        cursor: pointer;
      }
    }
  }
}

// 话题列表 - 极简布局，充足间距
.topic-list {
  display: flex;
  flex-direction: column;
  gap: 16px; // 卡片之间增加间距，避免拥挤
}

.topic-card {
  background: #fff;
  padding: 20px; // 增加内边距
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  cursor: pointer;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

// 内容区 - 合理间距，解决拥挤
.topic-content {
  flex: 1;
  padding-right: 20px; // 增加右侧间距
}

// 徽章组 - 简约样式
.badge-group {
  margin-bottom: 10px;
  display: flex;
  gap: 8px;
  align-items: center;

  .badge {
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    color: #fff;

    &.badge-top {
      background: #ff7d00;
    }

    &.badge-good {
      background: #00b42a;
    }

    &.badge-source {
      background: #86909c;
      &--primary   { background: #409EFF; }
      &--success   { background: #67C23A; }
      &--warning   { background: #E6A23C; }
      &--danger    { background: #F56C6C; }
      &--info      { background: #909399; }
      &--default   { background: #86909c; }
    }
  }
}

// 标题 - 加大字号，增加间距
.topic-title {
  font-size: 18px;
  color: #1d2129;
  font-weight: 500;
  line-height: 1.5;
  margin-bottom: 12px;
  transition: color 0.2s;

  &:hover {
    color: #2f54eb;
  }
}

// 摘要 - 合理行高，增加间距
.topic-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2; // 显示2行，避免内容挤压
  -webkit-box-orient: vertical;
  overflow: hidden;
}

// 元信息 - 简约排版，充足间距
.topic-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #999;
  font-size: 13px;
  flex-wrap: wrap;

  .meta-author {
    color: #333;
    cursor: pointer;

    &:hover {
      color: #2f54eb;
    }
  }

  .meta-sep {
    color: #e5e5e5;
  }

  .meta-item {
    cursor: pointer;
    transition: color 0.2s;

    &.active {
      color: #2f54eb;
    }

    &:hover {
      color: #2f54eb;
    }
  }
}

// 缩略图 - 简约样式，圆角，优化尺寸
.topic-thumbnail {
  width: 160px;
  height: 120px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

// 空状态 - 极简
.empty-tip {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 14px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

// 回到顶部 - 简约
.back-top {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 40px;
  height: 40px;
  background: #f5f5f5;
  color: #666;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
  z-index: 999;

  &:hover {
    background: #2f54eb;
    color: #fff;
  }
}

// 分页 - 简约适配
.pagination-container {
  margin-top: 30px;
  text-align: right;
}

// 移动端适配 - 简约布局
@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }

  .topic-card {
    flex-direction: column;
    padding: 15px;
  }

  .topic-content {
    padding-right: 0;
    margin-bottom: 15px;
  }

  .topic-thumbnail {
    width: 100%;
    height: auto;
  }

  .search-filter-bar {
    gap: 10px;
  }
}
</style>
