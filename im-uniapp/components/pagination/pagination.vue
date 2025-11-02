<template>
  <view class="pagination-container">
    <view class="loading-more" v-if="isLoading">
      <text>加载中...</text>
    </view>
    <view class="load-more" v-else-if="hasMore" @click="loadMore">
      <text>点击加载更多</text>
    </view>
    <view class="no-more" v-else>
      <text>没有更多数据了</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'Pagination',
  props: {
    totalPage: {
      type: Number,
      default: 0
    },
    pageNo: {
      type: Number,
      default: 1
    }
  },
  computed: {
    hasMore() {
      return this.pageNo < this.totalPage;
    }
  },
  data() {
    return {
      isLoading: false
    }
  },
  methods: {
    loadMore() {
      if (!this.hasMore) {
        return;
      }
      
      this.isLoading = true;
      
      // 触发父组件的loadMore方法
      this.$emit('loadMore', () => {
        this.isLoading = false;
      });
    }
  }
}
</script>

<style lang="scss" scoped>
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20rpx 0;
  
  .loading-more, .load-more, .no-more {
    text-align: center;
    padding: 20rpx;
    font-size: 28rpx;
    border-radius: 10rpx;
  }
  
  .loading-more {
    color: #999999;
  }
  
  .load-more {
    color: #1890ff;
    background-color: #f0f8ff;
    cursor: pointer;
    
    &:hover {
      background-color: #e6f7ff;
    }
  }
  
  .no-more {
    color: #cccccc;
  }
}
</style>