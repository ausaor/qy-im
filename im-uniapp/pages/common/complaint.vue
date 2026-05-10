<template>
	<view class="page">
		<!-- 导航栏 -->
		<nav-bar back>投诉</nav-bar>
		
		<view class="container">
			<!-- 投诉对象 -->
			<view class="form-item">
				<view class="label">投诉对象</view>
				<view class="value">{{ formData.targetName }}</view>
			</view>
			
			<!-- 投诉原因 -->
			<view class="form-item" @click="showReasonPicker">
				<view class="label">投诉原因</view>
				<view class="value picker">
					<text :class="{ placeholder: !formData.reason }">{{ formData.reason || '请选择' }}</text>
					<uni-icons type="arrowright" size="16" color="#c0c4cc"></uni-icons>
				</view>
			</view>
			
			<!-- 图片证据 -->
			<view class="form-item">
				<view class="label-header">
					<view class="label">图片证据(选填)</view>
					<view class="count">{{ evidenceImgList.length }}/9</view>
				</view>
				<view class="image-list">
					<view class="image-item" v-for="(img, index) in evidenceImgList" :key="index">
						<image :src="img" mode="aspectFill" class="image"></image>
						<view class="delete-btn" @click.stop="deleteImage(index)">
							<uni-icons type="closeempty" size="16" color="#fff"></uni-icons>
						</view>
					</view>
					<image-upload 
						v-if="evidenceImgList.length < 9"
						:maxCount="1" 
						:onSuccess="onImageSuccess"
						:onError="onImageError"
					>
						<view class="upload-btn">
							<uni-icons type="plus" size="40" color="#c0c4cc"></uni-icons>
						</view>
					</image-upload>
				</view>
			</view>
			
			<!-- 投诉内容 -->
			<view class="form-item">
				<textarea 
					class="content-input" 
					v-model="formData.content" 
					placeholder="请填写投诉内容" 
					maxlength="500"
					:show-confirm-bar="false"
				></textarea>
				<view class="char-count">{{ formData.content.length }}/500</view>
			</view>
			
			<!-- 提交按钮 -->
			<button class="submit-btn" type="primary" @click="submitComplaint" :disabled="submitting">
				{{ submitting ? '提交中...' : '提交' }}
			</button>
		</view>
	</view>
</template>

<script>
import NavBar from '@/components/nav-bar/nav-bar.vue'
import ImageUpload from '@/components/image-upload/image-upload.vue'

export default {
	components: {
		NavBar,
		ImageUpload
	},
	data() {
		return {
			submitting: false,
			formData: {
				targetId: null,
				targetName: '',
				targetType: '',
				reason: '',
				content: '',
				evidenceImgList: []
			},
			evidenceImgList: [],
			reasonOptions: [
				'对我造成骚扰',
				'疑似诈骗',
				'传播不良内容',
				'其他不正规行为'
			]
		}
	},

	onLoad(options) {
		this.formData.targetId = options.targetId;
		this.formData.targetName = options.targetName;
		this.formData.targetType = options.targetType;
	},

	methods: {
		// 显示投诉原因选择器
		showReasonPicker() {
			uni.showActionSheet({
				itemList: this.reasonOptions,
				success: (res) => {
					this.formData.reason = this.reasonOptions[res.tapIndex];
				}
			});
		},
		
		// 图片上传成功
		onImageSuccess(file, data) {
			if (data.code == 200) {
				this.evidenceImgList.push(data.data.originUrl);
				uni.showToast({
					title: '上传成功',
					icon: 'success'
				});
			}
		},
		
		// 图片上传失败
		onImageError(file, err) {
			uni.showToast({
				title: '上传失败',
				icon: 'none'
			});
		},
		
		// 删除图片
		deleteImage(index) {
			uni.showModal({
				title: '提示',
				content: '是否删除该图片？',
				success: (res) => {
					if (res.confirm) {
						this.evidenceImgList.splice(index, 1);
					}
				}
			});
		},
		
		// 提交投诉
		submitComplaint() {
			// 验证表单
			if (!this.formData.reason) {
				uni.showToast({
					title: '请选择投诉原因',
					icon: 'none'
				});
				return;
			}
			
			if (!this.formData.content || this.formData.content.trim() === '') {
				uni.showToast({
					title: '请填写投诉内容',
					icon: 'none'
				});
				return;
			}
			
			if (this.formData.content.length < 10) {
				uni.showToast({
					title: '投诉内容至少10个字符',
					icon: 'none'
				});
				return;
			}
			
			this.submitting = true;
			
			const submitData = {
				targetId: this.formData.targetId,
				targetName: this.formData.targetName,
				targetType: this.formData.targetType,
				reason: this.formData.reason,
				content: this.formData.content,
				evidenceImgList: this.evidenceImgList
			};
			
			this.$http({
				url: '/complaint/submit',
				method: 'post',
				data: submitData
			}).then(() => {
				uni.showToast({
					title: '投诉成功',
					icon: 'success'
				});
				
				setTimeout(() => {
					uni.navigateBack();
				}, 1500);
			}).catch(() => {
				this.submitting = false;
			}).finally(() => {
				this.submitting = false;
			});
		}
	}
}
</script>

<style scoped lang="scss">
.page {
	min-height: 100vh;
	background-color: #f7f7f7;
}

.container {
	padding: 20rpx;
}

.form-item {
	background-color: #fff;
	border-radius: 12rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	
	.label {
		font-size: 30rpx;
		color: #333;
		font-weight: 500;
		margin-bottom: 20rpx;
	}
	
	.label-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
		
		.label {
			margin-bottom: 0;
		}
		
		.count {
			font-size: 26rpx;
			color: #999;
		}
	}
	
	.value {
		font-size: 28rpx;
		color: #666;
		display: flex;
		align-items: center;
		justify-content: space-between;
		
		&.picker {
			color: #999;
			
			.placeholder {
				color: #c0c4cc;
			}
		}
	}
	
	.image-list {
		display: flex;
		flex-wrap: wrap;
		gap: 20rpx;
		
		.image-item {
			position: relative;
			width: 200rpx;
			height: 200rpx;
			border-radius: 8rpx;
			overflow: hidden;
			
			.image {
				width: 100%;
				height: 100%;
			}
			
			.delete-btn {
				position: absolute;
				top: 0;
				right: 0;
				width: 40rpx;
				height: 40rpx;
				background-color: rgba(0, 0, 0, 0.5);
				display: flex;
				align-items: center;
				justify-content: center;
				border-radius: 0 8rpx 0 8rpx;
			}
		}
		
		.upload-btn {
			width: 200rpx;
			height: 200rpx;
			border-radius: 8rpx;
			border: 2rpx dashed #c0c4cc;
			display: flex;
			align-items: center;
			justify-content: center;
			background-color: #fafafa;
		}
	}
	
	.content-input {
		width: 100%;
		min-height: 200rpx;
		padding: 20rpx;
		border: 2rpx solid #e5e5e5;
		border-radius: 8rpx;
		font-size: 28rpx;
		box-sizing: border-box;
		background-color: #fafafa;
	}
	
	.char-count {
		text-align: right;
		font-size: 24rpx;
		color: #999;
		margin-top: 10rpx;
	}
}

.submit-btn {
	margin-top: 40rpx;
	border-radius: 50rpx;
	font-size: 32rpx;
	font-weight: 500;
}
</style>