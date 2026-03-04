<template>
  <div class="observation-list-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入标题"
            clearable
            size="small"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="物种类型" prop="speciesType">
          <el-select v-model="queryParams.speciesType" placeholder="请选择" clearable size="small">
            <el-option label="植物" value="plant" />
            <el-option label="动物" value="animal" />
            <el-option label="真菌" value="fungi" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="物种名称" prop="speciesName">
          <el-input
            v-model="queryParams.speciesName"
            placeholder="请输入物种名称"
            clearable
            size="small"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="观察地点" prop="location">
          <el-input
            v-model="queryParams.location"
            placeholder="请输入地点"
            clearable
            size="small"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="审核状态" prop="reviewStatus">
          <el-select v-model="queryParams.reviewStatus" placeholder="请选择" clearable size="small">
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="草稿" value="draft" />
          </el-select>
        </el-form-item>
        <el-form-item label="观察时间">
          <el-date-picker
            v-model="dateRange"
            size="small"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="toolbar-card" shadow="never">
      <el-row :gutter="10">
        <el-col :span="1.5">
          <el-button
            type="primary"
            icon="el-icon-plus"
            size="small"
            @click="handleAdd"
          >新增观察</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="success"
            icon="el-icon-download"
            size="small"
            @click="handleExport"
          >导出</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="info"
            icon="el-icon-pie-chart"
            size="small"
            @click="handleStatistics"
          >统计分析</el-button>
        </el-col>
        <el-col :span="1.5" class="view-switch">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="card">
              <i class="el-icon-menu"></i> 卡片
            </el-radio-button>
            <el-radio-button label="list">
              <i class="el-icon-s-operation"></i> 列表
            </el-radio-button>
          </el-radio-group>
        </el-col>
      </el-row>
    </el-card>

    <!-- 卡片视图 -->
    <div v-if="viewMode === 'card'" class="card-view">
      <el-row :gutter="20">
        <el-col 
          v-for="item in observationList" 
          :key="item.recordId"
          :xs="24" 
          :sm="12" 
          :md="8" 
          :lg="6"
          class="card-col"
        >
          <el-card class="observation-card" :body-style="{ padding: '0px' }" shadow="hover">
            <!-- 图片封面 -->
            <div class="card-image" @click="handleView(item)">
              <img 
                v-if="item.coverImage" 
                :src="item.coverImage" 
                class="image"
              >
              <div v-else class="no-image">
                <i class="el-icon-picture-outline"></i>
              </div>
              <!-- 审核状态标签 -->
              <div class="status-tag" :class="'status-' + item.reviewStatus">
                <el-tag 
                  :type="getStatusType(item.reviewStatus)" 
                  size="small"
                  effect="dark"
                >
                  {{ getStatusText(item.reviewStatus) }}
                </el-tag>
              </div>
            </div>
            
            <!-- 卡片内容 -->
            <div class="card-content">
              <div class="card-title" @click="handleView(item)">{{ item.title }}</div>
              <div class="card-info">
                <div class="info-item">
                  <i class="el-icon-location-outline"></i>
                  <span>{{ item.location }}</span>
                </div>
                <div class="info-item">
                  <i class="el-icon-date"></i>
                  <span>{{ item.observationTime }}</span>
                </div>
                <div class="info-item">
                  <el-tag size="mini" type="success">{{ getSpeciesTypeText(item.speciesType) }}</el-tag>
                  <span class="species-name">{{ item.speciesName }}</span>
                </div>
              </div>
              
              <!-- 操作按钮 -->
              <div class="card-actions">
                <el-button-group>
                  <el-button size="mini" icon="el-icon-view" @click="handleView(item)">查看</el-button>
                  <el-button 
                    v-if="item.reviewStatus === 'draft' || item.reviewStatus === 'rejected'"
                    size="mini" 
                    icon="el-icon-edit" 
                    @click="handleEdit(item)"
                  >编辑</el-button>
                  <el-button 
                    v-if="item.reviewStatus === 'approved'"
                    size="mini" 
                    type="success"
                    icon="el-icon-share" 
                    @click="handleShare(item)"
                  >分享</el-button>
                  <el-button 
                    v-if="item.reviewStatus === 'draft'"
                    size="mini" 
                    type="primary"
                    icon="el-icon-s-promotion" 
                    @click="handleSubmitReview(item)"
                  >提交审核</el-button>
                  <el-button 
                    size="mini" 
                    type="danger"
                    icon="el-icon-delete" 
                    @click="handleDelete(item)"
                  >删除</el-button>
                </el-button-group>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 空状态 -->
      <el-empty v-if="observationList.length === 0" description="暂无观察记录">
        <el-button type="primary" @click="handleAdd">立即创建</el-button>
      </el-empty>
    </div>

    <!-- 列表视图 -->
    <el-card v-else class="list-view" shadow="never">
      <el-table 
        v-loading="loading" 
        :data="observationList"
        @row-click="handleView"
        style="width: 100%"
      >
        <el-table-column label="封面" width="100" align="center">
          <template slot-scope="scope">
            <img 
              v-if="scope.row.coverImage" 
              :src="scope.row.coverImage" 
              class="table-image"
            >
            <div v-else class="table-no-image">
              <i class="el-icon-picture-outline"></i>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="标题" prop="title" min-width="150" show-overflow-tooltip />
        
        <el-table-column label="物种信息" min-width="120">
          <template slot-scope="scope">
            <el-tag size="mini" type="success">{{ getSpeciesTypeText(scope.row.speciesType) }}</el-tag>
            <div class="species-name-text">{{ scope.row.speciesName }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="观察地点" prop="location" min-width="120" show-overflow-tooltip />
        
        <el-table-column label="观察时间" prop="observationTime" width="160" />
        
        <el-table-column label="审核状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.reviewStatus)" size="small">
              {{ getStatusText(scope.row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-view" @click.stop="handleView(scope.row)">查看</el-button>
            <el-button 
              v-if="scope.row.reviewStatus === 'draft' || scope.row.reviewStatus === 'rejected'"
              size="mini" 
              type="text" 
              icon="el-icon-edit" 
              @click.stop="handleEdit(scope.row)"
            >编辑</el-button>
            <el-button 
              v-if="scope.row.reviewStatus === 'approved'"
              size="mini" 
              type="text"
              icon="el-icon-share" 
              @click.stop="handleShare(scope.row)"
            >分享</el-button>
            <el-button 
              v-if="scope.row.reviewStatus === 'draft'"
              size="mini" 
              type="text"
              icon="el-icon-s-promotion" 
              @click.stop="handleSubmitReview(scope.row)"
            >提交审核</el-button>
            <el-button 
              size="mini" 
              type="text" 
              icon="el-icon-delete" 
              @click.stop="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 分享到社群对话框 -->
    <el-dialog title="分享到社群" :visible.sync="shareDialogVisible" width="600px" append-to-body>
      <el-form :model="shareForm" :rules="shareRules" ref="shareForm" label-width="100px">
        <el-form-item label="选择板块" prop="topicType">
          <el-select v-model="shareForm.topicType" placeholder="请选择板块" style="width: 100%">
            <el-option label="观察分享" value="observation" />
            <el-option label="物种讨论" value="species" />
            <el-option label="经验交流" value="experience" />
          </el-select>
        </el-form-item>
        <el-form-item label="分享标题" prop="title">
          <el-input v-model="shareForm.title" placeholder="请输入分享标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分享内容" prop="content">
          <el-input 
            v-model="shareForm.content" 
            type="textarea" 
            :rows="5"
            placeholder="请输入分享内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="shareDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmShare" :loading="shareLoading">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRecord, delRecord, exportRecord, submitForReview, shareRecordToCommunity } from '@/api/user/record'

export default {
  name: 'ObservationList',
  data() {
    return {
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 12,
        title: null,
        speciesType: null,
        speciesName: null,
        location: null,
        reviewStatus: null
      },
      // 日期范围
      dateRange: [],
      // 列表数据
      observationList: [],
      total: 0,
      loading: false,
      // 视图模式
      viewMode: 'card',
      // 分享对话框
      shareDialogVisible: false,
      shareLoading: false,
      shareForm: {
        diaryId: null,
        topicType: '',
        title: '',
        content: ''
      },
      shareRules: {
        topicType: [
          { required: true, message: '请选择板块', trigger: 'change' }
        ],
        title: [
          { required: true, message: '请输入分享标题', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入分享内容', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true
      const params = {
        ...this.queryParams
      }
      if (this.dateRange && this.dateRange.length === 2) {
        params.beginTime = this.dateRange[0]
        params.endTime = this.dateRange[1]
      }
      listRecord(params).then(response => {
        this.observationList = response.rows.map(item => {
          // 解析封面图片
          if (item.images) {
            const images = JSON.parse(item.images)
            item.coverImage = images.length > 0 ? images[0] : null
          }
          return item
        })
        this.total = response.total
        this.loading = false
      })
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.$router.push('/user/observation/upload')
    },

    /** 查看详情 */
    handleView(row) {
      this.$router.push('/user/observation/detail/' + row.recordId)
    },

    /** 编辑按钮操作 */
    handleEdit(row) {
      this.$router.push('/user/observation/upload/' + row.recordId)
    },

    /** 提交审核 */
    handleSubmitReview(row) {
      this.$confirm('提交审核后将无法修改，是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return submitForReview(row.recordId)
      }).then(() => {
        this.$message.success('提交审核成功')
        this.getList()
      })
    },

    /** 分享按钮操作 */
    handleShare(row) {
      this.shareForm = {
        diaryId: row.recordId,
        topicType: 'observation',
        title: row.title,
        content: row.description ? row.description.substring(0, 200) : ''
      }
      this.shareDialogVisible = true
    },

    /** 确认分享 */
    confirmShare() {
      this.$refs.shareForm.validate(valid => {
        if (valid) {
          this.shareLoading = true
          shareRecordToCommunity(this.shareForm).then(response => {
            this.$message.success('分享成功')
            this.shareDialogVisible = false
            this.shareLoading = false
          }).catch(() => {
            this.shareLoading = false
          })
        }
      })
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      this.$confirm('是否确认删除该观察记录?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delRecord(row.recordId)
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      })
    },

    /** 导出按钮操作 */
    handleExport() {
      this.$confirm('是否确认导出所有观察记录数据?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return exportRecord(this.queryParams)
      }).then(response => {
        this.download(response.msg)
      })
    },

    /** 统计分析 */
    handleStatistics() {
      this.$router.push('/user/observation/statistics')
    },

    /** 获取物种类型文本 */
    getSpeciesTypeText(type) {
      const typeMap = {
        plant: '植物',
        animal: '动物',
        fungi: '真菌',
        other: '其他'
      }
      return typeMap[type] || type
    },

    /** 获取审核状态文本 */
    getStatusText(status) {
      const statusMap = {
        draft: '草稿',
        pending: '待审核',
        approved: '已通过',
        rejected: '已驳回'
      }
      return statusMap[status] || status
    },

    /** 获取审核状态类型 */
    getStatusType(status) {
      const typeMap = {
        draft: 'info',
        pending: 'warning',
        approved: 'success',
        rejected: 'danger'
      }
      return typeMap[status] || 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
.observation-list-container {
  padding: 20px;
  
  .search-card,
  .toolbar-card {
    margin-bottom: 20px;
  }
  
  .view-switch {
    float: right;
  }
  
  // 卡片视图样式
  .card-view {
    .card-col {
      margin-bottom: 20px;
    }
    
    .observation-card {
      height: 100%;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-5px);
      }
      
      .card-image {
        position: relative;
        height: 200px;
        overflow: hidden;
        background: #f5f7fa;
        
        .image {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        .no-image {
          display: flex;
          align-items: center;
          justify-content: center;
          height: 100%;
          font-size: 48px;
          color: #dcdfe6;
        }
        
        .status-tag {
          position: absolute;
          top: 10px;
          right: 10px;
        }
      }
      
      .card-content {
        padding: 15px;
        
        .card-title {
          font-size: 16px;
          font-weight: bold;
          margin-bottom: 10px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          color: #303133;
          
          &:hover {
            color: #409EFF;
          }
        }
        
        .card-info {
          margin-bottom: 15px;
          
          .info-item {
            display: flex;
            align-items: center;
            margin-bottom: 8px;
            font-size: 13px;
            color: #606266;
            
            i {
              margin-right: 5px;
              color: #909399;
            }
            
            .species-name {
              margin-left: 5px;
            }
          }
        }
        
        .card-actions {
          border-top: 1px solid #ebeef5;
          padding-top: 10px;
          
          .el-button-group {
            display: flex;
            flex-wrap: wrap;
            gap: 5px;
          }
        }
      }
    }
  }
  
  // 列表视图样式
  .list-view {
    .table-image {
      width: 60px;
      height: 60px;
      object-fit: cover;
      border-radius: 4px;
    }
    
    .table-no-image {
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f7fa;
      border-radius: 4px;
      font-size: 24px;
      color: #dcdfe6;
    }
    
    .species-name-text {
      margin-top: 5px;
      font-size: 12px;
      color: #606266;
    }
  }
}

// 移动端适配
@media screen and (max-width: 768px) {
  .observation-list-container {
    padding: 10px;
    
    .search-card {
      ::v-deep .el-form-item {
        margin-bottom: 10px;
      }
    }
    
    .view-switch {
      float: none;
      margin-top: 10px;
    }
    
    .card-view {
      .observation-card {
        .card-image {
          height: 150px;
        }
        
        .card-content {
          .card-actions {
            .el-button-group {
              .el-button {
                font-size: 12px;
                padding: 5px 10px;
              }
            }
          }
        }
      }
    }
  }
}
</style>
