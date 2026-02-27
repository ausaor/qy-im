<template>
  <div v-show="show">
    <div class="emotion-box" :style="{'left': x +'px','top':y+'px'}">
      <div class="close-btn" @click="close">×</div>
      <el-scrollbar style="height:250px;position: relative;">
        <div class="emotion-item-list" v-show="emoIndex === 1 && emoType === 'emo'">
          <div v-for="(emoText, i) in $emo.emoTextList" :key="i">
            <div class="emotion-item" @click="onClickEmo(emoText)" v-html="$emo.textToImg(emoText)"></div>
          </div>
        </div>
        <div class="emotion-item-list" v-show="emoIndex === 2 && emoType === 'emo'">
          <div v-for="(emoText, i) in $emo.originalEmoTextList" :key="i">
            <div class="emotion-item" @click="onClickEmo(emoText)" v-html="$emo.textToImg(emoText)"></div>
          </div>
        </div>
        <div class="sticker-items" v-show="emoType === 'album'">
          <div class="sticker-item" v-for="(item, index) in emoAlbumImgs" :key="index" @click="chooseEmoAlbumImg(item)">
            <img :src="item.thumbUrl" :alt="item.name">
            <div class="sticker-name" :title="item.name">{{item.name}}</div>
          </div>
        </div>
      </el-scrollbar>
      <div class="emotion-bottom">
        <div class="emo-items">
          <div 
            class="emo-items-left" 
            :class="{ 'disabled': !canScrollLeft }"
            @click="scrollLeft">
            <i class="el-icon-arrow-left"></i>
          </div>
          <div class="emo-items-center-wrapper">
            <div class="emo-items-center" :style="transformStyle">
              <div class="emo-item icon iconfont icon-biaoqing emo-item-1" :class="emoIndex === 1 && emoType === 'emo' ? 'active' : ''"  @click="chooseEmo(1)"></div>
              <div class="emo-item icon iconfont icon-biaoqing emo-item-2" :class="emoIndex === 2 && emoType === 'emo' ? 'active' : ''"  @click="chooseEmo(2)"></div>
              <div class="emo-item" v-for="(album, index) in emoAlbums" :key="index" :class="albumId === album.id ? 'active' : ''" @click="chooseEmoAlbum(album.id)">
                <img :src="album.logoUrl" :alt="album.name">
              </div>
            </div>
          </div>
          <div 
            class="emo-items-right" 
            :class="{ 'disabled': !canScrollRight }"
            @click="scrollRight">
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "emotion",
  data() {
    return {
      show: false,
      pos: {
        x: 0,
        y: 0
      },
      emoIndex: 1,
      albumId: null,
      emoType: 'emo',
      emoAlbumImgs: [],
      scrollPosition: 0,
      itemWidth: 48, // 每个item的宽度(32px + 8px margin * 2)
      visibleItems: 8, // 可视区域内显示的item数量
      maxScroll: 0,
    }
  },
  methods: {
    onClickEmo(emoText) {
      let emotion = `#${emoText};`
      this.$emit('emotion', emotion)
    },
    chooseEmoAlbumImg(albumImg) {
      this.$emit('chooseEmoAlbumImg', albumImg)
    },
    open(pos) {
      this.pos = pos;
      this.show = true;
      this.scrollPosition = 0;
      this.$nextTick(() => {
        this.updateMaxScroll();
      });
    },
    close() {
      this.show = false;
    },
    chooseEmo(index) {
      this.albumId = null;
      this.emoType = 'emo';
      this.emoIndex = index;
    },
    chooseEmoAlbum(albumId) {
      this.emoType = 'album';
      this.albumId = albumId;
      this.emoAlbumImgs = this.$store.getters.getEmoAlbumImgs(albumId);
    },
    scrollLeft() {
      const totalItems = 2 + this.emoAlbums.length; // 2个默认表情 + 相册数量
      const maxPosition = Math.max(0, totalItems - this.visibleItems);
      this.scrollPosition = Math.max(0, this.scrollPosition - 5);
    },
    scrollRight() {
      const totalItems = 2 + this.emoAlbums.length;
      const maxPosition = Math.max(0, totalItems - this.visibleItems);
      this.scrollPosition = Math.min(maxPosition, this.scrollPosition + 5);
    },
    updateMaxScroll() {
      this.$nextTick(() => {
        const totalItems = 2 + this.emoAlbums.length;
        this.maxScroll = Math.max(0, totalItems - this.visibleItems);
      });
    }
  },
  computed: {
    x() {
      return this.pos.x - 20;
    },
    y() {
      return this.pos.y - 310;
    },
    emoAlbums() {
      return  this.$store.getters.getEmoAlbums();
    },
    transformStyle() {
      return {
        transform: `translateX(-${this.scrollPosition * this.itemWidth}px)`,
        transition: 'transform 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
      };
    },
    canScrollLeft() {
      return this.scrollPosition > 0;
    },
    canScrollRight() {
      const totalItems = 2 + this.emoAlbums.length;
      return this.scrollPosition < Math.max(0, totalItems - this.visibleItems);
    }
  }
}
</script>

<style scoped lang="scss">
.emotion-box {
  position: fixed;
  width: 500px;
  box-sizing: border-box;
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  z-index: 10;

  .close-btn {
    position: absolute;
    top: -15px;
    right: -12px;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background-color: #f5f5f5;
    color: #999;
    font-size: 18px;
    font-weight: bold;
    cursor: pointer;
    transition: all 0.3s ease;
    z-index: 11;

    &:hover {
      background-color: #ff4d4f;
      color: #ffffff;
      transform: rotate(90deg);
    }
  }

  .emotion-item-list {
    /* grid布局 两端对齐,最后一行左对齐*/
    display: grid;
    grid-template-columns: repeat(10, 1fr);
    grid-auto-rows: min-content; /* 设置自动生成行的高度为内容的最小高度 */
    /* 为所有网格项设置内容居中 */
    place-items: center;
    gap: 8px;
    padding: 10px;
    overflow-x: hidden;
    margin-bottom: 10px;

    .emotion-item {
      text-align: center;
      cursor: pointer;
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      transition: all 0.2s ease-in-out;

      &:hover {
        transform: translateY(-2px);
        background-color: #f0f8ff;
        box-shadow: 0 2px 8px rgba(0, 120, 215, 0.15);
      }
    }
  }

  .sticker-items {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    place-items: center;
    grid-gap: 6px;
    margin-top: 6px;

    .sticker-item {
      width: 56px;
      height: 72px;
      border-radius: 6px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: transform .2s;

      img {
        width: 48px;
        height: 48px;
      }

      .sticker-name {
        margin-top: 4px;
        font-size: 12px;
        color: gray;
        text-align: center;
        width: 100%;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }

  .el-scrollbar ::v-deep .el-scrollbar__bar {
    display: none !important;
  }

  .el-scrollbar ::v-deep .el-scrollbar__wrap {
    overflow-x: hidden;
  }

  .emotion-bottom {
    position: relative;
    background: linear-gradient(to top, #ffffff, #fafafa);
    display: flex;
    width: 100%;
    height: 56px;
    border-top: 1px solid #f0f0f0;
    border-radius: 0 0 12px 12px;
    box-shadow: 0 -2px 15px rgba(0, 0, 0, 0.08);
    z-index: 10;
    overflow: hidden;

    .emo-items {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
      padding: 10px 12px;
      background: linear-gradient(90deg, #fafafa 0%, #ffffff 100%);
      position: relative;

      .emo-items-left,
      .emo-items-right {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        cursor: pointer;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 3px 12px rgba(102, 126, 234, 0.4);
        z-index: 2;
        user-select: none;

        i {
          font-size: 16px;
          font-weight: bold;
        }

        &:hover:not(.disabled) {
          transform: scale(1.2) translateY(-3px);
          box-shadow: 0 5px 20px rgba(102, 126, 234, 0.5);
          background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
        }

        &.disabled {
          background: linear-gradient(135deg, #f0f0f0 0%, #e0e0e0 100%);
          color: #c0c0c0;
          cursor: not-allowed;
          box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
          transform: none;

          i {
            opacity: 0.6;
          }
        }
      }

      .emo-items-center-wrapper {
        flex: 1;
        margin: 0 12px;
        overflow: hidden;
        position: relative;

        &::before,
        &::after {
          content: '';
          position: absolute;
          top: 0;
          width: 20px;
          height: 100%;
          z-index: 1;
          pointer-events: none;
        }

        &::before {
          left: 0;
          background: linear-gradient(90deg, #ffffff 0%, transparent 100%);
        }

        &::after {
          right: 0;
          background: linear-gradient(90deg, transparent 0%, #ffffff 100%);
        }
      }

      .emo-items-center {
        display: flex;
        align-items: center;
        width: max-content;
        min-width: 100%;
        /* 确保子元素的圆形不会被裁剪 */
        overflow: visible;

        .emo-item {
          width: 32px;
          height: 32px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 10px;
          margin: 0 8px;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
          position: relative;
          cursor: pointer;

          img {
            width: 26px;
            height: 26px;
            border-radius: 50%;
          }
        }

        .icon {
          font-size: 24px;
        }

        .active {
          position: relative;
          box-shadow: rgba(64, 158, 255, 0.2) 0px 0px 0px 2px;
          background: rgb(232, 243, 255);
          border-radius: 50%;
          /* 确保圆形背景完全显示 */
          overflow: visible;
          z-index: 1;
        }

        .emo-item-1 {
          color: #409EFF; /* 蓝色 */
        }

        .emo-item-2 {
          color: #E6A23C; /* 橙色 */
        }
      }
    }

    /* 底部区域悬停效果 */
    &:hover .emotion-bottom {
      box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.12);
    }

    /* 响应式处理 */
    @media (max-width: 576px) {
      .emotion-box {
        width: 95vw;
        left: 2.5vw !important;
      }

      .emo-items {
        padding: 6px 8px;

        .emo-items-left,
        .emo-items-right {
          width: 28px;
          height: 28px;

          i {
            font-size: 14px;
          }
        }

        .emo-item {
          width: 28px;
          height: 28px;
          margin: 0 6px;
        }

        .icon {
          font-size: 20px;
        }
      }
    }

    /* 滚动条美化 */
    .el-scrollbar ::v-deep .el-scrollbar__bar.is-horizontal {
      display: none !important;
    }
  }
}
</style>