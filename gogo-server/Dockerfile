FROM azul/zulu-openjdk-alpine:17-jre-headless-latest
ENV TZ=Asia/Shanghai
WORKDIR /opt/gogo
ADD target/gogo.jar /opt/gogo/gogo.jar
ADD ./application.yml /opt/gogo/application.yml
EXPOSE 4999
ENTRYPOINT ["java","-XX:+HeapDumpOnOutOfMemoryError","-XX:+UseG1GC","-XX:+UseStringDeduplication","-Xms64m","-Xmx64m","-Dguice_bytecode_gen_option=DISABLED","-server","-jar","gogo.jar"]
