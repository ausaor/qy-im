<template>
  <el-dialog title="邀请好友" :visible.sync="visible"  width="42%" :before-close="onClose">
    <div class="agm-container">
      <div class="agm-l-box">
        <el-input width="200px" placeholder="搜索好友" class="input-with-select" v-model="searchText">
          <i class="el-icon-search el-input__icon" slot="prefix"></i>
        </el-input>
        <el-scrollbar style="height:400px;">
          <div v-for="(friend,index) in friends" :key="friend.id" v-show="friend.nickName.startsWith(searchText)">
            <friend-item :showDelete="false" @click.native="onSwitchCheck(friend)"
                         :menu="false" :friend="friend" :index="index" :active="false" :groupType="groupType">
              <el-checkbox :disabled="friend.disabled" @click.native.stop="" class="agm-friend-checkbox" v-model="friend.isCheck"
               size="medium"></el-checkbox>
            </friend-item>
          </div>
        </el-scrollbar>
      </div>
      <div class="agm-arrow el-icon-d-arrow-right"></div>
      <div class="agm-r-box">
        <div class="agm-select-tip"> 已勾选{{checkCount}}位好友</div>
        <el-scrollbar style="height:400px;">
          <div v-for="(friend,index) in friends" :key="friend.id">
            <friend-item v-if="friend.isCheck && !friend.disabled" :friend="friend" :index="index" :active="false"
                         :menu="false" :isTemplate="isTemplate" @del="onRemoveFriend(friend)"
                         :groupType="groupType" :selectCharacterVisible="groupType !== 0"
                         @select="selectCharacter(friend, index)">
            </friend-item>
          </div>
        </el-scrollbar>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="onClose()">取 消</el-button>
      <el-button type="primary" @click="onOk()">确 定</el-button>
    </span>
    <el-dialog
        width="30%"
        title="请选择角色"
        :visible.sync="selectTemplateCharacterVisible"
        :before-close="closeSelectCharacter"
        append-to-body>
      <div>
        <el-input width="200px" placeholder="搜索角色" class="input-with-select" v-model="characterSearchText">
          <i class="el-icon-search el-input__icon" slot="prefix"></i>
        </el-input>
      </div>
      <el-scrollbar style="height:400px;">
        <div v-for="(templateCharacter, index) in selectableCharacters" :key="index"
             v-show="templateCharacter.name.startsWith(characterSearchText)">
          <template-character-item class="character-item-left" :templateCharacter="templateCharacter"></template-character-item>
          <div class="character-item-right">
            <el-button :disabled="!templateCharacter.selectable ||templateCharacter.choosed "
                       :type="characterActiveIndex === index ? 'success' : ''"
                       icon="el-icon-check"
                       circle
                       @click="chooseTemplateCharacter(templateCharacter, index)"></el-button>
          </div>
          <p style="clear:both;"></p>
        </div>
      </el-scrollbar>
      <span slot="footer" class="dialog-footer">
      <el-button @click="closeSelectCharacter">取 消</el-button>
      <el-button type="primary" @click="chooseTemplateCharacterOk">确 定</el-button>
    </span>
    </el-dialog>
    <el-dialog title="选择群聊模板" :visible.sync="selectCharactersVisible"  width="50%" :before-close="closeCharacterDialog" append-to-body>
      <div class="template-group-container">
        <div class="agm-l-box">
          <el-input width="200px" placeholder="搜索模板群聊" class="input-with-select" v-model="searchText" >
            <i class="el-icon-search el-input__icon" slot="prefix"></i>
          </el-input>
          <el-scrollbar style="height:400px;">
            <div v-for="(templateGroup,index) in templateGroupList" :key="templateGroup.id"
                 v-show="templateGroup.groupName.startsWith(searchText)"
                 class="template-group-box">
              <template-group-item :templateGroup="templateGroup" class="group-item-left"></template-group-item>
              <div class="group-item-right">
                <el-button :type="groupActiveIndex === index ? 'success' : ''" icon="el-icon-check" circle
                           @click="queryTemplateCharacter(templateGroup, index)" ></el-button>
              </div>
              <p style="clear:both;"></p>
            </div>
          </el-scrollbar>
        </div>
        <div class="agm-r-box">
          <el-input width="200px" placeholder="搜索角色" class="input-with-select" v-model="characterSearchText" >
            <i class="el-icon-search el-input__icon" slot="prefix"></i>
          </el-input>
          <el-scrollbar style="height:400px;">
            <div class="template-character-box" v-for="(templateCharacter,index) in templateCharacterList"
                 :key="templateCharacter.id" v-show="templateCharacter.name.startsWith(characterSearchText)">
              <template-character-item class="character-item-left" :templateCharacter = "templateCharacter"></template-character-item>
              <div class="character-item-right">
                <el-button :type="characterActiveIndex === index ? 'success' : ''" icon="el-icon-check" circle
                           @click="chooseTemplateCharacter(templateCharacter, index)"></el-button>
              </div>
              <p style="clear:both;"></p>
            </div>
          </el-scrollbar>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
			<el-button @click="handleClose()">取 消</el-button>
			<el-button type="primary" @click="handleOk()">确 定</el-button>
		</span>
    </el-dialog>
    <group-template-character-choose
        :visible="groupTemplateCharacterVisible"
        :template-group-id="templateGroupId"
        :append-to-body="true"
        @close="() => {this.groupTemplateCharacterVisible=false}"
        @choose="chooseCharacterOK">
    </group-template-character-choose>
  </el-dialog>
</template>

<script>
	import FriendItem from '../friend/FriendItem.vue';
  import TemplateCharacterItem from "@/components/group/TemplateCharacterItem";
  import HeadImage from '../common/HeadImage.vue';
  import TemplateGroupItem from "@/components/group/TemplateGroupItem";
  import GroupTemplateCharacterChoose from "@/components/template/GroupTemplateCharacterChoose";

	export default {
		name: "addGroupMember",
		components: {
			FriendItem,
      TemplateCharacterItem,
      HeadImage,
      TemplateGroupItem,
      GroupTemplateCharacterChoose,
		},
		data() {
			return {
				searchText: "",
        characterSearchText: "",
				friends: [],
        selectTemplateCharacterVisible: false,
        selectCharactersVisible: false,
        groupTemplateCharacterVisible: false,
        characterActiveIndex: -1,
        activeTemplateCharacter: {},  // 当前选中的角色
        curSelectedFriend: {},
        selectedFriendIndex: -1,
        templateGroupList: [],
        templateCharacterList: [],
        groupActiveIndex: -1,
        activeTemplateGroup: {},
			}
		},
    props: {
      visible: {
        type: Boolean
      },
      groupId: {
        type: Number
      },
      members: {
        type: Array
      },
      isTemplate: {
        type: Boolean
      },
      templateGroupId: {
        type: Number
      },
      selectableCharacters: {
        type: Array
      },
      groupType: {
        type: Number,
      }
    },
		methods: {
      onClose() {
        for (let i= 0; i < this.selectableCharacters.length; i++) {
          if (this.selectableCharacters[i].selectable) {
            this.selectableCharacters[i].choosed = false;
          }
        }
				this.$emit("close");
			},
      onOk() {
        if (!this.groupId) {
          this.$message.warning("请选择一个群聊");
          return
        }
			  let returnFlag = false;
        if (this.groupType !== 0) {
          this.friends.forEach((f) => {
            if (f.isCheck && !f.disabled) {
              if (f.templateCharacterId === undefined || f.templateCharacterId === null) {
                returnFlag = true;
              }
            }
          })
          if (returnFlag) {
            this.$message.warning("请为用户分配模板角色");
            return false
          }
        }

				let inviteVO = {
					groupId: this.groupId,
          inviteFriends: [],
          isTemplate: this.isTemplate,
          groupType: this.groupType,
				}
				this.friends.forEach((f) => {
					if (f.isCheck && !f.disabled) {
            let obj = {
              friendId: f.id,
              templateCharacterId: f.templateCharacterId,
              templateCharacterAvatar: f.templateCharacterAvatar,
              templateCharacterName: f.templateCharacterName
            }
						inviteVO.inviteFriends.push(obj);
					}
				})
        if (this.groupType === 1 || this.groupType === 2) {
          const templateCharacterIds = inviteVO.inviteFriends.map(item => item["templateCharacterId"]);
          let templateCharacterIdSet = new Set(templateCharacterIds);
          if (templateCharacterIdSet.size !== templateCharacterIds.length) {
            this.$message.warning("存在重复的模板角色，请重新选择");
            return;
          }
        }
				if (inviteVO.inviteFriends.length > 0) {
					this.$http({
						url: "/group/invite",
						method: 'post',
						data: inviteVO
					}).then(() => {
						this.$message.success("邀请成功");
					}).finally(() => {
            this.$emit("reload");
            this.$emit("close");
          })
				}
			},
      onRemoveFriend(friend) {
				friend.isCheck = false;
				// 将已选择的角色的不可选标识去掉
				if (friend.choosedCharacterIndex !== undefined &&  friend.choosedCharacterIndex !== null) {
          this.selectableCharacters[friend.choosedCharacterIndex].choosed = false;
        }
        friend.choosedCharacterIndex = null;
        friend.templateCharacterId = null;
        friend.templateCharacterAvatar = null;
        friend.templateCharacterName = null;
			},
      onSwitchCheck(friend) {
        // 取消好友选择，需要将好友已选的角色变成可选状态
        if (this.groupType === 1) {
          if (!friend.disabled) {
            if (friend.isCheck) {
              if (friend.choosedCharacterIndex !== undefined &&  friend.choosedCharacterIndex !== null) {
                this.selectableCharacters[friend.choosedCharacterIndex].choosed = false;
              }
              friend.choosedCharacterIndex = null;
              friend.templateCharacterId = null;
              friend.templateCharacterAvatar = null;
              friend.templateCharacterName = null;
            }
            friend.isCheck = !friend.isCheck
          }
        } else {
          friend.isCheck = !friend.isCheck
        }
			},
      selectCharacter(friend, index) {
        if (this.groupType === 1) {
          this.selectTemplateCharacterVisible = true
        } else if (this.groupType === 2 || this.groupType === 3) {
          this.queryTemplateGroup();
          this.selectCharactersVisible = true;
        } else if (this.groupType === 4) {
          this.groupTemplateCharacterVisible = true;
        }
        this.selectedFriendIndex = index;
        this.curSelectedFriend = friend;
      },
      closeSelectCharacter() {
        this.characterActiveIndex = -1;
        this.activeTemplateCharacter = {};
        this.selectTemplateCharacterVisible = false
      },
      chooseTemplateCharacter(templateCharacter, index) {
			  // 记录选择的角色数组下标
        this.characterActiveIndex = index;
        // 记录选择的角色
        this.activeTemplateCharacter = templateCharacter;
      },
      chooseTemplateCharacterOk() {
			  if (this.characterActiveIndex === -1) {
          this.$message.warning("请选择一位角色");
			    return false
        }
        // 标识当前角色已被选择
        this.selectableCharacters[this.characterActiveIndex].choosed = true;
        let friendIndex = this.selectedFriendIndex;
        // 当前好友之前已选择过角色，需要把之前选择的角色的不可选标识去掉
        if (this.friends[friendIndex].choosedCharacterIndex !== undefined
            && this.friends[friendIndex].choosedCharacterIndex !== null) {
          this.selectableCharacters[this.friends[friendIndex].choosedCharacterIndex].choosed = false;
        }
        // 记录被选择的角色的位置下标
        this.friends[friendIndex].choosedCharacterIndex = this.characterActiveIndex;
        this.friends[friendIndex].templateCharacterId = this.activeTemplateCharacter.id;
        this.friends[friendIndex].templateCharacterAvatar = this.activeTemplateCharacter.avatar;
        this.friends[friendIndex].templateCharacterName = this.activeTemplateCharacter.name;

        //this.curSelectedFriend.choosedCharacterIndex = this.characterActiveIndex;
        //this.curSelectedFriend.templateCharacterId = this.activeTemplateCharacter.id;
        //this.curSelectedFriend.templateCharacterAvatar = this.activeTemplateCharacter.avatar;
        //this.curSelectedFriend.templateCharacterName = this.activeTemplateCharacter.name;
        //this.$set(this.friends, this.selectedFriendIndex, this.curSelectedFriend);

        //let newFriends = Object.assign([], this.friends);
        //console.log("friends", this.friends);
        //console.log("newFriends", newFriends);

        this.characterActiveIndex = -1;
        this.activeTemplateCharacter = {};
        this.selectTemplateCharacterVisible = false;
        //this.$forceUpdate();
      },
      closeCharacterDialog() {
        this.selectCharactersVisible = false;
      },
      queryTemplateCharacter(templateGroup, index) {
        this.groupActiveIndex = index;
        this.activeTemplateGroup = templateGroup;
        this.characterActiveIndex = -1;
        this.$http({
          url: `/templateCharacter/list/${templateGroup["id"]}`,
          method: 'get'
        }).then((data) => {
          this.templateCharacterList = data;
        });
      },
      queryTemplateGroup(){
        this.templateGroupList = []
        this.$http({
          url: "/templateGroup/list",
          method: 'get',
          params: ''
        }).then(data => {
          this.templateGroupList = data;
        })
      },
      handleClose() {
        this.groupActiveIndex = -1;
        this.characterActiveIndex = -1;
        this.templateGroupList = [];
        this.templateCharacterList = [];
        this.activeTemplateGroup = {};
        this.selectCharactersVisible = false;
      },
      handleOk() {
        if (this.characterActiveIndex === -1) {
          this.$message.warning("请选择一位角色");
          return false
        }
        let friendIndex = this.selectedFriendIndex;

        // 记录被选择的角色的位置下标
        this.friends[friendIndex].choosedCharacterIndex = this.characterActiveIndex;
        this.friends[friendIndex].templateCharacterId = this.activeTemplateCharacter.id;
        this.friends[friendIndex].templateCharacterAvatar = this.activeTemplateCharacter.avatar;
        this.friends[friendIndex].templateCharacterName = this.activeTemplateCharacter.name;

        this.characterActiveIndex = -1;
        this.activeTemplateCharacter = {};
        this.handleClose();
      },
      chooseCharacterOK(character) {
        let friendIndex = this.selectedFriendIndex;
        this.friends[friendIndex].templateCharacterId = character.id;
        this.friends[friendIndex].templateCharacterAvatar = character.avatar;
        this.friends[friendIndex].templateCharacterName = character.name;
        this.groupTemplateCharacterVisible = false;
      },
		},
		computed: {
			checkCount() {
				return this.friends.filter((f) => f.isCheck && !f.disabled).length;
			}
		},
		watch: {
			visible: function(newData, oldData) {
				if (newData) {
					this.friends = [];
					this.$store.state.friendStore.friends.forEach((f) => {
						let friend = JSON.parse(JSON.stringify(f))
						let m = this.members.filter((m) => !m.quit)
							.find((m) => m.userId == f.id);
						//console.log(m);
						if (m) {
							// 好友已经在群里
							friend.disabled = true;
							friend.isCheck = true
						} else {
							friend.disabled = false;
							friend.isCheck = false;
						}
						friend.templateCharacterAvatar = '';
						this.friends.push(friend);
					})
				}
			}
		}

	}
</script>

<style scoped lang="scss">
	.agm-container {
		display: flex;

		.agm-l-box {
			flex: 1;
      border-radius: 5px;
      overflow: hidden;

			.agm-friend-checkbox {
				margin-right: 20px;
			}
		}

    .agm-arrow {
      display: flex;
      align-items: center;
      font-size: 20px;
      padding: 5px;
      font-weight: 600;
      color: #53a0e7cc;
    }

		.agm-r-box {
			flex: 1;
      border-radius: 5px;

			.agm-select-tip {
				text-align: left;
				height: 40px;
				line-height: 40px;
				text-indent: 5px;
			}
		}
	}

  .template-group-container {
    display: flex;

    .agm-l-box {
      flex: 1;
      border: var(--border-color) solid 1px;
      box-sizing: content-box;

      .template-group-box {
        width: 100%;

        .group-item-left {
          float: left;
        }
        .group-item-right {
          float: right;
          margin-right: 10px;
          height: 65px;
          line-height: 65px;
        }
      }
    }

    .agm-r-box {
      flex: 1;
      border: var(--border-color) solid 1px;

      .template-character-box {
        width: 100%;

        .character-item-left {
          float: left;
        }
        .character-item-right {
          float: right;
          margin-right: 10px;
          height: 65px;
          line-height: 65px;
        }
      }
      .agm-select-tip {
        text-align: left;
        height: 40px;
        line-height: 40px;
        text-indent: 5px;
      }
    }
  }

  .character-item-left {
    float: left;
  }

  .character-item-right {
    float: right;
    margin-right: 10px;
    height: 65px;
    line-height: 65px;
  }
</style>
