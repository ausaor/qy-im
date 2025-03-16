<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-left cursor-pointer" @click="handleBack">
        <uni-icons type="left" size="20" color="#333"/>
      </view>
      <view class="nav-title">发布动态</view>
      <view :class="['nav-right', 'cursor-pointer', { 'nav-right-active': canPublish }]" @click="handlePublish">发布</view>
    </view>

    <!-- 主体内容区 -->
    <view class="content">
      <!-- 用户信息 -->
      <view class="user-info">
        <head-image class="avatar" :name="form.nickName" :url="form.avatar" :size="80"></head-image>
        <text class="nickname">{{form.nickName}}</text>
      </view>

      <!-- 文本输入区 -->
      <textarea
          v-model="form.content"
          class="input-area"
          placeholder="想记录点什么"
          placeholder-class="placeholder"
          maxlength="2000"
      />

      <!-- 功能按钮区 -->
      <view class="function-bar">
        <view class="function-item cursor-pointer" @click="chooseMedia('image')">
          <uni-icons type="image" size="24" :color="imageEnabled ? '#07C160' : '#cccccc'"/>
        </view>
        <view class="function-item cursor-pointer" @click="chooseMedia('video')">
          <uni-icons type="videocam" size="24" :color="videoEnabled ? '#07C160' : '#cccccc'"/>
        </view>
        <view class="function-item cursor-pointer" @click="handleLocation">
          <uni-icons type="location" size="24" color="#666"/>
        </view>
      </view>

      <!-- 媒体展示区 -->
      <view class="media-grid" v-if="fileList.length > 0">
        <view
            v-for="(item, index) in fileList"
            :key="index"
            class="media-item"
        >
          <image
              v-if="item.fileType === 1"
              :src="item.url"
              mode="aspectFill"
              class="media-content"
              @click="previewMedia(index)"
          />
          <video
              v-if="item.fileType === 2"
              :poster="item.coverUrl"
              :src="item.url"
              class="media-content"
              @click="previewMedia(index)"
          />
          <view class="delete-btn cursor-pointer" @click="deleteMedia(index)">
            <uni-icons type="closeempty" size="20" color="#fff"/>
          </view>
        </view>

        <!-- 上传按钮 -->
        <view class="upload-btn cursor-pointer" v-if="showUploadBtn" @click="showUploadActionSheet">
          <uni-icons type="camera" size="30" color="#999"/>
        </view>
      </view>

      <!-- 权限设置 -->
      <view class="permission-section">
        <radio-group class="permission-group" @change="handlePermissionChange">
          <label class="permission-item">
            <radio value="9" :checked="form.scope === 9" color="#07C160"/>
            <text>公开</text>
          </label>
          <label class="permission-item">
            <radio value="1" :checked="form.scope === 1" color="#07C160"/>
            <text>自己可见</text>
          </label>
          <label class="permission-item">
            <radio value="2" :checked="form.scope === 2" color="#07C160"/>
            <text>好友可见</text>
          </label>
<!--          <label class="permission-item">
            <radio value="exclude" :checked="permission === 'exclude'" color="#07C160"/>
            <text>部分不可见</text>
            <uni-icons type="right" size="16" color="#999"/>
          </label>-->
        </radio-group>
      </view>
      <view class="visible-range">
        <view class="visible-range-item" style="border-bottom: 1px solid #eee;">
          <text style="margin-right: 20rpx;">群聊空间可见</text>
          <up-switch v-model="form.groupVisible" activeColor="#13ce66" inactiveColor="#ff4949"></up-switch>
        </view>
        <view class="visible-range-item">
          <text style="margin-right: 20rpx;">地区空间可见</text>
          <up-switch v-model="form.regionVisible" activeColor="#13ce66" inactiveColor="#ff4949"></up-switch>
        </view>
      </view>
    </view>

    <!-- 底部发布按钮 -->
    <view class="publish-btn-container">
      <button class="publish-btn" :disabled="!canPublish" @click="handlePublish">发布</button>
    </view>

    <!-- 上传进度弹窗 -->
    <uni-popup ref="uploadingPopup" type="center">
      <view class="upload-progress">
        <view class="progress-text">上传中 {{ uploadProgress }}%</view>
        <progress :percent="uploadProgress" active stroke-width="3" />
      </view>
    </uni-popup>
  </view>
</template>

<script>
import UNI_APP from '@/.env.js'
import HeadImage from "../../components/head-image/head-image.vue";

export default {
  components: {HeadImage},
  data() {
    return {
      uploadProgress: 0,
      uploadingPopup: null,
      form: {
        id: null,
        content: '',
        category: '',
        scope: 9,
        anonymous: false,
        nickName: '',
        avatar: '',
        characterId: null,
        enableCharacterChoose: true,
        groupVisible: true,
        regionVisible: true,
        files: []
      },
      fileList: [],
    };
  },

  computed: {
    canPublish() {
      return this.form.content.trim().length > 0 || this.fileList.length > 0;
    },
    showUploadBtn() {
      const imageCount = this.fileList.filter(item => item.fileType === 1).length;
      const videoCount = this.fileList.filter(item => item.fileType === 2).length;
      return !(imageCount >= 9 || videoCount >=1);
    },
    imageEnabled() {
      const imageCount = this.fileList.filter(item => item.fileType === 1).length;
      const videoCount = this.fileList.filter(item => item.fileType === 2).length;
      return imageCount < 9 && videoCount < 1;
    },
    videoEnabled() {
      const imageCount = this.fileList.filter(item => item.fileType === 1).length;
      const videoCount = this.fileList.filter(item => item.fileType === 2).length;
      return imageCount < 1 && videoCount < 1;
    },
    imageAction() {
      return `/image/upload`;
    },
    videoAction() {
      return `/video/upload`;
    },
    userInfo() {
      return this.userStore.userInfo;
    }
  },

  methods: {
    handleBack() {
      if (this.form.content || this.fileList.length > 0) {
        uni.showModal({
          title: '提示',
          content: '是否放弃编辑？',
          success: (res) => {
            if (res.confirm) {
              uni.navigateBack();
            }
          }
        });
      } else {
        uni.navigateBack();
      }
    },

    chooseMedia(mediaType) {
      const isVideo = this.fileList.some(item => item.fileType === 2);
      const isImage = this.fileList.some(item => item.fileType === 1);

      if (isVideo && this.fileList.length >= 1) {
        uni.showToast({ title: '最多选择1个视频', icon: 'none' });
        return;
      }

      if (isImage && this.fileList.length >= 9) {
        uni.showToast({ title: '最多选择9张图片', icon: 'none' });
        return;
      }

      if (mediaType === 'image') {
        uni.chooseImage({
          count: 9 - this.fileList.length,
          mediaType: ['image'],
          sourceType: ['album'],
          success: (res) => {
            res.tempFiles.forEach(file => {
              this.fileList.push({
                fileType: 1,
                url: file.path,
                thumbFile: file,
                upload: false,
              });
            });
          }
        });
      } else if (mediaType === 'video') {
        uni.chooseVideo({
          sourceType: ['album'],
          maxDuration: 60,
          camera: 'back',
          success: (res) => {
            console.log("res-----------", res)
            this.fileList.push({
              fileType: 2,
              url: res.tempFilePath,
              thumbFile: res.tempFile,
              upload: false,
            });
          }
        });
      }
    },

    showUploadActionSheet() {
      this.chooseMedia('image');
      // uni.showActionSheet({
      //   itemList: ['图片', '视频'],
      //   success: (res) => {
      //     switch (res.tapIndex) {
      //       case 0:
      //         this.chooseMedia('image');
      //         break;
      //       case 1:
      //         this.chooseMedia('video');
      //         break;
      //     }
      //   }
      // });
    },

    previewMedia(index) {
      const item = this.fileList[index];
      if (item.fileType === 1) {
        uni.previewImage({
          urls: this.fileList.filter(m => m.fileType === 1).map(m => m.url),
          current: item.url
        });
      }
    },

    deleteMedia(index) {
      uni.showModal({
        title: '提示',
        content: '是否删除该文件？',
        success: (res) => {
          if (res.confirm) {
            this.fileList.splice(index, 1);
          }
        }
      });
    },

    handleLocation() {
      uni.chooseLocation({
        success: (res) => {
          console.log('选择位置:', res);
        }
      });
    },

    handlePermissionChange(e) {
      this.form.scope = e.detail.value;
    },

    async handlePublish() {
      if (!this.canPublish) {
        return
      }

      let uploadSuccess = true;

      if (this.fileList && this.fileList.length > 0) {
        this.$refs.uploadingPopup.open();
        let progress = Math.floor(1 / this.fileList.length * 100);
        console.log("progress", progress)
        this.form.files = [];
        for (let i = 0; i < this.fileList.length; i++) {
          if (this.fileList[i].url.startsWith("blob:")) {
            if (this.fileList[i].fileType === 1) {
              await this.uploadFile(this.fileList[i].url, this.imageAction).then(data => {
                console.log("image-result", data)
                if (data) {
                  this.form.files.push({
                    fileType: 1,
                    url: data.originUrl,
                  });
                  this.uploadProgress += progress;
                } else {
                  this.uploadProgress = 0;
                  this.$refs.uploadingPopup.close();
                  uploadSuccess = false;
                }
              });
            } else if (this.fileList[i].fileType === 2) {
              await this.uploadFile(this.fileList[i].url, this.videoAction).then(result => {
                console.log("video-result", result)
                if (result) {
                  this.form.files.push({
                    fileType: 2,
                    url: result.videoUrl,
                    coverUrl: result.coverUrl,
                  });
                  this.uploadProgress += progress;
                } else {
                  this.uploadProgress = 0;
                  this.$refs.uploadingPopup.close();
                  uploadSuccess = false;
                }
              });
            }
          } else {
            this.form.files.push({
              fileType: this.fileList[i].fileType,
              url: this.fileList[i].url,
            });
            this.uploadProgress += progress;
          }
        }
      }
      this.$refs.uploadingPopup.close();
      if (!uploadSuccess) {
        uni.showToast({
          title: '文件上传失败',
          icon: 'fail',
        });
        return;
      }
      console.log("this.uploadProgress", this.uploadProgress)
      this.uploadProgress = 100;
      console.log("form", this.form);

      let url = "/talk"
      if (this.form.id != null) {
        url += "/update"
      } else {
        url += "/add"
      }
      this.$http({
        url: url,
        method: 'post',
        data: this.form
      }).then((data) => {
        uni.showToast({
          title: "发布成功",
          icon: "success",
          duration: 2000,
          success: () => {
            setTimeout(() => {
              let pages = getCurrentPages();
              let prevPage = pages[pages.length - 2];
              prevPage.$vm.refreshTalkList();
              uni.navigateBack();
            }, 2000);
          },
        });
      }).finally(() => {

      })
    },
    uploadFile(filePath, apiPath) {
      return new Promise((resolve, reject) => {
        uni.uploadFile({
          url: UNI_APP.BASE_URL + apiPath,
          header: {
            accessToken: uni.getStorageSync("loginInfo").accessToken
          },
          filePath: filePath, // 要上传文件资源的路径
          name: 'file',
          success: (res) => {
            let data = JSON.parse(res.data);
            if (data.code != 200) {
              uni.showToast({
                icon: "none",
                title: data.message,
              })
              resolve(null);
            } else {
              resolve(data.data);
            }
          },
          fail: (err) => {
            console.log(err);
            resolve(null);
          }
        })
      })
    },
    getTalkDetail(talkId) {
      this.$http({
        url: `/talk/getTalkDetail/${talkId}`,
        method: 'get'
      }).then((data) => {
        this.form = data;
        this.form.files = [];
        this.fileList = data.fileList;
      });
    },
  },
  onLoad(options) {
    this.form.category = options.category;
    if (options.talkId) {
      this.getTalkDetail(options.talkId);
    } else {
      this.form.avatar = this.userInfo.headImage;
      this.form.nickName = this.userInfo.nickName;
    }
  }
}
</script>


<style lang="scss" scoped>

.container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f8f8f8;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  height: 88rpx;
  background-color: #fff;
  border-bottom: 1px solid #eee;
}

.nav-left {
  display: flex;
  align-items: center;
}

.nav-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.nav-right {
  font-size: 14px;
  color: #999;
}

.nav-right-active {
  color: #07C160;
}

.content {
  flex: 1;
  overflow: auto;
  padding: 20rpx 30rpx;
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}

.nickname {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.input-area {
  width: 100%;
  height: 200rpx;
  padding: 20rpx 0;
  font-size: 14px;
  line-height: 1.5;
  color: #333;
}

.placeholder {
  color: #999;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 5rpx;
  margin: 20rpx 0;
}

.media-item {
  position: relative;
  width: 220rpx;
  height: 220rpx;
  margin: 0 20rpx 20rpx 0;
}

.media-item:nth-child(3n) {
  margin-right: 0;
}

.media-content {
  width: 100%;
  height: 100%;
  border-radius: 8rpx;
  background-color: #f0f0f0;
}

.delete-btn {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
}

.upload-btn {
  width: 220rpx;
  height: 220rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-radius: 8rpx;
  border: 1px dashed #ddd;
}

.function-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1px solid #eee;
}

.function-item {
  margin-right: 40rpx;
}

.permission-section {
  margin-top: 20rpx;
  background-color: #fff;
  border-radius: 8rpx;
  padding: 0 20rpx;
}

.permission-group {
  display: flex;
  flex-direction: column;
}

.permission-item {
  display: flex;
  align-items: center;
  height: 100rpx;
  border-bottom: 1px solid #eee;
}

.permission-item:last-child {
  border-bottom: none;
}

.permission-item text {
  margin-left: 20rpx;
  font-size: 14px;
  color: #333;
  flex: 1;
}

.publish-btn-container {
  padding: 20rpx 30rpx;
  background-color: #fff;
}

.visible-range {
  background-color: #fff;
  margin: 20rpx 0;
  padding: 10rpx 20rpx;
  border-radius: 8rpx;

  .visible-range-item {
    padding: 20rpx 0;
    display: flex;
    align-items: center;
  }
}

.publish-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background-color: #07C160;
  color: #fff;
  border-radius: 44rpx;
  font-size: 16px;
}

.publish-btn[disabled] {
  background-color: #9FE6B8;
}

.upload-progress {
  background-color: #fff;
  padding: 40rpx;
  border-radius: 8rpx;
  width: 500rpx;
}

.progress-text {
  text-align: center;
  font-size: 14px;
  color: #333;
  margin-bottom: 20rpx;
}

.cursor-pointer {
  cursor: pointer;
}
</style>

