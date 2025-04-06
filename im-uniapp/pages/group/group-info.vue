<template>
	<view class="page group-info">
		<nav-bar back home>群聊信息</nav-bar>
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
        <view class="upload-btn" v-if="group.groupType===0">
          <image-upload :onSuccess="onUploadMemberAvatarSuccess" class="upload-image">
            <image v-if="myGroupMemberInfo.headImage" :src="myGroupMemberInfo.headImage" class="head-image"></image>
            <uni-icons v-else type="cloud-upload" size="40" color="#888888"></uni-icons>
          </image-upload>
          <text style="font-size: 26rpx;">上传</text>
        </view>
				<view class="invite-btn" @click="onInviteMember()">
					<uni-icons type="plusempty" size="20" color="#888888"></uni-icons>
				</view>
        <view class="view-btn" @click="viewGroupMemberInfo" v-if="group.groupType!==0">
          <uni-icons type="search" size="20" color="#888888"></uni-icons>
        </view>
        <view class="view-btn" v-if="group.groupType!==0" @click="switchCharacter">
          <uni-icons type="loop" size="20" color="#888888"></uni-icons>
        </view>
        <view class="view-btn" v-if="group.groupType!==0" @click="switchCharacterAvatar">
          <uni-icons type="person" size="20" color="#888888"></uni-icons>
        </view>
        <view class="view-btn" @click="toGroupSpace">
          <svg-icon icon-class="shejiaotubiao-40" style="width: 86rpx;height: 86rpx;"></svg-icon>
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
		</bar-group>
    <view v-if="!group.quit && isOwner" class="group-types">
      <view class="group-type-item" style="color: rgb(0,47,167)" @click="switchGroupType(1)">切换模板群聊</view>
      <view class="group-type-item" style="color: rgb(176,89,35)" @click="switchGroupType(4)">切换模板角色群聊</view>
      <view class="group-type-item" style="color: rgb(144,0,33)" v-if="group.groupType !== 3" @click="switchGroupType(3)">切换角色群聊</view>
      <view class="group-type-item" style="color: rgb(105,149,114)" v-if="group.groupType !== 2" @click="switchGroupType(2)">切换多元角色群聊</view>
      <view class="group-type-item" v-if="group.groupType !== 0" @click="showSwitchCommonGroupPopup">切换普通群聊</view>
    </view>
    <group-member-list ref="groupMemberList" :members="groupMembers"></group-member-list>
    <group-template-list ref="groupTemplateList" :group-templates="templateGroupList" @confirm="chooseGroupTemplate"></group-template-list>
    <character-list ref="characterList" :characters="characters" @confirm="chooseCharacter"></character-list>
    <character-avatar-list ref="characterAvatarList" :characterAvatars="characterAvatars" @confirm="chooseCharacterAvatar"></character-avatar-list>
    <up-popup :show="showSwitchCommonGroup" mode="center" @close="closeCommonPopup" :customStyle="{width: '90%', height: '500rpx', borderRadius: '18rpx'}">
      <view class="common-form">
        <view class="form-item">
          <view class="label">群聊头像</view>
          <view class="value">
            <image-upload :onSuccess="onUnloadImageSuccess">
              <image v-if="myGroupMemberInfo.headImage" :src="group.headImage" class="group-image"></image>
            </image-upload>
          </view>
        </view>
        <view class="form-item">
          <view class="label">群聊名称</view>
          <view class="value">
            <input class="input" maxlength="20"  v-model="group.name"  placeholder="请输入群聊名称"/>
          </view>
        </view>
        <view class="btns">
          <up-button  text="取消" @click="() => showSwitchCommonGroup = false"></up-button>
          <up-button type="primary" text="确定" @click="switchCommonGroup"></up-button>
        </view>
      </view>
    </up-popup>
	</view>
</template>

<script>
import GroupTemplateList from "../../components/group-template-list/group-template-list.vue";
import GroupMemberList from "../../components/group-member-list/group-member-list.vue";
import CharacterList from "../../components/character-list/character-list.vue";
import CharacterAvatarList from "../../components/character-avatar-list/character-avatar-list.vue";
import SvgIcon from "../../components/svg-icon/svg-icon.vue";

export default {
  components: {SvgIcon, CharacterAvatarList, CharacterList, GroupMemberList, GroupTemplateList},
	data() {
		return {
			groupId: null,
			group: {},
			groupMembers: [],
      myGroupMemberInfo: {},
      showSwitchCommonGroup: false,
      templateGroupList: [],
      characters: [],
      characterAvatars: [],
      activeGroupTemplateId: null,
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
    toGroupSpace() {
      uni.navigateTo({
        url: `/pages/activity/activity-space?category=group&section=group&groupId=${this.groupId}&spaceTitle=群空间动态`
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
									uni.navigateTo({
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
									uni.navigateTo({
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
        this.myGroupMemberInfo = this.groupMembers.find((m) => m.userId === this.mine.id);
			})
		},
    viewGroupMemberInfo() {
      this.$refs.groupMemberList.open();
    },
    switchGroupType(toGroupType) {
      uni.navigateTo({
        url: `/pages/group/group-switch?id=${this.groupId}&toGroupType=${toGroupType}`
      })
    },
    showSwitchCommonGroupPopup() {
      this.showSwitchCommonGroup = true;
    },
    closeCommonPopup() {
      this.showSwitchCommonGroup = false;
    },
    switchCommonGroup() {
      let paramVO = {
        groupId: this.group.id,
        name: this.group.name,
        avatar: this.group.headImage
      }

      this.$http({
        url: "/group/switchCommonGroup",
        method: 'post',
        data: paramVO
      }).then((group) => {
        this.loadGroupInfo();
        this.loadGroupMembers();
      }).finally(() =>{
        this.showSwitchCommonGroup = false;
      })
    },
    onUnloadImageSuccess(file, res) {
      this.group.headImage = res.data.originUrl;
      this.group.headImage = res.data.thumbUrl;
    },
    onUploadMemberAvatarSuccess(file, res) {
      this.myGroupMemberInfo.headImage = res.data.originUrl;
      this.group.memberHeadImage = res.data.originUrl;
      this.modifyGroup(this.group);
    },
    async switchCharacter() {
      if (this.group.groupType === 1 || this.group.groupType === 4) {
        this.activeGroupTemplateId = this.group.templateGroupId;
        await this.queryCharacterList(this.group.templateGroupId);
        this.$refs.characterList.open();
      } else if (this.group.groupType === 2 || this.group.groupType === 3) {
        await this.queryGroupTemplateList();
        this.$refs.groupTemplateList.open();
      }
    },
    async switchCharacterAvatar() {
      await this.queryCharacterAvatars(this.group.templateCharacterId);
      this.$refs.characterAvatarList.open();
    },
    async queryCharacterAvatars(templateCharacterId) {
      await this.$http({
        url: `/characterAvatar/list/${templateCharacterId}`,
        method: 'get'
      }).then((data) => {
        this.characterAvatars = data;
      });
    },
    async queryGroupTemplateList() {
      await this.$http({
        url: "/templateGroup/list",
        method: 'get',
        params: ''
      }).then(data => {
        this.templateGroupList = data;
      })
    },
    async queryCharacterList(templateGroupId) {
      await this.$http({
        url: `/templateCharacter/list/${templateGroupId}`,
        method: 'get'
      }).then(result => {
        this.characters = result;
      });
    },
    async chooseGroupTemplate(groupTemplate) {
      this.activeGroupTemplateId = groupTemplate.id;
      await this.queryCharacterList(groupTemplate.id);
      this.$refs.characterList.open();
    },
    chooseCharacter(character) {
      let groupMemberVO = {
        groupId: this.group.id,
        templateGroupId: this.activeGroupTemplateId,
        templateCharacterId: character.id,
        templateCharacterName: character.name,
        templateCharacterAvatar: character.avatar,
        groupType: this.group.groupType,
      }
      this.$http({
        url: "/group/member/switchTemplateCharacter",
        method: 'post',
        data: groupMemberVO
      }).then(() => {
        uni.showToast({
          title: "切换成功",
          icon: 'none'
        });
        this.loadGroupInfo();
        this.loadGroupMembers();
      }).finally(() =>{
      })
    },
    chooseCharacterAvatar(characterAvatar) {
      let switchCharacterAvatarVO = {
        groupId: this.group.id,
        groupType: this.group.groupType,
        templateGroupId: this.group.templateGroupId,
        templateCharacterId: characterAvatar.templateCharacterId,
        characterAvatarId: characterAvatar.id,
      }
      this.$http({
        url: "/group/member/switchCharacterAvatar",
        method: 'post',
        data: switchCharacterAvatarVO
      }).then(() => {
        uni.showToast({
          title: "切换成功",
          icon: 'none'
        });
        this.loadGroupMembers();
        this.loadGroupInfo();
      }).finally(() =>{

      })
    },
    modifyGroup(data) {
      this.$http({
        url: "/group/modify",
        method: "put",
        data: data
      }).then((group) => {
        this.groupStore.updateGroup(group);
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
		},
    mine() {
      return this.userStore.userInfo;
    },
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
      align-items: center;
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

      .upload-btn {
        width: 120rpx;
        display: flex;
        flex-direction: column;
        position: relative;
        align-items: center;
        white-space: nowrap;

        .upload-image {
          width: 86rpx;
          height: 86rpx;
          border: $im-border solid 2rpx;
          border-radius: 50%;

          display: flex;
          justify-content: center;
          align-items: center;

          .head-image {
            width: 86rpx;
            height: 86rpx;
            border-radius: 50%;
          }
        }
      }
		}

		.member-more {
			padding-top: 24rpx;
			text-align: center;
			font-size: $im-font-size-small;
			color: $im-text-color-lighter;
		}
	}

  .group-types {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10rpx;
    margin-bottom: 20rpx;

    .group-type-item {
      padding: 20rpx 20rpx;
      background-color: white;
      text-align: left;
      color: #35567f;
      font-size: 32rpx;
      font-weight: 600;
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

  .common-form {
    margin-top: 60rpx;
    width: 100%;

    .form-item {
      padding: 0 40rpx;
      display: flex;
      background: white;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 10rpx;

      .label {
        line-height: 100rpx;
        font-size: $im-font-size;
        white-space: nowrap;
      }

      .value{
        white-space: nowrap;
      }

      .input {
        flex: 1;
        text-align: right;
        line-height: 100rpx;
        font-size: $im-font-size-small;
      }

      .group-image {
        width: 120rpx;
        height: 120rpx;
        border-radius: 50%;
        border: 1px solid #ccc;
      }
    }

    .btns {
      margin-top: 50rpx;
      display: flex;
      justify-content: center;
      gap: 60rpx;
      padding: 0 40rpx;
    }
  }
}
</style>