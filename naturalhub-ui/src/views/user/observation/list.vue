<template>
  <div class="observation-list-container">
    <div class="view-switch-top">
      <el-button :class="['view-btn', { active: viewType === 'grid' }]" @click="viewType = 'grid'"><i
        class="el-icon-menu"></i> 网格
      </el-button>
      <el-button :class="['view-btn', { active: viewType === 'list' }]" @click="viewType = 'list'"><i
        class="el-icon-tickets"></i> 列表
      </el-button>
    </div>

    <div class="page-header">
      <h1 class="page-title">我的观察记录</h1>
      <p class="page-subtitle">记录每一次与自然的相遇</p>
    </div>

    <div class="search-wrapper">
      <el-input v-model="queryParams.title" placeholder="搜索观察记录..." prefix-icon="el-icon-search" clearable
                @keyup.enter.native="handleQuery" class="search-input"/>
      <el-button type="primary" @click="handleQuery" class="search-btn">搜索</el-button>
      <el-button type="success" icon="el-icon-plus" @click="handleAdd" class="add-btn">新增观察记录</el-button>
    </div>

    <div v-loading="loading" class="content-area">
      <div v-if="viewType === 'grid' && observationList.length > 0" class="grid-view">
        <div v-for="item in observationList" :key="item.recordId" class="grid-card" @click="handleView(item)">
          <div class="card-image-wrapper">
            <img v-if="item.coverImage" :src="item.coverImage" class="card-image" alt="observation">
            <div v-else class="card-image-placeholder"><i class="el-icon-picture-outline"></i></div>
            <div class="card-tag">
              <el-tag :type="getAuditStatusType(item.auditStatus)" size="small" effect="dark">
                {{ getAuditStatusText(item.auditStatus) }}
              </el-tag>
            </div>
          </div>
          <div class="card-info">
            <div class="card-title">{{ item.title }}</div>
            <div class="info-item"><span class="label">物种：</span><span class="info-value">{{
                item.speciesName
              }}</span></div>
            <div class="info-item"><span class="label">地点：</span><span class="info-value">{{ item.location }}</span>
            </div>
            <div class="info-item"><span class="label">时间：</span><span
              class="info-value">{{ formatGridDateTime(item.observationTime) }}</span></div>
            <div class="card-actions">
              <el-button size="mini" type="text" icon="el-icon-view" @click.stop="handleView(item)">查看</el-button>
              <el-button v-if="item.auditStatus === 0 || item.auditStatus === 3" size="mini" type="text"
                         icon="el-icon-edit" @click.stop="handleEdit(item)">编辑
              </el-button>
              <el-button v-if="item.auditStatus === 2 && item.isShared !== 1" size="mini" type="text"
                         icon="el-icon-share" @click.stop="handleShare(item)">分享
              </el-button>
              <el-button
                v-if="item.isShared === 1"
                size="mini"
                type="text"
                icon="el-icon-share"
                disabled
                style="cursor: not-allowed; color: #52c41a"
              >
                已分享
              </el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(item)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="viewType === 'list'">
        <div class="list-header">
          <div class="header-col col-1">媒体</div>
          <div class="header-col col-2">名称</div>
          <div class="header-col col-3">观察日期</div>
          <div class="header-col col-4">地方</div>
          <div class="header-col col-5">操作</div>
        </div>

        <div v-if="observationList.length > 0" class="list-content">
          <div v-for="(item, index) in observationList" :key="item.recordId" class="list-item"
               :class="{ 'gray-row': index % 2 === 1 }" @click="handleView(item)">
            <div class="list-col col-1">
              <div class="list-image">
                <img v-if="item.coverImage" :src="item.coverImage" alt="observation">
                <div v-else class="list-image-placeholder"><i class="el-icon-picture-outline"></i></div>
              </div>
            </div>
            <div class="list-col col-2">
              <div class="species-name">{{ item.speciesName }}</div>
            </div>
            <div class="list-col col-3">
              <div class="obs-date">{{
                  formatDatePart(item.observationTime)
                }}<br/>{{ formatTimePart(item.observationTime) }}
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
              <el-button v-if="item.auditStatus === 2 && item.isShared !== 1" size="mini" type="text"
                         icon="el-icon-share" @click.stop="handleShare(item)">分享
              </el-button>
              <el-button
                v-if="item.isShared === 1"
                size="mini"
                type="text"
                icon="el-icon-share"
                disabled
                style="cursor: not-allowed; color: #52c41a"
              >
                已分享
              </el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click.stop="handleDelete(item)">删除</el-button>
            </div>
          </div>
        </div>

        <el-empty v-if="observationList.length === 0" description="暂无观察记录" class="empty-state">
          <el-button type="primary" @click="handleAdd">立即创建</el-button>
        </el-empty>
      </div>

      <el-empty v-if="viewType === 'grid' && observationList.length === 0" description="暂无观察记录"
                class="empty-state">
        <el-button type="primary" @click="handleAdd">立即创建</el-button>
      </el-empty>

      <div v-if="hasMore" class="load-more" :class="{loading: loadingMore}">
        <span v-if="!loadingMore">下拉加载更多</span>
        <span v-else>加载中...</span>
      </div>
    </div>

    <!-- 分享弹窗 -->
    <share-dialog
      :visible.sync="shareDialogVisible"
      :source-type="1"
      :init-title="shareForm.title"
      :init-content="shareForm.content"
      :loading="shareLoading"
      @confirm="confirmShare"
    />
  </div>
</template>

<script>
import {listRecord, delRecord, shareRecordToCommunity} from '@/api/user/record'
import ShareDialog from '@/views/user/components/ShareDialog'

export default {
  name: 'ObservationList',
  data() {
    return {
      viewType: 'grid',
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
    }
  },
  components: {ShareDialog},
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
      const month = months[date.getMonth()]
      const day = date.getDate()
      const year = date.getFullYear()
      return `${month} ${day}, ${year}`
    },
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
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
    },
    getAuditStatusText(status) {
      const item = this.auditStatusOptions.find(d => d.dictValue == status)
      return item ? item.dictLabel : ""
    },
    getAuditStatusType(status) {
      switch (status) {
        case 0:
          return ""
        case 1:
          return "warning"
        case 2:
          return "success"
        case 3:
          return "danger"
        default:
          return ""
      }
    },
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
    handleQuery() {
      this.getList(false)
    },
    handleAdd() {
      this.$router.push('/user/observation/upload')
    },
    handleView(row) {
      this.$router.push('/user/observation/detail/' + row.recordId)
    },
    handleEdit(row) {
      this.$router.push('/user/observation/upload/' + row.recordId)
    },
    handleShare(row) {
      this.shareForm = {
        recordId: row.recordId,
        topicType: 'observation',
        title: row.title,
        content: row.description ? row.description.substring(0, 200) : ''
      }
      this.shareDialogVisible = true
    },
    confirmShare({title, content}) {
      this.shareLoading = true
      shareRecordToCommunity(this.shareForm.recordId, {title, content}).then(() => {
        this.$message.success('分享成功')
        this.shareDialogVisible = false
        this.$router.push('/user/community')
      }).finally(() => {
        this.shareLoading = false
      })
    },
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
.observation-list-container {
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

    // 修复搜索图标垂直居中
    ::v-deep .el-input__prefix {
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

        .card-tag {
          position: absolute;
          top: 6px;
          right: 6px;
          z-index: 1;
        }
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
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;

          .label {
            font-weight: 400;
            color: #888;
          }

          .info-value {
            display: inline-block;
            max-width: calc(100% - 40px);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            vertical-align: middle;
          }
        }

        .card-actions {
          margin-top: 10px;
          display: flex;
          gap: 6px;
          flex-wrap: wrap;
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
      display: flex;
      align-items: center;
      flex: 1;
      min-width: 150px;
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
        flex: 1;
        min-width: 150px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;

        &.col-5 {
          gap: 4px;
          flex-wrap: wrap;
        }
      }

      .list-image {
        width: 80px;
        height: 80px;
        border-radius: 4px;
        overflow: hidden;
        background: #f0f0f0;
        flex-shrink: 0;

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
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .obs-date {
        font-size: 14px;
        color: #333;
        line-height: 1.4;
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

@media (max-width: 1200px) {
  .grid-view {
    grid-template-columns: repeat(3, 1fr) !important;
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
}
</style>
