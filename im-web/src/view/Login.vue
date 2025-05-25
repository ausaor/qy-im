<template>
	<div class="login-view" ref="loginView" :style="backImgStyle">
    <el-form :model="loginForm"  status-icon :rules="rules" ref="loginForm"  label-width="60px" class="web-ruleForm" @keyup.enter.native="submitForm('loginForm')">
      <div class="login-brand">{{websiteName}}</div>
      <el-form-item label="用户名" prop="username">
        <el-input type="username" v-model="loginForm.userName" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input type="password" v-model="loginForm.password" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item prop="code">
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
          <el-image
              style="width: 30px; height: 30px"
              :src="require('@/assets/image/qq.png')"
              @click="qqLogin"
              />
        </div>
      </div>
    </el-form>
    <div class="footer-wrap">
      <a href="https://beian.miit.gov.cn/" target="_blank">

      </a>
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
			return {
        websiteName: '輕語',
        codeUrl: '',
				loginForm: {
          terminal: this.$enums.TERMINAL_TYPE.WEB,
          userName: '',
					password: '',
          code: '',
          uuid: ''
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
          code: [
            { required: true, trigger: "change", message: "请输入验证码" },
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
      this.websiteName = Math.floor(Math.random()*2) === 0 ? '輕語' : '青語';
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
        let url = require(`@/assets/audio/tip.wav`);
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
      toRegister() {
        this.$router.push("/register");
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

		.web-ruleForm {
			height: 446px;
			padding: 20px;
			margin-top: 150px ;
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
          font-size: 2rem;
          text-align: center;
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
