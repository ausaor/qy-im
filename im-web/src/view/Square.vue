<template>
  <el-container class="im-box-square">
    <el-aside width="260px" class="l-square-box">
      <div class="l-square-list">
        <community-item-talk :community="communityList[0]" :active="communityList[0].sort === activeIndex"
                          @click.native="gotoTalkSpace(communityList[0])"></community-item-talk>
        <community-item :community="communityList[1]" :active="communityList[1].sort === activeIndex"
                        @click.native="handleActiveItem(communityList[1])" :notify-count="totalCharacterNotifyCount"></community-item>
        <community-item v-if="isAdmin" :community="communityList[2]" :active="communityList[2].sort === activeIndex"
                        @click.native="handleActiveItem(communityList[2])"></community-item>
        <community-item v-if="isAdmin" :community="communityList[3]" :active="communityList[3].sort === activeIndex"
                        @click.native="handleActiveItem(communityList[3])"></community-item>
        <community-item v-if="isAdmin" :community="communityList[4]" :active="communityList[4].sort === activeIndex"
                        @click.native="handleActiveItem(communityList[4])"></community-item>
        <community-item v-if="isSuperAdmin" :community="communityList[5]" :active="communityList[5].sort === activeIndex"
                        @click.native="handleActiveItem(communityList[5])"></community-item>
        <community-item v-if="isSuperAdmin" :community="communityList[6]" :active="communityList[6].sort === activeIndex"
                        @click.native="handleActiveItem(communityList[6])"></community-item>
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
        {name: "群聊模板", sort: 2, route: '/home/square/templateGroup', iconId: '#icon-qiqiaoban'},
        {name: "用户管理", sort: 3, route: '/home/square/users', iconId: '#icon-person'},
        {name: "群聊管理", sort: 4, route: '/home/square/groups', iconId: '#icon-qunliaoguanli'},
        {name: "地区管理", sort: 5, route: '/home/square/regions', iconId: '#icon-diquguanli'},
        {name: "系统消息", sort: 6, route: '/home/square/sysMsg', iconId: '#icon-xitongxiaoxi'},
        {name: "歌曲管理", sort: 7, route: '/home/square/musics', iconId: '#icon-Music'},
      ],
      activeIndex: -1,
    }
  },
  methods: {
    gotoTalkSpace(community) {
      if (this.unreadUserCount > 0 || this.notifyCount > 0) {
        if (this.notifyCount > 0) {
          this.readedTalkNotify();
        }
        this.$store.commit("resetUnreadTalkInfo")
      }

      this.activeIndex = community.sort;
      this.$router.push(community.route);
    },
    handleActiveItem(community) {
      //console.log("community", community)
      this.activeIndex = community.sort;
      this.$router.push(community.route);
    },
    readedTalkNotify() {
      let params = {
        category: 'private'
      };
      this.$http({
        url: `/talk-notify/readed`,
        method: 'post',
        data: params
      }).then(() => {})
    }
  },
  computed: {
    unreadUserCount() {
      return this.$store.state.talkStore.unreadUserList.length;
    },
    notifyCount() {
      return this.$store.state.talkStore.notifyCount;
    },
    totalCharacterNotifyCount() {
      return this.$store.getters.getTotalCharacterNotifyCount();
    },
    mine() {
      return this.$store.state.userStore.userInfo;
    },
    isAdmin() {
      // 是否管理人员
      return this.mine.role === 'SUPER_ADMIN' || this.mine.role === 'ADMIN';
    },
    isSuperAdmin() {
      return this.mine.role === 'SUPER_ADMIN';
    }
  }
}
</script>

<style scoped lang="scss">
.im-box-square {
  .l-square-box {
    display: flex;
    flex-direction: column;
    border-right: #cccccc solid 1px;
    background: white;
    overflow: hidden;

    .badge-item {
      margin-top: 10px;
    }
  }

  .community-box {
    margin-top: -10px;
    flex: 1;
  }
}
</style>