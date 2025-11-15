<template>
	<div class="login-view" ref="loginView" :style="backImgStyle">
    <el-form :model="loginForm"  status-icon :rules="rules" ref="loginForm"  label-width="60px" class="web-ruleForm" @keyup.enter.native="submitForm('loginForm')">
      <div class="login-brand">{{websiteName}}</div>
      <el-form-item label="用户名" prop="username" v-if="loginForm.loginType===0">
        <el-input type="username" v-model="loginForm.userName" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password" v-if="loginForm.loginType===0">
        <el-input type="password" v-model="loginForm.password" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="loginForm.loginType===0">
        <el-input
            v-model="loginForm.code"
            auto-complete="off"
            placeholder="验证码"
            style="width: 120px"
        >
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      <el-form-item label="邮箱" prop="email" v-if="loginForm.loginType===1">
        <el-input type="email" v-model="loginForm.email" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="" prop="emailCode" v-if="loginForm.loginType===1">
        <el-input
            v-model="loginForm.emailCode"
            auto-complete="off"
            placeholder="邮箱验证码"
            style="width: 120px"
        />
        <el-button class="email-code" :disabled="disabled" @click="sendVerificationCode">{{validateBtn}}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm('loginForm')">登陆</el-button>
        <el-button @click="resetForm('loginForm')">清空</el-button>
      </el-form-item>
      <div class="register-box">
        <div class="register" @click="toRegister()">立即注册</div>
        <div class="forget-pwd">忘记密码</div>
        <p style="clear:both;"></p>
      </div>
      <div class="social-login">
        <div class="social-login-title">社交账号登录</div>
        <div class="social-login-wrapper">
          <svg class="icon svg-icon" aria-hidden="true" @click="qqLogin">
            <use xlink:href="#icon-QQ"></use>
          </svg>
          <svg class="icon svg-icon" aria-hidden="true" @click="toggleLoginType" v-if="loginForm.loginType===0">
            <use xlink:href="#icon-email"></use>
          </svg>
          <svg class="icon svg-icon" aria-hidden="true" @click="toggleLoginType" v-if="loginForm.loginType===1">
            <use xlink:href="#icon-diannao"></use>
          </svg>
        </div>
      </div>
    </el-form>
    <div class="footer-wrap">
      <a href="https://beian.miit.gov.cn/" target="_blank"></a>
    </div>
  </div>
</template>

<script>
import printer from "@/components/common/printer";
import * as THREE from "three";
import BIRDS from "vanta/src/vanta.birds";

	export default {
		name: "login",
    components: {
      printer,
    },
		data() {
      let pattern = new RegExp("[`~!@#$^&*()=|{}':;',\\[\\].<>《》/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？]");
      let reg =/[\u4E00-\u9FA5]|[\uFE30-\uFFA0]/gi;
			let checkUsername = (rule, value, callback) => {
				//console.log("checkUsername");
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
			let checkPassword = (rule, value, callback) => {
				//console.log("checkPassword");
				if (value === '') {
					callback(new Error('请输入密码'));
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
			return {
        websiteName: '青語',
        codeUrl: '',
        validateBtn: '获取验证码',
        disabled: false,
				loginForm: {
          terminal: this.$enums.TERMINAL_TYPE.WEB,
          userName: '',
					password: '',
          code: '',
          uuid: '',
          email: '',
          emailCode: '',
          loginType: 0
				},
				rules: {
          userName: [{
						validator: checkUsername,
						trigger: 'blur'
					}],
					password: [{
						validator: checkPassword,
						trigger: 'blur'
					}],
          email: [{
            validator: checkMail,
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
				},
        backImgStyle: {
          backgroundImage: "url(" + require("@/assets/image/forest.jpeg") + ")",
          backgroundSize: "100% 100%",
          backgroundRepeat: "no-repeat",
          position: "relative",
          display: "flex",
          justifyContent: "space-around",
          width: "100%",
          height: "100%",
        },
			}
		},
    beforeCreate() {
    },
    created() {
      this.getCode();
    },
    methods: {
      getCode() {
        this.$http({
          url: "captchaImage",
          method: "get"
        }).then((result) => {
          this.codeUrl = "data:image/gif;base64," + result['img'];
          this.loginForm.uuid = result['uuid'];
        })
      },
			submitForm(formName) {
				this.$refs[formName].validate((valid) => {
					if (valid) {
						this.$http({
								url: "/login",
								method: 'post',
								data: this.loginForm
							})
							.then((data) => {
                // 保存密码到cookie(不安全)
                this.setCookie('username',this.loginForm.userName);
                //this.setCookie('password',this.loginForm.password);
                // 保存token
                sessionStorage.setItem("accessToken",data.accessToken);
                sessionStorage.setItem("refreshToken",data.refreshToken);
                this.$message.success("天青色等烟雨，而我在等你！");
                this.$router.push("/home/chat");
                this.playAudio();
							})

					}
				});
			},
			resetForm(formName) {
				this.$refs[formName].resetFields();
			},
      playAudio() {
        // 谷歌浏览器提示音需要用户主动交互才能播放，登录入口主动交互一次，后面消息提示音就能正常播放了
        let audio = new Audio();
        let url = require(`@/assets/audio/tip.mp3`);
        audio.src = url;
        audio.play();
      },
			// 获取cookie、
			getCookie(name) {
				let reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
				let arr = document.cookie.match(reg)
			    if (arr){
					 return unescape(arr[2]);
				}
			    return '';
			 },
			  // 设置cookie,增加到vue实例方便全局调用
			 setCookie (name, value, expiredays) {
			    let exdate = new Date();
			    exdate.setDate(exdate.getDate() + expiredays);
			    document.cookie = name + "=" + escape(value) + ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString());
			  },
			  // 删除cookie
			  delCookie (name) {
            let exp = new Date();
            exp.setTime(exp.getTime() - 1);
            let cval = this.getCookie(name);
            if (cval != null){
            document.cookie = name + "=" + cval + ";expires=" + exp.toGMTString();
				  }
			  },
      qqLogin() {
        //保留当前路径
        if (
            navigator.userAgent.match(
                /(iPhone|iPod|Android|ios|iOS|iPad|Backerry|WebOS|Symbian|Windows Phone|Phone)/i
            )
        ) {
          // eslint-disable-next-line no-undef
          QC.Login.showPopup({
            appId: this.config.QQ_APP_ID,
            redirectURI: this.config.QQ_REDIRECT_URI
          });
        } else {
          window.open(
              "https://graph.qq.com/oauth2.0/show?which=Login&display=pc&client_id=" +
              +this.config.QQ_APP_ID +
              "&response_type=token&scope=all&redirect_uri=" +
              this.config.QQ_REDIRECT_URI,
              "_self"
          );
        }
      },
      toggleLoginType() {
        this.loginForm.loginType = this.loginForm.loginType === 0 ? 1 : 0;
      },
      toRegister() {
        this.$router.push("/register");
      },
      sendVerificationCode() {
        if (!this.loginForm.email) {
          this.$message.warning('请输入邮箱');
          return;
        }
        const regEmail = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
        if (!regEmail.test(this.loginForm.email)) {
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
          toEmail: this.loginForm.email,
          category: 'LOGIN'
        }
        this.$http({
          url: "/email/getCode",
          method: "post",
          data: params
        }).then(()=>{

        })
      },
		},
		mounted() {
			this.loginForm.userName = this.getCookie("username");

      this.vantaEffect = BIRDS({
        el: this.$refs.loginView,
        THREE: THREE,
        mouseControls: true,
        touchControls: true,
        gyroControls: false,
        minHeight: 200.0,
        minWidth: 200.0,
        scale: 1.0,
        scaleMobile: 1.0,
        backgroundAlpha: 0.1,
        color1: 0x1a6138,
        color2: 0xf6f7f7
      })
		},
    beforeDestroy() {
      if (this.vantaEffect) {
        console.log("beforeDestroy")
        this.vantaEffect.destroy();
      }
    },
	}
</script>

<style scoped lang="scss">
	.login-view {
    display: flex;
    justify-content: center;
    align-items: center;

		.web-ruleForm {
      width: 350px;
			height: 446px;
			padding: 20px;
			background: rgba(255,255,255,.75);
			box-shadow: 0px 0px  1px #ccc;
			border-radius: 5px;
			overflow: hidden;
	    z-index: 100;
			
			.login-brand {
				line-height: 30px;
				margin: 10px 0 20px 0;
				font-size: 30px;
				font-weight: 600;
				letter-spacing: 2px;
				text-transform: uppercase;
				text-align: center;
        background: linear-gradient(to right, red, orange, yellow, green, cyan, blue,  purple);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        animation:  hue 3s linear infinite;
			}

      .login-code {
        width: 90px;
        height: 38px;
        float: right;
        margin-right: 25px;
        margin-left: 10px;
        img {
          cursor: pointer;
          vertical-align: middle;
        }

        .login-code-img {
          height: 38px;
        }
      }

      @keyframes hue {
        0% {
          filter: hue-rotate(0deg);
        }
        100% {
          filter: hue-rotate(360deg);
        }
      }
			
			.register-box {
				line-height: 40px;

        .register {
          float: left;
          cursor:pointer;
        }

        .forget-pwd {
          float: right;
          cursor:pointer;
        }
			}

      .social-login {
        .social-login-title {
          margin-top: 5px;
          color: #b5b5b5;
          font-size: 0.75rem;
          text-align: center;
        }
        .social-login-title::before {
          content: "";
          display: inline-block;
          background-color: #d8d8d8;
          width: 60px;
          height: 1px;
          margin: 0 12px;
          vertical-align: middle;
        }
        .social-login-title::after {
          content: "";
          display: inline-block;
          background-color: #d8d8d8;
          width: 60px;
          height: 1px;
          margin: 0 12px;
          vertical-align: middle;
        }

        .social-login-wrapper {
          margin-top: 1rem;
          display: flex;
          justify-content: center;
          align-items: center;
          gap: 10px;

          .icon {
            width: 30px;
            height: 30px;
          }
        }
      }
		}

    .footer-wrap {
      z-index: 999;
      color: #cccccc;
      font-size: 14px;
      position: absolute;
      bottom: 5px;

      a, a:visited, a:link, a:hover{
        text-decoration: none;
        color:white;
      }
    }
  }

	
</style>
