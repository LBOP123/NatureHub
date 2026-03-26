<template>
  <div class="knowledge-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <el-button type="success" size="small" icon="el-icon-plus" @click="newConversation" style="width: 100%">
          新建对话
        </el-button>
      </div>
      <div class="conversation-list">
        <div v-if="conversations.length === 0" class="empty-conversations">
          <p>暂无对话</p>
        </div>
        <div
          v-for="conv in conversations"
          :key="conv.id"
          :class="['conversation-item', { active: currentConversationId === conv.id }]"
          @click="switchConversation(conv.id)"
        >
          <div class="conversation-title">{{ conv.title }}</div>
          <div v-if="conv.relatedSpecies" class="conversation-tag">
            <el-tag size="mini" type="success">{{ conv.relatedSpecies }}</el-tag>
          </div>
          <div class="conversation-time">{{ conv.updateTime }}</div>
          <el-button
            type="text" size="mini" icon="el-icon-delete" class="delete-btn"
            @click.stop="deleteConversation(conv.id)"
          />
        </div>
      </div>
    </div>

    <div class="chat-area">
      <div class="chat-header">
        <h3>自然知识库</h3>
      </div>

      <div class="messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-state">
          <div class="empty-icon">🌿</div>
          <div class="empty-text">基于Wiki知识库，探索物种知识</div>
        </div>

        <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.type]">
          <div class="message-content">
            <!-- 普通消息内容 -->
            <div v-if="!msg.isVideoLink" v-html="msg.html"></div>

            <!-- 视频引导区（仅普通 AI 消息且未关闭） -->
            <template v-if="msg.type === 'ai' && !msg.isVideoLink && !msg.videoDismissed">
              <div class="video-guide-section">
                <div class="guide-divider"></div>
                <div class="guide-content">
                  <div class="guide-title">💡 想更直观地了解这个知识点吗？</div>
                  <p>我可以为你生成一段讲解视频，用画面和声音帮你快速理解，需要吗？</p>
                  <div class="guide-actions">
                    <el-button type="success" size="small" @click="generateVideo(msg)">是的，生成视频</el-button>
                    <el-button size="small" @click="dismissGuide(msg)">暂不需要</el-button>
                  </div>
                </div>
              </div>
            </template>

            <!-- 视频链接消息（isVideoLink=true 时） -->
            <template v-if="msg.isVideoLink">
              <div class="video-link-section">
                <div v-if="msg.videoStatus === '1' || msg.videoStatus === '2'" class="video-generating">
                  <i class="el-icon-loading"></i>
                  <span>视频生成中，请耐心等待（约 3-5 分钟）…</span>
                  <el-button type="text" size="mini" @click="refreshVideoStatus(msg)">手动刷新</el-button>
                </div>
                <div v-else-if="msg.videoStatus === '3' && msg.videoUrl" class="video-link-result">
                  <span>🎬 视频已生成：</span>
                  <a :href="msg.videoUrl" target="_blank" class="video-link">点击查看视频链接</a>
                </div>
                <div v-else-if="msg.videoStatus === '4'" class="video-failed">
                  <i class="el-icon-circle-close"></i>
                  <span>视频生成失败：{{ msg.videoError || '请稍后重试' }}</span>
                  <el-button type="text" size="mini" @click="retryVideoMsg(msg)">重试</el-button>
                </div>
              </div>
            </template>
          </div>
        </div>

        <div v-if="loading" class="message-item ai">
          <div class="message-content loading-content">
            <span class="loading-dot"></span>
            <span class="loading-dot"></span>
            <span class="loading-dot"></span>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="quick-questions">
          <el-button size="small" @click="quickAsk('绿萝的养护方法')">绿萝养护</el-button>
          <el-button size="small" @click="quickAsk('猫咪的常见习性')">猫咪的常见习性</el-button>
          <el-button size="small" @click="quickAsk('蜜蜂的作用')">蜜蜂的作用</el-button>
          <el-button size="small" @click="quickAsk('食品发酵的生物小知识')">食品发酵的生物小知识</el-button>
        </div>
        <div class="input-row">
          <el-input
            v-model="question" type="textarea" :rows="3"
            placeholder="探寻自然奥秘，在此输入你的问题…"
            @keydown.enter.ctrl="sendQuestion"
          />
          <el-button type="success" @click="sendQuestion" :loading="loading" :disabled="!question.trim()">
            查询
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'
import { marked } from 'marked'
import { createVideoTask, getVideoTask } from '@/api/user/video'

export default {
  name: 'KnowledgePage',
  data() {
    return {
      question: '',
      messages: [],
      loading: false,
      conversations: [],
      currentConversationId: null,
      // 轮询定时器 map：taskId -> timer
      pollingTimers: {}
    }
  },
  mounted() {
    this.loadConversations()
  },
  beforeDestroy() {
    // 清理所有轮询定时器
    Object.values(this.pollingTimers).forEach(t => clearInterval(t))
  },
  methods: {
    quickAsk(q) {
      this.question = q
      this.sendQuestion()
    },

    async sendQuestion() {
      if (!this.question.trim() || this.loading) return
      const userQuestion = this.question.trim()
      if (!this.currentConversationId) {
        await this.createNewConversation(userQuestion)
      }
      this.messages.push({ type: 'user', html: userQuestion, videoDismissed: true })
      this.question = ''
      this.loading = true
      this.$nextTick(() => this.scrollToBottom())

      try {
        const response = await request({
          url: '/api/qa/knowledge',
          method: 'post',
          data: { question: userQuestion, conversationId: this.currentConversationId },
          timeout: 60000
        })
        if (response.code === 200) {
          const answer = typeof response.data === 'string'
            ? response.data
            : (response.data?.data || response.data || '暂无回答')
          // 保存原始文本，用于视频生成 prompt
          this.messages.push({
            type: 'ai',
            html: this.formatAnswer(answer),
            rawContent: answer,          // 原始文本，传给扣子
            questionTitle: userQuestion, // 问题标题，存 remark
            videoDismissed: false,
            videoTaskId: null,
            videoStatus: null,
            videoUrl: null,
            videoError: null
          })
          this.loadConversations()
        } else {
          this.$message.error(response.msg || '查询失败')
          this.messages.push({ type: 'ai', html: '抱歉，查询失败：' + (response.msg || '未知错误'), videoDismissed: true })
        }
      } catch (error) {
        console.error('知识库查询失败:', error)
        this.$message.error('查询失败，请重试')
        this.messages.push({ type: 'ai', html: '抱歉，知识库查询失败，请稍后重试', videoDismissed: true })
      } finally {
        this.loading = false
        this.$nextTick(() => this.scrollToBottom())
      }
    },

    // ============ 视频生成 ============

    async generateVideo(sourceMsg) {
      // 1. 隐藏引导区
      this.$set(sourceMsg, 'videoDismissed', true)

      // 2. 自动追加一条用户消息
      this.messages.push({ type: 'user', html: '帮我生成一段讲解视频', videoDismissed: true, isVideoLink: false })

      // 3. 追加 AI 视频链接占位消息
      const videoMsg = {
        type: 'ai',
        isVideoLink: true,
        videoDismissed: true,
        videoStatus: '1',
        videoTaskId: null,
        videoUrl: null,
        videoError: null,
        rawContent: sourceMsg.rawContent,
        questionTitle: sourceMsg.questionTitle
      }
      this.messages.push(videoMsg)
      this.$nextTick(() => this.scrollToBottom())

      try {
        const res = await createVideoTask({
          content: sourceMsg.rawContent || sourceMsg.html.replace(/<[^>]+>/g, ''),
          remark: sourceMsg.questionTitle || '',
          conversationId: this.currentConversationId
        })
        if (res.code === 200 && res.data) {
          const taskId = res.data.id
          this.$set(videoMsg, 'videoTaskId', taskId)
          this.$set(videoMsg, 'videoStatus', '2')
          this.startPolling(videoMsg, taskId)
        } else {
          this.$set(videoMsg, 'videoStatus', '4')
          this.$set(videoMsg, 'videoError', res.msg || '提交失败')
          this.$message.error(res.msg || '提交失败')
        }
      } catch (e) {
        this.$set(videoMsg, 'videoStatus', '4')
        this.$set(videoMsg, 'videoError', e.message || '网络错误')
        this.$message.error('提交失败：' + e.message)
      }
    },

    retryVideoMsg(videoMsg) {
      this.$set(videoMsg, 'videoStatus', '1')
      this.$set(videoMsg, 'videoError', null)
      this.$set(videoMsg, 'videoUrl', null)
      this.generateVideo({ rawContent: videoMsg.rawContent, questionTitle: videoMsg.questionTitle, videoDismissed: true })
    },
    startPolling(msg, taskId) {
      // 最多轮询 20 分钟（每 8 秒一次，共 150 次）
      let count = 0
      const maxCount = 150
      const timer = setInterval(async () => {
        count++
        if (count > maxCount) {
          clearInterval(timer)
          delete this.pollingTimers[taskId]
          this.$set(msg, 'videoStatus', '4')
          this.$set(msg, 'videoError', '生成超时，请手动刷新')
          return
        }
        try {
          const res = await getVideoTask(taskId)
          if (res.code === 200 && res.data) {
            const task = res.data
            this.$set(msg, 'videoStatus', task.taskStatus)
            if (task.taskStatus === '3') {
              // 成功
              this.$set(msg, 'videoUrl', task.videoUrl)
              clearInterval(timer)
              delete this.pollingTimers[taskId]
              this.$message.success('🎬 视频生成成功！')
              this.$nextTick(() => this.scrollToBottom())
            } else if (task.taskStatus === '4') {
              // 失败
              this.$set(msg, 'videoError', task.errorMessage || '生成失败')
              clearInterval(timer)
              delete this.pollingTimers[taskId]
            }
          }
        } catch (e) {
          console.error('轮询视频状态失败', e)
          // 401 登录过期时停止轮询，避免无意义请求
          if (e && (e.message || '').includes('401')) {
            clearInterval(timer)
            delete this.pollingTimers[taskId]
            this.$set(msg, 'videoStatus', '4')
            this.$set(msg, 'videoError', '登录已过期，请重新登录后手动刷新状态')
          }
        }
      }, 8000)
      this.pollingTimers[taskId] = timer
    },

    async refreshVideoStatus(msg) {
      if (!msg.videoTaskId) return
      try {
        const res = await getVideoTask(msg.videoTaskId)
        if (res.code === 200 && res.data) {
          const task = res.data
          this.$set(msg, 'videoStatus', task.taskStatus)
          if (task.taskStatus === '3') {
            this.$set(msg, 'videoUrl', task.videoUrl)
            this.$message.success('🎬 视频生成成功！')
          } else if (task.taskStatus === '4') {
            this.$set(msg, 'videoError', task.errorMessage || '生成失败')
          } else {
            this.$message.info('仍在生成中，请稍候...')
          }
        }
      } catch (e) {
        this.$message.error('查询失败：' + e.message)
      }
    },

    dismissGuide(msg) {
      this.$set(msg, 'videoDismissed', true)
    },

    // ============ 对话管理 ============

    async loadConversations() {
      try {
        const response = await request({ url: '/api/qa/conversations', method: 'get', params: { qaType: 2 } })
        if (response.code === 200) {
          this.conversations = response.data || []
          if (this.conversations.length > 0 && !this.currentConversationId) {
            this.switchConversation(this.conversations[0].id)
          }
        }
      } catch (error) {
        console.error('加载对话列表失败', error)
        this.conversations = []
      }
    },

    async loadConversationMessages(conversationId) {
      try {
        const response = await request({ url: `/api/qa/conversation/${conversationId}/messages`, method: 'get' })
        if (response.code === 200) {
          const data = response.data || []
          const msgs = []
          data.forEach(item => {
            const isVideoRecord = item.question && item.question.startsWith('[视频生成]')
            if (isVideoRecord) {
              msgs.push({ type: 'user', html: '帮我生成一段讲解视频', videoDismissed: true, isVideoLink: false })
              const answer = item.answer || ''
              let videoStatus, videoUrl, videoError
              if (answer === '[GENERATING]') {
                // 仍在生成中
                videoStatus = '2'
                videoUrl = null
                videoError = null
              } else if (answer.startsWith('[FAILED] ')) {
                // 生成失败
                videoStatus = '4'
                videoUrl = null
                videoError = answer.replace('[FAILED] ', '')
              } else {
                // 生成成功，answer 就是视频 URL
                videoStatus = '3'
                videoUrl = answer
                videoError = null
              }
              msgs.push({
                type: 'ai',
                isVideoLink: true,
                videoDismissed: true,
                videoStatus,
                videoTaskId: null,
                videoUrl,
                videoError
              })
            } else {
              msgs.push({ type: 'user', html: item.question, videoDismissed: true, isVideoLink: false })
              msgs.push({ type: 'ai', html: this.formatAnswer(item.answer), rawContent: item.answer, videoDismissed: true, isVideoLink: false })
            }
          })
          this.messages = msgs
        }
      } catch (error) {
        console.error('加载对话消息失败', error)
        this.messages = []
      }
    },

    newConversation() {
      this.currentConversationId = null
      this.messages = []
      this.$message.success('已创建新对话，请开始提问')
    },

    async createNewConversation(firstQuestion) {
      try {
        const response = await request({
          url: '/api/qa/conversation',
          method: 'post',
          data: { title: firstQuestion.substring(0, 20) + (firstQuestion.length > 20 ? '...' : ''), qaType: 2 }
        })
        if (response.code === 200) {
          this.currentConversationId = response.data
          await this.loadConversations()
        }
      } catch (error) {
        console.error('创建对话失败', error)
        this.$message.error('创建对话失败')
      }
    },

    switchConversation(conversationId) {
      this.currentConversationId = conversationId
      this.loadConversationMessages(conversationId)
    },

    async deleteConversation(conversationId) {
      try {
        await this.$confirm('确定删除这个对话吗？', '提示', {
          confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
        })
        const response = await request({ url: `/api/qa/conversation/${conversationId}`, method: 'delete' })
        if (response.code === 200) {
          this.$message.success('删除成功')
          if (this.currentConversationId === conversationId) this.newConversation()
          this.loadConversations()
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('删除失败')
      }
    },

    formatAnswer(content) {
      if (!content) return ''
      try {
        return marked.parse(content)
      } catch (error) {
        return content.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>').replace(/\n/g, '<br>')
      }
    },

    scrollToBottom() {
      const container = this.$refs.messagesContainer
      if (container) container.scrollTop = container.scrollHeight
    }
  }
}
</script>

<style lang="scss" scoped>
.knowledge-container {
  display: flex;
  height: 85vh;
  max-width: 1400px;
  margin: 0 auto;
  background: #f5f5f5;
}

.sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e5e5e5;
  display: flex;
  flex-direction: column;
}
.sidebar-header { padding: 16px; border-bottom: 1px solid #e5e5e5; }
.conversation-list { flex: 1; overflow-y: auto; padding: 8px; }
.empty-conversations { text-align: center; padding: 40px 20px; color: #999; font-size: 14px; }
.conversation-item {
  padding: 12px; margin-bottom: 4px; border-radius: 8px;
  cursor: pointer; transition: background 0.2s; position: relative;
  &:hover { background: #f5f5f5; .delete-btn { display: block; } }
  &.active { background: #e6f7ff; }
}
.conversation-title { font-size: 14px; color: #333; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.conversation-tag { margin-bottom: 4px; }
.conversation-time { font-size: 12px; color: #999; }
.delete-btn { position: absolute; top: 8px; right: 8px; display: none; color: #999; &:hover { color: #f56c6c; } }

.chat-area { flex: 1; display: flex; flex-direction: column; background: #fff; }
.chat-header { padding: 16px 24px; border-bottom: 1px solid #e5e5e5; h3 { margin: 0; font-size: 16px; font-weight: 500; color: #333; } }

.messages { flex: 1; overflow-y: auto; padding: 24px; }
.empty-state { text-align: center; padding: 60px 20px; color: #999; .empty-icon { font-size: 48px; margin-bottom: 16px; } }

.message-item {
  margin-bottom: 16px; display: flex;
  &.user { justify-content: flex-end; .message-content { background: #67C23A; color: #fff; } }
  &.ai   { justify-content: flex-start; .message-content { background: #f5f5f5; color: #333; } }
}

.message-content {
  max-width: 75%; padding: 12px 16px; border-radius: 8px; line-height: 1.6; font-size: 14px; word-break: break-word;
  &.loading { display: flex; gap: 4px; padding: 16px; }
  ::v-deep strong { font-weight: 600; }
  ::v-deep {
    h1,h2,h3,h4,h5,h6 { margin: 12px 0 8px; font-weight: 600; line-height: 1.4; }
    p { margin: 8px 0; }
    ul,ol { padding-left: 24px; }
    code { background: rgba(0,0,0,0.05); padding: 2px 6px; border-radius: 3px; }
    pre { padding: 12px; background: rgba(0,0,0,0.05); border-radius: 6px; overflow-x: auto; }
    a { color: #409EFF; }
  }
}

.loading-dot {
  width: 8px; height: 8px; border-radius: 50%; background: #999; animation: loading 1.4s infinite;
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}
@keyframes loading {
  0%,60%,100% { opacity: 0.3; transform: scale(0.8); }
  30% { opacity: 1; transform: scale(1); }
}
.loading-content { display: flex; gap: 4px; padding: 16px; background: #f5f5f5; border-radius: 8px; }

/* 视频引导区 */
.video-guide-section { margin-top: 16px; }
.guide-divider { height: 1px; background: linear-gradient(to right, transparent, #ddd, transparent); margin-bottom: 14px; }
.guide-content {
  background: linear-gradient(135deg, #f0f9ff 0%, #f5f5f5 100%);
  border-left: 3px solid #67C23A; padding: 12px 16px; border-radius: 6px;
}
.guide-title { font-size: 13px; font-weight: 600; color: #333; margin-bottom: 6px; }
.guide-content p { font-size: 13px; color: #666; line-height: 1.6; margin: 0 0 10px; }
.guide-actions { display: flex; gap: 8px; }

/* 视频链接区 */
.video-link-section {
  margin-top: 10px;
  padding: 10px 14px;
  background: #f0f9ff;
  border-left: 3px solid #409EFF;
  border-radius: 6px;
  font-size: 13px;
}
.video-generating {
  display: flex; align-items: center; gap: 8px; color: #666;
  i { color: #409EFF; animation: rotating 1.5s linear infinite; }
}
.video-link-result {
  display: flex; align-items: center; gap: 6px; color: #333;
}
.video-link {
  color: #409EFF; font-weight: 500; text-decoration: underline;
  &:hover { color: #66b1ff; }
}
.video-failed {
  display: flex; align-items: center; gap: 8px; font-size: 13px; color: #f56c6c;
  i { font-size: 16px; }
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

.input-area {
  padding: 16px 24px; border-top: 1px solid #e5e5e5; background: #fff;
  .quick-questions { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
  .input-row { display: flex; gap: 12px; }
  ::v-deep .el-textarea { flex: 1; }
  .el-button { align-self: flex-end; }
}
</style>
