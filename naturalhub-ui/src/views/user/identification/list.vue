<template>
  <div class="identification-list-container">
    <!-- 左上角视图切换 -->
    <div class="view-switch-top">
      <el-button
        :class="['view-btn', { active: viewMode === 'card' }]"
        @click="viewMode = 'card'"
      >
        <i class="el-icon-menu"></i> 卡片
      </el-button>
      <el-button
        :class="['view-btn', { active: viewMode === 'list' }]"
        @click="viewMode = 'list'"
      >
        <i class="el-icon-tickets"></i> 列表
      </el-button>
    </div>

    <!-- 顶部标题 -->
    <div class="page-header">
      <h1 class="page-title">我的物种鉴定</h1>
      <p class="page-subtitle">记录每一次未知的自然探索</p>
    </div>

    <!-- 搜索框区域 -->
    <div class="search-wrapper">
      <el-input
        v-model="queryParams.title"
        placeholder="搜索鉴定记录..."
        prefix-icon="el-icon-search"
        clearable
        @keyup.enter.native="handleQuery"
        class="search-input"
      />
      <el-button type="primary" @click="handleQuery" class="search-btn">搜索</el-button>
      <el-button type="success" icon="el-icon-plus" @click="handleAdd" class="add-btn">发起鉴定</el-button>
    </div>

    <!-- 内容区域 -->
    <div v-loading="loading" class="content-area">
      <!-- 卡片视图 -->
      <div v-if="viewMode === 'card' && identificationList.length > 0" class="grid-view">
        <div v-for="item in identificationList" :key="item.identificationId" class="grid-card" @click="handleView(item)">
          <div class="card-image-wrapper">
            <img v-if="item.coverImage" :src="item.coverImage" class="card-image" alt="identification">
            <div v-else class="card-image-placeholder"><i class="el-icon-picture-outline"></i></div>
            <!-- 审核状态标签 -->
            <div class="card-tag">
              <el-tag
                :type="getAuditStatusType(item.auditStatus)"
                size="small"
                effect="dark"
              >
                {{ getAuditStatusText(item.auditStatus) }}
              </el-tag>
            </div>
            <!-- 鉴定状态 -->
            <div class="card-tag-left">
              <el-tag size="mini" :type="getStatusType(item.status)">{{ getStatusText(item.status) }}</el-tag>
            </div>
          </div>
          <div class="card-info">
            <div class="card-title">{{ item.title }}</div>
            <div class="info-item single-line"><span class="label">地点：</span>{{ item.location }}</div>
            <div class="info-item single-line"><span class="label">时间：</span>{{ item.observationTime }}</div>
            <div class="info-item single-line"><span class="label">回答：</span>{{ item.answerCount || 0 }} 个</div>
            <div class="card-actions">
              <el-button size="mini" type="text" icon="el-icon-view" @click.stop="handleView(item)">查看</el-button>
              <el-button v-if="item.auditStatus === 0 || item.auditStatus === 3" size="mini" type="text"
                         icon="el-icon-edit" @click.stop="handleEdit(item)">编辑
              </el-button>
              <el-button v-if="item.auditStatus === 2 && item.isShared !== 1" size="mini" type="text" icon="el-icon-share"
                         @click.stop="handleShare(item)">分享
              </el-button>
              <el-button v-if="item.auditStatus === 0" size="mini" type="text" icon="el-icon-s-promotion"
                         @click.stop="handleSubmitReview(item)">提交
              </el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(item)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-if="viewMode === 'list'">
        <div class="list-header">
          <div class="header-col col-1">媒体</div>
          <div class="header-col col-2">标题</div>
          <div class="header-col col-3">观察时间</div>
          <div class="header-col col-4">地点</div>
          <div class="header-col col-5">操作</div>
        </div>

        <div v-if="identificationList.length > 0" class="list-content">
          <div
            v-for="(item, index) in identificationList"
            :key="item.identificationId"
            class="list-item"
            :class="{ 'gray-row': index % 2 === 1 }"
            @click="handleView(item)"
          >
            <div class="list-col col-1">
              <div class="list-image">
                <img v-if="item.coverImage" :src="item.coverImage" alt="identification">
                <div v-else class="list-image-placeholder"><i class="el-icon-picture-outline"></i></div>
              </div>
            </div>
            <div class="list-col col-2">
              <div class="species-name">{{ item.title }}</div>
            </div>
            <div class="list-col col-3">
              <div class="obs-date">{{ item.observationTime }}</div>
            </div>
            <div class="list-col col-4">
              <div class="obs-location">{{ item.location }}</div>
            </div>
            <div class="list-col col-5">
              <el-button size="mini" type="text" icon="el-icon-view" @click.stop="handleView(item)">查看</el-button>
              <el-button v-if="item.auditStatus === 0 || item.auditStatus === 3" size="mini" type="text"
                         icon="el-icon-edit" @click.stop="handleEdit(item)">编辑
              </el-button>
              <el-button v-if="item.auditStatus === 2 && item.isShared !== 1" size="mini" type="text" icon="el-icon-share"
                         @click.stop="handleShare(item)">分享
              </el-button>
              <el-button v-if="item.auditStatus === 0" size="mini" type="text" icon="el-icon-s-promotion"
                         @click.stop="handleSubmitReview(item)">提交
              </el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(item)">删除</el-button>
            </div>
          </div>
        </div>

        <el-empty v-if="identificationList.length === 0" description="暂无鉴定记录" class="empty-state">
          <el-button type="primary" @click="handleAdd">立即发起</el-button>
        </el-empty>
      </div>

      <!-- 卡片空状态 -->
      <el-empty v-if="viewMode === 'card' && identificationList.length === 0" description="暂无鉴定记录" class="empty-state">
        <el-button type="primary" @click="handleAdd">立即发起</el-button>
      </el-empty>
    </div>

    <!-- 分享弹窗 -->
    <el-dialog title="分享到社群" :visible.sync="shareDialogVisible" width="600px" append-to-body>
      <el-form :model="shareForm" :rules="shareRules" ref="shareForm" label-width="100px">
        <el-form-item label="选择板块" prop="category">
          <el-select v-model="shareForm.category" placeholder="请选择板块" style="width: 100%">
            <el-option label="鉴定求助" value="identify_help" />
            <el-option label="物种科普" value="species_science" />
            <el-option label="野外探索" value="field_explore" />
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
        <el-button @click="shareDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShare" :loading="shareLoading">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listIdentification, delIdentification, shareIdentification, updateIdentification } from '@/api/user/identification'

export default {
  name: 'IdentificationList',
  data() {
    return {
      viewMode: 'card',
      queryParams: {
        pageNum: 1,
        pageSize: 8,
        title: null,
        location: null,
        auditStatus: null,
        status: null
      },
      dateRange: [],
      identificationList: [],
      total: 0,
      loading: false,
      // 若依字典
      auditStatusOptions: [],
      identifyStatusOptions: [],
      // 分享
      shareDialogVisible: false,
      shareLoading: false,
      shareForm: {
        identificationId: null,
        category: 'identify_help',
        title: '',
        content: ''
      },
      shareRules: {
        category: [{ required: true, message: '请选择板块', trigger: 'change' }],
        title: [{ required: true, message: '请输入分享标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入分享内容', trigger: 'blur' }]
      }
    }
  },
  created() {
    // 加载若依字典
    this.getDicts('nh_audit_status').then(res => { this.auditStatusOptions = res.data || [] })
    this.getDicts('nh_identify_status').then(res => { this.identifyStatusOptions = res.data || [] })
    this.getList()
  },
  methods: {
    async getList() {
      this.loading = true
      const params = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        params.beginTime = this.dateRange[0]
        params.endTime = this.dateRange[1]
      }
      try {
        const res = await listIdentification(params)
        this.identificationList = res.rows.map(item => {
          if (item.images) {
            const images = item.images.split(',')
            item.coverImage = images[0] || null
          }
          return item
        })
        this.total = res.total
      } catch (e) {
        this.$message.error('加载失败')
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.queryParams.title = null
      this.queryParams.location = null
      this.queryParams.auditStatus = null
      this.queryParams.status = null
      this.handleQuery()
    },
    handleAdd() {
      this.$router.push('/user/identification/create')
    },
    handleView(row) {
      this.$router.push({ path: '/user/identification/detail', query: { id: row.identificationId } })
    },
    handleEdit(row) {
      this.$router.push('/user/identification/create?id=' + row.identificationId)
    },
    handleSubmitReview(row) {
      this.$confirm('提交审核后将无法修改，是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => updateIdentification({ identificationId: row.identificationId, auditStatus: 1 }))
        .then(() => {
          this.$message.success('提交审核成功')
          this.getList()
        })
    },
    handleShare(row) {
      this.shareForm = {
        identificationId: row.identificationId,
        category: 'identify_help',
        title: row.title,
        content: row.description || ''
      }
      this.shareDialogVisible = true
    },
    confirmShare() {
      this.$refs.shareForm.validate(valid => {
        if (valid) {
          this.shareLoading = true
          shareIdentification(this.shareForm.identificationId).then(() => {
            this.$message.success('分享成功')
            this.shareDialogVisible = false
            this.getList()
          }).finally(() => {
            this.shareLoading = false
          })
        }
      })
    },
    handleDelete(row) {
      this.$confirm('是否确认删除该鉴定记录?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => delIdentification(row.identificationId))
        .then(() => {
          this.getList()
          this.$message.success('删除成功')
        })
    },

    getStatusText(value) {
      const item = this.identifyStatusOptions.find(i => i.dictValue == value)
      return item ? item.dictLabel : value
    },
    getStatusType(value) {
      const item = this.identifyStatusOptions.find(i => i.dictValue == value)
      return item ? item.listClass : 'info'
    },
    getAuditStatusText(value) {
      const item = this.auditStatusOptions.find(i => i.dictValue == value)
      return item ? item.dictLabel : value
    },
    getAuditStatusType(value) {
      const item = this.auditStatusOptions.find(i => i.dictValue == value)
      return item ? item.listClass : 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
.identification-list-container {
  padding: 20px 30px;
  background: #f5f5f5;
  min-height: 130vh;
  position: relative;
}

.view-switch-top {
  position: absolute;
  top: 20px;
  left: 30px;
  display: flex;
  z-index: 10;

  .view-btn {
    height: 36px;
    width: 80px;
    border: 1px solid #ddd;
    background: #fff;
    color: #666;
    font-size: 14px;

    &.active {
      background: #409eff;
      color: #fff;
      border-color: #409eff;
    }

    &:first-child {
      border-radius: 4px 0 0 4px;
      border-right: none;
    }

    &:last-child {
      border-radius: 0 4px 4px 0;
    }
  }
}

.page-header {
  text-align: center;
  margin-bottom: 20px;
  padding-top: 20px;

  .page-title {
    font-size: 28px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px 0;
  }

  .page-subtitle {
    font-size: 14px;
    color: #999;
    margin: 0;
  }
}

.search-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  flex-wrap: nowrap !important;
  width: 100%;

  .search-input {
    flex: 1;
    height: 44px;
    font-size: 14px;
    border-radius: 4px;
    display: flex;
    align-items: center;

    :deep(.el-input__prefix) {
      display: flex;
      align-items: center;
      top: 50%;
      transform: translateY(-50%);
    }
  }

  .search-btn, .add-btn {
    height: 44px;
    padding: 0 20px;
    font-size: 14px;
    border-radius: 4px;
    white-space: nowrap;
    flex-shrink: 0;
  }
}

.content-area {
  margin-bottom: 20px;

  .grid-view {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;

    .grid-card {
      background: #fff;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
      transition: all 0.2s;
      cursor: pointer;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 3px 8px rgba(0, 0, 0, 0.12);
      }

      .card-image-wrapper {
        width: 100%;
        height: 180px;
        background: #fafafa;
        overflow: hidden;
        position: relative;

        .card-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .card-image-placeholder {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 40px;
          color: #ddd;
        }
      }

      .card-tag {
        position: absolute;
        top: 6px;
        right: 6px;
        z-index: 1;
      }

      .card-tag-left {
        position: absolute;
        top: 6px;
        left: 6px;
        z-index: 1;
      }

      .card-info {
        padding: 14px;

        .card-title {
          font-size: 15px;
          font-weight: 600;
          color: #333;
          margin-bottom: 8px;
          line-height: 1.3;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .info-item {
          margin-bottom: 6px;
          font-size: 12px;
          color: #666;

          .label {
            font-weight: 400;
            color: #888;
          }
        }

        .single-line {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .card-actions {
          margin-top: 10px;
          display: flex;
          gap: 6px;
        }
      }
    }
  }

  .list-header {
    display: flex;
    background: #e9e9e9;
    border-bottom: 1px solid #ddd;
    font-weight: 500;
    font-size: 14px;
    color: #333;

    .header-col {
      padding: 8px 10px;
      flex: 1;
      min-width: 140px;
    }
  }

  .list-content {
    border: 1px solid #ddd;
    border-top: none;

    .list-item {
      display: flex;
      align-items: center;
      background: #fff;
      border-bottom: 1px solid #e5e5e5;
      cursor: pointer;
      min-height: 90px;

      &.gray-row {
        background: #fafafa;
      }

      .list-col {
        padding: 8px 10px;
        flex: 1;
        min-width: 140px;
      }

      .list-image {
        width: 80px;
        height: 80px;
        border-radius: 4px;
        overflow: hidden;
        background: #f0f0f0;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
    }
  }

  .empty-state {
    padding: 60px 0;
    text-align: center;
  }
}

@media (max-width: 1200px) {
  .grid-view {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 992px) {
  .grid-view {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 768px) {
  .identification-list-container { padding: 15px 10px; }
  .grid-view { grid-template-columns: 1fr; }
  .view-switch-top { left: 10px; }

  .search-wrapper {
    flex-wrap: wrap !important;
    .search-input { width: 100%; }
    .search-btn, .add-btn { flex: 1; }
  }
}
</style>
