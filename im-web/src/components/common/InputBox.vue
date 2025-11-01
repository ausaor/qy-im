<template>
  <div class="input-box" v-show="show" :style="{width: width}">
    <div class="content-input-box">
      <div ref="textareaRef" contenteditable="true" @input="onInput"
           @paste="optimizePasteEvent" :data-placeholder="placeholder"
           class="comment-textarea">
      </div>
    </div>
    <div class="emoji-wrapper" v-show="showType === 'emoji'">
      <Emoji @chooseEmoji="handleChooseEmoji"/>
    </div>
    <div class="character-wrapper" v-show="showType === 'word'">
      <CharacterWord :character-id="characterId" :show="showType === 'word'" @send="sendWord"/>
    </div>
    <div class="btn-group">
      <div class="point ci-point" v-if="characterId" @click="changeShowType('word')">
        <i class="icon iconfont icon-minganci"></i>
      </div>
      <div class="point biaoqing-point" @click="changeShowType('emoji')">
        <i class="icon iconfont icon-biaoqing"></i>
      </div>
      <div class="sendBtn point" @click="send()">发送</div>
    </div>
  </div>
</template>

<script>
  import Emoji from "@components/emoji/index.vue";
  import CharacterWord from "@components/template/CharacterWord.vue";

  export default {
    name: "InputBox",
    components: {
      Emoji,
      CharacterWord
    },
    props: {
      placeholder: {
        type: String,
        default: ''
      },
      width: {
        type: String,
        default: '50%'
      },
      characterId: {
        type: Number,
        default: null
      }
    },
    data() {
      return {
        lastEditRange: null,
        show: false,
        showType: '',
      }
    },
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

        let edit = this.$refs.textareaRef;
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
      },
      createSendText() {
        let sendText = ""
        this.$refs.textareaRef.childNodes.forEach((node) => {
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
      send() {
        let el = this.$refs.textareaRef;
        if (!el.innerHTML) {
          this.$message.warning("请输入评论内容");
          return
        }
        let sendText = this.createSendText();
        if (!sendText.trim()) {
          return
        }
         this.$emit('send', sendText)
        el.innerHTML = '';
      },
      view() {
        this.show = true;
      },
      hide() {
        this.show = false;
      },
      changeShowType(type) {
        if (this.showType === type) {
          this.showType = ''
          return
        }
        this.showType = type
      },
      sendWord(word) {
        console.log(word)
      }
    }
  }
</script>

<style scoped lang="scss">
.input-box {
  border: 1px solid #67C23A;
  border-radius: 5px;
  background-color: #fff;
  position: relative;
  min-height: 100px;
  padding-bottom: 30px;

  .content-input-box {
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
  }

  .btn-group {
    border-top: 1px solid #67C23A;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    position: absolute;
    bottom: 0;
    right: 0;
    gap: 8px;
    height: 30px;
    width: 100%;
    padding: 10px;

    .biaoqing-point {
      cursor: pointer;
      color: #5fb878;
    }

    .ci-point {
      cursor: pointer;
      color: #b89f5f;
    }

    i {
      font-size: 1.3rem;
    }

    .sendBtn {
      cursor: pointer;
      background-color: #67C23A;
      color: #fff;
      padding: 2px 3px;
      border-radius: 5px;
      font-size: 0.8rem;
    }
  }
}
</style>