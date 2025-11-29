export default {
	state: {
		userInfo: { // 用户信息窗口
			show: false,
			user: {},
			pos:{
				x:0,
				y:0
			}
		},
		fullImage: { // 全屏大图
			show: false,
			url: ""
		},
		theme: {
			colors: [
				{
					name: '青春绿',
					gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
				},
				{
					name: '晨曦粉',
					gradient: 'linear-gradient(135deg, #ffdee0 0%, #f8b5c2 100%)'
				},
				{
					name: '天空蓝',
					gradient: 'linear-gradient(135deg, #d2f3ff 0%, #9fd8ff 100%)'
				},
				{
					name: '薰衣草紫',
					gradient: 'linear-gradient(135deg, #e6e6fa 0%, #d8bfd8 100%)'
				},
				{
					name: '薄荷绿',
					gradient: 'linear-gradient(135deg, #d0f0fd 0%, #98ded9 100%)'
				},
				{
					name: '柠檬黄',
					gradient: 'linear-gradient(135deg, #fff9c4 0%, #fff59d 100%)'
				},
				{
					name: '浅珊瑚',
					gradient: 'linear-gradient(135deg, #ffcab9 0%, #f8a5a5 100%)'
				},
				{
					name: '海洋蓝',
					gradient: 'linear-gradient(135deg, #b3e5fc 0%, #81d4fa 100%)'
				},
				{
					name: '樱花粉',
					gradient: 'linear-gradient(135deg, #f8bbd0 0%, #f48fb1 100%)'
				}
			],
			currentThemeIndex: 0
		},
		chatBubble: {
			bubbles: [
				{
					name: '清新薄荷',
					background: 'linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 100%)',
					color: '#00695c'
				},
				{
					name: '温柔樱花',
					background: 'linear-gradient(135deg, #f8bbd0 0%, #f48fb1 100%)',
					color: '#880e4f'
				},
				{
					name: '宁静天空',
					background: 'linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%)',
					color: '#0d47a1'
				},
				{
					name: '优雅薰衣',
					background: 'linear-gradient(135deg, #ede7f6 0%, #d1c4e9 100%)',
					color: '#4527a0'
				},
				{
					name: '温暖橙阳',
					background: 'linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%)',
					color: '#e65100'
				},
				{
					name: '清新竹叶',
					background: 'linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%)',
					color: '#1b5e20'
				},
				{
					name: '浪漫紫罗兰',
					background: 'linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%)',
					color: '#4a148c'
				},
				{
					name: '纯净白云',
					background: 'linear-gradient(135deg, #fafafa 0%, #eeeeee 100%)',
					color: '#212121'
				},
				{
					name: '海洋之心',
					background: 'linear-gradient(135deg, #e1f5fe 0%, #b3e5fc 100%)',
					color: '#01579b'
				},
				{
					name: '甜美蜜桃',
					background: 'linear-gradient(135deg, #ffe0b2 0%, #ffcc80 100%)',
					color: '#e65100'
				}
			],
			currentBubbleIndex: 0
		}
	},
	mutations: {
		showUserInfoBox(state,user){
			state.userInfo.show = true;
			state.userInfo.user = user;
			
		},
		setUserInfoBoxPos(state,pos){
			let w =  document.documentElement.clientWidth;
			let h =  document.documentElement.clientHeight;
			state.userInfo.pos.x = Math.min(pos.x,w-350);
			state.userInfo.pos.y = Math.min(pos.y,h-200); 
		},
		closeUserInfoBox(state){
			state.userInfo.show = false;
		},
		showFullImageBox(state,url){
			state.fullImage.show = true;
			state.fullImage.url = url;
		},
		closeFullImageBox(state){
			state.fullImage.show = false;
		},
		setThemeColors(state, colors) {
			state.theme.colors = colors;
		},
		setCurrentThemeIndex(state, index) {
			state.theme.currentThemeIndex = index;
		},
		setChatBubbleIndex(state, index) {
			state.chatBubble.currentBubbleIndex = index;
		}
	}
}