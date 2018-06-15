# 一个谷歌搜索的镜像站点项目
> 本程序通过`网页`和`API`两种方式提供服务。

## 1 网页

1)URL: https://176.122.157.73:5000

## 2 API
### 2.1 搜索
1)HTTP方法: GET

2)URL: https://176.122.157.73:5000/api/search?q=github&p=1

3)响应示例：
```
{
  "key": "github",
  "page": 0,
  "amount": 163000000,
  "elapsed": 0.27,
  "entries": [
    {
      "name": "GitHub - Wikipedia",
      "url": "https://en.wikipedia.org/wiki/GitHub",
      "desc": "GitHub is a web-based hosting service for version control using git. It is mostly used for computer code. It offers all of the distributed version control and source ..."
    }
  ],
  "error": null
}
```

### 2.2 补全
1)HTTP方法: GET

2)URL: https://176.122.157.73:5000/api/complete?q=github

3)响应示例:
```
{
  "key": "github",
  "lints": [
    "github",
    "github<b> desktop</b>",
    "github<b> stock</b>",
    "github<b> microsoft</b>",
    "github<b> pages</b>",
    "github<b> api</b>",
    "github<b> tutorial</b>",
    "github<b> login</b>",
    "github<b> markdown</b>",
    "github<b> gist</b>"
  ],
  "error": null
}
```

## 3 使用工具
> 站在巨人的肩膀上

* [Netty](https://netty.io/)
* [Jsoup](https://jsoup.org/)