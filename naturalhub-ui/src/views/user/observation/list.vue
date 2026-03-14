<template>
  <div class="observation-list-container">
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
      <h1 class="page-title">我的观察记录</h1>
      <p class="page-subtitle">记录每一次与自然的相遇</p>
    </div>

    <!-- 搜索框区域 -->
    <div class="search-wrapper">
      <el-input
        v-model="queryParams.title"
        placeholder="搜索观察记录..."
        prefix-icon="el-icon-search"
        clearable
        @keyup.enter.native="handleQuery"
        class="search-input"
      />
      <el-button type="primary" @click="handleQuery" class="search-btn">搜索</el-button>
      <el-button type="success" icon="el-icon-plus" @click="handleAdd" class="add-btn">新增观察记录</el-button>
    </div>

    <!-- 内容区域 -->
    <div v-loading="loading" class="content-area">
      <!-- 网格视图 -->
      <div v-if="viewType === 'grid' && observationList.length > 0" class="grid-view">
        <div v-for="item in observationList" :key="item.recordId" class="grid-card" @click="handleView(item)">
          <div class="card-image-wrapper">
            <img v-if="item.coverImage" :src="item.coverImage" class="card-image" alt="observation">
            <div v-else class="card-image-placeholder"><i class="el-icon-picture-outline"></i></div>
            <!-- 审核状态标签 → 右上角 -->
            <div class="card-tag">
              <el-tag
                :type="getAuditStatusType(item.auditStatus)"
                size="small"
                effect="dark"
              >
                {{ getAuditStatusText(item.auditStatus) }}
              </el-tag>
            </div>
          </div>
          <div class="card-info">
            <!-- 独立的标题样式 -->
            <div class="card-title">{{ item.title }}</div>
            <div class="info-item"><span class="label">物种：</span>{{ item.speciesName }}</div>
            <div class="info-item"><span class="label">地点：</span>{{ item.location }}</div>
            <div class="info-item"><span class="label">时间：</span>{{ formatGridDateTime(item.observationTime) }}</div>
            <div class="card-actions">
              <el-button size="mini" type="text" icon="el-icon-view" @click.stop="handleView(item)">查看</el-button>
              <el-button v-if="item.auditStatus === 0 || item.auditStatus === 3" size="mini" type="text"
                         icon="el-icon-edit" @click.stop="handleEdit(item)">编辑
              </el-button>
              <el-button v-if="item.auditStatus === 2" size="mini" type="text" icon="el-icon-share"
                         @click.stop="handleShare(item)">分享
              </el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(item)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-if="viewType === 'list'">
        <!-- 列表表头 -->
        <div class="list-header">
          <div class="header-col col-1">媒体</div>
          <div class="header-col col-2">名称</div>
          <div class="header-col col-3">观察日期</div>
          <div class="header-col col-4">地方</div>
          <div class="header-col col-5">操作</div>
        </div>

        <!-- 列表内容 -->
        <div v-if="observationList.length > 0" class="list-content">
          <div
            v-for="(item, index) in observationList"
            :key="item.recordId"
            class="list-item"
            :class="{ 'gray-row': index % 2 === 1 }"
            @click="handleView(item)"
          >
            <!-- 媒体列 -->
            <div class="list-col col-1">
              <div class="list-image">
                <img v-if="item.coverImage" :src="item.coverImage" alt="observation">
                <div v-else class="list-image-placeholder"><i class="el-icon-picture-outline"></i></div>
              </div>
            </div>

            <!-- 名称列 -->
            <div class="list-col col-2">
              <div class="species-name">{{ item.speciesName }}</div>
            </div>

            <!-- 日期列 -->
            <div class="list-col col-3">
              <div class="obs-date">
                {{ formatDatePart(item.observationTime) }}<br/>
                {{ formatTimePart(item.observationTime) }}
              </div>
            </div>

            <!-- 地点列 -->
            <div class="list-col col-4">
              <div class="obs-location">{{ item.location }}</div>
            </div>

            <!-- 操作列 -->
            <div class="list-col col-5">
              <el-button size="mini" type="text" icon="el-icon-view" @click.stop="handleView(item)">查看</el-button>
              <el-button v-if="item.auditStatus === 0 || item.auditStatus === 3" size="mini" type="text"
                         icon="el-icon-edit" @click.stop="handleEdit(item)">编辑
              </el-button>
              <el-button v-if="item.auditStatus === 2" size="mini" type="text" icon="el-icon-share"
                         @click.stop="handleShare(item)">分享
              </el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(item)">删除</el-button>
            </div>
          </div>
        </div>

        <!-- 列表空状态 -->
        <el-empty v-if="observationList.length === 0" description="暂无观察记录" class="empty-state">
          <el-button type="primary" @click="handleAdd">立即创建</el-button>
        </el-empty>
      </div>

      <!-- 网格视图空状态 -->
      <el-empty v-if="viewType === 'grid' && observationList.length === 0" description="暂无观察记录"
                class="empty-state">
        <el-button type="primary" @click="handleAdd">立即创建</el-button>
      </el-empty>

      <!-- 加载更多提示 -->
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
            <el-option label="观察分享" value="observation"/>
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
import {listRecord, delRecord, submitForReview, shareRecordToCommunity} from '@/api/user/record'

export default {
  name: 'ObservationList',
  data() {
    return {
      viewType: 'grid', // 默认网格视图
      queryParams: {pageNum: 1, pageSize: 8, title: null},
      speciesTypeOptions: [],
      auditStatusOptions: [],
      observationList: [],
      total: 0,
      loading: false,
      loadingMore: false,
      hasMore: true,
      shareDialogVisible: false,
      shareLoading: false,
      shareForm: {recordId: null, topicType: '', title: '', content: ''},
      shareRules: {
        topicType: [{required: true, message: '请选择板块', trigger: 'change'}],
        title: [{required: true, message: '请输入分享标题', trigger: 'blur'}],
        content: [{required: true, message: '请输入分享内容', trigger: 'blur'}]
      }
    }
  },
  created() {
    this.getDicts('nh_species_type').then(res => {
      this.speciesTypeOptions = res.data || []
    })
    this.getDicts('nh_audit_status').then(res => {
      this.auditStatusOptions = res.data || []
    })
    this.getList()
    this.bindScroll()
  },
  destroyed() {
    window.removeEventListener('scroll', this.handleScroll)
  },
  methods: {
    // 网格视图时间格式化
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
    // 列表视图日期格式化
    formatDatePart(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
      const month = months[date.getMonth()]
      const day = date.getDate()
      const year = date.getFullYear()
      return `${month} ${day}, ${year}`
    },
    // 列表视图时间格式化
    formatTimePart(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      let hours = date.getHours()
      const ampm = hours >= 12 ? 'PM' : 'AM'
      hours = hours % 12
      hours = hours ? hours : 12
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${hours}:${minutes} ${ampm} PST`
    },
    // 日期格式化（备用）
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
    },
    getAuditStatusText(status) {
      const item = this.auditStatusOptions.find(d => d.dictValue == status);
      return item ? item.dictLabel : "";
    },
    getAuditStatusType(status) {
      switch (status) {
        case 0: return "";        // 未提交
        case 1: return "warning"; // 审核中
        case 2: return "success"; // 通过
        case 3: return "danger";  // 驳回
        default: return "";
      }
    },
    // 获取列表数据
    async getList(loadMore = false) {
      if (!loadMore) {
        this.observationList = []
        this.queryParams.pageNum = 1
        this.hasMore = true
      }
      this.loading = !loadMore
      this.loadingMore = loadMore
      try {
        const res = await listRecord(this.queryParams)
        const newList = res.rows.map(item => {
          if (item.images) {
            const images = JSON.parse(item.images)
            item.coverImage = images.length > 0 ? images[0] : null
          }
          return item
        })
        this.observationList = [...this.observationList, ...newList]
        this.total = res.total
        this.hasMore = this.observationList.length < this.total
      } catch (e) {
        this.$message.error('数据加载失败')
      } finally {
        this.loading = false
        this.loadingMore = false
      }
    },
    // 搜索
    handleQuery() {
      this.getList(false)
    },
    // 新增
    handleAdd() {
      this.$router.push('/user/observation/upload')
    },
    // 查看详情
    handleView(row) {
      this.$router.push('/user/observation/detail/' + row.recordId)
    },
    // 编辑
    handleEdit(row) {
      this.$router.push('/user/observation/upload/' + row.recordId)
    },
    // 提交审核
    handleSubmitReview(row) {
      this.$confirm('提交审核后将无法修改，是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => submitForReview(row.recordId)).then(() => {
        this.$message.success('提交审核成功')
        this.getList()
      })
    },
    // 分享
    handleShare(row) {
      this.shareForm = {
        recordId: row.recordId,
        topicType: 'observation',
        title: row.title,
        content: row.description ? row.description.substring(0, 200) : ''
      }
      this.shareDialogVisible = true
    },
    // 确认分享
    confirmShare() {
      this.$refs.shareForm.validate(valid => {
        if (valid) {
          this.shareLoading = true
          shareRecordToCommunity(this.shareForm.recordId).then(() => {
            this.$message.success('分享成功')
            this.shareDialogVisible = false
            this.getList()
          }).catch(() => {
            this.shareLoading = false
          })
        }
      })
    },
    // 删除
    handleDelete(row) {
      this.$confirm('是否确认删除该观察记录?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => delRecord(row.recordId)).then(() => {
        this.getList()
        this.$message.success('删除成功')
      })
    },
    // 绑定滚动加载
    bindScroll() {
      window.addEventListener('scroll', this.handleScroll)
    },
    // 滚动加载更多
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
.observation-list-container {
  padding: 20px 30px;
  background: #f5f5f5;
  min-height: 130vh;
  position: relative;
}

// 视图切换按钮
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

// 页面标题
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

// 搜索框样式
.search-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  max-width: 1200px; // 加宽搜索框容器
  margin-left: auto;
  margin-right: auto;

  .search-input {
    flex: 1;
    height: 50px; // 加高搜索框
    font-size: 16px;
    border-radius: 6px;

    &:focus {
      border-color: #409eff;
      box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
    }
  }

  .search-btn {
    height: 40px;
    margin-bottom: 10px;
    padding: 0 25px;
    font-size: 16px;
    border-radius: 6px;
  }

  .add-btn {
    height: 40px;
    margin-bottom: 10px;
    padding: 0 25px;
    font-size: 16px;
    border-radius: 6px;
    white-space: nowrap;
  }
}

// 内容区域
.content-area {
  margin-bottom: 20px;

  // 网格视图
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

      .card-info {
        padding: 14px;

        // 新增标题样式
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
          display: block;
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

        .card-actions {
          margin-top: 10px;
          display: flex;
          gap: 6px;
        }
      }
    }
  }

  // 列表视图样式
  .list-header {
    display: flex;
    background: #e9e9e9;
    border-bottom: 1px solid #ddd;
    font-weight: 500;
    font-size: 14px;
    color: #333;

    .header-col {
      padding: 8px 10px;
      display: flex;
      align-items: center;

      &.col-1 {
        flex: 1;
        min-width: 150px;
      }

      &.col-2 {
        flex: 1;
        min-width: 150px;
      }

      &.col-3 {
        flex: 1;
        min-width: 150px;
      }

      &.col-4 {
        flex: 1;
        min-width: 150px;
      }

      &.col-5 {
        flex: 1;
        min-width: 150px;
      }
    }
  }

  .list-content {
    border: 1px solid #ddd;
    border-top: none;

    .list-item {
      display: flex;
      align-items: center;
      background: #ffffff;
      border-bottom: 1px solid #e5e5e5;
      cursor: pointer;
      min-height: 90px;

      &.gray-row {
        background: #fafafa;
      }

      &:last-child {
        border-bottom: none;
      }

      .list-col {
        padding: 8px 10px;
        display: flex;
        align-items: center;

        &.col-1 {
          flex: 1;
          min-width: 150px;
        }

        &.col-2 {
          flex: 1;
          min-width: 150px;
        }

        &.col-3 {
          flex: 1;
          min-width: 150px;
        }

        &.col-4 {
          flex: 1;
          min-width: 150px;
        }

        &.col-5 {
          flex: 1;
          min-width: 150px;
          display: flex;
          gap: 4px;
          justify-content: flex-start;
        }
      }

      // 媒体列图片样式
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

        .list-image-placeholder {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          color: #ccc;
        }
      }

      .species-name, .obs-location {
        font-size: 14px;
        color: #333;
        line-height: 1.4;
      }

      .obs-date {
        font-size: 14px;
        color: #333;
        line-height: 1.4;
        white-space: normal;
      }
    }
  }

  // 响应式适配
  @media (max-width: 1200px) {
    .grid-view {
      grid-template-columns: repeat(3, 1fr) !important;
    }

    .list-header, .list-item {
      .header-col, .list-col {
        &.col-1, &.col-2, &.col-3, &.col-4, &.col-5 {
          min-width: 120px;
        }
      }
    }
  }

  @media (max-width: 992px) {
    .grid-view {
      grid-template-columns: repeat(2, 1fr) !important;
    }

    .search-wrapper {
      flex-wrap: wrap;

      .add-btn {
        margin-top: 10px;
        flex: 1;
      }
    }

    .list-header, .list-item {
      .header-col, .list-col {
        &.col-1, &.col-2, &.col-3, &.col-4, &.col-5 {
          min-width: 100px;
          padding: 6px 8px;
        }
      }
    }
  }

  @media (max-width: 768px) {
    .observation-list-container {
      padding: 15px 10px;
    }

    .grid-view {
      grid-template-columns: 1fr !important;
    }

    .view-switch-top {
      top: 15px;
      left: 10px;
    }

    // 移动端列表适配
    .list-header, .list-item {
      flex-direction: column;
      align-items: stretch;
      min-height: auto;

      .header-col, .list-col {
        padding: 8px;
        border-bottom: 1px solid #eee;
        margin-bottom: 5px;

        &.col-1, &.col-2, &.col-3, &.col-4, &.col-5 {
          min-width: auto;
          flex: none;
          width: 100%;
          display: flex;
          justify-content: space-between;
          align-items: center;

          .list-image {
            width: 60px !important;
            height: 60px !important;
            margin: 0;
          }

          .species-name, .obs-location, .obs-date {
            flex: 1;
            margin-left: 10px;
            text-align: left;
          }

          .el-button {
            margin-left: 10px;
          }
        }
      }
    }

    // 移动端操作列特殊处理
    .list-col.col-5 {
      display: flex;
      flex-wrap: wrap;
      gap: 5px;
      justify-content: center;
    }
  }

  // 空状态样式
  .empty-state {
    padding: 60px 0;
    text-align: center;
  }

  // 加载更多样式
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

// 响应式适配
@media (max-width: 1200px) {
  .grid-view {
    grid-template-columns: repeat(3, 1fr) !important;
  }

  .list-header, .list-item {
    .header-col, .list-col {
      &.col-1, &.col-2, &.col-3, &.col-4, &.col-5 {
        min-width: 120px;
      }
    }
  }
}

@media (max-width: 992px) {
  .grid-view {
    grid-template-columns: repeat(2, 1fr) !important;
  }

  .search-wrapper {
    flex-wrap: wrap;

    .add-btn {
      margin-top: 10px;
      flex: 1;
    }
  }

  .list-header, .list-item {
    .header-col, .list-col {
      &.col-1, &.col-2, &.col-3, &.col-4, &.col-5 {
        min-width: 100px;
      }
    }
  }
}

@media (max-width: 768px) {
  .observation-list-container {
    padding: 15px 10px;
  }

  .grid-view {
    grid-template-columns: 1fr !important;
  }

  .view-switch-top {
    top: 15px;
    left: 10px;
  }

  // 移动端列表适配
  .list-header, .list-item {
    flex-direction: column;
    align-items: stretch;
    min-height: auto;

    .header-col, .list-col {
      padding: 8px;
      border-bottom: 1px solid #eee;
      margin-bottom: 5px;

      &.col-1, &.col-2, &.col-3, &.col-4, &.col-5 {
        min-width: auto;
        flex: none;
        width: 100%;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .list-image {
          width: 60px !important;
          height: 60px !important;
          margin: 0;
        }

        .species-name, .obs-location, .obs-date {
          flex: 1;
          margin-left: 10px;
          text-align: left;
        }

        .el-button {
          margin-left: 10px;
        }
      }
    }
  }

  // 移动端操作列特殊处理
  .list-col.col-5 {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
    justify-content: center;
  }
}
</style>
