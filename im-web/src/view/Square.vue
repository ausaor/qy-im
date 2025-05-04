<template>
  <el-container class="im-box-square">
    <el-aside width="14%" class="l-square-box">
      <div class="l-square-list">
        <div v-for="(community,index) in communityList" :key="index">
          <community-item :community="community" :active="index === activeIndex"
                          @click.native="handleActiveItem(community,index)"></community-item>
        </div>
      </div>
    </el-aside>
    <el-main class="community-box">
      <router-view></router-view>
    </el-main>
  </el-container>
</template>

<script>
import CommunityItem from "@/components/square/CommunityItem";

export default {
  name: "Square",
  components: {
    CommunityItem
  },
  data() {
    return {
      communityList: [
        {name: "群聊模板", sort: 1, route: '/home/square/templateGroup', iconId: '#icon-qiqiaoban'},
        {name: "空间动态", sort: 2, route: '/home/square/friendActivity', iconId: '#icon-shejiaotubiao-40'}
      ],
      activeIndex: -1,
    }
  },
  methods: {
    handleActiveItem(community, index) {
      //console.log("community", community)
      this.activeIndex = index;
      this.$router.push(community.route);
    }
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
  }

  .community-box {
    margin-top: -10px;
  }
}
</style>