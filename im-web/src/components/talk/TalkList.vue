<template>
  <div class="talk-list">
    <div class="contentBox">
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
              <video v-if="fileItem.fileType == 2"
                     class="file-items"
                     :poster="fileItem.coverUrl"
                     :src="fileItem.url"
                     style="object-fit: cover"
                     controls
              ></video>
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
                <i class="el-icon-star-on"></i> 赞
              </span>
              <span class="like" v-else @click="cancelLike(item)">
                <i class="el-icon-star-on" style="color: yellow"></i> 取消
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
            <div v-if="item.talkStarVOS"
                 :class="item.talkStarVOS && item.talkCommentVOS ? 'like-container is_border' : 'like-container'">
              <span class="star-user" v-for="(user, user_index) in item.talkStarVOS" :key="user_index"
                    @click="showUserInfo($event, user.userId)">
                <i class="el-icon-star-on" style="color: yellow"></i> {{ user.nickname }}
                <span v-if="user_index < item.talkStarVOS.length - 1">，</span>
              </span>
            </div>
            <div class="commentBox">
              <div class="commentItem" v-for="(comment, comment_index) in item.talkCommentVOS"
                   :key="comment_index">
                <div class="comment-content">
<!--                  <avatar :url="comment.userAvatar" :userId="comment.userId" :size="'small'"
                          class="comment-avatar"></avatar>-->
                  <head-image class="comment-avatar" :url="comment.userAvatar" :id="comment.userId" :name="comment.userNickname" :size="32"/>
                  <span class="username" v-if="!comment.replyCommentId" @click="showUserInfo($event, comment.userId)">
                      {{ comment.userNickname }}：
                  </span>

                  <div v-else style="display: flex;align-items: flex-end;">
                    <span class="username" @click="showUserInfo($event, comment.userId)">{{
                        comment.userNickname
                      }}</span>
                      <span style="margin-left: 5px; margin-right: 5px;">回复</span>
<!--                    <avatar :url="comment.replyUserAvatar" :userId="comment.replyUserId" :size="'small'"
                            class="comment-avatar"></avatar>-->
                    <head-image class="comment-avatar" :url="comment.replyUserAvatar" :id="comment.replyUserId" :name="comment.replyUserNickname" :size="32"/>
                    <span class="username"
                          @click="showUserInfo($event, comment.replyUserId)">{{ comment.replyUserNickname }}：</span>
                  </div>
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
                      <el-button slot="reference" icon="el-icon-delete-solid" size="mini" type="danger" circle
                                 @click.stop></el-button>
                    </el-popconfirm>
                  </div>
                </div>
              </div>

              <div class="contentInputBox" ref="contentInputBox">
                <div class="">
                  <div ref="textareaRef" contenteditable="true" @input="onInput"
                       @paste="optimizePasteEvent" :data-placeholder="placeholder"
                       class="comment-textarea"></div>
                  <span class="point biaoqing-point" @click="showEmoji = !showEmoji">
                      <i class="icon iconfont icon-biaoqing"></i>
                  </span>
                  <a class="sendBtn point" @click="sayComment(item)">发送</a>
                </div>
                <div class="emoji-wrapper" v-if="showEmoji">
                  <Emoji @chooseEmoji="handleChooseEmoji"/>
                </div>
              </div>
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
      <span class="character-item" v-on:click="openCharacterChooseDialog">
        <el-avatar fit="fit" size="medium" icon="el-icon-user-solid" :src="commentSetForm.avatar">
        </el-avatar>
      </span>
          <span class="nick-name">{{ commentSetForm.nickName }} <el-button @click="removeCharacter" class="del-btn"
                                                                           v-if="commentSetForm.commentCharacterId"
                                                                           type="danger" icon="el-icon-delete"
                                                                           size="mini" circle></el-button></span>
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
  </div>
</template>

<script>
import AddTalk from "@/components/talk/AddTalk";
import HeadImage from "@/components/common/HeadImage";
import BatchFileUpload from "@/components/common/BatchFileUpload";
import Avatar from "@/components/common/Avatar";
import Pagination from "@/components/pagination/Pagination";
import TemplateCharacterChoose from "@/components/template/TemplateCharacterChoose";
import Emoji from '@/components/emoji'
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
        commentCharacterId: null,
        nickName: '',
        avatar: '',
        anonymous: false
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
    }
  },
  created() {
    if(this.section === 'my-friends') {
      this.pageQueryTalkList();
    }
  },
  computed: {},
  methods: {
    onInput(e) {
      let selection = window.getSelection()
      this.lastEditRange = selection.getRangeAt(0);
    },
    optimizePasteEvent(e) {
      // 监听粘贴内容到输入框事件，对内容进行处理 处理掉复制的样式标签，只拿取文本部分
      e.stopPropagation()
      e.preventDefault()
      let text = '', event = (e.originalEvent || e)
      if (event.clipboardData && event.clipboardData.getData) {
        text = event.clipboardData.getData('text/plain')
      } else if (window.clipboardData && window.clipboardData.getData) {
        text = window.clipboardData.getData('text')
      }
      if (document.queryCommandSupported('insertText')) {
        document.execCommand('insertText', false, text)
      } else {
        document.execCommand('paste', false, text)
      }
    },
    sayComment(talk) {
      let el = this.$refs.textareaRef[this.commentLastIndex];
      //let el = document.getElementById("textarea")
      if (!el.innerHTML) {
        this.$message.warning("请输入评论内容");
        return
      }
      let sendText = this.createSendText();
      if (!sendText.trim()) {
        return
      }
      this.comment.content = sendText
      let params = {
        talkId: talk.id,
        content: this.comment.content,
        userNickname: talk.commentCharacterName,
        characterId: talk.commentCharacterId,
        userAvatar: talk.commentCharacterAvatar,
        anonymous: talk.commentAnonymous ? talk.commentAnonymous : false,
        replyCommentId: this.comment.replyCommentId
      }
      this.$http({
        url: "/talk/addTalkComment",
        method: 'post',
        data: params
      }).then((data) => {
        if (data.characterId) {
          talk.commentCharacterId = data.characterId;
          talk.commentCharacterName = data.userNickname;
          talk.commentCharacterAvatar = data.userAvatar;
        }
        talk.commentAnonymous = data.anonymous;
        talk.talkCommentVOS.push(data);
        this.$message.success("评论成功");
        this.comment = {}
        this.$refs.contentInputBox[this.commentLastIndex].style.display = "none"
        this.showCommentBox = false
        el.innerHTML = '';
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
        avatar: talk.commentCharacterAvatar,
        anonymous: talk.commentAnonymous ? talk.commentAnonymous : false
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
        }
        talk.commentAnonymous = data.anonymous;
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
      this.commentSetForm.anonymous = talk.commentAnonymous;
      this.curTalk = talk;
      this.curTalkIndex = index;
      this.commentSetVisible = true;
    },
    commentSetDialogClose() {
      this.commentSetVisible = false;
      this.commentSetForm.nickName = '';
      this.commentSetForm.avatar = '';
      this.commentSetForm.commentCharacterId = null;
      this.commentSetForm.anonymous = false;
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
      this.commentSetForm.anonymous = false;
    },
    closeChooseCharacterDialog() {
      this.chooseCharacterDialogVisible = false;
    },
    confirmChooseCharacter(resultData) {
      this.commentSetForm.nickName = resultData.templateCharacter.name;
      this.commentSetForm.avatar = resultData.templateCharacter.avatar;
      this.commentSetForm.commentCharacterId = resultData.templateCharacter.id;
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
      this.talkList[this.curTalkIndex].commentAnonymous = this.commentSetForm.anonymous;
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
        this.$refs.contentInputBox[this.commentLastIndex].style.display = "none"
      }
      if (this.commentLastIndex == index) {
        if (this.$refs.contentInputBox[index].style.display == "block") {
          this.$refs.contentInputBox[index].style.display = "none"
        } else {
          this.$refs.contentInputBox[index].style.display = "block"
        }
      } else {
        this.$refs.contentInputBox[index].style.display = "block"
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
    //添加表情
    handleChooseEmoji(emoText) {
      // 创建一个img标签（表情）
      let img = document.createElement('img');
      img.src = this.$emo.textToUrl(emoText);
      img.style.verticalAlign = 'middle';
      img.style.marginLeft = "2px"
      img.style.marginRight = "2px"
      img.style.height = "25px"
      img.style.width = "25px"
      img.dataset.code = emoText;

      let edit = this.$refs.textareaRef[this.commentLastIndex];
      edit.focus()
      let selection = window.getSelection()
      // 如果存在最后的光标对象
      if (this.lastEditRange) {
        // 选区对象清除所有光标
        selection.removeAllRanges();
        // 并添加最后记录的光标，以还原之前的状态
        selection.addRange(this.lastEditRange);
        // 获取到最后选择的位置
        let range = selection.getRangeAt(0);
        // 在此位置插入表情图
        range.insertNode(img)
        // false，表示将Range对象所代表的区域的起点移动到终点处
        range.collapse(false)

        // 记录最后的位置
        this.lastEditRange = selection.getRangeAt(0);
      } else {
        // 将表情添加到可编辑的div中，作为可编辑div的子节点
        edit.appendChild(img)
        // 使用选取对象，选取可编辑div中的所有子节点
        selection.selectAllChildren(edit)
        // 合并到最后面，即实现了添加一个表情后，把光标移到最后面
        selection.collapseToEnd()
      }
      this.showEmoji = false
    },
    createSendText() {
      let sendText = ""
      this.$refs.textareaRef[this.commentLastIndex].childNodes.forEach((node) => {
        if (node.nodeName == "#text") {
          sendText += this.html2Escape(node.textContent);
        } else if (node.nodeName == "SPAN") {
          sendText += node.innerText;
        } else if (node.nodeName == "IMG") {
          sendText += node.dataset.code;
        }
      })
      return sendText;
    },
    html2Escape(strHtml) {
      return strHtml.replace(/[<>&"]/g, function (c) {
        return {
          '<': '&lt;',
          '>': '&gt;',
          '&': '&amp;',
          '"': '&quot;'
        }[c];
      });
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

<style lang="scss">
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

          /*svg {
            width: 18px;
            height: 18px;
            vertical-align: -3px;
          }*/
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
            bottom: 0px;
            display: inline-block;
            background-color: #6CC6CB;
            padding: 0 5px;
            cursor: pointer;
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
            text-align: left;
            padding-left: 10px;

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

              span {

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

            .contentInputBox {
              width: 50%;
              border: 1px solid #67C23A;
              border-radius: 5px;
              background-color: #fff;
              position: relative;
              min-height: 100px;
              display: none;
              margin-left: 10px;

              .comment-textarea {
                text-align: left;
                min-height: 100px;
                outline: none;
                padding-left: 10px;
                padding-top: 5px;

                &:empty:before {
                  content: attr(data-placeholder);
                  color: #666;
                }
              }

              .biaoqing-point {
                cursor: pointer;
                display: inline-block;
              }

              i {
                font-size: 1.3rem;
                position: absolute;
                right: 80px;
                bottom: 12px;
              }

              .sendBtn {
                cursor: pointer;
                display: inline-block;
                background-color: #67C23A;
                color: #fff;
                padding: 5px 8px 5px 8px;
                border-radius: 5px;
                position: absolute;
                right: 20px;
                bottom: 5px;
              }
            }
          }
        }
      }
    }
  }
}

</style>