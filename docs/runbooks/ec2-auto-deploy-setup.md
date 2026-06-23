# EC2 자동 배포 셋업 Runbook

`scripts/auto-deploy.sh`를 EC2에서 cron으로 동작시키기 위한 1회성 준비 절차.
대상: Amazon Linux 2 / Amazon Linux 2023, Docker / Docker Compose v2 설치 완료된 EC2 인스턴스 (기본 사용자 `ec2-user`).

설계 문서: `docs/superpowers/specs/2026-06-23-ec2-cron-auto-deploy-design.md`

## 0. 사전 조건 확인

```bash
docker --version          # Docker 24+ 권장
docker compose version    # v2 필수 ("docker-compose"가 아니라 "docker compose")
git --version
flock --version           # util-linux 포함
```

## 1. 프로젝트 clone

```bash
sudo mkdir -p /home/ec2-user
cd /home/ec2-user
git clone https://github.com/MemoReel/backend.git
cd backend
```

이미 clone되어 있다면:

```bash
cd /home/ec2-user/backend
git remote -v        # origin이 올바른 리포지토리인지 확인
git checkout main
git pull --ff-only
```

## 2. `.env` 파일 작성

`/home/ec2-user/backend/.env`에 운영 환경변수를 작성한다. 키는 `docker-compose.yml`의 `environment:` 블록에서 참조하는 모든 항목.

```bash
cat > /home/ec2-user/backend/.env <<'EOF'
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
DB_URL=jdbc:mysql://<RDS_ENDPOINT>:3306/<DB_NAME>
DB_USERNAME=<USER>
DB_PASSWORD=<PASSWORD>
DB_DRIVER=com.mysql.cj.jdbc.Driver
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
MEMOREEL_STORAGE_S3_BUCKET=<BUCKET>
MEMOREEL_STORAGE_S3_REGION=ap-northeast-2
ANTHROPIC_API_KEY=<KEY>
EOF
chmod 600 /home/ec2-user/backend/.env
```

> `.env`는 git 비추적. 절대 커밋하지 말 것.

## 3. docker 그룹 권한

`ec2-user` 사용자가 sudo 없이 docker를 실행할 수 있어야 cron이 정상 동작한다.

```bash
sudo usermod -aG docker ec2-user
```

적용을 위해 한 번 로그아웃 후 재로그인. 확인:

```bash
docker ps    # 에러 없이 컨테이너 목록이 떠야 함
```

## 4. 로그 디렉토리 생성

```bash
sudo mkdir -p /var/log/memoreel
sudo chown ec2-user:ec2-user /var/log/memoreel
```

## 5. (권장) Swap 구성

t3.small 이하 인스턴스는 Gradle `--no-cache` 빌드 중 OOM 위험. 2GB swap을 1회 구성한다.

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
free -h    # Swap 행에 2.0Gi 표시 확인
```

## 6. 수동 1회 실행으로 검증

```bash
cd /home/ec2-user/backend
./scripts/auto-deploy.sh
```

기대 동작:
- 첫 실행: clone 직후라면 SHA가 이미 동일 → `no change` 로그 후 종료, **또는** 새 커밋이 있으면 풀 빌드 후 컨테이너 기동.
- 강제 빌드를 보고 싶다면 임의로 `git reset --hard HEAD~1` 후 재실행 (검증 끝나면 원복).

`docker ps`로 컨테이너가 떠 있는지 확인:

```bash
docker ps --filter name=memoreel-backend
```

## 7. crontab 등록

```bash
crontab -e
```

다음 한 줄을 추가:

```cron
*/5 * * * * /home/ec2-user/backend/scripts/auto-deploy.sh >> /var/log/memoreel/auto-deploy.log 2>&1
```

확인:

```bash
crontab -l
```

## 8. 동작 확인

5분 이내에 첫 로그가 남는지 확인:

```bash
tail -f /var/log/memoreel/auto-deploy.log
```

기대 로그 예시:

```
[2026-06-23 10:30:01] fetch: origin/main
[2026-06-23 10:30:02] no change: HEAD=<sha>
```

이후 의도적으로 작은 변경(예: README 줄바꿈)을 `origin/main`에 push한 뒤 5분 내 다음 로그가 남는지 확인:

```
[2026-06-23 10:35:01] update detected: <old-sha> -> <new-sha>
[2026-06-23 10:35:01] git pull --ff-only
[2026-06-23 10:35:02] docker compose down
[2026-06-23 10:35:05] docker compose build --no-cache app
[2026-06-23 10:38:30] docker compose up -d
[2026-06-23 10:38:35] docker image prune -f
[2026-06-23 10:38:35] deploy success: now at <new-sha>
```

## 문제 해결

| 증상 | 원인 / 조치 |
| --- | --- |
| `permission denied` on docker 명령 | 4단계 docker 그룹 권한 누락. 재로그인 필요 |
| `error: Your local changes ... would be overwritten` | EC2에 우발적 로컬 수정. `git status`로 확인 후 `git stash` 또는 `git reset --hard origin/main` |
| 빌드 중 OOM kill | 5단계 swap 구성 |
| cron 로그가 안 남음 | crontab 등록 확인 (`crontab -l`), `/var/log/memoreel/` 권한 확인 |
| 락 파일이 계속 잡혀 있음 | 이전 빌드가 중단됨. `rm /tmp/memoreel-auto-deploy.lock` |

## 비활성화 / 롤백

자동 배포를 잠시 멈추려면:

```bash
crontab -e    # 해당 라인 주석 처리
```

특정 SHA로 강제 고정:

```bash
cd /home/ec2-user/backend
git reset --hard <sha>
docker compose down
docker compose build --no-cache app
docker compose up -d
```
