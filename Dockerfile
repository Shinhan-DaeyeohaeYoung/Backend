FROM openjdk:17-jdk-slim AS builder
WORKDIR /app

# 1) Wrapper, 설정 복사
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle

# 2) 멀티모듈 프로젝트의 서브프로젝트들 복사
COPY daeyo-common/ daeyo-common/
COPY daeyo-domain/ daeyo-domain/
COPY daeyo-external-api/ daeyo-external-api/
COPY daeyo-batch/ daeyo-batch/
COPY daeyo-infra/ daeyo-infra/

# 3) API 모듈만 빌드
RUN chmod +x gradlew \
 && ./gradlew :daeyo-external-api:bootJar --no-daemon -x test

FROM openjdk:17-jdk-slim
WORKDIR /app
ARG JAR=daeyo-external-api/build/libs/daeyo-external-api-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/${JAR} app.jar

ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","app.jar"]