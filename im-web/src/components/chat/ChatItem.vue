<template>
  <div class="chat-item" :class="active ? 'active' : ''" @contextmenu.prevent="showRightMenu($event)">
    <div class="chat-left">
      <head-image :url="chat.headImage" :name="chat.showName" :size="45"
        :id="chat.type=='PRIVATE'?chat.targetId:0"></head-image>
      <div v-show="chat.unreadCount>0" class="unread-text">{{chat.unreadCount}}</div>
    </div>
    <div class="chat-right">
      <div class="chat-name">
        <div class="chat-name-text" :style="{'color': chat.groupType === 1 ? 'red' : 'black'}">
          <div>{{chat.type=='GROUP' ? chat.showName : (chat.friendRemark ? chat.friendRemark : chat.showName)}}</div>
          <el-tag size="mini" effect="dark" v-if="chat.type=='GROUP'">群</el-tag>
          <el-tag size="mini" effect="dark" v-if="chat.type=='SYSTEM'" color="rgb(222, 28, 28)">官方</el-tag>
        </div>
        <div class="chat-time-text">
          {{showTime}}
        </div>
      </div>
      <div class="chat-content">
        <div class="chat-at-text">{{atText}}</div>
        <div class="chat-send-name" v-show="isShowSendName">{{chat.sendNickName+':&nbsp;'}}</div>
        <div class="chat-content-text" v-html="$emo.transform(chat.lastContent)"></div>
      </div>
    </div>
    <right-menu v-show="rightMenu.show" :pos="rightMenu.pos" :items="rightMenu.items" @close="rightMenu.show=false"
                @select="onSelectMenu"></right-menu>
  </div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';
import RightMenu from '../common/RightMenu.vue';

export default {
  name: "chatItem",
  components: {
    HeadImage,
    RightMenu
  },
  data() {
    return {
      rightMenu: {
        show: false,
        pos: {
          x: 0,
          y: 0
        },
        items: [{
          key: 'TOP',
          name: '置顶',
          icon: 'el-icon-top'
        }, {
          key: 'DELETE',
          name: '删除',
          icon: 'el-icon-delete'
        }]
      }
    }
  },
  props: {
    chat: {
      type: Object
    },
    active: {
      type: Boolean
    },
    index: {
      type: Number
    }
  },
  methods: {
    showRightMenu(e) {
      this.rightMenu.pos = {
        x: e.x,
        y: e.y
      };
      this.rightMenu.show = "true";
    },
    onSelectMenu(item) {
      this.$emit(item.key.toLowerCase(), this.msgInfo);
    }
  },
  mounted() {
  },
  computed: {
    isShowSendName() {
      if (!this.chat.sendNickName) {
        return false;
      }
      let size = this.chat.messages.length;
      if (size == 0) {
        return false;
      }
      // 只有群聊的普通消息需要显示名称
      let lastMsg = this.chat.messages[size - 1];
      return this.$msgType.isNormal(lastMsg.type)
    },
    showTime() {
      return this.$date.toTimeText(this.chat.lastSendTime, true)
    },
    atText() {
      if (this.chat.atMe) {
        return "[有人@我]"
      } else if (this.chat.atAll) {
        return "[@全体成员]"
      }
      return "";
    }
  }
}
</script>

<style scoped lang="scss">
.chat-item {
  height: 50px;
  display: flex;
  margin-bottom: 1px;
  position: relative;
  padding: 5px;
  align-items: center;
  background-color: var(--bg-color);
  white-space: nowrap;
  color: black;
  cursor: pointer;

  &:hover {
    background-color: var(--active-color);
  }

  &.active {
    background-color: var(--active-color);
  }

  .chat-left {
    position: relative;
    display: flex;
    width: 45px;
    height: 45px;

    .unread-text {
      position: absolute;
      background-color: #f56c6c;
      right: -5px;
      top: -5px;
      color: white;
      border-radius: 30px;
      padding: 1px 5px;
      font-size: 10px;
      text-align: center;
      white-space: nowrap;
      border: 1px solid #f1e5e5;
    }
  }


  .chat-right {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding-left: 10px;
    text-align: left;
    overflow: hidden;

    .chat-name {
      display: flex;
      line-height: 25px;
      height: 25px;

      .chat-tag {
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 1px;
      }

      .chat-name-text {
        flex: 1;
        font-size: 15px;
        font-weight: 600;
        white-space: nowrap;
        overflow: hidden;
        display: flex;
        align-items: center;

        .el-tag {
          min-width: 22px;
          text-align: center;
          background-color: #2830d3;
          border-radius: 10px;
          border: 0;
          height: 16px;
          line-height: 16px;
          font-size: 10px;
          margin-left: 2px;
          opacity: .8;
        }
      }


      .chat-time-text{
        font-size: 13px;
        text-align: right;
        color: #888888;
        white-space: nowrap;
        overflow: hidden;
        padding-left: 10px;
      }
    }

    .chat-content {
      display: flex;
      line-height: 22px;

      .chat-at-text {
        color: #c70b0b;
        font-size: 12px;
      }

      .chat-send-name{
        font-size: 13px;
      }


      .chat-content-text {
        flex: 1;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        font-size: 13px;
        img {
          width: 20px !important;
          height: 20px !important;
          vertical-align: bottom;
        }
      }

    }
  }
}
</style>