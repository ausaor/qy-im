<template>
	<el-dialog title="是否加入通话?" :visible.sync="isShow"  width="400px">
	  <div class="rtc-group-join">
	  	<div class="host-info">
	  		<head-image :name="rtcInfo.host.aliasName" :url="rtcInfo.host.headImage" :size="80"></head-image>
			<div class="host-text">{{'发起人:'+rtcInfo.host.aliasName}}</div>
		</div>
	  	<div class="users-info">
	  		<div>{{rtcInfo.userInfos.length+'人正在通话中'}}</div>
	  		<div class="user-list">
	  			<div class="user-item" v-for="user in rtcInfo.userInfos" :key="user.id">
	  				<head-image :url="user.headImage" :name="user.aliasName" :size="40">
					</head-image>
	  			</div>
	  		</div>
	  	</div>
	  </div>
	  <span slot="footer" class="dialog-footer">
	    <el-button @click="onReject()">拒绝</el-button>
	    <el-button type="primary" @click="onOk()">接受</el-button>
	  </span>
	</el-dialog>
</template>

<script>
	import HeadImage from '@/components/common/HeadImage'
  import RtcGroupApi from "@/api/rtcGroupApi";
	
	export default{
		name: "rtcGroupJoin",
		components:{
			HeadImage
		},
		data() {
			return {
				isShow: false,
				rtcInfo: {
					host:{},
					userInfos:[],
          groupId: null,
				},
        API: new RtcGroupApi(), // API
			}
		},
		methods: {
			open(rtcInfo) {
				this.rtcInfo = rtcInfo;
				this.isShow = true;
			},
			onOk() {
				this.isShow = false;
				let userInfos = this.rtcInfo.userInfos;
				let mine = this.$store.state.userStore.userInfo;
				if(!userInfos.find((user)=>user.id==mine.id)){
					// 加入自己的信息
					userInfos.push({
						id: mine.id,
						aliasName: mine.nickName,
						headImage: mine.headImage,
						isCamera: true,
						isMicroPhone: true
					})
				}
        userInfos.push(this.rtcInfo.host);
				let rtcInfo = {
					isHost: false,
					groupId: this.rtcInfo.groupId,
					inviterId: this.rtcInfo.host?.id || mine.id,
					host: this.rtcInfo.host,
					userInfos: userInfos
				}
				// 通过 home.vue 打开多人视频窗口
				this.$eventBus.$emit("openGroupVideo", rtcInfo);
							
			},
			onReject(){
        this.API.reject(this.rtcInfo.groupId).then(() => {
          this.isShow = false;
        })
			}
		}
	}
</script>

<style lang="scss" scoped>
	.rtc-group-join {
		height: 260px;
		padding: 10px;
		.host-info {
			display: flex;
			flex-direction: column;
			font-size: 16px;
			padding: 10px;
			height: 100px;
			align-items: center;
			
			.host-text{
				margin-top: 5px;
			}
		}
		
		.users-info {
			font-size: 16px;
			margin-top: 20px;
			.user-list {
				display: flex;
				padding: 5px 5px;
				height: 90px;
				flex-wrap: wrap;
				justify-content: center;
				
				.user-item{
					padding: 2px;
				}
			}
		}
	}
</style>