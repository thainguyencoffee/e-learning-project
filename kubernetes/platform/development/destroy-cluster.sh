#!/bin/sh

echo "\n🏴️ Destroying Kubernetes cluster...\n"

minikube stop --profile elproject

minikube delete --profile elproject

echo "\n🏴️ Cluster destroyed\n"