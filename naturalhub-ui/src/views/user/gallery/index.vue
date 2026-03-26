<template>
  <div class="gallery-page">
    <div class="gallery-header">
      <div class="gallery-title-row">
        <div class="gallery-title-text">
          <h2>我的3D展馆</h2>
          <p>管理哪些3D模型公开展示在个人中心</p>
        </div>
        <el-button type="primary" icon="el-icon-magic-stick" @click="$router.push('/user/mark3D')">
          去创建3D模型
        </el-button>
      </div>

      <div class="gallery-stats">
        <div class="stat-item">
          <span class="stat-num">{{ totalTasks }}</span>
          <span class="stat-label">已完成模型</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-num public">{{ publicCount }}</span>
          <span class="stat-label">公开展示中</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-num">{{ totalTasks - publicCount }}</span>
          <span class="stat-label">未公开</span>
        </div>
      </div>
    </div>

    <div v-loading="loading" class="gallery-body">
      <div v-if="taskList.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">🗿</div>
        <p class="empty-text">还没有完成的3D模型</p>
        <p class="empty-sub">前往「3D标本库」上传图片，AI将为你生成交互式3D模型</p>
      </div>

      <div v-else class="model-grid">
        <div
          v-for="item in taskList"
          :key="item.id"
          class="model-card"
          :class="{ 'is-public': item.isPublic === 1 }"
        >
          <div class="card-thumb" @click="previewModel(item)">
            <img
              v-if="item.thumbnailQiniu || item.thumbnailUrl"
              :src="item.thumbnailQiniu || item.thumbnailUrl"
              class="thumb-img"
            />
            <div v-else class="thumb-placeholder">
              <i class="el-icon-box"></i>
            </div>
            <div class="thumb-overlay">
              <i class="el-icon-view"></i>
              <span>预览模型</span>
            </div>
          </div>

          <div class="card-body">
            <div class="card-name-row">
                <span class="card-name" :title="item.taskName || ('模型 #' + item.id)">
                  {{ item.taskName || ('模型 #' + item.id) }}
                </span>
              <el-button type="text" size="mini" icon="el-icon-edit" @click.stop="startRename(item)"/>
            </div>
            <div class="card-time">{{ formatTime(item.createTime) }}</div>
          </div>

          <div class="card-footer">
            <div class="public-badge" :class="item.isPublic === 1 ? 'badge-on' : 'badge-off'">
              <i :class="item.isPublic === 1 ? 'el-icon-unlock' : 'el-icon-lock'"></i>
              {{ item.isPublic === 1 ? '公开展示中' : '未公开' }}
            </div>
            <el-switch
              :value="item.isPublic === 1"
              active-color="#43cea2"
              inactive-color="#dcdfe6"
              :loading="item._toggling"
              @change="(val) => togglePublic(item, val)"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <el-dialog
      :visible.sync="previewVisible"
      :title="previewTask ? (previewTask.taskName || '模型 #' + previewTask.id) : '3D预览'"
      width="780px"
      append-to-body
      custom-class="model-preview-dialog"
      @closed="onPreviewClosed"
    >
      <div v-if="previewTask" class="preview-dialog-body">
        <div class="preview-viewer-wrap" ref="previewViewerWrap">
          <model-viewer
            ref="dialogViewer"
            :src="previewTask.modelUrlGlbQiniu || previewTask.modelUrlGlb"
            alt="3D模型预览"
            camera-controls
            shadow-intensity="1"
            background-color="#e0e0e0"
            enable-pan
            auto-rotate
            class="dialog-model-viewer"
            @load="modelLoading = false"
            @error="modelLoading = false"
          />
          <div v-if="modelLoading" class="preview-loading">
            <i class="el-icon-loading"></i>
            <span>模型加载中...</span>
          </div>
        </div>
        <div class="preview-actions">
          <el-button type="primary" icon="el-icon-full-screen" @click="enterFullscreen">全屏查看</el-button>
          <el-button icon="el-icon-download" @click="downloadModel(previewTask)">下载 GLB</el-button>
        </div>
        <div class="preview-meta">
          <span><i class="el-icon-time"></i> {{ formatTime(previewTask.createTime) }}</span>
          <span v-if="previewTask.isPublic === 1" class="meta-public">
            <i class="el-icon-unlock"></i> 公开展示中
          </span>
          <span v-else class="meta-private">
            <i class="el-icon-lock"></i> 未公开
          </span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {listMyMark3dTasks, toggleMark3dPublic} from '@/api/user/mark3d'

export default {
  name: 'My3DGallery',
  data() {
    return {
      loading: false,
      taskList: [],
      previewVisible: false,
      previewTask: null,
      modelLoading: false,
      renameVisible: false,
      renameTask: null,
      renameInput: ''
    }
  },
  computed: {
    totalTasks() {
      return this.taskList.length
    },
    publicCount() {
      return this.taskList.filter(t => t.isPublic === 1).length
    }
  },
  mounted() {
    this.loadModelViewer()
    this.loadTasks()
  },
  methods: {
    loadModelViewer() {
      if (typeof customElements !== 'undefined' && !customElements.get('model-viewer')) {
        const script = document.createElement('script')
        script.type = 'module'
        script.src = 'https://unpkg.com/@google/model-viewer/dist/model-viewer.min.js'
        document.head.appendChild(script)
      }
    },
    loadTasks() {
      this.loading = true
      listMyMark3dTasks({taskStatus: '3', pageNum: 1, pageSize: 100})
        .then(res => {
          this.taskList = (res.rows || []).map(t => ({...t, _toggling: false}))
        })
        .finally(() => {
          this.loading = false
        })
    },
    togglePublic(item, val) {
      item._toggling = true
      const isPublic = val ? 1 : 0
      toggleMark3dPublic(item.id, isPublic)
        .then(res => {
          if (res.code === 200) {
            item.isPublic = isPublic
            this.$message.success(isPublic === 1 ? '已公开展示' : '已取消公开')
          } else {
            this.$message.error(res.msg || '操作失败')
          }
        })
        .catch(() => this.$message.error('操作失败'))
        .finally(() => {
          item._toggling = false
        })
    },
    previewModel(item) {
      this.previewTask = item
      this.modelLoading = true
      this.previewVisible = true
    },
    onPreviewClosed() {
      this.previewTask = null
      this.modelLoading = false
    },
    enterFullscreen() {
      const el = this.$refs.previewViewerWrap
      if (!el) return
      if (el.requestFullscreen) el.requestFullscreen()
      else if (el.webkitRequestFullscreen) el.webkitRequestFullscreen()
    },
    downloadModel(item) {
      const url = item.modelUrlGlbQiniu || item.modelUrlGlb
      if (!url) return
      const a = document.createElement('a')
      a.href = url
      a.download = (item.taskName || 'model') + '.glb'
      a.target = '_blank'
      a.click()
    },
    startRename(item) {
      this.renameTask = item
      this.renameInput = item.taskName || ''
      this.renameVisible = true
      this.$nextTick(() => {
        this.$prompt('请输入新的模型名称', '重命名', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputValue: this.renameInput,
          inputValidator: (v) => v && v.trim() ? true : '名称不能为空'
        }).then(({ value }) => {
          import('@/api/user/mark3d').then(({ renameMark3dTask }) => {
            renameMark3dTask(item.id, value.trim()).then(res => {
              if (res.code === 200) {
                item.taskName = value.trim()
                this.$message.success('重命名成功')
              } else {
                this.$message.error(res.msg || '重命名失败')
              }
            })
          })
        }).catch(() => {})
      })
    },
    formatTime(t) {
      if (!t) return ''
      return new Date(t).toLocaleString('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.gallery-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 4px;
}

.gallery-header {
  margin-bottom: 28px;
}

.gallery-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  .gallery-title-text {
    h2 {
      margin: 0 0 4px;
      font-size: 24px;
      font-weight: 700;
      color: #1a1a2e;
    }

    p {
      margin: 0;
      font-size: 14px;
      color: #909399;
    }
  }
}

.gallery-stats {
  display: flex;
  align-items: center;
  gap: 0;
  background: #fff;
  border-radius: 12px;
  padding: 16px 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  width: fit-content;

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 0 28px;

    .stat-num {
      font-size: 28px;
      font-weight: 700;
      color: #303133;
      line-height: 1;
      margin-bottom: 4px;

      &.public {
        color: #43cea2;
      }
    }

    .stat-label {
      font-size: 13px;
      color: #909399;
    }
  }

  .stat-divider {
    width: 1px;
    height: 36px;
    background: #EBEEF5;
  }
}

.empty-state {
  text-align: center;
  padding: 80px 20px;

  .empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px;
  }

  .empty-sub {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.model-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.model-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  border: 2px solid transparent;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    transform: translateY(-3px);
  }

  &.is-public {
    border-color: rgba(67, 206, 162, 0.4);
  }

  .card-thumb {
    position: relative;
    width: 100%;
    padding-top: 75%;
    cursor: pointer;
    overflow: hidden;
    background: #f5f7fa;

    .thumb-img {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .thumb-placeholder {
      position: absolute;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #e0e0e0;

      i {
        font-size: 40px;
        color: #4a5568;
      }
    }

    .thumb-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s ease;
      color: #fff;
      gap: 6px;

      i {
        font-size: 28px;
      }

      span {
        font-size: 13px;
      }
    }

    &:hover .thumb-overlay {
      opacity: 1;
    }

    &:hover .thumb-img {
      transform: scale(1.05);
    }
  }

  .card-body {
    padding: 12px 14px 8px;

    .card-name {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin-bottom: 4px;
    }

    .card-time {
      font-size: 12px;
      color: #C0C4CC;
    }
  }

  .card-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 14px 12px;
    border-top: 1px solid #f5f5f5;

    .public-badge {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      font-weight: 500;

      &.badge-on {
        color: #43cea2;
      }

      &.badge-off {
        color: #C0C4CC;
      }

      i {
        font-size: 13px;
      }
    }
  }
}

.preview-dialog-body {
  .preview-viewer-wrap {
    position: relative;
    background: #e0e0e0;
    border-radius: 8px;
    overflow: hidden;
    margin-bottom: 14px;
  }

  .dialog-model-viewer {
    width: 100%;
    height: 420px;
    display: block;
  }

  .preview-loading {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: rgba(26, 31, 46, 0.85);
    color: #fff;
    gap: 10px;

    i {
      font-size: 36px;
      animation: spin 1s linear infinite;
    }

    span {
      font-size: 14px;
    }
  }

  .preview-actions {
    display: flex;
    gap: 12px;
    justify-content: center;
    margin-bottom: 12px;
  }

  .preview-meta {
    display: flex;
    gap: 20px;
    font-size: 13px;
    color: #909399;
    justify-content: center;

    .meta-public {
      color: #43cea2;
    }

    .meta-private {
      color: #C0C4CC;
    }
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

:-webkit-full-screen .preview-viewer-wrap {
  background: #e0e0e0;
}

:fullscreen .preview-viewer-wrap {
  background: #e0e0e0;
}

:-webkit-full-screen .dialog-model-viewer {
  width: 100% !important;
  height: 100% !important;
}

:fullscreen .dialog-model-viewer {
  width: 100% !important;
  height: 100% !important;
}

::v-deep .model-preview-dialog {
  .el-dialog__body {
    padding: 16px 20px 20px;
  }
}

@media (max-width: 768px) {
  .model-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .gallery-stats {
    padding: 12px 16px;
  }

  .stat-item {
    padding: 0 14px;
  }
}
</style>
