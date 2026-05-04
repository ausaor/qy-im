<template>
	<view class="emoji-selector-container">
		<!-- 顶部导航栏 -->
		<scroll-view class="emoji-nav-bar" scroll-x="true" show-scrollbar="false">
			<view class="nav-tabs-wrapper">
				<view 
					v-for="tab in emoTabs" 
					:key="tab.id" 
					class="nav-tab-item"
					:class="{ active: activeTabId === tab.id }"
					@click="onTabClick(tab)"
				>
					<view class="tab-content">
						<uni-icons v-if="tab.id === 'emo' || tab.id === 'original'" custom-prefix="iconfont" :type="tab.icon" size="28" class="tab-icon" :class="tab.icon" :color="tab.color ? tab.color : 'black'"></uni-icons>
            <uni-icons v-if="tab.id === 'search' || tab.id === 'favorite'" :type="tab.icon" size="28" class="tab-icon" :color="tab.color ? tab.color : 'black'"></uni-icons>
            <image v-if="tab.id.startsWith('album_')" class="album-emo-item" :src="tab.icon"></image>
					</view>
				</view>
				<!-- 右侧占位元素，确保滚动到最右边时有右间距 -->
				<view class="nav-tab-spacer"></view>
			</view>
		</scroll-view>

		<!-- 表情内容区域 -->
		<scroll-view class="emoji-content-area" scroll-y="true">
			<!-- 搜索表情列表 -->
			<view v-if="activeTabId === 'search'" class="search-container">
				<!-- 搜索框 -->
				<view class="search-box-wrapper">
					<input 
						class="search-input" 
						type="text" 
						placeholder="搜索表情" 
						v-model="searchText"
						@confirm="onSearch"
						@input="onSearchInput"
					/>
					<view class="search-btn" @click="onSearch">
						<text class="iconfont icon-search"></text>
					</view>
				</view>
				
				<!-- 搜索结果列表 -->
				<view class="emoji-grid search-grid">
					<view 
						v-for="(item, index) in searchResults" 
						:key="index"
						class="emoji-item search-item"
						@click="onSelectEmoji(item)"
					>
						<image class="emoji-image search-image" :src="item.thumbUrl || item.imageUrl" mode="aspectFit" />
					</view>
					<view v-if="searchResults.length === 0 && searchText" class="empty-tip">
						<text>未找到相关表情</text>
					</view>
				</view>
			</view>

			<!-- emo表情列表 -->
			<view v-else-if="activeTabId === 'emo'" class="emoji-grid">
				<view 
					v-for="(emoText, index) in emoTextList" 
					:key="index"
					class="emoji-item"
					@click="onSelectEmoText(emoText, 'emo')"
				>
					<image class="emoji-image" :src="$emo.textToPath(emoText)" mode="aspectFit" />
				</view>
			</view>

			<!-- 经典表情列表 -->
			<view v-else-if="activeTabId === 'original'" class="emoji-grid">
				<view 
					v-for="(emoText, index) in originalEmoTextList" 
					:key="index"
					class="emoji-item"
					@click="onSelectEmoText(emoText, 'original')"
				>
					<image class="emoji-image" :src="$emo.textToPathOriginal(emoText)" mode="aspectFit" />
				</view>
			</view>

			<!-- 收藏表情列表 -->
			<view v-else-if="activeTabId === 'favorite'" class="emoji-grid favorite-grid">
				<view 
					v-for="(item, index) in favoriteList" 
					:key="index"
					class="emoji-item favorite-item"
					@click="onSelectEmoji(item)"
					@longpress="onLongPressFavorite(item)"
				>
					<image class="emoji-image favorite-image" :src="item.thumbUrl || item.imageUrl" mode="aspectFit" />
				</view>
				<view v-if="favoriteList.length === 0" class="empty-tip">
					<text>暂无收藏表情</text>
				</view>
			</view>

			<!-- 相册表情列表 -->
			<view v-else-if="activeTabId.startsWith('album_')" class="emoji-grid album-grid">
				<view 
					v-for="(item, index) in currentAlbumImgs" 
					:key="index"
					class="emoji-item album-item"
					@click="onSelectEmoji(item)"
				>
					<image class="emoji-image album-image" :src="item.thumbUrl || item.imageUrl" mode="aspectFit" />
					<text class="emoji-name">{{ item.name }}</text>
				</view>
				<view v-if="currentAlbumImgs.length === 0" class="empty-tip">
					<text>该相册暂无表情</text>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import emo from '@/common/emotion.js';
import UniIcons from "../../uni_modules/uni-icons/components/uni-icons/uni-icons.vue";

export default {
	name: "album-img-list",
  components: {UniIcons},
	data() {
		return {
			emoTextList: emo.emoTextList,
			originalEmoTextList: emo.originalEmoTextList,
			activeTabId: 'emo',
			searchText: '',
			searchResults: [],
			favoriteList: [],
			currentAlbumImgs: [],
			searchTimer: null
		};
	},
	computed: {
		emoTabs() {
			// 合并固定标签和相册标签
			return this.uiStore.emoSelector.tabs;
		}
	},
	methods: {
    // 标签点击事件
		onTabClick(tab) {
			this.activeTabId = tab.id;
			this.uiStore.setActiveTab(tab.id);
			
			// 根据不同标签加载对应数据
			if (tab.id === 'favorite') {
				this.loadFavoriteList();
			} else if (tab.id.startsWith('album_')) {
				const albumId = tab.albumId || parseInt(tab.id.replace('album_', ''));
				this.loadAlbumImgs(albumId);
			} else if (tab.id === 'search') {
				// 搜索标签，聚焦输入框
				this.$nextTick(() => {
					uni.createSelectorQuery().in(this).select('.search-input').node().exec((res) => {
						if (res[0]) {
							res[0].node.focus();
						}
					});
				});
			}
		},
		
		// 搜索输入
		onSearchInput(e) {
			// 清除之前的定时器
			if (this.searchTimer) {
				clearTimeout(this.searchTimer);
			}
			
			// 设置防抖
			this.searchTimer = setTimeout(() => {
				if (this.searchText.trim()) {
					this.onSearch();
				} else {
					this.searchResults = [];
				}
			}, 500);
		},
		
		// 搜索表情
		onSearch() {
			if (!this.searchText.trim()) {
				return;
			}
			
			uni.showLoading({ title: '搜索中...' });
			this.uiStore.searchEmoImgs(this.searchText.trim())
				.then(results => {
					this.searchResults = results;
				})
				.catch(err => {
					uni.showToast({
						title: '搜索失败',
						icon: 'none'
					});
				})
				.finally(() => {
					uni.hideLoading();
				});
		},
		
		// 加载收藏列表
		loadFavoriteList() {
      uni.showLoading({ title: '加载中...' });
      this.uiStore.loadEmoFavoriteList()
          .then(list => {
            this.favoriteList = list;
          })
          .catch(err => {
            uni.showToast({
              title: '加载失败',
              icon: 'none'
            });
          })
          .finally(() => {
            uni.hideLoading();
          });
		},
		
		// 加载相册图片
		loadAlbumImgs(albumId) {
      console.log('loadAlbumImgs', albumId);
			const cacheKey = `album_${albumId}`;
      console.log('this.uiStore.emoAlbumImgMap', this.uiStore.emoAlbumImgMap);
			if (this.uiStore.emoAlbumImgMap.has(cacheKey)) {
				this.currentAlbumImgs = this.uiStore.emoAlbumImgMap.get(cacheKey);
				return;
			}
			
			uni.showLoading({ title: '加载中...' });
			this.uiStore.loadEmoAlbumImgs(albumId)
				.then(imgs => {
					this.currentAlbumImgs = imgs;
				})
				.catch(err => {
					uni.showToast({
						title: '加载失败',
						icon: 'none'
					});
				})
				.finally(() => {
					uni.hideLoading();
				});
		},
		
		// 选择emo文本表情
		onSelectEmoText(emoText, type) {
			this.$emit('select', {
				type: type,
				emoText: emoText
			});
		},
		
		// 选择表情图片
		onSelectEmoji(item) {
			this.$emit('select', {
				type: 'image',
				...item
			});
		},
		
		// 长按收藏表情
		onLongPressFavorite(item) {
			uni.showActionSheet({
				itemList: ['删除收藏'],
				success: (res) => {
					if (res.tapIndex === 0) {
						this.deleteFavorite(item);
					}
				}
			});
		},
		
		// 删除收藏
		deleteFavorite(item) {
			uni.showModal({
				title: '确认删除',
				content: '确定要删除该收藏表情吗？',
				success: (res) => {
					if (res.confirm) {
						this.$http({
							url: '/emoFavorite/delete',
							method: 'POST',
							data: { id: item.id }
						}).then(() => {
							// 从列表中移除
							this.favoriteList = this.favoriteList.filter(f => f.id !== item.id);
							uni.showToast({
								title: '删除成功',
								icon: 'success'
							});
						}).catch(err => {
							uni.showToast({
								title: '删除失败',
								icon: 'none'
							});
						});
					}
				}
			});
		}
	}
}
</script>

<style scoped lang="scss">
.emoji-selector-container {
	display: flex;
	flex-direction: column;
	height: 100%;
	background-color: #ffffff;
}

.emoji-nav-bar {
  width: 100%;
	flex-shrink: 0;
	border-bottom: 1rpx solid #f0f0f0;
	background-color: #fafafa;
	
	.nav-tabs-wrapper {
		display: flex;
		width: auto;
		min-width: 100%;
		padding: 10rpx 20rpx;
		box-sizing: border-box;
		
		.nav-tab-item {
      display: inline-flex;
      flex-shrink: 0;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      width: 80rpx !important;
      min-width: 80rpx;
      height: 80rpx;
      margin: 0 10rpx;
      color: #000;
      transition: all .2s ease;
			
			&.active {
				background-color: #ececfb;;
			}
			
			.tab-content {
				display: flex;
				align-items: center;
				justify-content: center;
				
				.album-emo-item {
					width: 48rpx;
					height: 48rpx;
          border-radius: 50%;
				}
			}
		}
		
		/* 右侧占位元素，确保滚动到最右边时有右间距 */
		.nav-tab-spacer {
			flex-shrink: 0;
			width: 10rpx;
			height: 1px;
		}
	}
}

.search-box-wrapper {
	display: flex;
	align-items: center;
	padding: 20rpx;
	background-color: #fafafa;
	border-bottom: 1rpx solid #f0f0f0;
	
	.search-input {
		flex: 1;
		height: 64rpx;
		padding: 0 24rpx;
		font-size: 28rpx;
		background-color: #ffffff;
		border-radius: 32rpx;
		border: 1rpx solid #e0e0e0;
	}
	
	.search-btn {
		margin-left: 16rpx;
		width: 64rpx;
		height: 64rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: #007aff;
		border-radius: 32rpx;
		
		.iconfont {
			font-size: 32rpx;
			color: #ffffff;
		}
	}
}

.emoji-content-area {
	flex: 1;
	height: 0;
	background-color: #ffffff;
}

.emoji-grid {
	display: grid;
	grid-template-columns: repeat(6, 1fr);
	gap: 16rpx;
	padding: 20rpx;
	
	.emoji-item {
		display: flex;
		align-items: center;
		justify-content: center;
		aspect-ratio: 1;
		border-radius: 12rpx;
		background-color: #f8f8f8;
		transition: all 0.2s ease;
		
		&:active {
			transform: scale(0.95);
			background-color: #e8e8e8;
		}
		
		.emoji-image {
			width: 64rpx;
			height: 64rpx;
		}
	}
}

.album-grid {
	grid-template-columns: repeat(5, 1fr) !important;
	gap: 12rpx !important;
	padding: 16rpx !important;
	
	.album-item {
		flex-direction: column;
		align-items: center;
		padding: 16rpx 8rpx;
		height: auto;
		aspect-ratio: auto;
		
		.album-image {
			width: 100rpx;
			height: 100rpx;
			margin-bottom: 8rpx;
		}
		
		.emoji-name {
			font-size: 22rpx;
			color: #666666;
			text-align: center;
			line-height: 1.4;
			overflow: hidden;
			text-overflow: ellipsis;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			word-break: break-all;
			width: 100%;
		}
	}
}

.favorite-grid {
	grid-template-columns: repeat(5, 1fr) !important;
	gap: 12rpx !important;
	padding: 16rpx !important;
	
	.favorite-item {
		flex-direction: column;
		align-items: center;
		padding: 16rpx 8rpx;
		height: auto;
		aspect-ratio: auto;
		
		.favorite-image {
			width: 100rpx;
			height: 100rpx;
		}
	}
}

.search-grid {
	grid-template-columns: repeat(5, 1fr) !important;
	gap: 12rpx !important;
	padding: 16rpx !important;
	
	.search-item {
		flex-direction: column;
		align-items: center;
		padding: 16rpx 8rpx;
		height: auto;
		aspect-ratio: auto;
		
		.search-image {
			width: 100rpx;
			height: 100rpx;
		}
	}
}

.empty-tip {
	grid-column: 1 / -1;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 100rpx 0;
	
	text {
		font-size: 28rpx;
		color: #999999;
	}
}
</style>
