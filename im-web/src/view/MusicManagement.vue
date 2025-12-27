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
      class="music-table"
      :row-class-name="tableRowClassName"
      stripe
      :header-cell-style="{background: '#f5f7fa', color: '#606266'}"
    >
      <el-table-column type="index" label="序号" width="80" align="center"></el-table-column>
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
      <el-table-column label="操作" min-width="220">
        <template slot-scope="scope">
          <el-button size="mini" type="success" @click.stop="handlePlay(scope.row)" icon="el-icon-video-play" :title="isPlaying && playingId === scope.row.id ? '暂停' : '播放'">
            {{ isPlaying && playingId === scope.row.id ? '暂停' : '播放' }}
          </el-button>
          <el-button size="mini" type="primary" @click.stop="handleEdit(scope.row)" icon="el-icon-edit">编辑</el-button>
          <el-button size="mini" type="danger" @click.stop="handleDelete(scope.row)" icon="el-icon-delete">删除</el-button>
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

    <!-- 音乐播放器 -->
    <div class="music-player" v-if="currentMusic">
      <div class="music-box">
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
          <div class="audio-player-controls">
            <el-button
                type="text"
                @click="togglePlayPause"
                class="play-pause-btn"
            >
              <i :class="isPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'" class="play-icon"></i>
            </el-button>
          </div>
        </div>
      </div>
      <div class="audio-progress">
        <span class="time">{{ formatTime(currentTime) }}</span>
        <el-slider
            v-model="sliderTime"
            :max="currentMusic.duration || 100"
            @change="onSliderChange"
            class="progress-slider"
        ></el-slider>
        <span class="time">{{ formatTime(currentMusic.duration || 0) }}</span>
      </div>
    </div>
    <music-upload ref="musicUploadRef" :category="'public'" :id="form.id" @add="addMusic" @update="updateMusic"></music-upload>
  </div>
</template>

<script>
import MusicUpload from "@components/common/musicUpload.vue";

export default {
  name: "MusicManagement",
  components: {
    MusicUpload
  },
  data() {
    return {
      loading: false,
      searchForm: {
        name: '',
        singer: '',
      },
      tableData: [],
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
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
      currentMusic: null, // 当前播放的音乐
      playingId: null, // 当前正在播放的行ID
      isPlaying: false, // 是否正在播放
      currentTime: 0, // 当前播放时间
      sliderTime: 0, // 滑块时间
      audio: null // 音频元素
    }
  },
  computed: {

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
      this.searchForm.category = 'public'
      this.$http({
        url: `/music/listPage?pageNo=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`,
        method: "post",
        data: this.searchForm
      }).then((data) => {
        this.tableData = data.data;
        this.pagination.total = data.total;
      }).finally(() => {
        this.loading = false;
      })
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
      this.$refs.musicUploadRef.open();
    },
    
    // 编辑
    handleEdit(row) {
      // 复制数据以避免直接修改原数据
      this.form = { ...row };
      this.$refs.musicUploadRef.open();
    },
    
    // 播放音乐
    handlePlay(row) {
      // 如果点击的是当前正在播放的音乐，则暂停
      if (this.playingId === row.id) {
        if (this.isPlaying) {
          this.pauseMusic();
        } else {
          this.playMusic(row);
        }
      } else {
        // 播放新的音乐
        this.playMusic(row);
      }
    },
    
    // 删除
    handleDelete(row) {
      this.$confirm(`确定要删除歌曲 "${row.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let params = {
          id: row.id
        }
        this.$http({
          url: `/music/delete`,
          method: "delete",
          data: params
        }).then((data) => {
          this.$message.success("删除成功");
          // 重新加载数据
          this.loadData();
        })
      }).catch(() => {
        // 取消删除
      });
    },
    addMusic() {
      this.loadData()
    },
    updateMusic() {
      this.loadData()
    },
    
    // 点击行播放
    handleRowClick(row) {
      // 如果点击的是当前正在播放的音乐，则暂停
      if (this.playingId === row.id) {
        if (this.isPlaying) {
          this.pauseMusic();
        } else {
          this.playMusic(row);
        }
      } else {
        // 播放新的音乐
        this.playMusic(row);
      }
    },
    
    // 播放音乐
    playMusic(row) {
      // 如果当前播放的不是同一首歌，则切换歌曲
      if (!this.audio || this.currentMusic.id !== row.id) {
        // 如果已经有音频实例，先暂停
        if (this.audio) {
          this.audio.pause();
        }
        
        // 创建新的音频实例
        this.audio = new Audio(row.url);
        
        // 设置音频事件
        this.audio.onended = () => {
          this.isPlaying = false;
          this.currentTime = 0;
          this.sliderTime = 0;
        };
        
        this.audio.ontimeupdate = () => {
          this.currentTime = this.audio.currentTime;
          this.sliderTime = this.audio.currentTime;
        };
      }
      
      // 更新当前播放音乐信息
      this.currentMusic = row;
      this.playingId = row.id;
      
      this.audio.play().then(() => {
        this.isPlaying = true;
      }).catch(error => {
        console.error('播放失败:', error);
        this.$message.error('播放失败，请检查音频链接');
        this.isPlaying = false;
      });
    },
    
    // 暂停音乐
    pauseMusic() {
      if (this.audio) {
        this.audio.pause();
        this.isPlaying = false;
      }
    },
    
    // 切换播放暂停
    togglePlayPause() {
      if (this.isPlaying) {
        this.pauseMusic();
      } else {
        // 如果存在currentMusic，则播放当前音乐
        if (this.currentMusic) {
          this.playMusic(this.currentMusic);
        }
      }
    },
    
    // 格式化时间
    formatTime(time) {
      const mins = Math.floor(time / 60);
      const secs = Math.floor(time % 60);
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    },
    
    // 滑块改变事件
    onSliderChange() {
      if (this.audio) {
        this.audio.currentTime = this.sliderTime;
        this.currentTime = this.sliderTime;
      }
    },
  },
  
  // 组件销毁时清理音频资源
  beforeDestroy() {
    if (this.audio) {
      this.audio.pause();
      this.audio = null;
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
  height: 100%;
  
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
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 15px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    padding: 15px;
    z-index: 9999;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    
    .music-box {
      display: flex;
      align-items: center;
      margin-bottom: 10px;
      
      .player-info {
        display: flex;
        align-items: center;

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
        padding-left: 20px;
        display: flex;
        flex: 1;
        .audio-player-controls {
          .play-pause-btn {
            font-size: 24px;
            color: white;
            padding: 0;
            
            .play-icon {
              transition: transform 0.3s;
            }
          }
        }
      }
    }
    
    .audio-progress {
      display: flex;
      align-items: center;
      margin-bottom: 10px;

      .time {
        color: white;
        font-size: 12px;
        min-width: 35px;
      }
      
      .progress-slider {
        flex: 1;
        margin: 0 10px;
        height: 6px;
        border-radius: 3px;
        
        ::v-deep .el-slider__runway {
          height: 6px;
          border-radius: 3px;
          
          .el-slider__bar {
            height: 6px;
            border-radius: 3px;
            background-color: #fff;
          }
          
          .el-slider__button {
            width: 14px;
            height: 14px;
            border: 2px solid white;
          }
        }
      }
    }
  }
}
</style>