<template>
  <div v-show="show">
    <div class="emotion-box" :style="{'left': x +'px','top':y+'px'}">
      <div class="emotion-top">
        <div class="emo-items">
          <div class="emo-items-left">
            <div class="emo-item" :class="emoIndex === 1 ? 'active' : ''" v-html="$emo.textToImg($emo.emoTextList[0])" @click="chooseEmo(1)"></div>
            <el-divider direction="vertical"></el-divider>
            <div class="emo-item" :class="emoIndex === 2 ? 'active' : ''" v-html="$emo.textToImg($emo.originalEmoTextList[13])" @click="chooseEmo(2)"></div>
          </div>
          <div class="emo-items-right">
            <div class="emo-item close-button" @click="close()">×</div>
          </div>
        </div>
      </div>
      <el-scrollbar style="height:250px;position: relative;">
        <div class="emotion-item-list" v-show="emoIndex === 1">
          <div v-for="(emoText, i) in $emo.emoTextList" :key="i">
            <div class="emotion-item" @click="onClickEmo(emoText)" v-html="$emo.textToImg(emoText)"></div>
          </div>
        </div>
        <div class="emotion-item-list" v-show="emoIndex === 2">
          <div v-for="(emoText, i) in $emo.originalEmoTextList" :key="i">
            <div class="emotion-item" @click="onClickEmo(emoText)" v-html="$emo.textToImg(emoText)"></div>
          </div>
        </div>
      </el-scrollbar>
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
    }
  },
  methods: {
    onClickEmo(emoText) {
      let emotion = `#${emoText};`
      this.$emit('emotion', emotion)
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
      return this.pos.x - 20;
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
  width: 500px;
  box-sizing: border-box;
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;

  .el-scrollbar ::v-deep .el-scrollbar__bar {
    display: none !important;
  }

  .el-scrollbar ::v-deep .el-scrollbar__wrap {
    overflow-x: hidden;
  }
  
  .emotion-top {
    position: absolute;
    top: 0;
    background: linear-gradient(to bottom, #ffffff, #fafafa);
    display: flex;
    width: 100%;
    height: 40px;
    border-bottom: 1px solid #f0f0f0;
    border-radius: 12px 12px 0 0;
    z-index: 10;

    .emo-items {
      display: flex;
      justify-content: center;
      align-items: center;
      cursor: pointer;
      width: 100%;
      padding: 0 5px;

      .emo-items-left {
        display: flex;
        align-items: center;
      }

      .emo-item {
        width: 25px;
        height: 25px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 8px;
        margin: 5px;
        transition: all 0.2s ease;

        &:hover {
          background-color: #e6f7ff;
        }
      }

      .active {
        background-color: #e6f7ff;
        box-shadow: 0 0 0 2px rgba(0, 120, 215, 0.2);
      }
      
      .emo-items-right {
        display: flex;
        align-items: center;
        justify-content: flex-end;
        width: 100%;
      }

      .close-button {
        color: #ff6b6b;
        font-size: 26px;
        font-weight: 500;
        position: static;
        float: none;
        right: auto;
        transition: all 0.2s ease;
        width: auto;
        padding: 0 10px;
        
        &:hover {
          color: #ff5252;
          background-color: #ffebee;
        }
      }
    }
  }

  .emotion-item-list {
    /* grid布局 两端对齐,最后一行左对齐*/
    display: grid;
    grid-template-columns: repeat(10,1fr);
    grid-auto-rows: min-content; /* 设置自动生成行的高度为内容的最小高度 */
    /* 为所有网格项设置内容居中 */
    place-items: center;
    gap: 8px;
    padding: 10px;
    overflow-x: hidden;
    margin-top: 30px;

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
  
  /* 移除废弃的 emotion-bottom 样式 */
}
</style>