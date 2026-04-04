<template>
  <div class="file-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <i class="el-icon-folder"></i>
        文件管理
      </h2>
      <p class="page-subtitle">管理您的所有上传文件</p>
    </div>

    <!-- 查询表单 -->
    <div class="search-form">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="文件名称">
          <el-input 
            v-model="searchForm.fileName" 
            placeholder="请输入文件名称" 
            clearable
            prefix-icon="el-icon-search"
            size="small">
          </el-input>
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select 
            v-model="searchForm.fileType" 
            placeholder="请选择文件类型" 
            clearable
            size="small">
            <el-option label="图片" value="IMAGE"></el-option>
            <el-option label="视频" value="VIDEO"></el-option>
            <el-option label="音频" value="AUDIO"></el-option>
            <el-option label="文档" value="FILE"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="存储类型">
          <el-select 
            v-model="searchForm.storageType" 
            placeholder="请选择存储类型" 
            clearable
            size="small">
            <el-option label="本地磁盘" value="disk"></el-option>
            <el-option label="MinIO" value="minio"></el-option>
            <el-option label="七牛云" value="qiniu"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <div class="action-buttons">
            <el-button type="primary" @click="handleSearch" size="small" icon="el-icon-search">查询</el-button>
            <el-button @click="resetForm" size="small" icon="el-icon-refresh">重置</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- 文件列表表格 -->
    <el-table
      :data="fileList"
      stripe
      border
      v-loading="loading"
      style="width: 100%"
      class="file-table"
      header-cell-class-name="table-header-gray"
      max-height="600"
    >
      <el-table-column type="index" label="序号" width="70" align="center" fixed></el-table-column>
      
      <el-table-column prop="fileName" label="文件名称" width="180" show-overflow-tooltip></el-table-column>
      
      <el-table-column prop="extension" label="文件后缀" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" effect="plain">{{ scope.row.extension }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="fileType" label="文件类型" width="100" align="center">
        <template slot-scope="scope">
          <el-tag 
            size="mini" 
            :type="getFileTypeTag(scope.row.fileType)"
            effect="light">
            {{ scope.row.fileType }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="fileSize" label="文件大小 (KB)" width="120" align="right">
        <template slot-scope="scope">
          <span class="file-size">{{ formatFileSize(scope.row.fileSize) }}</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="path" label="文件路径" min-width="150" show-overflow-tooltip></el-table-column>
      
      <el-table-column prop="storageType" label="存储类型" width="120" align="center">
        <template slot-scope="scope">
          <el-tag 
            size="mini" 
            :type="getStorageTypeTag(scope.row.storageType)"
            effect="plain">
            {{ getStorageTypeText(scope.row.storageType) }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="createByName" label="上传人" width="120" align="center" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ scope.row.createByName || '-' }}</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="createTime" label="上传时间" min-width="160" align="center">
        <template slot-scope="scope">
          <span>{{ formatDateTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      
      <el-table-column 
        label="操作" 
        width="200" 
        fixed="right" 
        align="center"
        class-name="operation-column">
        <template slot-scope="scope">
          <div class="operation-buttons">
            <el-button 
              type="primary" 
              size="mini" 
              @click="handlePreview(scope.row)"
              icon="el-icon-view"
              circle
              title="预览">
            </el-button>
            <el-button 
              type="danger" 
              size="mini" 
              @click="handleDelete(scope.row)"
              icon="el-icon-delete"
              circle
              title="删除">
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

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
        background
      >
      </el-pagination>
    </div>

    <!-- 文件预览对话框 -->
    <el-dialog
      title="文件预览"
      :visible.sync="previewVisible"
      width="60%"
      :before-close="handleClosePreview"
      center
    >
      <div class="preview-content" v-loading="previewLoading">
        <!-- 图片预览 -->
        <div v-if="currentFile && currentFile.fileType === 'IMAGE'">
          <img :src="currentFile.url" alt="文件预览" class="preview-image">
        </div>
        <!-- 视频预览 -->
        <div v-else-if="currentFile && currentFile.fileType === 'VIDEO'">
          <video 
            ref="videoPlayer"
            class="video-player"
            controls
            preload="auto"
            :src="currentFile.url"
            poster="">
            您的浏览器不支持视频播放
          </video>
        </div>
        <!-- 音频预览 -->
        <div v-else-if="currentFile && currentFile.fileType === 'AUDIO'">
          <div class="audio-player-container">
            <div class="audio-info">
              <i class="el-icon-headset audio-icon"></i>
              <span class="audio-filename">{{ currentFile.fileName }}</span>
            </div>
            <div class="audio-controls">
              <audio 
                ref="audioPlayer"
                :src="currentFile.url" 
                preload="auto"
                @play="onAudioPlay"
                @pause="onAudioPause"
                @ended="onAudioEnded"
                @timeupdate="onAudioTimeUpdate">
                您的浏览器不支持音频播放
              </audio>
              <div class="custom-controls">
                <el-button 
                  type="primary" 
                  circle 
                  size="medium"
                  @click="toggleAudioPlay"
                  :icon="isAudioPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'">
                </el-button>
                <div class="time-display">
                  <span>{{ formatTime(audioCurrentTime) }}</span>
                  <span>/</span>
                  <span>{{ formatTime(audioDuration) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- PDF 预览 -->
        <div v-else-if="currentFile && currentFile.extension === '.pdf'">
          <iframe :src="currentFile.url" width="100%" height="500px" frameborder="0"></iframe>
        </div>
        <!-- 其他文件 -->
        <div v-else class="unsupported-file">
          <i class="el-icon-document"></i>
          <p>该文件类型暂不支持预览</p>
          <el-button type="primary" @click="downloadFile" icon="el-icon-download">下载文件</el-button>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleClosePreview" size="small">关 闭</el-button>
        <el-button type="primary" @click="downloadFile" size="small" icon="el-icon-download">下载</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "FileInfoManage",
  data() {
    return {
      // 查询表单
      searchForm: {
        fileName: '',
        fileType: '',
        storageType: ''
      },
      
      // 文件列表
      fileList: [],
      
      // 加载状态
      loading: false,
      previewLoading: false,
      
      // 分页参数
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      
      // 预览相关
      previewVisible: false,
      currentFile: null,
      
      // 音频播放状态
      isAudioPlaying: false,
      audioCurrentTime: 0,
      audioDuration: 0
    }
  },
  
  created() {
    this.fetchFileList();
  },
  
  methods: {
    // 获取文件列表
    fetchFileList() {
      this.loading = true;
      const params = {
        ...this.searchForm,
        pageNo: this.pagination.currentPage,
        pageSize: this.pagination.pageSize
      };
      
      this.$http({
        url: '/file/page',
        method: 'post',
        data: params
      }).then((data) => {
        this.fileList = data.data || [];
        this.pagination.total = data.total || 0;
      }).catch((error) => {
        console.error('获取文件列表失败:', error);
        this.$message.error('获取文件列表失败');
      }).finally(() => {
        this.loading = false;
      });
    },
    
    // 处理查询
    handleSearch() {
      this.pagination.currentPage = 1;
      this.fetchFileList();
    },
    
    // 重置表单
    resetForm() {
      this.searchForm = {
        fileName: '',
        fileType: '',
        storageType: ''
      };
      this.pagination.currentPage = 1;
      this.fetchFileList();
    },
    
    // 处理分页大小变化
    handleSizeChange(size) {
      this.pagination.pageSize = size;
      this.pagination.currentPage = 1;
      this.fetchFileList();
    },
    
    // 处理页码变化
    handleCurrentChange(page) {
      this.pagination.currentPage = page;
      this.fetchFileList();
    },
    
    // 格式化文件大小
    formatFileSize(size) {
      if (!size) return '0 KB';
      return size.toFixed(2);
    },
    
    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return '-';
      const date = new Date(dateTime);
      if (isNaN(date.getTime())) return '-';
      
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
    
    // 获取文件类型标签颜色
    getFileTypeTag(type) {
      const tagMap = {
        'image': 'success',
        'video': 'primary',
        'audio': 'warning',
        'document': 'info',
        'other': ''
      };
      return tagMap[type] || '';
    },
    
    // 获取存储类型标签颜色
    getStorageTypeTag(type) {
      const tagMap = {
        'disk': 'success',
        'minio': 'primary',
        'qiniu': 'warning'
      };
      return tagMap[type] || '';
    },
    
    // 获取存储类型文本
    getStorageTypeText(type) {
      const textMap = {
        'disk': '本地磁盘',
        'minio': 'MinIO',
        'qiniu': '七牛云'
      };
      return textMap[type] || type;
    },
    
    // 预览文件
    handlePreview(row) {
      this.currentFile = row;
      this.previewVisible = true;
      this.previewLoading = true;
      this.isAudioPlaying = false;
      this.audioCurrentTime = 0;
      this.audioDuration = 0;
      
      // 模拟加载延迟
      setTimeout(() => {
        this.previewLoading = false;
        // 不再自动播放视频和音频，等待用户手动点击
      }, 500);
    },
    
    // 关闭预览
    handleClosePreview() {
      this.previewVisible = false;
      // 停止播放
      if (this.$refs.videoPlayer) {
        this.$refs.videoPlayer.pause();
        this.$refs.videoPlayer.src = '';
      }
      if (this.$refs.audioPlayer) {
        this.$refs.audioPlayer.pause();
        this.$refs.audioPlayer.src = '';
      }
      this.isAudioPlaying = false;
      this.audioCurrentTime = 0;
      this.audioDuration = 0;
      this.currentFile = null;
    },
    
    // 切换音频播放/暂停
    toggleAudioPlay() {
      if (!this.$refs.audioPlayer) return;
      
      const audio = this.$refs.audioPlayer;
      if (this.isAudioPlaying) {
        audio.pause();
      } else {
        audio.volume = 0.5;
        audio.play().catch(err => {
          console.log('音频播放失败:', err);
          this.$message.warning('音频播放失败');
          this.isAudioPlaying = false;
        });
      }
    },
    
    // 音频播放事件
    onAudioPlay() {
      this.isAudioPlaying = true;
    },
    
    // 音频暂停事件
    onAudioPause() {
      this.isAudioPlaying = false;
    },
    
    // 音频播放结束事件
    onAudioEnded() {
      this.isAudioPlaying = false;
      this.audioCurrentTime = 0;
    },
    
    // 音频时间更新事件
    onAudioTimeUpdate() {
      if (this.$refs.audioPlayer) {
        this.audioCurrentTime = this.$refs.audioPlayer.currentTime;
        this.audioDuration = this.$refs.audioPlayer.duration || 0;
      }
    },
    
    // 格式化时间显示
    formatTime(seconds) {
      if (!seconds || isNaN(seconds)) return '00:00';
      const mins = Math.floor(seconds / 60);
      const secs = Math.floor(seconds % 60);
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    },
    
    // 下载文件
    downloadFile() {
      if (!this.currentFile || !this.currentFile.url) {
        this.$message.warning('文件链接不存在');
        return;
      }
      
      const link = document.createElement('a');
      link.href = this.currentFile.url;
      link.download = this.currentFile.fileName;
      link.target = '_blank';
      link.click();
      
      this.$message.success('开始下载文件');
    },
    
    // 删除文件
    handleDelete(row) {
      this.$confirm(`确定要删除文件 "${row.fileName}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(() => {
        this.$http({
          url: '/file/delete',
          method: 'post',
          data: { id: row.id }
        }).then(() => {
          this.$message.success('删除成功');
          this.fetchFileList();
        }).catch((error) => {
          console.error('删除失败:', error);
          this.$message.error('删除失败');
        });
      }).catch(() => {
        // 用户取消删除
      });
    }
  }
}
</script>

<style scoped lang="scss">
.file-management-container {
  height: 100%;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

// 确保容器可以完全显示
.file-management-container::-webkit-scrollbar {
  height: 8px;
}

.file-management-container::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 4px;
}

.file-management-container::-webkit-scrollbar-track {
  background-color: #f5f5f5;
}

.page-header {
  margin-bottom: 25px;
  text-align: center;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0 0 8px 0;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    
    i {
      color: #409EFF;
      font-size: 28px;
    }
  }
  
  .page-subtitle {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.search-form {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
}

::v-deep .el-form-item {
  margin-bottom: 0;
  margin-right: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.file-table {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  table-layout: auto;
}

// 确保表格可以水平滚动
::v-deep .el-table__body-wrapper {
  overflow-x: auto !important;
  -webkit-overflow-scrolling: touch;
}

::v-deep .el-table__header-wrapper {
  overflow-x: hidden;
}

// 表格滚动条样式
::v-deep .el-table__body-wrapper::-webkit-scrollbar {
  height: 8px;
}

::v-deep .el-table__body-wrapper::-webkit-scrollbar-thumb {
  background-color: #409EFF;
  border-radius: 4px;
}

::v-deep .el-table__body-wrapper::-webkit-scrollbar-track {
  background-color: #f0f0f0;
}

// 操作列固定样式 - 确保右侧固定列可见
::v-deep .el-table__fixed-right {
  z-index: 100 !important;
  box-shadow: -2px 0 12px rgba(0, 0, 0, 0.15) !important;
  background-color: #fff !important;
  border-left: 1px solid #ebeef5 !important;
}

::v-deep .el-table__fixed-right-patch {
  background-color: #fff !important;
}

// 左侧固定列样式
::v-deep .el-table__fixed-left {
  z-index: 100 !important;
  background-color: #fff !important;
  border-right: 1px solid #ebeef5 !important;
}

// 操作列按钮容器
::v-deep .operation-column {
  .cell {
    padding: 0 8px;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}

.operation-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}

::v-deep .el-table__header th {
  background: linear-gradient(to bottom, #f5f7fa, #e8ebef);
  color: #606266;
  font-weight: 600;
}

.table-header-gray {
  background-color: #fafafa;
}

.file-size {
  font-family: 'Courier New', monospace;
  font-weight: 500;
  color: #e6a23c;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 25px;
  padding: 15px 0;
}

::v-deep .el-pagination {
  .el-pager li {
    &.active {
      background-color: #409EFF;
      color: white;
      border-radius: 4px;
    }
    
    &:hover {
      background-color: #ecf5ff;
    }
  }
}

// 预览对话框样式
.preview-content {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  
  .preview-image {
    max-width: 100%;
    max-height: 600px;
    object-fit: contain;
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }
  
  .video-player {
    width: 100%;
    max-height: 600px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    background: #000;
  }
  
  .audio-player-container {
    width: 100%;
    max-width: 500px;
    padding: 30px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
    color: white;
    
    .audio-info {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.2);
      
      .audio-icon {
        font-size: 32px;
        margin-right: 12px;
        color: #fff;
      }
      
      .audio-filename {
        font-size: 14px;
        font-weight: 500;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
      }
    }
    
    .audio-controls {
      position: relative;
      
      audio {
        display: none;
      }
      
      .custom-controls {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 15px;
        
        .el-button {
          flex-shrink: 0;
          width: 56px;
          height: 56px;
          border: none;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
          transition: all 0.3s ease;
          
          &:hover {
            transform: scale(1.1);
          }
          
          &:active {
            transform: scale(0.95);
          }
        }
        
        .time-display {
          flex: 1;
          font-size: 14px;
          font-family: 'Courier New', monospace;
          font-weight: 600;
          letter-spacing: 1px;
          color: rgba(255, 255, 255, 0.9);
          
          span:nth-child(2) {
            margin: 0 8px;
            opacity: 0.6;
          }
        }
      }
    }
  }
  
  .unsupported-file {
    text-align: center;
    padding: 40px;
    
    i {
      font-size: 80px;
      color: #909399;
      margin-bottom: 20px;
    }
    
    p {
      font-size: 16px;
      color: #606266;
      margin-bottom: 20px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .file-management-container {
    padding: 10px;
    max-width: 100vw;
  }
  
  .search-form {
    padding: 15px;
  }
  
  ::v-deep .el-form--inline .el-form-item {
    display: block;
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .action-buttons {
    justify-content: center;
  }
  
  // 小屏幕下调整表格
  ::v-deep .el-table {
    font-size: 12px;
  }
  
  ::v-deep .el-table__cell {
    padding: 8px 5px;
  }
}

// 动画效果
::v-deep .el-table__row {
  transition: all 0.2s ease;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

::v-deep .el-button--circle {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: scale(1.1);
  }
}

::v-deep .el-tag {
  transition: all 0.2s;
  
  &:hover {
    opacity: 0.8;
  }
}
</style>