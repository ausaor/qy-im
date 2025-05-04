<template>
    <div class="group-member-item" :style="{'height':height+'px'}">
        <div class="member-avatar">
            <head-image :id="member.userId" :size="headImageSize" :name="member.aliasName"
				:url="member.headImage" :online="member.online"> </head-image>
        </div>
        <div class="member-info">
            <div class="member-name">
              <span>{{ member.friendRemark ? member.friendRemark : member.aliasName }}</span>
              <span v-if="member.isLeader" style="color: white;background-color: orange;font-size: 12px;margin-left: 10px">群主</span>
            </div>
            <div class="join-type">
              <el-tag size="mini" type="success" v-if="member.joinType===1">常驻</el-tag>
              <el-tag size="mini" type="warning" v-if="member.joinType===0">临时</el-tag>
            </div>
        </div>
		<slot></slot>
    </div>
</template>

<script>
import HeadImage from "../common/HeadImage.vue";
export default {
    name: "groupMemberItem",
    components: { HeadImage },
    data() {
        return {};
    },
    props: {
        member: {
            type: Object,
            required: true
        },
        height:{
            type: Number,
            default: 50
        }
    },
    computed:{
        headImageSize(){
            return Math.ceil(this.height * 0.75)
        }
    }
}
</script>

<style scoped lang="scss">
.group-member-item {
    display: flex;
    margin-bottom: 2px;
    position: relative;
    padding-top: 2px;
    padding-left: 10px;;
    padding-right: 10px;
    align-items: center;
    background-color: var(--bg-color);
    white-space: nowrap;
    box-sizing: border-box;

    &:hover {
        background-color: #eeeeee;
    }

    &.active {
        background-color: #eeeeee;
    }

    .member-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding-left: 10px;
      text-align: left;
      overflow: hidden;

      .member-name {
        line-height: 25px;
        height: 25px;
      }

      .join-type {
        display: flex;
        line-height: 22px;
      }
    }
}
</style>
