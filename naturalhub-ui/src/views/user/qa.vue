<template>
  <div class="qa-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="newConversation" style="width: 100%">
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
        <h3>AI科普问答</h3>
      </div>

      <div class="messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-state">
          <div class="empty-icon">💬</div>
          <div class="empty-text">开始新的对话吧</div>
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
        <div class="input-row">
          <el-input
            v-model="question"
            type="textarea"
            :rows="3"
            placeholder="输入你的问题..."
            @keydown.enter.ctrl="sendQuestion"
          />
          <el-button type="primary" @click="sendQuestion" :loading="loading" :disabled="!question.trim()">
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'
import { marked } from 'marked'

export default {
  name: 'QaPage',
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
    async sendQuestion() {
      if (!this.question.trim() || this.loading) return

      const userQuestion = this.question.trim()

      if (!this.currentConversationId) {
        await this.createNewConversation(userQuestion)
      }

      this.messages.push({ type: 'user', content: userQuestion })
      this.question = ''
      this.loading = true

      this.$nextTick(() => this.scrollToBottom())

      try {
        const response = await request({
          url: '/api/qa/ask',
          method: 'post',
          data: {
            question: userQuestion,
            conversationId: this.currentConversationId
          },
          timeout: 60000
        })

        if (response.code === 200) {
          const answer = typeof response.data === 'string' ? response.data : (response.data?.data || response.data || '暂无回答')
          this.messages.push({ type: 'ai', content: answer })
          this.loadConversations()
        } else {
          this.$message.error(response.msg || '提问失败')
          this.messages.push({ type: 'ai', content: '抱歉，回答失败：' + (response.msg || '未知错误') })
        }
      } catch (error) {
        console.error('请求失败:', error)
        this.$message.error('请求失败，请重试')
        this.messages.push({ type: 'ai', content: '抱歉，网络请求失败，请稍后重试' })
      } finally {
        this.loading = false
        this.$nextTick(() => this.scrollToBottom())
      }
    },

    async loadConversations() {
      try {
        const response = await request({
          url: '/api/qa/conversations',
          method: 'get',
          params: { qaType: 1 }
        })
        if (response.code === 200) {
          this.conversations = response.data || []
          if (this.conversations.length > 0 && !this.currentConversationId) {
            this.switchConversation(this.conversations[0].id)
          }
        }
      } catch (error) {
        console.error('加载对话失败', error)
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
          this.$nextTick(() => this.scrollToBottom())
        }
      } catch (error) {
        console.error('加载消息失败', error)
      }
    },

    newConversation() {
      this.currentConversationId = null
      this.messages = []
      this.$message.success('已创建新对话')
    },

    async createNewConversation(firstQuestion) {
      try {
        const response = await request({
          url: '/api/qa/conversation',
          method: 'post',
          data: {
            title: firstQuestion.substring(0, 20) + (firstQuestion.length > 20 ? '...' : ''),
            qaType: 1
          }
        })
        if (response.code === 200) {
          this.currentConversationId = response.data
          this.loadConversations()
        }
      } catch (error) {
        this.$message.error('创建对话失败')
      }
    },

    switchConversation(conversationId) {
      this.currentConversationId = conversationId
      this.loadConversationMessages(conversationId)
    },

    async deleteConversation(conversationId) {
      try {
        await this.$confirm('确定删除？', '提示', { type: 'warning' })
        const res = await request({
          url: `/api/qa/conversation/${conversationId}`,
          method: 'delete'
        })
        if (res.code === 200) {
          this.$message.success('删除成功')
          if (this.currentConversationId === conversationId) {
            this.newConversation()
          }
          this.loadConversations()
        }
      } catch (e) {}
    },

    formatAnswer(content) {
      if (!content) return ''
      try {
        return marked.parse(content)
      } catch (error) {
        console.error('Markdown渲染失败:', error)
        return content
          .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
          .replace(/\n/g, '<br>')
      }
    },

    scrollToBottom() {
      const el = this.$refs.messagesContainer
      if (el) {
        setTimeout(() => {
          el.scrollTop = el.scrollHeight
        }, 50)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.qa-container {
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
    .delete-btn { display: block; }
  }
  &.active { background: #e6f7ff; }
}

.conversation-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.conversation-time { font-size:12px; color:#999; }
.delete-btn { position:absolute; top:8px; right:8px; display:none; color:#999; &:hover{color:#f56c6c;} }

.chat-area { flex:1; display:flex; flex-direction:column; background:#fff; }
.chat-header { padding:16px 24px; border-bottom:1px solid #e5e5e5; h3{margin:0; font-size:16px;} }

.messages { flex:1; overflow-y:auto; padding:24px; }
.empty-state { text-align:center; padding:60px 20px; color:#999; .empty-icon{font-size:48px; margin-bottom:16px;} }

.message-item {
  margin-bottom:16px; display:flex;
  &.user{justify-content:flex-end; .message-content{background:#1890ff; color:#fff;}}
  &.ai{justify-content:flex-start; .message-content{background:#f5f5f5; color:#333;}}
}

.message-content {
  max-width:70%; padding:12px 16px; border-radius:8px; line-height:1.6; word-break:break-word;
  &.loading{display:flex; gap:4px; padding:16px;}
  ::v-deep strong{font-weight:600;}
  ::v-deep {
    h1,h2,h3{margin:12px 0 8px; font-weight:600;}
    code{background:rgba(0,0,0,0.05); padding:2px 6px; border-radius:3px;}
    pre{background:rgba(0,0,0,0.05); padding:12px; border-radius:6px; overflow-x:auto;}
    a{color:#409EFF;}
  }
}

.loading-dot {
  width:8px; height:8px; border-radius:50%; background:#999; animation:loading 1.4s infinite;
  &:nth-child(2){animation-delay:0.2s;}
  &:nth-child(3){animation-delay:0.4s;}
}

@keyframes loading {
  0%,60%,100%{opacity:0.3; transform:scale(0.8);}
  30%{opacity:1; transform:scale(1);}
}

.input-area {
  padding:16px 24px; border-top:1px solid #e5e5e5; background:#fff;
  .input-row{display:flex; gap:12px;}
  ::v-deep .el-textarea{flex:1;}
  .el-button{align-self:flex-end;}
}
</style>
