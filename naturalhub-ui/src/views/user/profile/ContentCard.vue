<template>
  <el-card class="content-card" shadow="hover" @click.native="handleClick">
    <div class="card-header">
      <div class="card-badges">
        <el-tag v-if="item.isTop === '1'" type="danger" size="mini" effect="dark">置顶</el-tag>
        <el-tag v-if="item.isEssence === '1'" type="warning" size="mini" effect="dark">精华</el-tag>
        <el-tag :type="getStatusType(item.auditStatus)" size="mini">{{ getStatusLabel(item.auditStatus) }}</el-tag>
        <el-tag v-if="contentType === 'record'" type="success" size="mini"><i class="el-icon-view"></i> 观察记录</el-tag>
        <el-tag v-else-if="contentType === 'survey'" type="primary" size="mini"><i class="el-icon-map-location"></i> 野外调查</el-tag>
        <el-tag v-else-if="contentType === 'identification'" type="warning" size="mini"><i class="el-icon-search"></i> 物种鉴定</el-tag>
      </div>
      <div class="card-meta">
        <span class="meta-time"><i class="el-icon-time"></i> {{ parseTime(item.createTime, '{y}-{m}-{d}') }}</span>
        <span v-if="item.location" class="meta-location"><i class="el-icon-location"></i> {{ item.location }}</span>
      </div>
    </div>

    <h3 class="card-title">{{ item.title }}</h3>

    <div class="card-content">
      <p>{{ truncate(item.description || item.content, 200) }}</p>
    </div>

    <div class="card-stats">
      <span><i class="el-icon-view"></i> {{ item.viewCount || 0 }}</span>
      <span><i class="el-icon-thumb"></i> {{ item.likeCount || 0 }}</span>
      <span><i class="el-icon-chat-dot-round"></i> {{ item.commentCount || item.answerCount || 0 }}</span>
      <span><i class="el-icon-star-off"></i> {{ item.collectCount || 0 }}</span>
    </div>
  </el-card>
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
  methods: {
    handleClick() {
      this.$emit('click', this.item)
    },
    getStatusLabel(status) {
      if (status === '0' || status === 0) return '待审核'
      if (status === '1' || status === 1) return '已通过'
      if (status === '2' || status === 2) return '未通过'
      return '未知状态'
    },
    getStatusType(status) {
      if (status === '0' || status === 0) return 'info'
      if (status === '1' || status === 1) return 'success'
      if (status === '2' || status === 2) return 'danger'
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
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;

    .card-badges {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    .card-meta {
      display: flex;
      gap: 12px;
      font-size: 13px;
      color: #909399;

      span {
        display: inline-flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .card-title {
    font-size: 18px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0 0 12px 0;
    line-height: 1.4;
  }

  .card-content {
    margin-bottom: 12px;

    p {
      color: #606266;
      line-height: 1.6;
      margin: 0;
    }
  }

  .card-stats {
    display: flex;
    gap: 24px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
    font-size: 14px;
    color: #909399;

    span {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: #409EFF;
      }

      i {
        font-size: 16px;
      }
    }
  }
}
</style>
