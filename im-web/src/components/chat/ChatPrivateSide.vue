<template>
  <div class="chat-private-side">
    <div class="friend-info-section">
      <div class="friend-card">
        <div class="friend-avatar">
          <head-image :url="friend.headImage" :name="friend.nickName" :size="80" :online="friend.online"></head-image>
        </div>
        <div class="friend-details">
          <div class="friend-name-row">
            <div class="friend-name">{{friend.nickName}}</div>
            <div class="gender-icons">
              <i v-if="friend.sex === 0" class="el-icon-male gender-icon male"/>
              <i v-if="friend.sex === 1" class="el-icon-female gender-icon female"/>
            </div>
          </div>
          <div class="friend-id-row">
            <div class="friend-id">ID: {{friend.userName}}</div>
          </div>
        </div>
      </div>
      <div class="personal-setting">
        <div class="switch-item">
          <div class="label">消息免打扰</div>
          <div class="switch-container">
            <div
              class="custom-switch"
              :class="{ 'switch-active': friend.isDnd, 'switch-inactive': !friend.isDnd }"
              @click="toggleFriendMsgDnd(!friend.isDnd)">
            </div>
          </div>
        </div>
      </div>
      <div class="friend-info-section-card">
        <div class="section-title">好友信息</div>
        <div class="form-item">
          <label class="form-label">备注</label>
          <div
            class="form-value"
            :class="{ 'editing': editingField === 'remark' }"
            @click="editField('remark')"
            v-if="editingField !== 'remark'">
            {{friend.friendRemark || '点击添加备注'}}
          </div>
          <input
            v-else
            v-model="friend.friendRemark"
            class="form-input"
            placeholder="好友备注仅自己可见"
            maxlength="20"
            @blur="saveAndExitEdit"
            @keyup.enter="saveAndExitEdit">
        </div>
      </div>
      <div class="btn-group" @click="deleteFriend">
        <span class="danger-text">删除好友</span>
      </div>
      <div class="complaint-tip" @click="complaintVisible = true">
        <span>被骚扰了？投诉该用户</span>
      </div>
    </div>
    <complaint :target-type="'user'" :target-name="friend.nickName" :target-id="friend.id" :visible="complaintVisible" @close="closeComplaint"></complaint>
  </div>
</template>

<script>

import HeadImage from "@components/common/HeadImage.vue";
import Complaint from "@components/common/Complaint.vue";

export default {
  name: "chatPrivateSide",
  components: {Complaint, HeadImage},
  data() {
    return {
      complaintVisible: false,
      editingField: '',
    }
  },
  props: {
    friend: {
      type: Object
    },
  },
  methods: {
    toggleFriendMsgDnd(value) {
      this.friend.isDnd = value;
      this.updateFriendInfo();
    },
    updateFriendInfo() {
      let friend = this.friend;
      this.$http({
        url: "/friend/update",
        method: "put",
        data: friend
      }).then(() => {
        this.$message.success('操作成功');
        this.$store.commit("updateFriend", friend);
        this.$store.commit("updateChatFromFriend", friend);
      })
    },
    deleteFriend() {
      this.$confirm(`确认删除'${this.friend.nickName}',并清空聊天记录吗?`, '确认解除?', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: `/friend/delete/${this.friend.id}`,
          method: 'delete'
        }).then((data) => {
          this.$message.success("删除好友成功");
          this.$store.commit("removeFriend", this.friend.id);
          this.$store.commit("removePrivateChat", this.friend.id);
        })
      })
    },
    closeComplaint() {
      this.complaintVisible = false;
    },
    editField(fieldName) {
      this.editingField = fieldName;
    },
    saveAndExitEdit() {
      this.editingField = '';
      this.updateFriendInfo();
    },
  },
}
</script>

<style scoped lang="scss">
.chat-private-side {
  padding: 10px;
  position: relative;
  height: 100%;
  overflow-y: auto;
  box-sizing: border-box;

  .friend-info-section {

    .friend-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 20px;
      margin-bottom: 12px;
      background: linear-gradient(120deg, #f5f7fa, #e4edf9);
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      }

      .friend-avatar {
        flex-shrink: 0;
      }

      .friend-details {
        flex: 1;

        .friend-name-row {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;

          .friend-name {
            margin: 0;
            font-size: 18px;
            font-weight: 600;
            color: #000000;
          }

          .gender-icons {
            .gender-icon {
              font-size: 16px;
              border-radius: 50%;
              padding: 2px;
              transition: all .3s ease;
              margin-left: 3px;
            }

            .gender-icon.male {
              color: #1890ff;
              background: rgba(24, 144, 255, .1);
            }

            .gender-icon.female {
              color: #f5222d;
              background: rgba(245, 34, 45, .1);
            }
          }
        }

        .friend-id-row {
          display: flex;
          align-items: center;
          gap: 8px;

          .friend-id {
            margin: 0;
            font-size: 14px;
            color: grey;
            text-align: left;
          }
        }
      }
    }

    .personal-setting {
      background: linear-gradient(120deg, #f5f7fa, #e4edf9);
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      margin-bottom: 12px;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      }

      .switch-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 15px;

        .label {
          font-size: 14px;
          color: #606266;
          font-weight: 500;
        }

        .switch-container {
          .custom-switch {
            position: relative;
            display: inline-block;
            width: 40px;
            height: 20px;
            border-radius: 10px;
            cursor: pointer;
            transition: background-color 0.3s;

            &.switch-active {
              background-color: #13ce66;
            }

            &.switch-inactive {
              background-color: #ccc;
            }

            &::after {
              content: '';
              position: absolute;
              width: 18px;
              height: 18px;
              border-radius: 50%;
              background: white;
              top: 1px;
              transition: transform 0.3s;
              box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
            }

            &.switch-active::after {
              transform: translateX(20px);
            }
          }
        }
      }
    }

    .friend-info-section-card {
      background: linear-gradient(120deg, #f5f7fa, #e4edf9);
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      margin-bottom: 12px;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      }

      .section-title {
        padding: 15px;
        border-bottom: 1px solid #eee;
        font-size: 14px;
        color: #606266;
        font-weight: 500;
      }

      .form-item {
        display: flex;
        align-items: center;
        padding: 15px;

        .form-label {
          width: 60px;
          line-height: 30px;
          color: #606266;
          font-weight: 500;
          font-size: 14px;
        }

        .form-value {
          flex: 1;
          min-height: 30px;
          line-height: 30px;
          padding: 0 10px;
          border-radius: 6px;
          background: rgba(255, 255, 255, 0.7);
          transition: all 0.3s ease;
          cursor: pointer;
          color: #909399;
          font-size: 14px;

          &:hover {
            background: rgba(255, 255, 255, 0.9);
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
          }

          &.editing {
            border: 1px solid #409eff;
            border-radius: 4px;
            background: white;
            box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
          }
        }

        .form-input {
          flex: 1;
          height: 30px;
          max-width: 180px;
          line-height: 30px;
          padding: 0 10px;
          border: 1px solid #409eff;
          border-radius: 4px;
          background: white;
          box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
          outline: none;
        }
      }
    }

    .btn-group {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 15px;
      margin-bottom: 12px;
      background: linear-gradient(120deg, #f5f7fa, #e4edf9);
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      transition: all 0.3s ease;
      cursor: pointer;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      }

      .danger-text {
        color: #f56c6c;
        font-weight: bold;
        font-size: 14px;
      }
    }

    .complaint-tip {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 8px 12px;
      font-size: 13px;
      color: #5359dc;
      cursor: pointer;
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        background: rgba(83, 89, 220, 0.06);
        color: #3a3fd4;
      }
    }
  }
}
</style>