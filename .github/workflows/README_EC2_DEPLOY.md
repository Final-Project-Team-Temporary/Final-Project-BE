# EC2 배포 설정 가이드

## 개요

`ec2-deploy.yml` 워크플로우는 `develop` 브랜치에 푸시될 때 자동으로 EC2 인스턴스에 애플리케이션을 배포합니다.

## 배포 프로세스

```
1. Code Push to develop branch
   ↓
2. GitHub Actions Triggered
   ↓
3. Build Application (Gradle)
   ↓
4. Build Docker Image
   ↓
5. Push to Docker Hub
   ↓
6. SSH to EC2
   ↓
7. Pull New Image from Docker Hub
   ↓
8. Stop Old Container
   ↓
9. Start New Container
   ↓
10. Health Check
    ↓
11. Deployment Complete ✅
```

## 필수 GitHub Secrets 설정

GitHub Repository → Settings → Secrets and variables → Actions에서 다음 Secrets를 설정해야 합니다:

### 1. 기존 Secrets

| Secret Name | 설명 | 예시 |
|------------|------|------|
| `APP_ENV_B64` | Base64로 인코딩된 환경변수 파일 | `echo -n "$(cat .env)" \| base64` |

### 2. Docker Hub Secrets (필수)

| Secret Name | 설명 | 예시 | 생성 방법 |
|------------|------|------|----------|
| `DOCKERHUB_USERNAME` | Docker Hub 사용자명 | `myusername` | Docker Hub 계정명 |
| `DOCKERHUB_REPOSITORY` | Docker Hub 리포지토리 이름 | `econoeasy-be` | Docker Hub에서 생성한 리포지토리명 |
| `DOCKERHUB_TOKEN` | Docker Hub Access Token | `dckr_pat_...` | Docker Hub → Account Settings → Security → New Access Token |

### 3. EC2 배포용 Secrets (필수)

| Secret Name | 설명 | 예시 | 생성 방법 |
|------------|------|------|----------|
| `EC2_HOST` | EC2 인스턴스 퍼블릭 IP 또는 도메인 | `54.180.123.456` 또는 `api.example.com` | AWS EC2 콘솔에서 확인 |
| `EC2_USER` | EC2 SSH 접속 사용자명 | `ubuntu` (Ubuntu AMI) 또는 `ec2-user` (Amazon Linux) | AMI 타입에 따라 다름 |
| `EC2_SSH_KEY` | EC2 SSH Private Key | `-----BEGIN RSA PRIVATE KEY-----...` | 아래 "SSH Key 생성 방법" 참조 |

### Docker Hub Access Token 생성 방법

1. Docker Hub 로그인 (https://hub.docker.com)
2. Account Settings → Security → New Access Token
3. Token Description 입력 (예: "GitHub Actions Deploy")
4. Access permissions: Read & Write 선택
5. Generate 클릭
6. 생성된 토큰을 복사하여 `DOCKERHUB_TOKEN`에 등록

### SSH Key 생성 및 설정 방법

#### 1. EC2에 SSH Key Pair가 없는 경우

```bash
# 로컬에서 SSH Key 생성
ssh-keygen -t rsa -b 4096 -f ~/.ssh/ec2_deploy_key -C "github-actions-deploy"

# Public Key를 EC2에 복사
ssh-copy-id -i ~/.ssh/ec2_deploy_key.pub ubuntu@YOUR_EC2_IP
# 또는 수동으로 EC2의 ~/.ssh/authorized_keys에 추가

# Private Key 내용을 GitHub Secrets에 등록
cat ~/.ssh/ec2_deploy_key
# 출력된 전체 내용(-----BEGIN부터 -----END까지)을 복사하여 EC2_SSH_KEY에 등록
```

#### 2. 기존 EC2 Key Pair를 사용하는 경우

```bash
# .pem 파일을 Private Key로 변환 (필요시)
chmod 400 your-key.pem

# Private Key 내용 확인
cat your-key.pem
# 출력된 전체 내용을 복사하여 EC2_SSH_KEY에 등록
```

### APP_ENV_B64 생성 방법

```bash
# .env 파일을 Base64로 인코딩
cat .env | base64 | tr -d '\n'

# 또는 (macOS)
base64 -i .env | tr -d '\n'

# 출력된 문자열을 APP_ENV_B64에 등록
```

## EC2 인스턴스 사전 준비사항

EC2 인스턴스에 다음이 설치되어 있어야 합니다:

### 1. Docker 설치

```bash
# Ubuntu의 경우
sudo apt-get update
sudo apt-get install -y docker.io
sudo systemctl start docker
sudo systemctl enable docker

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER
# ⚠️ 로그아웃 후 재로그인 필요

# Amazon Linux 2의 경우
sudo yum update -y
sudo yum install -y docker
sudo service docker start
sudo usermod -a -G docker ec2-user
```

### 2. 포트 설정 (Security Group)

AWS EC2 Console → Security Groups에서:
- **Inbound Rules**에 다음 포트 허용:
  - `8080` (애플리케이션 포트) - 0.0.0.0/0 또는 ALB/ELB에서만
  - `22` (SSH) - GitHub Actions IP 또는 제한된 IP만
  - `80`, `443` (필요시 Nginx/ALB 사용)

### 3. 디스크 공간 확인

```bash
# 디스크 공간 확인
df -h

# Docker 이미지로 인한 디스크 부족 방지를 위해 정기적 정리 설정 (선택사항)
# Cron job 추가
crontab -e

# 매주 일요일 새벽 3시에 Docker 정리
0 3 * * 0 docker system prune -af --volumes
```

## 배포 트리거 방법

### 1. 자동 배포 (Push to develop)

```bash
# develop 브랜치에 푸시하면 자동 배포
git checkout develop
git pull origin develop
git merge feature/your-feature
git push origin develop
# → GitHub Actions 자동 실행
```

### 2. 수동 배포 (Workflow Dispatch)

GitHub Repository → Actions → "EC2 Deploy (CI/CD)" → "Run workflow"
- Branch: develop 선택
- image_tag: latest 또는 특정 commit SHA 입력
- "Run workflow" 클릭

## 배포 확인

### 1. GitHub Actions 로그 확인

GitHub Repository → Actions → 해당 워크플로우 실행 클릭

### 2. EC2에서 직접 확인

```bash
# SSH 접속
ssh -i your-key.pem ubuntu@YOUR_EC2_IP

# 컨테이너 상태 확인
docker ps

# 로그 확인
docker logs econoeasy-be

# 실시간 로그 보기
docker logs -f econoeasy-be

# 애플리케이션 Health Check
curl http://localhost:8080/actuator/health
```

### 3. 외부에서 접근 확인

```bash
# Health Check
curl http://YOUR_EC2_IP:8080/actuator/health

# 또는 브라우저에서
http://YOUR_EC2_IP:8080/actuator/health
```

## 롤백 방법

### 방법 1: 이전 이미지로 재배포

```bash
# GitHub Actions에서 Workflow Dispatch로 실행
# image_tag에 이전 commit SHA 입력
```

### 방법 2: EC2에서 수동 롤백

```bash
# SSH 접속
ssh -i your-key.pem ubuntu@YOUR_EC2_IP

# 이전 이미지 확인
docker images

# 컨테이너 중지 및 제거
docker stop econoeasy-be
docker rm econoeasy-be

# 이전 이미지로 재시작
docker run -d \
  --name econoeasy-be \
  --restart unless-stopped \
  -p 8080:8080 \
  --env-file /home/ubuntu/.env \
  YOUR_DOCKERHUB_USERNAME/YOUR_REPOSITORY:PREVIOUS_TAG
```

## 문제 해결 (Troubleshooting)

### 1. SSH 접속 실패

```
Error: Permission denied (publickey)
```

**해결 방법**:
- EC2_SSH_KEY가 올바르게 등록되었는지 확인
- EC2의 ~/.ssh/authorized_keys에 public key가 있는지 확인
- SSH key 권한 확인 (600 이어야 함)

### 2. Docker Hub 로그인 실패

```
Error: unauthorized: authentication required
```

**해결 방법**:
- DOCKERHUB_USERNAME과 DOCKERHUB_TOKEN이 올바르게 설정되었는지 확인
- Docker Hub Access Token이 만료되지 않았는지 확인
- Token 권한이 Read & Write로 설정되어 있는지 확인

### 3. 컨테이너 시작 실패

```bash
# 컨테이너 로그 확인
docker logs econoeasy-be

# 일반적인 원인:
# - 환경변수 누락 (.env 파일 확인)
# - 포트 충돌 (기존 프로세스가 8080 사용 중)
# - 메모리 부족

# 포트 사용 확인
sudo lsof -i :8080
sudo netstat -nlp | grep 8080
```

### 4. 디스크 공간 부족

```bash
# Docker 정리
docker system prune -af

# 이미지 정리
docker images
docker rmi IMAGE_ID
```

## 보안 권장사항

1. **SSH Key 관리**
   - Private Key는 절대 공개 저장소에 커밋하지 않기
   - GitHub Secrets 사용
   - 정기적으로 Key 교체

2. **Security Group**
   - SSH 포트(22)는 필요한 IP만 허용
   - 가능하면 GitHub Actions IP 범위만 허용

3. **환경변수 관리**
   - APP_ENV_B64는 민감한 정보 포함
   - GitHub Secrets에만 저장
   - EC2의 .env 파일 권한 제한 (chmod 600)

4. **Docker Hub Access Token 관리**
   - Access Token은 절대 공개 저장소에 커밋하지 않기
   - Read & Write 권한만 부여 (Admin 권한 불필요)
   - 정기적으로 Token 재생성 및 교체
   - 사용하지 않는 Token은 즉시 삭제

## 모니터링 및 로깅

### 애플리케이션 로그

```bash
# 컨테이너 로그 확인
docker logs econoeasy-be --tail 100

# 실시간 로그
docker logs -f econoeasy-be

# 로그 파일 위치 (호스트)
/var/lib/docker/containers/CONTAINER_ID/CONTAINER_ID-json.log
```

### 리소스 모니터링

```bash
# 컨테이너 리소스 사용량
docker stats econoeasy-be

# 시스템 리소스
top
htop
free -h
df -h
```

## 기존 ECS 워크플로우

기존 ECS 배포 워크플로우(`cd.yml`, `ci-cd.yml`)는 비활성화되었습니다.
- 참고용으로 보관되어 있으며 실행되지 않습니다.
- 파일 상단에 `DEPRECATED` 표시

## 추가 참고사항

- 현재 설정은 **단일 EC2 인스턴스** 배포용입니다.
- 무중단 배포가 필요한 경우 **Blue-Green** 또는 **Rolling** 배포 전략 고려
- 고가용성이 필요한 경우 **ALB + Auto Scaling Group** 사용 권장

## 문의

배포 관련 문제 발생 시:
1. GitHub Actions 로그 확인
2. EC2 컨테이너 로그 확인
3. 이 문서의 "문제 해결" 섹션 참조