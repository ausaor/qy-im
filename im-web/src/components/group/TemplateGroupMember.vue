<template>
	<div class="template-group-member">
    <div class="avatar">
      <head-image :url="member.userAvatar" :name="member.nickName"></head-image>
    </div>
    <div class="text">
      <div>{{member.nickName}}</div>
    </div>
    <div v-if="member.isAdmin" class="is-admin">
      <el-tag type="warning" size="small" effect="dark">群主</el-tag>
    </div>
    <div v-if="member.isTemplate" class="template-character-name">
      <div>{{member.aliasName}}</div>
    </div>
    <div v-if="member.isTemplate" class="avatar">
      <head-image :url="member.headImage" :name="member.aliasName"></head-image>
    </div>
	</div>
</template>

<script>
	import HeadImage from "../common/HeadImage.vue";
	export default{
		name: "templateGroupMember",
		components:{HeadImage},
		data(){
			return {};
		},
		props:{
			member:{
				type: Object,
				required: true
			},
		},
		methods:{

		}
	}
</script>

<style scoped lang="scss">
.template-group-member {
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

  .is-admin {
    text-align: center;
  }

  .template-character-name {
    margin-right: 15px;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    height: 100%;
    flex-shrink: 0;
    overflow: hidden;
    text-align: right;
  }
}
</style>
