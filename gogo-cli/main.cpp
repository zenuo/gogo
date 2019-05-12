#include <iostream>
#include <sstream>
#include <curl/curl.h>

using namespace std;

static const char *EXECUTEBALE_NAME = "gogo-cli";

/**
 * @brief do_get 执行GET请求
 * @param key 关键词
 * @param page 页码
 */
void do_get(string key, int page);

/**
 * @brief help 打印帮助信息
 */
void help();

int main(int argc, char *argv[])
{
    //std::cout << argc << std::endl;
    //若命令行参数个数不在[1,2]之间
    if (argc == 1 || argc > 3)
    {
        help();
        return 1;
    } else {
        //关键词
        string key(argv[1]);
        if (argc == 2) {
            do_get(key, 1);
        } else {
            //页数
            string pageString(argv[1]);
            std::istringstream iss(pageString);
            int page = iss.peek();

            if (page <= 0)
            {
                //页数必须大于0
                fprintf(stdout, "Sorry, page must be greater than 0.\r\n");
                exit(1);
            } else {
                do_get(key, page);
            }
        }
    }
}

void do_get(string key, int page)
{
    CURL *curl;
    CURLcode res;

    curl_global_init(CURL_GLOBAL_DEFAULT);
    //初始化
    curl = curl_easy_init();
    if( curl ) {
        //允许自签发的证书
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 0L);

        //URL编码
        char *keyEscaped = curl_easy_escape(curl, key.c_str(), key.length());

        string keyEscapedString(keyEscaped);
        string url = "https://176.122.157.73:5000/api/search?q=" + keyEscapedString + "&p=" + std::to_string(page);

        //设置主机名
        curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
        curl_easy_setopt(curl, CURLOPT_HTTPGET, 1L);

        /* Perform the request, res will get the return code */
        res = curl_easy_perform(curl);
        /* Check for errors */
        if(res != CURLE_OK)
            fprintf(stderr, "curl_easy_perform() failed: %s\n",
                    curl_easy_strerror(res));

        /* always cleanup */
        curl_easy_cleanup(curl);
    }
    curl_global_cleanup();
}

void help()
{
    fprintf(stdout, "usage: %s <key> [page]\r\n", EXECUTEBALE_NAME);
}
