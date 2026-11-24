package iuh.fit.www_phone_ver1.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.milvus.enabled", havingValue = "true")
public class MilvusConfig {

  @Value("${app.milvus.host:localhost}")
  private String host;

  @Value("${app.milvus.port:19530}")
  private int port;

  @Value("${app.milvus.connect-timeout-ms:3000}")
  private long connectTimeoutMs;

  @Value("${app.milvus.rpc-deadline-ms:5000}")
  private long rpcDeadlineMs;

  @Bean(destroyMethod = "close")
  public MilvusServiceClient milvusClient() {
    ConnectParam param =
        ConnectParam.newBuilder()
            .withHost(host)
            .withPort(port)
            .withConnectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
            .withRpcDeadline(rpcDeadlineMs, TimeUnit.MILLISECONDS)
            .withKeepAliveTime(30_000, TimeUnit.MILLISECONDS)
            .withKeepAliveTimeout(5_000, TimeUnit.MILLISECONDS)
            .keepAliveWithoutCalls(false)
            .build();
    return new MilvusServiceClient(param);
  }
}
