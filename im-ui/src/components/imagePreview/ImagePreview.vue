<template>
    <div class=''>
        <viewer :images="newImages" style="height: 800px;">
        </viewer>
    </div>
</template>
   
<script>

export default {
    name: "ImagePreview",
    props: {
        imgUrls: {
            type: Array
        },
    },
    data() {
        return {
            newImages: []
        }
    },

    watch: {
        img: function (newData, oldData) {
            //console.log(newData)
            this.newImages = []
            for (let i = 0; i < newData.urls; i++) {
                this.newImages.push({ src: newData.urls[i] })
            }
            this.$viewerApi({
                images: this.newImages,
            });
        },
    },
    methods: {

    },
}
</script>
   
<style lang='scss' scoped></style>