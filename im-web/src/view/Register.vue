<template>
	<el-container class="register-view">
		<div>
			<el-form :model="registerForm" status-icon :rules="rules" ref="registerForm" label-width="80px" class="web-ruleForm">
				<div class="register-brand">欢迎注册</div>
				<el-form-item label="用户名" prop="userName">
					<el-input type="userName" v-model="registerForm.userName" autocomplete="off"></el-input>
				</el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input type="email" v-model="registerForm.email" autocomplete="off" placeholder="仅支持126.com,163.com,qq.com,yeah.net,foxmail.com,sina.com,aliyun.com后缀邮箱"></el-input>
        </el-form-item>
				<el-form-item label="昵称" prop="nickName">
					<el-input type="nickName" v-model="registerForm.nickName" autocomplete="off"></el-input>
				</el-form-item>
				<el-form-item label="密码" prop="password">
					<el-input type="password" v-model="registerForm.password" autocomplete="off"></el-input>
				</el-form-item>
				<el-form-item label="确认密码" prop="confirmPassword">
					<el-input type="password" v-model="registerForm.confirmPassword" autocomplete="off"></el-input>
				</el-form-item>
        <el-form-item prop="code">
          <el-input
              v-model="registerForm.code"
              auto-complete="off"
              placeholder="验证码"
              style="width: 63%"
          >
          </el-input>
          <div class="login-code">
            <img :src="codeUrl" @click="getCode" class="login-code-img"/>
          </div>
        </el-form-item>
        <el-form-item prop="emailCode">
          <el-input
              v-model="registerForm.emailCode"
              auto-complete="off"
              placeholder="邮箱验证码"
              style="width: 63%"
          />
          <el-button class="email-code" :disabled="disabled" @click="sendVerificationCode">{{validateBtn}}</el-button>
        </el-form-item>
        <el-form-item>
					<el-button type="primary" @click="submitForm('registerForm')">注册</el-button>
					<el-button @click="resetForm('registerForm')">清空</el-button>
          <el-button type="success" @click="toLogin()" class="to-login">前往登录</el-button>
				</el-form-item>
			</el-form>
		</div>
	</el-container>
</template>

<script>
	export default {
		name: "login",
		data() {
      let pattern = new RegExp("[`~!@#$^&*()=|{}':;',\\[\\].<>《》/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？]");
      let reg =/[\u4E00-\u9FA5]|[\uFE30-\uFFA0]/gi;
      let checkUserName = (rule, value, callback) => {
				if (!value) {
					return callback(new Error('请输入用户名'));
				}
				if (pattern.test(value)) {
          callback(new Error('不允许输入特殊字符'));
        }
        if (reg.test(value)) {
          callback(new Error('用户名不能有中文'));
        }
				callback();
			};
      let checkMail = (rule, value, callback) => {
        if (value === '') {
          return callback(new Error('请输入邮箱'));
        }
        const regEmail = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
        if (!regEmail.test(value)) {
          return callback(new Error('邮箱格式错误'));
        }
        callback();
      };
			let checkNickName = (rule, value, callback) => {
				if (!value) {
					return callback(new Error('请输入昵称'));
				}
        if (pattern.test(value)) {
          callback(new Error('不允许输入特殊字符'));
        }
				callback();
			};
			let checkPassword = (rule, value, callback) => {
				if (value === '') {
					return callback(new Error('请输入密码'));
				}
				callback();
			};

			let checkConfirmPassword = (rule, value, callback) => {
				//console.log("checkConfirmPassword");
				if (value === '') {
					return callback(new Error('请输入密码'));
				}
				if (value !== this.registerForm.password) {
					return callback(new Error('两次密码输入不一致'));
				}
				callback();
			};


			return {
        codeUrl: "",
				registerForm: {
					userName: '',
          email: '',
					nickName: '',
					password: '',
					confirmPassword: '',
          code: '',
          emailCode: '',
          uuid: ''
				},
        validateBtn: '获取验证码',
        disabled: false,
				rules: {
					userName: [{
            required: true,
						validator: checkUserName,
						trigger: 'blur'
					}],
          email: [{
            required: true,
            validator: checkMail,
            trigger: 'blur'
          }],
					nickName: [{
            required: true,
						validator: checkNickName,
						trigger: 'blur'
					}],
					password: [{
            required: true,
						validator: checkPassword,
						trigger: 'blur'
					}],
					confirmPassword: [{
            required: true,
						validator: checkConfirmPassword,
						trigger: 'blur'
					}],
          code: [
              { required: true, trigger: "change", message: "请输入验证码" },
              { min: 1, max: 4, message: "请输入4位验证码", trigger: "change"}
          ],
          emailCode: [
            { required: true, trigger: "change", message: "请输入邮箱验证码" },
            { min: 1, max: 4, message: "请输入4位验证码", trigger: "change"}
          ]
				}
			};
		},
    created() {
		  this.getCode();
    },
    methods: {
			submitForm(formName) {
				this.$refs[formName].validate((valid) => {
					if (valid) {
						this.$http({
								url: "/register",
								method: 'post',
								data: this.registerForm
							})
							.then((data) => {
								this.$message.success("注册成功!");
							}).catch(() => {
							  //this.getCode();
            })
					}
				});
			},
			resetForm(formName) {
				this.$refs[formName].resetFields();
			},
      toLogin() {
        this.$router.push("/login");
      },
      getCode() {
        this.$http({
          url: "captchaImage",
          method: "get"
        }).then((result) => {
          this.codeUrl = "data:image/gif;base64," + result['img'];
          this.registerForm.uuid = result['uuid'];
        })
      },
      sendVerificationCode() {
        if (!this.registerForm.email) {
          this.$message.warning('请输入邮箱');
          return;
        }
        const regEmail = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
        if (!regEmail.test(this.registerForm.email)) {
          this.$message.warning('邮箱格式错误');
          return;
        }

        let time = 90;
        let timer = setInterval(() => {
          if(time === 0){
            clearInterval(timer);
            this.validateBtn = '获取验证码';
            this.disabled = false;
          }else{
            this.disabled = true;
            this.validateBtn = time + '秒后重试';
            time--;
          }
        }, 1000);

        this.getEmailCode();
      },
      getEmailCode() {
        let params = {
          toEmail: this.registerForm.email,
          category: 'REGISTER'
        }
        this.$http({
          url: "/email/getCode",
          method: "post",
          data: params
        }).then(()=>{

        })
      },
		}
	}
</script>

<style scoped lang="scss">
	.register-view {
		position: fixed;
		display: flex;
		justify-content: space-around;
		width: 100%;
		height: 100%;
		background-image: url("../assets/image/background-image.jpg");
		background-size: cover;
		-webkit-user-select: none;
		background-size: cover;
		
		
		.web-ruleForm {
			width: 500px;
			min-height: 500px;
			padding: 20px;
			margin-top: 100px ;
			background: rgba(255,255,255,.75);
			box-shadow: 0px 0px  1px #ccc;
			border-radius: 3px;
			overflow: hidden;
			
			.register-brand {
				line-height: 50px;
				margin: 20px 0 30px 0;
				font-size: 22px;
				font-weight: 600;
				letter-spacing: 2px;
				text-align: center;
				text-transform: uppercase;
			}

      .login-code {
        width: 33%;
        height: 38px;
        float: right;

        img {
          cursor: pointer;
          vertical-align: middle;
        }

        .login-code-img {
          height: 38px;
        }
      }

      .email-code {
        width: 33%;
      }
		}
	}

	

	
</style>
