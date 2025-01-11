<template>
	<el-dialog title="添加好友" :visible.sync="dialogVisible" width="35%" :before-close="handleClose">
		<el-input  placeholder="输入好友昵称或用户名,最多展示10条" class="input-with-select"
               v-model="searchText" @keyup.enter.native="searchByKeyWord()">
			<el-button slot="append" icon="el-icon-search" @click="searchByKeyWord()"></el-button>
		</el-input>
		<el-scrollbar style="height:400px">
			<div v-for="(user) in users" :key="user.id" v-show="user.id != $store.state.userStore.userInfo.id">
				<div class="item">
					<div class="avatar">
						<head-image :name="user.nickName"
                :url="user.headImage"
                :online="user.online"
            ></head-image>
					</div>
					<div class="add-friend-text">
            <div class="text-user-name">
              <div>{{user.userName}}</div>
              <div :class="user.online ? 'online-status  online':'online-status'">{{ user.online?"[在线]":"[离线]"}}</div>
            </div>
            <div class="text-nick-name">
              <div>昵称:{{user.nickName}}</div>
            </div>
					</div>
					 <el-button type="success" v-show="!isFriend(user.id)" plain @click="handleAddFriend(user)">添加</el-button>
					 <el-button type="info" v-show="isFriend(user.id)" plain disabled>已添加</el-button>
				</div>
			</div>
		</el-scrollbar>
    <div class="page-box">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50]"
          :page-size="10"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
      </el-pagination>
    </div>
	</el-dialog>
</template>

<script>
	import HeadImage from '../common/HeadImage.vue'
	
	
	export default {
		name: "addFriend",
		components:{HeadImage},
		data() {
			return {
				users: [],
				searchText: "",
        currentPage: 1,
        pageSize: 10,
        total: 0,
			}
		},
		props: {
			dialogVisible: {
				type: Boolean
			}
		},
		methods: {
			handleClose() {
				this.$emit("close");
			},
      searchByKeyWord() {
        this.currentPage = 1;
        this.handleSearch();
      },
			handleSearch() {
				this.$http({
					url: `/user/pageFindByNickName?pageNo=${this.currentPage}&pageSize=${this.pageSize}`,
					method: "get",
					params: {
						nickName: this.searchText
					}
				}).then((data) => {
          this.total= data.total;
					this.users = data.data;
				})
			},
			handleAddFriend(user){
				this.$http({
					url: "/friend/add",
					method: "post",
					params: {
						friendId: user.id
					}
				}).then((data) => {
					this.$message.success("添加成功，对方已成为您的好友");
					let friend = {
						id:user.id,
						nickName: user.nickName,
						headImage: user.headImage,
						online: user.online
					}
					this.$store.commit("addFriend",friend);
				})
			},
			isFriend(userId){
				let friends = this.$store.state.friendStore.friends;
				let friend = friends.find((f)=> f.id==userId);			
				return friend != undefined;
			},
      handleSizeChange(pageSize) {
        this.pageSize = pageSize;
			  this.handleSearch();
      },
      handleCurrentChange(currentPage) {
        this.currentPage = currentPage;
        this.handleSearch();
      }
		},
	
		mounted() {
			this.handleSearch();
		}
	}
</script>

<style scoped lang="scss">
  .el-dialog {
    min-width: 400px;
  }

	.item {
		height: 65px;
		display: flex;
		position: relative;
		padding-left: 15px;
		align-items: center;
		padding-right: 25px;
		
		.add-friend-text {
      margin-left: 15px;
      flex: 3;
      display: flex;
      flex-direction: column;
      flex-shrink: 0;
      overflow: hidden;

      .text-user-name{
        display: flex;
        flex-direction: row;
        font-weight: 600;
        font-size: 16px;
        line-height: 25px;

        .online-status{
          font-size: 12px;
          font-weight: 600;
          &.online{
            color: #5fb878;
          }
        }
      }

      .text-nick-name{
        display: flex;
        flex-direction: row;
        font-size: 12px;
        line-height: 20px;
      }
		}
	}
</style>
