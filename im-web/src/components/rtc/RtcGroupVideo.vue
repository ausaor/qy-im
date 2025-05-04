<template>
	<el-dialog v-dialogDrag top="5vh" title="语音通话" :close-on-click-modal="false" :close-on-press-escape="false"
			   :visible.sync="isShow" width="50%">
		<div class='rtc-group-video'>

		</div>
	</el-dialog>
</template>

<script>
	export default {
		name: "rtcGroupVideo",
		data() {
			return {
				isShow: false
			}
		},
		methods: {
			open() {
				this.isShow = true;
			},
			onRTCMessage(){
				//this.isShow = true;
			}
		}
	}
</script>

<style scoped lang="scss">
	.rtc-group-video {
		height: 300px;
		background-color: #E8F2FF;
	}
</style>