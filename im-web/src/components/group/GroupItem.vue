<template>
	<div class="group-item" :class="active ? 'active' : ''">
		<div class="avatar">
			<head-image :size="45" :name="group.remark" :url="group.headImage"> </head-image>
		</div>
		<div class="group-info">
      <div class="group-tag">
        <el-tag size="mini" effect="dark" color="rgb(0,47,167)"  v-if="group.groupType===1">模板群聊</el-tag>
        <el-tag size="mini" effect="dark" color="rgb(105,149,114)" v-if="group.groupType===2">多元角色群聊</el-tag>
        <el-tag size="mini" effect="dark" color="rgb(144,0,33)" v-if="group.groupType===3">角色群聊</el-tag>
        <el-tag size="mini" effect="dark" color="rgb(176,89,35)" v-if="group.groupType===4">模板角色群聊</el-tag>
      </div>
			<div class="group-name">{{group.remark}}</div>
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
    background-color: var(--bg-color);
    white-space: nowrap;
    &:hover {
      background-color: var(--active-color);
    }

    &.active {
      background-color: var(--active-color);
    }

    .group-avatar {
      width: 45px;
      height: 45px;
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
      }

      .group-name {
        display: flex;
        line-height: 25px;
        height: 25px;
        font-weight: 600;
      }
    }
  }
</style>
