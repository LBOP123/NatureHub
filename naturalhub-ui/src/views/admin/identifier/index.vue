<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="用户名" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="申请状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width:140px">
          <el-option
            v-for="dict in statusOptions"
            :key="dict.dictValue"
            :label="dict.dictLabel"
            :value="parseInt(dict.dictValue)"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb8">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#E6A23C"><i class="el-icon-time"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#67C23A"><i class="el-icon-circle-check"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.approved }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#F56C6C"><i class="el-icon-circle-close"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.rejected }}</div>
              <div class="stat-label">已拒绝</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="ID" align="center" prop="id" width="70" />
      <el-table-column label="用户名" align="center" prop="userName" width="120" />
      <el-table-column label="昵称" align="center" prop="nickName" width="120" />
      <el-table-column label="真实姓名" align="center" prop="realName" width="110" />
      <el-table-column label="专业背景" align="center" prop="expertise" :show-overflow-tooltip="true" min-width="180" />
      <el-table-column label="申请状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <dict-tag :options="statusOptions" :value="getAuditStatusText(scope.row.status)" />
        </template>
      </el-table-column>
      <el-table-column label="申请时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核人" align="center" prop="reviewBy" width="110" />
      <el-table-column label="审核时间" align="center" prop="reviewTime" width="160">
        <template slot-scope="scope">
          <span v-if="scope.row.reviewTime">{{ parseTime(scope.row.reviewTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)">详情</el-button>
          <template v-if="scope.row.status === 0">
            <el-button size="mini" type="text" icon="el-icon-check" style="color:#67C23A" @click="handleApprove(scope.row)">通过</el-button>
            <el-button size="mini" type="text" icon="el-icon-close" style="color:#F56C6C" @click="handleReject(scope.row)">拒绝</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 详情对话框 -->
    <el-dialog title="申请详情" :visible.sync="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="申请状态">
          <dict-tag :options="statusOptions" :value="getAuditStatusText(detail.status)" />
        </el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detail.nickName }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ detail.realName || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ parseTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="专业背景" :span="2">{{ detail.expertise }}</el-descriptions-item>
        <el-descriptions-item label="鉴定经验" :span="2">
          <div style="white-space:pre-wrap">{{ detail.experience }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="个人简介" :span="2" v-if="detail.bio">{{ detail.bio }}</el-descriptions-item>
        <el-descriptions-item label="资质证明" :span="2" v-if="detail.qualification">
          <div class="qualification-imgs">
            <el-image
              v-for="(url, idx) in qualificationImages"
              :key="idx"
              :src="url"
              :preview-src-list="qualificationImages"
              style="width:100px;height:100px;margin-right:8px"
              fit="cover"
            />
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="审核人" v-if="detail.reviewBy">{{ detail.reviewBy }}</el-descriptions-item>
        <el-descriptions-item label="审核时间" v-if="detail.reviewTime">{{ parseTime(detail.reviewTime) }}</el-descriptions-item>
        <el-descriptions-item label="拒绝原因" :span="2" v-if="detail.rejectReason">
          <span style="color:#F56C6C">{{ detail.rejectReason }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
        <el-button v-if="detail.status === 0" type="success" @click="handleApprove(detail)">审核通过</el-button>
        <el-button v-if="detail.status === 0" type="danger"  @click="handleReject(detail)">审核拒绝</el-button>
      </div>
    </el-dialog>

    <!-- 通过确认对话框 -->
    <el-dialog title="审核通过确认" :visible.sync="approveOpen" width="420px" append-to-body>
      <div style="padding:10px 0">
        <p>确定通过 <strong>{{ currentRow.userName }}</strong> 的鉴定者申请？</p>
        <p style="color:#909399;font-size:13px;margin-top:8px">通过后系统将自动为该用户分配「鉴定者」角色。</p>
      </div>
      <div slot="footer">
        <el-button @click="approveOpen = false">取 消</el-button>
        <el-button type="success" :loading="submitting" @click="submitApprove">确认通过</el-button>
      </div>
    </el-dialog>

    <!-- 拒绝对话框 -->
    <el-dialog title="审核拒绝" :visible.sync="rejectOpen" width="500px" append-to-body>
      <el-form ref="rejectForm" :model="rejectForm" :rules="rejectRules" label-width="80px">
        <el-form-item label="拒绝原因" prop="rejectReason">
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请填写拒绝原因，将展示给申请人"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="rejectOpen = false">取 消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitReject">确认拒绝</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listApplication, approveApplication, rejectApplication } from '@/api/admin/identifier'

export default {
  name: 'IdentifierApplicationAdmin',
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      list: [],
      statusOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: null,
        status: null
      },
      stats: { pending: 0, approved: 0, rejected: 0 },
      // 详情
      detailOpen: false,
      detail: {},
      qualificationImages: [],
      // 通过
      approveOpen: false,
      currentRow: {},
      // 拒绝
      rejectOpen: false,
      rejectForm: { rejectReason: '' },
      rejectRules: {
        rejectReason: [{ required: true, message: '请填写拒绝原因', trigger: 'blur' }]
      },
      submitting: false
    }
  },
  created() {
    this.getDicts('nh_audit_status').then(res => { this.statusOptions = res.data || [] })
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listApplication(this.queryParams).then(res => {
        this.list = res.rows
        this.total = res.total
        this.loading = false
        this.calcStats()
      })
    },
    getAuditStatusText(value) {
      const item = this.statusOptions.find(i => i.dictValue == value)
      return item ? item.dictLabel : value
    },
    calcStats() {
      // 从全量数据中统计（仅当前页），实际项目可加后端统计接口
      listApplication({ pageNum: 1, pageSize: 9999 }).then(res => {
        const all = res.rows || []
        this.stats.pending  = all.filter(r => r.status === 0).length
        this.stats.approved = all.filter(r => r.status === 1).length
        this.stats.rejected = all.filter(r => r.status === 2).length
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleView(row) {
      this.detail = { ...row }
      this.qualificationImages = row.qualification
        ? row.qualification.split(',').filter(u => u)
        : []
      this.detailOpen = true
    },
    handleApprove(row) {
      this.currentRow = row
      this.approveOpen = true
      this.detailOpen = false
    },
    submitApprove() {
      this.submitting = true
      approveApplication(this.currentRow.id).then(() => {
        this.$modal.msgSuccess('已通过申请，鉴定者角色已分配')
        this.approveOpen = false
        this.getList()
      }).finally(() => { this.submitting = false })
    },
    handleReject(row) {
      this.currentRow = row
      this.rejectForm.rejectReason = ''
      this.rejectOpen = true
      this.detailOpen = false
    },
    submitReject() {
      this.$refs.rejectForm.validate(valid => {
        if (!valid) return
        this.submitting = true
        rejectApplication(this.currentRow.id, this.rejectForm.rejectReason).then(() => {
          this.$modal.msgSuccess('已拒绝该申请')
          this.rejectOpen = false
          this.getList()
        }).finally(() => { this.submitting = false })
      })
    }
  }
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
}
.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  margin-right: 15px;
  flex-shrink: 0;
}
.stat-content { flex: 1; }
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}
.stat-label {
  font-size: 14px;
  color: #909399;
}
.qualification-imgs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
