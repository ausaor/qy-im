<template>

</template>

<script>
import TalkList from "@/components/talk/TalkList";

export default {
  name: "GroupTalkList",
  components: {
    TalkList,
  },
  props: {
    visible: {
      type: Boolean
    },
    groupId: {
      type: Number,
    }
  },
  data() {
    return {

    }
  }
}
</script>

<style scoped lang="scss">

</style>