<template>
  <el-container class="im-box-square">
    <el-aside width="14%" class="l-square-box">
      <div class="l-square-list">
        <community-item-talk :community="communityList[0]" :active="communityList[0].sort === activeIndex"
                          @click.native="gotoTalkSpace(communityList[0])"></community-item-talk>
        <community-item :community="communityList[1]" :active="communityList[1].sort === activeIndex"
                        @click.native="handleActiveItem(communityList[1])"></community-item>
      </div>
    </el-aside>
    <el-main class="community-box">
      <router-view></router-view>
    </el-main>
  </el-container>
</template>

<script>
import CommunityItem from "@/components/square/CommunityItem";
import CommunityItemTalk  from "../components/square/CommunityItemTalk.vue";

export default {
  name: "Square",
  components: {
    CommunityItem,
    CommunityItemTalk
  },
  data() {
    return {
      communityList: [
        {name: "空间动态", sort: 1, route: '/home/square/friendActivity', iconId: '#icon-shejiaotubiao-40'},
        {name: "群聊模板", sort: 2, route: '/home/square/templateGroup', iconId: '#icon-qiqiaoban'}
      ],
      activeIndex: -1,
    }
  },
  methods: {
    gotoTalkSpace(community) {
      if (this.unreadUserCount > 0) {
        this.$store.commit("resetUnreadTalkInfo")
      }
      this.activeIndex = community.sort;
      this.$router.push(community.route);
    },
    handleActiveItem(community) {
      //console.log("community", community)
      this.activeIndex = community.sort;
      this.$router.push(community.route);
    }
  },
  computed: {
    unreadUserCount() {
      return this.$store.state.talkStore.unreadUserList.length;
    },
  }
}
</script>

<style scoped lang="scss">
.im-box-square {
  .l-square-box {
    display: flex;
    flex-direction: column;
    border-right: var(--line-color) solid 1px;
    background: white;
    overflow: hidden;

    .badge-item {
      margin-top: 10px;
    }
  }

  .community-box {
    margin-top: -10px;
  }
}
</style>