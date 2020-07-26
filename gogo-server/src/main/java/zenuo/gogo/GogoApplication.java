package zenuo.gogo;

import zenuo.gogo.core.Server;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 入口类
 *
 * @author zenuo
 * 2018-06-02 19:12:15
 */
public class GogoApplication {
    public static void main(String[] args) {
        //若当前目录存在配置文件
        final String configFilePath = "./application.yml";
        if (Files.exists(Paths.get(configFilePath))) {
            //从当前目录中读取
            System.setProperty("spring.config.location", "file:" + configFilePath);
        }
        //启动
        new Server();
    }
}
