<template>
  <div  v-show="show">
    <div class="emotion-box" :style="{'left':x+'px','top':y+'px'}">
      <el-scrollbar style="height:250px;position: relative;">
        <div class="emotion-item-list" v-show="emoIndex === 1">
          <el-tooltip class="item" effect="dark" v-for="(emoText, i) in $emo.emoTextList" :key="i"
                      :content="emoText.replace(/emo/g, '')" placement="top">
            <div class="emotion-item" @click="onClickEmo(emoText)" v-html="$emo.textToImg(emoText)"></div>
          </el-tooltip>
        </div>
        <div class="emotion-item-list" v-show="emoIndex === 2">
          <el-tooltip class="item" effect="dark" v-for="(emoText, i) in $emo.originalEmoTextList" :key="i"
                      :content="emoText" placement="top">
            <div class="emotion-item" @click="onClickEmo(emoText)" v-html="$emo.textToImg(emoText)"></div>
          </el-tooltip>
        </div>
        <div class="emotion-bottom">
          <div class="emo-items">
            <div class="emo-item" :class="emoIndex === 1 ? 'active' : ''" v-html="$emo.textToImg($emo.emoTextList[0])" @click="chooseEmo(1)"></div>
            <el-divider direction="vertical"></el-divider>
            <div class="emo-item" :class="emoIndex === 2 ? 'active' : ''" v-html="$emo.textToImg($emo.originalEmoTextList[13])" @click="chooseEmo(2)"></div>
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
      return this.pos.x - 200;
    },
    y() {
      return this.pos.y - 280;
    }
  }
}
</script>
<style scoped lang="scss">


.emotion-box {
  position: fixed;
  width: 500px;
  box-sizing: border-box;
  border: 1px solid #b4b4b4;
  border-radius: 5px;
  background-color: #f5f5f5;
  box-shadow: 0 0 10px #ccc;

  .emotion-item-list {
    /* grid布局 两端对齐,最后一行左对齐*/
    display: grid;
    grid-template-columns: repeat(10,1fr);
    gap: 5px;
    margin-bottom: 35px;
    overflow-x: hidden;

    .emotion-item {
      text-align: center;
      cursor: pointer;
      padding: 5px;
    }

  }

  &:after {
    content: "";
    position: absolute;
    left: 185px;
    bottom: -30px;
    width: 0;
    height: 0;
    border-style: solid dashed dashed;
    border-color: #f5f5f5 transparent transparent;
    overflow: hidden;
    border-width: 15px;
  }

  .emotion-bottom {
    position: absolute;
    bottom: 0;
    background: #f5f5f5;
    display: flex;
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
      }

      .active {
        background-color: #b6e8d9;
      }
    }
  }
}
</style>