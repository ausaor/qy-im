<template>
  <el-container class="chat-page">
    <el-aside width="260px" class="aside">
      <div class="header" @click="chooseRegion">
        {{mapVisible ? '关闭' : '选择地区'}}
      </div>
      <el-scrollbar class="group-list-items">
        <div v-for="(regionGroup,index) in regionGroupStore.regionGroups" :key="index">
          <region-group-item :regionGroup="regionGroup" :active="regionGroup === regionGroupStore.activeRegionGroup"
                      @click.native="onActiveItem(regionGroup, index)" @quit="quitRegionGroup(regionGroup)">
          </region-group-item>
        </div>
      </el-scrollbar>
    </el-aside>
    <el-container class="chat-box">
      <drawer
          :visible="mapVisible" :width=80 @close="closeMapVisible" :zIndex=6 :show-close="false">
        <template v-slot:main>
          <div class="region-map">
            <div class="tree-side">
              <div class="tree-aside">
                <el-tree
                    node-key="code"
                    :lazy="true"
                    :load="loadNode"
                    @node-click="nodeClickEvent"
                    :expand-on-click-node="false">
                <span class="custom-tree-node" slot-scope="{ node }" style="text-align: left">
                  <span>{{ node.label }}</span>
                </span>
                </el-tree>
              </div>
              <div class="tree-footer">
                <div>当前选中：{{curNode.name}}</div>
                <div style="display: flex;align-items: center;">
                  <el-button type="text" size="medium" @click="() => joinRegionGroup(curNode, 0)">
                    临时加入
                  </el-button>
                  <el-button type="text" size="medium" @click="() => joinRegionGroup(curNode, 1)">
                    加入常驻
                  </el-button>
                  <div class="tips">
                    <el-popover
                        placement="top-start"
                        width="200"
                        trigger="hover"
                        content="临时加入的地区群聊有效时长限制2小时，到期自动退出">
                      <div class="el-icon-question" slot="reference"></div>
                    </el-popover>
                  </div>
                </div>
                <div>
                  <el-button type="text" size="medium" @click="viewActivityRegions">
                    活跃地区
                  </el-button>
                </div>
                <div class="region-space" @click="openRegionSpace">
                  <svg class="icon svg-icon" aria-hidden="true">
                    <use xlink:href="#icon-shejiaotubiao-40"></use>
                  </svg>
                  <span style="color: orange;margin-left: 8px;font-size: 16px;">地区空间</span>
                </div>
              </div>
            </div>
            <div class="map-aside">
              <gao-de-map ref="GaoDeMap"></gao-de-map>
              <drawer
                  :visible="regionSpaceVisible"
                  @close="closeDrawer"
                  :zIndex=19
                  :width=50>
                <template v-slot:header>
                  <space-cover :name="'地区空间'" @refresh="refreshTalkList" :show-add="false" :show-notify="false"></space-cover>
                </template>
                <template v-slot:main>
                  <talk-list ref="talkListRef" :category="'region'" :section="'region'" :region-code="curNode?.code"></talk-list>
                </template>
              </drawer>
            </div>
          </div>
        </template>
      </drawer>
      <el-dialog  :visible="activityRegionsVisible" width="450px" title="活跃地区" :before-close="closeActivityRegions">
        <div class="activity-regions">
          <el-scrollbar style="height: 400px;">
            <div v-for="(region, index) in activityRegions" :key="index" class="region-item">
              <div>{{region.fullName}}</div>
              <el-button size="small" :type="regionActiveIndex === index ? 'success' : ''" icon="el-icon-check" circle @click="chooseActivityRegion(region, index)"></el-button>
            </div>
          </el-scrollbar>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="() => joinRegionGroup(activityRegion, 0)">临时加入</el-button>
          <el-button type="primary" @click="() => joinRegionGroup(activityRegion, 1)">加入常驻</el-button>
        </span>
      </el-dialog>
      <region-chat-box v-if="regionGroupStore.activeRegionChat && regionGroupStore.activeRegionChat.targetId === regionGroupStore.activeRegionGroup.id"
                       :chat="regionGroupStore.activeRegionChat"></region-chat-box>
    </el-container>
  </el-container>
</template>

<script>
import RegionGroupItem from "@/components/regionGroup/RegionGroupItem";
import RegionChatBox from "@/components/chat/RegionChatBox";
import MapLayer from "@/components/common/MapLayer";
import GaoDeMap from "@/components/regionGroup/GaoDeMap";
import TalkList from "@/components/talk/TalkList";
import Drawer from "@/components/common/Drawer";
import SpaceCover from "@/components/common/SpaceCover";

export default {
  name: "RegionGroup",
  components: {
    MapLayer,
    RegionGroupItem,
    RegionChatBox,
    GaoDeMap,
    TalkList,
    Drawer,
    SpaceCover,
  },
  data() {
    return {
      searchText: "",
      mapVisible: false,
      dialogVisible: false,
      regionSpaceVisible: false,
      activeRegionGroup: {},
      regionGroupMembers: [],
      regionTree: [],
      curNode: {
        longitude: 116.397428,
        latitude: 39.90923,
      },
      lngLat: {
        lng: 116.397428,
        lat: 39.90923,
      },
      activityRegion: {},
      regionActiveIndex: -1,
      activityRegions: [],
      activityRegionsVisible: false,
    }
  },
  computed: {
    regionGroupStore() {
      return this.$store.state.regionGroupStore;
    },
    loading(){
      return this.regionGroupStore.loadingRegionGroupMsg
    }
  },
  created() {

  },
  methods: {
    onActiveItem(regionGroup, index) {
      this.$store.commit("activeRegionGroup", index);
      this.activeRegionGroup = JSON.parse(JSON.stringify(regionGroup));
      let chat = {
        type: 'REGION-GROUP',
        targetId: regionGroup.id,
        showName: regionGroup.regionGroupName,
        headImage: null,
      };
      this.$store.commit("openRegionChat", chat);
      // store数据不能直接修改，所以深拷贝一份内存
      this.$store.commit("activeRegionChatById", regionGroup.id);
    },
    loadRegionGroupMembers() {
      this.$http({
        url: `/region/group/members/${this.activeRegionGroup.id}`,
        method: "get"
      }).then((members) => {
        this.regionGroupMembers = members;
      })
    },
    chooseRegion() {
      this.mapVisible = !this.mapVisible;
      this.regionSpaceVisible = false;
      if (this.mapVisible) {
        this.$refs.GaoDeMap.initAMap(this.curNode.longitude, this.curNode.latitude);
      }
    },
    loadNode(node, resolve) {
      this.getTreeData(node, resolve)
    },
    getTreeData(node, resolve) {
      let parentCode = node?.data?.code;
      let url = '/region/findSubRegion?parentCode=';
      if (parentCode) {
        url = url + parentCode
      }
      this.$http({
        url: url,
        method: "get"
      }).then((regionGroups) => {
        if (regionGroups.length > 0) {
          let arr = new Array();
          for (let i = 0; i < regionGroups.length; i++) {
            let regionGroup = regionGroups[i];
            let obj = {
              code: regionGroup.code,
              parentCode: regionGroup.parentCode,
              label: regionGroup.name,
              name: regionGroup.name,
              latitude: regionGroup.latitude,
              longitude: regionGroup.longitude,
              level: regionGroup.level,
              children: []
            }
            arr.push(obj);
          }
          if (!node) {
            return resolve(arr); // resolve是一个函数，它会接受一个数组，作为你要展开的节点的孩子节点，如果没有孩子节点，直接传一个空数组
          } else {
            return resolve(arr); // 如果需要设置一个树节点，也可以在这里设置，比如说设置这个孩子节点是叶子节点（也就是不能展开的末节点），可以给其中的数据设置item.isLeaf = true
          }
        } else {
          return resolve([])
        }
      })
    },
    joinRegionGroup(node, joinType) {
      let params = {
        code: node.code,
        name: node.name,
        joinType: joinType
      }
      this.$http({
        url: "/region/group/join",
        method: "post",
        data: params
      }).then((regionGroup) => {
        //this.$store.commit("addRegionGroup", regionGroup);
        this.$message.success("加入成功");
      })
    },
    quitRegionGroup(regionGroup) {
      let params = {
        id: regionGroup.id,
        code: regionGroup.code,
        num: regionGroup.num,
        joinType: regionGroup.joinType
      }
      this.$http({
        url: "/region/group/quit",
        method: "post",
        data: params
      }).then(() => {
        this.$store.commit("removeRegionGroup", regionGroup.id);
        this.$store.commit("removeRegionGroupChat", regionGroup.id);
        //this.reset();
        this.$message.success("操作成功");
      })
    },
    reset(){
      //this.activeGroup={};
      //this.groupMembers=[];
    },
    nodeClickEvent(data, node) {
      this.curNode = data;
      if (data.longitude && data.latitude) {
        this.$refs.GaoDeMap.initAMap(data.longitude, data.latitude);
        //this.lngLat = {lng: data.longitude, lat: data.latitude};
      } else {
        this.getRegionLngLat(data, node);
      }
    },
    getRegionLngLat(data, node) {
      this.$http({
        url: "/region/getRegionLonAndLat?code=" + data.code,
        method: "get",
      }).then((region) => {
        if(region.longitude, region.latitude) {
          node.data.longitude = region.longitude;
          node.data.latitude = region.latitude;
          this.$refs.GaoDeMap.initAMap(region.longitude, region.latitude);
        }
      })
    },
    openRegionSpace() {
      if (this.curNode && this.curNode.code) {
        this.regionSpaceVisible = true;
      } else {
        this.$message.warning('请先选择一个地区');
      }
    },
    refreshTalkList() {
      this.$refs.talkListRef.refreshTalkList();
    },
    closeMapVisible() {
      this.mapVisible = false;
    },
    closeDrawer() {
      this.regionSpaceVisible = false;
    },
    viewActivityRegions() {
      if (this.activityRegions.length > 0) {
        this.activityRegionsVisible = true;
      } else {
        this.$http({
          url: '/region/findActivityRegions',
          method: 'get'
        }).then((data) => {
          this.activityRegionsVisible = true;
          this.activityRegions = data;
        })
      }
    },
    chooseActivityRegion(region, index) {
      this.activityRegion = region;
      this.regionActiveIndex = index;
      this.$refs.GaoDeMap.initAMap(region.longitude, region.latitude);
    },
    closeActivityRegions() {
      this.activityRegionsVisible = false;
      this.activityRegion = {};
      this.regionActiveIndex = -1;
    },
  },
}
</script>

<style scoped lang="scss">
  .chat-page {
    .aside {
      display: flex;
      flex-direction: column;
      background: white;
      border-right: 1px solid #cccccc;

      .header {
        background-color: #FFFAFA;
        color: orange;
        width: 98%;
        line-height: 30px;
        height: 30px;
        border: 1px solid #6CC6CB;
        margin: 5px auto;
        border-radius: 6px;
        cursor: pointer;
      }

      .group-list-items {
        flex: 1;
      }
    }

    .region-map {
      display: flex;
      height: 100vh;
      position: relative;
      overflow: hidden;
      box-sizing: border-box;

      .tree-side {
        width: 15%;
        margin-top: 10px;
        height: 100%;
        display: flex;
        flex-direction: column;
        overflow: auto;

        .tree-aside {
          width: 100%;
          min-height: 600px;
          box-sizing: content-box;
          flex: 1;
          overflow-y: auto;
          flex-shrink: 0;
        }

        .tree-footer {
          flex: 1;
          flex-shrink: 0;
          width: 100%;
          height: 150px;
          text-align: left;
          margin-top: 5px;
          overflow: auto;
          padding: 5px;
          box-shadow: 0 -2px 5px rgba(0,0,0,0.1);

          .tips {
            margin-left: 10px;
            width: 36px;
            height: 36px;
            line-height: 36px;
          }
        }
      }

      .map-aside {
        width: 85%;
        height: 100%;
        overscroll-behavior: contain;
        overflow: auto;
      }
    }

    .activity-regions {
      .region-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-top: 10px;
        color: orange;
        border-bottom: 1px solid lightgray;
        padding-bottom: 5px;
      }
    }

    .region-space {
      display: flex;
      justify-content: left;
      align-items: center;
      cursor: pointer;

      .icon {
        display: block;
        height: 30px;
        line-height: 30px;
        font-size: 28px;
        color: #333;
        -webkit-transition: font-size 0.25s linear, width 0.25s linear;
        -moz-transition: font-size 0.25s linear, width 0.25s linear;
        transition: font-size 0.25s linear, width 0.25s linear;
      }
    }
  }
</style>