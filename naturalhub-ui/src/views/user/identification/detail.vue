<template>
  <div class="identification-detail-container">
    <el-card v-loading="loading" class="detail-card">
      <!-- 头部操作栏 -->
      <div slot="header" class="detail-header">
        <el-button icon="el-icon-back" size="small" @click="goBack">返回</el-button>
        <div class="header-actions">
          <el-button
            v-if="identification.auditStatus === 'pending' || identification.auditStatus === 'rejected'"
            type="primary"
            icon="el-icon-edit"
            size="small"
            @click="handleEdit"
          >编辑</el-button>
          <el-button
            v-if="identification.auditStatus === 'approved' && identification.isShared === '0'"
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
        <h1 class="detail-title">{{ identification.title || '暂无标题' }}</h1>
        <div class="detail-status">
          <el-tag :type="getStatusType(identification.status)" size="medium">
            {{ getStatusText(identification.status) }}
          </el-tag>
          <el-tag :type="getAuditStatusType(identification.auditStatus)" size="medium" style="margin-left: 10px">
            {{ getAuditStatusText(identification.auditStatus) }}
          </el-tag>
          <span v-if="identification.auditStatus === 'rejected'" class="reject-reason">
            <el-popover placement="top" width="300" trigger="hover">
              <div class="reject-info">
                <p><strong>驳回原因：</strong></p>
                <p>{{ identification.auditRemark || '暂无说明' }}</p>
                <p class="reject-time">驳回时间：{{ identification.auditTime || '暂无' }}</p>
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
        <el-descriptions-item label="观察时间">
          <i class="el-icon-time"></i> {{ parseTime(identification.observationTime, '{y}-{m}-{d} {h}:{i}') }}
        </el-descriptions-item>
        <el-descriptions-item label="观察地点">
          <i class="el-icon-location"></i> {{ identification.location || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="经纬度" v-if="identification.latitude && identification.longitude">
          <el-button type="text" size="small" @click="showMap">
            {{ identification.latitude }}, {{ identification.longitude }}
            <i class="el-icon-map-location"></i>
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="回答数量">
          <el-tag type="primary" size="small">{{ identification.answerCount || 0 }} 个回答</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="浏览次数">
          {{ identification.viewCount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="是否分享">
          <el-tag :type="identification.isShared === 1 ? 'success' : 'info'" size="small">
            {{ identification.isShared === 1 ? '已分享' : '未分享' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ identification.createTime || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ identification.updateTime || '暂无' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 详细描述 -->
      <div v-if="identification.description" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-document"></i> 详细描述
        </h3>
        <div class="section-content">
          {{ identification.description }}
        </div>
      </div>

      <!-- 特征描述 -->
      <div v-if="identification.features" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-view"></i> 特征描述
        </h3>
        <div class="section-content">
          {{ identification.features }}
        </div>
      </div>

      <!-- 生境描述 -->
      <div v-if="identification.habitat" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-place"></i> 生境描述
        </h3>
        <div class="section-content">
          {{ identification.habitat }}
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

      <!-- 回答列表 -->
      <div class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-chat-dot-round"></i> 回答列表（{{ answers.length }}）
          <el-button
            v-if="identification.auditStatus === 'approved' && !isOwner"
            style="float: right"
            type="primary"
            size="small"
            @click="showAnswerDialog = true"
          >我来回答</el-button>
        </h3>

        <div v-if="answers.length === 0" class="no-answer">
          <el-empty description="暂无回答"></el-empty>
        </div>

        <div v-else class="answer-list">
          <div v-for="answer in answers" :key="answer.answerId" class="answer-item">
            <div class="answer-header">
              <span class="answer-user">{{ answer.userName }}</span>
              <el-tag v-if="answer.isBest === '1'" type="success" size="mini">最佳答案</el-tag>
              <span class="answer-time">{{ parseTime(answer.createTime) }}</span>
            </div>

            <div class="answer-content">
              <div v-if="answer.speciesName" class="species-name">
                <strong>物种名称：</strong>{{ answer.speciesName }}
                <el-tag v-if="answer.confidence === 'high'" type="success" size="mini">高置信度</el-tag>
                <el-tag v-else-if="answer.confidence === 'medium'" type="warning" size="mini">中置信度</el-tag>
                <el-tag v-else-if="answer.confidence === 'low'" type="info" size="mini">低置信度</el-tag>
              </div>
              <p>{{ answer.content }}</p>
              <div v-if="answer.reference" class="reference">
                <strong>参考资料：</strong>{{ answer.reference }}
              </div>
              <div v-if="answer.images" class="answer-images">
                <el-image
                  v-for="(url, index) in getAnswerImages(answer.images)"
                  :key="index"
                  :src="url"
                  :preview-src-list="getAnswerImages(answer.images)"
                  fit="cover"
                  style="width: 100px; height: 100px; margin-right: 10px"
                />
              </div>
            </div>

            <div class="answer-actions">
              <el-button
                type="text"
                icon="el-icon-thumb"
                @click="handleLike(answer)"
              >点赞 ({{ answer.likeCount }})</el-button>
              <el-button
                v-if="isOwner && answer.isBest !== '1' && identification.status !== 'resolved'"
                type="text"
                icon="el-icon-check"
                @click="handleSetBest(answer)"
              >采纳</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 审核信息 -->
      <div v-if="identification.auditStatus !== 'pending'" class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-s-check"></i> 审核信息
        </h3>
        <el-timeline>
          <el-timeline-item
            timestamp="提交审核"
            :color="identification.auditStatus === 'pending' ? '#409EFF' : '#67C23A'"
          >
            <p>提交时间：{{ identification.createTime || '暂无' }}</p>
          </el-timeline-item>
          <el-timeline-item
            v-if="identification.auditStatus === 'approved'"
            timestamp="审核通过"
            color="#67C23A"
          >
            <p>审核时间：{{ identification.auditTime || '暂无' }}</p>
            <p v-if="identification.auditBy">审核人：{{ identification.auditBy }}</p>
            <p v-if="identification.auditRemark">审核意见：{{ identification.auditRemark }}</p>
          </el-timeline-item>
          <el-timeline-item
            v-if="identification.auditStatus === 'rejected'"
            timestamp="审核驳回"
            color="#F56C6C"
          >
            <p>驳回时间：{{ identification.auditTime || '暂无' }}</p>
            <p v-if="identification.auditBy">审核人：{{ identification.auditBy }}</p>
            <p v-if="identification.auditRemark">驳回原因：{{ identification.auditRemark }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>

    <!-- 回答对话框 -->
    <el-dialog title="提交回答" :visible.sync="showAnswerDialog" width="600px" append-to-body>
      <el-form ref="answerForm" :model="answerForm" :rules="answerRules" label-width="100px">
        <el-form-item label="物种名称" prop="speciesName">
          <el-input v-model="answerForm.speciesName" placeholder="请输入物种名称" />
        </el-form-item>
        <el-form-item label="置信度" prop="confidence">
          <el-select v-model="answerForm.confidence" placeholder="请选择置信度">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="回答内容" prop="content">
          <el-input
            v-model="answerForm.content"
            type="textarea"
            placeholder="请输入回答内容"
            :rows="5"
          />
        </el-form-item>
        <el-form-item label="参考资料">
          <el-input v-model="answerForm.reference" placeholder="请输入参考资料" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showAnswerDialog = false">取 消</el-button>
        <el-button type="primary" @click="submitAnswer">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 地图对话框 -->
    <el-dialog title="观察位置" :visible.sync="mapDialogVisible" width="800px" append-to-body>
      <div id="map-container" style="height: 500px;"></div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="mapDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getIdentification,
  submitAnswer,
  setBestAnswer,
  likeAnswer,
  shareIdentification,
  delIdentification
} from "@/api/user/identification";

export default {
  name: "IdentificationDetail",
  data() {
    return {
      loading: true,
      identification: {},
      answers: [],
      imageList: [],
      showAnswerDialog: false,
      mapDialogVisible: false,
      answerForm: {
        speciesName: null,
        confidence: 'medium',
        content: null,
        reference: null
      },
      answerRules: {
        content: [
          { required: true, message: "回答内容不能为空", trigger: "blur" }
        ]
      }
    };
  },
  computed: {
    isOwner() {
      return this.identification.userId === this.$store.state.user.userId;
    }
  },
  created() {
    this.getDetail();
  },
  methods: {
    getDetail() {
      const identificationId = this.$route.query.id;
      getIdentification(identificationId).then(response => {
        this.identification = response.data;
        this.answers = response.answers || [];

        if (this.identification.images) {
          this.imageList = this.identification.images.split(',');
        }

        this.loading = false;
      });
    },
    goBack() {
      this.$router.go(-1);
    },
    handleEdit() {
      this.$router.push({
        path: "/user/identification/create",
        query: { id: this.identification.identificationId }
      });
    },
    handleShare() {
      this.$modal.confirm('确认分享到社群？').then(() => {
        return shareIdentification(this.identification.identificationId);
      }).then(() => {
        this.$modal.msgSuccess("分享成功");
        this.getDetail();
      }).catch(() => {});
    },
    handleDelete() {
      this.$modal.confirm('是否确认删除该鉴定求助?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delIdentification(this.identification.identificationId);
      }).then(() => {
        this.$modal.msgSuccess('删除成功');
        this.$router.push('/user/identification/list');
      });
    },
    submitAnswer() {
      this.$refs["answerForm"].validate(valid => {
        if (valid) {
          submitAnswer(this.identification.identificationId, this.answerForm).then(response => {
            this.$modal.msgSuccess("回答提交成功");
            this.showAnswerDialog = false;
            this.resetAnswerForm();
            this.getDetail();
          });
        }
      });
    },
    resetAnswerForm() {
      this.answerForm = {
        speciesName: null,
        confidence: 'medium',
        content: null,
        reference: null
      };
    },
    handleSetBest(answer) {
      this.$modal.confirm('确认采纳此答案为最佳答案？').then(() => {
        return setBestAnswer(this.identification.identificationId, answer.answerId);
      }).then(() => {
        this.$modal.msgSuccess("采纳成功");
        this.getDetail();
      }).catch(() => {});
    },
    handleLike(answer) {
      likeAnswer(answer.answerId).then(response => {
        this.$modal.msgSuccess("点赞成功");
        this.getDetail();
      });
    },
    showMap() {
      this.mapDialogVisible = true;
      this.$nextTick(() => {
        this.initMap();
      });
    },
    initMap() {
      const mapContainer = document.getElementById('map-container');
      if (mapContainer) {
        mapContainer.innerHTML = `
          <div style="text-align: center; padding: 50px;">
            <p>经度：${this.identification.longitude || '暂无'}</p>
            <p>纬度：${this.identification.latitude || '暂无'}</p>
            <p style="color: #909399; margin-top: 20px;">提示：集成地图API后可显示地图</p>
          </div>
        `;
      }
    },
    getAnswerImages(images) {
      if (!images) return [];
      return images.split(',');
    },
    getStatusText(status) {
      const statusMap = { pending: '待鉴定', answered: '已回答', resolved: '已解决' };
      return statusMap[status] || status;
    },
    getStatusType(status) {
      const typeMap = { pending: 'warning', answered: 'primary', resolved: 'success' };
      return typeMap[status] || 'info';
    },
    getAuditStatusText(status) {
      const statusMap = { pending: '待审核', approved: '已通过', rejected: '已驳回' };
      return statusMap[status] || status;
    },
    getAuditStatusType(status) {
      const typeMap = { pending: 'warning', approved: 'success', rejected: 'danger' };
      return typeMap[status] || 'info';
    }
  }
};
</script>

<style lang="scss" scoped>
.identification-detail-container {
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

      .answer-list {
        .answer-item {
          padding: 15px;
          border-bottom: 1px solid #EBEEF5;

          &:last-child {
            border-bottom: none;
          }

          .answer-header {
            display: flex;
            align-items: center;
            margin-bottom: 10px;

            .answer-user {
              font-weight: bold;
              margin-right: 10px;
            }

            .answer-time {
              margin-left: auto;
              color: #909399;
              font-size: 12px;
            }
          }

          .answer-content {
            margin: 10px 0;

            .species-name {
              margin-bottom: 10px;
              padding: 10px;
              background-color: #f4f4f5;
              border-radius: 4px;
            }

            .reference {
              margin-top: 10px;
              padding: 10px;
              background-color: #f9f9f9;
              border-left: 3px solid #409EFF;
            }

            .answer-images {
              margin-top: 10px;
            }
          }

          .answer-actions {
            margin-top: 10px;
          }
        }
      }

      .no-answer {
        text-align: center;
        padding: 40px 0;
      }
    }
  }
}

@media screen and (max-width: 768px) {
  .identification-detail-container { padding: 10px; }
  .detail-card .detail-header { flex-direction: column; align-items: flex-start; }
  .detail-title { font-size: 22px !important; }
}
</style>
