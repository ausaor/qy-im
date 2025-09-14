<template>
  <el-drawer
      title=""
      :visible.sync="drawerVisible"
      direction="rtl"
      size="50%"
      :with-header="false"
      :show-close="false"
      class="music-drawer"
      @closed="handleClose"
  >
    <div class="request-box">
      <el-tabs type="card">
        <el-tab-pane v-if="isOwner" :label="'申请中的(' + joinGroupRequests.length + ')'">
          <div class="request-item" v-for="(item, idx) in joinGroupRequests" :key="idx">
            <div class="group-info">
              <div class="group-avatar">
                <head-image :size="45" :id="item.userId" :name="item.userNickname" :url="item.userHeadImage"></head-image>
              </div>
              <div class="request-info">
                <div class="nick-name">
                  <div>{{item.userNickname}}</div>
                </div>
                <div class="info-text"><div>{{item.remark}}</div></div>
              </div>
              <div class="character-info" v-if="item.templateCharacterName">
                <div class="character-avatar">
                  <head-image :size="30" :name="item.templateCharacterName" :url="item.templateCharacterAvatar"></head-image>
                </div>
                <div class="character-name">
                  {{item.templateCharacterName}}
                </div>
              </div>
              <div class="btn-group">
                <el-button type="warning" size="mini" @click="modifyGroupRequest(item.id)">修改</el-button>
                <el-button type="danger" size="mini" @click="rejectGroupRequest(item.id)">拒绝</el-button>
                <el-button type="primary" size="mini" @click="approveGroupRequest(item.id)">同意</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="'邀请中的(' + inviteGroupRequests.length + ')'">
          <div class="request-item" v-for="(item, idx) in inviteGroupRequests" :key="idx">
            <div class="group-info">
              <div class="group-avatar">
                <head-image :size="45" :id="item.userId" :name="item.userNickname" :url="item.userHeadImage"></head-image>
              </div>
              <div class="request-info">
                <div class="nick-name">
                  <div>{{item.userNickname}}</div>
                </div>
                <div class="info-text"><div>{{item.remark}}</div></div>
              </div>
              <div class="character-info" v-if="item.templateCharacterName">
                <div class="character-name">角色：</div>
                <div class="character-avatar">
                  <head-image :size="30" :name="item.templateCharacterName" :url="item.templateCharacterAvatar"></head-image>
                </div>
                <div class="character-name">
                  {{item.templateCharacterName}}
                </div>
              </div>
              <div class="launch-user-info">
                <div>邀请人：</div>
                <div class="launch-user-avatar">
                  <head-image :size="30" :id="item.launchUserId" :name="item.launchUserNickname" :url="item.launchUserHeadImage"></head-image>
                </div>
                <div class="launch-user-name">{{item.launchUserNickname}}</div>
              </div>
              <div class="btn-group">
                <el-button type="warning" size="mini" @click="modifyGroupRequest(item.id)">修改</el-button>
                <el-button type="danger" size="mini" @click="recallGroupRequest(item.id)">撤回</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-drawer>
</template>

<script>
import HeadImage from "@components/common/HeadImage.vue";

export default {
  name: 'GroupRequestPanel',
  components: {HeadImage},
  props: {
    isOwner: {
      type: Boolean,
      default: false
    },
    joinGroupRequests: {
      type: Array,
      default: () => []
    },
    inviteGroupRequests: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      drawerVisible: false,
    }
  },
  methods: {
    show() {
      this.drawerVisible = true
    },
    handleClose() {
      this.$emit('closed');
    },
    modifyGroupRequest() {

    },
    rejectGroupRequest(id) {
      this.$http({
        url: `/group/request/reject?id=${id}`,
        method: "post",
      }).then(() => {

      })
    },
    approveGroupRequest(id) {
      this.$http({
        url: `/group/request/approve?id=${id}`,
        method: "post",
      }).then(() => {

      })
    },
    recallGroupRequest(id) {
      this.$http({
        url: `/group/request/recall?id=${id}`,
        method: "post",
      }).then(() => {

      })
    }
  }
}
</script>

<style scoped>
.request-box {
  flex: 1;

  .group-info {
    display: flex;
    position: relative;
    align-items: center;
    cursor: pointer;
    margin: 0 10px;
    padding: 10px;
    border-bottom: 1px solid #ccc;

    .request-info {
      flex: 1;
      margin-left: 15px;
      display: flex;
      flex-direction: column;
      overflow: hidden;

      .nick-name {
        display: flex;
        align-items: center;
        font-weight: 600;
        font-size: 16px;
      }

      .info-text {
        display: flex;
        word-break: break-all;
        font-size: 14px;
        line-height: 20px;
        text-align: left;
      }
    }

    .character-info {
      flex: 1;
      display: flex;
      align-items: center;

      .character-name {
        margin-left: 10px;
      }
    }

    .launch-user-info {
      flex: 1;
      display: flex;
      align-items: center;
    }

    .btn-group {
      text-align: left !important;
      display: flex;
    }
  }
}
</style>