# 基础镜像
FROM python:3.9-slim

# 设置工作目录
WORKDIR /app

# 复制依赖文件并安装依赖
COPY requirements.txt requirements.txt
RUN pip install -r requirements.txt

# 复制当前目录下的所有文件到工作目录
COPY . .

# 暴露端口，与 Flask 应用中的端口一致
EXPOSE 5000

# 启动 Flask 应用
CMD ["python", "retrieve-server.py"]