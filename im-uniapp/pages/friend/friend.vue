<template>

	<view class="tab-page friend">
		<nav-bar add search @add="onAddNewFriends" @search="showSearch = !showSearch">通讯录</nav-bar>
		<view class="nav-bar" v-if="showSearch">
			<view class="nav-search">
				<uni-search-bar v-model="searchText" radius="100" cancelButton="none"
					placeholder="点击搜索好友"></uni-search-bar>
			</view>
		</view>
    <view class="socializes">
      <view class="socializes-item" @click.stop="onAddNewFriends">
        <view class="item-icon">
          <svg-icon class="svg-icon" icon-class="xindepengyou"></svg-icon>
        </view>
        <view class="item-name">新的朋友</view>
      </view>
      <view class="socializes-item" @click.stop="toGroupPage">
        <view class="item-icon">
          <svg-icon class="svg-icon" icon-class="qunliao"></svg-icon>
        </view>
        <view class="item-name">群聊</view>
      </view>
    </view>
		<view class="friend-tip" v-if="friends.length == 0">
			温馨提示：您现在还没有任何好友，快点击右上方'+'按钮添加好友吧~
		</view>
		<view class="friend-items" v-else>
			<up-index-list :index-list="friendIdx">
				<template v-for="(friends, i) in friendGroups">
					<up-index-item>
						<up-index-anchor :text="friendIdx[i] == '*' ? '在线' : friendIdx[i]"></up-index-anchor>
						<view v-for="(friend, idx) in friends" :key="idx">
							<friend-item :friend="friend"></friend-item>
						</view>
					</up-index-item>
				</template>
			</up-index-list>
		</view>
	</view>
</template>

<script>
import { pinyin } from 'pinyin-pro';
import SvgIcon from "../../components/svg-icon/svg-icon.vue";

export default {
  components: {SvgIcon},
	data() {
		return {
			showSearch: false,
			searchText: ''
		}
	},
	methods: {
		onAddNewFriends() {
			uni.navigateTo({
				url: "/pages/friend/friend-add"
			})
		},
		firstLetter(strText) {
			// 使用pinyin-pro库将中文转换为拼音
			let pinyinOptions = {
				toneType: 'none', // 无声调
				type: 'normal' // 普通拼音
			};
			let pyText = pinyin(strText, pinyinOptions);
			return pyText[0];
		},
		isEnglish(character) {
			return /^[A-Za-z]+$/.test(character);
		},
    toGroupPage() {
			uni.navigateTo({
				url: "/pages/group/group"
			})
		},
	},
	computed: {
		friends() {
			return this.friendStore.friends;
		},
		friendGroupMap() {
			// 按首字母分组
			let groupMap = new Map();
			this.friends.forEach((f) => {
				if (this.searchText && !f.nickName.includes(this.searchText)) {
					return;
				}
				let letter = this.firstLetter(f.nickName).toUpperCase();
				// 非英文一律为#组
				if (!this.isEnglish(letter)) {
					letter = "#"
				}
				if (f.online) {
					letter = '*'
				}
				if (groupMap.has(letter)) {
					groupMap.get(letter).push(f);
				} else {
					groupMap.set(letter, [f]);
				}
			})
			// 排序
			let arrayObj = Array.from(groupMap);
			arrayObj.sort((a, b) => {
				// #组在最后面
				if (a[0] == '#' || b[0] == '#') {
					return b[0].localeCompare(a[0])
				}
				return a[0].localeCompare(b[0])
			})
			groupMap = new Map(arrayObj.map(i => [i[0], i[1]]));
			return groupMap;
		},
		friendIdx() {
			return Array.from(this.friendGroupMap.keys());
		},
		friendGroups() {
			return Array.from(this.friendGroupMap.values());
		}
	}
}
</script>

<style lang="scss" scoped>
.friend {
	position: relative;
	display: flex;
	flex-direction: column;

	:deep(.u-index-anchor) {
		height: 60rpx !important;
		background-color: unset !important;
		border-bottom: none !important;
	}

	:deep(.u-index-anchor__text) {
		color: $im-text-color !important;
	}

	:deep(.u-index-list__letter__item) {
		width: 48rpx !important;
		height: 48rpx !important;
	}

	:deep(.u-index-list__letter__item__index) {
		font-size: $im-font-size-small !important;
	}

  .socializes {
    background-color: #ffffff;
    padding: 10rpx 30rpx;

    .socializes-item {
      display: flex;
      align-items: center;
      padding: 10rpx 0;

      .item-icon {
        width: 2.4rem;
        height: 2.4rem;

        .svg-icon {
          width: 2.4rem;
          height: 2.4rem;
        }
      }

      .item-name {
        width: 100%;
        height: 2.4rem;
        line-height: 2.4rem;
        margin-left: 20rpx;
        color: $im-text-color;
      }
    }
  }

	.friend-tip {
		position: absolute;
		top: 400rpx;
		padding: 50rpx;
		text-align: center;
		line-height: 50rpx;
		color: $im-text-color-lighter;
	}

	.friend-items {
    flex: 1;
		padding: 0;
		overflow: scroll;
		position: relative;

		.scroll-bar {
			height: 100%;
		}
	}
}
</style>