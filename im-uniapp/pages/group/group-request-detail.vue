<template>
  <view class="page group-request-detail">
    <nav-bar back>群聊请求详情</nav-bar>
    <view class="content">
      <!-- 群聊信息卡片 -->
      <view class="card group-info-card">
        <view class="card-header">
          <text class="card-title">群聊信息</text>
        </view>
        <view class="card-body">
          <view class="group-info">
            <head-image :url="request.groupHeadImage" :name="request.groupName" size="small"></head-image>
            <view class="info">
              <view class="group-name">
                <text class="name-text">{{request.groupName}}</text>
                <view class="group-type" :style="{marginLeft: '10px'}">
                  <uni-tag v-if="request.groupType===1" :circle="true" text="模板群聊" type="primary" size="mini" custom-style="background-color: rgb(0,47,167);border-color: rgb(0,47,167);" />
                  <uni-tag v-if="request.groupType===2" :circle="true" text="多元角色群聊" type="primary" size="mini" custom-style="background-color: rgb(105,149,114);border-color: rgb(105,149,114);" />
                  <uni-tag v-if="request.groupType===3" :circle="true" text="角色群聊" type="primary" size="mini" custom-style="background-color: rgb(144,0,33);border-color: rgb(144,0,33);" />
                  <uni-tag v-if="request.groupType===4" :circle="true" text="模板角色群聊" type="primary" size="mini" custom-style="background-color: rgb(176,89,35);border-color: rgb(176,89,35);" />
                </view>
              </view>
              <view class="info-text" v-if="request.remark">
                <text class="label">备注：</text>
                <text class="remark">{{request.remark}}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 用户信息卡片 -->
      <view class="card user-info-card">
        <view class="card-header">
          <text class="card-title">用户信息</text>
        </view>
        <view class="card-body">
          <view class="user-info">
            <head-image :url="request.userHeadImage" :name="request.userNickname" :size="60"></head-image>
            <view class="user-name">{{request.userNickname}}</view>
          </view>
        </view>
      </view>

      <!-- 角色信息卡片（条件渲染） -->
      <view class="card character-info-card" v-if="request.templateCharacterName">
        <view class="card-header">
          <text class="card-title">角色信息</text>
        </view>
        <view class="card-body">
          <view class="character-info">
            <head-image :url="request.templateCharacterAvatar" :name="request.templateCharacterName" :size="60"></head-image>
            <view class="character-name">{{request.templateCharacterName}}</view>
          </view>
        </view>
      </view>

      <!-- 发起用户卡片 -->
      <view class="card launch-user-card">
        <view class="card-header">
          <text class="card-title">发起用户</text>
        </view>
        <view class="card-body">
          <view class="launch-user">
            <head-image :url="request.launchUserHeadImage" :name="request.launchUserNickname" :size="60"></head-image>
            <view class="user-name">{{request.launchUserNickname}}</view>
          </view>
        </view>
      </view>

      <!-- 操作按钮区域 -->
      <view class="btn-group">
        <button v-if="type === '1'" size="mini" type="success" class="operation-btn" @click="approveGroupRequest(request.id)">同意</button>
        <button v-if="type === '1'" size="mini" type="warn" class="operation-btn" @click="rejectGroupRequest(request.id)">拒绝</button>
        <button v-if="type === '2'" size="mini" type="info" class="operation-btn" @click="recallGroupRequest(request.id)">撤回</button>
        <button size="mini" type="primary" class="operation-btn" @click="modifyGroupRequest">修改</button>
      </view>
    </view>
    <group-template-list ref="templateGroupList" :group-templates="templateGroupList" @confirm="chooseGroupTemplate"></group-template-list>
    <character-list ref="characterList" :characters="characters" @confirm="chooseCharacter"></character-list>
  </view>
</template>

<script>
import GroupTemplateList from "../../components/group-template-list/group-template-list.vue";
import CharacterList from "../../components/character-list/character-list.vue";

export default {
  name: "group-request-detail",
  components: {CharacterList, GroupTemplateList},
  data() {
    return {
      request: {},
      type: '',
      templateGroupList: [],
      characters: [],
    };
  },
  methods: {
    getRequestDetail(id) {
      this.$http({
        url: `/group/request/detail?id=${id}`,
        method: 'GET'
      }).then((data) => {
        this.request = data;
      }).catch(() => {
      });
    },
    rejectGroupRequest(id) {
      this.$http({
        url: `/group/request/reject?id=${id}`,
        method: "post",
      }).then(() => {
        uni.navigateBack({
          delta: 1
        })
      })
    },
    approveGroupRequest(id) {
      this.$http({
        url: `/group/request/approve?id=${id}`,
        method: "post",
      }).then(() => {
        uni.navigateBack({
          delta: 1
        })
      })
    },
    recallGroupRequest(id) {
      this.$http({
        url: `/group/request/recall?id=${id}`,
        method: "post",
      }).then(() => {
        uni.navigateBack({
          delta: 1
        })
      })
    },
    async modifyGroupRequest() {
      await this.queryTemplateGroup();
      this.$refs.templateGroupList.open();
    },
    async chooseGroupTemplate(templateGroup) {
      await this.queryTemplateCharacter(templateGroup.id);
      this.$refs.characterList.open();
    },
    chooseCharacter(templateCharacter) {
      let param = {
        id: this.request.id,
        templateCharacterId: templateCharacter.id,
      }
      this.$http({
        url: `/group/request/update`,
        method: "post",
        data: param,
      }).then(() => {
        this.request.templateCharacterId = templateCharacter.id;
        this.request.templateCharacterName = templateCharacter.name;
        this.request.templateCharacterAvatar = templateCharacter.avatar;
        // 提示修改成功
        uni.showToast({
          title: "修改成功",
          icon: 'none'
        })
      })
    },
    async queryTemplateGroup(){
      if (this.templateGroupList.length) {
        return;
      }
      await this.$http({
        url: "/templateGroup/list",
        method: 'get',
        params: ''
      }).then(data => {
        this.templateGroupList = data;
      })
    },
    async queryTemplateCharacter(templateGroupId) {
      await this.$http({
        url: `/templateCharacter/list/${templateGroupId}`,
        method: 'get'
      }).then(result => {
        this.characters = result;
      });
    },
  },
  onLoad(options) {
    this.type = options.type;
    this.getRequestDetail(options.id)
  }
}
</script>

<style scoped lang="scss">
.group-request-detail {
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  min-height: 100vh;

  .content {
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 12px; // 卡片间距
  }

  // 卡片基础样式
  .card {
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: box-shadow 0.3s ease;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12); // hover增强阴影
    }

    .card-header {
      padding: 12px 16px;
      border-bottom: 1px solid #f0f2f5;

      .card-title {
        font-size: 0.9375rem;
        font-weight: 600;
        color: #1d2129;
      }
    }

    .card-body {
      padding: 16px;
    }
  }

  // 群聊信息卡片
  .group-info-card {
    .group-info {
      display: flex;
      align-items: center;

      .info {
        margin-left: 12px;
        flex: 1;
      }

      .group-name {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        .name-text {
          font-size: 0.9375rem;
          font-weight: 500;
          color: #1d2129;
        }
      }

      .info-text {
        font-size: 0.8125rem;
        color: #909399;
        display: flex;
        align-items: center;

        .label {
          margin-right: 4px;
        }

        .remark {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          max-width: calc(100% - 40px);
        }
      }
    }
  }

  // 用户信息卡片
  .user-info-card,
  .character-info-card,
  .launch-user-card {
    .user-info,
    .character-info,
    .launch-user {
      display: flex;
      align-items: center;

      .user-name,
      .character-name {
        margin-left: 14rpx;
        font-size: 0.875rem;
        color: #1d2129;
      }
    }
  }

  // 按钮区域
  .btn-group {
    display: flex;
    gap: 12px;
    margin-top: 8px;

    .operation-btn {
      border-radius: 8px;
      font-size: 0.8125rem;
    }
  }
}
</style>