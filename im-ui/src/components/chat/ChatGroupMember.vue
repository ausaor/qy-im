<template>
  <div class="chat-group-member" :style="{'height':height+'px'}">
    <div class="member-avatar">
      <head-image :size="headImageSize" :name="member.aliasName" :url="member.headImage"> </head-image>
    </div>
    <div class="member-name" :style="{'line-height':height+'px'}">
      <div>
        <span>{{ member.aliasName }}</span>
        <span v-if="showNickName" style="margin-left: 10px;">{{ member.nickName }}</span>
        <span v-show="member.userId !== -1" :class="member.online ? 'online':'un-online'">{{member.online ? '[在线]' : '[离线]'}}</span>
        <span v-show="member.userId !== -1 && (member.joinType ===1 || member.joinType ===0)" :class="member.joinType===1 ? 'permanent' : 'temporary'">{{member.joinType===1 ? '[常驻]' : '[临时]'}}</span>
      </div>
    </div>
  </div>
</template>

<script>
import HeadImage from "../common/HeadImage.vue";
export default {
  name: "ChatGroupMember",
  components: { HeadImage },
  data() {
    return {};
  },
  props: {
    member: {
      type: Object,
      required: true
    },
    height:{
      type: Number,
      default: 50
    },
    showNickName: {
      type: Boolean,
      default: false,
    }
  },
  computed:{
    headImageSize(){
      return Math.ceil(this.height * 0.75)
    }
  }
}
</script>

<style lang="scss">
.chat-group-member {
  display: flex;
  margin-bottom: 1px;
  position: relative;
  padding: 0 5px;
  align-items: center;
  background-color: var(--bg-color);
  white-space: nowrap;
  box-sizing: border-box;

  &:hover {
    background-color: #eeeeee;
  }

  &.active {
    background-color: #eeeeee;
  }

  .member-name {
    padding-left: 10px;
    height: 100%;
    text-align: left;
    white-space: nowrap;
    overflow: hidden;
    font-size: 14px;
    font-weight: 600;

    span {
      margin: auto .2rem;
    }

    .online {
      color: var(--online-color);
    }

    .un-online {
      color: var(--unline-color);
    }

    .permanent {
      color: var(--permanent-color);
    }

    .temporary {
      color: var(--temporary-color);
    }
  }
}
</style>
