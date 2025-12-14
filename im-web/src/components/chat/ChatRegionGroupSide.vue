<template>
  <div class="chat-group-side">
    <div class="group-side-search">
      <el-input placeholder="搜索群成员" v-model="searchText" size="small">
      </el-input>
      <el-button class="refresh-btn" icon="el-icon-refresh" @click="loadRegionGroupMembers(regionGroup.id)"></el-button>
    </div>
    <!-- 群成员卡片 -->
    <div class="card-container members-card">
      <div class="card-header">群成员</div>
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
    </div>
    
    <!-- 地区空间卡片 -->
    <div class="card-container feature-card" @click="openRegionSpace">
      <div class="feature-card-content group-space">
        <svg class="icon svg-icon" aria-hidden="true">
          <use xlink:href="#icon-shejiaotubiao-40"></use>
        </svg>
        <span class="feature-title">地区空间</span>
        <div class="new-talk-info">
          <div v-show="unreadTalkCount" class="new-talk-text">{{unreadTalkCount}}条新动态</div>
          <div v-show="talkList.length" class="new-talk-list">
            <head-image v-for="(talk, index) in talkList" :key="index" :url="talk.avatar" :name="talk.nickName" :size="24"></head-image>
          </div>
        </div>
        <div v-show="unreadNotifyCount>0" class="unread-text">{{unreadNotifyCount}}</div>
      </div>
    </div>
    
    <!-- 地区歌单卡片 -->
    <div class="card-container feature-card" @click="openRegionMusic">
      <div class="feature-card-content group-music">
        <svg class="icon svg-icon" aria-hidden="true">
          <use xlink:href="#icon-Music"></use>
        </svg>
        <span class="feature-title">地区歌单</span>
      </div>
    </div>
    
    <!-- 操作功能卡片 -->
    <div class="card-container operation-card">
      <div class="card-header">群操作</div>
      <div class="operation-box">
      <div class="operation-item group-space" @click="openRegionSpace">
        <svg class="icon svg-icon" aria-hidden="true">
          <use xlink:href="#icon-shejiaotubiao-40"></use>
        </svg>
        <span style="color: orange;margin-left: 10px;font-size: 16px;">地区空间</span>
        <div class="new-talk-info">
          <div v-show="unreadTalkCount" class="new-talk-text">{{unreadTalkCount}}条新动态</div>
          <div v-show="talkList.length" class="new-talk-list">
            <head-image v-for="(talk, index) in talkList" :key="index" :url="talk.avatar" :name="talk.nickName" :size="24"></head-image>
          </div>
        </div>
        <div v-show="unreadNotifyCount>0" class="unread-text">{{unreadNotifyCount}}</div>
      </div>
      <div class="operation-item group-music" @click="openRegionMusic">
        <svg class="icon svg-icon" aria-hidden="true">
          <use xlink:href="#icon-Music"></use>
        </svg>
        <span style="color: orange;margin-left: 10px;font-size: 16px;">地区歌单</span>
      </div>
      <div class="operation-item" v-if="myGroupMemberInfo.isOwner">
        <div class="leader-transfer">
          <el-button type="text" @click="leaderTransfer">群主转移</el-button>
        </div>
      </div>
      <div class="operation-item member-info">
        <div class="label-text">备注：</div>
        <el-input
            size="mini"
            placeholder="群昵称"
            minlength="1"
            maxlength="10"
            style="width: 40%"
            v-model="myGroupMemberInfo.aliasName"/>
        <file-upload class="avatar-uploader" :action="imageAction"
                     :showLoading="true" :maxSize="maxSize" @success="onUploadMemberAvatarSuccess"
                     :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']">
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
        <div class="label-text">投票通知：</div>
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
      <div class="operation-item all-banned" v-if="myGroupMemberInfo.isOwner">
        <div class="label-text">全员禁言：</div>
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
      <div class="operation-item banned-time" v-if="myGroupMemberInfo.isOwner">
        <div class="label-text">禁言时长(小时)：</div>
        <input 
          type="number" 
          size="mini" 
          v-model="bannedTime" 
          min="1" 
          max="720" 
          class="banned-time-input"
        />
      </div>
      <div class="operation-item" v-if="myGroupMemberInfo.isOwner">
        <div class="user-banned">
          <el-button type="text" @click="doBanned('banned')">成员禁言</el-button>
        </div>
        <div class="user-banned">
          <el-button type="text" @click="doBanned('unBanned')">解除禁言</el-button>
        </div>
      </div>
    </div>
  </div>
  <drawer
        :visible="regionSpaceVisible"
        @close="closeDrawer"
        :width=60>
      <template v-slot:header>
        <space-cover :name="'地区空间'" @refresh="refreshTalkList" @add="handleShowAddTalk" @showTalkNotify="showTalkNotify" :notify-count="unreadNotifyCount"></space-cover>
      </template>
      <template v-slot:main>
        <talk-list ref="talkListRef" :category="'region'" :section="'my-region'" :region-group-id="regionGroup.id" :region-code="regionGroup.code"
                   :new-talk-list="talkList" :new-talk-count="unreadTalkCount"></talk-list>
      </template>
    </drawer>
    <talk-notify ref="talkNotifyRef" :category="'region'" :region-code="regionGroup.code"></talk-notify>
    <music-play ref="musicPlayRef" :category="'region'" :section="'region'" :region-code="regionGroup.code"></music-play>
  </div>
</template>

<script>
import RegionGroupMemberItem from "@/components/regionGroup/RegionGroupMemberItem";
import TalkList from "@/components/talk/TalkList";
import SpaceCover from "@/components/common/SpaceCover";
import Drawer from "@/components/common/Drawer";
import FileUpload from "@/components/common/FileUpload";
import HeadImage from "@components/common/HeadImage.vue";
import TalkNotify from "@components/talk/TalkNotify.vue";
import MusicPlay from "@components/common/musicPlay.vue";

export default {
  name: "ChatRegionGroupSide",
  components: {
    TalkNotify,
    HeadImage,
    RegionGroupMemberItem,
    TalkList,
    SpaceCover,
    Drawer,
    FileUpload,
    MusicPlay,
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
      if (this.curMember == null || this.curMember.joinType === 0 || !this.curMember.isOwner) {
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
      if (!this.myGroupMemberInfo.isOwner) {
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
      if (!this.myGroupMemberInfo.isOwner) {
        this.$message.warning("您不是当前地区群聊群主");
        return;
      }
      if (!this.curMember) {
        this.$message.warning("请先选择群聊成员");
        return;
      }
      if (this.curMember.userId === this.mine.id) {
        this.$message.warning("不能对自己进行操作");
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
      this.$store.commit("resetRegionTalk", this.regionGroup.code);
      this.$refs.talkListRef.refreshTalkList();
    },
    openRegionMusic() {
      this.$refs.musicPlayRef.show();
    },
    closeDrawer() {
      this.regionSpaceVisible = false;
    },
    handleShowAddTalk() {
      this.$refs.talkListRef.handleShowAddTalk();
    },
    refreshTalkList() {
      this.$store.commit("resetRegionTalk", this.regionGroup.code);
      this.$store.commit("resetRegionNotify", this.regionGroup.code);
      this.$refs.talkListRef.refreshTalkList();
    },
    showTalkNotify() {
      this.$refs.talkNotifyRef.show();
      this.$store.commit("resetRegionNotify", this.regionGroup.code);
      if (this.unreadNotifyCount > 0) {
        this.readedTalkNotify();
      }
    },
    readedTalkNotify() {
      let params = {
        category: 'region',
        regionCode: this.regionGroup.code
      };
      this.$http({
        url: `/talk-notify/readed`,
        method: 'post',
        data: params
      }).then(() => {})
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
    talkList() {
      let talkMap =this.$store.state.talkStore.regionTalks;
      let talks = talkMap.get(this.regionGroup.code)
      if (talks && talks.length > 2) {
        return talks.slice(0, 2);
      }
      return talks ? talks : [];
    },
    unreadTalkCount() {
      let talkMap =this.$store.state.talkStore.regionTalks;
      let talks = talkMap.get(this.regionGroup.code);
      if (talks) {
        return talks.length;
      }
      return 0;
    },
    unreadNotifyCount() {
      let notifyMap =this.$store.state.talkStore.regionNotify;
      let count = notifyMap.get(this.regionGroup.code);
      if (count) {
        return count;
      }
      return 0;
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

<style scoped lang="scss">
.chat-group-side {
  border-left: 1px solid #cccccc;
  background-color: #f5f7fa;
  height: 100vh;
  box-sizing: border-box;
  padding: 10px;
  display: flex;
  flex-direction: column;

  .group-side-search {
    display: flex;
    margin-bottom: 15px;

    .refresh-btn {
      padding: 5px !important;
      margin: 5px;
      font-size: 14px;
      color: #54CC36;
      border: #54CC36 1px solid;
      background-color: #F0F8FF;
      border-radius: 50%;
    }
  }

  // 卡片容器样式
  .card-container {
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    padding: 15px;
    margin-bottom: 20px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }

    .card-header {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 15px;
      padding-bottom: 10px;
      border-bottom: 1px solid #ebeef5;
    }
  }
  
  // 新增的功能卡片样式
  .feature-card {
    cursor: pointer;
    padding: 0;
    
    .feature-card-content {
      padding: 15px;
      display: flex;
      align-items: center;
      position: relative;
      
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
      
      .feature-title {
        color: orange;
        margin-left: 10px;
        font-size: 16px;
        font-weight: 500;
      }
      
      .new-talk-info {
        position: absolute;
        top: 50%;
        right: 10px;
        transform: translateY(-50%);
        display: flex;
        align-items: center;

        .new-talk-text {
          font-size: 12px;
          color: red;
        }

        .new-talk-list {
          display: flex;
          align-items: center;
        }
      }

      .unread-text {
        position: absolute;
        line-height: 16px;
        background-color: #f56c6c;
        left: 30px;
        top: 0;
        color: white;
        border-radius: 16px;
        padding: 0 5px;
        font-size: 10px;
        text-align: center;
        white-space: nowrap;
        border: 1px solid #f1e5e5;
      }
    }
    
    &:hover {
      background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    }
  }

  .members-card {
    display: flex;
    flex: 1;
    flex-direction: column;
    max-height: 400px;
    
    .group-side-scrollbar {
      overflow-y: auto;
      flex: 1;
      
      .region-group-member-item {
        margin-bottom: 8px;
        padding: 8px;
        border-radius: 8px;
        transition: background-color 0.3s;
        
        &:hover {
          background-color: #f0f2f5;
        }
      }
    }
  }

  .operation-box {
    .operation-item {
      display: flex;
      width: 100%;
      margin: 10px 0;
      padding: 12px 15px;
      border-radius: 8px;
      transition: all 0.3s ease;
      min-height: 40px;
      align-items: center;
      
      &:hover {
        background-color: #f0f2f5;
        transform: translateX(5px);
      }
      
      &:first-child {
        margin-top: 0;
      }
      
      &:last-child {
        margin-bottom: 0;
      }

      .label-text {
        font-size: 14px;
      }
    }

    .group-space {
      justify-content: left;
      align-items: center;
      cursor: pointer;
      position: relative;

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

      .new-talk-info {
        position: absolute;
        top: 50%;
        right: 10px;
        transform: translateY(-50%);
        display: flex;
        align-items: center;

        .new-talk-text {
          font-size: 12px;
          color: red;
        }

        .new-talk-list {
          display: flex;
          align-items: center;
        }
      }

      .unread-text {
        position: absolute;
        line-height: 16px;
        background-color: #f56c6c;
        left: 30px;
        top: 0;
        color: white;
        border-radius: 16px;
        padding: 0 5px;
        font-size: 10px;
        text-align: center;
        white-space: nowrap;
        border: 1px solid #f1e5e5;
      }
    }

    .group-music {
      justify-content: left;
      align-items: center;
      cursor: pointer;
      position: relative;

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
      min-height: 36px;
      align-items: center;

      .label-text {
        margin-right: 10px;
        font-weight: 500;
        color: #606266;
        white-space: nowrap; // 防止文字换行
      }

      .avatar-uploader {
        width: 28px;
        height: 28px;
        margin-left: 10px;
        margin-right: 20px;

        .member-avatar {
          width: 28px;
          height: 28px;
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

      .label-text {
        white-space: nowrap;
        flex-shrink: 0;
        margin-right: 10px;
        font-weight: 500;
        color: #606266;
      }
      
      .banned-time-input {
        width: 100px;
        padding: 5px 10px;
        border: 1px solid #DCDFE6;
        border-radius: 4px;
        font-size: 12px;
        text-align: center;
        color: #606266;
        background-color: #fff;
        outline: none;
        transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
        
        &:focus {
          border-color: #409EFF;
        }
        
        &::-webkit-outer-spin-button,
        &::-webkit-inner-spin-button {
          -webkit-appearance: none;
          margin: 0;
        }
        
        &[type=number] {
          -moz-appearance: textfield;
        }
      }
      
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