# 使用官方 Python 3.8 镜像作为基础镜像
FROM python:3.8-slim

# 设置工作目录
WORKDIR /app

# 复制当前目录所有内容到工作目录
COPY . /app

# 安装依赖
RUN pip install --no-cache-dir flask elasticsearch

# 暴露应用端口
EXPOSE 5000

# 运行 Flask 应用
CMD ["python", "elastic-search-server.py"]