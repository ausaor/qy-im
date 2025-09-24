<template>
  <div class="message-list-container">
    <!-- 查询条件区域 -->
    <div class="search-form">
      <el-form :model="searchForm" ref="searchForm" :inline="true" class="search-form-content">
        <el-form-item label="标题" prop="title">
          <el-input
              v-model="searchForm.title"
              placeholder="请输入标题"
              clearable
              style="width: 200px"
              size="mini"
          ></el-input>
        </el-form-item>
        <el-form-item label="消息类型" prop="type">
          <el-select
              v-model="searchForm.type"
              placeholder="请选择消息类型"
              clearable
              style="width: 150px"
              size="mini"
          >
            <el-option
                v-for="item in messageTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
                size="mini"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" icon="el-icon-search" size="mini">查询</el-button>
          <el-button @click="handleReset" icon="el-icon-refresh" size="mini">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd" icon="el-icon-plus" size="mini">新增</el-button>
      <el-button
          type="danger"
          @click="handleBatchDelete"
          icon="el-icon-delete"
          :disabled="selectedRows.length === 0"
          size="mini"
      >
        批量删除
      </el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
          :data="tableData"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          stripe
          style="width: 100%"
      >
        <el-table-column type="selection" width="55" align="center"></el-table-column>

        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="title-text">{{ scope.row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="coverUrl" label="封面图片" width="100" align="center">
          <template slot-scope="scope">
            <el-image
                v-if="scope.row.coverUrl"
                :src="scope.row.coverUrl"
                :preview-src-list="[scope.row.coverUrl]"
                class="cover-image"
                fit="cover"
            ></el-image>
            <span v-else class="no-image">暂无图片</span>
          </template>
        </el-table-column>

        <el-table-column prop="intro" label="简述" min-width="200" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="intro-text">{{ scope.row.intro || '暂无简述' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="type" label="消息类型" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getMessageTypeTag(scope.row.type)" size="small">
              {{ getMessageTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createByName" label="创建人" width="120" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.createByName || '未知' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template slot-scope="scope">
            <span>{{ formatTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
                type="text"
                size="mini"
                @click="handleEdit(scope.row)"
                icon="el-icon-edit"
            >
              修改
            </el-button>
            <el-button
                type="text"
                size="mini"
                @click="handlePush(scope.row)"
                icon="el-icon-s-promotion"
                style="color: #67C23A"
            >
              推送
            </el-button>
            <el-button
                type="text"
                size="mini"
                @click="handleDelete(scope.row)"
                icon="el-icon-delete"
                style="color: #F56C6C"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
      ></el-pagination>
    </div>

    <el-dialog
        title="推送通知"
        :visible.sync="visible"
        width="500px"
        :close-on-click-modal="false"
    >
      <el-form
          ref="pushForm"
          :model="form"
          :rules="rules"
          label-width="100px"
      >
        <!-- 标题输入 -->
        <el-form-item label="标题" prop="title">
          <el-input
              v-model="form.title"
              placeholder="请输入标题"
              maxlength="50"
          ></el-input>
        </el-form-item>

        <!-- 推送者选择（固定选项） -->
        <el-form-item label="推送者" prop="pusherId" style="margin-top: 12px;">
          <el-select
              v-model="form.pusherId"
              placeholder="请选择推送者"
              style="width: 100%"
          >
            <el-option
                v-for="pusher in pushers"
                :key="pusher.id"
                :label="pusher.name"
                :value="pusher.id"
            ></el-option>
          </el-select>
        </el-form-item>

        <!-- 接收用户选择 -->
        <el-form-item label="接收用户" prop="receiveType" style="margin-top: 12px;">
          <el-radio-group
              v-model="form.receiveType"
              @change="handleReceiveTypeChange"
          >
            <el-radio label="all">全部用户</el-radio>
            <el-radio label="part">指定用户</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 指定用户选择区域 -->
        <el-form-item
            v-if="form.receiveType === 'part'"
            label=" "
            prop="selectedUserIds"
            style="margin-top: 12px;"
        >
          <div class="user-selector">
            <!-- 用户搜索输入框 -->
            <el-input
                v-model="searchUser"
                placeholder="请输入用户名搜索"
                clearable
            >
              <el-button
                  slot="append"
                  icon="el-icon-search"
                  @click="handleUserSearch"
              ></el-button>
            </el-input>

            <!-- 搜索结果下拉框 -->
            <el-select
                v-model="form.selectedUserIds"
                multiple
                collapse-tags
                :popper-append-to-body="false"
                @change="handleUserSelect"
                v-if="showUserDropdown"
            >
              <el-option
                  v-for="user in filteredUsers"
                  :key="user.id"
                  :label="user.nickName"
                  :value="user.id"
              ></el-option>
            </el-select>

            <!-- 已选择用户标签 -->
            <div class="selected-users" v-if="form.selectedUserIds.length > 0">
              <el-tag
                  v-for="userId in form.selectedUserIds"
                  :key="userId"
                  closable
                  @close="removeUser(userId)"
              >
                {{ getUserNameById(userId) }}
              </el-tag>
            </div>
          </div>
        </el-form-item>

        <!-- 推送时间选择 -->
        <el-form-item label="推送时间" prop="pushTime" style="margin-top: 12px;">
          <el-date-picker
              v-model="form.pushTime"
              type="datetime"
              placeholder="选择推送时间"
              style="width: 100%"
          ></el-date-picker>
        </el-form-item>
      </el-form>

      <!-- 底部按钮 -->
      <div slot="footer" class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'SysMsg',
  data() {
    return {
      // 查询表单
      searchForm: {
        title: '',
        type: ''
      },
      // 表格数据
      tableData: [],
      // 选中的行
      selectedRows: [],
      // 加载状态
      loading: false,
      // 分页信息
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      // 消息类型选项
      messageTypeOptions: [
        { value: 0, label: '文字' },
        { value: 1, label: '图片' },
        { value: 2, label: '文件' },
        { value: 3, label: '音频' },
        { value: 4, label: '视频' },
        { value: 5, label: '外部链接' },
        { value: 9, label: '富文本' }
      ],
      pushers: [],
      visible: false,
      // 固定的推送者列表
      // 表单数据
      form: {
        sysMsgId: null, // 消息ID
        title: '',
        pusherId: null, // 推送者ID
        receiveType: 'all', // 默认全部用户
        selectedUserIds: [], // 选中的用户ID数组
        selectedUsers: [], // 选中的用户数组
        pushTime: ''
      },
      // 表单验证规则
      rules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' },
          { max: 50, message: '标题不能超过50个字符', trigger: 'blur' }
        ],
        pusherId: [
          { required: true, message: '请选择推送者', trigger: 'change' }
        ],
        receiveType: [
          { required: true, message: '请选择接收用户类型', trigger: 'change' }
        ],
        selectedUserIds: [
          {
            required: true,
            message: '请选择至少一个用户',
            trigger: 'change',
            validator: (rule, value, callback) => {
              // 只有选择指定用户时才验证
              if (this.form.receiveType === 'part' && value.length === 0) {
                callback(new Error('请选择至少一个用户'));
              } else {
                callback();
              }
            }
          }
        ]
      },
      // 用户搜索相关
      searchUser: '',
      filteredUsers: [], // 搜索到的用户列表
      showUserDropdown: false, // 是否显示用户下拉框
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    // 加载数据
    async loadData() {
      this.loading = true
      try {
        this.$http({
          url: `/message/system/page?pageNo=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`,
          method: "post",
          data: this.searchForm
        }).then((data) => {
          this.tableData = data.data
          this.pagination.total = data.total
        })
      } catch (error) {
        this.$message.error('数据加载失败')
      } finally {
        this.loading = false
      }
    },

    // 查询
    handleSearch() {
      this.pagination.currentPage = 1
      this.loadData()
    },

    // 重置
    handleReset() {
      this.$refs.searchForm.resetFields()
      this.pagination.currentPage = 1
      this.loadData()
    },

    // 新增
    handleAdd() {
      this.$router.push({ path: '/home/square/sysMsgEdit'})
    },

    // 批量删除
    handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要删除的数据')
        return
      }

      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 条数据吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const param = {
          ids: this.selectedRows.map(item => item.id)
        }
        this.$http({
          url: `/message/system/batchDelete`,
          method: "post",
          data: param
        }).then((data) => {
          this.loadData()
          this.$message.success('删除成功')
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },

    // 修改
    handleEdit(row) {
      this.$router.push({ path: '/home/square/sysMsgEdit', query: { id: row.id }})
    },

    // 推送
    handlePush(row) {
      this.getPushers();
      this.form.title = row.title
      this.form.sysMsgId = row.id
      this.visible = true
    },

    // 删除
    handleDelete(row) {
      this.$confirm(`确定要删除消息"${row.title}"吗？`, '删除确认', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const param = {
          ids: [row.id]
        }
        this.$http({
          url: `/message/system/batchDelete`,
          method: "post",
          data: param
        }).then((data) => {
          this.loadData()
          this.$message.success('删除成功')
        })
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },

    // 选择变化
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },

    // 分页大小变化
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.pagination.currentPage = 1
      this.loadData()
    },

    // 当前页变化
    handleCurrentChange(val) {
      this.pagination.currentPage = val
      this.loadData()
    },

    // 获取消息类型文本
    getMessageTypeText(type) {
      const option = this.messageTypeOptions.find(item => item.value === type)
      return option ? option.label : '未知'
    },

    // 获取消息类型标签样式
    getMessageTypeTag(type) {
      const tagMap = {
        0: '',
        1: 'success',
        2: 'info',
        3: 'warning',
        4: 'danger',
        5: 'success',
        9: 'info'
      }
      return tagMap[type] || ''
    },

    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return '未知'
      const date = new Date(timeStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    getPushers() {
      this.$http({
        url: `/pusher/list`,
        method: "get",
      }).then((data) => {
        this.pushers = data
      })
    },
    // 处理接收用户类型变化
    handleReceiveTypeChange(val) {
      // 如果切换到全部用户，清空已选用户
      if (val === 'all') {
        this.form.selectedUserIds = [];
        this.form.selectedUsers = [];
      }
      // 触发表单验证
      this.$refs.pushForm.validateField('selectedUserIds');
    },

    // 搜索用户
    async handleUserSearch() {
      if (!this.searchUser.trim()) {
        this.filteredUsers = [];
        this.showUserDropdown = false;
        return;
      }

      try {
        // 调用后端接口查询用户
        this.$http({
          url: `/user/findByName?name=${this.searchUser.trim()}`,
          method: "get",
        }).then((response) => {
          // 过滤掉已选择的用户
          this.filteredUsers = response.filter(user =>
              !this.form.selectedUserIds.includes(user.id)
          );

          this.showUserDropdown = this.filteredUsers.length > 0;
        });
      } catch (error) {
        this.$message.error('查询用户失败，请稍后重试');
      }
    },

    // 处理用户选择
    handleUserSelect(values) {
      // 在filteredUsers中查找id在values中的用户
      let selectedUsers = this.filteredUsers.filter(user => values.includes(user.id));
      // 在filteredUsers中查找id不在values中的用户
      const unselectedUsers = this.filteredUsers.filter(user => !values.includes(user.id));
      if (unselectedUsers.length > 0) {
        // 获取unselectedUsers中对象的id属性，并转换成Set
        const unselectedIds = new Set(unselectedUsers.map(user => user.id));
        // 将this.form.selectedUsers中对象属性id在unselectedIds中的元素移除
        this.form.selectedUsers = this.form.selectedUsers.filter(user => !unselectedIds.has(user.id));
      }
      // 将selectedUsers对象的id与this.form.selectedUsers中对象id相同的元素去掉
      if (selectedUsers.length > 0) {
        selectedUsers = selectedUsers.filter(user => !this.form.selectedUsers.some(item => user.id === item.id));
      }
      this.form.selectedUsers.push(...selectedUsers);
    },

    // 移除已选择的用户
    removeUser(userId) {
      this.form.selectedUserIds = this.form.selectedUserIds.filter(id => id !== userId);
      this.form.selectedUsers = this.form.selectedUsers.filter(user => user.id !== userId);
      this.$refs.pushForm.validateField('selectedUserIds');
    },

    // 根据用户ID获取用户名
    getUserNameById(userId) {
      const user = this.form.selectedUsers.find(u => u.id === userId);
      return user ? user.nickName : '未知用户';
    },

    // 提交表单
    async handleSubmit() {
      this.$refs.pushForm.validate(async (valid) => {
        if (valid) {
          try {
            // 构造提交数据
            const submitData = {
              sysMsgId: this.form.sysMsgId,
              title: this.form.title,
              pusherId: this.form.pusherId,
              recvRange: this.form.receiveType,
              sendTime: this.form.pushTime,
              // 只有选择指定用户时才需要传用户ID
              recvUserIds: this.form.receiveType === 'part' ? this.form.selectedUserIds : []
            };

            this.$http({
              url: '/push/task/pushSystemMessage',
              method: "post",
              data: submitData
            }).then((data) => {
              this.$message.success('提交成功');
              this.visible = false;
            }).finally(() => {
              this.resetForm();
            })
          } catch (error) {
            this.$message.error('提交失败，请稍后重试');
          }
        }
      });
    },

    // 重置表单
    resetForm() {
      this.$refs.pushForm.resetFields();
      this.searchUser = '';
      this.filteredUsers = [];
      this.showUserDropdown = false;
    }
  }
}
</script>

<style scoped lang="scss">
.message-list-container {
  background-color: #ffffff;
  max-width: 78vw;
  padding: 10px;
}

.search-form {
  background-color: #f8f9fa;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-form-content {
  margin: 0;
}

.toolbar {
  margin-bottom: 20px;
  padding: 0 4px;
}

.table-container {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.cover-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  cursor: pointer;
}

.no-image {
  color: #c0c4cc;
  font-size: 12px;
}

.title-text {
  font-weight: 500;
  color: #303133;
}

.intro-text {
  color: #606266;
  line-height: 1.4;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
  padding: 20px 0;
}

/* Element UI 表格样式优化 */
::v-deep .el-table {
  font-size: 14px;
}

::v-deep .el-table th {
  background-color: #fafafa;
  color: #606266;
  font-weight: 600;
}

::v-deep .el-table td {
  padding: 12px 0;
}

::v-deep .el-table--striped .el-table__body tr.el-table__row--striped td {
  background-color: #fafafa;
}

::v-deep .el-button--text {
  padding: 0 8px;
  margin: 0 2px;
}

::v-deep .el-form-item {
  margin-bottom: 0;
  margin-right: 20px;
}

::v-deep .el-form-item__label {
  font-weight: 500;
  color: #606266;
}

.user-selector {
  width: 100%;
}

.selected-users {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

::v-deep .el-select__tags {
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-list-container {
    padding: 10px;
  }

  .search-form {
    padding: 15px;
  }

  ::v-deep .el-form--inline .el-form-item {
    display: block;
    margin-right: 0;
  }

  .toolbar {
    text-align: center;
  }

  .pagination-container {
    text-align: center;
  }
}
</style>