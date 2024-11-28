FROM alpine:3.20
RUN apk add --no-cache curl
COPY . /opt/sy-acgn
ENV AMPER_BOOTSTRAP_CACHE_DIR=/opt/sy-acgn/build
RUN cd /opt/sy-acgn && ./amper --shared-caches-root=/opt/sy-acgn/build task :server:compileJvm
ENTRYPOINT [ "/opt/sy-acgn/amper", "--root=/opt/sy-acgn", "--shared-caches-root=/opt/sy-acgn/build", "task", ":server:runJvm" ]