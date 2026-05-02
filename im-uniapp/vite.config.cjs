const { defineConfig } = require("vite");
const uni = require("@dcloudio/vite-plugin-uni");
const path = require('path');
const { createSvgIconsPlugin } = require('vite-plugin-svg-icons');

module.exports = defineConfig({
	plugins: [
		uni(),
		createSvgIconsPlugin({
			iconDirs: [path.resolve(process.cwd(), 'static/svg-icons')],
			symbolId: 'icon-[dir]-[name]',
			inject: true,
			customDomId: '__svg__icons__dom__',
		}),
	],
	// 关键配置：指定需要转译的依赖
	optimizeDeps: {
		include: ['uuid']
	},
	// 构建优化：减少文件数量，提升加载速度
	build: {
		rollupOptions: {
			output: {
				// 手动拆分代码块，减少文件数量
				manualChunks: {
					// 将 uni-ui 组件库打包到一个文件
					'uni-ui': ['@/uni_modules/uview-plus'],
					// 将公共组件打包
					'common-components': [
						'@/components/head-image/head-image.vue',
						'@/components/nav-bar/nav-bar.vue',
					],
				},
				// 减少 chunk 数量
				chunkFileNames: 'assets/[name]-[hash].js',
				entryFileNames: 'assets/[name]-[hash].js',
				assetFileNames: 'assets/[name]-[hash].[ext]',
			}
		},
		// 启用 CSS 代码分割
		cssCodeSplit: true,
		// 提高警告限制
		chunkSizeWarningLimit: 1000,
	},
	server: {
		proxy: {
			'/api': {
				rewrite: path => path.replace(/^\/?api/, ''),
				logLevel: 'debug',
				target: 'http://127.0.0.1:8888',
				changeOrigin: true
			}
		}
	}
});
