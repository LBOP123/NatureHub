<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="话题标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入话题标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="话题分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="请选择话题分类" clearable>
          <el-option v-for="dict in categoryTypeOptions" :key="dict.dictValue" :label="dict.dictLabel" :value="parseInt(dict.dictValue)" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态" prop="auditStatus">
        <el-select v-model="queryParams.auditStatus" placeholder="请选择审核状态" clearable>
          <el-option v-for="dict in auditStatusOptions" :key="dict.dictValue" :label="dict.dictLabel" :value="parseInt(dict.dictValue)" />
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
            type="primary"
            plain
            icon="el-icon-time"
            size="mini"
            @click="showPendingOnly"
          >待审核</el-button>
        </el-badge>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="topicList">
      <el-table-column label="话题ID" align="center" prop="topicId" width="80" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" min-width="200">
        <template slot-scope="scope">
          <el-link type="primary" @click="handleView(scope.row)">{{ scope.row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="分类" align="center" prop="category" width="100">
        <template slot-scope="scope">
          <dict-tag :options="categoryTypeOptions" :value="String(scope.row.category)" />
        </template>
      </el-table-column>
      <el-table-column label="发布者" align="center" prop="userName" width="100" />
      <el-table-column label="审核状态" align="center" prop="auditStatus" width="100">
        <template slot-scope="scope">
          <dict-tag :options="auditStatusOptions" :value="String(scope.row.auditStatus)" />
        </template>
      </el-table-column>
      <el-table-column label="审核人" align="center" prop="auditBy" width="100" />
      <el-table-column label="审核时间" align="center" prop="auditTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.auditTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看</el-button>
          <el-button
            v-if="scope.row.auditStatus === 0"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleAudit(scope.row, 1)"
          >通过</el-button>
          <el-button
            v-if="scope.row.auditStatus === 0"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleAudit(scope.row, 2)"
          >驳回</el-button>
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

    <!-- 话题详情对话框 -->
    <el-dialog title="话题详情" :visible.sync="detailVisible" width="900px" append-to-body>
      <div class="topic-detail" v-if="currentTopic">
        <div class="detail-header">
          <h2>{{ currentTopic.title }}</h2>
          <div class="detail-badges">
            <dict-tag :options="categoryTypeOptions" :value="String(currentTopic.category)" />
            <dict-tag :options="auditStatusOptions" :value="String(currentTopic.auditStatus)" />
          </div>
        </div>

        <div class="detail-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="发布者">{{ currentTopic.userName }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ parseTime(currentTopic.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="审核状态">
              <dict-tag :options="auditStatusOptions" :value="String(currentTopic.auditStatus)" />
            </el-descriptions-item>
            <el-descriptions-item label="审核人">{{ currentTopic.auditBy || '未审核' }}</el-descriptions-item>
            <el-descriptions-item label="审核时间">{{ parseTime(currentTopic.auditTime) || '未审核' }}</el-descriptions-item>
            <el-descriptions-item label="审核备注">{{ currentTopic.auditRemark || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-content">
          <h4>话题内容</h4>
          <div class="content-text">{{ currentTopic.content }}</div>
        </div>

        <div class="detail-actions" v-if="currentTopic.auditStatus === 0">
          <el-button
            type="success"
            icon="el-icon-check"
            @click="handleAudit(currentTopic, 1)"
          >通过审核</el-button>
          <el-button
            type="danger"
            icon="el-icon-close"
            @click="handleAudit(currentTopic, 2)"
          >驳回审核</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog :title="auditTitle" :visible.sync="auditVisible" width="500px" append-to-body>
      <el-form ref="auditForm" :model="auditForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-tag v-if="auditForm.auditStatus === 1" type="success">通过</el-tag>
          <el-tag v-else type="danger">驳回</el-tag>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input
            v-model="auditForm.auditRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入审核备注（选填）"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="auditVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitAudit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTopic, getTopic, auditTopic } from "@/api/community/topic"
import { listAllCategory } from "@/api/community/category"

export default {
  name: "TopicAudit",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      pendingCount: 0,
      topicList: [],
      categoryList: [],
      auditStatusOptions: [],
      categoryTypeOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        category: null,
        auditStatus: 0
      },
      detailVisible: false,
      currentTopic: null,
      auditVisible: false,
      auditTitle: '',
      auditForm: {
        topicId: null,
        auditStatus: null,
        auditRemark: null
      }
    }
  },
  created() {
    this.getDicts('nh_audit_status').then(res => { this.auditStatusOptions = res.data; });
    this.getDicts('nh_community_category_type').then(res => { this.categoryTypeOptions = res.data; });
    this.getCategoryList()
    this.getList()
    this.getPendingCount()
  },
  methods: {
    getCategoryList() {
      listAllCategory().then(response => {
        this.categoryList = response.data || []
      })
    },
    getList() {
      this.loading = true
      listTopic(this.queryParams).then(response => {
        this.topicList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getPendingCount() {
      listTopic({ auditStatus: 0, pageNum: 1, pageSize: 1 }).then(response => {
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
      this.queryParams.auditStatus = 0
      this.handleQuery()
    },
    handleView(row) {
      getTopic(row.topicId).then(response => {
        this.currentTopic = response.data
        this.detailVisible = true
      })
    },
    handleAudit(row, auditStatus) {
      this.auditForm.topicId = row.topicId
      this.auditForm.auditStatus = auditStatus
      this.auditForm.auditRemark = null
      this.auditTitle = auditStatus === '1' ? '通过审核' : '驳回审核'
      this.auditVisible = true
    },
    submitAudit() {
      auditTopic(this.auditForm.topicId, this.auditForm.auditStatus, this.auditForm.auditRemark).then(() => {
        this.$modal.msgSuccess("审核成功")
        this.auditVisible = false
        this.detailVisible = false
        this.getList()
        this.getPendingCount()
      })
    },
    getCategoryName(categoryCode) {
      const category = this.categoryList.find(item => item.categoryCode === categoryCode)
      return category ? category.categoryName : categoryCode
    }
  }
}
</script>

<style lang="scss" scoped>
.topic-detail {
  .detail-header {
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 2px solid #f0f0f0;

    h2 {
      margin: 0 0 10px 0;
      color: #2c3e50;
    }

    .detail-badges {
      display: flex;
      gap: 8px;
    }
  }

  .detail-info {
    margin-bottom: 20px;
  }

  .detail-content {
    margin-bottom: 20px;

    h4 {
      color: #2c3e50;
      margin: 0 0 10px 0;
    }

    .content-text {
      padding: 15px;
      background: #f5f7fa;
      border-radius: 4px;
      line-height: 1.8;
      white-space: pre-wrap;
      color: #606266;
    }
  }

  .detail-actions {
    display: flex;
    gap: 10px;
    padding-top: 15px;
    border-top: 1px solid #f0f0f0;
  }
}
</style>
