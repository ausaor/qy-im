<template>
  <div id="container" style="height:100%; width:100%;overflow: hidden"></div>
</template>
<script>
import AMapLoader from "@amap/amap-jsapi-loader";

export default {
  name: "GaoDeMap",
  props: {

  },
  data() {
    return {
      loaded: false,
      map: null,
      lng: 116.397428,
      lat: 39.90923,
      circle: null,
    }
  },
  mounted() {

  },
  unmounted() {
    this.map?.destroy();
  },
  watch: {

  },
  methods: {
    initAMap(lng, lat) {
      AMapLoader.load({
        key: this.$store.getters.getGaoDeMapKey(), // 申请好的Web端开发者Key，首次调用 load 时必填
        version: "2.0", // 指定要加载的 JSAPI 的版本，缺省时默认为 1.4.15
        plugins: [], // 需要使用的的插件列表，如比例尺'AMap.Scale'等
      }).then((AMap) => {
        if (!this.loaded) {
          console.log("地图初始化......")
          // 高德地图使用手册
          // https://developer.amap.com/api/javascript-api-v2/documentation#map
          this.map = new AMap.Map("container", {
            // 设置地图容器id
            viewMode: "3D", // 是否为3D地图模式
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
          //构造点标记
          /*let marker = new AMap.Marker({
            icon: "https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png",
            position: [116.397428, 39.90923],
          });*/
          //构造矢量圆形
          let circle = new AMap.Circle({
            center: new AMap.LngLat(lng, lat), //圆心位置
            radius: 10000, //半径 单位:米
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
          this.loaded = true
        } else {
          console.log("地图已初始化")
          let circle = new AMap.Circle({
            center: new AMap.LngLat(lng, lat), //圆心位置
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
          this.map.setZoomAndCenter(11, [lng, lat]);
        }
      }).catch((e) => {
        console.log(e);
      });
    },
  },
};
</script>
<style lang="scss">

</style>
