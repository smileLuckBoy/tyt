TYT为跳一跳小程序刷分工具

* 搭建ADB环境，安装adb命令
* 修改TYT.java文件中`AdbBackend ADB_BACKEND = new AdbBackend("/Users/zsmj/.android/platform-tools/adb", false);`中adb位置
* 本代码中参数适配1920 * 1080,通过checkScreen()函数可检测当前像素，如更换尺寸，请修改jump()中跳跃系数1.37为测试合适值

