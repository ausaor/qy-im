<template>
	<view class="page mine">
		<nav-bar>我的</nav-bar>
		<uni-card :is-shadow="false" is-full :border="false">
			<view class="content" @click="onModifyInfo()">
				<head-image :name="userInfo.nickName" :url="userInfo.headImage" :size="160"></head-image>
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
		<bar-group>
			<arrow-bar title="修改密码" icon="icon-modify-pwd"  @tap="onModifyPassword()"></arrow-bar>
      <view class="activity-space" @tap="onActivitySpace()">
        <text class="icon iconfont icon-shejiaotubiao-40" style="color: #f5be3f"></text>
        <text class="title">
          动态空间
          <uni-badge v-show="notifyCount" class="uni-badge-left-margin" :text="notifyCount" type="primary"
                     :customStyle="{background: '#f56c6c'}" /></text>
        <text class="count" v-show="unreadUserCount">{{ unreadUserCount }}人新发表</text>
        <view class="new-talk-user">
          <head-image v-for="(talk, index) in talkList" :key="index" :url="talk.avatar" :name="talk.nickName" :size="45"></head-image>
        </view>
        <uni-icons class="arrow" type="right" size="16"></uni-icons>
      </view>
		</bar-group>
		<bar-group>
			<btn-bar title="退出登录" type="danger" @tap="onQuit()"></btn-bar>
		</bar-group>
	</view>
</template>

<script>
export default {
	data() {
		return {}
	},
	methods: {
		onModifyInfo() {
			uni.navigateTo({
				url: "/pages/mine/mine-edit"
			})
		},
		onModifyPassword() {
			uni.navigateTo({
				url: "/pages/mine/mine-password"
			})
		},
    onActivitySpace() {
      if (this.unreadUserCount > 0 || this.notifyCount > 0) {
        if (this.notifyCount > 0) {
          this.readedTalkNotify();
        }
        this.talkStore.resetUnreadTalkInfo();
      }
      uni.navigateTo({
        url: "/pages/activity/activity-space?category=private&section=my-friends"
      })
    },
    readedTalkNotify() {
      this.$http({
        url: `/talk-notify/readed?category=private`,
        method: 'post'
      }).then(() => {

      })
    },
		onQuit() {
			uni.showModal({
				title: '确认退出?',
				success: (res) => {
					if (res.confirm) {
						console.log(getApp())
						getApp().$vm.exit()
					}
				}
			});
		}
	},
	computed: {
		userInfo() {
			return this.userStore.userInfo;
		},
    unreadUserCount() {
      return this.talkStore.unreadUserList.length;
    },
    notifyCount() {
      return this.talkStore.notifyCount;
    },
    talkList() {
      return this.talkStore.lastTalks;
    }
	}
}
</script>

<style scoped lang="scss">
.mine {
	.content {
		//height: 200rpx;
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

		.info-arrow {
			width: 50rpx;
			font-size: 30rpx;
			position: relative;
			left: 30rpx;
		}
	}

  .activity-space {
    width: 100%;
    height: 90rpx;
    font-size: $im-font-size;
    color: $im-text-color;
    margin-top: 5rpx;
    background-color: white;
    line-height: 90rpx;
    display: flex;

    .icon {
      margin-left: 40rpx;
    }

    .title {
      flex: 1;
      margin-left: 10rpx;
    }

    .notifyCount {
      margin-right: 10rpx;
      display: flex;
      align-items: center;
    }

    .count {
      color: red;
    }

    .new-talk-user {
      display: flex;
      align-items: center;
    }

    .arrow {
      margin-right: 40rpx;
    }
  }
}
</style>
