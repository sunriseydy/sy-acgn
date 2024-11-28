#!/bin/sh

set -e
AMPER_BOOTSTRAP_CACHE_DIR=${PWD}/build
./amper --shared-caches-root=./build task :server:compileJvm

REG=registry.cn-shanghai.aliyuncs.com/sunriseydy/sy-acgn
TAG=dev-$(date "+%Y%m%d%H%M%S")
docker build -t ${REG}:${TAG} .