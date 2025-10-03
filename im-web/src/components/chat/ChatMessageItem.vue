<template>
	<div class="chat-msg-item">
		<div class="chat-msg-tip" v-if="msgInfo.type==$enums.MESSAGE_TYPE.RECALL || msgInfo.type == $enums.MESSAGE_TYPE.TIP_TEXT">
      {{msgInfo.content}}
    </div>
    <div class="chat-msg-tip" v-if="msgInfo.type==$enums.MESSAGE_TYPE.TIP_TIME">
      {{$date.toTimeText(msgInfo.sendTime)}}
    </div>

		<div class="chat-msg-normal" v-if="isNormal" :class="{'chat-msg-mine':mine}">
			<div class="head-image">
				<head-image :name="showInfo.showName" :size="40" :url="showInfo.headImage" :id="msgInfo.sendId"></head-image>
			</div>
			<div class="chat-msg-content">
        <div v-show="mode==1 && (msgInfo.groupId || msgInfo.regionGroupId)" class="chat-msg-top">
          <span :style="nameColorStyle">{{showInfo.showName}}</span>
          <span v-show="myGroupMemberInfo.showNickName">{{showInfo.nickName}}</span>
          <span v-if="isOwner && (msgInfo.groupId || msgInfo.regionGroupId)" class="group-master">群主</span>
          <span v-if="msgInfo.sendId===1" class="blogger">博主</span>
        </div>
				<div v-show="mode==2" class="chat-msg-top">
					<span :style="nameColorStyle">{{showInfo.showName}}</span>
					<span v-show="myGroupMemberInfo.showNickName">{{showInfo.nickName}}</span>
          <span v-if="isOwner && (msgInfo.groupId || msgInfo.regionGroupId)" class="group-master">群主</span>
          <span v-if="msgInfo.sendId===1" class="blogger">博主</span>
          <span>{{$date.toTimeText(msgInfo.sendTime)}}</span>
				</div>
				<div class="chat-msg-bottom" @contextmenu.prevent="showRightMenu($event)">
          <div ref="chatMsgBox">
            <span ref="textChatMsg" class="chat-msg-text" v-if="msgInfo.type==$enums.MESSAGE_TYPE.TEXT"
                  v-html="htmlText"></span>
            <div class="chat-msg-image" v-if="msgInfo.type==$enums.MESSAGE_TYPE.IMAGE">
              <div class="img-load-box" v-loading="loading" element-loading-text="上传中.."
                   element-loading-background="rgba(0, 0, 0, 0.4)">
                <img class="send-image" :src="JSON.parse(msgInfo.content).originUrl" loading="lazy"
                     @click.stop="showFullImageBox()" />
              </div>
              <span title="发送失败" v-show="loadFail" @click.stop="onSendFail"
                    class="send-fail el-icon-warning"></span>
            </div>
            <div class="chat-msg-video" v-if="msgInfo.type==$enums.MESSAGE_TYPE.VIDEO">
              <div class="video-load-box" v-loading="loading" element-loading-text="上传中.."
                   element-loading-background="rgba(0, 0, 0, 0.4)">
                <img class="video-image" :src="JSON.parse(msgInfo.content).coverUrl" loading="lazy"/>
                <span class="play-icon el-icon-video-play" @click="onPlayVideo(JSON.parse(msgInfo.content).videoUrl, JSON.parse(msgInfo.content).coverUrl)"></span>
              </div>
              <span title="发送失败" v-show="loadFail" @click="onSendFail"
                    class="send-fail el-icon-warning"></span>
            </div>
            <div class="chat-msg-file" v-if="msgInfo.type==$enums.MESSAGE_TYPE.FILE">
              <div class="chat-file-box" v-loading="loading">
                <div class="chat-file-info">
                  <el-link class="chat-file-name" :underline="true" target="_blank" type="primary"
                           :href="data.url">{{data.name}}</el-link>
                  <div class="chat-file-size">{{fileSize}}</div>
                </div>
                <div class="chat-file-icon">
                  <span type="primary" class="el-icon-document"></span>
                </div>
              </div>
              <span title="发送失败" v-show="loadFail" @click="onSendFail"
                    class="send-fail el-icon-warning"></span>
            </div>
            <div class="chat-msg-voice" v-if="msgInfo.type==$enums.MESSAGE_TYPE.AUDIO">
<!--              @click="onPlayVoice()"-->
<!--              <audio controls :src="JSON.parse(msgInfo.content).url"></audio>-->
              <mini-audio :audio-source="JSON.parse(msgInfo.content).url"></mini-audio>
            </div>
            <div class="chat-msg-word-voice" v-if="msgInfo.type==$enums.MESSAGE_TYPE.WORD_VOICE">
              <span class="word" :title="JSON.parse(msgInfo.content).word">{{JSON.parse(msgInfo.content).word}}</span>
              <span class="voice" @click.stop="playVoice(JSON.parse(msgInfo.content).voice)">
                <span v-show="!isPlaying"  class="icon iconfont icon-xitongxiaoxi" style="color: orange;"></span>
                <span v-show="isPlaying"  class="icon iconfont icon-yinpinzanting" style="color: orange;"></span>
              </span>
            </div>
            <div class="chat-action chat-msg-text" v-if="isAction">
              <span v-if="msgInfo.type==$enums.MESSAGE_TYPE.ACT_RT_VOICE" title="重新呼叫" @click.stop="$emit('call')"
                    class="iconfont icon-chat-voice"></span>
              <span v-if="msgInfo.type==$enums.MESSAGE_TYPE.ACT_RT_VIDEO" title="重新呼叫" @click.stop="$emit('call')"
                    class="iconfont icon-chat-video"></span>
              <span>{{msgInfo.content}}</span>
            </div>
<!--            <div class="chat-msg-status" v-if="!isAction">
						  <span class="chat-readed" v-show="msgInfo.selfSend && !msgInfo.groupId
						  && msgInfo.status == $enums.MESSAGE_STATUS.READED">已读</span>
              <span class="chat-unread" v-show="msgInfo.selfSend && !msgInfo.groupId
						  && msgInfo.status != $enums.MESSAGE_STATUS.READED">未读</span>
            </div>-->
            <div class="chat-receipt" v-show="msgInfo.receipt" @click.stop="onShowReadedBox">
              <span v-if="msgInfo.receiptOk" class="icon iconfont icon-icon-ok" title="全体已读"></span>
              <span v-else>{{msgInfo.readedCount}}人已读</span>
            </div>
          </div>
          <div class="quote-message" v-if="msgInfo.quoteMsg" @click.stop="scrollToMessage(msgInfo.quoteMsg.id)"
               @contextmenu.prevent.stop="showQuoteRightMenu($event)">
            <div class="chat-quote-message">
              <div class="send-user">{{showInfo.quoteShowName}}：</div>
              <div class="quote-content">
                <span v-if="msgInfo.quoteMsg.type==$enums.MESSAGE_TYPE.TEXT"
                      v-html="htmlQuoteText"></span>
                <div v-if="msgInfo.quoteMsg.type==$enums.MESSAGE_TYPE.IMAGE">
                  <img class="quote-image" :src="JSON.parse(msgInfo.quoteMsg.content).originUrl"/>
                </div>
                <div v-if="msgInfo.quoteMsg.type==$enums.MESSAGE_TYPE.VIDEO">
                  <img class="quote-video-image" :src="JSON.parse(msgInfo.quoteMsg.content).coverUrl"/>
                  <i class="quote-play-icon el-icon-video-play"></i>
                </div>
                <div v-if="msgInfo.quoteMsg.type==$enums.MESSAGE_TYPE.AUDIO">
                  <div style="display: flex;align-items: center;">
                    <span>音频消息</span>
                    <span class="icon iconfont icon-yinpin"></span>
                  </div>
                </div>
                <div v-if="msgInfo.quoteMsg.type==$enums.MESSAGE_TYPE.FILE" class="quote-file">
                  <div class="quote-file-info">
                    <el-link class="quote-file-name" :underline="true" target="_blank" type="primary"
                             :href="quoteMsgData.url">{{quoteMsgData.name}}</el-link>
                    <div class="quote-file-size">{{quoteMsgFileSize}}</div>
                  </div>
                  <div class="quote-file-icon">
                    <span type="primary" class="el-icon-document"></span>
                  </div>
                </div>
                <div v-if="msgInfo.quoteMsg.type==$enums.MESSAGE_TYPE.WORD_VOICE" class="quote-word-voice">
                  <span class="word" :title="JSON.parse(msgInfo.quoteMsg.content).word">{{JSON.parse(msgInfo.quoteMsg.content).word}}</span>
                  <span class="voice icon iconfont icon-xitongxiaoxi" style="color: orange;"></span>
                </div>
              </div>
            </div>
          </div>
				</div>
			</div>

		</div>
		<right-menu v-show="menu && rightMenu.show" :pos="rightMenu.pos" :items="menuItems"
        @close="rightMenu.show=false" @select="onSelectMenu"></right-menu>
    <right-menu v-show="menu && rightMenuQuote.show" :pos="rightMenuQuote.pos" :items="menuItemsQuote"
                @close="rightMenuQuote.show=false" @select="onSelectMenuQuote"></right-menu>
    <chat-group-readed ref="chatGroupReadedBox" :msgInfo="msgInfo" :groupMembers="groupMembers"></chat-group-readed>
	</div>
</template>

<script>
	import HeadImage from "../common/HeadImage.vue";
	import RightMenu from '../common/RightMenu.vue';
  import ChatGroupReaded from './ChatGroupReaded.vue';

	export default {
		name: "messageItem",
		components: {
			HeadImage,
			RightMenu,
      ChatGroupReaded
		},
		props: {
      uid: {
        type: String,
      },
      mode: {
        type: Number,
        default: 1
      },
			mine: {
				type: Boolean,
				required: true
			},
      showInfo: {
        type: Object,
        required: true,
        default() {
          return {
            headImage: "",
            showName: "",
            nickName: "",
            quoteShowName: "",
            characterNum: null,
          }
        }
      },
			/*headImage: {
				type: String,
				required: true
			},
			showName: {
				type: String,
				required: true
			},
      nickName: {
        type: String,
        required: false
      },*/
			msgInfo: {
				type: Object,
				required: true
			},
      myGroupMemberInfo: {
        type: Object,
        required: true,
        default() {
          return {}
        }
      },
      groupMembers: {
        type: Array
      },
			menu:{
				type: Boolean,
				default: true
			},
			isOwner: {
        type: Boolean,
        default: false
      },
		},
		data() {
			return {
				audioPlayState: 'STOP',
        isPlaying: false,
        audio: null,
				rightMenu: {
					show: false,
					pos: {
						x: 0,
						y: 0
					}
				},
        rightMenuQuote: {
          show: false,
          pos: {
            x: 0,
            y: 0
          }
        },
        colors:["#7dd24b","#c7515a","#db68ef","#15d29b",
          "#85029b", "#c9b455","#fb2609","#bda818",
          "#af0831","#326eb6"]
			}
		},
    beforeDestroy() {
      // 组件销毁时移除监听
      if (this.$refs.textChatMsg) {
        this.$refs.textChatMsg.removeEventListener('click', this.handleAtClick);
      }
    },
    mounted() {
      // 添加事件监听器处理@用户名的点击
      if (this.$refs.textChatMsg) {
        this.$refs.textChatMsg.addEventListener('click', this.handleAtClick);
      }
    },
		methods: {
      onSendFail() {
				this.$message.error("该文件已发送失败，目前不支持自动重新发送，建议手动重新发送")
			},
			showFullImageBox() {
				let imageUrl = JSON.parse(this.msgInfo.content).originUrl;
				if (imageUrl) {
					this.$store.commit('showFullImageBox', imageUrl);
				}
			},
      showQuoteMsgFullImageBox() {
        let imageUrl = JSON.parse(this.msgInfo.quoteMsg.content).originUrl;
        if (imageUrl) {
          this.$store.commit('showFullImageBox', imageUrl);
        }
      },
      onPlayVoice() {
				if (!this.audio) {
					this.audio = new Audio();
				}
				this.audio.src = JSON.parse(this.msgInfo.content).url;
				this.audio.play();
				this.onPlayVoice = 'RUNNING';
			},
			showRightMenu(e) {
				this.rightMenu.pos = {
					x: e.x,
					y: e.y
				};
				this.rightMenu.show = "true";
			},
      showQuoteRightMenu(e) {
        this.rightMenuQuote.pos = {
          x: e.x,
          y: e.y
        };
        this.rightMenuQuote.show = "true";
      },
      onSelectMenu(item) {
        this.msgInfo.showName = this.showInfo.showName;
				this.$emit(item.key.toLowerCase(), this.msgInfo);
			},
      onSelectMenuQuote() {
        this.$emit('scrollToMessage', this.msgInfo.quoteMsg.id);
      },
      onShowReadedBox() {
        let rect = this.$refs.chatMsgBox.getBoundingClientRect();
        this.$refs.chatGroupReadedBox.open(rect);
      },
      playVoice(url) {
        if (!this.audio) {
          this.audio = new Audio();
          this.audio.src = url;
          // 监听事件
          this.audio.addEventListener("ended", this.handleEnded);
          this.audio.addEventListener("pause", this.handlePause);
        }

        if (this.audioPlayState == 'STOP') {
          this.audio.play();
          this.isPlaying = true;
          this.audioPlayState = "PLAYING";
        } else if (this.audioPlayState == 'PLAYING') {
          this.audio.pause();
          this.audioPlayState = "PAUSE"
        } else if (this.audioPlayState == 'PAUSE') {
          this.audio.play();
          this.isPlaying = true;
          this.audioPlayState = "PLAYING"
        }
        this.$emit("audioStateChange", this.audioPlayState, this.msgInfo);
      },
      stopPlayAudio() {
        if (this.audio) {
          this.audio.pause();
          // 移除监听
          this.audio.removeEventListener("ended", this.handleEnded);
          this.audio.removeEventListener("pause", this.handlePause);
          this.isPlaying = false;
          this.audioPlayState = "STOP";
          this.audio = null;
        }
      },
      handleEnded() {
        console.log("播放完成");
        this.isPlaying = false;
        this.audioPlayState = "STOP";
        // 移出监听
        this.audio.removeEventListener("ended", this.handleEnded);
        this.audio.removeEventListener("pause", this.handlePause);
        this.audio = null;
        this.$emit("audioStateChange", this.audioPlayState, this.msgInfo);
      },
      handlePause() {
        console.log("播放暂停");
        this.isPlaying = false;
        this.audioPlayState = "STOP";
        this.$emit("audioStateChange", this.audioPlayState, this.msgInfo);
      },
      scrollToMessage(msgId) {
        this.$emit('scrollToMessage', msgId)
      },
      onPlayVideo(url, coverImageUrl) {
        this.$emit('playVideo', {videoUrl: url, coverImageUrl: coverImageUrl});
      },
      showUserInfo(e, userId){
        if(userId && userId>0){
          this.$http({
            url: `/user/find/${userId}`,
            method: 'get'
          }).then((user) => {
            this.$store.commit("setUserInfoBoxPos",e);
            this.$store.commit("showUserInfoBox",user);
          })
        }
      },
      handleAtClick(event) {
        if (event.target.classList.contains('at-user')) {
          const userId = parseInt(event.target.getAttribute('data-userid'));
          // 判断userId是否数字
          if (isNaN(userId)) {
            return;
          }
          this.showUserInfo(event, userId);
        }
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
            const replacement = `<span class="at-user" data-userid="${userId}" style="cursor: pointer;">@${username}</span>`;
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
			loading() {
				return this.msgInfo.loadStatus && this.msgInfo.loadStatus === "loading";
			},
			loadFail() {
				return this.msgInfo.loadStatus && this.msgInfo.loadStatus === "fail";
			},
			data() {
				return JSON.parse(this.msgInfo.content)
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
      quoteMsgData() {
        return JSON.parse(this.msgInfo.quoteMsg.content)
      },
      quoteMsgFileSize() {
        let size = this.quoteMsgData.size;
        if (size > 1024 * 1024) {
          return Math.round(size / 1024 / 1024) + "M";
        }
        if (size > 1024) {
          return Math.round(size / 1024) + "KB";
        }
        return size + "B";
      },
			menuItems() {
				let items = [];
				items.push({
					key: 'DELETE',
					name: '删除',
					icon: 'el-icon-delete'
				});
        if (this.$msgType.isNormal(this.msgInfo.type)) {
          items.push({
            key: 'QUOTE',
            name: '引用',
            icon: 'el-icon-chat-round'
          });
        }
				if (this.msgInfo.selfSend && this.msgInfo.id > 0) {
					items.push({
						key: 'RECALL',
						name: '撤回',
						icon: 'el-icon-refresh-left'
					});
				}
				return items;
			},
      menuItemsQuote() {
        let items = [];
        items.push({
          key: 'scrollToMessage',
          name: '定位到原消息',
          icon: ''
        });
        return items;
      },
      isAction(){
        return this.$msgType.isAction(this.msgInfo.type);
      },
      isNormal() {
        const type = this.msgInfo.type;
        return this.$msgType.isNormal(type) || this.$msgType.isAction(type)
      },
      htmlText() {
        let color = this.msgInfo.selfSend ? 'white' : '';
        let text = this.$url.replaceURLWithHTMLLinks(this.msgInfo.content, color)
        text = this.$emo.transform(text)
        return this.formatMessage(text, this.msgInfo.atUserIds)
      },
      htmlQuoteText() {
        let text = this.$url.replaceURLWithHTMLLinks(this.msgInfo.quoteMsg?.content, '')
        text = this.$emo.transform(text)
        return this.formatMessage(text, this.msgInfo.quoteMsg?.atUserIds ? this.msgInfo.quoteMsg.atUserIds : [])
      },
      nameColorStyle() {
        let index = 0;
        if (this.showInfo.characterNum != null && this.showInfo.characterNum <= 10) {
          index = this.showInfo.characterNum - 1;
        } else {
          return '';
        }
        return `color:${this.colors[index]};`
      }
		},
	}
</script>

<style scoped lang="scss">
	.chat-msg-item {

		.chat-msg-tip {
			line-height: 50px;
      font-size: 14px;
      text-align: center;
		}

		.chat-msg-normal {
			position: relative;
			font-size: 0;
			padding-left: 50px;
      min-height: 50px;
      margin-top: 10px;

			.head-image {
				position: absolute;
				width: 40px;
				height: 40px;
				top: 0;
				left: 0;
			}

			.chat-msg-content {
        text-align: left;

        .send-fail {
          color: #e60c0c;
          font-size: 30px;
          cursor: pointer;
          margin: 0 20px;
        }

				.chat-msg-top {
					display: flex;
					flex-wrap: nowrap;
					color: #333;
					font-size: 14px;
					line-height: 20px;

          .group-master {
            background-color: orange;
            color: white;
            font-size: 12px;
            padding: 0 5px;
            border-radius: 10px;
          }

          .blogger {
            background-color: #1E90FF;
            color: white;
            font-size: 12px;
            padding: 0 5px;
            border-radius: 10px;
          }

					span {
						margin-right: 12px;
					}
				}

				.chat-msg-bottom {
          display: inline-block;
          padding-right: 300px;

					.chat-msg-text {
            display: inline-flex;
            position: relative;
            line-height: 26px;
            padding: 6px 10px;
            background-color: #daf3fd;
            border-radius: 10px;
            color: black;
            font-size: 14px;
            text-align: left;
            white-space: pre-wrap;
            word-break: break-all;
            box-shadow: 1px 1px 1px #c0c0f0;

            &:after {
              content: "";
              position: absolute;
              left: -10px;
              top: 13px;
              width: 0;
              height: 0;
              border-style: solid dashed dashed;
              border-color: #daf3fd transparent transparent;
              overflow: hidden;
              border-width: 10px;
						}

            .at-user {
              cursor: pointer;
            }
					}

					.chat-msg-image {
						display: flex;
						flex-wrap: nowrap;
						flex-direction: row;
						align-items: center;

						.send-image {
              min-width: 200px;
              min-height: 150px;
              max-width: 400px;
              max-height: 300px;
              border-radius: 8px;
              cursor: pointer;
						}

						.send-fail {
							color: #e60c0c;
							font-size: 30px;
							cursor: pointer;
							margin: 0 20px;
						}
					}

          .chat-msg-video {
            display: flex;
            flex-wrap: nowrap;
            flex-direction: row;
            align-items: center;

            .video-load-box {
              position: relative;

              .video-image {
                min-width: 200px;
                max-width: 400px;
                min-height: 150px;
                max-height: 300px;
                border-radius: 8px;
                cursor: pointer;
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
          }

					.chat-msg-file {
						display: flex;
						flex-wrap: nowrap;
						flex-direction: row;
						align-items: center;
						cursor: pointer;
            padding-bottom: 5px;

						.chat-file-box {
							display: flex;
							flex-wrap: nowrap;
							align-items: center;
							min-height: 80px;
              box-shadow: 5px 5px 2px #c0c0c0;
							border: var(--border-color) solid 1px;
							border-radius: 6px;
							background-color: var(--active-color);
							padding: 10px 15px;

							.chat-file-info {
								flex: 1;
								height: 100%;
								text-align: left;
								font-size: 14px;

								.chat-file-name {
                  display: inline-block;
                  min-width: 150px;
                  max-width: 300px;
									font-size: 16px;
									font-weight: 600;
									margin-bottom: 15px;
                  white-space: pre-wrap;
                  word-break: break-all;
								}
							}

							.chat-file-icon {
								font-size: 50px;
								color: #d42e07;
							}
						}

						.send-fail {
							color: #e60c0c;
							font-size: 30px;
							cursor: pointer;
							margin: 0 20px;
						}

					}

					.chat-msg-voice {
						font-size: 14px;
						cursor: pointer;

						audio {
							height: 45px;
							padding: 5px 0;
						}
					}

          .chat-msg-word-voice {
            display: block;
            position: relative;
            line-height: 26px;
            margin-top: 3px;
            padding: 7px;
            background-color: #daf3fd;
            border-radius: 10px;
            color: black;
            font-size: 14px;
            text-align: left;
            white-space: pre-wrap;
            word-break: break-all;
            box-shadow: 1px 1px 1px #c0c0f0;

            &:after {
              content: "";
              position: absolute;
              left: -10px;
              top: 13px;
              width: 0;
              height: 0;
              border-style: solid dashed dashed;
              border-color: #daf3fd transparent transparent;
              overflow: hidden;
              border-width: 10px;
            }

            .word {
              overflow: hidden;
              white-space: nowrap;
              text-overflow: ellipsis;
              width: 260px;
              height: 20px;
            }
            .voice {
              cursor: pointer;
            }

            .icon {
              font-size: 20px;
              height: 20px;
            }
          }

          .quote-message {
            display: block;
            margin-top: 3px;
            cursor: pointer;

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
                position: relative;

                .quote-image {
                  min-width: 40px;
                  min-height: 30px;
                  max-width: 80px;
                  max-height: 60px;
                  cursor: pointer;
                }

                .quote-video-image {
                  min-width: 40px;
                  min-height: 30px;
                  max-width: 80px;
                  max-height: 60px;
                  cursor: pointer;
                }

                .quote-play-icon {
                  font-size: 30px;
                  font-weight: 500;
                  position: absolute;
                  left: 50%;
                  top: 50%;
                  transform: translate(-50%, -50%);
                  color: white;
                  cursor: pointer;
                }

                .quote-video {
                  min-width: 80px;
                  min-height: 60px;
                  max-width: 160px;
                  max-height: 120px;
                  cursor: pointer;
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
          }

          .chat-action {
            display: flex;
            align-items: center;

            .iconfont {
              cursor: pointer;
              font-size: 22px;
              padding-right: 8px;
            }
          }

          .chat-msg-status {
            display: block;

            .chat-readed {
              font-size: 12px;
              color: #888;
              font-weight: 600;
            }

            .chat-unread {
              font-size: 12px;
              color: #f23c0f;
              font-weight: 600;
            }
          }

          .chat-receipt {
            height: 20px;
            font-size: 13px;
            color: blue;
            cursor: pointer;

            .icon-ok {
              font-size: 20px;
              color: #329432;
              width: 20px;
              height: 20px;
              display: inline-block;
            }
          }
				}
			}


			&.chat-msg-mine {
				text-align: right;
				padding-left: 0;
				padding-right: 50px;

				.head-image {
					left: auto;
					right: 0;
				}

				.chat-msg-content {
          text-align: right;

					.chat-msg-top {
						flex-direction: row-reverse;

						span {
							margin-left: 12px;
							margin-right: 0;
						}
					}

					.chat-msg-bottom {
            padding-left: 180px;
            padding-right: 0;

						.chat-msg-text {
							margin-left: 10px;
              background-color: rgb(88, 127, 240);
							color: #fff;
							vertical-align: top;
              box-shadow: 1px 1px 1px #ccc;

							&:after {
								left: auto;
								right: -10px;
                border-top-color: rgb(88, 127, 240);
							}

              .at-user {
                cursor: pointer;
              }
						}

            .chat-msg-word-voice {
              margin-left: 10px;
              background-color: rgb(88, 127, 240);
              color: #fff;
              vertical-align: top;
              box-shadow: 1px 1px 1px #ccc;

              &:after {
                left: auto;
                right: -10px;
                border-top-color: rgb(88, 127, 240);
              }
            }

						.chat-msg-image {
							flex-direction: row-reverse;
						}

            .chat-msg-video {
              flex-direction: row-reverse;
            }

						.chat-msg-file {
							flex-direction: row-reverse;
						}

            .chat-action {
              flex-direction: row-reverse;

              .iconfont {
                transform: rotateY(180deg);
              }
            }
					}
				}
			}

		}
	}
</style>