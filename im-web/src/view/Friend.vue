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
      <el-scrollbar class="friend-list-items">
        <div v-for="(friend,index) in $store.state.friendStore.friends" :key="index">
          <friend-item v-show="friend.nickName.startsWith(searchText)"  :index="index"
                       :active="friend === $store.state.friendStore.activeFriend" @chat="onSendMessage(friend)"
                       @delete="onDelItem(friend,index)" @click.native="onActiveItem(friend,index)"
                       :friend="friend">
          </friend-item>
        </div>
      </el-scrollbar>
    </el-aside>
    <el-container class="friend-box">
      <div class="friend-header" v-show="userInfo.id">
        {{userInfo.nickName}}
      </div>
      <div v-show="userInfo.id">
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
                  </el-descriptions>
                </el-col>
              </el-row>
            </div>
            <div class="frient-btn-group">
              <el-button v-show="isFriend" icon="el-icon-chat-dot-round" type="primary"  @click="onSendMessage(userInfo)">发送消息</el-button>
              <el-button v-show="!isFriend" icon="el-icon-plus" type="primary"  @click="onAddFriend(userInfo)">加为好友</el-button>
              <el-button v-show="isFriend" icon="el-icon-delete"  type="danger" @click="onDelItem(userInfo,activeIdx)">删除好友</el-button>
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
  },
  data() {
    return {
      searchText: "",
      showAddFriend: false,
      userInfo: {},
      activeIdx: -1,
      friendSpaceVisible: false,
      maxSize: 2 * 1024 * 1024,
    }
  },
  methods: {
    onShowAddFriend() {
      this.showAddFriend = true;
    },
    onCloseAddFriend() {
      this.showAddFriend = false;
    },
    onActiveItem(friend, idx) {
      this.$store.commit("activeFriend", idx);
      this.activeIdx = idx
      this.loadUserInfo(friend, idx);
    },
    onDelItem(friend, idx) {
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
          this.$store.commit("removeFriend", idx);
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
        this.$message.success("添加成功，对方已成为您的好友");
        let friend = {
          id:user.id,
          nickName: user.nickName,
          headImage: user.headImage,
          online: user.online
        }
        this.$store.commit("addFriend",friend);
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
    updateFriendInfo(friend, user, index) {
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
    loadUserInfo(friend, index) {
      this.$http({
        url: `/user/find/${friend.id}`,
        method: 'get'
      }).then((user) => {
        this.userInfo = user;
        // 如果发现好友的头像和昵称改了，进行更新
        if (user.nickName != friend.nickName || user.headImage != friend.headImage) {
          this.updateFriendInfo(friend, user, index)
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
    closeDrawer() {
      this.friendSpaceVisible = false;
    },
    refreshTalkList() {
      this.$refs.talkListRef.refreshTalkList();
    },
    onUploadAvatarSuccess(data) {
      this.userInfo.myHeadImageToFriend = data.originUrl;
    },
  },
  computed: {
    friendStore() {
      return this.$store.state.friendStore;
    },
    isFriend(){
      return this.friendStore.friends.find((f)=>f.id==this.userInfo.id);
    },
    curFriend() {
      return this.friendStore.activeFriend;
    },
    imageAction(){
      return `/image/upload`;
    },
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
      border-bottom: 1px #ddd solid;

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

    .friend-list-items {
      flex: 1;
    }
  }

  .friend-box {
    display: flex;
    flex-direction: column;
    border: var(--border-color) solid 1px;

    .friend-header {
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