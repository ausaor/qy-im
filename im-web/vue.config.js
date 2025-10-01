const path = require('path')
const webpack = require("webpack")

module.exports = {

  devServer: {
    hot: true,
    host: '0.0.0.0',
    port: 8080,
    open: true,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8888',
        changeOrigin: true,
        ws: false,
        pathRewrite: {
          '^/api': ''
        }
      }
    }
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@components': path.resolve(__dirname, 'src/components'),
        '@assets': path.resolve(__dirname, 'src/assets')
      }
    },
    plugins: [
      new webpack.ProvidePlugin({
        'window.Quill': 'quill'
      })
    ]
  }
}
