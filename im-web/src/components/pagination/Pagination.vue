<template>
    <div class=''>
        <!-- 分页按钮 -->
        <div class="page" v-if="pageNo < totalPage" @click="handlePage">
            加载更多
        </div>
        <div class="tips" v-else>
            <div class="divider"></div>
            <div class="content">没有更多内容了</div>
            <div class="divider"></div>
        </div>
    </div>
</template>
   
<script>

export default {
    name: 'pagination',

    props: {
        pageNo: {
            type: Number,
            default: 0
        },
        totalPage: {
            type: Number,
            default: 0
        },
        changePage: {
            type: Function,
            default: () => { }
        }
    },
    methods: {
        handlePage() {
            this.$emit('changePage', this.pageNo + 1);
        },
    },
}
</script>
   
<style lang='scss' scoped>
.page {
    text-align: center;
    background-color: rgba(0, 0, 0, .8);
    width: 120px;
    height: 30px;
    line-height: 30px;
    border-radius: 50px;
    margin: 10px auto;
    cursor: pointer;
    color: #fff;

    &:hover {
        color: var(--theme-color);
    }
}

.tips {
    text-align: center;
    color: var(--text-color);
    display: flex;
    align-items: center;

    .divider {
        display: inline-block;
        width: 100%;
        background-color: var(--border-line);
        height: 1px;
    }

    .content {
        display: inline-block;
        width: 300px;
        font-size: 14px;
    }
}
</style>