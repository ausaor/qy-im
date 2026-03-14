<template>
  <div class="rtc-private-acceptor">
    <div class="acceptor-header">
      <div class="avatar-wrapper">
        <head-image :id="friend.id" :name="friend.nickName" :url="friend.headImage" :size="80"
                    :isShowUserInfo="false"></head-image>
        <div class="calling-indicator">
          <div class="pulse-ring"></div>
        </div>
      </div>
      <div class="mode-badge" :class="mode">
        <span class="icon iconfont" :class="mode === 'video' ? 'icon-video' : 'icon-phone'"></span>
        {{ mode === 'video' ? '视频通话' : '语音通话' }}
      </div>
    </div>
    
    <div class="acceptor-content">
      <div class="caller-name">{{ friend.nickName }}</div>
      <div class="acceptor-text">
        {{ tip }}
      </div>
    </div>
    
    <div class="btn-group">
      <div class="action-btn reject-btn" @click="$emit('reject')" title="拒绝">
        <span class="icon iconfont icon-icon_phone_reject_nor"></span>
      </div>
      <div class="action-btn accept-btn" @click="$emit('accept')" title="接受">
        <span class="icon iconfont icon-icon_phone_accept_hover"></span>
      </div>
    </div>
  </div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';

export default {
  name: "rtcPrivateAcceptor",
  components: {
    HeadImage
  },
  data() {
    return {}
  },
  props: {
    mode: {
      type: String
    },
    friend: {
      type: Object
    }
  },
  computed: {
    tip() {
      let modeText = this.mode == "video" ? "视频" : "语音"
      return `${this.friend.nickName} 请求和您进行${modeText}通话...`
    }
  }
}
</script>

<style scoped lang="scss">
.rtc-private-acceptor {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  right: 5px;
  bottom: 5px;
  width: 320px;
  padding: 24px;
  background: linear-gradient(145deg, #ffffff 0%, #f8f9ff 100%);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 4px 16px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(255, 255, 255, 0.8) inset;
  border-radius: 20px;
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    box-shadow: 
      0 12px 48px rgba(0, 0, 0, 0.16),
      0 6px 24px rgba(0, 0, 0, 0.12),
      0 0 0 1px rgba(255, 255, 255, 1) inset;
    transform: translateY(-2px);
  }

  .acceptor-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    margin-bottom: 16px;

    .avatar-wrapper {
      position: relative;
      margin-bottom: 12px;

      .pulse-ring {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 100px;
        height: 100px;
        border-radius: 50%;
        border: 3px solid rgba(64, 158, 255, 0.3);
        animation: pulse 2s ease-out infinite;
        z-index: -1;

        @keyframes pulse {
          0% {
            width: 80px;
            height: 80px;
            opacity: 1;
            border-color: rgba(64, 158, 255, 0.5);
          }
          100% {
            width: 140px;
            height: 140px;
            opacity: 0;
            border-color: rgba(64, 158, 255, 0);
          }
        }
      }
    }

    .mode-badge {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 16px;
      border-radius: 20px;
      font-size: 13px;
      font-weight: 500;
      background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
      color: #1976d2;
      box-shadow: 0 2px 8px rgba(25, 118, 210, 0.15);

      &.video {
        background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
        color: #7b1fa2;
        box-shadow: 0 2px 8px rgba(123, 31, 162, 0.15);
      }

      .icon {
        font-size: 16px;
      }
    }
  }

  .acceptor-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    margin-bottom: 20px;

    .caller-name {
      font-size: 20px;
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 8px;
      text-align: center;
      letter-spacing: 0.5px;
    }

    .acceptor-text {
      font-size: 14px;
      color: #7f8c8d;
      text-align: center;
      line-height: 1.5;
      max-width: 280px;
    }
  }

  .btn-group {
    display: flex;
    justify-content: center;
    gap: 16px;
    width: 100%;

    .action-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 16px 24px;
      border-radius: 16px;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      min-width: 100px;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: inherit;
        opacity: 0;
        transition: opacity 0.3s ease;
        z-index: -1;
      }

      &:hover {
        transform: translateY(-3px);
        
        &::before {
          opacity: 0.1;
        }

        .icon {
          transform: scale(1.1);
        }
      }

      &:active {
        transform: translateY(-1px) scale(0.98);
      }

      .icon {
        font-size: 42px;
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      }

      .btn-text {
        font-size: 14px;
        font-weight: 500;
      }

      &.accept-btn {
        background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
        box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
        border: 2px solid rgba(76, 175, 80, 0.2);

        .icon {
          color: #4caf50;
          animation: acceptPulse 2s ease-in-out infinite;
        }

        .btn-text {
          color: #2e7d32;
        }

        &:hover {
          background: linear-gradient(135deg, #c8e6c9 0%, #a5d6a7 100%);
          box-shadow: 0 6px 20px rgba(76, 175, 80, 0.3);
        }

        @keyframes acceptPulse {
          0%, 100% {
            transform: scale(1);
            filter: brightness(1);
          }
          50% {
            transform: scale(1.05);
            filter: brightness(1.1);
          }
        }
      }

      &.reject-btn {
        background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
        box-shadow: 0 4px 12px rgba(244, 67, 54, 0.2);
        border: 2px solid rgba(244, 67, 54, 0.2);

        .icon {
          color: #f44336;
        }

        .btn-text {
          color: #c62828;
        }

        &:hover {
          background: linear-gradient(135deg, #ffcdd2 0%, #ef9a9a 100%);
          box-shadow: 0 6px 20px rgba(244, 67, 54, 0.3);
        }
      }
    }
  }
}
</style>
