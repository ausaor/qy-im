<template>
  <uni-popup ref="commentPopup" type="bottom">
    <view class="comment-input-box">
      <editor id="editor" class="comment-input" ref="myEditor" :placeholder="commentPlaceholder"
              :read-only="isReadOnly" @focus="onEditorFocus" @blur="onEditorBlur" @ready="onEditorReady" @input="onTextInput">
      </editor>
      <view class="send-btn cursor-pointer" size="mini" @click="submitComment">发送</view>
      <uni-icons v-if="chatTabBox === 'none'" type="plus" size="40" @click="toggleToTools" />
      <uni-icons v-else type="close" size="40" @click="toggleToNone" />
    </view>
    <view class="comment-tools-box">
      <view v-if="chatTabBox == 'tools'" class="chat-tools">
        <view class="chat-tools-item">
          <image-upload :maxCount="9" sourceType="album" :onBefore="onUploadImageBefore"
                        :onSuccess="onUploadImageSuccess" :onError="onUploadImageFail">
            <svg-icon class="tool-icon" icon-class="tupian"></svg-icon>
          </image-upload>
          <view class="tool-name">图片</view>
        </view>
        <view class="chat-tools-item" @click="toggleToEmo">
          <svg-icon class="tool-icon" icon-class="fabiaoqing"></svg-icon>
          <view class="tool-name">表情</view>
        </view>
        <view class="chat-tools-item" v-if="characterId">
          <svg-icon class="tool-icon" icon-class="minganci" @click="toggleToCharacterWord"></svg-icon>
          <view class="tool-name">角色台词</view>
        </view>
        <view class="chat-tools-item" v-if="characterId">
          <svg-icon class="tool-icon" icon-class="biaoqing" @click="toggleToCharacterEmo"></svg-icon>
          <view class="tool-name">角色表情</view>
        </view>
      </view>
      <scroll-view class="emo-box" scroll-y="true" v-if="chatTabBox === 'emo'">
        <view class="emotion-item-list">
          <image class="emotion-item emoji-large" :title="emoText" :src="$emo.textToPathOriginal(emoText)"
                 v-for="(emoText, i) in $emo.originalEmoTextList" :key="i" @click="selectEmoji(emoText)" mode="aspectFit"
                 lazy-load="true"></image>
        </view>
      </scroll-view>
    </view>
    <character-word-list ref="characterWordList" :words="words" @confirm="chooseCharacterWord"></character-word-list>
    <character-emo-list ref="characterEmoList" :emos="emos" @confirm="chooseCharacterEmo"></character-emo-list>
  </uni-popup>
</template>

<script>
import SvgIcon from "../svg-icon/svg-icon.vue";
import characterWordList from "../character-word-list/character-word-list.vue";
import characterEmoList from "../character-emo-list/character-emo-list.vue";

export default {
  name: "comment-box",
  components: {characterEmoList, characterWordList, SvgIcon},
  props: {
    commentPlaceholder: {
      type: String,
      default: "请输入评论..."
    },
    characterId: {
      type: Number,
      default: null
    }
  },
  data() {
    return {
      showEmo: false,
      commentText: "",
      isReadOnly: false,
      chatTabBox: 'none',
      words: {
        group: [],
        character: []
      },
      emos: {
        group: [],
        character: []
      }
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
          this.chatTabBox = 'none';
        }
      })
    },
    onUploadImageBefore(file) {

    },
    onUploadImageSuccess(file, res) {

    },
    onUploadImageFail(file, err) {

    },
    toggleToTools() {
      if (this.chatTabBox === 'tools') {
        this.switchChatTabBox('none')
      } else {
        this.switchChatTabBox('tools')
      }
    },
    toggleToNone() {
      this.switchChatTabBox('none')
    },
    toggleToEmo() {
      if (this.chatTabBox === 'emo') {
        this.switchChatTabBox('none')
      } else {
        this.switchChatTabBox('emo')
      }
    },
    switchChatTabBox(chatTabBox) {
      this.chatTabBox = chatTabBox;
    },
    chooseCharacterWord(data) {
      this.$emit("sendWord", data);
    },
    chooseCharacterEmo(data) {
      this.$emit("sendEmo", data);
    },
    toggleToCharacterWord() {
      this.queryCharacterWord(this.characterId).then((data) => {
        if (data.group.length === 0 && data.character.length === 0) {
          uni.showToast({
            title: "该角色未配置语音台词",
            icon: "none"
          })
        } else {
          this.words.group = data.group;
          this.words.character = data.character;
          this.$refs.characterWordList.open();
        }
      })
    },
    toggleToCharacterEmo() {
      this.queryCharacterEmo(this.characterId).then((data) => {
        if (data.group.length === 0 && data.character.length === 0) {
          uni.showToast({
            title: "该角色未配置表情",
            icon: "none"
          })
        } else {
          this.emos.group = data.group;
          this.emos.character = data.character;
          this.$refs.characterEmoList.open();
        }
      })
    },
    queryCharacterWord(characterId) {
      return new Promise((resolve, reject) => {
        this.$http({
          url: `/character/word/publishedWord?characterId=${characterId}`,
          method: "get",
        }).then((data) => {
          resolve(data)
        })
      });
    },
    queryCharacterEmo(characterId) {
      return new Promise((resolve, reject) => {
        this.$http({
          url: `/character/emo/publishedEmo?characterId=${characterId}`,
          method: "get",
        }).then((data) => {
          resolve(data)
        })
      });
    },
  }
}
</script>

<style scoped lang="scss">
$icon-color: rgba(0, 0, 0, 0.88);
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

.comment-tools-box {
  background-color: $im-bg;

  .chat-tools {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    grid-gap: 20rpx;
    align-items: center;
    padding: 40rpx;
    box-sizing: border-box;
  }

  .chat-tools-item {
    padding: 16rpx;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: center;

    .tool-icon {
      padding: 26rpx;
      font-size: 54rpx;
      border-radius: 20%;
      background-color: white;
      color: $icon-color;

      &:active {
        background-color: $im-bg-active;
      }
    }

    .tool-name {
      height: 60rpx;
      line-height: 60rpx;
      font-size: 28rpx;
    }
  }
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