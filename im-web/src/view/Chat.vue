<template>
	<el-container class="chat-page">
		<el-aside width="260px" class="aside">
			<div class="header">
        <el-input class="search-text" placeholder="搜索" v-model="searchText">
          <i class="el-icon-search el-input__icon" slot="prefix"> </i>
        </el-input>
			</div>
      <div class="chat-loading"  v-if="loading" v-loading="true" element-loading-text="消息接收中..."
           element-loading-spinner="el-icon-loading" element-loading-background="#F9F9F9" element-loading-size="24">
      </div>
			<el-scrollbar class="chat-items">
				<div v-for="(chat,index) in chatStore.chats" :key="index">
					<chat-item
               v-show="!chat.delete && chat.showName.includes(searchText)"
               :chat="chat" :index="index"
               @click.native="onActiveItem(index)"
               @delete="onDelItem(index)"
               @top="onTop(index)"
               :active="chat === chatStore.activeChat"></chat-item>
				</div>
			</el-scrollbar>
		</el-aside>
		<el-container>
			<chat-box v-if="chatStore.activeChat && (chatStore.activeChat.type=== 'PRIVATE' || chatStore.activeChat.type=== 'GROUP')" :chat="chatStore.activeChat"></chat-box>
      <chat-system-box v-if="chatStore.activeChat && chatStore.activeChat.type=== 'SYSTEM'" :chat="chatStore.activeChat"></chat-system-box>
		</el-container>
	</el-container>
</template>

<script>
	import ChatItem from "../components/chat/ChatItem.vue";
	import ChatBox from "../components/chat/ChatBox.vue";
	import ChatSystemBox from "@/components/chat/ChatSystemBox";
	
	export default {
		name: "chat",
		components: {
			ChatItem,
			ChatBox,
      ChatSystemBox
		},
		data() {
			return {
				searchText: "",
				messageContent: "",
				group: {},
				groupMembers: [] 
			}
		},
		methods: {
      onActiveItem(index) {
				this.$store.commit("activeChat", index);
			},
      onDelItem(index) {
        this.$store.commit("removeChat", index);
      },
      onTop(chatIdx) {
        this.$store.commit("moveTop", chatIdx);
      },
		},
		computed: {
			chatStore() {
				return this.$store.state.chatStore;
			},
      loading(){
        return this.chatStore.loadingGroupMsg || this.chatStore.loadingPrivateMsg || this.chatStore.loadingSystemMsg
      }
		}
	}
</script>

<style scoped lang="scss">
  .chat-page {
    .aside {
      display: flex;
      flex-direction: column;
      background: white;
      border-right: 1px solid #cccccc;

      .header {
        height: 50px;
        display: flex;
        align-items: center;
        padding: 0 8px;
      }

      .chat-loading {
        height: 50px;
        background-color: #eee;

        .el-icon-loading {
          font-size: 24px;
          color: #999999;
        }

        .el-loading-text {
          color: #999999;
        }
      }

      .chat-items {
        flex: 1;
      }
    }
  }
</style>
