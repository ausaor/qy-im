<template>
  <div class="item" :class="active ? 'active' : ''">
    <div class="avatar">
      <el-badge :value="''" :max="99" class="badge-tip">
        <svg class="icon svg-icon" aria-hidden="true">
          <use :xlink:href="community.iconId"></use>
        </svg>
      </el-badge>
    </div>
    <div class="text">
      <div>{{community.name}}</div>
      <div class="new-talk-info">
        <div v-show="unreadUserCount">{{unreadUserCount}}人新发表</div>
        <div v-show="talkList.length" class="new-talk-user">
          <head-image v-for="(talk, index) in talkList" :key="index" :url="talk.avatar" :name="talk.nickName" :size="24"></head-image>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';

export default {
  name: "CommunityItem",
  components: {
    HeadImage
  },
  data() {
    return {}
  },
  props: {
    community: {
      type: Object
    },
    active: {
      type: Boolean
    }
  },
  computed: {
    unreadUserCount() {
      return this.$store.state.talkStore.unreadUserList.length;
    },
    talkList() {
      return this.$store.state.talkStore.lastTalks;
    }
  }
}
</script>

<style scoped lang="scss">
.item {
  height: 65px;
  display: flex;
  margin-bottom: 1px;
  position: relative;
  padding-left: 15px;
  align-items: center;
  padding-right: 5px;
  background-color: var(--bg-color);
  white-space: nowrap;
  cursor: pointer;

  &:hover {
   background-color: var(--active-color);
  }

  &.active {
   background-color: var(--active-color);
  }

  .avatar {
    position: relative;
    width: 45px;
    height: 45px;

    display: flex;
    justify-content: center; /*水平居中*/
    align-items: center; /*垂直居中*/

    .icon {
      display: block;
      height: 45px;
      line-height: 45px;
      font-size: 28px;
      color: #333;
      -webkit-transition: font-size 0.25s linear, width 0.25s linear;
      -moz-transition: font-size 0.25s linear, width 0.25s linear;
      transition: font-size 0.25s linear, width 0.25s linear;
    }
  }

  .text {
    position: relative;
    display: flex;
    flex: 1;
    align-items: center;
    justify-content: space-between;
    margin-left: 10px;
    height: 100%;
    overflow: hidden;
    text-align: left;

    .new-talk-info {
      color: red;
      font-size: 14px;
      font-weight: bold;
      display: flex;
      align-items: center;

      .new-talk-user {
        display: flex;
        align-items: center;
      }
    }
  }
}
</style>