<!--<template>-->
<!--  <div id="mapDiv" style="height:100vh; width:1000vw"></div>-->
<!--</template>-->

<!--<script>-->
<!--export default {-->
<!--  name: "TianDiMap",-->
<!--  data() {-->
<!--    return {-->
<!--      centerPoint: {-->
<!--        longitude: 116.3683244,-->
<!--        latitude: 39.915085,-->
<!--      },-->
<!--      map: null,-->
<!--    }-->
<!--  },-->
<!--  mounted() {-->
<!--    let T = window.T;-->
<!--    let imageURL = 'http://t0.tianditu.gov.cn/img_w/wmts?' +-->
<!--        'SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles' +-->
<!--        '&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=天地图tk';-->
<!--    let lay = new T.TileLayer(imageURL, { minZoom: 1, maxZoom: 16 });-->
<!--    let config = { layers: [lay] };-->

<!--    this.map = new T.Map('mapDiv', config); // 地图实例-->
<!--    this.map.centerAndZoom(new T.LngLat(this.centerPoint.longitude, this.centerPoint.latitude), 7);-->
<!--    // //允许鼠标双击放大地图-->
<!--    this.map.enableScrollWheelZoom();-->
<!--    this.map.checkResize ()-->

<!--    //创建地图图层对象-->
<!--    let mapTypeSelect = [{-->
<!--      'title': '地图', //地图控件上所要显示的图层名称-->
<!--      'icon': 'http://api.tianditu.gov.cn/v4.0/image/map/maptype/vector.png', //地图控件上所要显示的图层图标（默认图标大小80x80）-->
<!--      'layer': window.TMAP_NORMAL_MAP //地图类型对象，即MapType。-->
<!--    },-->
<!--      {-->
<!--        'title': '卫星',-->
<!--        'icon': ' http://api.tianditu.gov.cn/v4.0/image/map/maptype/satellite.png',-->
<!--        'layer': window.TMAP_SATELLITE_MAP-->
<!--      }, {-->
<!--        'title': '卫星混合',-->
<!--        'http': 'api.tianditu.gov.cn/v4.0/image/map/maptype/satellitepoi.png',-->
<!--        'layer': 'TMAP_HYBRID_MAP'-->
<!--      }, {-->
<!--        'title': '地形',-->
<!--        'icon': ' http://api.tianditu.gov.cn/v4.0/image/map/maptype/terrain.png',-->
<!--        'layer': window.TMAP_TERRAIN_MAP-->
<!--      },-->
<!--      {-->
<!--        'title': '地形混合',-->
<!--        'icon': ' http://api.tianditu.gov.cn/v4.0/image/map/maptype/terrainpoi.png',-->
<!--        'layer': window.TMAP_TERRAIN_HYBRID_MAP-->
<!--      }];-->
<!--    let ctrl = new T.Control.MapType({ mapTypes: mapTypeSelect }); // 初始化地图类型选择控件-->

<!--    this.map.addControl(ctrl); //添加地图选择控件-->

<!--    this.map.setMapType(window.TMAP_HYBRID_MAP);      // 设置地图位地星混合图层-->

<!--    this.initMap();-->
<!--  },-->
<!--  methods: {-->
<!--    initMap() {-->
<!--      let T = window.T;-->

<!--      //设置显示地图的中心点和级别-->
<!--      this.map.clearOverLays();-->
<!--      this.map.centerAndZoom(new T.LngLat(this.centerPoint.longitude, this.centerPoint.latitude), 3);-->

<!--      let icon = new T.Icon({-->
<!--        iconUrl: 'http://api.tianditu.gov.cn/img/map/markerA.png',-->
<!--        iconSize: new T.Point(33, 33),-->
<!--        iconAnchor: new T.Point(10, 25)-->
<!--      });-->

<!--      let latlng = new T.LngLat(this.centerPoint.longitude,this.centerPoint.latitude, 7); // 设置坐标点传入经度纬度-->
<!--      let marker = new T.Marker(latlng, { icon: icon });// 创建标注-->

<!--      const that = this;-->
<!--      marker.addEventListener('click', function (e) { // 监听点击事件-->

<!--        let clientx = e.lnglat.lat; // 获取marker当前经纬度-->
<!--        let clientY = e.lnglat.lng;-->
<!--        that.map.centerAndZoom(new T.LngLat(clientY, clientx), 10); // 重新创建地图对象-->
<!--        // 这里获取的经度纬度是实际经纬度四舍五入后的获取的-->
<!--      });-->
<!--    },-->
<!--  }-->
<!--}-->
<!--</script>-->

<!--<style lang="scss">-->

<!--</style>-->