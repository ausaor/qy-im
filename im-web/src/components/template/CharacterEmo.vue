<template>
  <div class='emo-container'>
    <div class="emo-box">
      <div class="emo-btn-top">
        <div class="group-item-box">
          <div class="group-item" :class="tabIndex === 1 ? 'active' : ''" @click="changeTab(1)">
            <span class="icon iconfont icon-biaoqing" style="color: #1E90FF;"></span>
          </div>
          <el-divider direction="vertical"></el-divider>
          <div class="group-item" :class="tabIndex === 2 ? 'active' : ''" @click="changeTab(2)">
            <span class="icon iconfont icon-biaoqing" style="color: orange"></span>
          </div>
        </div>
      </div>
      <div class="emo-list-box">
        <div class="emo-list" v-show="tabIndex === 1">
          <div v-for="(emo, i) in emos.character" :key="i" class="emotion-item" @click="chooseEmo(emo)">
            <img :src="emo.url" style="vertical-align:bottom;"/>
          </div>
        </div>
        <div class="emo-list" v-show="tabIndex === 2">
          <div v-for="(emo, i) in emos.group" :key="i" class="emotion-item" @click="chooseEmo(emo)">
            <img :src="emo.url" style="vertical-align:bottom;"/>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "CharacterEmo",
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
      emos: {},
      tabIndex: 1,
    }
  },
  methods: {
    getCharacterEmo() {
      this.$http({
        url: `/character/emo/publishedEmo?characterId=${this.characterId}`,
        method: "get",
      }).then((data) => {
        this.emos = data
      })
    },
    changeTab(index) {
      this.tabIndex = index
    },
    chooseEmo(emo) {
      this.$emit('choose', emo)
    },
  },
  watch: {
    show(val) {
      if (val) {
        this.getCharacterEmo()
      }
    }
  },
}
</script>



<style scoped lang="scss">
.emo-container {
  min-height: 200px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  padding: 5px;
  border-radius: 5px;

  .emo-box {
    text-align: left;
    position: relative;
    height: 100%;

    .emo-btn-top {
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
          width: 30px;
          height: 30px;
          display: flex;
          justify-content: center;
          align-items: center;
          border-radius: 50%;

          .icon {
            font-size: 22px;
          }
        }

        .active {
          background-color: #ededed;
        }
      }
    }

    .emo-list-box {
      min-height: 180px;
      max-height: 180px;
      overflow-y: auto;
      margin-top: 0;

      &::-webkit-scrollbar {
        display: none;
      }

      .emo-list {
        overflow-y: scroll;

        /* grid布局 两端对齐,最后一行左对齐*/
        display: grid;
        grid-template-columns: repeat(5,1fr);
        gap: 5px;
        margin-bottom: 35px;
        overflow-x: hidden;

        .emotion-item {
          text-align: center;
          cursor: pointer;
          padding: 5px;

          img {
            width: 50px;
            height: 50px;
          }
        }
      }
    }
  }
}
</style>