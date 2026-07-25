<template>
  <div
    class="friend-item"
    :class="{ active }"
    @click="handleClick"
  >
    <div class="friend-avatar">
      <head-image
        :url="item.headImage"
        :name="item.nickName"
        :size="collapsed ? 42 : 36"
        :online="item.online"
      ></head-image>
      <span v-if="newVideoCount > 0" class="new-badge">{{ newVideoCount }}</span>
    </div>
    <div v-if="!collapsed" class="friend-info">
      <div class="friend-name">{{ item.friendRemark || item.nickName }}</div>
      <div v-if="item.signature" class="friend-signature">{{ item.signature }}</div>
    </div>
  </div>
</template>

<script>
import HeadImage from '@/components/common/HeadImage.vue'

export default {
  name: 'ShortVideoFriendItem',
  components: {
    HeadImage
  },
  props: {
    item: {
      type: Object,
      required: true
    },
    collapsed: {
      type: Boolean,
      default: false
    },
    active: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    newVideoCount() {
      const videos = this.$store.state.shortVideoStore.shortVideoMap.get(this.videoKey)
      return videos ? videos.length : 0
    },
    videoKey() {
      return this.item.id + '-user'
    }
  },
  watch: {
    active(isActive) {
      if (isActive) {
        this.handleActivate()
      }
    }
  },
  mounted() {
    if (this.active) {
      this.handleActivate()
    }
  },
  methods: {
    handleClick() {
      if (this.active) {
        this.handleActivate()
      }
      this.$emit('select', this.item)
    },
    handleActivate() {
      if (this.newVideoCount > 0) {
        this.readedTargetNotify()
      }
      this.$store.commit('resetShortVideo', this.videoKey)
    },
    readedTargetNotify() {
      this.$http({
        url: `/shortVideoNotify/readed?targetId=${this.item.id}&targetType=user`,
        method: 'post'
      })
    }
  }
}
</script>

<style scoped lang="scss">
.friend-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.15s;
  gap: 10px;

  &:hover {
    background: #f5f7fa;
  }

  &.active {
    background: #ecf5ff;

    .friend-name {
      color: #409EFF;
    }
  }
}

.friend-avatar {
  flex-shrink: 0;
  position: relative;

  .new-badge {
    position: absolute;
    top: -6px;
    right: -6px;
    min-width: 18px;
    height: 18px;
    line-height: 18px;
    border-radius: 9px;
    background: #F56C6C;
    color: #fff;
    font-size: 11px;
    text-align: center;
    padding: 0 4px;
    white-space: nowrap;
    box-shadow: 0 0 0 2px #fff;
  }
}

.friend-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.friend-name {
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.friend-signature {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
