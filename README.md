# SeatCatcher Backend Repo
![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/seat-catcher/SeatCatcher-API?utm_source=oss&utm_medium=github&utm_campaign=seat-catcher%2FSeatCatcher-API&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)

## 기술 스택
- Java 17
- Spring Boot 3.4.3
- Spring Data JPA
- Spring Security
- RabbitMQ

## 참여 방법
아래 그대로 콘솔에서 실행해주세요  
모든 명령은 MACOS 및 UBUNTU 환경 기준입니다.  
```bash
git clone https://github.com/seat-catcher/SeatCatcher-API.git
cd path-to-SeatCatcher-API
git checkout -b your-name

# 작업 후
git add .
git commit -m "what: your commit message"
git pull origin main
# 충돌 발생 시 해결 후
git push origin your-name
```

이후, GitHub에서 Pull Request를 생성해주세요.  
Github copilot과 CodeRabbit이 알아서 리뷰를 달아줍니다.  

## 로컬에서 스프링부트 구동하는 방법
### 환경변수 및 파일 확인
src/main/resources 디렉토리 안에 다음 파일들이 존재해야 합니다.  
- application.properties -> 로컬 스프링부트 환경변수 설정 파일입니다.
- seat-catcher-firebase-adminsdk-fbsvc-5d0ae449ec.json -> Firebase Admin SDK 인증 파일입니다.

### 실행 방법 1. docker compose로 구동
```bash
# application.properties 파일의 rabbitmq host를 rabbitmq로 변경
docker compose -f docker-compose.dev.yml up --build -d
docker compose -f docker-compose.dev.yml logs -f backend # 백엔드 로그 확인 (ctrl + c로 종료)
```

### 실행 방법 2. IDE로 구동
```bash
# application.properties의 rabbitmq host를 localhost로 변경
docker compose -f docker-compose.dev.yml up -d rabbitmq
```
그리고 IDE에서 Spring Boot 애플리케이션을 실행하면 됩니다.

## 규칙

### 1. 반드시 기능 개발 / 리팩토링 / 버그 수정 시 테스트 코드 로컬에서 돌려보기
PR 생성 시 Github Actions에서 테스트가 자동으로 실행되지만 로컬에서 먼저 돌려보는 것을 권장드립니다

### 2. PR 생성 시 Github Copilot과 CodeRabbit이 리뷰를 달아줍니다.
리뷰를 참고해서 수정 후 다시 PR을 생성해주세요.

### 3. 생성형 AI 적극 사용 권장
claude code, Gemini cli 좋습니다. 추천드려요

### 4. Commit message, PR message는 알아서 작성하시되, 명확하게 알아볼 수 있게끔 작성해주세요
영어, 한글 상관 없습니다

### 5. PR올리기 전 반드시 main 브랜치에 변경사항 있는지 확인해주세요
충돌 발생 시 반드시 해결 후 PR 올려주세요.