<template>
  <div id="app">
		<router-view></router-view>
  </div>
</template>

<script>


export default {
  name: 'App',
  components: {
  },
  created() {
    this.$http({
      url: "/website/report",
      method: "post",
    }).then((data) => {
    })
  }
}
</script>

<style lang="scss">
 @import './assets/style/global.css';
 
#app {
  /*font-family: 'Avenir', Helvetica, Arial, sans-serif;*/
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  position: absolute;
  height: 100%;
  width: 100%;
}

 .el-message {
   z-index: 99999999 !important;
 }

 /* 隐藏全局滚动条 */
 html::-webkit-scrollbar,
 body::-webkit-scrollbar {
   width: 0 !important;
   height: 0 !important;
   background: transparent;
 }

 html {
   scrollbar-width: none; /* Firefox */
   -ms-overflow-style: none; /* IE 10+ */
 }

 body {
   -ms-overflow-style: none;
   overflow: auto;
 }

 .el-scrollbar__thumb {
   //background-color: var(--theme-color) !important;
 }

 .el-dialog {
   border-radius: 8px !important;
   overflow: hidden !important;
 }

 .el-dialog__header {
   padding: 12px 18px !important;
   position: relative;
   text-align: center;
 }

 .el-dialog__close {
   font-size: 20px;
 }

 .el-dialog__title {
   font-size: 18px;
   font-weight: 600;
   color: #000000;
   letter-spacing: .5px;
 }

 .el-dialog__headerbtn {
   top: 15px;
   right: 20px;
   font-size: 18px;
 }

 .el-checkbox__inner {
   border-radius: 50% !important;
 }

 .el-input__inner {
   border-radius: 5px !important;
   //border: var(--line-color) 1px solid !important;
 }

 .el-textarea__inner{
   border-radius: 5px !important;
   //border: var(--line-color) 1px solid !important;
 }

 .el-icon-search {
   //color: var(--line-color) !important;
 }

 .el-checkbox {
   display: flex;
   align-items: center;

   //修改选中框的大小
   .el-checkbox__inner {
     width: 20px;
     height: 20px;

     //修改选中框中的对勾的大小和位置
     &::after {
       height: 12px;
       left: 7px;
     }
   }

   //修改点击文字颜色不变
   .el-checkbox__input.is-checked+.el-checkbox__label {
     color: #333333;
   }

   .el-checkbox__label {
     line-height: 20px;
     padding-left: 8px;
   }
 }
</style>
