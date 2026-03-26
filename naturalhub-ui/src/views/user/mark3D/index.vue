<template>
  <div class="mark3d-container">
    <div class="page-header">
      <div class="header-left">
        <span class="header-icon"><svg t="1774345536413" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="18408" width="64" height="64"><path d="M512 0l-494.93504 221.86496v574.57664L512 1024l494.93504-221.86496V221.86496L512 0z m233.24672 796.44672l-91.02336 39.81824v-329.95328l85.33504-39.81824c96.71168-39.82336 159.28832-17.06496 159.28832 96.71168 0 108.0832-62.58176 193.41312-153.6 233.2416z m136.53504-546.13504L512 415.28832l-369.77664-164.98176L512 79.64672l369.78176 170.66496z m-790.7584 51.2L512 489.24672v449.41824l-420.97664-187.72992v-449.42336z" fill="#444B5A" p-id="18409"></path><path d="M739.55328 517.68832l-17.06496 5.68832v227.55328l17.06496-5.68832c56.88832-22.75328 96.71168-73.95328 96.71168-153.6-5.6832-85.32992-39.81824-96.70656-96.71168-73.95328z m-392.52992 119.46496c34.13504 0 56.88832-22.75328 56.88832-56.88832 0-56.8832-39.81824-102.4-108.08832-125.15328-39.82336-17.06496-68.26496-11.37664-96.71168 0L233.24672 512c17.06496-11.37664 39.82336-11.37664 62.57664-5.68832 28.44672 11.38176 45.51168 34.13504 45.51168 56.88832 0 28.44672-17.06496 39.81824-79.6416 17.06496v45.5168c68.26496 28.4416 91.01824 56.8832 91.01824 85.32992s-22.75328 34.13504-56.88832 22.75328c-28.44672-11.37664-51.2-34.12992-73.95328-62.57664l-28.44672 28.44672c22.75328 34.12992 56.88832 68.26496 108.08832 85.32992 62.58176 22.75328 113.78176 11.38176 113.78176-51.2-5.69344-34.12992-34.14016-73.95328-68.27008-96.71168z" fill="#444B5A" p-id="18410"></path></svg></span>
        <div>
          <h2>3D标本库</h2>
          <p>上传一张图片，AI智能生成交互式3D模型</p>
        </div>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧历史任务列表 -->
      <div class="history-panel">
        <el-card shadow="hover" class="history-card">
          <div slot="header" class="panel-header">
            <span><i class="el-icon-time"></i> 我的任务</span>
            <el-button type="text" icon="el-icon-refresh" :loading="listLoading" @click="loadTaskList">刷新</el-button>
          </div>

          <div v-if="taskList.length === 0 && !listLoading" class="empty-hint">
            <i class="el-icon-box"></i>
            <p>暂无3D任务</p>
          </div>

          <div class="task-list" v-else>
            <div
              v-for="item in taskList"
              :key="item.id"
              class="task-item"
              :class="{ active: selectedTask && selectedTask.id === item.id }"
              @click="selectTask(item)"
            >
              <div class="task-thumb">
                <img v-if="item.frontImageUrl" :src="item.frontImageUrl" />
                <img v-else src="https://via.placeholder.com/52" />
              </div>
              <div class="task-info">
                <div class="task-name">{{ item.taskName || ('任务 #' + item.id) }}</div>
                <el-tag :type="statusTagType(item.taskStatus)" size="mini" class="status-tag">
                  {{ statusLabel(item.taskStatus) }}
                </el-tag>
                <div class="task-time">{{ formatTime(item.createTime) }}</div>
              </div>
            </div>
          </div>

          <div v-if="taskTotal > taskPageSize" class="list-pagination">
            <el-pagination
              small layout="prev, pager, next"
              :total="taskTotal" :page-size="taskPageSize" :current-page="taskPage"
              @current-change="onPageChange"
            />
          </div>
        </el-card>
      </div>

      <!-- 右侧创建面板 -->
      <div class="create-panel">
        <el-card shadow="hover" class="create-card">
          <div slot="header" class="panel-header">
            <span><i class="el-icon-plus"></i> 创建3D任务</span>
          </div>

          <el-form :model="form" :rules="rules" ref="taskForm" label-position="top" class="task-form">
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="form.taskName" placeholder="为这个3D模型取个名字（可选）" clearable />
            </el-form-item>

            <div class="image-upload-single">
              <div class="slot-label">
                <i class="el-icon-picture-outline"></i> 上传图片
                <span class="required-star">*</span>
              </div>
              <el-upload
                class="slot-uploader"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :before-upload="beforeUpload"
                :on-success="(r) => onUploadSuccess(r, 'frontImageUrl')"
                :on-error="onUploadError"
                accept="image/*"
              >
                <div class="upload-box single" :class="{ filled: form.frontImageUrl }">
                  <img v-if="form.frontImageUrl" :src="form.frontImageUrl" class="preview-img" />
                  <div v-else class="upload-placeholder">
                    <i class="el-icon-camera"></i>
                    <span>点击上传</span>
                  </div>
                  <div v-if="form.frontImageUrl" class="replace-mask">替换</div>
                </div>
              </el-upload>
            </div>

            <div class="form-tips">
              <i class="el-icon-info"></i>
              上传一张清晰图片，建议使用纯色背景，图片质量越高3D效果越好。
            </div>

            <div class="form-actions">
              <el-button
                type="primary" size="medium"
                :loading="submitting"
                :disabled="!allImagesUploaded"
                @click="submitTask"
                icon="el-icon-magic-stick"
              >
                {{ submitting ? '提交中...' : '开始生成3D模型' }}
              </el-button>
              <el-button size="medium" @click="resetForm" icon="el-icon-refresh-left">重置</el-button>
            </div>
          </el-form>
        </el-card>
      </div>
    </div>

    <!-- 详情面板 -->
    <div class="detail-panel" v-if="selectedTask">
      <el-card shadow="hover" class="detail-card">
        <div slot="header" class="panel-header">
          <span>
            <i class="el-icon-view"></i>
            {{ selectedTask.taskName || ('任务 #' + selectedTask.id) }}&nbsp;
            <el-tag :type="statusTagType(selectedTask.taskStatus)" size="small">{{ statusLabel(selectedTask.taskStatus) }}</el-tag>
          </span>
          <div class="header-actions">
            <el-button
              v-if="selectedTask.taskStatus !== '3' && selectedTask.taskStatus !== '4'"
              size="small" type="primary" plain icon="el-icon-refresh"
              :loading="syncing" @click="syncStatus"
            >刷新状态</el-button>
            <el-button size="small" type="danger" plain icon="el-icon-delete" @click="deleteTask(selectedTask.id)">删除任务</el-button>
          </div>
        </div>

        <div v-if="selectedTask.taskStatus === '1' || selectedTask.taskStatus === '2'" class="status-block pending">
          <div class="spin-icon"><i class="el-icon-loading"></i></div>
          <p class="status-title">3D模型生成中，请稍候…</p>
          <el-progress :percentage="selectedTask.progress || 0" :stroke-width="10" style="max-width:400px;margin:0 auto 10px" />
          <p class="status-sub">生成通常需要 1~5 分钟，完成后点击「刷新状态」查看结果</p>
        </div>

        <div v-else-if="selectedTask.taskStatus === '4'" class="status-block failed">
          <i class="el-icon-circle-close"></i>
          <p class="status-title">生成失败</p>
          <p class="status-sub">{{ selectedTask.errorMessage || '未知错误，请重新提交' }}</p>
        </div>

        <div v-else-if="selectedTask.taskStatus === '3'" class="result-block">
          <div class="viewer-wrap">
            <div class="viewer-area" ref="viewerContainer">
              <!-- 模型加载状态 -->
              <div v-if="modelLoading" class="model-loading">
                <i class="el-icon-loading"></i>
                <span>模型加载中...</span>
              </div>

              <!-- 模型加载错误 -->
              <div v-if="modelLoadError" class="model-error">
                <i class="el-icon-circle-close"></i>
                <p>模型加载失败</p>
                <el-button type="text" @click="retryLoadModel">重试</el-button>
              </div>

              <!-- 3D 模型查看器 -->
              <model-viewer
                ref="modelViewer"
                v-show="!modelLoading && !modelLoadError"
                :src="selectedTask.modelUrlGlbQiniu || selectedTask.modelUrlGlb"
                alt="3D模型预览"
                camera-controls
                shadow-intensity="1"
                background-color="#e0e0e0"
                enable-pan
                enable-zoom
                touch-action="pan-y"
                auto-rotate
                interaction-prompt="auto"
                class="custom-model-viewer"
                @load="onModelLoad"
                @error="onModelError"
              />

              <!-- 全屏状态提示 -->
              <transition name="fade">
                <div v-if="isFullscreen" class="fullscreen-hint">
                  <i class="el-icon-full-screen"></i>
                  <span>按 ESC 退出全屏</span>
                </div>
              </transition>
            </div>

            <!-- 操作栏 -->
            <div class="model-actions">
              <el-button
                type="success"
                icon="el-icon-download"
                @click="downloadModel(selectedTask.modelUrlGlbQiniu || selectedTask.modelUrlGlb, 'model.glb')"
              >
                下载 GLB
              </el-button>

              <el-button
                :type="isFullscreen ? 'warning' : 'primary'"
                :icon="isFullscreen ? 'el-icon-close' : 'el-icon-full-screen'"
                @click="toggleFullscreen"
              >
                {{ isFullscreen ? '退出全屏' : '全屏查看' }}
              </el-button>
            </div>
          </div>

          <div class="reference-images">
            <h4>参考图片</h4>
            <div class="ref-grid">
              <div class="ref-item" v-if="selectedTask.frontImageUrl">
                <img :src="selectedTask.frontImageUrl" alt="正面" @click="previewImage(selectedTask.frontImageUrl)" />
                <span>正面</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog :visible.sync="previewVisible" width="50%" append-to-body center>
      <img :src="previewUrl" style="width:100%;height:auto;" />
    </el-dialog>
  </div>
</template>

<script>
import { listMyMark3dTasks, createMark3dTask, syncMark3dTaskStatus, deleteMark3dTask } from '@/api/user/mark3d'
import { getToken } from '@/utils/auth'

export default {
  name: 'Mark3dIndex',
  data() {
    return {
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      uploadHeaders: { Authorization: 'Bearer ' + getToken() },
      taskList: [],
      listLoading: false,
      taskTotal: 0,
      taskPage: 1,
      taskPageSize: 10,
      selectedTask: null,
      syncing: false,
      form: {
        taskName: '',
        frontImageUrl: ''
      },
      rules: {
        taskName: [{ max: 100, message: '名称不超过 100 字', trigger: 'blur' }]
      },
      submitting: false,
      previewVisible: false,
      previewUrl: '',
      // 新增：模型加载状态
      modelLoading: false,
      modelLoadError: false,
      // 新增：全屏状态
      isFullscreen: false,
      // 新增：全屏事件监听器
      fullscreenListener: null
    }
  },
  computed: {
    allImagesUploaded() {
      return !!this.form.frontImageUrl
    }
  },
  mounted() {
    this.loadModelViewer()
    this.loadTaskList()
    // 添加全屏状态监听
    this.addFullscreenListener()
  },
  beforeDestroy() {
    // 移除全屏监听
    this.removeFullscreenListener()
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
    loadTaskList() {
      this.listLoading = true
      listMyMark3dTasks({pageNum: this.taskPage, pageSize: this.taskPageSize})
        .then(res => {
          this.taskList = res.rows || []
          this.taskTotal = res.total || 0
        })
        .finally(() => {
          this.listLoading = false
        })
    },
    onPageChange(page) {
      this.taskPage = page
      this.loadTaskList()
    },
    selectTask(item) {
      this.selectedTask = {...item}
      // 重置模型加载状态
      this.modelLoading = false
      this.modelLoadError = false
      this.$nextTick(() => {
        const el = document.querySelector('.detail-panel')
        if (el) el.scrollIntoView({behavior: 'smooth', block: 'start'})
      })
    },
    syncStatus() {
      this.syncing = true
      syncMark3dTaskStatus(this.selectedTask.id)
        .then(res => {
          if (res.code === 200) {
            this.selectedTask = res.data
            const idx = this.taskList.findIndex(t => t.id === res.data.id)
            if (idx !== -1) this.$set(this.taskList, idx, res.data)
            this.$message.success('状态已更新')
            // 如果状态变为完成，重置加载状态
            if (res.data.taskStatus === '3') {
              this.modelLoading = false
              this.modelLoadError = false
            }
          } else {
            this.$message.error(res.msg || '同步失败')
          }
        })
        .catch(e => this.$message.error('同步失败：' + e.message))
        .finally(() => {
          this.syncing = false
        })
    },
    beforeUpload(file) {
      const isImg = file.type.startsWith('image/')
      if (!isImg) {
        this.$message.error('只能上传图片')
        return false
      }
      const sizeOk = file.size / 1024 / 1024 < 10
      if (!sizeOk) {
        this.$message.error('图片不超过 10MB')
        return false
      }
      return true
    },
    onUploadSuccess(response, field) {
      if (response.code === 200) {
        this.$set(this.form, field, response.url)
        this.$message.success('上传成功')
      } else {
        this.$message.error(response.msg || '上传失败')
      }
    },
    onUploadError() {
      this.$message.error('上传失败，请重试')
    },
    submitTask() {
      this.$refs.taskForm.validate(valid => {
        if (!valid) return
        if (!this.allImagesUploaded) {
          this.$message.warning('请上传图片')
          return
        }
        this.submitting = true
        createMark3dTask({...this.form})
          .then(res => {
            if (res.code === 200) {
              this.$message.success(res.msg || '任务已提交')
              this.resetForm()
              this.taskPage = 1
              this.loadTaskList()
            } else {
              this.$message.error(res.msg || '提交失败')
            }
          })
          .catch(e => this.$message.error('提交失败：' + e.message))
          .finally(() => {
            this.submitting = false
          })
      })
    },
    resetForm() {
      this.form = {
        taskName: '',
        frontImageUrl: ''
      }
      this.$refs.taskForm && this.$refs.taskForm.resetFields()
    },
    deleteTask(id) {
      this.$confirm('确定删除该 3D 任务吗？', '提示', {type: 'warning'})
        .then(() => {
          deleteMark3dTask(id).then(res => {
            if (res.code === 200) {
              this.$message.success('删除成功')
              this.selectedTask = null
              this.loadTaskList()
            } else {
              this.$message.error(res.msg || '删除失败')
            }
          })
        }).catch(() => {
      })
    },
    downloadModel(url, filename) {
      const a = document.createElement('a')
      a.href = url
      a.download = filename
      a.target = '_blank'
      a.click()
    },
    // 优化：全屏查看方法
    async toggleFullscreen() {
      const container = this.$refs.viewerContainer
      if (!container) return

      try {
        if (!this.isFullscreen) {
          // 进入全屏
          if (container.requestFullscreen) {
            await container.requestFullscreen()
          } else if (container.webkitRequestFullscreen) {
            await container.webkitRequestFullscreen()
          } else if (container.mozRequestFullScreen) {
            await container.mozRequestFullScreen()
          } else if (container.msRequestFullscreen) {
            await container.msRequestFullscreen()
          }
          this.$message.success('已进入全屏模式')
        } else {
          // 退出全屏
          if (document.exitFullscreen) {
            await document.exitFullscreen()
          } else if (document.webkitExitFullscreen) {
            await document.webkitExitFullscreen()
          } else if (document.mozCancelFullScreen) {
            await document.mozCancelFullScreen()
          } else if (document.msExitFullscreen) {
            await document.msExitFullscreen()
          }
        }
      } catch (err) {
        console.error('Fullscreen error:', err)
        this.$message.error('全屏切换失败，请检查浏览器权限')
      }
    },
    // 新增：添加全屏事件监听
    addFullscreenListener() {
      this.fullscreenListener = () => {
        this.isFullscreen = !!(
          document.fullscreenElement ||
          document.webkitFullscreenElement ||
          document.mozFullScreenElement ||
          document.msFullscreenElement
        )
      }
      document.addEventListener('fullscreenchange', this.fullscreenListener)
      document.addEventListener('webkitfullscreenchange', this.fullscreenListener)
      document.addEventListener('mozfullscreenchange', this.fullscreenListener)
      document.addEventListener('MSFullscreenChange', this.fullscreenListener)
    },
    // 新增：移除全屏事件监听
    removeFullscreenListener() {
      if (this.fullscreenListener) {
        document.removeEventListener('fullscreenchange', this.fullscreenListener)
        document.removeEventListener('webkitfullscreenchange', this.fullscreenListener)
        document.removeEventListener('mozfullscreenchange', this.fullscreenListener)
        document.removeEventListener('MSFullscreenChange', this.fullscreenListener)
      }
    },
    // 新增：模型加载完成回调
    onModelLoad() {
      this.modelLoading = false
      this.modelLoadError = false
    },
    // 新增：模型加载错误回调
    onModelError() {
      this.modelLoading = false
      this.modelLoadError = true
      this.$message.error('模型加载失败，请重试')
    },
    // 新增：重试加载模型
    retryLoadModel() {
      this.modelLoadError = false
      this.modelLoading = true
      // 强制重新渲染 model-viewer
      const tempUrl = this.selectedTask.modelUrlGlbQiniu || this.selectedTask.modelUrlGlb
      this.selectedTask.modelUrlGlbQiniu = null
      this.$nextTick(() => {
        this.selectedTask.modelUrlGlbQiniu = tempUrl
      })
    },
    previewImage(url) {
      this.previewUrl = url
      this.previewVisible = true
    },
    statusLabel(status) {
      const map = {'1': '等待中', '2': '生成中', '3': '已完成', '4': '已失败'}
      return map[status] || status
    },
    statusTagType(status) {
      const map = {'1': 'info', '2': 'warning', '3': 'success', '4': 'danger'}
      return map[status] || 'info'
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
.mark3d-container {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
  background-color: #e0e0e0;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: #ffffff;
  border: 1px solid #EBEEF5;
  border-radius: 12px;
  color: #303133;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .header-icon {
      font-size: 48px;
    }

    h2 {
      margin: 0 0 4px;
      font-size: 24px;
      font-weight: 700;
    }

    p {
      margin: 0;
      font-size: 14px;
      opacity: 0.75;
    }
  }
}

.main-content {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  margin-bottom: 20px;
  align-items: stretch;
}

.history-panel {
  display: flex;
}

.history-card {
  flex: 1;
  display: flex;
  flex-direction: column;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .empty-hint {
    text-align: center;
    padding: 40px 0;
    color: #909399;

    i {
      font-size: 40px;
      display: block;
      margin-bottom: 8px;
    }

    p {
      margin: 0;
    }
  }

  .task-list {
    flex: 1;
    max-height: 480px;
    overflow-y: auto;
    padding-right: 4px;

    &::-webkit-scrollbar {
      width: 5px;
    }

    &::-webkit-scrollbar-thumb {
      background: #C0C4CC;
      border-radius: 3px;
    }
  }

  .task-item {
    display: flex;
    gap: 10px;
    padding: 10px;
    margin-bottom: 8px;
    border: 1px solid #EBEEF5;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.25s;

    &:hover {
      background: #f5f7fa;
      border-color: #409EFF;
    }

    &.active {
      background: #ecf5ff;
      border-color: #409EFF;
      box-shadow: 0 0 0 2px rgba(64, 158, 255, .2);
    }

    .task-thumb {
      width: 52px;
      height: 52px;
      border-radius: 6px;
      overflow: hidden;
      background: #f0f2f5;
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .task-info {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .task-name {
        font-size: 13px;
        font-weight: 600;
        color: #303133;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .task-time {
        font-size: 11px;
        color: #909399;
      }
    }
  }

  .list-pagination {
    margin-top: 10px;
    text-align: center;
    border-top: 1px solid #EBEEF5;
    padding-top: 10px;
  }
}

.create-panel {
  display: flex;
}

.create-card {
  flex: 1;
  display: flex;
  flex-direction: column;

  .panel-header {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .task-form {
    flex: 1;
    padding: 4px 0;
    display: flex;
    flex-direction: column;
  }

  .image-upload-single {
    flex: 1;
    margin-bottom: 16px;
    display: flex;
    flex-direction: column;
    width: 100%;
  }

  .slot-label {
    font-size: 13px;
    font-weight: 600;
    color: #606266;
    margin-bottom: 8px;

    .required-star {
      color: #F56C6C;
      margin-left: 2px;
    }
  }

  .slot-uploader {
    width: 100% !important;
    display: block !important;
  }

  .upload-box {
    width: 100% !important;
    min-height: 350px;
    border: 2px dashed #DCDFE6;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.25s;
    box-sizing: border-box;
  }

  .upload-box.single {
    min-height: 350px;
    min-width: 900px;
  }

  .upload-placeholder {
    text-align: center;
    color: #C0C4CC;
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    i {
      font-size: 32px;
      display: block;
      margin-bottom: 8px;
    }

    span {
      font-size: 12px;
    }
  }

  .preview-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .replace-mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    color: #fff;
    font-size: 13px;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.25s;
  }

  .upload-box:hover .replace-mask {
    opacity: 1;
  }

  .upload-box:hover {
    border-color: #409EFF;
  }

  .upload-box.filled {
    border-style: solid;
    border-color: #67C23A;
  }

  .form-tips {
    font-size: 12px;
    color: #909399;
    background: #f4f4f5;
    border-radius: 6px;
    padding: 10px 14px;
    margin-bottom: 20px;

    i {
      margin-right: 4px;
    }
  }

  .form-actions {
    text-align: center;
    margin-top: auto;

    .el-button {
      min-width: 140px;
    }
  }
}

.detail-panel {
  margin-top: 20px;

  .detail-card {
    border-radius: 12px;
  }

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .status-block {
    text-align: center;
    padding: 50px 20px;

    &.pending .spin-icon i {
      font-size: 52px;
      color: #E6A23C;
      animation: rotating 2s linear infinite;
    }

    &.failed i {
      font-size: 52px;
      color: #F56C6C;
    }

    .status-title {
      font-size: 18px;
      font-weight: 600;
      margin: 16px 0 8px;
      color: #303133;
    }

    .status-sub {
      font-size: 13px;
      color: #909399;
      margin: 0;
    }
  }

  .result-block {
    .viewer-wrap {
      .viewer-area {
        margin-bottom: 16px;
        position: relative;
        background: #e0e0e0;
        border-radius: 12px;
        overflow: hidden;
        min-height: 600px;

        // 模型加载状态
        .model-loading {
          position: absolute;
          inset: 0;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          background: rgba(224, 224, 224, 0.9);
          z-index: 10;

          i {
            font-size: 48px;
            color: #409EFF;
            animation: rotating 1.5s linear infinite;
            margin-bottom: 12px;
          }

          span {
            font-size: 14px;
            color: #606266;
          }
        }

        // 模型加载错误
        .model-error {
          position: absolute;
          inset: 0;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          background: rgba(255, 255, 255, 0.95);
          z-index: 10;

          i {
            font-size: 48px;
            color: #F56C6C;
            margin-bottom: 12px;
          }

          p {
            font-size: 14px;
            color: #606266;
            margin-bottom: 12px;
          }
        }

        // 全屏状态提示
        .fullscreen-hint {
          position: absolute;
          top: 20px;
          left: 50%;
          transform: translateX(-50%);
          background: rgba(0, 0, 0, 0.7);
          color: #fff;
          padding: 8px 16px;
          border-radius: 20px;
          font-size: 13px;
          display: flex;
          align-items: center;
          gap: 6px;
          z-index: 100;
          pointer-events: none;

          i {
            font-size: 16px;
          }
        }
      }

      .model-actions {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        justify-content: center;
        padding: 20px 0;
        border-top: 1px solid #EBEEF5;

        .el-button {
          min-width: 120px;
        }
      }
    }

    .reference-images {
      margin-top: 24px;

      h4 {
        margin: 0 0 12px;
        font-size: 14px;
        color: #606266;
      }

      .ref-grid {
        display: grid;
        grid-template-columns: repeat(1, 1fr);
        gap: 12px;
      }

      .ref-item {
        text-align: center;
        max-width: 200px;

        img {
          width: 100%;
          height: 110px;
          object-fit: cover;
          border-radius: 6px;
          cursor: pointer;
          transition: transform 0.2s;
          border: 1px solid #EBEEF5;

          &:hover {
            transform: scale(1.04);
          }
        }

        span {
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
          display: block;
        }
      }
    }
  }
}

// 旋转动画
@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 淡入淡出动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 900px) {
  .main-content {
    grid-template-columns: 1fr;
  }

  .viewer-area {
    min-height: 400px !important;
  }
}
</style>

/* 全局样式 */
<style lang="scss">
/* 设置基础背景为灰色 */
model-viewer {
  background-color: #e0e0e0 !important;
}

/* 自定义默认覆盖层：创建底部白色椭圆底座效果 */
model-viewer::part(default-overlay) {
  background: radial-gradient(
      ellipse at center bottom,
      rgba(255, 255, 255, 0.9) 0%,
      rgba(255, 255, 255, 0.6) 20%,
      transparent 60%
  );
  mask-image: none;
}

/* 优化全屏按钮样式 */
model-viewer::part(fullscreen-button) {
  background: rgba(255, 255, 255, 0.9) !important;
  border-radius: 6px !important;
  padding: 8px !important;
  right: 12px !important;
  top: 12px !important;
  color: #303133 !important;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;

  &:hover {
    background: #ffffff !important;
    color: #409EFF !important;
    transform: scale(1.1);
  }
}

/* 确保模型容器高度足够，以便看到底座效果 */
.custom-model-viewer {
  width: 100%;
  height: 650px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.05);
}

/* 全屏时的模型查看器样式 */
:-webkit-full-screen .custom-model-viewer,
:-moz-full-screen .custom-model-viewer,
:-ms-fullscreen .custom-model-viewer,
:fullscreen .custom-model-viewer {
  width: 100% !important;
  height: 100% !important;
  border-radius: 0;
}

/* 全屏时的容器样式 */
:-webkit-full-screen .viewer-area,
:-moz-full-screen .viewer-area,
:-ms-fullscreen .viewer-area,
:fullscreen .viewer-area {
  background: #e0e0e0;
}
</style>
