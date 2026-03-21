<template>
  <div class="identification-detail-container">
    <el-card v-loading="loading" class="detail-card">
      <div slot="header" class="detail-header">
        <el-button icon="el-icon-back" size="small" @click="goBack">返回</el-button>
        <div class="header-actions">
          <el-button
            v-if="identification.auditStatus === 0 || identification.auditStatus === 3"
            type="primary"
            icon="el-icon-edit"
            size="small"
            @click="handleEdit"
          >编辑</el-button>
          <el-button
            v-if="identification.auditStatus === 0"
            type="success"
            icon="el-icon-s-promotion"
            size="small"
            @click="handleSubmitReview"
          >提交审核</el-button>
          <el-button
            v-if="identification.auditStatus === 2 && identification.isShared !== 1"
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

      <div class="detail-title-section">
        <h1 class="detail-title">{{ identification.title || '暂无标题' }}</h1>
        <div class="detail-status">
          <el-tag :type="getStatusType(identification.status)" size="medium">{{ getStatusText(identification.status) }}</el-tag>
          <el-tag :type="getAuditStatusType(identification.auditStatus)" size="medium" style="margin-left:10px">{{ getAuditStatusText(identification.auditStatus) }}</el-tag>
          <span v-if="identification.auditStatus === 3" class="reject-reason">
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

      <el-descriptions :column="2" border class="detail-info">
        <el-descriptions-item label="观察时间">
          <i class="el-icon-time"></i> {{ parseTime(identification.observationTime, '{y}-{m}-{d} {h}:{i}') }}
        </el-descriptions-item>
        <el-descriptions-item label="观察地点">
          <i class="el-icon-location"></i> {{ identification.location || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="回答数量">
          <el-tag type="primary" size="small">{{ identification.answerCount || 0 }} 条</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="浏览次数">{{ identification.viewCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="分享状态">
          <el-tag :type="identification.isShared === 1 ? 'success' : 'info'" size="small">
            {{ identification.isShared === 1 ? '已分享' : '未分享' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ identification.createTime || '暂无' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="identification.description" class="detail-section">
        <h3 class="section-title"><i class="el-icon-document"></i> 问题描述</h3>
        <div class="section-content">{{ identification.description }}</div>
      </div>

      <div v-if="identification.features" class="detail-section">
        <h3 class="section-title"><i class="el-icon-view"></i> 物种特征</h3>
        <div class="section-content">{{ identification.features }}</div>
      </div>

      <div v-if="identification.habitat" class="detail-section">
        <h3 class="section-title"><i class="el-icon-place"></i> 生境信息</h3>
        <div class="section-content">{{ identification.habitat }}</div>
      </div>

      <div v-if="imageList.length > 0" class="detail-section">
        <h3 class="section-title"><i class="el-icon-picture"></i> 鉴定图片</h3>
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

      <div class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-chat-dot-round"></i> 鉴定回答（{{ answers.length }}）
          <el-button
            v-if="identification.auditStatus === 2 && !isOwner"
            style="float:right"
            type="primary"
            size="small"
            @click="showAnswerDialog = true"
          >我来鉴定</el-button>
        </h3>

        <div v-if="answers.length === 0" class="no-answer">
          <el-empty description="暂无鉴定回答"></el-empty>
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
                <strong>鉴定结果：</strong>{{ answer.speciesName }}
                <el-tag v-if="answer.confidence === 'high'" type="success" size="mini">可信度高</el-tag>
                <el-tag v-else-if="answer.confidence === 'medium'" type="warning" size="mini">可信度中</el-tag>
                <el-tag v-else-if="answer.confidence === 'low'" type="info" size="mini">可信度低</el-tag>
              </div>
              <p>{{ answer.content }}</p>
              <div v-if="answer.reference" class="reference">
                <strong>参考依据：</strong>{{ answer.reference }}
              </div>
            </div>
            <div class="answer-actions">
              <el-button type="text" icon="el-icon-thumb" @click="handleLike(answer)">
                点赞 ({{ answer.likeCount }})
              </el-button>
              <el-button
                v-if="isOwner && answer.isBest !== '1' && identification.status !== 2"
                type="text"
                icon="el-icon-check"
                @click="handleSetBest(answer)"
              >采纳</el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="identification.auditStatus !== 0" class="detail-section">
        <h3 class="section-title"><i class="el-icon-s-check"></i> 审核流程</h3>
        <el-timeline>
          <el-timeline-item
            timestamp="提交鉴定"
            :color="identification.auditStatus === 1 ? '#409EFF' : '#67C23A'"
          >
            <p>提交时间：{{ identification.createTime || '暂无' }}</p>
          </el-timeline-item>
          <el-timeline-item
            v-if="identification.auditStatus === 2"
            timestamp="审核通过"
            color="#67C23A"
          >
            <p>审核时间：{{ identification.auditTime || '暂无' }}</p>
            <p v-if="identification.auditRemark">审核意见：{{ identification.auditRemark }}</p>
          </el-timeline-item>
          <el-timeline-item
            v-if="identification.auditStatus === 3"
            timestamp="审核驳回"
            color="#F56C6C"
          >
            <p>驳回时间：{{ identification.auditTime || '暂无' }}</p>
            <p v-if="identification.auditRemark">驳回原因：{{ identification.auditRemark }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>

    <el-dialog title="我来鉴定" :visible.sync="showAnswerDialog" width="600px" append-to-body>
      <el-form ref="answerForm" :model="answerForm" :rules="answerRules" label-width="100px">
        <el-form-item label="物种名称" prop="speciesName">
          <el-input v-model="answerForm.speciesName" placeholder="请输入鉴定的物种名称" />
        </el-form-item>
        <el-form-item label="可信度" prop="confidence">
          <el-select v-model="answerForm.confidence" placeholder="请选择可信度">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="鉴定意见" prop="content">
          <el-input
            v-model="answerForm.content"
            type="textarea"
            placeholder="请输入详细的鉴定依据"
            :rows="5"
          />
        </el-form-item>
        <el-form-item label="参考资料">
          <el-input v-model="answerForm.reference" placeholder="请输入参考资料或文献" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showAnswerDialog = false">取 消</el-button>
        <el-button type="primary" @click="submitAnswer">提 交</el-button>
      </div>
    </el-dialog>

    <!-- 分享弹窗 -->
    <share-dialog
      :visible.sync="shareDialogVisible"
      :source-type="2"
      :init-title="shareForm.title"
      :init-content="shareForm.content"
      :loading="shareLoading"
      @confirm="confirmShare"
    />

  </div>
</template>

<script>
import {
  getIdentification,
  submitAnswer,
  setBestAnswer,
  likeAnswer,
  shareIdentification,
  delIdentification,
  updateIdentification
} from "@/api/user/identification";
import ShareDialog from '@/views/user/components/ShareDialog'

export default {
  name: "IdentificationDetail",
  components: { ShareDialog },
  data() {
    return {
      loading: true,
      identification: {},
      answers: [],
      imageList: [],
      auditStatusOptions: [],
      identifyStatusOptions: [],
      showAnswerDialog: false,
      mapDialogVisible: false,
      shareDialogVisible: false,
      shareLoading: false,
      shareForm: {
        title: '',
        content: ''
      },
      answerForm: {
        speciesName: null,
        confidence: "medium",
        content: null,
        reference: null
      },
      answerRules: {
        content: [{ required: true, message: "回答内容不能为空", trigger: "blur" }]
      }
    };
  },
  computed: {
    isOwner() {
      return this.identification.userId === this.$store.state.user.userId;
    }
  },
  created() {
    this.getDicts("nh_audit_status").then(res => {
      this.auditStatusOptions = res.data || [];
    });
    this.getDicts("nh_identification_status").then(res => {
      this.identifyStatusOptions = res.data || [];
    });
    this.getDetail();
  },
  methods: {
    getDetail() {
      getIdentification(this.$route.query.id).then(response => {
        this.identification = response.data;
        this.answers = response.answers || [];
        if (this.identification.images) {
          this.imageList = this.identification.images.split(",");
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
    handleSubmitReview() {
      this.$confirm("提交审核后将无法修改，是否继续？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          return updateIdentification({
            identificationId: this.identification.identificationId,
            auditStatus: 1
          });
        })
        .then(() => {
          this.$message.success("提交审核成功");
          this.getDetail();
        })
        .catch(() => {});
    },
    handleShare() {
      this.shareForm = {
        title: this.identification.title || '鉴定求助分享',
        content: this.identification.description ? this.identification.description.substring(0, 200) : ''
      };
      this.shareDialogVisible = true;
    },
    confirmShare({ title, content }) {
      this.shareLoading = true;
      shareIdentification(this.identification.identificationId, { title, content }).then(() => {
        this.$message.success('分享成功');
        this.identification.isShared = 1;
        this.shareDialogVisible = false;
        this.$router.push('/user/community');
      }).finally(() => {
        this.shareLoading = false;
      });
    },
    handleDelete() {
      this.$modal.confirm("是否确认删除该鉴定求助？")
        .then(() => {
          return delIdentification(this.identification.identificationId);
        })
        .then(() => {
          this.$modal.msgSuccess("删除成功");
          this.$router.push("/user/identification/list");
        })
        .catch(() => {});
    },
    submitAnswer() {
      this.$refs["answerForm"].validate(valid => {
        if (valid) {
          submitAnswer(this.identification.identificationId, this.answerForm).then(() => {
            this.$modal.msgSuccess("回答提交成功");
            this.showAnswerDialog = false;
            this.answerForm = {
              speciesName: null,
              confidence: "medium",
              content: null,
              reference: null
            };
            this.getDetail();
          });
        }
      });
    },
    handleSetBest(answer) {
      this.$modal.confirm("确认采纳此答案为最佳答案？")
        .then(() => {
          return setBestAnswer(this.identification.identificationId, answer.answerId);
        })
        .then(() => {
          this.$modal.msgSuccess("采纳成功");
          this.getDetail();
        })
        .catch(() => {});
    },
    handleLike(answer) {
      likeAnswer(answer.answerId).then(() => {
        this.$modal.msgSuccess("点赞成功");
        this.getDetail();
      });
    },
    getStatusText(value) {
      const i = this.identifyStatusOptions.find(i => i.dictValue == value);
      return i ? i.dictLabel : value;
    },
    getStatusType(value) {
      const i = this.identifyStatusOptions.find(i => i.dictValue == value);
      return i ? i.listClass : "info";
    },
    getAuditStatusText(value) {
      const i = this.auditStatusOptions.find(i => i.dictValue == value);
      return i ? i.dictLabel : value;
    },
    getAuditStatusType(status) {
      switch (status) {
        case 0: return "";
        case 1: return "warning";
        case 2: return "success";
        case 3: return "danger";
        default: return "";
      }
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
      .header-actions .el-button {
        margin-left: 10px;
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
        i {
          color: #409EFF;
          margin-right: 8px;
        }
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
  .identification-detail-container {
    padding: 10px;
  }
  .detail-title {
    font-size: 22px !important;
  }
}
</style>
