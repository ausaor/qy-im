<template>

</template>

<script>
export default {
  name: "short-video-user",
  data() {
    return {

    }
  },
  computed: {
    mine() {
      return this.userStore.userInfo;
    },
  },
  onLoad(options) {
    console.log(options);
  }
}
</script>

<style scoped lang="scss">

</style>