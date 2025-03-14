<template>
	<div class="friend-item" :class="active ? 'active' : ''" @contextmenu.prevent="showRightMenu($event)">
		<div class="avatar">
			<head-image :size="45" :name="friend.nickName"
          :url="friend.headImage"
          :online="friend.online">
      </head-image>
		</div>
		<div class="friend-info">
			<div class="friend-name">{{ friend.friendRemark ? friend.friendRemark : friend.nickName}}</div>
<!--      <div class="friend-online" :class="friend.online ? 'online':''">{{ friend.online?"[在线]":"[离线]"}}</div>-->
      <div class="friend-online">
        <el-image v-show="friend.onlineWeb" class="online" :src="require('@/assets/image/online_web.png')"
                  title="电脑设备在线" />
        <el-image v-show="friend.onlineApp" class="online" :src="require('@/assets/image/online_app.png')"
                  title="移动设备在线" />
      </div>
		</div>
    <div class="avatar" v-if="selectCharacterVisible" @click="selectCharacter()">
      <avatar-image :url="friend.templateCharacterAvatar" :name="friend.templateCharacterName"></avatar-image>
    </div>
    <right-menu v-show="menu && rightMenu.show" :pos="rightMenu.pos" :items="rightMenu.items"
                @close="rightMenu.show=false" @select="onSelectMenu"></right-menu>
		<slot></slot>
	</div>
</template>

<script>
	import HeadImage from '../common/HeadImage.vue';
  import RightMenu from "../common/RightMenu.vue";
  import AvatarImage from "@/components/common/AvatarImage";

	export default {
		name: "friendItem",
		components: {
			HeadImage,
      RightMenu,
      AvatarImage
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
            key: 'CHAT',
            name: '发送消息',
            icon: 'el-icon-chat-dot-round'
          }, {
            key: 'DELETE',
            name: '删除好友',
            icon: 'el-icon-delete'
          }]
        }
      }
		},
		methods:{
      showRightMenu(e) {
        this.rightMenu.pos = {
          x: e.x,
          y: e.y
        };
        this.rightMenu.show = "true";
      },
      onSelectMenu(item) {
        this.$emit(item.key.toLowerCase(), this.msgInfo);
      },
      selectCharacter() {
        this.$emit('select',this.friend,this.index)
      }
		},
    // computed:{
    //   friend(){
    //     return this.$store.state.friendStore.friends[this.index];
    //   }
    // },
		props: {
			friend: {
				type: Object
			},
			active: {
				type: Boolean
			},
			index: {
				type: Number
			},
      menu:{
				type: Boolean,
				default: true
			},
      isTemplate: {
        type: Boolean
      },
      selectCharacterVisible: {
			  type: Boolean,
        default: false,
      }
		},
    mounted() {
      //console.log(this.menu)
    }
	}
</script>

<style scope lang="scss">
	.friend-item {
		height: 50px;
		display: flex;
		margin-bottom: 1px;
		position: relative;
    padding: 5px;
		align-items: center;
		background-color: var(--bg-color);
		white-space: nowrap;
    cursor: pointer;

		&:hover {
			background-color: var(--active-color);
		}

		&.active {
			background-color: var(--active-color);
		}


    .friend-avatar {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 45px;
      height: 45px;
    }

    .friend-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding-left: 10px;
      text-align: left;

      .friend-name {
        font-size: 15px;
        font-weight: 600;
        line-height: 30px;
        white-space: nowrap;
        overflow: hidden;
      }

      .friend-online {
        .online {
          padding-right: 2px;
          width: 15px;
          height: 15px;

        }
      }
    }
	}
</style>
