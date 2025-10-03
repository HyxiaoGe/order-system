# 基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制jar文件到容器中
COPY gateway-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 9090

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]