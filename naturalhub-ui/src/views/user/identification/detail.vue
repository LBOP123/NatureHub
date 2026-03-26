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

      <!-- AI识别结果 -->
      <div v-if="identification.aiSpeciesName" class="detail-section ai-result">
        <h3 class="section-title"><i class="el-icon-cpu"></i> AI识别结果</h3>
        <el-card class="ai-card">
          <div class="ai-content">
            <div class="ai-main">
              <h4 class="ai-species">{{ identification.aiSpeciesName }}</h4>
              <el-tag :type="identification.aiScore >= 0.8 ? 'success' : (identification.aiScore >= 0.5 ? 'warning' : 'info')" size="medium">
                置信度: {{ (identification.aiScore * 100).toFixed(1) }}%
              </el-tag>
            </div>
            <div v-if="identification.baikeDescription" class="ai-desc">
              {{ identification.baikeDescription }}
            </div>
            <div v-if="identification.baikeUrl" class="ai-link">
              <a :href="identification.baikeUrl" target="_blank">
                <i class="el-icon-link"></i> 查看百度百科
              </a>
              <a v-if="identification.baikeImageUrl" href="javascript:void(0)" @click="showBaikeImage" style="margin-left: 12px;"><i class="el-icon-picture"></i> 查看百科图片</a>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 社群投票 -->
      <div v-if="identification.isShared === 1" class="detail-section vote-section">
        <h3 class="section-title"><i class="el-icon-data-analysis"></i> 社群投票</h3>
        <el-card class="vote-card">
          <div class="vote-body">
            <div class="vote-row">
              <span class="vote-label agree-label">同意</span>
              <span class="vote-score agree-score">{{ voteStatus.agreeScore }} 分</span>
              <el-tooltip :content="voteStatus.agreeScore + ' / 20 分'" placement="top">
                <div class="vote-bar-wrap">
                  <div class="vote-bar agree-bar" :style="{ width: Math.min(voteStatus.agreeScore / 20 * 100, 100) + '%' }"></div>
                </div>
              </el-tooltip>
              <span class="vote-max">/ 20</span>
            </div>
            <div class="vote-row">
              <span class="vote-label disagree-label">不同意</span>
              <span class="vote-score disagree-score">{{ voteStatus.disagreeScore }} 分</span>
              <el-tooltip :content="voteStatus.disagreeScore + ' / 20 分'" placement="top">
                <div class="vote-bar-wrap">
                  <div class="vote-bar disagree-bar" :style="{ width: Math.min(voteStatus.disagreeScore / 20 * 100, 100) + '%' }"></div>
                </div>
              </el-tooltip>
              <span class="vote-max">/ 20</span>
            </div>
          </div>
          <div class="vote-actions" v-if="voteStatus.voteStatus === '1'">
            <el-button
              type="success"
              icon="el-icon-thumb"
              :disabled="hasVoted || isOwner"
              @click="handleVote('0')"
            >同意</el-button>
            <el-button
              type="danger"
              icon="el-icon-thumb"
              :disabled="hasVoted || isOwner"
              @click="handleVote('1')"
            >不同意</el-button>
            <el-button
              v-if="isOwner"
              type="warning"
              icon="el-icon-finished"
              @click="handleEndVoting"
            >结束投票</el-button>
            <span v-if="hasVoted" class="voted-hint">
              <i class="el-icon-check"></i> 您已投票
            </span>
            <span v-else-if="isOwner" class="voted-hint">
              <i class="el-icon-info"></i> 提问者不能投票
            </span>
          </div>
          <div class="vote-footer">
            <el-tag :type="getVoteStatusType()" size="small">{{ getVoteStatusText() }}</el-tag>
            <el-tag v-if="voteStatus.voteResult && voteStatus.voteResult !== '2'"
                    :type="voteStatus.voteResult === '0' ? 'success' : 'danger'"
                    size="small" style="margin-left:8px">
              {{ voteStatus.voteResult === '0' ? '已通过' : '未通过' }}
            </el-tag>
            <span class="vote-hint">任一方达到 20 分自动结束</span>
          </div>
        </el-card>
      </div>

      <div class="detail-section">
        <h3 class="section-title">
          <i class="el-icon-chat-dot-round"></i> 鉴定回答（{{ answers.length }}）
          <el-button
            v-if="identification.auditStatus === 2 && !isOwner && isIdentifier"
            style="float:right"
            type="primary"
            size="small"
            @click="showAnswerDialog = true"
          >我来鉴定</el-button>
        </h3>

        <div v-if="adoptedAnswer" class="adopted-banner">
          <div class="adopted-title"><i class="el-icon-medal"></i> 已采纳鉴定意见</div>
          <div class="adopted-content">
            <div class="adopted-user-row">
              <el-avatar v-if="resolveAvatar(adoptedAnswer.userAvatar)" :size="34" :src="resolveAvatar(adoptedAnswer.userAvatar)"></el-avatar>
              <div v-else class="adopted-avatar-text" :style="{ background: getAvatarColor(adoptedAnswer) }">{{ getAvatarText(adoptedAnswer) }}</div>
              <strong>{{ adoptedAnswer.nickName || adoptedAnswer.userName }}</strong>
            </div>
            <div class="adopted-line"><strong>鉴定结果：</strong>{{ adoptedAnswer.speciesName || '未填写' }}</div>
            <div class="adopted-line"><strong>鉴定依据：</strong>{{ adoptedAnswer.content || '未填写' }}</div>
          </div>
        </div>

        <div v-if="answers.length === 0" class="no-answer">
          <el-empty description="暂无鉴定回答"></el-empty>
        </div>
        <div v-else class="answer-list">
          <div v-for="answer in sortedAnswers" :key="answer.answerId" class="answer-item" :class="{ 'adopted-answer': answer.isBest === 1 || answer.isBest === '1' }">
            <div class="answer-header">
              <div class="answer-user-wrap">
                <el-avatar v-if="resolveAvatar(answer.userAvatar)" :size="36" :src="resolveAvatar(answer.userAvatar)"></el-avatar>
                <div v-else class="answer-avatar-text" :style="{ background: getAvatarColor(answer) }">{{ getAvatarText(answer) }}</div>
                <div class="answer-user-meta">
                  <span class="answer-user">{{ answer.nickName || answer.userName }}</span>
                  <el-tag v-if="answer.userType === '2'" type="warning" size="mini">鉴定者</el-tag>
                </div>
              </div>
              <div class="answer-right">
                <el-tag v-if="answer.isBest === 1 || answer.isBest === '1'" type="success" size="mini">已采纳</el-tag>
                <span class="answer-time">{{ parseTime(answer.createTime) }}</span>
              </div>
            </div>
            <div class="answer-content">
              <div class="result-basis">
                <div class="result-line">
                  <strong>鉴定结果：</strong>
                  <span>{{ answer.speciesName || '未填写' }}</span>
                </div>
                <div class="basis-line">
                  <strong>鉴定依据：</strong>
                  <span class="result-basis-text">{{ answer.content || '未填写' }}</span>
                  <el-button
                    v-if="answer.reference"
                    type="text"
                    class="reference-link"
                    @click="showReference(answer.reference)"
                  >
                    <i class="el-icon-link"></i>
                  </el-button>
                </div>
              </div>
            </div>
            <div class="answer-actions">
              <el-button type="text" icon="el-icon-thumb" @click="handleLike(answer)">
                点赞 ({{ answer.likeCount }})
              </el-button>
              <el-button
                v-if="isOwner && !(answer.isBest === 1 || answer.isBest === '1')"
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
            <p v-if="identification.auditBy">审核人：{{ identification.auditBy }}</p>
          </el-timeline-item>
          <el-timeline-item
            v-if="identification.auditStatus === 3"
            timestamp="审核驳回"
            color="#F56C6C"
          >
            <p>驳回时间：{{ identification.auditTime || '暂无' }}</p>
            <p v-if="identification.auditRemark">驳回原因：{{ identification.auditRemark }}</p>
            <p v-if="identification.auditBy">审核人：{{ identification.auditBy }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>

    <el-dialog title="我来鉴定" :visible.sync="showAnswerDialog" width="600px" append-to-body>
      <el-form ref="answerForm" :model="answerForm" :rules="answerRules" label-width="100px">
        <el-form-item label="物种名称" prop="speciesName">
          <el-input v-model="answerForm.speciesName" placeholder="请输入鉴定的物种名称" />
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
          <el-input v-model="answerForm.reference" type="textarea" :rows="3" placeholder="请输入参考资料原文" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showAnswerDialog = false">取 消</el-button>
        <el-button type="primary" @click="submitAnswer">提 交</el-button>
      </div>
    </el-dialog>

    <el-dialog title="参考资料" :visible.sync="referenceDialogVisible" width="560px" append-to-body>
      <div class="reference-dialog-content">{{ currentReference }}</div>
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

    <!-- 百科图片预览弹窗 -->
    <el-dialog title="百科图片" :visible.sync="baikeImageVisible" width="500px" append-to-body>
      <div class="baike-image-preview">
        <img :src="identification.baikeImageUrl" referrerpolicy="no-referrer" style="width:100%;display:block;" />
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
  delIdentification,
  updateIdentification,
  getVoteStatus,
  submitVote,
  getVoteCount,
  getVoteDetails,
  endVoting
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
      baikeImageVisible: false,
      referenceDialogVisible: false,
      currentReference: '',
      shareLoading: false,
      shareForm: {
        title: '',
        content: ''
      },
      answerForm: {
        speciesName: null,
        content: null,
        reference: null
      },
      answerRules: {
        content: [{ required: true, message: "回答内容不能为空", trigger: "blur" }]
      },
      voteStatus: {
        agreeScore: 0,
        disagreeScore: 0,
        voteStatus: '0',
        voteResult: 'undetermined'
      },
      hasVoted: false,
      voteCount: 0
    };
  },
  computed: {
    isOwner() {
      return this.identification.userId === this.$store.state.user.id;
    },
    isIdentifier() {
      return this.$store.getters.userType === '2';
    },
    adoptedAnswer() {
      return this.answers.find(item => item.isBest === 1 || item.isBest === '1') || null;
    },
    sortedAnswers() {
      const list = [...this.answers];
      return list.sort((a, b) => {
        const aBest = (a.isBest === 1 || a.isBest === '1') ? 1 : 0;
        const bBest = (b.isBest === 1 || b.isBest === '1') ? 1 : 0;
        return bBest - aBest;
      });
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

        // 获取投票状态
        if (this.identification.isShared === 1) {
          this.getVoteStatusInfo();
        }
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
    resolveAvatar(avatar) {
      if (!avatar) return '';
      if (avatar.startsWith('http://') || avatar.startsWith('https://')) return avatar;
      if (avatar.startsWith('/dev-api') || avatar.startsWith('/prod-api')) return avatar;
      return (process.env.VUE_APP_BASE_API || '') + avatar;
    },
    getAvatarText(answer) {
      const name = (answer.nickName || answer.userName || '').trim();
      if (!name) return '?';
      return name.charAt(0).toUpperCase();
    },
    getAvatarColor(answer) {
      const colors = ['#43a06b', '#2e7d9a', '#8e6bbf', '#c0640a', '#c0392b', '#1a6b8a', '#6d8c3e', '#7b4f9e', '#1a8c6b', '#e67e22'];
      const name = (answer.nickName || answer.userName || '').trim();
      if (!name) return colors[0];
      let hash = 0;
      for (let i = 0; i < name.length; i++) hash += name.charCodeAt(i);
      return colors[hash % colors.length];
    },
    formatResultBasis(answer) {
      const species = answer.speciesName ? `【${answer.speciesName}】` : '';
      return `${species}${answer.content || ''}`;
    },
    showReference(reference) {
      this.currentReference = reference;
      this.referenceDialogVisible = true;
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
    },
    getVoteStatusInfo() {
      getVoteStatus(this.$route.query.id).then(response => {
        this.voteStatus = response.data || {
          agreeScore: 0,
          disagreeScore: 0,
          voteStatus: '0',
          voteResult: '2'
        };
        this.checkIfVoted();
      });
    },
    checkIfVoted() {
      // 检查当前用户是否已投票
      getVoteDetails(this.$route.query.id).then(response => {
        const votes = response.data || [];
        const currentUserId = this.$store.state.user.id;
        this.hasVoted = votes.some(v => v.userId == currentUserId);
        this.voteCount = votes.length;
      });
    },
    handleVote(voteType) {
      this.$confirm(`确认投票"${voteType === '0' ? '同意' : '不同意'}"吗？`, "投票确认", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        return submitVote(this.$route.query.id, { voteType });
      }).then(() => {
        this.$message.success("投票成功");
        this.hasVoted = true;
        this.getVoteStatusInfo();
      }).catch(() => {});
    },
    handleEndVoting() {
      this.$confirm("确认结束投票吗？结束后将根据当前投票情况确定最终结果。", "结束投票", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        return endVoting(this.$route.query.id);
      }).then(() => {
        this.$message.success("投票已结束");
        this.getVoteStatusInfo();
      }).catch(() => {});
    },
    showBaikeImage() {
      this.baikeImageVisible = true;
    },
    getVoteStatusType() {
      switch (this.voteStatus.voteStatus) {
        case '0': return 'info';
        case '1': return 'primary';
        case '2': return 'success';
        default: return 'info';
      }
    },
    getVoteStatusText() {
      switch (this.voteStatus.voteStatus) {
        case '0': return '未开始';
        case '1': return '进行中';
        case '2': return '已结束';
        default: return '未知';
      }
    },
    getAgreePercent() {
      const total = this.voteStatus.agreeScore + this.voteStatus.disagreeScore;
      return total === 0 ? 0 : Math.round((this.voteStatus.agreeScore / total) * 100);
    },
    getDisagreePercent() {
      const total = this.voteStatus.agreeScore + this.voteStatus.disagreeScore;
      return total === 0 ? 0 : Math.round((this.voteStatus.disagreeScore / total) * 100);
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
      .adopted-banner {
        margin-bottom: 16px;
        padding: 12px 14px;
        border-radius: 8px;
        border: 1px solid #b3e19d;
        background: #f0f9eb;

        .adopted-title {
          font-size: 14px;
          font-weight: 600;
          color: #67c23a;
          margin-bottom: 6px;
        }

        .adopted-content {
          font-size: 13px;
          color: #4a4a5a;
          line-height: 1.7;

          .adopted-user-row {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 6px;
          }

          .adopted-avatar-text {
            width: 34px;
            height: 34px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fff;
            font-size: 14px;
            font-weight: 700;
            user-select: none;
          }

          .adopted-line {
            margin-top: 4px;
          }
        }
      }

      .answer-list {
        .answer-item {
          padding: 15px;
          border-bottom: 1px solid #EBEEF5;
          &:last-child {
            border-bottom: none;
          }

          &.adopted-answer {
            border-radius: 8px;
            margin-bottom: 10px;
          }

          .answer-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 10px;

            .answer-user-wrap {
              display: flex;
              align-items: center;
              gap: 10px;

              .answer-avatar-text {
                width: 36px;
                height: 36px;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                color: #fff;
                font-size: 14px;
                font-weight: 700;
                user-select: none;
              }
            }

            .answer-user-meta {
              display: flex;
              align-items: center;
              gap: 6px;
            }

            .answer-user {
              font-weight: bold;
            }

            .answer-right {
              display: flex;
              align-items: center;
              gap: 8px;
            }

            .answer-time {
              color: #909399;
              font-size: 12px;
            }
          }

          .answer-content {
            margin: 10px 0;

            .result-basis {
              margin-bottom: 10px;
              padding: 10px;
              background-color: #f4f4f5;
              border-radius: 4px;
              line-height: 1.7;
              font-size: 13px;

              .result-line,
              .basis-line {
                display: flex;
                align-items: flex-start;
                gap: 4px;
              }

              .basis-line {
                margin-top: 4px;
              }

              .result-basis-text {
                white-space: pre-wrap;
              }
            }

            .reference-link {
              margin-left: 6px;
              padding: 0;
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
    .ai-result {
      .ai-card {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        .ai-content {
          .ai-main {
            display: flex;
            align-items: center;
            gap: 15px;
            .ai-species {
              font-size: 24px;
              font-weight: bold;
              margin: 0;
            }
          }
          .ai-desc {
            margin: 15px 0;
            opacity: 0.95;
            line-height: 1.6;
          }
          .ai-link {
            a {
              color: white;
              text-decoration: none;
              display: inline-flex;
              align-items: center;
              padding: 8px 16px;
              background: rgba(255,255,255,0.2);
              border-radius: 20px;
              &:hover {
                background: rgba(255,255,255,0.3);
              }
            }
          }
        }
      }
    }
    .vote-section {
      .vote-card {
        ::v-deep .el-card__body { padding: 16px 20px; }
        .vote-body {
          display: flex;
          flex-direction: column;
          gap: 14px;
          margin-bottom: 14px;
          .vote-row {
            display: flex;
            align-items: center;
            gap: 10px;
            .vote-label {
              font-size: 13px;
              font-weight: 600;
              width: 42px;
              flex-shrink: 0;
              &.agree-label { color: #67c23a; }
              &.disagree-label { color: #f56c6c; }
            }
            .vote-score {
              font-size: 15px;
              font-weight: bold;
              width: 52px;
              text-align: right;
              flex-shrink: 0;
              &.agree-score { color: #67c23a; }
              &.disagree-score { color: #f56c6c; }
            }
            .vote-bar-wrap {
              flex: 1;
              height: 10px;
              background: #e4e7ed;
              border-radius: 5px;
              overflow: hidden;
              .vote-bar {
                height: 100%;
                border-radius: 5px;
                transition: width 0.5s ease;
                &.agree-bar { background: linear-gradient(90deg, #67c23a, #95d475); }
                &.disagree-bar { background: linear-gradient(90deg, #f56c6c, #f89898); }
              }
            }
            .vote-max {
              font-size: 12px;
              color: #909399;
              flex-shrink: 0;
              width: 28px;
            }
          }
        }
        .vote-actions {
          display: flex;
          gap: 15px;
          align-items: center;
          flex-wrap: wrap;
          margin-bottom: 14px;
          .voted-hint {
            color: #909399;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 5px;
            padding: 8px 16px;
            background: #f4f4f5;
            border-radius: 20px;
          }
        }
        .vote-footer {
          display: flex;
          align-items: center;
          padding-top: 12px;
          border-top: 1px solid #ebeef5;
          gap: 8px;
          .vote-hint {
            margin-left: auto;
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }
  }

  .baike-image-preview {
    text-align: center;
    .image-placeholder {
      padding: 40px;
      color: #909399;
      i {
        font-size: 40px;
      }
    }
  }

  .reference-dialog-content {
    font-size: 14px;
    color: #4a4a5a;
    line-height: 1.8;
    white-space: pre-wrap;
    max-height: 360px;
    overflow-y: auto;
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
