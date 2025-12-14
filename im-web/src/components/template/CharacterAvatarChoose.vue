<template>
  <el-dialog
      width="24%"
      title="选择角色头像"
      :visible.sync="visible"
      :before-close="handleClose"
      :modal="modal"
      :append-to-body="appendToBody">
    <el-scrollbar style="height:400px;">
      <div v-for="(characterAvatar, index) in characterAvatars" :key="index" class="character-avatar-item">
        <character-avatar-item class="left" :characterAvatar="characterAvatar"></character-avatar-item>
        <div class="right">
          <el-button :type="avatarActiveIndex === index ? 'success' : ''"
                     icon="el-icon-check"
                     circle
                     @click="chooseCharacterAvatar(characterAvatar, index)"></el-button>
        </div>
      </div>
    </el-scrollbar>
    <span slot="footer" class="dialog-footer">
        <el-button @click="handleClose" size="small">取 消</el-button>
        <el-button type="primary" @click="handleOk" size="small">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import CharacterAvatarItem from "@/components/group/CharacterAvatarItem";

export default {
  name: "TemplateCharacterChoose",
  components: {
    CharacterAvatarItem
  },
  props: {
    visible: {
      type: Boolean
    },
    appendToBody: {
      type: Boolean,
      default: false
    },
    characterId: {
      type: Number,
      default: null,
    },
    modal: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      characterAvatars: [],
      avatarActiveIndex: -1,
      characterAvatar: {},
    }
  },
  methods: {
    handleClose() {
      this.avatarActiveIndex = -1;
      this.$emit('close');
    },
    chooseCharacterAvatar(avatar, index) {
      this.avatarActiveIndex  = index;
      this.characterAvatar = avatar;
    },
    handleOk() {
      this.avatarActiveIndex = -1;
      this.$emit('confirm', this.characterAvatar)
    },
    queryCharacterAvatar() {
      this.$http({
        url: `/characterAvatar/list/${this.characterId}`,
        method: 'get'
      }).then((data) => {
        this.characterAvatars = data;
      });
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.queryCharacterAvatar();
      }
    }
  },
}

</script>

<style scoped lang="scss">
.character-avatar-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 10px;
}
</style>