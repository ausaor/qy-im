<template>
  <div class="cover">
    <img :src="require('@/assets/image/friendActivityBg.jpg')" alt="">
    <div class="info">{{name}}</div>
    <a class="operateBtn" @click="add" v-if="showAdd">
      <i class="el-icon-camera-solid"></i>
    </a>
    <a class="refreshBtn" @click="refresh">
      <i class="el-icon-refresh"></i>
    </a>
  </div>
</template>

<script>
export default {
  name: "SpaceCover",
  props: {
    name: {
      type: String
    },
    showAdd: {
      type: Boolean,
      default: true,
    }
  },
  methods: {
    add() {
      this.$emit("add");
    },
    refresh() {
      this.$emit("refresh");
    },
  }

}
</script>

<style scoped lang="scss">
.cover {
  width: 72%;
  position: relative;
  margin: 0 auto;
  img {
    border-radius: 5px;
    width: 100%;
    height: 100%;
  }

  .info {
    position: absolute;
    top: 60%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 50px;
    color: #ffffff;
    font-weight: 700;
    text-shadow: 0 0 10px red,0 0 20px red,0 0 30px red,0 0 40px red;
  }

  .operateBtn {
    position: absolute;
    top: 10px;
    right: 10px;
    display: inline-block;
    background-color: #6CC6CB;
    padding: 0 5px;
    cursor: pointer;
    color: white;
    font-weight: bold;
    font-size: 24px;
    border-radius: 50%;
  }

  .refreshBtn {
    position: absolute;
    bottom: 10px;
    right: 10px;
    display: inline-block;
    background-color: #6CC6CB;
    padding: 0 5px;
    cursor: pointer;
    color: white;
    font-weight: bold;
    font-size: 24px;
    border-radius: 50%;
  }
}
</style>