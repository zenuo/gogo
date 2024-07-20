「勾勾」是一个搜索工具，搜索结果基于[谷歌搜索](https://google.com)，致力于「安全、简洁」的搜索体验。

“Gogo” is a search tool, search results based on [Google Search](https://google.com), dedicated to ‘safe and concise’ search experience.

### 实例列表 Demo List

> 强烈感谢[webbillion](https://github.com/webbillion)同学的域名服务🤗🎉 Thanks to [webbillion](https://github.com/webbillion) for the domain name service!

- [gogo.webbillion.cn](https://gogo.webbillion.cn/)
- [176.122.157.231:5002](https://176.122.157.231:5002)

## 上手 Get started

```
docker pull ghcr.io/zenuo/gogo:lastest
docker run -p 4998:4998 --name gogo ghcr.io/zenuo/gogo
```

## 如何使用 How to use

### 1 Web

![search.png](image/search.png)

### 2 API

1. 搜索 Search
    ```bash
    $ curl -X GET -k "http://localhost:4998/api/search?q=github&p=1"
    {
      "result": [
        {
          "name": "The world's leading software development platform · GitHub",
          "url": "https://github.com/",
          "desc": "GitHub brings together the world's largest community of developers to discover, share, and build better software. From open source projects to private team ..."
        }
      ],
      "error": null
    }
    ```

2. 关键词提示 Lint
    ```bash
    $ curl -X GET -k "http://localhost:4998/api/lint?q=github"
    {
      "result": [
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

### 从源代码构建 Build from source

```
$ git clone https://github.com/zenuo/gogo.git
$ cd gogo/gogo-server
$ cargo build -rv
$ ./target/release/gogo-server config.json
```

## 参考

- [Hosting SPA with warp in rust](https://ethanfrei.com/posts/hosting-spa-with-warp.html)
- [Publishing Docker images](https://docs.github.com/en/actions/publishing-packages/publishing-docker-images)