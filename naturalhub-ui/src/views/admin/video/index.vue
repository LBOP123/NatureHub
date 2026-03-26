<template>
  <div class="app-container">
    <!-- ???? -->
    <el-row :gutter="20" class="mb20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#409EFF"><i class="el-icon-video-camera"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">????</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#67C23A"><i class="el-icon-circle-check"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.succeeded }}</div>
              <div class="stat-label">???</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#E6A23C"><i class="el-icon-time"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.inProgress }}</div>
              <div class="stat-label">???</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#F56C6C"><i class="el-icon-circle-close"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.failed }}</div>
              <div class="stat-label">???</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ???? -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px">
      <el-form-item label="???" prop="username">
        <el-input v-model="queryParams.username" placeholder="??????" clearable size="small" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="??" prop="taskStatus">
        <el-select v-model="queryParams.taskStatus" placeholder="??" clearable size="small" style="width:110px">
          <el-option label="???" value="1" />
          <el-option label="???" value="2" />
          <el-option label="???" value="3" />
          <el-option label="???" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">??</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">??</el-button>
      </el-form-item>
    </el-form>

    <!-- ??? -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini"
          :disabled="multiple" @click="handleBatchDelete"
          v-hasPermi="['admin:video:remove']">????</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="fetchList"></right-toolbar>
    </el-row>

    <!-- ???? -->
    <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" prop="id" width="70" align="center" />
      <el-table-column label="???" prop="username" align="center" width="120" />
      <el-table-column label="????" prop="remark" min-width="200" show-overflow-tooltip />
      <el-table-column label="??" align="center" width="100">
        <template slot-scope="{row}">
          <el-tag :type="statusTagType(row.taskStatus)" size="small">{{ statusLabel(row.taskStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="????" align="center" min-width="120">
        <template slot-scope="{row}">
          <el-link v-if="row.videoUrl" type="success" :href="row.videoUrl" target="_blank" icon="el-icon-video-play">????</el-link>
          <span v-else style="color:#C0C4CC">-</span>
        </template>
      </el-table-column>
      <el-table-column label="????" prop="errorMessage" min-width="150" show-overflow-tooltip>
        <template slot-scope="{row}">
          <span style="color:#f56c6c" v-if="row.errorMessage">{{ row.errorMessage }}</span>
          <span v-else style="color:#C0C4CC">-</span>
        </template>
      </el-table-column>
      <el-table-column label="????" prop="createTime" align="center" min-width="160">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="??" width="130" align="center" fixed="right">
        <template slot-scope="{row}">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(row)">??</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C"
            @click="handleDelete(row)" v-hasPermi="['admin:video:remove']">??</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total"
      :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="fetchList" />

    <!-- ???? -->
    <el-dialog title="??????" :visible.sync="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border v-if="detailRow">
        <el-descriptions-item label="??ID">{{ detailRow.id }}</el-descriptions-item>
        <el-descriptions-item label="???">{{ detailRow.username }}</el-descriptions-item>
        <el-descriptions-item label="??">
          <el-tag :type="statusTagType(detailRow.taskStatus)" size="small">{{ statusLabel(detailRow.taskStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="????">{{ parseTime(detailRow.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detailRow && detailRow.remark">
        <el-divider content-position="left">????</el-divider>
        <div class="content-box">{{ detailRow.remark }}</div>
      </div>
      <div v-if="detailRow && detailRow.videoUrl">
        <el-divider content-position="left">????</el-divider>
        <div class="content-box link-box">
          <a :href="detailRow.videoUrl" target="_blank" class="video-link"><i class="el-icon-video-play"></i> {{ detailRow.videoUrl }}</a>
        </div>
      </div>
      <div v-if="detailRow && detailRow.errorMessage">
        <el-divider content-position="left">????</el-divider>
        <div class="content-box error-box">{{ detailRow.errorMessage }}</div>
      </div>
      <div slot="footer"><el-button @click="detailOpen = false">? ?</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
import Pagination from '@/components/Pagination'

export default {
  name: 'VideoAdmin',
  components: { Pagination },
  data() {
    return {
      loading: false,
      showSearch: true,
      tableData: [],
      total: 0,
      multiple: true,
      selectedIds: [],
      detailOpen: false,
      detailRow: null,
      queryParams: { pageNum: 1, pageSize: 10, username: '', taskStatus: '' },
      stats: { total: 0, succeeded: 0, inProgress: 0, failed: 0 }
    }
  },
  mounted() {
    this.fetchStats()
    this.fetchList()
  },
  methods: {
    /** ???????????????? */
    fetchStats() {
      request({
        url: '/admin/video/list',
        method: 'get',
        params: { pageNum: 1, pageSize: 9999, username: '', taskStatus: '' }
      }).then(res => {
        const rows = res.rows || []
        this.stats = {
          total:      res.total || rows.length,
          succeeded:  rows.filter(r => r.taskStatus === '3').length,
          inProgress: rows.filter(r => r.taskStatus === '1' || r.taskStatus === '2').length,
          failed:     rows.filter(r => r.taskStatus === '4').length
        }
      }).catch(() => {})
    },
    fetchList() {
      this.loading = true
      request({ url: '/admin/video/list', method: 'get', params: this.queryParams })
        .then(res => { this.tableData = res.rows || []; this.total = res.total || 0 })
        .finally(() => { this.loading = false })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.fetchList() },
    resetQuery() { this.$refs.queryForm.resetFields(); this.handleQuery() },
    handleSelectionChange(rows) {
      this.selectedIds = rows.map(r => r.id)
      this.multiple = !rows.length
    },
    handleView(row) { this.detailRow = row; this.detailOpen = true },
    handleDelete(row) {
      this.$confirm('?????????', '??', { type: 'warning' }).then(() => {
        request({ url: '/admin/video/' + row.id, method: 'delete' }).then(res => {
          if (res.code === 200) { this.$message.success('????'); this.fetchList(); this.fetchStats() }
          else this.$message.error(res.msg || '????')
        })
      }).catch(() => {})
    },
    handleBatchDelete() {
      this.$confirm('??????? ' + this.selectedIds.length + ' ?????', '??', { type: 'warning' }).then(() => {
        request({ url: '/admin/video/' + this.selectedIds.join(','), method: 'delete' }).then(res => {
          if (res.code === 200) {
            this.$message.success('??????')
            this.selectedIds = []; this.multiple = true
            this.fetchList(); this.fetchStats()
          } else this.$message.error(res.msg || '????')
        })
      }).catch(() => {})
    },
    statusLabel(s)   { return { '1': '???', '2': '???', '3': '???', '4': '???' }[s] || '-' },
    statusTagType(s) { return { '1': 'info', '2': 'warning', '3': 'success', '4': 'danger' }[s] || 'info' }
  }
}
</script>

<style lang="scss" scoped>
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
.content-box {
  padding: 15px; background: #f5f7fa; border-radius: 4px;
  line-height: 1.8; white-space: pre-wrap; word-break: break-word;
  max-height: 200px; overflow-y: auto;
}
.link-box  { background: #e8f4f8; }
.error-box { background: #fff0f0; }
.video-link {
  color: #409EFF; text-decoration: none; word-break: break-all;
  &:hover { text-decoration: underline; }
  i { margin-right: 4px; }
}
</style>
