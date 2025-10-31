## Build metrics demo application 
#### it can be used by prometheus to scrape data
#### main use of this project is as a runtime ms that can generate p8s metrics and this info will be used in another project "project-name" which will use Langchain/Langgraph and LLM, mcp server and p8s client to interact with p8s. 

Port Config:

PORT_NUM = 9400

### Build fat jar
```
<path>\P8sMetricsDemo> ./gradlew clean
<path>\P8sMetricsDemo> ./gradlew build shadowJar
```

### Test application
```
java -jar .\build\libs\P8sMetricsDemo-1.0-SNAPSHOT-all.jar
```

### Check java component dependencies (for base jre image)
```
<path>\P8sMetricsDemo> deps --list-deps --ignore-missing-deps .\build\libs\P8sMetricsDemo-1.0-SNAPSHOT-all.jar
   java.base
   java.logging
   java.management
   jdk.httpserver
   jdk.management
   jdk.unsupported
```

### Build image
```
<path>\P8sMetricsDemo> docker build . -t wonkday/metrics-demo:v2.0
```

### Verify Image
```
<path>\P8sMetricsDemo> docker images
REPOSITORY                    TAG       IMAGE ID       CREATED         SIZE
wonkday/metrics-demo          v2.0      d90f3423826f   2 minutes ago   81.9MB
```

Optional:
```
<path>\P8sMetricsDemo> docker push wonkday/metrics-demo:v2.0
```

### Load image to k8s (in WSL)
```
$ minikube image load wonkday/metrics-demo:v2.0
```

### Verify loaded image
```
$ minikube image ls | grep metrics-demo
docker.io/wonkday/metrics-demo:v2.0
docker.io/wonkday/metrics-demo:v1.0
```
### Deploy image to k8s (minikube in WSL-Ubuntu)
```shell
$ kubectl apply -f metrics-demo.yaml
configmap/metrics-demo-config created
deployment.apps/metrics-demo configured
service/metrics-demo created
```
### Access WSL k8s service for metrics demo directly from windows
```shell
kubectl -n default port-forward svc/metrics-demo-service 9400:80
```
After port forward, access metrics data via - http://localhost:9400/metrics


#### Accessing demo app in local cluster via ingress

#### Install 'ingress' add-on
```shell
  minikube addons enable ingress
  
🌟  The 'ingress' addon is enabled  
```

#### Setup dummy ip in HOSTS file
```shell
   minikube ip
192.168.49.2   
```
```declarative
On Windows, edit C:\Windows\System32\drivers\etc\hosts 
On macOS/Linux, edit /etc/hosts
and add below text (modify ip as per above result)
```
```text
# Map a fake domain to the Minikube IP
192.168.49.2  metrics.local.com
```
