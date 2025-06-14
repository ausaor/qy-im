<template>
  <uni-popup ref="commentPopup" type="bottom">
    <view class="comment-input-box">
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
</template>

<script>
export default {
  name: "comment-box",
  props: {
    commentPlaceholder: {
      type: String,
      default: "请输入评论..."
    }
  },
  data() {
    return {
      showEmo: false,
      commentText: "",
      isReadOnly: false
    }
  },
  methods: {
    open() {
      this.$refs.commentPopup.open();
    },
    cancel() {
      this.$refs.commentPopup.close();
    },
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
    chooseEmoji() {
      this.showEmo = !this.showEmo;
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

          this.$emit("submit", sendText);
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.comment-input-box {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background-color: #ffffff;
  border-top: 2rpx solid #eeeeee;
  position: relative;
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