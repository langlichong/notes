
## Refes
http://gitlab.genew.cn/help/ci/yaml/index

# 安装
## Download the binary for your system
sudo curl -L --output /usr/local/bin/gitlab-runner https://gitlab-runner-downloads.s3.amazonaws.com/latest/binaries/gitlab-runner-linux-amd64

## 说明
```
- gitlab-runner安装完后，默认会创建一个gitlab-runner的用户，可以sudo su - gitlab-runner 查看用户下目录结构
- 在gitlab-runner用户的 /builds（本机目录/home/gitlab-runner/builds，gitlab-runner安装目录下的builds）
- builds目录下是不同的已经注册的gitlab runner 实例(准确说是项目目录，即/builds/<project-path>/)，后续的脚本执行都是在builds目录下的对应实例中进行
```

## 关键字
![](assets/markdown-img-paste-2022113014580963.png)
![stages](assets/markdown-img-paste-20221130150358325.png)
![](assets/markdown-img-paste-20221130150530750.png)

## runner 执行器类型
![](assets/markdown-img-paste-20221129165438556.png)
![](assets/markdown-img-paste-2022112916551461.png)
![](assets/markdown-img-paste-20221129174149413.png)
![](assets/markdown-img-paste-20221129174350776.png)

## Cicd 脚本是在哪里执行的
![](assets/markdown-img-paste-20221129175029647.png)
![](assets/markdown-img-paste-20221129175825100.png)

## services
![](assets/markdown-img-paste-20221129180107845.png)

## 配置runner克隆源码的目录
![](assets/markdown-img-paste-20221130102805718.png)

## Java Demo
http://events.jianshu.io/p/8778847c108d
https://gitlab.com/gitlab-examples/maven/simple-maven-example
https://gitlab.com/gitlab-examples/spring-gitlab-cf-deploy-demo/-/blob/master/.gitlab-ci.yml

## docker executor 重复拉取镜像问题
![](assets/markdown-img-paste-20221130114921190.png)

## Give it permission to execute
sudo chmod +x /usr/local/bin/gitlab-runner

## Create a GitLab Runner user
sudo useradd --comment 'GitLab Runner' --create-home gitlab-runner --shell /bin/bash

## Install and run as a service
sudo gitlab-runner install --user=gitlab-runner --working-directory=/home/gitlab-runner
sudo gitlab-runner start

# 注册
```
sudo gitlab-runner register --url http://gitlab.genew.cn/ --registration-token $REGISTRATION_TOKEN
e.g.: sudo gitlab-runner register --url http://gitlab.genew.cn/ --registration-token GR1348941ye_haSVZ5i3MUd5syV_s  --name base-api-runner
```

## vue demo
```
stages:
  - install
  - build
  - deploy

# 定义需要缓存的目录
cache:
  paths:
    - node_modules
    - dist

install-job:
  stage: install
  script:
    - npm install

build-job:
  stage: build
  script:
    - npm run build
  # 可在流水线上下载本阶段作业的制品
  artifacts:
    name: "dist"
    paths:
      - dist

deploy-job:
  stage: deploy
  script:
    - cp -r .\dist\* D:\nginx-1.20.2\html\
```

## java Demo 1
```
stages:
  - build
  - deploy

# 定义需要缓存的目录
cache:
  paths:
    - target

build-job:
  stage: build
  script:
    - mvn -DskipTests=true clean package
  # 可在流水线上下载本阶段作业的制品
  artifacts:
    name: "jar"
    paths:
      - target\java-cicd-test-0.0.1-SNAPSHOT.jar

deploy-job:
  stage: deploy
  script:
    - cp target\java-cicd-test-0.0.1-SNAPSHOT.jar D:\java\
    - cd D:\java\
    - java -jar java-cicd-test-0.0.1-SNAPSHOT.jar
```
## java Demo 2
```
image: maven:latest

variables:
  MAVEN_CLI_OPTS: "-s .m2/settings.xml --batch-mode"
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"

cache:
  paths:
    - .m2/repository/
    - target/

build:
  stage: build
  script:
    - mvn $MAVEN_CLI_OPTS compile

test:
  stage: test
  script:
    - mvn $MAVEN_CLI_OPTS test

deploy:
  stage: deploy
  script:
    - mvn $MAVEN_CLI_OPTS deploy
  only:
    - master
```
