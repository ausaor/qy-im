<template>
  <div class="region-management">
    <!-- 查询条件区域 -->
    <el-form :inline="true" :model="searchForm" class="search-form" label-width="80px">
      <el-form-item label="地区编码" prop="code">
        <el-input size="mini" :style="{ width: '120px' }" v-model="searchForm.code" placeholder="请输入地区编码" clearable></el-input>
      </el-form-item>
      <el-form-item label="地区名称" prop="name">
        <el-input size="mini" :style="{ width: '120px' }" v-model="searchForm.name" placeholder="请输入地区名称" clearable></el-input>
      </el-form-item>
      <el-form-item label="地区级别" prop="level">
        <el-select v-model="searchForm.level" :style="{ width: '120px' }" placeholder="请选择地区级别" size="mini" clearable>
          <el-option label="省" value="1"></el-option>
          <el-option label="市" value="2"></el-option>
          <el-option label="县/区" value="3"></el-option>
          <el-option label="镇/乡" value="4"></el-option>
          <el-option label="村" value="5"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="上级编码" prop="parentCode">
        <el-input size="mini" :style="{ width: '120px' }" v-model="searchForm.parentCode" placeholder="请输入上级地区编码" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button size="mini" type="primary" @click="handleSearch">查询</el-button>
        <el-button size="mini" @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 地区列表 -->
    <el-table
        :data="regionList"
        border
        row-key="code"
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        v-loading="listLoading"
        lazy
        :load="handleExpand"
        element-loading-text="加载中..."
    >
      <el-table-column type="index" label="序号" width="60"></el-table-column>
      <el-table-column prop="code" align="left" label="地区编码" min-width="210">
        <template slot-scope="scope">
          <span class="code-link">
            {{ scope.row.code }}
          </span>
<!--          <i
              v-if="scope.row.hasChildren"
              class="el-icon-arrow-right"
              :class="{ 'rotate-icon': scope.row.expanded }"
              @click.stop="handleExpand(scope.row)"
          ></i>-->
        </template>
      </el-table-column>
      <el-table-column prop="name" label="地区名称" min-width="150"></el-table-column>
      <el-table-column prop="parentCode" label="上级地区编码" min-width="120"></el-table-column>
      <el-table-column prop="level" align="center" label="地区级别" min-width="100">
        <template slot-scope="scope">
          {{ formatLevel(scope.row.level) }}
        </template>
      </el-table-column>
      <el-table-column prop="longitude" label="经度" min-width="100"></el-table-column>
      <el-table-column prop="latitude" label="纬度" min-width="100"></el-table-column>
      <el-table-column prop="isBanned" align="center" label="是否禁言" min-width="90">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.isBanned ? 'danger' : 'success'">
            {{ scope.row.isBanned ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="120" fixed="right">
        <template slot-scope="scope">
          <el-button
              size="mini"
              type="text"
              @click="handleBan(scope.row)"
              v-if="!scope.row.isBanned"
          >
            <span style="color: red;">禁言</span>
          </el-button>
          <el-button
              size="mini"
              type="text"
              @click="handleUnban(scope.row)"
              v-if="scope.row.isBanned"
          >
            解除
          </el-button>
          <el-button
              size="mini"
              type="text"
              @click="openRegionGroupDrawer(scope.row)"
          >
            <span style="color: orange;">地区群</span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
      ></el-pagination>
    </div>

    <!-- 地区群列表抽屉 -->
    <el-drawer
        title="地区群列表"
        :visible.sync="regionGroupDrawerVisible"
        :size="1200"
        direction="rtl"
        :before-close="handleGroupDrawerClose"
    >
      <el-table
          :data="regionGroupList"
          border
          row-key="num"
          v-loading="groupLoading"
          element-loading-text="加载中..."
      >
        <el-table-column prop="regionGroupName" label="群名称" min-width="150"></el-table-column>
        <el-table-column prop="num" label="群编号" width="90" align="center"></el-table-column>
        <el-table-column prop="groupAdmin" label="群主" width="120" align="center"></el-table-column>
        <el-table-column prop="effectiveTime" label="群主生效时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.effectiveTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="expirationTime" label="群主失效时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.expirationTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="isBanned" label="是否禁言" align="center" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.isBanned ? 'danger' : 'success'">
              {{ scope.row.isBanned ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注"></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button
                size="mini"
                type="text"
                @click="handleGroupBan(scope.row)"
                v-if="!scope.row.isBanned"
            >
              <span style="color: red;">禁言</span>
            </el-button>
            <el-button
                size="mini"
                type="text"
                @click="handleGroupUnban(scope.row)"
                v-if="scope.row.isBanned"
            >
              解除
            </el-button>
            <el-button
                size="mini"
                type="text"
                @click="openGroupMemberDrawer(scope.row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <!-- 地区群成员列表抽屉 -->
    <el-drawer
        title="地区群成员列表"
        :visible.sync="groupMemberDrawerVisible"
        :size="810"
        direction="rtl"
        :before-close="handleMemberDrawerClose"
    >
      <el-table
          :data="groupMemberList"
          border
          row-key="userName"
          v-loading="memberLoading"
          element-loading-text="加载中..."
      >
        <el-table-column prop="userName" label="用户名" width="150" align="left">
          <template slot-scope="scope">
            <div class="group-name-cell">
              <span>{{ scope.row.userName }}</span>
              <el-tag size="mini" v-if="scope.row.isLeader">群主</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="aliasName" label="群员昵称" width="120" align="center"></el-table-column>
        <el-table-column prop="headImage" label="头像" align="center" width="80">
          <template slot-scope="scope">
            <div class="avatar-cell">
              <head-image :url="scope.row.headImage" :name="scope.row.aliasName" :id="scope.row.userId" :online="scope.row.online" :size="40"></head-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="加入时间" width="160">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="joinType" label="加入方式" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" type="success" v-if="scope.row.joinType===1">常驻</el-tag>
            <el-tag size="mini" type="warning" v-if="scope.row.joinType===0">临时</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isBanned" label="是否禁言" align="center" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.isBanned ? 'danger' : 'success'">
              {{ scope.row.isBanned ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button
                size="mini"
                type="danger"
                @click="handleMemberBan(scope.row)"
                v-if="!scope.row.isBanned"
            >
              禁言
            </el-button>
            <el-button
                size="mini"
                type="success"
                @click="handleMemberUnban(scope.row)"
                v-if="scope.row.isBanned"
            >
              解除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script>
import HeadImage from "@components/common/HeadImage.vue";

export default {
  name: 'RegionsManagement',
  components: {HeadImage},
  data() {
    return {
      // 查询表单
      searchForm: {
        code: '',
        name: '',
        level: '1', // 默认查询1级地区
        parentCode: ''
      },
      // 地区列表数据
      regionList: [],
      // 列表加载状态
      listLoading: false,
      // 分页信息
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      // 地区群抽屉显示状态
      regionGroupDrawerVisible: false,
      // 当前选中的地区
      currentRegion: null,
      // 地区群列表数据
      regionGroupList: [],
      // 地区群加载状态
      groupLoading: false,
      // 地区群成员抽屉显示状态
      groupMemberDrawerVisible: false,
      // 当前选中的地区群
      currentRegionGroup: null,
      // 地区群成员列表数据
      groupMemberList: [],
      // 成员列表加载状态
      memberLoading: false
    }
  },
  created() {
    // 初始化加载数据
    this.loadRegionList();
  },
  methods: {
    // 加载地区列表
    loadRegionList() {
      this.listLoading = true;

      this.$http({
        url: `/region/page?pageNo=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`,
        method: "post",
        data: this.searchForm
      }).then((data) => {
        this.regionList = data.data;
        this.pagination.total = data.total; // 模拟总数
      }).finally(() => {
        this.listLoading = false;
      })
    },

    // 处理查询
    handleSearch() {
      this.pagination.currentPage = 1;
      this.loadRegionList();
    },

    // 重置表单
    resetForm() {
      this.searchForm = {
        code: '',
        name: '',
        level: '1',
        parentCode: ''
      };
      this.pagination.currentPage = 1;
      this.loadRegionList();
    },

    // 处理分页大小变化
    handleSizeChange(size) {
      this.pagination.pageSize = size;
      this.loadRegionList();
    },

    // 处理页码变化
    handleCurrentChange(page) {
      this.pagination.currentPage = page;
      this.loadRegionList();
    },

    // 格式化地区级别
    formatLevel(level) {
      const levelMap = {
        '1': '省',
        '2': '市',
        '3': '县/区',
        '4': '镇/乡',
        '5': '村'
      };
      return levelMap[level] || level;
    },
    // 处理展开/折叠下级地区
    handleExpand(row, treeNode, resolve) {
      // 如果已经展开，则折叠
      if (treeNode.expanded) {
        treeNode.expanded = false;
        return;
      }

      // 懒加载下级数据
      treeNode.loading = true;

      this.$http({
        url: `/region/findSubRegion?parentCode=${row.code}`,
        method: "get",
      }).then((data) => {
        resolve(data);
      }).finally(() => {
        treeNode.loading = false;
        treeNode.expanded = true;
      })
    },

    // 处理地区禁言
    handleBan(row) {
      try {
        this.$prompt('请输入禁言时长（小时）', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /^([1-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|60000)$/,
          inputErrorMessage: '只能输入正整数(1~60000)'
        }).then(({ value }) => {
          let param = {
            codes: [row.code],
            banDuration: value,
            banType: 'sys'
          }
          this.$http({
            url: '/region/banMsg',
            method: 'post',
            data: param
          }).then(() => {
            row.isBanned = true;
            this.$message({
              type: 'success',
              message: '禁言成功!'
            });
          }).catch((e) => {
            this.$message.error('操作失败');
          })
        })
      } catch (e) {
        this.$message.error('操作失败');
      }
    },

    // 处理地区解除禁言
    handleUnban(row) {
      let param = {
        codes: [row.code],
        banDuration: 1,
        banType: 'sys'
      }
      this.$http({
        url: '/region/unBanMsg',
        method: 'post',
        data: param
      }).then(() => {
        row.isBanned = false;
        this.$message({
          type: 'success',
          message: '解除成功!'
        });
      }).catch((e) => {
        this.$message.error('操作失败');
      })
    },

    // 打开地区群抽屉
    openRegionGroupDrawer(region) {
      this.currentRegion = region;
      this.regionGroupDrawerVisible = true;

      // 加载地区群数据
      this.loadRegionGroupList(region.code);
    },

    // 加载地区群列表
    loadRegionGroupList(regionCode) {
      this.groupLoading = true;
      this.$http({
        url: `/region/group/findRegionGroupsByCode?code=${regionCode}`,
        method: 'get',
      }).then((data) => {
        this.regionGroupList = data;
      }).catch((e) => {
        this.$message.error('查询失败');
      }).finally(() => {
        this.groupLoading = false;
      })
    },

    // 关闭地区群抽屉时的回调
    handleGroupDrawerClose(done) {
      this.groupMemberDrawerVisible = false;
      // 延迟关闭，确保子抽屉先关闭
      setTimeout(() => {
        done();
      }, 300);
    },

    // 处理地区群禁言
    handleGroupBan(row) {
      try {
        this.$prompt('请输入禁言时长（小时）', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /^([1-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|60000)$/,
          inputErrorMessage: '只能输入正整数(1~60000)'
        }).then(({ value }) => {
          let param = {
            code: row.code,
            num: row.num,
            id: row.id,
            allBanned: true,
            banDuration: value,
            banType: 'sys'
          }

          this.$http({
            url: '/region/group/banMsg',
            method: 'post',
            data: param
          }).then(() => {
            row.isBanned = true;
            this.$message.success("操作成功");
          }).catch((e) => {
            this.$message.error('操作失败');
          })
        })
      } catch (e) {
        this.$message.error('操作失败');
      }
    },

    // 处理地区群解除禁言
    handleGroupUnban(row) {
      let param = {
        code: row.code,
        num: row.num,
        id: row.id,
        allBanned: true,
        banDuration: 1,
        banType: 'sys'
      }

      this.$http({
        url: '/region/group/unBanMsg',
        method: 'post',
        data: param
      }).then(() => {
        row.isBanned = false;
        this.$message.success("操作成功");
      }).catch((e) => {
        this.$message.error('操作失败');
      })
    },

    // 打开地区群成员抽屉
    openGroupMemberDrawer(group) {
      this.currentRegionGroup = group;
      this.groupMemberDrawerVisible = true;

      // 加载地区群成员数据
      this.loadGroupMemberList(group.id);
    },

    // 加载地区群成员列表
    loadGroupMemberList(groupId) {
      this.memberLoading = true;
      this.$http({
        url: `/region/group/members/${groupId}`,
        method: 'get',
      }).then((data) => {
        if (data && data.length > 0) {
          this.groupMemberList = data.filter((item) => !item.quit);
        }
      }).catch((e) => {
        this.$message.error('查询失败');
      }).finally(() => {
        this.memberLoading = false;
      })
    },

    // 关闭成员抽屉时的回调
    handleMemberDrawerClose(done) {
      done();
    },

    // 格式化加入方式
    formatJoinType(type) {
      return type === 0 ? '临时' : '常驻';
    },

    // 处理成员禁言
    handleMemberBan(row) {
      try {
        this.$prompt('请输入禁言时长（小时）', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /^([1-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|60000)$/,
          inputErrorMessage: '只能输入正整数(1~60000)'
        }).then(({ value }) => {
          let param = {
            code: this.currentRegionGroup.code,
            num: this.currentRegionGroup.num,
            id: this.currentRegionGroup.id,
            userId: row.userId,
            joinType: row.joinType,
            aliasName: row.aliasName,
            banDuration: value,
            allBanned: false,
            banType: 'sys'
          }

          this.$http({
            url: "/region/group/banMsg",
            method: 'post',
            data: param
          }).then(() => {
            row.isBanned = true;
            this.$message.success("操作成功");
          }).catch((e) => {
            this.$message.error('操作失败');
          })
        })
      } catch (e) {
        this.$message.error('操作失败');
      }
    },

    // 处理成员解除禁言
    handleMemberUnban(row) {
      let param = {
        code: this.currentRegionGroup.code,
        num: this.currentRegionGroup.num,
        id: this.currentRegionGroup.id,
        userId: row.userId,
        joinType: row.joinType,
        aliasName: row.aliasName,
        banDuration: 1,
        allBanned: false,
        banType: 'sys'
      }

      this.$http({
        url: "/region/group/unBanMsg",
        method: 'post',
        data: param
      }).then(() => {
        row.isBanned = false;
        this.$message.success("操作成功");
      }).catch((e) => {
        this.$message.error('操作失败');
      })
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return '';

      // 如果是字符串，转换为Date对象
      if (typeof date === 'string') {
        date = new Date(date);
      }

      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');

      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }
  }
}
</script>

<style lang="scss" scoped>
.region-management {
  padding: 15px;
  background-color: #fff;
  min-height: calc(100% - 40px);
}

.search-form {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.code-link {
  color: #409eff;
  cursor: pointer;
  text-decoration: underline;
}

.group-name-cell {
  display: flex;
  align-items: center;
  justify-content: left;
}

.avatar-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.rotate-icon {
  transform: rotate(90deg);
  transition: transform 0.3s ease;
}

::v-deep .el-form-item{
  margin-bottom: 0;
}

::v-deep .el-table__expand-icon {
  display: none;
}

::v-deep .el-icon-arrow-right {
  cursor: pointer;
  margin-left: 5px;
  transition: transform 0.3s ease;
}
</style>
