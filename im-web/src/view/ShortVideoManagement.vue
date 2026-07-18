<template>
  <div class="short-video-management">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分类">
          <el-select
            v-model="searchForm.type"
            placeholder="请选择分类"
            clearable
            size="mini"
            style="width: 160px"
          >
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            size="mini"
            style="width: 160px"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="可见范围">
          <el-select
            v-model="searchForm.scope"
            placeholder="请选择可见范围"
            clearable
            size="mini"
            style="width: 160px"
          >
            <el-option
              v-for="item in scopeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="标题">
          <el-input
            v-model="searchForm.title"
            placeholder="请输入标题"
            clearable
            size="mini"
            style="width: 180px"
          />
        </el-form-item>

        <el-form-item label="用户">
          <el-input
            v-model="searchForm.userId"
            placeholder="请输入用户ID"
            clearable
            size="mini"
            style="width: 160px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="mini" @click="handleSearch">查询</el-button>
          <el-button size="mini" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <div class="table-scroll">
        <el-table
          ref="shortVideoTable"
          v-loading="tableLoading"
          :data="tableData"
          :fit="false"
          border
          stripe
          element-loading-text="加载中..."
          class="short-video-table"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="title" label="标题" width="220" show-overflow-tooltip />

          <el-table-column label="封面" width="100" align="center">
            <template slot-scope="scope">
              <div class="cover-cell">
                <img
                  v-if="scope.row.coverUrl"
                  :src="scope.row.coverUrl"
                  class="cover-image"
                  alt="cover"
                >
                <span v-else>-</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="分类" width="110" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" effect="plain">
                {{ getTypeLabel(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="可见范围" width="110" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" :type="getScopeTagType(scope.row.scope)" effect="plain">
                {{ getScopeLabel(scope.row.scope) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="时长" width="100" align="center">
            <template slot-scope="scope">
              {{ formatDuration(scope.row.duration) }}
            </template>
          </el-table-column>

          <el-table-column label="大小(KB)" width="100" align="right">
            <template slot-scope="scope">
              {{ formatSize(scope.row.size) }}
            </template>
          </el-table-column>

          <el-table-column prop="playCount" label="播放次数" width="100" align="center" />
          <el-table-column prop="likeCount" label="点赞次数" width="100" align="center" />
          <el-table-column prop="favoriteCount" label="收藏次数" width="100" align="center" />
          <el-table-column prop="commentCount" label="评论条数" width="100" align="center" />

          <el-table-column label="状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" effect="dark" :type="getStatusTagType(scope.row.status)">
                {{ getStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="发布时间" width="170" align="center">
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.publishTime) }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="150" align="center">
            <template slot-scope="scope">
              <el-button
                type="text"
                size="mini"
                :disabled="!canReview(scope.row)"
                :loading="scope.row.approveLoading"
                @click="handleApprove(scope.row)"
              >
                通过
              </el-button>
              <el-button
                type="text"
                size="mini"
                :disabled="!canReview(scope.row)"
                :loading="scope.row.rejectLoading"
                @click="openRejectDialog(scope.row)"
              >
                不通过
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container">
        <el-pagination
          :current-page="pagination.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      title="审核不通过"
      :visible.sync="rejectDialogVisible"
      width="460px"
      :before-close="handleRejectDialogClose"
    >
      <el-form label-width="100px">
        <el-form-item label="不通过原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请输入审核不通过原因"
          />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="mini" @click="handleRejectDialogClose">取消</el-button>
        <el-button
          type="danger"
          size="mini"
          :loading="rejectForm.loading"
          @click="confirmReject"
        >
          确定
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ShortVideoManagement',
  data() {
    return {
      searchForm: {
        type: '',
        status: '',
        scope: '',
        title: '',
        userId: ''
      },
      typeOptions: [
        { label: '用户', value: 'user' },
        { label: '群组', value: 'group' },
        { label: '角色', value: 'character' },
        { label: '群聊模板', value: 'template' }
      ],
      statusOptions: [
        { label: '审核中', value: '1' },
        { label: '已发布', value: '2' },
        { label: '未通过', value: '3' }
      ],
      scopeOptions: [
        { label: '公开', value: 9 },
        { label: '关注可见', value: 3 },
        { label: '好友可见', value: 2 },
        { label: '私密', value: 1 }
      ],
      tableData: [],
      tableLoading: false,
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      rejectDialogVisible: false,
      rejectForm: {
        id: null,
        reason: '',
        loading: false
      }
    }
  },
  created() {
    this.loadData()
  },
  mounted() {
    window.addEventListener('resize', this.handleWindowResize)
    this.refreshTableLayout()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleWindowResize)
  },
  methods: {
    loadData() {
      this.tableLoading = true
      const data = {}
      if (this.searchForm.type) {
        data.type = this.searchForm.type
      }
      if (this.searchForm.status !== '' && this.searchForm.status !== null) {
        data.status = Number(this.searchForm.status)
      }
      if (this.searchForm.scope !== '' && this.searchForm.scope !== null) {
        data.scope = Number(this.searchForm.scope)
      }
      if (this.searchForm.title) {
        data.title = this.searchForm.title
      }
      if (this.searchForm.userId) {
        data.userId = Number(this.searchForm.userId)
      }

      this.$http({
        url: '/shortVideo/pageList',
        method: 'post',
        params: {
          pageNo: this.pagination.currentPage,
          pageSize: this.pagination.pageSize
        },
        data
      }).then((res) => {
        this.tableData = (res.data || []).map(item => ({
          ...item,
          approveLoading: false,
          rejectLoading: false
        }))
        this.pagination.total = res.total || 0
      }).catch(() => {
        this.$message.error('获取短视频列表失败')
      }).finally(() => {
        this.tableLoading = false
        this.refreshTableLayout()
      })
    },

    handleSearch() {
      this.pagination.currentPage = 1
      this.loadData()
    },

    resetSearch() {
      this.searchForm = {
        type: '',
        status: '',
        scope: '',
        title: '',
        userId: ''
      }
      this.handleSearch()
    },

    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.pagination.currentPage = 1
      this.loadData()
    },

    handleCurrentChange(val) {
      this.pagination.currentPage = val
      this.loadData()
    },

    canReview(row) {
      return String(row.status) === '1'
    },

    handleApprove(row) {
      if (!this.canReview(row)) {
        this.$message.warning('当前状态不可审核')
        return
      }
      this.$set(row, 'approveLoading', true)
      this.$http({
        url: '/shortVideo/review',
        method: 'post',
        data: {
          id: row.id,
          status: '2'
        }
      }).then(() => {
        this.$message.success('审核通过')
        this.loadData()
      }).catch(() => {
        this.$message.error('审核失败')
      }).finally(() => {
        this.$set(row, 'approveLoading', false)
      })
    },

    openRejectDialog(row) {
      if (!this.canReview(row)) {
        this.$message.warning('当前状态不可审核')
        return
      }
      this.rejectForm.id = row.id
      this.rejectForm.reason = ''
      this.rejectForm.loading = false
      this.$set(row, 'rejectLoading', false)
      this.rejectDialogVisible = true
    },

    confirmReject() {
      if (!this.rejectForm.id) {
        this.$message.warning('参数异常')
        return
      }
      if (!this.rejectForm.reason || !this.rejectForm.reason.trim()) {
        this.$message.warning('请输入审核不通过原因')
        return
      }
      this.rejectForm.loading = true
      this.$http({
        url: '/shortVideo/review',
        method: 'post',
        data: {
          id: this.rejectForm.id,
          status: '3',
          reason: this.rejectForm.reason.trim()
        }
      }).then(() => {
        this.$message.success('审核不通过')
        this.handleRejectDialogClose()
        this.loadData()
      }).catch(() => {
        this.$message.error('审核失败')
      }).finally(() => {
        this.rejectForm.loading = false
      })
    },

    handleRejectDialogClose() {
      this.rejectDialogVisible = false
      this.rejectForm.id = null
      this.rejectForm.reason = ''
      this.rejectForm.loading = false
    },

    getTypeLabel(type) {
      const map = {
        user: '用户',
        group: '群组',
        character: '角色',
        template: '群聊模板'
      }
      return map[type] || '-'
    },

    getScopeLabel(scope) {
      const map = {
        9: '公开',
        3: '关注可见',
        2: '好友可见',
        1: '私密'
      }
      return map[scope] || '-'
    },

    getScopeTagType(scope) {
      const map = {
        9: 'success',
        3: 'warning',
        2: 'primary',
        1: 'info'
      }
      return map[scope] || ''
    },

    getStatusLabel(status) {
      const map = {
        '1': '审核中',
        '2': '已发布',
        '3': '未通过'
      }
      return map[String(status)] || '-'
    },

    getStatusTagType(status) {
      const map = {
        '1': 'warning',
        '2': 'success',
        '3': 'danger'
      }
      return map[String(status)] || 'info'
    },

    formatDuration(duration) {
      if (duration === null || duration === undefined) {
        return '-'
      }
      const totalSeconds = Number(duration)
      if (Number.isNaN(totalSeconds)) {
        return duration
      }
      const minutes = Math.floor(totalSeconds / 60)
      const seconds = totalSeconds % 60
      return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
    },

    formatSize(size) {
      if (size === null || size === undefined || size === '') {
        return '-'
      }
      const value = Number(size)
      if (Number.isNaN(value)) {
        return size
      }
      return (value / 1024).toFixed(2)
    },

    formatDateTime(value) {
      if (!value) {
        return '-'
      }
      if (typeof value === 'string') {
        return value
      }
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) {
        return value
      }
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')
      const second = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    },

    handleWindowResize() {
      this.refreshTableLayout()
    },

    refreshTableLayout() {
      this.$nextTick(() => {
        if (this.$refs.shortVideoTable) {
          this.$refs.shortVideoTable.doLayout()
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.short-video-management {
  width: 100%;
  min-width: 0;
  background-color: #f5f7fa;
  min-height: 100vh;
  overflow: hidden;
}

.search-card {
  margin-bottom: 10px;
}

.search-form {
  margin-top: 0;
}

::v-deep .el-card__body,
.el-main {
  padding: 10px;
}

::v-deep .el-form-item {
  margin-bottom: 0;
}

.table-card {
  width: 100%;
  min-width: 0;
  overflow: hidden;
}

.table-card ::v-deep .el-card__body {
  min-width: 0;
  overflow: hidden;
}

.table-scroll {
  width: 100%;
  min-width: 0;
  overflow: hidden;
}

::v-deep .short-video-table.el-table {
  width: 100% !important;
  min-width: 0;
}

::v-deep .short-video-table.el-table .el-table__header,
::v-deep .short-video-table.el-table .el-table__body,
::v-deep .short-video-table.el-table .el-table__footer {
  min-width: 1620px;
}

::v-deep .short-video-table.el-table .el-table__body-wrapper {
  overflow-x: auto !important;
  overflow-y: auto !important;
  -webkit-overflow-scrolling: touch;
}

::v-deep .short-video-table.el-table .el-table__header-wrapper {
  overflow-x: auto !important;
  overflow-y: hidden !important;
}

.cover-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 56px;
}

.cover-image {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

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

::v-deep .el-table__fixed-right {
  z-index: 100 !important;
  background-color: #fff !important;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.08) !important;
  border-left: 1px solid #ebeef5 !important;
}
</style>
