<template>
  <el-dialog title="" :visible.sync="isShow" width="420px" class="rtc-enter-dialog" :show-close="false">
    <div class="rtc-group-join">
      <!-- 标题区域 -->
      <div class="dialog-header">
        <div class="header-icon">
          <i class="el-icon-video-play"></i>
        </div>
        <h3 class="dialog-title">是否加入通话？</h3>
      </div>

      <!-- 发起人信息 -->
      <div class="host-card">
        <div class="host-avatar">
          <head-image :name="rtcInfo.host.aliasName" :url="rtcInfo.host.headImage" :size="70"></head-image>
          <div class="host-badge"></div>
        </div>
        <div class="host-details">
          <div class="host-label">发起人</div>
          <div class="host-name">{{ rtcInfo.host.aliasName }}</div>
        </div>
      </div>

      <!-- 通话中用户 -->
      <div class="users-card">
        <div class="users-header">
          <span class="user-count">
            <i class="el-icon-user"></i>
            {{ rtcInfo.userInfos.length }}人正在通话中
          </span>
        </div>
        <div class="user-list">
          <div class="user-item" v-for="(user, index) in rtcInfo.userInfos" :key="user.id">
            <el-tooltip :content="user.aliasName" placement="top" effect="light">
              <div class="avatar-wrapper">
                <head-image :url="user.headImage" :name="user.aliasName" :size="45"></head-image>
                <div class="user-status"></div>
              </div>
            </el-tooltip>
          </div>
          <div v-if="rtcInfo.userInfos.length === 0" class="no-users">
            <i class="el-icon-user-solid"></i>
            <span>暂无其他用户</span>
          </div>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="dialog-footer">
        <el-button @click="onCancel()" class="btn-cancel">
          <i class="el-icon-close"></i>取消
        </el-button>
        <el-button type="primary" @click="onJoin()" class="btn-join">
          <i class="el-icon-video-camera"></i>立即加入
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import HeadImage from '@/components/common/HeadImage'
import RtcGroupApi from "@/api/rtcGroupApi";

export default{
  name: "rtcGroupJoin",
  components:{
    HeadImage
  },
  data() {
    return {
      isShow: false,
      rtcInfo: {
        host: {},
        userInfos:[],
        groupId: null,
        myMemberInfo: {}
      },
      API: new RtcGroupApi(), // API
    }
  },
  methods: {
    open(rtcInfo) {
      this.rtcInfo = rtcInfo;
      this.isShow = true;
    },
    onJoin() {
      let userInfos = this.rtcInfo.userInfos;
      userInfos.push({
        id: this.rtcInfo.myMemberInfo.userId,
        aliasName: this.rtcInfo.myMemberInfo.aliasName,
        headImage: this.rtcInfo.myMemberInfo.headImage,
        isCamera: false,
        isMicroPhone: true
      })
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
        groupId: this.rtcInfo.groupId,
        inviterId: this.rtcInfo.host?.id,
        host: this.rtcInfo.host,
        userInfos: userInfos,
        joinMidway: true,
      }
      this.API.join(this.rtcInfo.groupId).then(() => {
        // 通过 home.vue 打开多人视频窗口
        this.$eventBus.$emit("openGroupVideo", rtcInfo);
        this.isShow = false;
      })
    },
    onCancel(){
      this.isShow = false;
    }
  }
}
</script>

<style lang="scss" scoped>
.rtc-enter-dialog ::v-deep .el-dialog {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  
  .el-dialog__header {
    padding: 0;
    margin-right: 0;
  }
  
  .el-dialog__body {
    padding: 0;
  }
}

.rtc-group-join {
  padding: 24px;
  
  // 头部区域
  .dialog-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 24px;
    
    .header-icon {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12px;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      
      i {
        font-size: 32px;
        color: white;
      }
    }
    
    .dialog-title {
      font-size: 20px;
      font-weight: 600;
      color: #2c3e50;
      margin: 0;
      text-align: center;
    }
  }

  // 发起人卡片
  .host-card {
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%);
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
    border: 1px solid rgba(102, 126, 234, 0.1);
    
    .host-avatar {
      position: relative;
      flex-shrink: 0;
      
      .host-badge {
        position: absolute;
        bottom: 2px;
        right: 2px;
        width: 14px;
        height: 14px;
        background: linear-gradient(135deg, #67c23a 0%, #52c41a 100%);
        border: 2px solid white;
        border-radius: 50%;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
      }
    }
    
    .host-details {
      flex: 1;
      
      .host-label {
        font-size: 12px;
        color: #909399;
        margin-bottom: 4px;
        font-weight: 500;
      }
      
      .host-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }
  }

  // 用户列表卡片
  .users-card {
    background: #ffffff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 24px;
    border: 1px solid #ebeef5;
    
    .users-header {
      margin-bottom: 16px;
      
      .user-count {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;
        font-weight: 600;
        color: #606266;
        background: #f0f2f5;
        padding: 6px 12px;
        border-radius: 16px;
        
        i {
          font-size: 16px;
          color: #667eea;
        }
      }
    }
    
    .user-list {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      justify-content: flex-start;
      min-height: 50px;
      
      .user-item {
        .avatar-wrapper {
          position: relative;
          transition: transform 0.2s ease;
          cursor: default;
          
          &:hover {
            transform: scale(1.08);
          }
          
          .user-status {
            position: absolute;
            bottom: 0px;
            right: 0px;
            width: 12px;
            height: 12px;
            background: linear-gradient(135deg, #67c23a 0%, #52c41a 100%);
            border: 2px solid white;
            border-radius: 50%;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
          }
        }
      }
      
      .no-users {
        width: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 16px;
        color: #c0c4cc;
        font-size: 13px;
        
        i {
          font-size: 32px;
          margin-bottom: 8px;
          opacity: 0.3;
        }
      }
    }
  }

  // 底部按钮
  .dialog-footer {
    display: flex;
    gap: 12px;
    justify-content: center;
    
    .btn-cancel {
      min-width: 100px;
      padding: 12px 24px;
      border-radius: 8px;
      border-color: #dcdfe6;
      color: #606266;
      font-size: 14px;
      font-weight: 500;
      transition: all 0.3s ease;
      
      &:hover {
        border-color: #909399;
        color: #909399;
      }
      
      i {
        margin-right: 4px;
      }
    }
    
    .btn-join {
      min-width: 120px;
      padding: 12px 28px;
      border-radius: 8px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      font-size: 14px;
      font-weight: 600;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
      }
      
      &:active {
        transform: translateY(0);
      }
      
      i {
        margin-right: 6px;
      }
    }
  }
}
</style>