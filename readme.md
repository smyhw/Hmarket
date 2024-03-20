# Hmarket
> 喵窝同款天喵市场

这是蓝瓜的原始仓库 -> [https://github.com/NyaaCat/Hmarket](https://ci.nyaacat.com/job/NyaaCore/)

# 如何安装？
1. 依赖`nyaacore`和`Vault`，需要先安装他们
2. 把依赖和本插件都扔进`plugins`文件夹
3. 启动服务器
4. 给玩家配置权限
5. (可选)编辑配置文件，将语言更改为中文(`language: zh_CN`)
6. 完事~

* nyaacore可以从nyaacat的ci处获取 -> [https://ci.nyaacat.com/job/NyaaCore/](https://ci.nyaacat.com/job/NyaaCore/)
* vault的项目地址 -> [https://github.com/MilkBowl/Vault](https://github.com/MilkBowl/Vault)

**注意**:
* nyaacore似乎严格区分游戏版本


# 权限
给予玩家`hmarket.mall`和`hmarket.my`节点即可

*更具体一点？那咱也不知道，蓝瓜都没写readme!*

# 做了哪些更改？
* 原版依赖`nyaaecore`，这枝fork使其改为依赖`Vault`
* 税款直接扔虚空，没有系统账户来接收它们
* 中文语言文件；这不是喵窝的原版文本，那并不公开