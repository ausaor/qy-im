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
    <div class="character-wrapper" v-show="showType === 'c-emo'">
      <CharacterEmo :character-id="characterId" :show="showType === 'c-emo'" @choose="chooseCharacterEmoji"/>
    </div>
    <div class="btn-group">
      <div class="point c-emo-point" v-if="characterId" @click="changeShowType('c-emo')">
        <i class="icon iconfont icon-biaoqing"></i>
      </div>
      <div class="point ci-point" v-if="characterId" @click="changeShowType('word')">
        <i class="icon iconfont icon-minganci"></i>
      </div>
      <div class="point biaoqing-point" @click="changeShowType('emoji')">
        <i class="icon iconfont icon-biaoqing"></i>
      </div>
      <div class="point img-point">
        <file-upload :action="'/image/upload'" :maxSize="5*1024*1024" :fileTypes="['image/jpeg', 'image/png', 'image/jpg', 'image/webp','image/gif']"
                     @before="onImageBefore" @success="onImageSuccess" @fail="onImageFail">
          <i class="icon iconfont icon-tupian"></i>
        </file-upload>
      </div>
      <div class="sendBtn point" @click="send()">发送</div>
    </div>
  </div>
</template>

<script>
  import Emoji from "@components/emoji/index.vue";
  import CharacterWord from "@components/template/CharacterWord.vue";
  import FileUpload from "@components/common/FileUpload.vue";
  import CharacterEmo from "@components/template/CharacterEmo.vue";

  export default {
    name: "InputBox",
    components: {
      FileUpload,
      Emoji,
      CharacterWord,
      CharacterEmo
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
        characterEmo: null,
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
        img.dataset.imgType = 'emoji';

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
        // 检查是否包含图片
        let hasImage = false;
        let imageUrls = [];
        let sendText = ""
        this.$refs.textareaRef.childNodes.forEach((node) => {
          if (node.nodeName == "#text") {
            sendText += this.html2Escape(node.textContent);
          } else if (node.nodeName == "SPAN") {
            sendText += node.innerText;
          } else if (node.nodeName == "IMG" && node.dataset.imgType==="emoji") {
            sendText += node.dataset.code;
          } else if (node.nodeName == "IMG" && node.dataset.imgType === "common") {
            hasImage = true;
            imageUrls.unshift({originUrl: node.dataset.url});
          } else if (node.nodeName == "IMG" && node.dataset.imgType === "c-emo") {
            hasImage = true;
            imageUrls.unshift({
              originUrl: node.dataset.url,
              id: this.characterEmo.id,
              templateGroupId: this.characterEmo.templateGroupId,
              characterId: this.characterEmo.characterId,
              characterName: this.characterEmo.characterName,
              name: this.characterEmo.name,
            });
          }
        })
        let sendObj = {
          content: hasImage ? imageUrls[0] : sendText,
          type: hasImage ? 1 : 0, // 0 文本，1图片
        }
        return sendObj;
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
        
        let sendObj = this.createSendText();

        if (!sendObj.content) {
          return
        }
        
        this.$emit('send', sendObj)
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
        this.$emit('sendWord', word)
      },
      onImageBefore() {
      },
      onImageSuccess(data, file) {
        // 先清空输入框
        let edit = this.$refs.textareaRef;
        edit.innerHTML = '';
        // 创建图片元素并插入到输入框
        let img = document.createElement('img');
        img.src = data.originUrl;
        img.style.maxWidth = "200px";
        img.style.maxHeight = "200px";
        img.dataset.url = data.originUrl;
        img.dataset.imgType = "common";
        

        edit.focus();
        let selection = window.getSelection();
        
        // 如果存在最后的光标对象
        if (this.lastEditRange) {
          // 选区对象清除所有光标
          selection.removeAllRanges();
          // 并添加最后记录的光标，以还原之前的状态
          selection.addRange(this.lastEditRange);
          // 获取到最后选择的位置
          let range = selection.getRangeAt(0);
          // 在此位置插入图片
          range.insertNode(img);
          // false，表示将Range对象所代表的区域的起点移动到终点处
          range.collapse(false);
          
          // 记录最后的位置
          this.lastEditRange = selection.getRangeAt(0);
        } else {
          // 将图片添加到可编辑的div中，作为可编辑div的子节点
          edit.appendChild(img);
          // 使用选取对象，选取可编辑div中的所有子节点
          selection.selectAllChildren(edit);
          // 合并到最后面，即实现了添加一个图片后，把光标移到最后面
          selection.collapseToEnd();
        }
      },
      onImageFail() {
        this.$message.error("图片上传失败")
      },
      chooseCharacterEmoji(emo) {
        this.characterEmo = emo;
        let edit = this.$refs.textareaRef;
        edit.innerHTML = '';
        // 创建图片元素并插入到输入框
        let img = document.createElement('img');
        img.src = emo.url;
        img.style.maxWidth = "150px";
        img.style.maxHeight = "150px";
        img.dataset.url = emo.url;
        img.dataset.imgType = "c-emo";

        edit.focus();
        let selection = window.getSelection();

        // 如果存在最后的光标对象
        if (this.lastEditRange) {
          // 选区对象清除所有光标
          selection.removeAllRanges();
          // 并添加最后记录的光标，以还原之前的状态
          selection.addRange(this.lastEditRange);
          // 获取到最后选择的位置
          let range = selection.getRangeAt(0);
          // 在此位置插入图片
          range.insertNode(img);
          // false，表示将Range对象所代表的区域的起点移动到终点处
          range.collapse(false);

          // 记录最后的位置
          this.lastEditRange = selection.getRangeAt(0);
        } else {
          // 将图片添加到可编辑的div中，作为可编辑div的子节点
          edit.appendChild(img);
          // 使用选取对象，选取可编辑div中的所有子节点
          selection.selectAllChildren(edit);
          // 合并到最后面，即实现了添加一个图片后，把光标移到最后面
          selection.collapseToEnd();
        }
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
      color: #f9c73d;
    }

    .ci-point {
      cursor: pointer;
      color: #b89f5f;
    }

    .img-point {
      cursor: pointer;
      color: #48a5b9;
    }

    .c-emo-point {
      cursor: pointer;
      color: #1aa5ff;
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