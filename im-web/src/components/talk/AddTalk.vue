<template>
  <el-dialog title="发布动态" :visible.sync="visible"  width="44%" :before-close="handleClose" :append-to-body="true">
    <el-form :rules="rules" ref="ruleForm"  label-width="auto"
             :model="form" class="form-box">
      <el-form-item label="内容：" prop="content" label-width="120px" class="form-item">
<!--        <el-input class="form-content" type="textarea" :autosize="{ minRows: 6 }" placeholder="请输入说说内容"
                  v-model="form.content">
        </el-input>-->
        <div class="contentInputBox" ref="contentInputBox">
          <div class="">
            <div ref="textareaRef" contenteditable="true" @input="onInput"
                 @paste="optimizePasteEvent" :data-placeholder="placeholder"
                 class="comment-textarea" v-html="$emo.transform(form.content)"></div>
            <span class="point biaoqing-point" @click="showEmoji = !showEmoji">
                <i class="icon iconfont icon-biaoqing"></i>
            </span>
          </div>
          <div class="emoji-wrapper" v-if="showEmoji">
            <Emoji @chooseEmoji="handleChooseEmoji"/>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="可见范围：" prop="scope" label-width="120px" class="form-item" v-if="category==='private'">
        <el-select v-model="form.scope" placeholder="请选择">
          <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="群空间可见：" prop="groupVisible" label-width="120px" class="form-item" v-if="category==='private'">
        <div style="display: flex">
          <el-switch
              style="display: block"
              v-model="form.groupVisible"
              active-color="#13ce66"
              inactive-color="#ff4949"
              active-text="开启"
              inactive-text="关闭"
              @change="groupVisibleChange">
          </el-switch>
          <div class="tips">
            <el-popover
                placement="top-start"
                width="200"
                trigger="hover"
                content="群空间可见需要将可见范围设置成【公开】，并且加入的所有群聊可见">
              <div class="el-icon-question" slot="reference"></div>
            </el-popover>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="地区空间可见：" prop="regionVisible" label-width="120px" class="form-item" v-if="category==='private'">
        <div style="display: flex">
          <el-switch
              style="display: block"
              v-model="form.regionVisible"
              active-color="#13ce66"
              inactive-color="#ff4949"
              active-text="开启"
              inactive-text="关闭"
              @change="regionVisibleChange">
          </el-switch>
          <div class="tips">
            <el-popover
                placement="top-start"
                width="200"
                trigger="hover"
                content="地区空间可见需要将可见范围设置成【公开】，并且所有常驻的地区群聊可见">
              <div class="el-icon-question" slot="reference"></div>
            </el-popover>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="角色选择：" prop="character" label-width="120px" class="form-item">
        <span class="character-item" v-on:click="openCharacterChooseDialog">
          <el-avatar :fit="fit" size="medium" icon="el-icon-user-solid" :src="form.avatar">
          </el-avatar>
        </span>
        <span class="nick-name">{{form.nickName}} <el-button @click="removeCharacter" class="del-btn" v-if="form.characterId" type="danger" icon="el-icon-delete" size="mini" circle></el-button></span>
      </el-form-item>
      <el-form-item label="图片|视频|音频：" prop="imgUrl" label-width="120px" class="form-item">
<!--        <batch-image-upload class="form-content"
                            :action="imageAction"
                            :showLoading="true"
                            :maxSize="maxSize"
                            :fileTypes="['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif']"
                            :imageList="imageList"
                            @success="handleUploadImageSuccess"
                            @remove="handleRemove"
                            @change="handleChange"
                            ref="imageUploader">
        </batch-image-upload>-->
        <multi-media-upload :file-list="fileList" :limit="9" :file-size="10" :multiple="true" @update:fileList="updateFileList"/>
      </el-form-item>
      <el-form-item>
          <el-button type="primary" @click="submitForm('ruleForm')">发布</el-button>
          <el-button @click="resetForm('ruleForm')">清空</el-button>
      </el-form-item>
    </el-form>
    <el-dialog
        width="50%"
        title="选择角色"
        :visible.sync="chooseCharacterDialogVisible"
        :before-close="closeChooseCharacterDialog"
        append-to-body>
      <div class="agm-container">
        <div class="agm-l-box">
          <el-input width="200px" placeholder="搜索模板群聊" class="input-with-select" v-model="searchText" >
            <el-button slot="append" icon="el-icon-search" ></el-button>
          </el-input>
          <el-scrollbar style="height:400px;">
            <div v-for="(templateGroup,index) in templateGroupList" :key="templateGroup.id"
                 v-show="templateGroup.groupName.startsWith(searchText)"
                 class="template-group-box">
              <template-group-item :templateGroup="templateGroup" class="group-item-left"></template-group-item>
              <div class="group-item-right">
                <el-button :type="groupActiveIndex === index ? 'success' : ''" icon="el-icon-check" circle
                           @click="queryTemplateCharacter(templateGroup, index)" ></el-button>
              </div>
              <p style="clear:both;"></p>
            </div>
          </el-scrollbar>
        </div>
        <div class="agm-r-box">
          <el-input width="200px" placeholder="搜索模板人物" class="input-with-select" v-model="characterSearchText" >
            <el-button slot="append" icon="el-icon-search" ></el-button>
          </el-input>
          <el-scrollbar style="height:400px;">
            <div class="template-character-box" v-for="(templateCharacter,index) in templateCharacterList"
                 :key="templateCharacter.id" v-show="templateCharacter.name.startsWith(characterSearchText)">
              <template-character-item class="character-item-left" :templateCharacter = "templateCharacter"></template-character-item>
              <div class="character-item-right">
                <el-button :type="characterActiveIndex === index ? 'success' : ''" icon="el-icon-check" circle
                           @click="chooseTemplateCharacter(templateCharacter, index)"></el-button>
              </div>
              <p style="clear:both;"></p>
            </div>
          </el-scrollbar>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
			  <el-button @click="closeChooseCharacterDialog()">取 消</el-button>
			  <el-button type="primary" @click="handleOk()">确 定</el-button>
		  </span>
    </el-dialog>
  </el-dialog>
</template>

<script>
import BatchFileUpload from "@/components/common/BatchFileUpload";
import BatchImageUpload from "@/components/common/BatchImageUpload";
import HeadImage from "@/components/common/HeadImage";
import TemplateGroupItem from "@/components/group/TemplateGroupItem";
import TemplateCharacterItem from "@/components/group/TemplateCharacterItem";
import Emoji from '@/components/emoji';
import MultiMediaUpload from "@/components/common/MultiMediaUpload";

export default {
  name: "AddTalk",
  components: {
    HeadImage,
    BatchFileUpload,
    BatchImageUpload,
    TemplateGroupItem,
    TemplateCharacterItem,
    Emoji,
    MultiMediaUpload,
  },
  props: {
    visible: {
      type: Boolean
    },
    talkId: {
      type: Number,
      default: null
    },
    category: {
      type: String,
      required: true
    },
    groupId: {
      type: Number,
    },
    regionGroupId: {
      type: Number,
    },
    regionCode: {
      type: String,
    }
  },
  data() {
    return {
      fit: 'fill',
      maxSize: 5 * 1024 * 1024,
      imageList: [],
      uploadHeaders: {"accessToken":sessionStorage.getItem('accessToken')},
      fileTypes: ['image/jpeg', 'image/png', 'image/jpg','image/webp', 'image/gif'],
      rules: {
        scope: [
          { required: true, message: '请选择公开范围', trigger: 'blue' }
        ],
      },
      form: {
        id: null,
        content: '',
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
      options: [
        {value: 1, label: "私密"},
        {value: 2, label: "好友可见"},
        {value: 9, label: "公开"}
      ],
      chooseCharacterDialogVisible: false,
      searchText: "",
      characterSearchText: "",
      templateGroupList: [],
      templateCharacterList: [],
      groupActiveIndex: -1,
      characterActiveIndex: -1,
      templateCharacter: {},
      placeholder: "请输入内容",
      lastEditRange: null,
      showEmoji: false,
      fileUploadLoading: null,
    }
  },
  created() {
    if (this.talkId !== null) {
      this.getTalkDetail(this.talkId);
    }
  },
  methods: {
    onInput(e) {
      let selection = window.getSelection()
      this.lastEditRange = selection.getRangeAt(0);
    },
    optimizePasteEvent(e) {
      // 监听粘贴内容到输入框事件，对内容进行处理 处理掉复制的样式标签，只拿取文本部分
      e.stopPropagation()
      e.preventDefault()
      let text = '', event = (e.originalEvent || e)
      if (event.clipboardData && event.clipboardData.getData) {
        text = event.clipboardData.getData('text/plain')
      } else if (window.clipboardData && window.clipboardData.getData) {
        text = window.clipboardData.getData('text')
      }
      if (document.queryCommandSupported('insertText')) {
        document.execCommand('insertText', false, text)
      } else {
        document.execCommand('paste', false, text)
      }
    },
    //添加表情
    handleChooseEmoji(emoText) {
      // 创建一个img标签（表情）
      let img = document.createElement('img');
      img.src = this.$emo.textToUrl(emoText);
      img.style.verticalAlign = 'middle';
      img.style.marginLeft = "2px"
      img.style.marginRight = "2px"
      img.style.height = "25px"
      img.style.width = "25px"
      img.dataset.code = emoText;

      let edit = this.$refs.textareaRef;
      edit.focus()
      let selection = window.getSelection()
      // 如果存在最后的光标对象
      if (this.lastEditRange) {
        // 选区对象清除所有光标
        selection.removeAllRanges();
        // 并添加最后记录的光标，以还原之前的状态
        selection.addRange(this.lastEditRange);
        // 获取到最后选择的位置
        let range = selection.getRangeAt(0);
        // 在此位置插入表情图
        range.insertNode(img)
        // false，表示将Range对象所代表的区域的起点移动到终点处
        range.collapse(false)

        // 记录最后的位置
        this.lastEditRange = selection.getRangeAt(0);
      } else {
        // 将表情添加到可编辑的div中，作为可编辑div的子节点
        edit.appendChild(img)
        // 使用选取对象，选取可编辑div中的所有子节点
        selection.selectAllChildren(edit)
        // 合并到最后面，即实现了添加一个表情后，把光标移到最后面
        selection.collapseToEnd()
      }
      this.showEmoji = false
    },
    createSendText() {
      let sendText = ""
      this.$refs.textareaRef.childNodes.forEach((node) => {
        if (node.nodeName == "#text") {
          sendText += this.html2Escape(node.textContent);
        } else if (node.nodeName == "SPAN") {
          sendText += node.innerText;
        } else if (node.nodeName == "IMG") {
          sendText += node.dataset.code;
        } else if (node.nodeName == "DIV") {
          sendText += '\n';
          node.childNodes.forEach((divNode) => {
            if (divNode.nodeName == "#text") {
              sendText += this.html2Escape(divNode.textContent);
            } else if (divNode.nodeName == "IMG") {
              sendText += divNode.dataset.code;
            } else if (divNode.nodeName == "SPAN") {
              sendText += divNode.innerText;
            }
          })
        }
      })
      return sendText;
    },
    html2Escape(strHtml) {
      return strHtml.replace(/[<>&"]/g, function (c) {
        return {
          '<': '&lt;',
          '>': '&gt;',
          '&': '&amp;',
          '"': '&quot;'
        }[c];
      });
    },
    handleClose() {
      this.$emit("close");
    },
    clearFiles () {
      //this.$refs.imageUploader.clearImages();
      //this.$refs.imageUploader.$emit("removeImages")
      this.fileList = [];
    },
    handleChange(info, fileList) {
      this.imageList = fileList;
    },
    handleUploadImageSuccess(data) {
      this.imageList.push({ url: data.originUrl});
    },
    handleRemove(file) {
      this.imageList.forEach((item, index) => {
        if (item.url === file.url) {
          this.imageList.splice(index, 1);
        }
      });
    },
    updateFileList(fileList) {
      this.fileList = fileList;
    },
    async submitForm(formName) {
      let talkParam = this.form;
      talkParam.category = this.category;
      talkParam.groupId = this.groupId;
      talkParam.regionGroupId = this.regionGroupId;
      talkParam.regionCode = this.regionCode;
      //talkParam.imgUrls = this.imageList.map(obj => {return obj.url});
      let el = this.$refs.textareaRef;
      if (!el.innerHTML) {
        this.$message.warning("请输入动态内容");
        return
      }
      let sendText = this.createSendText();
      if (!sendText.trim()) {
        return
      }
      talkParam.content = sendText;
      talkParam.files = [];
      if (this.fileList.length > 0) {
        this.loading = this.$loading({
          lock: true,
          text: '正在上传文件...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });
      }
      for (let i = 0; i < this.fileList.length; i++) {
        if (this.fileList[i].url.startsWith("blob:")) {
          let obj = await this.uploadFile(this.fileList[i]);
          if (obj == null) {
            this.loading && this.loading.close();
            return;
          }
          talkParam.files.push(obj);
        } else {
          talkParam.files.push(this.fileList[i]);
        }
      }
      this.loading && this.loading.close();

      //console.log("talkParam", talkParam);
      this.$refs[formName].validate((valid) => {
        if (valid) {
          let url = "/talk"
          if (talkParam.id != null) {
            url += "/update"
          } else {
            url += "/add"
          }
          this.$http({
            url: url,
            method: 'post',
            data: talkParam
          }).then((data) => {
            this.$message.success("发布成功");
            this.resetForm('ruleForm');
            this.clearFiles();
            this.$emit("refresh");
          }).finally(() => {
            this.$emit("close");
          })
        }
      });
    },
    async uploadFile(file) {
      return new Promise((resolve, reject) => {
        let result = null;
        let formData = new FormData()
        let url = '';
        if (file.fileType === 1) {
          url = this.imageAction;
        } else if (file.fileType === 2) {
          url = this.videoAction;
        } else if (file.fileType === 3){
          url = this.audioAction;
        }
        formData.append('file', file.raw)
        this.$http({
          url: url,
          data: formData,
          method: 'post',
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }).then((data) => {
          result = {fileType: file.fileType, url: null}
          let fileUrl = '';
          if (file.fileType === 1) {
            fileUrl = data.originUrl;
          } else if (file.fileType === 2) {
            fileUrl = data.videoUrl;
            result.coverUrl = data.coverUrl;
          } else if (file.fileType === 3) {
            fileUrl = data.url;
            result.name = data.originalName;
          } else {
            fileUrl = data;
          }
          result.url = fileUrl;
        }).catch((e) => {
          this.$message.warning('上传文件异常！');
        }).finally(() => {
          resolve(result)
        })
      });

    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
      this.form.avatar = '';
      this.form.nickName = '';
      this.fileList = [];
    },
    openCharacterChooseDialog() {
      if (!this.form.characterId) {
        this.chooseCharacterDialogVisible = true;
        this.queryTemplateGroup();
      } else {
        this.$message.warning("已存在角色");
      }
    },
    closeChooseCharacterDialog() {
      this.chooseCharacterDialogVisible = false;
      this.templateGroupList = [];
      this.templateCharacterList = [];
      this.groupActiveIndex = -1;
      this.characterActiveIndex = -1;
    },
    queryTemplateGroup(){
      this.templateGroupList = []
      this.$http({
        url: "/templateGroup/list",
        method: 'get',
        params: ''
      }).then(data => {
        this.templateGroupList = data;
      })
    },
    queryTemplateCharacter(templateGroup, index) {
      this.groupActiveIndex = index;
      this.characterActiveIndex = -1;
      this.$http({
        url: `/templateCharacter/list/${templateGroup["id"]}`,
        method: 'get'
      }).then((data) => {
        this.templateCharacterList = data;
      });
    },
    chooseTemplateCharacter(templateCharacter, index) {
      this.characterActiveIndex = index;
      this.templateCharacter = templateCharacter;
    },
    handleOk() {
      this.form.nickName = this.templateCharacter.name;
      this.form.avatar = this.templateCharacter.avatar;
      this.form.characterId = this.templateCharacter.id;
      this.closeChooseCharacterDialog();
    },
    removeCharacter() {
      this.form.avatar = '';
      this.form.nickName = '';
      this.form.characterId = null;
    },
    getTalkDetail(talkId) {
      let _this = this
      this.$http({
        url: `/talk/getTalkDetail/${talkId}`,
        method: 'get'
      }).then((data) => {
        _this.form = data;
        _this.fileList = data.fileList;
        if (data.imgUrls && data.imgUrls.length > 0) {
          _this.imageList = data.imgUrls.map(item => {return {url: item}});
        }
      });
    },
    groupVisibleChange(value) {
      this.form.groupVisible = value;
    },
    regionVisibleChange(value) {
      this.form.regionVisible = value;
    },
  },
  computed: {
    imageAction() {
      return `/image/upload`;
    },
    videoAction() {
      return `/video/upload`;
    },
    audioAction() {
      return `/audio/upload`;
    },
    fileAction() {
      return `/file/upload`;
    }
  },
}
</script>

<style lang="scss">
.form-box {
  .form-item {
    .el-form-item__content {
      text-align: left;
    }

    .contentInputBox {
      width: 100%;
      border: 1px solid #67C23A;
      border-radius: 5px;
      background-color: #fff;
      position: relative;
      min-height: 100px;
      display: block;

      .comment-textarea {
        text-align: left;
        min-height: 100px;
        outline: none;
        padding-left: 10px;
        padding-top: 5px;
        white-space: pre-wrap;

        &:empty:before {
          content: attr(data-placeholder);
          color: #666;
        }
      }

      .biaoqing-point {
        cursor: pointer;
        display: inline-block;
        position: absolute;
        right: 5px;
        bottom: 0;

        i {
          font-size: 1.3rem;
          color: #67C23A;
        }
      }
    }
  }

  .character-item, nick-name {
    display: inline-block;
    height: 36px;
    line-height: 36px;
    vertical-align:middle;
    margin-right: 10px;
  }

  .nick-name {
    .del-btn {
      margin-left: 30px;
    }
  }

  .tips {
    margin-left: 10px;
    width: 20px;
    height: 20px;
    line-height: 20px;
  }
}

.agm-container {
  display: flex;

  .agm-l-box {
    flex: 1;
    border: var(--border-color) solid 1px;
    box-sizing: content-box;

    .template-group-box {
      width: 100%;

      .group-item-left {
        float: left;
      }
      .group-item-right {
        float: right;
        margin-right: 10px;
        height: 65px;
        line-height: 65px;
      }
    }
  }

  .agm-r-box {
    flex: 1;
    border: var(--border-color) solid 1px;

    .template-character-box {
      width: 100%;

      .character-item-left {
        float: left;
      }
      .character-item-right {
        float: right;
        margin-right: 10px;
        height: 65px;
        line-height: 65px;
      }
    }
  }
}

</style>