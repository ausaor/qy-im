<template>
  <el-dialog title="选择群聊模板" :visible.sync="visible"  width="50%" :before-close="handleClose">
    <div class="agm-container">
      <div class="agm-l-box">
        <el-input width="200px" placeholder="搜索模板群聊" class="input-with-select" v-model="searchText" >
          <el-button slot="append" icon="el-icon-search" ></el-button>
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
          <el-button slot="append" icon="el-icon-search" ></el-button>
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
</template>

<script>
import HeadImage from '../common/HeadImage.vue';
import TemplateGroupItem from "@/components/group/TemplateGroupItem";
import TemplateCharacterItem from "@/components/group/TemplateCharacterItem";
export default {
  name: "CreateTemplateGroup",
  components: {
    HeadImage,
    TemplateGroupItem,
    TemplateCharacterItem
  },
  props: {
    visible: {
      type: Boolean
    },
    groupType: {
      type: Number,
      required: true,
    }
  },
  data() {
    return {
      searchText: "",
      characterSearchText: "",
      activeIndex: -1,
      templateGroupList: [],
      templateCharacterList: [],
      radio: -1,
      groupActiveIndex: -1,
      characterActiveIndex: -1,
      templateGroup: {},
      templateCharacter: {}
    }
  },
  methods: {
    handleClose() {
      this.$emit("close");
      this.groupActiveIndex = -1;
      this.characterActiveIndex = -1;
      this.templateGroupList = [];
      this.templateCharacterList = [];
      this.templateGroup = {};
      this.templateCharacter = {};
    },
    handleOk() {
      let _this = this;
      if (this.templateGroup.id === null || this.templateGroup.id === undefined) {
        this.$message.warning("请选择一个群聊模板");
        return;
      }
      if (this.templateCharacter.id === null || this.templateCharacter.id === undefined) {
        this.$message.warning("请选择一个角色");
        return;
      }
      if (this.groupType===2 || this.groupType===3) {
        this.$prompt('请输入群聊名称', '创建群聊', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /\S/,
          inputErrorMessage: '请输入群聊名称'
        }).then((o) => {
          let templateGroup = {
            templateGroupId: this.templateGroup.id,
            templateGroupAvatar: this.templateGroup.avatar,
            templateGroupName: this.templateGroup.groupName,
            templateCharacterId: this.templateCharacter.id,
            templateCharacterAvatar: this.templateCharacter.avatar,
            templateCharacterName: this.templateCharacter.name,
            groupType: this.groupType,
            name: o.value,
          }
          this.$http({
            url: "/group/createTemplateGroup",
            method: 'post',
            data: templateGroup
          }).then((group) => {
            this.$message.success('创建成功');
            this.$store.commit("addGroup", group);
            _this.handleClose();
          })
        })
      } else {
        let templateGroup = {
          templateGroupId: this.templateGroup.id,
          templateGroupAvatar: this.templateGroup.avatar,
          templateGroupName: this.templateGroup.groupName,
          templateCharacterId: this.templateCharacter.id,
          templateCharacterAvatar: this.templateCharacter.avatar,
          templateCharacterName: this.templateCharacter.name,
          groupType: this.groupType,
          name: this.templateGroup.groupName,
        }
        this.$http({
          url: "/group/createTemplateGroup",
          method: 'post',
          data: templateGroup
        }).then(group => {
          this.$message.success("创建成功");
          this.$store.commit("addGroup", group);
          _this.handleClose();
        })
      }
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
      this.groupActiveIndex = index;
      this.templateGroup = templateGroup;
      this.templateCharacter = {};
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
</style>