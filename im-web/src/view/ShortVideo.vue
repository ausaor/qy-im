<template>
  <div class="short-video-container">
    <!-- 左侧菜单栏 -->
    <div class="side-menu">
      <div
        class="menu-item"
        :class="{ active: currentPath === '/home/shortVideo/recom' }"
        @click="navigateTo('/home/shortVideo/recom')"
      >
        <span class="menu-icon iconfont icon-tuijian1" style="color: #FF5E5B"></span>
        <span class="menu-label">推荐</span>
      </div>
      <div
        class="menu-item"
        :class="{ active: currentPath === '/home/shortVideo/star' }"
        @click="navigateTo('/home/shortVideo/star')"
      >
        <span class="menu-icon iconfont icon-tuijian" style="color: #FFB800"></span>
        <span class="menu-label">星选</span>
      </div>
      <div
        class="menu-item"
        :class="{ active: currentPath === '/home/shortVideo/follow' }"
        @click="navigateTo('/home/shortVideo/follow')"
      >
        <span class="menu-icon iconfont icon-guanzhu" style="color: #FF9F43"></span>
        <span class="menu-label">关注</span>
      </div>
      <div
        class="menu-item"
        :class="{ active: currentPath === '/home/shortVideo/friend' }"
        @click="navigateTo('/home/shortVideo/friend')"
      >
        <span class="menu-icon iconfont icon-friend" style="color: #0ABDE3"></span>
        <span class="menu-label">朋友</span>
      </div>
      <div
        class="menu-item"
        :class="{ active: currentPath === '/home/shortVideo/my' }"
        @click="navigateTo('/home/shortVideo/my')"
      >
        <span class="menu-icon iconfont icon-person" style="color: #A55EEA"></span>
        <span class="menu-label">我的</span>
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
    }
  },
  computed: {
    currentPath() {
      return this.$route.path
    },
  },
  mounted() {
    this.fetchFollowList()
  },
  methods: {
    navigateTo(path) {
      if (this.$route.path !== path) {
        this.$router.push(path)
      }
    },
    fetchFollowList() {
      this.$http({
        url: '/follow/list',
        method: 'get'
      }).then((data) => {
          this.$store.commit('setFollows', data)
      })
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