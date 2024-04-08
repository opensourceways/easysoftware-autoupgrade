
# software-market-autocommit

#### 介绍

software-market-autocommit是对软件市场精品应用和rpm包自动提交升级pr服务; 主要流程是先比对dockerhub应用软件version和该软件上游的最新版本,如果该软件符合升级条件,就升级上openeuler对应osversion下的该软件,以调取openapi的方式去commit、创建issue、创建pr等
#### 软件架构

* SpringBoot

* gitee openapi

#### 安装教程

##### 基本安装

1. 克隆工程
   > git clone https:******.git

2. 打包方式
   > mvn clean install package -Dmaven.test.skip

3. 启动应用
   > java -jar target/software-market-autocommit-0.0.1-SNAPSHOT.jar


##### 容器安装运行~~~~

1. 克隆工程
   > git clone https:******.git


2. 打包方式
    * 用Docker打包（到software-market-autocommit目录中， 执行Dockerfile文件： docker build -t software-market-autocommit . ）
    
3. 启动应用
    * docker run -d -v /data/software-market-autocommit/application.yaml:/home/software-market-autocommit/application.yaml  -e APPLICATION_PATH=/home/software-market-autocommit/application.yaml -p 8080:8080 --name my-container my-image 
#### 使用说明

