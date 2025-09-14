<template>
  <div class="item" :class="active ? 'active' : ''">
    <div class="avatar">
      <svg class="icon svg-icon" aria-hidden="true">
        <use :xlink:href="community.iconId"></use>
      </svg>
    </div>
    <div class="text">
      <div>{{community.name}}</div>
    </div>
  </div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';

export default {
  name: "CommunityItem",
  components: {
    HeadImage
  },
  data() {
    return {}
  },
  props: {
    community: {
      type: Object
    },
    active: {
      type: Boolean
    }
  }
}
</script>

<style scoped lang="scss">
.item {
  height: 45px;
  display: flex;
  margin-bottom: 1px;
  position: relative;
  padding-left: 15px;
  align-items: center;
  padding-right: 5px;
  background-color: white;
  white-space: nowrap;
  cursor: pointer;

  &:hover {
   background-color: var(--active-color);
  }

  &.active {
   background-color: var(--active-color);
  }

  .avatar {
    position: relative;
    width: 45px;
    height: 45px;

    display: flex;
    justify-content: center; /*水平居中*/
    align-items: center; /*垂直居中*/

    .icon {
      display: block;
      height: 45px;
      line-height: 45px;
      font-size: 28px;
      color: #333;
      -webkit-transition: font-size 0.25s linear, width 0.25s linear;
      -moz-transition: font-size 0.25s linear, width 0.25s linear;
      transition: font-size 0.25s linear, width 0.25s linear;
    }
  }

  .text {
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    flex: 1;
    margin-left: 10px;
    height: 100%;
    flex-shrink: 0;
    overflow: hidden;
    text-align: left;
  }
}
</style>