<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb20">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#409EFF"><i class="el-icon-box"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalTasks || stats.total || 0 }}</div>
              <div class="stat-label">任务总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#67C23A"><i class="el-icon-circle-check"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.success || stats.succeeded || 0 }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#E6A23C"><i class="el-icon-time"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.running || stats.inProgress || 0 }}</div>
              <div class="stat-label">生成中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#F56C6C"><i class="el-icon-circle-close"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.fail || stats.failed || 0 }}</div>
              <div class="stat-label">失败数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="70px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable size="small" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="任务名称" prop="taskName">
        <el-input v-model="queryParams.taskName" placeholder="请输入任务名称" clearable size="small" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="taskStatus">
        <el-select v-model="queryParams.taskStatus" placeholder="全部" clearable size="small" style="width:110px">
          <el-option label="等待中" value="1" />
          <el-option label="生成中" value="2" />
          <el-option label="已完成" value="3" />
          <el-option label="已失败" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="公开展示" prop="isPublic">
        <el-select v-model="queryParams.isPublic" placeholder="全部" clearable size="small" style="width:100px">
          <el-option label="已公开" :value="1" />
          <el-option label="未公开" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini"
                   :disabled="selectedIds.length === 0" @click="handleBatchDelete"
                   v-hasPermi="['admin:mark3d:remove']">批量删除</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" border fit>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" prop="id" width="70" align="center" />
      <el-table-column label="缩略图" width="80" align="center">
        <template slot-scope="{row}">
          <el-image v-if="row.thumbnailQiniu || row.thumbnailUrl"
                    :src="row.thumbnailQiniu || row.thumbnailUrl"
                    :preview-src-list="[row.thumbnailQiniu || row.thumbnailUrl]"
                    style="width:60px;height:60px;border-radius:4px;" fit="cover" />
          <span v-else style="color:#C0C4CC">-</span>
        </template>
      </el-table-column>
      <el-table-column label="任务名称" prop="taskName" min-width="130" show-overflow-tooltip>
        <template slot-scope="{row}">{{ row.taskName || ('任务 #' + row.id) }}</template>
      </el-table-column>
      <el-table-column label="用户" prop="username" align="center" min-width="110" />
      <el-table-column label="状态" align="center" min-width="100">
        <template slot-scope="{row}">
          <el-tag :type="statusTagType(row.taskStatus)" size="small">{{ statusLabel(row.taskStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="进度" align="center" min-width="80">
        <template slot-scope="{row}">
          {{ row.taskStatus === '3' ? '100%' : row.taskStatus === '4' ? '-' : (row.progress || 0) + '%' }}
        </template>
      </el-table-column>
      <el-table-column label="公开展示" align="center" min-width="100">
        <template slot-scope="{row}">
          <el-switch v-if="row.taskStatus === '3'"
            :value="row.isPublic === 1" active-color="#43cea2" inactive-color="#dcdfe6"
            :loading="row._toggling"
            @change="(val) => handleTogglePublic(row, val)"
            v-hasPermi="['admin:mark3d:edit']" />
          <span v-else style="color:#C0C4CC">-</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" align="center" min-width="160" />
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template slot-scope="{row}">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(row)" v-hasPermi="['admin:mark3d:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-refresh"
                     v-if="row.taskStatus !== '3' && row.taskStatus !== '4'"
                     @click="handleSync(row)" v-hasPermi="['admin:mark3d:edit']">同步</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete"
                     style="color:#F56C6C" @click="handleDelete(row)" v-hasPermi="['admin:mark3d:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total"
                :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
                @pagination="fetchList" />

    <!-- 详情弹窗 -->
    <el-dialog title="3D任务详情" :visible.sync="detailVisible" width="900px" append-to-body>
      <div v-if="detailRow" class="detail-dialog">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="任务ID">{{ detailRow.id }}</el-descriptions-item>
          <el-descriptions-item label="腾讯云JobId">{{ detailRow.meshyTaskId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ detailRow.taskName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ detailRow.username }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detailRow.taskStatus)">{{ statusLabel(detailRow.taskStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="进度">{{ detailRow.progress || 0 }}%</el-descriptions-item>
          <el-descriptions-item label="公开展示">
            <el-tag :type="detailRow.isPublic === 1 ? 'success' : 'info'">
              {{ detailRow.isPublic === 1 ? '公开展示中' : '未公开' }}
            </el-tag>
            <el-switch v-if="detailRow.taskStatus === '3'" style="margin-left:12px"
              :value="detailRow.isPublic === 1" active-color="#43cea2" inactive-color="#dcdfe6"
              @change="(val) => handleTogglePublicDetail(val)"
              v-hasPermi="['admin:mark3d:edit']" />
          </el-descriptions-item>
          <el-descriptions-item label="GLB（七牛云永久）" :span="2">
            <el-link v-if="detailRow.modelUrlGlbQiniu" type="success" :href="detailRow.modelUrlGlbQiniu" target="_blank">{{ detailRow.modelUrlGlbQiniu }}</el-link>
            <span v-else style="color:#C0C4CC">暂无七牛云地址</span>
          </el-descriptions-item>
          <el-descriptions-item label="GLB（腾讯云临时）" :span="2">
            <el-link v-if="detailRow.modelUrlGlb" type="warning" :href="detailRow.modelUrlGlb" target="_blank">{{ detailRow.modelUrlGlb }}</el-link>
            <span v-else style="color:#C0C4CC">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="错误信息" :span="2">{{ detailRow.errorMessage || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailRow.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detailRow.updateTime }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="detailRow.frontImageUrl" style="margin-top:16px">
          <div style="font-size:13px;color:#606266;margin-bottom:8px">参考图片</div>
          <el-image :src="detailRow.frontImageUrl" :preview-src-list="[detailRow.frontImageUrl]"
                    fit="cover" style="width:120px;height:120px;border-radius:6px;" />
        </div>
        <div v-if="detailRow.modelUrlGlbQiniu || detailRow.modelUrlGlb" class="model-preview">
          <div style="font-size:13px;color:#606266;margin:16px 0 8px">3D模型预览</div>
          <model-viewer :src="detailRow.modelUrlGlbQiniu || detailRow.modelUrlGlb"
                        alt="3D模型" auto-rotate camera-controls shadow-intensity="1"
                        style="width:100%;height:400px;background:#1a1f2e;border-radius:8px;" />
        </div>
      </div>
      <div slot="footer">
        <el-button @click="detailVisible = false">关 闭</el-button>
        <el-button v-if="detailRow && detailRow.taskStatus !== '3' && detailRow.taskStatus !== '4'"
                   type="primary" icon="el-icon-refresh" @click="handleSync(detailRow)">同步状态</el-button>
      </div>
    </el-dialog>
  </div>
</template><script>
import { listMark3dTasksAdmin, getMark3dTaskAdmin, syncMark3dTaskStatusAdmin,
  deleteMark3dTaskAdmin, getMark3dStatistics, updateMark3dPublicAdmin } from '@/api/user/mark3d'
import Pagination from '@/components/Pagination'

export default {
  name: 'Mark3dAdmin',
  components: { Pagination },
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10, username: '', taskName: '', taskStatus: '', isPublic: null },
      selectedIds: [],
      stats: {},
      detailVisible: false,
      detailRow: null
    }
  },
  mounted() {
    this.loadModelViewer()
    this.fetchStats()
    this.fetchList()
  },
  methods: {
    loadModelViewer() {
      if (typeof customElements !== 'undefined' && !customElements.get('model-viewer')) {
        const script = document.createElement('script')
        script.type = 'module'
        script.src = 'https://unpkg.com/@google/model-viewer/dist/model-viewer.min.js'
        document.head.appendChild(script)
      }
    },
    fetchStats() {
      getMark3dStatistics().then(res => {
        if (res.code === 200) this.stats = res.data || {}
      }).catch(err => { console.error('统计接口失败', err) })
    },
    fetchList() {
      this.loading = true
      listMark3dTasksAdmin(this.queryParams).then(res => {
        this.tableData = (res.rows || []).map(r => ({ ...r, _toggling: false }))
        this.total = res.total || 0
      }).finally(() => { this.loading = false })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.fetchList() },
    resetQuery() { this.$refs.queryForm.resetFields(); this.handleQuery() },
    handleSelectionChange(rows) { this.selectedIds = rows.map(r => r.id) },
    handleDetail(row) {
      getMark3dTaskAdmin(row.id).then(res => {
        if (res.code === 200) { this.detailRow = res.data; this.detailVisible = true }
        else this.$message.error(res.msg || '加载详情失败')
      }).catch(e => { this.$message.error('加载详情异常：' + e.message) })
    },
    handleSync(row) {
      this.$message.info('同步中...')
      syncMark3dTaskStatusAdmin(row.id).then(res => {
        if (res.code === 200) {
          this.$message.success('同步成功')
          this.fetchList()
          this.fetchStats()
          if (this.detailVisible && this.detailRow && this.detailRow.id === row.id) {
            this.detailRow = res.data
          }
        } else {
          this.$message.error(res.msg || '同步失败')
        }
      }).catch(e => { this.$message.error('同步异常：' + e.message) })
    },
    handleTogglePublic(row, val) {
      row._toggling = true
      const isPublic = val ? 1 : 0
      updateMark3dPublicAdmin(row.id, isPublic).then(res => {
        if (res.code === 200) {
          row.isPublic = isPublic
          this.$message.success(isPublic === 1 ? '已设为公开' : '已取消公开')
        } else {
          this.$message.error(res.msg || '操作失败')
        }
      }).finally(() => { row._toggling = false })
    },
    handleTogglePublicDetail(val) {
      if (!this.detailRow) return
      const isPublic = val ? 1 : 0
      updateMark3dPublicAdmin(this.detailRow.id, isPublic).then(res => {
        if (res.code === 200) {
          this.detailRow.isPublic = isPublic
          const row = this.tableData.find(r => r.id === this.detailRow.id)
          if (row) row.isPublic = isPublic
          this.$message.success(isPublic === 1 ? '已设为公开' : '已取消公开')
        } else { this.$message.error(res.msg || '操作失败') }
      })
    },
    handleDelete(row) {
      this.$confirm('确定删除该任务吗？', '提示', { type: 'warning' }).then(() => {
        deleteMark3dTaskAdmin(row.id).then(res => {
          if (res.code === 200) { this.$message.success('删除成功'); this.fetchList(); this.fetchStats() }
          else this.$message.error(res.msg || '删除失败')
        })
      }).catch(() => {})
    },
    handleBatchDelete() {
      this.$confirm(`确定删除选中的 ${this.selectedIds.length} 条任务吗？`, '提示', { type: 'warning' }).then(() => {
        deleteMark3dTaskAdmin(this.selectedIds.join(',')).then(res => {
          if (res.code === 200) {
            this.$message.success('批量删除成功')
            this.selectedIds = []
            this.fetchList()
            this.fetchStats()
          } else { this.$message.error(res.msg || '删除失败') }
        })
      }).catch(() => {})
    },
    statusLabel(status) {
      return { '1': '等待中', '2': '生成中', '3': '已完成', '4': '已失败' }[status] || '-'
    },
    statusTagType(status) {
      return { '1': 'info', '2': 'warning', '3': 'success', '4': 'danger' }[status] || 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container { width: 100%; }
.mb20 { margin-bottom: 20px; }
.mb8  { margin-bottom: 8px; }
.stat-card {
  display: flex;
  align-items: center;
  .stat-icon {
    width: 60px; height: 60px; border-radius: 8px;
    display: flex; align-items: center; justify-content: center;
    font-size: 28px; color: #fff; margin-right: 16px;
  }
  .stat-content { flex: 1; }
  .stat-value { font-size: 24px; font-weight: bold; color: #303133; margin-bottom: 4px; }
  .stat-label { font-size: 14px; color: #909399; }
}
.model-preview { margin-top: 16px; }
::v-deep .el-table { width: 100% !important; table-layout: auto; }
</style>