## Build metrics demo application 
#### it can be used by prometheus to scrape data

#### main use of this project is as a runtime ms that can generate p8s metrics and this info will be used in another project "project-name" which will use Langchain/Langgraph and LLM, mcp server and p8s client to interact with p8s. 

#### Port: 
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
<path>\P8sMetricsDemo> docker build . -t wonkday/metrics-demo:v1.0
```

### Verify Image
```
<path>\P8sMetricsDemo> docker images
REPOSITORY                    TAG       IMAGE ID       CREATED        SIZE
wonkday/metrics-demo          v1.0      6a5c66f86927   27 seconds ago   81.9MB
```

Optional:
```
<path>\P8sMetricsDemo> docker push wonkday/metrics-demo:v1.0
```

### Load image to k8s
```
minikube image load wonkday/metrics-demo:v1.0
```

### Verify loaded image
```
<path>\P8sMetricsDemo> minikube image ls | Select-String -Pattern "metrics-demo"

docker.io/wonkday/metrics-demo:v1.0
```

### Accessing demo app in local cluster via ingress

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
