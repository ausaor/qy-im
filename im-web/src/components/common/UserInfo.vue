<template>
	<div class="user-info-mask" @click="$emit('close')">
		<div class="user-info" :style="{left: pos.x+'px',top: pos.y+'px'}" @click.stop>
			<div class="user-info-box">
				<div class="avatar">
					<head-image :name="user.nickName" :url="user.headImage" :size="60" :online="user.online"
                @click.native="showFullImage()"> </head-image>
				</div>
        <div class="info-card">
          <div class="header">
            <div class="nick-name">{{ user.nickName }}</div>
            <div v-if="user.sex == 0" class="icon iconfont icon-man" style="color: darkblue;"></div>
            <div v-if="user.sex == 1" class="icon iconfont icon-woman" style="color: darkred;"></div>
            <div v-if="user.role === 'SUPER_ADMIN'" class="blogger">博主</div>
            <div v-if="user.role === 'ADMIN'" class="sys-admin">系统管理员</div>
          </div>
          <div class="info-item">
            用户名: {{ user.userName }}
          </div>
          <div class="info-item">
            地区: {{ user.province }}
          </div>
          <div class="signature">
            {{ user.signature }}
          </div>
        </div>
				
			</div>
			<el-divider content-position="center"></el-divider>
			<div class="user-btn-group">
				<el-button size="small" v-show="isFriend" type="primary" @click="onSendMessage()">发消息</el-button>
				<el-button size="small" v-show="!isFriend" type="primary" @click="onAddFriend()">加为好友</el-button>
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
		width: 300px;
		background-color: white;
		border: var(--border-color) solid 1px;
		border-radius: 5px;
		padding: 15px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);

		.user-info-box {
			display: flex;

      .info-card {
        flex: 1;
        padding-left: 10px;

        .header {
          display: flex;
          align-items: center;

          .nick-name {
            font-size: 16px;
            font-weight: 600;
          }

          .icon {
            margin-left: 3px;
            font-size: 14px;
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
        }

        .info-item {
          font-size: 14px;
          margin-top: 5px;
          word-break: break-all;
        }

        .signature {
          font-size: 13px;
          margin-top: 5px;
          color: #888;
          word-break: break-all;
        }
      }
		}

		.user-btn-group {
			text-align: center;
		}
	}
</style>
