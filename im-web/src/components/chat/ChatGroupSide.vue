<template>
	<div class="chat-group-side">
		<el-scrollbar class="group-side-scrollbar">
			<div v-show="!group.quit" class="member-list">
				<div class="member-tools">
					<div class="tool-btn" title="邀请好友进群聊" @click="showAddGroupMember=true">
						<i class="el-icon-plus"></i>
					</div>
					<div class="tool-text">邀请</div>
				</div>
        <div class="member-tools">
          <div class="tool-btn" title="群聊成员信息" @click="openGroupMemberInfoDialog">
            <i class="el-icon-search"></i>
          </div>
          <div class="tool-text">查看</div>
        </div>
        <div class="member-tools" v-if="isOwner">
          <div class="tool-btn" title="禁言" @click="onMuteMember">
            <i class="el-icon-turn-off-microphone"></i>
          </div>
          <div class="tool-text">禁言</div>
        </div>
        <div class="member-tools" v-if="isOwner">
          <div class="tool-btn" title="解除禁言" @click="onUnMuteMember">
            <i class="el-icon-remove-outline"></i>
          </div>
          <div class="tool-text">解除禁言</div>
        </div>
				<div v-for="(member) in displayedGroupMembers" :key="member.id">
					<group-member class="group-side-member" v-show="!member.quit && member.aliasName.startsWith(searchText)" :member="member"
					 :showDel="false" :right-menu-visible="myGroupMemberInfo.isAdmin" @ban="banMemberMsg" @unban="unBanMemberMsg"></group-member>
				</div>
			</div>
      <div class="group-space" v-if="!group.quit" @click="openGroupSpace">
        <svg class="icon svg-icon" aria-hidden="true">
          <use xlink:href="#icon-shejiaotubiao-40"></use>
        </svg>
        <span style="color: orange;margin-left: 10px;font-size: 16px;">群空间</span>
        <div class="new-talk-info">
          <div v-show="unreadTalkCount" class="new-talk-text">{{unreadTalkCount}}条新动态</div>
          <div v-show="talkList.length" class="new-talk-list">
            <head-image v-for="(talk, index) in talkList" :key="index" :url="talk.avatar" :name="talk.nickName" :size="24"></head-image>
          </div>
        </div>
        <div v-show="unreadNotifyCount>0" class="unread-text">{{unreadNotifyCount}}</div>
      </div>
      <div class="group-music" @click="openGroupMusic">
        <svg class="icon svg-icon" aria-hidden="true">
          <use xlink:href="#icon-Music"></use>
        </svg>
        <span style="color: #b7eb81;margin-left: 10px;font-size: 16px;">群歌单</span>
      </div>
			<!-- 修改表单部分 -->
			<div class="group-side-form">
				<div class="form-item">
					<label class="form-label">群聊名称</label>
					<div class="form-value readonly">{{group.name}}</div>
				</div>
				<div class="form-item">
					<label class="form-label">群主</label>
					<div class="form-value readonly">{{ownerName}}</div>
				</div>
				<div class="form-item">
					<label class="form-label">备注</label>
					<div 
						class="form-value" 
						:class="{ 'editing': editingField === 'remark' }"
						@click="editField('remark')"
						v-if="editingField !== 'remark'">
						{{group.remark || '点击添加备注'}}
					</div>
					<input 
						v-else
						v-model="group.remark" 
						class="form-input"
						placeholder="群聊的备注仅自己可见"
						maxlength="20"
            @blur="saveAndExitEdit">
				</div>
				<div class="form-item">
					<label class="form-label">我在本群的昵称</label>
					<div 
						class="form-value"
						:class="{ 'editing': editingField === 'aliasName', 'readonly': group.groupType!==0 }"
						@click="group.groupType===0 && editField('aliasName')"
						v-if="editingField !== 'aliasName'">
						{{group.aliasName || '点击设置昵称'}}
					</div>
					<input 
						v-else
						v-model="group.aliasName" 
						class="form-input"
						placeholder="xx"
						maxlength="10"
            @blur="saveAndExitEdit"
						:disabled="group.groupType!==0">
				</div>
        <div class="form-item" v-show="group.groupType!==0">
					<label class="form-label">昵称前缀</label>
					<div 
						class="form-value"
						:class="{ 'editing': editingField === 'aliasNamePrefix' }"
						@click="editField('aliasNamePrefix')"
						v-if="editingField !== 'aliasNamePrefix'">
						{{group.aliasNamePrefix || '点击设置前缀'}}
					</div>
					<input 
						v-else
						v-model="group.aliasNamePrefix" 
						class="form-input"
						placeholder="xx"
						maxlength="10"
            @blur="saveAndExitEdit">
				</div>
        <div class="form-item" v-show="group.groupType!==0">
					<label class="form-label">昵称后缀</label>
					<div 
						class="form-value"
						:class="{ 'editing': editingField === 'aliasNameSuffix' }"
						@click="editField('aliasNameSuffix')"
						v-if="editingField !== 'aliasNameSuffix'">
						{{group.aliasNameSuffix || '点击设置后缀'}}
					</div>
					<input 
						v-else
						v-model="group.aliasNameSuffix" 
						class="form-input"
						placeholder="xx"
						maxlength="10"
            @blur="saveAndExitEdit">
				</div>
			</div>
			
			<!-- 群公告卡片 -->
      <div class="notice-card">
        <div class="card-header">
          <h3>群公告</h3>
          <div v-if="isOwner" class="edit-icon" @click="editNotice">
            <i class="el-icon-edit"></i>
          </div>
        </div>
        <div class="card-content">
          <div class="notice-content" v-if="editingField !== 'notice'">
            {{group.notice || '群主未设置'}}
          </div>
          <div v-else class="notice-editor">
            <el-input
              type="textarea"
              :rows="5"
              placeholder="请输入群公告内容"
              v-model="group.notice"
              maxlength="500"
              show-word-limit>
            </el-input>
            <div class="editor-actions">
              <el-button size="mini" @click="cancelEditNotice">取消</el-button>
              <el-button size="mini" type="primary" @click="saveNotice">保存</el-button>
            </div>
          </div>
        </div>
      </div>
			
			<!-- 开关项合集卡片 -->
      <div class="switch-card">
        <div class="card-header">
          <h3>群组设置</h3>
        </div>
        <div class="card-content">
          <div class="form-item" v-if="group.groupType!==0" style="border-bottom: 1px solid lightgray; padding-bottom: 10px">
            <label class="form-label">群成员名称显示</label>
            <div class="switch-container">
              <div 
                class="custom-switch" 
                :class="{ 'switch-active': myGroupMemberInfo.showNickName, 'switch-inactive': !myGroupMemberInfo.showNickName }"
                @click="toggleShowNickName">
              </div>
            </div>
          </div>
          <div class="form-item" v-if="isOwner" style="border-bottom: 1px solid lightgray; padding-bottom: 10px">
            <label class="form-label">进群审核</label>
            <div class="switch-container">
              <div 
                class="custom-switch" 
                :class="{ 'switch-active': group.enterReview, 'switch-inactive': !group.enterReview }"
                @click="toggleEnterReview">
              </div>
            </div>
          </div>
          <div class="form-item" v-if="isOwner">
            <label class="form-label">全局禁言</label>
            <div class="switch-container">
              <div 
                class="custom-switch" 
                :class="{ 'switch-active': group.isBanned, 'switch-inactive': !group.isBanned }"
                @click="toggleAllBanned">
              </div>
            </div>
          </div>
          <div class="form-item" v-if="isOwner">
            <label class="form-label" style="width: 80px">禁言时长</label>
            <div
              class="form-value"
              :class="{ 'editing': editingField === 'bannedTime' }"
              @click="editField('bannedTime')"
              v-if="editingField !== 'bannedTime'">
              {{bannedTime}}
            </div>
            <input
                v-else
                v-model.number="bannedTime"
                style="width: 50px;"
                type="number"
                class="form-input"
                min="1"
                max="60000"
                @keyup.enter="exitEdit">
              <span>（分钟）</span>
          </div>
        </div>
      </div>
		</el-scrollbar>
    <drawer
        :visible="groupSpaceVisible"
        @close="closeDrawer"
        :width=60>
      <template v-slot:header>
        <space-cover :name="'群空间'" @refresh="refreshTalkList" @add="handleShowAddTalk" @showTalkNotify="showTalkNotify" :notify-count="unreadNotifyCount"></space-cover>
      </template>
      <template v-slot:main>
        <talk-list ref="talkListRef" :category="'group'" :section="'group'" :group-id="group.id" :new-talk-list="talkList" :new-talk-count="unreadTalkCount"></talk-list>
      </template>
    </drawer>
    <talk-notify ref="talkNotifyRef" :category="'group'" :group-id="group.id"></talk-notify>
    <music-play ref="musicPlayRef" :category="'group'" :section="'group'" :groupId="group.id"></music-play>
    <add-group-member
        :visible="showAddGroupMember"
        :groupId="group.id"
        :members="groupMembers"
        :isTemplate="group.isTemplate"
        :templateGroupId="group.templateGroupId"
        :selectableCharacters="selectableCharacters"
        :groupType="group.groupType"
        @reload="$emit('reload')"
        @close="showAddGroupMember=false"></add-group-member>
    <el-dialog
        width="25%"
        title="群成员信息"
        :visible.sync="groupMemberVisible"
        :before-close="closeGroupMemberInfoDialog">
      <el-scrollbar style="height:400px;">
        <div v-for="(groupMember, index) in groupMembers" :key="index" v-show="!groupMember.quit">
          <template-group-member class="r-group-member" :member="groupMember"></template-group-member>
        </div>
      </el-scrollbar>
    </el-dialog>
    <BanGroupMember ref="banGroupMemberRef" :visible="banGroupMemberVisible" :operation="banOperation" :members="banMembers" @close="closeBanGroupMemberDialog" @confirm="doBanMembers"></BanGroupMember>
	</div>
</template>

<script>
	import AddGroupMember from '../group/AddGroupMember.vue';
	import GroupMember from '../group/GroupMember.vue';
	import TemplateGroupMember from "@/components/group/TemplateGroupMember";
	import TalkList from "@/components/talk/TalkList";
	import Drawer from "@/components/common/Drawer";
	import SpaceCover from "@/components/common/SpaceCover";
  import TalkNotify from "../talk/TalkNotify.vue";
  import HeadImage from "@components/common/HeadImage.vue";
  import MusicPlay  from "@components/common/musicPlay.vue";
  import BanGroupMember from "@components/group/BanGroupMember.vue";

	export default {
		name: "chatGroupSide",
		components: {
      HeadImage,
			AddGroupMember,
			GroupMember,
      TemplateGroupMember,
      TalkList,
      Drawer,
      SpaceCover,
      TalkNotify,
      MusicPlay,
      BanGroupMember,
		},
		data() {
			return {
				searchText: "",
				showAddGroupMember: false,
        selectableCharacters: [],
        groupMemberVisible: false,
        showNickName: false,
        groupSpaceVisible: false,
        bannedTime: 1,
        editingField: '', // 当前正在编辑的字段
        rightMenuItems: [{
          key: 'BAN',
          name: '禁言',
          icon: 'el-icon-turn-off-microphone'
        }, {
          key: 'UNBAN',
          name: '解除禁言',
          icon: 'el-icon-remove-outline'
        }],
        banOperation: "ban",
        banGroupMemberVisible: false,
        banMembers: []
			}
		},
		props: {
			group: {
				type: Object
			},
			groupMembers: {
				type: Array
			},
      friends: {
        type: Array
      },
      myGroupMemberInfo: {
        type: Object
      }
		},
		computed: {
			displayedGroupMembers() {
				// 限制最多显示18个群成员
				return this.groupMembers.slice(0, 18);
			},
			ownerName() {
				let member = this.groupMembers.find((m) => m.userId == this.group.ownerId);
				return member && member.aliasName;
			},
			isOwner() {
				return this.group.ownerId == this.$store.state.userStore.userInfo.id;
			},
      talkList() {
        let talkMap =this.$store.state.talkStore.groupsTalks;
        let talks = talkMap.get(this.group.id)
        if (talks && talks.length > 2) {
          return talks.slice(0, 2);
        }
        return talks ? talks : [];
      },
      unreadTalkCount() {
        let talkMap =this.$store.state.talkStore.groupsTalks;
        let talks = talkMap.get(this.group.id);
        if (talks) {
          return talks.length;
        }
        return 0;
      },
      unreadNotifyCount() {
        let notifyMap =this.$store.state.talkStore.groupNotify;
        let count = notifyMap.get(this.group.id);
        if (count) {
          return count;
        }
        return 0;
      },
		},
		methods: {
      onClose() {
				this.$emit('close');
			},
			loadGroupMembers() {
				this.$http({
					url: `/group/members/${this.group.id}`,
					method: "get"
				}).then((members) => {
					this.groupMembers = members;
				})
			},
      onSaveGroup() {
				let vo = this.group;
				vo.showNickName = this.myGroupMemberInfo.showNickName;
				this.$http({
					url: "/group/modify",
					method: "put",
					data: vo
				}).then((group) => {
					this.$store.commit("updateGroup", group);
					this.$emit('reload');
					this.$message.success("修改成功");
				})
			},
      onQuit() {
				this.$confirm('退出群聊后将不再接受群里的消息，确认退出吗？', '确认退出?', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					this.$http({
						url: `/group/quit/${this.group.id}`,
						method: 'delete'
					}).then(() => {
						this.$store.commit("removeGroup", this.group.id);
						this.$store.commit("activeGroup", -1);
						this.$store.commit("removeGroupChat", this.group.id);
					});
				})
			},
      querySelectableTemplateCharacter(group) {
			  if (group == null) {
          return false;
        }
			  if (group.id === undefined || group.id === null) {
			    return false
        }
        if (group.templateGroupId === undefined || group.templateGroupId === null) {
          return false
        }
        if (group.groupType !== 1) {
          return false;
        }
        let paramVO = {
          groupId: group.id,
          templateGroupId: group.templateGroupId
        }
        this.$http({
          url: "/templateCharacter/findSelectableTemplateCharacter",
          method: 'post',
          data: paramVO
        }).then(result => {
          this.selectableCharacters = result;
        }).finally(() =>{

        })
      },
      closeGroupMemberInfoDialog() {
        this.groupMemberVisible = false;
      },
      openGroupMemberInfoDialog() {
        this.groupMemberVisible = true;
      },
      showNickNameChange() {
        this.$emit("change", this.myGroupMemberInfo.showNickName);
      },
      enterGroupReviewChange(value) {
        this.group.enterReview = value;
      },
      openGroupSpace() {
        this.groupSpaceVisible = true;
        this.$refs.talkListRef.refreshTalkList();
        this.$store.commit("resetGroupTalk", this.group.id);
      },
      openGroupMusic() {
        this.$refs.musicPlayRef.show();
      },
      closeDrawer() {
        this.groupSpaceVisible = false;
      },
      handleShowAddTalk() {
        this.$refs.talkListRef.handleShowAddTalk();
      },
      refreshTalkList() {
        this.$refs.talkListRef.refreshTalkList();
        this.$store.commit("resetGroupTalk", this.group.id);
        this.$store.commit("resetGroupNotify", this.group.id);
      },
      showTalkNotify() {
        this.$refs.talkNotifyRef.show();
        this.$store.commit("resetGroupNotify", this.group.id);
        if (this.unreadNotifyCount > 0) {
          this.readedTalkNotify();
        }
      },
      readedTalkNotify() {
        let params = {
          category: 'group',
          groupId: this.group.id
        };
        this.$http({
          url: `/talk-notify/readed`,
          method: 'post',
          data: params
        }).then(() => {})
      },
      doAllBanned(value) {
        let paramVO = {
          id: this.group.id,
          allBanned: value,
          banDuration: this.bannedTime,
          banType: 'master'
        }
        let url = "";
        if (value) {
          url = '/group/banMsg';
        } else {
          url = '/group/unBanMsg'
        }
        this.$http({
          url: url,
          method: 'post',
          data: paramVO
        }).then(() => {
          this.group.isBanned = value;
          this.$message.success("操作成功");
        }).catch((e) => {
          this.group.isBanned = !value;
        })
      },
      banMemberMsg(userIds) {
        if (userIds.includes(this.myGroupMemberInfo.userId)) {
          this.$message.warning('不能禁言自己');
          return
        }
        this.$prompt('请输入禁言时长（分钟）', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /^(-1|[1-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|60000)$/,
          inputErrorMessage: '只能输入-1或正整数(1~60000)，其中-1表示永久禁言'
        }).then(({ value }) => {
          let paramVO = {
            id: this.group.id,
            userIds: userIds,
            banDuration: value,
            banType: 'master'
          }
          this.$http({
            url: '/group/banMsg',
            method: 'post',
            data: paramVO
          }).then(() => {
            this.$message.success("操作成功");
            this.$emit('reload');
          }).catch((e) => {
          })
        })
      },
      unBanMemberMsg(userIds) {
        let paramVO = {
          id: this.group.id,
          userIds: userIds,
          banDuration: this.bannedTime,
          banType: 'master'
        }
        this.$http({
          url: '/group/unBanMsg',
          method: 'post',
          data: paramVO
        }).then(() => {
          this.$message.success("操作成功");
          this.$emit('reload');
        }).catch((e) => {
        })
      },
      // 新增方法：处理字段编辑
      editField(fieldName) {
        this.editingField = fieldName;
      },
      // 保存并退出编辑模式
      saveAndExitEdit() {
        this.editingField = '';
        this.onSaveGroup();
      },
      exitEdit() {
        this.editingField = '';
      },
      // 切换显示昵称
      toggleShowNickName() {
        this.myGroupMemberInfo.showNickName = !this.myGroupMemberInfo.showNickName;
        this.showNickNameChange();
      },
      // 切换进群审核
      toggleEnterReview() {
        this.group.enterReview = !this.group.enterReview;
        this.enterGroupReviewChange(this.group.enterReview);
      },
      // 切换全体禁言
      toggleAllBanned() {
        this.doAllBanned(!this.group.isBanned);
      },
      // 编辑群公告
      editNotice() {
        // 保存原始公告内容，用于取消时恢复
        this.originalNotice = this.group.notice;
        this.editingField = 'notice';
      },
      // 取消编辑群公告
      cancelEditNotice() {
        this.editingField = '';
        this.group.notice = this.originalNotice;
      },
      // 保存群公告
      saveNotice() {
        this.editingField = '';
        this.onSaveGroup();
      },
      closeBanGroupMemberDialog() {
        this.banGroupMemberVisible = false;
      },
      onMuteMember() {
        this.banOperation = 'mute';
        this.banMembers = JSON.parse(JSON.stringify(this.groupMembers));
        this.banGroupMemberVisible = true;
      },
      onUnMuteMember() {
        this.banOperation = 'unmute';
        this.banMembers = JSON.parse(JSON.stringify(this.groupMembers.filter((m) => m.isBanned)));
        this.banGroupMemberVisible = true;
      },
      doBanMembers(userIds) {
        if (!userIds || userIds.length === 0) {
          return
        }
        this.banGroupMemberVisible = false;
        if (this.banOperation === 'mute') {
          this.banMemberMsg(userIds);
        } else {
          this.unBanMemberMsg(userIds);
        }
      }
		},
    watch: {
		  group: {
        handler(newGroup, oldGroup) {
          if ((newGroup && !oldGroup) || (newGroup && newGroup.id !== oldGroup.id)) {
            this.querySelectableTemplateCharacter(newGroup);
          }
        }
      },
      groupMembers: {
        handler(newGroupMembers, oldGroupMembers) {
          newGroupMembers.forEach((item, index) => {
            let friend = this.friends.find((f) => f.id === item.userId);
            if (friend) {
              if (this.group.groupType !== 0) {
                item.nickName = friend.friendRemark ? friend.friendRemark : item.nickName;
              }
            }
          })
        }
      }
    }
  }
</script>

<style scoped lang="scss">
	.chat-group-side {
    border-left: 1px solid #EBEEF5;

    .member-list {
      padding: 15px;
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      font-size: 14px;
      text-align: center;
      background: linear-gradient(120deg, #f5f7fa, #e4edf9);
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      margin: 15px;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      }

      .group-side-member {
        margin-left: 5px;
      }

      .member-tools {
        display: flex;
        flex-direction: column;
        align-items: center;
        width: 54px;
        margin-left: 5px;

        .tool-btn {
          width: 38px;
          height: 38px;
          line-height: 38px;
          border: 1px solid #EBEEF5;
          font-size: 14px;
          cursor: pointer;
          box-sizing: border-box;
          border-radius: 10px;
          background: white;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
          transition: all 0.3s ease;

          &:hover {
            border: #409eff solid 1px;
            transform: scale(1.05);
            box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2);
          }
        }

        .tool-text {
          font-size: 12px;
          text-align: center;
          width: 100%;
          height: 30px;
          line-height: 30px;
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: hidden;
          color: #606266;
        }
      }
    }

    .el-divider--horizontal {
      margin: 0;
    }

    .group-space {
      position: relative;
      display: flex;
      justify-content: center;
      align-items: center;
      cursor: pointer;
      background: linear-gradient(120deg, #f5f7fa, #e4edf9);
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      margin: 15px;
      padding: 15px 0;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      }

      .icon {
        display: block;
        height: 45px;
        line-height: 45px;
        font-size: 28px;
        color: #409eff;
        -webkit-transition: font-size 0.25s linear, width 0.25s linear;
        -moz-transition: font-size 0.25s linear, width 0.25s linear;
        transition: font-size 0.25s linear, width 0.25s linear;
      }

      .new-talk-info {
        position: absolute;
        top: 50%;
        right: 20px;
        transform: translateY(-50%);
        display: flex;
        align-items: center;

        .new-talk-text {
          font-size: 12px;
          color: #f56c6c;
          margin-right: 10px;
        }

        .new-talk-list {
          display: flex;
          align-items: center;
        }
      }

      .unread-text {
        position: absolute;
        line-height: 16px;
        background-color: #f56c6c;
        right: 20px;
        top: 10px;
        color: white;
        border-radius: 16px;
        padding: 0 5px;
        font-size: 10px;
        text-align: center;
        white-space: nowrap;
        border: 1px solid #f1e5e5;
        box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);
      }
    }

    .group-music {
      position: relative;
      display: flex;
      justify-content: center;
      align-items: center;
      cursor: pointer;
      background: linear-gradient(120deg, #f5f7fa, #e4edf9);
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      margin: 15px;
      padding: 15px 0;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      }

      .icon {
        display: block;
        height: 45px;
        line-height: 45px;
        font-size: 28px;
        color: #67c23a;
        -webkit-transition: font-size 0.25s linear, width 0.25s linear;
        -moz-transition: font-size 0.25s linear, width 0.25s linear;
        transition: font-size 0.25s linear, width 0.25s linear;
      }
    }

		.group-side-form {
			text-align: left;
			padding: 15px;
			background: linear-gradient(120deg, #f5f7fa, #e4edf9);
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      margin: 15px;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      }

			.form-item {
				display: flex;
				align-items: center;
				margin-bottom: 12px;
				padding-bottom: 12px;
				border-bottom: 1px solid #eee;
				
				&:last-child {
					border-bottom: none;
				}
				
				.form-label {
					width: 120px;
					padding: 0;
					line-height: 30px;
          color: #606266;
          font-weight: 500;
				}
				
				.form-value {
					flex: 1;
					min-height: 30px;
					line-height: 30px;
					padding: 0 10px;
          border-radius: 6px;
          background: rgba(255, 255, 255, 0.7);
          transition: all 0.3s ease;
					
					&.readonly {
						color: #999;
					}
					
					&.editing {
						border: 1px solid #409eff;
						border-radius: 4px;
            background: white;
            box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
					}
          
          &:not(.readonly):not(.editing) {
            cursor: pointer;
            
            &:hover {
              background: rgba(255, 255, 255, 0.9);
              box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
            }
          }
				}
				
				.form-input {
					flex: 1;
					height: 30px;
          max-width: 120px;
					line-height: 30px;
					padding: 0 10px;
					border: 1px solid #409eff;
					border-radius: 4px;
          background: white;
          box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
				}
			}
		}
		
		.notice-card {
      background: linear-gradient(120deg, #f5f7fa, #e4edf9);
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      margin: 15px;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      }
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px;
        border-bottom: 1px solid #eee;
        
        h3 {
          margin: 0;
          color: #606266;
          font-weight: 500;
        }
        
        .edit-icon {
          cursor: pointer;
          color: #409eff;
          font-size: 16px;
          
          &:hover {
            opacity: 0.8;
          }
        }
      }
      
      .card-content {
        padding: 15px;
        
        .notice-content {
          white-space: pre-wrap;
          line-height: 1.6;
          color: #606266;
          min-height: 60px;
        }
        
        .notice-editor {
          .el-textarea {
            margin-bottom: 15px;
            
            ::v-deep .el-textarea__inner {
              border: 1px solid #409eff;
              border-radius: 4px;
              background: white;
              box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
              resize: vertical;
              
              &:focus {
                border-color: #409eff;
              }
            }
          }
          
          .editor-actions {
            text-align: right;
            
            ::v-deep .el-button {
              border-radius: 4px;
              transition: all 0.3s ease;
              
              &:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
              }
              
              &.el-button--primary {
                background: linear-gradient(120deg, #409eff, #1a73e8);
                border: none;
                box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
                
                &:hover {
                  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.4);
                }
              }
            }
          }
        }
      }
    }
		
		.switch-card {
      background: linear-gradient(120deg, #f5f7fa, #e4edf9);
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      margin: 15px;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      }
      
      .card-header {
        padding: 15px;
        border-bottom: 1px solid #eee;
        
        h3 {
          margin: 0;
          color: #606266;
          font-weight: 500;
        }
      }
      
      .card-content {
        padding: 15px;
      }
      
      .form-item {
        display: flex;
        align-items: center;
        margin-bottom: 12px;
        padding-bottom: 12px;
        border-bottom: 1px solid #eee;
        
        &:last-child {
          border-bottom: none;
          margin-bottom: 0;
          padding-bottom: 0;
        }
        
        .form-label {
          width: 120px;
          padding: 0;
          line-height: 30px;
          color: #606266;
          font-weight: 500;
        }
        
        .switch-container {
          flex: 1;
        }
        
        .custom-switch {
          position: relative;
          display: inline-block;
          width: 40px;
          height: 20px;
          border-radius: 10px;
          cursor: pointer;
          transition: background-color 0.3s;
          
          &.switch-active {
            background-color: #13ce66;
          }
          
          &.switch-inactive {
            background-color: #ccc;
          }
          
          &::after {
            content: '';
            position: absolute;
            width: 18px;
            height: 18px;
            border-radius: 50%;
            background: white;
            top: 1px;
            transition: transform 0.3s;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
          }
          
          &.switch-active::after {
            transform: translateX(20px);
          }
        }
        
        .form-value {
          flex: 1;
          min-height: 30px;
          line-height: 30px;
          padding: 0 10px;
          border-radius: 6px;
          background: rgba(255, 255, 255, 0.7);
          transition: all 0.3s ease;
          
          &.editing {
            border: 1px solid #409eff;
            border-radius: 4px;
            background: white;
            box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
          }
          
          &:not(.readonly):not(.editing) {
            cursor: pointer;
            
            &:hover {
              background: rgba(255, 255, 255, 0.9);
              box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
            }
          }
        }
        
        .form-input {
          flex: 1;
          height: 30px;
          max-width: 120px;
          line-height: 30px;
          padding: 0 10px;
          border: 1px solid #409eff;
          border-radius: 4px;
          background: white;
          box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
        }

        .el-textarea__inner {
          min-height: 100px !important;
        }

        .el-input-number--mini {
          width: 100px;
          margin-right: 10px;
        }
      }
    }

			.btn-group {
				text-align: center;
			}
	}
</style>