<template>
  <el-dialog
      width="50%"
      title="选择角色"
      :visible.sync="visible"
      :before-close="handleClose"
      :modal="modal"
      :append-to-body="appendToBody">
    <div class="agm-container">
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
        <div @click="viewCharacterAvatars">
          <el-avatar icon="el-icon-user-solid" :src="characterAvatar.avatar"></el-avatar>
        </div>
			  <el-button @click="handleClose()" size="small">取 消</el-button>
			  <el-button type="primary" @click="handleOk()" size="small">确 定</el-button>
    </span>
    <character-avatar-choose
        :visible="characterAvatarVisible"
        :appendToBody="appendToBody"
        :characterId="templateCharacter.id"
        :modal="false"
        @close="closeCharacterAvatarVisible"
        @confirm="confirmCharacterAvatar">
    </character-avatar-choose>
  </el-dialog>
</template>

<script>
import HeadImage from "@/components/common/HeadImage";
import TemplateGroupItem from "@/components/group/TemplateGroupItem";
import TemplateCharacterItem from "@/components/group/TemplateCharacterItem";
import CharacterAvatarChoose from "./CharacterAvatarChoose.vue";

export default {
  name: "TemplateCharacterChoose",
  components: {
    HeadImage,
    TemplateGroupItem,
    TemplateCharacterItem,
    CharacterAvatarChoose,
  },
  props: {
    visible: {
      type: Boolean
    },
    appendToBody: {
      type: Boolean,
      default: false
    },
    modal: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      templateGroupList: [],
      templateCharacterList: [],
      groupActiveIndex: -1,
      characterActiveIndex: -1,
      searchText: '',
      characterSearchText: '',
      templateGroup: {},
      templateCharacter: {},
      characterAvatar: {},
      resultData: {},
      characterAvatarVisible: false
    }
  },
  created() {

  },
  methods: {
    handleClose() {
      this.$emit("close");
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
    queryTemplateCharacter(templateGroup, index) {
      if (this.templateGroup.id !== templateGroup) {
        this.characterAvatar = {};
        this.templateCharacter = {};
      }
      this.templateGroup = templateGroup;
      this.groupActiveIndex = index;
      this.characterActiveIndex = -1;
      this.$http({
        url: `/templateCharacter/list/${templateGroup["id"]}`,
        method: 'get'
      }).then((data) => {
        this.templateCharacterList = data;
      });
    },
    chooseTemplateCharacter(templateCharacter, index) {
      this.characterActiveIndex = index;
      this.templateCharacter = templateCharacter;
      if (this.templateCharacter.id !== this.characterAvatar.templateCharacterId) {
        this.characterAvatar = {};
      }
    },
    handleOk() {
      this.groupActiveIndex = -1;
      this.characterActiveIndex = -1;
      this.templateGroupList = [];
      this.templateCharacterList = [];
      this.resultData.templateGroup = this.templateGroup
      this.resultData.templateCharacter = this.templateCharacter
      this.resultData.characterAvatar = this.characterAvatar
      this.$emit("confirm", this.resultData);
    },
    confirmCharacterAvatar(data) {
      this.characterAvatar = data;
      this.characterAvatarVisible = false;
    },
    closeCharacterAvatarVisible() {
      this.characterAvatarVisible = false;
    },
    viewCharacterAvatars() {
      if (!this.templateCharacter.id) {
        this.$message.warning('请先选择一个角色');
        return;
      }
      this.characterAvatarVisible = true;
    }
  },
  watch: {
    visible: function() {
      if (this.visible) {
        this.queryTemplateGroup();
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
  }
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: right;
  gap: 10px;
}
</style>