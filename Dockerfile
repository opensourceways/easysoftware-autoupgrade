FROM openeuler/openeuler:22.03 as BUILDER

RUN cd / \
    && yum install -y wget \
    && wget https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jdk/x64/linux/OpenJDK17U-jdk_x64_linux_hotspot_17.0.10_7.tar.gz \
    && tar -zxvf OpenJDK17U-jdk_x64_linux_hotspot_17.0.10_7.tar.gz \
    && wget https://repo.huaweicloud.com/apache/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz \
    && tar -zxvf apache-maven-3.8.1-bin.tar.gz

COPY . /software-market-autocommit

ENV JAVA_HOME=/jdk-17.0.10+7
ENV PATH=${JAVA_HOME}/bin:$PATH

ENV MAVEN_HOME=/apache-maven-3.8.1
ENV PATH=${MAVEN_HOME}/bin:$PATH

RUN cd /software-market-autocommit \
    && mvn clean install package -Dmaven.test.skip

FROM openeuler/openeuler:22.03

RUN yum update -y \
    && yum install -y shadow

RUN groupadd -g 1001 autocommitpr \
    && useradd -u 1001 -g autocommitpr -s /bin/bash -m autocommitpr

ENV WORKSPACE=/home/autocommitpr

WORKDIR ${WORKSPACE}

COPY --chown=autocommitpr --from=Builder /software-market-autocommit/target/software-market-autocommit-0.0.1-SNAPSHOT.jar ${WORKSPACE}/target/

RUN echo "umask 027" >> /home/autocommitpr/.bashrc \
    && source /home/autocommitpr/.bashrc \
    && chmod 550 -R /home/autocommitpr \
    && rm -rf /software-market-autocommit/*

RUN dnf install -y wget \
    && wget https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jre/x64/linux/OpenJDK17U-jre_x64_linux_hotspot_17.0.10_7.tar.gz \
    && tar -zxvf OpenJDK17U-jre_x64_linux_hotspot_17.0.10_7.tar.gz \
    && rm -rf OpenJDK17U-jre_x64_linux_hotspot_17.0.10_7.tar.gz \
    && yum remove wget  shadow -y \
    && yum clean all
ENV JAVA_HOME=${WORKSPACE}/jdk-17.0.10+7-jre
ENV PATH=${JAVA_HOME}/bin:$PATH
ENV LANG="C.UTF-8"
ENV NO_ID_USER=anonymous

EXPOSE 8080

USER autocommitpr

CMD java -jar ${WORKSPACE}/target/software-market-autocommit-0.0.1-SNAPSHOT.jar --spring.config.location=${APPLICATION_PATH}