#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
import pty
import select
import termios
import tty
import errno
import re
import json
import urllib.request
import urllib.error

# Banner của chương trình
BANNER = """\033[94m
┌────────────────────────────────────────────────────────┐
│             \033[92mCLI Auto-Confirm Assistant (AI)            \033[94m│
│      \033[90mTự động trả lời YES/1 cho các câu hỏi CLI\033[94m         │
└────────────────────────────────────────────────────────┘\033[0m"""

USAGE = """
\033[1mSử dụng:\033[0m
  python3 auto_confirm.py [lệnh_cli] [các_tham_số...]

\033[1mVí dụ:\033[0m
  python3 auto_confirm.py npm init
  python3 auto_confirm.py rm -i *
  python3 auto_confirm.py git clean -i

\033[1mCấu hình AI (Tùy chọn):\033[0m
  Xuất biến môi trường GEMINI_API_KEY để sử dụng Trí Tuệ Nhân Tạo phân tích sâu hơn:
  export GEMINI_API_KEY="your_api_key_here"
"""

def get_gemini_decision(prompt_text, api_key):
    """
    Sử dụng Gemini API để xác định xem prompt hiện tại có phải câu hỏi xác nhận đồng ý không.
    """
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={api_key}"
    
    system_instruction = (
        "You are an expert CLI automation system. Analyze the given CLI prompt. "
        "Determine if it is a confirmation/acceptance/agreement question (e.g. Yes/No, y/n, [y/N], select 1 to agree, do you want to continue, are you sure, overwrite file?). "
        "If it is a confirmation prompt and the positive answer (like yes, y, 1, co) represents continuing/agreeing, return ONLY the exact keystrokes/response (e.g. 'y', 'yes', '1', 'có'). "
        "If it is a general information input (like entering a name, version, text value, directory path, password), or if it is ambiguous, reply with 'MANUAL'. "
        "Respond ONLY with the response string. Do not use quotes, formatting, or explanations."
    )
    
    data = {
        "contents": [{
            "parts": [{
                "text": f"{system_instruction}\n\nCLI Prompt:\n{prompt_text}"
            }]
        }],
        "generationConfig": {
            "temperature": 0.0,
            "maxOutputTokens": 10
        }
    }
    
    try:
        req = urllib.request.Request(
            url,
            data=json.dumps(data).encode("utf-8"),
            headers={"Content-Type": "application/json"}
        )
        # Timeout 2 giây để tránh làm nghẽn tiến trình CLI
        with urllib.request.urlopen(req, timeout=2.0) as response:
            res_data = json.loads(response.read().decode("utf-8"))
            decision = res_data["candidates"][0]["content"]["parts"][0]["text"].strip()
            # Clean up quotes if LLM returned them
            decision = decision.replace('"', '').replace("'", "")
            return decision
    except Exception:
        # Lỗi mạng hoặc lỗi API -> Fallback về heuristic cục bộ
        return None

def get_local_decision(prompt_text):
    """
    Phân tích cục bộ dựa trên từ khóa và biểu thức chính quy (Regex).
    Hỗ trợ cả tiếng Anh và tiếng Việt.
    """
    text = prompt_text.strip().lower()
    lines = [line.strip() for line in prompt_text.splitlines() if line.strip()]
    last_line = lines[-1].lower() if lines else text
    
    # 1. Các định dạng câu hỏi Yes/No kinh điển (check ở dòng cuối cùng)
    if re.search(r'\[y/n\]', last_line) or re.search(r'\(y/n\)', last_line) or re.search(r'\[y/n\]', last_line):
        return 'y'
    if re.search(r'\[yes/no\]', last_line) or re.search(r'\(yes/no\)', last_line):
        return 'yes'
    if re.search(r'\[y/n/c\]', last_line) or re.search(r'\[y/n/a\]', last_line):
        return 'y'
        
    # 2. Câu hỏi Tiếng Việt: [có/không], [c/k], (có/không), (c/k) (check ở dòng cuối cùng)
    if re.search(r'\[có/không\]', last_line) or re.search(r'\(có/không\)', last_line):
        return 'có'
    if re.search(r'\[c/k\]', last_line) or re.search(r'\(c/k\)', last_line):
        return 'c'
        
    # 3. Lựa chọn dạng danh sách số (check trong toàn bộ text)
    if re.search(r'1[\.\)]\s*(yes|y|đồng ý|có|confirm|agree|ok|tiếp tục|continue)', text):
        # Kiểm tra nếu dòng cuối cùng yêu cầu nhập số hoặc lựa chọn
        if any(kw in last_line for kw in ['lựa chọn', 'choice', 'select', 'option', '[', 'number', '1-']):
            return '1'
        if last_line.endswith(':') or last_line.endswith('?'):
            return '1'

    # 4. Câu hỏi kết thúc bằng dấu '?' chứa từ khóa xác nhận hành động nguy hiểm/quan trọng
    if last_line.endswith('?'):
        confirm_keywords = [
            'sure', 'continue', 'proceed', 'overwrite', 'agree', 'accept', 'confirm',
            'tiếp tục', 'đồng ý', 'chắc chắn', 'ghi đè', 'xác nhận', 'xóa'
        ]
        if any(keyword in last_line for keyword in confirm_keywords):
            return 'y'
            
    return 'MANUAL'

def run_command(command_args):
    # Khởi tạo API Key
    api_key = os.environ.get("GEMINI_API_KEY")
    
    # Kiểm tra xem stdin có phải là TTY thực sự không
    is_tty = sys.stdin.isatty()
    old_tty = None
    
    if is_tty:
        # Lưu cài đặt terminal gốc
        old_tty = termios.tcgetattr(sys.stdin)
    
    # Tạo tiến trình con trong PTY
    pid, master_fd = pty.fork()
    
    if pid == 0:
        # Tiết trình con: chạy lệnh đích
        try:
            os.execvp(command_args[0], command_args)
        except Exception as e:
            sys.stderr.write(f"\r\n\033[91mLỗi khởi chạy lệnh '{command_args[0]}': {e}\033[0m\r\n")
            sys.exit(1)
            
    # Tiến trình cha: giám sát tương tác
    if is_tty:
        # Chuyển stdin sang chế độ raw để bắt trực tiếp phím nhấn
        tty.setraw(sys.stdin.fileno())
    
    buffer = b""
    last_replied_prompt = ""
    
    try:
        while True:
            # select với timeout 0.1s để phát hiện khi tiến trình con ngừng ghi ra stdout (chờ nhập liệu)
            # Chỉ lắng nghe stdin nếu stdin là TTY hoặc đang có dữ liệu đầu vào
            inputs = [master_fd]
            if is_tty or select.select([sys.stdin.fileno()], [], [], 0)[0]:
                inputs.append(sys.stdin.fileno())
                
            r, w, x = select.select(inputs, [], [], 0.1)
            
            if not r:
                # Không có dữ liệu mới -> tiến trình con có thể đang chờ input
                if buffer:
                    try:
                        prompt_text = buffer.decode("utf-8", errors="ignore")
                    except Exception:
                        prompt_text = ""
                    
                    if prompt_text and prompt_text != last_replied_prompt:
                        lines = [line.strip() for line in prompt_text.splitlines() if line.strip()]
                        last_line = lines[-1] if lines else prompt_text
                        
                        # Chỉ kiểm tra nếu dòng cuối cùng trông giống một câu hỏi/prompt xác nhận
                        is_likely_prompt = any(
                            last_line.endswith(char) for char in ['?', ':', '>', ']', '}', '❯']
                        ) or any(
                            pat in last_line.lower() for pat in ['[y/n]', '(y/n)', '[yes/no]', '[có/không]', '[c/k]']
                        )
                        
                        if is_likely_prompt:
                            decision = None
                            
                            # Thử dùng Gemini AI nếu có key
                            if api_key:
                                decision = get_gemini_decision(prompt_text, api_key)
                                
                            # Nếu không có AI hoặc AI yêu cầu nhập thủ công -> dùng quy tắc cục bộ
                            if not decision or decision == "MANUAL":
                                decision = get_local_decision(prompt_text)
                                
                            if decision and decision != "MANUAL":
                                last_replied_prompt = prompt_text
                                
                                # Ghi log màu xanh thông báo đã tự động điền
                                os.write(sys.stdout.fileno(), f"\r\n\033[92m[Auto-Yes AI] Tự động chọn: {decision}\033[0m\r\n".encode())
                                
                                # Gửi phản hồi đến tiến trình con
                                os.write(master_fd, (decision + "\n").encode())
                                buffer = b""
                                continue
                                
                continue
                
            # Đọc output từ tiến trình con
            if master_fd in r:
                try:
                    data = os.read(master_fd, 1024)
                except OSError as e:
                    if e.errno == errno.EIO:
                        break
                    raise
                if not data:
                    break
                    
                sys.stdout.buffer.write(data)
                sys.stdout.buffer.flush()
                
                buffer += data
                # Chỉ lưu tối đa 2KB cuối cùng để tiết kiệm bộ nhớ và phân tích hiệu quả
                if len(buffer) > 2048:
                    buffer = buffer[-2048:]
                    
            # Nhận input thủ công từ người dùng và đẩy vào tiến trình con
            if sys.stdin.fileno() in r:
                try:
                    data = os.read(sys.stdin.fileno(), 1024)
                except Exception:
                    break
                if not data:
                    break
                os.write(master_fd, data)
                # Reset lại lịch sử trả lời để không bỏ lỡ câu hỏi tiếp theo
                last_replied_prompt = ""
                
    finally:
        if is_tty and old_tty:
            # Khôi phục chế độ terminal ban đầu
            termios.tcsetattr(sys.stdin.fileno(), termios.TCSADRAIN, old_tty)
        
        # Chờ tiến trình con kết thúc và lấy mã thoát
        _, status = os.waitpid(pid, 0)
        # Trả về exit code thực tế của tiến trình con
        if os.WIFEXITED(status):
            sys.exit(os.WEXITSTATUS(status))
        elif os.WIFSIGNALED(status):
            sys.exit(128 + os.WTERMSIG(status))
        else:
            sys.exit(0)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(BANNER)
        print(USAGE)
        sys.exit(1)
        
    # Chạy lệnh được truyền vào tham số
    run_command(sys.argv[1:])
