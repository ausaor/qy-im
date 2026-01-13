import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../view/Login'
import Register from '../view/Register'
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
          name: "AIChat",
          path: "/home/ai-chat",
          component: () => import("../view/AIChat"),
        },
        {
          name: "Square",
          path: "/home/square",
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
              }
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
