<template>
  <el-dialog
      width="30%"
      title="请选择角色"
      :visible.sync="visible"
      :before-close="handleClose">
    <div>
      <el-input width="200px" placeholder="搜索角色" class="input-with-select" v-model="characterSearchText">
        <el-button slot="append" icon="el-icon-search"></el-button>
      </el-input>
    </div>
    <el-scrollbar style="height:400px;">
      <div v-for="(templateCharacter, index) in characters" :key="index"
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
        <el-button @click="handleClose">取 消</el-button>
        <el-button type="primary" @click="handleOk">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import TemplateCharacterItem from "@components/group/TemplateCharacterItem.vue";

export default {
  name: "TemplateCharacterChoose",
  components: {
    TemplateCharacterItem
  },
  props: {
    characters: {
      type: Array,
      default: []
    },
    visible: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      characterSearchText: "",
      characterActiveIndex: -1,
      activeTemplateCharacter: {},  // 当前选中的角色
    };
  },
  methods: {
    handleClose() {
      this.$emit("close");
    },
    handleOk() {
      if (this.characterActiveIndex === -1) {
        this.$message.warning("请选择一位角色");
        return false
      }
      this.$emit("choose", this.activeTemplateCharacter)
      this.characterActiveIndex = -1;
    },
    chooseTemplateCharacter(templateCharacter, index) {
      this.activeTemplateCharacter = templateCharacter;
      this.characterActiveIndex = index;
    },
  }
}
</script>

<style scoped lang="scss">
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