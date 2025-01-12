<template>
  <div v-show="show">
    <div class="word-box" :style="{'left':x+'px','top':y+'px'}">
      <el-scrollbar style="height:250px;position: relative;">
        <div class="word-list">
          <div v-for="(item, i) in words" :key="i">
              {{item.word}}
          </div>
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script>
export default {
  name: "Word",
  props: {
    words : {
      type: Array,
    }
  },
  data() {
    return {
      show: false,
      pos: {
        x: 0,
        y: 0
      },
    }
  },
  methods: {
    open(pos) {
      this.pos = pos;
      this.show = true;
    },
    close() {
      this.show = false;
    },
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

<style lang="scss" scoped>
.word-box {
  position: fixed;
  width: 300px;
  box-sizing: border-box;
  border: 1px solid #b4b4b4;
  border-radius: 5px;
  background-color: #f5f5f5;
  box-shadow: 0 0 10px #ccc;
  text-align: left;
  overflow: hidden;

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
}
</style>