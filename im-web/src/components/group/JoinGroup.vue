<template>
  <el-dialog title="查询群聊" :visible.sync="dialogVisible" width="36%" :before-close="handleClose">
    <el-input  placeholder="输入群聊名称,最多展示10条" class="input-with-select"
               v-model="searchText" @keyup.enter.native="searchByKeyWord()">
      <el-button slot="append" icon="el-icon-search" @click="searchByKeyWord()"></el-button>
    </el-input>
    <el-scrollbar style="height:400px">
      <div v-for="(group) in groups" :key="group.id">
        <div class="item">
          <div class="avatar">
            <head-image :url="group.headImage" :name="group.name"></head-image>
          </div>
          <div class="group-name">
            {{group.name}}
          </div>
          <div class="status-tag">
            <el-tag class="tag" v-if="group.groupType===1" effect="dark" size="medium" type="warning">模板群聊</el-tag>
            <el-tag class="tag" v-if="group.groupType===2" effect="dark" size="medium" type="warning">多元角色群聊</el-tag>
            <el-tag class="tag" v-if="group.groupType===3" effect="dark" size="medium" type="warning">角色群聊</el-tag>
            <el-tag class="tag" v-if="group.groupType===4" effect="dark" size="medium" type="warning">模板角色群聊</el-tag>
          </div>
          <div class="btn">
            <el-button icon="el-icon-search" circle @click="viewGroup(group)" title="查看"></el-button>
            <el-button type="primary" size="medium" @click="joinGroup(group)">加入</el-button>
          </div>
        </div>
      </div>
    </el-scrollbar>
    <div class="page-box">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50]"
          :page-size="10"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
      </el-pagination>
    </div>
    <el-dialog
        width="30%"
        title="请选择模板人物"
        :visible.sync="selectTemplateCharacterVisible"
        :before-close="closeSelectCharacter"
        append-to-body>
      <div>
        <el-input width="200px" placeholder="搜索模板人物" class="input-with-select" v-model="characterSearchText">
          <el-button slot="append" icon="el-icon-search"></el-button>
        </el-input>
      </div>
      <el-scrollbar style="height:400px;">
        <div v-for="(templateCharacter, index) in selectableCharacters" :key="index"
             v-show="templateCharacter.name.startsWith(characterSearchText)">
          <template-character-item class="character-item-left" :templateCharacter="templateCharacter"></template-character-item>
          <div class="character-item-right">
            <el-button :disabled="!templateCharacter.selectable || templateCharacter.choosed "
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
    <template-character-choose :visible="characterChooseVisible" @close="characterChooseVisible = false" @confirm="switchCharacterEvent" :append-to-body="true"></template-character-choose>
    <group-template-character-choose
      :visible="groupTemplateCharacterChooseVisible"
      :append-to-body="true"
      :template-group-id="curChooseGroup.templateGroupId"
      @close="() => {this.groupTemplateCharacterChooseVisible = false}"
      @choose="chooseCharacterOK">
    </group-template-character-choose>
    <el-dialog
        width="25%"
        title="群聊成员信息"
        :visible.sync="groupMemberVisible"
        :before-close="closeGroupMemberInfoDialog"
        append-to-body>
      <el-scrollbar style="height:400px;">
        <div v-for="(groupMember, index) in groupMembers" :key="index">
          <template-group-member class="r-group-member" :member="groupMember" v-show="!groupMember.quit"></template-group-member>
        </div>
      </el-scrollbar>
    </el-dialog>
  </el-dialog>
</template>

<script>
import HeadImage from '../common/HeadImage.vue'
import TemplateCharacterItem from "@/components/group/TemplateCharacterItem";
import TemplateGroupMember from "@/components/group/TemplateGroupMember";
import TemplateCharacterChoose from "@/components/template/TemplateCharacterChoose";
import GroupTemplateCharacterChoose from "@/components/template/GroupTemplateCharacterChoose";

export default {
  name: "JoinGroup",
  components:{
    HeadImage,
    TemplateCharacterItem,
    TemplateGroupMember,
    TemplateCharacterChoose,
    GroupTemplateCharacterChoose
  },
  data() {
    return {
      group: {},
      groups: [],
      searchText: "",
      currentPage: 1,
      pageSize: 10,
      total: 0,
      selectTemplateCharacterVisible: false,
      characterChooseVisible: false,
      groupTemplateCharacterChooseVisible: false,
      selectableCharacters: [], // 可以选择的模板人物
      characterSearchText: '',
      characterActiveIndex: -1,
      curChooseCharacter: null, // 当前选择的模板人物
      curChooseGroup: {}, // 当前选择的群聊
      groupMemberVisible: false,
      groupMembers: [],
      friends: [],
    }
  },
  props: {
    dialogVisible: {
      type: Boolean
    }
  },
  methods: {
    handleClose() {
      this.$emit("close");
    },
    searchByKeyWord() {
      this.currentPage = 1;
      this.handleSearch();
    },
    handleSearch() {
      this.$http({
        url: `/group/queryNotJoinGroups?pageNo=${this.currentPage}&pageSize=${this.pageSize}`,
        method: "get",
        params: {
          keyWord: this.searchText
        }
      }).then((data) => {
        this.total= data.total;
        this.groups = data.data;
      })
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize;
      this.handleSearch();
    },
    handleCurrentChange(currentPage) {
      this.currentPage = currentPage;
      this.handleSearch();
    },
    joinGroup(group) {
      // 是普通群聊
      if (group.groupType === 0) {
        let groupJoinVo = {
          groupId: group.id,
          isTemplate: false,
          groupType: 0,
        }
        this.doJoinGroup(groupJoinVo);
      } else if (group.groupType === 1) {
        this.selectableCharacters = [];
        this.curChooseGroup = group;
        this.querySelectableTemplateCharacter();
        this.selectTemplateCharacterVisible = true;
      } else if (group.groupType === 2 || group.groupType === 3) {
        this.curChooseGroup = group;
        this.characterChooseVisible = true;
      } else if (group.groupType === 4) {
        this.curChooseGroup = group;
        this.groupTemplateCharacterChooseVisible = true;
      }
    },
    doJoinGroup(groupJoinVo) {
      this.$http({
        url: "/group/joinGroup",
        method: "post",
        data: groupJoinVo
      }).then((group) => {
        //this.$store.commit("addGroup", group);
        this.closeSelectCharacter();
        this.$emit("close");
        this.$message.success("操作成功");
        this.handleSearch();
      })
    },
    closeSelectCharacter() {
      this.characterActiveIndex = -1;
      this.selectTemplateCharacterVisible = false;
      this.characterChooseVisible = false;
      this.curChooseGroup = {};
      this.curChooseCharacter = null;
    },
    chooseTemplateCharacter(templateCharacter, index) {
      this.curChooseCharacter = templateCharacter;
      this.characterActiveIndex = index;
    },
    chooseTemplateCharacterOk() {
      let groupJoinVo = {
        groupId: this.curChooseGroup.id,
        isTemplate: true,
        groupType: this.curChooseGroup.groupType,
        templateGroupId: this.curChooseGroup.templateGroupId,
        templateCharacterId: this.curChooseCharacter.id,
        templateCharacterAvatar: this.curChooseCharacter.avatar,
        templateCharacterName: this.curChooseCharacter.name
      }
      this.doJoinGroup(groupJoinVo);
    },
    querySelectableTemplateCharacter() {
      let paramVO = {
        groupId: this.curChooseGroup.id,
        templateGroupId: this.curChooseGroup.templateGroupId
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
    viewGroup(group) {
      this.group = group;
      this.groupMembers = [];
      this.loadGroupMembers(group);
      this.groupMemberVisible = true;
    },
    closeGroupMemberInfoDialog() {
      this.groupMemberVisible = false;
    },
    loadGroupMembers(group) {
      this.$http({
        url: `/group/members/${group.id}`,
        method: "get"
      }).then((members) => {
        members.forEach((item, index) => {
          let friend = this.friends.find((f) => f.id === item.userId);
          if (friend) {
            if (this.group.groupType !== 0) {
              item.nickName = friend.friendRemark ? friend.friendRemark : item.nickName;
            } else {
              item.aliasName = friend.friendRemark ? friend.friendRemark : item.aliasName;
            }
          }
        })
        this.groupMembers = members;
      })
    },
    switchCharacterEvent(data) {
      this.characterChooseVisible = false;
      let groupJoinVo = {
        groupId: this.curChooseGroup.id,
        isTemplate: false,
        groupType: this.curChooseGroup.groupType,
        templateCharacterId: data.templateCharacter.id,
        templateCharacterAvatar: data.templateCharacter.avatar,
        templateCharacterName: data.templateCharacter.name
      }
      this.doJoinGroup(groupJoinVo);
    },
    chooseCharacterOK(character) {
      this.groupTemplateCharacterChooseVisible = false;
      console.log("character", character)
      let groupJoinVo = {
        groupId: this.curChooseGroup.id,
        isTemplate: true,
        groupType: this.curChooseGroup.groupType,
        templateGroupId: this.curChooseGroup.templateGroupId,
        templateCharacterId: character.id,
        templateCharacterAvatar: character.avatar,
        templateCharacterName: character.name
      }
      this.doJoinGroup(groupJoinVo);
    }
  },
  mounted() {

  },
  watch: {
    dialogVisible: {
      handler(newValue, oldValue) {
        this.friends = this.$store.state.friendStore.friends;
        this.handleSearch();
      }
    }
  }
}
</script>

<style scoped lang="scss">
.item {
  height: 80px;
  display: flex;
  position: relative;
  padding-left: 15px;
  align-items: center;
  padding-right: 25px;

  .group-name {
    margin-left: 20px;
  }

  .status-tag {
    margin-left: 20px;
  }

  .btn {
    margin-left: auto;
  }
}
</style>