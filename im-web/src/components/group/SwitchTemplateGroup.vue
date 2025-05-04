<template>
  <el-dialog title="群聊类型切换" :visible.sync="visible" width="50%" :before-close="handleClose">
    <div class="agm-container">
      <div class="agm-l-box">
        <el-input width="200px" placeholder="搜索模板群聊" class="input-with-select" v-model="searchText">
          <el-button slot="append" icon="el-icon-search"></el-button>
        </el-input>
        <el-scrollbar style="height:400px;">
          <div v-for="(templateGroup,index) in templateGroupList" :key="templateGroup.id" class="template-group-box">
            <template-group-item :templateGroup="templateGroup" class="group-item-left"></template-group-item>
            <div class="group-item-right">
              <el-button :type="groupActiveIndex === index ? 'success' : ''" icon="el-icon-check" circle
                         @click="queryTemplateCharacter(templateGroup, index)"></el-button>
            </div>
            <p style="clear:both;"></p>
          </div>
        </el-scrollbar>
      </div>
      <div class="agm-r-box">
        <el-scrollbar style="height:400px;">
          <div v-for="(friend,index) in groupMembers" :key="index">
            <user-item v-show="!friend.quit" :friend="friend" :index="index" :active="false"
                         :isTemplate="true" :showDelete="false"
                         @select="selectCharacter(friend, index)">
            </user-item>
          </div>
        </el-scrollbar>
      </div>
    </div>
    <el-dialog
        width="30%"
        title="请选择角色"
        :visible.sync="selectTemplateCharacterVisible"
        :before-close="closeSelectCharacter"
        append-to-body>
      <el-scrollbar style="height:400px;">
        <div v-for="(templateCharacter, index) in templateCharacterList" :key="index">
          <template-character-item class="character-item-left" :templateCharacter="templateCharacter"></template-character-item>
          <div class="character-item-right">
            <el-button :type="characterActiveIndex === index ? 'success' : ''"
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
    <span slot="footer" class="dialog-footer">
      <el-button v-show="toGroupType===2 || toGroupType === 3" @click="randomSetCharacters">随机设置</el-button>
			<el-button @click="handleClose()">取 消</el-button>
			<el-button type="primary" @click="handleOk()">确 定</el-button>
		</span>
  </el-dialog>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';
import TemplateGroupItem from "@/components/group/TemplateGroupItem";
import TemplateCharacterItem from "@/components/group/TemplateCharacterItem";
import FriendItem from '../friend/FriendItem.vue';
import UserItem from "@/components/user/UserItem";

export default {
  name: "SwitchTemplateGroup",
  components: {
    HeadImage,
    TemplateGroupItem,
    TemplateCharacterItem,
    FriendItem,
    UserItem
  },
  props: {
    visible: {
      type: Boolean
    },
    group: {
      type: Object
    },
    groupMembers: {
      type: Array
    },
    toGroupType: {
      type: Number,
    }
  },
  data() {
    return {
      searchText: "",
      activeIndex: -1,
      templateGroupList: [],
      templateCharacterList: [],
      groupActiveIndex: -1,
      templateGroup: {},
      selectTemplateCharacterVisible: false,
      selectedFriendIndex: -1,
      curSelectedFriend: {},
      characterActiveIndex: -1,
      activeTemplateCharacter: {},  // 当前选中的角色
    }
  },
  created() {

  },
  methods: {
    handleClose() {
      this.groupMembers.forEach(function (item) {
        item.templateCharacterAvatar = null;
        item.templateCharacterId = null;
        item.templateCharacterName = null;
      })
      this.groupActiveIndex = -1;
      this.templateCharacterList = [];
      this.templateGroup = {};
      this.$emit("close");
    },
    handleOk() {
      let groupMemberList = this.groupMembers.filter(item => !item.quit);
      if (this.toGroupType === 1 || this.toGroupType === 2) {
        // 判断是否有重复的角色
        const templateCharacterIds = groupMemberList.map(item => item["templateCharacterId"]);
        let templateCharacterIdSet = new Set(templateCharacterIds);
        //console.log("templateCharacterIds", templateCharacterIds)
        //console.log("templateCharacterIdSet", templateCharacterIdSet)
        if (templateCharacterIdSet.size !== templateCharacterIds.length) {
          this.$message.warning("存在重复的模板角色，请重新选择");
          return;
        }
      }
      let flag = false;
      for (let i = 0; i < groupMemberList.length; i++) {
        if (!groupMemberList[i].templateCharacterId) {
            flag = true;
            break;
        }
      }
      if (flag) {
        this.$message.warning("有用户未分配角色！");
        return;
      }

      let paramVO = {
        groupId: this.group.id,
        name: this.group.name,
        templateGroupId: this.templateGroup.id,
        groupMembers: groupMemberList,
        toGroupType: this.toGroupType,
      }
      //console.log("paramVO", paramVO)
      let url = null;
      if (this.toGroupType === 1) {
        url = "/group/switchTemplateGroup";
      } else if (this.toGroupType === 2) {
        url = "/group/switchMultCharacterGroup";
      } else if (this.toGroupType === 3) {
        url = "/group/switchMoreCharactersGroup"
      } else if (this.toGroupType === 4) {
        url = "/group/switchTemplateMultCharactersGroup"
      }
      this.$http({
        url: url,
        method: 'post',
        data: paramVO
      }).then((group) => {
        this.$store.commit("updateGroup", group);
        this.$store.commit("updateChatFromGroup", group);
        this.$message.success("切换成功");
      }).finally(() =>{
        this.groupActiveIndex = -1;
        this.templateCharacterList = [];
        this.templateGroup = {};
        this.$emit("close");
      })
    },
    queryTemplateGroup(){
      this.templateGroupList = []
      this.$http({
        url: "/templateGroup/list",
        method: 'get',
        params: ''
      }).then(data => {
        this.templateGroupList = data;
        //this.$forceUpdate()
      })
    },
    queryTemplateCharacter(templateGroup, index) {
      //console.log("curGroup", this.group)
      if (this.group.templateGroupId === templateGroup.id) {
        this.$message.warning("请选择另一个类型的模板群聊");
        return false
      }
      if (this.toGroupType === 1) {
        const count = this.groupMembers.filter(item => !item.quit).length;
        if (templateGroup.count < count) {
          this.$message.warning("当前模板群聊的角色数量小于当前群聊人数，请选择另一个模板群聊");
          return false
        }
      }

      this.groupActiveIndex = index;
      this.templateGroup = templateGroup;
      this.$http({
        url: `/templateCharacter/list/${templateGroup["id"]}`,
        method: 'get'
      }).then((data) => {
        this.templateCharacterList = data;
        if (this.toGroupType === 1 || this.toGroupType === 4) {
          let index = 0;
          for (let i=0; i<this.groupMembers.length; i++) {
            if (!this.groupMembers[i].quit && index < data.length) {
              this.groupMembers[i].templateCharacterAvatar = this.templateCharacterList[index].avatar
              this.groupMembers[i].templateCharacterId = this.templateCharacterList[index].id
              this.groupMembers[i].templateCharacterName = this.templateCharacterList[index].name
              index++;
            }
          }
        }
      });
    },
    selectCharacter(friend, index) {
      if (this.groupActiveIndex===-1) {
        this.$message.warning("请选择一个模板群聊");
        return false;
      }
      this.selectTemplateCharacterVisible = true
      this.selectedFriendIndex = index;
      this.curSelectedFriend = friend;
    },
    closeSelectCharacter() {
      this.selectTemplateCharacterVisible = false;
      this.characterActiveIndex = -1;
      this.activeTemplateCharacter = {};
    },
    chooseTemplateCharacter(templateCharacter, index) {
      this.characterActiveIndex = index;
      this.activeTemplateCharacter = templateCharacter;
    },
    chooseTemplateCharacterOk() {
      let friendIndex = this.selectedFriendIndex;
      this.groupMembers[friendIndex].templateCharacterAvatar = this.activeTemplateCharacter.avatar;
      this.groupMembers[friendIndex].templateCharacterId = this.activeTemplateCharacter.id;
      this.groupMembers[friendIndex].templateCharacterName = this.activeTemplateCharacter.name;
      this.characterActiveIndex = -1;
      this.activeTemplateCharacter = {};
      this.selectTemplateCharacterVisible = false;
    },
    randomSetCharacters() {
      let count = this.groupMembers.filter(item => !item.quit).length;
      this.$http({
        url: '/templateCharacter/getRandomCharacters?count=' + count,
        method: 'get'
      }).then((characters) => {
        if (characters && characters.length > 0) {
          let index = 0;
          this.groupMembers.filter(item => {
            if (!item.quit && index < characters.length) {
              item.templateCharacterAvatar = characters[index].avatar
              item.templateCharacterId = characters[index].id
              item.templateCharacterName = characters[index].name
              index++;
            }
          })
        }
      })
    }
  },
  watch: {
    visible:function () {
      if (this.visible) {
        this.queryTemplateGroup()
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