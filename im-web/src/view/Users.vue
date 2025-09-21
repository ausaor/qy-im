<template>
  <div class="user-management-container">
    <!-- 查询表单 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="用户名">
        <el-input v-model="searchForm.userName" placeholder="请输入用户名" clearable size="mini"></el-input>
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="searchForm.nickName" placeholder="请输入昵称" clearable size="mini"></el-input>
      </el-form-item>
      <el-form-item label="性别">
        <el-select v-model="searchForm.sex" placeholder="请选择性别" clearable size="mini">
          <el-option label="男" :value="0"></el-option>
          <el-option label="女" :value="1"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <div class="action-buttons">
          <el-button type="primary" @click="fetchUserData" size="mini">查询</el-button>
          <el-button @click="resetSearch" size="mini">重置</el-button>
        </div>
      </el-form-item>
    </el-form>
    <!-- 用户表格 -->
    <el-table
        :data="userList"
        class="user-table"
        stripe
        border
        v-loading="loading"
        style="width: 100%">
      <el-table-column label="序号" width="80" align="center">
        <template slot-scope="scope">
          <div class="index-cell">
            {{ (pagination.currentPage - 1) * pagination.pageSize + scope.$index + 1 }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="用户名" min-width="150" align="center"></el-table-column>
      <el-table-column prop="nickName" label="昵称" min-width="150" align="center"></el-table-column>
      <el-table-column label="头像" min-width="100" align="center">
        <template slot-scope="scope">
          <div class="avatar-cell">
            <head-image :url="scope.row.headImage" :name="scope.row.nickName" :online="scope.row.online" :size="40"></head-image>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="性别" min-width="80" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.sex === 0 ? '男' : '女' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="ipAddress" label="IP地址" min-width="150" align="center"></el-table-column>
      <el-table-column prop="city" label="城市" min-width="120" align="center"></el-table-column>
      <el-table-column label="禁言状态" min-width="120" align="center">
        <template slot-scope="scope">
            <span :class="['status-tag', scope.row.isBanned ? 'banned-tag' : 'active-tag']">
              {{ scope.row.isBanned ? '已禁言' : '正常' }}
            </span>
        </template>
      </el-table-column>
      <el-table-column label="封禁状态" min-width="120" align="center">
        <template slot-scope="scope">
            <span :class="['status-tag', scope.row.isDisable ? 'disabled-tag' : 'enabled-tag']">
              {{ scope.row.isDisable ? '已封禁' : '正常' }}
            </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="200" class="operation-cell" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button
              size="mini"
              :type="scope.row.isBanned ? 'success' : 'warning'"
              @click="toggleBan(scope.row)"
          >
            {{ scope.row.isBanned ?  '解禁' : '禁言' }}
          </el-button>
          <el-button
              size="mini"
              :type="scope.row.isDisable ? 'success' : 'danger'"
              @click="toggleDisable(scope.row)"
          >
            {{ scope.row.isDisable ? '解封' : '封禁' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 无数据提示 -->
    <div v-if="userList.length === 0 && !loading" class="no-data">
      <i class="el-icon-user-solid"></i>
      <p>暂无用户数据</p>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script>
import HeadImage from "@components/common/HeadImage.vue";

export default {
  name: "Users",
  components: {
    HeadImage
  },
  data() {
    return {
      loading: false,
      searchForm: {
        userName: '',
        nickName: '',
        sex: null
      },
      userList: [],
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
    }
  },
  mounted() {
    this.fetchUserData();
  },
  methods: {
    // 获取用户数据
    fetchUserData() {
      this.loading = true;
      this.$http({
        url: `/user/page?pageNo=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`,
        method: "post",
        data: this.searchForm
      }).then((data) => {
        this.userList = data.data;
        this.pagination.total = data.total;
      }).finally(() => {
        this.loading = false;
      })
    },

    // 切换禁言状态
    toggleBan(user) {
      const action = user.isBanned ? '解除禁言' : '禁言';
      this.$confirm(`确定要${action}用户 "${user.nickName}" 吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        if (!user.isBanned) {
          this.$prompt('请输入禁言时长（分钟）', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputPattern: /^([1-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|60000)$/,
            inputErrorMessage: '只能输入正整数(1~60000)'
          }).then(({ value }) => {
            let param = {
              userId: user.id,
              banDuration: value,
            }
            this.$http({
              url: '/user/bandUser',
              method: 'post',
              data: param
            }).then(() => {
              user.isBanned = true
              this.$message.success("操作成功");
            }).catch((e) => {
            })
          })
        } else {
          let param = {
            userId: user.id,
          }
          this.$http({
            url: '/user/unBandUser',
            method: 'post',
            data: param
          }).then(() => {
            user.isBanned = false
            this.$message.success("操作成功");
          }).catch((e) => {
          })
        }
      }).catch(() => {
        this.$message.info('已取消操作');
      });
    },

    // 切换封禁状态
    toggleDisable(user) {
      const action = user.isDisable ? '解封' : '封禁';
      this.$confirm(`确定要${action}用户 "${user.nickName}" 吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let url;
        if (!user.isDisable) {
          url = `/user/banAccount?userId=${user.id}`
        } else {
          url = `/user/unBanAccount?userId=${user.id}`
        }
        this.$http({
          url: url,
          method: 'get',
        }).then(() => {
          user.isDisable = !user.isDisable
          this.$message.success("操作成功");
        }).catch((e) => {
        })
      }).catch(() => {
        this.$message.info('已取消操作');
      });
    },

    // 重置搜索条件
    resetSearch() {
      this.searchForm = {
        userName: '',
        nickName: '',
        sex: null
      };
      this.pagination.currentPage = 1;
      this.fetchUserData();
    },

    // 分页大小改变
    handleSizeChange(pageSize) {
      this.pagination.pageSize = pageSize;
      this.fetchUserData();
    },

    // 当前页改变
    handleCurrentChange(currentPage) {
      this.pagination.currentPage = currentPage;
      this.fetchUserData();
    }
  }
}

</script>

<style scoped lang="scss">
.user-management-container {
  max-width: 78vw;
  margin: 0 auto;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.search-form {
  background-color: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 20px;
}
::v-deep .el-form-item {
  margin-bottom: 0;
}
.action-buttons {
  display: flex;
  justify-content: flex-end;
}
.user-table {
  width: 100%;
  overflow: hidden;
}
.avatar-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}
.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 10px;
  border: 1px solid #ebeef5;
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
.disabled-tag {
  background-color: #fef0f0;
  color: #f56c6c;
}
.enabled-tag {
  background-color: #f0f9eb;
  color: #67c23a;
}
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.operation-cell button {
  margin-right: 5px;
}
.no-data {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}
.no-data i {
  font-size: 40px;
  margin-bottom: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-management-container {
    padding: 10px;
  }
}
</style>