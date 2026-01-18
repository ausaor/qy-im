<template>
  <div class="say-main container">
    <div class="say-container">
      <div class="cover">
        <div class="info">心语</div>
        <a class="operateBtn" @click="handleShowAddTalk">
          <i class="el-icon-camera-solid"></i>
        </a>
        <div class="tab-box">
          <el-tabs @tab-click="handleTabClick" v-model="section">
            <el-tab-pane label="全部" name="my-friends">
            </el-tab-pane>
            <el-tab-pane label="我的" name="my">
            </el-tab-pane>
            <el-tab-pane label="好友" name="friends">
            </el-tab-pane>
          </el-tabs>
          <div class="btns">
             <span class="play-music-play" @click="showMusicPlay">
              <i class="el-icon-headset"></i>
            </span>
            <span class="talk-notify-msg" @click="showTalkNotify">
              <i class="el-icon-chat-dot-round"></i>
              <div v-if="notifyCount > 0" class="unread-text">{{notifyCount}}</div>
            </span>
            <span class="refreshBtn" @click="refreshTalkList">
              <i class="el-icon-refresh"></i>
            </span>
          </div>
        </div>
      </div>
      <talk-notify ref="talkNotify" :category="'private'"></talk-notify>
      <talk-list ref="talkListRef" :category="'private'" :section="section"></talk-list>
      <music-play ref="musicPlayRef" :category="'private'" :section="'my'"></music-play>
    </div>
  </div>
</template>

<script>
import TalkList from "@/components/talk/TalkList";
import TalkNotify from "../components/talk/TalkNotify.vue";
import MusicPlay from "@components/common/musicPlay.vue";

export default {
  name: "FriendActivity",
  components: {
    TalkList,
    TalkNotify,
    MusicPlay,
  },
  data() {
    return {
      section: 'my-friends'
    }
  },
  created() {

  },
  computed: {
    notifyCount() {
      return this.$store.state.talkStore.notifyCount;
    },
  },
  methods: {
    handleShowAddTalk() {
      this.$refs.talkListRef.handleShowAddTalk();
    },
    refreshTalkList() {
      if (this.notifyCount > 0) {
        this.readedTalkNotify();
      }
      this.$store.commit("resetUnreadTalkInfo")
      this.$refs.talkListRef.refreshTalkList();
    },
    handleTabClick(tab, event) {
      this.section = tab.name;
      this.$refs.talkListRef.refreshTalkList();
    },
    readedTalkNotify() {
      let params = {
        category: 'private'
      };
      this.$http({
        url: `/talk-notify/readed`,
        method: 'post',
        data: params
      }).then(() => {})
    },
    showTalkNotify() {
      if (this.notifyCount > 0) {
        this.readedTalkNotify();
        this.$store.commit("resetUnreadTalkNotify");
      }
      this.$refs.talkNotify.show();
    },
    showMusicPlay() {
      this.$refs.musicPlayRef.show();
    }
  }
}
</script>

<style scoped lang="scss">
.say-main {

  .say-container {

    .cover {
      width: 72%;
      min-height: 400px;
      max-height: 500px;
      position: relative;
      margin: 0 auto;
      border-radius: 8px;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1), 0 6px 20px rgba(0, 0, 0, 0.1);
      overflow: hidden;
      background-image: url('~@/assets/image/friendActivityBg.jpg');
      background-size: cover;
      background-position: center;
      background-repeat: no-repeat;

      .info {
        position: absolute;
        top: 60%;
        left: 50%;
        transform: translate(-50%, -50%);
        font-size: 50px;
        color: #ffffff;
        font-weight: 700;
        text-shadow: 0 0 10px red,0 0 20px red,0 0 30px red,0 0 40px red;
      }

      .operateBtn {
        position: absolute;
        top: 10px;
        right: 10px;
        display: inline-block;
        padding: 0 5px;
        cursor: pointer;
        font-weight: bold;
        font-size: 28px;
        color: white;

        .el-icon-camera-solid {
          background-color: #6CC6CB;
          border-radius: 50%;
        }
      }
    }

    .tab-box {
      padding: 0 15px;
      position: absolute;
      bottom: 5px;
      width: 100%;
      display: flex;
      justify-content: space-between;

      .btns {
        display: flex;
        align-items: center;
        gap: 16px;

        .play-music-play {
          display: inline-block;
          cursor: pointer;
          font-weight: bold;
          font-size: 28px;
          color: white;

          .el-icon-headset {
            background-color: #6CC6CB;
            border-radius: 50%;
          }
        }

        .talk-notify-msg {
          position: relative;
          display: inline-block;
          cursor: pointer;
          font-weight: bold;
          font-size: 28px;
          color: white;

          .el-icon-chat-dot-round {
            background-color: #6CC6CB;
            border-radius: 50%;
          }

          .unread-text {
            position: absolute;
            height: 20px;
            min-width: 20px;
            line-height: 20px;
            background-color: #f56c6c;
            left: 20px;
            top: -5px;
            color: white;
            border-radius: 50%;
            padding: 0 5px;
            font-size: 10px;
            text-align: center;
            white-space: nowrap;
            border: 1px solid #f1e5e5;
          }
        }

        .refreshBtn {
          display: inline-block;
          cursor: pointer;
          font-weight: bold;
          font-size: 28px;
          color: white;

          .el-icon-refresh {
            background-color: #6CC6CB;
            border-radius: 50%;
          }
        }
      }
    }
  }
}
</style>