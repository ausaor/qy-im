<template>
  <div class="short-video-recom">
    <short-video-play></short-video-play>
  </div>
</template>

<script>
import ShortVideoPlay from '@/components/shortVideo/ShortVideoPlay.vue'

export default {
  name: 'ShortVideoRecom',
  components: {
    ShortVideoPlay
  }
}
</script>

<style scoped lang="scss">
.short-video-recom {
  width: 100%;
  height: 100%;
}
</style>
