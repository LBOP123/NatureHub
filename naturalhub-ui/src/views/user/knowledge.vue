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
            type="text"
            size="mini"
            icon="el-icon-delete"
            class="delete-btn"
            @click.stop="deleteConversation(conv.id)"
          />
        </div>
      </div>
    </div>

    <div class="chat-area">
      <div class="chat-header">
        <h3>物种知识图谱</h3>
      </div>

      <div class="messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-state">
          <div class="empty-icon">🌿</div>
          <div class="empty-text">基于Wiki知识库，探索物种知识</div>
        </div>
        <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.type]">
          <div class="message-content" v-html="formatAnswer(msg.content)"></div>
        </div>
        <div v-if="loading" class="message-item ai">
          <div class="message-content loading">
            <span class="loading-dot"></span>
            <span class="loading-dot"></span>
            <span class="loading-dot"></span>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="quick-questions">
          <el-button size="small" @click="quickAsk('绿萝的养护方法')">绿萝养护</el-button>
          <el-button size="small" @click="quickAsk('多肉植物怎么浇水')">多肉浇水</el-button>
          <el-button size="small" @click="quickAsk('仙人掌的生长习性')">仙人掌习性</el-button>
          <el-button size="small" @click="quickAsk('蝴蝶兰的繁殖方法')">蝴蝶兰繁殖</el-button>
        </div>
        <div class="input-row">
          <el-input
            v-model="question"
            type="textarea"
            :rows="3"
            placeholder="输入关于物种的问题..."
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

export default {
  name: 'KnowledgePage',
  data() {
    return {
      question: '',
      messages: [],
      loading: false,
      conversations: [],
      currentConversationId: null
    }
  },
  mounted() {
    this.loadConversations()
  },
  methods: {
    quickAsk(question) {
      this.question = question
      this.sendQuestion()
    },

    async sendQuestion() {
      if (!this.question.trim() || this.loading) return

      const userQuestion = this.question.trim()

      if (!this.currentConversationId) {
        await this.createNewConversation(userQuestion)
      }

      this.messages.push({
        type: 'user',
        content: userQuestion
      })
      this.question = ''
      this.loading = true

      this.$nextTick(() => {
        this.scrollToBottom()
      })

      try {
        const response = await request({
          url: '/api/qa/knowledge',
          method: 'post',
          data: {
            question: userQuestion,
            conversationId: this.currentConversationId
          },
          timeout: 60000
        })

        if (response.code === 200) {
          // 处理返回的数据，可能是字符串或对象
          const answer = typeof response.data === 'string' ? response.data : (response.data?.data || response.data || '暂无回答')
          
          this.messages.push({
            type: 'ai',
            content: answer
          })
          this.loadConversations()
        } else {
          this.$message.error(response.msg || '查询失败')
          // 添加错误消息到聊天
          this.messages.push({
            type: 'ai',
            content: '抱歉，查询失败：' + (response.msg || '未知错误')
          })
        }
      } catch (error) {
        console.error('知识库查询失败:', error)
        this.$message.error('查询失败，请重试')
        // 添加错误消息到聊天
        this.messages.push({
          type: 'ai',
          content: '抱歉，知识库查询失败，请稍后重试'
        })
      } finally {
        this.loading = false
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      }
    },

    async loadConversations() {
      try {
        const response = await request({
          url: '/api/qa/conversations',
          method: 'get',
          params: { qaType: 2 }
        })
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
        const response = await request({
          url: `/api/qa/conversation/${conversationId}/messages`,
          method: 'get'
        })
        if (response.code === 200) {
          const data = response.data || []
          this.messages = data.map(item => [
            { type: 'user', content: item.question },
            { type: 'ai', content: item.answer }
          ]).flat()
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
          data: {
            title: firstQuestion.substring(0, 20) + (firstQuestion.length > 20 ? '...' : ''),
            qaType: 2
          }
        })
        if (response.code === 200) {
          this.currentConversationId = response.data
          await this.loadConversations()
        }
      } catch (error) {
        console.error('创建对话失败', error)
        this.$message.error('创建对话失败，请检查后端服务')
      }
    },

    switchConversation(conversationId) {
      this.currentConversationId = conversationId
      this.loadConversationMessages(conversationId)
    },

    async deleteConversation(conversationId) {
      try {
        await this.$confirm('确定删除这个对话吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        const response = await request({
          url: `/api/qa/conversation/${conversationId}`,
          method: 'delete'
        })
        if (response.code === 200) {
          this.$message.success('删除成功')
          if (this.currentConversationId === conversationId) {
            this.newConversation()
          }
          this.loadConversations()
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },

    formatAnswer(content) {
      if (!content) return ''
      return content
        .replace(/<[^>]+>/g, '')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/【(.*?)】/g, '<strong style="color: #67C23A;">$1</strong>')
        .replace(/\n/g, '<br>')
    },

    scrollToBottom() {
      const container = this.$refs.messagesContainer
      if (container) {
        container.scrollTop = container.scrollHeight
      }
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

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e5e5e5;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.empty-conversations {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}

.conversation-item {
  padding: 12px;
  margin-bottom: 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;

  &:hover {
    background: #f5f5f5;

    .delete-btn {
      display: block;
    }
  }

  &.active {
    background: #e6f7ff;
  }
}

.conversation-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-tag {
  margin-bottom: 4px;
}

.conversation-time {
  font-size: 12px;
  color: #999;
}

.delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  display: none;
  color: #999;

  &:hover {
    color: #f56c6c;
  }
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e5e5e5;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;

  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 14px;
  }
}

.message-item {
  margin-bottom: 16px;
  display: flex;

  &.user {
    justify-content: flex-end;

    .message-content {
      background: #67C23A;
      color: #fff;
    }
  }

  &.ai {
    justify-content: flex-start;

    .message-content {
      background: #f5f5f5;
      color: #333;
    }
  }
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.6;
  font-size: 14px;
  word-break: break-word;

  ::v-deep strong {
    font-weight: 600;
  }

  &.loading {
    display: flex;
    gap: 4px;
    padding: 16px;
  }
}

.loading-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #999;
  animation: loading 1.4s infinite;

  &:nth-child(2) {
    animation-delay: 0.2s;
  }

  &:nth-child(3) {
    animation-delay: 0.4s;
  }
}

@keyframes loading {
  0%, 60%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  30% {
    opacity: 1;
    transform: scale(1);
  }
}

.input-area {
  padding: 16px 24px;
  border-top: 1px solid #e5e5e5;
  background: #fff;

  .quick-questions {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
    flex-wrap: wrap;
  }

  .input-row {
    display: flex;
    gap: 12px;
  }

  ::v-deep .el-textarea {
    flex: 1;
  }

  .el-button {
    align-self: flex-end;
  }
}
</style>
