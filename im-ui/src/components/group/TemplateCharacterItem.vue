<template>
  <div class="template-character-item">
    <div class="avatar">
      <head-image :url="templateCharacter.avatar"> </head-image>
    </div>
    <div class="text">
      <div>{{ templateCharacter.name}}</div>
    </div>
  </div>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';

export default {
  name: "TemplateCharacterItem",
  components: {
    HeadImage
  },
  data() {
    return {}
  },
  props: {
    templateCharacter: {
      type: Object
    }
  },
}
</script>

<style scoped lang="scss">
.template-character-item {
  height: 65px;
  display: flex;
  margin-bottom: 1px;
  position: relative;
  padding-left: 15px;
  align-items: center;
  padding-right: 5px;
  background-color: var(--bg-color);
  white-space: nowrap;

.avatar {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 45px;
  height: 45px;
}

.text {
  margin-left: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  height: 100%;
  flex-shrink: 0;
  overflow: hidden;
  text-align: left;
}
}
</style>