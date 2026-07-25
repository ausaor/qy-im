<template>
  <div
    class="follow-item"
    :class="{ active }"
    @click="handleClick"
  >
    <div class="follow-avatar">
      <head-image
        :url="item.targetAvatar"
        :name="item.targetName"
        :size="collapsed ? 42 : 36"
      ></head-image>
      <span v-if="newVideoCount > 0" class="new-badge">{{ newVideoCount }}</span>
    </div>
    <div v-if="!collapsed" class="follow-info">
      <div class="follow-name">
        <span class="type-star" :style="{ color: starColor }">★</span>
        {{ item.targetName }}
      </div>
      <div class="follow-type">{{ typeName }}</div>
    </div>
  </div>
</template>

<script>
import HeadImage from '@/components/common/HeadImage.vue'

export default {
  name: 'ShortVideoFollowItem',
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
      const key = this.item.targetId + '-' + this.item.type
      const videos = this.$store.state.shortVideoStore.shortVideoMap.get(key)
      return videos ? videos.length : 0
    },
    starColor() {
      const colorMap = {
        user: '#409EFF',
        group: '#67C23A',
        character: '#E6A23C',
        template: '#9B59B6'
      }
      return colorMap[this.item.type] || '#999'
    },
    typeName() {
      const nameMap = {
        user: '用户',
        group: '群组',
        character: '角色',
        template: '群聊模板'
      }
      return nameMap[this.item.type] || this.item.type
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
      this.$store.commit('resetShortVideo', this.item.targetId + '-' + this.item.type)
    },
  }
}
</script>

<style scoped lang="scss">
.follow-item {
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

    .follow-name {
      color: #409EFF;
    }
  }
}

.follow-avatar {
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

.follow-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.follow-name {
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  gap: 4px;

  .type-star {
    font-size: 14px;
    flex-shrink: 0;
  }
}

.follow-type {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
</style>
