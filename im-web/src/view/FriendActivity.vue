<template>
  <div class="say-main container">
    <div class="say-container">
      <div class="cover">
        <img :src="require('@/assets/image/friendActivityBg.jpg')" alt="">
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
            <span class="talk-notify-msg" @click="showTalkNotify">
              <i class="el-icon-chat-dot-round"></i>
              <span v-if="notifyCount > 0" class="unread-text">{{notifyCount}}</span>
            </span>
            <span class="refreshBtn" @click="refreshTalkList">
              <i class="el-icon-refresh"></i>
            </span>
          </div>
        </div>
      </div>
      <talk-notify ref="talkNotify" :category="'private'"></talk-notify>
      <talk-list ref="talkListRef" :category="'private'" :section="section"></talk-list>
    </div>
  </div>
</template>

<script>
import TalkList from "@/components/talk/TalkList";
import TalkNotify from "../components/talk/TalkNotify.vue";

export default {
  name: "FriendActivity",
  components: {
    TalkList,
    TalkNotify,
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
    },
    readedTalkNotify() {
      this.$http({
        url: `/talk-notify/readed?category=private`,
        method: 'post'
      }).then(() => {})
    },
    showTalkNotify() {
      if (this.notifyCount > 0) {
        this.readedTalkNotify();
        this.$store.commit("resetUnreadTalkNotify");
      }
      this.$refs.talkNotify.show();
    },
  }
}
</script>

<style scoped lang="scss">
.col-xl,
.col-xl-auto,
.col-xl-12,
.col-xl-11,
.col-xl-10,
.col-xl-9,
.col-xl-8,
.col-xl-7,
.col-xl-6,
.col-xl-5,
.col-xl-4,
.col-xl-3,
.col-xl-2,
.col-xl-1,
.col-lg,
.col-lg-auto,
.col-lg-12,
.col-lg-11,
.col-lg-10,
.col-lg-9,
.col-lg-8,
.col-lg-7,
.col-lg-6,
.col-lg-5,
.col-lg-4,
.col-lg-3,
.col-lg-2,
.col-lg-1,
.col-md,
.col-md-auto,
.col-md-12,
.col-md-11,
.col-md-10,
.col-md-9,
.col-md-8,
.col-md-7,
.col-md-6,
.col-md-5,
.col-md-4,
.col-md-3,
.col-md-2,
.col-md-1,
.col-sm,
.col-sm-auto,
.col-sm-12,
.col-sm-11,
.col-sm-10,
.col-sm-9,
.col-sm-8,
.col-sm-7,
.col-sm-6,
.col-sm-5,
.col-sm-4,
.col-sm-3,
.col-sm-2,
.col-sm-1,
.col,
.col-auto,
.col-12,
.col-11,
.col-10,
.col-9,
.col-8,
.col-7,
.col-6,
.col-5,
.col-4,
.col-3,
.col-2,
.col-1 {
  width: 100%;
  padding: 4px !important;
}

.say-main {

  .say-container {

    .cover {
      width: 72%;
      position: relative;
      margin: 0 auto;
      img {
        border-radius: 5px;
        width: 100%;
        height: 100%;
      }

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

        .talk-notify-msg {
          position: relative;
          display: inline-block;
          cursor: pointer;
          font-weight: bold;
          font-size: 28px;
          color: white;

          .unread-text {
            position: absolute;
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