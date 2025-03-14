<template>
  <div class='emoji-container'>
    <div class="emojiBox">
      <span class="emoji-item" v-for="(emoText, index) of $emo.originalEmoTextList" :key="index" @click="chooseEmoji(emoText)">
          <el-tooltip class="item" effect="dark" :content="emoText" placement="top">
              <img :src="$emo.textToUrl(emoText)" class="emoji" :title="emoText" />
          </el-tooltip>
      </span>
    </div>
    <div class="btnBox">
      <el-tag type="" effect="dark">表情</el-tag>
    </div>
  </div>
</template>
   
<script>
export default {
    name: 'emoji',
    data() {
        return {
            emojiList: [],
            heoList: [],
            type: 0,
            myEmojiList: [],
            files: [],
            visible: false,
            left: 0,
            top: 0,
            emoji: {},
        }
    },

    created() {
    },
    methods: {
      chooseEmoji(emoText) {
        let emotion = `#${emoText};`
        this.$emit('chooseEmoji', emotion)
      },
    },
}
</script>
   
<style lang='scss' scoped>
.el-radio-button__inner {
    padding: 8px 15px !important;
}

.emoji-container {
    max-width: 400px;
    background-color: #fff;
    border: 1px solid #e4e7ed;
    padding: 5px;
    border-radius: 5px;

    .emojiBox {
        min-height: 150px;
        max-height: 150px;
        max-width: 400px;
        overflow-y: scroll;
        text-align: left;

        &::-webkit-scrollbar {
            display: none;
        }

        .emoji-item {
            cursor: pointer;
            display: inline-block;

            .emoji {
                padding: 3px;
                border-radius: 5px;
                width: 30px;
                height: 30px;

                &:hover {
                    background-color: rgb(221, 221, 221)
                }
            }

            .heoImg {
                width: 40px;
                height: 40px;
                transition: all 0.35s;
                margin-left: 5px;

                &:hover {
                    transform: scale(1.2);
                }
            }
        }
    }

    .btnBox {
        text-align: left;
        margin-top: 20px;
        padding-top: 5px;
        border-top: 2px solid #e4e7ed;
        color: #000;
    }
}
</style>