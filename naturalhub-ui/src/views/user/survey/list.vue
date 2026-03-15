<template>
  <div class="survey-list-container">
    <!-- 左上角视图切换 -->
    <div class="view-switch-top">
      <el-button
        :class="['view-btn', { active: viewType === 'grid' }]"
        @click="viewType = 'grid'"
      >
        <i class="el-icon-menu"></i> 网格
      </el-button>
      <el-button
        :class="['view-btn', { active: viewType === 'list' }]"
        @click="viewType = 'list'"
      >
        <i class="el-icon-tickets"></i> 列表
      </el-button>
    </div>

    <!-- 顶部标题 -->
    <div class="page-header">
      <h1 class="page-title">我的野外调查</h1>
      <p class="page-subtitle">记录每一次专业的野外调查</p>
    </div>

    <!-- 搜索框区域 -->
    <div class="search-wrapper">
      <el-input
        v-model="queryParams.title"
        placeholder="搜索调查记录..."
        prefix-icon="el-icon-search"
        clearable
        @keyup.enter.native="handleQuery"
        class="search-input"
      />
      <el-button type="primary" @click="handleQuery" class="search-btn">搜索</el-button>
      <el-button type="success" icon="el-icon-plus" @click="handleAdd" class="add-btn">创建调查</el-button>
    </div>

    <!-- 内容区域 -->
    <div v-loading="loading" class="content-area">
      <!-- 网格视图 -->
      <div v-if="viewType === 'grid' && surveyList.length > 0" class="grid-view">
        <div v-for="item in surveyList" :key="item.surveyId" class="grid-card" @click="handleView(item)">
          <div class="card-image-wrapper">
            <img v-if="item.coverImage" :src="item.coverImage" class="card-image" alt="survey">
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
            <!-- 生境类型 -->
            <div class="card-tag-left">
              <el-tag size="mini" type="success">{{ getHabitatTypeText(item.habitatType) }}</el-tag>
            </div>
          </div>
          <div class="card-info">
            <div class="card-title">{{ item.title }}</div>
            <div class="info-item single-line"><span class="label">地点：</span>{{ item.location }}</div>
            <div class="info-item single-line"><span class="label">时间：</span>{{ formatGridDateTime(item.surveyDate) }}</div>
            <div class="info-item single-line"><span class="label">物种：</span>{{ item.speciesCount || 0 }} 种</div>
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
      <div v-if="viewType === 'list'">
        <div class="list-header">
          <div class="header-col col-1">媒体</div>
          <div class="header-col col-2">标题</div>
          <div class="header-col col-3">调查日期</div>
          <div class="header-col col-4">地点</div>
          <div class="header-col col-5">操作</div>
        </div>

        <div v-if="surveyList.length > 0" class="list-content">
          <div
            v-for="(item, index) in surveyList"
            :key="item.surveyId"
            class="list-item"
            :class="{ 'gray-row': index % 2 === 1 }"
            @click="handleView(item)"
          >
            <div class="list-col col-1">
              <div class="list-image">
                <img v-if="item.coverImage" :src="item.coverImage" alt="survey">
                <div v-else class="list-image-placeholder"><i class="el-icon-picture-outline"></i></div>
              </div>
            </div>
            <div class="list-col col-2">
              <div class="species-name">{{ item.title }}</div>
            </div>
            <div class="list-col col-3">
              <div class="obs-date">
                {{ formatDatePart(item.surveyDate) }}<br/>
                {{ formatTimePart(item.surveyDate) }}
              </div>
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

        <el-empty v-if="surveyList.length === 0" description="暂无调查记录" class="empty-state">
          <el-button type="primary" @click="handleAdd">立即创建</el-button>
        </el-empty>
      </div>

      <!-- 网格空状态 -->
      <el-empty v-if="viewType === 'grid' && surveyList.length === 0" description="暂无调查记录" class="empty-state">
        <el-button type="primary" @click="handleAdd">立即创建</el-button>
      </el-empty>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more" :class="{loading: loadingMore}">
        <span v-if="!loadingMore">下拉加载更多</span>
        <span v-else>加载中...</span>
      </div>
    </div>

    <!-- 分享弹窗 -->
    <el-dialog title="分享到社群" :visible.sync="shareDialogVisible" width="600px" append-to-body>
      <el-form :model="shareForm" :rules="shareRules" ref="shareForm" label-width="100px">
        <el-form-item label="选择板块" prop="topicType">
          <el-select v-model="shareForm.topicType" placeholder="请选择板块" style="width: 100%">
            <el-option label="调查分享" value="survey"/>
            <el-option label="物种讨论" value="species"/>
            <el-option label="经验交流" value="experience"/>
          </el-select>
        </el-form-item>
        <el-form-item label="分享标题" prop="title">
          <el-input v-model="shareForm.title" placeholder="请输入分享标题" maxlength="100" show-word-limit/>
        </el-form-item>
        <el-form-item label="分享内容" prop="content">
          <el-input v-model="shareForm.content" type="textarea" :rows="5" placeholder="请输入分享内容" maxlength="500"
                    show-word-limit/>
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
import { listSurvey, delSurvey, shareSurvey, updateSurvey } from '@/api/user/survey'

export default {
  name: 'SurveyList',
  data() {
    return {
      viewType: 'grid',
      queryParams: { pageNum: 1, pageSize: 8, title: null },
      auditStatusOptions: [],
      habitatTypeOptions: [],
      surveyList: [],
      total: 0,
      loading: false,
      loadingMore: false,
      hasMore: true,
      shareDialogVisible: false,
      shareLoading: false,
      shareForm: { surveyId: null, topicType: '', title: '', content: '' },
      shareRules: {
        topicType: [{ required: true, message: '请选择板块', trigger: 'change' }],
        title: [{ required: true, message: '请输入分享标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入分享内容', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getDicts('nh_audit_status').then(res => { this.auditStatusOptions = res.data || [] })
    this.getDicts('nh_habitat_type').then(res => { this.habitatTypeOptions = res.data || [] })
    this.getList()
    this.bindScroll()
  },
  destroyed() {
    window.removeEventListener('scroll', this.handleScroll)
  },
  methods: {
    formatGridDateTime(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },
    formatDatePart(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
      return `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}`
    },
    formatTimePart(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      let hours = date.getHours()
      const ampm = hours >= 12 ? 'PM' : 'AM'
      hours = hours % 12 || 12
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${hours}:${minutes} ${ampm}`
    },
    getAuditStatusText(status) {
      const item = this.auditStatusOptions.find(d => d.dictValue == status)
      return item ? item.dictLabel : ''
    },
    getAuditStatusType(status) {
      switch (status) {
        case 0: return ''
        case 1: return 'warning'
        case 2: return 'success'
        case 3: return 'danger'
        default: return ''
      }
    },
    getHabitatTypeText(val) {
      const item = this.habitatTypeOptions.find(d => parseInt(d.dictValue) === val)
      return item ? item.dictLabel : '未知'
    },
    async getList(loadMore = false) {
      if (!loadMore) {
        this.surveyList = []
        this.queryParams.pageNum = 1
        this.hasMore = true
      }
      this.loading = !loadMore
      this.loadingMore = loadMore
      try {
        const res = await listSurvey(this.queryParams)
        const newList = res.rows.map(item => {
          if (item.images) {
            const imgs = item.images.split(',')
            item.coverImage = imgs[0] || null
          }
          return item
        })
        this.surveyList = [...this.surveyList, ...newList]
        this.total = res.total
        this.hasMore = this.surveyList.length < this.total
      } catch (e) {
        this.$message.error('数据加载失败')
      } finally {
        this.loading = false
        this.loadingMore = false
      }
    },
    handleQuery() {
      this.getList(false)
    },
    handleAdd() {
      this.$router.push('/user/survey/create')
    },
    handleView(row) {
      this.$router.push({ path: '/user/survey/detail', query: { id: row.surveyId } })
    },
    handleEdit(row) {
      this.$router.push('/user/survey/create?id=' + row.surveyId)
    },
    handleSubmitReview(row) {
      this.$confirm('提交审核后将无法修改，是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return updateSurvey({ surveyId: row.surveyId, auditStatus: 1 })
      }).then(() => {
        this.$message.success('提交审核成功')
        this.getList()
      })
    },
    handleShare(row) {
      this.shareForm = {
        surveyId: row.surveyId,
        topicType: 'survey',
        title: row.title,
        content: '本次调查发现' + (row.speciesCount || 0) + '个物种'
      }
      this.shareDialogVisible = true
    },
    confirmShare() {
      this.$refs.shareForm.validate(valid => {
        if (valid) {
          this.shareLoading = true
          shareSurvey(this.shareForm.surveyId).then(() => {
            this.$message.success('分享成功')
            this.shareDialogVisible = false
            this.getList()
          }).catch(() => {
            this.shareLoading = false
          })
        }
      })
    },
    handleDelete(row) {
      this.$confirm('是否确认删除该调查记录?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => delSurvey(row.surveyId)).then(() => {
        this.getList()
        this.$message.success('删除成功')
      })
    },
    bindScroll() {
      window.addEventListener('scroll', this.handleScroll)
    },
    handleScroll() {
      if (this.loadingMore || !this.hasMore) return
      const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
      const clientHeight = document.documentElement.clientHeight
      const scrollHeight = document.documentElement.scrollHeight
      if (scrollTop + clientHeight + 200 >= scrollHeight) {
        this.queryParams.pageNum++
        this.getList(true)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.survey-list-container {
  padding: 20px 30px;
  background: #f5f5f5;
  min-height: 130vh;
  position: relative;
}

// 视图切换
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

// 标题
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

// 搜索栏 强制一行 不换行
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

    // 修复搜索图标垂直居中
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

// 内容
.content-area {
  margin-bottom: 20px;

  // 网格
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
          max-width: 100%;
        }

        // 单行隐藏 不换行
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

  // 列表
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

  .load-more {
    text-align: center;
    padding: 16px 0;
    font-size: 14px;
    color: #999;

    &.loading {
      color: #409eff;
    }
  }
}

// 响应式
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
  .survey-list-container { padding: 15px 10px; }
  .grid-view { grid-template-columns: 1fr; }
  .view-switch-top { left: 10px; }

  .search-wrapper {
    flex-wrap: wrap !important;
    .search-input { width: 100%; }
    .search-btn, .add-btn { flex: 1; }
  }
}
</style>
