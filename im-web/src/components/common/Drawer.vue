<template>
  <div ref="drawerBox" class="drawer-box" v-show="visible" :style="drawerBoxStyle">
    <div class="close-btn" @click="close" v-show="showClose" :style="closeBtnStyle">&gt;</div>
    <header>
      <slot name="header"></slot>
    </header>
    <main>
      <slot name="main"></slot>
    </main>
  </div>
</template>

<script>
export default {
  name: "Drawer",
  data() {
    return {}
  },
  props: {
    visible: {
      type: Boolean,
      required: true,
      default: false,
    },
    showClose: {
      type: Boolean,
      default: true,
    },
    width: {
      type: Number,
      default: 50,
    },
    height: {
      type: Number,
      default: 100
    },
    zIndex: {
      type: Number,
      default: 1,
    }
  },
  mounted () {

  },
  // beforeDestroy 与 destroyed 里面移除都行
  beforeDestroy () {

  },
  methods: {
    close() {
      this.$emit("close");
    },
  },
  computed: {
    drawerBoxStyle() {
      let w = this.width;
      let h = this.height;
      let zIndex = this.zIndex
      return `width:${w}%; height:${h}%;z-index:${zIndex};`
    },
    closeBtnStyle() {
      let w = this.width;
      return `right:${w}%;`
    },
  }
}
</script>

<style scoped lang="scss">
.drawer-box {
  position: fixed;
  right: 0;
  top: 0;
  bottom: 0;
  transition: right linear 1.5s;
  border-radius: 6px;

  box-sizing: border-box;
  background-color: #FFF;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 10px -5px rgba(0, 0, 0, .2), 0 16px 24px 2px rgba(0, 0, 0, .14), 0 6px 30px 5px rgba(0, 0, 0, .12);
  overflow: scroll;
  outline: 0;

  .close-btn {
    position: fixed;
    top: 50%;
    transform: translate(30px, -30px);
    width: 30px;
    height: 60px;
    line-height: 60px;
    border: none;
    cursor: pointer;
    font-size: 18px;
    color: #fff;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 0 8px 8px 0;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: -2px 2px 10px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;
    font-weight: bold;
  }
}
</style>