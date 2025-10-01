<template>
  <div class="chat-system-box" @mousemove="readedMessage()">
    <el-container>
      <el-header height="50px">
        <span class="pusher-name">{{chat.showName}}</span>
      </el-header>
      <el-main class="chat-main" id="chat-system-box">
        <div class="chat-system-item" v-for="(msgInfo,idx) in chat.messages" :key="idx" v-if="msgInfo.type !== 20">
          <div class="chat-msg-tip">{{$date.toTimeText(msgInfo.sendTime)}}</div>
          <div class="message-box">
            <div class="title">{{msgInfo.title}}</div>
            <div v-if="msgInfo.type === 0" class="content" :style="bgStyle(msgInfo)">
              {{msgInfo.content}}
            </div>
            <div v-if="msgInfo.type === 9" class="content" :style="bgStyle(msgInfo)">
            </div>
            <div v-if="msgInfo.type === 1" class="image-content">
              <image-carousel :images="JSON.parse(msgInfo.content)" :height="'240px'"></image-carousel>
            </div>
            <div v-if="msgInfo.type===4" class="content">
              <div class="video-msg">
                <img :src="JSON.parse(msgInfo.content)[0].coverUrl">
                <span class="play-icon el-icon-video-play" @click="onPlayVideo(JSON.parse(msgInfo.content)[0].url, JSON.parse(msgInfo.content)[0].coverUrl)"></span>
              </div>
<!--              <video class="video-msg" controls="controls" preload="none" :src="JSON.parse(msgInfo.content)[0].url" :poster="JSON.parse(msgInfo.content)[0].coverUrl"></video>-->
            </div>
            <div v-if="msgInfo.type===3" class="audio-content">
              <vue-audio :width="400" :audio-source="JSON.parse(msgInfo.content)[0].url"></vue-audio>
            </div>
            <div class="intro" v-if="msgInfo.intro">
              {{msgInfo.intro}}
            </div>
            <div class="to-detail" v-if="msgInfo.type === 9" @click="viewDetail(msgInfo)">
              查看详情
            </div>
          </div>
        </div>
      </el-main>
      <div class="chat-system-content" v-show="showSysMsgContent">
        <el-container>
          <el-header style="height: 50px;">
            <span class="back el-icon-back" @click="goBack"></span>
          </el-header>
          <el-main>
            <span class="title">{{curMsgInfo?.title}}</span>
            <div class="rich-text" v-html="contentDetail?.richText">
            </div>
          </el-main>
        </el-container>
      </div>
    </el-container>
    <video-play ref="videoPlay" :videoUrl="videoUrl" :posterUrl="posterUrl" @close="closeVideoPlay"></video-play>
  </div>
</template>

<script>
import ImageCarousel from "@components/common/ImageCarousel.vue";
import VideoPlay from "@components/common/VideoPlay.vue";

export default {
  name: "ChatSystemBox",
  components: {
    VideoPlay,
    ImageCarousel,
  },
  props: {
    chat: {
      type: Object
    }
  },
  data() {
    return {
      showSysMsgContent: false,
      curMsgInfo: {},
      contentDetail: {},
      videoUrl: '',
      posterUrl: '',
    }
  },
  methods: {
    readedMessage() {
      if (this.chat.unreadCount == 0) {
        return;
      }
      this.$store.commit("resetUnreadCount", this.chat)

      let  url = `/message/system/readed?pusherId=${this.chat.targetId}`
      this.$http({
        url: url,
        method: 'put'
      }).then(() => {})
    },
    scrollToBottom() {
      this.$nextTick(() => {
        let div = document.getElementById("chat-system-box");
        div.scrollTop = div.scrollHeight;
      });
    },
    viewDetail(msgInfo) {
      this.curMsgInfo = msgInfo;
      this.$http({
        url: `/message/system/content?id=${msgInfo.id}`,
        method: 'get'
      }).then((data) => {
        this.showSysMsgContent = true;
        this.contentDetail = data
      })
    },
    goBack() {
      this.contentDetail = {}
      this.curMsgInfo = {};
      this.showSysMsgContent = false;
    },
    onPlayVideo(videoUrl, coverImageUrl) {
      this.videoUrl = videoUrl;
      this.posterUrl = coverImageUrl;
      this.$refs.videoPlay.onPlayVideo()
    },
    closeVideoPlay() {
      this.videoUrl = '';
      this.posterUrl = '';
    },
  },
  computed: {
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
  created() {
    this.scrollToBottom();
  },
  mounted() {
    let div = document.getElementById("chat-system-box");
    div.addEventListener('scroll', this.onScroll)
  },
  watch: {
    chat: {
      handler(newChat, oldChat) {
        if (newChat.type === 'SYSTEM' && (!oldChat || newChat.targetId !== oldChat.targetId)) {
          // 滚到底部
          this.scrollToBottom();
        }
      }
    }
  }
}
</script>

<style scoped lang="scss">
/* 引入Quill核心样式（必须） */
@import '~quill/dist/quill.snow.css';
.chat-system-box {
  display: flex;
  flex-direction: column;
  position: relative;
  width: 100%;
  background: #f8f8f8;

  .el-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #ebeef5;

    .pusher-name {
      color: #90cb6c;
    }
  }

  .chat-main {
    background: #f8f8f8;
    margin: 0 3px;
    display: flex;
    flex-direction: column;
    align-items: center;

    .chat-system-item {

      .chat-msg-tip {
        line-height: 20px;
        font-size: 12px;
        color: #999999;
        text-align: center;
      }

      .message-box {
        width: 450px;
        background-color: #fff;
        text-align: left;
        border-radius: 3%;
        margin: 15px;
        padding: 5px 20px;
        cursor: pointer;
        box-shadow: 5px 5px 10px -4px #63645e;

        .title {
          text-align: center;
          font-size: 18px;
          white-space: nowrap;
          overflow: hidden;
          margin: 10px 20px;
          font-weight: 600;
          color: orange;
        }

        .content {
          width: 100%;
          height: 240px;
          padding: 8px;
          font-size: 14px;
          overflow-wrap: break-word;
          border: 1px solid #eeeeee;

          .video-msg {
            position: relative;
            width: 100%;
            height: 100%;
            object-fit: cover;

            img {
              max-width: 100%;  /* 图片宽度不超过父元素宽度 */
              max-height: 100%; /* 图片高度不超过父元素高度 */
              display: block;   /* 去除图片底部的空白间隙 */
              margin: 0 auto;   /* 可选：让图片水平居中 */
            }

            .play-icon {
              display: block;
              position: absolute;
              font-size: 80px;
              font-weight: 500;
              width: 80px;
              height: 80px;
              left: 50%;
              top: 50%;
              transform: translate(-50%, -50%);
              cursor: pointer;
              color: #ffffff;
            }
          }
        }

        .image-content {
          width: 100%;
          border: 1px solid #eeeeee;
        }

        .audio-content {
          width: 100%;
          padding-top: 5px;
          padding-bottom: 5px;
          border: 1px solid #eeeeee;

          .vueAudioBetter {

          }
        }

        .intro {
          padding: 8px;
          font-size: 16px;
          border-bottom: 1px solid #eeeeee;
          overflow-wrap: break-word;
        }

        .to-detail {
          font-size: 14px;
          padding: 8px;
          text-align: left;
          color: #00f;
        }
      }
    }
  }

  .chat-system-content {
    z-index: 10;
    position: absolute;
    background-color: #fff;
    width: 100%;
    height: 100%;

    .back {
      position: absolute;
      left: 20px;
      line-height: 50px;
      font-size: 26px;
      cursor: pointer;
    }

    .title {
      font-size: 24px;
      font-weight: bolder;
      text-align: center;
      display: block;
      margin-top: 10px;
    }

    .rich-text {
      padding: 10px 20px 20px 20px;
    }
  }
}
</style>