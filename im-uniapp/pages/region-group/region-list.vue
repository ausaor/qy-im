<template>
  <view class="page region-list">
    <!-- 顶部导航栏 -->
    <nav-bar back :theme-index="5">选择地区</nav-bar>
    <view class="container">
      <!-- 当前选中地区 -->
      <view class="current-location-card">
        <view class="location-info">
          <text class="location-label">当前选中</text>
          <text class="location-name">{{ curRegion.name }}</text>
        </view>
        <view class="location-actions">
          <svg-icon icon-class="shejiaotubiao-40" class="location-icon" @click="toRegionGroup"></svg-icon>
        </view>
      </view>

            <view class="header-buttons">
        <button class="header-btn temp-btn" @click="joinRegionGroup(curRegion, 0)">临时加入</button>
        <button class="header-btn primary-btn" @click="joinRegionGroup(curRegion, 1)">加入常驻</button>
      </view>

            <view class="region-name">
        <text class="region-tab active">全部地区</text>
        <text class="region-tab" @click="viewActivityRegions">活跃地区</text>
        <text class="region-tab" @click="viewRegionGroups">地区群聊</text>
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
      <uni-popup ref="activityRegions" type="top" @change="onActivityRegionsPopupChange">
        <view class="activity-regions-box">
          <view class="popup-header">
            <text class="popup-title">活跃地区</text>
            <uni-icons :color="'#4285f4'" type="star-filled" size="20"></uni-icons>
          </view>
          <scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
            <view class="region-item" v-for="(region, index) in activityRegions" :key="index" @click="chooseActivityRegion(region, index)">
              <text class="region-full-name">{{region.fullName}}</text>
              <uni-icons 
                :color="regionActiveIndex === index ? '#007aff' : '#c0c4cc'" 
                :type="regionActiveIndex === index ? 'checkbox-filled' : 'circle'" 
                size="20" 
                color="#999">
              </uni-icons>
            </view>
          </scroll-view>
          <view class="btns">
            <button type="default" size="normal" class="cancel-btn" @click="closeActivityRegionsPopup">取消</button>
            <button type="primary" size="normal" class="confirm-btn" @click="confirmActivityRegion">确定</button>
          </view>
        </view>
      </uni-popup>
      <uni-popup ref="regionGroups" type="top" @change="onRegionGroupsPopupChange">
        <view class="region-groups-box">
          <scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
            <view class="region-group-item" v-for="(group, index) in regionGroups" :key="index" @click="chooseTargetRegionGroup(group, index)">
              <view class="group-info">
                <text class="group-name">{{group.regionGroupName}}</text>
                <uni-icons 
                  :color="regionGroupActiveIndex === index ? '#007aff' : '#c0c4cc'" 
                  :type="regionGroupActiveIndex === index ? 'checkbox-filled' : 'circle'" 
                  size="20">
                </uni-icons>
              </view>
              <view class="group-arrow">
                <uni-icons type="arrowright" size="16" color="#c0c4cc"></uni-icons>
              </view>
            </view>
          </scroll-view>
          <view class="btns">
            <button type="default" size="normal" class="join-btn temp-join" @click="joinTargetRegionGroup(0)">临时加入</button>
            <button type="primary" size="normal" class="join-btn permanent-join" @click="joinTargetRegionGroup(1)">加入常驻</button>
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
      regionGroups: [],
      regionGroupActiveIndex: -1,
      chooseRegionGroup: {},
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
    viewRegionGroups() {
      if (!this.curRegion.code) {
        uni.showToast({
          icon: "none",
          title: '请先选择地区',
        })
        return
      }
      this.loadRegionGroupList(this.curRegion.code)
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
    },
    chooseTargetRegionGroup(group, index) {
      this.regionGroupActiveIndex = index;
      this.chooseRegionGroup = group;
    },
    loadRegionGroupList(regionCode) {
      this.$http({
        url: `/region/group/findRegionGroupsByCode?code=${regionCode}`,
        method: 'get',
      }).then((data) => {
        this.regionGroups = data;
        this.$refs.regionGroups.open();
      }).catch((e) => {
        uni.showToast({
          icon: "none",
          title: '获取地区群聊列表失败'
        })
      })
    },
    joinTargetRegionGroup(joinType) {
      if (!this.chooseRegionGroup.id) {
        uni.showToast({
          icon: "none",
          title: '请先选择一个群聊'
        })
        return;
      }
      let params = {
        id: this.chooseRegionGroup.id,
        code: this.chooseRegionGroup.code,
        num: this.chooseRegionGroup.num,
        joinType: joinType
      }
      this.$http({
        url: `/region/group/joinTarget`,
        method: 'post',
        data: params
      }).then((data) => {
        uni.showToast({
          icon: "none",
          title: '加入成功'
        })
      }).catch((e) => {
        uni.showToast({
          icon: "none",
          title: e.message || '加入失败'
        })
      })
    },
    onRegionGroupsPopupChange(e) {
      if (!e.show) {
        // 弹窗已关闭，重置选中状态
        this.regionGroupActiveIndex = -1;
        this.chooseRegionGroup = {};
      }
    },
    onActivityRegionsPopupChange(e) {
      if (!e.show) {
        // 弹窗已关闭，重置选中状态
        this.regionActiveIndex = -1;
      }
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
  margin: 20rpx 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40rpx;
}

.header-btn {
  margin: 0;
  padding: 15rpx 40rpx;
  height: 70rpx;
  line-height: 40rpx;
  font-size: 28rpx;
  border-radius: 12rpx;
  border: none;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.header-btn:active {
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.12);
}

.temp-btn {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  color: #495057;
}

.primary-btn {
  background: linear-gradient(135deg, #4285f4, #3c9cff);
  color: #ffffff;
}

.current-location-card {
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #f8f9ff, #eef2ff);
  border-radius: 20rpx;
  margin: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(66, 133, 244, 0.15);
  border: 2rpx solid #e6e9ff;
  transition: all 0.3s ease;
}

.current-location-card:hover {
  box-shadow: 0 8rpx 30rpx rgba(66, 133, 244, 0.25);
  transform: translateY(-2rpx);
}

.location-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.location-label {
  font-size: 24rpx;
  color: #6c757d;
  margin-bottom: 8rpx;
}

.location-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #4285f4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.location-actions {
  display: flex;
  align-items: center;
}

.location-icon {
  width: 60rpx;
  height: 60rpx;
  padding: 10rpx;
  border-radius: 50%;
  background: #4285f4;
  color: white;
  transition: all 0.3s ease;
}

.location-icon:hover {
  background: #3c7ae4;
  transform: scale(1.1);
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
  justify-content: space-around;
  align-items: center;
  padding: 20rpx 30rpx;
  margin: 0 20rpx 10rpx 20rpx;
  background: #f8f9ff;
  border-radius: 12rpx;
  border-bottom: 2rpx solid #e6e9ff;
}

.region-tab {
  padding: 12rpx 20rpx;
  font-size: 28rpx;
  color: #6c757d;
  font-weight: 500;
  border-radius: 8rpx;
  transition: all 0.3s ease;
}

.region-tab:hover {
  background: #eef2ff;
  color: #4285f4;
}

.region-tab.active {
  background: #4285f4;
  color: white;
  box-shadow: 0 4rpx 12rpx rgba(66, 133, 244, 0.3);
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
  flex-direction: column;
  justify-content: space-between;
  padding-bottom: 10rpx;
  border-radius: 20rpx 20rpx 0 0;
  box-shadow: 0 -10rpx 30rpx rgba(0, 0, 0, 0.1);

  .popup-header {
    display: flex;
    align-items: center;
    padding: 30rpx 20rpx 20rpx;
    border-bottom: 1px solid #f5f5f5;
    background-color: #fafafa;
    border-radius: 20rpx 20rpx 0 0;

    .popup-title {
      font-size: 36rpx;
      font-weight: 600;
      color: #333;
      margin-right: 15rpx;
    }
  }

  .region-item {
    display: flex;
    align-items: center;
    padding: 25rpx 30rpx;
    border-bottom: 1px solid #f8f8f8;
    transition: background-color 0.3s;
  }

  .region-item:hover {
    background-color: #f5f5f5;
  }

  .region-full-name {
    color: #333;
    font-size: 30rpx;
    flex: 1;
    word-wrap: break-word;
    word-break: break-all;
    margin-right: 20rpx;
  }

  .btns {
    display: flex;
    padding: 30rpx 20rpx 20rpx;
    gap: 20rpx;
  }

  .cancel-btn, .confirm-btn {
    flex: 1;
    height: 80rpx;
    border-radius: 12rpx;
    font-size: 32rpx;
    font-weight: 500;
  }

  .cancel-btn {
    background-color: #f0f0f0;
    color: #666;
  }

  .confirm-btn {
    background: linear-gradient(135deg, #4285f4, #3c9cff);
    color: #fff;
    border: none;
  }
}

.region-groups-box {
  min-height: 600rpx;
  max-height: 1000rpx;
  background-color: white;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-bottom: 10rpx;
}
  
.region-group-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fff;
  transition: background-color 0.3s;
}
  
.region-group-item:hover {
  background-color: #f5f5f5;
}
  
.group-info {
  display: flex;
  align-items: center;
  flex: 1;
  gap: 15rpx;
}
  
.group-name {
  font-size: 32rpx;
  color: #333;
  flex: 1;
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
}
  
.group-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background-color: #f0f0f0;
}
  
.join-btn {
  flex: 1;
  margin: 0 20rpx 20rpx;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 500;
}
  
.temp-join {
  background-color: #f0f0f0;
  color: #666;
}
  
.permanent-join {
  background: linear-gradient(135deg, #4285f4, #3c9cff);
  color: #fff;
  border: none;
}
  
.btns {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
}
</style>