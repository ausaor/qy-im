<template>
  <view class="page chat-system" id="chatBox" :chatidx="chatIdx">
    <nav-bar back>{{ title }}</nav-bar>
    <view class="chat-msg" :style="{height: chatMainHeight+'px'}">
      <scroll-view ref="messagesContainer" class="scroll-box" scroll-y="true" upper-threshold="200" @scroll="onScroll"
                   @scrolltoupper="onScrollToTop" @scrolltolower="onScrollToBottom"
                   :scroll-into-view="'chat-item-' + scrollMsgIdx" :scroll-top="scrollTop">
        <view v-if="chat" class="chat-wrap">
          <view class="system-chat-item" v-for="(msgInfo, idx) in chat.messages" :key="idx">
            <view :id="'chat-item-' + idx" v-if="msgInfo.type !== 20">{{msgInfo.title}}</view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
export default {
  name: "chat-system-box",
  data() {
    return {
      chatIdx: 0,
      chat: {},
      isShowKeyBoard: false, // 键盘是否正在弹起
      scrollMsgIdx: 0, // 滚动条定位为到哪条消息
      chatMainHeight: 0, // 聊天窗口高度
      keyboardHeight: 290, // 键盘高度
      windowHeight: 1000, // 窗口高度
      initHeight: 1000, // h5初始高度
      scrollTop: 0, // 用于ios h5定位滚动条
      showMinIdx: 0, // 下标小于showMinIdx的消息不显示，否则可能很卡
      scrollViewHeight: 0, // 滚动条总长度
      currentScrollTop: 0,          // 当前滚动位置
      lastScrollTop: 0,      // 上一次滚动位置
      scrollDirection: null, // 滚动方向：'up' 或 'down'
      timer: null, // 防抖计时器
      newMessageSize: 0, // 滚动条不在底部时新的消息数量
      isInBottom: true, // 滚动条是否在底部
    }
  },
  onLoad(options) {
    this.chatIdx = options.chatIdx;

    this.chat = this.chatStore.chats[options.chatIdx];
    console.log(this.chat);
    // 初始状态只显示20条消息
    let size = this.messageSize;
    this.showMinIdx = size > 20 ? size - 20 : 0;
    // 激活当前会话
    this.chatStore.activeChat(options.chatIdx);
    // 监听键盘高度
    this.listenKeyBoard();
    // 计算聊天窗口高度
    this.windowHeight = uni.getSystemInfoSync().windowHeight;
    this.screenHeight = uni.getSystemInfoSync().screenHeight;
    this.reCalChatMainHeight();

    this.$nextTick(() => {
      // 上面获取的windowHeight可能不准，重新计算一次聊天窗口高度
      this.windowHeight = uni.getSystemInfoSync().windowHeight;
      this.reCalChatMainHeight();
      this.scrollToBottom();
      // #ifdef H5
      this.initHeight = window.innerHeight;
      // 兼容ios的h5:禁止页面滚动
      const chatBox = document.getElementById('chatBox')
      chatBox.addEventListener('touchmove', e => {
        e.preventDefault()
      }, { passive: false });
      // #endif
    });
  },
  onUnload() {
    this.unListenKeyboard();
  },
  methods: {
    onScroll(e) {
      // 记录当前滚动条高度
      this.scrollViewHeight = e.detail.scrollHeight;
      //console.log('scrollViewHeight', this.scrollViewHeight);

      // 清除之前的计时器
      if (this.timer) clearTimeout(this.timer)

      // 设置防抖（100ms内只执行一次）
      this.timer = setTimeout(() => {
        const currentScrollTop = e.detail.scrollTop

        // 判断滚动方向
        if (currentScrollTop < this.lastScrollTop) {
          this.scrollDirection = 'up'
          this.isInBottom = false;
          console.log('向上滚动')
          // 这里可以添加向上滚动的自定义逻辑
        } else if (currentScrollTop > this.lastScrollTop) {
          this.scrollDirection = 'down'
          console.log('向下滚动')
        }
        // 更新滚动位置记录
        this.lastScrollTop = currentScrollTop
        this.currentScrollTop = currentScrollTop
      }, 100)
    },
    onScrollToTop() {
      console.log("onScrollToTop")
      if (this.showMinIdx > 0) {
        //  #ifndef H5
        // 防止滚动条定格在顶部，不能一直往上滚
        this.scrollToMsgIdx(this.showMinIdx);
        // #endif

        // #ifdef H5
        // 防止滚动条定格在顶部，不能一直往上滚，h5采用scroll-top定位
        this.holdingScrollBar(this.scrollViewHeight);
        // #endif

        // 多展示20条信息
        this.showMinIdx = this.showMinIdx > 20 ? this.showMinIdx - 20 : 0;
      }
      // 清除底部标识
      if (this.showMinIdx > 0) {
        this.isInBottom = false;
      }
    },
    holdingScrollBar(scrollViewHeight) {
      // 内容高度
      const query = uni.createSelectorQuery().in(this);
      setTimeout(() => {
        query.select('.chat-wrap').boundingClientRect();
        query.exec(data => {
          this.scrollTop = data[0].height - scrollViewHeight;
          if(this.scrollTop < 10){
            // 未渲染完成，重试一次
            this.holdingScrollBar();
          }
        });
      }, 50)
    },
    onClickToBottom() {
      this.scrollToBottom();
      // 有些设备滚到底部时会莫名触发滚动到顶部的事件
      // 所以这里延迟100s保证能准确设置底部标志
      setTimeout(() => {
        this.isInBottom = true;
        this.newMessageSize = 0;
      }, 100)
    },
    onScrollToBottom(e) {
      console.log("onScrollToBottom")
      // 设置底部标识
      this.isInBottom = true;
      this.newMessageSize = 0;
    },
    reCalChatMainHeight() {
      setTimeout(() => {
        let h = this.windowHeight;
        // 减去标题栏高度
        h -= 50;
        // 减去键盘高度
        if (this.isShowKeyBoard) {
          console.log("减去键盘高度:", this.keyboardHeight)
          h -= this.keyboardHeight;
          this.scrollToBottom();
        }
        // #ifndef H5
        // h5需要减去状态栏高度
        h -= uni.getSystemInfoSync().statusBarHeight;
        // #endif
        this.chatMainHeight = h;
        console.log("窗口高度:", this.chatMainHeight)
        if (this.isShowKeyBoard) {
          this.scrollToBottom();
        }
        // ios浏览器键盘把页面顶起后，页面长度不会变化，这里把页面拉到顶部适配一下
        // #ifdef H5
        if (uni.getSystemInfoSync().platform == 'ios') {
          // 不同手机需要的延时时间不一致，采用分段延时的方式处理
          const delays = [50, 100, 500];
          delays.forEach((delay) => {
            setTimeout(() => {
              uni.pageScrollTo({
                scrollTop: 0,
                duration: 10
              });
            }, delay);
          })
        }
        // #endif
      }, 30)
    },
    listenKeyBoard() {
      // #ifdef H5
      if (navigator.platform == "Win32") {
        // 电脑端不需要弹出键盘
        console.log("navigator.platform:", navigator.platform)
        return;
      }
      if (uni.getSystemInfoSync().platform == 'ios') {
        // ios h5实现键盘监听
        window.addEventListener('focusin', this.focusInListener);
        window.addEventListener('focusout', this.focusOutListener);
        // 监听键盘高度，ios13以上开始支持
        if (window.visualViewport) {
          window.visualViewport.addEventListener('resize', this.resizeListener);
        }
      } else {
        // 安卓h5实现键盘监听
        window.addEventListener('resize', this.resizeListener);
      }
      // #endif
      // #ifndef H5
      // app实现键盘监听
      uni.onKeyboardHeightChange(this.keyBoardListener);
      // #endif
    },
    unListenKeyboard() {
      // #ifdef H5
      window.removeEventListener('resize', this.resizeListener);
      window.removeEventListener('focusin', this.focusInListener);
      window.removeEventListener('focusout', this.focusOutListener);
      // #endif
      // #ifndef H5
      uni.offKeyboardHeightChange(this.keyBoardListener);
      // #endif
    },
    keyBoardListener(res) {
      this.isShowKeyBoard = res.height > 0;
      if (this.isShowKeyBoard) {
        this.keyboardHeight = res.height; // 获取并保存键盘高度
      }
      this.reCalChatMainHeight();
    },
    scrollToBottom() {
      let size = this.messageSize;
      if (size > 0) {
        this.scrollToMsgIdx(size - 1);
      }
    },
    scrollToMsgIdx(idx) {
      // 如果scrollMsgIdx值没变化，滚动条不会移动
      if (idx == this.scrollMsgIdx && idx > 0) {
        this.$nextTick(() => {
          // 先滚动到上一条
          this.scrollMsgIdx = idx - 1;
          // 再滚动目标位置
          this.scrollToMsgIdx(idx);
        });
        return;
      }
      this.$nextTick(() => {
        this.scrollMsgIdx = idx;
      });
    },
    resizeListener() {
      let keyboardHeight = this.initHeight - window.innerHeight;
      // 兼容部分ios浏览器
      if (window.visualViewport && uni.getSystemInfoSync().platform == 'ios') {
        keyboardHeight = this.initHeight - window.visualViewport.height;
      }
      console.log("resizeListener:", window.visualViewport.height)
      this.isShowKeyBoard = keyboardHeight > 150;
      if (this.isShowKeyBoard) {
        this.keyboardHeight = keyboardHeight;
      }
      this.reCalChatMainHeight();
    },
    focusInListener() {
      console.log("focusInListener")
      this.isShowKeyBoard = true;
      this.reCalChatMainHeight();
    },
    focusOutListener() {
      console.log("focusOutListener")
      this.isShowKeyBoard = false;
      this.reCalChatMainHeight();
    },
  },
  computed: {
    mine() {
      return this.userStore.userInfo;
    },
    title() {
      if (!this.chat) {
        return "";
      }
      return this.chat.showName;
    },
    messageSize() {
      if (!this.chat || !this.chat.messages) {
        return 0;
      }
      return this.chat.messages.length;
    },
  }
}
</script>

<style scoped lang="scss">
.chat-system {
  .chat-msg {
    flex: 1;
    overflow: hidden;
    position: relative;
    background-color: #f6f6f6;
    padding: .625rem;

    .scroll-box {
      height: 100%;
    }
  }
}
</style>