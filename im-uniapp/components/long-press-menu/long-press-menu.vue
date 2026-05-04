<template>
	<view>
		<view @longpress.stop="onLongPress($event)" @touchstart="onTouchStart" @touchmove.stop="onTouchMove" @touchend="onTouchEnd" @contextmenu.prevent>
			<slot></slot>
		</view>
		<view v-if="isShowMenu" class="menu-mask" @touchstart="onClose()" @contextmenu.prevent=""></view>
		<view v-if="isShowMenu" class="menu" :style="menuStyle">
			<view class="menu-item" v-for="(item) in items" :key="item.key" @click.prevent="onSelectMenu(item)">
				<text :style="itemStyle(item)"> {{ item.name }}</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: "long-press-menu",
	data() {
		return {
			isShowMenu: false,
			isTouchMove: false,
			style: "",
      menuStyle: "",
			touchStartX: 0,
			touchStartY: 0,
		}
	},
	props: {
		items: {
			type: Array
		}
	},
	methods: {
		onLongPress(e) {
			console.log('长按事件触发', e);
			if (this.isTouchMove) {
				// 屏幕移动时不弹出
				console.log('检测到触摸移动，不显示菜单');
				return;
			}
			// 阻止默认行为，防止浏览器上下文菜单
			e.preventDefault && e.preventDefault();
			uni.getSystemInfo({
				success: (res) => {
					let touches = e.touches ? e.touches[0] : e.changedTouches[0];
					if (!touches) {
						console.error('无法获取触摸点信息');
						return;
					}
					let style = "";
					/* 因 非H5端不兼容 style 属性绑定 Object ，所以拼接字符 */
					if (touches.clientY > (res.windowHeight / 2)) {
						style = `bottom:${res.windowHeight - touches.clientY}px;`;
					} else {
						style = `top:${touches.clientY}px;`;
					}
					if (touches.clientX > (res.windowWidth / 2)) {
						style += `right:${res.windowWidth - touches.clientX}px;`;
					} else {
						style += `left:${touches.clientX}px;`;
					}
					this.menuStyle = style;
					console.log('菜单样式:', style);
					//
					this.$nextTick(() => {
						this.isShowMenu = true;
						console.log('菜单已显示');
					});
				}
			})
		},
		onTouchStart(e) {
			// 记录触摸起始位置
			if (e.touches && e.touches.length > 0) {
				const touch = e.touches[0];
				this.touchStartX = touch.pageX;
				this.touchStartY = touch.pageY;
				this.isTouchMove = false;
				console.log('触摸开始', this.touchStartX, this.touchStartY);
			}
		},
		onTouchMove(e) {
			// 检测是否有明显的移动
			if (e.touches && e.touches.length > 0) {
				const touch = e.touches[0];
				const moveX = Math.abs(touch.pageX - this.touchStartX);
				const moveY = Math.abs(touch.pageY - this.touchStartY);
				// 如果移动距离超过5px，认为是移动而非长按
				if (moveX > 5 || moveY > 5) {
					this.onClose();
					this.isTouchMove = true;
					console.log('检测到明显移动，关闭菜单');
				}
			}
		},
		onTouchEnd() {
			this.isTouchMove = false;
			console.log('触摸结束');
		},
		onSelectMenu(item) {
			this.$emit("select", item);
			this.isShowMenu = false;
		},
		onClose() {
			this.isShowMenu = false;
		},
		itemStyle(item) {
			if (item.color) {
				return `color:${item.color};`
			}
			return `color:#000;`;
		}
	}
}
</script>

<style lang="scss" scoped>
.menu-mask {
	position: fixed;
	left: 0;
	top: 0;
	right: 0;
	bottom: 0;
	width: 100%;
	height: 100%;
	z-index: 999;
}

.menu {
	position: fixed;
	border-radius: 4px;
	overflow: hidden;
	background-color: #fff;
	z-index: 1000;
	box-shadow: $im-box-shadow-dark;
	min-width: 120px;

	.menu-item {
		height: 28px;
		min-width: 120rpx;
		line-height: 28px;
		font-size: $im-font-size-small;
		display: flex;
		padding: 6px 20px;
		justify-content: flex-start;
		cursor: pointer;
		user-select: none;
		-webkit-user-select: none;

		&:active {
			background: $im-bg-active;
		}

		.menu-icon {
			margin-right: 10rpx;
		}
	}
}
</style>