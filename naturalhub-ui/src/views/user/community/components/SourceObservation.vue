<template>
  <div>
    <el-descriptions title="观察记录信息" :column="1" border size="small">
      <el-descriptions-item v-if="data.speciesName" label="物种名称">
        <span class="species-name">{{ data.speciesName }}</span>
      </el-descriptions-item>
      <el-descriptions-item v-if="data.speciesType" label="物种类型">
        <el-tag type="success" size="small">{{ speciesTypeMap[data.speciesType] || data.speciesType }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item v-if="data.observationTime" label="观察时间">
        <i class="el-icon-time"></i> {{ parseTime(data.observationTime, '{y}-{m}-{d} {h}:{i}') }}
      </el-descriptions-item>
      <el-descriptions-item v-if="data.location" label="观察地点">
        <i class="el-icon-location"></i> {{ data.location }}
      </el-descriptions-item>
      <el-descriptions-item v-if="data.habitat" label="生境描述">{{ data.habitat }}</el-descriptions-item>
    </el-descriptions>
    <div v-if="data.description" class="desc-section">
      <p class="desc-label"><i class="el-icon-document"></i> 详细描述</p>
      <p class="desc-text">{{ data.description }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SourceObservation',
  props: {
    data: { type: Object, required: true }
  },
  data() {
    return {
      speciesTypeMap: { plant: '植物', animal: '动物', fungi: '真菌', other: '其他' }
    }
  }
}
</script>

<style lang="scss" scoped>
.species-name { font-weight: 700; color: #409EFF; font-style: italic; font-size: 15px; }
.desc-section { background: #f9fafb; border-radius: 6px; padding: 12px 16px; margin-top: 12px; }
.desc-label { font-size: 13px; font-weight: 600; color: #606266; margin: 0 0 6px 0; }
.desc-text { font-size: 14px; color: #4a4a5a; line-height: 1.7; white-space: pre-wrap; margin: 0; }
</style>
