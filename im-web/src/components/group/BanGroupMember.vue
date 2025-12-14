<template>
  <el-dialog :title="title" :visible.sync="visible"  width="42%" :before-close="onClose">
    <div class="container">
      <div class="left-box">
        <el-input width="200px" placeholder="搜索" v-model="searchText">
          <i class="el-icon-search el-input__icon" slot="prefix"></i>
        </el-input>
        <el-scrollbar style="height:400px;">
          <div v-for="member in members" :key="member.id" v-show="member.aliasName.startsWith(searchText)">
            <div class="member-item" @click="onCheck(member)">
              <head-image :url="member.headImage" :name="member.aliasName" :online="member.online" :size="38"></head-image>
              <span class="member-name">{{ member.aliasName }}</span>
              <div v-if="member.isBanned" class="banned-tag">禁言中</div>
              <el-checkbox :disabled="operation === 'mute' && member.isBanned" v-model="member.isCheck" size="medium"></el-checkbox>
            </div>
          </div>
        </el-scrollbar>
      </div>
      <div class="arrow el-icon-d-arrow-right"></div>
      <div class="right-box">
        <div class="select-tip">已勾选{{checkCount}}位成员</div>
        <el-scrollbar style="height:400px;">
          <div v-for="member in selectedMembers" :key="member.id">
            <div class="member-item" @click="onRemove(member)">
              <head-image :url="member.headImage" :name="member.aliasName" :online="member.online" :size="38"></head-image>
              <span class="member-name">{{ member.aliasName }}</span>
              <el-checkbox v-model="member.isCheck" size="medium"></el-checkbox>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="onClose()" size="small">取 消</el-button>
      <el-button type="primary" @click="onOk()" size="small">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>

import HeadImage from "@components/common/HeadImage.vue";

export default {
  name: "BanGroupMember",
  components: {
    HeadImage,
  },
  props: {
    visible: {
      type: Boolean
    },
    members: {
      type: Array
    },
    operation: {
      type: String
    },
  },
  data() {
    return {
      searchText: "",
    }
  },
  computed: {
    checkCount() {
      return this.selectedMembers.length;
    },
    selectedMembers() {
      return this.members.filter(member => member.isCheck);
    },
    title() {
      return this.operation === "mute" ? "禁言" : "解除禁言";
    }
  },
  methods: {
    onClose() {
      this.$emit("close");
    },
    onOk() {
      let userIds = this.members.filter(member => member.isCheck).map(member => member.userId);
      this.$emit("confirm", userIds);
    },
    onCheck(member) {
      if (this.operation === "mute" && member.isBanned) {
        return
      }
      this.$set(member, 'isCheck', !member.isCheck);
    },
    onRemove(member) {
      // 使用 $set 确保响应式更新
      this.$set(member, 'isCheck', false);
    }
  },
}
</script>


<style scoped lang="scss">
.container {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .left-box, .right-box {
    flex: 1;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    padding: 15px;
    background-color: #f8f9fa;

    .el-input {
      margin-bottom: 15px;
    }

    .el-scrollbar {
      height: 400px;

      &::v-deep .el-scrollbar__wrap {
        overflow-x: hidden;
      }

      &::v-deep .el-scrollbar__bar.is-horizontal {
        display: none;
      }
    }

    .member-item {
      padding: 8px 10px;
      margin-bottom: 8px;
      border-radius: 6px;
      transition: all 0.3s;
      cursor: pointer;
      display: flex;
      align-items: center;
      position: relative;

      &:hover {
        background-color: #e3f2fd;
      }

      .head-image {
        margin-right: 10px;
        flex-shrink: 0;
      }
      
      .member-name {
        flex-grow: 1;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      /* 恢复 el-checkbox 的显示 */
      .el-checkbox {
        position: absolute;
        right: 10px;
        top: 50%;
        transform: translateY(-50%);
        flex-shrink: 0;
      }

      .banned-tag {
        position: absolute;
        right: 40px;
        top: 50%;
        transform: translateY(-50%);
        background-color: #ffebee;
        color: #f44336;
        padding: 2px 8px;
        border-radius: 10px;
        font-size: 12px;
        flex-shrink: 0;
      }
    }
  }

  .arrow {
    font-size: 24px;
    color: #909399;
    margin: 0 15px;
    align-self: center;
  }

  .right-box {
    .select-tip {
      padding: 10px 0;
      font-weight: bold;
      color: #409eff;
      border-bottom: 1px solid #ebeef5;
      margin-bottom: 10px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;

  .el-button {
    margin-left: 20px;
  }
}
</style>