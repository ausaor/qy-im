<template>
  <view class="page region-list">
    <!-- 顶部导航栏 -->
    <nav-bar back>选择地区</nav-bar>
    <view class="container">
      <!-- 当前选中地区 -->
      <view class="current-location">
        <text>当前选中：{{ curRegion.name }}</text>
      </view>

      <view class="header-buttons">
        <button class="header-btn" @click="joinRegionGroup(curRegion, 0)">临时加入</button>
        <button class="header-btn primary-btn" @click="joinRegionGroup(curRegion, 1)">加入常驻</button>
      </view>

      <text class="region-name">全部地区</text>
      <!-- 地区选择区域 -->
      <scroll-view class="region-list-box" scroll-y>
        <tree-node
            v-for="item in treeData"
            :key="item.id"
            :node="item"
            :level="item.level"
            :props="customProps"
            @node-click="handleNodeClick"
            @node-toggle="handleNodeToggle"
        >
          <!-- 自定义节点内容 -->
          <template v-slot:node="{ node }">
            <view class="custom-node">
              <text>{{ node.name }}</text>
              <text v-if="node.count" class="count">({{ node.count }})</text>
            </view>
          </template>
        </tree-node>
      </scroll-view>

      <!-- 地图展示区域 -->
      <view class="map-container" ref="mapContainer">
      </view>
    </view>
  </view>
</template>

<script>

export default {
  components: {

  },
  data() {
    return {
      currentLocation: "全部地区",
      regionList: [],
      curRegion: {},

      // 自定义字段映射（适配不同数据结构）
      customProps: {
        id: 'id',
        label: 'name',
        children: 'childList'
      },
      // 示例数据
      treeData: []
    }
  },
  methods: {
    handleNodeClick(node) {
      console.log('点击节点:', node);
      this.curRegion = node;
      if (node.childList.length === 0) {
        this.$http({
          url: `/region/findSubRegion?parentCode=${node.id}`,
          method: 'get'
        }).then((regionList) => {
          regionList.forEach((region) => {
            node.childList.push({
              id: region.code,
              code: region.code,
              name: region.name,
              lat: region.latitude,
              lng: region.longitude,
              parentCode: region.parentCode,
              level: region.level,
              childList: []
            })
          });
        })
      }
    },
    handleNodeToggle(node) {
      console.log('切换展开状态:', node)
    },
    getRegionList(parentCode) {
      if (!parentCode) {
        parentCode = '';
      }
      this.$http({
        url: `/region/findSubRegion?parentCode=${parentCode}`,
        method: 'get'
      }).then((regionList) => {
        regionList.forEach((region) => {
          this.treeData.push({
            id: region.code,
            code: region.code,
            name: region.name,
            lat: region.latitude,
            lng: region.longitude,
            parentCode: region.parentCode,
            childList: []
          })
        });
      });
    },
    joinRegionGroup(node, joinType) {
      if (!node.code) {
        uni.showToast({
          icon: "none",
          title: '请选择地区',
        })
        return;
      }
      let params = {
        code: node.code,
        name: node.name,
        joinType: joinType
      }
      this.$http({
        url: "/region/group/join",
        method: "post",
        data: params
      }).then((regionGroup) => {
        uni.showToast({
          icon: "none",
          title: '加入成功',
        })
      })
    },
  },
  onLoad(options) {
    this.getRegionList('');
  },
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #ffffff;
}

.header-buttons {
  margin-top: 10rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 80rpx;
}

.header-btn {
  margin: 0;
  padding: 0 20rpx;
  height: 60rpx;
  line-height: 60rpx;
  font-size: 14px;
  background-color: #f5f5f5;
  color: #666666;
  border-radius: 6px;
}

.primary-btn {
  background-color: #4285f4;
  color: #ffffff;
}

.current-location {
  padding: 20rpx 30rpx;
  font-size: 14px;
  color: #666666;
  border-bottom: 1px solid #f0f0f0;
}

.region-list-box {
  flex: 1;
  overflow: auto;
  padding: 30rpx 0;
  max-height: 800rpx;
}

.container {
  padding: 20rpx;
}

.custom-node {
  display: flex;
  align-items: center;
}

.count {
  margin-left: 10px;
  color: #666;
  font-size: 12px;
}

.region-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1px solid #f5f5f5;
}

.region-name {
  margin-left: 16rpx;
  font-size: 16px;
  color: #333333;
}

.sub-region {
  padding-left: 40rpx;
}

.active {
  background-color: #f0f7ff;
}

.map-container {
  flex: 1;
  width: 100%;
  position: relative;
}

.map-image {
  width: 100%;
  height: 100%;
}

.cursor-pointer {
  cursor: pointer;
}
</style>