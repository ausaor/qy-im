<template>
	<view class="page user-info">
		<nav-bar back>用户信息</nav-bar>
		<uni-card :is-shadow="false" is-full :border="false">
			<view class="content">
				<head-image :name="userInfo.nickName" :url="userInfo.headImage" :size="160"
					@click="onShowFullImage()"></head-image>

				<view class="info-item">
					<view class="info-primary">
						<text class="info-username">
							{{ userInfo.userName }}
						</text>
						<text v-show="userInfo.sex == 0" class="iconfont icon-man" color="darkblue"></text>
						<text v-show="userInfo.sex == 1" class="iconfont icon-girl" color="darkred"></text>
					</view>
					<view class="info-text">
						<text class="label-text">
							昵称:
						</text>
						<text class="content-text">
							{{ userInfo.nickName }}
						</text>
					</view>
					<view class="info-text">
						<view>
							<text class="label-text">
								签名:
							</text>
							<text class="content-text">
								{{ userInfo.signature }}
							</text>
						</view>
					</view>
				</view>
			</view>
		</uni-card>
		<bar-group v-if="!showFriendRequest">
			<btn-bar v-show="isFriend" type="primary" title="发送消息" @tap="onSendMessage()">
			</btn-bar>
			<btn-bar v-show="!isFriend" type="primary" title="加为好友" @tap="onAddFriend()"></btn-bar>
			<btn-bar v-show="isFriend" type="danger" title="删除好友" @tap="onDelFriend()"></btn-bar>
		</bar-group>
    <view class="friend-request-info" v-if="showFriendRequest">
      <bar-group>
        <view class="info-item">
          <view class="label">对方请求添加您为好友</view>
          <view class="value">{{ $date.formatDateTime(friendRequestInfo?.applyTime) }}</view>
        </view>
        <view class="remark-item">
          <view class="remark-text">{{friendRequestInfo?.sendNickName + ': ' + friendRequestInfo?.remark}}</view>
        </view>
      </bar-group>
      <bar-group>
        <btn-bar type="primary" title="同意" @tap="approveFriendRequest"></btn-bar>
        <btn-bar type="danger" title="拒绝" @tap="rejectFriendRequest"></btn-bar>
      </bar-group>
    </view>
    <view class="user-space">
      <view class="activity-space" @click="toFriendSpace">
        <svg-icon icon-class="shejiaotubiao-40" style="width: 60rpx;height: 60rpx;"></svg-icon>
        <text class="label">空间</text>
      </view>
    </view>
	</view>
</template>

<script>
import SvgIcon from "../../components/svg-icon/svg-icon.vue";

export default {
  components: {SvgIcon},
	data() {
		return {
			userInfo: {},
      showFriendRequest: false,
      requestId: '',
      friendRequestInfo: {},
		}
	},
	methods: {
		onShowFullImage() {
			let imageUrl = this.userInfo.headImage;
			if (imageUrl) {
				uni.previewImage({
					urls: [imageUrl]
				})
			}
		},
		onSendMessage() {
			let chat = {
				type: 'PRIVATE',
				targetId: this.userInfo.id,
				showName: this.userInfo.nickName,
				headImage: this.userInfo.headImage,
			};
			this.chatStore.openChat(chat);
			let chatIdx = this.chatStore.findChatIdx(chat);
			uni.navigateTo({
				url: "/pages/chat/chat-box?chatIdx=" + chatIdx
			})
		},
		onAddFriend() {
			this.$http({
				url: "/friend/add?friendId=" + this.userInfo.id,
				method: "POST"
			}).then((data) => {
				let friend = {
					id: this.userInfo.id,
					nickName: this.userInfo.nickName,
					headImage: this.userInfo.headImage,
					online: this.userInfo.online
				}
				this.friendStore.addFriend(friend);
				uni.showToast({
					title: '对方已成为您的好友',
					icon: 'none'
				})
			})
		},
		onDelFriend() {
			uni.showModal({
				title: "确认删除",
				content: `确认删除 '${this.userInfo.nickName}',并删除聊天记录吗?`,
				success: (res) => {
					if (res.cancel)
						return;
					this.$http({
						url: `/friend/delete/${this.userInfo.id}`,
						method: 'delete'
					}).then((data) => {
						this.friendStore.removeFriend(this.userInfo.id);
						this.chatStore.removePrivateChat(this.userInfo.id);
						uni.showToast({
							title: `与 '${this.userInfo.nickName}'的好友关系已解除`,
							icon: 'none'
						})
					})
				}
			})
		},
		updateFriendInfo() {
			// store的数据不能直接修改，深拷贝一份store的数据
			let friend = JSON.parse(JSON.stringify(this.friendInfo));
			friend.headImage = this.userInfo.headImage;
			friend.nickName = this.userInfo.nickName;
			this.$http({
				url: "/friend/update",
				method: "PUT",
				data: friend
			}).then(() => {
				// 更新好友列表中的昵称和头像
				this.friendStore.updateFriend(friend);
				// 更新会话中的头像和昵称
				this.chatStore.updateChatFromFriend(this.userInfo);
			})
		},
		loadUserInfo(id) {
			this.$http({
				url: "/user/find/" + id,
				method: 'GET'
			}).then((user) => {
				this.userInfo = user;
				// 如果发现好友的头像和昵称改了，进行更新
				if (this.isFriend && (this.userInfo.headImage != this.friendInfo.headImage ||
					this.userInfo.nickName != this.friendInfo.nickName)) {
					this.updateFriendInfo()
				}
			})
		},
    toFriendSpace() {
      uni.navigateTo({
        url: `/pages/activity/activity-space?category=private&section=friend&friendId=${this.userInfo.id}&spaceTitle=好友空间`
      })
    },
    approveFriendRequest() {
      this.$http({
        url: `/friend/request/approve?id=${this.requestId}`,
        method: "post",
      }).then(() => {
        this.showFriendRequest = false;
        uni.showToast({
          title: this.userInfo.nickName + '已成为您的好友',
          icon: 'none'
        })
      })
    },
    rejectFriendRequest() {
      this.$http({
        url: `/friend/request/reject?id=${this.requestId}`,
        method: "post",
      }).then(() => {
        this.showFriendRequest = false;
      })
    }
	},
	computed: {
		isFriend() {
			return this.friendStore.friends.some((f) => f.id === this.userInfo.id && !f.deleted);
		},
		friendInfo() {
			return this.friendStore.friends.find((f) => f.id === this.userInfo.id);
		}
	},
	onLoad(options) {
		// 查询用户信息
		this.loadUserInfo(options.id);
    if (options.requestId) {
      this.requestId = options.requestId;
      this.friendRequestInfo = this.friendStore.findFriendRequest(parseInt(options.requestId));
      this.showFriendRequest = true;
    }
	}
}
</script>

<style lang="scss" scoped>
.user-info {
	.content {
		height: 200rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 20rpx;

		.info-item {
			display: flex;
			align-items: flex-start;
			flex-direction: column;
			padding-left: 40rpx;
			flex: 1;

			.info-text {
				line-height: 1.5;
				//margin-bottom: 10rpx;
			}

			.label-text {
				font-size: $im-font-size-small;
				color: $im-text-color-light;

			}

			.content-text {
				font-size: $im-font-size-small;
				color: $im-text-color-light;
			}

			.info-primary {
				display: flex;
				align-items: center;
				margin-bottom: 10rpx;

				.info-username {
					font-size: $im-font-size-large;
					font-weight: 600;
				}

				.icon-man {
					color: $im-text-color;
					font-size: $im-font-size-large;
					padding-left: 10rpx;
				}

				.icon-girl {
					color: darkred;
				}
			}
		}
	}

  .friend-request-info {

    .info-item {
      display: flex;
      background-color: #fff;
      padding: 0 1.25rem;

      .label {
        line-height: 3.125rem;
        font-size: .9375rem;
      }

      .value {
        flex: 1;
        text-align: right;
        line-height: 3.125rem;
        color: #909399;
        font-size: .875rem;
      }
    }

    .remark-item {
      background-color: #fff;
      padding: 0 1.25rem;

      .remark-text {
        flex: 1;
        line-height: 1.875rem;
        color: #909399;
        font-size: .9375rem;
      }
    }
  }

  .user-space {
    width: 100%;
    padding: 10rpx 20rpx;
    background-color: white;

    .activity-space {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10rpx;

      .label {
        color: #35567f;
        font-size: 32rpx;
        font-weight: 600;
      }
    }
  }
}
</style>