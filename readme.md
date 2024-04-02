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

# 配置文件
注：目前为止，以下配置项目确保有效  
剩余项目可能并未实现*（比如什么求购，拍卖啥的）*
```
language: en_US
#交易达成时收取交易税(买家付)(百分比)
tax:
  market: 10.0
  signshop: 5.0
#上架时收取的一次性上架费(卖家付)(固定费用)
fee:
  market: 100.0
  signshop: 0.0
#仓储费，每天向所有上架物品的卖家收取(百分比)
#每天的仓储费 = 基础费用+物品单价*附加费用
storage:
  market:
    #免费天数(注意：-1在这里没有特殊效果，和0天一样！)
    #瓜佬啊，你这句<freedays: -1 # unlimited>给咱干迷糊了好久qwq
    freedays: -1
    #基础费用(固定值)
    base: 0.0
    #附加费用(按单个物品价格的百分比)
    percent: 0
  signshop:
    freedays: -1
    base: 0.0
    percent: 0.0
#槽位限制(最多上架多少种物品)
limits:
  slots:
    market: 5
    signshop-sell: 128
    signshop-buy: 256
  signs: 3
  frames: 12
timers:
  auction:
    duration: 2400
    interval: 60
  request:
    duration: 2400
    interval: 60
sign:
  create:
    max-lock-time-ms: 10000


```