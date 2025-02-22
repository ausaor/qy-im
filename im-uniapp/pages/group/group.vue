<template>
	<view class="tab-page group">
		<nav-bar add search @add="onCreateNewGroup" @search="showSearch = !showSearch">群聊</nav-bar>
		<view class="nav-bar" v-if="showSearch">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" cancelButton="none" radius="100"
					placeholder="点击搜索群聊"></uni-search-bar>
			</view>
		</view>
		<view class="group-tip" v-if="groupStore.groups.length == 0">
			温馨提示：您现在还没有加入任何群聊，点击右上方'+'按钮可以创建群聊哦~
		</view>
		<view class="group-items" v-else>
      <view v-show="groupOptionShow" class="group-option">
        <view class="group-item" @click="createGroup(0)">普通群聊</view>
        <view class="group-item" @click="createGroup(1)">模板群聊</view>
        <view class="group-item" @click="createGroup(4)">模板角色群聊</view>
        <view class="group-item" @click="createGroup(2)">多元角色群聊</view>
        <view class="group-item" @click="createGroup(3)">角色群聊</view>
      </view>
			<scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
				<view v-for="group in groupStore.groups" :key="group.id">
					<group-item v-if="!group.quit && group.remark.includes(searchText)"
						:group="group"></group-item>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			showSearch: false,
			searchText: "",
      groupOptionShow: false,
		}
	},
	methods: {
		onFocusSearch() {

		},
		onCreateNewGroup() {
      this.groupOptionShow = !this.groupOptionShow;
		},
    createGroup(groupType) {
      this.groupOptionShow = false
      uni.navigateTo({
				url: "/pages/group/group-edit?groupType=" + groupType,
			})
    }
	}
}
</script>

<style lang="scss" scoped>
.group {
	position: relative;
	display: flex;
	flex-direction: column;

	.group-tip {
		position: absolute;
		top: 400rpx;
		padding: 50rpx;
		text-align: left;
		line-height: 50rpx;
		color: darkblue;
		font-size: 30rpx;
	}

	.group-items {
		flex: 1;
		padding: 0;
		overflow: hidden;
		position: relative;

		.scroll-bar {
			height: 100%;
		}

    .group-option {
      Z-index: 9;
      position: absolute;
      top: 3rpx;
      right: 2rpx;
      border: 1rpx solid #8f939c;
      border-radius: 5rpx;
      background: #daf8ed;

      .group-item {
        padding-top: 3rpx;
        padding-left: 5rpx;
        padding-right: 5rpx;
      }
    }
	}
}
</style>