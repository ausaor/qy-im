<template>
  <div class='word-container'>
    <div class="word-box">
      <div class="word-btn-top">
        <div class="group-item-box">
          <div class="group-item" :class="wordIndex === 1 ? 'active' : ''" @click="chooseWordGroup(1)">
            <span class="icon iconfont icon-minganci" style="color: #1E90FF;"></span>
          </div>
          <el-divider direction="vertical"></el-divider>
          <div class="group-item" :class="wordIndex === 2 ? 'active' : ''" @click="chooseWordGroup(2)">
            <span class="icon iconfont icon-minganci" style="color: orange"></span>
          </div>
        </div>
        <div class="send" @click="send">发送</div>
      </div>
      <div class="word-list-box">
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
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "CharacterWord",
  props: {
    characterId: {
      type: Number,
      default: null
    },
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      words: {},
      wordIndex: 1,
      activeIndex: -1,
      audio: null,
      audioSrc: '',
      activeWord: null
    }
  },
  methods: {
    getCharacterWord() {
      this.$http({
        url: `/character/word/publishedWord?characterId=${this.characterId}`,
        method: "get",
      }).then((data) => {
        this.words = data
      })
    },
    chooseWordGroup(index) {
      this.wordIndex = index
    },
    chooseWord(word, index) {
      this.activeIndex = index;
      this.activeWord = word;
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
      this.$emit('send', this.activeWord)
    }
  },
  watch: {
    show(val) {
      if (val) {
        this.getCharacterWord()
      }
    }
  },
}
</script>



<style scoped lang="scss">
.word-container {
  min-height: 200px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  padding: 5px;
  border-radius: 5px;

  .word-box {
    text-align: left;
    position: relative;
    height: 100%;

    .word-btn-top {
      position: sticky;
      top: 0;
      display: flex;
      justify-content: space-between;
      width: 100%;
      background-color: #fff;
      z-index: 10;

      .group-item-box {
        display: flex;
        align-items: center;

        .group-item {
          cursor: pointer;

          .icon {
            font-size: 22px;
          }
        }

        .active {
          background-color: #ededed;
        }
      }

      .send {
        cursor: pointer;
        color: #4cd964;
      }
    }

    .word-list-box {
      min-height: 180px;
      max-height: 180px;
      overflow-y: auto;
      margin-top: 0;

      &::-webkit-scrollbar {
        display: none;
      }

      .word-list {
        overflow-y: scroll;

        .word-item {
          display: flex;
          align-items: center;
          padding-right: 10px;
          padding-left: 10px;

          .choose {
            flex: 0 0 14px;
            width: 14px;
            height: 14px;
            border-radius: 50%;
            margin-right: 5px;
            border: 1px solid #4cd964;
            cursor: pointer;
          }

          .choosed {
            background-color: #4cd964;
          }

          .word {
            flex: 1;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
          }
          .voice {
            flex: 0 0 auto;
            cursor: pointer;
          }

          .icon {
            font-size: 20px;
            height: 20px;
          }
        }
      }
    }
  }
}
</style>