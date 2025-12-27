<template>
  <div class="music-management">
    <div class="header">
      <h2 class="title">🎵 歌曲列表管理</h2>
      <p class="subtitle">管理您的音乐库，享受美妙音乐时光</p>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="歌曲名称">
          <el-input 
            v-model="searchForm.name" 
            placeholder="请输入歌曲名称" 
            clearable
            prefix-icon="el-icon-search">
          </el-input>
        </el-form-item>
        <el-form-item label="歌手">
          <el-input 
            v-model="searchForm.singer" 
            placeholder="请输入歌手" 
            clearable
            prefix-icon="el-icon-user">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch" icon="el-icon-search">搜索</el-button>
          <el-button @click="onReset" icon="el-icon-refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮 -->
    <div class="operation">
      <el-button type="primary" @click="handleAdd" icon="el-icon-plus">新增歌曲</el-button>
    </div>

    <!-- 歌曲列表表格 -->
    <el-table
      :data="tableData"
      style="width: 100%"
      v-loading="loading"
      @row-click="handleRowClick"
      class="music-table"
      :row-class-name="tableRowClassName"
      stripe
      :header-cell-style="{background: '#f5f7fa', color: '#606266'}"
    >
      <el-table-column type="index" label="序号" width="80" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.$index === 0">1</el-tag>
          <el-tag v-else>{{ scope.$index + 1 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="歌曲名称" min-width="150">
        <template slot-scope="scope">
          <div class="song-info">
            <span class="song-name">{{ scope.row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="singer" label="歌手" min-width="120">
        <template slot-scope="scope">
          <div class="singer-info">
            <i class="el-icon-user"></i>
            <span>{{ scope.row.singer }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="cover" label="封面图" width="120">
        <template slot-scope="scope">
          <el-image
            :src="scope.row.cover"
            fit="cover"
            lazy
            style="width: 50px; height: 50px; border-radius: 8px;"
            :preview-src-list="[scope.row.cover]"
          >
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="时长" min-width="120">
        <template slot-scope="scope">
          <div class="duration-info">
            <i class="el-icon-time"></i>
            {{ formatDuration(scope.row.duration) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="playCount" label="播放次数" min-width="120">
        <template slot-scope="scope">
          <div class="count-info">
            <i class="el-icon-headset"></i>
            {{ scope.row.playCount }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="likeCount" label="点赞次数" min-width="120">
        <template slot-scope="scope">
          <div class="count-info">
            <i class="el-icon-star-on" style="color: #e6a23c;"></i>
            {{ scope.row.likeCount }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="180">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" @click="handleEdit(scope.row)" icon="el-icon-edit">编辑</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.row)" icon="el-icon-delete">删除</el-button>
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
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
      >
      </el-pagination>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      :before-close="handleDialogClose"
      center
    >
      <el-form :model="form" :rules="formRules" ref="form" label-width="120px">
        <el-form-item label="歌曲名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入歌曲名称"></el-input>
        </el-form-item>
        <el-form-item label="歌手" prop="singer">
          <el-input v-model="form.singer" placeholder="请输入歌手"></el-input>
        </el-form-item>
        <el-form-item label="歌曲链接" prop="url">
          <el-input v-model="form.url" placeholder="请输入歌曲链接"></el-input>
        </el-form-item>
        <el-form-item label="封面图链接" prop="cover">
          <el-input v-model="form.cover" placeholder="请输入封面图链接"></el-input>
        </el-form-item>
        <el-form-item label="时长(秒)" prop="duration">
          <el-input v-model.number="form.duration" placeholder="请输入时长(秒)"></el-input>
        </el-form-item>
        <el-form-item label="播放次数" prop="playCount">
          <el-input v-model.number="form.playCount" placeholder="请输入播放次数"></el-input>
        </el-form-item>
        <el-form-item label="点赞次数" prop="likeCount">
          <el-input v-model.number="form.likeCount" placeholder="请输入点赞次数"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSave">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 音乐播放器 -->
    <div class="music-player" v-if="currentMusic">
      <div class="player-info">
        <el-image
          :src="currentMusic.cover"
          fit="cover"
          style="width: 60px; height: 60px; border-radius: 8px; margin-right: 15px;"
        >
          <div slot="error" class="image-slot">
            <i class="el-icon-picture-outline"></i>
          </div>
        </el-image>
        <div class="player-text">
          <div class="song-name">{{ currentMusic.name }}</div>
          <div class="singer">{{ currentMusic.singer }}</div>
        </div>
      </div>
      <div class="player-controls">
        <vue-audio :src="currentMusic.url" ref="audioPlayer" :autoplay="true" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "MusicManagement",
  data() {
    return {
      loading: false,
      searchForm: {
        name: '',
        singer: ''
      },
      tableData: [],
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      // 模拟数据
      allData: [
        { id: 1, name: '夜曲', singer: '周杰伦', url: 'https://music.example.com/yequ.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 245, playCount: 1200, likeCount: 350 },
        { id: 2, name: '青花瓷', singer: '周杰伦', url: 'https://music.example.com/qinghuaci.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 267, playCount: 2500, likeCount: 890 },
        { id: 3, name: '稻香', singer: '周杰伦', url: 'https://music.example.com/daoxiang.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 210, playCount: 3100, likeCount: 1200 },
        { id: 4, name: '七里香', singer: '周杰伦', url: 'https://music.example.com/qilixiang.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 280, playCount: 2800, likeCount: 950 },
        { id: 5, name: '简单爱', singer: '周杰伦', url: 'https://music.example.com/jiandanai.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 235, playCount: 1900, likeCount: 720 },
        { id: 6, name: '告白气球', singer: '周杰伦', url: 'https://music.example.com/gaobaiqiqiu.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 215, playCount: 3200, likeCount: 1100 },
        { id: 7, name: '菊花台', singer: '周杰伦', url: 'https://music.example.com/juhuatai.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 275, playCount: 1600, likeCount: 480 },
        { id: 8, name: '发如雪', singer: '周杰伦', url: 'https://music.example.com/faruxue.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 305, playCount: 1400, likeCount: 420 },
        { id: 9, name: '东风破', singer: '周杰伦', url: 'https://music.example.com/dongfengpo.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 320, playCount: 2100, likeCount: 650 },
        { id: 10, name: '双截棍', singer: '周杰伦', url: 'https://music.example.com/shuangjiejun.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 195, playCount: 1800, likeCount: 560 },
        { id: 11, name: '龙卷风', singer: '周杰伦', url: 'https://music.example.com/longjuanfeng.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 250, playCount: 2200, likeCount: 720 },
        { id: 12, name: '安静', singer: '周杰伦', url: 'https://music.example.com/anjing.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 260, playCount: 1700, likeCount: 480 },
        { id: 13, name: '轨迹', singer: '周杰伦', url: 'https://music.example.com/guiji.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 240, playCount: 1500, likeCount: 410 },
        { id: 14, name: '忍者', singer: '周杰伦', url: 'https://music.example.com/renzhe.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 200, playCount: 1300, likeCount: 350 },
        { id: 15, name: '星晴', singer: '周杰伦', url: 'https://music.example.com/xingqing.mp3', cover: 'https://via.placeholder.com/50x50.png', duration: 225, playCount: 1650, likeCount: 460 }
      ],
      dialogVisible: false,
      dialogType: 'add', // 'add' 或 'edit'
      form: {
        id: null,
        name: '',
        singer: '',
        url: '',
        cover: '',
        duration: 0,
        playCount: 0,
        likeCount: 0
      },
      formRules: {
        name: [
          { required: true, message: '请输入歌曲名称', trigger: 'blur' }
        ],
        singer: [
          { required: true, message: '请输入歌手', trigger: 'blur' }
        ],
        url: [
          { required: true, message: '请输入歌曲链接', trigger: 'blur' }
        ],
        duration: [
          { required: true, message: '请输入时长', trigger: 'blur' },
          { type: 'number', message: '时长必须为数字值', trigger: 'blur' }
        ],
        playCount: [
          { required: true, message: '请输入播放次数', trigger: 'blur' },
          { type: 'number', message: '播放次数必须为数字值', trigger: 'blur' }
        ],
        likeCount: [
          { required: true, message: '请输入点赞次数', trigger: 'blur' },
          { type: 'number', message: '点赞次数必须为数字值', trigger: 'blur' }
        ]
      },
      currentMusic: null, // 当前播放的音乐
      playingId: null // 当前正在播放的行ID
    }
  },
  computed: {
    dialogTitle() {
      return this.dialogType === 'add' ? '新增歌曲' : '编辑歌曲';
    }
  },
  methods: {
    // 格式化时长
    formatDuration(seconds) {
      const mins = Math.floor(seconds / 60);
      const secs = Math.floor(seconds % 60);
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    },
    
    // 为表格行添加类名以显示播放状态
    tableRowClassName({ row }) {
      if (this.playingId === row.id) {
        return 'playing-row';
      }
      return '';
    },
    
    // 搜索
    onSearch() {
      this.pagination.currentPage = 1; // 搜索时重置到第一页
      this.loadData();
    },
    
    // 重置
    onReset() {
      this.searchForm = {
        name: '',
        singer: ''
      };
      this.pagination.currentPage = 1; // 重置时也重置到第一页
      this.loadData();
    },
    
    // 加载数据
    loadData() {
      this.loading = true;
      
      // 模拟异步请求延迟
      setTimeout(() => {
        // 过滤数据
        let filteredData = this.allData;
        if (this.searchForm.name) {
          filteredData = filteredData.filter(item => 
            item.name.toLowerCase().includes(this.searchForm.name.toLowerCase())
          );
        }
        if (this.searchForm.singer) {
          filteredData = filteredData.filter(item => 
            item.singer.toLowerCase().includes(this.searchForm.singer.toLowerCase())
          );
        }
        
        // 计算总数
        this.pagination.total = filteredData.length;
        
        // 计算当前页数据
        const startIndex = (this.pagination.currentPage - 1) * this.pagination.pageSize;
        const endIndex = startIndex + this.pagination.pageSize;
        this.tableData = filteredData.slice(startIndex, endIndex);
        
        this.loading = false;
      }, 500);
    },
    
    // 分页大小改变
    handleSizeChange(val) {
      this.pagination.pageSize = val;
      this.pagination.currentPage = 1; // 改变页大小时回到第一页
      this.loadData();
    },
    
    // 当前页改变
    handleCurrentChange(val) {
      this.pagination.currentPage = val;
      this.loadData();
    },
    
    // 新增
    handleAdd() {
      this.dialogType = 'add';
      this.form = {
        id: null,
        name: '',
        singer: '',
        url: '',
        cover: '',
        duration: 0,
        playCount: 0,
        likeCount: 0
      };
      this.dialogVisible = true;
    },
    
    // 编辑
    handleEdit(row) {
      this.dialogType = 'edit';
      // 复制数据以避免直接修改原数据
      this.form = { ...row };
      this.dialogVisible = true;
    },
    
    // 删除
    handleDelete(row) {
      this.$confirm(`确定要删除歌曲 "${row.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 从 allData 中删除
        this.allData = this.allData.filter(item => item.id !== row.id);
        // 重新加载数据
        this.loadData();
        this.$message.success('删除成功');
      }).catch(() => {
        // 取消删除
      });
    },
    
    // 保存
    handleSave() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          if (this.dialogType === 'add') {
            // 新增
            const newId = Math.max(...this.allData.map(item => item.id), 0) + 1;
            const newSong = {
              ...this.form,
              id: newId
            };
            this.allData.unshift(newSong);
            this.$message.success('新增成功');
          } else {
            // 编辑
            const index = this.allData.findIndex(item => item.id === this.form.id);
            if (index !== -1) {
              this.allData.splice(index, 1, { ...this.form });
              this.$message.success('编辑成功');
            }
          }
          
          this.dialogVisible = false;
          this.loadData();
        } else {
          this.$message.error('请填写正确的表单信息');
          return false;
        }
      });
    },
    
    // 关闭对话框
    handleDialogClose() {
      this.dialogVisible = false;
    },
    
    // 点击行播放
    handleRowClick(row) {
      // 如果点击的是当前正在播放的音乐，则暂停
      if (this.playingId === row.id) {
        if (this.$refs.audioPlayer) {
          this.$refs.audioPlayer.$refs.audio.pause();
          this.playingId = null;
        }
      } else {
        // 播放新的音乐
        this.currentMusic = row;
        this.playingId = row.id;
        // 稍微延迟以确保DOM更新
        this.$nextTick(() => {
          if (this.$refs.audioPlayer) {
            this.$refs.audioPlayer.$refs.audio.play();
          }
        });
      }
    }
  },
  
  mounted() {
    this.loadData();
  }
}
</script>

<style scoped lang="scss">
.music-management {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
  min-height: 100vh;
  
  .header {
    text-align: center;
    margin-bottom: 30px;
    
    .title {
      color: #2c3e50;
      font-weight: 700;
      font-size: 28px;
      margin-bottom: 10px;
      background: linear-gradient(45deg, #3498db, #2c3e50);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .subtitle {
      color: #7f8c8d;
      font-size: 16px;
    }
  }
  
  .search-form {
    background: #fff;
    padding: 25px;
    border-radius: 12px;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
    margin-bottom: 25px;
    border: 1px solid #ebeef5;
  }
  
  .operation {
    margin-bottom: 25px;
    
    .el-button {
      padding: 12px 20px;
      border-radius: 8px;
      font-weight: 500;
    }
  }
  
  .music-table {
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
    border: 1px solid #ebeef5;
    
    ::v-deep .playing-row {
      background: linear-gradient(to right, #e3f2fd, #f0f9ff) !important;
      color: #409EFF;
      border-left: 4px solid #409EFF;
    }
    
    .song-info {
      display: flex;
      align-items: center;
      
      .song-name {
        font-weight: 500;
        color: #2c3e50;
      }
    }
    
    .singer-info {
      display: flex;
      align-items: center;
      color: #606266;
      
      i {
        margin-right: 5px;
        color: #409EFF;
      }
    }
    
    .duration-info {
      display: flex;
      align-items: center;
      color: #909399;
      
      i {
        margin-right: 5px;
      }
    }
    
    .count-info {
      display: flex;
      align-items: center;
      
      i {
        margin-right: 5px;
      }
    }
  }
  
  .pagination {
    margin-top: 30px;
    text-align: center;
    padding: 20px 0;
    
    ::v-deep .el-pagination {
      .el-pager {
        li.active {
          color: #409EFF;
          border: 1px solid #409EFF;
        }
      }
    }
  }
  
  .music-player {
    position: fixed;
    bottom: 30px;
    right: 30px;
    width: 350px;
    height: 90px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 15px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    display: flex;
    align-items: center;
    padding: 15px;
    z-index: 9999;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    
    .player-info {
      display: flex;
      align-items: center;
      flex: 1;
      
      .player-text {
        margin-left: 15px;
        color: white;
        
        .song-name {
          font-weight: 600;
          font-size: 16px;
          margin-bottom: 5px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          max-width: 150px;
        }
        
        .singer {
          font-size: 13px;
          opacity: 0.8;
        }
      }
    }
    
    .player-controls {
      flex: 1.5;
      
      ::v-deep .vue-audio {
        .audio-wrapper {
          background: rgba(255, 255, 255, 0.2) !important;
          border-radius: 20px;
          padding: 8px 15px !important;
          
          .audio-btn {
            color: white !important;
          }
          
          .audio-timer {
            color: white !important;
          }
          
          .audio-progress {
            background: rgba(255, 255, 255, 0.3) !important;
            
            .audio-progress-current {
              background: white !important;
            }
            
            .audio-progress-circle {
              background: white !important;
            }
          }
        }
      }
    }
  }
}
</style>