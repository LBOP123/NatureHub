<template>
  <div class="observation-detail-container">
    <el-card v-loading="loading" class="detail-card">
      <!-- 头部操作栏 -->
      <div slot="header" class="detail-header">
        <el-button icon="el-icon-back" size="small" @click="goBack">返回</el-button>
        <div class="header-actions">
          <el-button
            v-if="detail.reviewStatus === 'draft' || detail.reviewStatus === 'rejected'"
            type="primary"
            icon="el-icon-edit"
            size="small"
            @click="handleEdit"
          >编辑</el-button>
          <el-button
            v-if="detail.reviewStatus === 'draft'"
            type="success"
            icon="el-icon-s-promotion"
            size="small"
            @click="handleSubmitReview"
          >提交审核</el-button>
          <el-button
            v-if="detail.reviewStatus === 'approved'"
            type="success"
            icon="el-icon-share"
            size="small"
            @click="handleShare"
          >分享到社群</el-button>
          <el-button
            type="danger"
            icon="el-icon-delete"
            size="small"
            @click="handleDelete"
          >删除</el-button>
        </div>
      </div>

      <!-- 标题和状态 -->
      <div class="detail-title-section">
        <h1 class="detail-title">{{ detail.title || '暂无标题' }}</h1>
        <div class="detail-status">
          <el-tag :type="getStatusType(detail.reviewStatus)" size="medium">
            {{ getStatusText(detail.reviewStatus) }}
          </el-tag>
          <span v-if="detail.reviewStatus === 'rejected'" class="reject-reason">
            <el-popover placement="top" width="300" trigger="hover">
              <div class="reject-info">
                <p><strong>驳回原因：</strong></p>
                <p>{{ detail.rejectReason || '暂无说明' }}</p>
                <p class="reject-time">驳回时间：{{ detail.reviewTime || '暂无' }}</p>
              </div>
              <el-button slot="reference" type="text" size="small">
                <i class="el-icon-warning"></i> 查看驳回原因
              </el-button>
            </el-popover>
          </span>
        </div>
      </div>

      <!-- 基本信息 -->
      <el-descriptions :column="2" border class="detail-info">
        <el-descriptions-item label="物种类型">
          <el-tag type="success" size="small">{{ getSpeciesTypeText(detail.speciesType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="物种名称">
          <span class="species-name">{{ detail.speciesName || '暂无' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="观察时间">
          <i class="el-icon-time"></i> {{ detail.observationTime || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="观察地点">
          <i class="el-icon-location"></i> {{ detail.location || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="经纬度" v-if="detail.latitude && detail.longitude">
          <el-button type="text" size="small" @click="showMap">
            {{ detail.latitude }}, {{ detail.longitude }}
            <i class="el-icon-map-location"></i>
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="是否分享">
          <el-tag :type="detail.isShared === '1' ? 'success' : 'info'" size="small">
            {{ detail.isShared === '1' ? '已分享' : '未分享' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ detail.createTime || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ detail.updateTime || '暂无' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 生境描述 -->
      <div v-if="detail.habitat" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-place"></i> 生境描述
        </h3>
        <div class="section-content">
          {{ detail.habitat }}
        </div>
      </div>

      <!-- 图片展示 -->
      <div v-if="imageList.length > 0" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-picture"></i> 观察图片
        </h3>
        <div class="image-gallery">
          <el-image
            v-for="(url, index) in imageList"
            :key="index"
            :src="url"
            :preview-src-list="imageList"
            fit="cover"
            class="gallery-image"
          />
        </div>
      </div>

      <!-- 视频展示 -->
      <div v-if="videoList.length > 0" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-video-camera"></i> 观察视频
        </h3>
        <div class="video-gallery">
          <video
            v-for="(url, index) in videoList"
            :key="index"
            :src="url"
            controls
            class="gallery-video"
          />
        </div>
      </div>

      <!-- 详细描述 -->
      <div v-if="detail.description" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-document"></i> 详细描述
        </h3>
        <div class="section-content description-content">
          {{ detail.description }}
        </div>
      </div>

      <!-- 审核信息 -->
      <div v-if="detail.reviewStatus !== 'draft'" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-s-check"></i> 审核信息
        </h3>
        <el-timeline>
          <el-timeline-item
            timestamp="提交审核"
            :color="detail.reviewStatus === 'pending' ? '#409EFF' : '#67C23A'"
          >
            <p>提交时间：{{ detail.submitTime || detail.createTime || '暂无' }}</p>
          </el-timeline-item>
          <el-timeline-item
            v-if="detail.reviewStatus === 'approved'"
            timestamp="审核通过"
            color="#67C23A"
          >
            <p>审核时间：{{ detail.reviewTime || '暂无' }}</p>
            <p v-if="detail.reviewerId">审核人ID：{{ detail.reviewerId }}</p>
            <p v-if="detail.reviewComment">审核意见：{{ detail.reviewComment }}</p>
          </el-timeline-item>
          <el-timeline-item
            v-if="detail.reviewStatus === 'rejected'"
            timestamp="审核驳回"
            color="#F56C6C"
          >
            <p>驳回时间：{{ detail.reviewTime || '暂无' }}</p>
            <p v-if="detail.reviewerId">审核人ID：{{ detail.reviewerId }}</p>
            <p v-if="detail.rejectReason">驳回原因：{{ detail.rejectReason }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>

    <!-- 地图对话框 -->
    <el-dialog title="观察位置" :visible.sync="mapDialogVisible" width="800px" append-to-body>
      <div id="map-container" style="height: 500px;"></div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="mapDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 分享对话框 -->
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
import { getRecord, delRecord, submitForReview, shareRecordToCommunity } from '@/api/user/record'

export default {
  name: 'ObservationDetail',
  data() {
    return {
      loading: false,
      // 完全适配数据库 nh_observation_record
      detail: {
        recordId: null,
        userId: null,
        title: '',
        observationTime: '',
        location: '',
        latitude: '',
        longitude: '',
        speciesType: '',
        speciesName: '',
        habitat: '',
        description: '',
        images: '',
        videos: '',
        reviewStatus: 'draft',
        submitTime: '',
        reviewTime: '',
        reviewerId: null,
        reviewComment: '',
        rejectReason: '',
        specimenId: null,
        isShared: '0',
        delFlag: '0',
        createBy: '',
        createTime: '',
        updateBy: '',
        updateTime: '',
        remark: ''
      },
      imageList: [],
      videoList: [],
      mapDialogVisible: false,
      shareDialogVisible: false,
      shareLoading: false,
      shareForm: {
        recordId: null,
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
    const recordId = this.$route.params.recordId
    if (recordId) {
      this.getDetail(recordId)
    }
  },
  methods: {
    /** 获取详情（已修复接口+解析） */
    getDetail(recordId) {
      this.loading = true
      getRecord(recordId).then(response => {
        this.detail = { ...this.detail, ...response.data }

        // 图片解析（防报错）
        if (this.detail.images) {
          try {
            this.imageList = JSON.parse(this.detail.images)
          } catch (e) {
            this.imageList = []
          }
        }
        // 视频解析（防报错）
        if (this.detail.videos) {
          try {
            this.videoList = JSON.parse(this.detail.videos)
          } catch (e) {
            this.videoList = []
          }
        }
      }).finally(() => {
        this.loading = false
      })
    },

    /** 返回 */
    goBack() {
      this.$router.go(-1)
    },

    /** 编辑 */
    handleEdit() {
      this.$router.push('/user/observation/upload/' + this.detail.recordId)
    },

    /** 提交审核 */
    handleSubmitReview() {
      this.$confirm('提交审核后将无法修改，是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return submitForReview(this.detail.recordId)
      }).then(() => {
        this.$message.success('提交审核成功')
        this.getDetail(this.detail.recordId)
      })
    },

    /** 分享 */
    handleShare() {
      this.shareForm = {
        recordId: this.detail.recordId,
        topicType: 'observation',
        title: this.detail.title || '观察记录分享',
        content: this.detail.description ? this.detail.description.substring(0, 200) : ''
      }
      this.shareDialogVisible = true
    },

    /** 确认分享 */
    confirmShare() {
      this.$refs.shareForm.validate(valid => {
        if (valid) {
          this.shareLoading = true
          shareRecordToCommunity(this.shareForm).then(() => {
            this.$message.success('分享成功')
            this.detail.isShared = '1'
            this.shareDialogVisible = false
            this.$router.push('/user/community')
          }).finally(() => {
            this.shareLoading = false
          })
        }
      })
    },

    /** 删除 */
    handleDelete() {
      this.$confirm('是否确认删除该观察记录?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delRecord(this.detail.recordId)
      }).then(() => {
        this.$message.success('删除成功')
        this.$router.push('/user/observation/list')
      })
    },

    /** 显示地图 */
    showMap() {
      this.mapDialogVisible = true
      this.$nextTick(() => {
        this.initMap()
      })
    },

    /** 初始化地图 */
    initMap() {
      const mapContainer = document.getElementById('map-container')
      if (mapContainer) {
        mapContainer.innerHTML = `
          <div style="text-align: center; padding: 50px;">
            <p>经度：${this.detail.longitude || '暂无'}</p>
            <p>纬度：${this.detail.latitude || '暂无'}</p>
            <p style="color: #909399; margin-top: 20px;">提示：集成地图API后可显示地图</p>
          </div>
        `
      }
    },

    /** 物种类型 */
    getSpeciesTypeText(type) {
      const typeMap = { plant: '植物', animal: '动物', fungi: '真菌', other: '其他' }
      return typeMap[type] || '未知'
    },

    /** 状态文本 */
    getStatusText(status) {
      const statusMap = { draft: '草稿', pending: '待审核', approved: '已通过', rejected: '已驳回' }
      return statusMap[status] || status
    },

    /** 状态样式 */
    getStatusType(status) {
      const typeMap = { draft: 'info', pending: 'warning', approved: 'success', rejected: 'danger' }
      return typeMap[status] || 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
.observation-detail-container {
  padding: 20px;

  .detail-card {
    max-width: 1200px;
    margin: 0 auto;

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-actions {
        .el-button {
          margin-left: 10px;
        }
      }
    }

    .detail-title-section {
      margin-bottom: 30px;
      padding-bottom: 20px;
      border-bottom: 2px solid #f0f0f0;

      .detail-title {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
        margin: 0 0 15px 0;
      }
    }

    .detail-info {
      margin-bottom: 30px;
      .species-name { font-weight: bold; color: #67C23A; }
    }

    .detail-section {
      margin-bottom: 30px;

      .section-title {
        font-size: 18px;
        font-weight: bold;
        border-left: 4px solid #409EFF;
        padding-left: 10px;
        margin-bottom: 15px;

        i { color: #409EFF; margin-right: 8px; }
      }

      .section-content {
        padding: 15px;
        background: #f5f7fa;
        border-radius: 4px;
        line-height: 1.8;
      }

      .image-gallery {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 15px;

        .gallery-image {
          width: 100%;
          height: 200px;
          border-radius: 8px;
          cursor: pointer;
        }
      }

      .video-gallery {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: 15px;

        .gallery-video {
          width: 100%;
          border-radius: 8px;
        }
      }
    }
  }
}

@media screen and (max-width: 768px) {
  .observation-detail-container { padding: 10px; }
  .detail-card .detail-header { flex-direction: column; align-items: flex-start; }
  .detail-title { font-size: 22px !important; }
}
</style>
