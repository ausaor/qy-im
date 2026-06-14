<template>
  <div class="talk-management">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分类">
          <el-select
              v-model="searchForm.category"
              placeholder="请选择分类"
              clearable
              style="width: 160px;"
              size="mini"
          >
            <el-option
                v-for="item in categoryOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 160px;"
              size="mini"
          >
            <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="用户ID">
          <el-input
              v-model="searchForm.userId"
              placeholder="请输入用户ID"
              clearable
              style="width: 160px;"
              size="mini"
          ></el-input>
        </el-form-item>

        <el-form-item label="群组ID">
          <el-input
              v-model="searchForm.groupId"
              placeholder="请输入群组ID"
              clearable
              style="width: 160px;"
              size="mini"
          ></el-input>
        </el-form-item>

        <el-form-item label="地区编码">
          <el-input
              v-model="searchForm.regionCode"
              placeholder="请输入地区编码"
              clearable
              style="width: 160px;"
              size="mini"
          ></el-input>
        </el-form-item>

        <el-form-item label="角色ID">
          <el-input
              v-model="searchForm.characterId"
              placeholder="请输入角色ID"
              clearable
              style="width: 160px;"
              size="mini"
          ></el-input>
        </el-form-item>

        <el-form-item label="群聊模板ID">
          <el-input
              v-model="searchForm.groupTemplateId"
              placeholder="请输入群聊模板ID"
              clearable
              style="width: 160px;"
              size="mini"
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch" size="mini">查询</el-button>
          <el-button @click="resetSearch" size="mini">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table
          :data="talkList"
          border
          stripe
          v-loading="tableLoading"
          element-loading-text="加载中..."
          element-loading-spinner="el-icon-loading"
          element-loading-background="rgba(0, 0, 0, 0.1)"
      >
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>

        <el-table-column prop="nickName" label="用户昵称" align="center" min-width="100">
          <template slot-scope="scope">
            <span>{{ scope.row.nickName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="头像" align="center" width="80">
          <template slot-scope="scope">
            <div class="avatar-cell">
              <head-image :url="scope.row.avatar" :name="scope.row.nickName" :size="40"></head-image>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="动态内容" align="center" min-width="200" :show-overflow-tooltip="true">
          <template slot-scope="scope">
            <div class="content-cell" v-html="$emo.transform(scope.row.content)"></div>
          </template>
        </el-table-column>

        <el-table-column label="分类" align="center" width="90">
          <template slot-scope="scope">
            <el-tag
                :type="getCategoryTagType(scope.row.category)"
                effect="plain"
                size="mini"
            >
              {{ getCategoryName(scope.row.category) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" align="center" width="90">
          <template slot-scope="scope">
            <el-tag
                :type="getStatusTagType(scope.row.status)"
                effect="dark"
                size="mini"
            >
              {{ scope.row.statusName || getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="address" label="发布地址" align="center" min-width="100"></el-table-column>

        <el-table-column label="创建时间" align="center" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="更新时间" align="center" width="160">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.updateTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="160" fixed="right">
          <template slot-scope="scope">
            <el-button
                size="mini"
                type="text"
                @click="handleApprove(scope.row)"
                :loading="scope.row.approveLoading"
                class="operation-btn"
            >
              通过
            </el-button>
            <el-button
                size="mini"
                type="text"
                @click="handleReject(scope.row)"
                :loading="scope.row.rejectLoading"
                class="operation-btn"
            >
              不通过
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
        ></el-pagination>
      </div>
    </el-card>

    <el-dialog
        title="审核不通过"
        :visible.sync="rejectDialogVisible"
        width="450px"
        :before-close="handleRejectDialogClose"
    >
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="不通过原因">
          <el-input
              v-model="rejectForm.reason"
              type="textarea"
              :rows="4"
              placeholder="请输入审核不通过原因"
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="rejectDialogVisible = false" size="mini">取消</el-button>
        <el-button type="danger" @click="confirmReject" size="mini" :loading="rejectForm.loading">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import HeadImage from "@/components/common/HeadImage";

export default {
  name: 'TalkManagement',
  components: {HeadImage},
  data() {
    return {
      searchForm: {
        category: 'private',
        status: '',
        userId: '',
        groupId: '',
        regionCode: '',
        characterId: '',
        groupTemplateId: ''
      },

      categoryOptions: [
        {label: '个人', value: 'private'},
        {label: '群聊', value: 'group'},
        {label: '地区', value: 'region'},
        {label: '角色', value: 'character'}
      ],

      statusOptions: [
        {label: '待审批', value: '0'},
        {label: '审核中', value: '1'},
        {label: '已发布', value: '2'},
        {label: '未通过', value: '3'}
      ],

      talkList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      tableLoading: false,

      rejectDialogVisible: false,
      rejectForm: {
        id: null,
        reason: '',
        loading: false
      }
    };
  },
  created() {
    this.loadTalkData();
  },
  methods: {
    loadTalkData() {
      this.tableLoading = true;
      const params = {};
      Object.keys(this.searchForm).forEach(key => {
        if (this.searchForm[key] !== '' && this.searchForm[key] !== null) {
          if (key === 'userId' || key === 'groupId' || key === 'characterId' || key === 'groupTemplateId') {
            params[key] = this.searchForm[key] ? Number(this.searchForm[key]) : undefined;
          } else {
            params[key] = this.searchForm[key];
          }
        }
      });

      this.$http({
        url: '/talk/pageTalkList',
        method: 'post',
        params: {
          pageNo: this.currentPage,
          pageSize: this.pageSize
        },
        data: params
      }).then((data) => {
        this.talkList = data.data || [];
        this.total = data.total || 0;
      }).catch(() => {
        this.$message.error('获取动态列表失败');
      }).finally(() => {
        this.tableLoading = false;
      });
    },

    handleSearch() {
      this.currentPage = 1;
      this.loadTalkData();
    },

    resetSearch() {
      this.searchForm = {
        category: 'private',
        status: '',
        userId: '',
        groupId: '',
        regionCode: '',
        characterId: '',
        groupTemplateId: ''
      };
      this.handleSearch();
    },

    handleSizeChange(val) {
      this.pageSize = val;
      this.loadTalkData();
    },

    handleCurrentChange(val) {
      this.currentPage = val;
      this.loadTalkData();
    },

    handleApprove(row) {
      this.$set(row, 'approveLoading', true);
      this.$http({
        url: '/talk/review',
        method: 'post',
        data: {
          id: row.id,
          status: '2'
        }
      }).then(() => {
        this.$message.success('审核通过');
        this.loadTalkData();
      }).catch(() => {
        this.$message.error('操作失败');
      }).finally(() => {
        this.$set(row, 'approveLoading', false);
      });
    },

    handleReject(row) {
      this.rejectForm.id = row.id;
      this.rejectForm.reason = '';
      this.rejectForm.loading = false;
      this.rejectDialogVisible = true;
    },

    confirmReject() {
      if (!this.rejectForm.id) {
        this.$message.warning('参数异常');
        return;
      }
      this.rejectForm.loading = true;
      this.$http({
        url: '/talk/review',
        method: 'post',
        data: {
          id: this.rejectForm.id,
          status: '3',
          reason: this.rejectForm.reason
        }
      }).then(() => {
        this.$message.success('审核不通过');
        this.rejectDialogVisible = false;
        this.loadTalkData();
      }).catch(() => {
        this.$message.error('操作失败');
      }).finally(() => {
        this.rejectForm.loading = false;
      });
    },

    handleRejectDialogClose() {
      this.rejectDialogVisible = false;
      this.rejectForm.id = null;
      this.rejectForm.reason = '';
    },

    getCategoryName(category) {
      const map = {
        'private': '个人',
        'group': '群聊',
        'region': '地区',
        'public': '公开',
        'character': '角色'
      };
      return map[category] || category;
    },

    getCategoryTagType(category) {
      const map = {
        'private': '',
        'group': 'primary',
        'region': 'success',
        'public': 'warning',
        'character': 'info'
      };
      return map[category] || '';
    },

    getStatusName(status) {
      const map = {
        '0': '待审批',
        '1': '审核中',
        '2': '已发布',
        '3': '未通过'
      };
      return map[status] || status;
    },

    getStatusTagType(status) {
      const map = {
        '0': 'info',
        '1': 'warning',
        '2': 'success',
        '3': 'danger'
      };
      return map[status] || 'info';
    },

    formatDateTime(date) {
      if (!date) return '';
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
};
</script>

<style lang="scss" scoped>
.talk-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.search-card {
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.search-form {
  margin-top: 0;
}

::v-deep .el-card__body, .el-main {
  padding: 10px;
}

::v-deep .el-form-item {
  margin-bottom: 0;
}

.table-card {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

// 表格滚动条样式
::v-deep .el-table__body-wrapper::-webkit-scrollbar {
  height: 8px;
}

::v-deep .el-table__body-wrapper::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 4px;
}

::v-deep .el-table__body-wrapper::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

// 固定列样式增强 - 确保右侧操作列在滚动时可见
::v-deep .el-table__fixed-right {
  z-index: 100 !important;
  background-color: #fff !important;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.08) !important;
  border-left: 1px solid #ebeef5 !important;
}

.avatar-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.content-cell {
  text-align: left;
  word-break: break-all;

  ::v-deep img {
    vertical-align: middle;
  }
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.operation-btn {
  margin-right: 5px;
}

.operation-btn:last-child {
  margin-right: 0;
}

::v-deep .el-table__row {
  transition: background-color 0.2s;
}

::v-deep .el-button {
  transition: all 0.2s;
}

::v-deep .el-tag {
  transition: all 0.2s;
}
</style>
