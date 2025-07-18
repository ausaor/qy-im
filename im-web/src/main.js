import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui';
import vuetify from './plugins/vuetify'
import VueAudio from 'vue-audio-better'
import 'element-ui/lib/theme-chalk/index.css';
import '@/assets/icon/iconfont.css';
import '@/assets/icon/iconfont.js';
import httpRequest from './api/httpRequest';
import * as socketApi from './api/wssocket';
import * as messageType from './api/messageType';
import * as sseApi from './api/sse';
import common from './api/common.js';
import emotion from './api/emotion.js';
import url from './api/url.js';
import element from './api/element.js';
import store from './store';
import config from "./assets/js/config";
import * as  enums from './api/enums.js';
import * as  date from './api/date.js';
import './utils/directive/dialogDrag';
import { v4 as uuidv4 } from 'uuid'
import VueImageSwipe from "vue-image-swipe";
import "vue-image-swipe/dist/vue-image-swipe.css";
import VideoPlayer from 'vue-video-player'
import 'video.js/dist/video-js.css'
import 'vue-video-player/src/custom-theme.css'

Vue.use(VideoPlayer);
Vue.use(ElementUI);
Vue.use(VueImageSwipe);
Vue.use(VueAudio)

// 挂载全局
Vue.prototype.$wsApi = socketApi;
Vue.prototype.$sseApi = sseApi;
Vue.prototype.$msgType = messageType
Vue.prototype.$date = date;
Vue.prototype.$commonUtil = common;
Vue.prototype.$http = httpRequest // http请求方法
Vue.prototype.$emo = emotion; // emo表情
Vue.prototype.$url = url; // url转换
Vue.prototype.$elm = element; // 元素操作
Vue.prototype.$enums = enums; // 枚举
Vue.prototype.$eventBus = new Vue(); // 全局事件
Vue.prototype.config = config;
Vue.prototype.$uuid = {
  v4: () => uuidv4()  // 封装为函数避免复用同一ID
}
Vue.prototype.$shortId = () => uuidv4().split('-')[0];
Vue.config.productionTip = false;

import hljs from 'highlight.js';

import 'highlight.js/styles/atom-one-dark-reasonable.css' //样式
//创建v-highlight全局指令
Vue.directive('highlight', function (el) {
  let blocks = el.querySelectorAll('pre code');
  blocks.forEach((block) => {
    hljs.highlightBlock(block)
  })
})

import VueViewer from 'v-viewer'
import 'viewerjs/dist/viewer.css'
Vue.use(VueViewer);

new Vue({
  el: '#app',
  // 配置路由
  router,
  store,
  vuetify,
  render: h=>h(App)
})
