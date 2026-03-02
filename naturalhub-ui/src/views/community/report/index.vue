<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="举报类型" prop="reportType">
        <el-select v-model="queryParams.reportType" placeholder="请选择举报类型" clearable>
          <el-option label="话题" value="topic" />
          <el-option label="评论" value="comment" />
        </el-select>
      </el-form-item>
      <el-form-item label="举报原因" prop="reason">
        <el-select v-model="queryParams.reason" placeholder="请选择举报原因" clearable>
          <el-option label="垃圾广告" value="spam" />
          <el-option label="辱骂攻击" value="abuse" />
          <el-option label="违法违规" value="illegal" />
          <el-option label="虚假信息" value="false" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择处理状态" clearable>
          <el-option label="待处理" value="pending" />
          <el-option label="已处理" value="processed" />
          <el-option label="已忽略" value="ignored" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-badge :value="pendingCount" class="item">
          <el-button
            type="warning"
            plain
            icon="el-icon-warning"
            size="mini"
            @click="showPendingOnly"
          >待处理</el-button>
        </el-badge>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['community:report:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="reportList">
      <el-table-column label="举报ID" align="center" prop="reportId" width="80" />
      <el-table-column label="举报类型" align="center" prop="reportType" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.reportType === '0'" type="primary">话题</el-tag>
          <el-tag v-else-if="scope.row.reportType === '1'" type="success">评论</el-tag>
          <el-tag v-else type="fail">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="被举报内容" align="center" prop="targetTitle" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="被举报用户" align="center" prop="targetUserName" width="120" />
      <el-table-column label="举报人" align="center" prop="reporterName" width="120" />
      <el-table-column label="举报原因" align="center" prop="reason" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.reason === 'spam'" type="warning">垃圾广告</el-tag>
          <el-tag v-else-if="scope.row.reason === 'abuse'" type="danger">辱骂攻击</el-tag>
          <el-tag v-else-if="scope.row.reason === 'illegal'" type="danger">违法违规</el-tag>
          <el-tag v-else-if="scope.row.reason === 'false'" type="warning">虚假信息</el-tag>
          <el-tag v-else type="info">其他</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 'pending'" type="warning">待处理</el-tag>
          <el-tag v-else-if="scope.row.status === 'processed'" type="success">已处理</el-tag>
          <el-tag v-else type="info">已忽略</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="举报时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
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
            v-if="scope.row.status === 'pending'"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleProcess(scope.row)"
            v-hasPermi="['community:report:handle']"
          >处理</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 举报详情对话框 -->
    <el-dialog title="举报详情" :visible.sync="detailVisible" width="800px" append-to-body>
      <div class="report-detail" v-if="currentReport">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="举报类型">
            <el-tag v-if="currentReport.reportType === 'topic'" type="primary">话题</el-tag>
            <el-tag v-else type="success">评论</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="举报原因">
            <el-tag v-if="currentReport.reason === 'spam'" type="warning">垃圾广告</el-tag>
            <el-tag v-else-if="currentReport.reason === 'abuse'" type="danger">辱骂攻击</el-tag>
            <el-tag v-else-if="currentReport.reason === 'illegal'" type="danger">违法违规</el-tag>
            <el-tag v-else-if="currentReport.reason === 'false'" type="warning">虚假信息</el-tag>
            <el-tag v-else type="info">其他</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="被举报用户">{{ currentReport.targetUserName }}</el-descriptions-item>
          <el-descriptions-item label="举报人">{{ currentReport.reporterName }}</el-descriptions-item>
          <el-descriptions-item label="举报时间" :span="2">{{ parseTime(currentReport.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="被举报内容" :span="2">
            <div class="content-box">{{ currentReport.targetTitle }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="举报描述" :span="2">
            <div class="content-box">{{ currentReport.description }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag v-if="currentReport.status === 'pending'" type="warning">待处理</el-tag>
            <el-tag v-else-if="currentReport.status === 'processed'" type="success">已处理</el-tag>
            <el-tag v-else type="info">已忽略</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理结果" v-if="currentReport.handleResult">
            {{ getHandleResultText(currentReport.handleResult) }}
          </el-descriptions-item>
          <el-descriptions-item label="处理人" v-if="currentReport.handleBy">{{ currentReport.handleBy }}</el-descriptions-item>
          <el-descriptions-item label="处理时间" v-if="currentReport.handleTime">{{ parseTime(currentReport.handleTime) }}</el-descriptions-item>
          <el-descriptions-item label="处理备注" :span="2" v-if="currentReport.handleRemark">
            <div class="content-box">{{ currentReport.handleRemark }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-actions" v-if="currentReport.status === 'pending'">
          <el-button type="primary" @click="handleProcess(currentReport)">处理举报</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 处理举报对话框 -->
    <el-dialog title="处理举报" :visible.sync="processVisible" width="500px" append-to-body>
      <el-form ref="processForm" :model="processForm" :rules="processRules" label-width="100px">
        <el-form-item label="处理结果" prop="handleResult">
          <el-select v-model="processForm.handleResult" placeholder="请选择处理结果">
            <el-option label="删除内容" value="delete_content" />
            <el-option label="禁言用户" value="ban_user" />
            <el-option label="警告" value="warning" />
            <el-option label="忽略" value="ignore" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="processForm.handleRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注（选填）"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="processVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitProcess">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listReport, getReport, handleReport, exportReport } from "@/api/community/report"

export default {
  name: "CommunityReport",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      pendingCount: 0,
      reportList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        reportType: null,
        reason: null,
        status: 'pending'
      },
      detailVisible: false,
      currentReport: null,
      processVisible: false,
      processForm: {
        reportId: null,
        handleResult: null,
        handleRemark: null
      },
      processRules: {
        handleResult: [
          { required: true, message: '请选择处理结果', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getPendingCount()
  },
  methods: {
    getList() {
      this.loading = true
      listReport(this.queryParams).then(response => {
        this.reportList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getPendingCount() {
      listReport({ status: 'pending', pageNum: 1, pageSize: 1 }).then(response => {
        this.pendingCount = response.total
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    showPendingOnly() {
      this.queryParams.status = 'pending'
      this.handleQuery()
    },
    handleView(row) {
      getReport(row.reportId).then(response => {
        this.currentReport = response.data
        this.detailVisible = true
      })
    },
    handleProcess(row) {
      this.processForm.reportId = row.reportId
      this.processForm.handleResult = null
      this.processForm.handleRemark = null
      this.processVisible = true
    },
    submitProcess() {
      this.$refs['processForm'].validate(valid => {
        if (valid) {
          handleReport(this.processForm.reportId, this.processForm.handleResult, this.processForm.handleRemark).then(() => {
            this.$modal.msgSuccess("处理成功")
            this.processVisible = false
            this.detailVisible = false
            this.getList()
            this.getPendingCount()
          })
        }
      })
    },
    handleExport() {
      this.download('community/report/export', {
        ...this.queryParams
      }, `举报记录_${new Date().getTime()}.xlsx`)
    },
    getHandleResultText(result) {
      const map = {
        'delete_content': '删除内容',
        'ban_user': '禁言用户',
        'warning': '警告',
        'ignore': '忽略'
      }
      return map[result] || result
    }
  }
}
</script>

<style lang="scss" scoped>
.report-detail {
  .content-box {
    padding: 10px;
    background: #f5f7fa;
    border-radius: 4px;
    line-height: 1.6;
    white-space: pre-wrap;
  }

  .detail-actions {
    margin-top: 20px;
    padding-top: 15px;
    border-top: 1px solid #f0f0f0;
    text-align: center;
  }
}
</style>
