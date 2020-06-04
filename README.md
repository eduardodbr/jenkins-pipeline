# jenkins-pipeline

This repo holds an app based on [benthos](http://benthos.dev) to produce mock data to a kafka broker. The purpose of this project is to simulate a canary/production CI/CD pipeline using jenkins deployed on kubernetes (repo [here](https://github.com/eduardodbr/kubernetes-env)). 

Jenkins monitors the branches `prodution` and `canary`. When a change is committed to one of the branches, Jenkins builds and deploys the app to kubernetes on the respetive namespace using the configuration defined [here](producer/kubernetes/).