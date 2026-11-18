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

  @Bean(destroyMethod = "close")
  public MilvusServiceClient milvusClient() {
    ConnectParam param =
        ConnectParam.newBuilder()
            .withHost(host)
            .withPort(port)
            .withConnectTimeout(3000, TimeUnit.MILLISECONDS)
            .build();
    return new MilvusServiceClient(param);
  }
}
