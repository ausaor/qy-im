<template>
  <view class="page chat-system">
    <nav-bar back>{{ title }}</nav-bar>
    <view class="container">
      <scroll-view>
        <!-- mp-html组件用于解析HTML内容 -->
        <mp-html
            :content="contentDetail"
            :plugins="['lazy-load']"
            class="quill-content"
        ></mp-html>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import mpHtml from 'mp-html/dist/uni-app/components/mp-html/mp-html.vue'

export default {
  name: "chat-system-content",
  components: {
    mpHtml
  },
  data() {
    return {
      sysMsgId: null,
      title: '',
      contentDetail: null,
    }
  },
  onLoad(options) {
    this.sysMsgId = options.id;
    this.title = options.title;
    this.getContentDetail();
  },
  methods: {
    getContentDetail() {
      this.$http({
        url: `/message/system/content?id=${this.sysMsgId}`,
        method: 'get'
      }).then((data) => {
        this.contentDetail = data.richText
      })
    }
  },
}
</script>

<style scoped lang="scss">
.container {
  flex: 1;
  padding: 0 1.25rem;
  overflow: hidden;
  position: relative;
  background-color: #f6f6f6;
}

/* 针对mp-html的自定义样式调整 */
.quill-content {
  font-size: 16px;
  line-height: 1.6;
}

/* 适配移动端的图片显示 */
.quill-content img {
  max-width: 100% !important;
  height: auto !important;
}
</style>