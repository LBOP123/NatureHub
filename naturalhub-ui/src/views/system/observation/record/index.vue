<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="用户ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入用户ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="物种类型" prop="speciesType">
        <el-select v-model="queryParams.speciesType" placeholder="请选择物种类型" clearable>
          <el-option label="植物" value="plant" />
          <el-option label="动物" value="animal" />
          <el-option label="真菌" value="fungi" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态" prop="reviewStatus">
        <el-select v-model="queryParams.reviewStatus" placeholder="请选择审核状态" clearable>
          <el-option label="草稿" value="draft" />
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已驳回" value="rejected" />
        </el-select>
      </el-form-item>
      <el-form-item label="观察时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-check"
          size="mini"
          :disabled="multiple"
          @click="handleBatchApprove"
          v-hasPermi="['system:record:approve']"
        >批量通过</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:record:remove']"
        >批量删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:record:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409EFF;">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalRecords }}</div>
              <div class="stat-label">总记录数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #E6A23C;">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.pendingRecords }}</div>
              <div class="stat-label">待审核</div>
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
              <div class="stat-value">{{ stats.approvedRecords }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #F56C6C;">
              <i class="el-icon-close"></i>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.rejectedRecords }}</div>
              <div class="stat-label">已驳回</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="记录ID" align="center" prop="recordId" width="80" />
      <el-table-column label="用户ID" align="center" prop="userId" width="80" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" min-width="150" />
      <el-table-column label="物种类型" align="center" prop="speciesType" width="100">
        <template slot-scope="scope">
          <el-tag :type="getSpeciesTypeTag(scope.row.speciesType)" size="small">
            {{ getSpeciesTypeText(scope.row.speciesType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="物种名称" align="center" prop="speciesName" :show-overflow-tooltip="true" width="120" />
      <el-table-column label="观察地点" align="center" prop="location" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="观察时间" align="center" prop="observationTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.observationTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" align="center" prop="reviewStatus" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.reviewStatus)" size="small">
            {{ getStatusText(scope.row.reviewStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="submitTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.submitTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['system:record:query']"
          >查看</el-button>
          <el-button
            v-if="scope.row.reviewStatus === 'pending'"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-hasPermi="['system:record:approve']"
          >通过</el-button>
          <el-button
            v-if="scope.row.reviewStatus === 'pending'"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleReject(scope.row)"
            v-hasPermi="['system:record:reject']"
          >驳回</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:record:remove']"
          >删除</el-button>
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

    <!-- 查看详情对话框 -->
    <el-dialog :title="'观察记录详情 - ' + detail.title" :visible.sync="detailOpen" width="900px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="记录ID">{{ detail.recordId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="物种类型">
          <el-tag :type="getSpeciesTypeTag(detail.speciesType)" size="small">
            {{ getSpeciesTypeText(detail.speciesType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="物种名称">{{ detail.speciesName }}</el-descriptions-item>
        <el-descriptions-item label="观察时间">{{ parseTime(detail.observationTime) }}</el-descriptions-item>
        <el-descriptions-item label="观察地点">{{ detail.location }}</el-descriptions-item>
        <el-descriptions-item label="经纬度">
          {{ detail.latitude }}, {{ detail.longitude }}
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getStatusType(detail.reviewStatus)" size="small">
            {{ getStatusText(detail.reviewStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="生境描述" :span="2">{{ detail.habitat }}</el-descriptions-item>
        <el-descriptions-item label="详细描述" :span="2">
          <div style="white-space: pre-wrap;">{{ detail.description }}</div>
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
        <el-descriptions-item label="提交时间" v-if="detail.submitTime">
          {{ parseTime(detail.submitTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="审核时间" v-if="detail.reviewTime">
          {{ parseTime(detail.reviewTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2" v-if="detail.reviewComment">
          {{ detail.reviewComment }}
        </el-descriptions-item>
        <el-descriptions-item label="驳回原因" :span="2" v-if="detail.rejectReason">
          <el-alert :title="detail.rejectReason" type="error" :closable="false" />
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
        <el-button
          v-if="detail.reviewStatus === 'pending'"
          type="success"
          @click="handleApprove(detail)"
          v-hasPermi="['system:record:approve']"
        >审核通过</el-button>
        <el-button
          v-if="detail.reviewStatus === 'pending'"
          type="danger"
          @click="handleReject(detail)"
          v-hasPermi="['system:record:reject']"
        >审核驳回</el-button>
      </div>
    </el-dialog>

    <!-- 审核通过对话框 -->
    <el-dialog title="审核通过" :visible.sync="approveOpen" width="500px" append-to-body>
      <el-form ref="approveForm" :model="approveForm" label-width="100px">
        <el-form-item label="审核意见">
          <el-input
            v-model="approveForm.reviewComment"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="approveOpen = false">取 消</el-button>
        <el-button type="success" @click="submitApprove">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 审核驳回对话框 -->
    <el-dialog title="审核驳回" :visible.sync="rejectOpen" width="500px" append-to-body>
      <el-form ref="rejectForm" :model="rejectForm" :rules="rejectRules" label-width="100px">
        <el-form-item label="驳回原因" prop="rejectReason">
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rejectOpen = false">取 消</el-button>
        <el-button type="danger" @click="submitReject">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRecord, getRecord, delRecord, approveRecord, rejectRecord, batchApproveRecord, exportRecord, getRecordStats } from "@/api/system/observationRecord";

export default {
  name: "ObservationRecord",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 观察记录表格数据
      recordList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 详情对话框
      detailOpen: false,
      detail: {},
      detailImages: [],
      // 审核通过对话框
      approveOpen: false,
      approveForm: {},
      // 审核驳回对话框
      rejectOpen: false,
      rejectForm: {},
      rejectRules: {
        rejectReason: [
          { required: true, message: "驳回原因不能为空", trigger: "blur" }
        ]
      },
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        title: null,
        speciesType: null,
        reviewStatus: null
      },
      // 统计数据
      stats: {
        totalRecords: 0,
        pendingRecords: 0,
        approvedRecords: 0,
        rejectedRecords: 0,
        draftRecords: 0
      }
    };
  },
  created() {
    this.getList();
    this.getStats();
  },
  methods: {
    /** 查询观察记录列表 */
    getList() {
      this.loading = true;
      listRecord(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.recordList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 获取统计数据 */
    getStats() {
      getRecordStats().then(response => {
        this.stats = response.data;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.recordId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 查看详情 */
    handleView(row) {
      const recordId = row.recordId || this.ids[0];
      getRecord(recordId).then(response => {
        this.detail = response.data;
        this.detailImages = this.detail.images ? JSON.parse(this.detail.images) : [];
        this.detailOpen = true;
      });
    },
    /** 审核通过按钮 */
    handleApprove(row) {
      this.approveForm = {
        recordId: row.recordId,
        reviewComment: ''
      };
      this.approveOpen = true;
      this.detailOpen = false;
    },
    /** 提交审核通过 */
    submitApprove() {
      approveRecord(this.approveForm).then(response => {
        this.$modal.msgSuccess("审核通过");
        this.approveOpen = false;
        this.getList();
        this.getStats();
      });
    },
    /** 审核驳回按钮 */
    handleReject(row) {
      this.rejectForm = {
        recordId: row.recordId,
        rejectReason: ''
      };
      this.rejectOpen = true;
      this.detailOpen = false;
    },
    /** 提交审核驳回 */
    submitReject() {
      this.$refs["rejectForm"].validate(valid => {
        if (valid) {
          rejectRecord(this.rejectForm).then(response => {
            this.$modal.msgSuccess("审核驳回");
            this.rejectOpen = false;
            this.getList();
            this.getStats();
          });
        }
      });
    },
    /** 批量审核通过 */
    handleBatchApprove() {
      const recordIds = this.ids;
      this.$modal.confirm('是否确认批量审核通过选中的' + recordIds.length + '条记录？').then(function() {
        return batchApproveRecord(recordIds);
      }).then(() => {
        this.getList();
        this.getStats();
        this.$modal.msgSuccess("批量审核通过成功");
      }).catch(() => {});
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const recordIds = row.recordId || this.ids;
      this.$modal.confirm('是否确认删除观察记录编号为"' + recordIds + '"的数据项？').then(function() {
        return delRecord(recordIds);
      }).then(() => {
        this.getList();
        this.getStats();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/observation/record/export', {
        ...this.queryParams
      }, `record_${new Date().getTime()}.xlsx`)
    },
    /** 获取物种类型文本 */
    getSpeciesTypeText(type) {
      const typeMap = {
        plant: '植物',
        animal: '动物',
        fungi: '真菌',
        other: '其他'
      };
      return typeMap[type] || type;
    },
    /** 获取物种类型标签 */
    getSpeciesTypeTag(type) {
      const tagMap = {
        plant: 'success',
        animal: 'warning',
        fungi: 'info',
        other: ''
      };
      return tagMap[type] || '';
    },
    /** 获取审核状态文本 */
    getStatusText(status) {
      const statusMap = {
        draft: '草稿',
        pending: '待审核',
        approved: '已通过',
        rejected: '已驳回'
      };
      return statusMap[status] || status;
    },
    /** 获取审核状态类型 */
    getStatusType(status) {
      const typeMap = {
        draft: 'info',
        pending: 'warning',
        approved: 'success',
        rejected: 'danger'
      };
      return typeMap[status] || 'info';
    }
  }
};
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
