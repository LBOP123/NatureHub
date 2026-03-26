<template>
  <div class="source-identification">
    <!-- panel 模式：图片右侧，只显示基本信息 + 详细描述 -->
    <template v-if="mode === 'panel'">
      <el-descriptions title="鉴定求助信息" :column="1" border size="small" class="info-section">
        <el-descriptions-item v-if="data.observationTime" label="观察时间">
          <i class="el-icon-time"></i> {{ parseTime(data.observationTime, '{y}-{m}-{d} {h}:{i}') }}
        </el-descriptions-item>
        <el-descriptions-item v-if="data.location" label="观察地点">
          <i class="el-icon-location"></i> {{ data.location }}
        </el-descriptions-item>
        <el-descriptions-item v-if="data.habitat" label="生境">{{ data.habitat }}</el-descriptions-item>
        <el-descriptions-item v-if="data.features" label="特征描述">{{ data.features }}</el-descriptions-item>
        <el-descriptions-item v-if="data.status !== null" label="鉴定状态">
          <el-tag :type="statusType(data.status)" size="small">{{ statusMap[data.status] || data.status }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="data.description" class="desc-section">
        <p class="desc-label"><i class="el-icon-document"></i> 详细描述</p>
        <p class="desc-text">{{ data.description }}</p>
      </div>
    </template>

    <!-- full 模式：点赞下方，显示 AI识别 + 投票 + 回答 -->
    <template v-else-if="mode === 'full'">
    <div v-if="data.aiSpeciesName" class="ai-result-section">
      <p class="section-label"><i class="el-icon-cpu"></i> AI识别结果</p>
      <div class="ai-card">
        <div class="ai-main">
          <h4 class="ai-species">{{ data.aiSpeciesName }}</h4>
          <el-tag :type="data.aiScore >= 0.8 ? 'success' : (data.aiScore >= 0.5 ? 'warning' : 'info')" size="small">
            置信度: {{ data.aiScore ? (data.aiScore * 100).toFixed(1) : 0 }}%
          </el-tag>
        </div>
        <div v-if="data.baikeDescription" class="ai-desc">{{ data.baikeDescription }}</div>
        <div class="ai-actions">
          <a v-if="data.baikeUrl" :href="data.baikeUrl" target="_blank" class="ai-link">
            <i class="el-icon-link"></i> 查看百度百科
          </a>
          <a
            v-if="data.baikeImageUrl"
            class="ai-link"
            style="margin-left:10px; cursor:pointer"
            @click.prevent="baikeImageVisible = true"
          >
            <i class="el-icon-picture"></i> 查看百科图片
          </a>
        </div>
      </div>
    </div>

    <!-- 投票区域（仅投票进行中时显示操作按钮） -->
    <div v-if="data.voteStatus" class="vote-section">
      <p class="section-label"><i class="el-icon-data-analysis"></i> 社群投票</p>
      <div class="vote-bars">
        <div class="vote-row">
          <span class="vote-label agree-label">同意</span>
          <span class="vote-score agree-score">{{ data.voteAgreeScore || 0 }} 分</span>
          <div class="vote-bar-wrap">
            <div class="vote-bar agree-bar"
                 :style="{ width: Math.min((data.voteAgreeScore || 0) / 20 * 100, 100) + '%' }"></div>
          </div>
          <span class="vote-max">/ 20</span>
        </div>
        <div class="vote-row">
          <span class="vote-label disagree-label">不同意</span>
          <span class="vote-score disagree-score">{{ data.voteDisagreeScore || 0 }} 分</span>
          <div class="vote-bar-wrap">
            <div class="vote-bar disagree-bar"
                 :style="{ width: Math.min((data.voteDisagreeScore || 0) / 20 * 100, 100) + '%' }"></div>
          </div>
          <span class="vote-max">/ 20</span>
        </div>
      </div>

      <!-- 投票按钮（投票进行中 且 未投票 且 非提问者） -->
      <div v-if="data.voteStatus === '1'" class="vote-actions">
        <template v-if="!isOwner">
          <el-button
            v-if="!hasVoted"
            type="success"
            size="small"
            icon="el-icon-thumb"
            :loading="voteLoading"
            @click="handleVote('0')"
          >同意</el-button>
          <el-button
            v-if="!hasVoted"
            type="danger"
            size="small"
            icon="el-icon-thumb"
            :loading="voteLoading"
            @click="handleVote('1')"
          >不同意</el-button>
          <span v-if="hasVoted" class="voted-hint"><i class="el-icon-check"></i> 您已投票</span>
        </template>
        <span v-else class="voted-hint"><i class="el-icon-info"></i> 提问者不能投票</span>
      </div>

      <div class="vote-footer">
        <el-tag :type="voteStatusTagType" size="mini">{{ voteStatusText }}</el-tag>
        <el-tag v-if="data.voteResult && data.voteResult !== '2'"
                :type="data.voteResult === '0' ? 'success' : 'danger'"
                size="mini" style="margin-left:6px">
          {{ data.voteResult === '0' ? '已通过' : '未通过' }}
        </el-tag>
        <span class="vote-hint">任一方达到 20 分自动结束</span>
      </div>
    </div>

    <!-- 鉴定回答列表 -->
    <div class="answers-section">
      <div class="answers-header">
        <p class="section-label"><i class="el-icon-chat-dot-round"></i> 鉴定回答（{{ answers.length }}）</p>
        <el-button
          v-if="data.auditStatus === 2 && !isOwner && isIdentifier"
          type="primary"
          size="mini"
          @click="showAnswerDialog = true"
        >我来鉴定</el-button>
      </div>

      <div v-if="answersLoading" class="answers-loading">
        <i class="el-icon-loading"></i> 加载中...
      </div>
      <div v-else-if="answers.length === 0" class="no-answer">
        <el-empty description="暂无鉴定回答，快来第一个鉴定吧" :image-size="60"></el-empty>
      </div>
      <div v-else class="answer-list">
        <div v-for="answer in sortedAnswers" :key="answer.answerId" class="answer-item" :class="{ 'adopted-answer': answer.isBest === 1 || answer.isBest === '1' }">
          <div class="answer-header">
            <div class="answer-user-wrap">
              <el-avatar v-if="resolveAvatar(answer.userAvatar)" :size="34" :src="resolveAvatar(answer.userAvatar)"></el-avatar>
              <div v-else class="answer-avatar-text" :style="{ background: getAvatarColor(answer) }">{{ getAvatarText(answer) }}</div>
              <div class="answer-user-meta">
                <span class="answer-user">{{ answer.nickName || answer.userName }}</span>
                <el-tag v-if="answer.userType === '2'" type="warning" size="mini">鉴定者</el-tag>
              </div>
            </div>
            <div class="answer-header-right">
              <el-tag v-if="answer.isBest === 1 || answer.isBest === '1'" type="success" size="mini" effect="dark">已采纳</el-tag>
              <span class="answer-time">{{ parseTime(answer.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
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
            <el-button type="text" size="mini" icon="el-icon-thumb" @click="handleLikeAnswer(answer)">
              点赞 ({{ answer.likeCount || 0 }})
            </el-button>
            <el-button
              v-if="isOwner && !(answer.isBest === 1 || answer.isBest === '1')"
              type="text"
              size="mini"
              icon="el-icon-check"
              @click="handleSetBest(answer)"
            >采纳</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 提交回答弹窗 -->
    <el-dialog title="我来鉴定" :visible.sync="showAnswerDialog" width="560px" append-to-body>
      <el-form ref="answerForm" :model="answerForm" :rules="answerRules" label-width="90px">
        <el-form-item label="物种名称" prop="speciesName">
          <el-input v-model="answerForm.speciesName" placeholder="请输入鉴定的物种名称" />
        </el-form-item>
        <el-form-item label="鉴定意见" prop="content">
          <el-input
            v-model="answerForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入详细的鉴定依据和分析"
          />
        </el-form-item>
        <el-form-item label="参考资料">
          <el-input v-model="answerForm.reference" type="textarea" :rows="3" placeholder="参考资料原文（选填）" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showAnswerDialog = false">取 消</el-button>
        <el-button type="primary" :loading="answerLoading" @click="submitAnswer">提 交</el-button>
      </div>
    </el-dialog>

    <el-dialog title="参考资料" :visible.sync="referenceDialogVisible" width="560px" append-to-body>
      <div class="reference-dialog-content">{{ currentReference }}</div>
    </el-dialog>
    </template>

    <!-- 百科图片弹窗（在模式判断外，确保始终挂载） -->
    <el-dialog title="百科图片" :visible.sync="baikeImageVisible" width="500px" append-to-body>
      <div style="text-align:center">
        <img :src="data.baikeImageUrl" referrerpolicy="no-referrer" style="width:100%;display:block;" />
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
  getVoteDetails,
  submitVote
} from '@/api/user/identification'

export default {
  name: 'SourceIdentification',
  props: {
    data: { type: Object, required: true },
    mode: { type: String, default: 'panel' } // 'panel'=图片右侧只显示基本信息, 'full'=点赞下方显示AI+投票+回答
  },
  data() {
    return {
      statusMap: { 0: '未鉴定', 1: '鉴定中', 2: '已鉴定' },
      // 回答相关
      answers: [],
      answersLoading: false,
      showAnswerDialog: false,
      answerLoading: false,
      answerForm: { speciesName: '', content: '', reference: '' },
      answerRules: {
        content: [{ required: true, message: '鉴定意见不能为空', trigger: 'blur' }]
      },
      // 投票相关
      hasVoted: false,
      voteLoading: false,
      baikeImageVisible: false,
      referenceDialogVisible: false,
      currentReference: ''
    }
  },
  computed: {
    currentUserId() {
      return this.$store.getters.id
    },
    isOwner() {
      return this.data.userId == this.currentUserId
    },
    isIdentifier() {
      return this.$store.getters.userType === '2'
    },
    voteStatusTagType() {
      return { '0': 'info', '1': 'primary', '2': 'success' }[this.data.voteStatus] || 'info'
    },
    voteStatusText() {
      return { '0': '未开始', '1': '进行中', '2': '已结束' }[this.data.voteStatus] || '未知'
    },
    sortedAnswers() {
      const list = [...this.answers]
      return list.sort((a, b) => {
        const aBest = (a.isBest === 1 || a.isBest === '1') ? 1 : 0
        const bBest = (b.isBest === 1 || b.isBest === '1') ? 1 : 0
        return bBest - aBest
      })
    }
  },
  watch: {
    'data.identificationId': {
      immediate: true,
      handler(id) {
        if (id) {
          this.loadAnswers()
          this.checkIfVoted()
        }
      }
    }
  },
  methods: {
    statusType(s) {
      return { 0: 'info', 1: 'primary', 2: 'success' }[s] || ''
    },

    // ===== 回答相关 =====
    loadAnswers() {
      if (!this.data.identificationId) return
      this.answersLoading = true
      getIdentification(this.data.identificationId).then(res => {
        this.answers = res.answers || []
      }).catch(() => {
        this.answers = []
      }).finally(() => {
        this.answersLoading = false
      })
    },

    submitAnswer() {
      this.$refs.answerForm.validate(valid => {
        if (!valid) return
        this.answerLoading = true
        submitAnswer(this.data.identificationId, this.answerForm).then(() => {
          this.$message.success('回答提交成功')
          this.showAnswerDialog = false
          this.answerForm = { speciesName: '', content: '', reference: '' }
          this.loadAnswers()
        }).catch(() => {
          this.$message.error('提交失败，请重试')
        }).finally(() => {
          this.answerLoading = false
        })
      })
    },

    handleLikeAnswer(answer) {
      likeAnswer(answer.answerId).then(() => {
        this.$message.success('点赞成功')
        this.loadAnswers()
      })
    },

    handleSetBest(answer) {
      this.$confirm('确认采纳此答案为最佳答案？').then(() => {
        return setBestAnswer(this.data.identificationId, answer.answerId)
      }).then(() => {
        this.$message.success('采纳成功')
        this.loadAnswers()
      }).catch(() => {})
    },

    resolveAvatar(avatar) {
      if (!avatar) return ''
      if (avatar.startsWith('http://') || avatar.startsWith('https://')) return avatar
      if (avatar.startsWith('/dev-api') || avatar.startsWith('/prod-api')) return avatar
      return (process.env.VUE_APP_BASE_API || '') + avatar
    },

    getAvatarText(answer) {
      const name = (answer.nickName || answer.userName || '').trim()
      if (!name) return '?'
      return name.charAt(0).toUpperCase()
    },

    getAvatarColor(answer) {
      const colors = ['#43a06b', '#2e7d9a', '#8e6bbf', '#c0640a', '#c0392b', '#1a6b8a', '#6d8c3e', '#7b4f9e', '#1a8c6b', '#e67e22']
      const name = (answer.nickName || answer.userName || '').trim()
      if (!name) return colors[0]
      let hash = 0
      for (let i = 0; i < name.length; i++) hash += name.charCodeAt(i)
      return colors[hash % colors.length]
    },

    formatResultBasis(answer) {
      const species = answer.speciesName ? `【${answer.speciesName}】` : ''
      return `${species}${answer.content || ''}`
    },

    showReference(reference) {
      this.currentReference = reference
      this.referenceDialogVisible = true
    },

    // ===== 投票相关 =====
    checkIfVoted() {
      if (!this.data.identificationId || this.data.voteStatus === '0') return
      getVoteDetails(this.data.identificationId).then(res => {
        const votes = res.data || []
        this.hasVoted = votes.some(v => v.userId == this.currentUserId)
      })
    },

    handleVote(voteType) {
      this.$confirm(`确认投票"${voteType === '0' ? '同意' : '不同意'}"吗？`, '投票确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.voteLoading = true
        return submitVote(this.data.identificationId, { voteType })
      }).then(() => {
        this.$message.success('投票成功')
        this.hasVoted = true
        // 通知父组件刷新 sourceData
        this.$emit('vote-updated')
      }).catch(() => {}).finally(() => {
        this.voteLoading = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.source-identification {
  .info-section {
    margin-bottom: 16px;
  }

  .desc-section {
    background: #f9fafb;
    border-radius: 6px;
    padding: 12px 16px;
    margin-bottom: 16px;
  }
  .desc-label {
    font-size: 13px;
    font-weight: 600;
    color: #606266;
    margin: 0 0 6px 0;
  }
  .desc-text {
    font-size: 14px;
    color: #4a4a5a;
    line-height: 1.7;
    white-space: pre-wrap;
    margin: 0;
  }

  .section-label {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 10px 0;
    i { color: #409EFF; margin-right: 5px; }
  }

  // ===== AI识别区域 =====
  .ai-result-section {
    margin-bottom: 16px;
    .ai-card {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 8px;
      padding: 16px 20px;
      color: white;
      .ai-main {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 10px;
        .ai-species {
          font-size: 20px;
          font-weight: bold;
          margin: 0;
        }
      }
      .ai-desc {
        font-size: 13px;
        line-height: 1.7;
        opacity: 0.92;
        margin-bottom: 12px;
      }
      .ai-actions {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
        .ai-link {
          color: white;
          text-decoration: none;
          display: inline-flex;
          align-items: center;
          gap: 4px;
          padding: 6px 14px;
          background: rgba(255,255,255,0.2);
          border-radius: 16px;
          font-size: 13px;
          transition: background 0.2s;
          i { font-size: 13px; }
          &:hover { background: rgba(255,255,255,0.32); }
        }
      }
    }
  }

  // ===== 投票区域 =====
  .vote-section {
    background: #f9fafb;
    border-radius: 8px;
    padding: 14px 16px;
    margin-bottom: 16px;

    .vote-bars {
      display: flex;
      flex-direction: column;
      gap: 10px;
      margin-bottom: 12px;

      .vote-row {
        display: flex;
        align-items: center;
        gap: 8px;

        .vote-label {
          width: 42px;
          font-size: 12px;
          font-weight: 600;
          flex-shrink: 0;
          &.agree-label { color: #67c23a; }
          &.disagree-label { color: #f56c6c; }
        }
        .vote-score {
          width: 48px;
          font-size: 13px;
          font-weight: bold;
          text-align: right;
          flex-shrink: 0;
          &.agree-score { color: #67c23a; }
          &.disagree-score { color: #f56c6c; }
        }
        .vote-bar-wrap {
          flex: 1;
          height: 8px;
          background: #e4e7ed;
          border-radius: 4px;
          overflow: hidden;
          .vote-bar {
            height: 100%;
            border-radius: 4px;
            transition: width 0.5s ease;
            &.agree-bar { background: linear-gradient(90deg, #67c23a, #95d475); }
            &.disagree-bar { background: linear-gradient(90deg, #f56c6c, #f89898); }
          }
        }
        .vote-max {
          font-size: 11px;
          color: #909399;
          flex-shrink: 0;
          width: 28px;
        }
      }
    }

    .vote-actions {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;
      flex-wrap: wrap;
      .voted-hint {
        font-size: 13px;
        color: #909399;
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 5px 10px;
        background: #f4f4f5;
        border-radius: 12px;
      }
    }

    .vote-footer {
      display: flex;
      align-items: center;
      gap: 6px;
      padding-top: 10px;
      border-top: 1px solid #ebeef5;
      .vote-hint {
        margin-left: auto;
        font-size: 11px;
        color: #c0c4cc;
      }
    }
  }

  // ===== 回答区域 =====
  .answers-section {
    margin-top: 4px;

    .answers-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      .section-label { margin: 0; }
    }

    .answers-loading {
      text-align: center;
      padding: 20px;
      color: #909399;
      font-size: 13px;
    }

    .no-answer {
      padding: 10px 0;
    }

    .answer-list {
      .answer-item {
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        &:last-child { border-bottom: none; }

        &.adopted-answer {
          border-radius: 8px;
          padding: 12px;
          margin-bottom: 10px;
        }

        .answer-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 8px;

          .answer-user-wrap {
            display: flex;
            align-items: center;
            gap: 10px;

            .answer-avatar-text {
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
          }

          .answer-user-meta {
            display: flex;
            align-items: center;
            gap: 6px;
          }

          .answer-user {
            font-size: 13px;
            font-weight: 600;
            color: #303133;
          }

          .answer-header-right {
            display: flex;
            align-items: center;
            gap: 8px;
          }

          .answer-time {
            font-size: 11px;
            color: #c0c4cc;
          }
        }

        .answer-content {
          .result-basis {
            padding: 8px 10px;
            background: #f4f4f5;
            border-radius: 4px;
            margin-bottom: 8px;
            font-size: 13px;
            line-height: 1.7;

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
              color: #4a4a5a;
              white-space: pre-wrap;
            }

            .reference-link {
              margin-left: 6px;
              padding: 0;
            }
          }
        }

        .answer-actions {
          margin-top: 6px;
          display: flex;
          gap: 4px;
        }
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
</style>
