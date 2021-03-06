package ohnosequences.nisperon

import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.auth.{PropertiesCredentials, InstanceProfileCredentialsProvider, BasicAWSCredentials}
import ohnosequences.awstools.ec2.EC2
import ohnosequences.awstools.autoscaling.AutoScaling
import ohnosequences.awstools.sqs.SQS
import ohnosequences.awstools.sns.SNS
import ohnosequences.awstools.s3.S3
import ohnosequences.awstools.dynamodb.DynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import java.io.File
import com.amazonaws.regions.Regions

class AWS(credentialsFile: File = new File(System.getProperty("user.home"), "metapasta.credentials")) {
  val AWS_ACCESS_KEY = "AWS_ACCESS_KEY"
  val AWS_SECRET_KEY = "AWS_SECRET_KEY"

  val env: Option[(String, String)] = try {
    Some(System.getenv(AWS_ACCESS_KEY) -> System.getenv(AWS_SECRET_KEY))
  } catch {
    case t: Throwable => None
  }

  val credentialsProvider = env match {
    case Some((accessKey: String, secretKey: String)) => {
      new StaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))
    }
    case _ => {
      if (credentialsFile.exists()) {
        new StaticCredentialsProvider(new PropertiesCredentials(credentialsFile))
      } else {
        // println("why I'm here: " + env)
        new InstanceProfileCredentialsProvider()
      }
    }
  }

  val ec2 = EC2.create(credentialsProvider)
  val as = AutoScaling.create(credentialsProvider, ec2)
  val sqs = SQS.create(credentialsProvider)
  val sns = SNS.create(credentialsProvider)
  val s3 = S3.create(credentialsProvider)

 // val ddb = DynamoDB.create(credentialsProvider)
  val ddb = new AmazonDynamoDBClient(credentialsProvider)
  ddb.setRegion(com.amazonaws.regions.Region.getRegion(Regions.EU_WEST_1))
}


