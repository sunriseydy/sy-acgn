FROM registry.cn-shanghai.aliyuncs.com/sunriseydy/amazoncorretto:21.0.4
COPY . /opt/sy-acgn
ENV AMPER_JAVA_HOME=/etc/alternatives/java_sdk
RUN /opt/sy-acgn/amper task :server:compileJvm
ENTRYPOINT [ "/opt/sy-acgn/amper", "task", ":server:runJvm" ]