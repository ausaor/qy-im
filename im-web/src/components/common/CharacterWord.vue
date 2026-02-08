<template>
  <div v-show="show">
    <div class="word-box" :style="{'left':x+'px','top':y+'px'}">
      <el-scrollbar style="height:360px;position: relative;">
        <div class="word-list" v-show="wordIndex === 1">
          <div class="word-item" v-for="(item, i) in words.character" :key="i">
              <div class="choose" :class="activeIndex===i ? 'choosed': ''" @click="chooseWord(item, i)"></div>
              <div class="word" :title="item.word">{{item.word}}</div>
              <div class="voice" @click="playVoice(item)">
                <svg class="icon svg-icon" aria-hidden="true">
                  <use xlink:href="#icon-xitongxiaoxi"></use>
                 </svg>
              </div>
          </div>
        </div>
        <div class="word-list" v-show="wordIndex === 2">
          <div class="word-item" v-for="(item, i) in words.group" :key="i">
            <div class="choose" :class="activeIndex===i ? 'choosed': ''" @click="chooseWord(item, i)"></div>
            <div class="word" :title="item.word">{{item.word}}</div>
            <div class="voice" @click="playVoice(item)">
              <svg class="icon svg-icon" aria-hidden="true">
                <use xlink:href="#icon-xitongxiaoxi"></use>
              </svg>
            </div>
          </div>
        </div>
        <div class="word-bottom">
          <div class="btn-group">
            <div class="group-item" :class="wordIndex === 1 ? 'active' : ''" @click="chooseWordGroup(1)">
              <span class="icon iconfont icon-minganci" style="color: #1E90FF;"></span>
            </div>
            <el-divider direction="vertical"></el-divider>
            <div class="group-item" :class="wordIndex === 2 ? 'active' : ''" @click="chooseWordGroup(2)">
              <span class="icon iconfont icon-minganci" style="color: orange"></span>
            </div>
          </div>
          <div class="btn-group">
            <div class="group-item close-btn" @click="close">关闭</div>
            <el-divider direction="vertical"></el-divider>
            <div class="group-item send-btn" @click="send">发送</div>
          </div>
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script>
export default {
  name: "CharacterWord",
  props: {
    words : {
      type: Object,
    }
  },
  data() {
    return {
      show: false,
      pos: {
        x: 0,
        y: 0
      },
      wordIndex: 1,
      activeIndex: -1,
      activeWord: null,
      audio: null,
      audioSrc: '',
    }
  },
  methods: {
    open(pos) {
      this.pos = pos;
      this.show = true;
    },
    close() {
      this.activeIndex = -1;
      this.activeWord = null;
      this.show = false;
    },
    chooseWord(item, index) {
      this.activeIndex = index;
      this.activeWord = item;
    },
    playVoice(item) {
      if (!this.audio) {
        this.audio = new Audio();
        this.audioSrc = item.voice;
        this.audio.src = item.voice;
        this.audio.play();
      } else {
        if (item.voice === this.audioSrc) {
          this.audioSrc = '';
          this.audio.pause();
          this.audio = null;
        } else {
          this.audio.pause();
          this.audio = new Audio();
          this.audioSrc = item.voice;
          this.audio.src = item.voice;
          this.audio.play();
        }
      }
    },
    send() {
      if (!this.activeWord) {
        return
      }
      this.$emit('send', this.activeWord);
      this.activeIndex = -1;
      this.activeWord = null;
    },
    chooseWordGroup(index) {
      this.wordIndex = index;
    }
  },
  computed: {
    x() {
      return this.pos.x - 10;
    },
    y() {
      return this.pos.y - 370;
    }
  }
}
</script>

<style lang="scss" scoped>
.word-box {
  position: fixed;
  width: 450px;
  box-sizing: border-box;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff, #f8f9fa);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  text-align: left;
  border: 1px solid rgba(224, 224, 224, 0.8);
  z-index: 10;

  .el-scrollbar ::v-deep .el-scrollbar__bar {
    display: none !important;
  }

  .el-scrollbar ::v-deep .el-scrollbar__wrap {
    overflow-x: hidden;
  }

  .word-list {
    padding-bottom: 25px;
    overflow-y: scroll;

    .word-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 15px;
      margin: 5px 10px;
      border-radius: 8px;
      transition: all 0.3s ease;
      background: rgba(255, 255, 255, 0.7);
      border: 1px solid rgba(240, 240, 240, 0.8);

      &:hover {
        background: rgba(245, 247, 250, 0.9);
        transform: translateY(-2px);
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
      }

      .choose {
        width: 18px;
        height: 18px;
        border-radius: 50%;
        margin-right: 10px;
        border: 2px solid #409EFF;
        cursor: pointer;
        position: relative;
        transition: all 0.3s ease;

        input {
          appearance: none; /* 清除默认样式 */
          -webkit-appearance: none; /* 兼容WebKit浏览器 */
          outline: none; /* 去除外边框 */
          width: 18px; /* 设置宽度 */
          height: 18px; /* 设置高度 */
          text-align: center; /* 水平居中 */
          line-height: 18px; /* 垂直居中 */
          border: 2px solid #409EFF; /* 边框 */
          border-radius: 50%; /* 圆角 */
        }
        input::after {
          content: '✓'; /* 对号 */
          color: transparent; /* 初始透明 */
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          font-size: 12px;
          font-weight: bold;
        }
        input:checked::after {
          color: #409EFF; /* 选中后变蓝 */
        }
      }

      .choosed {
        background-color: #409EFF;
        border-color: #409EFF;
        
        &::after {
          content: '✓';
          color: white;
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          font-size: 12px;
          font-weight: bold;
        }
      }

      .word {
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        flex: 1;
        font-size: 14px;
        color: #333;
        padding: 0 10px;
      }
      
      .voice {
        cursor: pointer;
        color: #409EFF;
        transition: all 0.3s ease;
        padding: 5px;
        border-radius: 50%;
        
        &:hover {
          background: rgba(64, 158, 255, 0.1);
          transform: scale(1.1);
        }
      }

      .icon {
        font-size: 20px;
        height: 20px;
      }
    }
  }

  .word-bottom {
    position: absolute;
    bottom: 0;
    background: linear-gradient(to top, #f8f9fa, #ffffff);
    display: flex;
    width: 100%;
    height: 40px;
    padding: 8px 15px;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid rgba(224, 224, 224, 0.8);
    border-radius: 0 0 12px 12px;

    .btn-group {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .group-item {
        cursor: pointer;
        height: 30px;
        display: flex;
        align-items: center;
        padding: 0 12px;
        border-radius: 6px;
        font-size: 14px;
        font-weight: 500;
        transition: all 0.3s ease;

        .icon {
          font-size: 20px;
        }
        
        &.close-btn {
          color: #f56c6c;
          border: 1px solid #f56c6c;
          
          &:hover {
            background: rgba(245, 108, 108, 0.1);
          }
        }
        
        &.send-btn {
          color: #409EFF;
          border: 1px solid #409EFF;
          
          &:hover {
            background: rgba(64, 158, 255, 0.1);
          }
        }
      }
    }

    .active {
      background-color: rgba(64, 158, 255, 0.15);
      border: 1px solid #409EFF;
      box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
    }
  }
}
</style>