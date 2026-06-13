<template>
	<view class="tab-page">
		<nav-bar search @search="onSearch()" :theme-index="0">消息</nav-bar>
		<view v-if="loading" class="chat-loading">
      <custom-loading :size="50" :mask="false">
        <view>消息接收中...</view>
      </custom-loading>
		</view>
		<view v-if="initializing" class="chat-loading">
      <custom-loading :size="50" :mask="false">
        <view>正在初始化...</view>
      </custom-loading>
		</view>
		<view class="nav-bar" v-if="showSearch">
			<view class="nav-search">
				<uni-search-bar focus="true" radius="100" v-model="searchText" cancelButton="none"
					placeholder="搜索"></uni-search-bar>
			</view>
		</view>
		<view class="chat-tip" v-if="!loading && chatStore.chats.length == 0">
			温馨提示：您现在还没有任何聊天消息，快跟您的好友发起聊天吧~
		</view>
		<scroll-view class="scroll-bar" v-else scroll-with-animation="true" scroll-y="true" :style="{height: scrollViewHeight}">
			<view v-for="(chat, index) in chatStore.chats" :key="index">
				<long-press-menu v-if="isShowChat(chat)" :items="menu.items" @select="onSelectMenu($event, index)">
					<chat-item :chat="chat" :index="index" :active="menu.chatIdx == index"></chat-item>
				</long-press-menu>
			</view>
		</scroll-view>
	</view>
</template>

<script>

import NavBar from "../../components/nav-bar/nav-bar.vue";
import LongPressMenu from "../../components/long-press-menu/long-press-menu.vue";
import ChatItem from "../../components/chat-item/chat-item.vue";
import CustomLoading from "../../components/custom-loading/custom-loading.vue";

export default {
  components: {CustomLoading, ChatItem, LongPressMenu, NavBar},
	data() {
		return {
			showSearch: false,
			searchText: "",
			scrollViewHeight: '0px',
			menu: {
				show: false,
				style: "",
				chatIdx: -1,
				isTouchMove: false,
				items: [{
					key: 'DELETE',
					name: '删除该聊天',
					icon: 'trash',
					color: '#e64e4e'
				},
				{
					key: 'TOP',
					name: '置顶该聊天',
					icon: 'arrow-up'
				}
				]
			}
		}
	},
	methods: {
		onSelectMenu(item, chatIdx) {
			switch (item.key) {
				case 'DELETE':
					this.removeChat(chatIdx);
					break;
				case 'TOP':
					this.moveToTop(chatIdx);
					break;
				default:
					break;
			}
			this.menu.show = false;
		},
		removeChat(chatIdx) {
			this.chatStore.removeChat(chatIdx);
		},
		moveToTop(chatIdx) {
			this.chatStore.moveTop(chatIdx);
		},
		isShowChat(chat) {
			if (chat.delete) {
				return false;
			}
			return !this.searchText || chat.showName.includes(this.searchText)
		},
		onSearch() {
			this.showSearch = !this.showSearch;
			this.searchText = "";
		},
		refreshUnreadBadge() {
			if (this.unreadCount > 0) {
				uni.setTabBarBadge({
					index: 0,
					text: this.unreadCount + ""
				})
			} else {
				uni.removeTabBarBadge({
					index: 0,
					complete: () => {}
				})
			}
		},
    refreshRegionUnreadBadge() {
      if (this.regionUnreadCount > 0) {
        uni.setTabBarBadge({
          index: 1,
          text: this.regionUnreadCount + ""
        })
      } else {
        uni.removeTabBarBadge({
          index: 1,
          complete: () => {}
        })
      }
    },
    refreshOtherUnreadBadge() {
      if (this.friendRequestCount > 0 || this.notifyCount > 0 || this.unreadUserCount > 0 || this.receivedGroupRequestsCount > 0 || this.joinGroupRequestsCount > 0 || this.starSpaceNotifyCount > 0) {
        uni.setTabBarBadge({
          index: 2,
          text: this.friendRequestCount + this.notifyCount + this.unreadUserCount + this.receivedGroupRequestsCount + this.joinGroupRequestsCount  + this.starSpaceNotifyCount + ""
        })
      } else {
        uni.removeTabBarBadge({
          index: 2,
          complete: () => {}
        })
      }
    },
	},
	computed: {
    mine() {
      return this.userStore.userInfo;
    },
		unreadCount() {
			let count = 0;
			this.chatStore.chats.forEach(chat => {
				if (!chat.delete) {
					count += chat.unreadMsgCount;
				}
			})
			return count;
		},
    regionUnreadCount() {
      let count = 0;
      this.regionStore.regionChats.forEach(chat => {
        if (!chat.delete) {
          count += chat.unreadMsgCount;
        }
      })
      return count;
    },
    friendRequestCount() {
      return this.friendStore.friendRequests.filter((r) => r.recvId === this.mine.id && r.status === 1).length
    },
    receivedGroupRequestsCount() {
      return this.groupStore.groupRequests.filter((r) => r.userId === this.mine.id && r.status === 1 && r.type === 2).length
    },
    joinGroupRequestsCount() {
      // 群组申请(当前用户是群主，待审核的加群申请)
      return this.groupStore.groupRequests
          .filter((r) => r.groupOwnerId === this.mine.id && r.status === 1 && r.type === 1).length;
    },
    unreadUserCount() {
      return this.talkStore.unreadUserList.length;
    },
    notifyCount() {
      return this.talkStore.notifyCount;
    },
    starSpaceNotifyCount() {
      return this.talkStore.getTotalCharacterNotifyCount();
    },
		loading() {
			return this.chatStore.isLoading();
		},
		initializing(){
      return !this.configStore.appInit;
		}
	},
	watch: {
		unreadCount(newCount, oldCount) {
			this.refreshUnreadBadge();
		},
    regionUnreadCount(newCount, oldCount) {
      this.refreshRegionUnreadBadge();
    },
    friendRequestCount(newCount, oldCount) {
      this.refreshOtherUnreadBadge();
    },
    receivedGroupRequestsCount(newCount, oldCount) {
      this.refreshOtherUnreadBadge();
    },
    joinGroupRequestsCount(newCount, oldCount) {
      this.refreshOtherUnreadBadge();
    },
    unreadUserCount(newCount, oldCount) {
      this.refreshOtherUnreadBadge();
    },
    notifyCount(newCount, oldCount) {
      this.refreshOtherUnreadBadge();
    },
    starSpaceNotifyCount(newCount, oldCount) {
      this.refreshOtherUnreadBadge();
    }
	},
	onShow() {
		this.refreshUnreadBadge();
    this.refreshRegionUnreadBadge();
    this.refreshOtherUnreadBadge();
		// 设置 scroll-view 的高度
		this.$nextTick(() => {
			const query = uni.createSelectorQuery().in(this);
			query.select('.tab-page').boundingClientRect(data => {
				if (data) {
					// 减去底部 tabBar 的高度（通常为 50px 或 100rpx）
					const tabBarHeight = 50; // px
					this.scrollViewHeight = (data.height - tabBarHeight) + 'px';
				}
			}).exec();
		});
	}
}
</script>

<style  lang="scss">
.tab-page {
	position: relative;
	display: flex;
	flex-direction: column;

	.chat-tip {
		padding: 50rpx;
		line-height: 50rpx;
		text-align: center;
		color: $im-text-color-lighter;
	}

	.chat-loading {
		display: block;
		width: 100%;
		height: 120rpx;
		background: white;

		color: $im-text-color-lighter;

		.loading-box {
			position: relative;
		}
	}

	.scroll-bar {
		flex: 1;
		height: 100%;
		/* 确保在移动端浏览器中可以滚动 */
		overflow-y: auto;
		-webkit-overflow-scrolling: touch;
	}
}
</style>