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
            <div v-if="msgInfo.type === 0 || msgInfo.type === 9" class="content" :style="bgStyle(msgInfo)">
              {{msgInfo.content}}
            </div>
            <div v-if="msgInfo.type === 1" class="image-content">
              <image-carousel :images="JSON.parse(msgInfo.content)" :height="'240px'"></image-carousel>
            </div>
            <div v-if="msgInfo.type===4" class="content">
              <video class="video-msg" controls="controls" preload="none" :src="JSON.parse(msgInfo.content)[0].videoUrl" :poster="JSON.parse(msgInfo.content)[0].coverUrl"></video>
            </div>
            <div v-if="msgInfo.type===3" class="audio-content">
              <vue-audio :width="400" :audio-source="JSON.parse(msgInfo.content)[0].url"></vue-audio>
            </div>
            <div class="intro" v-if="msgInfo.intro">
              {{msgInfo.intro}}
            </div>
            <div class="to-detail" v-if="msgInfo.type === 9">
              查看详情
            </div>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import ImageCarousel from "@components/common/ImageCarousel.vue";

export default {
  name: "ChatSystemBox",
  components: {
    ImageCarousel,
  },
  props: {
    chat: {
      type: Object
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
            width: 100%;
            height: 100%;
            object-fit: cover;
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
}
</style>