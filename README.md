
### 项目介绍

輕語&青語：一个web端即时通讯项目。



### 源码地址

Gitee：https://gitee.com/auraor/qy-im

GitHub：https://github.com/ausaor/qy-im



### 演示视频

web端：https://www.bilibili.com/video/BV1LzfzYdEuk/?vd_source=2578ac860d7d3e326b54e4fa654bdb04

移动端：https://www.bilibili.com/video/BV1W3ZPYNETx/?vd_source=2578ac860d7d3e326b54e4fa654bdb04



### 支持功能

1、私聊（支持好友备注，与好友聊天头像配置）

2、群聊（群聊模式目前有普通群聊，模板群聊，模板角色群聊，多元角色群聊，角色群聊，地区群聊）

--2.1 普通群聊：支持群聊昵称设置以及群头像设置

--2.2 模板群聊：所有群成员群内昵称及头像为同一个群聊模板下的角色名称和头像，群聊角色唯一

--2.3 模板角色群聊：所有群成员群内昵称及头像为同一个群聊模板下的角色名称和头像，群聊角色不唯一

--2.4 多元角色群聊：所有群成员群内昵称及头像为不同群聊模板下的角色名称和头像，群聊角色唯一

--2.5 角色群聊：所有群成员群内昵称及头像为不同群聊模板下的角色名称和头像，群聊角色不唯一

--2.6 不同群聊模式之间支持任意切换；非普通群聊，支持群聊用户切换角色，以及切换角色拥有的名称和头像

--2.7 地区群聊：用户可以选择常驻或临时方式，加入到由系统设置的地区群聊中

--2.8 所有群聊模式支持群动态发布功能

3、聊天消息支持发送文字，表情，图片，视频，音频，文件消息；（文字消息支持敏感词过滤；图片消息支持图片内容是否违禁检测，需开通百度图片内容检测功能）

4、群聊创建、删除和群成员管理、群公告、@群成员等

5、支持会话消息置顶，消息撤回功能

6、朋友圈（支持使用模板角色名称和头像作为动态的用户信息展示，或用于评论和点赞朋友圈）

7、支持用户自定义群聊模板，配置群聊模板角色，以及配置角色拥有的头像昵称，表情，角色台词

8、支持配置系统推送消息，并支持配置不同推送主题（推送消息支持图片，音频，视频类型）



### 技术栈

后端：spring boot 2.7.17，mybatis-plus，druid，netty，redission，hutool......

前端：vue2，element-ui，vuetify，axios，three.js，@amap/amap-jsapi-loader（高德地图依赖）......

数据库：mysql，redis

第三方服务：七牛云（可选），高德地图功能，QQ登录服务



### 开发环境

后端：jdk1.8

前端：nodejs-14



### 项目数据

群聊模板相关图片：https://share.weiyun.com/1UgHOyll

地区&角色台词数据：https://share.weiyun.com/TOJa1qYv



### 项目截图

登录页

<img src="img/image-20250125205609962.png" alt="image-20250125205609962" style="zoom: 33%;float:left;" />
<p style="clear:both;"></p>



私聊

<img src="img/image-20250125210030774.png" alt="image-20250125210030774" style="zoom:33%;float:left;" />
<p style="clear:both;"></p>


普通群聊

<img src="img/image-20250125210203980.png" alt="image-20250125210203980" style="zoom:33%;float:left;" />
<p style="clear:both;"></p>


模板群聊

<img src="img/image-20250125210300257.png" alt="image-20250125210300257" style="zoom:33%;float:left;" />
<p style="clear:both;"></p>


模板角色群聊

<img src="img/image-20250125211913562.png" alt="image-20250125211913562" style="zoom:33%;float:left;" />

<p style="clear:both;"></p>


多元角色群聊

<img src="img/image-20250125212012095.png" alt="image-20250125212012095" style="zoom:33%;float:left;" />

<p style="clear:both;"></p>


角色群聊

<img src="img/image-20250125212055589.png" alt="image-20250125212055589" style="zoom:33%;float:left;" />

<p style="clear:both;"></p>

系统类消息

<img src="img/image-20250126135849320.png" alt="image-20250126135849320" style="zoom:33%;float:left;" />

<p style="clear:both;"></p>

<img src="img/image-20250126140014025.png" alt="image-20250126140014025" style="zoom:33%;float:left;" />

<p style="clear:both;"></p>



好友列表

<img src="img/image-20250125212204491.png" alt="image-20250125212204491" style="zoom:33%;float:left;" />

<p style="clear:both;"></p>


群聊列表

<img src="img/image-20250125212324438.png" alt="image-20250125212324438" style="zoom:33%;float:left;" />
<p style="clear:both;"></p>


地区群聊

<img src="img/image-20250125212613512.png" alt="image-20250125212613512" style="zoom:33%;float:left;" />

<p style="clear:both;"></p>
<img src="img/image-20250126142630247.png" alt="image-20250126142630247" style="zoom:33%;float:left;" />

<p style="clear:both;"></p>


空间动态

<img src="img/image-20250125212835186.png" alt="image-20250125212835186" style="zoom:33%;float:left;" />

<p style="clear:both;"></p>
<img src="img/image-20250125212905811.png" alt="image-20250125212905811" style="zoom:33%;float:left;" />

<p style="clear:both;"></p>

群聊模板

<img src="img/image-20250126145531206.png" alt="image-20250126145531206" style="zoom:33%;float:left;" />

<p style="clear:both;"></p>

<img src="img/image-20250126145628794.png" alt="image-20250126145628794" style="zoom:33%;float:left;" />

<p style="clear:both;"></p>

移动端

<img src="img/image-20250329181644512.png" alt="image-20250329181644512" style="zoom:25%;" /><img src="img/image-20250329181750784.png" alt="image-20250329181750784" style="zoom:25%;" /><img src="img/image-20250329181844423.png" alt="image-20250329181844423" style="zoom:25%;" /><img src="img/image-20250329182004686.png" alt="image-20250329182004686" style="zoom:25%;" /><img src="img/image-20250329182110536.png" alt="image-20250329182110536" style="zoom:25%;" />



<img src="img/image-20250329182240063.png" alt="image-20250329182240063" style="zoom:25%;" /><img src="img/image-20250329182321819.png" alt="image-20250329182321819" style="zoom:25%;" /><img src="img/image-20250329195759183.png" alt="image-20250329195759183" style="zoom:25%;" /><img src="img/image-20250329195846128.png" alt="image-20250329195846128" style="zoom:25%;" /><img src="img/image-20250329195929705.png" alt="image-20250329195929705" style="zoom:25%;" />

### 联系方式

邮箱：qingyu017@qq.com

有任何问题，欢迎给我留言哦



### 交流群

QQ群号：157414719




### 点下star吧
喜欢的朋友麻烦点个star，鼓励一下作者吧！



### 项目赞赏

如果您喜欢这个项目，请作者喝杯可乐也可以的哦

<img src="img/image-20250125213832882.png" alt="image-20250125213832882" style="zoom:50%;float:left;" />

<p style="clear:both;"></p>


### 参考项目

https://gitee.com/bluexsx/box-im
