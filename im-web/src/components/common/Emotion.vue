<template>
  <div v-show="show">
    <div class="emotion-box" :style="{'left': x +'px','top':y+'px'}">
      <div class="close-btn" @click="close">×</div>
      <!-- 悬停预览窗口 -->
      <div 
        v-show="previewVisible" 
        class="preview-window" 
        :class="'preview-' + previewSide"
        :style="{
          left: previewPosition.x + 'px',
          top: previewPosition.y + 'px'
        }"
      >
        <img :src="previewImageUrl" alt="预览图片" />
      </div>
      <right-menu v-show="rightMenu.show" :pos="rightMenu.pos" :items="rightMenu.items" @close="rightMenu.show=false"
                  @select="onSelectMenu"></right-menu>
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
          <div 
            class="sticker-item" 
            v-for="(item, index) in emoAlbumImgs" 
            :key="index" 
            @click="chooseEmoAlbumImg(item)"
            @mouseenter="showPreview($event, item.imageUrl)"
            @mouseleave="hidePreview()"
          >
            <img :src="item.thumbUrl" :alt="item.name">
            <div class="sticker-name" :title="item.name">{{item.name}}</div>
          </div>
        </div>
        <div class="search-emo-box" v-show="emoType === 'search'">
          <div class="search-input">
            <el-input class="search-text" size="small" placeholder="输入表情名称搜索" v-model="searchText" clearable @change="searchEmoji">
              <i class="el-icon-search el-input__icon" slot="prefix"></i>
            </el-input>
          </div>
          <div class="search-emo-list">
            <div 
              class="sticker-item" 
              v-for="(item, index) in searchEmojiList" 
              :key="index" 
              @click="chooseEmoAlbumImg(item)"
              @mouseenter="showPreview($event, item.imageUrl)"
              @mouseleave="hidePreview()"
            >
              <img :src="item.thumbUrl" :alt="item.name">
              <div class="sticker-name" :title="item.name">{{item.name}}</div>
            </div>
          </div>
        </div>
        <div class="collect-emo-box" v-show="emoType === 'collect'">
          <div class="sticker-item upload-item">
            <file-upload class="image-uploader" :action="imageAction"
                         :showLoading="true" :maxSize="maxSize" @success="onUploadImage"
                         :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']">
              <i class="el-icon-plus"></i>
            </file-upload>
          </div>
          <div class="sticker-item" v-for="(item, index) in favoriteEmojiList" :key="index"
            @click="chooseCollectEmoImg(item)"
            @contextmenu.prevent="showCollectRightMenu($event, item, index)"
            @mouseenter="showPreview($event, item.imageUrl)"
            @mouseleave="hidePreview()">
            <img :src="item.thumbUrl" :alt="item.name">
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
              <div class="emo-item el-icon-search" :class="emoType === 'search' ? 'active' : ''"  @click="toSearchEmo"></div>
              <div class="emo-item icon iconfont icon-hongxin1" :class="emoType === 'collect' ? 'active' : ''"  @click="toCollectEmo"></div>
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
import FileUpload from "@components/common/FileUpload.vue";
import RightMenu from "@components/common/RightMenu.vue";

export default {
  name: "emotion",
  components: {RightMenu, FileUpload},
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
      searchText: '',
      maxSize: 2 * 1024 * 1024,
      favoriteEmojiList: [],
      searchEmojiList: [],
      // 悬停预览相关数据
      previewImageUrl: '',
      previewVisible: false,
      previewPosition: { x: 0, y: 0 },
      previewSide: 'right', // 'left' 或 'right'
      rightMenu: {
        show: false,
        pos: {
          x: 0,
          y: 0
        },
        items: [{
          key: 'TOP',
          name: '置顶',
          icon: 'el-icon-top'
        }, {
          key: 'DELETE',
          name: '删除',
          icon: 'el-icon-delete'
        }]
      },
      currentSelectedItem: null
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
    chooseCollectEmoImg(albumImg) {
      albumImg.id = albumImg.emoId;
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
    toSearchEmo() {
      this.albumId = null;
      this.emoType = 'search';
    },
    toCollectEmo() {
      this.albumId = null;
      this.emoType = 'collect';
      this.queryFavoriteEmoji();
    },
    scrollLeft() {
      const totalItems = 4 + this.emoAlbums.length; // 2个默认表情 + 相册数量
      const maxPosition = Math.max(0, totalItems - this.visibleItems);
      this.scrollPosition = Math.max(0, this.scrollPosition - 5);
    },
    scrollRight() {
      const totalItems = 4 + this.emoAlbums.length;
      const maxPosition = Math.max(0, totalItems - this.visibleItems);
      this.scrollPosition = Math.min(maxPosition, this.scrollPosition + 5);
    },
    updateMaxScroll() {
      this.$nextTick(() => {
        const totalItems = 4 + this.emoAlbums.length;
        this.maxScroll = Math.max(0, totalItems - this.visibleItems);
      });
    },
    onUploadImage(data) {
      let param = {
        imageUrl: data.originUrl,
      }
      this.$http({
        url: '/emoFavorite/addEmoFavorite',
        method: 'post',
        data: param
      }).then(result => {
        this.$message.success('上传成功');
        this.favoriteEmojiList.unshift(result);
      })
    },
    queryFavoriteEmoji() {
      this.$http({
        url: '/emoFavorite/list',
        method: 'get',
      }).then(result => {
        this.favoriteEmojiList = result;
      })
    },
    topFavoriteEmoji(item, index) {
      // 置顶收藏表情
      // this.$http({
      //   url: '/emoFavorite/top',
      //   method: 'post',
      //   data: {
      //     emoId: item.emoId
      //   }
      // }).then(() => {
      //   this.$message.success('置顶成功');
      //   // 将该表情移动到列表最前面
      //   this.favoriteEmojiList.splice(index, 1);
      //   this.favoriteEmojiList.unshift(item);
      // }).catch(() => {
      //   this.$message.error('置顶失败');
      // });
    },
    deleteFavoriteEmoji(item, index) {
      // 删除收藏表情
      this.$http({
        url: `/emoFavorite/delete`,
        method: 'post',
        data: {
          id: item.id
        }
      }).then(() => {
        this.$message.success('删除成功');
        // 从列表中移除该表情
        this.favoriteEmojiList.splice(index, 1);
      }).catch(() => {
        this.$message.error('删除失败');
      });
    },
    searchEmoji() {
      if (!this.searchText) {
        return
      }
      this.$http({
        url: '/emoImg/search',
        method: 'get',
        params: {
          name: this.searchText
        }
      }).then(result => {
        this.searchEmojiList = result;
      })
    },
    // 悬停预览相关方法
    showPreview(event, imageUrl) {
      if (!imageUrl) return;
      
      this.previewImageUrl = imageUrl;
      this.calculatePreviewPosition(event);
      this.previewVisible = true;
    },
    hidePreview() {
      this.previewVisible = false;
      this.previewImageUrl = '';
    },
    calculatePreviewPosition(event) {
      // 获取 emotion-box 容器的边界信息
      const emotionBox = this.$el.querySelector('.emotion-box');
      if (!emotionBox) return;
      
      const containerRect = emotionBox.getBoundingClientRect();
      const scrollContainer = emotionBox.closest('.el-scrollbar__wrap');
      const scrollTop = scrollContainer ? scrollContainer.scrollTop : 0;
      
      // 计算相对位置（相对于 emotion-box）
      const mouseX = event.clientX - containerRect.left;
      const mouseY = event.clientY - containerRect.top + scrollTop;
      
      // 判断应该在左侧还是右侧显示
      const containerWidth = containerRect.width;
      this.previewSide = mouseX < containerWidth / 2 ? 'right' : 'left';
      
      // 调整预览窗口尺寸
      const previewWidth = 160;
      const previewHeight = 160;
      
      let x, y;
      
      if (this.previewSide === 'right') {
        x = mouseX + 20; // 右侧显示
      } else {
        x = mouseX - previewWidth - 20; // 左侧显示
      }
      
      // 垂直居中对齐鼠标位置
      y = mouseY - previewHeight / 2;
      
      // 边界检测和修正
      const margin = 15;
      
      // 水平边界
      if (x < margin) x = margin;
      if (x + previewWidth > containerWidth - margin) {
        x = containerWidth - previewWidth - margin;
      }
      
      // 垂直边界
      const containerHeight = containerRect.height;
      if (y < margin) y = margin;
      if (y + previewHeight > containerHeight - margin) {
        y = containerHeight - previewHeight - margin;
      }
      
      this.previewPosition = { x, y };
    },
    showRightMenu(e) {
      this.rightMenu.pos = {
        x: e.x,
        y: e.y
      };
      this.rightMenu.show = "true";
    },
    onSelectMenu(item) {
      // 如果有当前选中的收藏表情项，则处理收藏表情的菜单操作
      if (this.currentSelectedItem) {
        const { item: selectedItem, index } = this.currentSelectedItem;
        
        switch(item.key) {
          case 'TOP':
            this.topFavoriteEmoji(selectedItem, index);
            break;
          case 'DELETE':
            this.deleteFavoriteEmoji(selectedItem, index);
            break;
        }
        
        // 清空当前选中项
        this.currentSelectedItem = null;
      } else {
        // 处理其他类型的菜单操作
        this.$emit(item.key.toLowerCase());
      }
    },
    showCollectRightMenu(e, item, index) {
      // 关闭预览弹窗
      this.hidePreview();
      
      // 显示右键菜单
      this.rightMenu.pos = {
        x: e.clientX,
        y: e.clientY
      };
      this.rightMenu.show = true;
      
      // 传递当前选中的表情信息
      this.currentSelectedItem = {
        item: item,
        index: index
      };
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
      const totalItems = 4 + this.emoAlbums.length;
      return this.scrollPosition < Math.max(0, totalItems - this.visibleItems);
    },
    imageAction(){
      return `/image/upload`;
    },
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
  overflow: visible; // 允许预览窗口超出容器

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

  .search-emo-box {
    padding: 12px;
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    border-radius: 12px;
    margin: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid rgba(0, 0, 0, 0.03);

    .search-input {
      margin-bottom: 12px;
      
      .search-text ::v-deep .el-input__inner {
        border-radius: 24px;
        border: 2px solid #e9ecef;
        background: #ffffff;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        padding-left: 36px;
        
        &:focus {
          border-color: #409eff;
          box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
        }
      }
      
      .el-icon-search {
        color: #6c757d;
      }
    }

    .search-emo-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, 64px);
      grid-auto-rows: minmax(80px, auto);
      gap: 12px;
      padding: 4px;
      
      .sticker-item {
        width: 100%;
        min-height: 80px;
        border-radius: 12px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        padding: 8px;
        background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
        border: 1px solid rgba(0, 0, 0, 0.05);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
        position: relative;
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 3px;
          background: linear-gradient(90deg, #409eff, #667eea);
          transform: scaleX(0);
          transition: transform 0.3s ease;
        }
        
        &:hover {
          transform: translateY(-4px) scale(1.03);
          box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15);
          border-color: rgba(64, 158, 255, 0.3);
          background: linear-gradient(135deg, #ffffff 0%, #e3f2fd 100%);
          
          &::before {
            transform: scaleX(1);
          }
          
          .sticker-name {
            color: #409eff;
            font-weight: 500;
          }
        }
        
        &:active {
          transform: translateY(-2px) scale(0.98);
        }

        img {
          width: 52px;
          height: 52px;
          border-radius: 8px;
          object-fit: cover;
          transition: all 0.3s ease;
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
        }

        .sticker-name {
          margin-top: 8px;
          font-size: 13px;
          color: #495057;
          text-align: center;
          width: 100%;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          font-weight: 400;
          transition: all 0.3s ease;
        }
      }
    }
  }

  .collect-emo-box {
    background: linear-gradient(135deg, #fffaf0 0%, #ffffff 100%);
    border-radius: 12px;
    margin: 8px;
    box-shadow: 0 2px 12px rgba(255, 165, 0, 0.08);
    border: 1px solid rgba(255, 165, 0, 0.1);

    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(64px, 1fr));
    grid-auto-rows: 64px;
    gap: 12px;
    padding: 4px;
    justify-items: center;

    .sticker-item {
      width: 64px;
      height: 64px;
      border-radius: 12px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      padding: 8px;
      background: linear-gradient(135deg, #ffffff 0%, #fff8e1 100%);
      border: 1px solid rgba(255, 165, 0, 0.15);
      box-shadow: 0 2px 8px rgba(255, 165, 0, 0.08);
      position: relative;
      overflow: hidden;
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 3px;
        background: linear-gradient(90deg, #ffa500, #ffb74d);
        transform: scaleX(0);
        transition: transform 0.3s ease;
      }
      
      &:hover {
        transform: translateY(-4px) scale(1.05);
        box-shadow: 0 8px 24px rgba(255, 165, 0, 0.2);
        border-color: rgba(255, 165, 0, 0.4);
        background: linear-gradient(135deg, #ffffff 0%, #ffecb3 100%);

        &::before {
          transform: scaleX(1);
        }
      }

      &:active {
        transform: translateY(-2px) scale(0.98);
      }

      img {
        width: 64px;
        height: 64px;
        object-fit: cover;
      }
    }

    .upload-item {
      .image-uploader {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .el-icon-plus {
          font-size: 24px;
          color: #ffa500;
          transition: all 0.3s ease;
        }
        
        &:hover .el-icon-plus {
          color: #ff8c00;
          transform: scale(1.2);
        }
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
          margin: 0 8px;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
          position: relative;
          cursor: pointer;
          border-radius: 50%;

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

// 悬停预览窗口样式
.preview-window {
  position: absolute;
  width: 160px;
  height: 160px;
  background-color: rgba(0, 0, 0, 0.7); // 更深的透明黑色背景
  border-radius: 12px;
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.4), 0 0 0 1px rgba(255, 255, 255, 0.1);
  z-index: 100;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(12px); // 增强背景模糊效果
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1); // 添加过渡效果
  
  img {
    max-width: 95%;
    max-height: 95%;
    object-fit: contain;
    border-radius: 8px;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
    transition: transform 0.2s ease;
    
    &:hover {
      transform: scale(1.05);
    }
  }
  
  // 右侧显示的箭头指示器
  &.preview-right::before {
    content: '';
    position: absolute;
    left: -10px;
    top: 50%;
    transform: translateY(-50%);
    width: 0;
    height: 0;
    border-top: 10px solid transparent;
    border-bottom: 10px solid transparent;
    border-right: 10px solid rgba(0, 0, 0, 0.92);
    filter: drop-shadow(-2px 0 2px rgba(0, 0, 0, 0.2));
  }
  
  // 左侧显示的箭头指示器
  &.preview-left::before {
    content: '';
    position: absolute;
    right: -10px;
    top: 50%;
    transform: translateY(-50%);
    width: 0;
    height: 0;
    border-top: 10px solid transparent;
    border-bottom: 10px solid transparent;
    border-left: 10px solid rgba(0, 0, 0, 0.92);
    filter: drop-shadow(2px 0 2px rgba(0, 0, 0, 0.2));
  }
}
</style>