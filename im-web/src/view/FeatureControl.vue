<template>
  <div class="feature-control">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <i class="el-icon-setting"></i>
        </div>
        <div class="header-text">
          <h2>系统功能控制台</h2>
          <p>全局管控各项功能模块的启用状态，关闭后对应功能将立即对所有用户生效</p>
        </div>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <span class="stat-value">{{ enabledCount }}</span>
          <span class="stat-label">已开启</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value disabled">{{ disabledCount }}</span>
          <span class="stat-label">已关闭</span>
        </div>
      </div>
    </div>

    <!-- 功能分类卡片 -->
    <div class="feature-sections">
      <!-- 上传功能 -->
      <div class="section-card">
        <div class="section-header">
          <div class="section-icon upload-icon">
            <i class="el-icon-upload2"></i>
          </div>
          <div class="section-title">
            <h3>上传功能</h3>
            <span>文件与媒体资源上传管控</span>
          </div>
        </div>
        <div class="feature-grid">
          <div
            v-for="feature in uploadFeatures"
            :key="feature.code"
            class="feature-card"
            :class="{ 'is-disabled': featureStates[feature.code] === false }"
          >
            <div class="feature-info">
              <div class="feature-icon" :style="{ background: feature.color }">
                <i :class="feature.icon"></i>
              </div>
              <div class="feature-meta">
                <span class="feature-name">{{ feature.name }}</span>
                <span class="feature-code">{{ feature.code }}</span>
              </div>
            </div>
            <div class="feature-controls">
              <el-switch
                v-model="featureStates[feature.code]"
                :active-value="true"
                :inactive-value="false"
                active-color="#13ce66"
                inactive-color="#dcdfe6"
                @change="(val) => handleSwitch(feature, val)"
              ></el-switch>
              <el-popover
                v-if="featureStates[feature.code] === false"
                placement="bottom"
                width="280"
                trigger="click"
                :ref="`popover-${feature.code}`"
              >
                <div class="duration-picker">
                  <p class="duration-label">设置关闭时长（可选）</p>
                  <div class="duration-options">
                    <el-button
                      v-for="opt in durationOptions"
                      :key="opt.value"
                      :type="tempDuration[feature.code] === opt.value ? 'primary' : 'default'"
                      size="mini"
                      @click="tempDuration[feature.code] = opt.value"
                    >{{ opt.label }}</el-button>
                  </div>
                  <el-input
                    v-model="tempDuration[feature.code]"
                    placeholder="自定义秒数"
                    size="mini"
                    type="number"
                    style="margin-top: 8px;"
                  >
                    <template slot="append">秒</template>
                  </el-input>
                  <div class="duration-actions">
                    <el-button size="mini" type="primary" @click="confirmDuration(feature)">
                      确认
                    </el-button>
                    <el-button size="mini" @click="cancelDuration(feature)">
                      保持永久
                    </el-button>
                  </div>
                </div>
                <el-button
                  slot="reference"
                  type="text"
                  size="mini"
                  class="duration-btn"
                >
                  定时关闭
                  <i class="el-icon-arrow-down"></i>
                </el-button>
              </el-popover>
            </div>
          </div>
        </div>
      </div>

      <!-- 消息功能 -->
      <div class="section-card">
        <div class="section-header">
          <div class="section-icon msg-icon">
            <i class="el-icon-chat-dot-round"></i>
          </div>
          <div class="section-title">
            <h3>消息功能</h3>
            <span>即时通讯消息发送管控</span>
          </div>
        </div>
        <div class="feature-grid">
          <div
            v-for="feature in messageFeatures"
            :key="feature.code"
            class="feature-card"
            :class="{ 'is-disabled': featureStates[feature.code] === false }"
          >
            <div class="feature-info">
              <div class="feature-icon" :style="{ background: feature.color }">
                <i :class="feature.icon"></i>
              </div>
              <div class="feature-meta">
                <span class="feature-name">{{ feature.name }}</span>
                <span class="feature-code">{{ feature.code }}</span>
              </div>
            </div>
            <div class="feature-controls">
              <el-switch
                v-model="featureStates[feature.code]"
                :active-value="true"
                :inactive-value="false"
                active-color="#13ce66"
                inactive-color="#dcdfe6"
                @change="(val) => handleSwitch(feature, val)"
              ></el-switch>
              <el-popover
                v-if="featureStates[feature.code] === false"
                placement="bottom"
                width="280"
                trigger="click"
                :ref="`popover-${feature.code}`"
              >
                <div class="duration-picker">
                  <p class="duration-label">设置关闭时长（可选）</p>
                  <div class="duration-options">
                    <el-button
                      v-for="opt in durationOptions"
                      :key="opt.value"
                      :type="tempDuration[feature.code] === opt.value ? 'primary' : 'default'"
                      size="mini"
                      @click="tempDuration[feature.code] = opt.value"
                    >{{ opt.label }}</el-button>
                  </div>
                  <el-input
                    v-model="tempDuration[feature.code]"
                    placeholder="自定义秒数"
                    size="mini"
                    type="number"
                    style="margin-top: 8px;"
                  >
                    <template slot="append">秒</template>
                  </el-input>
                  <div class="duration-actions">
                    <el-button size="mini" type="primary" @click="confirmDuration(feature)">
                      确认
                    </el-button>
                    <el-button size="mini" @click="cancelDuration(feature)">
                      保持永久
                    </el-button>
                  </div>
                </div>
                <el-button
                  slot="reference"
                  type="text"
                  size="mini"
                  class="duration-btn"
                >
                  定时关闭
                  <i class="el-icon-arrow-down"></i>
                </el-button>
              </el-popover>
            </div>
          </div>
        </div>
      </div>

      <!-- 社交互动功能 -->
      <div class="section-card">
        <div class="section-header">
          <div class="section-icon social-icon">
            <i class="el-icon-share"></i>
          </div>
          <div class="section-title">
            <h3>社交互动</h3>
            <span>用户社交与内容互动管控</span>
          </div>
        </div>
        <div class="feature-grid">
          <div
            v-for="feature in socialFeatures"
            :key="feature.code"
            class="feature-card"
            :class="{ 'is-disabled': featureStates[feature.code] === false }"
          >
            <div class="feature-info">
              <div class="feature-icon" :style="{ background: feature.color }">
                <i :class="feature.icon"></i>
              </div>
              <div class="feature-meta">
                <span class="feature-name">{{ feature.name }}</span>
                <span class="feature-code">{{ feature.code }}</span>
              </div>
            </div>
            <div class="feature-controls">
              <el-switch
                v-model="featureStates[feature.code]"
                :active-value="true"
                :inactive-value="false"
                active-color="#13ce66"
                inactive-color="#dcdfe6"
                @change="(val) => handleSwitch(feature, val)"
              ></el-switch>
              <el-popover
                v-if="featureStates[feature.code] === false"
                placement="bottom"
                width="280"
                trigger="click"
                :ref="`popover-${feature.code}`"
              >
                <div class="duration-picker">
                  <p class="duration-label">设置关闭时长（可选）</p>
                  <div class="duration-options">
                    <el-button
                      v-for="opt in durationOptions"
                      :key="opt.value"
                      :type="tempDuration[feature.code] === opt.value ? 'primary' : 'default'"
                      size="mini"
                      @click="tempDuration[feature.code] = opt.value"
                    >{{ opt.label }}</el-button>
                  </div>
                  <el-input
                    v-model="tempDuration[feature.code]"
                    placeholder="自定义秒数"
                    size="mini"
                    type="number"
                    style="margin-top: 8px;"
                  >
                    <template slot="append">秒</template>
                  </el-input>
                  <div class="duration-actions">
                    <el-button size="mini" type="primary" @click="confirmDuration(feature)">
                      确认
                    </el-button>
                    <el-button size="mini" @click="cancelDuration(feature)">
                      保持永久
                    </el-button>
                  </div>
                </div>
                <el-button
                  slot="reference"
                  type="text"
                  size="mini"
                  class="duration-btn"
                >
                  定时关闭
                  <i class="el-icon-arrow-down"></i>
                </el-button>
              </el-popover>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FeatureControl',
  data() {
    return {
      // 上传功能列表
      uploadFeatures: [
        {
          code: 'FEATURE_IMAGE_UPLOAD',
          name: '图片上传',
          icon: 'el-icon-picture-outline',
          color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
        },
        {
          code: 'FEATURE_VIDEO_UPLOAD',
          name: '视频上传',
          icon: 'el-icon-video-camera',
          color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
        },
        {
          code: 'FEATURE_AUDIO_UPLOAD',
          name: '音频上传',
          icon: 'el-icon-microphone',
          color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
        },
        {
          code: 'FEATURE_FILE_UPLOAD',
          name: '文件上传',
          icon: 'el-icon-folder-opened',
          color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
        }
      ],
      // 消息功能列表
      messageFeatures: [
        {
          code: 'FEATURE_PRIVATE_MESSAGE_SEND',
          name: '发送私聊消息',
          icon: 'el-icon-chat-line-square',
          color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
        },
        {
          code: 'FEATURE_GROUP_MESSAGE_SEND',
          name: '发送群聊消息',
          icon: 'el-icon-chat-round',
          color: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)'
        },
        {
          code: 'FEATURE_REGIONAL_GROUP_MESSAGE_SEND',
          name: '发送地区群聊消息',
          icon: 'el-icon-location-outline',
          color: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)'
        }
      ],
      // 社交互动功能列表
      socialFeatures: [
        {
          code: 'FEATURE_REGISTER',
          name: '用户注册',
          icon: 'el-icon-user',
          color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
        },
        {
          code: 'FEATURE_MUSIC_PUBLISH',
          name: '新增音乐',
          icon: 'el-icon-headset',
          color: 'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)'
        },
        {
          code: 'FEATURE_DYNAMIC_PUBLISH',
          name: '新增动态',
          icon: 'el-icon-edit-outline',
          color: 'linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%)'
        },
        {
          code: 'FEATURE_DYNAMIC_LIKE',
          name: '动态点赞',
          icon: 'el-icon-star-off',
          color: 'linear-gradient(135deg, #fddb92 0%, #d1fdff 100%)'
        },
        {
          code: 'FEATURE_DYNAMIC_COMMENT',
          name: '动态评论',
          icon: 'el-icon-chat-dot-square',
          color: 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)'
        }
      ],
      // 功能状态缓存: true=开启, false=关闭
      featureStates: {},
      // 临时时长选择
      tempDuration: {},
      // 时长快捷选项
      durationOptions: [
        { label: '5分钟', value: 300 },
        { label: '15分钟', value: 900 },
        { label: '30分钟', value: 1800 },
        { label: '1小时', value: 3600 },
        { label: '3小时', value: 10800 },
        { label: '12小时', value: 43200 },
        { label: '24小时', value: 86400 }
      ]
    };
  },
  computed: {
    allFeatures() {
      return [
        ...this.uploadFeatures,
        ...this.messageFeatures,
        ...this.socialFeatures
      ];
    },
    enabledCount() {
      return this.allFeatures.filter(f => this.featureStates[f.code] !== false).length;
    },
    disabledCount() {
      return this.allFeatures.filter(f => this.featureStates[f.code] === false).length;
    }
  },
  created() {
    this.initStates();
  },
  methods: {
    initStates() {
      this.$http({
        url: '/system/featureStates',
        method: 'get'
      }).then((res) => {
        const list = res || [];
        list.forEach(item => {
          this.$set(this.featureStates, item.featureCode, item.isOpen);
          this.$set(this.tempDuration, item.featureCode, null);
        });
      }).catch(() => {
        // 请求失败，兜底全部默认开启
        const allFeatures = [
          ...this.uploadFeatures,
          ...this.messageFeatures,
          ...this.socialFeatures
        ];
        allFeatures.forEach(f => {
          if (!(f.code in this.featureStates)) {
            this.$set(this.featureStates, f.code, true);
            this.$set(this.tempDuration, f.code, null);
          }
        });
      });
    },
    handleSwitch(feature, isOpen) {
      const dto = {
        featureCode: feature.code,
        isOpen: isOpen,
        duration: null
      };

      if (!isOpen && this.tempDuration[feature.code]) {
        dto.duration = this.tempDuration[feature.code];
      }

      this.$http({
        url: '/system/switchFeature',
        method: 'post',
        data: dto
      }).then(() => {
        const action = isOpen ? '已开启' : '已关闭';
        const durationMsg = dto.duration ? `，${dto.duration}秒后自动恢复` : '';
        this.$message.success(`${feature.name}${action}${durationMsg}`);
        // 重置临时时长
        this.tempDuration[feature.code] = null;
      }).catch(() => {
        // 请求失败，回滚状态
        this.featureStates[feature.code] = !isOpen;
        this.$message.error(`操作失败，${feature.name}状态未能更新`);
      });
    },
    confirmDuration(feature) {
      const duration = this.tempDuration[feature.code];
      if (duration === null || duration === undefined || duration === '') {
        // 没选时长，直接关闭永久
        this.handleSwitch(feature, false);
      } else {
        this.handleSwitch(feature, false);
      }
    },
    cancelDuration(feature) {
      // 不设时长，永久关闭
      this.tempDuration[feature.code] = null;
      this.handleSwitch(feature, false);
    }
  }
};
</script>


<style scoped lang="scss">
.feature-control {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(135deg, #f0f2f5 0%, #e8ecf1 50%, #f5f6fa 100%);
}

// ==================== 页面头部 ====================
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(15, 52, 96, 0.25);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -10%;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(102, 126, 234, 0.15) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -30%;
    left: 20%;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(79, 172, 254, 0.1) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.header-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.1);

  i {
    font-size: 28px;
    color: #a8b8ff;
  }
}

.header-text {
  h2 {
    margin: 0;
    font-size: 22px;
    font-weight: 700;
    color: #e8ecf1;
    letter-spacing: 1px;
  }

  p {
    margin: 6px 0 0;
    font-size: 13px;
    color: rgba(232, 236, 241, 0.6);
    letter-spacing: 0.5px;
  }
}

.header-stats {
  display: flex;
  align-items: center;
  gap: 24px;
  position: relative;
  z-index: 1;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #13ce66;
  font-variant-numeric: tabular-nums;

  &.disabled {
    color: #f56c6c;
  }
}

.stat-label {
  font-size: 12px;
  color: rgba(232, 236, 241, 0.55);
  letter-spacing: 1px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
}

// ==================== 功能分类区 ====================
.feature-sections {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: box-shadow 0.3s ease;

  &:hover {
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 24px;
  background: linear-gradient(120deg, #f8f9fc 0%, #eef0f5 100%);
  border-bottom: 1px solid #ebeef5;
}

.section-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;

  i {
    font-size: 20px;
    color: #fff;
  }
}

.upload-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.msg-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.social-icon {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
}

.section-title {
  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  span {
    font-size: 12px;
    color: #909399;
    margin-top: 2px;
    display: block;
  }
}

// ==================== 功能卡片网格 ====================
.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
  padding: 20px 24px;
}

.feature-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  background: linear-gradient(120deg, #fafbfc 0%, #f5f7fa 100%);
  border-radius: 12px;
  border: 1px solid #ebeef5;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
    border-color: #d0d5dd;
  }

  &.is-disabled {
    background: linear-gradient(120deg, #fef0f0 0%, #fdf6f6 100%);
    border-color: #fde2e2;

    .feature-name {
      color: #909399;
    }
  }
}

.feature-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.feature-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

  i {
    font-size: 20px;
    color: #fff;
  }
}

.feature-meta {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.feature-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  transition: color 0.3s ease;
}

.feature-code {
  font-size: 11px;
  color: #909399;
  font-family: 'SF Mono', 'Monaco', 'Menlo', 'Consolas', monospace;
  letter-spacing: 0.5px;
}

.feature-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.duration-btn {
  font-size: 11px;
  color: #909399;
  padding: 2px 6px;
  transition: color 0.2s;

  &:hover {
    color: #409eff;
  }

  i {
    font-size: 10px;
    margin-left: 2px;
  }
}

// ==================== 时长选择弹窗 ====================
.duration-picker {
  .duration-label {
    margin: 0 0 10px;
    font-size: 13px;
    color: #606266;
    font-weight: 500;
  }

  .duration-options {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .duration-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 10px;
  }
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .feature-control {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    padding: 20px;
  }

  .header-stats {
    width: 100%;
    justify-content: space-around;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }

  .feature-card {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;

    .feature-controls {
      align-self: flex-end;
    }
  }
}
</style>