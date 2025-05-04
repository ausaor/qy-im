<template>
	<el-container class="chat-page">
		<el-aside width="14%" class="chat-list-box">
			<div class="chat-list-header">
        <el-input class="search-text" placeholder="搜索" v-model="searchText">
          <i class="el-icon-search el-input__icon" slot="prefix"> </i>
        </el-input>
			</div>
      <div class="chat-list-loading"  v-if="loading" v-loading="true" element-loading-text="消息接收中..."
           element-loading-spinner="el-icon-loading" element-loading-background="#eee">
        <div class="chat-loading-box"></div>
      </div>
			<el-scrollbar class="chat-list-items">
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
		<el-container class="chat-box">
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
		.chat-list-box {
			display: flex;
			flex-direction: column;
			border-right: 1px #54CC36 solid;
			background: white;
			width: 3rem;

			.chat-list-header {
        padding: 3px 8px;
        line-height: 50px;
        border-bottom: 1px #ddd solid;
			}

      .chat-list-loading{
        height: 50px;
        background-color: #eee;

        .chat-loading-box{
          height: 100%;
        }
      }

			.chat-list-items{
				flex: 1;
			}
		}
	}
</style>
