# eks-spring-service
Simple Spring Boot rest services on EKS

# Steps

0. Optional - See if you have JAVA_HOME setup

# Refer article at https://medium.com/@vin0d/quick-way-to-set-java-home-in-mac-70cd4e82a9c5

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


# Continue reading the steps at https://medium.com/p/8c173737f1a