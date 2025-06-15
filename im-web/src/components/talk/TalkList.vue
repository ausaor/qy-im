<template>
  <div class="talk-list">
    <div class="contentBox">
      <div class="talk-notice" v-show="newTalkCount > 0">
        <div class="new-talk-content">
          <head-image v-for="(talk, index) in newTalkList" :key="index" :url="talk.avatar" :name="talk.nickName" :size="24"></head-image>
          <span class="new-talk-text">{{newTalkCount}}条新动态</span>
        </div>
      </div>
      <div class="sayItem" v-for="(item, index) in talkList" :key="index">
        <head-image :url="item.avatar" :id="item.userId" :name="item.nickName" :size="45"></head-image>
        <div class="rightBox">
          <div class="nickname">
            {{ item.nickName }}
          </div>
          <p class="content" v-highlight v-html="$emo.transform(item.content)"></p>
          <v-row class="talk-files" v-if="item.fileList">
            <v-col
                :cols="8" :sm="6" :md="4"
                v-for="(fileItem, idx) of item.fileList"
                :key="idx"
            >
              <v-img v-if="fileItem.fileType == 1"
                  class="file-items"
                  :src="fileItem.url"
                  aspect-ratio="1"
                  max-height="200"
                  @click.prevent="previewImg(fileItem.url, [fileItem.url])"
              />
              <div v-if="fileItem.fileType == 2" class="file-items">
                <img class="video-image" :src="fileItem.coverUrl" loading="lazy"/>
                <span class="play-icon el-icon-video-play" @click="playVideo(fileItem.url, fileItem.coverUrl)"></span>
              </div>
              <vue-audio v-if="fileItem.fileType == 3"
                         :audio-source="fileItem.url"
              ></vue-audio>
            </v-col>
          </v-row>
          <div class="bottomBox">
            <div v-if="item.address" class="address">
              <a>
                <i class="el-icon-location-outline"></i> {{ item.address }}
              </a>
            </div>
            <span class="time">
              <a>
                <i class="el-icon-time"></i> {{ item.createTime }}
              </a>
            </span>
            <div class="operate" ref="operate">
              <span class="like" v-if="!item.isLike" @click="sayLike(item)">
                <span class="icon iconfont icon-dianzan1"></span>
              </span>
              <span class="like" v-else @click="cancelLike(item)">
                <span class="icon iconfont icon-dianzan1" style="color: orange;"></span>
              </span>
              <span class="fgx"></span>
              <span class="commentBtn" @click="handleShowCommentBox(null, item.id, index)">
                <i class="el-icon-chat-dot-square"></i> 评论
              </span>
              <span class="delBtn" v-if="item.isOwner">
                <el-popconfirm
                    confirm-button-text='确认'
                    cancel-button-text='取消'
                    icon="el-icon-info"
                    icon-color="red"
                    title="确认删除当前动态吗？"
                    @confirm="delTalk(item, index)"
                >
                  <el-button slot="reference" icon="el-icon-delete-solid" size="mini" circle @click.stop></el-button>
                </el-popconfirm>

              </span>
              <span class="editBtn" v-if="item.isOwner && item.category === category" @click="editTalk(item)">
                <i class="el-icon-edit"></i>
              </span>
              <span class="fgx"></span>
              <span class="setBtn" @click="commentSetDialogOpen(item, index)">
                <i class="el-icon-setting"></i>
              </span>
            </div>
            <a class="operateBtn" @click="handleShowOperate(index)">
              <i class="el-icon-more"></i>
            </a>
          </div>
          <div class="interaction">
            <div v-if="item.talkStarVOS && item.talkStarVOS.length"
                 :class="item.talkStarVOS && item.talkCommentVOS ? 'like-container is_border' : 'like-container'">
              <svg class="icon svg-icon" aria-hidden="true">
                <use xlink:href="#icon-dianzan1"></use>
              </svg>
              <span class="star-user" v-for="(star, user_index) in item.talkStarVOS" :key="user_index"
                    @click="showUserInfo($event, star.userId)">
                {{ star.nickname }}
                <span v-if="user_index < item.talkStarVOS.length - 1">，</span>
              </span>
            </div>
            <div class="commentBox">
              <div class="commentItem" v-for="(comment, comment_index) in getVisibleComments(item)"
                   :key="comment_index">
                <div class="comment-content">
                  <head-image class="comment-avatar" :url="comment.userAvatar" :id="comment.userId" :name="comment.userNickname" :size="24"/>
                  <span class="username" v-if="!comment.replyCommentId" @click="showUserInfo($event, comment.userId)">
                      {{ comment.userNickname }}：
                  </span>
                  <span style="margin-left: 5px; margin-right: 5px;" v-if="comment.replyCommentId">回复</span>
                  <head-image v-if="comment.replyCommentId" class="comment-avatar" :url="comment.replyUserAvatar" :id="comment.replyUserId" :name="comment.replyUserNickname" :size="24"/>
                  <span class="username" v-if="comment.replyCommentId" @click="showUserInfo($event, comment.replyUserId)">{{ comment.replyUserNickname }}：</span>
                  <span class="content point" v-html="$emo.transform(comment.content)"
                        @click="handleShowCommentBox(comment, item.id, index)">
                  </span>
                  <div class="del-btn" v-if="comment.isOwner">
                    <el-popconfirm
                        confirm-button-text='确认'
                        cancel-button-text='取消'
                        icon="el-icon-info"
                        icon-color="red"
                        title="确认删除当前评论吗？"
                        @confirm="delComment(item, comment.id)"
                    >
                      <el-button slot="reference" size="mini" type="text" @click.stop style="color: #d42e07">删除</el-button>
                    </el-popconfirm>
                  </div>
                </div>
              </div>
              <div class="load-more-comments"
                  v-if="item.talkCommentVOS.length > getVisibleCommentsCount(item.id)"
                  @click="loadMoreComments(item.id)">
                <span class="load-more-text">
                  展开更多评论 ({{ item.talkCommentVOS.length - getVisibleCommentsCount(item.id) }}条)
                </span>
                <i class="el-icon-arrow-down"></i>
              </div>
              <!-- Collapse Comments -->
              <div class="collapse-comments"
                  v-if="getVisibleCommentsCount(item.id) > 5"
                  @click="collapseComments(item.id)">
                <span class="collapse-text">收起评论</span>
                <i class="el-icon-arrow-up"></i>
              </div>
              <input-box ref="contentInputBox" :placeholder="placeholder" @send="(...args) => sayComment(item, ...args)"></input-box>
            </div>
          </div>
        </div>
      </div>
      <pagination :totalPage="page.totalPage" :pageNo="page.pageNo" @changePage="handlePage"></pagination>
    </div>
    <add-talk :visible.sync="addTalkVisible"
              v-if="addTalkVisible"
              :category="category"
              :group-id="groupId"
              :region-group-id="regionGroupId"
              :region-code="regionCode"
              @close="closeAddTalkDialog"
              @refresh="refreshTalkList"
              :talkId="curTalk.id">
    </add-talk>
    <!--    <image-preview :img="images"></image-preview>-->
    <el-dialog title="评论设置"
               :visible.sync="commentSetVisible"
               :before-close="commentSetDialogClose"
               :append-to-body="true"
               width="25%">
      <el-form ref="commentSetForm" label-width="auto"
               :model="commentSetForm" class="form-box">
        <el-form-item label="角色选择：" prop="character" label-width="120px" class="form-item">
          <div style="display: flex;align-items: center;gap: 10px;">
            <span class="character-item" v-on:click="openCharacterChooseDialog">
              <el-avatar fit="fit" size="medium" icon="el-icon-user-solid" :src="commentSetForm.avatar"></el-avatar>
            </span>
            <span class="nick-name">{{ commentSetForm.nickName }}</span>
            <el-button @click="removeCharacter" class="del-btn"
                       v-if="commentSetForm.commentCharacterId"
                       type="danger" icon="el-icon-delete"
                       size="mini" circle></el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="confirmCharacter()">确认</el-button>
          <el-button @click="cancelCharacter()">取消</el-button>
        </el-form-item>
      </el-form>
      <template-character-choose
          :visible.sync="chooseCharacterDialogVisible"
          :appendToBody="true"
          @close="closeChooseCharacterDialog"
          @confirm="confirmChooseCharacter">
      </template-character-choose>
    </el-dialog>
    <video-play  ref="videoPlay" :videoUrl="videoUrl" :posterUrl="posterUrl" @close="closeVideoPlay"></video-play>
  </div>
</template>

<script>
import AddTalk from "@/components/talk/AddTalk";
import HeadImage from "@/components/common/HeadImage";
import BatchFileUpload from "@/components/common/BatchFileUpload";
import Avatar from "@/components/common/Avatar";
import Pagination from "@/components/pagination/Pagination";
import TemplateCharacterChoose from "@/components/template/TemplateCharacterChoose";
import Emoji from '@/components/emoji';
import VideoPlay  from "../common/VideoPlay.vue";
import InputBox from "@components/common/InputBox.vue";
/*import ImagePreview from "@/components/imagePreview/ImagePreview";*/

export default {
  name: "FriendActivity",
  components: {
    HeadImage,
    BatchFileUpload,
    AddTalk,
    Avatar,
    Pagination,
    TemplateCharacterChoose,
    Emoji,
    VideoPlay,
    InputBox,
    /*ImagePreview*/
  },
  props: {
    category: {
      type: String,
      required: true
    },
    section: {
      type: String,
      required: true
    },
    friendId: {
      type: Number,
      default: null,
    },
    groupId: {
      type: Number,
      default: null,
    },
    regionGroupId: {
      type: Number,
      default: null,
    },
    regionCode: {
      type: String,
      default: null,
    },
    newTalkList: {
      type: Array,
      default: () => {
        return []
      }
    },
    newTalkCount: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      addTalkVisible: false,
      page: {
        pageNo: 1,
        pageSize: 10,
        totalPage: 0,
      },
      talkList: [],
      images: {},
      lastIndex: null,
      curTalk: {},
      curTalkIndex: -1,
      commentSetVisible: false,
      commentSetForm: {
        commentCharacterAvatarId: null,
        commentCharacterId: null,
        nickName: '',
        avatar: '',
      },
      chooseCharacterDialogVisible: false,
      commentLastIndex: null,
      placeholder: "请输入内容",
      comment: {
        replyUserId: null,
        replyUserNickname: null,
        replyCommentId: null,
        talkId: null,
        content: ""
      },
      showCommentBox: false,
      lastEditRange: null,
      showEmoji: false,
      videoUrl: '',
      posterUrl: '',
      visibleCommentsCount: {},
    }
  },
  created() {
    if(this.section === 'my-friends') {
      this.pageQueryTalkList();
    }
  },
  computed: {},
  methods: {
    getVisibleComments(talk) {
      const count = this.visibleCommentsCount[talk.id] || 5
      return talk.talkCommentVOS.slice(0, count)
    },
    getVisibleCommentsCount(talkId) {
      return this.visibleCommentsCount[talkId] || 5
    },
    loadMoreComments(talkId) {
      const currentCount = this.visibleCommentsCount[talkId] || 5
      this.$set(this.visibleCommentsCount, talkId, currentCount + 10)
    },
    collapseComments(talkId) {
      this.$set(this.visibleCommentsCount, talkId, 5)
    },
    sayComment(talk, sendText) {
      if (!sendText.trim()) {
        return
      }
      this.comment.content = sendText
      let params = {
        talkId: talk.id,
        content: this.comment.content,
        userNickname: talk.commentCharacterName,
        characterId: talk.commentCharacterId,
        avatarId: talk.commentCharacterAvatarId,
        userAvatar: talk.commentCharacterAvatar,
        replyCommentId: this.comment.replyCommentId
      }
      this.$http({
        url: "/talk/addTalkComment",
        method: 'post',
        data: params
      }).then((data) => {
        if (data.characterId) {
          talk.commentCharacterId = data.characterId;
          talk.commentCharacterAvatarId = data.avatarId;
          talk.commentCharacterName = data.userNickname;
          talk.commentCharacterAvatar = data.userAvatar;
        }
        talk.talkCommentVOS.push(data);
        this.$message.success("评论成功");
        this.comment = {}
        this.$refs.contentInputBox[this.commentLastIndex].hide();
        this.showCommentBox = false
      }).finally(() => {
        this.placeholder = '请输入内容';
      })
    },
    showUserInfo(e, userId) {
      if (userId && userId > 0) {
        this.$http({
          url: `/user/find/${userId}`,
          method: 'get'
        }).then((user) => {
          this.$store.commit("setUserInfoBoxPos", e);
          this.$store.commit("showUserInfoBox", user);
        })
      }
    },
    handleShowAddTalk() {
      this.addTalkVisible = true;
    },
    refreshTalkList() {
      this.page.pageNo = 1;
      this.page.totalPage = 0;
      this.talkList = [];
      this.pageQueryTalkList();
    },
    pageQueryTalkList() {
      let friendIds = [];
      if (this.friendId) {
        friendIds.push(this.friendId);
      }
      if (this.section === 'friend' && friendIds.length === 0) {
        return
      }
      if (this.section === 'group' && !this.groupId) {
        return
      }
      if (this.section === 'my-region' && !this.regionGroupId) {
        return
      }
      if (this.section === 'region' && !this.regionCode) {
        return
      }
      let params = {
        category: this.category,
        section: this.section,
        groupId: this.groupId,
        regionGroupId: this.regionGroupId,
        regionCode: this.regionCode,
        friendIds: friendIds,
      }
      this.$http({
        url: `/talk/pageQueryTalkList?pageNo=${this.page.pageNo}&pageSize=${this.page.pageSize}`,
        method: "post",
        data: params
      }).then((data) => {
        this.talkList.push(...data.data)
        this.page.totalPage = (data.total - 1) / this.page.pageSize + 1;
      })
    },
    handlePreviewImg(imgs) {
      this.images = {
        urls: imgs,
        time: new Date().getTime()
      }
    },
    previewImg(img, imgUrls) {
      this.$imagePreview({
        images: imgUrls,
        index: imgUrls.indexOf(img)
      });
    },
    handlePage(pageNo) {
      this.page.pageNo = pageNo;
      this.pageQueryTalkList();
    },
    handleShowOperate(index) {
      if (this.lastIndex != null && this.lastIndex != index) {
        this.$refs.operate[this.lastIndex].style.display = "none"
      }
      if (this.lastIndex == index) {
        if (this.$refs.operate[index].style.display == "block") {
          this.$refs.operate[index].style.display = "none"
        } else {
          this.$refs.operate[index].style.display = "block"
        }
      } else {
        this.$refs.operate[index].style.display = "block"
      }
      this.lastIndex = index
    },
    sayLike(talk) {
      let params = {
        talkId: talk.id,
        nickname: talk.commentCharacterName,
        characterId: talk.commentCharacterId,
        avatarId: talk.commentCharacterAvatarId,
        avatar: talk.commentCharacterAvatar,
      }
      this.$http({
        url: "/talk/like",
        method: 'post',
        data: params
      }).then((data) => {
        talk.isLike = true
        if (data.characterId) {
          talk.commentCharacterId = data.characterId;
          talk.commentCharacterName = data.nickname;
          talk.commentCharacterAvatar = data.avatar;
          talk.commentCharacterAvatarId = data.avatarId;
        }
        talk.talkStarVOS.push(data);
        this.$message.success("点赞成功");
      }).finally(() => {

      })
    },
    cancelLike(talk) {
      let params = {
        talkId: talk.id
      }
      //let userId = this.$store.state.userStore.userInfo.id;
      this.$http({
        url: "/talk/cancelLike",
        method: 'post',
        data: params
      }).then((data) => {
        talk.isLike = false;
        //talk.commentCharacterId = null;
        //talk.commentCharacterName = null;
        //talk.commentCharacterAvatar = null;
        talk.talkStarVOS = talk.talkStarVOS.filter(function (item) {
          return !item.isOwner;
        });
        this.$message.success("取消成功");
      }).finally(() => {

      })
    },
    delTalk(talk, index) {
      this.$http({
        url: "/talk/delete",
        method: 'delete',
        data: {id: talk.id}
      }).then((data) => {
        this.$message.success("删除成功");
        //this.talkList.splice(index, 1);
        this.refreshTalkList();
        this.lastIndex = null;
      }).finally(() => {

      })
    },
    editTalk(talk) {
      this.curTalk = talk;
      this.addTalkVisible = true;
    },
    closeAddTalkDialog() {
      this.addTalkVisible = false;
      this.curTalk = {};
    },
    delBtn(e) {
      e.stopPropagation();
    },
    commentSetDialogOpen(talk, index) {
      this.commentSetForm.nickName = talk.commentCharacterName ? talk.commentCharacterName : talk.commentUserNickname;
      this.commentSetForm.avatar = talk.commentCharacterAvatar ? talk.commentCharacterAvatar : talk.commentUserAvatar;
      this.commentSetForm.commentCharacterId = talk.commentCharacterId;
      this.commentSetForm.commentCharacterAvatarId = talk.commentCharacterAvatarId;
      this.curTalk = talk;
      this.curTalkIndex = index;
      this.commentSetVisible = true;
    },
    commentSetDialogClose() {
      this.commentSetVisible = false;
      this.commentSetForm.nickName = '';
      this.commentSetForm.avatar = '';
      this.commentSetForm.commentCharacterId = null;
      this.commentSetForm.commentCharacterAvatarId = null;
      this.curTalk = {};
      this.curTalkIndex = -1;
    },
    openCharacterChooseDialog() {
      this.chooseCharacterDialogVisible = true;
    },
    removeCharacter() {
      this.commentSetForm.commentCharacterId = null;
      this.commentSetForm.nickName = '';
      this.commentSetForm.avatar = '';
    },
    resetCommentCharacter() {
      this.commentSetForm.commentCharacterId = null;
      this.commentSetForm.nickName = '';
      this.commentSetForm.avatar = '';
    },
    closeChooseCharacterDialog() {
      this.chooseCharacterDialogVisible = false;
    },
    confirmChooseCharacter(resultData) {
      if (resultData?.characterAvatar?.id) {
        this.commentSetForm.commentCharacterAvatarId = resultData.characterAvatar.id;
        this.commentSetForm.nickName = resultData.characterAvatar.level === 0 ? resultData.templateCharacter.name : resultData.characterAvatar.name;
        this.commentSetForm.avatar = resultData.characterAvatar.avatar;
        this.commentSetForm.commentCharacterId = resultData.templateCharacter.id;
      } else {
        this.commentSetForm.nickName = resultData.templateCharacter.name;
        this.commentSetForm.avatar = resultData.templateCharacter.avatar;
        this.commentSetForm.commentCharacterId = resultData.templateCharacter.id;
      }

      this.chooseCharacterDialogVisible = false;
    },
    confirmCharacter() {
      // if (this.curTalk.commentCharacterId && this.commentSetForm.commentCharacterId) {
      //   if (this.curTalk.commentCharacterId !== this.commentSetForm.commentCharacterId) {
      //     this.$message.warning("不能更改评论角色");
      //     return false;
      //   }
      // }
      this.talkList[this.curTalkIndex].commentCharacterId = this.commentSetForm.commentCharacterId;
      this.talkList[this.curTalkIndex].commentCharacterAvatar = this.commentSetForm.avatar;
      this.talkList[this.curTalkIndex].commentCharacterName = this.commentSetForm.nickName;
      this.talkList[this.curTalkIndex].commentCharacterAvatarId = this.commentSetForm.commentCharacterAvatarId;
      this.resetCommentCharacter();
      this.curTalk = {};
      this.curTalkIndex = -1;
      this.commentSetVisible = false;
    },
    cancelCharacter() {
      this.resetCommentCharacter();
      this.curTalk = {};
      this.curTalkIndex = -1;
      this.commentSetVisible = false;
    },
    handleShowCommentBox(comment, talkId, index) {
      //let userId = this.$store.state.userStore.userInfo.id;
      if (comment && comment.isOwner) {
        this.$message.warning("不能回复自己的评论");
        return false;
      }
      if (this.commentLastIndex != null && this.commentLastIndex != index) {
        this.$refs.contentInputBox[this.commentLastIndex].hide();
      }
      if (this.commentLastIndex == index) {
        if (this.$refs.contentInputBox[index].show) {
          this.$refs.contentInputBox[index].hide();
        } else {
          this.$refs.contentInputBox[index].view();
        }
      } else {
        this.$refs.contentInputBox[index].view();
      }
      this.commentLastIndex = index

      if (comment) {
        this.placeholder = "回复" + comment.userNickname + ":"
        this.comment.replyUserId = comment.userId
        this.comment.replyUserNickname = comment.userNickname
        this.comment.replyCommentId = comment.id
      } else {
        this.comment.replyUserId = null
        this.comment.replyUserNickname = null
        this.comment.replyCommentId = null
        this.placeholder = "请输入内容"
      }
      this.comment.talkId = talkId
      this.showCommentBox = !this.showCommentBox
    },
    delComment(talk, commentId) {
      this.$http({
        url: `/talkComment/delete/${commentId}`,
        method: 'delete'
      }).then((data) => {
        talk.talkCommentVOS = talk.talkCommentVOS.filter(function (item) {
          return item.id !== commentId;
        });
        this.$message.success("删除成功");
      }).finally(() => {

      })
    },
    playVideo(videoUrl, coverImageUrl) {
      this.videoUrl = videoUrl;
      this.posterUrl = coverImageUrl;
      this.$refs.videoPlay.onPlayVideo()
    },
    closeVideoPlay() {
      this.videoUrl = '';
      this.posterUrl = '';
    },
  },
  watch: {
    section: {
      handler(newSection, oldSection) {
        console.log("newSection", newSection);
        if (newSection !== oldSection) {
          this.refreshTalkList();
        }
      }
    },
    groupId: {
      handler(newGroupId, oldGroupId) {
        console.log("newGroupId", newGroupId);
        if (newGroupId !== oldGroupId) {
          this.refreshTalkList();
        }
      }
    },
    regionGroupId: {
      handler(newRegionGroupId, oldRegionGroupId) {
        console.log("newRegionGroupId", newRegionGroupId);
        if (newRegionGroupId !== oldRegionGroupId) {
          this.refreshTalkList();
        }
      }
    },
    friendId: {
      handler(newFriendId, oldFriendId) {
        console.log("newFriendId", newFriendId);
        if (newFriendId && newFriendId !== oldFriendId) {
          this.refreshTalkList();
        }
      }
    },
    regionCode: {
      handler(newRegionCode, oldRegionCode) {
        console.log("newRegionCode", newRegionCode);
        if (newRegionCode !== oldRegionCode && this.section === 'region') {
          this.refreshTalkList();
        }
      }
    }
  }
}
</script>

<style scoped lang="scss">
.col-xl,
.col-xl-auto,
.col-xl-12,
.col-xl-11,
.col-xl-10,
.col-xl-9,
.col-xl-8,
.col-xl-7,
.col-xl-6,
.col-xl-5,
.col-xl-4,
.col-xl-3,
.col-xl-2,
.col-xl-1,
.col-lg,
.col-lg-auto,
.col-lg-12,
.col-lg-11,
.col-lg-10,
.col-lg-9,
.col-lg-8,
.col-lg-7,
.col-lg-6,
.col-lg-5,
.col-lg-4,
.col-lg-3,
.col-lg-2,
.col-lg-1,
.col-md,
.col-md-auto,
.col-md-12,
.col-md-11,
.col-md-10,
.col-md-9,
.col-md-8,
.col-md-7,
.col-md-6,
.col-md-5,
.col-md-4,
.col-md-3,
.col-md-2,
.col-md-1,
.col-sm,
.col-sm-auto,
.col-sm-12,
.col-sm-11,
.col-sm-10,
.col-sm-9,
.col-sm-8,
.col-sm-7,
.col-sm-6,
.col-sm-5,
.col-sm-4,
.col-sm-3,
.col-sm-2,
.col-sm-1,
.col,
.col-auto,
.col-12,
.col-11,
.col-10,
.col-9,
.col-8,
.col-7,
.col-6,
.col-5,
.col-4,
.col-3,
.col-2,
.col-1 {
  width: 100%;
  padding: 4px !important;
}

.talk-list {

  .contentBox {
    width: 72%;
    margin: 20px auto;
    color: #5fb878;

    .talk-notice {
      display: flex;
      justify-content: center;
      align-items: center;

      .new-talk-content {
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 8px 16px;
        background-color: #f5f5f5;
        border-radius: 20px;
        cursor: pointer;

        .new-talk-text {
          font-size: 14px;
          color: red;
          margin-left: 5px;
        }
      }
    }

    .sayItem {
      padding: 10px;
      display: flex;
      margin-bottom: 10px;
      border-radius: 5px;
      position: relative;
      background-color: #ffffff;
      overflow: hidden;

      .avatar {
        img {
          vertical-align: top;
        }
      }

      .rightBox {
        padding-left: 10px;
        display: flex;
        flex-direction: column;
        width: 100%;

        .nickname {
          text-align: left;
          color: #f56c6c;
        }

        .content {
          text-align: left;
          margin-top: 10px;
          white-space: pre-wrap;
        }

        .talk-files {
          margin-top: 4px;
          margin-right: 2px;

          .file-items {
            cursor: pointer;
            border-radius: 4px;
            height: 100%;
            width: 100%;
            position: relative;

            .video-image {
              height: 100%;
              width: 100%;
            }

            .play-icon {
              display: block;
              position: absolute;
              font-size: 80px;
              font-weight: 500;
              width: 80px;
              height: 80px;
              left: 50%;
              top: 50%;
              transform: translate(-50%, -50%);
              cursor: pointer;
              color: #ffffff;
            }
          }

          .vueAudioBetter {
            height: 100%;
            width: 100%;
          }
        }

        .bottomBox {
          margin-top: 20px;
          position: relative;
          display: flex;
          flex-direction: column;

          .address {
            display: flex;
            margin-right: auto;
            color: #5597bd;
            margin-bottom: 10px;
            font-size: 14px;
          }

          .time {
            display: flex;
            margin-right: auto;
            font-size: 14px;
          }

          .operateBtn {
            position: absolute;
            right: 20px;
            bottom: 0;
            display: inline-block;
            padding: 0 5px;
            cursor: pointer;
            font-size: 20px;
            font-weight: bold;
          }

          .operate {
            position: absolute;
            right: 55px;
            bottom: -8px;
            background-color: #4b5153;
            padding: 8px;
            border-radius: 5px;
            display: none;

            &::after {
              content: '';
              position: absolute;
              right: -15px;
              bottom: 10px;
              width: 0;
              height: 0;
              border-width: 8px;
              border-style: solid;
              border-color: transparent transparent transparent #4b5153;
            }

            span {
              padding: 0 10px;
              color: #fff;
              position: relative;
              cursor: pointer;

              &:hover {
                color: #f56c6c;
              }
            }

            .like::after {
              content: "";
              width: 2px;
              height: 100%;
              background-color: #373d40;
              position: absolute;
              right: 0;
            }
          }
        }

        .interaction {
          background-color: #ffffff;
          margin-top: 15px;
          border-radius: 5px;

          .like-container {
            display: flex;
            align-items: center;
            justify-content: left;

            .icon {
              margin-right: 5px;
              display: block;
              height: 20px;
              line-height: 20px;
              font-size: 18px;
              -webkit-transition: font-size 0.25s linear, width 0.25s linear;
              -moz-transition: font-size 0.25s linear, width 0.25s linear;
              transition: font-size 0.25s linear, width 0.25s linear;
            }

            .star-user {
              cursor: pointer;
            }

            span {
              font-size: 14px;
              color: #576b95;
            }
          }

          .is_border {
            border-bottom: 1px dashed rgb(126, 120, 120);
          }

          .commentBox {
            .commentItem {
              text-align: left;
              margin-bottom: 5px;
              position: relative;

              .comment-content {
                display: flex;
                align-items: flex-end;
              }

              .comment-avatar {
                display: inline-block;
              }

              &:first-child {
                margin-top: 10px;
              }

              .username {
                color: #5597bd;
                cursor: pointer;
              }

              .content {
                cursor: pointer;
              }

              .del-btn {
                color: #d42e07;
                font-size: 12px;
                position: absolute;
                right: 5px;
              }
            }

            .load-more-comments,
            .collapse-comments {
              display: flex;
              align-items: center;
              justify-content: center;
              padding: 8px;
              color: #409eff;
              cursor: pointer;
              font-size: 13px;
              border-top: 1px solid #f0f0f0;
              margin-top: 8px;
            }

            .load-more-comments:hover,
            .collapse-comments:hover {
              background-color: #f8f9fa;
            }

            .load-more-text,
            .collapse-text {
              margin-right: 4px;
            }
          }
        }
      }
    }
  }
}

</style>