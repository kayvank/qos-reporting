package repository

import com.amazonaws.auth.{AWSCredentials, AWSCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.kinesis.{AmazonKinesisClient, AmazonKinesisClientBuilder}
import utils.Global.cfgVevo

import scala.util.Try

sealed trait  BasicAWSCredentialsProvider {

  final class BasicAWSCredentialsProvider(basic: BasicAWSCredentials) extends
    AWSCredentialsProvider {
    @Override def getCredentials: AWSCredentials = basic

    @Override def refresh = {}
  }

}

object KplConfig extends BasicAWSCredentialsProvider{

  case class Config(
    maxBufferedTime: Long = Try(cfgVevo.getLong(
      "kinesis.stream.qos.buffered.time.millisecond")
    ).toOption.getOrElse(6300),
    credentialsProvider: AWSCredentialsProvider = (new BasicAWSCredentialsProvider(
      new BasicAWSCredentials(
        cfgVevo.getString("aws.access-key"),
        cfgVevo.getString("aws.secret-key")))),
    maxConnections: Int = Try(cfgVevo.getInt("kinesis.stream.qos.connections.max")
    ).toOption.getOrElse(1))

}

object Kcl extends BasicAWSCredentialsProvider{

  val client = AmazonKinesisClientBuilder.standard()
    .withCredentials(new BasicAWSCredentialsProvider(
      new BasicAWSCredentials(
        cfgVevo.getString("aws.access-key"),
        cfgVevo.getString("aws.secret-key"))))
    .withRegion(cfgVevo.getString("kinesis.stream.qos.region"))
    .build()
}