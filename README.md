# aws-sagemaker-example
Example of how to use the SpringBoot and PMML model as your own model for AWS SageMaker.

The parts handling the PMML stuff (aka the model, loading the model and predicting) were inspired from

https://github.com/hkropp/jpmml-iris-example/blob/master/src/main/resources/sample/Iris.csv
https://henning.kropponline.de/2015/09/06/jpmml-example-random-forest/

# How to run
1. Clone the project

git clone https://github.com/spicoflorin/aws-sagemaker-example.git

2. In the directory aws-sagemaker-example, run:
  
  mvn clean package
  
3. In the directory aws-sagemaker-example, build the docker image in the format of ECR Sagemaker  

docker build . --tag your-accountid.dkr.ecr.your-region.amazonaws.com/your-ecr-repository-name

4. Test the image

4.1. Start the container 

docker  run -p 8080:8080 your-accountid.dkr.ecr.your-region.amazonaws.com/your-ecr-repository-name

4.2 Test with a value

curl -X POST http://localhost:8080/invocations -d '6.7,2.5,5.8,1.8,Iris-virginica' -H 'Content-Type: text/csv'



