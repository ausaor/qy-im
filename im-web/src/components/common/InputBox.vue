<template>
  <div class="input-box" v-show="show">
    <div class="content-input-box">
      <div ref="textareaRef" contenteditable="true" @input="onInput"
           @paste="optimizePasteEvent" :data-placeholder="placeholder"
           class="comment-textarea"></div>
      <span class="point biaoqing-point" @click="showEmoji = !showEmoji">
        <i class="icon iconfont icon-biaoqing"></i>
      </span>
      <a class="sendBtn point" @click="send()">发送</a>
    </div>
    <div class="emoji-wrapper" v-if="showEmoji">
      <Emoji @chooseEmoji="handleChooseEmoji"/>
    </div>
  </div>
</template>

<script>
  import Emoji from "@components/emoji/index.vue";

  export default {
    name: "InputBox",
    components: {
      Emoji
    },
    props: {
      placeholder: {
        type: String,
        default: ''
      },
    },
    data() {
      return {
        showEmoji: false,
        lastEditRange: null,
        show: false,
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
        this.showEmoji = false
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
      },
      view() {
        this.show = true;
      },
      hide() {
        this.show = false;
      },
    }
  }
</script>

<style scoped lang="scss">
.input-box {
  width: 50%;
  border: 1px solid #67C23A;
  border-radius: 5px;
  background-color: #fff;
  position: relative;
  min-height: 100px;
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
</style>