
<template>
  <view class="page-box">
    <!-- 顶部导航栏 -->
    <view class="top-nav-bar">
      <uni-icons class="back-icon cursor-pointer" type="back" size="24" color="#ffffff" @click="goBack"/>
      <uni-icons class="camera-icon cursor-pointer" type="camera" size="24" color="#ffffff" @click="openCamera"/>
    </view>

    <!-- 固定背景图 -->
    <view class="fixed-bg">
      <image class="bg-image" src="https://public.readdy.ai/ai/img_res/be9bbbcabbc10bdd17557fbb7c1b0b28.jpg" mode="aspectFill"/>
    </view>

    <!-- 内容区域 -->
    <scroll-view class="content-area" scroll-y @scrolltolower="loadMore">
      <view class="header-space"></view>

      <!-- 动态列表 -->
      <view class="moments-list">
        <view v-for="(item, index) in moments" :key="index" class="moment-item">
          <view class="user-info">
            <image class="avatar" :src="item.avatar" mode="aspectFill"/>
            <view class="info-right">
              <text class="nickname">{{item.nickname}}</text>
              <text class="time">{{item.time}}</text>
            </view>
            <text class="delete-btn cursor-pointer" @click="deleteMoment(index)">删除</text>
          </view>

          <view class="content">
            <text class="text">{{item.content}}</text>
            <view class="image-grid" v-if="item.images && item.images.length">
              <image
                  v-for="(img, imgIndex) in item.images"
                  :key="imgIndex"
                  :src="img"
                  class="content-image cursor-pointer"
                  mode="aspectFill"
                  @click="previewImage(item.images, imgIndex)"
              />
            </view>
          </view>

          <view class="interaction">
            <view class="like-comment">
              <view class="action-btn cursor-pointer" @click="toggleLike(index)">
                <uni-icons :type="item.isLiked ? 'heart-filled' : 'heart'" size="20" :color="item.isLiked ? '#ff5d5d' : '#666'"/>
                <text :class="['count', item.isLiked ? 'liked' : '']">{{item.likes}}</text>
              </view>
              <view class="action-btn cursor-pointer" @click="showCommentInput(index)">
                <uni-icons type="chat" size="20" color="#666"/>
                <text class="count">{{item.comments.length}}</text>
              </view>
            </view>

            <view class="comments" v-if="item.comments.length">
              <view v-for="(comment, cIndex) in item.comments" :key="cIndex" class="comment">
                <text class="comment-user">{{comment.user}}</text>
                <text class="comment-text">{{comment.text}}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <uni-load-more :status="loadMoreStatus"/>
    </scroll-view>

    <!-- 评论输入框 -->
    <uni-popup ref="commentPopup" type="bottom">
      <view class="comment-input-box">
        <input
            class="input"
            v-model="commentText"
            placeholder="说点什么..."
            @confirm="submitComment"
        />
        <button class="send-btn cursor-pointer" @click="submitComment">发送</button>
      </view>
    </uni-popup>
  </view>
</template>

<script lang="ts" setup>
import { ref } from 'vue';

const moments = ref([
  {
    nickname: '元歌',
    avatar: 'https://public.readdy.ai/ai/img_res/718f526c479002e76297a3cade749324.jpg',
    time: '2025-03-08',
    content: '在撒哈拉沙漠体验热气球之旅，感受壮丽的沙漠日出，这里的景色令人难忘。',
    images: ['https://public.readdy.ai/ai/img_res/84c00efbeca9b1dc2c280eb439528646.jpg'],
    likes: 45,
    isLiked: false,
    comments: [
      { user: '清风', text: '太美了！好想去体验' },
      { user: '明月', text: '景色真是壮观，分享好照片谢谢' }
    ]
  },
  {
    nickname: '元歌',
    avatar: 'https://public.readdy.ai/ai/img_res/d5465a8b03d6f2d125dc9d9b5f078d00.jpg',
    time: '2025-03-07',
    content: '今天和可爱的小狗狗一起度过了愉快的下午时光。',
    images: ['https://public.readdy.ai/ai/img_res/fa7fc362bf3c0e32ac8d6019128ac69b.jpg'],
    likes: 32,
    isLiked: false,
    comments: [
      { user: '小雨', text: '好可爱的狗狗！' }
    ]
  }
]);

const commentPopup = ref();
const commentText = ref('');
const currentMomentIndex = ref(-1);
const loadMoreStatus = ref('more');

const goBack = () => {
  uni.navigateBack();
};

const openCamera = () => {
  uni.chooseImage({
    count: 9,
    success: (res) => {
      console.log(res.tempFilePaths);
    }
  });
};

const toggleLike = (index: number) => {
  const moment = moments.value[index];
  moment.isLiked = !moment.isLiked;
  moment.likes += moment.isLiked ? 1 : -1;
};

const showCommentInput = (index: number) => {
  currentMomentIndex.value = index;
  commentText.value = '';
  commentPopup.value.open();
};

const submitComment = () => {
  if (!commentText.value.trim()) return;

  if (currentMomentIndex.value >= 0) {
    moments.value[currentMomentIndex.value].comments.push({
      user: '我',
      text: commentText.value
    });
  }

  commentText.value = '';
  commentPopup.value.close();
};

const deleteMoment = (index: number) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这条动态吗？',
    success: (res) => {
      if (res.confirm) {
        moments.value.splice(index, 1);
      }
    }
  });
};

const previewImage = (images: string[], current: number) => {
  uni.previewImage({
    urls: images,
    current: images[current]
  });
};

const loadMore = () => {
  loadMoreStatus.value = 'loading';
  setTimeout(() => {
    loadMoreStatus.value = 'noMore';
  }, 2000);
};
</script>

<style lang="scss" scoped>
.page-box {
  height: 100%;
  background-color: #f8f8f8;
}

.top-nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  background-color: transparent;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30rpx;
  z-index: 100;
}

.back-icon, .camera-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.fixed-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 400rpx;
  z-index: 2;
}

.bg-image {
  width: 100%;
  height: 100%;
}

.content-area {
  position: relative;
  height: 100%;
  z-index: 1;
}

.header-space {
  height: 400rpx;
}

.moments-list {
  background-color: #ffffff;
  border-radius: 20rpx 20rpx 0 0;
  margin-top: -20rpx;
  position: relative;
}

.moment-item {
  padding: 30rpx;
  border-bottom: 2rpx solid #f5f5f5;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.info-right {
  flex: 1;
  margin-left: 20rpx;
}

.nickname {
  font-size: 30rpx;
  color: #333333;
  font-weight: 500;
}

.time {
  font-size: 24rpx;
  color: #999999;
  display: block;
  margin-top: 4rpx;
}

.delete-btn {
  font-size: 28rpx;
  color: #999999;
}

.content {
  margin-top: 20rpx;
}

.text {
  font-size: 28rpx;
  color: #333333;
  line-height: 1.6;
}

.image-grid {
  margin-top: 20rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.content-image {
  width: 220rpx;
  height: 220rpx;
  border-radius: 8rpx;
}

.interaction {
  margin-top: 20rpx;
}

.like-comment {
  display: flex;
  gap: 40rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.count {
  font-size: 28rpx;
  color: #666666;
}

.liked {
  color: #ff5d5d;
}

.comments {
  margin-top: 20rpx;
  background-color: #f8f8f8;
  padding: 20rpx;
  border-radius: 8rpx;
}

.comment {
  margin-bottom: 10rpx;
}

.comment-user {
  font-size: 28rpx;
  color: #576b95;
  font-weight: 500;
}

.comment-text {
  font-size: 28rpx;
  color: #333333;
  margin-left: 10rpx;
}

.comment-input-box {
  display: flex;
  padding: 20rpx;
  background-color: #ffffff;
  border-top: 2rpx solid #eeeeee;
}

.input {
  flex: 1;
  height: 72rpx;
  background-color: #f8f8f8;
  border-radius: 36rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
}

.send-btn {
  margin-left: 20rpx;
  width: 120rpx;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  background-color: #07c160;
  color: #ffffff;
  border-radius: 36rpx;
  font-size: 28rpx;
}

.cursor-pointer {
  cursor: pointer;
}
</style>

