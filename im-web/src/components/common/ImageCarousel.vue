<template>
  <div class="carousel-container" :style="{ height: height }">
    <!-- 轮播主体 -->
    <swiper
        :options="swiperOptions"
        ref="carousel"
        class="carousel-swiper"
    >
      <!-- 轮播图片 -->
      <swiper-slide
          v-for="(image, index) in newImages"
          :key="index"
          class="carousel-slide"
      >
        <img
            :src="image.url"
            :alt="image.name || '轮播图片'"
            class="carousel-image"
            @click="showFullImageBox(image.url)"
        >
        <!-- 图片描述（可选） -->
        <div
            v-if="image.name"
            class="carousel-caption"
        >
          {{ image.name }}
        </div>
      </swiper-slide>

      <!-- 指示器 -->
      <div
          class="swiper-pagination"
          slot="pagination"
      ></div>

      <!-- 上一张/下一张按钮 -->
      <div
          class="swiper-button-prev"
          slot="button-prev"
      ></div>
      <div
          class="swiper-button-next"
          slot="button-next"
      ></div>
    </swiper>

    <!-- 播放/暂停控制按钮 -->
    <el-button
        v-if="autoplay"
        :icon="isPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'"
        class="play-control-btn"
        @click="togglePlay"
        circle
        size="mini"
    ></el-button>
  </div>
</template>

<script>
import { swiper, swiperSlide } from 'vue-awesome-swiper'
import 'swiper/css/swiper.css'

export default {
  name: 'ImageCarousel',
  components: {
    swiper,
    swiperSlide,
  },
  props: {
    /**
     * 图片列表
     * 格式: [{ url: '图片地址', alt: '图片描述', caption: '底部说明文字' }]
     */
    images: {
      type: Array,
      required: true,
      validator: (value) => {
        // 验证每个图片对象是否包含url属性
        return value.every(item => item && item.url)
      }
    },
    // 是否自动播放
    autoplay: {
      type: Boolean,
      default: false
    },
    // 自动播放间隔时间（毫秒）
    interval: {
      type: Number,
      default: 3000
    },
    // 是否循环播放
    loop: {
      type: Boolean,
      default: true
    },
    // 轮播高度，如 '400px' 或 '50%'
    height: {
      type: String,
      default: '400px'
    }
  },
  data() {
    return {
      isPlaying: this.autoplay,
      swiperOptions: {
        autoplay: this.autoplay ? {
          delay: this.interval,
          disableOnInteraction: false // 用户操作后是否继续自动播放
        } : false,
        loop: this.loop,
        pagination: {
          el: '.swiper-pagination',
          clickable: true // 允许点击指示器切换
        },
        navigation: {
          nextEl: '.swiper-button-next',
          prevEl: '.swiper-button-prev'
        },
        // 响应式配置
        responsive: {
          640: {
            slidesPerView: 1
          }
        }
      }
    }
  },
  computed: {
    newImages() {
      // 获取图片名称，并去除扩展名
      this.images.forEach(item => {
        item.name = this.getFileNameWithoutExtension(item.name)
      })
      return this.images
    }
  },
  watch: {
    // 监听自动播放状态变化
    autoplay(newVal) {
      this.isPlaying = newVal
      this.swiperOptions.autoplay = newVal ? {
        delay: this.interval,
        disableOnInteraction: false
      } : false
    },
    // 监听播放间隔变化
    interval(newVal) {
      if (this.autoplay && this.$refs.carousel) {
        this.$refs.carousel.swiper.params.autoplay.delay = newVal
      }
    },
    // 监听循环状态变化
    loop(newVal) {
      if (this.$refs.carousel) {
        this.$refs.carousel.swiper.params.loop = newVal
      }
    }
  },
  methods: {
    // 切换播放/暂停状态
    togglePlay() {
      this.isPlaying = !this.isPlaying
      const swiper = this.$refs.carousel.swiper

      if (this.isPlaying) {
        swiper.autoplay.start()
      } else {
        swiper.autoplay.stop()
      }
    },
    showFullImageBox(imageUrl) {
      if (imageUrl) {
        this.$store.commit('showFullImageBox', imageUrl);
      }
    },
    getFileNameWithoutExtension(filename) {
      // 找到最后一个点的位置
      const lastDotIndex = filename.lastIndexOf('.');

      // 如果没有找到点（没有后缀），则返回原文件名
      // 否则返回从开始到最后一个点之前的部分
      return lastDotIndex === -1 ? filename : filename.slice(0, lastDotIndex);
    }
  }
}
</script>

<style scoped lang="scss">
.carousel-container {
  position: relative;
  width: 100%;
  overflow: hidden;
}

.carousel-swiper {
  width: 100%;
  height: 100%;
}

.carousel-slide {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.carousel-image:hover {
  transform: scale(1.02);
}

.carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 15px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  font-size: 16px;
  transform: translateY(0);
  transition: transform 0.3s ease;
}

.play-control-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  z-index: 10;
  background-color: rgba(255, 255, 255, 0.7);
  color: #333;
  transition: all 0.3s ease;
}

.play-control-btn:hover {
  background-color: white;
  transform: scale(1.1);
}

/* 样式穿透修改swiper默认样式 */
::v-deep .swiper-pagination-bullet-active {
  background-color: #409EFF; /* Element UI 主题色 */
}

::v-deep .swiper-button-prev,
::v-deep .swiper-button-next {
  color: #409EFF; /* Element UI 主题色 */
  background-color: rgba(255, 255, 255, 0.3);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

::v-deep .swiper-button-prev:hover,
::v-deep .swiper-button-next:hover {
  background-color: rgba(255, 255, 255, 0.7);
  transform: scale(1.1);
}

::v-deep .swiper-button-prev::after,
::v-deep .swiper-button-next::after {
  font-size: 18px;
  font-weight: bold;
}
</style>
