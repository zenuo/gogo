package main

import (
	"crypto/tls"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"os"
	"strconv"
)

const executableName string = "gogo-cli"
const version string = "0.1"
const host string = "176.122.157.73"
const port int = 5000

func main() {
	switch len(os.Args) {
	case 2:
		request(os.Args[1], 1)
		break
	case 3:
		i, error := strconv.Atoi(os.Args[2])
		if error != nil {
			fmt.Println("Error: ", error)
			os.Exit(1)
		}
		request(os.Args[1], i)
		break
	default:
		help()
	}
}

func request(key string, page int) {
	if page < 1 {
		fmt.Println("Error: page must be greater than 0")
		os.Exit(1)
	}
	url := fmt.Sprintf("https://%s:%d/api/search?q=%s&p=%d",
		host,
		port,
		url.QueryEscape(key),
		page)
	http.DefaultTransport.(*http.Transport).TLSClientConfig = &tls.Config{InsecureSkipVerify: true}
	client := &http.Client{}
	req, _ := http.NewRequest("GET", url, nil)
	req.Header.Add("User-Agent", fmt.Sprintf("gogo-cli/%s", version))
	resp, error := client.Do(req)
	if error != nil {
		fmt.Println("Error: ", error)
		os.Exit(2)
	}
	defer resp.Body.Close()
	body, error := ioutil.ReadAll(resp.Body)
	if error != nil {
		fmt.Println("Error: ", error)
		os.Exit(2)
	}
	json := string(body)
	fmt.Println(json)
}

func help() {
	fmt.Printf("usage: %s <key> [page]\r\n", executableName)
}
