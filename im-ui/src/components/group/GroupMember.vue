<template>
	<div class="group-member" @contextmenu.prevent="showRightMenu($event)">
    <head-image :id="member.userId" :name="member.aliasName"
                :url="member.headImage" :size="50"
                :online="member.online" >
    </head-image>
    <div  v-if="showDel" @click.stop="onDelete()" class="btn-kick el-icon-error"></div>
    <div class="member-name" :title="member.aliasName">{{member.aliasName}}</div>
    <right-menu v-show="rightMenu.show" :pos="rightMenu.pos" :items="rightMenuItems" @close="rightMenu.show=false"
                @select="onSelectMenu"></right-menu>
	</div>
</template>

<script>
	import HeadImage from "../common/HeadImage.vue";
  import RightMenu from '../common/RightMenu.vue';
	export default{
		name: "groupMember",
		components:{
		  HeadImage,
      RightMenu,
    },
		data(){
			return {
        rightMenu: {
          show: false,
          pos: {
            x: 0,
            y: 0
          }
        }
      };
		},
		props:{
			member:{
				type: Object,
				required: true
			},
			showDel:{
				type: Boolean,
				default: false
			},
      rightMenuVisible: {
			  type: Boolean,
        default: false,
      },
      rightMenuItems: {
			  type: Array,
        default: []
      }
		},
		methods:{
      onDelete(){
				this.$emit("del",this.member);
			},
      showRightMenu(e) {
        if (!this.rightMenuVisible) {
          return
        }
        this.rightMenu.pos = {
          x: e.x,
          y: e.y
        };
        this.rightMenu.show = "true";
      },
      onSelectMenu(item) {
        this.$emit(item.key.toLowerCase(), this.member);
      }
		}
	}
</script>

<style lang="scss">
	.group-member{
		display: flex;
		flex-direction: column;
		align-items: center;
		width: 50px;
    position: relative;
		.member-name {
			font-size: 12px;
			text-align: center;
			width: 100%;
			height: 30px;
			line-height: 30px;
			white-space: nowrap;
			text-overflow:ellipsis; 
			overflow:hidden
		}
		
		.btn-kick {
			display: none;
			position: absolute;
			right: -8px;
			top: -8px;
			color: darkred;
			font-size: 20px;
			cursor: pointer;
		}
		
		&:hover .btn-kick{
			display: block;
			color: #ce1818;
		}
	}
</style>
