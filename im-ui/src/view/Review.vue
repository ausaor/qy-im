<template>
  <div>
    <div class="go-back">
      <el-button plain icon="el-icon-arrow-left" @click="goBack">返回</el-button>
    </div>
    <div class="tab-box">
      <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
        <el-tab-pane label="模板群聊" name="templateGroup"></el-tab-pane>
        <el-tab-pane label="模板头像" name="characterAvatar"></el-tab-pane>
      </el-tabs>
    </div>
    <div class="template-group-list" v-if="activeTab === 'templateGroup'">
      <el-card shadow="always" class="box-card" v-for="(templateGroup,index) in templateGroups" :key="index">
        <div slot="header" class="header">
          <span>{{ templateGroup.groupName }}</span>
          <el-button class="review-button" @click="reviewTemplateGroup(templateGroup)" type="primary" size="small" >审核<i class="el-icon-edit el-icon--right"></i></el-button>
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
    <div class="character-avatar-list" v-if="activeTab==='characterAvatar'">
      <el-card shadow="always" class="box-card" v-for="(templateCharacter,index) in templateCharactersAvatars" :key="index">
        <div slot="header" class="header">
          <span>{{ templateCharacter.name }}</span>
          <el-button class="review-button" @click="submitAvatarAuditConclusion(templateCharacter, 'pass')" type="success" size="small" >
            通过<i class="el-icon-check el-icon--right"></i></el-button>
          <el-button class="review-button" @click="submitAvatarAuditConclusion(templateCharacter, 'noPass')" type="danger" size="small" >
            不通过<i class="el-icon-close el-icon--right"></i></el-button>
        </div>
        <div class="head-image-box">
          <head-image class="head-image" :url="templateCharacter.avatar" :size="80"></head-image>
        </div>
        <div class="character-avatar-box">
          <div class="character-avatar-item" v-for="(characterAvatar) in templateCharacter.characterAvatarVOList" :key="characterAvatar.id">
            <div class="avatar-box">
              <head-image class="avatar-uploader" :size="45" :url="characterAvatar.avatar"></head-image>
              <span class="character-name">
                {{characterAvatar.name}}
              </span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    <el-dialog class="review-dialog"
               :title="curTemplateGroup.groupName"
               :visible.sync="showTemplateCharacterDialog"
               width="400px"
               :before-close="handleTemplateCharacterClose">
      <div class="template-group-avatar">
        <head-image class="head-image" :url="curTemplateGroup.avatar" :size="80"></head-image>
      </div>
      <div class="template-character-box">
        <el-scrollbar style="height:360px;">
          <div class="template-character-item" v-for="(templateCharacter) in templateCharacters" :key="templateCharacter.id">
            <div class="avatar-box">
              <head-image class="avatar-uploader" :size="45" :url="templateCharacter.avatar"></head-image>
            </div>
            <span class="character-name">
              {{templateCharacter.name}}
            </span>
            <div class="status-tag">
              <el-tag class="tag" v-if="templateCharacter.status==='3'" effect="dark" size="small" type="danger">未通过</el-tag>
              <el-tag class="tag" v-if="templateCharacter.status==='2'" effect="dark" size="small" type="success">已发布</el-tag>
              <el-tag class="tag" v-if="templateCharacter.status==='1'" effect="dark" size="small" type="warning">审核中</el-tag>
              <el-tag class="tag" v-if="templateCharacter.status==='0'" effect="dark" size="small" type="info">待审批</el-tag>
            </div>
          </div>
        </el-scrollbar>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="success" @click="submitAuditConclusion('pass')">通过</el-button>
        <el-button type="danger" @click="submitAuditConclusion('noPass')">不通过</el-button>
		  </span>
    </el-dialog>
  </div>
</template>

<script>
import HeadImage from "@/components/common/HeadImage";

export default {
  name: "Review",
  components: {
    HeadImage,
  },
  data() {
    return {
      templateGroups: [],
      templateCharacters: [],
      templateCharactersAvatars: [],
      curTemplateGroup: {},
      showTemplateCharacterDialog: false,
      activeTab: 'templateGroup',
    }
  },
  created() {
    this.findReviewingTemplateGroups();
  },
  methods: {
    findReviewingTemplateGroups() {
      this.$http({
        url: "/templateGroup/findReviewingTemplateGroups",
        method: "get",
      }).then((data) => {
        this.templateGroups = data;
      })
    },
    findReviewingCharacterAvatar() {
      this.$http({
        url: "/characterAvatar/findReviewingCharacterAvatar",
        method: "get",
      }).then((data) => {
        this.templateCharactersAvatars = data;
      })
    },
    handleTemplateCharacterClose() {
      this.curTemplateGroup = {};
      this.showTemplateCharacterDialog = false;
    },
    reviewTemplateGroup(templateGroup) {
      this.curTemplateGroup = templateGroup;
      this.queryTemplateCharacter(templateGroup);
      this.showTemplateCharacterDialog = true;
    },
    queryTemplateCharacter(templateGroup) {
      this.$http({
        url: `/templateCharacter/list/${templateGroup.id}`,
        method: 'get'
      }).then((data) => {
        this.templateCharacters = data;
      });
    },
    submitAuditConclusion(conclusion) {
      //console.log("conclusion", conclusion)
      let submitVo = {
        comments: conclusion,
        templateGroupId: this.curTemplateGroup.id
      }
      this.$http({
        url: "/templateGroup/submitAuditConclusion",
        method: "post",
        data: submitVo
      }).then(()=>{
        this.$message.success("操作成功");
        this.findReviewingTemplateGroups();
      }).finally(() =>{
        this.handleTemplateCharacterClose();
      })
    },
    submitAvatarAuditConclusion(templateCharacter, conclusion) {
      let submitVo = {
        comments: conclusion,
        templateCharacterId: templateCharacter.id
      }
      this.$http({
        url: "/characterAvatar/submitAuditConclusion",
        method: "post",
        data: submitVo
      }).then(()=>{
        this.$message.success("操作成功");
        this.findReviewingCharacterAvatar();
      }).finally(() =>{
      })
    },
    goBack() {
      this.$router.push("/home/square/templateGroup");
    },
    handleTabClick(tab, event) {
      //console.log(tab.name);
      if (tab.name === 'templateGroup') {
        this.findReviewingTemplateGroups();
      } else if(tab.name === 'characterAvatar') {
        this.findReviewingCharacterAvatar();
      }
    }
  }
}
</script>

<style lang="scss">
.go-back {
  float: left;
  margin-left: 10px;
}

.tab-box {
  clear: both;
  margin-left: 10px;
}

.template-group-list {
  clear: both;

  .box-card {
    margin: 10px 10px;
    width: 48%;
    float: left;

    .header {

      .review-button {
        margin: 0 5px;
        float: right;
        padding: 3px 0;
        width: 64px;
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

.character-avatar-list {
  .box-card {
    margin: 10px 10px;
    width: 48%;
    height: 360px;
    float: left;

    .header {
      .review-button {
        margin: 0 5px;
        float: right;
        padding: 3px 0;
        width: 64px;
        height: 32px
      }
    }

    .head-image-box {
      float: left;
      display: inline-block;
      margin-right: 20px;

      .head-image {

      }
    }

    .character-avatar-box {
      height: 180px;

      .character-avatar-item {
        width: 14%;
        border: 1px #6CC6CB solid;
        float: left;
        margin-top: 5px;
        margin-left: 10px;
      }
    }

    .buttons {
      text-align: right;
      clear: both;
    }
  }
}

.review-dialog {

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

      .character-name {
        margin-left: 10px;
        width: 150px;
        height: 45px;
        line-height: 45px;
      }

      .status-tag {
        margin-left: 10px;
        height: 45px;
        line-height: 45px;
      }
    }

  }
}
</style>