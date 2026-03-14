<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="发布人" prop="createBy">
        <el-input
          v-model="queryParams.createBy"
          placeholder="请输入发布人"
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
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option v-for="dict in identificationStatusOptions" :key="dict.dictValue" :label="dict.dictLabel"
                     :value="parseInt(dict.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态" prop="auditStatus">
        <el-select v-model="queryParams.auditStatus" placeholder="请选择审核状态" clearable>
          <el-option v-for="dict in auditStatusOptions" :key="dict.dictValue" :label="dict.dictLabel"
                     :value="parseInt(dict.dictValue)"/>
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
          v-hasPermi="['identification:admin:approve']"
        >批量通过
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['identification:admin:remove']"
        >批量删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['identification:admin:export']"
        >导出
        </el-button>
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
              <div class="stat-value">{{ stats.totalCount || 0 }}</div>
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
              <div class="stat-value">{{ stats.pendingCount || 0 }}</div>
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
              <div class="stat-value">{{ stats.approvedCount || 0 }}</div>
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
              <div class="stat-value">{{ stats.rejectedCount || 0 }}</div>
              <div class="stat-label">已驳回</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="identificationList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="ID" align="center" prop="identificationId" width="80"/>
      <el-table-column label="发布人" align="center" prop="createBy" width="100"/>
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" min-width="150"/>
      <el-table-column label="观察地点" align="center" prop="location" :show-overflow-tooltip="true" width="150"/>
      <el-table-column label="观察时间" align="center" prop="observationTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.observationTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <dict-tag :options="identificationStatusOptions" :value="getIdentificationStatus(scope.row.status)"/>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" align="center" prop="auditStatus" width="100">
        <template slot-scope="scope">
          <dict-tag :options="auditStatusOptions" :value="getAuditStatus(scope.row.auditStatus)"/>
        </template>
      </el-table-column>
      <el-table-column label="回答数" align="center" prop="answerCount" width="80"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250" fixed="right">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['identification:admin:query']"
          >查看
          </el-button>
          <el-button
            v-if="scope.row.auditStatus === 1"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-hasPermi="['identification:admin:approve']"
          >通过
          </el-button>
          <el-button
            v-if="scope.row.auditStatus === 1"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleReject(scope.row)"
            v-hasPermi="['identification:admin:reject']"
          >驳回
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['identification:admin:remove']"
          >删除
          </el-button>
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
    <el-dialog :title="'鉴定求助详情'" :visible.sync="detailOpen" width="900px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="记录ID">{{ detail.identificationId }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ detail.createBy }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="观察时间">{{ parseTime(detail.observationTime) }}</el-descriptions-item>
        <el-descriptions-item label="观察地点">{{ detail.location }}</el-descriptions-item>
        <el-descriptions-item label="经纬度" v-if="detail.latitude && detail.longitude">
          {{ detail.latitude }}, {{ detail.longitude }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :options="identificationStatusOptions" :value="String(detail.status)"/>
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <dict-tag :options="auditStatusOptions" :value="String(detail.auditStatus)"/>
        </el-descriptions-item>
        <el-descriptions-item label="回答数">{{ detail.answerCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="特征描述" :span="2" v-if="detail.features">{{
            detail.features
          }}
        </el-descriptions-item>
        <el-descriptions-item label="生境描述" :span="2" v-if="detail.habitat">{{
            detail.habitat
          }}
        </el-descriptions-item>
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
        <el-descriptions-item label="创建时间">{{ parseTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="审核时间" v-if="detail.auditTime">
          {{ parseTime(detail.auditTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2" v-if="detail.auditRemark">
          {{ detail.auditRemark }}
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
        <el-button
          v-if="detail.auditStatus === 1"
          type="success"
          @click="handleApprove(detail)"
          v-hasPermi="['identification:admin:approve']"
        >审核通过
        </el-button>
        <el-button
          v-if="detail.auditStatus === 1"
          type="danger"
          @click="handleReject(detail)"
          v-hasPermi="['identification:admin:reject']"
        >审核驳回
        </el-button>
      </div>
    </el-dialog>

    <!-- 审核通过对话框 -->
    <el-dialog title="审核通过" :visible.sync="approveOpen" width="500px" append-to-body>
      <el-form ref="approveForm" :model="approveForm" label-width="100px">
        <el-form-item label="审核意见">
          <el-input
            v-model="approveForm.auditRemark"
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
        <el-form-item label="驳回原因" prop="auditRemark">
          <el-input
            v-model="rejectForm.auditRemark"
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
import {
  listIdentification,
  getIdentification,
  delIdentification,
  auditIdentification,
  batchApproveIdentification,
  getIdentificationStats
} from "@/api/admin/identification";

export default {
  name: "IdentificationAdmin",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      identificationList: [],
      detailOpen: false,
      detail: {},
      detailImages: [],
      approveOpen: false,
      approveForm: {},
      rejectOpen: false,
      rejectForm: {},
      rejectRules: {
        auditRemark: [
          {required: true, message: "驳回原因不能为空", trigger: "blur"}
        ]
      },
      dateRange: [],
      // 字典数据
      identificationStatusOptions: [],
      auditStatusOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        createBy: null,
        title: null,
        status: null,
        auditStatus: null
      },
      stats: {
        totalCount: 0,
        pendingCount: 0,
        approvedCount: 0,
        rejectedCount: 0
      }
    };
  },
  created() {
    this.getDicts('nh_identification_status').then(res => {
      this.identificationStatusOptions = res.data;
    });
    this.getDicts('nh_audit_status').then(res => {
      this.auditStatusOptions = res.data;
    });
    this.getStats();
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listIdentification(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.identificationList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getStats() {
      getIdentificationStats().then(response => {
        this.stats = response.data || response || {
          totalCount: 0,
          pendingCount: 0,
          approvedCount: 0,
          rejectedCount: 0
        };
      }).catch(() => {
        this.stats = {
          totalCount: 0,
          pendingCount: 0,
          approvedCount: 0,
          rejectedCount: 0
        };
      });
    },
    getIdentificationStatus(val) {
      const item = this.identificationStatusOptions.find(d => d.dictValue == val);
      return item ? item.dictLabel : "未知";
    },
    // 审核状态转换
    getAuditStatus(val) {
      const item = this.auditStatusOptions.find(d => d.dictValue == val);
      return item ? item.dictLabel : "未知";
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.identificationId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleView(row) {
      this.detailOpen = true;
      this.detail = row;
      this.detailImages = [];

      getIdentification(row.identificationId).then(res => {
        this.detail = res.data || res || {};
        if (this.detail.images) {
          this.detailImages = this.detail.images.split(',');
        }
      });
    },
    handleApprove(row) {
      this.approveForm = {
        identificationId: row.identificationId,
        auditStatus: 2,
        auditRemark: ''
      };
      this.approveOpen = true;
      this.detailOpen = false;
    },
    submitApprove() {
      auditIdentification(this.approveForm).then(response => {
        this.$modal.msgSuccess("审核通过");
        this.approveOpen = false;
        this.getList();
        this.getStats();
      });
    },
    handleReject(row) {
      this.rejectForm = {
        identificationId: row.identificationId,
        auditStatus: 3,
        auditRemark: ''
      };
      this.rejectOpen = true;
      this.detailOpen = false;
    },
    submitReject() {
      this.$refs["rejectForm"].validate(valid => {
        if (valid) {
          auditIdentification(this.rejectForm).then(response => {
            this.$modal.msgSuccess("审核驳回");
            this.rejectOpen = false;
            this.getList();
            this.getStats();
          });
        }
      });
    },
    handleBatchApprove() {
      const identificationIds = this.ids;
      this.$modal.confirm('是否确认批量审核通过选中的' + identificationIds.length + '条记录？').then(function () {
        return batchApproveIdentification(identificationIds);
      }).then(() => {
        this.getList();
        this.getStats();
        this.$modal.msgSuccess("批量审核通过成功");
      }).catch(() => {
      });
    },
    handleDelete(row) {
      const identificationIds = row.identificationId || this.ids;
      this.$modal.confirm('是否确认删除鉴定求助编号为"' + identificationIds + '"的数据项？').then(function () {
        return delIdentification(identificationIds);
      }).then(() => {
        this.getList();
        this.getStats();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    handleExport() {
      this.download('admin/identification/export', {
        ...this.queryParams
      }, `identification_${new Date().getTime()}.xlsx`);
    },


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
