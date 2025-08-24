<template>
  <el-container class="friend-page">
    <el-aside width="14%" class="friend-list-box">
      <div class="friend-list-header">
        <el-input class="search-text" placeholder="搜索" v-model="searchText">
          <i class="el-icon-search el-input__icon" slot="prefix"> </i>
        </el-input>
        <el-button plain class="add-btn" icon="el-icon-plus" title="添加好友" @click="onShowAddFriend()"></el-button>
        <add-friend :dialogVisible="showAddFriend" @close="onCloseAddFriend"></add-friend>
      </div>
      <el-scrollbar class="friend-items">
        <div class="top-item" @click="showFriendRequest" :class="showType === 1 ? 'active' : ''">
          <div class="top-item-avatar">
            <head-image :size="45" :name="'新的朋友'" :url="require('@/assets/image/new_friend.png')"></head-image>
          </div>
          <div class="top-item-info">
            <div class="top-item-name">新的朋友</div>
          </div>
        </div>
        <div v-for="(friends, i) in friendValues" :key="i">
          <div class="letter">{{ friendKeys[i] }}</div>
          <div v-for="(friend) in friends" :key="friend.id">
            <friend-item :friend="friend" :active="friend.id === activeFriend.id"
                         @chat="onSendMessage(friend)" @delete="onDelFriend(friend)"
                         @click.native="onActiveItem(friend)">
            </friend-item>
          </div>
          <div v-if="i < friendValues.length - 1" class="divider"></div>
        </div>
      </el-scrollbar>
    </el-aside>
    <el-container class="container">
      <div class="header" v-show="showType !== 0">
        {{headerTitle}}
      </div>
      <div v-show="showType === 1">
        <div class="friend-request request-box">
          <el-tabs type="card">
            <el-tab-pane :label="'收到的申请('+ receivedFriendRequest.length +')'">
              <el-scrollbar class="request-list">
                <div class="request-item" v-for="(item, idx) in receivedFriendRequest" :key="idx">
                  <div class="friend-request-item">
                    <div class="friend-avatar"><head-image :size="45" :name="item.sendNickName" :url="item.sendHeadImage"></head-image></div>
                    <div class="request-info">
                      <div class="nick-name"><div>{{item.sendNickName}}</div></div>
                      <div class="info-text"><div>{{item.remark}}</div></div>
                    </div>
                    <div class="btn-group">
                      <el-button type="danger" size="mini" @click="rejectFriendRequest(item.id)">拒绝</el-button>
                      <el-button type="primary" size="mini" @click="approveFriendRequest(item.id)">同意</el-button>
                    </div>
                  </div>
                </div>
              </el-scrollbar>
            </el-tab-pane>
            <el-tab-pane :label="'发起的申请(' + launchFriendRequest.length + ')'">
              <div class="request-item" v-for="(item, idx) in launchFriendRequest" :key="idx">
                <div class="friend-request-item">
                  <div class="friend-avatar"><head-image :size="45" :name="item.recvNickName" :url="item.recvHeadImage"></head-image></div>
                  <div class="request-info">
                    <div class="nick-name"><div>{{item.recvNickName}}</div></div>
                    <div class="info-text"><div>{{item.remark}}</div></div>
                  </div>
                  <div class="btn-group">
                    <el-button type="danger" size="mini" @click="recallFriendRequest(item.id)">撤回</el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
      <div v-show="showType === 2">
        <div class="friend-detail">
          <head-image  :size="200"
                       :name="userInfo.nickName"
                       :url="userInfo.headImage"
                       @click.native="showFullImage()"></head-image>
          <div>
            <div class="info-item">
              <el-row>
                <el-col :span="10">
                  <el-descriptions title="好友信息" class="description" :column="1">
                    <el-descriptions-item label="用户名">{{ userInfo.userName }}
                    </el-descriptions-item>
                    <el-descriptions-item label="昵称">{{ userInfo.nickName }}
                    </el-descriptions-item>
                    <el-descriptions-item label="性别">{{ userInfo.sex==0?"男":"女" }}</el-descriptions-item>
                    <el-descriptions-item label="签名">{{ userInfo.signature }}</el-descriptions-item>
                  </el-descriptions>
                </el-col>
                <el-col :span="6">
                  <el-descriptions title="" class="description" :column="1">
                    <el-descriptions-item label="备注名">
                      <el-input v-model="userInfo.friendRemark" size="small" maxlength="15" placeholder="好友备注"></el-input>
                    </el-descriptions-item>
                    <el-descriptions-item label="聊天头像">
                      <file-upload class="avatar-uploader" :action="imageAction"
                                   :showLoading="true" :maxSize="maxSize" @success="onUploadAvatarSuccess"
                                   :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']">
                        <img v-if="userInfo.myHeadImageToFriend" :src="userInfo.myHeadImageToFriend" class="my-avatar">
                        <i v-else class="el-icon-upload"></i>
                      </file-upload>
                    </el-descriptions-item>
                    <el-descriptions-item label="动态">
                      <div class="friend-space" @click="openFriendSpace">
                        <svg class="icon svg-icon" aria-hidden="true">
                          <use xlink:href="#icon-shejiaotubiao-40"></use>
                        </svg>
                      </div>
                    </el-descriptions-item>
                    <el-descriptions-item label="歌单">
                      <div class="friend-music" @click="openFriendMusic">
                        <svg class="icon svg-icon" aria-hidden="true">
                          <use xlink:href="#icon-Music"></use>
                        </svg>
                      </div>
                    </el-descriptions-item>
                  </el-descriptions>
                </el-col>
              </el-row>
            </div>
            <div class="frient-btn-group">
              <el-button v-show="isFriend" icon="el-icon-chat-dot-round" type="primary"  @click="onSendMessage(userInfo)">发送消息</el-button>
              <el-button v-show="!isFriend" icon="el-icon-plus" type="primary"  @click="onAddFriend(userInfo)">加为好友</el-button>
              <el-button v-show="isFriend" icon="el-icon-delete"  type="danger" @click="onDelFriend(userInfo)">删除好友</el-button>
              <el-button v-show="isFriend" type="success" @click="modifyFriendInfo(userInfo)">提交</el-button>
            </div>
          </div>
        </div>
        <el-divider content-position="center"></el-divider>

      </div>
    </el-container>
    <drawer
        :visible="friendSpaceVisible"
        :width="60"
        @close="closeDrawer">
      <template v-slot:header>
        <space-cover :name="'好友空间'" @refresh="refreshTalkList" :show-add="false" :show-notify="false"></space-cover>
      </template>
      <template v-slot:main>
        <talk-list ref="talkListRef" :category="'private'" :section="'friend'" :friend-id="userInfo.id"></talk-list>
      </template>
    </drawer>
    <music-play ref="musicPlayRef" :category="'private'" :section="'friend'" :friend-id="userInfo.id" :show-upload="false"></music-play>
  </el-container>
</template>

<script>
import FriendItem from "../components/friend/FriendItem.vue";
import AddFriend from "../components/friend/AddFriend.vue";
import HeadImage from "../components/common/HeadImage.vue";
import TalkList from "@/components/talk/TalkList";
import Drawer from "@/components/common/Drawer";
import SpaceCover from "@/components/common/SpaceCover";
import FileUpload from "@/components/common/FileUpload";
import MusicPlay from "@components/common/musicPlay.vue";
import { pinyin } from 'pinyin-pro';

export default {
  name: "friend",
  components: {
    FriendItem,
    AddFriend,
    HeadImage,
    TalkList,
    Drawer,
    SpaceCover,
    FileUpload,
    MusicPlay,
  },
  data() {
    return {
      searchText: "",
      showAddFriend: false,
      userInfo: {},
      activeIdx: -1,
      friendSpaceVisible: false,
      maxSize: 2 * 1024 * 1024,
      activeFriend: {},
      showType: 0, // 0:不展示；1:新的朋友 2:好友信息
      headerTitle: '',
    }
  },
  methods: {
    showFriendRequest() {
      this.showType = 1;
      this.headerTitle = '新的朋友';
      this.activeFriend = {}
    },
    onShowAddFriend() {
      this.showAddFriend = true;
    },
    onCloseAddFriend() {
      this.showAddFriend = false;
    },
    onActiveItem(friend) {
      this.activeFriend = friend;
      this.headerTitle = friend.nickName
      this.showType = 2;
      this.loadUserInfo(friend);
    },
    onDelFriend(friend) {
      this.$confirm(`确认删除'${friend.nickName}',并清空聊天记录吗?`, '确认解除?', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: `/friend/delete/${friend.id}`,
          method: 'delete'
        }).then((data) => {
          this.$message.success("删除好友成功");
          this.$store.commit("removeFriend", friend.id);
          this.$store.commit("removePrivateChat", friend.id);
        })
      })
    },
    onAddFriend(user){
      this.$http({
        url: "/friend/add",
        method: "post",
        params: {
          friendId: user.id
        }
      }).then((data) => {
        if (data === 1) {
          this.$message.warning("申请成功，请等待对方通过")
        } else if (data === 2) {
          this.$message.success("申请成功，对方已成为您的好友");
        }
      })
    },
    onSendMessage(user) {
      let chat = {
        type: 'PRIVATE',
        targetId: user.id,
        showName: user.friendRemark ? user.friendRemark : user.nickName,
        headImage: user.headImage,
      };
      this.$store.commit("openChat", chat);
      this.$store.commit("activeChat", 0);
      this.$router.push("/home/chat");
    },
    showFullImage() {
      if (this.userInfo.headImage) {
        this.$store.commit('showFullImageBox', this.userInfo.headImage);
      }
    },
    updateFriendInfo(friend, user) {
      // store的数据不能直接修改，深拷贝一份store的数据
      friend = JSON.parse(JSON.stringify(friend));
      friend.nickName = user.nickName;
      friend.headImage = user.headImage;
      this.$http({
        url: "/friend/update",
        method: "put",
        data: friend
      }).then(() => {
        this.$store.commit("updateFriend", friend);
        this.$store.commit("updateChatFromFriend", user);
      })
    },
    loadUserInfo(friend) {
      this.$http({
        url: `/user/find/${friend.id}`,
        method: 'get'
      }).then((user) => {
        this.userInfo = user;
        this.headerTitle = user.nickName;
        // 如果发现好友的头像和昵称改了，进行更新
        if (user.nickName != friend.nickName || user.headImage != friend.headImage) {
          this.updateFriendInfo(friend, user)
        }
      })
    },
    modifyFriendInfo(friend) {
      friend = JSON.parse(JSON.stringify(friend));
      if (!this.userInfo.friendRemark) {
        this.$message.warning('不能输入空内容');
        return;
      }
      friend.nickName = this.userInfo.nickName;
      friend.friendRemark = this.userInfo.friendRemark;
      friend.myHeadImageToFriend = this.userInfo.myHeadImageToFriend;
      this.$http({
        url: "/friend/update",
        method: "put",
        data: friend
      }).then(() => {
        this.$message.success('操作成功');
        this.$store.commit("updateFriend", friend);
        this.$store.commit("updateChatFromFriend", this.userInfo);
      })
    },
    openFriendSpace() {
      this.friendSpaceVisible = true;
    },
    openFriendMusic() {
      this.$refs.musicPlayRef.show();
    },
    closeDrawer() {
      this.friendSpaceVisible = false;
    },
    refreshTalkList() {
      this.$refs.talkListRef.refreshTalkList();
    },
    onUploadAvatarSuccess(data) {
      this.userInfo.myHeadImageToFriend = data.originUrl;
    },
    firstLetter(strText) {
      // 使用pinyin-pro库将中文转换为拼音
      let pinyinOptions = {
        toneType: 'none', // 无声调
        type: 'normal' // 普通拼音
      };
      let pyText = pinyin(strText, pinyinOptions);
      return pyText[0];
    },
    isEnglish(character) {
      return /^[A-Za-z]+$/.test(character);
    },
    rejectFriendRequest(id) {
      this.$http({
        url: `/friend/request/reject?id=${id}`,
        method: "post",
      }).then(() => {

      })
    },
    approveFriendRequest(id) {
      this.$http({
        url: `/friend/request/approve?id=${id}`,
        method: "post",
      }).then(() => {

      })
    },
    recallFriendRequest(id) {
      this.$http({
        url: `/friend/request/recall?id=${id}`,
        method: "post",
      }).then(() => {

      })
    },
  },
  computed: {
    mine() {
      return this.$store.state.userStore.userInfo;
    },
    friendStore() {
      return this.$store.state.friendStore;
    },
    isFriend(){
      return this.friendStore.friends.some(f=>f.id===this.userInfo.id && !f.deleted);
    },
    curFriend() {
      return this.friendStore.activeFriend;
    },
    imageAction(){
      return `/image/upload`;
    },
    friendMap() {
      // 按首字母分组
      let map = new Map();
      this.friendStore.friends.forEach((f) => {
        if (f.deleted || (this.searchText && !f.nickName.includes(this.searchText))) {
          return;
        }
        let letter = this.firstLetter(f.nickName).toUpperCase();
        // 非英文一律为#组
        if (!this.isEnglish(letter)) {
          letter = "#"
        }
        if (f.online) {
          letter = '在线'
        }
        if (map.has(letter)) {
          map.get(letter).push(f);
        } else {
          map.set(letter, [f]);
        }
      })
      // 排序
      let arrayObj = Array.from(map);
      arrayObj.sort((a, b) => {
        // #组在最后面
        if (a[0] == '#' || b[0] == '#') {
          return b[0].localeCompare(a[0])
        }
        return a[0].localeCompare(b[0])
      })
      map = new Map(arrayObj.map(i => [i[0], i[1]]));
      return map;
    },
    friendKeys() {
      return Array.from(this.friendMap.keys());
    },
    friendValues() {
      return Array.from(this.friendMap.values());
    },
    receivedFriendRequest() {
      return this.$store.state.friendStore.friendRequest.filter((r) => r.recvId === this.mine.id && r.status === 1)
    },
    launchFriendRequest() {
      return this.$store.state.friendStore.friendRequest.filter((r) => r.sendId === this.mine.id && r.status === 1)
    }
  },
  mounted() {

  }
}
</script>

<style scoped lang="scss">
.friend-page {
  .friend-list-box {
    display: flex;
    flex-direction: column;
    border-right: #54CC36 solid 1px;
    background: white;

    .friend-list-header {
      height: 50px;
      display: flex;
      align-items: center;
      padding: 5px 8px;

      .add-btn {
        padding: 5px !important;
        margin: 5px;
        font-size: 20px;
        color: #54CC36;
        border: #54CC36 1px solid;
        background-color: #F0F8FF;
        border-radius: 50%;
      }
    }

    .friend-items {
      flex: 1;

      .top-item {
        height: 50px;
        display: flex;
        position: relative;
        padding: 5px;
        align-items: center;
        white-space: nowrap;
        cursor: pointer;

        .top-item-avatar {
          display: flex;
          justify-content: center;
          align-items: center;
        }

        .top-item-info {
          flex: 1;
          display: flex;
          flex-direction: column;
          padding-left: 10px;
          text-align: left;

          .top-item-name {
            font-size: 14px;
            white-space: nowrap;
            overflow: hidden;
          }
        }

        &.active {
          background-color: var(--active-color);
        }
      }

      .letter {
        text-align: left;
        font-size: var(--im-larger-size-larger);
        padding: 5px 15px;
        color: var(--im-text-color-light);
      }

      .divider {
        border-bottom: 1px solid #ddd;
        margin: 10px;
      }
    }
  }

  .container {
    display: flex;
    flex-direction: column;
    border: var(--border-color) solid 1px;

    .header {
      width: 100%;
      height: 50px;
      padding: 5px;
      line-height: 50px;
      font-size: 20px;
      text-align: left;
      text-indent: 10px;
      font-weight: 600;
      background-color: white;
      border: var(--border-color) solid 1px;
    }

    .request-box {
      flex: 1;

      .friend-request-item {
        display: flex;
        position: relative;
        align-items: center;
        cursor: pointer;
        margin: 0 30px;
        padding: 10px;
        border-bottom: 1px solid #ccc;

        .request-info {
          margin-left: 15px;
          flex: 3;
          display: flex;
          flex-direction: column;
          flex-shrink: 0;
          overflow: hidden;

          .nick-name {
            display: flex;
            align-items: center;
            font-weight: 600;
            font-size: 16px;
            line-height: 30px;
          }

          .info-text {
            display: flex;
            word-break: break-all;
            font-size: 14px;
            line-height: 20px;
            text-align: left;
          }
        }

        .btn-group {
          text-align: left !important;
          margin-top: 20px;
          margin-left: 60px;
        }
      }
    }

    .friend-request {
      display: flex;
      flex-direction: column;
    }

    .friend-detail {
      display: flex;
      padding: 50px 80px 20px 80px;
      text-align: center;

      .info-item {
        margin-left: 20px;
        background-color: #ffffff;

        .avatar-uploader {
          width: 32px;
          height: 32px;
          line-height: 32px;

          .my-avatar {
            width: 32px;
            height: 32px;
            object-fit: cover;
          }
        }

        .friend-space {
          display: flex;
          justify-content: center;
          align-items: center;
          cursor: pointer;
          margin-left: .5rem;

          .icon {
            display: block;
            height: 30px;
            line-height: 30px;
            font-size: 28px;
            color: #333;
            -webkit-transition: font-size 0.25s linear, width 0.25s linear;
            -moz-transition: font-size 0.25s linear, width 0.25s linear;
            transition: font-size 0.25s linear, width 0.25s linear;
          }
        }

        .friend-music {
          display: flex;
          justify-content: center;
          align-items: center;
          cursor: pointer;
          margin-left: .5rem;

          .icon {
            display: block;
            height: 26px;
            line-height: 26px;
            font-size: 26px;
            color: #333;
            -webkit-transition: font-size 0.25s linear, width 0.25s linear;
            -moz-transition: font-size 0.25s linear, width 0.25s linear;
            transition: font-size 0.25s linear, width 0.25s linear;
          }
        }
      }

      .description {
        padding: 20px 20px 0px 20px;
      }
    }

    .frient-btn-group {
      text-align: left !important;
      padding: 20px;
      display: flex;
    }
  }
}
</style>