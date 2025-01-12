<template>
  <el-dialog
      width="30%"
      title="选择角色"
      :visible.sync="visible"
      :before-close="handleClose"
      :append-to-body="appendToBody">
    <div class="character-choose-box">
      <div>
        <el-input width="200px" placeholder="搜索角色" class="input-with-select" v-model="characterSearchText">
          <el-button slot="append" icon="el-icon-search"></el-button>
        </el-input>
      </div>
      <el-scrollbar style="height:400px;">
        <div v-for="(character, index) in characterList" :key="index"
             v-show="character.name.startsWith(characterSearchText)">
          <template-character-item class="character-item-left" :templateCharacter="character"></template-character-item>
          <div class="character-item-right">
            <el-button :type="characterActiveIndex === index ? 'success' : ''"
                       icon="el-icon-check"
                       circle
                       @click="chooseTemplateCharacter(character, index)"></el-button>
          </div>
          <p style="clear:both;"></p>
        </div>
      </el-scrollbar>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleClose">取 消</el-button>
        <el-button type="primary" @click="chooseTemplateCharacterOk">确 定</el-button>
      </span>
    </div>
  </el-dialog>
</template>

<script>
import TemplateCharacterItem from "@/components/group/TemplateCharacterItem";

export default {
  name: "GroupTemplateCharacterChoose",
  components: {
    TemplateCharacterItem,
  },
  props: {
    templateGroupId: {
      type: Number,
      default: null,
    },
    visible: {
      type: Boolean,
      default: false,
    },
    appendToBody: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      characterSearchText: "",
      characterList: [],
      characterActiveIndex: -1,
      activeTemplateCharacter: {},  // 当前选中的模板人物
    }
  },
  methods: {
    handleClose() {
      this.$emit("close");
    },
    chooseTemplateCharacter(templateCharacter, index) {
      // 记录选择的模板人物数组下标
      this.characterActiveIndex = index;
      // 记录选择的模板人物
      this.activeTemplateCharacter = templateCharacter;
    },
    chooseTemplateCharacterOk() {
      if (this.characterActiveIndex === -1) {
        this.$message.warning("请选择一位角色");
        return false
      }
      this.$emit("choose", this.activeTemplateCharacter)
      this.characterActiveIndex = -1;
    },
    queryTemplateCharacter(templateGroupId) {
      this.$http({
        url: `/templateCharacter/list/${templateGroupId}`,
        method: 'get'
      }).then((data) => {
        this.characterList = data;
      });
    }
  },
  watch: {
    visible: function() {
      if (this.visible && this.templateGroupId) {
        this.queryTemplateCharacter(this.templateGroupId);
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.character-choose-box {
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
</style>