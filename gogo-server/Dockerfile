FROM azul/zulu-openjdk-alpine:11-jre
ENV TZ=Asia/Shanghai
WORKDIR /opt/gogo
ADD target/gogo.jar /opt/gogo/gogo.jar
ADD ./application.yml /opt/gogo/application.yml
EXPOSE 4999
ENTRYPOINT ["java","-XX:+UseG1GC","-XX:+UseStringDeduplication","-Xms64m","-Xmx64m","-server","-jar","gogo.jar"]
