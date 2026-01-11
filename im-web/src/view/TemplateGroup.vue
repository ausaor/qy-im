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
        <el-tab-pane label="我的绑定角色" name="myCharacters">
        </el-tab-pane>
      </el-tabs>
    </div>
    <div class="template-group-list" v-show="activeTab === 'templateGroup' || activeTab === 'allTemplateGroup'">
      <el-card shadow="always" class="box-card" v-for="(templateGroup,index) in templateGroups" :key="index">
        <div slot="header" class="header" style="display: flex;justify-content: space-between;">
          <div>{{ templateGroup.groupName }}</div>
          <div style="display: flex;justify-content: right;">
            <el-button v-if="templateGroup.isOwner" class="operate-button"
                       type="primary" icon="el-icon-edit" circle size="mini"
                       @click="editTemplateGroup(templateGroup)"></el-button>
            <el-button v-if="templateGroup.isOwner" class="operate-button"
                       type="danger" icon="el-icon-delete" circle size="mini"
                       @click="deleteTemplateGroup(templateGroup)"></el-button>
            <div style="position: relative;">
              <div v-if="characterListNotifyCount(templateGroup.characterIds) > 0" class="group-template-notify">{{characterListNotifyCount(templateGroup.characterIds)}}</div>
              <el-button class="operate-button"
                         icon="el-icon-user-solid" circle size="mini"
                         @click="editTemplateCharacter(templateGroup)"></el-button>
            </div>
            <el-button class="operate-button" circle size="mini" icon="icon iconfont icon-minganci"
                       @click="openCharacterWordDialog({templateGroup: templateGroup})">
            </el-button>
            <el-button class="operate-button" circle icon="icon iconfont icon-biaoqing" size="mini"
                       @click="openCharacterEmoDialog({templateGroup: templateGroup})">
            </el-button>
            <div style="position: relative;">
              <div v-if="groupTemplateNotifyCount(templateGroup.id) > 0" class="group-template-notify">{{groupTemplateNotifyCount(templateGroup.id)}}</div>
              <el-button class="operate-button template-group-space" circle icon="icon iconfont icon-shejiaotubiao-40" size="mini"
                         @click="openGroupTemplateSpaceDialog(templateGroup)">
              </el-button>
            </div>
            <el-button class="operate-button template-group-characters-space" circle icon="icon iconfont icon-shejiaotubiao-40" size="mini"
                       @click="openGroupTemplateCharactersSpaceDialog(templateGroup)">
            </el-button>
          </div>
        </div>
        <div>
          <head-image class="head-image" :url="templateGroup.avatar" :size="80"></head-image>
          <div class="info">
            <el-descriptions title="群聊信息" :column="2">
              <el-descriptions-item label="角色" span="2">{{ templateGroup.count }}</el-descriptions-item>
              <el-descriptions-item label="创建人" span="2">{{ templateGroup.creator }}</el-descriptions-item>
              <el-descriptions-item label="创建时间" span="2">{{ templateGroup.createTime }}</el-descriptions-item>
              <el-descriptions-item label="更新时间" span="2">{{ templateGroup.updateTime }}</el-descriptions-item>
              <el-descriptions-item label="状态" span="2">
                <el-tag v-if="templateGroup.status==='3'" effect="dark" size="small" type="danger">未通过</el-tag>
                <el-tag v-if="templateGroup.status==='2'" effect="dark" size="small" type="success">已发布</el-tag>
                <el-tag v-if="templateGroup.status==='1'" effect="dark" size="small" type="warning">审核中</el-tag>
                <el-tag v-if="templateGroup.status==='0'" effect="dark" size="small" type="info">编辑中</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-card>
    </div>
    <div class="my-characters" v-show="activeTab === 'myCharacters'">
      <div class="character-item" v-for="(item,index) in myCharacters" :key="index">
        <div class="character-avatar">
          <head-image :name="item.characterName" :url="item.characterAvatar" :size="60"></head-image>
          <div class="character-name">{{ item.characterName }}</div>
        </div>
        <div class="character-content">
          <div class="content-item" @click="openCharacterSpaceDialog(item.character)" style="position: relative;">
            <div v-if="characterNotifyCount(item.characterId) > 0" class="my-character-notify">{{characterNotifyCount(item.characterId)}}</div>
            <svg class="icon svg-icon" aria-hidden="true">
              <use xlink:href="#icon-kongjian2"></use>
            </svg>
          </div>
          <div class="content-item">
            <svg class="icon svg-icon" aria-hidden="true" @click="openCharacterWordDialog({templateGroup: {id: item.character.templateGroupId}, character: item.character })">
              <use xlink:href="#icon-minganci"></use>
            </svg>
          </div>
          <div class="content-item" @click="openCharacterEmoDialog({templateGroup: {id: item.character.templateGroupId}, character: item.character })">
            <svg class="icon svg-icon" aria-hidden="true">
              <use xlink:href="#icon-biaoqing"></use>
            </svg>
          </div>
          <div class="content-item">
            <svg class="icon svg-icon" aria-hidden="true" @click="openCharacterAvatarDialog(item.character)">
              <use xlink:href="#icon-person"></use>
            </svg>
          </div>
          <div class="content-item">
            <svg class="icon svg-icon" aria-hidden="true">
              <use xlink:href="#icon-Music"></use>
            </svg>
          </div>
        </div>
      </div>
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
               width="540px"
               v-dialogDrag
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
              <head-image class="avatar-uploader" :size="36" :url="templateCharacter.avatar"></head-image>
            </div>
            <div style="line-height: 45px;">
              <el-input
                  class="name-input"
                  type="text"
                  placeholder="请输入内容"
                  v-model="templateCharacter.name"
                  maxlength="20"
                  :disabled="!curTemplateGroup.isOwner"
                  show-word-limit
                  size="small"
              />
            </div>
            <div class="status-tag">
              <el-tag class="tag" v-if="templateCharacter.status==='3'" effect="dark" size="mini" type="danger">未通过
              </el-tag>
              <el-tag class="tag" v-if="templateCharacter.status==='2'" effect="dark" size="mini" type="success">已发布
              </el-tag>
              <el-tag class="tag" v-if="templateCharacter.status==='1'" effect="dark" size="mini" type="warning">审核中
              </el-tag>
              <el-tag class="tag" v-if="templateCharacter.status==='0'" effect="dark" size="mini" type="info">待审批
              </el-tag>
            </div>
            <el-button class="edit-character-avatar" type="warning" icon="el-icon-user" circle
                       @click="openCharacterAvatarDialog(templateCharacter)" size="mini"></el-button>
            <el-button style="margin-left: 8px;" type="primary" circle size="mini"
                       @click="openCharacterWordDialog({templateGroup: curTemplateGroup, character: templateCharacter})">词</el-button>
            <el-button style="margin-left: 8px;" icon="icon iconfont icon-biaoqing" circle size="mini"
                       @click="openCharacterEmoDialog({templateGroup: curTemplateGroup, character: templateCharacter})"></el-button>
            <div style="position: relative;margin-left: 8px;margin-right: 8px;">
              <div v-if="characterNotifyCount(templateCharacter.id)>0" class="character-notify">{{characterNotifyCount(templateCharacter.id)}}</div>
              <el-button class="edit-character-space"
                         icon="icon iconfont icon-shejiaotubiao-40" circle size="mini" @click="openCharacterSpaceDialog(templateCharacter)"></el-button>
            </div>
            <el-button class="edit-character-user" type="primary" icon="el-icon-user-solid" circle
                       @click="openCharacterUserDialog(templateCharacter)" size="mini"></el-button>
            <el-button v-if="curTemplateGroup.isOwner" class="delete-button"
                       type="danger" icon="el-icon-delete" circle size="mini"
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
              <head-image class="avatar-uploader" :size="36" :url="characterAvatar.avatar"></head-image>
            </div>
            <div style="line-height: 45px;">
              <el-input
                  class="name-input"
                  type="text"
                  placeholder="请输入内容"
                  v-model="characterAvatar.name"
                  maxlength="20"
                  :disabled="!curTemplateCharacter.isOwner"
                  show-word-limit
                  size="small"
              />
            </div>

<!--            <el-select class="select-item" v-model="characterAvatar.level" placeholder="请选择"
                       :disabled="!curTemplateCharacter.isOwner">
              <el-option
                  v-for="item in levelOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
              </el-option>
            </el-select>-->
            <el-input-number style="width: 80px;margin-left: 20px;" controls-position="right" v-model="characterAvatar.level" :min="0" :max="10" size="mini"></el-input-number>
            <div class="status-tag">
              <el-tag class="tag" v-if="characterAvatar.status==='3'" effect="dark" size="mini" type="danger">未通过
              </el-tag>
              <el-tag class="tag" v-if="characterAvatar.status==='2'" effect="dark" size="mini" type="success">已发布
              </el-tag>
              <el-tag class="tag" v-if="characterAvatar.status==='1'" effect="dark" size="mini" type="warning">审核中
              </el-tag>
              <el-tag class="tag" v-if="characterAvatar.status==='0'" effect="dark" size="mini" type="info">编辑中
              </el-tag>
            </div>
            <el-button  v-if="curTemplateCharacter.isOwner" class="delete-button" size="mini"
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
    <el-dialog class="edit-character-word"
               :title="wordTitle"
               :visible.sync="showEditWordDialog"
               width="600px"
               :before-close="handleWordDialogClose">
      <el-scrollbar style="height:500px;">
        <el-form :model="dynamicValidateForm" ref="dynamicValidateForm" label-width="70px" class="character-word">
          <el-form-item
              v-for="(item, index) in dynamicValidateForm.words"
              :label="'台词' + (index + 1)"
              :prop="'words.' + index + '.word'"
              :key="item.id || item.tempId"
              :rules="{required: true, message: '台词不能为空', trigger: 'blur'}"
          >
            <el-input v-model="item.word" :minlength="1" :maxlength="50" :show-word-limit="true" :clearable="true" :disabled="!curTemplateGroup.isOwner && !curTemplateCharacter.isOwner"></el-input>
            <el-button class="voice" v-if="item.voice" icon="el-icon-mic" circle style="color: orange;margin-left: 8px;position: relative;" @click="playWordVoice(item.voice)">
              <i v-if="curTemplateGroup.isOwner || curTemplateCharacter.isOwner" @click.stop="onDeleteVoice(index)" class="btn-remove el-icon-error"></i>
            </el-button>
            <file-upload v-else class="voice-uploader" :action="audioAction"
                         :showLoading="true" :maxSize="maxSize" @success="onUploadVoiceSuccess"
                         :fileTypes="['audio/mpeg', 'audio/mp3', 'audio/ape', 'audio/wav', 'audio/flac','audio/m4a', 'audio/kgm', 'audio/ncm', 'audio/mgg']">
              <i class="el-icon-upload"></i>
            </file-upload>
            <div style="margin-left: 8px;">
              <el-tag v-if="item.status==='3'" effect="dark" size="mini" type="danger">未通过</el-tag>
              <el-tag v-if="item.status==='2'" effect="dark" size="mini" type="success">已发布</el-tag>
              <el-tag v-if="item.status==='1'" effect="dark" size="mini" type="warning">审核中</el-tag>
              <el-tag v-if="item.status==='0'" effect="dark" size="mini" type="info">待发布</el-tag>
            </div>
            <el-button v-if="curTemplateGroup.isOwner || curTemplateCharacter.isOwner" :type="activeWordIndex === index ? 'success' : ''" icon="el-icon-check" circle
                       style="margin-left: 8px;" @click="chooseWord(index)"></el-button>
          </el-form-item>
        </el-form>
      </el-scrollbar>
      <div slot="footer" class="dialog-footer">
        <el-button type="danger"  @click.prevent="removeWord" v-if="curTemplateGroup.isOwner || curTemplateCharacter.isOwner">删除</el-button>
        <el-button type="primary" @click="submitWordForm('dynamicValidateForm')" v-if="curTemplateGroup.isOwner || curTemplateCharacter.isOwner">提交</el-button>
        <el-button type="success" @click="publishWord" v-if="curTemplateGroup.isOwner || curTemplateCharacter.isOwner">发布</el-button>
        <el-button @click="addWord" v-if="curTemplateGroup.isOwner || curTemplateCharacter.isOwner">新增</el-button>
      </div>
    </el-dialog>
    <el-dialog class="edit-character-emo"
               :title="wordTitle"
               :visible.sync="showEditEmoDialog"
               width="600px"
               :before-close="handleEmoDialogClose">
      <div style="height:500px;" class="character-emo-box">
        <div v-if="curTemplateGroup.isOwner || curTemplateCharacter.isOwner" class="upload-emo">
          <batch-file-upload class="emo-uploader"
                             :action="imageAction"
                             :showLoading="true"
                             :maxSize="maxSize"
                             @success="handleUploadCharacterEmoSuccess"
                             :uploadList="uploadEmoList"
                             :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']">
            <i class="el-icon-plus avatar-uploader-icon"></i>
          </batch-file-upload>
        </div>
        <div v-for="(item, index) in characterEmos" :key="index" class="character-emo-item">
          <img :src="item.url" class="emo-image">
          <i v-if="curTemplateGroup.isOwner" @click.stop="onDeleteEmo(index)" class="btn-remove el-icon-error"></i>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="saveEmo" v-if="curTemplateGroup.isOwner || curTemplateCharacter.isOwner">保存</el-button>
      </div>
    </el-dialog>
    <el-dialog class="edit-character-users"
               :title="characterUserTitle"
               :visible.sync="showCharacterUsersDialog"
               width="600px"
               :before-close="closeCharacterUsersDialog">
      <div class="character-users-content">
        <!-- 已绑定用户列表 -->
        <div class="bound-users-section">
          <h3>已绑定用户</h3>
          <div v-if="characterUsers.length > 0" class="user-list">
            <div v-for="user in characterUsers" :key="'bound-' + user.userId" class="user-item">
              <head-image :url="user.headImage" :name="user.userName" :size="40"></head-image>
              <span class="user-name">{{ user.userName }}</span>
              <el-button
                v-if="curTemplateCharacter.isOwner"
                type="danger"
                size="mini"
                @click="unBindCharacterUser(user)">
                解除
              </el-button>
            </div>
          </div>
          <div v-else class="empty-state">暂无已绑定用户</div>
        </div>

        <!-- 搜索用户区域 -->
        <div class="search-users-section">
          <h3>添加用户</h3>
          <div class="search-box">
            <el-input
              v-model="userNameParam"
              placeholder="输入用户名搜索"
              class="search-input"
              clearable
            >
              <el-button slot="append" icon="el-icon-search" @click="queryUsers"></el-button>
            </el-input>
          </div>

          <!-- 搜索结果用户列表 -->
          <div v-if="userList.length > 0" class="user-list">
            <div v-for="user in userList" :key="'search-' + user.id" class="user-item">
              <head-image :url="user.headImage" :name="user.userName" :size="40"></head-image>
              <span class="user-name">{{ user.userName }}</span>
              <el-button v-if="curTemplateCharacter.isOwner"
                type="primary"
                size="mini"
                @click="bindCharacterUser(user.id)"
                :disabled="isUserAlreadyBound(user.id)">
                {{ isUserAlreadyBound(user.id) ? '已绑定' : '绑定' }}
              </el-button>
            </div>
          </div>
          <div v-else class="empty-state">暂无搜索结果</div>
        </div>
      </div>
    </el-dialog>
    <drawer
        :visible="characterSpaceVisible"
        @close="closeDrawer"
        :width=60>
      <template v-slot:header>
        <space-cover :name="spaceName" :show-add="section!=='groupTemplate&Characters' && isOwner"
                     @refresh="refreshTalkList" @add="handleShowAddTalk" @showTalkNotify="showTalkNotify"></space-cover>
      </template>
      <template v-slot:main>
        <talk-list ref="talkListRef" :category="'character'" :section="section" :character-id="characterId" :group-template-id="groupTemplateId"></talk-list>
      </template>
    </drawer>
    <talk-notify ref="talkNotifyRef"
                :category="'character'"
                 :section="section"
                :character-id="characterId"
                :group-template-id="groupTemplateId"></talk-notify>
  </div>
</template>

<script>
import HeadImage from "@/components/common/HeadImage";
import FileUpload from "@/components/common/FileUpload";
import BatchFileUpload from "@/components/common/BatchFileUpload";
import TemplateCharacterItem from "@/components/group/TemplateCharacterItem";
import SpaceCover from "@components/common/SpaceCover.vue";
import Drawer from "@components/common/Drawer.vue";
import TalkList from "@components/talk/TalkList.vue";
import TalkNotify from "@components/talk/TalkNotify.vue";

export default {
  name: "TemplateGroup",
  components: {
    HeadImage,
    FileUpload,
    TemplateCharacterItem,
    BatchFileUpload,
    Drawer,
    SpaceCover,
    TalkList,
    TalkNotify,
  },
  data() {
    return {
      templateGroups: [],
      templateCharacters: [],
      characterAvatars: [],
      characterEmos: [],
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
      uploadEmoList: [],
      levelOptions: [{value: 0, label: '0级'}, {value: 1, label: '1级'}],
      page: {
        pageNo: 1,
        pageSize: 10,
      },
      activeTab: 'templateGroup',
      dynamicValidateForm: {
        words: [],
      },
      wordTitle: '',
      showEditWordDialog: false,
      showEditEmoDialog: false,
      activeWordIndex: -1,
      audio: null,
      audioSrc: '',
      characterSpaceVisible: false,
      section: '',
      spaceName: '角色空间',
      characterId: null,
      groupTemplateId: null,
      isOwner: false,
      characterUsers: [],
      userList: [],
      showCharacterUsersDialog: false,
      userNameParam: '',
      characterUserTitle: '',
      myCharacters: [],
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
      this.$confirm('确认要删除当前角色吗?', '请确认?', {
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
        avatar: data.originUrl,
        templateCharacterName: this.curTemplateCharacter.name,
        name: data.name, status: '0', level: 0
      });
    },
    handleUploadCharacterEmoSuccess(data) {
      this.characterEmos.push({
        url: data.originUrl,
        name: data.name,
      })
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
      this.$confirm('确认要删除当前角色头像吗?', '请确认?', {
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
        this.$message.warning("请待当前角色审核通过");
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
      } else if (tab.name === 'myCharacters') {
        this.activeTab = 'myCharacters';
        this.queryMyCharacters();
      }
    },
    queryMyCharacters() {
      this.$http({
        url: "/characterUser/getMyCharacters",
        method: 'get'
      }).then((data) => {
        this.myCharacters = data;
      });
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
    },
    openCharacterWordDialog({templateGroup, character} = {}) {
      this.curTemplateGroup = templateGroup;
      this.curTemplateCharacter = character ? character : {};
      if (character) {
        this.wordTitle = character.name
      } else {
        this.wordTitle = templateGroup.groupName;
      }
      let param = {
        templateGroupId: templateGroup.id,
        characterId: character?.id,
      }
      this.queryCharacterWord(param);
    },
    openCharacterEmoDialog({templateGroup, character} = {}) {
      this.curTemplateGroup = templateGroup;
      this.curTemplateCharacter = character ? character : {};
      if (character) {
        this.wordTitle = character.name
      } else {
        this.wordTitle = templateGroup.groupName;
      }
      let param = {
        templateGroupId: templateGroup.id,
        characterId: character?.id,
      }
      this.queryCharacterEmo(param);
    },
    openCharacterSpaceDialog(templateCharacter) {
      if (!templateCharacter) {
        this.$message.warning('角色信息为空');
        return
      }
      this.groupTemplateId = null;
      this.section = "character";
      this.characterId = templateCharacter.id;
      this.spaceName = templateCharacter.name + "•星空间";
      this.isOwner = templateCharacter.isOwner;
      this.characterSpaceVisible = true;
      // 使用 $nextTick 确保所有 props 已更新到子组件
      this.$nextTick(() => {
        this.refreshTalkList();
      })
      if (this.characterNotifyCount(templateCharacter.id) > 0) {
        this.readedCharacterTalkNotify(templateCharacter.id);
      }
      this.$store.commit("resetCharacterNotify", templateCharacter.id);
    },
    openGroupTemplateSpaceDialog(groupTemplate) {
      this.characterId = null;
      this.section = "groupTemplate";
      this.groupTemplateId = groupTemplate.id;
      this.spaceName = groupTemplate.groupName + "•星空间";
      this.isOwner = groupTemplate.isOwner;
      this.characterSpaceVisible = true;
      // 使用 $nextTick 确保所有 props 已更新到子组件
      this.$nextTick(() => {
        this.refreshTalkList();
      })
      if (this.groupTemplateNotifyCount(groupTemplate.id) > 0) {
        this.readedGroupTemplateTalkNotify(groupTemplate.id);
      }
      this.$store.commit("resetGroupTemplateNotify", groupTemplate.id);
    },
    openGroupTemplateCharactersSpaceDialog(groupTemplate) {
      this.characterId = null;
      this.section = "groupTemplate&Characters";
      this.groupTemplateId = groupTemplate.id;
      this.spaceName = groupTemplate.groupName + "•星空间";
      this.characterSpaceVisible = true;
      // 使用 $nextTick 确保所有 props 已更新到子组件
      this.$nextTick(() => {
        this.refreshTalkList();
      })
    },
    openCharacterUserDialog(character) {
      this.curTemplateCharacter = character;
      this.characterUserTitle = character.name + "-绑定用户";
      this.showCharacterUsersDialog = true;
      this.queryCharacterUsers(character.id);
    },
    readedCharacterTalkNotify(characterId) {
      let params = {
        category: 'character',
        characterId: characterId
      };
      this.$http({
        url: `/talk-notify/readed`,
        method: 'post',
        data: params
      }).then(() => {})
    },
    readedGroupTemplateTalkNotify(groupTemplateId) {
      let params = {
        category: 'character',
        groupTemplateId: groupTemplateId
      };
      this.$http({
        url: `/talk-notify/readed`,
        method: 'post',
        data: params
      }).then(() => {})
    },
    queryCharacterUsers(characterId) {
      this.$http({
        url: `/characterUser/getCharacterUsers?characterId=${characterId}`,
        method: 'get'
      }).then((data) => {
        this.characterUsers = data;
      })
    },
    queryUsers() {
      if (!this.userNameParam) {
        this.$message.warning("请输入用户名");
        return;
      }
      this.$http({
        url: `/user/findByName?name=${this.userNameParam}`,
        method: 'get'
      }).then((data) => {
        this.userList = data;
      })
    },
    queryCharacterWord(param) {
      this.$http({
        url: "/character/word/findCharacterWords",
        method: 'post',
        data: param
      }).then((data) => {
        this.dynamicValidateForm.words = data;
        this.showEditWordDialog = true;
      })
    },
    queryCharacterEmo(param) {
      this.$http({
        url: "/character/emo/findCharacterEmos",
        method: 'post',
        data: param
      }).then((data) => {
        this.characterEmos = data;
        this.showEditEmoDialog = true;
      })
    },
    handleWordDialogClose() {
      this.activeWordIndex = -1;
      this.showEditWordDialog = false;
    },
    handleEmoDialogClose() {
      this.showEditEmoDialog = false;
    },
    closeCharacterUsersDialog() {
      this.curTemplateCharacter = {};
      this.userList = [];
      this.characterUsers = [];
      this.showCharacterUsersDialog = false;
    },
    submitWordForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          let param = {
            templateGroupId: this.curTemplateGroup.id,
            characterId: this.curTemplateCharacter.id,
            words: this.dynamicValidateForm.words
          }
          this.$http({
            url: "/character/word/save",
            method: 'post',
            data: param
          }).then((data) => {
            let param2 = {
              templateGroupId: this.curTemplateGroup.id,
              characterId: this.curTemplateCharacter.id,
            }
            this.queryCharacterWord(param2)
            this.$message.success('保存成功');
          })
        } else {
          return false;
        }
      });
    },
    addWord() {
      this.dynamicValidateForm.words.push({
        word: '',
        voice: '',
        templateGroupId: this.curTemplateGroup.id,
        status: '0',
        tempId: Date.now(),
      });
    },
    removeWord() {
      if (this.activeWordIndex === -1) {
        this.$message.warning('请先选择语音台词');
        return;
      }

      if (!this.dynamicValidateForm.words[this.activeWordIndex].id) {
        this.dynamicValidateForm.words.splice(this.activeWordIndex, 1)
      } else {
        this.$confirm(`请确认是否删除当前语音台词【${this.dynamicValidateForm.words[this.activeWordIndex].word}】？`, {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$http({
            url: `/character/word/delete/${this.dynamicValidateForm.words[this.activeWordIndex].id}`,
            method: 'delete',
          }).then(() => {
            this.dynamicValidateForm.words.splice(this.activeWordIndex, 1)
            this.$message.success("删除成功");
          });
        })
      }
    },
    playWordVoice(url) {
      if (!this.audio) {
        this.audio = new Audio();
        this.audioSrc = url;
        this.audio.src = url;
        this.audio.play();
      } else {
        if (url === this.audioSrc) {
          this.audioSrc = '';
          this.audio.pause();
          this.audio = null;
        } else {
          this.audio.pause();
          this.audio = new Audio();
          this.audioSrc = url;
          this.audio.src = url;
          this.audio.play();
        }
      }
    },
    onUploadVoiceSuccess(data) {
      if (this.activeWordIndex === -1) {
        this.$message.warning('请先选中一句台词');
        return
      }
      this.dynamicValidateForm.words[this.activeWordIndex].voice = data.url;
      this.dynamicValidateForm.words[this.activeWordIndex].word = data.originalName;
    },
    chooseWord(index) {
      this.activeWordIndex = index;
    },
    onDeleteVoice(index) {
      this.dynamicValidateForm.words[index].voice = '';
    },
    publishWord() {
      let param = {
        templateGroupId: this.curTemplateGroup.id,
        characterId: this.curTemplateCharacter.id,
      }
      this.$http({
        url: `/character/word/publish`,
        method: 'post',
        data: param
      }).then(() => {
        this.queryCharacterWord(param);
        this.$message.success("发布成功");
      });
    },
    saveEmo() {
      let param = {
        templateGroupId: this.curTemplateGroup.id,
        characterId: this.curTemplateCharacter.id,
        emos: this.characterEmos
      }
      this.$http({
        url: "/character/emo/save",
        method: 'post',
        data: param
      }).then((data) => {
        let param2 = {
          templateGroupId: this.curTemplateGroup.id,
          characterId: this.curTemplateCharacter.id,
        }
        this.queryCharacterEmo(param2)
        this.$message.success('保存成功');
      })
    },
    onDeleteEmo(index) {
      if (!this.characterEmos[index].id) {
        this.characterEmos.splice(index, 1)
      } else {
        this.$confirm(`请确认是否删除角色当前表情？`, {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$http({
            url: `/character/emo/delete/${this.characterEmos[index].id}`,
            method: 'delete',
          }).then(() => {
            this.characterEmos.splice(index, 1);
            this.$message.success("删除成功");
          });
        })
      }
    },
    bindCharacterUser(userId) {
      // 检查用户是否已经绑定
      if (this.isUserAlreadyBound(userId)) {
        this.$message.warning('该用户已绑定到角色');
        return;
      }
      
      let param = {
        characterId: this.curTemplateCharacter.id,
        userIds: [userId],
      }
      this.$http({
        url: "/characterUser/bindCharacterUser",
        method: 'post',
        data: param
      }).then((data) => {
        this.$message.success('绑定成功');
        // 重新查询角色用户列表
        this.queryCharacterUsers(this.curTemplateCharacter.id);
      }).catch(error => {
        this.$message.error('绑定失败');
      });
    },
    unBindCharacterUser(characterUser) {
      let param = {
        characterId: characterUser.characterId,
        userIds: [characterUser.userId],
      }
      this.$http({
        url: "/characterUser/unbindCharacterUser",
        method: 'post',
        data: param
      }).then((data) => {
        this.$message.success('解除成功');
        // 重新查询角色用户列表
        this.queryCharacterUsers(this.curTemplateCharacter.id);
      }).catch(error => {
        this.$message.error('绑定失败');
      });
    },
    isUserAlreadyBound(userId) {
      // 检查用户是否已绑定到当前角色
      return this.characterUsers.some(user => user.userId === userId);
    },
    closeDrawer() {
      this.section = '';
      this.characterId = null;
      this.groupTemplateId = null;
      this.characterSpaceVisible = false;
    },
    refreshTalkList() {
      this.$refs.talkListRef.refreshTalkList();
    },
    handleShowAddTalk() {
      this.$refs.talkListRef.handleShowAddTalk();
    },
    showTalkNotify() {
      this.$nextTick(() => {
        this.$refs.talkNotifyRef.show();
      })
    },
    groupTemplateNotifyCount(groupTemplateId) {
      return this.$store.getters.getGroupTemplateNotifyCount(groupTemplateId)
    },
    characterNotifyCount(characterId) {
      return this.$store.getters.getCharacterNotifyCount(characterId)
    },
    characterListNotifyCount(characterIds) {
      return this.$store.getters.getCharactersNotifyCount(characterIds)
    }
  },
  computed: {
    imageAction() {
      return `/image/upload`;
    },
    audioAction(){
      return `/audio/upload`;
    },
    isAdmin() {
      return this.$store.state.userStore.userInfo.id === 1;
    }
  }
}
</script>

<style scoped lang="scss">
.buttons {
  margin: 0 10px;
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

      .group-template-notify {
        position: absolute;
        top: -10px;
        right: 0px;
        background-color: #f56c6c;
        color: #fff;
        border-radius: 30px;
        padding: 1px 5px;
        font-size: 10px;
        text-align: center;
        white-space: nowrap;
        border: 1px solid #f1e5e5;
      }

      .operate-button {
        margin: 0 5px;
        float: right;
        padding: 3px 0;
        width: 32px;
        height: 32px
      }

      .template-group-space {
        color: orange;
      }

      .template-group-characters-space {
        color: #00f2fe;
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

.my-characters {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 20px;
  padding: 20px;

  .character-item {
    padding: 10px;
    border-radius: 8px;
    background-color: var(--bg-color);
    transition: all 0.3s ease;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    display: flex;
    flex-direction: column;
    align-items: center;

    .character-avatar {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;

      .character-name {
        text-align: center;
        font-size: 14px;
        font-weight: 500;
        word-break: break-all;
        max-width: 100%;
      }
    }

    .character-content {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(30%, 1fr));
      gap: 5px;
      justify-content: center;
      width: 100%;
      margin-top: 10px;

      .content-item {
        display: flex;
        align-items: center;
        justify-content: center;

        .my-character-notify {
          position: absolute;
          top: -10px;
          right: 6px;
          background-color: #f56c6c;
          color: #fff;
          border-radius: 30px;
          padding: 1px 5px;
          font-size: 10px;
          text-align: center;
          white-space: nowrap;
          border: 1px solid #f1e5e5;
        }
      }

      .icon {
        display: block;
        height: 25px;
        width: 25px;
        line-height: 25px;
      }
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
    text-align: center;
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
        margin-left: 8px;
      }

      .character-notify {
        position: absolute;
        top: -8px;
        right: 0;
        background-color: #f56c6c;
        color: #fff;
        border-radius: 30px;
        padding: 1px 5px;
        font-size: 10px;
        text-align: center;
        white-space: nowrap;
        border: 1px solid #f1e5e5;
      }

      .delete-button {
        margin-left: 8px;
      }

      .edit-character-space {
        color: orange;
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
    text-align: center;
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

.edit-character-word {
  .character-word {

    ::v-deep .el-form-item__content {
      display: flex;
      justify-content: left;

      .el-input {
        width: 64%;
      }
    }

    .btn-remove {
      display: none;
      position: absolute;
      right: -10px;
      top: -10px;
      color: darkred;
      font-size: 20px;
      cursor: pointer;
    }

    .voice:hover .btn-remove {
      display: block;
      color: #ce1818;
    }

    .voice-uploader {
      width: 40px;
      height: 40px;
      margin-left: 8px;
    }
  }
}

.edit-character-emo {

  .character-emo-box {
    padding-top: 15px;
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    grid-auto-rows: min-content; /* 设置自动生成行的高度为内容的最小高度 */
    /* 为所有网格项设置内容居中 */
    place-items: center;
    gap: 15px;
    overflow: scroll;

    .upload-emo {
      width: 60px;
      height: 60px;
      line-height: 60px;
      border: #cccccc solid 1px;
      font-size: 25px;
      cursor: pointer;
      box-sizing: border-box;
      text-align: center;
    }

    .character-emo-item {
      width: 60px;
      height: 60px;
      line-height: 60px;
      border: #cccccc solid 1px;
      position: relative;

      .emo-image {
        width: 60px;
        height: 60px;
      }

      .btn-remove {
        display: none;
        position: absolute;
        right: -10px;
        top: -10px;
        color: darkred;
        font-size: 20px;
        cursor: pointer;
      }
    }

    .character-emo-item:hover .btn-remove {
      display: block;
      color: #ce1818;
    }

  }
}

.edit-character-users {
  .character-users-content {
    max-height: 400px;
    overflow-y: auto;
    padding: 10px 0;
  }

  .bound-users-section,
  .search-users-section {
    margin-bottom: 20px;
  }

  .bound-users-section h3,
  .search-users-section h3 {
    margin: 10px 0;
    font-size: 16px;
    font-weight: bold;
    color: #333;
  }

  .search-box {
    margin: 15px 0;
  }

  .search-input {
    width: 100%;
  }

  .user-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .user-item {
    display: flex;
    align-items: center;
    padding: 8px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    background-color: #f5f7fa;
    transition: all 0.3s;

    &:hover {
      background-color: #e6f7ff;
      border-color: #91d5ff;
    }

    .user-name {
      margin: 0 15px;
      flex: 1;
      color: #333;
    }

    .el-button {
      margin-left: auto;
    }
  }

  .empty-state {
    text-align: center;
    color: #909399;
    font-size: 14px;
    padding: 20px;
  }
}

</style>