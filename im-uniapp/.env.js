//设置环境(打包前修改此变量)
const ENV = "DEV";
const UNI_APP = {}
// 高德地图key
UNI_APP.AMAP_KEY = "xxxxxxxxxxxxxxxxxxx";
if(ENV=="DEV"){
	UNI_APP.BASE_URL = "http://127.0.0.1:8888";
	UNI_APP.WS_URL = "ws://127.0.0.1:8878/im";
	// H5 走本地代理解决跨域问题
	// #ifdef H5
		UNI_APP.BASE_URL = "/api";
	// #endif
}
if(ENV=="PROD"){
	UNI_APP.BASE_URL = "http://im.timemory.xyz/api";
	UNI_APP.WS_URL = "ws://im.timemory.xyz/im";
}
export default UNI_APP