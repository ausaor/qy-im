<template>
  <div class="send-content-area">
    <div class="chat-input-area">
      <div :class="['edit-chat-container',isEmpty?'':'not-empty']" contenteditable="true" @paste.prevent="onPaste"
           @keydown="onKeydown" @compositionstart="compositionFlag=true" @compositionend="onCompositionEnd"
           @input="onEditorInput" @mousedown="onMousedown" ref="content" @blur="onBlur">
      </div>
      <chat-at-box @select="onAtSelect" :search-text="atSearchText" ref="atBox" :ownerId="ownerId"
                   :members="groupMembers" :show-nick-name="showNickName"></chat-at-box>
    </div>
    <div class="quote-message" v-if="quoteMessage.show">
      <div class="chat-quote-message">
        <div class="send-user">{{quoteMessage.msgInfo?.showName}}：</div>
        <div class="quote-content">
          <span v-if="quoteMessage.msgInfo?.type===$enums.MESSAGE_TYPE.TEXT" v-html="htmlText"></span>
          <div v-if="quoteMessage.msgInfo?.type===$enums.MESSAGE_TYPE.IMAGE">
            <img :src="JSON.parse(quoteMessage.msgInfo?.content).originUrl" class="quote-image">
          </div>
          <div v-if="quoteMessage.msgInfo?.type===$enums.MESSAGE_TYPE.VIDEO" class="quote-video-box">
<!--            <video controls="controls" preload="none" :src="JSON.parse(quoteMessage.msgInfo?.content).videoUrl"
                   :poster="JSON.parse(quoteMessage.msgInfo?.content).coverUrl" class="quote-video"></video>-->
            <img :src="JSON.parse(quoteMessage.msgInfo?.content).coverUrl" class="quote-video-image">
            <span class="play-icon el-icon-video-play"></span>
          </div>
          <div v-if="quoteMessage.msgInfo?.type===$enums.MESSAGE_TYPE.AUDIO">
            <mini-audio :audio-source="JSON.parse(quoteMessage.msgInfo?.content).url"></mini-audio>
          </div>
          <div v-if="quoteMessage.msgInfo?.type===$enums.MESSAGE_TYPE.FILE" class="quote-file">
            <div class="quote-file-info">
              <el-link class="quote-file-name" :underline="true" target="_blank" type="primary"
                       :href="data.url">{{data.name}}</el-link>
              <div class="quote-file-size">{{fileSize}}</div>
            </div>
            <div class="quote-file-icon">
              <span type="primary" class="el-icon-document"></span>
            </div>
          </div>
          <div v-if="quoteMessage.msgInfo?.type===$enums.MESSAGE_TYPE.WORD_VOICE" class="quote-word-voice">
            <span class="word" :title="JSON.parse(quoteMessage.msgInfo.content).word">{{JSON.parse(quoteMessage.msgInfo.content).word}}</span>
            <span class="voice">
                <svg class="icon svg-icon" aria-hidden="true">
                  <use xlink:href="#icon-xitongxiaoxi"></use>
                </svg>
              </span>
          </div>
        </div>
      </div>
      <div class="quote-remove" @click="removeQuoteMsg">
        <i class="el-icon-close"></i>
      </div>
    </div>
  </div>
</template>

<script>
	import ChatAtBox from "./ChatAtBox";

	export default {
		name: "ChatInput",
		components: { ChatAtBox },
		props: {
			ownerId: {
				type: Number,
			},
			groupMembers: {
				type: Array,
			},
      showNickName: {
			  type: Boolean,
        default: false,
      },
      quoteMessage: {
			  type: Object,
			  default() {
				  return {
					  msgInfo: null,
					  show: false
				}
			}
		},
		},
		data() {
			return {
				imageList: [],
				fileList: [],
				currentId: 0,
				atSearchText: null,
				compositionFlag: false,
				atIng: false,
				isEmpty: true,
				changeStored: true,
				blurRange: null
			}
		},
		methods: {
			onPaste(e) {
				this.isEmpty = false;
				let txt = e.clipboardData.getData('Text')
				let range = window.getSelection().getRangeAt(0)
				if (range.startContainer !== range.endContainer || range.startOffset !== range.endOffset) {
					range.deleteContents();
				}
				// 粘贴图片和文件时，这里没有数据
				if (txt && typeof(txt) == 'string') {
					let textNode = document.createTextNode(txt);
					range.insertNode(textNode)
					range.collapse();
					return;
				}
				let items = (e.clipboardData || window.clipboardData).items
				if (items.length) {
					for (let i = 0; i < items.length; i++) {
						if (items[i].type.indexOf('image') !== -1) {
							let file = items[i].getAsFile();
							let imagePush = {
								fileId: this.generateId(),
								file: file,
								url: URL.createObjectURL(file),
                exists: false,
							};
							this.imageList[imagePush.fileId] = (imagePush);
							let line = this.newLine();
							let imageElement = document.createElement('img');
							imageElement.className = 'chat-image no-text';
							imageElement.src = imagePush.url;
							imageElement.dataset.imgId = imagePush.fileId;
							line.appendChild(imageElement);
							let after = document.createTextNode('\u00A0');
							line.appendChild(after);
							this.selectElement(after, 1);		
						} else {
							let asFile = items[i].getAsFile();
							if (!asFile) {
								continue;
							}
							let filePush = { fileId: this.generateId(), file: asFile };
							this.fileList[filePush.fileId] = (filePush)
							let line = this.newLine();
							let fileElement = this.createFile(filePush);
							line.appendChild(fileElement);
							let after = document.createTextNode('\u00A0');
							line.appendChild(after);
							this.selectElement(after, 1);
						}
					}
				}
				range.collapse();
			},
			selectElement(element, endOffset) {
				let selection = window.getSelection();
				// 插入元素可能不是立即执行的，vue可能会在插入元素后再更新dom
				this.$nextTick(() => {
					let t1 = document.createRange();
					t1.setStart(element, 0);
					t1.setEnd(element, endOffset || 0);
					if (element.firstChild) {
						t1.selectNodeContents(element.firstChild);
					}
					t1.collapse();
					selection.removeAllRanges();
					selection.addRange(t1);
					// 需要时自动聚焦
					if (element.focus) {
						element.focus();
					}
				})
			},
			onCompositionEnd(e) {
				this.compositionFlag = false;
				this.onEditorInput(e);
			},
			onKeydown(e) {
				if (e.keyCode === 13) {
					e.preventDefault();
					e.stopPropagation();
					if (this.atIng) {
						console.log('选中at的人')
						this.$refs.atBox.select();
						return;
					}
					if (e.ctrlKey) {
						let line = this.newLine();
						let after = document.createTextNode('\u00A0');
						line.appendChild(after);
						this.selectElement(line.childNodes[0], 0);
					} else {
						// 中文输入标记
						if (this.compositionFlag) {
							return;
						}
						this.submit();
					}
					return;
				}
				// 删除键
				if (e.keyCode === 8) {
					console.log("delete")
					// 等待dom更新
					setTimeout(() => {
						let s = this.$refs.content.innerHTML.trim();
						// 空dom时，需要刷新dom
						//console.log(s);
						if (s === '' || s === '<br>' || s === '<div>&nbsp;</div>' ) {
							// 拼接随机长度的空格，以刷新dom
							this.empty();
							this.isEmpty = true;
							this.selectElement(this.$refs.content);
						} else {
							this.isEmpty = false;
						}
					})
				}
				// at框打开时，上下键移动特殊处理
				if (this.atIng) {
					if (e.keyCode === 38) {
						e.preventDefault();
						e.stopPropagation();
						this.$refs.atBox.moveUp();
					}
					if (e.keyCode === 40) {
						e.preventDefault();
						e.stopPropagation();
						this.$refs.atBox.moveDown();
					}
				}

			},
			onAtSelect(member) {
				this.atIng = false;
				// 选中输入的 @xx 符
				let blurRange = this.blurRange;
				let endContainer = blurRange.endContainer
				let startOffset = endContainer.data.indexOf("@"+this.atSearchText);
				let endOffset = startOffset + this.atSearchText.length + 1;
				blurRange.setStart(blurRange.endContainer, startOffset);
				blurRange.setEnd(blurRange.endContainer,  endOffset);
				blurRange.deleteContents()
				blurRange.collapse();
				console.log("onAtSelect")
				this.focus();
				// 创建元素节点
				let element = document.createElement('SPAN')
				element.className = "chat-at-user";
				element.dataset.id = member.userId;
				element.contentEditable = 'false'
				element.innerText = `@${member.aliasName}`
				blurRange.insertNode(element)
				// 光标移动到末尾
				blurRange.collapse()

				// 插入空格
				let textNode = document.createTextNode('\u00A0');
				blurRange.insertNode(textNode);

				blurRange.collapse()
				this.atSearchText = "";
				this.selectElement(textNode, 1);
			},
			onEditorInput(e) {
				this.isEmpty = false;
				this.changeStored = false;
				if (this.$props.groupMembers && !this.compositionFlag) {
					let selection = window.getSelection()
					let range = selection.getRangeAt(0);
					// 截取@后面的名称作为过滤条件，并以空格结束
					let endContainer = range.endContainer;
					let endOffset = range.endOffset;
					let textContent = endContainer.textContent;
					let startIndex = -1;
					for (let i = endOffset; i >= 0; i--) {
						if (textContent[i] === '@') {
							startIndex = i;
							break;
						}
					}
					// 没有at符号，则关闭弹窗
					if (startIndex === -1) {
						this.$refs.atBox.close();
						return;
					}

					let endIndex = endOffset;
					for (let i = endOffset; i < textContent.length; i++) {
						if (textContent[i] === ' ') {
							endIndex = i;
							break;
						}
					}
					this.atSearchText = textContent.substring(startIndex + 1, endIndex).trim();
					// 打开选择弹窗
					if (this.atSearchText == '') {
						this.showAtBox(e)
					}
				}

			},
			onBlur(e) {
				this.updateRange();
				
			},
			onMousedown() {
				if (this.atIng) {
					this.$refs.atBox.close();
					this.atIng = false;
				}
			},
			insertEmoji(emojiText) {
				let emojiElement = document.createElement('img');
				emojiElement.className = 'chat-emoji no-text';
				emojiElement.dataset.emojiCode = emojiText;
				emojiElement.src = this.$emo.textToUrl(emojiText);

				let blurRange = this.blurRange;
				if (!blurRange) {
					this.focus();
					this.updateRange();
					blurRange = this.blurRange;
				}
				if (blurRange.startContainer !== blurRange.endContainer || blurRange.startOffset !== blurRange.endOffset) {
					blurRange.deleteContents();
				}
				blurRange.insertNode(emojiElement);
				blurRange.collapse()

				let textNode = document.createTextNode('\u00A0');
				blurRange.insertNode(textNode)
				blurRange.collapse()

				this.selectElement(textNode);
				this.updateRange();
				this.isEmpty = false;
			},
      insertImage(obj) {
        let imagePush = {
          fileId: this.generateId(),
          url: obj.url,
          imgObj: obj,
          exists: true,
        };
        this.imageList[imagePush.fileId] = (imagePush);
        let line = this.newLine();
        let imageElement = document.createElement('img');
        imageElement.className = 'chat-image no-text';
        imageElement.src = imagePush.url;
        imageElement.dataset.imgId = imagePush.fileId;
        line.appendChild(imageElement);
        let after = document.createTextNode('\u00A0');
        line.appendChild(after);
        this.selectElement(after, 1);
      },
			generateId() {
				return this.currentId++;
			},
			createFile(filePush) {
				let file = filePush.file;
				let fileId = filePush.fileId;
				let container = document.createElement('div');
				container.className = 'chat-file-container no-text';
				container.contentEditable = 'false';
				container.dataset.fileId = fileId;

				let left = document.createElement('div');
				left.className = 'file-position-left';
				container.appendChild(left);

				let icon = document.createElement('div');
				icon.className = 'el-icon-document';
				left.appendChild(icon);

				let right = document.createElement('div');
				right.className = 'file-position-right';
				container.appendChild(right);

				let fileName = document.createElement('div');
				fileName.className = 'file-name';
				fileName.innerText = file.name;

				let fileSize = document.createElement('div');
				fileSize.className = 'file-size';
				fileSize.innerText = this.sizeConvert(file.size);

				right.appendChild(fileName);
				right.appendChild(fileSize);

				return container;
			},
			sizeConvert(len) {
				if (len < 1024) {
					return len + 'B';
				} else if (len < 1024 * 1024) {
					return (len / 1024).toFixed(2) + 'KB';
				} else if (len < 1024 * 1024 * 1024) {
					return (len / 1024 / 1024).toFixed(2) + 'MB';
				} else {
					return (len / 1024 / 1024 / 1024).toFixed(2) + 'GB';
				}
			},
			updateRange() {
				let selection = window.getSelection();
				this.blurRange = selection.getRangeAt(0);
			},
			newLine() {
				let selection = window.getSelection();
				let range = selection.getRangeAt(0);
				let divElement = document.createElement('div');
				let endContainer = range.endContainer;
				let parentElement = endContainer.parentElement;
				if (parentElement.parentElement === this.$refs.content) {
					divElement.innerHTML  = endContainer.textContent.substring(range.endOffset).trim(); 
					endContainer.textContent = endContainer.textContent.substring(0, range.endOffset);
					// 插入到当前div（当前行）后面
					parentElement.insertAdjacentElement('afterend', divElement);
				} else {
					divElement.innerHTML = '';
					this.$refs.content.append(divElement);
				}
				return divElement;
			},
			clear() {
				this.empty();
				this.imageList = [];
				this.fileList = [];
			},
			empty() {
				this.$refs.content.innerHTML = "";
				let line = this.newLine();
				let after = document.createTextNode('\u00A0');
				line.appendChild(after);
				this.$nextTick(()=>this.selectElement(after));
			},
			showAtBox(e) {
				this.atIng = true;
				// show之后会自动更新当前搜索的text
				// this.atSearchText = "";
				let selection = window.getSelection()
				let range = selection.getRangeAt(0)
				// 光标所在坐标
				let pos = range.getBoundingClientRect();
				this.$refs.atBox.open({
					x: pos.x,
					y: pos.y
				})
				// 记录光标所在位置
				this.updateRange();
			},
			html2Escape(strHtml) {
				return strHtml.replace(/[<>&"]/g, function(c) {
					return {
						'<': '&lt;',
						'>': '&gt;',
						'&': '&amp;',
						'"': '&quot;'
					} [c];
				});
			},
			submit() {
				let nodes = this.$refs.content.childNodes;
				let fullList = [];
				let tempText = '';
				let atUserIds = [];
				let each = (nodes) => {
					for (let i = 0; i < nodes.length; i++) {
						let node = nodes[i];
						if (!node) {
							continue;
						}
						if (node.nodeType === 3) {
							tempText += this.html2Escape(node.textContent);
							continue;
						}
						let nodeName = node.nodeName.toLowerCase();
						if (nodeName === 'script') {
							continue;
						}
						let text = tempText.trim();
						if (nodeName === 'img') {
							let imgId = node.dataset.imgId;
							if (imgId) {
								if (text) {
									fullList.push({
										type: 'text',
										content: text,
										atUserIds: atUserIds
									})
									tempText = '';
									atUserIds = []
								}
								fullList.push({
									type: 'image',
									content: this.imageList[imgId]
								})
							} else {
								let emojiCode = node.dataset.emojiCode;
								tempText += emojiCode;
							}
						} else if (nodeName === 'div') {
							let fileId = node.dataset.fileId
							// 文件
							if (fileId) {
								if (text) {
									fullList.push({
										type: 'text',
										content: text,
										atUserIds: atUserIds
									})
									tempText = '';
									atUserIds = []
								}
								fullList.push({
									type: 'file',
									content: this.fileList[fileId]
								})
							} else {
								tempText += '\n';
								each(node.childNodes);
							}
						} else if (nodeName === 'span') {
							if(node.dataset.id){
								tempText += (node.innerHTML + `#{${node.dataset.id}}`);
								atUserIds.push(node.dataset.id)
							}else {
								tempText += node.outerHtml;
							}
						}
					}
				}
				each(nodes)
				let text = tempText.trim();
				if (text !== '') {
					fullList.push({
						type: 'text',
						content: text,
						atUserIds: atUserIds
					})
				}
				this.$emit('submit', fullList);
			},
			focus() {
				this.$refs.content.focus();
			},
      removeQuoteMsg() {
        this.$emit('removeQuoteMsg')
      },
      formatMessage(content, atUserIds) {
        // 将@用户名#{id}格式转换为可点击的span
        const regex = /@([^#]+)#\{(\d+)\}/g;
        let result = content;
        let match;

        // 用于存储已处理的匹配项
        const processedMatches = [];

        // 迭代所有匹配项
        while ((match = regex.exec(content)) !== null) {
          const fullMatch = match[0];
          const username = match[1];
          const userId = parseInt(match[2]);

          // 检查这个匹配是否已经处理过（避免重复处理）
          if (processedMatches.includes(fullMatch)) {
            continue;
          }

          processedMatches.push(fullMatch);

          // 检查用户ID是否在atUserIds中
          if (atUserIds.includes(userId)) {
            // 如果在，转换为可点击元素
            const replacement = `<span class="at-user" data-userid="${userId}">@${username}</span>`;
            result = result.replace(new RegExp(this.escapeRegExp(fullMatch), 'g'), replacement);
          } else {
            // 如果不在，转换为不可点击元素
            const replacement = `<span class="at-user-disabled">@${username}</span>`;
            result = result.replace(new RegExp(this.escapeRegExp(fullMatch), 'g'), replacement);
          }
        }

        return result;
      },
      // 转义正则表达式特殊字符
      escapeRegExp(string) {
        return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
      }
		},
    computed: {
      htmlText() {
        let text = this.$url.replaceURLWithHTMLLinks(this.quoteMessage.msgInfo?.content, '')
        text = this.$emo.transform(text)
        return this.formatMessage(text, this.quoteMessage.msgInfo.atUserIds)
      },
      data() {
        return JSON.parse(this.quoteMessage.msgInfo.content)
      },
      fileSize() {
        let size = this.data.size;
        if (size > 1024 * 1024) {
          return Math.round(size / 1024 / 1024) + "M";
        }
        if (size > 1024) {
          return Math.round(size / 1024) + "KB";
        }
        return size + "B";
      },
    }
	}
</script>

<style lang="scss">
  .send-content-area {
    position: relative;
    display: flex;
    flex-direction: column;
    height: 100%;
    background-color: #fff !important;

    .chat-input-area {
      width: 100%;
      height: 100%;
      position: relative;

      .edit-chat-container {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        border: 1px solid #c3c3c3;
        outline: none;
        padding: 5px;
        line-height: 30px;
        font-size: 16px;
        text-align: left;
        overflow-y: scroll;

        // 单独一行时，无法在前面输入的bug
        >div:before {
          content: "\00a0";
          font-size: 14px;
          position: absolute;
          top: 0;
          left: 0;
        }

        .chat-image {
          display: block;
          max-width: 200px;
          max-height: 100px;
          border: 1px solid #e6e6e6;
          cursor: pointer;
        }

        .chat-emoji {
          width: 30px;
          height: 30px;
          vertical-align: top;
          cursor: pointer;
        }

        .chat-file-container {
          max-width: 65%;
          padding: 10px;
          border: 2px solid #587ff0;
          display: flex;
          background: #eeeC;
          border-radius: 10px;

          .file-position-left {
            display: flex;
            width: 80px;
            justify-content: center;
            align-items: center;

            .el-icon-document {
              font-size: 40px;
              text-align: center;
              color: #d42e07;
            }
          }

          .file-position-right {
            flex: 1;

            .file-name {
              font-size: 16px;
              font-weight: 600;
              color: #66b1ff;
            }

            .file-size {
              font-size: 14px;
              font-weight: 600;
              color: black;
            }
          }
        }

        .chat-at-user {
          color: #00f;
          font-weight: 600;

          border-radius: 3px;
        }
      }

      .edit-chat-container>div:nth-of-type(1):after {
        content: '请输入消息（按Ctrl+Enter键换行）';
        color: gray;
      }

      .edit-chat-container.not-empty>div:nth-of-type(1):after {
        content: none;
      }

    }

    .quote-message {
      position: absolute;
      bottom: 10px;
      left: 10px;
      font-size: 14px;
      max-width: 80%;
      border-radius: 5px;

      .chat-quote-message {
        background: #eee;
        padding: 5px;
        display: inline-flex;
        align-items: center;
        border-radius: 8px;
        font-size: 12px;
        color: #999;

        .send-user {
          margin-right: 10px;
          font-weight: 600;
        }

        .quote-content {
          display: flex;
          align-items: center;

          .quote-image {
            min-width: 40px;
            min-height: 30px;
            max-width: 80px;
            max-height: 60px;
            cursor: pointer;
          }

          .quote-video-box {
            position: relative;
          }

          .quote-video {
            min-width: 80px;
            min-height: 60px;
            max-width: 160px;
            max-height: 120px;
            cursor: pointer;
          }

          .quote-video-image {
            min-width: 40px;
            min-height: 30px;
            max-width: 80px;
            max-height: 60px;
            cursor: pointer;
          }

          .play-icon {
            display: block;
            position: absolute;
            font-size: 32px;
            font-weight: 500;
            width: 32px;
            height: 32px;
            left: 50%;
            top: 50%;
            transform: translate(-50%, -50%);
            cursor: pointer;
            color: #ffffff;
          }

          .quote-file {
            display: flex;
            flex-wrap: nowrap;
            flex-direction: row;
            align-items: center;
            cursor: pointer;
            padding: 2px 15px;

            .quote-file-info {
              flex: 1;
              height: 100%;
              text-align: left;
              font-size: 14px;
              margin-right: 10px;

              .quote-file-name {
                display: inline-block;
                min-width: 160px;
                max-width: 220px;
                font-size: 14px;
                margin-bottom: 4px;
                white-space: pre-wrap;
                word-break: break-all;
              }

              .quote-file-size {
                font-size: 12px;
                color: #999;
              }
            }

            .quote-file-icon {
              font-size: 32px;
              color: #d42e07;
            }
          }

          .quote-word-voice {
            display: flex;
            align-items: center;

            .word {
              display: flex;
              align-items: center;
              overflow: hidden;
              white-space: nowrap;
              text-overflow: ellipsis;
              max-width: 260px;
            }
            .voice {
              cursor: pointer;
            }

            .icon {
              font-size: 16px;
              height: 16px;
            }
          }
        }
      }

      .quote-remove {
        display: none;
        position: absolute;
        top: -8px;
        right: -8px;
        width: 20px;
        height: 20px;
        line-height: 20px;
        font-size: 14px;
        color: #fff;
        border-radius: 50%;
        background: #aaa;
        cursor: pointer;
      }

      .quote-remove:hover {
        background: #888;
      }
    }

    .quote-message:hover {
      .quote-remove {
        display: block;
      }
    }
  }
</style>