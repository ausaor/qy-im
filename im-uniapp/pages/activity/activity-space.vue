
<template>
  <view class="page-box">
    <!-- 顶部导航栏 -->
    <view class="top-nav-bar" :style="{ backgroundColor: headerBgColor }">
      <uni-icons class="back-icon cursor-pointer item-left" type="back" size="24" :color="headerColor" @click="goBack"/>
      <text class="item-center" :style="{opacity: fontOpacity}">{{spaceTitle}}</text>
      <view class="item-right">
        <uni-icons v-show="showAdd" class="camera-icon cursor-pointer" type="camera" size="24" :color="headerColor" @click="openCamera"/>
      </view>
    </view>

    <!-- 内容区域 -->
    <scroll-view class="content-area"
                 scroll-y
                 @scroll="handleScroll"
                 @scrolltolower="loadMore">
      <!-- 固定背景图 -->
      <view class="fixed-bg">
        <image class="bg-image" src="@/static/image/activity-bg.jpg" mode="aspectFill"/>
        <head-image class="my-head-image" :url="mine.headImage" :name="mine.nickName" size="small"></head-image>
        <uni-icons class="refresh-btn" type="refresh" color="white" size="24" @click="refreshTalkList"></uni-icons>
      </view>

      <!-- 动态列表 -->
      <view class="moments-list">
        <view v-for="(item, index) in talkList" :key="index" class="moment-item">
          <view class="user-info">
            <head-image class="avatar" :url="item.avatar" :name="item.nickName" :id="item.userId" size="small"></head-image>
            <view class="info-right">
              <text class="nickname">{{item.nickName}}</text>
              <text class="time">{{item.createTime}}</text>
            </view>
            <view class="more-action cursor-pointer" @click.stop="moreAction(item)">
              <uni-icons type="more-filled" size="30"></uni-icons>
            </view>
<!--            <text class="delete-btn cursor-pointer" @click="deleteMoment(index)" v-if="item.isOwner">删除</text>-->
          </view>

          <view class="content">
            <view class="text">
              <up-parse class="rich-text" :showImgMenu="false" :content="nodesText(item.content)"></up-parse>
            </view>

            <view class="image-grid" v-if="item.fileList && item.fileList.length" v-show="!item.showType || item.showType!=='swiper'">
              <view v-for="(fileItem, fileIndex) in item.fileList"
                    :key="fileIndex" class="grid-item">
                <image v-if="fileItem.fileType === 1"
                       :src="fileItem.url"
                       class="content-image cursor-pointer"
                       mode="aspectFill"
                       @click="previewImage([fileItem.url], 0)"
                />
                <video v-else-if="fileItem.fileType === 2" :src="fileItem.url" :poster="fileItem.coverUrl" class="content-video" :controls="true"></video>
                <view class="content-audio" v-else-if="fileItem.fileType === 3" @click.stop="toggleAudio(fileItem)">
                  <svg-icon v-show="!fileItem.isPlaying" :icon-class="'yinpinbofang'" style="width: 220rpx;height: 220rpx;" :class-name="'yinpinColor'"></svg-icon>
                  <view class="rc-wave" v-show="fileItem.isPlaying">
                    <text class="note" style="--d: 0"></text>
                    <text class="note" style="--d: 1"></text>
                    <text class="note" style="--d: 2"></text>
                    <text class="note" style="--d: 3"></text>
                    <text class="note" style="--d: 4"></text>
                    <text class="note" style="--d: 5"></text>
                    <text class="note" style="--d: 6"></text>
                    <text class="note" style="--d: 7"></text>
                    <text class="note" style="--d: 8"></text>
                    <text class="note" style="--d: 9"></text>
                  </view>
                </view>
              </view>
            </view>

            <!-- 媒体内容区域 -->
            <view class="media-container" v-show="item.showType==='swiper'">
              <swiper v-if="item.fileList && item.fileList.length > 0" class="media-swiper" :indicator-dots="true">
                <swiper-item v-for="(media, mediaIndex) in item.fileList" :key="mediaIndex" class="media-item">
                  <image v-if="media.fileType === 1" :src="media.url" mode="aspectFill" class="media-content cursor-pointer" @click="previewImage([media.url], 0)" />
                  <video v-else-if="media.fileType === 2" :src="media.url" :poster="media.coverUrl" class="media-content" :controls="true" />
                  <view v-else-if="media.fileType === 3" class="media-content audio-player" @click.stop="toggleAudio(media)">
                    <svg-icon v-show="!media.isPlaying" :icon-class="'yinpinbofang'" style="width: 220rpx;height: 220rpx;" :class-name="'yinpinColor'"></svg-icon>
                    <view class="rc-wave" v-show="media.isPlaying">
                      <text class="note" style="--d: 0"></text>
                      <text class="note" style="--d: 1"></text>
                      <text class="note" style="--d: 2"></text>
                      <text class="note" style="--d: 3"></text>
                      <text class="note" style="--d: 4"></text>
                      <text class="note" style="--d: 5"></text>
                      <text class="note" style="--d: 6"></text>
                      <text class="note" style="--d: 7"></text>
                      <text class="note" style="--d: 8"></text>
                      <text class="note" style="--d: 9"></text>
                    </view>
                  </view>
                </swiper-item>
              </swiper>
            </view>
          </view>

          <view class="interaction">
            <view style="display: flex; align-items: center; justify-content: space-between;">
              <view class="location">
                <uni-icons  type="location" size="20"/>
                <text>{{item.address}}</text>
              </view>
              <view class="like-comment">
                <view class="action-btn cursor-pointer" @click="likeAction(item)">
                  <uni-icons :type="item.isLike ? 'hand-up-filled' : 'hand-up'" size="20" :color="item.isLike ? '#FFA500' : '#666'"/>
                  <text :class="['count', item.isLike ? 'liked' : '']">{{item.talkStarVOS.length}}</text>
                </view>
                <view class="action-btn cursor-pointer" @click="doComment(null, item)">
                  <uni-icons type="chat" size="20" color="#666"/>
                  <text class="count">{{item.talkCommentVOS.length}}</text>
                </view>
                <view class="action-btn cursor-pointer" @click.stop="toggleShowType(item)">
                  <uni-icons v-show="!item.showType || item.showType!=='swiper'" custom-prefix="iconfont" type="icon-grid" size="20" color="#666"/>
                  <uni-icons v-show="item.showType ==='swiper'" custom-prefix="iconfont" type="icon-swiper" size="20" color="#666"/>
                </view>
              </view>
            </view>

            <view class="star-user">
              <uni-icons v-if="item.talkStarVOS && item.talkStarVOS.length > 0" type="hand-up-filled" size="20" :color="'#666'"/>
              <text v-for="(user, user_index) in item.talkStarVOS" :key="user_index">
                {{ user.nickname }}
                {{user_index < item.talkStarVOS.length - 1 ? '、' : ''}}
              </text>
            </view>

            <view class="comments" v-if="item.talkCommentVOS.length">
              <view v-for="(comment, cIndex) in item.talkCommentVOS" :key="cIndex" class="comment">
                <view class="comment-user" @click.stop="showUserInfo(comment.userId)">
                  <text>{{comment.userNickname}}</text>
                </view>
                <view class="reply-user" v-if="comment.replyCommentId" style="margin-left: 10rpx;">
                  <text style="margin-right: 10rpx;color: #3cc51f">回复</text>
                  <text @click.stop="showUserInfo(comment.replyUserId)">{{comment.replyUserNickname}}</text>
                </view>
                <text>：</text>
                <view class="comment-text" @click.stop="doComment(comment, item)">
                  <up-parse class="comment-rich-text" :showImgMenu="false" :content="nodesText(comment.content)"></up-parse>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <uni-load-more :status="loadMoreStatus"/>
    </scroll-view>

    <!-- 评论输入框 -->
    <uni-popup ref="commentPopup" type="bottom" @change="commentPopupChange">
      <view class="comment-input-box">
<!--        <input
            class="input"
            v-model="commentText"
            :placeholder="commentPlaceholder"
            @confirm="submitComment"
        />-->
        <editor id="editor" class="comment-input" ref="myEditor" :placeholder="commentPlaceholder"
                :read-only="isReadOnly" @focus="onEditorFocus" @blur="onEditorBlur" @ready="onEditorReady" @input="onTextInput">
        </editor>
        <uni-icons @click="chooseEmoji" custom-prefix="iconfont" type="icon-icon_emoji" size="32" :color="showEmo ? '#07C160' : '#cccccc'"/>
        <view class="send-btn cursor-pointer" size="mini" @click="submitComment">发送</view>
      </view>
      <scroll-view class="emo-box" scroll-y="true" v-show="showEmo">
        <view class="emotion-item-list">
          <image class="emotion-item emoji-large" :title="emoText" :src="$emo.textToPathOriginal(emoText)"
                 v-for="(emoText, i) in $emo.originalEmoTextList" :key="i" @click="selectEmoji(emoText)" mode="aspectFit"
                 lazy-load="true"></image>
        </view>
      </scroll-view>
    </uni-popup>
    <uni-popup ref="talkSetPopup" type="bottom" background-color="#fff" @change="talkSetPopupChange">
      <view style="font-size: 28rpx;">
        <view style="background-color: white;padding: 20rpx 0;display: flex;justify-content: center;align-items: center;gap: 20rpx;">
          <text>我的角色:</text>
          <head-image :url="commentSetForm.avatar" :name="commentSetForm.nickName" size="mini" @click="showGroupTemplatesPopup"></head-image>
          <text>{{commentSetForm.nickName}}</text>
        </view>
        <view v-show="curTalk.isOwner && category=== curTalk.category" style="background-color: white;padding: 20rpx 0;text-align: center;" @click="doEditTalk">
          编辑
        </view>
        <view v-show="curTalk.isOwner" style="background-color: white;color: red;text-align: center;padding: 20rpx 0;" @click="doDeleteTalk">删除</view>
        <view style="background-color: white;color: grey;text-align: center;padding: 20rpx 0;" @click.stop="closeTalkSetPopup">取消</view>
      </view>
    </uni-popup>
    <uni-popup ref="delCommentPopup" type="bottom">
      <view style="background-color: white;padding: 20rpx 0;font-size: 28rpx;">
        <view style="color: red;text-align: center;margin-bottom: 20rpx;" @click.stop="doDeleteComment">删除</view>
        <view style="background-color: #e2e4ea;height: 10rpx;width: 100%;"></view>
        <view style="color: grey;text-align: center;margin-top: 20rpx;" @click.stop="closeDelCommentPopup">取消</view>
      </view>
    </uni-popup>
    <group-template-list ref="groupTemplateListRef" :group-templates="groupTemplates" @confirm="chooseGroupTemplate"></group-template-list>
    <character-list ref="characterListRef" :characters="characters" @confirm="chooseCharacter"></character-list>
  </view>
</template>

<script>
import HeadImage from "../../components/head-image/head-image.vue";
import SvgIcon from "../../components/svg-icon/svg-icon.vue";
import GroupTemplateList from "../../components/group-template-list/group-template-list.vue";
import CharacterList from "../../components/character-list/character-list.vue";
import {throttle} from "../../common/throttle";

export default {
  components: {CharacterList, GroupTemplateList, SvgIcon, HeadImage},
  data() {
    return {
      showAdd: true,
      spaceTitle: '空间动态',
      headerBgColor: 'rgba(239, 244, 255, 0)', // 初始透明
      headerColor: '#ffffff',
      fontOpacity: 0,
      category: '',
      section: '',
      commentText: '',
      currentMomentIndex: -1,
      loadMoreStatus: 'more',
      friendId: null,
      groupId: null,
      regionGroupId: null,
      regionCode: null,
      page: {
        pageNo: 1,
        pageSize: 10,
        totalPage: 0,
      },
      talkList: [],
      commentPlaceholder: '说点什么...',
      curTalk: {},
      comment: {
        replyUserId: null,
        replyUserNickname: null,
        replyCommentId: null,
        talkId: null,
        content: ""
      },
      commentSetForm: {
        commentCharacterId: null,
        nickName: '',
        avatar: '',
      },
      deleteComment: null,
      showType: 'grid',
      audioPlayState: 'STOP',
      innerAudioContext: null,
      audioContext: null,
      audioUrl: null,
      playingAudio: null,
      groupTemplates: [],
      characters: [],
      isRefreshing: false,
      showEmo: false,
      editorCtx: null, // 编辑器上下文
      isEmpty: true, // 编辑器是否为空
      isFocus: false, // 编辑器是否焦点
      isReadOnly: false, // 编辑器是否只读
    };
  },
  methods: {
    handleScroll: throttle(function(e) {
      //console.log('自定义节流函数:', e.detail.scrollTop);
      const scrollTop = e.detail.scrollTop;

      // 计算透明度（示例：0~200px 滚动范围，透明度从0到0.8）
      let opacity = 0;
       if (scrollTop > 200) {
         this.headerColor = '#000000';
         this.fontOpacity = 1;
        opacity = 1;
      } else if (scrollTop > 100) {
         this.headerColor = '#000000';
         this.fontOpacity = 0.8;
         opacity = 0.6;
      } else {
         this.headerColor = '#ffffff';
         this.fontOpacity = 0;
      }
      this.headerBgColor = `rgba(239, 244, 255, ${opacity})`;
    }, 150),
    selectEmoji(emoText) {
      let path = this.$emo.textToPathOriginal(emoText)
      // 先把键盘禁用了，否则会重新弹出键盘
      this.isReadOnly = true;
      this.isEmpty = false;
      this.$nextTick(() => {
        this.editorCtx.insertImage({
          src: path,
          alt: emoText,
          extClass: 'emoji-small',
          nowrap: true,
          complete: () => {
            this.isReadOnly = false;
            this.editorCtx.blur();
          }
        });
      })
    },
    onTextInput(e) {
      //const content = e.detail.html; // 获取富文本 HTML
      //const text = e.detail.text;    // 获取纯文本
      //this.form.content = content;
      this.isEmpty = e.detail.html == '<p><br></p>'
    },
    onEditorReady() {
      console.log("编辑器已初始化")
      const query = uni.createSelectorQuery().in(this);
      query.select('#editor').context((res) => {
        this.editorCtx = res.context
      }).exec()
    },
    onEditorFocus(e) {
      this.isFocus = true;
      this.showEmo = false;
    },
    onEditorBlur(e) {
      this.isFocus = false;
    },
    goBack() {
      uni.navigateBack();
    },
    openCamera() {
      // uni.chooseImage({
      //   count: 9,
      //   success: (res) => {
      //     console.log(res.tempFilePaths);
      //   }
      // });
      let url = `/pages/activity/activity-add?category=${this.category}`;
      if (this.groupId) {
        url += `&groupId=${this.groupId}`;
      }
      uni.navigateTo({
        url: url
      })
    },
    doEditTalk() {
      let talkId = this.curTalk.id;
      this.$refs.talkSetPopup.close();
      let url = `/pages/activity/activity-add?category=${this.category}&talkId=${talkId}`;
      if (this.groupId) {
        url += `&groupId=${this.groupId}`;
      }
      uni.navigateTo({
        url: url
      })
    },
    previewImage(images, current) {
      uni.previewImage({
        urls: images,
        current: images[current]
      });
    },
    toggleAudio(media) {
      if (this.audioUrl && this.audioUrl !== media.url && this.audioPlayState === "PLAYING") {
        uni.showToast({
          title: "请先暂停已播放音频",
          icon: 'none'
        });
        return;
      }

      // 初始化音频播放器
      if (!this.innerAudioContext ||  this.audioUrl !== media.url) {
        this.innerAudioContext = uni.createInnerAudioContext();
        let url = media.url;
        this.innerAudioContext.src = url;
        this.innerAudioContext.onEnded((e) => {
          console.log('停止')
          this.audioPlayState = "STOP"
          this.playingAudio.isPlaying = false;
        })
        this.innerAudioContext.onError((e) => {
          this.audioPlayState = "STOP"
          this.playingAudio.isPlaying = false;
          console.log("播放音频出错");
          console.log(e)
        });
      }
      media.isPlaying = !media.isPlaying;
      this.audioUrl = media.url;
      this.playingAudio = media;
      if (this.audioPlayState == 'STOP') {
        this.innerAudioContext.play();
        this.audioPlayState = "PLAYING";
      } else if (this.audioPlayState == 'PLAYING') {
        this.innerAudioContext.pause();
        this.audioPlayState = "PAUSE"
      } else if (this.audioPlayState == 'PAUSE') {
        this.innerAudioContext.play();
        this.audioPlayState = "PLAYING"
      }
    },
    async loadMore() {
      this.loadMoreStatus = 'loading';
      this.page.pageNo++;
      if (this.page.pageNo <= this.page.totalPage) {
        await this.pageQueryTalkList();
        this.loadMoreStatus = 'more';
      } else {
        this.loadMoreStatus = 'noMore';
      }
    },
    async pageQueryTalkList() {
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
    nodesText(content) {
      let color = '';
      let text = this.$url.replaceURLWithHTMLLinks(content, color)
      return this.$emo.transform(text, 'emoji-small').replace(/\n/g, '<br>');
    },
    showUserInfo(userId) {
      if (userId && userId > 0) {
        uni.navigateTo({
          url: "/pages/common/user-info?id=" + userId
        })
      }
    },
    likeAction(talk) {
      if (!talk.isLike) {
        let params = {
          talkId: talk.id,
          nickname: talk.commentCharacterName,
          characterId: talk.commentCharacterId,
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
          }
          talk.talkStarVOS.push(data);
          uni.showToast({
            title: "点赞成功",
            icon: 'none'
          });
        }).finally(() => {

        })
      } else {
        let params = {
          talkId: talk.id
        }
        this.$http({
          url: "/talk/cancelLike",
          method: 'post',
          data: params
        }).then((data) => {
          talk.isLike = false;
          talk.talkStarVOS = talk.talkStarVOS.filter(function (item) {
            return !item.isOwner;
          });
          uni.showToast({
            title: "已取消",
            icon: 'none'
          });
        }).finally(() => {

        })
      }
    },
    moreAction(talk) {
      this.commentSetForm.nickName = talk.commentCharacterName ? talk.commentCharacterName : talk.commentUserNickname;
      this.commentSetForm.avatar = talk.commentCharacterAvatar ? talk.commentCharacterAvatar : talk.commentUserAvatar;
      this.commentSetForm.commentCharacterId = talk.commentCharacterId;
      this.curTalk = talk;
      this.$refs.talkSetPopup.open();
    },
    doComment(comment, talk) {
      if (comment && comment.isOwner) {
        this.$refs.delCommentPopup.open();
        this.deleteComment = comment;
        this.curTalk = talk;
        return;
      }
      if (comment) {
        this.commentPlaceholder = "回复" + comment.userNickname + ":"
        this.comment.replyUserId = comment.userId
        this.comment.replyUserNickname = comment.userNickname
        this.comment.replyCommentId = comment.id
      } else {
        this.comment.replyUserId = null
        this.comment.replyUserNickname = null
        this.comment.replyCommentId = null
      }
      this.curTalk = talk;
      this.comment.talkId = talk.id
      this.$refs.commentPopup.open();
    },
    submitComment() {
      this.editorCtx.getContents({
        success: async (e) => {
          let commentText = "";
          e.delta.ops.forEach((op) => {
            if (op.insert.image) {
              // emo表情
              commentText += `#${op.attributes.alt};`
            } else (
                // 文字
                commentText += op.insert
            )
          })
          if (!commentText.trim()) {
            return uni.showToast({
              title: "不能发送空白信息",
              icon: "none"
            });
          }
          let sendText = commentText;
          if (!sendText.trim()) {
            return
          }
          this.comment.content = sendText
          let talk = this.curTalk;
          let params = {
            talkId: talk.id,
            content: this.comment.content,
            userNickname: talk.commentCharacterName,
            characterId: talk.commentCharacterId,
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
              talk.commentCharacterName = data.userNickname;
              talk.commentCharacterAvatar = data.userAvatar;
            }
            talk.talkCommentVOS.push(data);
            uni.showToast({
              title: "评论成功",
              icon: 'none'
            });
          }).finally(() => {
            this.$refs.commentPopup.close();
            this.comment = {}
            this.commentPlaceholder = '说点什么...';
            this.curTalk = {};
            this.commentText = '';
            this.showEmo = false;
          })
        }
      })
    },
    commentPopupChange(e) {
      if (!e.show) {
        this.commentPlaceholder = "说点什么...";
      }
    },
    doDeleteComment() {
      let talk = this.curTalk;
      let commentId = this.deleteComment.id;
      this.$http({
        url: `/talkComment/delete/${commentId}`,
        method: 'delete'
      }).then((data) => {
        talk.talkCommentVOS = talk.talkCommentVOS.filter(function (item) {
          return item.id !== commentId;
        });
        uni.showToast({
          title: "删除成功",
          icon: 'none'
        });
      }).finally(() => {
        this.curTalk = {};
        this.deleteComment = null;
        this.$refs.delCommentPopup.close();
      })
    },
    closeDelCommentPopup() {
      this.deleteComment = null;
      this.$refs.delCommentPopup.close();
    },
    seek(e) {
      this.audio.seek(e.detail.value);
    },
    toggleShowType(talk) {
      talk.showType = (!talk.showType || talk.showType === 'grid') ? 'swiper' : 'grid';
    },
    talkSetPopupChange(e) {
      if (!e.show) {
        this.commentSetForm.nickName = '';
        this.commentSetForm.avatar = '';
        this.commentSetForm.commentCharacterId = null;
        this.curTalk = {};
      }
    },
    closeTalkSetPopup() {
      this.$refs.talkSetPopup.close();
    },
    doDeleteTalk() {
      uni.showModal({
        title: '提示',
        content: '确定要删除这条动态吗？',
        success: (res) => {
          if (res.confirm) {
            this.$http({
              url: "/talk/delete",
              method: 'delete',
              data: {id: this.curTalk.id}
            }).then((data) => {
              uni.showToast({
                title: "删除成功",
                icon: 'none'
              });
              this.refreshTalkList();
            }).finally(() => {
              this.curTalk = {};
              this.$refs.talkSetPopup.close();
            })
          } else {
            this.curTalk = {};
            this.$refs.talkSetPopup.close();
          }
        }
      });
    },
    onPullDownRefresh() {
      try {
        // 模拟异步请求
        console.log("下拉刷新")
        this.isRefreshing = true;
        this.refreshTalkList();
      } finally {
        setTimeout(() => {
          uni.stopPullDownRefresh();
          this.isRefreshing = false; // 确保状态重置
          console.log("下拉刷新关闭")
        }, 1000)
      }

    },
    refreshTalkList() {
      this.page.pageNo = 1;
      this.page.totalPage = 0;
      this.talkList = [];
      this.pageQueryTalkList();
    },
    showGroupTemplatesPopup() {
      if (!this.groupTemplates || this.groupTemplates.length === 0) {
        this.queryGroupTemplateList();
      }
      this.$refs.groupTemplateListRef.open();
    },
    async queryGroupTemplateList() {
      await this.$http({
        url: "/templateGroup/list",
        method: 'get',
        params: ''
      }).then(data => {
        this.groupTemplates = data;
      })
    },
    chooseGroupTemplate(groupTemplate) {
      this.$refs.groupTemplateListRef.cancel();
      if (groupTemplate) {
        this.queryCharacterList(groupTemplate.id);
        this.$refs.characterListRef.open();
      }
    },
    async queryCharacterList(templateGroupId) {
      await this.$http({
        url: `/templateCharacter/list/${templateGroupId}`,
        method: 'get'
      }).then(result => {
        this.characters = result;
      });
    },
    chooseCharacter(character) {
      this.$refs.characterListRef.cancel();
      this.commentSetForm.commentCharacterId = character.id;
      this.commentSetForm.nickName = character.name;
      this.commentSetForm.avatar = character.avatar;
      this.curTalk.commentCharacterId = character.id;
      this.curTalk.commentCharacterAvatar = character.avatar;
      this.curTalk.commentCharacterName = character.name;
    },
    chooseEmoji() {
      this.showEmo = !this.showEmo;
    },
  },
  computed: {
    mine() {
      return this.userStore.userInfo;
    }
  },
  onLoad(options) {
    console.log(options.category)
    console.log(options.section)
    this.category = options.category;
    this.section = options.section;
    if (this.section === 'friend') {
      this.showAdd = false;
    }
    if (options.groupId) {
      this.groupId = options.groupId;
    }
    if (options.friendId) {
      this.friendId = options.friendId;
    }
    if (options.spaceTitle) {
      this.spaceTitle = options.spaceTitle;
    }
    if (options.regionGroupId) {
      this.regionGroupId = options.regionGroupId;
    }
    if (options.regionCode) {
      this.regionCode = options.regionCode;
    }
    this.pageQueryTalkList();
  },
  onHide() {
    console.log("onHide")
    if (this.playingAudio) {
      this.playingAudio.isPlaying = false;
    }
    if (this.innerAudioContext) {
      this.audioPlayState = 'STOP';
      this.innerAudioContext.pause();
    }
  },
  onUnload() {
    console.log('页面卸载');
    if (this.playingAudio) {
      this.playingAudio.isPlaying = false;
    }
    if (this.innerAudioContext) {
      this.audioUrl = null;
      this.audioPlayState = 'STOP';
      this.innerAudioContext.pause();
      this.innerAudioContext = null;
    }
  },
};
</script>

<style lang="scss" scoped>
.page-box {
  height: 100%;
  background-color: #f8f8f8;
}

.top-nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  background-color: transparent;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30rpx;
  z-index: 100;
  transition: background-color 0.3s; /* 添加过渡效果 */
}

.back-icon, .camera-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.fixed-bg {
  width: 100%;
  height: 400rpx;
  z-index: 2;
  position: relative;
}

.bg-image {
  width: 100%;
  height: 100%;
}

.my-head-image {
  position: absolute;
  left: 35rpx;
  bottom: 10rpx;
  z-index: 5;
}

.refresh-btn {
  position: absolute;
  width: 48rpx;
  height: 48rpx;
  right: 35rpx;
  bottom: 10rpx;
  z-index: 5;
}

.content-area {
  position: relative;
  height: 100vh;
  z-index: 1;
}

.header-space {
  width: 100%;
  height: 400rpx;
}

.moments-list {
  background-color: #ffffff;
  border-radius: 20rpx 20rpx 0 0;
  margin-top: 20rpx;
  position: relative;
}

.moment-item {
  padding: 30rpx;
  border-bottom: 2rpx solid #f5f5f5;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.info-right {
  flex: 1;
  margin-left: 20rpx;
}

.nickname {
  font-size: 30rpx;
  color: #333333;
  font-weight: 500;
}

.time {
  font-size: 24rpx;
  color: #999999;
  display: block;
  margin-top: 4rpx;
}

.more-action {
  font-size: 28rpx;
  color: #999999;
}

.delete-btn {
  font-size: 28rpx;
  color: #999999;
}

.content {
  margin-top: 20rpx;
}

.text {
  font-size: 28rpx;
  color: #333333;
  line-height: 1.6;
}

.rich-text {
  word-wrap: break-word;   /* 长单词或URL换行 */
  white-space: pre-wrap;   /* 保留空白符但允许换行 */
  overflow-wrap: break-word; /* 类似word-wrap */
  word-break: break-all;   /* 更激进的中英文换行 */
  display: flex;
  align-items: center;
}

/* 使用 /deep/ 或 ::v-deep 穿透组件作用域 */
::v-deep .up-parse-inner-element {
  word-wrap: break-word;
  white-space: pre-wrap;
  display: flex;
  align-items: center;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 5rpx;

  /* 默认三列布局 (3,5,6,7,8,9 张) */
  .grid-item {
    flex: 1 1 32.5%;
    max-width: 32.5%;
    height: 220rpx;
    box-sizing: border-box;
  }

  /* 单张图片占满宽度 */
  .grid-item:only-child {
    flex: 0 0 100%;
    max-width: 100%;
    height: 440rpx;
  }

  /* 两张图片时各占 50% */
  .grid-item:nth-child(1):nth-last-child(2),
  .grid-item:nth-child(2):nth-last-child(1) {
    flex: 0 0 49%;
    max-width: 49%;
  }

  /* 四张图片时各占 50% */
  .grid-item:nth-child(1):nth-last-child(4),
  .grid-item:nth-child(2):nth-last-child(3),
  .grid-item:nth-child(3):nth-last-child(2),
  .grid-item:nth-child(4):nth-last-child(1) {
    flex: 0 0 49%;
    max-width: 49%;
  }
}

.content-image {
  width: 100%;
  height: 100%;
  border-radius: 8rpx;
  object-fit: cover;
}

.content-video {
  width: 100%;
  height: 100%;
  border-radius: 8rpx;
  object-fit: cover;
}

.content-audio {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 8rpx;
}

.rc-wave {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  position: relative;
  height: 220rpx;
  width: 220rpx;

  .note {
    background: linear-gradient(to top, $im-color-primary-light-1 0%, $im-color-primary-light-9 100%);
    width: 4px;
    height: 90%;
    border-radius: 5rpx;
    margin-right: 4px;
    animation: loading 0.5s infinite linear;
    animation-delay: calc(0.1s * var(--d));

    @keyframes loading {
      0% {
        background-image: linear-gradient(to right, $im-color-primary-light-1 0%, $im-color-primary-light-9 100%);
        height: 20%;
        border-radius: 5rpx;
      }

      50% {
        background-image: linear-gradient(to top, $im-color-primary-light-1 0%, $im-color-primary-light-9 100%);
        height: 90%;
        border-radius: 5rpx;
      }

      100% {
        background-image: linear-gradient(to top, $im-color-primary-light-1 0%, $im-color-primary-light-9 100%);
        height: 20%;
        border-radius: 5rpx;
      }
    }
  }
}

.yinpinColor {
  color: #3cc51f;
}

.media-container {
  margin-bottom: 20rpx;
}

.media-swiper {
  height: 400rpx;
  border-radius: 8rpx;
  overflow: hidden;
  object-fit: cover;
}

.media-item {
  width: 100%;
  height: 100%;
}

.media-content {
  width: 100%;
  height: 100%;
}

.audio-player {
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: white;
  padding: 20rpx;
  border-radius: 8rpx;
}

.audio-cover {
  width: 100rpx;
  height: 100rpx;
  border-radius: 8rpx;
  margin-right: 20rpx;
}

.audio-controls {
  flex: 1;
  display: flex;
  align-items: center;
}

.audio-progress {
  flex: 1;
  height: 4rpx;
  background-color: #e0e0e0;
  margin: 0 20rpx;
}

.progress-bar {
  height: 100%;
  background-color: #576b95;
}

.audio-time {
  font-size: 12px;
  color: #999999;
}

.interaction {
  margin-top: 20rpx;
}

.location {
  display: flex;
  align-items: center;
}

.star-user {
  margin-top: 10rpx;
  display: flex;
  align-items: center;
}

.like-comment {
  display: flex;
  justify-content: right;
  gap: 40rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.count {
  font-size: 28rpx;
  color: #666666;
}

.liked {
  color: #ff5d5d;
}

.comments {
  margin-top: 20rpx;
  background-color: #f8f8f8;
  padding: 20rpx;
  border-radius: 8rpx;
}

.comment {
  margin-bottom: 12rpx;
  display: flex;
  align-items: flex-end;
  height: 38rpx;
}

.comment-user {
  font-size: 28rpx;
  color: #576b95;
  font-weight: 500;
  height: 38rpx;
}

.reply-user {
  font-size: 28rpx;
  color: #576b95;
  font-weight: 500;
  height: 38rpx;
}

.comment-text {
  flex: 1;
  font-size: 28rpx;
  color: #333333;
  display: flex;
  align-items: flex-end;
  height: 38rpx;
}

.comment-rich-text {
  display: flex;
  align-items: flex-end;
}

.comment-input-box {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background-color: #ffffff;
  border-top: 2rpx solid #eeeeee;
  position: relative;
}

.input {
  flex: 1;
  height: 72rpx;
  background-color: #f8f8f8;
  border-radius: 36rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
}

.comment-input {
  width: 100%;
  height: 100%;
  min-height: 60rpx;
  max-height: 200rpx;
  font-size: 30rpx;
  background-color: #f8f8f8;
  border: 1rpx solid #eeeeee;
  border-radius: 30rpx;
}

.send-btn {
  margin-left: 20rpx;
  width: 120rpx;
  height: 60rpx;
  line-height: 60rpx;
  text-align: center;
  background-color: #07c160;
  color: #ffffff;
  border-radius: 36rpx;
}

.emo-box {
  background-color: white;
  padding: 20rpx;
  height: 310px;
  box-sizing: border-box;

  .emotion-item-list {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    align-content: center;

    .emotion-item {
      text-align: center;
      cursor: pointer;
      padding: 5px;
    }
  }
}

.cursor-pointer {
  cursor: pointer;
}
</style>

