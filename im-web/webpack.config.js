const { GitRevisionPlugin } = require('git-revision-webpack-plugin');

module.exports = {
  plugins: [new GitRevisionPlugin()],
  output: {
    publicPath: 'http://my-fancy-cdn.com/(git-revision-version)/',
    filename: '(name)-(git-revision-hash).js'
  }
};