# 1. Java 17 런타임 환경 (본인 자바 버전에 맞게 수정)
FROM openjdk:17-jdk-slim

# 2. 작업 디렉토리 지정
WORKDIR /app

# 3. 빌드된 JAR 파일 복사 (Gradle 기준)
ARG JAR_FILE=build/libs/*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 4. 애플리케이션 포트 노출
EXPOSE 8080

# 5. 서버 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]