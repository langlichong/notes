- 安装msys2
- terminal 新开配置中命令部分填写如下内容:

  <img width="984" height="131" alt="windows_terminal_fish" src="https://github.com/user-attachments/assets/eaa83ffc-04f4-48de-8501-af4aef8f404e" />


- zed 配置fish terminal
  ```json
  "terminal": {
    "shell": {
      "with_arguments": {
        "program": "C:\\msys64\\usr\\bin\\fish.exe",
        "args": [], // 移除 --login，让其作为普通交互式 shell 启动
        "env": {
          "MSYSTEM": "UCRT64",
          "CHERE_INVOKING": "1",
          "MSYS2_PATH_TYPE": "inherit",
          "TERM": "xterm-256color",
        },
      },
    },
  }
  ```
