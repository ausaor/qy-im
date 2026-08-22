<template>
  <el-dialog
    :title="isEdit ? '编辑短视频' : '发布短视频'"
    :visible.sync="visible"
    width="600px"
    :before-close="handleClose"
    :append-to-body="true"
    :close-on-click-modal="false"
  >
    <el-form
      :rules="rules"
      ref="formRef"
      label-width="100px"
      :model="form"
      class="form-box"
    >
      <el-form-item label="视频上传" prop="videoUrl">
        <VideoUpload
          ref="videoUploadRef"
          @upload-success="handleVideoUploadSuccess"
          @upload-remove="handleVideoRemove"
        />
      </el-form-item>

      <el-form-item label="可见范围" prop="scope">
        <el-select v-model="form.scope" placeholder="请选择可见范围">
          <el-option
            v-for="item in scopeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入视频标题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="我的角色" prop="objectId" v-show="myCharacters.length">
        <div class="character-selector">
          <el-select v-model="selectValue" placeholder="请选择" @change="selectMyCharacterChange">
            <el-option
                v-for="item in myCharacters"
                :key="item.characterId"
                :label="item.characterName"
                :value="item.characterId">
              <span style="float: left">{{ item.characterName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px"><head-image :name="item.characterName" :url="item.characterAvatar" :size="30"></head-image></span>
            </el-option>
          </el-select>
          <div v-if="form.type !== 'user'" class="selected-character">
            <head-image :name="form.objectName" :url="form.avatar" :size="32"/>
            <el-button class="clear-character" type="text" icon="el-icon-close" @click="clearCharacter"/>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="角色头像" prop="avatarId" v-show="myCharacters.length">
        <div class="character-selector">
          <el-select v-model="selectAvatarValue" placeholder="请选择" @change="selectCharacterAvatarChange">
            <el-option
                v-for="item in characterAvatars"
                :key="item.id"
                :label="item.name"
                :value="item.id">
              <span style="float: left">{{ item.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px"><head-image :name="item.name" :url="item.avatar" :size="30"></head-image></span>
            </el-option>
          </el-select>
          <div v-if="form.type !== 'user'" class="selected-character">
            <head-image :name="form.avatarName" :url="form.avatarImage" :size="32"/>
            <el-button class="clear-character" type="text" icon="el-icon-close" @click="clearAvatar"/>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="封面图片" prop="coverUrl">
        <ImageUpload
          ref="coverUploadRef"
          v-model="form.coverUrl"
          @upload-success="handleCoverUploadSuccess"
          @upload-remove="handleCoverRemove"
        />
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose" size="small">取消</el-button>
      <el-button
        type="primary"
        @click="submitForm"
        size="small"
        :loading="submitting"
      >
        {{ isEdit ? '更新' : '发布' }}
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import VideoUpload from '@/components/common/VideoUpload.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import HeadImage from "@components/common/HeadImage.vue";

export default {
  name: 'ShortVideoEdit',
  components: {
    HeadImage,
    VideoUpload,
    ImageUpload,
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    videoId: {
      type: Number,
      default: null
    },
    type: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      submitting: false,
      form: {
        id: null,
        scope: 9,
        title: '',
        coverUrl: '',
        videoUrl: '',
        duration: 0,
        width: 0,
        height: 0,
        size: 0,
        type: 'user',
        objectId: null,
        avatarId: null,
        objectName: '',
        avatar: '',
        avatarName: '',
        avatarImage: '',
      },
      rules: {
        videoUrl: [
          { required: true, message: '请上传视频', trigger: 'change' }
        ],
        scope: [
          { required: true, message: '请选择可见范围', trigger: 'change' }
        ],
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' }
        ],
        coverUrl: [
          { required: true, message: '请上传视频以生成封面', trigger: 'change' }
        ]
      },
      scopeOptions: [
        { value: 9, label: '公开' },
        { value: 3, label: '关注可见' },
        { value: 2, label: '好友可见' },
        { value: 1, label: '私密' }
      ],
      myCharacters: [],
      characterAvatars: [],
      selectValue: '',
      selectAvatarValue: '',
      selectAvatars: [],
    }
  },
  computed: {
    isEdit() {
      return this.videoId != null
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.resetForm()
        if (this.videoId != null) {
          this.loadVideoDetail()
        }
        this.queryMyCharacters();
      }
    },
    // videoId() {
    //   if (this.visible && this.videoId != null) {
    //     this.loadVideoDetail()
    //   }
    // }
  },
  methods: {
    handleClose() {
      this.resetForm()
      this.$emit('close')
    },
    resetForm() {
      this.form = {
        id: null,
        scope: 9,
        title: '',
        coverUrl: '',
        videoUrl: '',
        duration: 0,
        width: 0,
        height: 0,
        size: 0,
        type: 'user',
        objectId: null,
        objectName: '',
        avatar: '',
      }
      this.selectValue = ''
      this.$nextTick(() => {
        if (this.$refs.formRef) {
          this.$refs.formRef.clearValidate()
        }
      })
    },
    loadVideoDetail() {
      this.$http({
        url: `/shortVideo/detail/${this.videoId}`,
        method: 'get'
      }).then((data) => {
        this.form.id = data.id
        this.form.objectId = data.objectId
        this.form.avatarId = data.avatarId
        this.form.objectName = data.objectName || ''
        this.form.avatar = data.avatar || ''
        this.form.type = data.type
        this.form.scope = data.scope
        this.form.title = data.title || ''
        this.form.coverUrl = data.coverUrl || ''
        this.form.videoUrl = data.videoUrl || ''
        this.form.duration = data.duration || 0
        this.form.width = data.width || 0
        this.form.height = data.height || 0
        this.form.size = data.size || 0
        this.syncSelectedCharacter()
      }).catch(() => {
        this.$message.error('获取视频详情失败')
      })
    },
    handleVideoUploadSuccess(data) {
      this.form.videoUrl = data.videoUrl
      this.form.coverUrl = data.coverUrl
      if (data.size) {
        this.form.size = data.size
      }
      this.form.duration = data.duration
      this.form.width = data.width
      this.form.height = data.height
    },
    handleVideoRemove() {
      this.form.videoUrl = ''
      this.form.coverUrl = ''
      this.form.size = 0
    },
    handleCoverUploadSuccess(data) {
      this.form.coverUrl = data.originUrl
      this.form.width = data.width
    },
    handleCoverRemove() {
      this.form.coverUrl = ''
    },
    submitForm() {
      this.$refs.formRef.validate((valid) => {
        if (!valid) {
          return
        }
        this.submitting = true
        const isEdit = this.form.id != null
        const url = isEdit ? '/shortVideo/update' : '/shortVideo/add'
        const params = {
          type: this.form.type || this.type,
          objectId: this.form.objectId,
          avatarId: this.form.avatarId,
          scope: this.form.scope,
          title: this.form.title,
          coverUrl: this.form.coverUrl,
          videoUrl: this.form.videoUrl,
          duration: this.form.duration,
          width: this.form.width,
          height: this.form.height,
          size: this.form.size,
        }
        if (isEdit) {
          params.id = this.form.id
        }
        this.$http({
          url: url,
          method: 'post',
          data: params
        }).then(() => {
          this.$message.success(isEdit ? '更新成功' : '发布成功')
          this.resetForm()
          this.$emit('refresh')
          this.handleClose()
        }).catch(() => {
          this.$message.error(isEdit ? '更新失败' : '发布失败')
        }).finally(() => {
          this.submitting = false
        })
      })
    },
    queryMyCharacters() {
      this.$http({
        url: "/characterUser/getMyCharacters",
        method: 'get'
      }).then((data) => {
        this.myCharacters = data;
        this.syncSelectedCharacter();
      });
    },
    syncSelectedCharacter() {
      if (!this.form.objectId || !this.myCharacters.length) return;
      const character = this.myCharacters.find(item => String(item.id) === String(this.form.objectId));
      if (!character) return;
      this.selectValue = character.characterId;
      this.form.objectName = character.characterName;
      this.form.avatar = character.characterAvatar;
    },
    selectMyCharacterChange(value) {
      let character = this.myCharacters.find(item => item.characterId === value)
      this.form.objectName = character.characterName;
      this.form.avatar = character.characterAvatar;
      this.form.objectId = character.characterId;
      this.form.type = 'character';
      this.form.avatarId = null;
      this.form.avatarName = '';
      this.form.avatarImage = '';
      this.selectAvatarValue = '';
      this.characterAvatars = character.characterAvatars;
    },
    selectCharacterAvatarChange(value) {
      let avatar = this.characterAvatars.find(item => item.id === value)
      this.form.avatarName = avatar.name;
      this.form.avatarImage = avatar.avatar;
      this.form.avatarId = avatar.id;
    },
    clearCharacter() {
      this.selectValue = '';
      this.form.objectId = null;
      this.form.objectName = '';
      this.form.avatar = '';
      this.form.avatarId = null;
      this.form.avatarName = '';
      this.form.avatarImage = '';
      this.selectAvatarValue = '';
      this.form.type = 'user';
    },
    clearAvatar() {
      this.form.avatarId = null;
      this.form.avatarName = '';
      this.form.avatarImage = '';
      this.selectAvatarValue = '';
    }
  }
}
</script>

<style scoped lang="scss">
.form-box {
  .el-form-item {
    margin-bottom: 20px;
  }
}

.dialog-footer {
  text-align: right;
}

.character-selector {
  display: flex;
  align-items: center;

  .el-select {
    flex: 1;
  }
}

.selected-character {
  display: flex;
  align-items: center;
  margin-left: 12px;
}

.clear-character {
  margin-left: 4px;
  padding: 0;
  color: #909399;
}
</style>
