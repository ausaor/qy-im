<template>
	<view class="page group-invite">
    <nav-bar back>邀请</nav-bar>
		<view class="nav-bar">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" radius="100" cancelButton="none"
					placeholder="输入好友昵称搜索"></uni-search-bar>
			</view>
		</view>
		<view class="friend-items">
			<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="(friend, index) in friendItems" v-show="!searchText || friend.nickName.includes(searchText)"
					:key="friend.id">
					<view class="friend-item" @click="onSwitchChecked(friend)"
						:class="{ checked: friend.checked, disabled: friend.disabled }">
						<head-image :name="friend.nickName" :online="friend.online"
							:url="friend.headImage"></head-image>
						<view class="friend-name">{{ friend.nickName }}</view>
            <view class="character-info" v-if="group.groupType !== 0 && !friend.disabled">
              <view class="character-name">{{friend.templateCharacterName}}</view>
              <character-avatar :name="friend.templateCharacterName"
                          :url="friend.templateCharacterId ? friend.templateCharacterAvatar : '/static/image/default_head.png'"
                                @click.stop="selectCharacter(friend, index)">
              </character-avatar>
            </view>
					</view>
				</view>
			</scroll-view>
		</view>
    <character-list ref="characterList" :characters="characters" @confirm="chooseTemplateCharacter"></character-list>
    <group-template-list ref="groupTemplateList" :group-templates="templateGroupList" @confirm="chooseGroupTemplate"></group-template-list>
		<button class="bottom-btn" type="primary" :disabled="inviteSize == 0"
			@click="onInviteFriends()">邀请({{ inviteSize }}) </button>
	</view>
</template>

<script>
import CharacterAvatar from "../../components/character-avatar/character-avatar.vue";
import CharacterList from "../../components/character-list/character-list.vue";
import GroupTemplateList from "../../components/group-template-list/group-template-list.vue";

export default {
  components: {GroupTemplateList, CharacterList, CharacterAvatar},
	data() {
		return {
			groupId: null,
			searchText: "",
      group: {},
			groupMembers: [],
			friendItems: [],
      curSelectedFriend: {},
      selectedFriendIndex: -1,
      characters: [],
      templateGroupList: [],
		}
	},
	methods: {
		onInviteFriends() {
      let returnFlag = false;
      if (this.group.groupType !== 0) {
        this.friendItems.forEach((f) => {
          if (f.checked && !f.disabled) {
            if (!f.templateCharacterId) {
              returnFlag = true;
            }
          }
        })
        if (returnFlag) {
          uni.showToast({
            title: "请为用户分配模板角色",
            icon: 'none'
          })
          return false
        }
      }
			let inviteVo = {
				groupId: this.groupId,
				friendIds: [],
        characterInviteVOList: [],
        isTemplate: this.group.isTemplate,
        groupType: this.group.groupType,
			}
			this.friendItems.forEach((f) => {
				if (f.checked && !f.disabled) {
					inviteVo.friendIds.push(f.id);
          if (this.group.groupType !== 0) {
            let obj = {
              friendId: f.id,
              templateCharacterId: f.templateCharacterId,
              templateCharacterAvatar: f.templateCharacterAvatar,
              templateCharacterName: f.templateCharacterName
            }
            inviteVo.characterInviteVOList.push(obj);
          }
				}
			})
      if (this.group.groupType === 1 || this.group.groupType === 2) {
        const templateCharacterIds = inviteVo.characterInviteVOList.map(item => item["templateCharacterId"]);
        let templateCharacterIdSet = new Set(templateCharacterIds);
        if (templateCharacterIdSet.size !== templateCharacterIds.length) {
          uni.showToast({
            title: "存在重复的模板角色，请重新选择",
            icon: 'none'
          })
          return;
        }
      }
			if (inviteVo.friendIds.length > 0) {
				this.$http({
					url: "/group/invite",
					method: 'POST',
					data: inviteVo
				}).then(() => {
					uni.showToast({
						title: "邀请成功",
						icon: 'none'
					})
					setTimeout(() => {
						// 回退并刷新
						let pages = getCurrentPages();
						let prevPage = pages[pages.length - 2];
						prevPage.$vm.loadGroupMembers();
						uni.navigateBack();
					}, 1000);

				})
			}
		},
		onShowUserInfo(userId) {
			uni.navigateTo({
				url: "/pages/common/user-info?id=" + userId
			})
		},
		onSwitchChecked(friend) {
			if (!friend.disabled) {
				friend.checked = !friend.checked;
        if (!friend.checked) {
          friend.templateCharacterId = null;
          friend.templateCharacterAvatar = '';
          friend.templateCharacterName = null;
        }
			}
		},
		initFriendItems() {
			this.friendItems = [];
			let friends = this.friendStore.friends;
			friends.forEach((f => {
				let item = {
					id: f.id,
					headImage: f.headImage,
					nickName: f.nickName,
					online: f.online
				}
				item.disabled = this.isGroupMember(f.id);
				item.checked = item.disabled;
				this.friendItems.push(item);
			}))
		},
    loadGroup(id) {
      this.$http({
        url: `/group/find/${id}`,
        method: "GET"
      }).then((group) => {
        this.group = group;
      })
    },
		loadGroupMembers(id) {
			this.$http({
				url: `/group/members/${id}`,
				method: "GET"
			}).then((members) => {
				this.groupMembers = members.filter(m => !m.quit);
				this.initFriendItems();
			})
		},
		isGroupMember(id) {
			return this.groupMembers.some(m => m.userId == id);
		},
    async selectCharacter(friend, index) {
      friend.checked = true;
      this.selectedFriendIndex = index;
      this.curSelectedFriend = friend;
      if (this.group.groupType === 1) {
        await this.querySelectableTemplateCharacter();
        this.$refs.characterList.open();
      } else if (this.group.groupType === 2 || this.group.groupType === 3) {
          await this.queryTemplateGroup();
          this.$refs.groupTemplateList.open();
      } else if (this.group.groupType === 4) {
        await this.queryTemplateCharacter(this.group.templateGroupId);
        this.$refs.characterList.open();
      }
    },
    async queryTemplateGroup() {
      this.$http({
        url: "/templateGroup/list",
        method: 'get',
        params: ''
      }).then(data => {
        this.templateGroupList = data;
      })
    },
    async querySelectableTemplateCharacter() {
      let paramVO = {
        groupId: this.group.id,
        templateGroupId: this.group.templateGroupId
      }
      this.$http({
        url: "/templateCharacter/findSelectableTemplateCharacter",
        method: 'post',
        data: paramVO
      }).then(result => {
        this.characters = result;
      }).finally(() =>{

      })
    },
    async queryTemplateCharacter(templateGroupId) {
      this.$http({
        url: `/templateCharacter/list/${templateGroupId}`,
        method: 'get'
      }).then(result => {
        this.characters = result;
      });
    },
    chooseTemplateCharacter(character) {
      this.friendItems[this.selectedFriendIndex].templateCharacterId = character.id;
      this.friendItems[this.selectedFriendIndex].templateCharacterAvatar = character.avatar;
      this.friendItems[this.selectedFriendIndex].templateCharacterName = character.name;
    },
    async chooseGroupTemplate(groupTemplate) {
      await this.queryTemplateCharacter(groupTemplate.id);
      this.$refs.characterList.open();
    }
	},
	computed: {
		inviteSize() {
			return this.friendItems.filter(f => !f.disabled && f.checked).length;
		}
	},
	onLoad(options) {
		this.groupId = options.id;
    this.loadGroup(options.id);
		this.loadGroupMembers(options.id);
	}
}
</script>

<style lang="scss" scoped>
.group-invite {
	position: relative;
	display: flex;
	flex-direction: column;

	.friend-items {
		position: relative;
		flex: 1;
		overflow: hidden;

		.friend-item {
			height: 120rpx;
			display: flex;
			margin-bottom: 1rpx;
			position: relative;
			padding: 0 30rpx;
			align-items: center;
			background-color: white;
			white-space: nowrap;

			&.disabled {
				background-color: $im-bg-active !important;
			}

			&.checked {
				background-color: $im-color-primary-light-9;
			}

			.friend-name {
				flex: 1;
				padding-left: 20rpx;
				font-size: 30rpx;
				font-weight: 600;
				line-height: 60rpx;
				white-space: nowrap;
				overflow: hidden;
			}

      .character-info {
        display: flex;
        align-items: center;


        .character-name {
          font-size: 30rpx;
          font-weight: 600;
          line-height: 60rpx;
          white-space: nowrap;
          margin-right: 20rpx;
        }
      }
		}

		.scroll-bar {
			height: 100%;
		}
	}
}
</style>