import { defineConfig } from "vite"
import uni from "@dcloudio/vite-plugin-uni";
import path from 'path';
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons';
export default defineConfig({
	plugins: [
		uni(),
		createSvgIconsPlugin({
			iconDirs: [path.resolve(process.cwd(), 'static/svg-icons')],
			symbolId: 'icon-[name]',
		}),
	],
	// 关键配置：指定需要转译的依赖
	optimizeDeps: {
		include: ['uuid']
	},
	server: {
		proxy: {
			'/api': {
				rewrite: path => path.replace(/^\/api/, ''),
				logLevel: 'debug',
				target: 'http://127.0.0.1:8888',
				changeOrigin: true
			}
		}
	}
})