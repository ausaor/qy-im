<template>
	<el-dialog class="setting" title="设置" :visible.sync="visible"  width="500px" :before-close="onClose">
		<el-form :model="userInfo" label-width="80px" :rules="rules" ref="settingForm">
			<el-form-item label="头像">
				<file-upload  class="avatar-uploader"
				:action="imageAction" 
				:showLoading="true"
				:maxSize="maxSize"  
				@success="onUploadSuccess"
				:fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']">
					<img v-if="userInfo.headImage" :src="userInfo.headImage" class="avatar">
					<i v-else class="el-icon-plus avatar-uploader-icon"></i>
				</file-upload>
			</el-form-item>
			<el-form-item label="用户名">
				<el-input disabled v-model="userInfo.userName" autocomplete="off"></el-input>
			</el-form-item>
			<el-form-item prop="nickName" label="昵称">
				<el-input v-model="userInfo.nickName" autocomplete="off"></el-input>
			</el-form-item>
			<el-form-item label="性别">
				<el-radio-group v-model="userInfo.sex">
					<el-radio :label="0">男</el-radio>
					<el-radio :label="1">女</el-radio>
				</el-radio-group>
			</el-form-item>
			<el-form-item label="个性签名">
				<el-input type="textarea" v-model="userInfo.signature"></el-input>
			</el-form-item>
		</el-form>

		<span slot="footer" class="dialog-footer">
			<el-button @click="onClose()">取 消</el-button>
			<el-button type="primary" @click="onSubmit()">确 定</el-button>
		</span>
	</el-dialog>
</template>

<script>
	import FileUpload from "../common/FileUpload.vue";
	
	export default {
		name: "setting",
		components: {
			FileUpload
		},
		data() {
			return {
				userInfo: {

				},
				maxSize: 5*1024*1024,
				action: "/image/upload",
				rules: {
					nickName: [{
						required: true,
						message: '请输入昵称',
						trigger: 'blur'
					}]
				}
			}
		},
		methods: {

      onClose() {
				this.$emit("close");
			},
      onSubmit() {
				this.$refs['settingForm'].validate((valid) => {
					if (!valid) {
						return false;
					}
					this.$http({
						url: "/user/update",
						method: "put",
						data: this.userInfo
					}).then(()=>{
						this.$store.commit("setUserInfo",this.userInfo);
						this.$emit("close");
						this.$message.success("修改成功");
					})	
				});
			},
      onUploadSuccess(data, file) {
				this.userInfo.headImage = data.originUrl;
				this.userInfo.headImageThumb = data.thumbUrl;
			}
		},
		props: {
			visible: {
				type: Boolean
			}
		},
		computed:{
			imageAction(){
				return `/image/upload`;
			}
		},
		watch: {
			visible: function(newData, oldData) {
				// 深拷贝
				let mine = this.$store.state.userStore.userInfo;
				this.userInfo = JSON.parse(JSON.stringify(mine));
			}
		}
	}
</script>

<style scoped lang="scss" >
	.setting {
		.avatar-uploader {
			
			.el-upload {
				border: 1px dashed #d9d9d9 !important;
				border-radius: 6px;
				cursor: pointer;
				position: relative;
				overflow: hidden;
			}

			.el-upload:hover {
				border-color: #409EFF;
			}

			.avatar-uploader-icon {
				font-size: 28px;
				color: #8c939d;
				width: 178px;
				height: 178px;
				line-height: 178px;
				text-align: center;
			}

			.avatar {
				width: 178px;
				height: 178px;
				display: block;
			}
		}
	}
</style>
