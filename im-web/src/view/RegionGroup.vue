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
                <div class="current-selection">
                  <span>当前选中：{{curNode.name}}</span>
                  <div class="tips">
                    <el-popover
                        placement="top-start"
                        width="200"
                        trigger="hover"
                        content="临时加入的地区群聊有效时长限制2小时，到期自动退出">
                      <div class="el-icon-question" slot="reference"></div>
                    </el-popover>
                  </div></div>
                <div class="join-buttons">
                  <el-button type="text" size="medium" @click="() => joinRegionGroup(curNode, 0)">
                    临时加入
                  </el-button>
                  <el-button type="text" size="medium" @click="() => joinRegionGroup(curNode, 1)">
                    加入常驻
                  </el-button>
                </div>
                <div class="other-buttons">
                  <el-button type="text" size="medium" @click="viewActivityRegions">
                    活跃地区
                  </el-button>
                  <el-button type="text" size="medium" @click="viewRegionGroups">
                    地区群聊
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
                  <space-cover :name="spaceName" @refresh="refreshTalkList" :show-add="false" :show-notify="false"></space-cover>
                </template>
                <template v-slot:main>
                  <talk-list ref="talkListRef" :category="'region'" :section="'region'" :region-code="curNode?.code"></talk-list>
                </template>
              </drawer>
            </div>
          </div>
        </template>
      </drawer>
      <el-dialog  :visible="activityRegionsVisible" width="500px" title="活跃地区" :before-close="closeActivityRegions">
        <div class="activity-regions">
          <el-scrollbar style="height: 400px;">
            <div v-for="(region, index) in activityRegions" :key="index" class="region-item" :class="{'region-item-active': regionActiveIndex === index}" @click="chooseActivityRegion(region, index)">
              <div class="region-info">
                <div class="region-name">{{region.fullName}}</div>
              </div>
              <el-button class="region-select-btn" size="small" :type="regionActiveIndex === index ? 'success' : 'info'" icon="el-icon-check" circle></el-button>
            </div>
          </el-scrollbar>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="closeActivityRegions" size="small">取消</el-button>
          <el-button @click="() => joinRegionGroup(activityRegion, 0)" size="small">临时加入</el-button>
          <el-button type="primary" @click="() => joinRegionGroup(activityRegion, 1)" size="small">加入常驻</el-button>
        </span>
      </el-dialog>
      <el-dialog  :visible="regionGroupsVisible" width="450px" title="地区群聊" :before-close="closeRegionGroups">
        <div class="region-groups">
          <el-scrollbar style="height: 400px;">
            <div v-for="(group, index) in regionGroupList" :key="index" class="region-group-item">
              <div class="region-group-name">{{group.regionGroupName}}</div>
              <el-button size="small" :type="regionGroupActiveIndex === index ? 'success' : ''" icon="el-icon-check" circle @click="() => chooseActivityRegionGroup(group, index)"></el-button>
            </div>
          </el-scrollbar>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button  @click="() => joinTargetRegionGroup(0)" size="small">临时加入</el-button>
          <el-button type="primary" @click="() => joinTargetRegionGroup(1)" size="small">加入常驻</el-button>
        </span>
      </el-dialog>
      <region-chat-box v-if="regionGroupStore.activeRegionChat && regionGroupStore.activeRegionChat.targetId === regionGroupStore.activeRegionGroup.id"
                       :chat="regionGroupStore.activeRegionChat">
      </region-chat-box>
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
      regionGroupsVisible: false,
      regionGroupList: [], // 地区群列表数据
      regionGroupActiveIndex: -1,
      chooseRegionGroup: {},
    }
  },
  computed: {
    regionGroupStore() {
      return this.$store.state.regionGroupStore;
    },
    loading(){
      return this.regionGroupStore.loadingRegionGroupMsg
    },
    spaceName() {
      return this.curNode && this.curNode.name ? this.curNode.name : "地区空间";
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
      if (!node || !node.code) {
        this.$message.warning("请先选择地区");
        return;
      }
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
      if (this.regionSpaceVisible) {
        this.refreshTalkList();
      }
    },
    getRegionLngLat(data, node) {
      this.$http({
        url: "/region/getRegionLonAndLat?code=" + data.code,
        method: "get",
      }).then((region) => {
        if(region.longitude && region.latitude) {
          node.data.longitude = region.longitude;
          node.data.latitude = region.latitude;
          this.$refs.GaoDeMap.initAMap(region.longitude, region.latitude);
        }
      })
    },
    getRegionLngAndLat(region) {
      this.$http({
        url: "/region/getRegionLonAndLat?code=" + region.code,
        method: "get",
      }).then((data) => {
        if(data.longitude && data.latitude) {
          this.$refs.GaoDeMap.initAMap(data.longitude, data.latitude);
        }
      })
    },
    openRegionSpace() {
      if (this.curNode && this.curNode.code) {
        this.regionSpaceVisible = true;
        // 使用 $nextTick 确保所有 props 已更新到子组件
        this.$nextTick(() => {
          this.refreshTalkList();
        })
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
    viewRegionGroups() {
      if (!this.curNode || !this.curNode.code) {
        this.$message.warning("请先选择地区");
        return;
      }
      this.loadRegionGroupList(this.curNode.code)
    },
    chooseActivityRegion(region, index) {
      this.activityRegion = region;
      this.regionActiveIndex = index;
      if (region.longitude && region.latitude) {
        this.$refs.GaoDeMap.initAMap(region.longitude, region.latitude);
      } else {
        this.getRegionLngAndLat(region);
      }
    },
    chooseActivityRegionGroup(group, index) {
      this.regionGroupActiveIndex = index;
      this.chooseRegionGroup = group;
    },
    closeActivityRegions() {
      this.activityRegionsVisible = false;
      this.activityRegion = {};
      this.regionActiveIndex = -1;
    },
    closeRegionGroups() {
      this.regionGroupsVisible = false;
      this.regionGroupActiveIndex = -1;
      this.chooseRegionGroup = {};
    },
    loadRegionGroupList(regionCode) {
      this.$http({
        url: `/region/group/findRegionGroupsByCode?code=${regionCode}`,
        method: 'get',
      }).then((data) => {
        this.regionGroupList = data;
        this.regionGroupsVisible = true;
      }).catch((e) => {
        this.$message.error('查询失败');
      })
    },
    joinTargetRegionGroup(joinType) {
      if (!this.chooseRegionGroup.id) {
        this.$message.warning("请先选择一个群聊");
        return;
      }
      let params = {
        id: this.chooseRegionGroup.id,
        code: this.chooseRegionGroup.code,
        num: this.chooseRegionGroup.num,
        joinType: joinType
      }
      this.$http({
        url: `/region/group/joinTarget`,
        method: 'post',
        data: params
      }).then((data) => {
        this.$message.success("加入成功");
      }).catch((e) => {
        this.$message.error('加入失败');
      })
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
        background: linear-gradient(135deg, #ffedd5 0%, #fef3c7 100%);
        color: #c2410c;
        width: 98%;
        line-height: 36px;
        height: 36px;
        border: 1px solid #fed7aa;
        margin: 8px auto;
        border-radius: 8px;
        cursor: pointer;
        text-align: center;
        font-weight: 500;
        font-size: 14px;
        transition: all 0.3s ease;
        
        &:hover {
          background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
          transform: translateY(-2px);
          box-shadow: 0 4px 8px rgba(254, 215, 170, 0.3);
        }
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
      background: #f1f5f9;

      .tree-side {
        width: 20%;
        min-width: 220px;
        margin-top: 10px;
        height: 100%;
        display: flex;
        flex-direction: column;
        overflow: auto;
        padding: 10px;
        background: #ffffff;
        box-shadow: 4px 0 6px rgba(0, 0, 0, 0.05);

        .tree-aside {
          width: 100%;
          min-height: 600px;
          box-sizing: content-box;
          flex: 1;
          overflow-y: auto;
          flex-shrink: 0;
          padding: 10px 5px;
          
          :deep(.el-tree) {
            background: transparent;
            
            .el-tree-node__content {
              height: 36px;
              
              &:hover {
                background-color: #f1f5f9;
              }
            }
          }
          
          .custom-tree-node {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 14px;
            padding-right: 15px;
          }
        }

        .tree-footer {
          flex: 1;
          flex-shrink: 0;
          width: 100%;
          min-height: 200px;
          text-align: left;
          margin-top: 5px;
          overflow: auto;
          padding: 15px;
          background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
          border-radius: 12px;
          box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05), 0 1px 3px rgba(0, 0, 0, 0.05);
          border: 1px solid #e2e8f0;

          > div {
            margin-bottom: 12px;
            
            &:last-child {
              margin-bottom: 0;
            }
          }

          .current-selection {
            align-items: center;
            display: flex;
            font-size: 14px;
            font-weight: 500;
            color: #334155;
            padding: 8px 12px;
            background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
            border-radius: 8px;
            margin-bottom: 10px;

            .tips {
              margin-left: 8px;
              width: 30px;
              height: 30px;
              line-height: 30px;
              display: flex;
              align-items: center;
              justify-content: center;
              border-radius: 50%;
              background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
              color: #0ea5e9;
              cursor: pointer;

              &:hover {
                background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
              }
            }
          }

          .join-buttons {
            display: flex;
            align-items: center;
            gap: 10px;
            
            .el-button {
              border-radius: 8px;
              padding: 8px 16px;
              font-size: 14px;
              font-weight: 500;
              
              &.el-button--text {
                color: #3b82f6;
                background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
                border: 1px solid #93c5fd;
                
                &:hover {
                  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
                  color: #1d4ed8;
                }
              }
            }
          }

          .other-buttons {
            display: flex;
            align-items: center;
            gap: 10px;
            
            .el-button {
              border-radius: 8px;
              padding: 8px 16px;
              font-size: 14px;
              font-weight: 500;
              
              &.el-button--text {
                color: #10b981;
                background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
                border: 1px solid #86efac;
                
                &:hover {
                  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
                  color: #047857;
                }
              }
            }
          }

          .region-space {
            display: flex;
            justify-content: flex-start;
            align-items: center;
            cursor: pointer;
            padding: 10px;
            border-radius: 10px;
            background: linear-gradient(135deg, #ffedd5 0%, #fed7aa 100%);
            border: 1px solid #fed7aa;
            transition: all 0.3s ease;
            margin-top: 10px;
            
            &:hover {
              background: linear-gradient(135deg, #fed7aa 0%, #fdba74 100%);
              transform: translateY(-2px);
              box-shadow: 0 4px 8px rgba(254, 215, 170, 0.3);
            }
            
            .icon {
              display: block;
              height: 30px;
              line-height: 30px;
              font-size: 24px;
              color: #c2410c;
              -webkit-transition: font-size 0.25s linear, width 0.25s linear;
              -moz-transition: font-size 0.25s linear, width 0.25s linear;
              transition: font-size 0.25s linear, width 0.25s linear;
            }
            
            span {
              color: #c2410c;
              margin-left: 8px;
              font-size: 15px;
              font-weight: 500;
            }
          }
        }
      }

      .map-aside {
        width: 80%;
        height: 100%;
        overscroll-behavior: contain;
        overflow: auto;
        
        :deep(.amap-container) {
          height: 100%;
        }
      }
    }

    .activity-regions {
      .region-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin: 8px 0;
        padding: 15px;
        background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
        border-radius: 12px;
        cursor: pointer;
        transition: all 0.3s ease;
        border: 1px solid #bae6fd;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          background: linear-gradient(135deg, #e6f7ff 0%, #d1eaff 100%);
        }
        
        .region-info {
          flex: 1;
          
          .region-name {
            font-size: 15px;
            font-weight: 500;
            color: #1d4ed8;
            margin-bottom: 5px;
          }
        }
        
        .region-select-btn {
          margin-left: 10px;
        }
      }
      
      .region-item-active {
        background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
        border: 1px solid #86efac;
        
        .region-name {
          color: #166534;
        }
        
        &:hover {
          background: linear-gradient(135deg, #bbf7d0 0%, #a7f3d0 100%);
        }
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
    
    .region-groups {
      padding: 10px;
      
      .region-group-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin: 10px 0;
        padding: 15px;
        background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
        border-radius: 12px;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05), 0 1px 3px rgba(0, 0, 0, 0.05);
        transition: all 0.3s ease;
        border: 1px solid #fed7aa;
        cursor: pointer;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1), 0 2px 4px rgba(0, 0, 0, 0.05);
          background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
        }

        .region-group-name {
          font-size: 16px;
          font-weight: 500;
          color: #c2410c;
          flex: 1;
          padding-right: 15px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
    
    :deep(.el-dialog) {
      border-radius: 16px;
      overflow: hidden;
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
      
      .el-dialog__header {
        background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
        padding: 15px 20px;
        border-radius: 16px 16px 0 0;
      }
      
      .el-dialog__body {
        padding: 20px;
      }
      
      .el-dialog__footer {
        background: #f8fafc;
        padding: 15px;
        border-radius: 0 0 16px 16px;
      }
    }
  }
</style>