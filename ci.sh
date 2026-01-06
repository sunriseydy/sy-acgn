#!/bin/sh

./amper package -m server

REG=registry.cn-shanghai.aliyuncs.com/sunriseydy/sy-acgn
TAG=dev-$(date "+%Y%m%d%H%M%S")
docker build -t ${REG}:${TAG} .