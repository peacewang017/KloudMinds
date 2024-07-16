# 基于Node.js官方镜像构建
FROM node:lts-alpine AS build-stage

# 设置工作目录为/app
WORKDIR /app

# 复制package.json和package-lock.json文件到工作目录
COPY package*.json ./

# 安装项目依赖
RUN npm install

# 复制项目文件到工作目录
COPY . .

# 构建Vue应用
RUN npm run build

# 使用nginx作为静态文件服务器
FROM nginx:stable-alpine

# 将构建产物复制到nginx的默认静态文件目录
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 移除默认的nginx配置文件，并复制自定义nginx配置
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/

# 暴露80端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
