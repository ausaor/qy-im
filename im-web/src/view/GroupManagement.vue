<template>
  <div class="group-chat-management">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="群名称" prop="name">
          <el-input
              v-model="searchForm.name"
              placeholder="请输入群名称"
              clearable
              style="width: 200px;"
              size="mini"
          ></el-input>
        </el-form-item>

        <el-form-item label="群主" prop="owner">
          <el-input
              v-model="searchForm.owner"
              placeholder="请输入群主"
              clearable
              style="width: 200px;"
              size="mini"
          ></el-input>
        </el-form-item>

        <el-form-item label="群聊类型" prop="groupType">
          <el-select
              v-model="searchForm.groupType"
              placeholder="请选择群聊类型"
              clearable
              style="width: 200px;"
              size="mini"
          >
            <el-option
                v-for="(item, index) in groupTypeOptions"
                :key="index"
                :label="item"
                :value="index"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch" size="mini">查询</el-button>
          <el-button @click="resetSearch" size="mini">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 群聊列表 -->
    <el-card class="table-card">
      <el-table
          :data="groupList"
          border
          stripe
          v-loading="tableLoading"
          element-loading-text="加载中..."
          element-loading-spinner="el-icon-loading"
          element-loading-background="rgba(0, 0, 0, 0.1)"
      >
        <el-table-column type="index" label="序号" width="80" align="center"></el-table-column>

        <el-table-column prop="name" label="群名称" align="center" width="90">
          <template slot-scope="scope">
            <div class="group-name-cell">
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="群头像" align="center" width="90">
          <template slot-scope="scope">
            <div class="avatar-cell">
              <head-image :url="scope.row.headImage" :name="scope.row.name" :size="40"></head-image>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="owner" label="群主" align="center" min-width="60"></el-table-column>

        <el-table-column prop="groupType" label="群聊类型" align="center">
          <template slot-scope="scope">
            <el-tag
                :type="getGroupTypeTagType(scope.row.groupType)"
                effect="plain"
                size="mini"
            >
              {{ groupTypeOptions[scope.row.groupType] }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="isBanned" label="是否禁言" align="center" min-width="60">
          <template slot-scope="scope">
            <span :class="['status-tag', scope.row.isBanned ? 'banned-tag' : 'active-tag']">
              {{ scope.row.isBanned ? '已禁言' : '正常' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="createdTime" label="创建时间" align="center">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createdTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="280" fixed="right">
          <template slot-scope="scope">
            <el-button
                v-if="!scope.row.isBanned"
                size="mini"
                type="text"
                @click="handleBanGroup(scope.row)"
                :disabled="scope.row.isBanned"
                :loading="scope.row.banLoading"
                class="operation-btn"
            >
              禁言
            </el-button>
            <el-button
                v-if="scope.row.isBanned"
                size="mini"
                type="text"
                @click="handleUnbanGroup(scope.row)"
                :disabled="!scope.row.isBanned"
                :loading="scope.row.unbanLoading"
                class="operation-btn"
            >
              解除
            </el-button>
            <el-button
                size="mini"
                type="text"
                @click="handleViewMembers(scope.row)"
                :loading="scope.row.viewMembersLoading"
                class="operation-btn"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页控件 -->
      <div class="pagination-container">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalGroups"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 群成员抽屉 -->
    <el-drawer
        title="群成员列表"
        :visible.sync="memberDrawerVisible"
        :size="700"
        direction="rtl"
        :before-close="handleDrawerClose"
        :destroy-on-close="true"
    >
      <div class="member-list-container">
        <el-table
            :data="currentGroupMembers"
            border
            stripe
            v-loading="memberTableLoading"
            element-loading-text="加载成员中..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="rgba(0, 0, 0, 0.1)"
        >
          <el-table-column prop="userName" label="用户名" align="center"></el-table-column>

          <el-table-column prop="aliasName" label="群员昵称" align="center"></el-table-column>

          <el-table-column label="头像" align="center" width="100">
            <template slot-scope="scope">
              <el-avatar
                  :src="scope.row.headImage"
                  class="member-avatar"
                  :alt="scope.row.aliasName || scope.row.userName"
              ></el-avatar>
            </template>
          </el-table-column>

          <el-table-column prop="createdTime" label="加入时间" align="center">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.createdTime) }}
            </template>
          </el-table-column>

          <el-table-column prop="isBanned" label="是否禁言" align="center">
            <template slot-scope="scope">
              <el-switch
                  v-model="scope.row.isBanned"
                  :active-color="scope.row.isBanned ? '#F56C6C' : '#409EFF'"
                  @change="handleMemberBanChange(scope.row)"
                  :disabled="true"
              ></el-switch>
            </template>
          </el-table-column>

          <el-table-column label="操作" align="center" width="200">
            <template slot-scope="scope">
              <el-button
                  size="mini"
                  type="danger"
                  @click="handleBanMember(scope.row)"
                  :disabled="scope.row.isBanned"
                  :loading="scope.row.banLoading"
                  class="member-operation-btn"
              >
                禁言
              </el-button>
              <el-button
                  size="mini"
                  type="success"
                  @click="handleUnbanMember(scope.row)"
                  :disabled="!scope.row.isBanned"
                  :loading="scope.row.unbanLoading"
                  class="member-operation-btn"
              >
                解除禁言
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import HeadImage from "@components/common/HeadImage.vue";

export default {
  name: 'GroupManagement',
  components: {HeadImage},
  data() {
    return {
      // 查询表单
      searchForm: {
        name: '',
        owner: '',
        groupType: ''
      },

      // 群聊类型选项
      groupTypeOptions: [
        '普通群聊',
        '模板群聊',
        '多元角色群聊',
        '角色群聊',
        '模板角色群聊'
      ],

      // 群聊列表数据
      groupList: [],
      filteredGroupList: [],
      totalGroups: 0,

      // 分页参数
      currentPage: 1,
      pageSize: 10,

      // 加载状态
      tableLoading: false,
      memberTableLoading: false,

      // 群成员抽屉
      memberDrawerVisible: false,
      currentGroup: null,
      currentGroupMembers: []
    };
  },
  created() {
    // 初始化数据
    this.loadGroupData();
  },
  methods: {
    // 加载群聊数据
    loadGroupData() {
      this.tableLoading = true;

      this.$http({
        url: `/group/page?pageNo=${this.currentPage}&pageSize=${this.pageSize}`,
        method: "post",
        data: this.searchForm
      }).then((data) => {
        this.groupList = data.data;
        this.totalGroups = data.total;
      }).finally(() => {
        this.tableLoading = false;
      })
    },

    // 处理查询
    handleSearch() {
      this.currentPage = 1; // 重置到第一页
      this.loadGroupData();
    },

    // 重置查询条件
    resetSearch() {
      this.searchForm = {
        name: '',
        owner: '',
        groupType: ''
      };
      this.handleSearch();
    },

    // 处理分页大小变化
    handleSizeChange(val) {
      this.pageSize = val;
      this.loadGroupData();
    },

    // 处理当前页变化
    handleCurrentChange(val) {
      this.currentPage = val;
      this.loadGroupData();
    },

    // 获取群聊类型标签样式
    getGroupTypeTagType(type) {
      const typeMap = {
        0: '',
        1: 'primary',
        2: 'success',
        3: 'warning',
        4: 'info'
      };
      return typeMap[type] || '';
    },

    // 格式化日期时间
    formatDateTime(date) {
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
    },

    // 处理群聊禁言状态变更
    handleGroupBanChange(row) {
      // 这里只是UI反馈，实际状态变更由按钮触发
    },

    // 禁言群聊
    handleBanGroup(row) {
      row.banLoading = true;

      try {
        this.$prompt('请输入禁言时长（分钟）', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /^([1-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|60000)$/,
          inputErrorMessage: '只能输入正整数(1~60000)'
        }).then(({ value }) => {
          let paramVO = {
            id: row.id,
            allBanned: true,
            banDuration: value,
            banType: 'sys'
          }
          this.$http({
            url: '/group/banMsg',
            method: 'post',
            data: paramVO
          }).then(() => {
            row.isBanned = true;
            this.$message.success("操作成功");
          }).catch((e) => {
          })
        })
      } catch (e) {
        this.$message.error('操作失败');
      } finally {
        row.banLoading = false;
      }
    },

    // 解除群聊禁言
    handleUnbanGroup(row) {
      row.unbanLoading = true;
      let paramVO = {
        id: row.id,
        allBanned: true,
        banDuration: 1,
        banType: 'sys'
      }
      this.$http({
        url: '/group/unBanMsg',
        method: 'post',
        data: paramVO
      }).then(() => {
        row.isBanned = false;
        this.$message.success("操作成功");
      }).catch((e) => {
      }).finally(() => {
        row.unbanLoading = false;
      })
    },

    // 查看群成员
    handleViewMembers(row) {
      row.viewMembersLoading = true;
      this.currentGroup = row;
      this.memberDrawerVisible = true;
      this.memberTableLoading = true;

      // 模拟加载群成员数据
      setTimeout(() => {
        // 生成模拟成员数据
        const memberCount = 5 + Math.floor(Math.random() * 20);
        const members = [];

        for (let i = 1; i <= memberCount; i++) {
          members.push({
            id: `member_${row.id}_${i}`,
            userName: `user${i}`,
            aliasName: `群昵称${i}`,
            headImage: `https://picsum.photos/seed/member${i}${row.id}/200`,
            createdTime: new Date(Date.now() - Math.random() * 15 * 24 * 60 * 60 * 1000),
            isBanned: Math.random() > 0.8,
            banLoading: false,
            unbanLoading: false
          });
        }

        this.currentGroupMembers = members;
        this.memberTableLoading = false;
        row.viewMembersLoading = false;
      }, 800);
    },

    // 关闭抽屉
    handleDrawerClose() {
      this.memberDrawerVisible = false;
      this.currentGroupMembers = [];
      this.currentGroup = null;
    },

    // 处理成员禁言状态变更
    handleMemberBanChange(row) {
      // 这里只是UI反馈，实际状态变更由按钮触发
    },

    // 禁言成员
    handleBanMember(row) {
      row.banLoading = true;

      // 模拟API请求
      setTimeout(() => {
        row.isBanned = true;
        row.banLoading = false;
        this.$message.success('成员禁言成功');
      }, 600);
    },

    // 解除成员禁言
    handleUnbanMember(row) {
      row.unbanLoading = true;

      // 模拟API请求
      setTimeout(() => {
        row.isBanned = false;
        row.unbanLoading = false;
        this.$message.success('解除成员禁言成功');
      }, 600);
    }
  }
};
</script>

<style lang="scss" scoped>
.group-chat-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.search-form {
  margin-top: 0px;
}

::v-deep .el-card__body, .el-main {
  padding: 10px;
}

::v-deep .el-form-item {
  margin-bottom: 0;
}

.table-card {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.avatar-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.group-avatar {
  width: 50px;
  height: 50px;
  border-radius: 8px;
}

.status-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.banned-tag {
  background-color: #fef0f0;
  color: #f56c6c;
}
.active-tag {
  background-color: #f0f9eb;
  color: #67c23a;
}

.group-avatar-small {
  width: 36px;
  height: 36px;
  margin-right: 8px;
  border-radius: 6px;
}

.group-name-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.member-avatar {
  width: 40px;
  height: 40px;
  border-radius: 6px;
}

.member-list-container {
  padding: 10px 0;
}

.operation-btn {
  margin-right: 5px;
}

.operation-btn:last-child {
  margin-right: 0;
}

.member-operation-btn {
  margin-right: 5px;
}

.member-operation-btn:last-child {
  margin-right: 0;
}

/* 动画效果 */
::v-deep .el-table__row {
  transition: background-color 0.2s;
}

::v-deep .el-drawer {
  transition: transform 0.3s cubic-bezier(0.78, 0.14, 0.15, 0.86);
}

::v-deep .el-button {
  transition: all 0.2s;
}

::v-deep .el-tag {
  transition: all 0.2s;
}
</style>
