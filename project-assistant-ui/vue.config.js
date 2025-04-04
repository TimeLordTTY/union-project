const { defineConfig } = require("@vue/cli-service");

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    open: true, // 👈 启动时自动打开默认浏览器
  },
});
