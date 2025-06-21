<template>
  <div class="ai-chat-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2 class="title">AI Assistant</h2>
        <el-button
            type="primary"
            size="small"
            icon="el-icon-plus"
            @click="createNewChat"
            class="new-chat-btn"
        >
          新建对话
        </el-button>
      </div>

      <!-- 模型选择 -->
      <div class="model-selector">
        <el-select
            v-model="selectedModel"
            placeholder="选择模型"
            size="small"
            @change="handleModelChange"
            class="model-select"
        >
          <el-option label="DeepSeek" value="deepseek"></el-option>
          <el-option label="通义千问" value="qwen"></el-option>
          <el-option label="GPT-4" value="gpt4"></el-option>
          <el-option label="MCP模式" value="mcp"></el-option>
        </el-select>
      </div>

      <!-- 对话列表 -->
      <div class="chat-list">
        <div
            v-for="chat in chatList"
            :key="chat.id"
            :class="['chat-item', { active: currentChatId === chat.id }]"
            @click="switchChat(chat.id)"
        >
          <div class="chat-title">{{ chat.title }}</div>
          <div class="chat-time">{{ formatTime(chat.updateTime) }}</div>
          <el-button
              type="text"
              size="mini"
              icon="el-icon-delete"
              @click.stop="deleteChat(chat.id)"
              class="delete-btn"
          ></el-button>
        </div>
      </div>
    </div>

    <!-- 主对话区域 -->
    <div class="main-content">
      <div class="chat-header">
        <span class="model-badge">{{ getModelName(selectedModel) }}</span>
        <div class="connection-status">
          <i :class="['status-dot', isConnected ? 'connected' : 'disconnected']"></i>
          {{ isConnected ? '已连接' : '未连接' }}
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="message-container" ref="messageContainer">
        <div v-if="currentChat.messages.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="el-icon-chat-dot-round"></i>
          </div>
          <p class="empty-text">开始您的AI对话吧</p>
          <p class="empty-subtitle">选择一个模型，输入您的问题</p>
        </div>

        <div
            v-for="message in currentChat.messages"
            :key="message.id"
            :class="['message', message.role]"
        >
          <div class="message-avatar">
            <i :class="message.role === 'user' ? 'el-icon-user' : 'el-icon-cpu'"></i>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="role-name">{{ message.role === 'user' ? '用户' : 'AI助手' }}</span>
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            </div>
            <div
                class="message-text"
                v-html="renderMarkdown(message.content)"
            ></div>
          </div>
        </div>

        <!-- 正在输入指示器 -->
        <div v-if="isTyping" class="message assistant typing">
          <div class="message-avatar">
            <i class="el-icon-cpu"></i>
          </div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-wrapper">
          <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入您的问题..."
              @keydown.ctrl.enter="sendMessage"
              :disabled="isTyping"
              class="message-input"
          ></el-input>
          <div class="input-actions">
            <el-button
                type="primary"
                @click="sendMessage"
                :loading="isTyping"
                :disabled="!inputMessage.trim()"
                class="send-btn"
            >
              <i class="el-icon-s-promotion"></i>
              发送
            </el-button>
            <el-button
                v-if="isTyping"
                @click="stopGeneration"
                class="stop-btn"
            >
              <i class="el-icon-video-pause"></i>
              停止
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 装饰性背景元素 -->
    <div class="bg-decoration">
      <div class="floating-circle circle-1"></div>
      <div class="floating-circle circle-2"></div>
      <div class="floating-circle circle-3"></div>
      <div class="floating-circle circle-4"></div>
    </div>
  </div>
</template>

<script>
import { marked } from 'marked'
import DOMPurify from 'dompurify'

export default {
  name: 'LightAiChat',
  data() {
    return {
      selectedModel: 'deepseek',
      currentChatId: null,
      inputMessage: '',
      isTyping: false,
      isConnected: true,
      eventSource: null,
      chatList: [
        {
          id: 1,
          title: '新对话',
          updateTime: Date.now(),
          messages: []
        }
      ],
      messageIdCounter: 1
    }
  },
  computed: {
    currentChat() {
      return this.chatList.find(chat => chat.id === this.currentChatId) || { messages: [] }
    }
  },
  mounted() {
    this.currentChatId = this.chatList[0].id
    this.setupMarked()
  },
  methods: {
    setupMarked() {
      marked.setOptions({
        highlight: function(code, lang) {
          return `<pre><code class="language-${lang}">${code}</code></pre>`
        },
        breaks: true,
        gfm: true
      })
    },

    renderMarkdown(content) {
      const html = marked(content)
      return DOMPurify.sanitize(html)
    },

    createNewChat() {
      const newChat = {
        id: Date.now(),
        title: '新对话',
        updateTime: Date.now(),
        messages: []
      }
      this.chatList.unshift(newChat)
      this.currentChatId = newChat.id
    },

    switchChat(chatId) {
      this.currentChatId = chatId
      this.stopGeneration()
    },

    deleteChat(chatId) {
      if (this.chatList.length <= 1) {
        this.$message.warning('至少保留一个对话')
        return
      }

      this.chatList = this.chatList.filter(chat => chat.id !== chatId)
      if (this.currentChatId === chatId) {
        this.currentChatId = this.chatList[0].id
      }
    },

    handleModelChange() {
      this.$message.success(`已切换到 ${this.getModelName(this.selectedModel)} 模式`)
    },

    getModelName(model) {
      const modelMap = {
        deepseek: 'DeepSeek',
        qwen: '通义千问',
        gpt4: 'GPT-4',
        mcp: 'MCP模式'
      }
      return modelMap[model] || model
    },

    async sendMessage() {
      if (!this.inputMessage.trim() || this.isTyping) return

      const userMessage = {
        id: this.messageIdCounter++,
        role: 'user',
        content: this.inputMessage.trim(),
        timestamp: Date.now()
      }

      this.currentChat.messages.push(userMessage)
      this.updateChatTitle(userMessage.content)

      const messageToSend = this.inputMessage.trim()
      this.inputMessage = ''
      this.isTyping = true

      const aiMessage = {
        id: this.messageIdCounter++,
        role: 'assistant',
        content: '',
        timestamp: Date.now()
      }
      this.currentChat.messages.push(aiMessage)

      try {
        await this.streamResponse(messageToSend, aiMessage)
      } catch (error) {
        console.error('发送消息失败:', error)
        this.$message.error('发送消息失败，请重试')
        aiMessage.content += '抱歉，发生了错误，请重试。'
      } finally {
        this.isTyping = false
        this.scrollToBottom()
      }
    },

    async streamResponse(message, aiMessage) {
      // 模拟EventSource流式响应
      return new Promise((resolve, reject) => {
        //let accessToken = sessionStorage.getItem("accessToken");

        // 真实的EventSource实现示例：
        this.eventSource = new EventSource(`http://127.0.0.1:8181/chat/stream/msg/${this.currentChatId}?content=${encodeURIComponent(message)}&role=user&maxTokens=2000&temperature=0.7`)

        this.eventSource.onmessage = (event) => {
          const response = JSON.parse(event.data)
          // 获取流式响应的文本内容
          const text = response.result?.output?.text || response.results?.[0]?.output?.text || ''
          if (text) {
            aiMessage.content += text
            this.scrollToBottom();
          }
        }

        this.eventSource.onerror = (error) => {
          if (this.eventSource.readyState === EventSource.CLOSED) {
            console.log('this.eventSource.close()')
            this.eventSource.close();
            resolve();
          } else {
            console.log('this.eventSource.error.close()')
            this.eventSource.close()
            //reject(error)
            resolve();
          }
        }

        this.eventSource.addEventListener('end', () => {
          console.log('EventSource end event received')
          this.eventSource.close()
          resolve()
        })
      })
    },

    stopGeneration() {
      this.isTyping = false
      if (this.eventSource) {
        this.eventSource.close()
        this.eventSource = null
      }
    },

    updateChatTitle(firstMessage) {
      const chat = this.currentChat
      if (chat.messages.length <= 1) {
        chat.title = firstMessage.length > 20 ? firstMessage.substring(0, 20) + '...' : firstMessage
        chat.updateTime = Date.now()
      }
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messageContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },

    formatTime(timestamp) {
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
      return date.toLocaleDateString()
    }
  },

  beforeDestroy() {
    this.stopGeneration()
  }
}
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 25%, #f0f0ff 50%, #e8f5e8 75%, #f0fff0 100%);
  font-family: 'SF Pro Display', -apple-system, BlinkMacSystemFont, sans-serif;
  position: relative;
  overflow: hidden;
}

/* 装饰性背景元素 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.floating-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 200px;
  height: 200px;
  background: linear-gradient(45deg, #00bfff, #87ceeb);
  top: 10%;
  left: 5%;
  animation-delay: 0s;
}

.circle-2 {
  width: 150px;
  height: 150px;
  background: linear-gradient(45deg, #98fb98, #90ee90);
  top: 60%;
  right: 10%;
  animation-delay: 2s;
}

.circle-3 {
  width: 100px;
  height: 100px;
  background: linear-gradient(45deg, #dda0dd, #e6e6fa);
  top: 30%;
  right: 30%;
  animation-delay: 4s;
}

.circle-4 {
  width: 120px;
  height: 120px;
  background: linear-gradient(45deg, #ffd700, #ffffe0);
  bottom: 20%;
  left: 20%;
  animation-delay: 1s;
}

/* 侧边栏样式 */
.sidebar {
  width: 320px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(135, 206, 235, 0.3);
  display: flex;
  flex-direction: column;
  box-shadow: 0 0 30px rgba(135, 206, 235, 0.2);
  z-index: 10;
  position: relative;
}

.sidebar-header {
  padding: 25px;
  border-bottom: 1px solid rgba(135, 206, 235, 0.2);
  background: linear-gradient(135deg, rgba(240, 248, 255, 0.9), rgba(230, 243, 255, 0.9));
}

.title {
  color: #1e90ff;
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 20px 0;
  text-shadow: 0 2px 10px rgba(30, 144, 255, 0.3);
  background: linear-gradient(45deg, #1e90ff, #00bfff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.new-chat-btn {
  width: 100%;
  background: linear-gradient(45deg, #87ceeb, #00bfff);
  border: none;
  border-radius: 12px;
  color: #ffffff;
  font-weight: 600;
  padding: 12px;
  box-shadow: 0 4px 15px rgba(135, 206, 235, 0.4);
  transition: all 0.3s ease;
}

.new-chat-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(135, 206, 235, 0.6);
}

.model-selector {
  padding: 20px 25px;
  border-bottom: 1px solid rgba(135, 206, 235, 0.2);
  background: rgba(248, 248, 255, 0.6);
}

.model-select {
  width: 100%;
}

.model-select .el-input__inner {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(135, 206, 235, 0.4);
  border-radius: 8px;
  color: #4682b4;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
}

.chat-item {
  padding: 18px;
  margin-bottom: 12px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(135, 206, 235, 0.3);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  backdrop-filter: blur(10px);
}

.chat-item:hover {
  background: rgba(240, 248, 255, 0.9);
  border-color: rgba(30, 144, 255, 0.5);
  transform: translateX(8px);
  box-shadow: 0 4px 20px rgba(135, 206, 235, 0.3);
}

.chat-item.active {
  background: linear-gradient(135deg, rgba(135, 206, 235, 0.2), rgba(173, 216, 230, 0.2));
  border-color: #00bfff;
  box-shadow: 0 4px 25px rgba(0, 191, 255, 0.3);
}

.chat-title {
  color: #4682b4;
  font-weight: 600;
  margin-bottom: 8px;
  font-size: 15px;
}

.chat-time {
  color: #87ceeb;
  font-size: 12px;
}

.delete-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  color: #ff6b9d;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.chat-item:hover .delete-btn {
  opacity: 1;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  z-index: 10;
  position: relative;
}

.chat-header {
  padding: 25px;
  border-bottom: 1px solid rgba(135, 206, 235, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, rgba(248, 248, 255, 0.8), rgba(240, 248, 255, 0.8));
  backdrop-filter: blur(15px);
}

.model-badge {
  background: linear-gradient(45deg, #87ceeb, #00bfff);
  color: #ffffff;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 2px 10px rgba(135, 206, 235, 0.4);
}

.connection-status {
  color: #4682b4;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.status-dot.connected {
  background: #90ee90;
  box-shadow: 0 0 15px rgba(144, 238, 144, 0.6);
}

.status-dot.disconnected {
  background: #ff6b9d;
  box-shadow: 0 0 15px rgba(255, 107, 157, 0.6);
}

/* 消息容器 */
.message-container {
  flex: 1;
  overflow-y: auto;
  padding: 25px;
  background: rgba(255, 255, 255, 0.1);
}

.empty-state {
  text-align: center;
  margin-top: 120px;
}

.empty-icon {
  font-size: 80px;
  color: #87ceeb;
  margin-bottom: 25px;
  opacity: 0.7;
}

.empty-text {
  color: #4682b4;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 10px;
}

.empty-subtitle {
  color: #87ceeb;
  font-size: 16px;
}

.message {
  display: flex;
  margin-bottom: 25px;
  animation: fadeInUp 0.4s ease;
}

.message.user {
  justify-content: flex-end;
}

.message-avatar {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 15px;
  font-size: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.message.user .message-avatar {
  background: linear-gradient(45deg, #87ceeb, #00bfff);
  color: #ffffff;
}

.message.assistant .message-avatar {
  background: linear-gradient(45deg, #98fb98, #90ee90);
  color: #ffffff;
}

.message-content {
  max-width: 70%;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(135, 206, 235, 0.3);
  border-radius: 16px;
  padding: 20px;
  position: relative;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(135, 206, 235, 0.2);
}

.message.user .message-content {
  background: linear-gradient(135deg, rgba(135, 206, 235, 0.15), rgba(173, 216, 230, 0.15));
  border-color: rgba(0, 191, 255, 0.3);
}

.message.assistant .message-content {
  background: linear-gradient(135deg, rgba(152, 251, 152, 0.15), rgba(144, 238, 144, 0.15));
  border-color: rgba(144, 238, 144, 0.3);
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 13px;
}

.role-name {
  color: #4682b4;
  font-weight: 600;
}

.message-time {
  color: #87ceeb;
}

.message-text {
  color: #2f4f4f;
  line-height: 1.7;
  font-size: 15px;
  text-align: left;
  padding: 0 10px;
}

/* Markdown样式 */
.message-text h1, .message-text h2, .message-text h3 {
  color: #4682b4;
  margin: 20px 0 15px 0;
}

.message-text code {
  background: rgba(135, 206, 235, 0.2);
  padding: 3px 8px;
  border-radius: 6px;
  color: #4682b4;
  font-family: 'Monaco', 'Consolas', monospace;
}

.message-text pre {
  background: rgba(248, 248, 255, 0.8);
  padding: 20px;
  border-radius: 12px;
  overflow-x: auto;
  border-left: 4px solid #87ceeb;
  margin: 15px 0;
}

.message-text blockquote {
  border-left: 4px solid #87ceeb;
  padding-left: 20px;
  margin: 20px 0;
  color: #4682b4;
  background: rgba(240, 248, 255, 0.5);
  padding: 15px 20px;
  border-radius: 8px;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  gap: 6px;
  padding: 10px 0;
}

.typing-indicator span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #87ceeb;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

/* 输入区域 */
.input-area {
  padding: 25px;
  border-top: 1px solid rgba(135, 206, 235, 0.3);
  background: linear-gradient(135deg, rgba(248, 248, 255, 0.9), rgba(240, 248, 255, 0.9));
  backdrop-filter: blur(15px);
}

.input-wrapper {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 25px rgba(135, 206, 235, 0.2);
  border: 1px solid rgba(135, 206, 235, 0.3);
}

.message-input {
  margin-bottom: 15px;
}

.message-input .el-textarea__inner {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(135, 206, 235, 0.3);
  color: #2f4f4f;
  border-radius: 12px;
  padding: 15px;
  font-size: 15px;
  line-height: 1.6;
  resize: none;
}

.message-input .el-textarea__inner:focus {
  border-color: #00bfff;
  box-shadow: 0 0 20px rgba(0, 191, 255, 0.3);
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.send-btn {
  background: linear-gradient(45deg, #87ceeb, #00bfff);
  border: none;
  color: #ffffff;
  font-weight: 600;
  border-radius: 10px;
  padding: 12px 24px;
  box-shadow: 0 4px 15px rgba(135, 206, 235, 0.4);
  transition: all 0.3s ease;
}

.send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(135, 206, 235, 0.6);
}

.stop-btn {
  background: linear-gradient(45deg, #ff6b9d, #ff8fab);
  border: none;
  color: #ffffff;
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(255, 107, 157, 0.4);
}

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-15px);
    opacity: 1;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(240, 248, 255, 0.5);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(45deg, #87ceeb, #00bfff);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(45deg, #00bfff, #1e90ff);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 280px;
  }

  .message-content {
    max-width: 85%;
  }

  .input-area {
    padding: 20px;
  }
}

/* Element UI 组件样式覆盖 */
.el-select-dropdown {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(135, 206, 235, 0.3);
}

.el-select-dropdown__item {
  color: #4682b4;
}

.el-select-dropdown__item:hover {
  background: rgba(135, 206, 235, 0.1);
}

.el-message {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(135, 206, 235, 0.3);
  color: #4682b4;
}
</style>