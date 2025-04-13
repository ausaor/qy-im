<template>
  <view class="tab-page region-group">
    <nav-bar search add @search="onSearch()">地区群聊</nav-bar>
    <view v-if="loading" class="chat-loading">
      <loading :size="50" :mask="false">
        <view>消息接收中...</view>
      </loading>
    </view>
    <view v-if="initializing" class="chat-loading">
      <loading :size="50" :mask="false">
        <view>正在初始化...</view>
      </loading>
    </view>
    <view class="nav-bar" v-if="showSearch">
      <view class="nav-search">
        <uni-search-bar focus="true" radius="100" v-model="searchText" cancelButton="none"
                        placeholder="搜索"></uni-search-bar>
      </view>
    </view>
    <view class="chat-tip" v-if="!loading && regionStore.regionChats.length === 0">
      温馨提示：您现在还没有任何地区群聊消息~
    </view>
    <scroll-view class="scroll-bar" v-else scroll-with-animation="true" scroll-y="true">
      <view v-for="(regionGroup, index) in regionStore.regionGroups" :key="index">
        <long-press-menu :items="menu.items" @select="onSelectMenu($event, index)">
          <region-item :region-group="regionGroup" :index="index" :active="menu.chatIdx === index"></region-item>
        </long-press-menu>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import RegionItem from "../../components/region-item/region-item.vue";

export default {
  name: "region-group",
  components: {RegionItem},
  data() {
    return {
      showSearch: false,
      searchText: "",
      menu: {
        show: false,
        style: "",
        chatIdx: -1,
        isTouchMove: false,
        items: [{
          key: 'DELETE',
          name: '删除该聊天',
          icon: 'trash',
          color: '#e64e4e'
        },
          {
            key: 'TOP',
            name: '置顶该聊天',
            icon: 'arrow-up'
          }
        ]
      }
    }
  },
  methods: {
    onSearch() {
      this.showSearch = !this.showSearch;
      this.searchText = "";
    },
    refreshUnreadBadge() {
      if (this.unreadCount > 0) {
        uni.setTabBarBadge({
          index: 1,
          text: this.unreadCount + ""
        })
      } else {
        uni.removeTabBarBadge({
          index: 1,
          complete: () => {}
        })
      }
    }
  },
  computed: {
    loading() {
      return this.regionStore.isRegionLoading();
    },
    initializing(){
      return !getApp().$vm.isInit;
    },
    unreadCount() {
      let count = 0;
      this.regionStore.regionChats.forEach(chat => {
        if (!chat.delete) {
          count += chat.unreadCount;
        }
      })
      return count;
    },
  },
  watch: {
    unreadCount(newCount, oldCount) {
      this.refreshUnreadBadge();
    }
  },
  onShow() {
    this.refreshUnreadBadge();
  }
}
</script>

<style scoped lang="scss">
.region-group {

  .chat-loading {
    display: block;
    width: 100%;
    height: 120rpx;
    background: white;

    color: $im-text-color-lighter;

    .loading-box {
      position: relative;
    }
  }

  .scroll-bar {
    flex: 1;
    height: 100%;
  }
}
</style>