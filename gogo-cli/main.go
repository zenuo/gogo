package main

import (
	"crypto/tls"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"os"
	"runtime"
	"strconv"
	"strings"
)

const executableName string = "gogo-cli"
const version string = "2.0.0"
const host string = "176.122.157.231"
const port int = 5002

// Entry 条目
type Entry struct {
	Name string `json:"name"`
	URL  string `json:"url"`
	Desc string `json:"desc"`
}

// SearchResponse 搜索响应
type SearchResponse struct {
	Entries []Entry `json:"result"`
	Error   string  `json:"error"`
}

func main() {
	// 根据命令行参数长度判断
	switch len(os.Args) {
	case 1:
		help()
		break
	case 2:
		request(os.Args[1], 1)
		break
	default:
		// 最后一个参数
		lastArg := os.Args[len(os.Args)-1]
		// 最后一个参数转为整型
		i, err := strconv.Atoi(lastArg)
		if err != nil {
			key := strings.Join(os.Args[1:], " ")
			request(key, 1)
		} else {
			// 将最后一个命令行参数当作页码
			key := strings.Join(os.Args[1:len(os.Args)-1], " ")
			request(key, i)
		}
		break
	}
}

func request(key string, page int) {
	if page < 1 {
		fmt.Println("err: page must be greater than 0")
		os.Exit(1)
	}
	//构造URL
	url := fmt.Sprintf("https://%s:%d/api/search?q=%s&p=%d",
		host,
		port,
		url.QueryEscape(key),
		page)
	http.DefaultTransport.(*http.Transport).TLSClientConfig = &tls.Config{InsecureSkipVerify: true}
	client := &http.Client{}
	req, _ := http.NewRequest(http.MethodGet, url, nil)
	req.Header.Add("User-Agent", fmt.Sprintf("gogo-cli/%s", version))
	//请求
	resp, err := client.Do(req)
	if err != nil {
		fmt.Println("Error: ", err)
		os.Exit(2)
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		fmt.Println("Error: ", err)
		os.Exit(2)
	}
	//解组
	var sr SearchResponse
	e := json.Unmarshal(body, &sr)
	if e != nil {
		fmt.Println("Error: ", e)
		os.Exit(2)
	}
	//判断是否有错误
	if sr.Error != "" {
		fmt.Println("Error: ", sr.Error)
		os.Exit(3)
	}
	//打印
	if runtime.GOOS == "windows" {
		//若是Windows，则不打印样式
		for _, entry := range sr.Entries {
			fmt.Printf("%s\r\n", entry.Name)
			fmt.Printf("    %s\r\n", entry.URL)
			fmt.Printf("    %s\r\n", entry.Desc)
		}
	} else {
		//否则打印样式
		for _, entry := range sr.Entries {
			fmt.Printf("\033[1;33m%s\033[0m\r\n", entry.Name)
			fmt.Printf("    \033[4;32m%s\033[0m\r\n", entry.URL)
			fmt.Printf("    \033[0;2m%s\033[0m\r\n", entry.Desc)
		}
	}
}

func help() {
	fmt.Printf("usage: %s <key> [page]\r\n", executableName)
}
