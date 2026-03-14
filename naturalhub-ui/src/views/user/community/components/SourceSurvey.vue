<template>
  <div>
    <el-descriptions title="野外调查信息" :column="1" border size="small">
      <el-descriptions-item v-if="data.surveyDate" label="调查日期">
        <i class="el-icon-date"></i> {{ data.surveyDate }}
      </el-descriptions-item>
      <el-descriptions-item v-if="data.location" label="调查地点">
        <i class="el-icon-location"></i> {{ data.location }}
      </el-descriptions-item>
      <el-descriptions-item v-if="data.weather" label="天气情况">
        {{ data.weather }}<span v-if="data.temperature">，{{ data.temperature }}℃</span>
      </el-descriptions-item>
      <el-descriptions-item v-if="data.habitatType != null" label="生境类型">
        {{ getHabitatType(data.habitatType) }}
      </el-descriptions-item>
      <el-descriptions-item v-if="data.surveyMethod != null" label="调查方法">
        {{ getSurveyMethod(data.surveyMethod) }}
      </el-descriptions-item>
      <el-descriptions-item v-if="data.speciesCount != null" label="发现物种数">
        <span class="species-name">{{ data.speciesCount }} 种</span>
      </el-descriptions-item>
    </el-descriptions>
    <div v-if="data.findings" class="desc-section">
      <p class="desc-label"><i class="el-icon-discover"></i> 主要发现</p>
      <p class="desc-text">{{ data.findings }}</p>
    </div>
    <div v-if="data.description" class="desc-section">
      <p class="desc-label"><i class="el-icon-document"></i> 调查描述</p>
      <p class="desc-text">{{ data.description }}</p>
    </div>
  </div>
</template>

<script>
import { getDicts } from '@/api/system/dict/data'

export default {
  name: 'SourceSurvey',
  props: {
    data: { type: Object, required: true }
  },
  data() {
    return {
      habitatTypeOptions: [],
      surveyMethodOptions: []
    }
  },
  created() {
    this.getDicts('nh_habitat_type').then(res => { this.habitatTypeOptions = res.data || [] })
    this.getDicts('nh_survey_method').then(res => { this.surveyMethodOptions = res.data || [] })
  },
  methods: {
    getDicts(dictType) {
      return getDicts(dictType)
    },
    // 生境类型
    getHabitatType(val) {
      const item = this.habitatTypeOptions.find(d => d.dictValue == val);
      return item ? item.dictLabel : "未知";
    },
    // 调查方法
    getSurveyMethod(val) {
      const item = this.surveyMethodOptions.find(d => d.dictValue == val);
      return item ? item.dictLabel : "未知";
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
