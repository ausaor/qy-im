<template>
  <div v-show="show">
    <div class="word-box" :style="{'left':x+'px','top':y+'px'}">
      <el-scrollbar style="height:250px;position: relative;">
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
            <div class="group-item" :class="wordIndex === 1 ? 'active' : ''" @click="chooseWordGroup(1)">角色</div>
            <el-divider direction="vertical"></el-divider>
            <div class="group-item" :class="wordIndex === 2 ? 'active' : ''" @click="chooseWordGroup(2)">群聊</div>
          </div>
          <div class="btn-group">
            <div class="group-item" style="color: #d42e07;cursor: pointer;" @click="close">关闭</div>
            <el-divider direction="vertical"></el-divider>
            <div class="group-item" style="color: #409EFF;cursor: pointer;" @click="send">发送</div>
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
      let audio = new Audio();
      audio.src = item.voice;
      audio.play();
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
      return this.pos.x - 200;
    },
    y() {
      return this.pos.y - 280;
    }
  }
}
</script>

<style lang="scss" scoped>
.word-box {
  position: fixed;
  min-width: 300px;
  box-sizing: border-box;
  border: 1px solid #b4b4b4;
  border-radius: 5px;
  background-color: #f5f5f5;
  box-shadow: 0 0 10px #ccc;
  text-align: left;

  .word-list {
    padding-bottom: 25px;
    overflow-y: scroll;

    .word-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-right: 10px;
      padding-left: 10px;

      .choose {
        width: 14px;
        height: 14px;
        border-radius: 50%;
        margin-right: 5px;
        border: 1px solid #4cd964;
        cursor: pointer;

        input {
          appearance: none; /* 清除默认样式 */
          -webkit-appearance: none; /* 兼容WebKit浏览器 */
          outline: none; /* 去除外边框 */
          width: 18px; /* 设置宽度 */
          height: 18px; /* 设置高度 */
          text-align: center; /* 水平居中 */
          line-height: 18px; /* 垂直居中 */
          border: 1px solid #4cd964; /* 边框 */
          border-radius: 50%; /* 圆角 */
        }
        input::after {
          content: '√'; /* 对号 */
          color: transparent; /* 初始透明 */
        }
        input:checked::after {
          color: red; /* 选中后变红 */
        }
      }

      .choosed {
        background-color: #4cd964;
      }

      .word {
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        width: 258px;
      }
      .voice {
        cursor: pointer;
      }

      .icon {
        font-size: 20px;
        height: 20px;
      }
    }
  }

  &:after {
    content: "";
    position: absolute;
    left: 185px;
    bottom: -30px;
    width: 0;
    height: 0;
    border-style: solid dashed dashed;
    border-color: #f5f5f5 transparent transparent;
    overflow: hidden;
    border-width: 15px;
  }

  .word-bottom {
    position: absolute;
    bottom: 0;
    background: #f5f5f5;
    display: flex;
    width: 100%;
    padding: 5px;
    justify-content: space-between;
    align-items: center;

    .btn-group {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .group-item {
        cursor: pointer;
      }
    }

    .active {
      background-color: #b6e8d9;
    }
  }
}
</style>