<template>
  <view class="page group-switch-box">
    <nav-bar back>群聊类型切换</nav-bar>
    <view class="btns-box">
      <up-button text="随机设置" v-if="toGroupType ===2 || toGroupType ===3"></up-button>
      <up-button :text="groupTemplateName" type="primary" plain @click="openGroupTemplatePopup"></up-button>
      <up-button text="确认" type="success" plain></up-button>
    </view>
    <view class="nav-bar">
      <view class="nav-search">
        <uni-search-bar v-model="searchText" radius="100" cancelButton="none" placeholder="输入好友昵称搜索"></uni-search-bar>
      </view>
    </view>
    <view class="member-items">
      <scroll-view class="scroll-bar" scroll-with-animation="true" scroll-y="true">
        <view v-for="(member, index) in groupMembers" v-show="!searchText || member.aliasName.includes(searchText)"
              :key="member.id">
          <view class="member-item">
            <head-image :name="member.aliasName" :url="member.headImage" :id="member.userId" :online="member.online"></head-image>
            <view class="member-name">{{ member.aliasName }}</view>
            <view class="character-info">
              <view class="character-name">{{member.templateCharacterName}}</view>
              <character-avatar :name="member.templateCharacterName"
                                :url="member.templateCharacterAvatar ? member.templateCharacterAvatar : '/static/image/default_head.png'"
                                @click.stop="selectCharacter(member, index)">
              </character-avatar>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
    <group-template-list ref="templateGroupList" :group-templates="templateGroupList" @confirm="chooseGroupTemplate"></group-template-list>
    <character-list ref="characterList" :characters="characters" @confirm="chooseCharacter"></character-list>
  </view>
</template>

<script>
import CharacterAvatar from "../../components/character-avatar/character-avatar.vue";
import GroupTemplateList from "../../components/group-template-list/group-template-list.vue";
import CharacterList from "../../components/character-list/character-list.vue";

export default {
  name: "group-switch",
  components: {CharacterList, GroupTemplateList, CharacterAvatar},
  data() {
    return {
      searchText: "",
      toGroupType: null,
      group: {},
      groupMembers: [],
      templateGroupList: [],
      groupTemplateName: '群聊模板',
      activeGroupTemplate: {},
      characters: [],
      activeMember: {},
      activeMemberIndex: -1,
    }
  },
  onLoad(options) {
    this.toGroupType = Number(options.toGroupType);
    this.loadGroupInfo(Number(options.id));
    this.loadGroupMembers(Number(options.id));
  },
  methods: {
    loadGroupInfo(id) {
      this.$http({
        url: `/group/find/${id}`,
        method: 'GET'
      }).then((group) => {
        this.group = group;
      })
    },
    loadGroupMembers(id) {
      this.$http({
        url: `/group/members/${id}`,
        method: "GET"
      }).then((members) => {
        this.groupMembers = members.filter(m => !m.quit);
      })
    },
    selectCharacter(member, index) {
      this.activeMemberIndex = index;
      this.activeMember = member;
      this.$refs.characterList.open();
    },
    async openGroupTemplatePopup(){
      await this.queryGroupTemplates();
      this.$refs.templateGroupList.open();
    },
    async queryGroupTemplates() {
      this.$http({
        url: "/templateGroup/list",
        method: 'get',
        params: ''
      }).then(data => {
        this.templateGroupList = data;
      })
    },
    async chooseGroupTemplate(groupTemplate) {
      this.groupTemplateName = groupTemplate.groupName;
      this.activeGroupTemplate = groupTemplate;
      this.characters = [];
      await this.queryTemplateCharacter(groupTemplate.id);
      if (this.toGroupType === 1) {
        const count = this.groupMembers.length;
        if (groupTemplate.count < count) {
          uni.showToast({
            title: "当前群聊模板的角色数量小于当前群聊人数，请选择另一个模板群聊",
            icon: 'none'
          })
          return false
        }
      }
      if (this.toGroupType === 1 || this.toGroupType === 4) {
        let index = 0;
        for (let i=0; i < this.groupMembers.length; i++) {
          if (index < this.characters.length) {
            this.groupMembers[i].templateCharacterAvatar = this.characters[index].avatar
            this.groupMembers[i].templateCharacterId = this.characters[index].id
            this.groupMembers[i].templateCharacterName = this.characters[index].name
            index++;
          }
        }
      }
    },
    chooseCharacter(character) {
      this.groupMembers[this.activeMemberIndex].templateCharacterAvatar = character.avatar;
      this.groupMembers[this.activeMemberIndex].templateCharacterId = character.id;
      this.groupMembers[this.activeMemberIndex].templateCharacterName = character.name;
    },
    async queryTemplateCharacter(templateGroupId) {
      await this.$http({
        url: `/templateCharacter/list/${templateGroupId}`,
        method: 'get'
      }).then(result => {
        this.characters = result;
      });
    },
  }
}
</script>

<style scoped lang="scss">
.group-switch-box {
  position: relative;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  padding: 10rpx;

  .btns-box {
    display: flex;
    flex-direction: column;
    gap: 10rpx;
  }

  .member-items {
    position: relative;
    flex: 1;
    overflow: hidden;

    .scroll-bar {
      height: 100%;
    }

    .member-item {
      height: 120rpx;
      display: flex;
      margin-bottom: 1rpx;
      position: relative;
      padding: 0 30rpx;
      align-items: center;
      background-color: white;
      white-space: nowrap;

      .member-name {
        flex: 1;
        padding-left: 20rpx;
        font-size: 30rpx;
        font-weight: 600;
        line-height: 60rpx;
        white-space: nowrap;
        overflow: hidden;
      }

      .character-info {
        display: flex;
        align-items: center;


        .character-name {
          font-size: 30rpx;
          font-weight: 600;
          line-height: 60rpx;
          white-space: nowrap;
          margin-right: 20rpx;
        }
      }
    }
  }
}
</style>