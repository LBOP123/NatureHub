<template>
  <div class="survey-list-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="queryParams.title" placeholder="请输入标题" clearable size="small" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="调查地点" prop="location">
          <el-input v-model="queryParams.location" placeholder="请输入地点" clearable size="small" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="生境类型" prop="habitatType">
          <el-select v-model="queryParams.habitatType" placeholder="请选择" clearable size="small">
            <el-option v-for="dict in habitatTypeOptions" :key="dict.dictValue" :label="dict.dictLabel" :value="parseInt(dict.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态" prop="auditStatus">
          <el-select v-model="queryParams.auditStatus" placeholder="请选择" clearable size="small">
            <el-option v-for="dict in auditStatusOptions" :key="dict.dictValue" :label="dict.dictLabel" :value="parseInt(dict.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card class="toolbar-card" shadow="never">
      <el-row :gutter="10">
        <el-col :span="1.5"><el-button type="primary" icon="el-icon-plus" size="small" @click="handleAdd">创建调查</el-button></el-col>
        <el-col :span="1.5"><el-button type="success" icon="el-icon-download" size="small" @click="handleExport">导出</el-button></el-col>
        <el-col :span="1.5" class="view-switch">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="card"><i class="el-icon-menu"></i> 卡片</el-radio-button>
            <el-radio-button label="list"><i class="el-icon-s-operation"></i> 列表</el-radio-button>
          </el-radio-group>
        </el-col>
      </el-row>
    </el-card>
    <div v-if="viewMode === 'card'" class="card-view">
      <el-row :gutter="20">
        <el-col v-for="item in surveyList" :key="item.surveyId" :xs="24" :sm="12" :md="8" :lg="6" class="card-col">
          <el-card class="survey-card" :body-style="{ padding: '0px' }" shadow="hover">
            <div class="card-image" @click="handleView(item)">
              <img v-if="item.coverImage" :src="item.coverImage" class="image">
              <div v-else class="no-image"><i class="el-icon-map-location"></i></div>
              <div class="status-tag"><dict-tag :options="auditStatusOptions" :value="item.auditStatus" /></div>
              <div class="habitat-tag"><el-tag size="mini" type="success">{{ getHabitatTypeText(item.habitatType) }}</el-tag></div>
            </div>
            <div class="card-content">
              <div class="card-title" @click="handleView(item)">{{ item.title }}</div>
              <div class="card-info">
                <div class="info-item"><i class="el-icon-location-outline"></i><span>{{ item.location }}</span></div>
                <div class="info-item"><i class="el-icon-date"></i><span>{{ item.surveyDate }}</span></div>
                <div class="info-item"><i class="el-icon-s-data"></i><span>发现 {{ item.speciesCount || 0 }} 种</span></div>
              </div>
              <div class="card-actions">
                <el-button-group>
                  <el-button size="mini" icon="el-icon-view" @click="handleView(item)">查看</el-button>
                  <el-button v-if="item.auditStatus===0||item.auditStatus===3" size="mini" icon="el-icon-edit" @click="handleEdit(item)">编辑</el-button>
                  <el-button v-if="item.auditStatus===2&&item.isShared!==1" size="mini" type="success" icon="el-icon-share" @click="handleShare(item)">分享</el-button>
                  <el-button v-if="item.auditStatus===0" size="mini" type="primary" icon="el-icon-s-promotion" @click="handleSubmitReview(item)">提交审核</el-button>
                  <el-button size="mini" type="danger" icon="el-icon-delete" @click="handleDelete(item)">删除</el-button>
                </el-button-group>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="surveyList.length===0" description="暂无调查记录"><el-button type="primary" @click="handleAdd">立即创建</el-button></el-empty>
    </div>
    <el-card v-else class="list-view" shadow="never">
      <el-table v-loading="loading" :data="surveyList" @row-click="handleView" style="width:100%">
        <el-table-column label="标题" prop="title" min-width="150" show-overflow-tooltip />
        <el-table-column label="生境类型" width="100" align="center">
          <template slot-scope="scope"><el-tag size="mini" type="success">{{ getHabitatTypeText(scope.row.habitatType) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="调查地点" prop="location" min-width="120" show-overflow-tooltip />
        <el-table-column label="调查日期" prop="surveyDate" width="120" />
        <el-table-column label="物种数" width="80" align="center">
          <template slot-scope="scope"><el-tag size="mini" type="info">{{ scope.row.speciesCount||0 }}</el-tag></template>
        </el-table-column>
        <el-table-column label="审核状态" width="110" align="center">
          <template slot-scope="scope"><dict-tag :options="auditStatusOptions" :value="scope.row.auditStatus" /></template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-view" @click.stop="handleView(scope.row)">查看</el-button>
            <el-button v-if="scope.row.auditStatus===0||scope.row.auditStatus===3" size="mini" type="text" icon="el-icon-edit" @click.stop="handleEdit(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.auditStatus===2&&scope.row.isShared!==1" size="mini" type="text" icon="el-icon-share" @click.stop="handleShare(scope.row)">分享</el-button>
            <el-button v-if="scope.row.auditStatus===0" size="mini" type="text" icon="el-icon-s-promotion" @click.stop="handleSubmitReview(scope.row)">提交审核</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listSurvey, delSurvey, shareSurvey, updateSurvey } from '@/api/user/survey'
export default {
  name: 'SurveyList',
  data() {
    return {
      queryParams: { pageNum: 1, pageSize: 12, title: null, location: null, habitatType: null, auditStatus: null },
      dateRange: [], surveyList: [], total: 0, loading: false, viewMode: 'card',
      auditStatusOptions: [], habitatTypeOptions: [], surveyMethodOptions: []
    }
  },
  created() {
    this.getDicts('nh_audit_status').then(res => { this.auditStatusOptions = res.data })
    this.getDicts('nh_habitat_type').then(res => { this.habitatTypeOptions = res.data })
    this.getDicts('nh_survey_method').then(res => { this.surveyMethodOptions = res.data })
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      const params = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) { params.beginTime = this.dateRange[0]; params.endTime = this.dateRange[1] }
      listSurvey(params).then(response => {
        this.surveyList = response.rows.map(item => {
          if (item.images) { const imgs = item.images.split(','); item.coverImage = imgs[0] || null }
          return item
        })
        this.total = response.total; this.loading = false
      })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.dateRange = []; this.resetForm('queryForm'); this.handleQuery() },
    handleAdd() { this.$router.push('/user/survey/create') },
    handleView(row) { this.$router.push({ path: '/user/survey/detail', query: { id: row.surveyId } }) },
    handleEdit(row) { this.$router.push('/user/survey/create?id=' + row.surveyId) },
    handleSubmitReview(row) {
      this.$confirm('提交审核后将无法修改，是否继续?', '提示', { type: 'warning' })
        .then(() => updateSurvey({ surveyId: row.surveyId, auditStatus: 1 }))
        .then(() => { this.getList(); this.$message.success('提交审核成功，请等待管理员审核') })
        .catch(() => {})
    },
    handleShare(row) {
      this.$confirm('确认分享该调查记录到社群?', '提示', { type: 'info' })
        .then(() => shareSurvey(row.surveyId)).then(() => { this.$message.success('分享成功'); this.getList() }).catch(() => {})
    },
    handleDelete(row) {
      this.$confirm('是否确认删除该调查记录?', '警告', { type: 'warning' })
        .then(() => delSurvey(row.surveyId)).then(() => { this.getList(); this.$message.success('删除成功') }).catch(() => {})
    },
    handleExport() { this.$message.info('导出功能开发中...') },
    getHabitatTypeText(val) {
      const d = (this.habitatTypeOptions || []).find(d => parseInt(d.dictValue) === val)
      return d ? d.dictLabel : (val || '-')
    },
    getSurveyMethodText(val) {
      const d = (this.surveyMethodOptions || []).find(d => parseInt(d.dictValue) === val)
      return d ? d.dictLabel : (val || '-')
    }
  }
}
</script>

<style lang="scss" scoped>
.survey-list-container { padding: 20px; }
.search-card, .toolbar-card { margin-bottom: 20px; }
.view-switch { float: right; }
.card-view {
  .card-col { margin-bottom: 20px; }
  .survey-card {
    height: 100%; cursor: pointer; transition: all 0.3s;
    &:hover { transform: translateY(-5px); }
    .card-image {
      position: relative; height: 200px; overflow: hidden; background: #f5f7fa;
      .image { width: 100%; height: 100%; object-fit: cover; }
      .no-image { display: flex; align-items: center; justify-content: center; height: 100%; font-size: 48px; color: #dcdfe6; }
      .status-tag { position: absolute; top: 10px; right: 10px; }
      .habitat-tag { position: absolute; top: 10px; left: 10px; }
    }
    .card-content {
      padding: 15px;
      .card-title { font-size: 16px; font-weight: bold; margin-bottom: 10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #303133; &:hover { color: #409EFF; } }
      .card-info { margin-bottom: 15px; .info-item { display: flex; align-items: center; margin-bottom: 8px; font-size: 13px; color: #606266; i { margin-right: 5px; color: #909399; } } }
      .card-actions { border-top: 1px solid #ebeef5; padding-top: 10px; .el-button-group { display: flex; flex-wrap: wrap; gap: 5px; } }
    }
  }
}
.list-view {
  .table-image { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
  .table-no-image { width: 60px; height: 60px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; border-radius: 4px; font-size: 24px; color: #dcdfe6; }
}
</style>
