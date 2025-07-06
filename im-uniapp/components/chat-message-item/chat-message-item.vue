<template>
	<view class="chat-msg-item">
		<view class="chat-msg-tip"
			v-if="msgInfo.type == $enums.MESSAGE_TYPE.RECALL || msgInfo.type == $enums.MESSAGE_TYPE.TIP_TEXT">
			{{ msgInfo.content }}
		</view>
		<view class="chat-msg-tip" v-if="msgInfo.type == $enums.MESSAGE_TYPE.TIP_TIME">
			{{ $date.toTimeText(msgInfo.sendTime) }}
		</view>
		<view class="chat-msg-normal" v-if="isNormal" :class="{ 'chat-msg-mine': msgInfo.selfSend }">
			<head-image class="avatar" @longpress.prevent="$emit('longPressHead')" :id="msgInfo.sendId" :url="showInfo.headImage"
				:name="showInfo.showName" size="small"></head-image>
			<view class="chat-msg-content">
				<view v-if="(msgInfo.groupId || msgInfo.regionGroupId) && !msgInfo.selfSend" class="chat-msg-top">
					<text :style="nameColorStyle">{{ showInfo.showName }}</text>
          <text v-show="myGroupMemberInfo.showNickName" style="margin-left: 20rpx;">{{showInfo.nickName}}</text>
          <text v-if="isOwner && msgInfo.groupId" style="margin-left: 20rpx;" class="group-owner">群主</text>
          <text v-if="msgInfo.sendId===1" style="margin-left: 20rpx;" class="blogger">博主</text>
				</view>
				<view class="chat-msg-bottom">
					<view v-if="msgInfo.type == $enums.MESSAGE_TYPE.TEXT">
						<long-press-menu :items="menuItems" @select="onSelectMenu">
<!--							<up-parse class="chat-msg-text" :showImgMenu="false" :content="nodesText"></up-parse>-->
							 <rich-text class="chat-msg-text" :nodes="nodesText"></rich-text>
						</long-press-menu>
					</view>
          <view v-if="msgInfo.type == $enums.MESSAGE_TYPE.WORD_VOICE" class="chat-msg-word-voice">
            <long-press-menu :items="menuItems" @select="onSelectMenu">
              <view class="word-voice-box">
                <view class="chat-msg-word-voice-text">{{JSON.parse(msgInfo.content).word}}</view>
                <view class="voice" @click.stop="onPlayAudio(JSON.parse(msgInfo.content).voice)">
                  <svg-icon v-if="!isPlaying" :icon-class="'xitongxiaoxi'" />
                  <svg-icon v-if="isPlaying" :icon-class="'yinpinzanting'" />
                </view>
              </view>
            </long-press-menu>
          </view>
					<view class="chat-msg-image" v-if="msgInfo.type == $enums.MESSAGE_TYPE.IMAGE">
						<long-press-menu :items="menuItems" @select="onSelectMenu">
							<view class="img-load-box">
								<image class="send-image" mode="heightFix" :src="JSON.parse(msgInfo.content).originUrl"
									lazy-load="true" @click.stop="onShowFullImage()">
								</image>
								<loading v-if="loading"></loading>
							</view>
						</long-press-menu>
						<text title="发送失败" v-if="loadFail" @click="onSendFail"
							class="send-fail iconfont icon-warning-circle-fill"></text>
					</view>
					<view class="chat-msg-file" v-if="msgInfo.type == $enums.MESSAGE_TYPE.FILE">
						<long-press-menu :items="menuItems" @select="onSelectMenu">
							<view class="chat-file-box">
								<view class="chat-file-info">
									<uni-link class="chat-file-name" :text="data.name" showUnderLine="true"
										color="#007BFF" :href="data.url"></uni-link>
									<view class="chat-file-size">{{ fileSize }}</view>
								</view>
								<view class="chat-file-icon iconfont icon-file"></view>
								<loading v-if="loading"></loading>
							</view>
						</long-press-menu>
						<text title="发送失败" v-if="loadFail" @click="onSendFail"
							class="send-fail iconfont icon-warning-circle-fill"></text>
					</view>
          <view class="chat-msg-video" v-if="msgInfo.type == $enums.MESSAGE_TYPE.VIDEO">
            <long-press-menu :items="menuItems" @select="onSelectMenu">
              <view class="video-msg-box">
                <image class="video-cover-image" mode="heightFix" :src="JSON.parse(msgInfo.content).coverUrl" lazy-load="true"></image>
                <text class="play-icon iconfont icon-play" @click.stop="onPlayVideo(JSON.parse(msgInfo.content).videoUrl, JSON.parse(msgInfo.content).coverUrl)"></text>
                <loading v-if="loading"></loading>
              </view>
            </long-press-menu>
            <text title="发送失败" v-if="loadFail" @click="onSendFail"
                  class="send-fail iconfont icon-warning-circle-fill"></text>
          </view>
					<long-press-menu v-if="msgInfo.type == $enums.MESSAGE_TYPE.AUDIO" :items="menuItems"
						@select="onSelectMenu">
						<view class="chat-msg-audio chat-msg-text" @click.stop="onPlayAudio(JSON.parse(this.msgInfo.content).url)">
              <svg-icon :icon-class="'yinpin'" />
							<text class="chat-audio-text">{{ JSON.parse(msgInfo.content).duration ?  JSON.parse(msgInfo.content).duration + '"' : JSON.parse(msgInfo.content).originalName }}</text>
							<text v-if="audioPlayState == 'PAUSE'" class="iconfont icon-play"></text>
							<text v-if="audioPlayState == 'PLAYING'" class="iconfont icon-pause"></text>
						</view>
					</long-press-menu>
					<long-press-menu v-if="isAction" :items="menuItems" @select="onSelectMenu">
						<view class="chat-realtime chat-msg-text" @click="$emit('call')">
							<text v-if="msgInfo.type == $enums.MESSAGE_TYPE.ACT_RT_VOICE"
								class="iconfont icon-chat-voice"></text>
							<text v-if="msgInfo.type == $enums.MESSAGE_TYPE.ACT_RT_VIDEO"
								class="iconfont icon-chat-video"></text>
							<text>{{ msgInfo.content }}</text>
						</view>
					</long-press-menu>
          <view class="quote-message" v-if="msgInfo.quoteMsg">
            <long-press-menu :items="quoteMsgMenuItems" @select="scrollToMessage(msgInfo.quoteMsg.id)">
              <view class="chat-quote-message" @click.stop="scrollToMessage(msgInfo.quoteMsg.id)">
                <view class="send-user">{{showInfo.quoteShowName}}:</view>
                <view class="quote-content">
                  <view v-if="msgInfo.quoteMsg.type == $enums.MESSAGE_TYPE.TEXT">
                    <rich-text class="quote-text" :nodes="nodesTextQuote"></rich-text>
                  </view>
                  <view v-if="msgInfo.quoteMsg.type == $enums.MESSAGE_TYPE.IMAGE">
                    <image class="quote-image" mode="aspectFill" :src="JSON.parse(msgInfo.quoteMsg.content).originUrl" lazy-load="true"></image>
                  </view>
                  <view v-if="msgInfo.quoteMsg.type == $enums.MESSAGE_TYPE.VIDEO" class="quote-video">
                    <image class="video-cover-image" mode="heightFix" :src="JSON.parse(msgInfo.quoteMsg.content).coverUrl" lazy-load="true"></image>
                    <text class="play-icon iconfont icon-play"></text>
                  </view>
                  <view v-if="msgInfo.quoteMsg.type == $enums.MESSAGE_TYPE.FILE" class="quote-msg-file">
                    <view class="quote-file-box">
                      <view class="quote-file-info">
                        <uni-link class="quote-file-name" :text="quoteMsgData.name" showUnderLine="true"
                                  color="#007BFF" :href="quoteMsgData.url"></uni-link>
                        <view class="quote-file-size">{{quoteMsgFileSize}}</view>
                      </view>
                      <view class="quote-file-icon iconfont icon-file"></view>
                    </view>
                  </view>
                  <view v-if="msgInfo.quoteMsg.type == $enums.MESSAGE_TYPE.AUDIO" class="quote-msg-audio">
                    <text class="quote-audio-text">{{ JSON.parse(msgInfo.quoteMsg.content).duration ? (JSON.parse(msgInfo.quoteMsg.content).duration + '"') : JSON.parse(msgInfo.quoteMsg.content).originalName }}</text>
                    <svg-icon :icon-class="'yinpin'"/>
                  </view>
                  <view v-if="msgInfo.quoteMsg.type == $enums.MESSAGE_TYPE.WORD_VOICE" class="quote-msg-word-voice">
                    <view class="word">{{JSON.parse(msgInfo.quoteMsg.content).word}}</view>
                    <view class="voice">
                      <svg-icon :icon-class="'xitongxiaoxi'" />
                    </view>
                  </view>
                </view>
              </view>
            </long-press-menu>
          </view>
<!--					<view class="chat-msg-status" v-if="!isAction">
						<text class="chat-readed" v-if="msgInfo.selfSend && !msgInfo.groupId
							&& msgInfo.status == $enums.MESSAGE_STATUS.READED">已读</text>
						<text class="chat-unread" v-if="msgInfo.selfSend && !msgInfo.groupId
							&& msgInfo.status != $enums.MESSAGE_STATUS.READED">未读</text>
					</view>-->
					<view class="chat-receipt" v-if="msgInfo.receipt" @click.stop="onShowReadedBox">
						<text v-if="msgInfo.receiptOk" class="tool-icon iconfont icon-ok"></text>
						<text v-else>{{ msgInfo.readedCount }}人已读</text>
					</view>
				</view>
			</view>
		</view>
		<chat-group-readed ref="chatGroupReaded" :groupMembers="groupMembers" :msgInfo="msgInfo"></chat-group-readed>

	</view>
</template>

<script>
import SvgIcon from "../svg-icon/svg-icon.vue";

export default {
	name: "chat-message-item",
  components: {SvgIcon},
	props: {
    showInfo: {
      type: Object,
      required: true,
      default() {
        return {
          headImage: "",
          showName: "",
          nickName: "",
          characterNum: null,
          quoteShowName: "",
        }
      }
    },
		msgInfo: {
			type: Object,
			required: true
		},
		groupMembers: {
			type: Array
		},
    myGroupMemberInfo: {
      type: Object,
      required: true,
      default() {
        return {}
      }
    },
    isOwner: {
      type: Boolean,
      default: false
    }
	},
	data() {
		return {
      isPlaying: false,
			audioPlayState: 'STOP',
			innerAudioContext: null,
      audioContext: null,
      audioPlayStateQuote: 'STOP',
      innerAudioContextQuote: null,
      audioContextQuote: null,
			menu: {
				show: false,
				style: ""
			},
      colors:["#7dd24b","#c7515a","#db68ef","#15d29b",
        "#85029b", "#c9b455","#fb2609","#bda818",
        "#af0831","#326eb6"]
		}
	},
	methods: {
		onSendFail() {
			uni.showToast({
				title: "该文件已发送失败，目前不支持自动重新发送，建议手动重新发送",
				icon: "none"
			})
		},
		onPlayAudio(url) {
			// 初始化音频播放器
			if (!this.innerAudioContext) {
				this.innerAudioContext = uni.createInnerAudioContext();
				this.innerAudioContext.src = url;
				this.innerAudioContext.onEnded((e) => {
					console.log('停止')
          this.isPlaying = false;
					this.audioPlayState = "STOP"
					this.emit();
				})
				this.innerAudioContext.onError((e) => {
					this.audioPlayState = "STOP";
          this.isPlaying = false;
					console.log("播放音频出错");
					console.log(e)
					this.emit();
				});
			}
			if (this.audioPlayState == 'STOP') {
				this.innerAudioContext.play();
        this.isPlaying = true;
				this.audioPlayState = "PLAYING";
			} else if (this.audioPlayState == 'PLAYING') {
				this.innerAudioContext.pause();
        this.isPlaying = false;
				this.audioPlayState = "PAUSE"
			} else if (this.audioPlayState == 'PAUSE') {
				this.innerAudioContext.play();
        this.isPlaying = true;
				this.audioPlayState = "PLAYING"
			}
			this.emit();
		},
    onPlayQuoteAudio() {
      // 初始化音频播放器
      if (!this.innerAudioContextQuote) {
        this.innerAudioContextQuote = uni.createInnerAudioContext();
        let url = JSON.parse(this.msgInfo.quoteMsg.content).url;
        this.innerAudioContextQuote.src = url;
        this.innerAudioContextQuote.onEnded((e) => {
          console.log('停止')
          this.audioPlayStateQuote = "STOP"
          this.emit();
        })
        this.innerAudioContextQuote.onError((e) => {
          this.audioPlayStateQuote = "STOP"
          console.log("播放音频出错");
          console.log(e)
          this.emit();
        });
      }
      if (this.audioPlayStateQuote == 'STOP') {
        this.innerAudioContextQuote.play();
        this.audioPlayStateQuote = "PLAYING";
      } else if (this.audioPlayStateQuote == 'PLAYING') {
        this.innerAudioContextQuote.pause();
        this.audioPlayStateQuote = "PAUSE"
      } else if (this.audioPlayStateQuote == 'PAUSE') {
        this.innerAudioContextQuote.play();
        this.audioPlayStateQuote = "PLAYING"
      }
    },
		onSelectMenu(item) {
      this.msgInfo.showName = this.showInfo.showName;
			this.$emit(item.key.toLowerCase(), this.msgInfo);
			this.menu.show = false;
		},
    quoteMsgOnSelectMenu() {
      this.$emit('scrollToMessage', this.msgInfo.quoteMsg.id);
      this.menu.show = false;
    },
		onShowFullImage() {
			let imageUrl = JSON.parse(this.msgInfo.content).originUrl;
			uni.previewImage({
				urls: [imageUrl]
			})
		},
    onShowFullImageQuote() {
      let imageUrl = JSON.parse(this.msgInfo.quoteMsg.content).originUrl;
      uni.previewImage({
        urls: [imageUrl]
      })
    },
		onShowReadedBox() {
			this.$refs.chatGroupReaded.open();
		},
		emit() {
			this.$emit("audioStateChange", this.audioPlayState, this.msgInfo);
		},
		stopPlayAudio() {
			if (this.innerAudioContext) {
				this.innerAudioContext.stop();
				this.innerAudioContext = null;
				this.audioPlayState = "STOP";
        this.isPlaying = false;
			}
		},
    onPlayWordVoice(voiceUrl) {
      // 创建音频上下文
      this.audioContext = uni.createInnerAudioContext();
      // 设置音频源
      this.audioContext.src = voiceUrl;
      // 监听音频播放结束事件
      this.audioContext.onEnded(() => {
        console.log('音频播放结束');
      });
      // 监听音频播放错误事件
      this.audioContext.onError((res) => {
        console.log('音频播放出错:', res.errMsg);
      });
      this.audioContext.play();
    },
    scrollToMessage(msgId) {
      this.$emit('scrollToMessage', msgId)
    },
    onPlayVideo(url, coverImageUrl) {
      this.$emit('playVideo', {videoUrl: url, coverImageUrl: coverImageUrl});
    },
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
    quoteMsgMenuItems() {
      let items = [];
      items.push({
        key: 'origin',
        name: '定位到原消息',
        icon: 'flag'
      });

      return items;
    },
		menuItems() {
			let items = [];
			if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.TEXT) {
				items.push({
					key: 'COPY',
					name: '复制',
					icon: 'bars'
				});
			}
			if (this.msgInfo.selfSend && this.msgInfo.id > 0) {
				items.push({
					key: 'RECALL',
					name: '撤回',
					icon: 'refreshempty'
				});
			}
      if (this.$msgType.isNormal(this.msgInfo.type)) {
        items.push({
          key: 'QUOTE',
          name: '引用',
          icon: 'flag'
        })
      }
			items.push({
				key: 'DELETE',
				name: '删除',
				icon: 'trash',
				color: '#e64e4e'
			});
			if (this.msgInfo.type == this.$enums.MESSAGE_TYPE.FILE) {
				items.push({
					key: 'DOWNLOAD',
					name: '下载并打开',
					icon: 'download'
				});
			}
			return items;
		},
		isAction() {
			return this.$msgType.isAction(this.msgInfo.type);
		},
		isNormal() {
			const type = this.msgInfo.type;
			return this.$msgType.isNormal(type) || this.$msgType.isAction(type)
		},
		nodesText() {
			let color = this.msgInfo.selfSend ? 'white' : '';
			let text = this.$url.replaceURLWithHTMLLinks(this.msgInfo.content, color)
			return this.$emo.transform(text, 'emoji-normal')
		},
    nodesTextQuote() {
      let text = this.$url.replaceURLWithHTMLLinks(this.msgInfo.quoteMsg.content, '')
      return this.$emo.transform(text, 'emoji-normal')
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
	}

}
</script>

<style scoped lang="scss">
.chat-msg-item {
  $icon-color: rgba(0, 0, 0, 0.88);
	padding: 2rpx 20rpx;

	.chat-msg-tip {
		line-height: 60rpx;
		text-align: center;
		color: $im-text-color-lighter;
		font-size: $im-font-size-smaller-extra;
		padding: 10rpx;
	}

	.chat-msg-normal {
		position: relative;
		margin-bottom: 22rpx;
		padding-left: 110rpx;
		min-height: 80rpx;

		.avatar {
			position: absolute;
			top: 0;
			left: 0;
		}

		.chat-msg-content {
			text-align: left;

			.chat-msg-top {
				display: flex;
				flex-wrap: nowrap;
				color: $im-text-color-lighter;
				font-size: $im-font-size-smaller;
				line-height: $im-font-size-smaller;
				height: $im-font-size-smaller;

        .group-owner {
          background-color: orange;
          color: white;
          font-size: 14rpx;
          font-weight: 500;
          padding: 5rpx 12rpx;
          display: flex;
          align-items: center;
          border-radius: 16rpx;
        }

        .blogger {
          background-color: #1E90FF;
          color: white;
          font-size: 14rpx;
          font-weight: 500;
          padding: 5rpx 12rpx;
          display: flex;
          align-items: center;
          border-radius: 16rpx;
        }
			}

			.chat-msg-bottom {
				display: inline-block;
				padding-right: 80rpx;
				margin-top: 5rpx;

        .iconfont {
          font-size: 40rpx;
          margin: 0 6rpx;
          color: $icon-color;
        }

				.chat-msg-text {
					position: relative;
					line-height: 1.6;
					margin-top: 10rpx;
					padding: 16rpx 24rpx;
					background-color: $im-bg;
					border-radius: 20rpx;
					color: $im-text-color;
					font-size: $im-font-size;
					text-align: left;
					display: block;
					word-break: break-all;
					white-space: pre-line;
          display: inline-flex;
          overflow: visible;


					&:after {
						position: absolute;
						left: -20rpx;
						top: 26rpx;
						width: 6rpx;
						height: 6rpx;
						border-style: solid dashed dashed;
						border-color: $im-bg transparent transparent;
						overflow: hidden;
						border-width: 18rpx;
					}
				}

        .chat-msg-word-voice {
          display: flex;
          position: relative;
          line-height: 1.6;
          margin-top: 10rpx;
          padding: 16rpx 24rpx;
          background-color: $im-bg;
          border-radius: 20rpx;
          color: $im-text-color;
          font-size: $im-font-size;
          text-align: left;
          word-break: break-all;
          white-space: pre-line;


          &:after {
            content: "";
            position: absolute;
            left: -20rpx;
            top: 26rpx;
            width: 6rpx;
            height: 6rpx;
            border-style: solid dashed dashed;
            border-color: $im-bg transparent transparent;
            overflow: hidden;
            border-width: 18rpx;
          }

          .word-voice-box {
            display: flex;
            align-items: center;
          }
        }


				.chat-msg-image {
					display: flex;
					flex-wrap: nowrap;
					flex-direction: row;
					align-items: center;

					.img-load-box {
						position: relative;

						.send-image {
							min-width: 200rpx;
							max-width: 420rpx;
							height: 350rpx;
							cursor: pointer;
							border-radius: 4px;
						}
					}


					.send-fail {
						color: $im-color-danger;
						font-size: $im-font-size;
						cursor: pointer;
						margin: 0 20px;
					}
				}

				.chat-msg-file {
					display: flex;
					flex-wrap: nowrap;
					flex-direction: row;
					align-items: center;
					cursor: pointer;

					.chat-file-box {
						position: relative;
						display: flex;
						flex-wrap: nowrap;
						align-items: center;
						min-height: 60px;
						border-radius: 4px;
						padding: 10px 15px;
						box-shadow: $im-box-shadow-dark;

						.chat-file-info {
							flex: 1;
							height: 100%;
							text-align: left;
							font-size: 14px;
							width: 300rpx;

							.chat-file-name {
								font-weight: 600;
								margin-bottom: 15px;
								word-break: break-all;
							}
						}

						.chat-file-icon {
							font-size: 80rpx;
							color: #d42e07;
						}
					}

					.send-fail {
						color: #e60c0c;
						font-size: 50rpx;
						cursor: pointer;
						margin: 0 20rpx;
					}

				}

        .chat-msg-video {
          display: flex;
          align-items: center;

          .video-msg-box {
            width: 100%;
            position: relative;

            .send-video {
              width: 300rpx;
              height: 200rpx;
              border-radius: 10rpx;
              overflow: hidden;
              cursor: pointer;
              -o-object-fit: cover;
              object-fit: cover;
            }

            .video-cover-image {
              min-width: 200rpx;
              max-width: 420rpx;
              height: 350rpx;
              cursor: pointer;
              border-radius: 4px;
            }

            .play-icon {
              position: absolute;
              top: 50%;
              left: 50%;
              transform: translate(-50%, -50%);
              font-size: 100rpx;
              color: white;
            }
          }
        }

				.chat-msg-audio {
					display: flex;
					align-items: center;

					.chat-audio-text {
						padding-right: 8px;
					}

					.icon-voice-play {
						font-size: 18px;
						padding-right: 8px;
					}
				}

				.chat-realtime {
					display: flex;
					align-items: center;

					.iconfont {
						font-size: 20px;
						padding-right: 8px;
					}
				}

        .quote-message {

          .chat-quote-message {
            background: #eee;
            padding: 5rpx 10rpx;
            display: inline-flex;
            align-items: center;
            border-radius: 10rpx;
            font-size: 20rpx;
            color: #909399;
            margin-top: 8rpx;

            .send-user {
              margin-right: 20rpx;
              font-weight: 600;
              white-space: nowrap;
            }

            .quote-content {

              .quote-text {
                position: relative;
                line-height: 1.6;
                text-align: left;
                display: block;
                word-break: break-all;
                white-space: pre-line;
              }

              .quote-image {
                min-width: 120rpx;
                max-width: 160rpx;
                height: 120rpx;
                cursor: pointer;
                border-radius: 12rpx;
              }

              .quote-video {
                position: relative;

                .video-cover-image {
                  min-width: 120rpx;
                  max-width: 160rpx;
                  height: 120rpx;
                  cursor: pointer;
                  border-radius: 12rpx;
                }

                .play-icon {
                  position: absolute;
                  top: 50%;
                  left: 50%;
                  transform: translate(-50%, -50%);
                  font-size: 60rpx;
                  color: white;
                }
              }

              .quote-msg-file {
                display: flex;
                flex-wrap: nowrap;
                flex-direction: row;
                align-items: center;
                cursor: pointer;

                .quote-file-box {
                  position: relative;
                  display: flex;
                  flex-wrap: nowrap;
                  align-items: center;
                  min-height: 80rpx;

                  .quote-file-info {
                    flex: 1;
                    height: 100%;
                    text-align: left;
                    font-size: 26rpx;
                    width: 260rpx;

                    .quote-file-name {
                      font-weight: 600;
                      margin-bottom: 14rpx;
                      word-break: break-all;
                    }

                    .quote-file-size {
                      font-size: 18rpx;
                    }
                  }

                  .quote-file-icon {
                    font-size: 50rpx;
                    color: #d42e07;
                  }
                }
              }

              .quote-msg-audio {
                display: flex;
                align-items: center;
                padding: 6rpx 10rpx;

                .icon-voice-play {
                  font-size: 26rpx;
                  padding-right: 12rpx;
                }

                .quote-audio-text {
                  padding-right: 12rpx;
                }

                .icon-play {
                  font-size: 26rpx;
                }

                .icon-pause {
                  font-size: 26rpx;
                }
              }

              .quote-msg-word-voice {
                display: flex;
                align-items: center;

                .word {
                  width: 260rpx;
                }

                .voice {
                  padding-left: 10rpx;
                }
              }
            }
          }
        }

				.chat-msg-status {
					line-height: $im-font-size-smaller-extra;
					font-size: $im-font-size-smaller-extra;
					padding-top: 2rpx;

					.chat-readed {
						display: block;
						padding-top: 2rpx;
						color: $im-text-color-lighter;
					}

					.chat-unread {
						color: $im-color-danger;
					}
				}

				.chat-receipt {
					font-size: $im-font-size-smaller;
					color: $im-text-color-lighter;
					font-weight: 600;

					.icon-ok {
						font-size: 20px;
						color: $im-color-success;
					}
				}
			}
		}


		&.chat-msg-mine {
			text-align: right;
			padding-left: 0;
			padding-right: 110rpx;

			.avatar {
				left: auto;
				right: 0;
			}

			.chat-msg-content {
				text-align: right;

				.chat-msg-bottom {
					padding-left: 80rpx;
					padding-right: 0;

					.chat-msg-text {
						margin-left: 10px;
						background-color: $im-color-primary-light-2;
						color: #fff;

						&:after {
							left: auto;
							right: -9px;
							border-top-color: $im-color-primary-light-2;
						}
					}

          .chat-msg-word-voice {
            margin-left: 10px;
            background-color: $im-color-primary-light-2;
            color: #fff;

            &:after {
              left: auto;
              right: -9px;
              border-top-color: $im-color-primary-light-2;
            }
          }

					.chat-msg-image {
						flex-direction: row-reverse;
					}

					.chat-msg-file {
						flex-direction: row-reverse;
					}

          .chat-msg-video {
            flex-direction: row-reverse;
          }

					.chat-msg-audio {
						flex-direction: row-reverse;

						.chat-audio-text {
							padding-right: 0;
							padding-left: 8px;
						}

						.icon-voice-play {
							transform: rotateY(180deg);
						}
					}

					.chat-realtime {
						display: flex;
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