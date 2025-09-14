<template>
	<div class="group-item" :class="active ? 'active' : ''" @contextmenu.prevent="showRightMenu($event)">
		<div class="group-avatar">
			<head-image :size="45" :name="regionGroup.remark" :url="regionGroup.headImage"> </head-image>
      <div v-show="unreadCount>0" class="unread-text">{{unreadCount}}</div>
      <div class="group-activity" v-show="unreadTalkCount > 0 || unreadNotifyCount > 0"></div>
		</div>
		<div class="group-info">
			<div class="group-name">{{regionGroup.remark}}
        <span style="margin-left: 10px">
          <el-tag size="mini" type="success" v-if="regionGroup.joinType===1">常驻</el-tag>
          <el-tag size="mini" type="warning" v-if="regionGroup.joinType===0">临时</el-tag>
        </span>
      </div>
      <div class="chat-content">
        <div class="chat-at-text">{{atText}}</div>
        <div class="chat-send-name" v-show="isShowSendName">{{chat.sendNickName+':&nbsp;'}}</div>
        <div class="chat-content-text" v-if="chat.lastContent" v-html="$emo.transform(chat.lastContent)"></div>
      </div>
		</div>
    <right-menu v-show="menu && rightMenu.show" :pos="rightMenu.pos" :items="rightMenu.items"
                @close="rightMenu.show=false" @select="onSelectMenu"></right-menu>
    <slot></slot>
	</div>
</template>

<script>
	import HeadImage from '../common/HeadImage.vue';
  import RightMenu from "../common/RightMenu.vue";

	export default {
		name: "regionGroupItem",
		components: {
			HeadImage,
      RightMenu,
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
            key: 'QUIT',
            name: '退出',
            icon: 'el-icon-circle-close'
          }]
        }
      }
		},
		props: {
      regionGroup: {
				type: Object
			},
			active: {
				type: Boolean
			},
      menu:{
        type: Boolean,
        default: true
      },
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
        this.$emit(item.key.toLowerCase(), this.regionGroup);
      },
    },
    computed: {
      unreadCount() {
        let unreadCount = 0;
        let chat = this.$store.getters.findRegionChatByGroup(this.regionGroup.id);
        if (chat) {
          unreadCount += chat.unreadCount;
        }
        return unreadCount;
      },
      chat() {
        let chat = this.$store.getters.findRegionChatByGroup(this.regionGroup.id);
        return chat ? chat : {};
      },
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
      atText() {
        if (this.chat.atMe) {
          return "[有人@我]"
        } else if (this.chat.atAll) {
          return "[@全体成员]"
        }
        return "";
      },
      unreadTalkCount() {
        let talkMap =this.$store.state.talkStore.regionTalks;
        let talks = talkMap.get(this.regionGroup.code);
        if (talks) {
          return talks.length;
        }
        return 0;
      },
      unreadNotifyCount() {
        let notifyMap =this.$store.state.talkStore.regionNotify;
        let count = notifyMap.get(this.regionGroup.code);
        if (count) {
          return count;
        }
        return 0;
      },
    }
	}
</script>

<style scoped lang="scss" >
  .group-item {
    height: 50px;
    display: flex;
    margin-bottom: 1px;
    position: relative;
    padding: 5px;
    align-items: center;
    background-color: white;
    white-space: nowrap;
    cursor: pointer;

    &:hover {
      background-color: var(--active-color);
    }

    &.active {
      background-color: var(--active-color);
    }

    .group-avatar {
      position: relative;
      display: flex;
      width: 45px;
      height: 45px;

      .unread-text {
        position: absolute;
        background-color: #f56c6c;
        right: -5px;
        top: -3px;
        color: white;
        border-radius: 30px;
        padding: 1px 5px;
        font-size: 10px;
        text-align: center;
        white-space: nowrap;
        border: 1px solid #f1e5e5;
      }

      .group-activity {
        position: absolute;
        bottom: 0;
        right: -3px;
        width: 12px;
        height: 12px;
        border-radius: 50%;
        background: red;
      }
    }

    .group-info {
      flex: 1;
      display: flex;
      text-align: left;
      flex-direction: column;
      padding-left: 10px;
      overflow: hidden;

      .group-name {
        display: flex;
        line-height: 25px;
        height: 25px;
        font-weight: bold;
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
