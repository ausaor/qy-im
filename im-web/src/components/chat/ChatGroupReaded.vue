<template>
  <div v-show="show">
    <div class="chat-group-readed-mask" @click.self="close()">
      <div class="chat-group-readed" :style="{ 'left': pos.x + 'px', 'top': pos.y + 'px' }" @click.prevent="">
        <el-tabs type="border-card" :stretch="true">
          <el-tab-pane :label="`已读(${readedMembers.length})`">
            <el-scrollbar class="scroll-box">
              <div v-for="(member) in readedMembers" :key="member.id">
                <chat-group-member :member="member"></chat-group-member>
              </div>
            </el-scrollbar>
          </el-tab-pane>
          <el-tab-pane :label="`未读(${unreadMembers.length})`">
            <el-scrollbar class="scroll-box">
              <div v-for="(member) in unreadMembers" :key="member.id">
                <chat-group-member :member="member"></chat-group-member>
              </div>
            </el-scrollbar>
          </el-tab-pane>
        </el-tabs>
        <div v-show="msgInfo.selfSend" class="arrow-right" :style="{ 'top': pos.arrowY + 'px' }">
          <div class="arrow-right-inner">
          </div>
        </div>
        <div v-show="!msgInfo.selfSend" class="arrow-left" :style="{ 'top': pos.arrowY + 'px' }">
          <div class="arrow-left-inner">
          </div>
        </div>
      </div>
    </div>

  </div>
</template>


<script>
import ChatGroupMember from "./ChatGroupMember.vue";

export default {
  name: "chatGroupReaded",
  components: {
    ChatGroupMember
  },
  data() {
    return {
      show: false,
      pos: {
        x: 0,
        y: 0,
        arrowY: 0
      },
      readedMembers: [],
      unreadMembers: []
    }
  },
  props: {
    groupMembers: {
      type: Array
    },
    msgInfo: {
      type: Object
    }
  },
  methods: {
    close() {
      this.show = false;
    },
    open(rect) {
      this.show = true;
      this.pos.arrowY = 200;
      // 计算窗口位置
      if (this.msgInfo.selfSend) {
        // 自己发的消息弹出在消息的左边
        this.pos.x = rect.left - 310;
      } else {
        // 别人发的消息弹窗在消息右边
        this.pos.x = rect.right + 20;
      }
      this.pos.y = rect.top + rect.height / 2 - 215;
      // 防止窗口溢出
      if (this.pos.y < 0) {
        this.pos.arrowY += this.pos.y
        this.pos.y = 0;
      }
      this.loadReadedUser()
    },
    loadReadedUser() {
      this.readedMembers = [];
      this.unreadMembers = [];
      let url = '';
      let groupId = null;
      let type = '';
      if (this.msgInfo.groupId) {
        url= "/message/group/findReadedUsers";
        groupId = this.msgInfo.groupId;
        type = 'GROUP';
      } else {
        url= "/message/regionGroup/findReadedUsers";
        groupId = this.msgInfo.regionGroupId;
        type = 'REGION-GROUP';
      }
      this.$http({
        url: url,
        method: 'get',
        params: { groupId: groupId, messageId: this.msgInfo.id }
      }).then(userIds => {
        this.groupMembers.forEach(member => {
          // 发送者和已退群的不显示
          if (member.userId == this.msgInfo.sendId || member.quit) {
            return;
          }
          // 区分已读还是未读
          if (userIds.find(userId => member.userId == userId)) {
            this.readedMembers.push(member);
          } else  {
            this.unreadMembers.push(member);
          }
        })
        // 更新已读人数
        if (type === 'GROUP') {
          this.$store.commit("updateMessage", {
            id: this.msgInfo.id,
            groupId: this.msgInfo.groupId,
            readedCount: this.readedMembers.length,
            chatType: 'GROUP'
          })
        } else if (type === 'REGION-GROUP') {
          this.$store.commit("updateRegionMessage", {
            id: this.msgInfo.id,
            regionGroupId: this.msgInfo.regionGroupId,
            readedCount: this.readedMembers.length
          })
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.chat-group-readed-mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
  z-index: 9999;
}

.chat-group-readed {
  position: fixed;
  box-shadow: 0px 0px 10px #ccc;
  width: 300px;
  background-color: var(--bg-color);
  border-radius: 8px;

  .scroll-box {
    height: 400px;
  }

  .arrow-left {

    position: absolute;
    left: -15px;
    width: 0;
    height: 0;
    border-top: 15px solid transparent;
    border-bottom: 15px solid transparent;
    border-right: 15px solid #ccc;

    .arrow-left-inner {
      position: absolute;
      top: -12px;
      left: 3px;
      width: 0;
      height: 0;
      overflow: hidden;
      border-top: 12px solid transparent;
      border-bottom: 12px solid transparent;
      border-right: 12px solid white;
    }
  }

  .arrow-right {
    position: absolute;
    right: -15px;
    width: 0;
    height: 0;
    border-top: 15px solid transparent;
    border-bottom: 15px solid transparent;
    border-left: 15px solid #ccc;

    .arrow-right-inner {
      position: absolute;
      top: -12px;
      right: 3px;
      width: 0;
      height: 0;
      overflow: hidden;
      border-top: 12px solid transparent;
      border-bottom: 12px solid transparent;
      border-left: 12px solid white;
    }
  }
}
</style>