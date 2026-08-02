<template>
  <view class="short-video-edit-page">
    <view class="page-header">
      <view class="header-action" @tap="goBack">
        <uni-icons type="back" size="24" color="#202733"/>
      </view>
      <text class="header-title">{{ isEdit ? '编辑作品' : '发布作品' }}</text>
      <view class="header-action publish-action" :class="{ disabled: submitting }" @tap="submit">
        <text>{{ submitting ? '发布中' : '发布' }}</text>
      </view>
    </view>

    <scroll-view class="edit-content" scroll-y>
      <view class="video-section">
        <video-upload :onBefore="onUploadBefore" :onSuccess="onUploadSuccess" :onError="onUploadError">
          <view class="video-picker">
            <image v-if="form.coverUrl" class="cover-image" :src="form.coverUrl" mode="aspectFill"/>
            <view v-else class="cover-placeholder">
              <uni-icons type="videocam" size="44" color="#9ca7b5"/>
              <text>选择视频</text>
            </view>
            <view v-if="form.coverUrl" class="replace-mask">
              <uni-icons type="camera" size="21" color="#ffffff"/>
              <text>更换视频</text>
            </view>
            <view v-if="form.videoUrl" class="preview-mask" @tap.stop="previewVideo">
              <text class="preview-play-icon">▶</text>
              <text>预览</text>
            </view>
          </view>
        </video-upload>
        <text class="video-tip">支持从相册选择，视频时长不超过 60 秒</text>
      </view>

      <view class="form-card">
        <view class="form-row title-row">
          <text class="field-label">标题</text>
          <textarea v-model="form.title" class="title-input" placeholder="给作品起个标题吧"
                    placeholder-class="input-placeholder" :maxlength="100" auto-height/>
          <text class="title-count">{{ titleLength }}/100</text>
        </view>
        <view class="form-row scope-row">
          <text class="field-label">可见范围</text>
          <picker :range="scopeOptions" range-key="label" :value="scopeIndex" @change="onScopeChange">
            <view class="scope-picker">
              <text>{{ scopeOptions[scopeIndex].label }}</text>
              <uni-icons type="right" size="16" color="#9ca7b5"/>
            </view>
          </picker>
        </view>
        <view v-show="myCharacters.length" class="form-row character-row" @tap="chooseMyCharacters">
          <text class="field-label">选择角色</text>
          <view class="character-picker">
            <view v-show="form.type !== 'user'" class="selected-character">
              <head-image :name="form.objectName" :url="form.avatar" :size="56"/>
              <text class="selected-character-name">{{ form.objectName }}</text>
              <view class="clear-character" @tap.stop="clearCharacter">
                <uni-icons type="closeempty" size="18" color="#9ca7b5"/>
              </view>
            </view>
            <uni-icons type="right" size="16" color="#9ca7b5"/>
          </view>
        </view>
      </view>
    </scroll-view>
    <video-play v-if="previewVisible" :video-url="form.videoUrl" :cover-url="form.coverUrl"
                :visible.sync="previewVisible" @close="previewVisible = false"/>
    <character-list ref="characterList" :characters="characterList" @confirm="chooseCharacter" @more="moreCharacterAvatars"></character-list>
    <character-avatar-list ref="characterAvatarList" :characterAvatars="characterAvatarList" @confirm="chooseCharacterAvatar"></character-avatar-list>
  </view>
</template>

<script>
import VideoUpload from '../../components/video-upload/video-upload.vue'
import VideoPlay from '../../components/video-play/video-play.vue'
import CharacterList from "../../components/character-list/character-list.vue";
import CharacterAvatarList from "../../components/character-avatar-list/character-avatar-list.vue";
import HeadImage from "../../components/head-image/head-image.vue";

export default {
  name: 'short-video-edit',
  components: {VideoUpload, VideoPlay, CharacterList, CharacterAvatarList, HeadImage},
  data() {
    return {
      videoId: null, loadingDetail: false, uploading: false, submitting: false, previewVisible: false,
      scopeOptions: [
        {value: 9, label: '公开'}, {value: 3, label: '关注可见'},
        {value: 2, label: '好友可见'}, {value: 1, label: '私密'},
      ],
      form: {
        id: null,
        scope: 9,
        title: '',
        videoUrl: '',
        coverUrl: '',
        type: 'user',
        duration: null,
        width: null,
        height: null,
        size: null,
        objectId: null,
        avatarId: null,
        objectName: '',
        avatar: '',
      },
      myCharacters: [],
      characterList: [],
      characterAvatarList: [],
    }
  },
  computed: {
    isEdit() {
      return !!this.videoId
    },
    scopeIndex() {
      const index = this.scopeOptions.findIndex(item => item.value === Number(this.form.scope))
      return index === -1 ? 0 : index
    },
    titleLength() {
      return Array.from(this.form.title || '').length
    },
  },
  methods: {
    loadDetail() {
      this.loadingDetail = true
      uni.showLoading({title: '加载中...', mask: true})
      this.$http({url: `/shortVideo/detail/${this.videoId}`, method: 'GET'}).then((data) => {
        if (!data) {
          uni.showToast({icon: 'none', title: '视频不存在或已删除'})
          return
        }
        this.form = {
          ...this.form, id: data.id, scope: Number(data.scope), title: data.title || '',type: data.type,
          videoUrl: data.videoUrl || '', coverUrl: data.coverUrl || '', duration: data.duration,objectId: data.objectId,
          width: data.width, height: data.height, size: data.size, objectName: data.objectName || '',avatar: data.avatar || '',
        }
      }).finally(() => {
        this.loadingDetail = false
        uni.hideLoading()
      })
    },
    onUploadBefore() {
      if (this.uploading || this.submitting) return false
      this.uploading = true
      uni.showLoading({title: '视频上传中...', mask: true})
      return true
    },
    onUploadSuccess(file, res) {
      const video = res && res.data
      if (!video || !video.videoUrl || !video.coverUrl) {
        this.onUploadError()
        return
      }
      this.form.videoUrl = video.videoUrl
      this.form.coverUrl = video.coverUrl
      this.form.duration = video.duration == null ? null : Number(video.duration)
      this.form.width = video.width == null ? null : Number(video.width)
      this.form.height = video.height == null ? null : Number(video.height)
      this.form.size = file && file.size ? Number(file.size) : null
      this.uploading = false
      uni.hideLoading()
      uni.showToast({title: '上传成功', icon: 'success'})
    },
    onUploadError() {
      this.uploading = false
      uni.hideLoading()
      uni.showToast({icon: 'none', title: '视频上传失败，请重试'})
    },
    onScopeChange(event) {
      this.form.scope = this.scopeOptions[Number(event.detail.value)].value
    },
    previewVideo() {
      if (this.form.videoUrl) this.previewVisible = true
    },
    submit() {
      if (this.submitting || this.uploading || this.loadingDetail) return
      const title = this.form.title.trim()
      if (!this.form.videoUrl || !this.form.coverUrl) return uni.showToast({icon: 'none', title: '请先上传视频'})
      if (!title) return uni.showToast({icon: 'none', title: '请输入视频标题'})
      if (Array.from(title).length > 100) return uni.showToast({icon: 'none', title: '标题最多100个字'})

      const data = {...this.form, title, scope: Number(this.form.scope)}
      data.type = this.form.type;
      this.submitting = true
      uni.showLoading({title: this.isEdit ? '保存中...' : '发布中...', mask: true})
      this.$http({url: this.isEdit ? '/shortVideo/update' : '/shortVideo/add', method: 'POST', data}).then(() => {
        uni.showToast({title: this.isEdit ? '保存成功' : '发布成功', icon: 'success'})
        setTimeout(() => uni.navigateBack(), 600)
      }).finally(() => {
        this.submitting = false
        uni.hideLoading()
      })
    },
    queryMyCharacters() {
      this.$http({
        url: "/characterUser/getMyCharacters",
        method: 'get'
      }).then((data) => {
        this.myCharacters = data;
      });
    },
    chooseMyCharacters() {
      this.characterList = [];
      if (this.myCharacters.length) {
        this.myCharacters.forEach(item => {
          this.characterList.push(item.character);
        });
        this.$refs.characterList.open();
      }
    },
    chooseCharacter(character) {
      this.form.objectId = character.id;
      this.form.objectName = character.name;
      this.form.avatar = character.avatar;
      this.form.avatarId = null;
      this.form.type = 'character'
    },
    async moreCharacterAvatars(character) {
      this.form.objectId = character.id;
      this.form.objectName = character.name;
      this.form.avatar = character.avatar;
      this.form.type = 'character'
      await this.queryCharacterAvatars(character.id);
      this.$refs.characterAvatarList.open();
    },
    async queryCharacterAvatars(templateCharacterId) {
      await this.$http({
        url: `/characterAvatar/list/${templateCharacterId}`,
        method: 'get'
      }).then((data) => {
        this.characterAvatarList = data;
      });
    },
    chooseCharacterAvatar(characterAvatar) {
      this.form.avatarId = characterAvatar.id;
      this.form.avatar = characterAvatar.avatar;
      this.form.type = 'character'
      if (characterAvatar.level !== 0) {
        this.form.objectName = characterAvatar.name;
      }
    },
    clearCharacter() {
      this.form.objectId = null;
      this.form.avatarId = null;
      this.form.objectName = '';
      this.form.avatar = '';
      this.form.type = 'user'
    },
    goBack() {
      if (!this.submitting) uni.navigateBack()
    },
  },
  onLoad(options) {
    if (options.videoId) {
      this.videoId = options.videoId
      this.loadDetail()
    }
    this.queryMyCharacters();
  },
}
</script>

<style scoped lang="scss">
.short-video-edit-page {
  min-height: 100vh;
  background: #f7f8fa;
  color: #202733;
}

.page-header {
  height: 88rpx;
  padding: env(safe-area-inset-top) 28rpx 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-sizing: content-box;
  border-bottom: 1rpx solid #eef0f3;
}

.header-action {
  min-width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 32rpx;
  font-weight: 600;
}

.publish-action {
  justify-content: flex-end;
  color: #2878f0;
  font-size: 29rpx;
  font-weight: 600;
}

.disabled {
  opacity: .55;
}

.edit-content {
  height: calc(100vh - 88rpx - env(safe-area-inset-top));
}

.video-section {
  padding: 32rpx 32rpx 24rpx;
  background: #fff;
}

.video-picker {
  height: 360rpx;
  overflow: hidden;
  position: relative;
  border-radius: 16rpx;
  background: #edf0f4;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #758091;
  font-size: 28rpx;
  gap: 16rpx;
}

.replace-mask {
  position: absolute;
  right: 20rpx;
  bottom: 20rpx;
  padding: 10rpx 18rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #fff;
  background: rgba(0, 0, 0, .5);
  font-size: 24rpx;
}

.preview-mask {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 112rpx;
  height: 112rpx;
  border-radius: 56rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgba(0, 0, 0, .48);
  font-size: 22rpx;
}

.preview-play-icon { font-size: 30rpx; line-height: 34rpx; }

.video-tip {
  display: block;
  margin-top: 18rpx;
  color: #9ca7b5;
  font-size: 24rpx;
}

.form-card {
  margin-top: 20rpx;
  background: #fff;
}

.form-row {
  margin: 0 32rpx;
  display: flex;
  min-height: 104rpx;
  border-bottom: 1rpx solid #eef0f3;
}

.form-row:last-child {
  border-bottom: 0;
}

.field-label {
  width: 145rpx;
  padding-top: 32rpx;
  flex: none;
  color: #303946;
  font-size: 29rpx;
}

.title-row {
  position: relative;
  align-items: flex-start;
}

.title-input {
  flex: 1;
  min-height: 96rpx;
  padding: 27rpx 70rpx 18rpx 0;
  box-sizing: border-box;
  color: #202733;
  font-size: 29rpx;
  line-height: 42rpx;
}

.input-placeholder {
  color: #b3bac5;
}

.title-count {
  position: absolute;
  right: 0;
  bottom: 25rpx;
  color: #a8b0bc;
  font-size: 22rpx;
}

.scope-row {
  align-items: center;
}

.scope-row .field-label {
  padding-top: 0;
}

.scope-picker {
  min-width: 220rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
  color: #596474;
  font-size: 28rpx;
}

.character-row {
  align-items: center;
}

.character-row .field-label {
  padding-top: 0;
}

.character-picker {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.selected-character {
  display: flex;
  align-items: center;
  margin-right: 16rpx;
}

.selected-character-name {
  max-width: 260rpx;
  margin-left: 12rpx;
  overflow: hidden;
  color: #596474;
  font-size: 28rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.clear-character {
  margin-left: 12rpx;
  line-height: 1;
}
</style>
