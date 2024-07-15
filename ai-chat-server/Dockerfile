# 使用官方的 Python 基础镜像
FROM python:3.9-slim

# 设置工作目录
WORKDIR /app

# 复制当前目录到工作目录
COPY . .

# 安装依赖
RUN pip install --no-cache-dir flask openai requests

# 暴露端口
EXPOSE 5000

# 运行应用
CMD ["python", "ai-chat-server.py"]