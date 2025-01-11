<template>
  <div class="say-main container">
    <div class="say-container">
      <div class="cover">
        <img :src="require('@/assets/image/friendActivityBg.jpg')" alt="">
        <div class="info">心语</div>
        <a class="operateBtn" @click="handleShowAddTalk">
          <i class="el-icon-more"></i>
        </a>
        <a class="refreshBtn" @click="refreshTalkList">
          <i class="el-icon-refresh"></i>
        </a>
      </div>
      <div class="tab-box">
        <el-tabs type="card" @tab-click="handleTabClick" v-model="section">
          <el-tab-pane label="全部" name="my-friends">
          </el-tab-pane>
          <el-tab-pane label="个人" name="my">
          </el-tab-pane>
          <el-tab-pane label="好友" name="friends">
          </el-tab-pane>
        </el-tabs>
      </div>
      <talk-list ref="talkListRef" :category="'private'" :section="section"></talk-list>
    </div>
  </div>
</template>

<script>
import TalkList from "@/components/talk/TalkList";

export default {
  name: "FriendActivity",
  components: {
    TalkList,
  },
  data() {
    return {
      section: 'my-friends'
    }
  },
  created() {

  },
  computed: {

  },
  methods: {
    handleShowAddTalk() {
      this.$refs.talkListRef.handleShowAddTalk();
    },
    refreshTalkList() {
      this.$refs.talkListRef.refreshTalkList();
    },
    handleTabClick(tab, event) {
      this.section = tab.name;
    },
  }
}
</script>

<style lang="scss">
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
        background-color: #6CC6CB;
        padding: 0 5px;
        cursor: pointer;
      }

      .refreshBtn {
        position: absolute;
        bottom: 10px;
        right: 10px;
        display: inline-block;
        background-color: #6CC6CB;
        padding: 0 5px;
        cursor: pointer;
        color: white;
      }
    }

    .tab-box {
      width: 72%;
      margin: 0 auto;
    }
  }
}
</style>