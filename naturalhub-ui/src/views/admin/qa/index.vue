<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409EFF;">
              <i class="el-icon-chat-dot-round"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalQa || 0 }}</div>
              <div class="stat-label">问答总次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67C23A;">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalUsers || 0 }}</div>
              <div class="stat-label">提问用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #E6A23C;">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.todayQa || 0 }}</div>
              <div class="stat-label">今日问答</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #F56C6C;">
              <i class="el-icon-star-off"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.avgResponseTime || 0 }}s</div>
              <div class="stat-label">平均响应时间</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

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
      <el-form-item label="问题" prop="question">
        <el-input
          v-model="queryParams.question"
          placeholder="请输入问题关键词"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="问答类型" prop="qaType">
        <el-select v-model="queryParams.qaType" placeholder="请选择" clearable size="small">
          <el-option label="普通问答" :value="1" />
          <el-option label="知识图谱" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="提问时间">
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
    <el-table v-loading="loading" :data="qaList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="用户名" align="center" prop="username" width="120" />
      <el-table-column label="问题" align="center" prop="question" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="回答" align="center" prop="answer" :show-overflow-tooltip="true" min-width="250" />
      <el-table-column label="类型" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.qaType === 1 ? 'primary' : 'success'" size="small">
            {{ scope.row.qaType === 1 ? '普通问答' : '知识图谱' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="IP地址" align="center" prop="ipAddress" width="130" />
      <el-table-column label="提问时间" align="center" prop="createTime" width="160">
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
    <el-dialog title="问答详情" :visible.sync="open" width="800px" append-to-body>
      <el-descriptions :column="2" border v-if="form">
        <el-descriptions-item label="用户名">{{ form.username }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ form.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="问答类型">
          <el-tag :type="form.qaType === 1 ? 'primary' : 'success'" size="small">
            {{ form.qaType === 1 ? '普通问答' : '知识图谱问答' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提问时间">{{ parseTime(form.createTime) }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider content-position="left">用户问题</el-divider>
      <div class="content-box">{{ form.question }}</div>
      
      <el-divider content-position="left">AI回答</el-divider>
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
  name: "QaManagement",
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      qaList: [],
      open: false,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        username: null,
        question: null,
        qaType: null
      },
      form: {},
      statistics: {
        totalQa: 0,
        totalUsers: 0,
        todayQa: 0,
        avgResponseTime: 0
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
        this.qaList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    
    getStatistics() {
      request({
        url: '/admin/qa/statistics',
        method: 'get'
      }).then(response => {
        this.statistics = response.data
      })
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
      this.$confirm('是否确认删除选中的问答记录？', "警告", {
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
      this.$confirm('是否确认导出所有问答记录数据？', "警告", {
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
