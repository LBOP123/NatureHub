<template>
  <div>
    <el-descriptions title="鉴定求助信息" :column="1" border size="small">
      <el-descriptions-item v-if="data.observationTime" label="观察时间">
        <i class="el-icon-time"></i> {{ parseTime(data.observationTime, '{y}-{m}-{d} {h}:{i}') }}
      </el-descriptions-item>
      <el-descriptions-item v-if="data.location" label="观察地点">
        <i class="el-icon-location"></i> {{ data.location }}
      </el-descriptions-item>
      <el-descriptions-item v-if="data.habitat" label="生境">{{ data.habitat }}</el-descriptions-item>
      <el-descriptions-item v-if="data.features" label="特征描述">{{ data.features }}</el-descriptions-item>
      <el-descriptions-item v-if="data.status" label="鉴定状态">
        <el-tag :type="statusType(data.status)" size="small">{{ statusMap[data.status] || data.status }}</el-tag>
      </el-descriptions-item>
    </el-descriptions>
    <div v-if="data.description" class="desc-section">
      <p class="desc-label"><i class="el-icon-document"></i> 详细描述</p>
      <p class="desc-text">{{ data.description }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SourceIdentification',
  props: {
    data: { type: Object, required: true }
  },
  data() {
    return {
      statusMap: { pending: '待鉴定', resolved: '已解决', closed: '已关闭' }
    }
  },
  methods: {
    statusType(s) {
      return { pending: 'warning', resolved: 'success', closed: 'info' }[s] || ''
    }
  }
}
</script>

<style lang="scss" scoped>
.desc-section { background: #f9fafb; border-radius: 6px; padding: 12px 16px; margin-top: 12px; }
.desc-label { font-size: 13px; font-weight: 600; color: #606266; margin: 0 0 6px 0; }
.desc-text { font-size: 14px; color: #4a4a5a; line-height: 1.7; white-space: pre-wrap; margin: 0; }
</style>
