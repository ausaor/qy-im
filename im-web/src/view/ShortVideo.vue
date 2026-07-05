<template>
  <div class="short-video-container">
    <!-- 左侧菜单栏 -->
    <div class="side-menu">
      <div
        v-for="item in menuItems"
        :key="item.path"
        class="menu-item"
        :class="{ active: currentPath === item.path }"
        @click="navigateTo(item)"
      >
        <span
          class="menu-icon"
          :class="item.icon"
          :style="{ color: item.color }"
        ></span>
        <span class="menu-label">{{ item.label }}</span>
      </div>
    </div>

    <!-- 右侧内容区 -->
    <div class="content-area">
      <router-view />
    </div>
  </div>
</template>

<script>
export default {
  name: 'ShortVideo',
  data() {
    return {
      menuItems: [
        {
          key: 'recom',
          label: '推荐',
          path: '/home/shortVideo/recom',
          icon: 'iconfont icon-tuijian',
          color: '#FF5E5B'
        },
        {
          key: 'follow',
          label: '关注',
          path: '/home/shortVideo/follow',
          icon: 'iconfont icon-guanzhu',
          color: '#FF9F43'
        },
        {
          key: 'friend',
          label: '朋友',
          path: '/home/shortVideo/friend',
          icon: 'iconfont icon-friend',
          color: '#0ABDE3'
        },
        {
          key: 'my',
          label: '我的',
          path: '/home/shortVideo/my',
          icon: 'iconfont icon-person',
          color: '#A55EEA'
        }
      ]
    }
  },
  computed: {
    currentPath() {
      return this.$route.path
    },
  },
  methods: {
    navigateTo(item) {
      if (this.$route.path !== item.path) {
        this.$router.push(item.path)
      }
    },
  }
}
</script>

<style scoped lang="scss">
.short-video-container {
  display: flex;
  height: 100%;
  background: #f5f6fa;
}

.side-menu {
  width: 200px;
  min-width: 200px;
  height: 100%;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 24px;
  margin: 0 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  user-select: none;

  &:hover {
    background: #f0f1f5;
  }

  &.active {
    background: linear-gradient(135deg, #f0f1ff 0%, #f5f0ff 100%);

    .menu-label {
      font-weight: 600;
      color: #333;
    }
  }
}

.menu-icon {
  font-size: 22px;
  margin-right: 14px;
  flex-shrink: 0;
  transition: transform 0.25s ease;
}

.menu-item:hover .menu-icon {
  transform: scale(1.12);
}

.menu-label {
  font-size: 15px;
  color: #555;
  transition: color 0.25s ease;
}

.content-area {
  flex: 1;
  height: 100%;
  overflow-y: auto;
}
</style>