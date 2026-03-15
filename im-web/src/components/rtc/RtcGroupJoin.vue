<template>
  <div class="rtc-group-join-overlay">
    <div class="rtc-group-join">
      <div class="join-header">
        <div class="header-icon">
          <i class="el-icon-video-camera"></i>
        </div>
        <div class="header-text">邀请加入通话</div>
      </div>
      
      <div class="host-info">
        <head-image :name="host.aliasName" :url="host.headImage" :size="60"></head-image>
        <div class="host-name">{{host.aliasName}}</div>
        <div class="host-label">发起人</div>
      </div>
      
      <div class="users-info">
        <div class="user-count">{{userInfos.length}}人正在通话中</div>
        <div class="user-list">
          <div class="user-item" v-for="user in userInfos" :key="user.id">
            <head-image :url="user.headImage" :name="user.aliasName" :size="36">
            </head-image>
          </div>
        </div>
      </div>
      
      <div class="btn-group">
        <div class="icon iconfont icon-icon_phone_reject_nor reject" @click="onReject()" title="拒绝"></div>
        <div class="icon iconfont icon-icon_phone_accept_hover accept" @click="onOk()" title="接受"></div>
      </div>
    </div>
  </div>
</template>

<script>
	import HeadImage from '@/components/common/HeadImage'

	export default{
		name: "rtcGroupJoin",
		components:{
			HeadImage
		},
    props: {
      host: {
        type: Object,
        required: true,
        default: function () {
          return {}
        }
      },
      groupId: {
        type: String,
        required: true
      },
      userInfos: {
        type: Array,
        required: true,
        default: function () {
          return []
        }
      }
    },
		data() {
			return {}
		},
		methods: {
			onOk() {
				let userInfos = this.userInfos;
				let mine = this.$store.state.userStore.userInfo;
				if(!userInfos.find((user)=>user.id==mine.id)){
					// 加入自己的信息
					userInfos.push({
						id: mine.id,
						aliasName: mine.nickName,
						headImage: mine.headImage,
						isCamera: false,
						isMicroPhone: true
					})
				}
				let rtcInfo = {
					isHost: false,
					groupId: this.groupId,
					inviterId: this.host?.id || mine.id,
					host: this.host,
					userInfos: userInfos,
          joinMidway: false,
				}
        this.$emit("accept", rtcInfo)
			},
			onReject(){
        this.$emit("reject", this.groupId);
			}
		}
	}
</script>

<style lang="scss" scoped>
.rtc-group-join-overlay {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
  animation: slideInRight 0.3s ease-out;
  
  @keyframes slideInRight {
    from {
      transform: translateX(100%);
      opacity: 0;
    }
    to {
      transform: translateX(0);
      opacity: 1;
    }
  }
}

.rtc-group-join {
  width: 320px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.1);
  overflow: hidden;
  color: white;
  
  .join-header {
    display: flex;
    align-items: center;
    padding: 16px 20px;
    background: rgba(0, 0, 0, 0.2);
    
    .header-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      
      i {
        font-size: 20px;
      }
    }
    
    .header-text {
      font-size: 16px;
      font-weight: 600;
      letter-spacing: 0.5px;
    }
  }
  
  .host-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24px 20px 16px;
    background: rgba(255, 255, 255, 0.1);
    
    .host-name {
      margin-top: 12px;
      font-size: 18px;
      font-weight: 600;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }
    
    .host-label {
      margin-top: 4px;
      font-size: 12px;
      opacity: 0.8;
      background: rgba(255, 255, 255, 0.2);
      padding: 2px 12px;
      border-radius: 12px;
    }
  }
  
  .users-info {
    padding: 16px 20px;
    
    .user-count {
      text-align: center;
      font-size: 14px;
      margin-bottom: 12px;
      opacity: 0.9;
    }
    
    .user-list {
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 8px;
      max-height: 80px;
      overflow-y: auto;
      
      &::-webkit-scrollbar {
        width: 4px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: rgba(255, 255, 255, 0.3);
        border-radius: 2px;
      }
      
      .user-item {

      }
    }
  }
  
  .btn-group {
    display: flex;
    justify-content: space-around;
    align-items: center;
    padding: 20px;
    background: rgba(0, 0, 0, 0.2);
    
    .icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      background: rgba(255, 255, 255, 0.1);
      backdrop-filter: blur(10px);
      font-size: 28px;
      
      &:hover {
        transform: scale(1.1);
        background: rgba(255, 255, 255, 0.2);
      }
      
      &:active {
        transform: scale(0.95);
      }
      
      &.accept {
        color: #4ade80;
        animation: acceptPulse 2s ease-in-out infinite;
        
        @keyframes acceptPulse {
          0%, 100% {
            box-shadow: 0 0 0 0 rgba(74, 222, 128, 0.4);
          }
          50% {
            box-shadow: 0 0 0 12px rgba(74, 222, 128, 0);
          }
        }
      }
      
      &.reject {
        color: #f87171;
        
        &:hover {
          background: rgba(248, 113, 113, 0.2);
        }
      }
    }
  }
}
</style>