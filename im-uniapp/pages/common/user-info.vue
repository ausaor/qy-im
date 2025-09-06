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
    <uni-popup ref="popup" type="center" :mask-click="true" @maskClick="closePopup">
      <view class="custom-popup">
        <view class="popup-header">提示: 对方开启了好友验证,等待对方同意后才能成为好友</view>
        <view class="popup-content">
          <input class="input-field" v-model="friendRequestRemark" placeholder="请输入备注信息" type="text" focus>
        </view>
        <view class="popup-footer">
          <button class="popup-btn cancel-btn" @click="closePopup">取消</button>
          <button class="popup-btn confirm-btn" @click="confirmInput">确定</button>
        </view>
      </view>
    </uni-popup>
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
      friendRequestRemark: '',
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
      if (this.userInfo.friendReview) {
        this.friendRequestRemark = '我是' + this.mine.nickName;
        this.openPopup();
      } else {
        let param = {
          friendId: this.userInfo.id,
          remark: '我是' + this.mine.nickName
        }
        this.friendRequest(param);
      }
		},
    friendRequest(param) {
      this.$http({
        url: "/friend/add",
        method: "POST",
        data: param
      }).then((data) => {
        if (data === 1) {
          uni.showToast({
            title: "申请成功，请等待对方通过",
            icon: "none"
          })
        } else if (data === 2) {
          uni.showToast({
            title: "添加成功，对方已成为您的好友",
            icon: "none"
          })
        }
      }).finally(() => {
        this.closePopup();
      })
    },
    closePopup() {
      this.friendRequestRemark = '';
      this.$refs.popup.close();
    },
    openPopup() {
      this.$refs.popup.open();
    },
    confirmInput() {
      let param = {
        friendId: this.friendInfo.id,
        remark: this.friendRequestRemark
      }
      this.friendRequest(param)
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
    mine() {
      return this.userStore.userInfo;
    },
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

  .custom-popup {
    margin: auto;
    padding: 20px;
    width: 80%;
    border-radius: 12px;
    background-color: #fff;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  }

  .popup-header {
    text-align: center;
    font-size: 20px;
    font-weight: bold;
    color: #2c3e50;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #eee;
  }

  .popup-content {
    margin-bottom: 25px;
  }

  .popup-footer {
    display: flex;
    justify-content: space-between;
  }

  .popup-btn {
    flex: 1;
    margin: 0 10px;
    border-radius: 50px;
    padding: 10px;
    font-size: 16px;
    transition: all 0.3s ease;
  }

  .cancel-btn {
    background-color: #e74c3c;
    color: white;
    box-shadow: 0 4px 10px rgba(231, 76, 60, 0.3);
  }

  .confirm-btn {
    background: linear-gradient(45deg, #2ecc71, #27ae60);
    color: white;
    box-shadow: 0 4px 10px rgba(46, 204, 113, 0.3);
  }

  .popup-btn:active {
    transform: translateY(2px);
  }

  .input-field {
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 12px 15px;
    font-size: 16px;
    transition: border-color 0.3s;
  }

  .input-field:focus {
    border-color: #3498db;
    outline: none;
    box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
  }
}
</style>