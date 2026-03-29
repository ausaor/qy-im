<template>
	<el-container class="register-view">
		<div class="register-container">
			<div class="register-header">
				<div class="register-logo">
					<img src="@/assets/logo.png" alt="Logo" />
				</div>
				<h2 class="register-title">欢迎注册</h2>
				<p class="register-subtitle">创建您的专属账号</p>
			</div>
					
			<el-form :model="registerForm" status-icon :rules="rules" ref="registerForm" label-width="0px" class="web-ruleForm">
				<el-form-item prop="userName">
					<div class="input-wrapper">
						<i class="el-icon-user input-icon"></i>
						<el-input type="text" v-model="registerForm.userName" autocomplete="off" placeholder="请输入用户名" class="custom-input"></el-input>
					</div>
				</el-form-item>
                <el-form-item prop="email">
     <div class="input-wrapper">
     	<i class="el-icon-message input-icon"></i>
     	<el-input type="email" v-model="registerForm.email" autocomplete="off" placeholder="请输入邮箱地址" class="custom-input"></el-input>
     </div>
                </el-form-item>
				<el-form-item prop="nickName">
					<div class="input-wrapper">
						<i class="el-icon-s-custom input-icon"></i>
						<el-input type="text" v-model="registerForm.nickName" autocomplete="off" placeholder="请输入昵称" class="custom-input"></el-input>
					</div>
				</el-form-item>
				<el-form-item prop="password">
					<div class="input-wrapper">
						<i class="el-icon-lock input-icon"></i>
						<el-input type="password" v-model="registerForm.password" autocomplete="off" placeholder="请输入密码" class="custom-input"></el-input>
					</div>
				</el-form-item>
				<el-form-item prop="confirmPassword">
					<div class="input-wrapper">
						<i class="el-icon-lock input-icon"></i>
						<el-input type="password" v-model="registerForm.confirmPassword" autocomplete="off" placeholder="请再次输入密码" class="custom-input"></el-input>
					</div>
				</el-form-item>
                <el-form-item prop="code">
					<div class="input-wrapper code-wrapper">
						<i class="el-icon-key input-icon"></i>
						<el-input
							v-model="registerForm.code"
							auto-complete="off"
							placeholder="请输入验证码"
							class="custom-input code-input"
						>
						</el-input>
						<div class="login-code">
							<img :src="codeUrl" @click="getCode" class="login-code-img" title="点击刷新"/>
						</div>
					</div>
                </el-form-item>
                <el-form-item prop="emailCode">
					<div class="input-wrapper code-wrapper">
						<i class="el-icon-timer input-icon"></i>
						<el-input
							v-model="registerForm.emailCode"
							auto-complete="off"
							placeholder="请输入邮箱验证码"
							class="custom-input code-input"
						/>
						<el-button class="email-code-btn" :disabled="disabled" @click="sendVerificationCode">{{validateBtn}}</el-button>
					</div>
                </el-form-item>
                <el-form-item class="submit-wrapper">
					<el-button type="primary" @click="submitForm('registerForm')" class="submit-btn">立即注册</el-button>
				</el-form-item>
				<div class="action-links">
					<span>已有账号？</span>
					<el-link type="primary" @click="toLogin()" class="to-login">前往登录</el-link>
				</div>
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
    display: flex;
    justify-content: center;
    align-items: center;
		width: 100%;
		height: 100%;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		background-size: cover;
		position: relative;
		
		&::before {
			content: '';
			position: absolute;
			top: 0;
			left: 0;
			right: 0;
			bottom: 0;
			background: radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
						radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
			pointer-events: none;
		}
		
		.register-container {
			position: relative;
			z-index: 1;
			width: 480px;
			padding: 40px;
			background: rgba(255, 255, 255, 0.95);
			border-radius: 20px;
			box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3),
						0 0 0 1px rgba(255, 255, 255, 0.1) inset;
			backdrop-filter: blur(10px);
			animation: slideUp 0.6s ease-out;
			
			@keyframes slideUp {
				from {
					opacity: 0;
					transform: translateY(30px);
				}
				to {
					opacity: 1;
					transform: translateY(0);
				}
			}
			
			.register-header {
				text-align: center;
				margin-bottom: 35px;
				
				.register-logo {
					width: 80px;
					height: 80px;
					margin: 0 auto 15px;
					background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
					padding: 3px;
					border-radius: 50%;
					box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
					
					img {
						width: 100%;
						height: 100%;
						object-fit: contain;
						border-radius: 50%;
						background: white;
					}
				}
				
				.register-title {
					font-size: 28px;
					font-weight: 700;
					color: #2d3748;
					margin: 0 0 8px 0;
					background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
					-webkit-background-clip: text;
					-webkit-text-fill-color: transparent;
					background-clip: text;
				}
				
				.register-subtitle {
					font-size: 14px;
					color: #718096;
					margin: 0;
					font-weight: 400;
				}
			}
			
			.web-ruleForm {
				width: 100%;
				
				.el-form-item {
					margin-bottom: 20px;
					
					&:last-of-type {
						margin-bottom: 10px;
					}
				}
				
				.input-wrapper {
					display: flex;
					align-items: center;
					width: 100%;
					gap: 12px;
					
					.input-icon {
						flex-shrink: 0;
						color: #a0aec0;
						font-size: 18px;
						transition: color 0.3s ease;
						width: 20px;
						height: 20px;
						display: flex;
						align-items: center;
						justify-content: center;
					}
					
					.custom-input {
						flex: 1;
						
						.el-input__inner {
							height: 48px;
							padding: 0 15px;
							border: 2px solid #e2e8f0;
							border-radius: 12px;
							font-size: 14px;
							transition: all 0.3s ease;
							background: #f7fafc;
							
							&:hover {
								border-color: #cbd5e0;
							}
							
							&:focus {
								border-color: #667eea;
								background: white;
								box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
							}
						}
					}
					
					&.code-wrapper {
						position: relative;
						
						.login-code {
							position: absolute;
							right: 0;
							top: 50%;
							transform: translateY(-50%);
							z-index: 2;
							display: flex;
							align-items: center;
							
							.login-code-img {
								height: 38px;
								width: 90px;
								border-radius: 6px;
								cursor: pointer;
								border: 2px solid #e2e8f0;
								transition: border-color 0.3s ease;
								display: block;
								
								&:hover {
									border-color: #667eea;
								}
							}
						}
						
						.email-code-btn {
							position: absolute;
							right: 0;
							top: 50%;
							transform: translateY(-50%);
							z-index: 2;
							height: 36px;
							padding: 0 15px;
							border: none;
							border-radius: 8px;
							background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
							color: white;
							font-size: 13px;
							font-weight: 600;
							cursor: pointer;
							transition: box-shadow 0.3s ease, opacity 0.3s ease;
							box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
							
							&:hover:not(:disabled) {
								box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5);
							}
							
							&:disabled {
								opacity: 0.6;
								cursor: not-allowed;
								background: #cbd5e0;
							}
						}
					}
				}
				
				.submit-wrapper {
					margin-top: 30px;
					margin-bottom: 20px !important;
					
					.submit-btn {
						width: 100%;
						height: 50px;
						border: none;
						border-radius: 12px;
						background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
						font-size: 16px;
						font-weight: 600;
						letter-spacing: 1px;
						cursor: pointer;
						transition: all 0.3s ease;
						box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
						
						&:hover {
							transform: translateY(-2px);
							box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
						}
						
						&:active {
							transform: translateY(0);
						}
					}
				}
				
				.action-links {
					text-align: center;
					padding-top: 20px;
					border-top: 1px solid #e2e8f0;
					font-size: 14px;
					color: #718096;
					
					.to-login {
						margin-left: 8px;
						font-weight: 600;
						cursor: pointer;
						transition: all 0.3s ease;
						
						&:hover {
							transform: translateX(3px);
						}
					}
				}
			}
		}
	}
</style>
