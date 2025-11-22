<template>
  <div v-show="show">
    <div class="emotion-box" :style="{'left':x+'px','top':y+'px'}">
      <el-scrollbar style="height:250px;position: relative;">
        <div class="emotion-item-list" v-show="emoIndex === 1">
            <div v-for="(emo, i) in emos.character" :key="i" class="emotion-item" @click="onClickEmo(emo)">
              <img :src="emo.url" style="vertical-align:bottom;"/>
            </div>
        </div>
        <div class="emotion-item-list" v-show="emoIndex === 2">
          <div v-for="(emo, i) in emos.group" :key="i" class="emotion-item" @click="onClickEmo(emo)">
            <img :src="emo.url" style="vertical-align:bottom;"/>
          </div>
        </div>
        <div class="emotion-bottom">
          <div class="emo-items">
            <div style="display: flex; align-items: center;">
              <div class="emo-item" :class="emoIndex === 1 ? 'active' : ''" @click="chooseEmo(1)">
                <span class="icon iconfont icon-biaoqing" style="color: #1E90FF"></span>
              </div>
              <el-divider direction="vertical"></el-divider>
              <div class="emo-item" :class="emoIndex === 2 ? 'active' : ''" @click="chooseEmo(2)">
                <span class="icon iconfont icon-biaoqing" style="color: orange"></span>
              </div>
            </div>
            <div class="emo-item close-btn" @click="close()">关闭</div>
          </div>
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script>
export default {
  name: "emotion",
  props: {
    emos : {
      type: Object,
      default() {
        return {}
      }
    }
  },
  data() {
    return {
      show: false,
      pos: {
        x: 0,
        y: 0
      },
      emoIndex: 1,
    }
  },
  methods: {
    onClickEmo(emo) {
      this.$emit('emotion', emo)
    },
    open(pos) {
      this.pos = pos;
      this.show = true;
    },
    close() {
      this.show = false;
    },
    chooseEmo(index) {
      this.emoIndex = index;
    }
  },
  computed: {
    x() {
      return this.pos.x - 10;
    },
    y() {
      return this.pos.y - 260;
    }
  }
}
</script>
<style scoped lang="scss">
.emotion-box {
  position: fixed;
  width: 420px;
  box-sizing: border-box;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff, #f8f9fa);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  text-align: left;
  border: 1px solid rgba(224, 224, 224, 0.8);

  .el-scrollbar ::v-deep .el-scrollbar__bar {
    display: none !important;
  }

  .el-scrollbar ::v-deep .el-scrollbar__wrap {
    overflow-x: hidden;
  }

  .emotion-item-list {
    /* grid布局 两端对齐,最后一行左对齐*/
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 10px;
    margin: 15px;
    margin-bottom: 50px;
    overflow-x: hidden;

    .emotion-item {
      text-align: center;
      cursor: pointer;
      padding: 8px;
      border-radius: 10px;
      transition: all 0.3s ease;
      background: rgba(255, 255, 255, 0.7);
      border: 1px solid rgba(240, 240, 240, 0.8);

      &:hover {
        background: rgba(64, 158, 255, 0.1);
        transform: translateY(-3px);
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
      }

      img {
        width: 50px;
        height: 50px;
        transition: transform 0.3s ease;
        
        &:hover {
          transform: scale(1.1);
        }
      }
    }
  }

  .emotion-bottom {
    position: absolute;
    bottom: 0;
    background: linear-gradient(to top, #f8f9fa, #ffffff);
    display: flex;
    width: 100%;
    height: 40px;
    padding: 8px 15px;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid rgba(224, 224, 224, 0.8);
    border-radius: 0 0 12px 12px;

    .emo-items {
      display: flex;
      justify-content: space-between;
      align-items: center;
      cursor: pointer;
      width: 100%;

      .emo-item {
        width: 35px;
        height: 35px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 8px;
        margin: 0 5px;
        transition: all 0.3s ease;

        .icon {
          font-size: 22px;
        }
        
        &.close-btn {
          position: static;
          float: none;
          right: auto;
          color: #f56c6c;
          font-size: 14px;
          font-weight: 500;
          width: auto;
          padding: 0 12px;
          height: 30px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 6px;
          border: 1px solid #f56c6c;
          
          &:hover {
            background: rgba(245, 108, 108, 0.1);
          }
        }

        &:hover {
          background-color: rgba(64, 158, 255, 0.1);
        }
      }

      .active {
        background-color: rgba(64, 158, 255, 0.2);
        box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
        border: 1px solid #409EFF;
      }
      
      .el-divider {
        margin: 0 5px;
      }
    }
  }
}
</style>