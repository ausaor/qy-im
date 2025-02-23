<template>
	<view class="group-item" @click="showGroupInfo()">
		<head-image :name="group.remark" :url="group.headImage" size="small"></head-image>
		<view class="group-name">
      <view>
        <uni-tag v-if="group.groupType===1" :circle="true" text="模板群聊" type="primary" size="mini" custom-style="background-color: rgb(0,47,167);border-color: rgb(0,47,167);" />
        <uni-tag v-if="group.groupType===2" :circle="true" text="多元角色群聊" type="primary" size="mini" custom-style="background-color: rgb(105,149,114);border-color: rgb(105,149,114);" />
        <uni-tag v-if="group.groupType===3" :circle="true" text="角色群聊" type="primary" size="mini" custom-style="background-color: rgb(144,0,33);border-color: rgb(144,0,33);" />
        <uni-tag v-if="group.groupType===4" :circle="true" text="模板角色群聊" type="primary" size="mini" custom-style="background-color: rgb(176,89,35);border-color: rgb(176,89,35);" />
      </view>
			<view>{{ group.remark }}</view>
		</view>
	</view>
</template>

<script>
export default {
	name: "group-item",
	data() {
		return {}
	},
	methods: {
		showGroupInfo() {
			uni.navigateTo({
				url: "/pages/group/group-info?id=" + this.group.id
			})
		},
	},
	props: {
		group: {
			type: Object
		}
	}
}
</script>

<style scope lang="scss">
.group-item {
	height: 90rpx;
	display: flex;
	margin-bottom: 2rpx;
	position: relative;
	padding: 18rpx 20rpx;
	align-items: center;
	background-color: white;
	white-space: nowrap;

	&:hover {
		background-color: $im-bg-active;
	}

	&.active {
		background-color: $im-bg-active;
	}

	.group-name {
		font-size: $im-font-size;
		padding-left: 20rpx;
		text-align: left;
		white-space: nowrap;
		overflow: hidden;
	}
}
</style>
