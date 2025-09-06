<template>
	<view class="page friend-add">
		<nav-bar back>添加好友</nav-bar>
		<view class="nav-bar">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" radius="100" :focus="true" @confirm="onSearch()"
					@cancel="onCancel()" placeholder="用户名/昵称"></uni-search-bar>
			</view>
		</view>
		<view class="user-items">
			<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="(user) in users" :key="user.id" v-show="user.id != userStore.userInfo.id">
					<view class="user-item">
						<head-image :id="user.id" :name="user.nickName" :online="user.online"
							:url="user.headImage"></head-image>
						<view class="user-info">
							<view class="user-name">
								<view>{{ user.userName }}</view>
<!--								<uni-tag v-if="user.status == 1" circle type="error" text="已注销" size="small"></uni-tag>-->
							</view>
							<view class="nick-name">{{ `昵称:${user.nickName}`}}</view>
						</view>
						<view class="user-btns">
							<button type="primary" v-show="!isFriend(user.id) && !user.isManualApprove" size="mini" @click.stop="onAddFriend(user)">加为好友</button>
							<text v-show="isFriend(user.id)" class="status-tip">已添加</text>
              <text v-show="!isFriend(user.id) && user.isManualApprove" class="status-tip">等待对方验证</text>
						</view>
					</view>
				</view>
			</scroll-view>
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
export default {
	data() {
		return {
			searchText: "",
			users: [],
      friendRequestRemark: '',
      friendInfo: null,
		}
	},
	methods: {
		onCancel() {
			uni.navigateBack();
		},
		onSearch() {
			this.$http({
				url: "/user/findByName?name=" + this.searchText,
				method: "GET"
			}).then((data) => {
				this.users = data;
			})
		},
		onAddFriend(user) {
      this.friendInfo = user;
      if (user.friendReview) {
        this.friendRequestRemark = '我是' + this.userInfo.nickName;
        this.openPopup();
      } else {
        let param = {
          friendId: user.id,
          remark: '我是' + this.userInfo.nickName
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
          this.friendInfo.isManualApprove = true;
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
		onShowUserInfo(user) {
			uni.navigateTo({
				url: "/pages/common/user-info?id=" + user.id
			})
		},
		isFriend(userId) {
			return this.friendStore.friends.some((f) => f.id === userId && !f.deleted);
		},
    closePopup() {
      this.friendRequestRemark = '';
      this.friendInfo = null;
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
    }
	},
  computed: {
    userInfo() {
      return this.userStore.userInfo;
    },
  }
}
</script>

<style scoped lang="scss">
.friend-add {
	position: relative;
	display: flex;
	flex-direction: column;

	.user-items {
		position: relative;
		flex: 1;
		overflow: hidden;

		.user-item {
			height: 100rpx;
			display: flex;
			margin-bottom: 1rpx;
			position: relative;
			padding: 18rpx 20rpx;
			align-items: center;
			background-color: white;
			white-space: nowrap;

			.user-info {
				flex: 1;
				display: flex;
				flex-direction: column;
				padding-left: 20rpx;
				font-size: $im-font-size;
				white-space: nowrap;
				overflow: hidden;

				.user-name {
					display: flex;
					flex: 1;
					font-size: $im-font-size-large;
					white-space: nowrap;
					overflow: hidden;
					align-items: center;

					.uni-tag {
						text-align: center;
						margin-left: 5rpx;
						padding: 1px 5px;
					}
				}

				.nick-name {
					display: flex;
					font-size: $im-font-size-smaller;
					color: $im-text-color-lighter;
					padding-top: 8rpx;
				}
			}

      .user-btns {
        font-size: .875rem;
        color: #909399;
      }
		}

		.scroll-bar {
			height: 100%;
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