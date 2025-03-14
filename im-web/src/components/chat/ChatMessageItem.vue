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
          <span v-if="isOwner && (msgInfo.groupId || msgInfo.regionGroupId)" style="background-color: orange; color: white;">群主</span>
          <span v-if="msgInfo.sendId===1" style="background-color: #1E90FF; color: white;">博主</span>
        </div>
				<div v-show="mode==2" class="chat-msg-top">
					<span :style="nameColorStyle">{{showInfo.showName}}</span>
					<span v-show="myGroupMemberInfo.showNickName">{{showInfo.nickName}}</span>
          <span v-if="isOwner && (msgInfo.groupId || msgInfo.regionGroupId)" style="background-color: orange; color: white;">群主</span>
          <span v-if="msgInfo.sendId===1" style="background-color: #1E90FF; color: white;">博主</span>
          <span>{{$date.toTimeText(msgInfo.sendTime)}}</span>
				</div>
				<div class="chat-msg-bottom" @contextmenu.prevent="showRightMenu($event)">
          <div ref="chatMsgBox">
            <span class="chat-msg-text" v-if="msgInfo.type==$enums.MESSAGE_TYPE.TEXT"
                  v-html="htmlText"></span>
            <div class="chat-msg-image" v-if="msgInfo.type==$enums.MESSAGE_TYPE.IMAGE">
              <div class="img-load-box" v-loading="loading" element-loading-text="上传中.."
                   element-loading-background="rgba(0, 0, 0, 0.4)">
                <img class="send-image" :src="JSON.parse(msgInfo.content).originUrl"
                     @click="showFullImageBox()" />
              </div>
              <span title="发送失败" v-show="loadFail" @click="onSendFail"
                    class="send-fail el-icon-warning"></span>
            </div>
            <div class="chat-msg-video" v-if="msgInfo.type==$enums.MESSAGE_TYPE.VIDEO">
              <div class="video-load-box" v-loading="loading" element-loading-text="上传中.."
                   element-loading-background="rgba(0, 0, 0, 0.4)">
                <video class="send-video" controls="controls" preload="none" :src="JSON.parse(msgInfo.content).videoUrl" :poster="JSON.parse(msgInfo.content).coverUrl"></video>
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
              <span class="voice" @click="playVoice(JSON.parse(msgInfo.content).voice)">
                <svg class="icon svg-icon" aria-hidden="true">
                  <use xlink:href="#icon-xitongxiaoxi"></use>
                </svg>
              </span>
            </div>
            <div class="chat-action chat-msg-text" v-if="isAction">
              <span v-if="msgInfo.type==$enums.MESSAGE_TYPE.ACT_RT_VOICE" title="重新呼叫" @click="$emit('call')"
                    class="iconfont icon-chat-voice"></span>
              <span v-if="msgInfo.type==$enums.MESSAGE_TYPE.ACT_RT_VIDEO" title="重新呼叫" @click="$emit('call')"
                    class="iconfont icon-chat-video"></span>
              <span>{{msgInfo.content}}</span>
            </div>
<!--            <div class="chat-msg-status" v-if="!isAction">
						  <span class="chat-readed" v-show="msgInfo.selfSend && !msgInfo.groupId
						  && msgInfo.status == $enums.MESSAGE_STATUS.READED">已读</span>
              <span class="chat-unread" v-show="msgInfo.selfSend && !msgInfo.groupId
						  && msgInfo.status != $enums.MESSAGE_STATUS.READED">未读</span>
            </div>-->
            <div class="chat-receipt" v-show="msgInfo.receipt" @click="onShowReadedBox">
              <span v-if="msgInfo.receiptOk" class="icon iconfont icon-icon-ok" title="全体已读"></span>
              <span v-else>{{msgInfo.readedCount}}人已读</span>
            </div>

          </div>
				</div>
			</div>

		</div>
		<right-menu v-show="menu && rightMenu.show" :pos="rightMenu.pos" :items="menuItems"
        @close="rightMenu.show=false" @select="onSelectMenu"></right-menu>
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
      }
		},
		data() {
			return {
				audioPlayState: 'STOP',
				rightMenu: {
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
      onSelectMenu(item) {
				this.$emit(item.key.toLowerCase(), this.msgInfo);
			},
      onShowReadedBox() {
        let rect = this.$refs.chatMsgBox.getBoundingClientRect();
        this.$refs.chatGroupReadedBox.open(rect);
      },
      playVoice(url) {
        let audio = new Audio();
        audio.src = url;
        audio.play();
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
			menuItems() {
				let items = [];
				items.push({
					key: 'DELETE',
					name: '删除',
					icon: 'el-icon-delete'
				});
				if (this.msgInfo.selfSend && this.msgInfo.id > 0) {
					items.push({
						key: 'RECALL',
						name: '撤回',
						icon: 'el-icon-refresh-left'
					});
				}
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
        return this.$emo.transform(text)
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

<style lang="scss">
	.chat-msg-item {

		.chat-msg-tip {
			line-height: 50px;
      font-size: 14px;
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

					span {
						margin-right: 12px;
					}
				}

				.chat-msg-bottom {
          display: inline-block;
          padding-right: 300px;

					.chat-msg-text {
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
              z-index: -1;
							width: 0;
							height: 0;
							border-style: solid dashed dashed;
              border-color: white transparent transparent;
              overflow: hidden;
              border-width: 10px;
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
							border: var(--border-color) solid 1px;
              box-shadow: 2px 2px 2px #c0c0c0;
              border-radius: 6px;
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

            .send-video {
              min-width: 200px;
              max-width: 300px;
              max-height: 300px;
              border-radius: 10px;
              overflow: hidden;
              cursor: pointer;
              -o-object-fit: contain;
              object-fit: contain;
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
              z-index: -1;
              width: 0;
              height: 0;
              border-style: solid dashed dashed;
              border-color: white transparent transparent;
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

          .chat-at-user {
            background-color: #4cd964;
            padding: 2px 5px;
            color: white;
            border-radius: 3px;
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