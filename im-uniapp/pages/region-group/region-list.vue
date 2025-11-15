<template>
  <view class="page region-list">
    <!-- 顶部导航栏 -->
    <nav-bar back>选择地区</nav-bar>
    <view class="container">
      <!-- 当前选中地区 -->
      <view class="current-location">
        <text>当前选中：{{ curRegion.name }}</text>
        <svg-icon icon-class="shejiaotubiao-40" style="width: 60rpx;height: 60rpx;" @click="toRegionGroup"></svg-icon>
      </view>

      <view class="header-buttons">
        <button class="header-btn" @click="joinRegionGroup(curRegion, 0)">临时加入</button>
        <button class="header-btn primary-btn" @click="joinRegionGroup(curRegion, 1)">加入常驻</button>
      </view>

      <view class="region-name">
        <text>全部地区</text>
        <text style="color: #1E90FF;" @click="viewActivityRegions">活跃地区</text>
      </view>
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
      <!-- 拖拽条 -->
      <view
          class="drag-bar"
      >
        <view class="drag-line"></view>
      </view>

      <!-- 地图展示区域 -->
      <view class="map-container" ref="mapContainer" id="mapContainer" style="width: 100%; min-height: 200rpx;"></view>
      <uni-popup ref="activityRegions" type="top">
        <view class="activity-regions-box">
          <scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
            <view class="title">活跃地区<uni-icons :color="'#4285f4'" type="paperplane-filled"></uni-icons></view>
            <view class="region-item" v-for="(region, index) in activityRegions" :key="index">
              <text class="region-full-name">{{region.fullName}}</text>
              <uni-icons :color="'#3cc51f'" :type="regionActiveIndex === index ? 'circle-filled' : 'circle'" size="20" color="#999" @click="chooseActivityRegion(region, index)"></uni-icons>
            </view>
          </scroll-view>
          <view class="btns">
            <button type="default" size="mini" @click="closeActivityRegionsPopup">取消</button>
            <button type="primary" size="mini" @click="confirmActivityRegion">确定</button>
          </view>
        </view>
      </uni-popup>
    </view>
  </view>
</template>

<script>

import SvgIcon from "../../components/svg-icon/svg-icon.vue";
import UNI_APP from '@/.env.js'

export default {
  components: {
    SvgIcon
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
      treeData: [],
      map: null,
      circle: null,
      lng: 116.397428,
      lat: 39.90923,
      activityRegions: [],
      regionActiveIndex: -1,
    }
  },
  mounted() {
    this.initAMap(this.lng, this.lat)
  },
  beforeDestroy() {
    this.destroyMap(); // 避免内存泄漏
  },
  onUnload() {
    console.log('onUnload')
    this.map?.destroy();
  },
  methods: {
    destroyMap() {
      console.log('destroyMap')
      if (this.map) {
        this.map.destroy()
        this.map = null
      }
    },
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
        this.moveMapToTarget(node);
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
    toRegionGroup() {
      if (!this.curRegion.code) {
        uni.showToast({
          icon: "none",
          title: '请选择地区',
        })
        return;
      }
      uni.navigateTo({
        url: `/pages/activity/activity-space?category=region&section=region&regionCode=${this.curRegion.code}&spaceTitle=地区空间动态`
      })
    },
    // 初始化地图
    async initAMap(lng, lat) {
      if (typeof window.AMap === 'undefined') {
        await this.loadAMapScript()
      }

      this.map = new AMap.Map('mapContainer', {
        zoom: 11, // 初始化地图级别
        center: [lng, lat], // 初始化地图中心点位置
        layers: [new AMap.TileLayer()],// new AMap.TileLayer.Satellite()
        features: ['bg', 'road', 'point', 'building'],
        showLabel: true,
        rotateEnable: false,
        pitchEnable: false,
        pitch: 45,
        rotation: 0,
        zooms: [2, 20],
      });

      //构造矢量圆形
      let circle = new AMap.Circle({
        center: new AMap.LngLat(lng, lat), //圆心位置
        radius: 5000, //半径 单位:米
        strokeColor: "#ff8133", //线颜色
        strokeOpacity: 1, //线透明度
        strokeWeight: 3, //线粗细度
        fillColor: "#c87d32", //填充颜色
        fillOpacity: 0.2, //填充透明度
      });
      this.circle = circle;
      //单独将点标记和矢量圆形添加到地图上
      //this.map.add(marker);
      this.map.add(circle);
    },

    // 动态加载高德JS API
    loadAMapScript() {
      return new Promise((resolve, reject) => {
        const script = document.createElement('script')
        const gaodeMapKey = UNI_APP.AMAP_KEY;
        script.src = `https://webapi.amap.com/maps?v=2.0&key=${gaodeMapKey}&plugin=AMap.Geolocation`
        script.onload = resolve
        script.onerror = reject
        document.head.appendChild(script)
      })
    },
    moveMapToTarget(node) {
      this.getRegionLngLat(node, (region) => {
        let circle = new AMap.Circle({
          center: new AMap.LngLat(region.lng, region.lat), //圆心位置
          radius: 10000, //半径 单位:米
          strokeColor: "#ff8133", //线颜色
          strokeOpacity: 1, //线透明度
          strokeWeight: 3, //线粗细度
          fillColor: "#c87d32", //填充颜色
          fillOpacity: 0.2, //填充透明度
        });
        this.map.remove(this.circle);
        this.circle = circle;

        //单独将点标记和矢量圆形添加到地图上
        //this.map.add(marker);
        this.map.add(circle);
        this.map.setZoomAndCenter(11, [region.lng, region.lat]);
      })
    },
    getRegionLngLat(node, callback) {
      if (node.lng && node.lat) {
        callback(node);
      } else{
        this.$http({
          url: "/region/getRegionLonAndLat?code=" + node.code,
          method: "get",
        }).then((region) => {
            node.lng = region.longitude;
            node.lat = region.latitude;
            callback(node);
        })
      }
    },
    // 获取定位
    getLocation() {
      const geolocation = new AMap.Geolocation({
        enableHighAccuracy: true, // 高精度定位
        timeout: 10000
      })

      geolocation.getCurrentPosition((status, result) => {
        if (status === 'complete') {
          this.handleLocationSuccess(result)
        } else {
          this.handleLocationError(result)
        }
      })
    },

    handleLocationSuccess(result) {
      this.currentPosition = {
        lng: result.position.lng,
        lat: result.position.lat,
        address: result.formattedAddress
      }

      // 移动地图中心点
      this.map.setCenter([result.position.lng, result.position.lat])
      new AMap.Marker({
        position: [result.position.lng, result.position.lat],
        map: this.map
      })

      uni.showToast({ title: '定位成功' })
    },

    handleLocationError(err) {
      uni.showModal({
        title: '定位失败',
        content: err.message || '无法获取当前位置',
        showCancel: false
      })
    },
    viewActivityRegions() {
      if (this.activityRegions.length > 0) {
        this.$refs.activityRegions.open();
      } else {
        this.$http({
          url: '/region/findActivityRegions',
          method: 'get'
        }).then((data) => {
          this.activityRegions = data;
          this.$refs.activityRegions.open();
        })
      }
    },
    chooseActivityRegion(region, index) {
      this.regionActiveIndex = index;
      region.lng = region.longitude;
      region.lat = region.latitude;
      this.moveMapToTarget(region);
    },
    closeActivityRegionsPopup() {
      this.regionActiveIndex = -1;
      this.$refs.activityRegions.close();
    },
    confirmActivityRegion() {
      this.curRegion = this.activityRegions[this.regionActiveIndex];
      this.regionActiveIndex = -1;
      this.$refs.activityRegions.close();
    }
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 40rpx;
  font-weight: 600;
  color: #666666;
  border-bottom: 1px solid #f0f0f0;
}

.region-list-box {
  flex: 1;
  overflow: auto;
  padding-top: 20rpx;
  min-height: 800rpx;
}

.custom-node {
  display: flex;
  align-items: center;
}

.region-name {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 20rpx;
  font-size: 16px;
  color: #333333;
}

.drag-bar {
  height: 40rpx;
  background-color: #f5f5f5;
  display: flex;
  justify-content: center;
  align-items: center;
  touch-action: none; /* 禁止默认触摸行为 */
}

.drag-line {
  width: 120rpx;
  height: 8rpx;
  background-color: #ddd;
  border-radius: 4rpx;
}

.map-container {
  flex: 1;
  width: 100%;
  position: relative;
}

.activity-regions-box {
  min-height: 600rpx;
  max-height: 1000rpx;
  background-color: white;
  display: flex;
  flex-direction: column;      /* 垂直排列 */
  justify-content: space-between; /* 两端对齐 */
  padding-bottom: 10rpx;

  .title {
    padding-left: 20rpx;
    color: #4285f4;
  }

  .btns {
    display: flex;
    justify-content: center;
    gap: 100rpx;
  }
}

.region-item {
  display: flex;
  padding: 10rpx 20rpx;
  border-bottom: 1px solid lightgray;
}

.region-full-name {
  color: orange;
  font-size: 30rpx;
  font-weight: bold;
  flex: 1;
}
</style>