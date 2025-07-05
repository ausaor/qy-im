<template>
	<el-container class="group-page">
		<el-aside width="14%" class="group-list-box">
			<div class="group-list-header">
        <el-input class="search-text" placeholder="搜索" v-model="searchText">
          <i class="el-icon-search el-input__icon" slot="prefix"> </i>
        </el-input>
        <el-button plain class="add-btn" icon="el-icon-plus" title="创建群聊" @click="dialogVisible = true"></el-button>
			</div>
			<el-scrollbar class="group-list-items">
				<div v-for="(group,index) in groupStore.groups" :key="index">
					<group-item v-show="!group.quit && group.remark.startsWith(searchText)" :group="group" :active="group === groupStore.activeGroup"
					 @click.native="onActiveItem(group,index)">
					</group-item>
				</div>
			</el-scrollbar>
		</el-aside>
		<el-container class="group-box">
			<div class="group-header" v-show="activeGroup.id">
				{{activeGroup.remark}}({{groupMemberCount}})
			</div>
			<el-scrollbar class="group-container">
				<div v-show="activeGroup.id">
					<div class="group-info">
						<div class="avatar-box">
							<file-upload v-show="isOwner" class="avatar-uploader" :action="imageAction" :disabled="!isOwner || activeGroup.isTemplate"
                 :showLoading="true" :maxSize="maxSize" @success="onUploadSuccess"
                 :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']">
								<img v-if="activeGroup.headImage" :src="activeGroup.headImage" class="avatar">
								<i v-else class="el-icon-plus avatar-uploader-icon"></i>
							</file-upload>
              <head-image  v-show="!isOwner" class="avatar" :size="200"
                           :url="activeGroup.headImage"
                           :name="activeGroup.remark">
              </head-image>
							<el-button class="send-btn" @click="onSendMessage()">发送消息</el-button>
						</div>
            <div class="group-form-box">
              <el-form class="group-form" label-width="130px" :model="activeGroup" :rules="rules" ref="groupForm">
                <el-form-item label="群聊名称" prop="name">
                  <el-input v-model="activeGroup.name" :disabled="!isOwner || activeGroup.isTemplate" maxlength="20"></el-input>
                </el-form-item>
                <el-form-item label="群主">
                  <el-input :value="ownerName" disabled></el-input>
                </el-form-item>
                <el-form-item label="备注">
                  <el-input v-model="activeGroup.remark" placeholder="群聊的备注仅自己可见" maxlength="20"></el-input>
                </el-form-item>
                <el-form-item label="我在本群的昵称">
                  <el-input v-model="activeGroup.aliasName" :disabled="activeGroup.groupType!==0" placeholder="" maxlength="20"></el-input>
                </el-form-item>
                <el-form-item label="备注名" v-show="activeGroup.groupType!==0">
                  <el-input v-model="activeGroup.nickName" placeholder="" maxlength="10"></el-input>
                </el-form-item>
                <el-form-item label="群公告">
                  <el-input v-model="activeGroup.notice" :disabled="!isOwner" type="textarea" maxlength="1024" placeholder="群主未设置"></el-input>
                </el-form-item>
              </el-form>
              <div class="buttons-box">
                <div class="buttons">
                  <el-button type="success" @click="onSaveGroup()">提交</el-button>
                  <el-button type="danger" v-show="!isOwner" @click="onQuit()">退出群聊</el-button>
                  <el-button type="danger" v-show="isOwner" @click="onDissolve()">解散群聊</el-button>
                  <el-button type="primary" v-show="isOwner" @click="popupSwitchTemplateGroup(1)">切换模板群聊</el-button>
                </div>
                <div class="buttons">
                  <el-button type="primary" v-show="isOwner" @click="popupSwitchTemplateGroup(4)">切换模板角色群聊</el-button>
                  <el-button type="warning" v-show="isOwner && activeGroup.groupType !== 2" @click="popupSwitchTemplateGroup(2)">切换多元角色群聊</el-button>
                  <el-button type="info" v-show="isOwner && activeGroup.groupType !== 3" @click="popupSwitchTemplateGroup(3)">切换角色群聊</el-button>
                  <el-button v-show="isOwner && activeGroup.groupType !== 0" @click="popupSwitchCommonGroup()">切换普通群聊</el-button>
                </div>
              </div>
            </div>
					</div>
					<el-divider content-position="center"></el-divider>
					<el-scrollbar style="height:200px;">
						<div class="group-member-list">
							<div v-for="(member) in groupMembers" :key="member.id">
								<group-member v-show="!member.quit" class="group-member" :member="member" :showDel="isOwner&&member.userId!=activeGroup.ownerId"
								 @del="onKick" :right-menu-items="member.isBanned ? [rightMenuItems[1]] : [rightMenuItems[0]]" :right-menu-visible="myGroupMemberInfo.isAdmin"
                              @ban="banMemberMsg" @unban="unBanMemberMsg">
                </group-member>
							</div>
              <div class="member-avatar-upload" v-show="!myGroupMemberInfo.isTemplate">
                <div class="member-avatar-btn" title="上传我的群聊头像">
                  <file-upload class="avatar-uploader" :action="imageAction"
                               :showLoading="true" :maxSize="maxSize" @success="onUploadMemberAvatarSuccess"
                               :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']">
                    <img v-if="myGroupMemberInfo.headImage" :src="myGroupMemberInfo.headImage" class="member-avatar">
                    <i v-else class="el-icon-upload"></i>
                  </file-upload>
                </div>
                <div class="member-avatar-text">上传</div>
              </div>
							<div class="group-invite">
								<div class="invite-member-btn" title="邀请好友进群聊" @click="onInviteMember()">
									<i class="el-icon-plus"></i>
								</div>
								<div class="invite-member-text">邀请</div>
								<add-group-member :visible="showAddGroupMember" :groupId="activeGroup.id" :members="groupMembers"
                                  :isTemplate="activeGroup.isTemplate"
                                  :templateGroupId="activeGroup.templateGroupId"
                                  :selectableCharacters = "selectableCharacters"
                                  :groupType="activeGroup.groupType"
                                  @reload="loadGroupMembers"
								 @close="onCloseAddGroupMember"></add-group-member>
							</div>
              <div class="switch-character" v-show="activeGroup.groupType !== 0">
                <div class="switch-character-btn" title="切换角色" @click="switchCharacter()">
                  <i class="el-icon-refresh"></i>
                </div>
                <div class="switch-character-text">切换</div>
                <template-character-choose-dialog :characters="selectableCharacters" :visible="selectTemplateCharacterVisible"
                    @close="() => {this.selectTemplateCharacterVisible = false}"
                    @choose="chooseTemplateCharacterOk"></template-character-choose-dialog>
                <template-character-choose :visible="characterSwitchVisible" @close="characterSwitchVisible = false" @confirm="switchCharacterEvent"></template-character-choose>
                <group-template-character-choose
                    :visible="groupTemplateCharacterVisible"
                    :template-group-id="activeGroup.templateGroupId"
                    @close="() => {this.groupTemplateCharacterVisible=false}"
                    @choose="chooseCharacterOK">
                </group-template-character-choose>
              </div>
              <div class="switch-character-avatar" v-show="activeGroup.groupType !== 0">
                <div class="switch-character-avatar-btn" title="切换角色头像" @click="switchCharacterAvatar()">
                  <i class="el-icon-user-solid"></i>
                </div>
                <div class="switch-character-avatar-text">选择</div>
                <el-dialog
                    width="30%"
                    title="角色头像"
                    :visible.sync="selectCharacterAvatarVisible"
                    :before-close="closeSelectCharacterAvatar">
                  <el-scrollbar style="height:400px;">
                    <div v-for="(characterAvatar, index) in characterAvatars" :key="index">
                      <character-avatar-item class="character-avatar-item-left" :characterAvatar="characterAvatar"></character-avatar-item>
                      <div class="character-avatar-item-right">
                        <el-button :type="avatarActiveIndex === index ? 'success' : ''"
                                   icon="el-icon-check"
                                   circle
                                   @click="chooseCharacterAvatar(characterAvatar, index)"></el-button>
                      </div>
                      <p style="clear:both;"></p>
                    </div>
                  </el-scrollbar>
                  <span slot="footer" class="dialog-footer">
                    <el-button @click="closeSelectCharacterAvatar">取 消</el-button>
                    <el-button type="primary" @click="chooseCharacterAvatarOk">确 定</el-button>
                  </span>
                </el-dialog>
              </div>
              <div class="member-info" v-show="activeGroup.groupType !== 0">
                <div class="view-member-info-btn" title="群聊成员信息" @click="openGroupMemberInfoDialog">
                  <i class="el-icon-search"></i>
                </div>
                <div class="view-member-text">查看</div>
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
              </div>
						</div>
					</el-scrollbar>
				</div>
			</el-scrollbar>
		</el-container>
    <el-dialog
        title="请选择群聊模式"
        :visible.sync="dialogVisible"
        width="40%">
      <el-radio-group v-model="radio">
        <el-radio label="1">普通群聊</el-radio>
        <el-radio label="2">模板群聊</el-radio>
        <el-radio label="6">模板角色群聊</el-radio>
        <el-radio label="3">多元角色群聊</el-radio>
        <el-radio label="4">角色群聊</el-radio>
        <el-radio label="5">搜索群聊</el-radio>
      </el-radio-group>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="openCreateGroupDialog()">确 定</el-button>
      </span>
    </el-dialog>
    <create-template-group :visible="showCreateTemplateGroup" :groupType="groupType" @close="handleCloseTemplateGroup"></create-template-group>
    <switch-template-group :visible="showSwitchTemplateGroup"
                           :group="activeGroup"
                           :groupMembers="groupMembers"
                           :to-group-type="toGroupType"
                           @close="handleCloseSwitchTemplateGroup(activeGroup)"
                           @reload="loadGroupMembers"></switch-template-group>
    <el-dialog
        width="30%"
        title="切换普通群聊"
        :visible.sync="switchCommonGroupVisible"
        :before-close="closeSwitchCommonGroup">
        <div>
          <file-upload class="avatar-uploader-group" :action="imageAction" :showLoading="true"
                       :maxSize="maxSize" @success="doneUploadSuccess" :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']">
            <img v-if="commonGroup.headImage" :src="commonGroup.headImage" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </file-upload>
          <el-form class="r-group-form" label-width="130px" :model="commonGroup" :rules="rules" ref="commonGroupForm">
            <el-form-item label="群聊名称" prop="name">
              <el-input v-model="commonGroup.name" maxlength="20"></el-input>
            </el-form-item>
          </el-form>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="switchCommonGroupVisible = false">取 消</el-button>
          <el-button type="primary" @click="switchCommonGroup()">确 定</el-button>
        </span>
    </el-dialog>
    <join-group :dialogVisible="showJoinGroup" @close="closeJoinGroup"></join-group>
	</el-container>
</template>


<script>
	import GroupItem from '../components/group/GroupItem';
	import FileUpload from '../components/common/FileUpload';
	import GroupMember from '../components/group/GroupMember.vue';
	import AddGroupMember from '../components/group/AddGroupMember.vue';
	import CreateTemplateGroup from '../components/group/CreateTemplateGroup';
  import TemplateCharacterItem from "@/components/group/TemplateCharacterItem";
  import SwitchTemplateGroup from "@/components/group/SwitchTemplateGroup";
  import CharacterAvatarItem from "@/components/group/CharacterAvatarItem";
  import TemplateGroupMember from "@/components/group/TemplateGroupMember";
  import JoinGroup from "@/components/group/JoinGroup";
  import HeadImage from '../components/common/HeadImage.vue';
  import TemplateCharacterChoose from "@/components/template/TemplateCharacterChoose";
  import GroupTemplateCharacterChoose from "@/components/template/GroupTemplateCharacterChoose";
  import TemplateCharacterChooseDialog from "@/components/template/TemplateCharacterChooseDialog";

	export default {
		name: "group",
		components: {
			GroupItem,
			GroupMember,
			FileUpload,
			AddGroupMember,
      CreateTemplateGroup,
      TemplateCharacterItem,
      SwitchTemplateGroup,
      CharacterAvatarItem,
      TemplateGroupMember,
      JoinGroup,
      HeadImage,
      TemplateCharacterChoose,
      GroupTemplateCharacterChoose,
      TemplateCharacterChooseDialog,
		},
		data() {
			return {
			  toGroupType: null,// 要切换到的群聊类型
			  groupType: 0,
				searchText: "",
        characterSearchText: "",
				maxSize: 5 * 1024 * 1024,
				activeGroup: {},
        myGroupMemberInfo: {}, // 我的群聊成员信息
				groupMembers: [],
				showAddGroupMember: false,
        showCreateTemplateGroup: false,
        showSwitchTemplateGroup: false,
        characterSwitchVisible: false,
				rules: {
					name: [{
						required: true,
						message: '请输入群聊名称',
						trigger: 'blur'
					}]
				},
        dialogVisible: false,
        radio: '1',
        selectableCharacters: [],
        characterAvatars: [],
        isTemplateGroup: false,
        selectTemplateCharacterVisible: false,
        groupTemplateCharacterVisible: false,
        selectCharacterAvatarVisible: false,
        groupMemberVisible: false,
        characterActiveIndex: -1,
        avatarActiveIndex: -1,
        newTemplateCharacter: {},
        newCharacterAvatar: {},
        switchCommonGroupVisible: false,
        commonGroup: {
				  name: null,
          headImage: null,
          headImageThumb: null
        },
        showJoinGroup: false,
        friends: [],
        groupMemberCount: 0,
        rightMenuItems: [{
          key: 'BAN',
          name: '禁言',
          icon: 'el-icon-turn-off-microphone'
        }, {
          key: 'UNBAN',
          name: '解除禁言',
          icon: 'el-icon-remove-outline'
        }],
        bannedTime: 1,
			};
		},
    mounted() {
      this.friends = this.$store.state.friendStore.friends;
    },
    created() {
    },
    methods: {
		  openCreateGroupDialog() {
		    this.dialogVisible = false;
		    if (this.radio === '1') {
		      this.groupType = 0;
          this.onCreateGroup();
        } else if (this.radio === '2') {
		      this.groupType = 1;
          this.showCreateTemplateGroup = true;
        } else if (this.radio === '6') {
          this.groupType = 4;
          this.showCreateTemplateGroup = true;
        } else if (this.radio === '3') {
          this.groupType = 2;
          this.showCreateTemplateGroup = true;
        } else if (this.radio === '4') {
          this.groupType = 3;
          this.showCreateTemplateGroup = true;
        } else {
          this.showJoinGroup = true;
        }
      },
      handleCloseTemplateGroup() {
		    this.showCreateTemplateGroup = false;
      },
      onCreateGroup() {
				this.$prompt('请输入群聊名称', '创建群聊', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					inputPattern: /\S/,
					inputErrorMessage: '请输入群聊名称'
				}).then((o) => {
          let userInfo = this.$store.state.userStore.userInfo;
          let data = {
            name: o.value,
            remark: o.value,
            aliasName: userInfo.name,
            ownerId: userInfo.id,
            groupType: 0,
          }
					this.$http({
						url: `/group/create?groupName=${o.value}`,
						method: 'post',
            data: data
					}).then((group) => {
					  this.$message.success('创建成功');
						this.$store.commit("addGroup", group);
					})
				})
			},
      onActiveItem(group, index) {
				this.$store.commit("activeGroup", index);
				// store数据不能直接修改，所以深拷贝一份内存
				this.activeGroup = JSON.parse(JSON.stringify(group));
				// 重新加载群成员
				this.loadGroupMembers();
				if (group.isTemplate) {
				  this.isTemplateGroup = true;
          this.querySelectableTemplateCharacter();
        } else {
          this.isTemplateGroup = false;
        }
			},
      onInviteMember() {
        if (this.activeGroup.groupType===1) {
          this.querySelectableTemplateCharacter();
        }
				this.showAddGroupMember = true;
			},
      onCloseAddGroupMember() {
				this.showAddGroupMember = false;
				this.loadGroup(this.activeGroup.id);
			},
      onUploadSuccess(data) {
				this.activeGroup.headImage = data.originUrl;
				this.activeGroup.headImageThumb = data.thumbUrl;
			},
      onUploadMemberAvatarSuccess(data) {
        this.myGroupMemberInfo.headImage = data.originUrl;
      },
      onSaveGroup() {
				this.$refs['groupForm'].validate((valid) => {
					if (valid) {
						let vo = this.activeGroup;
						vo.memberHeadImage = this.myGroupMemberInfo.headImage;
						this.$http({
							url: "/group/modify",
							method: "put",
							data: vo
						}).then((group) => {
							this.$store.commit("updateGroup", group);
							this.$message.success("修改成功");
						})
					}
				});
			},
      onDissolve() {
				this.$confirm(`确认要解散'${this.activeGroup.name}'吗?`, {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					this.$http({
						url: `/group/delete/${this.activeGroup.id}`,
						method: 'delete'
					}).then(() => {
						this.$message.success(`群聊'${this.activeGroup.name}'已解散`);
						this.$store.commit("removeGroup", this.activeGroup.id);
            this.$store.commit("removeGroupChat", this.activeGroup.id);
            this.reset();
					});
				})

			},
      onKick(member) {
				this.$confirm(`确定将成员'${member.aliasName}'移出群聊吗？`, '确认移出?', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					this.$http({
						url: `/group/kick/${this.activeGroup.id}`,
						method: 'delete',
						params: {
							userId: member.userId
						}
					}).then(() => {
						this.$message.success(`已将${member.aliasName}移出群聊`);
						member.quit = true;
						this.groupMemberCount -= 1;
					});
				})

			},
      onQuit() {
				this.$confirm(`确认退出'${this.activeGroup.name}',并清空聊天记录吗？`, {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(() => {
					this.$http({
						url: `/group/quit/${this.activeGroup.id}`,
						method: 'delete'
					}).then(() => {
						this.$store.commit("removeGroup", this.activeGroup.id);
						this.$store.commit("removeGroupChat", this.activeGroup.id);
            this.reset();
					});
				})
			},
      reset(){
        this.activeGroup={};
        this.groupMembers=[];
      },
      onSendMessage() {
				let chat = {
					type: 'GROUP',
					targetId: this.activeGroup.id,
					showName: this.activeGroup.remark,
					headImage: this.activeGroup.headImage,
          groupType: this.activeGroup.groupType,
				};
				this.$store.commit("openChat", chat);
				this.$store.commit("activeChat", 0);
				this.$router.push("/home/chat");
			},
			loadGroupMembers() {
        this.groupMemberCount = 0;
				this.$http({
					url: `/group/members/${this.activeGroup.id}`,
					method: "get"
				}).then((members) => {
				  members.forEach((item, index) => {
            if (item.userId === this.mine.id) {
              this.myGroupMemberInfo = item;
            }
				    if (!item.quit) {
				      this.groupMemberCount += 1;
            }
            let friend = this.friends.find((f) => f.id === item.userId);
            if (friend) {
              if (this.activeGroup.groupType !== 0) {
                item.nickName = friend.friendRemark ? friend.friendRemark : item.nickName;
              } else {
                item.aliasName = friend.friendRemark ? friend.friendRemark : item.aliasName;
              }
            }
          })
					this.groupMembers = members;
				})
			},
      querySelectableTemplateCharacter() {
        let paramVO = {
          groupId: this.activeGroup.id,
          templateGroupId: this.activeGroup.templateGroupId
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
      switchCharacter() {
		    if (this.activeGroup.groupType === 1) {
		      this.selectTemplateCharacterVisible = true;
        } else if (this.activeGroup.groupType === 2 || this.activeGroup.groupType === 3) {
          this.characterSwitchVisible = true;
        } else if (this.activeGroup.groupType === 4){
          this.groupTemplateCharacterVisible = true;
        }
      },
      switchCharacterAvatar() {
        this.$http({
          url: '/characterAvatar/list/'+this.activeGroup.templateCharacterId,
          method: 'get'
        }).then((data) => {
          this.characterAvatars = data;
        });
        this.selectCharacterAvatarVisible = true;
      },
      chooseCharacterAvatar(characterAvatar, index) {
		    this.newCharacterAvatar = characterAvatar;
        this.avatarActiveIndex = index;
      },
      closeSelectCharacterAvatar() {
		    this.avatarActiveIndex = -1;
		    this.selectCharacterAvatarVisible = false;
      },
      chooseTemplateCharacterOk(newTemplateCharacter) {
        let groupMemberVO = {
          groupId: this.activeGroup.id,
          templateGroupId: this.activeGroup.templateGroupId,
          templateCharacterId: newTemplateCharacter.id,
          templateCharacterName: newTemplateCharacter.name,
          templateCharacterAvatar: newTemplateCharacter.avatar,
          groupType: this.activeGroup.groupType,
        }
        this.$http({
          url: "/group/member/switchTemplateCharacter",
          method: 'post',
          data: groupMemberVO
        }).then(() => {
          this.$message.success("修改成功");
          this.querySelectableTemplateCharacter();
          this.loadGroupMembers();
          this.loadGroup(this.activeGroup.id);
        }).finally(() =>{
          this.selectTemplateCharacterVisible = false;
        })
      },
      chooseCharacterAvatarOk() {
		    if (this.characterAvatars.length === 0) {
          this.$message.warning("无头像可选");
		      return
        }
        let switchCharacterAvatarVO = {
          groupId: this.activeGroup.id,
          groupType: this.activeGroup.groupType,
          templateGroupId: this.activeGroup.templateGroupId,
          templateCharacterId: this.newCharacterAvatar.templateCharacterId,
          characterAvatarId: this.newCharacterAvatar.id,
        }
        this.$http({
          url: "/group/member/switchCharacterAvatar",
          method: 'post',
          data: switchCharacterAvatarVO
        }).then(() => {
          this.$message.success("修改成功");
        }).finally(() =>{
          this.loadGroupMembers();
          this.loadGroup(this.activeGroup.id);
          this.avatarActiveIndex = -1;
          this.selectCharacterAvatarVisible = false;
        })
      },
      loadGroup(groupId) {
        this.$http({
          url: `/group/find/${groupId}`,
          method: 'get'
        }).then((group) => {
          this.activeGroup = group;
          this.$store.commit("updateGroup", group);
        });
      },
      popupSwitchTemplateGroup(toGroupType) {
		    this.toGroupType = toGroupType;
        this.showSwitchTemplateGroup = true;
      },
      handleCloseSwitchTemplateGroup() {
        this.showSwitchTemplateGroup = false;
        this.loadGroupMembers();
        this.loadGroup(this.activeGroup.id)
      },
      popupSwitchCommonGroup() {
        this.switchCommonGroupVisible = true;
      },
      switchCommonGroup() {
		    let paramVO = {
		      groupId: this.activeGroup.id,
          name: this.commonGroup.name,
          avatar: this.commonGroup.headImage
        }

        this.$http({
          url: "/group/switchCommonGroup",
          method: 'post',
          data: paramVO
        }).then((group) => {
          this.$message.success("修改成功");
          this.$store.commit("updateChatFromGroup", group);
        }).finally(() =>{
          this.loadGroup(this.activeGroup.id)
          this.loadGroupMembers();
          this.commonGroup = {};
          this.switchCommonGroupVisible = false;
        })
      },
      closeSwitchCommonGroup() {
        this.switchCommonGroupVisible = false;
      },
      doneUploadSuccess(data) {
        this.commonGroup.headImage = data.originUrl;
        this.commonGroup.headImageThumb = data.thumbUrl;
      },
      closeGroupMemberInfoDialog() {
		    this.groupMemberVisible = false;
      },
      openGroupMemberInfoDialog() {
        this.groupMemberVisible = true;
      },
      closeJoinGroup() {
		    this.showJoinGroup = false;
      },
      switchCharacterEvent(data) {
		    this.characterSwitchVisible = false;
        let groupMemberVO = {
          groupId: this.activeGroup.id,
          templateGroupId: data.templateGroup.id,
          templateCharacterId: data.templateCharacter.id,
          templateCharacterName: data.templateCharacter.name,
          templateCharacterAvatar: data.templateCharacter.avatar,
          groupType: this.activeGroup.groupType,
        }
        this.$http({
          url: "/group/member/switchTemplateCharacter",
          method: 'post',
          data: groupMemberVO
        }).then(() => {
          this.$message.success("修改成功");
        }).finally(() =>{
          this.loadGroupMembers();
          this.loadGroup(this.activeGroup.id);
        })
      },
      chooseCharacterOK(character) {
        this.groupTemplateCharacterVisible = false;
        let groupMemberVO = {
          groupId: this.activeGroup.id,
          templateGroupId: this.activeGroup.templateGroupId,
          templateCharacterId: character.id,
          templateCharacterName: character.name,
          templateCharacterAvatar: character.avatar,
          groupType: this.activeGroup.groupType,
        }
        this.$http({
          url: "/group/member/switchTemplateCharacter",
          method: 'post',
          data: groupMemberVO
        }).then(() => {
          this.$message.success("修改成功");
        }).finally(() =>{
          this.loadGroupMembers();
          this.loadGroup(this.activeGroup.id);
        })
      },
      banMemberMsg(member) {
        if (member.userId === this.myGroupMemberInfo.userId) {
          this.$message.warning('不能禁言自己');
          return
        }

        this.$prompt('请输入禁言时长（分钟）', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /^([1-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|60000)$/,
          inputErrorMessage: '只能输入正整数(1~60000)'
        }).then(({ value }) => {
          let paramVO = {
            id: this.activeGroup.id,
            userId: member.userId,
            banDuration: value,
            banType: 'master'
          }
          this.$http({
            url: '/group/banMsg',
            method: 'post',
            data: paramVO
          }).then(() => {
            member.isBanned = true;
            this.$message.success("操作成功");
          }).catch((e) => {
          })
        })
      },
      unBanMemberMsg(member) {
        let paramVO = {
          id: this.activeGroup.id,
          userId: member.userId,
          banDuration: this.bannedTime,
          banType: 'master'
        }
        this.$http({
          url: '/group/unBanMsg',
          method: 'post',
          data: paramVO
        }).then(() => {
          member.isBanned = false;
          this.$message.success("操作成功");
        }).catch((e) => {
        })
      }
		},
		computed: {
			groupStore() {
				return this.$store.state.groupStore;
			},
			ownerName() {
				let member = this.groupMembers.find((m) => m.userId == this.activeGroup.ownerId);
				return member && member.aliasName;
			},
			isOwner() {
				return this.activeGroup.ownerId == this.$store.state.userStore.userInfo.id;
			},
      mine() {
        return this.$store.state.userStore.userInfo;
      },
			imageAction(){
				return `/image/upload`;
			}
		},
	}
</script>

<style lang="scss">
	.group-page {
		.group-list-box {
			display: flex;
			flex-direction: column;
      border-right: #54CC36 solid 1px;
			background: white;

			.group-list-header {
        height: 50px;
        display: flex;
        align-items: center;
        padding: 3px 8px;
        background-color: white;
        border-bottom: 1px #ddd solid;

        .add-btn {
          padding: 5px !important;
          margin: 5px;
          font-size: 20px;
          color: #54CC36;
          border: #54CC36 1px solid;
          background-color: #F0F8FF;
          border-radius: 50%;
        }
			}
			
			.group-list-items {
				flex: 1;
			}
		}

		.group-box {
			display: flex;
			flex-direction: column;
			border: var(--border-color) solid 1px;

			.group-header {
				width: 100%;
				height: 50px;
				padding: 5px;
				line-height: 50px;
				font-size: 20px;
				font-weight: 600;
				text-align: left;
				text-indent: 10px;
				background-color: white;
				border: var(--border-color) solid 1px;
			}

			.group-container {
				padding: 20px;
				.group-info {
					display: flex;
					padding: 5px 20px;

          .avatar-box {

            .avatar-uploader {
              text-align: left;

              .el-upload {
                border: 1px dashed #d9d9d9 !important;
                border-radius: 6px;
                cursor: pointer;
                position: relative;
                overflow: hidden;
              }

              .el-upload:hover {
                border-color: #409EFF;
              }

              .avatar-uploader-icon {
                font-size: 28px;
                color: #8c939d;
                width: 200px;
                height: 200px;
                line-height: 200px;
                text-align: center;
              }

              .avatar {
                width: 200px;
                height: 200px;
                display: block;
              }
            }

            .send-btn {
              margin-top: 20px;
            }
          }

          .group-form-box{

            .group-form {
              flex: 1;
              padding-left: 40px;
              min-width: 700px;
            }

            .buttons-box {

              .buttons {
                display: flex;
                justify-content: right;
                margin-bottom: 10px;
              }
            }
          }
				}

				.group-member-list {
					padding: 5px 20px;
					display: flex;
					align-items: center;
					flex-wrap: wrap;
					font-size: 16px;
					text-align: center;

					.group-member {
						margin-right: 15px;
					}

          .member-avatar-upload {
            margin-right: 16px;
            display: flex;
            flex-direction: column;
            align-items: center;
            width: 60px;

            .member-avatar-btn {
              width: 100%;
              height: 60px;
              line-height: 60px;
              border: #cccccc solid 1px;
              font-size: 25px;
              cursor: pointer;
              box-sizing: border-box;

              &:hover {
                border: #aaaaaa solid 1px;
              }

              .avatar-uploader {

                .member-avatar {
                  width: 60px;
                  height: 60px;
                }
              }
            }

            .member-avatar-text {
              font-size: 16px;
              text-align: center;
              width: 100%;
              height: 30px;
              line-height: 30px;
              white-space: nowrap;
              text-overflow: ellipsis;
              overflow: hidden
            }
          }

					.group-invite {
						display: flex;
						flex-direction: column;
						align-items: center;
						width: 60px;

						.invite-member-btn {
							width: 100%;
							height: 60px;
							line-height: 60px;
							border: #cccccc solid 1px;
							font-size: 25px;
							cursor: pointer;
							box-sizing: border-box;

							&:hover {
								border: #aaaaaa solid 1px;
							}
						}

						.invite-member-text {
							font-size: 16px;
							text-align: center;
							width: 100%;
							height: 30px;
							line-height: 30px;
							white-space: nowrap;
							text-overflow: ellipsis;
							overflow: hidden
						}
					}

          .switch-character {
            margin-left: 16px;
            display: flex;
            flex-direction: column;
            align-items: center;
            width: 60px;

            .switch-character-btn {
              width: 100%;
              height: 60px;
              line-height: 60px;
              border: #cccccc solid 1px;
              font-size: 25px;
              cursor: pointer;
              box-sizing: border-box;

              &:hover {
                border: #aaaaaa solid 1px;
              }
            }

            .switch-character-text {
              font-size: 16px;
              text-align: center;
              width: 100%;
              height: 30px;
              line-height: 30px;
              white-space: nowrap;
              text-overflow: ellipsis;
              overflow: hidden
            }
          }

          .switch-character-avatar {
            margin-left: 16px;
            display: flex;
            flex-direction: column;
            align-items: center;
            width: 60px;

            .switch-character-avatar-btn {
              width: 100%;
              height: 60px;
              line-height: 60px;
              border: #cccccc solid 1px;
              font-size: 25px;
              cursor: pointer;
              box-sizing: border-box;

              &:hover {
                border: #aaaaaa solid 1px;
              }
            }

            .switch-character-avatar-text {
              font-size: 16px;
              text-align: center;
              width: 100%;
              height: 30px;
              line-height: 30px;
              white-space: nowrap;
              text-overflow: ellipsis;
              overflow: hidden
            }

            .character-avatar-item-left {
              float: left;
            }
            .character-avatar-item-right {
              float: right;
              margin-right: 10px;
              height: 65px;
              line-height: 65px;
            }
          }

          .member-info {
            margin-left: 16px;
            display: flex;
            flex-direction: column;
            align-items: center;
            width: 60px;

            .view-member-info-btn {
              width: 100%;
              height: 60px;
              line-height: 60px;
              border: #cccccc solid 1px;
              font-size: 25px;
              cursor: pointer;
              box-sizing: border-box;

              &:hover {
                border: #aaaaaa solid 1px;
              }
            }

            .view-member-text {
              font-size: 16px;
              text-align: center;
              width: 100%;
              height: 30px;
              line-height: 30px;
              white-space: nowrap;
              text-overflow: ellipsis;
              overflow: hidden
            }
          }
				}
			}
		}

    .avatar-uploader-group {
      width: 200px;
      height: 200px;
      line-height: 200px;
      margin-bottom: 20px;
      margin-left: 130px;

      .el-upload {
        border: 1px dashed #d9d9d9 !important;
        border-radius: 6px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
      }

      .el-upload:hover {
        border-color: #409EFF;
      }

      .avatar-uploader-icon {
        font-size: 28px;
        color: #8c939d;
        width: 200px;
        height: 200px;
        line-height: 200px;
        text-align: center;
      }

      .avatar {
        width: 200px;
        height: 200px;
        display: block;
      }
    }
	}
</style>
