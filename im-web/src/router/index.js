import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../view/Login'
import Register from '../view/Register'
import ForgetPwd from "@/view/ForgetPwd.vue";
import Home from '../view/Home'
// 安装路由
Vue.use(VueRouter);

// 配置导出路由
export default new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes: [{
      path: "/", 
      redirect: "/login" 
    },
    {
      name: "Login",
      path: '/login',
      component: Login
    },
    {
      name: "Register",
      path: '/register',
      component: Register
    },
    {
      name: "ForgetPwd",
      path: '/forgetPwd',
      component: ForgetPwd
    },
    {
      name: "Home",
      path: '/home',
      component: Home,
	  children:[
		  {
		    name: "Chat",
		    path: "/home/chat",
		    component: () => import("../view/Chat"),
		  },
		{
		  name: "Friends",
		  path: "/home/friend",
		  component: () => import("../view/Friend"),
		},
		{
		  name: "Group",
		  path: "/home/group",
		  component: () => import("../view/Group"),
		},
        {
          name: "RegionGroup",
          path: "/home/regionGroup",
          component: () => import("../view/RegionGroup"),
        },
        {
          name: "ShortVideo",
          path: "/home/shortVideo",
          redirect: "/home/shortVideo/recom",
          component: () => import("../view/ShortVideo.vue"),
          children: [
              {
                  name: "ShortVideoRecom",
                  path: "/home/shortVideo/recom",
                  component: () => import("../view/ShortVideoRecom.vue"),
              },
              {
                  name: "ShortVideoStar",
                  path: "/home/shortVideo/star",
                  component: () => import("../view/ShortVideoStar.vue"),
              },
              {
                  name: "ShortVideoFollow",
                  path: "/home/shortVideo/follow",
                  component: () => import("../view/ShortVideoFollow.vue"),
              },
              {
                  name: "ShortVideoFriend",
                  path: "/home/shortVideo/friend",
                  component: () => import("../view/ShortVideoFriend.vue"),
              },
              {
                  name: "ShortVideoMy",
                  path: "/home/shortVideo/my",
                  component: () => import("../view/ShortVideoMy.vue"),
              },
          ]
        },
        {
          name: "AIChat",
          path: "/home/ai-chat",
          component: () => import("../view/AIChat"),
        },
        {
          name: "Square",
          path: "/home/square",
          redirect: "/home/square/friendActivity",
          component: () => import("../view/Square"),
          children:[
              {
                  name: "TemplateGroup",
                  path: "/home/square/templateGroup",
                  component: () => import("../view/TemplateGroup"),
              },
              {
                  name: "FriendActivity",
                  path: "/home/square/friendActivity",
                  component: () => import("../view/FriendActivity"),
              },
              {
                  name: "StarSpace",
                  path: "/home/square/starSpace",
                  component: () => import("../view/StarSpace"),
              },
              {
                  name: "Review",
                  path: "/home/square/review",
                  component: () => import("../view/Review"),
              },
              {
                  name: "Users",
                  path: "/home/square/users",
                  component: () => import("../view/Users.vue"),
              },
              {
                  name: "Groups",
                  path: "/home/square/groups",
                  component: () => import("../view/GroupManagement.vue"),
              },
              {
                  name: "Regions",
                  path: "/home/square/regions",
                  component: () => import("../view/RegionsManagement.vue"),
              },
              {
                  name: "SysMsg",
                  path: "/home/square/sysMsg",
                  component: () => import("../view/SysMsg.vue"),
              },
              {
                  name: "SysMsgEdit",
                  path: "/home/square/sysMsgEdit",
                  component: () => import("../view/SysMsgEdit.vue"),
              },
              {
                  name: "Musics",
                  path: "/home/square/musics",
                  component: () => import("../view/MusicManagement.vue"),
              },
              {
                  name: "FileInfo",
                  path: "/home/square/fileInfo",
                  component: () => import("../view/FileInfoManage.vue"),
              },
              {
                  name: "TalkManagement",
                  path: "/home/square/talks",
                  component: () => import("../view/TalkManagement.vue"),
              },
              {
                  name: "FeatureControl",
                  path: "/home/square/featureControl",
                  component: () => import("../view/FeatureControl.vue"),
              },
              {
                  name: "ShortVideoManagement",
                  path: "/home/square/shortVideos",
                  component: () => import("../view/ShortVideoManagement.vue"),
              },
          ]
        },
      {
          name: "setting",
          path: "/home/setting",
          component: () => import("../view/setting.vue"),
      },
	  ]
    },
    {
      path: "/oauth/login/qq",
      component: () => import("../components/oauth/OauthLogin.vue")
    },
  ]

});
