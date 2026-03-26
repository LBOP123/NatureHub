<template>
  <div class="content-card" @click="handleClick">
    <!-- 顶部：徽章（横跨全宽） -->
    <div class="card-header">
      <div class="card-badges">
        <span v-if="item.isTop === '1'" class="badge badge-top">置顶</span>
        <span v-if="item.isEssence === '1'" class="badge badge-essence">精华</span>
        <span class="badge" :class="getTypeClass(contentType)">{{ getTypeLabel(contentType) }}</span>
      </div>
    </div>

    <!-- 主体：左侧文字 + 右侧缩略图 -->
    <div class="card-body">
      <!-- 左：标题 + 时间地址 + 摘要 -->
      <div class="card-main">
        <h3 class="card-title">{{ item.title }}</h3>
        <div class="card-meta">
          <span class="meta-time"><i class="el-icon-time"></i> {{ parseTime(item.createTime, '{y}-{m}-{d}') }}</span>
          <span v-if="item.location" class="meta-location"><i class="el-icon-location"></i> {{ item.location }}</span>
        </div>
        <div class="card-content">
          <p>{{ truncate(item.description || item.content || item.findings, 120) }}</p>
        </div>
      </div>

      <!-- 右：缩略图（顶部与标题对齐，底部与内容底对齐，stretch） -->
      <div v-if="coverImage" class="card-thumbnail">
        <img :src="coverImage" alt="封面" />
      </div>
    </div>

    <!-- 统计数字 -->
    <div class="card-stats">
      <span><i class="el-icon-view"></i> {{ item.viewCount || 0 }}</span>
      <span><i class="el-icon-thumb"></i> {{ item.likeCount || 0 }}</span>
      <span><i class="el-icon-chat-dot-round"></i> {{ item.commentCount || item.answerCount || 0 }}</span>
      <span><i class="el-icon-star-off"></i> {{ item.collectCount || 0 }}</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ContentCard',
  props: {
    item: {
      type: Object,
      required: true
    },
    contentType: {
      type: String,
      required: true,
      validator: (value) => ['record', 'survey', 'identification'].includes(value)
    }
  },
  computed: {
    coverImage() {
      const raw = this.item.images
      if (!raw) return null
      try {
        let arr = raw
        if (typeof raw === 'string') {
          arr = raw.trim().startsWith('[') ? JSON.parse(raw) : raw.split(',').map(s => s.trim()).filter(Boolean)
        }
        if (!Array.isArray(arr) || arr.length === 0) return null
        const url = arr[0]
        if (!url) return null
        if (url.startsWith('http://') || url.startsWith('https://')) return url
        return (process.env.VUE_APP_BASE_API || '') + url
      } catch (e) {
        return null
      }
    }
  },
  methods: {
    handleClick() {
      this.$emit('click', this.item)
    },
    getTypeLabel(type) {
      if (type === 'record') return '观察记录'
      if (type === 'survey') return '野外调查'
      if (type === 'identification') return '物种鉴定'
      return ''
    },
    getTypeClass(type) {
      if (type === 'record') return 'badge-record'
      if (type === 'survey') return 'badge-survey'
      if (type === 'identification') return 'badge-identification'
      return ''
    },
    truncate(value, length) {
      if (!value) return ''
      if (value.length <= length) return value
      return value.substring(0, length) + '...'
    }
  }
}
</script>

<style lang="scss" scoped>
.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  transition: all 0.25s ease;
  border: 1px solid #f0f0f0;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
    border-color: #e0e0e0;

    .card-thumbnail img {
      transform: scale(1.04);
    }
  }
}

// 顶部：仅徽章
.card-header {
  margin-bottom: 12px;

  .card-badges {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
    align-items: center;
  }
}

// 时间地址：在标题下方，缩略图左边
.card-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
  flex-wrap: wrap;

  span {
    display: inline-flex;
    align-items: center;
    gap: 3px;
  }
}

// 右侧缩略图：正方形，顶部与标题对齐
.card-thumbnail {
  flex-shrink: 0;
  width: 150px;
  height: 110px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f5f5;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.3s ease;
  }
}

// 让card-body顶部对齐（图片与标题顶对齐）
.card-body {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 12px;
}

// 左侧：标题 + 摘要，撑满剩余空间
.card-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
  margin: 0 0 8px 0;
  line-height: 1.5;
  transition: color 0.2s;

  &:hover {
    color: #2f54eb;
  }
}

.card-content {
  flex: 1;   // 撑满，使摘要区域与图片等高

  p {
    font-size: 14px;
    color: #666;
    line-height: 1.7;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

// 统计
.card-stats {
  display: flex;
  gap: 20px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
  font-size: 13px;
  color: #909399;

  span {
    display: inline-flex;
    align-items: center;
    gap: 4px;

    i { font-size: 14px; }
  }
}

// 徽章 — 与社群配色一致
.badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
  font-weight: 500;
  line-height: 1.6;

  &.badge-top            { background: #ff7d00; }
  &.badge-essence        { background: #00b42a; }
  &.badge-record         { background: #67C23A; }
  &.badge-survey         { background: #409EFF; }
  &.badge-identification { background: #E6A23C; }
}

@media (max-width: 600px) {
  .card-thumbnail {
    width: 80px;
    height: 80px;
  }
}
</style>
