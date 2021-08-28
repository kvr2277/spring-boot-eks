# eks-spring-service
Simple Spring Boot rest services on EKS

# Steps

1. Check basic java build

```
./mvnw install
java -jar target/*.jar
curl localhost:8080/name
```

2. Create ECR repo
aws ecr create-repository \
    --repository-name sample-repo \
    --image-tag-mutability IMMUTABLE \
    --image-scanning-configuration scanOnPush=true

3. Push image to ECR

Get token to your ECR
```
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 777258879183.dkr.ecr.us-east-1.amazonaws.com
```

Build image in your local
```
./mvnw spring-boot:build-image  
```

Tag it
```
docker tag eksdemo:0.0.1-SNAPSHOT 777258879183.dkr.ecr.us-east-1.amazonaws.com/sample-repo:latest
```

Push image
```
docker push 777258879183.dkr.ecr.us-east-1.amazonaws.com/sample-repo:latest
```

4. Now working with EKS

a)
Set up eksctl (source: https://docs.aws.amazon.com/eks/latest/userguide/eksctl.html)

```
brew tap weaveworks/tap
brew install weaveworks/tap/eksctl
eksctl version
```

b) Create cluster - takes ~30 minutes

```
eksctl create cluster -f cluster.yaml
```

Once created, test by executing below commands

```
kubectl get svc
kubectl get nodes -o wide
kubectl get pods --all-namespaces -o wide
```

c) Once basic cluster creation testing is done, delete the resources
```
eksctl delete cluster --region=us-east-1 --name=my-cluster
```


5. Deploying image to EKS (https://towardsdatascience.com/kubernetes-application-deployment-with-aws-eks-and-ecr-4600e11b2d3c)

kubectl apply -f eks-deployment.yaml
kubectl get deployments
kubectl apply -f eks-service.yaml
kubectl get pods -o wide
kubectl get nodes -o wide

Get the external IPs
18.204.56.240 
18.206.95.241

Edit worker node security group to allow inbound traffic for port 31479 from source anywhere - 0.0.0.0/0
eksctl-my-cluster-nodegroup-ng-1-workers-SG-JSDGZE4AAAA

http://18.206.95.241:31479/name
http://18.204.56.240:31479/name


delete cluster again

```
eksctl delete cluster --region=us-east-1 --name=my-cluster
```
