<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="发布人" prop="createBy">
        <el-input v-model="queryParams.createBy" placeholder="请输入发布人" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="审核状态" prop="auditStatus">
        <el-select v-model="queryParams.auditStatus" placeholder="请选择审核状态" clearable>
          <el-option v-for="dict in auditStatusOptions" :key="dict.dictValue"
                     :label="dict.dictLabel" :value="parseInt(dict.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="调查时间">
        <el-date-picker v-model="dateRange" style="width:240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-check" size="mini" :disabled="multiple"
                   @click="handleBatchApprove" v-hasPermi="['survey:admin:approve']">批量通过
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple"
                   @click="handleDelete" v-hasPermi="['survey:admin:remove']">批量删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini"
                   @click="handleExport" v-hasPermi="['survey:admin:export']">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#409EFF"><i class="el-icon-document"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.total || 0 }}</div>
              <div class="stat-label">总记录数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#E6A23C"><i class="el-icon-time"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.pending || 0 }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#67C23A"><i class="el-icon-success"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.approved || 0 }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background:#F56C6C"><i class="el-icon-close"></i></div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.rejected || 0 }}</div>
              <div class="stat-label">已驳回</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="surveyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="ID" align="center" prop="surveyId" width="80"/>
      <el-table-column label="发布人" align="center" prop="createBy" width="100"/>
      <el-table-column label="标题" align="center" prop="title" show-overflow-tooltip min-width="150"/>
      <el-table-column label="调查地点" align="center" prop="location" show-overflow-tooltip width="150"/>
      <el-table-column label="调查时间" align="center" prop="surveyDate" width="120">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.surveyDate, '{y}-{m}-{d}') }}</span></template>
      </el-table-column>
      <el-table-column label="审核状态" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="auditStatusOptions" :value="scope.row.auditStatusLabel"/>
        </template>
      </el-table-column>
      <el-table-column label="物种数" align="center" prop="speciesCount" width="80"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope"><span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)"
                     v-hasPermi="['survey:admin:query']">查看
          </el-button>
          <!-- 【改造点】1=待审核 -->
          <el-button v-if="scope.row.auditStatus === 1" size="mini" type="text" icon="el-icon-check"
                     @click="handleApprove(scope.row)" v-hasPermi="['survey:admin:approve']">通过
          </el-button>
          <el-button v-if="scope.row.auditStatus === 1" size="mini" type="text" icon="el-icon-close"
                     @click="handleReject(scope.row)" v-hasPermi="['survey:admin:reject']">驳回
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['survey:admin:remove']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 查看详情对话框 -->
    <el-dialog title="野外调查详情" :visible.sync="detailOpen" width="900px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="记录ID">{{ detail.surveyId }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ detail.createBy }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="调查日期">{{ detail.surveyDate }}</el-descriptions-item>
        <el-descriptions-item label="调查地点">{{ detail.location }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="生境类型">{{ getHabitatTypeText(detail.habitatType) }}</el-descriptions-item>
        <el-descriptions-item label="调查方法">{{ getSurveyTypeText(detail.surveyMethod) }}</el-descriptions-item>
        <el-descriptions-item label="天气情况">{{ detail.weather }}</el-descriptions-item>
        <el-descriptions-item label="温度">
          {{ detail.temperature !== null && detail.temperature !== undefined ? detail.temperature + ' ℃' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="团队成员" :span="2">{{ detail.teamMembers || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发现物种数">{{ detail.speciesCount || 0 }} 种</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <dict-tag :options="auditStatusOptions" :value="detail.auditStatusLabel"/>
        </el-descriptions-item>
        <el-descriptions-item label="调查描述" :span="2">
          <div style="white-space: pre-wrap;">{{ detail.description }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="主要发现" :span="2" v-if="detail.findings">
          <div style="white-space: pre-wrap;">{{ detail.findings }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="图片" :span="2" v-if="detailImages.length > 0">
          <el-image
            v-for="(url, index) in detailImages"
            :key="index"
            :src="url"
            :preview-src-list="detailImages"
            style="width: 100px; height: 100px; margin-right: 10px;"
            fit="cover"
          />
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2" v-if="detail.auditRemark">{{
            detail.auditRemark
          }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ parseTime(detail.updateTime) }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
        <el-button
          v-if="detail.auditStatus === 1"
          type="success"
          @click="handleApprove(detail)"
          v-hasPermi="['survey:admin:approve']"
        >审核通过
        </el-button>
        <el-button
          v-if="detail.auditStatus === 1"
          type="danger"
          @click="handleReject(detail)"
          v-hasPermi="['survey:admin:reject']"
        >审核驳回
        </el-button>
      </div>
    </el-dialog>

    <!-- 审核通过对话框 -->
    <el-dialog title="审核通过" :visible.sync="approveOpen" width="500px" append-to-body>
      <el-form ref="approveForm" :model="approveForm" label-width="100px">
        <el-form-item label="审核意见">
          <el-input v-model="approveForm.auditRemark" type="textarea" :rows="4" placeholder="请输入审核意见（可选）"
                    maxlength="500" show-word-limit/>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="approveOpen = false">取 消</el-button>
        <el-button type="success" @click="submitApprove">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 审核驳回对话框 -->
    <el-dialog title="审核驳回" :visible.sync="rejectOpen" width="500px" append-to-body>
      <el-form ref="rejectForm" :model="rejectForm" :rules="rejectRules" label-width="100px">
        <el-form-item label="驳回原因" prop="auditRemark">
          <el-input v-model="rejectForm.auditRemark" type="textarea" :rows="4" placeholder="请输入驳回原因"
                    maxlength="500" show-word-limit/>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="rejectOpen = false">取 消</el-button>
        <el-button type="danger" @click="submitReject">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {listSurvey, getSurvey, delSurvey, auditSurvey, batchApproveSurvey, getSurveyStats} from '@/api/admin/survey'

export default {
  name: 'SurveyAdmin',
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0,
      surveyList: [], detailOpen: false, detail: {}, detailImages: [],
      approveOpen: false, approveForm: {}, rejectOpen: false, rejectForm: {},
      rejectRules: {auditRemark: [{required: true, message: '驳回原因不能为空', trigger: 'blur'}]},
      dateRange: [],
      auditStatusOptions: [],
      habitatTypeOptions: [],
      surveyMethodOptions: [],
      queryParams: {pageNum: 1, pageSize: 10, createBy: null, title: null, surveyMethod: null, auditStatus: null},
      stats: {total: 0, pending: 0, approved: 0, rejected: 0}
    }
  },
  created() {
    this.getDicts('nh_audit_status').then(res => {
      this.auditStatusOptions = res.data
    })
    this.getDicts('nh_habitat_type').then(res => {
      this.habitatTypeOptions = res.data
    })
    this.getDicts('nh_survey_method').then(res => {
      this.surveyMethodOptions = res.data
    })
    this.getList()
    this.getStats()
  },
  methods: {
    getList() {
      this.loading = true
      listSurvey(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.surveyList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getStats() {
      getSurveyStats().then(response => {
        const resData = response.data || response || {}
        this.stats = {
          total: resData.totalCount || resData.total || 0,
          pending: resData.pendingCount || resData.pending || 0,
          approved: resData.approvedCount || resData.approved || 0,
          rejected: resData.rejectedCount || resData.rejected || 0
        }
      }).catch(() => {
        this.stats = {
          total: 0,
          pending: 0,
          approved: 0,
          rejected: 0
        }
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList()
    },
    resetQuery() {
      this.dateRange = [];
      this.resetForm('queryForm');
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.surveyId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleView(row) {
      this.detail = row
      this.detailImages = []
      this.detailOpen = true
      getSurvey(row.surveyId).then(res => {
        this.detail = res.data || res || {}
        if (this.detail.images) {
          this.detailImages = this.detail.images.split(',')
        }
      })
    },
    handleApprove(row) {
      this.approveForm = {surveyId: row.surveyId, auditStatus: 2, auditRemark: ''}
      this.approveOpen = true
      this.detailOpen = false
    },
    submitApprove() {
      auditSurvey(this.approveForm).then(() => {
        this.$modal.msgSuccess('审核通过')
        this.approveOpen = false
        this.getList();
        this.getStats()
      })
    },
    handleReject(row) {
      this.rejectForm = {surveyId: row.surveyId, auditStatus: 3, auditRemark: ''}
      this.rejectOpen = true
      this.detailOpen = false
    },
    submitReject() {
      this.$refs.rejectForm.validate(valid => {
        if (valid) {
          auditSurvey(this.rejectForm).then(() => {
            this.$modal.msgSuccess('审核驳回')
            this.rejectOpen = false
            this.getList();
            this.getStats()
          })
        }
      })
    },
    handleBatchApprove() {
      this.$modal.confirm('是否确认批量审核通过选中的 ' + this.ids.length + ' 条记录？')
        .then(() => batchApproveSurvey(this.ids))
        .then(() => {
          this.getList();
          this.getStats();
          this.$modal.msgSuccess('批量审核通过成功')
        })
        .catch(() => {
        })
    },
    handleDelete(row) {
      const surveyIds = row.surveyId ? [row.surveyId] : this.ids
      this.$modal.confirm('是否确认删除选中的野外调查记录？')
        .then(() => delSurvey(surveyIds))
        .then(() => {
          this.getList();
          this.getStats();
          this.$modal.msgSuccess('删除成功')
        })
        .catch(() => {
        })
    },
    handleExport() {
      this.download('admin/survey/export', {...this.queryParams}, `survey_${new Date().getTime()}.xlsx`)
    },
    getSurveyTypeText(val) {
      const d = (this.surveyMethodOptions || []).find(d => parseInt(d.dictValue) === val)
      return d ? d.dictLabel : (val || '-')
    },
    getSurveyTypeTag(type) {
      const m = {1: 'success', 2: 'warning', 3: 'primary', 10: 'info'}
      return m[type] || ''
    },
    getHabitatTypeText(val) {
      const d = (this.habitatTypeOptions || []).find(d => parseInt(d.dictValue) === val)
      return d ? d.dictLabel : (val || '-')
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
}

.stat-content {
  flex: 1;
}

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
</style>
