![대여해영](resources/대여해영.png)

# 대여해영
대학생활 스마트 대여 플랫폼

---

## 📌 목차
- [소개](#-소개)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [설치 및 실행 방법](#-설치-및-실행-방법)
- [환경 변수 설정](#-환경-변수-설정)
- [주요 기능](#-주요-기능)
- [API 문서](#-api-문서)
- [테스트](#-테스트)
- [기여 방법](#-기여-방법)
- [라이선스](#-라이선스)

---

## 📖 소개

> 대여해영은 대학교 대여 사업에서 대면 절차를 줄이고, 수기 행정 없이 온라인으로 물품을 대여할 수 있는 스마트 대여 플랫폼입니다.  

---

## 🛠 기술 스택
- **Backend**: Spring Boot, JPA, Spring Batch, Spring Security
- **Frontend**: React
- **Database**: MySQL, Redis
- **Infra**: AWS EC2, S3, Docker
- **Etc**: Swagger, OpenAI API

---

## 📂 프로젝트 구조
```bash
 Backend/
 ├── daeyo-ai/
 ├── daeyo-batch/
 ├── daeyo-common/
 ├── daeyo-domain/
 ├── daeyo-external-api/
 ├── daeyo-infra/
 └── scripts/
````

---

## 🚀 설치 및 실행 방법

### 1. 저장소 클론

```bash
git clone https://github.com/Shinhan-DaeyeohaeYoung/Backend.git

cd Backend
```

### 2. 환경 변수 설정

`.env` 또는 `application.yml` 파일에 환경 변수를 설정합니다.
[환경 변수 설정](#-환경-변수-설정) 참고.

### 3. 실행

```bash
# Backend

## 로컬 서버 실행
./gradlew bootRun

## 도커 컴포즈로 실행
cd scripts
docker-compose up -d
```

---

## ⚙ 환경 변수 설정

예시 `.env`:

```env
AWS_REGION=${AWS_REGION}
AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
AWS_S3_BUCKET_NAME=${AWS_S3_BUCKET_NAME}

JWT_SECRET=${JWT_SECRET}
JWT_ACCESS_TOKEN_VALIDITY_SECONDS=${JWT_ACCESS_TOKEN_VALIDITY_SECONDS}
JWT_REFRESH_TOKEN_VALIDITY_SECONDS=${JWT_REFRESH_TOKEN_VALIDITY_SECONDS}

CRYPTO_KEY_BASE64=${CRYPTO_KEY_BASE64}

MYSQL_DB=${MYSQL_DB}
MYSQL_USER=${MYSQL_USER}
MYSQL_ROOT_PW=${MYSQL_ROOT_PW}
MYSQL_PW=${MYSQL_PW}
MYSQL_TZ=Asia/Seoul

DB_HOST=${DB_HOST}
DB_PORT=${DB_PORT}
DB_SOURCE=${DB_SOURCE}
DB_USER=${DB_USER}
DB_PASSWORD=${DB_PASSWORD}

REDIS_HOST=${REDIS_HOST}
REDIS_PORT=${REDIS_PORT}

QR_SCHEME=${QR_SCHEME}
QR_SIZE=${QR_SIZE}
QR_SCAN_URL=${QR_SCAN_URL}
QR_JWT_SECRET=${QR_JWT_SECRET}
QR_JWT_DEFAULT_TTL=${QR_JWT_DEFAULT_TTL}

OPENAI_API_KEY=${OPENAI_API_KEY}
OPENAI_BASE_URL=${OPENAI_BASE_URL}

IMAGE_THRESHOLD_AGENT_URL=${IMAGE_THRESHOLD_AGENT_URL}
```
---

## 👏 팀원 및 팀 소개
|                                      신승용                                      |                                      윤규성                                      |                          이지혜                           |
|:-----------------------------------------------------------------------------:|:-----------------------------------------------------------------------------:|:------------------------------------------------------:|
|        <img src="https://github.com/sso9594.png" width="70%" alt=""/>         |            <img src="https://github.com/kyusung22.png" width="70%">            | <img src="https://github.com/Jihye511.png" width=70%>  |
|                                    BE, 팀장                                     |                                      FE                                       |                           FE                           |
| UI/UX, Figma 디자인 <br>앱 초기 구조 설정<br>앱 공통 컴포넌트 작성<br>모니터링(WebRTC)<br>앱 배포 푸시 알림 | UI/UX, Figma 디자인<br>웹 라우팅 설정<br>웹 공통 컴포넌트 작성<br>웹앱 배포<br>아기 프로필 선택<br>기록 및 통계 | Figma 디자인<br>마이 프로필<br>회원/비회원 관리<br>육아 가이드<br>일기<br>졸업 |

|                           길태은                           |                            안수진                            |                                              
|:-------------------------------------------------------:|:---------------------------------------------------------:|
| <img src="https://github.com/TaeeunKil.png" width="70%"> | <img src="https://github.com/bellecode20.png" width="70%"> |                                              
|                           FE                            |                            FE                             |                                              
|     Webrtc 모니터링<br>실시간 알림 기능<br>프레임 추출<br>CI/CD 작업      |           육아 기록<br>육아 일일,주간 통계<br>육아 졸업<br>배치작업           | 

---

## 🗂️ 기술 스택

## 🛠 기술 스택

### Backend
![Java](https://img.shields.io/badge/Java-17-007396?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-ORM-blue)

### Frontend
![Vue.js](https://img.shields.io/badge/Vue.js-3-4FC08D?logo=vue.js&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-BuildTool-646CFF?logo=vite&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/TailwindCSS-CSS-38B2AC?logo=tailwind-css&logoColor=white)

### Database
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?logo=redis&logoColor=white)

### Infra & DevOps
![AWS](https://img.shields.io/badge/AWS-Cloud-F90?logo=amazon-aws&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Container-2496ED?logo=docker&logoColor=white)
![Jenkins](https://img.shields.io/badge/Jenkins-CI/CD-D24939?logo=jenkins&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Orchestration-326CE5?logo=kubernetes&logoColor=white)

### Tools
![Git](https://img.shields.io/badge/Git-VersionControl-F05032?logo=git&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-API-85EA2D?logo=swagger&logoColor=white)
![Jira](https://img.shields.io/badge/Jira-Agile-0052CC?logo=jira&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-Docs-000000?logo=notion&logoColor=white)


---

## ✨ 주요 기능

- 대여/반납 시스템
  - QR 코드 스캔
  - AI 기반 파손율 판단
- 예약 및 대기열 시스템
  - 실시간 대기열 관리
  - 예약 알림
- 보증금 입출금 자동화- SOL 모임통장
  - 관리자 대시보드
  - 실시간 통계 및 보고서
  - 사용자 관리

---

## 🪜 아키텍처

![대여해영 아키텍처](resources/대여해영_아키텍처.png)

---

## 🔗 ERD

![대여해영 ERD](resources/대여해영_ERD.png)

---

## 📑 API 문서

Swagger 문서는 실행 후 아래 URL에서 확인할 수 있습니다.

* [Swagger UI](http://localhost:8082/swagger-ui.html)

---

## 🤝 기여 방법

1. 이슈 생성 또는 할당
2. `feature/브랜치명`으로 작업
3. 작업 완료 후 PR 생성
4. Merge 후 배포

---


