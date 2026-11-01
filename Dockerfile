# 1. Java 17 런타임 환경 (Temurin JDK 17)
FROM eclipse-temurin:21-jdk-alpine

# 2. 작업 디렉토리 지정
WORKDIR /app

# 3. build/libs 폴더 내부의 -SNAPSHOT.jar 파일 복사
COPY build/libs/*-SNAPSHOT.jar app.jar

# 4. 애플리케이션 포트 노출
EXPOSE 8080

# 5. 서버 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]