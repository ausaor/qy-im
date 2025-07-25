import App from './App'
import request from './common/request';
import emotion from './common/emotion.js';
import url from './common/url.js';
import * as  enums from './common/enums.js';
import * as date from './common/date';
import * as socketApi from './common/wssocket';
import * as messageType from './common/messageType';
import '@/static/iconfont/iconfont.js';
import { createSSRApp } from 'vue'
import uviewPlus from '@/uni_modules/uview-plus'
import * as pinia from 'pinia';
import useChatStore from '@/store/chatStore.js'
import useFriendStore from '@/store/friendStore.js'
import useGroupStore from '@/store/groupStore.js'
import useConfigStore from '@/store/configStore.js'
import useUserStore from '@/store/userStore.js'
import useRegionStore from '@/store/regionStore.js'
import useTalkStore from '@/store/talkStore.js'
import barGroup from '@/components/bar/bar-group'
import arrowBar from '@/components/bar/arrow-bar'
import btnBar from '@/components/bar/btn-bar'
import switchBar from '@/components/bar/switch-bar'
import 'virtual:svg-icons-register';
import SvgIcon from './components/svg-icon/svg-icon.vue';

//import VConsole from 'vconsole'
//new VConsole();

// #ifdef H5
import * as recorder from './common/recorder-h5';
// #endif
// #ifndef H5
import * as recorder from './common/recorder-app';
// #endif
export function createApp() {
  const app = createSSRApp(App)
  app.use(uviewPlus);
  app.use(pinia.createPinia());
  app.component('bar-group', barGroup);
  app.component('arrow-bar', arrowBar);
  app.component('btn-bar', btnBar);
  app.component('switch-bar', switchBar);
  app.component('svg-icon', SvgIcon);
  app.config.globalProperties.$http = request;
  app.config.globalProperties.$wsApi = socketApi;
  app.config.globalProperties.$msgType = messageType;
  app.config.globalProperties.$emo = emotion;
  app.config.globalProperties.$url = url;
  app.config.globalProperties.$enums = enums;
  app.config.globalProperties.$date = date;
  app.config.globalProperties.$rc = recorder;
  // 初始化时再挂载store对象
  app.config.globalProperties.$mountStore = () => {
    app.config.globalProperties.chatStore = useChatStore();
    app.config.globalProperties.friendStore = useFriendStore();
    app.config.globalProperties.groupStore = useGroupStore();
    app.config.globalProperties.configStore = useConfigStore();
    app.config.globalProperties.userStore = useUserStore();
    app.config.globalProperties.regionStore = useRegionStore();
    app.config.globalProperties.talkStore = useTalkStore();
  }
  return {
    app,
    pinia
  }
}
