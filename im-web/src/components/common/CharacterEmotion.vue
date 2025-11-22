<template>
  <div  v-show="show">
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
            <div class="emo-item" :class="emoIndex === 1 ? 'active' : ''" @click="chooseEmo(1)">
              <span class="icon iconfont icon-biaoqing" style="color: #1E90FF"></span>
            </div>
            <el-divider direction="vertical"></el-divider>
            <div class="emo-item" :class="emoIndex === 2 ? 'active' : ''" @click="chooseEmo(2)">
              <span class="icon iconfont icon-biaoqing" style="color: orange"></span>
            </div>
            <div class="emo-item" style="float: right;color: red;position: absolute;right: 5px;" @click="close()">关闭</div>
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
  width: 400px;
  box-sizing: border-box;
  border-radius: 5px;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.12), 0 0 6px rgba(0, 0, 0, 0.04);

  .emotion-item-list {
    /* grid布局 两端对齐,最后一行左对齐*/
    display: grid;
    grid-template-columns: repeat(6,1fr);
    gap: 5px;
    margin-bottom: 35px;
    overflow-x: hidden;

    .emotion-item {
      text-align: center;
      cursor: pointer;
      padding: 5px;

      img {
        width: 50px;
        height: 50px;
      }
    }

  }

  .emotion-bottom {
    position: absolute;
    bottom: 0;
    background: white;
    display: flex;
    height: 35px;
    width: 100%;

    .emo-items {
      display: flex;
      justify-content: center;
      align-items: center;
      cursor: pointer;

      .emo-item {
        width: 35px;
        height: 35px;
        display: flex;
        align-items: center;
        justify-content: center;

        .icon {
          font-size: 22px;
        }
      }

      .active {
        background-color: #ededed;
      }
    }
  }
}
</style>