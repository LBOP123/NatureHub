<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67C23A;">
              <i class="el-icon-collection"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalKnowledge || 0 }}</div>
              <div class="stat-label">知识库查询次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #E6A23C;">
              <i class="el-icon-star-on"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalSpecies || 0 }}</div>
              <div class="stat-label">涉及物种数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409EFF;">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.todayKnowledge || 0 }}</div>
              <div class="stat-label">今日查询</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #F56C6C;">
              <i class="el-icon-trophy"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.hotSpecies || '-' }}</div>
              <div class="stat-label">最热物种</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门知识点 -->
    <el-card class="mb20">
      <div slot="header">
        <span>🔥 热门知识点排行</span>
      </div>
      <el-table :data="hotSpeciesList" style="width: 100%">
        <el-table-column type="index" label="排名" width="80" align="center" />
        <el-table-column label="物种名称" align="center" prop="species" />
        <el-table-column label="查询次数" align="center" prop="count" width="150">
          <template slot-scope="scope">
            <el-tag :type="scope.$index < 3 ? 'danger' : 'info'">{{ scope.row.count }}次</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="占比" align="center" width="200">
          <template slot-scope="scope">
            <el-progress :percentage="getPercentage(scope.row.count)" :color="getProgressColor(scope.$index)"></el-progress>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px">
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="queryParams.username"
          placeholder="请输入用户名"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="物种名" prop="relatedSpecies">
        <el-input
          v-model="queryParams.relatedSpecies"
          placeholder="请输入物种名"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="问题" prop="question">
        <el-input
          v-model="queryParams.question"
          placeholder="请输入问题关键词"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="查询时间">
        <el-date-picker
          v-model="dateRange"
          size="small"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="knowledgeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="用户名" align="center" prop="username" width="120" />
      <el-table-column label="问题" align="center" prop="question" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="相关物种" align="center" prop="relatedSpecies" width="150">
        <template slot-scope="scope">
          <el-tag type="success" size="small" v-if="scope.row.relatedSpecies">{{ scope.row.relatedSpecies }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="回答摘要" align="center" prop="answer" :show-overflow-tooltip="true" min-width="250" />
      <el-table-column label="IP地址" align="center" prop="ipAddress" width="130" />
      <el-table-column label="查询时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 查看详情对话框 -->
    <el-dialog title="知识库查询详情" :visible.sync="open" width="800px" append-to-body>
      <el-descriptions :column="2" border v-if="form">
        <el-descriptions-item label="用户名">{{ form.username }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ form.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="相关物种">
          <el-tag type="success" size="small" v-if="form.relatedSpecies">{{ form.relatedSpecies }}</el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="查询时间">{{ parseTime(form.createTime) }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider content-position="left">用户问题</el-divider>
      <div class="content-box">{{ form.question }}</div>
      
      <el-divider content-position="left">知识库回答</el-divider>
      <div class="content-box answer-box">{{ form.answer }}</div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: "KnowledgeManagement",
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      knowledgeList: [],
      hotSpeciesList: [],
      open: false,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        username: null,
        question: null,
        relatedSpecies: null,
        qaType: 2
      },
      form: {},
      statistics: {
        totalKnowledge: 0,
        totalSpecies: 0,
        todayKnowledge: 0,
        hotSpecies: '-'
      }
    }
  },
  created() {
    this.getList()
    this.getStatistics()
  },
  methods: {
    getList() {
      this.loading = true
      request({
        url: '/admin/qa/list',
        method: 'get',
        params: this.queryParams
      }).then(response => {
        this.knowledgeList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    
    getStatistics() {
      request({
        url: '/admin/qa/statistics',
        method: 'get'
      }).then(response => {
        const data = response.data
        this.statistics.totalKnowledge = data.totalQa
        this.statistics.todayKnowledge = data.todayQa || 0
        this.hotSpeciesList = data.hotSpecies || []
        this.statistics.totalSpecies = this.hotSpeciesList.length
        this.statistics.hotSpecies = this.hotSpeciesList.length > 0 ? this.hotSpeciesList[0].species : '-'
      })
    },
    
    getPercentage(count) {
      if (this.hotSpeciesList.length === 0) return 0
      const max = this.hotSpeciesList[0].count
      return Math.round((count / max) * 100)
    },
    
    getProgressColor(index) {
      if (index === 0) return '#f56c6c'
      if (index === 1) return '#e6a23c'
      if (index === 2) return '#409eff'
      return '#67c23a'
    },
    
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.multiple = !selection.length
    },
    
    handleView(row) {
      this.form = row
      this.open = true
    },
    
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$confirm('是否确认删除选中的知识库查询记录？', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        return request({
          url: '/admin/qa/' + ids,
          method: 'delete'
        })
      }).then(() => {
        this.getList()
        this.getStatistics()
        this.$message.success("删除成功")
      }).catch(() => {})
    },
    
    handleExport() {
      this.$confirm('是否确认导出所有知识库查询记录？', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.$message.success("导出功能开发中")
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.mb20 {
  margin-bottom: 20px;
}

.mb8 {
  margin-bottom: 8px;
}

.stat-card {
  display: flex;
  align-items: center;
  
  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: #fff;
    margin-right: 16px;
  }
  
  .stat-content {
    flex: 1;
  }
  
  .stat-value {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 4px;
  }
  
  .stat-label {
    font-size: 14px;
    color: #909399;
  }
}

.content-box {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 300px;
  overflow-y: auto;
}

.answer-box {
  background: #e8f4f8;
}
</style>
