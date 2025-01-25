<template>
  <div>
    <div class="buttons">
      <el-button @click="addTemplateGroup()" type="primary">新增<i class="el-icon-plus el-icon--right"></i></el-button>
      <el-button v-if="isAdmin" plain @click="toReviewPage">待审批</el-button>
    </div>
    <div class="tab-box">
      <el-tabs type="card" @tab-click="handleTabClick">
        <el-tab-pane label="我的群聊模板" name="templateGroup">
        </el-tab-pane>
        <el-tab-pane label="全部群聊模板" name="allTemplateGroup">
        </el-tab-pane>
      </el-tabs>
    </div>
    <div class="template-group-list">
      <el-card shadow="always" class="box-card" v-for="(templateGroup,index) in templateGroups" :key="index">
        <div slot="header" class="header">
          <span>{{ templateGroup.groupName }}</span>
          <el-button v-if="templateGroup.isOwner" class="operate-button"
                     type="primary" icon="el-icon-edit" circle
                     @click="editTemplateGroup(templateGroup)"></el-button>
          <el-button v-if="templateGroup.isOwner" class="operate-button"
                     type="danger" icon="el-icon-delete" circle
                     @click="deleteTemplateGroup(templateGroup)"></el-button>
          <el-button class="operate-button"
                     icon="el-icon-user-solid" circle
                     @click="editTemplateCharacter(templateGroup)"></el-button>
        </div>
        <div>
          <head-image class="head-image" :url="templateGroup.avatar" :size="80"></head-image>
          <div class="info">
            <el-descriptions title="群聊信息" :column="2">
              <el-descriptions-item label="模板人物" span="2">{{ templateGroup.count }}</el-descriptions-item>
              <el-descriptions-item label="创建人" span="2">{{ templateGroup.creator }}</el-descriptions-item>
              <el-descriptions-item label="创建时间" span="2">{{ templateGroup.createTime }}</el-descriptions-item>
              <el-descriptions-item label="更新时间" span="2">{{ templateGroup.updateTime }}</el-descriptions-item>
              <el-descriptions-item label="状态" span="2">
                <el-tag v-if="templateGroup.status==='3'" effect="dark" size="small" type="danger">未通过</el-tag>
                <el-tag v-if="templateGroup.status==='2'" effect="dark" size="small" type="success">已发布</el-tag>
                <el-tag v-if="templateGroup.status==='1'" effect="dark" size="small" type="warning">审核中</el-tag>
                <el-tag v-if="templateGroup.status==='0'" effect="dark" size="small" type="info">待审批</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-card>
    </div>
    <div class="page-box" v-show="activeTab === 'allTemplateGroup'">
      <el-button class="previous"
                 type="success" icon="el-icon-arrow-left" circle
                 @click="prePageTemplateGroup"></el-button>
      <el-button class="next"
                 type="success" icon="el-icon-arrow-right" circle
                 @click="nextPageTemplateGroup"></el-button>
    </div>
    <el-dialog class="edit-template-group"
               :title="title"
               :visible.sync="showEditTemplateGroupDialog"
               width="500px" :before-close="handleClose">
      <el-form :model="curTemplateGroup" label-width="110px" label-position="right" :rules="rules"
               ref="templateGroupForm">
        <el-form-item label="头像" prop="avatar">
          <file-upload class="avatar-uploader" :action="imageAction" :showLoading="true"
                       :maxSize="maxSize" @success="handleUploadSuccess"
                       :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']">
            <img v-if="curTemplateGroup.avatar" :src="curTemplateGroup.avatar" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </file-upload>
        </el-form-item>
        <el-form-item label="模板群聊名称" prop="groupName">
          <el-input v-model="curTemplateGroup.groupName" autocomplete="off" clearable></el-input>
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="curTemplateGroup.description" autocomplete="off" clearable></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleClose()">取 消</el-button>
        <el-button type="primary" @click="handleSubmit()">确 定</el-button>
        <el-button v-if="curTemplateGroup.status==='1'" type="info" @click="withdrawalOfApproval()">撤回审批</el-button>
        <el-button v-if="curTemplateGroup.status==='0'" type="success"
                   @click="handleSubmitForApproval()">提交审批</el-button>
		  </span>
    </el-dialog>
    <el-dialog class="edit-template-character"
               :title="curTemplateGroup.groupName"
               :visible.sync="showEditTemplateCharacterDialog"
               width="500px"
               :before-close="handleEditTemplateCharacterClose">
      <div class="template-group-avatar">
        <head-image class="head-image" :url="curTemplateGroup.avatar" :size="80"></head-image>
      </div>
      <div v-if="curTemplateGroup.isOwner" class="upload-avatar">
        <batch-file-upload class="avatar-uploader"
                           :action="imageAction"
                           :showLoading="true"
                           :maxSize="maxSize"
                           @success="handleUploadNewCharacterSuccess"
                           :uploadList="uploadList"
                           :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']">
          <i class="el-icon-plus avatar-uploader-icon"></i>
        </batch-file-upload>
      </div>
      <div class="template-character-box">
        <el-scrollbar style="height:360px;">
          <div class="template-character-item" v-for="(templateCharacter, index) in templateCharacters"
               :key="templateCharacter.id">
            <div class="avatar-box">
              <head-image class="avatar-uploader" :size="45" :url="templateCharacter.avatar"></head-image>
            </div>
            <el-input
                class="name-input"
                type="text"
                placeholder="请输入内容"
                v-model="templateCharacter.name"
                maxlength="20"
                :disabled="!curTemplateGroup.isOwner"
                show-word-limit
            />
            <div class="status-tag">
              <el-tag class="tag" v-if="templateCharacter.status==='3'" effect="dark" size="small" type="danger">未通过
              </el-tag>
              <el-tag class="tag" v-if="templateCharacter.status==='2'" effect="dark" size="small" type="success">已发布
              </el-tag>
              <el-tag class="tag" v-if="templateCharacter.status==='1'" effect="dark" size="small" type="warning">审核中
              </el-tag>
              <el-tag class="tag" v-if="templateCharacter.status==='0'" effect="dark" size="small" type="info">待审批
              </el-tag>
            </div>
            <el-button class="edit-character-avatar" type="warning" icon="el-icon-orange" circle
                       @click="openCharacterAvatarDialog(templateCharacter)"></el-button>
            <el-button v-if="curTemplateGroup.isOwner" class="delete-button"
                       type="danger" icon="el-icon-delete" circle
                       @click="deleteTemplateCharacter(templateCharacter, index)"></el-button>
          </div>
        </el-scrollbar>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleEditTemplateCharacterClose()">取 消</el-button>
        <el-button v-if="curTemplateGroup.isOwner" type="primary"
                   @click="handleTemplateCharacterSubmit()">确 定</el-button>
        <el-button v-if="curTemplateGroup.isOwner && curTemplateGroup.status==='1'" type="info"
                   @click="withdrawalOfApproval()">撤回审批</el-button>
        <el-button v-if="curTemplateGroup.isOwner && curTemplateGroup.status==='0'" type="success"
                   @click="handleSubmitForApproval()">提交审批</el-button>
		  </span>
    </el-dialog>
    <el-dialog class="edit-character-avatar-dialog"
               :title="curTemplateCharacter.name"
               :visible.sync="showEditCharacterAvatarDialog"
               width="500px"
               :before-close="handleEditCharacterAvatarClose">
      <div class="template-character-avatar">
        <head-image class="head-image" :url="curTemplateCharacter.avatar" :size="80"></head-image>
      </div>
      <div v-if="curTemplateCharacter.isOwner" class="upload-avatar">
        <batch-file-upload class="avatar-uploader"
                           :action="imageAction"
                           :showLoading="true"
                           :maxSize="maxSize"
                           @success="handleUploadNewAvatarSuccess"
                           :uploadList="uploadList"
                           :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']">
          <i class="el-icon-plus avatar-uploader-icon"></i>
        </batch-file-upload>
      </div>
      <div class="character-avatar-box">
        <el-scrollbar style="height:360px;">
          <div class="character-avatar-item" v-for="(characterAvatar, index) in characterAvatars"
               :key="characterAvatar.id">
            <div class="avatar-box">
              <head-image class="avatar-uploader" :size="45" :url="characterAvatar.avatar"></head-image>
            </div>
            <el-input
                class="name-input"
                type="text"
                placeholder="请输入内容"
                v-model="characterAvatar.name"
                maxlength="20"
                :disabled="!curTemplateCharacter.isOwner"
                show-word-limit
            />
<!--            <el-select class="select-item" v-model="characterAvatar.level" placeholder="请选择"
                       :disabled="!curTemplateCharacter.isOwner">
              <el-option
                  v-for="item in levelOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
              </el-option>
            </el-select>-->
            <el-input-number style="width: 80px;margin-left: 20px;" controls-position="right" v-model="characterAvatar.level" :min="0" :max="10"></el-input-number>
            <div class="status-tag">
              <el-tag class="tag" v-if="characterAvatar.status==='3'" effect="dark" size="small" type="danger">未通过
              </el-tag>
              <el-tag class="tag" v-if="characterAvatar.status==='2'" effect="dark" size="small" type="success">已发布
              </el-tag>
              <el-tag class="tag" v-if="characterAvatar.status==='1'" effect="dark" size="small" type="warning">审核中
              </el-tag>
              <el-tag class="tag" v-if="characterAvatar.status==='0'" effect="dark" size="small" type="info">待审批
              </el-tag>
            </div>
            <el-button  v-if="curTemplateCharacter.isOwner" class="delete-button"
                       type="danger" icon="el-icon-delete" circle
                       @click="deleteCharacterAvatar(characterAvatar, index)"></el-button>
          </div>
        </el-scrollbar>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleEditCharacterAvatarClose()">取 消</el-button>
        <el-button v-if="curTemplateCharacter.isOwner" type="primary"
                   @click="handleCharacterAvatarsSubmit()">确 定</el-button>
        <el-button v-if="curTemplateCharacter.isOwner && curTemplateCharacter.status==='2'" type="info"
                   @click="withdrawalOfApprovalForAvatar()">撤回审批</el-button>
        <el-button v-if="curTemplateCharacter.isOwner && curTemplateCharacter.status==='2'" type="success"
                   @click="handleAvatarsSubmitForApproval()">提交审批</el-button>
		  </span>
    </el-dialog>
  </div>
</template>

<script>
import HeadImage from "@/components/common/HeadImage";
import FileUpload from "@/components/common/FileUpload";
import BatchFileUpload from "@/components/common/BatchFileUpload";
import TemplateCharacterItem from "@/components/group/TemplateCharacterItem";

export default {
  name: "TemplateGroup",
  components: {
    HeadImage,
    FileUpload,
    TemplateCharacterItem,
    BatchFileUpload
  },
  data() {
    return {
      templateGroups: [],
      templateCharacters: [],
      characterAvatars: [],
      showEditTemplateGroupDialog: false,
      showEditTemplateCharacterDialog: false,
      showEditCharacterAvatarDialog: false,
      curTemplateGroup: {},
      curTemplateCharacter: {},
      rules: {
        groupName: [{required: true, message: '请输入模板群聊名称', trigger: 'blur'},
          {min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur'}],
        avatar: [{required: true, message: '请上传群聊头像', trigger: 'blur'}]
      },
      maxSize: 5 * 1024 * 1024,
      title: "编辑模板群聊",
      uploadList: [],
      levelOptions: [{value: 0, label: '0级'}, {value: 1, label: '1级'}],
      page: {
        pageNo: 1,
        pageSize: 10,
      },
      activeTab: 'templateGroup'
    }
  },
  created() {
    this.queryMyTemplateGroups()
  },
  methods: {
    queryMyTemplateGroups() {
      this.$http({
        url: "/templateGroup/findMyTemplateGroups",
        method: "get"
      }).then((data) => {
        this.templateGroups = data;
      })
    },
    queryAllTemplateGroups() {
      this.$http({
        url: `/templateGroup/findAllTemplateGroups?pageNo=${this.page.pageNo}&pageSize=${this.page.pageSize}`,
        method: "get"
      }).then((data) => {
        if (data.length !== 0) {
          this.templateGroups = data;
        } else {
          this.$message.warning("已经是最后一页");
          if (this.page.pageNo !== 1) {
            this.page.pageNo = this.page.pageNo - 1;
          }
        }
      })
    },
    handleUploadSuccess(data) {
      this.curTemplateGroup.avatar = data.originUrl;
      this.$forceUpdate();
    },
    editTemplateGroup(templateGroup) {
      this.curTemplateGroup = templateGroup;
      this.title = "编辑模板群聊";
      this.showEditTemplateGroupDialog = true;
    },
    handleClose() {
      this.curTemplateGroup = {};
      this.showEditTemplateGroupDialog = false;
    },
    handleSubmit() {
      if (this.curTemplateGroup.status === '1') {
        this.$message.warning("审核中的模板群聊不能编辑");
        return false;
      }
      this.$refs['templateGroupForm'].validate((valid) => {
        if (!valid) {
          return false;
        }
        this.$http({
          url: "/templateGroup/addOrModify",
          method: "post",
          data: this.curTemplateGroup
        }).then(() => {
          this.$message.success("操作成功");
          this.queryMyTemplateGroups();
        }).finally(() => {
          this.handleClose();
        })
      })
    },
    addTemplateGroup() {
      this.title = "新增模板群聊";
      this.showEditTemplateGroupDialog = true;
    },
    deleteTemplateGroup(templateGroup) {
      this.$confirm('确认要删除当前模板群聊吗?', '请确认?', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: `/templateGroup/delete/${templateGroup.id}`,
          method: 'delete'
        }).then(() => {
          this.$message.success("删除成功");
          this.queryMyTemplateGroups();
        });
      })
    },
    editTemplateCharacter(templateGroup) {
      this.curTemplateGroup = templateGroup;
      this.queryTemplateCharacter(templateGroup);
      this.showEditTemplateCharacterDialog = true;
    },
    handleEditTemplateCharacterClose() {
      this.curTemplateGroup = {};
      this.templateCharacters = [];
      this.showEditTemplateCharacterDialog = false;
    },
    handleEditCharacterAvatarClose() {
      this.curTemplateCharacter = {};
      this.characterAvatars = [];
      this.showEditCharacterAvatarDialog = false;
    },
    queryTemplateCharacter(templateGroup) {
      this.$http({
        url: `/templateCharacter/list/${templateGroup.id}`,
        method: 'get'
      }).then((data) => {
        this.templateCharacters = data;
      });
    },
    deleteTemplateCharacter(templateCharacter, index) {
      this.$confirm('确认要删除当前模板人物吗?', '请确认?', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        if (templateCharacter.id !== null && templateCharacter.id !== undefined) {
          this.$http({
            url: `/templateCharacter/delete/${templateCharacter.id}`,
            method: 'delete'
          }).then(() => {
            this.templateCharacters.splice(index, 1);
            this.$message.success("删除成功");
            this.queryMyTemplateGroups();
          });
        } else {
          this.templateCharacters.splice(index, 1);
          this.$message.success("删除成功");
        }
      })
    },
    handleTemplateCharacterSubmit() {
      let submitVo = this.curTemplateGroup;
      if (submitVo.status === '1') {
        this.$message.warning("审核中的数据不能编辑");
        return false;
      }
      submitVo.templateCharacterVOList = this.templateCharacters;
      this.$http({
        url: "/templateCharacter/addOrModifyTemplateCharacters",
        method: 'post',
        data: submitVo
      }).then((data) => {
        this.$message.success("操作成功");
        this.queryMyTemplateGroups();
      }).finally(() => {
        this.handleEditTemplateCharacterClose();
      })
    },
    handleUploadNewCharacterSuccess(data) {
      this.templateCharacters.push({avatar: data.originUrl, name: data.name, status: '0'});
    },
    handleUploadNewAvatarSuccess(data) {
      this.characterAvatars.push({
        avatar: data.originUrl, templateCharacterName: this.curTemplateCharacter.name,
        name: data.name, status: '0', level: 0
      });
    },
    handleSubmitForApproval() {
      if (this.curTemplateGroup.status === '1') {
        this.$message.warning("数据已在审核中，请勿重复提交");
        return false;
      }
      this.$http({
        url: "/templateGroup/submitForApproval",
        method: 'post',
        data: {id: this.curTemplateGroup.id}
      }).then((data) => {
        this.$message.success("操作成功");
        this.handleClose();
        this.handleEditTemplateCharacterClose();
        this.queryMyTemplateGroups();
      })
    },
    withdrawalOfApproval() {
      if (this.curTemplateGroup.status !== '1') {
        this.$message.warning("当前数据状态不是审核中");
        return false;
      }
      this.$http({
        url: "/templateGroup/withdrawalOfApproval",
        method: 'post',
        data: {id: this.curTemplateGroup.id}
      }).then((data) => {
        this.$message.success("操作成功");
        this.handleClose();
        this.handleEditTemplateCharacterClose();
        this.queryMyTemplateGroups();
      })
    },
    openCharacterAvatarDialog(templateCharacter) {
      this.curTemplateCharacter = templateCharacter;
      this.queryAllCharacterAvatar(templateCharacter);
      this.showEditCharacterAvatarDialog = true;
    },
    toReviewPage() {
      this.$router.push("/home/square/review");
    },
    queryAllCharacterAvatar(templateCharacter) {
      this.$http({
        url: '/characterAvatar/listAll/' + templateCharacter.id,
        method: 'get'
      }).then((data) => {
        this.characterAvatars = data;
      });
    },
    deleteCharacterAvatar(characterAvatar, index) {
      this.$confirm('确认要删除当前模板人物头像吗?', '请确认?', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        if (characterAvatar.id !== null && characterAvatar.id !== undefined) {
          this.$http({
            url: `/characterAvatar/delete/${characterAvatar.id}`,
            method: 'delete'
          }).then(() => {
            this.characterAvatars.splice(index, 1);
            this.$message.success("删除成功");
          });
        } else {
          this.characterAvatars.splice(index, 1);
          this.$message.success("删除成功");
        }
      })
    },
    handleCharacterAvatarsSubmit() {
      let submitVo = this.curTemplateCharacter;
      if (submitVo.status === '1') {
        this.$message.warning("审核中的数据不能编辑");
        return false;
      }
      if (this.characterAvatars.length === 0) {
        this.$message.warning("请上传人物头像");
        return false;
      }
      submitVo.characterAvatarVOList = this.characterAvatars;
      this.$http({
        url: "/characterAvatar/addOrModify",
        method: 'post',
        data: submitVo
      }).then((data) => {
        this.$message.success("操作成功");
      }).finally(() => {
        this.handleEditCharacterAvatarClose();
      })
    },
    handleAvatarsSubmitForApproval() {
      if (this.curTemplateCharacter.status !== '2') {
        this.$message.warning("请待当前模板人物审核通过");
        return false;
      }
      if (this.characterAvatars.length === 0) {
        this.$message.warning("请上传人物头像");
        return false;
      }
      const count = this.characterAvatars.reduce((counter, obj) => {
        if (obj.status === '1') counter += 1
        return counter;
      }, 0);
      if (count === this.characterAvatars.length) {
        this.$message.warning("正在审核中请勿重复提交");
        return false;
      }

      this.$http({
        url: "/characterAvatar/submitForApproval",
        method: 'post',
        data: {id: this.curTemplateCharacter.id}
      }).then((data) => {
        this.$message.success("操作成功");
        this.handleEditCharacterAvatarClose();
      })
    },
    withdrawalOfApprovalForAvatar() {
      if (this.characterAvatars.length === 0) {
        return false;
      }
      const count = this.characterAvatars.reduce((counter, obj) => {
        if (obj.status === '0') counter += 1
        return counter;
      }, 0);
      if (count === this.characterAvatars.length) {
        return false;
      }
      this.$http({
        url: "/characterAvatar/withdrawalOfApproval",
        method: 'post',
        data: {id: this.curTemplateCharacter.id}
      }).then((data) => {
        this.$message.success("操作成功");
        this.handleEditCharacterAvatarClose();
      })
    },
    handleTabClick(tab, event) {
      if (tab.name === 'templateGroup') {
        this.activeTab = 'templateGroup';
        this.queryMyTemplateGroups();
      } else if (tab.name === 'allTemplateGroup') {
        this.activeTab = 'allTemplateGroup';
        this.page.pageNo = 1;
        this.queryAllTemplateGroups();
      }
    },
    prePageTemplateGroup() {
      if (this.page.pageNo === 1) {
        return false
      }
      this.page.pageNo = this.page.pageNo - 1;
      this.queryAllTemplateGroups();
    },
    nextPageTemplateGroup() {
      this.page.pageNo = this.page.pageNo + 1;
      this.queryAllTemplateGroups();
    }
  },
  computed: {
    imageAction() {
      return `/image/upload`;
    },
    isAdmin() {
      return this.$store.state.userStore.userInfo.id === 1;
    }
  }
}
</script>

<style lang="scss">
.buttons {
  margin: 0px 10px;
  text-align: left;
}

.tab-box {
  margin-top: 10px;
  margin-left: 10px;
}

.template-group-list {
  /* grid布局 两端对齐,最后一行左对齐*/
  display: grid;
  grid-template-columns: repeat(2,1fr);
  gap: 10px;

  .box-card {

    .header {

      .operate-button {
        margin: 0 5px;
        float: right;
        padding: 3px 0;
        width: 32px;
        height: 32px
      }
    }

    .head-image {
      float: left;
      display: inline-block;
      margin-right: 20px;
    }

    .info {
      float: left;
      display: inline-block;
      width: 240px;
    }
  }
}

.page-box {
  margin-top: 10px;
  width: 100%;
  display: flex;
  justify-content: center;
}

.edit-template-group {

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

.edit-template-character {

  .template-group-avatar {
    /*转为flex弹性盒布局*/
    display: flex;
    /*主轴上的对齐方式为居中*/
    justify-content: center;
    /*交叉轴上对齐方式为居中*/
    align-items: center;
  }

  .upload-avatar {
    width: 45px;
    height: 45px;
    line-height: 45px;
    border: #cccccc solid 1px;
    font-size: 25px;
    cursor: pointer;
    box-sizing: border-box;
    margin-left: 15px;
  }

  .template-character-box {

    .template-character-item {
      height: 45px;
      display: flex;
      position: relative;
      padding-left: 15px;
      align-items: center;
      padding-right: 5px;
      background-color: var(--bg-color);
      white-space: nowrap;
      margin: 10px 0;

      .avatar-box {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 45px;
        height: 45px;
      }

      .name-input {
        margin-left: 10px;
        width: 150px;
        height: 45px;
      }

      .status-tag {
        margin-left: 10px;
        height: 45px;
        line-height: 45px;
      }

      .edit-character-avatar {
        margin-left: 20px;
      }

      .delete-button {
        margin-left: 20px;
      }
    }

  }
}

.edit-character-avatar-dialog {

  .template-character-avatar {
    /*转为flex弹性盒布局*/
    display: flex;
    /*主轴上的对齐方式为居中*/
    justify-content: center;
    /*交叉轴上对齐方式为居中*/
    align-items: center;
  }

  .upload-avatar {
    width: 45px;
    height: 45px;
    line-height: 45px;
    border: #cccccc solid 1px;
    font-size: 25px;
    cursor: pointer;
    box-sizing: border-box;
    margin-left: 15px;
  }

  .character-avatar-box {

    .character-avatar-item {
      height: 45px;
      display: flex;
      position: relative;
      padding-left: 15px;
      align-items: center;
      padding-right: 5px;
      background-color: var(--bg-color);
      white-space: nowrap;
      margin: 10px 0;

      .avatar-box {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 45px;
        height: 45px;
      }

      .name-input {
        margin-left: 10px;
        width: 150px;
        height: 45px;
      }

      .select-item {
        margin-left: 10px;
        width: 70px;
      }

      .status-tag {
        margin-left: 10px;
        height: 45px;
        line-height: 45px;
      }

      .delete-button {
        margin-left: 20px;
      }
    }
  }
}
</style>