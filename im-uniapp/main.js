import App from './App'
import request from './common/request';
import emotion from './common/emotion.js';
import url from './common/url.js';
import * as common from './common/common.js'
import * as  enums from './common/enums.js';
import * as date from './common/date';
import * as socketApi from './common/wssocket';
import * as messageType from './common/messageType';
import '@/static/iconfont/iconfont.js';
import { createSSRApp } from 'vue'
import uviewPlus from '@/uni_modules/uview-plus'
// 引入 uni-ui 组件库，解决 H5 环境 easycom 失效问题
import UniForms from '@/uni_modules/uni-forms/components/uni-forms/uni-forms.vue'
import UniFormsItem from '@/uni_modules/uni-forms/components/uni-forms-item/uni-forms-item.vue'
import UniEasyinput from '@/uni_modules/uni-easyinput/components/uni-easyinput/uni-easyinput.vue'
import UniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue'
import UniSearchBar from '@/uni_modules/uni-search-bar/components/uni-search-bar/uni-search-bar.vue'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import UniBadge from '@/uni_modules/uni-badge/components/uni-badge/uni-badge.vue'
import UniTag from '@/uni_modules/uni-tag/components/uni-tag/uni-tag.vue'
import UniSegmentedControl from '@/uni_modules/uni-segmented-control/components/uni-segmented-control/uni-segmented-control.vue'
import UniNavBar from '@/uni_modules/uni-nav-bar/components/uni-nav-bar/uni-nav-bar.vue'
import UniLoadMore from '@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue'
import UniDateformat from '@/uni_modules/uni-dateformat/components/uni-dateformat/uni-dateformat.vue'
import UniDatetimePicker from '@/uni_modules/uni-datetime-picker/components/uni-datetime-picker/uni-datetime-picker.vue'
import UniFilePicker from '@/uni_modules/uni-file-picker/components/uni-file-picker/uni-file-picker.vue'
import UniDataPicker from '@/uni_modules/uni-data-picker/components/uni-data-picker/uni-data-picker.vue'
import UniDataSelect from '@/uni_modules/uni-data-select/components/uni-data-select/uni-data-select.vue'
import UniDataCheckbox from '@/uni_modules/uni-data-checkbox/components/uni-data-checkbox/uni-data-checkbox.vue'
import UniCollapse from '@/uni_modules/uni-collapse/components/uni-collapse/uni-collapse.vue'
import UniCollapseItem from '@/uni_modules/uni-collapse/components/uni-collapse-item/uni-collapse-item.vue'
import UniCard from '@/uni_modules/uni-card/components/uni-card/uni-card.vue'
import UniCalendar from '@/uni_modules/uni-calendar/components/uni-calendar/uni-calendar.vue'
import UniCombox from '@/uni_modules/uni-combox/components/uni-combox/uni-combox.vue'
import UniCountdown from '@/uni_modules/uni-countdown/components/uni-countdown/uni-countdown.vue'
import UniDrawer from '@/uni_modules/uni-drawer/components/uni-drawer/uni-drawer.vue'
import UniFab from '@/uni_modules/uni-fab/components/uni-fab/uni-fab.vue'
import UniFav from '@/uni_modules/uni-fav/components/uni-fav/uni-fav.vue'
import UniGoodsNav from '@/uni_modules/uni-goods-nav/components/uni-goods-nav/uni-goods-nav.vue'
import UniGrid from '@/uni_modules/uni-grid/components/uni-grid/uni-grid.vue'
import UniGridItem from '@/uni_modules/uni-grid/components/uni-grid-item/uni-grid-item.vue'
import UniGroup from '@/uni_modules/uni-group/components/uni-group/uni-group.vue'
import UniLink from '@/uni_modules/uni-link/components/uni-link/uni-link.vue'
import UniList from '@/uni_modules/uni-list/components/uni-list/uni-list.vue'
import UniListItem from '@/uni_modules/uni-list/components/uni-list-item/uni-list-item.vue'
import UniNoticeBar from '@/uni_modules/uni-notice-bar/components/uni-notice-bar/uni-notice-bar.vue'
import UniNumberBox from '@/uni_modules/uni-number-box/components/uni-number-box/uni-number-box.vue'
import UniPagination from '@/uni_modules/uni-pagination/components/uni-pagination/uni-pagination.vue'
import UniRate from '@/uni_modules/uni-rate/components/uni-rate/uni-rate.vue'
import UniRow from '@/uni_modules/uni-row/components/uni-row/uni-row.vue'
import UniCol from '@/uni_modules/uni-row/components/uni-col/uni-col.vue'
import UniSection from '@/uni_modules/uni-section/components/uni-section/uni-section.vue'
import UniSteps from '@/uni_modules/uni-steps/components/uni-steps/uni-steps.vue'
import UniSwipeAction from '@/uni_modules/uni-swipe-action/components/uni-swipe-action/uni-swipe-action.vue'
import UniSwipeActionItem from '@/uni_modules/uni-swipe-action/components/uni-swipe-action-item/uni-swipe-action-item.vue'
import UniSwiperDot from '@/uni_modules/uni-swiper-dot/components/uni-swiper-dot/uni-swiper-dot.vue'
import UniTable from '@/uni_modules/uni-table/components/uni-table/uni-table.vue'
import UniTr from '@/uni_modules/uni-table/components/uni-tr/uni-tr.vue'
import UniTd from '@/uni_modules/uni-table/components/uni-td/uni-td.vue'
import UniTh from '@/uni_modules/uni-table/components/uni-th/uni-th.vue'
import UniTitle from '@/uni_modules/uni-title/components/uni-title/uni-title.vue'
import UniTooltip from '@/uni_modules/uni-tooltip/components/uni-tooltip/uni-tooltip.vue'
import UniTransition from '@/uni_modules/uni-transition/components/uni-transition/uni-transition.vue'
import * as pinia from 'pinia';
import useChatStore from '@/store/chatStore.js'
import useFriendStore from '@/store/friendStore.js'
import useGroupStore from '@/store/groupStore.js'
import useConfigStore from '@/store/configStore.js'
import useUserStore from '@/store/userStore.js'
import useRegionStore from '@/store/regionStore.js'
import useTalkStore from '@/store/talkStore.js'
import uiStore from '@/store/uiStore.js'
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
  // 全局注册 uni-ui 组件，解决 H5 环境 easycom 失效问题
  app.component('uni-forms', UniForms);
  app.component('uni-forms-item', UniFormsItem);
  app.component('uni-easyinput', UniEasyinput);
  app.component('uni-popup', UniPopup);
  app.component('uni-search-bar', UniSearchBar);
  app.component('uni-icons', UniIcons);
  app.component('uni-badge', UniBadge);
  app.component('uni-tag', UniTag);
  app.component('uni-segmented-control', UniSegmentedControl);
  app.component('uni-nav-bar', UniNavBar);
  app.component('uni-load-more', UniLoadMore);
  app.component('uni-dateformat', UniDateformat);
  app.component('uni-datetime-picker', UniDatetimePicker);
  app.component('uni-file-picker', UniFilePicker);
  app.component('uni-data-picker', UniDataPicker);
  app.component('uni-data-select', UniDataSelect);
  app.component('uni-data-checkbox', UniDataCheckbox);
  app.component('uni-collapse', UniCollapse);
  app.component('uni-collapse-item', UniCollapseItem);
  app.component('uni-card', UniCard);
  app.component('uni-calendar', UniCalendar);
  app.component('uni-combox', UniCombox);
  app.component('uni-countdown', UniCountdown);
  app.component('uni-drawer', UniDrawer);
  app.component('uni-fab', UniFab);
  app.component('uni-fav', UniFav);
  app.component('uni-goods-nav', UniGoodsNav);
  app.component('uni-grid', UniGrid);
  app.component('uni-grid-item', UniGridItem);
  app.component('uni-group', UniGroup);
  app.component('uni-link', UniLink);
  app.component('uni-list', UniList);
  app.component('uni-list-item', UniListItem);
  app.component('uni-notice-bar', UniNoticeBar);
  app.component('uni-number-box', UniNumberBox);
  app.component('uni-pagination', UniPagination);
  app.component('uni-rate', UniRate);
  app.component('uni-row', UniRow);
  app.component('uni-col', UniCol);
  app.component('uni-section', UniSection);
  app.component('uni-steps', UniSteps);
  app.component('uni-swipe-action', UniSwipeAction);
  app.component('uni-swipe-action-item', UniSwipeActionItem);
  app.component('uni-swiper-dot', UniSwiperDot);
  app.component('uni-table', UniTable);
  app.component('uni-tr', UniTr);
  app.component('uni-td', UniTd);
  app.component('uni-th', UniTh);
  app.component('uni-title', UniTitle);
  app.component('uni-tooltip', UniTooltip);
  app.component('uni-transition', UniTransition);
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
  app.config.globalProperties.$commonUtil = common;
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
    app.config.globalProperties.uiStore = uiStore();
  }
  return {
    app,
    pinia
  }
}
