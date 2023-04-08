FROM openjdk:11-jre-slim
COPY . /app
WORKDIR /app
CMD ["java", "-jar", "your-app.jar"]
