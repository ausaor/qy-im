<template>
  <div class="chat-group-side">
    <div class="group-side-search">
      <el-input placeholder="搜索群成员" v-model="searchText">
      </el-input>
      <el-button class="refresh-btn" icon="el-icon-refresh" @click="loadRegionGroupMembers(regionGroup.id)"></el-button>
    </div>
    <el-scrollbar class="group-side-scrollbar">
      <div class="region-group-member-item" v-for="(m,index) in groupMembers" :key="''+m.userId+m.joinType">
        <region-group-member-item
            v-show="!m.quit&&m.aliasName.includes(searchText)"
            :member="m"
            >
          <el-button :type="activeIndex === index ? 'success' : ''" icon="el-icon-check" circle
                      size="mini" @click="chooseMember(m, index)"></el-button>
        </region-group-member-item>
      </div>
    </el-scrollbar>
    <div class="operation-box">
      <div class="operation-item group-space" @click="openRegionSpace">
        <svg class="icon svg-icon" aria-hidden="true">
          <use xlink:href="#icon-shejiaotubiao-40"></use>
        </svg>
        <span style="color: orange;margin-left: 10px;font-size: 16px;">地区空间</span>
      </div>
      <div class="operation-item" v-if="myGroupMemberInfo.isLeader">
        <div class="leader-transfer">
          <el-button type="text" @click="leaderTransfer">群主转移</el-button>
        </div>
      </div>
      <div class="operation-item member-info">
        <label>备注：</label>
        <el-input
            size="small"
            placeholder="群昵称"
            minlength="1"
            maxlength="10"
            style="width: 40%"
            v-model="myGroupMemberInfo.aliasName"/>
        <file-upload class="avatar-uploader" :action="imageAction"
                     :showLoading="true" :maxSize="maxSize" @success="onUploadMemberAvatarSuccess"
                     :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp']">
            <img v-if="myGroupMemberInfo.headImage" :src="myGroupMemberInfo.headImage" class="member-avatar">
            <i v-else class="el-icon-upload"></i>
        </file-upload>
        <el-button type="text" @click="modifyMemberInfo">提交</el-button>
      </div>
      <div class="operation-item leader-vote">
        <div class="vote-btn"><el-button type="text" @click="voteLeader">群主投票</el-button></div>
        <div class="vote-btn"><el-button type="text" @click="voteRemoveLeader">群主解除投票</el-button></div>
        <div class="tips">
          <el-popover
              placement="top-start"
              width="200"
              trigger="hover"
              content="【群主投票】与【群主解除投票】须在24小时内完成才可生效">
            <div class="el-icon-question" slot="reference"></div>
          </el-popover>
        </div>
      </div>
      <div class="operation-item vote-notice">
        <label>投票通知：</label>
        <el-switch
            style="display: block"
            v-model="announce"
            active-color="#13ce66"
            inactive-color="#ff4949"
            active-text="通知"
            inactive-text="关闭"
            @change="announceChange">
        </el-switch>
      </div>
      <div class="operation-item all-banned" v-if="myGroupMemberInfo.isLeader">
        <label>全员禁言：</label>
        <el-switch
            style="display: block"
            v-model="regionGroup.isBanned"
            active-color="#13ce66"
            inactive-color="#ff4949"
            active-text="开启"
            inactive-text="关闭"
            @change="doAllBanned">
        </el-switch>
      </div>
      <div class="operation-item banned-time" v-if="myGroupMemberInfo.isLeader">
        <label>禁言时长(小时)：</label>
        <el-input-number size="mini" v-model="bannedTime" :min="1" :max="720"></el-input-number>
        <el-button type="text" @click="doBanned">确认</el-button>
      </div>
      <div class="operation-item" v-if="myGroupMemberInfo.isLeader">
        <div class="user-banned">
          <el-button type="text" @click="doBanned('banned')">用户禁言</el-button>
        </div>
        <div class="user-banned">
          <el-button type="text" @click="doBanned('unBanned')">解除禁言</el-button>
        </div>
      </div>
    </div>
    <drawer
        :visible="regionSpaceVisible"
        @close="closeDrawer"
        :width=60>
      <template v-slot:header>
        <space-cover :name="'地区空间'" @refresh="refreshTalkList" @add="handleShowAddTalk"></space-cover>
      </template>
      <template v-slot:main>
        <talk-list ref="talkListRef" :category="'region'" :section="'my-region'" :region-group-id="regionGroup.id" :region-code="regionGroup.code"></talk-list>
      </template>
    </drawer>
  </div>
</template>

<script>
import RegionGroupMemberItem from "@/components/regionGroup/RegionGroupMemberItem";
import TalkList from "@/components/talk/TalkList";
import SpaceCover from "@/components/common/SpaceCover";
import Drawer from "@/components/common/Drawer";
import FileUpload from "@/components/common/FileUpload";

export default {
  name: "ChatRegionGroupSide",
  components: {
    RegionGroupMemberItem,
    TalkList,
    SpaceCover,
    Drawer,
    FileUpload,
  },
  props: {
    regionGroup: {
      type: Object
    },
    /*myGroupMemberInfo: {
      type: Object
    },*/
    /*groupMembers: {
      type: Array
    },
    friends: {
      type: Array
    },
    */
  },
  data() {
    return {
      searchText: "",
      activeIndex: -1,
      curMember: null,
      userIds: [],
      groupMembers: [],
      announce: false,
      allBanned: false, // 全员禁言
      bannedTime: 1,
      friends: [],
      myGroupMemberInfo: {},
      regionSpaceVisible: false,
      maxSize: 2 * 1024 * 1024,
    }
  },
  created() {
    /*this.friends = this.$store.state.friendStore.friends;
    this.loadRegionGroupMembers(this.regionGroup.id);*/
  },
  mounted() {
  },
  methods: {
    chooseMember(member, index) {
      if (this.activeIndex ===  index) {
        this.activeIndex = -1;
        this.curMember = null;
      } else {
        this.activeIndex = index;
        this.curMember = member;
      }
    },
    loadRegionGroupMembers(groupId) {
      this.$http({
        url: `/region/group/members/${groupId}`,
        method: 'get'
      }).then((groupMembers) => {
        this.myGroupMemberInfo = groupMembers.find((m) => m.userId === this.mine.id && !m.quit);
        groupMembers.forEach((item, index) => {
          let friend = this.friends.find((f) => f.id === item.userId);
          if (friend && friend.friendRemark) {
            item.friendRemark = friend.friendRemark;
          }
        })
        this.groupMembers = groupMembers;
      });
    },
    voteLeader() {
      if (this.myGroupMemberInfo.joinType === 0) {
        this.$message.warning("您不是当前地区群聊常驻用户");
        return;
      }
      if (this.curMember == null || this.curMember.joinType === 0) {
        this.$message.warning("请先选择一位常驻成员");
        return;
      }
      let paramVO = {
        regionGroupId: this.regionGroup.id,
        userId: this.curMember.userId,
        joinType: this.curMember.joinType,
        announce: this.announce,
      }
      this.$confirm(`【群主投票】请确认是否投票给群成员【${this.curMember.aliasName}】？`, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        console.log("paramVO", paramVO)

        this.$http({
          url: '/region/group/vote',
          method: 'post',
          data: paramVO
        }).then(() => {
          this.$message.success("操作成功");
        });
      })
    },
    voteRemoveLeader() {
      if (this.myGroupMemberInfo.joinType === 0) {
        this.$message.warning("您不是当前地区群聊常驻用户");
        return;
      }
      if (this.curMember == null || this.curMember.joinType === 0 || !this.curMember.isLeader) {
        this.$message.warning("请先选中群主");
        return;
      }
      let paramVO = {
        regionGroupId: this.regionGroup.id,
        userId: this.curMember.userId,
        joinType: this.curMember.joinType,
        announce: this.announce,
      }
      this.$confirm(`【群主解除投票】请确认是否投票解除群成员【${this.curMember.aliasName}】的群主身份？`, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        console.log("paramVO", paramVO)

        this.$http({
          url: '/region/group/voteRemove',
          method: 'post',
          data: paramVO
        }).then(() => {
          this.$message.success("操作成功");
        });
      })
    },
    announceChange(value) {
      this.announce = value;
    },
    doAllBanned(value) {
      if (!this.myGroupMemberInfo.isLeader) {
        this.$message.warning("您不是当前地区群聊群主");
        return;
      }
      let paramVO = {
        code: this.regionGroup.code,
        num: this.regionGroup.num,
        id: this.regionGroup.id,
        allBanned: value,
        banDuration: this.bannedTime,
        banType: 'master'
      }
      let url = "";
      if (value) {
        url = '/region/group/banMsg';
      } else {
        url = '/region/group/unBanMsg'
      }
      this.$http({
        url: url,
        method: 'post',
        data: paramVO
      }).then(() => {
        this.regionGroup.isBanned = value;
        this.$message.success("操作成功");
      }).catch((e) => {
        this.regionGroup.isBanned = !value;
      })
    },
    doBanned(type) {
      if (!this.myGroupMemberInfo.isLeader) {
        this.$message.warning("您不是当前地区群聊群主");
        return;
      }
      if (!this.curMember) {
        this.$message.warning("请先选择群聊成员");
        return;
      }
      let paramVO = {
        code: this.regionGroup.code,
        num: this.regionGroup.num,
        id: this.regionGroup.id,
        userId: this.curMember.userId,
        joinType: this.curMember.joinType,
        aliasName: this.curMember.aliasName,
        banDuration: this.bannedTime,
        allBanned: false,
        banType: 'master'
      }
      let url = "";
      if (type === 'banned') {
        url = "/region/group/banMsg";
      } else {
        url = "/region/group/unBanMsg";
      }
      this.$http({
        url: url,
        method: 'post',
        data: paramVO
      }).then(() => {
        this.loadRegionGroupMembers(this.regionGroup.id);
        this.$message.success("操作成功");
      }).catch((e) => {
      })
    },
    leaderTransfer() {
      if (this.curMember == null || this.curMember.joinType === 0) {
        this.$message.warning("请先选择一位常驻成员");
        return;
      }
      if (this.myGroupMemberInfo.userId === this.curMember.userId) {
        this.$message.warning('不能选择自己');
        return;
      }
      let paramVO = {
        regionGroupId: this.regionGroup.id,
        userId: this.curMember.userId,
        joinType: this.curMember.joinType,
      }
      this.$confirm(`【群主转移】请确认是否将群主转移给群成员【${this.curMember.aliasName}】？`, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: '/region/group/leaderTransfer',
          method: 'post',
          data: paramVO
        }).then(() => {
          this.$message.success("操作成功");
        });
      })
    },
    openRegionSpace() {
      this.regionSpaceVisible = true;
    },
    closeDrawer() {
      this.regionSpaceVisible = false;
    },
    handleShowAddTalk() {
      this.$refs.talkListRef.handleShowAddTalk();
    },
    refreshTalkList() {
      this.$refs.talkListRef.refreshTalkList();
    },
    onUploadMemberAvatarSuccess(data) {
      this.myGroupMemberInfo.headImage = data.originUrl;
    },
    modifyMemberInfo() {
      let param = {
        regionGroupId: this.regionGroup.id,
        joinType: this.myGroupMemberInfo.joinType,
        aliasName: this.myGroupMemberInfo.aliasName,
        headImage: this.myGroupMemberInfo.headImage
      }
      this.$http({
        url: '/region/group/modifyRegionGroupMember',
        method: 'post',
        data: param
      }).then(() => {
        this.$message.success("修改成功");
      });
    }
  },
  computed: {
    mine() {
      return this.$store.state.userStore.userInfo;
    },
    imageAction(){
      return `/image/upload`;
    },
  },
  watch: {
    regionGroup: {
      handler(newRegionGroup, oldRegionGroup) {
        this.friends = this.$store.state.friendStore.friends
        if (newRegionGroup.id > 0 && (!oldRegionGroup || newRegionGroup.id !== oldRegionGroup.id)) {
          this.loadRegionGroupMembers(newRegionGroup.id);
        }
      }
    }
  }
}
</script>

<style lang="scss">
.chat-group-side {
  border-left: 1px solid var(--line-color);
  background-color: var(--bg-color);
  height: 100%;
  box-sizing: content-box;
  padding-left: 2px;

  .group-side-search {
    display: flex;

    .refresh-btn {
      padding: 5px !important;
      margin: 5px;
      font-size: 20px;
      color: #54CC36;
      border: #54CC36 1px solid;
      background-color: #F0F8FF;
      border-radius: 50%;
    }
  }

  .group-side-scrollbar {
    .region-group-member-item {

    }
  }

  .agm-region-member-checkbox {

  }

  .operation-box {
    width: 100%;
    height: 40px;
    line-height: 40px;

    .operation-item {
      display: flex;
      width: 100%;
      margin: 2px auto;
      padding-left: 10px;
    }

    .group-space {
      justify-content: center;
      align-items: center;
      cursor: pointer;

      .icon {
        display: block;
        height: 45px;
        line-height: 45px;
        font-size: 28px;
        color: #333;
        -webkit-transition: font-size 0.25s linear, width 0.25s linear;
        -moz-transition: font-size 0.25s linear, width 0.25s linear;
        transition: font-size 0.25s linear, width 0.25s linear;
      }
    }

    .member-info {
      overflow: hidden;
      height: 32px;
      line-height: 32px;

      .avatar-uploader {
        width: 32px;
        height: 32px;
        line-height: 32px;
        margin-left: 10px;
        margin-right: 20px;

        .member-avatar {
          width: 32px;
          height: 32px;
          object-fit: cover;
        }
      }
    }

    .leader-vote {
      .vote-btn {
        display: flex;
        width: 40%;
        justify-content: left;
      }

      .tips {
        display: flex;
        margin-left: 10px;
        width: 20%;
        height: 40px;
        line-height: 40px;
        justify-content: left;
      }
    }

    .vote-notice {
      width: 100%;
      justify-content: left;
      align-items: center;
    }

    .all-banned {
      width: 100%;
      justify-content: left;
      align-items: center;
    }

    .banned-time {
      justify-content: left;
      align-items: center;

      .el-input-number--mini {
        width: 100px;
        margin-right: 10px;
      }
    }

    .user-banned {
      width: 50%;
      justify-content: center;
      align-items: center;
    }

  }
}
</style>