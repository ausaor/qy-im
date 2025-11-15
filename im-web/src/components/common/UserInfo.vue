<template>
	<div class="user-info-mask" @click="$emit('close')">
		<div class="user-info" :style="{left: pos.x+'px',top: pos.y+'px'}" @click.stop>
			<div class="user-card">
        <div class="user-header">
          <div class="avatar-section">
            <head-image :name="user.nickName" :url="user.headImage" :size="60" :online="user.online"
                        @click.native="showFullImage()"> </head-image>
          </div>
          <div class="user-basic-info">
            <div class="user-name-row">
              <span class="nick-name">{{ user.nickName }}</span>
              <i v-if="user.sex === 0" class="el-icon-male gender-icon male"/>
              <i v-if="user.sex === 1" class="el-icon-female gender-icon female"/>
              <span v-if="user.role === 'SUPER_ADMIN'" class="blogger">博主</span>
              <span v-if="user.role === 'ADMIN'" class="sys-admin">系统管理员</span>
            </div>
            <div class="user-id">
              <span>ID：{{user.userName}}</span>
            </div>
            <div class="user-region">
              <span>IP: {{ user.province }}</span>
            </div>
          </div>
        </div>
        <div class="signature-section">
          <div class="signature-label">个性签名</div>
          <div class="signature-content">{{ user.signature ? user.signature : "这个人很懒，什么也没留下" }}</div>
        </div>
        <div class="action-section" v-if="mine.id !== user.id">
          <el-button size="small" class="action-btn" v-show="isFriend" type="primary" @click="onSendMessage()"><i class="el-icon-position"></i><span style="margin-left: 5px;">发消息</span></el-button>
          <el-button size="small" class="action-btn" v-show="!isFriend && !isInFriendRequest" type="success" @click="onAddFriend()"><i class="el-icon-plus"></i><span style="margin-left: 5px;">加为好友</span></el-button>
          <div class="wait-status" v-show="!isFriend && isInFriendRequest">
            <i class="el-icon-time"></i>好友验证中
          </div>
        </div>
			</div>
		</div>
	</div>
</template>

<script>
	import HeadImage from './HeadImage.vue'

	export default {
		name: "userInfo",
		components: {
			HeadImage
		},
		data() {
			return {
	
			}
		},
		props: {
			user: {
				type: Object
			},
			pos: {
				type: Object
			}
		},
		methods: {
      onSendMessage() {
				let user = this.user;
				let chat = {
					type: 'PRIVATE',
					targetId: user.id,
					showName: user.nickName,
					headImage: user.headImage,
				};
				this.$store.commit("openChat", chat);
				this.$store.commit("activeChat", 0);
				if (this.$route.path != "/home/chat") {
					this.$router.push("/home/chat");
				}
				this.$emit("close");
			},
      onAddFriend() {
        if (this.user.friendReview) {
          this.$prompt(`提示: 对方开启了好友验证,等待对方同意后才能成为好友`, '申请添加好友', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputPlaceholder: `我是${this.mine.nickName}`,
            inputValue: `我是${this.mine.nickName}`,
            inputPattern: /\S/,
            inputErrorMessage: '请输入备注'
          }).then((data) => {
            let param = {
              friendId: this.user.id,
              remark: data.value
            }
            this.requestAddFriend(param)
          })
        } else {
          let param = {
            friendId: this.user.id
          }
          this.requestAddFriend(param)
        }
			},
      requestAddFriend(param) {
        this.$http({
          url: "/friend/add",
          method: "post",
          data: param
        }).then((data) => {
          if (data === 1) {
            this.$message.warning("申请成功，请等待对方通过")
          } else if (data === 2) {
            this.$message.success("申请成功，对方已成为您的好友");
          }
        })
      },
			showFullImage(){
				if(this.user.headImage){
					this.$store.commit("showFullImageBox", this.user.headImage);
				}
			}
		},
		computed: {
			isFriend() {
				let friends = this.$store.state.friendStore.friends;
				return friends.some(f => f.id === this.user.id && !f.deleted);
			},
      mine() {
        return this.$store.state.userStore.userInfo;
      },
      isInFriendRequest() {
        return this.$store.state.friendStore.friendRequest.some(f => f.status === 1 && (f.sendId === this.user.id || f.recvId === this.user.id));
      }
		}
	}
</script>

<style scoped lang="scss">
	.user-info-mask {
		background-color: rgba($color: #000000, $alpha: 0);
		position: absolute;
		width: 100%;
		height: 100%;
    z-index: 9999;
	}

	.user-info {
		position: absolute;
    width: 320px;
    background: hsla(0, 0%, 100%, .95);
    backdrop-filter: blur(20px);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, .12);
    border: 1px solid hsla(0, 0%, 100%, .2);
    overflow: hidden;

		.user-card {
			padding: 20px;

      .user-header {
        position: relative;
        display: flex;
        align-items: flex-start;
        gap: 16px;
        margin-bottom: 20px;

        .avatar-section {
          position: relative;
        }

        .user-basic-info {
          display: flex;
          flex-direction: column;
          justify-content: center;
          flex: 1;
          min-height: 70px;
          overflow: hidden;

          .user-name-row {
            gap: 8px;
            margin-bottom: 8px;

            .nick-name {
              font-size: 16px;
              font-weight: 700;
              line-height: 1.2;
              word-break: break-all;
            }

            .blogger {
              margin-left: 10px;
              background-color: #1E90FF;
              height: 15px;
              color: white;
              font-size: 12px;
              padding: 0 5px;
              border-radius: 10px;
              line-height: 15px;
            }

            .sys-admin {
              margin-left: 10px;
              background-color: #0A6E46;
              height: 15px;
              color: white;
              font-size: 12px;
              padding: 0 5px;
              border-radius: 10px;
              line-height: 15px;
            }

            .gender-icon {
              font-size: 16px;
              border-radius: 50%;
              padding: 2px;
              transition: all .3s ease;
              margin-left: 3px;
            }

            .gender-icon.male {
              color: #1890ff;
              background: rgba(24, 144, 255, .1);
            }

            .gender-icon.female {
              color: #f5222d;
              background: rgba(245, 34, 45, .1);
            }
          }

          .user-id {
            font-size: 14px;
            color: #000000;
            display: flex;
            align-items: center;
            word-break: break-all;
            gap: 6px;
          }

          .user-region {
            margin-top: 5px;
            font-size: 14px;
            color: #000000;
            display: flex;
            align-items: center;
            word-break: break-all;
            gap: 6px;
          }
        }
      }

      .signature-section {
        margin-bottom: 20px;
        padding: 16px;
        background: rgba(0, 0, 0, .02);
        border-radius: 12px;
        border: 1px solid rgba(0, 0, 0, .06);

        .signature-label {
          font-size: 12px;
          color: gray;
          font-weight: 500;
          margin-bottom: 8px;
          text-transform: uppercase;
          letter-spacing: .5px;
        }

        .signature-content {
          font-size: 14px;
          color: #000000;
          line-height: 1.5;
          font-style: italic;
          word-break: break-word;
        }
      }

      .action-section {
        text-align: center;

        .action-btn {
          margin-left: 0;
          width: 100%;
          padding: 10px 16px;
          border-radius: 8px;
          font-weight: 500;
          transition: all .3s ease;
          box-shadow: 0 2px 8px rgba(0, 0, 0, .1);
        }

        .wait-status {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          padding: 10px 16px;
          color: var(--im-text-color-light);
          font-size: 14px;
          background: rgba(0, 0, 0, .04);
          border-radius: 8px;

          i {
            font-size: 16px;
            color: #e6a23c;
          }
        }
      }
		}
	}
</style>
