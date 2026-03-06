<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409EFF;">
              <i class="el-icon-picture"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalRecognition || 0 }}</div>
              <div class="stat-label">识别总次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67C23A;">
              <i class="el-icon-success"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.successRate || 0 }}%</div>
              <div class="stat-label">识别成功率</div>
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
              <div class="stat-value">{{ statistics.todayRecognition || 0 }}</div>
              <div class="stat-label">今日识别</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #F56C6C;">
              <i class="el-icon-star-on"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.topSpecies || '-' }}</div>
              <div class="stat-label">最常识别物种</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

<!--    &lt;!&ndash; 识别趋势图表 &ndash;&gt;-->
<!--    <el-card class="mb20">-->
<!--      <div slot="header">-->
<!--        <span>📈 识别趋势（最近7天）</span>-->
<!--      </div>-->
<!--      <div id="trendChart" style="height: 300px;"></div>-->
<!--    </el-card>-->

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
      <el-form-item label="识别结果" prop="recognitionResult">
        <el-input
          v-model="queryParams.recognitionResult"
          placeholder="请输入识别结果"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="识别状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable size="small">
          <el-option label="成功" value="success" />
          <el-option label="失败" value="fail" />
        </el-select>
      </el-form-item>
      <el-form-item label="上传时间">
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
    <el-table v-loading="loading" :data="recognitionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="缩略图" align="center" width="100">
        <template slot-scope="scope">
          <el-image
            style="width: 60px; height: 60px; border-radius: 4px;"
            :src="scope.row.imageUrl"
            :preview-src-list="[scope.row.imageUrl]"
            fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column label="用户名" align="center" prop="username" width="120" />
      <el-table-column label="识别结果" align="center" prop="recognitionResult" width="150">
        <template slot-scope="scope">
          <el-tag type="success" v-if="scope.row.recognitionResult">{{ scope.row.recognitionResult }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="置信度" align="center" prop="confidence" width="100">
        <template slot-scope="scope">
          <el-progress :percentage="(parseFloat(scope.row.confidence) * 100).toFixed(0)" :color="getConfidenceColor(scope.row.confidence)"></el-progress>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'" size="small">
            {{ scope.row.status === 'success' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
<!--      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" min-width="150" />-->
      <el-table-column label="IP地址" align="center" prop="ipAddress" width="130" />
      <el-table-column label="上传时间" align="center" prop="createTime" width="160">
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
    <el-dialog title="识别详情" :visible.sync="open" width="800px" append-to-body>
      <el-row :gutter="20" v-if="form">
        <el-col :span="12">
          <div class="image-preview">
            <el-image
              style="width: 100%; border-radius: 8px;"
              :src="form.imageUrl"
              :preview-src-list="[form.imageUrl]"
              fit="contain"
            />
          </div>
        </el-col>
        <el-col :span="12">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ form.username }}</el-descriptions-item>
            <el-descriptions-item label="识别结果">
              <el-tag type="success" v-if="form.recognitionResult">{{ form.recognitionResult }}</el-tag>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="置信度">
              <el-progress :percentage="(parseFloat(form.confidence) * 100).toFixed(0)" :color="getConfidenceColor(form.confidence)"></el-progress>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="form.status === 'success' ? 'success' : 'danger'" size="small">
                {{ form.status === 'success' ? '成功' : '失败' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="IP地址">{{ form.ipAddress }}</el-descriptions-item>
            <el-descriptions-item label="上传时间">{{ parseTime(form.createTime) }}</el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">备注信息</el-divider>
          <div class="content-box">{{ form.remark || '无' }}</div>
        </el-col>
      </el-row>

      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: "RecognitionManagement",
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      recognitionList: [],
      open: false,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        username: null,
        recognitionResult: null,
        status: null
      },
      form: {},
      statistics: {
        totalRecognition: 0,
        successRate: 0,
        todayRecognition: 0,
        topSpecies: '-',
        trendData: []
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
        url: '/admin/recognition/list',
        method: 'get',
        params: this.queryParams
      }).then(response => {
        this.recognitionList = response.rows
        this.total = response.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },

    getStatistics() {
      request({
        url: '/admin/recognition/statistics',
        method: 'get'
      }).then(response => {
        this.statistics = response.data
        this.$nextTick(() => {
          this.initChart()
        })
      })
    },

    initChart() {
      if (!this.statistics.trendData || this.statistics.trendData.length === 0) return

      const chartDom = document.getElementById('trendChart')
      if (!chartDom) return

      // 使用 echarts（需要在 main.js 中引入）
      const myChart = this.$echarts ? this.$echarts.init(chartDom) : null
      if (!myChart) return

      const option = {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: this.statistics.trendData.map(item => item.date)
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          data: this.statistics.trendData.map(item => item.count),
          type: 'line',
          smooth: true,
          areaStyle: {
            color: '#409EFF',
            opacity: 0.3
          },
          itemStyle: {
            color: '#409EFF'
          }
        }]
      }
      myChart.setOption(option)
    },

    getConfidenceColor(confidence) {
      let conf = parseFloat(confidence) || 0
      conf = conf * 100
      if (conf >= 80) return '#67c23a'
      if (conf >= 60) return '#e6a23c'
      return '#f56c6c'
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
      const ids = row.id ? [row.id] : this.ids
      this.$confirm('是否确认删除选中的识别记录？', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        return request({
          url: '/admin/recognition/' + ids.join(','),
          method: 'delete'
        })
      }).then(() => {
        this.$message.success("删除成功")
        this.getList()
        this.getStatistics()
      }).catch(() => {})
    },

    handleExport() {
      this.$confirm('是否确认导出所有识别记录？', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        return request({
          url: '/admin/recognition/export',
          method: 'get',
          params: this.queryParams
        })
      }).then(() => {
        this.$message.success("导出成功")
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

.image-preview {
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}

.content-box {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
