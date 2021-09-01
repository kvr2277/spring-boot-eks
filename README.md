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
    --repository-name demo-repo \
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
docker tag eksdemo:0.0.1-SNAPSHOT 777258879183.dkr.ecr.us-east-1.amazonaws.com/demo-repo:latest
```

Push image
```
docker push 777258879183.dkr.ecr.us-east-1.amazonaws.com/demo-repo:latest
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
eksctl delete cluster --region=us-east-1 --name=demo-cluster
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
eksctl-demo-cluster-nodegroup-ng-1-workers-SG-JSDGZE4AAAA

http://18.206.95.241:31479/name
http://3.93.200.100:31479/name


delete cluster again

```
eksctl delete cluster --region=us-east-1 --name=demo-cluster
```


6. Exposing behind application load balancer (https://medium.com/cloudzone/aws-alb-ingress-controller-guide-ec16233f5903)

```
eksctl utils associate-iam-oidc-provider \
    --region us-east-1 \
    --cluster demo-cluster \
    --approve
```

tag 3 subnets (pick 2 as internet facing, 1 as internal facing). You internet facing subnet will have rout table for 0.0.0.0/0 pointing to igw-XXXX (internet gateway). For this exercise, you can use any one of the internet facing subnet as ur inetrnal facing subnet - or u can remove a subnet's route table entry pointing to Internet Gateway

```
kubernetes.io/cluster/demo-cluster Value shared 
kubernetes.io/role/elb  Value 1 <--- internet facing 2 subnets
kubernetes.io/role/internal-elb Value 1  <--- interal facing 1 is okay
```

```
curl -o iam-policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-alb-ingress-controller/v1.1.8/docs/examples/iam-policy.json

aws iam create-policy \
    --policy-name ALBIngressControllerIAMPolicy \
    --policy-document file://iam-policy.json

```

```
curl -o rbac-role-alb-ingress-controller.yaml https://raw.githubusercontent.com/kubernetes-sigs/aws-alb-ingress-controller/v1.1.8/docs/examples/rbac-role.yaml

kubectl apply -f rbac-role-alb-ingress-controller.yaml
```

```
aws iam create-role --role-name eks-alb-ingress-controller --assume-role-policy-document file://eks-ingress-trust-iam-policy.json

aws iam attach-role-policy --role-name eks-alb-ingress-controller --policy-arn=arn:aws:iam::777258879183:policy/ALBIngressControllerIAMPolicy

kubectl annotate serviceaccount -n kube-system alb-ingress-controller \
eks.amazonaws.com/role-arn=arn:aws:iam::777258879183:role/eks-alb-ingress-controller

kubectl apply -f  eks-alb-ingress-controller.yaml

kubectl apply -f eks-ingress.yaml


kubectl get ingress/demo-ingress -n default

we can see app live at 
http://81495355-default-demoingre-d07d-220047569.us-east-1.elb.amazonaws.com/
http://81495355-default-demoingre-d07d-220047569.us-east-1.elb.amazonaws.com/name

for any issues with ingress, check logs at

kubectl logs -n kube-system deployment.apps/alb-ingress-controller
```

delete cluster again

```
eksctl delete cluster --region=us-east-1 --name=demo-cluster
```