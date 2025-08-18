#!/usr/bin/env bash
set -euo pipefail

WORK_DIR="${1:-$HOME}"
PROMPT="${2:-/finish-subtasks-half-amount-of 커스텀 명령어를 실행해}"

# 작은따옴표/큰따옴표 안전하게 이스케이프
ESC_PROMPT=$(printf "%s" "$PROMPT" | sed "s/\"/\\\\\"/g")
ESC_WORKDIR=$(printf "%s" "$WORK_DIR" | sed "s/\"/\\\\\"/g")

/usr/bin/osascript <<EOF
-- iTerm가 없으면 실행
if application "iTerm" is not running then
  tell application "iTerm" to activate
end if

tell application "iTerm"
  activate
  -- 현재 윈도우 없으면 새로 생성
  if (count of windows) = 0 then
    create window with default profile
  end if

  tell current window
    create tab with default profile
    tell current session
      write text "cd \\"$ESC_WORKDIR\\""
      -- 상황에 맞게 프롬프트 구성 (예: TASK_ID/단계 표시)
      write text "claude \\"$ESC_PROMPT\\""
    end tell
  end tell
end tell
EOF
