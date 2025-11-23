<template>
	<div class="group-item" :class="active ? 'active' : ''">
		<div class="avatar">
			<head-image :size="45" :name="group.remark" :url="group.headImage"> </head-image>
      <div class="group-activity" v-show="unreadTalkCount > 0 || unreadNotifyCount > 0 || joinGroupRequestCount > 0"></div>
		</div>
		<div class="group-info">
      <div class="group-tag">
        <el-tag size="mini" effect="dark" color="rgb(0,47,167)"  v-if="group.groupType===1">模板群聊</el-tag>
        <el-tag size="mini" effect="dark" color="rgb(105,149,114)" v-if="group.groupType===2">多元角色群聊</el-tag>
        <el-tag size="mini" effect="dark" color="rgb(144,0,33)" v-if="group.groupType===3">角色群聊</el-tag>
        <el-tag size="mini" effect="dark" color="rgb(176,89,35)" v-if="group.groupType===4">模板角色群聊</el-tag>
        <span v-if="isOwner" class="group-owner">群主</span>
      </div>
			<div class="group-name">
        <span class="name">{{group.remark}}</span>
      </div>
		</div>
	</div>
</template>

<script>
	import HeadImage from '../common/HeadImage.vue';

	export default {
		name: "groupItem",
		components: {
			HeadImage
		},
		data() {
			return {}
		},
		props: {
			group: {
				type: Object
			},
			active: {
				type: Boolean
			}
		},
    computed: {
      mine() {
        return this.$store.state.userStore.userInfo;
      },
      unreadTalkCount() {
        let talkMap =this.$store.state.talkStore.groupsTalks;
        let talks = talkMap.get(this.group.id);
        if (talks) {
          return talks.length;
        }
        return 0;
      },
      unreadNotifyCount() {
        let notifyMap =this.$store.state.talkStore.groupNotify;
        let count = notifyMap.get(this.group.id);
        if (count) {
          return count;
        }
        return 0;
      },
      joinGroupRequestCount() {
        // 群组申请(当前用户是群主，待审核的加群申请)
        return this.group?.ownerId === this.mine.id ? this.$store.state.groupStore.groupRequests
            .filter((r) => r.groupId === this.group.id && r.groupOwnerId === this.mine.id && r.status === 1 && r.type === 1).length : 0;
      },
      isOwner() {
        return this.group.ownerId === this.mine.id;
      }
    }
	}
</script>

<style scoped lang="scss" >
  .group-item {
    height: 50px;
    display: flex;
    margin-bottom: 1px;
    position: relative;
    padding: 5px;
    align-items: center;
    background-color: white;
    white-space: nowrap;
    cursor: pointer;
    border-radius: 10px;
    &:hover {
      background-color: var(--active-color);
    }

    &.active {
      background-color: var(--active-color);
    }

    .avatar {
      width: 45px;
      height: 45px;
      position: relative;

      .group-activity {
        position: absolute;
        top: 0;
        left: 40px;
        width: 12px;
        height: 12px;
        border-radius: 50%;
        background: red;
      }
    }

    .group-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding-left: 10px;
      text-align: left;
      overflow: hidden;

      .group-tag {
        display: flex;
        line-height: 22px;
        height: 22px;

        .el-tag {
          min-width: 22px;
          text-align: center;
          background-color: #2830d3;
          border-radius: 10px;
          border: 0;
          height: 16px;
          line-height: 16px;
          font-size: 10px;
          margin-left: 2px;
          opacity: .8;
        }

        .group-owner {
          margin-left: 3px;
          display: inline-block;
          height: 15px;
          background-color: orange;
          color: white;
          font-size: 10px;
          padding: 0 5px;
          border-radius: 10px;
          line-height: 15px;
        }
      }

      .group-name {
        display: flex;
        align-items: center;
        line-height: 25px;
        height: 25px;
        font-weight: 600;

        .name {
          flex: 1;
          overflow: hidden;
        }
      }
    }
  }
</style>
