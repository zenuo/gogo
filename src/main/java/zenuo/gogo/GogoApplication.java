package zenuo.gogo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 入口类
 *
 * @author zenuo
 * 2018-06-02 19:12:15
 */
@SpringBootApplication
public class GogoApplication {
    public static void main(String[] args) {
        //若当前目录存在配置文件
        if (Files.exists(Paths.get("./application.yml"))) {
            //从当前目录中读取
            System.setProperty("spring.config.location", "file:./application.yml");
        }
        //启动
        SpringApplication.run(GogoApplication.class, args);
    }
}
