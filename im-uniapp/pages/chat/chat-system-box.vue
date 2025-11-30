<template>
  <view class="page chat-system" id="chatBox" :chatidx="chatIdx">
    <nav-bar back :theme-index="15">{{ title }}</nav-bar>
    <view class="chat-msg" :style="{height: chatMainHeight+'px'}">
      <scroll-view ref="messagesContainer" class="scroll-box" scroll-y="true" upper-threshold="200" @scroll="onScroll"
                   @scrolltoupper="onScrollToTop" @scrolltolower="onScrollToBottom"
                   :scroll-into-view="'chat-item-' + scrollMsgIdx" :scroll-top="scrollTop">
        <view v-if="chat" class="chat-wrap">
          <view class="system-chat-item" v-for="(msgInfo, idx) in chat.messages" :key="idx">
            <view :id="'chat-item-' + idx" v-if="msgInfo.type !== 20">
              <view class="chat-msg-tip">{{$date.toTimeText(msgInfo.sendTime)}}</view>
              <view class="message-box">
                <view class="title">{{msgInfo.title}}</view>
                <view class="message-content" v-if="msgInfo.type === 0" :style="bgStyle(msgInfo)">
                  {{msgInfo.content}}
                </view>
                <view class="message-content" v-if="msgInfo.type === 9" :style="bgStyle(msgInfo)"></view>
                <view class="image-content" v-if="msgInfo.type === 1">
                  <swiper class="media-swiper" :indicator-dots="true">
                    <swiper-item v-for="(item, index) in JSON.parse(msgInfo.content)" :key="index" class="media-item">
                      <image :src="item.url" mode="aspectFill" class="media-content cursor-pointer" @click="previewImage([item.url], 0)" />
                    </swiper-item>
                  </swiper>
                </view>
                <view v-if="msgInfo.type === 3" class="audio-content">
                  <music-player class="music-item"
                                ref="musicPlayerRef"
                                :audio-url="JSON.parse(msgInfo.content)[0].url"
                                :cover-img-url="msgInfo.coverUrl"
                                :audio-name="JSON.parse(msgInfo.content)[0].originalName"
                                :audio-duration="JSON.parse(msgInfo.content)[0].duration"></music-player>
                </view>
                <view class="video-content" v-if="msgInfo.type === 4">
                  <image class="video-cover" :src="JSON.parse(msgInfo.content)[0].coverUrl"
                         mode="aspectFill"/>
                  <text class="play-icon iconfont icon-play" @click.stop="onPlayVideo(JSON.parse(msgInfo.content)[0].url, JSON.parse(msgInfo.content)[0].coverUrl)"></text>
                </view>
                <view class="intro" v-if="msgInfo.intro">{{msgInfo.intro}}</view>
                <view class="bottom-bar" v-if="msgInfo.type === 9" @click.stop="viewDetail(msgInfo)">查看详情</view>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
    <!-- 视频弹窗组件 -->
    <video-play
        v-if="viewVideo"
        :video-url="videoSrc"
        :cover-url="videoCoverImage"
        :visible.sync="viewVideo"
        @close="handleVideoClose"
        @error="handleVideoError"
    ></video-play>
  </view>
</template>

<script>
import VideoPlay from "../../components/video-play/video-play.vue";
import MusicPlayer  from "../../components/music-player/music-player.vue";

export default {
  name: "chat-system-box",
  components: {VideoPlay, MusicPlayer},
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
      viewVideo: false,
      videoSrc: '',
      videoCoverImage: '',
    }
  },
  onLoad(options) {
    this.chatIdx = options.chatIdx;

    this.chat = this.chatStore.chats[options.chatIdx];
    console.log(this.chat);

    // 消息已读
    this.readedMessage();
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
    this.clearAllAudio();
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
    onPlayVideo(url, coverImageUrl) {
      this.videoSrc = url;
      this.videoCoverImage = coverImageUrl;
      this.viewVideo = true;
    },
    // 视频弹窗关闭回调
    handleVideoClose() {
      console.log('Video popup closed');
      this.videoSrc = "";
      this.videoCoverImage = "";
      this.viewVideo = false;
    },
    // 视频错误回调
    handleVideoError(e) {
      console.error('Video error in parent component:', e)
      this.videoSrc = "";
      this.videoCoverImage = "";
      this.viewVideo = false;
    },
    previewImage(images, current) {
      uni.previewImage({
        urls: images,
        current: images[current]
      });
    },
    clearAllAudio() {
      // 1. 获取所有组件实例（数组形式）
      const components = this.$refs.musicPlayerRef

      // 2. 遍历数组，调用每个组件的方法
      if (components) {
        // 处理单个组件的情况（确保是数组）
        const componentList = Array.isArray(components) ? components : [components]

        componentList.forEach(component => {
          // 调用组件内部的方法
          component.clearAudio()
        })
      }
    },
    readedMessage() {
      if (this.chat.unreadCount === 0) {
        return;
      }
      this.$http({
        url: `/message/system/readed?pusherId=${this.chat.targetId}`,
        method: 'PUT'
      }).then(() => {
        this.chatStore.resetUnreadCount(this.chat)
        this.scrollToBottom();
      })
    },
    viewDetail(msgInfo) {
      uni.navigateTo({
        url: `/pages/chat/chat-system-content?id=${msgInfo.id}&title=${msgInfo.title}`
      })
    }
  },
  computed: {
    unreadCount() {
      if (!this.chat || !this.chat.unreadCount) {
        return 0;
      }
      return this.chat.unreadCount;
    },
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
    bgStyle() {
      return (msgInfo) => {
        if (msgInfo.coverUrl) {
          let bgImg = msgInfo.coverUrl;
          return `background-image: url(${bgImg}); background-size:100% 100%;background-repeat:no-repeat;color:white;`
        } else {
          return '';
        }
      }
    }
  },
  watch: {
    unreadCount: {
      handler(newCount, oldCount) {
        if (newCount > 0) {
          // 消息已读
          this.readedMessage()
        }
      }
    }
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

      .system-chat-item {

        .chat-msg-tip {
          line-height: 1.875rem;
          text-align: center;
          color: #555;
          font-size: .75rem;
          padding: .3125rem;
        }

        .message-box {
          background-color: #fff;
          text-align: left;
          border-radius: 3%;
          margin: 0 .625rem 1.5625rem;
          padding: .15625rem .625rem;
          cursor: pointer;

          .title {
            text-align: center;
            font-size: .9375rem;
            padding: .46875rem;
            font-weight: 600;
            height: 1.5625rem;
            line-height: 1.5625rem;
            overflow: hidden;
          }

          .message-content {
            padding: 10rpx;
            width: 100%;
            height: 10.9375rem;
            border-bottom: 1px #eee solid;
          }

          .image-content {
            padding: 10rpx;
            width: 100%;
            height: 10.9375rem;

            .media-swiper {
              height: 100%;
              border-radius: 8rpx;
              overflow: hidden;
              object-fit: cover;
            }

            .media-item {
              width: 100%;
              height: 100%;
            }

            .media-content {
              width: 100%;
              height: 100%;
            }
          }

          .audio-content {
            width: 100%;
            border-bottom: 1px #eee solid;

            .music-item {
              width: 100%;
              height: 100%;
            }
          }

          .video-content {
            position: relative;
            width: 100%;
            height: 10.9375rem;
            border-bottom: 1px #eee solid;

            .video-cover {
              width: 100%;
              height: 100%;
            }

            .play-icon {
              position: absolute;
              top: 50%;
              left: 50%;
              transform: translate(-50%, -50%);
              font-size: 100rpx;
              color: white;
            }
          }

          .intro {
            padding: .5rem;
            font-size: 1rem;
            border-bottom: 1px #eee solid;
            overflow-wrap: break-word;
          }

          .bottom-bar {
            font-size: .8125rem;
            padding: .5rem;
            text-align: left;
            color: #00f;
            height: 1.25rem;
          }
        }
      }
    }
  }
}
</style>