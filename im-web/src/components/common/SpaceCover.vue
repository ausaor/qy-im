<template>
  <div class="cover">
    <div class="info">{{name}}</div>
    <a class="operateBtn" @click="add" v-if="showAdd">
      <i class="el-icon-camera-solid"></i>
    </a>
    <div class="bottom-btns">
      <div class="talk-notify-msg" @click="showTalkNotify" v-show="showNotify">
        <i class="el-icon-chat-dot-round"></i>
        <div v-show="notifyCount>0" class="unread-text">{{notifyCount}}</div>
      </div>
      <div class="refreshBtn" @click="refresh">
        <i class="el-icon-refresh"></i>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "SpaceCover",
  props: {
    name: {
      type: String
    },
    showAdd: {
      type: Boolean,
      default: true,
    },
    notifyCount: {
      type: Number,
      default: 0
    },
    showNotify: {
      type: Boolean,
      default: true
    }
  },
  methods: {
    add() {
      this.$emit("add");
    },
    refresh() {
      this.$emit("refresh");
    },
    showTalkNotify() {
      this.$emit("showTalkNotify");
    }
  }

}
</script>

<style scoped lang="scss">
.cover {
  width: 72%;
  min-height: 520px;
  position: relative;
  margin: 0 auto;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1), 0 6px 20px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  background-image: url('~@/assets/image/friendActivityBg.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;

  //&:hover {
  //  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15), 0 12px 40px rgba(0, 0, 0, 0.15);
  //  transform: translateY(-5px);
  //}


  .info {
    width: 100%;
    position: absolute;
    top: 60%;
    font-size: 50px;
    text-align: center;
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
    color: white;
    font-weight: bold;
    font-size: 24px;
    border-radius: 50%;
  }

  .bottom-btns {
    position: absolute;
    bottom: 10px;
    right: 10px;
    display: flex;
    align-items: center;
    gap: 16px;

    .talk-notify-msg {
      position: relative;
      display: inline-block;
      background-color: #6CC6CB;
      padding: 0 3px;
      cursor: pointer;
      color: white;
      font-weight: bold;
      font-size: 24px;
      border-radius: 50%;

      .unread-text {
        position: absolute;
        line-height: 16px;
        background-color: #f56c6c;
        left: 20px;
        top: -6px;
        color: white;
        border-radius: 16px;
        padding: 0 5px;
        font-size: 10px;
        text-align: center;
        white-space: nowrap;
        border: 1px solid #f1e5e5;
      }
    }

    .refreshBtn {
      display: inline-block;
      background-color: #6CC6CB;
      padding: 0 3px;
      cursor: pointer;
      color: white;
      font-weight: bold;
      font-size: 24px;
      border-radius: 50%;
    }
  }
}
</style>