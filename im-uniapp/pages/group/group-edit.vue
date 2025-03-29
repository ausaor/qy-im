<template>
	<view  class="page group-edit">
		<nav-bar back>修改群资料</nav-bar>
		<view class="form">
			<view class="form-item">
				<view class="label">群聊头像</view>
				<view class="value"></view>
				<image-upload v-if="isOwner && groupType!==1 && groupType !==4" :onSuccess="onUnloadImageSuccess">
					<image :src="group.headImage" class="group-image"></image>
				</image-upload>
				<head-image v-else class="group-image" :name="group.remark" :url="group.headImage"
					:size="120"></head-image>
			</view>
			<view class="form-item">
				<view class="label">群聊名称</view>
				<input class="input" :class="isOwner?'':'disable'" maxlength="20"  v-model="group.name" :disabled="!isOwner || groupType===1 || groupType===4" placeholder="请输入群聊名称"/>
			</view>
			<view class="form-item">
				<view class="label">群聊备注</view>
				<input class="input" maxlength="20"  v-model="group.remark"  :placeholder="group.name"/>
			</view>
			<view class="form-item">
				<view class="label">我在本群的昵称</view>
				<input class="input" maxlength="20"  v-model="group.aliasName" :disabled="groupType!==0"  :placeholder="groupType===0 ? userStore.userInfo.nickName : ''"/>
			</view>
      <view v-if="!isEdit && groupType!==0" class="form-item">
        <view class="label">群聊模板</view>
        <view class="form-value">
          <text style="color: #3c9cff;" @click="openTemplateListPopup">选择</text>
          <head-image class="template-image" :name="group.templateGroupName" :url="group.templateGroupAvatar"
                      :size="50"></head-image>
        </view>
      </view>
      <view v-if="!isEdit && groupType!==0" class="form-item">
        <view class="label" style="display: flex;align-items: center;">
          <text>模板角色</text>
        </view>
        <view class="form-value">
          <text style="color: #e6a64c;" @click="openCharacterListPopup">选择</text>
          <head-image class="template-image" :name="group.templateCharacterName" :url="group.templateCharacterAvatar"
                      :size="50"></head-image>
        </view>
      </view>
      <view class="form-item" v-if="groupType!==0">
        <view class="label">昵称前缀</view>
        <uni-easyinput v-model="group.aliasNamePrefix" placeholder="输入昵称前缀" maxlength="10"></uni-easyinput>
      </view>
      <view class="form-item" v-if="groupType!==0">
        <view class="label">昵称后缀</view>
        <uni-easyinput v-model="group.aliasNameSuffix" placeholder="输入昵称前缀" maxlength="10"></uni-easyinput>
      </view>
      <view class="form-item" v-if="groupType!==0">
        <view class="label">备注名</view>
        <uni-easyinput v-model="group.nickName" placeholder="" maxlength="10"></uni-easyinput>
      </view>
      <view class="form-item" v-if="groupType!==0">
        <view class="label">群成员名称显示</view>
        <view style="display: flex;align-items: center;">
          <up-text type="primary" text="关闭"></up-text>
          <up-switch v-model="group.showNickName" activeColor="#13ce66" inactiveColor="#ff4949"></up-switch>
          <up-text type="info" text="显示"></up-text>
        </view>
      </view>
			<view class="form-item">
				<view class="label">群公告</view>
				<textarea class="notice" :class="isOwner?'':'disable'" maxlength="512" :disabled="!isOwner" v-model="group.notice" :placeholder="isOwner?'请输入群公告':''"></textarea>
			</view>
		</view>	
		<button class="bottom-btn" type="primary" @click="submit()">提交</button>
    <group-template-list ref="groupTemplateList" :group-templates="groupTemplates" @confirm="chooseGroupTemplate"></group-template-list>
    <character-list ref="characterList" :characters="characters" @confirm="chooseTemplateCharacter"></character-list>
	</view>
</template>

<script>
import GroupTemplateList from "../../components/group-template-list/group-template-list.vue";
import CharacterList from "../../components/character-list/character-list.vue";

export default {
  components: {CharacterList, GroupTemplateList},
	data() {
		return {
			group: {
        templateGroupId: null,
        templateCharacterId: null,
        aliasNamePrefix: '',
        aliasNameSuffix: '',
        nickName: '',
        showNickName: false,
      },
			rules: {
				name: {
					rules: [{
						required: true,
						errorMessage: '请输入群聊名称',
					}]
				}
			},
      groupType: 0,
      groupTemplates: [],
      range: [],
      characters: [],
      characterRange: [],
      isEdit: false,
		}
	},

	methods: {
		submit() {
			if (this.group.id) {
				this.modifyGroup();
			} else {
        this.group.groupType = this.groupType;
        if (this.groupType === 0) {
          this.createNewGroup();
        } else {
          if (!this.group.templateGroupId || !this.group.templateCharacterId) {
            uni.showToast({
              title: "请选择群聊模板和角色",
              icon: 'none'
            });
            return;
          }
          this.createNewTemplateGroup();
        }
			}
		},
		onUnloadImageSuccess(file, res) {
			this.group.headImage = res.data.originUrl;
			this.group.headImage = res.data.thumbUrl;
		},
		modifyGroup() {
			this.$http({
				url: "/group/modify",
				method: "PUT",
				data: this.group
			}).then((group) => {
				this.groupStore.updateGroup(group);
				uni.showToast({
					title: "修改群聊信息成功",
					icon: 'none'
				});
				setTimeout(() => {
					let pages = getCurrentPages();
					let prevPage = pages[pages.length - 2];
					prevPage.$vm.loadGroupInfo();
					prevPage.$vm.loadGroupMembers();
					uni.navigateBack();
				}, 1000);

			})
		},
		createNewGroup() {
			this.$http({
				url: "/group/create",
				method: 'POST',
				data: this.group
			}).then((group) => {
				this.groupStore.addGroup(group);
				uni.showToast({
					title: `群聊创建成功，快邀请小伙伴进群吧`,
					icon: 'none',
					duration: 1500
				});
				setTimeout(() => {
					uni.navigateTo({
						url: "/pages/group/group-info?id=" + group.id
					});
				}, 1500)

			})
		},
    createNewTemplateGroup() {
      this.$http({
        url: "/group/createTemplateGroup",
        method: 'POST',
        data: this.group
      }).then((group) => {
        this.groupStore.addGroup(group);
        uni.showToast({
          title: `群聊创建成功，快邀请小伙伴进群吧`,
          icon: 'none',
          duration: 1500
        });
        setTimeout(() => {
          uni.navigateTo({
            url: "/pages/group/group-info?id=" + group.id
          });
        }, 1500)

      })
    },
		loadGroupInfo(id) {
			this.$http({
				url: `/group/find/${id}`,
				method: 'GET'
			}).then((group) => {
				this.group = group;
        this.groupType = group.groupType;
				// 更新聊天页面的群聊信息
				this.chatStore.updateChatFromGroup(group);
				// 更新聊天列表的群聊信息
				this.groupStore.updateGroup(group);

			});
		},
		initNewGroup() {
			let userInfo = this.userStore.userInfo;
			this.group = {
				name: this.groupType === 0 ? `${userInfo.userName}创建的群聊` : '',
				headImage: this.groupType === 0 ? userInfo.headImage : '',
				ownerId: this.userStore.userInfo.id,
			}
		},
    loadGroupTemplates() {
      this.$http({
        url: "/templateGroup/list",
        method: 'GET',
      }).then((list) => {
        this.groupTemplates = list;
        for (let i = 0; i < list.length; i++) {
          this.range.push({
            value: list[i].id,
            text: list[i].groupName,
          })
        }
      })
    },
    loadTemplateCharacters(groupTemplateId) {
      this.characters = [];
      this.characterRange = [];
      this.$http({
        url: `templateCharacter/list-published/${groupTemplateId}`,
        method: 'GET',
      }).then((list) => {
        this.characters = list;
        for (let i = 0; i < list.length; i++) {
          this.characterRange.push({
            value: list[i].id,
            text: list[i].name,
          })
        }
      })
    },
    change(value) {
      this.group.templateGroupId = value;
      let groupTemplate = this.groupTemplates.find(item => item.id === value);
      if (this.groupType === 1 || this.groupType === 4) {
        this.group.name = groupTemplate.groupName;
        this.group.remark = groupTemplate.groupName;
        this.group.headImage = groupTemplate.avatar;
        this.group.templateGroupAvatar = groupTemplate.avatar;
        this.group.templateGroupName = groupTemplate.groupName;
      }

      this.group.templateCharacterId = null;
      this.group.templateCharacterAvatar = null;
      this.group.templateCharacterName = null;
      this.loadTemplateCharacters(value);
    },
    characterChange(value) {
      this.group.templateCharacterId = value;
      let character = this.characters.find(item => item.id === value);
      this.group.templateCharacterAvatar = character.avatar;
      this.group.templateCharacterName = character.name;
      this.group.aliasName = character.name;
    },
    clearSelectedValue() {
      // 方法二：通过组件实例调用方法清除选中值（如果组件提供了相应方法）
      if (this.$refs.dataSelectRef) {
        // 假设组件有一个 clear 方法用于清除选中值
        this.$refs.dataSelectRef.clear();
      }
    },
    chooseGroupTemplate(groupTemplate) {
      this.group.templateGroupId = groupTemplate.id;
      if (this.groupType === 1 || this.groupType === 4) {
        this.group.name = groupTemplate.groupName;
        this.group.remark = groupTemplate.groupName;
        this.group.headImage = groupTemplate.avatar;
      }
      this.group.templateGroupName = groupTemplate.groupName;
      this.group.templateGroupAvatar = groupTemplate.avatar;

      this.group.templateCharacterId = null;
      this.group.templateCharacterAvatar = null;
      this.group.templateCharacterName = null;
      this.loadTemplateCharacters(groupTemplate.id);
    },
    openTemplateListPopup() {
      this.$refs.groupTemplateList.open();
    },
    chooseTemplateCharacter(character) {
      this.group.templateCharacterId = character.id;
      this.group.templateCharacterAvatar = character.avatar;
      this.group.templateCharacterName = character.name;
      this.group.aliasName = character.name;
    },
    openCharacterListPopup() {
      this.$refs.characterList.open();
    },
	},
	computed: {
		isOwner() {
			return this.userStore.userInfo.id == this.group.ownerId
		}
	},
	onLoad(options) {
    console.log("options", options);
    if (options.groupType) {
      this.groupType = Number(options.groupType);
      if (options.groupType !== '0') {
        this.loadGroupTemplates();
      }
    }
		if (options.id) {
			// 修改群聊
      this.isEdit = true;
			this.loadGroupInfo(options.id);
		} else {
			// 创建群聊
			this.initNewGroup();
		}

	}
}
</script>

<style lang="scss" scoped>
.group-edit {

	.form {
		margin-top: 20rpx;
	
		.form-item {
			padding: 0 40rpx;
			display: flex;
			background: white;
			align-items: center;
			margin-bottom: 2rpx;
	
			.label {
				width: 220rpx;
				line-height: 100rpx;
				font-size: $im-font-size;
				white-space: nowrap;
			}
			
			.value{
				flex: 1;


			}

      .form-value {
        display: flex;
        align-items: center;

        .template-image {
          border-radius: 50%;
          border: 1px solid #ccc;
          margin-left: 20rpx;
        }
      }
			
			.input {
				flex: 1;
				text-align: right;
				line-height: 100rpx;
				font-size: $im-font-size-small;
			}
			
			.disable {
				color: $im-text-color-lighter;
			}
			
			.notice {
				flex: 1;
				font-size: $im-font-size-small;
				max-height: 200rpx;
				padding: 14rpx 0;
			}
			
			.group-image {
				width: 120rpx;
				height: 120rpx;
				border-radius: 50%;
				border: 1px solid #ccc;
			}

      .character-image {
        display: flex;
        align-items: center;
      }
		}

    .group-template {
      display: flex;

      .template-item {
        width: 50%;
      }
    }
	}
}

</style>
