<template>
	<view class="page group-info">
		<nav-bar back>群聊信息</nav-bar>
		<view v-if="!group.quit" class="group-members">
			<view class="member-items">
				<view v-for="(member, idx) in groupMembers" :key="idx">
					<view class="member-item" v-if="idx < 9">
						<head-image :id="member.userId" :name="member.aliasName" :url="member.headImage" size="small"
							:online="member.online"></head-image>
						<view class="member-name">
							<text>{{ member.aliasName }}</text>
						</view>
					</view>
				</view>
				<view class="invite-btn" @click="onInviteMember()">
					<uni-icons type="plusempty" size="20" color="#888888"></uni-icons>
				</view>
        <view class="view-btn" @click="viewGroupMemberInfo" v-if="group.groupType!==0">
          <uni-icons type="search" size="20" color="#888888"></uni-icons>
        </view>
			</view>
			<view class="member-more" @click="onShowMoreMmeber()">{{ `查看全部群成员${groupMembers.length}人` }}></view>
		</view>
		<view class="form">
			<view class="form-item">
				<view class="label">群聊名称</view>
				<view class="value">{{group.name}}</view>
			</view>
			<view class="form-item">
				<view class="label">群主</view>
				<view class="value">{{ownerName}}</view>
			</view>
			<view class="form-item">
				<view class="label">群名备注</view>
				<view class="value">{{group.remark}}</view>
			</view>
			<view class="form-item">
				<view class="label">我在本群的昵称</view>
				<view class="value">{{group.aliasName}}</view>
			</view>
			<view v-if="group.notice" class="form-item" >
				<view class="label">群公告</view>
			</view>
			<view v-if="group.notice" class="form-item" >
				<uni-notice-bar :text="group.notice" />
			</view>
			
			<view v-if="!group.quit" class="group-edit" @click="onEditGroup()">修改群聊资料 > </view>
		</view>
		
		<bar-group v-if="!group.quit">
			<btn-bar type="primary" title="发送消息" @tap="onSendMessage()"></btn-bar>
			<btn-bar v-if="!isOwner" type="danger" title="退出群聊" @tap="onQuitGroup()"></btn-bar>
			<btn-bar v-if="isOwner" type="danger" title="解散群聊" @tap="onDissolveGroup()"></btn-bar>
			<btn-bar v-if="isOwner" type="primary" title="切换模板群聊" @tap="switchGroupType(1)"></btn-bar>
		</bar-group>
    <group-member-list ref="groupMemberList" :members="groupMembers"></group-member-list>
	</view>
</template>

<script>
import GroupTemplateList from "../../components/group-template-list/group-template-list.vue";
import GroupMemberList from "../../components/group-member-list/group-member-list.vue";

export default {
  components: {GroupMemberList, GroupTemplateList},
	data() {
		return {
			groupId: null,
			group: {},
			groupMembers: []
		}
	},
	methods: {
		onFocusSearch() {},
		onInviteMember() {
			uni.navigateTo({
				url: `/pages/group/group-invite?id=${this.groupId}`
			})
		},
		onShowMoreMmeber() {
			uni.navigateTo({
				url: `/pages/group/group-member?id=${this.groupId}`
			})
		},
		onEditGroup() {
			uni.navigateTo({
				url: `/pages/group/group-edit?id=${this.groupId}`
			})
		},
		onSendMessage() {
			let chat = {
				type: 'GROUP',
				targetId: this.group.id,
				showName: this.group.remark,
				headImage: this.group.headImage,
			};
			this.chatStore.openChat(chat);
			let chatIdx = this.chatStore.findChatIdx(chat);
			uni.navigateTo({
				url: "/pages/chat/chat-box?chatIdx=" + chatIdx
			})
		},
		onQuitGroup() {
			uni.showModal({
				title: '确认退出?',
				content: `退出群聊后将不再接受群里的消息，确认退出吗?`,
				success: (res) => {
					if (res.cancel)
						return;
					this.$http({
						url: `/group/quit/${this.groupId}`,
						method: 'DELETE'
					}).then(() => {
						uni.showModal({
							title: `退出成功`,
							content: `您已退出群聊'${this.group.name}'`,
							showCancel: false,
							success: () => {
								setTimeout(() => {
									uni.switchTab({
										url: "/pages/group/group"
									});
									this.groupStore.removeGroup(this.groupId);
									this.chatStore.removeGroupChat(this
										.groupId);
								}, 100)
							}
						})
					});
				}
			});
		},
		onDissolveGroup() {
			uni.showModal({
				title: '确认解散?',
				content: `确认要解散群聊'${this.group.name}'吗?`,
				success: (res) => {
					if (res.cancel)
						return;
					this.$http({
						url: `/group/delete/${this.groupId}`,
						method: 'delete'
					}).then(() => {
						uni.showModal({
							title: `解散成功`,
							content: `群聊'${this.group.name}'已解散`,
							showCancel: false,
							success: () => {
								setTimeout(() => {
									uni.switchTab({
										url: "/pages/group/group"
									});
									this.groupStore.removeGroup(this.groupId);
									this.chatStore.removeGroupChat(this
										.groupId);
								}, 100)
							}
						})
					});
				}
			});

		},
		loadGroupInfo() {
			this.$http({
				url: `/group/find/${this.groupId}`,
				method: 'GET'
			}).then((group) => {
				this.group = group;
				// 更新聊天页面的群聊信息
				this.chatStore.updateChatFromGroup(group);
				// 更新聊天列表的群聊信息
				this.groupStore.updateGroup(group);

			});
		},
		loadGroupMembers() {
			console.log("loadGroupMembers")
			this.$http({
				url: `/group/members/${this.groupId}`,
				method: "GET"
			}).then((members) => {
				this.groupMembers = members.filter(m => !m.quit);
			})
		},
    viewGroupMemberInfo() {
      this.$refs.groupMemberList.open();
    },
    switchGroupType(toGroupType) {
      uni.navigateTo({
        url: `/pages/group/group-switch?id=${this.groupId}&toGroupType=${toGroupType}`
      })
    }
	},
	computed: {
		ownerName() {
			let member = this.groupMembers.find((m) => m.userId == this.group.ownerId);
			return member && member.aliasName;
		},
		isOwner() {
			return this.group.ownerId == this.userStore.userInfo.id;
		}
	},
	onLoad(options) {
		this.groupId = options.id;
		// 查询群聊信息
		this.loadGroupInfo(options.id);
		// 查询群聊成员
		this.loadGroupMembers(options.id)
	}

}
</script>

<style lang="scss">
.group-info {
	.group-members {
		padding: 30rpx;
		background: white;

		.member-items {
			display: flex;
			flex-wrap: wrap;

			.member-item {
				width: 120rpx;
				display: flex;
				flex-direction: column;
				margin: 8rpx 2rpx;
				position: relative;
				align-items: center;
				padding-right: 5px;
				white-space: nowrap;

				.member-name {
					width: 100%;
					flex: 1;
					overflow: hidden;
					text-align: center;
					white-space: nowrap;
					padding-top: 8rpx;
					font-size: $im-font-size-smaller;
				}
			}

			.invite-btn {
				display: flex;
				justify-content: center;
				align-items: center;
				width: 86rpx;
				height: 86rpx;
				margin: 10rpx;
				border: $im-border solid 2rpx;
				border-radius: 10%;
			}

      .view-btn {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 86rpx;
        height: 86rpx;
        margin: 10rpx;
        border: $im-border solid 2rpx;
        border-radius: 10%;
      }
		}

		.member-more {
			padding-top: 24rpx;
			text-align: center;
			font-size: $im-font-size-small;
			color: $im-text-color-lighter;
		}
	}

	.form {
		margin-top: 20rpx;

		.form-item {
			padding: 0 40rpx;
			display: flex;
			background: white;
			align-items: center;
			margin-top: 2rpx;
			
			.label {
				width: 220rpx;
				line-height: 100rpx;
				font-size: $im-font-size;
				white-space: nowrap;
			}
	
			.value {
				flex: 1;
				text-align: right;
				line-height: 100rpx;
				color: $im-text-color-lighter;
				font-size: $im-font-size-small;
				white-space: nowrap;
				overflow: hidden;
			}
		}
		
		.group-edit {
			padding: 10rpx 40rpx 30rpx 40rpx	;
			text-align: center;
			background: white;
			font-size: $im-font-size-small;
			color: $im-text-color-lighter;
		}
	}
}
</style>