# syntax=docker/dockerfile:1
FROM azul/zulu-openjdk-alpine:17-jre-headless-latest
ENV TZ=Asia/Shanghai
WORKDIR /opt/gogo
ADD target/gogo.jar /opt/gogo/gogo.jar
ADD ./application.yml /opt/gogo/application.yml
ADD ./gogo-server.sh /opt/gogo/gogo-server.sh
EXPOSE 4999
ENTRYPOINT ["sh","/opt/gogo/gogo-server.sh"]
