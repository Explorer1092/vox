package com.voxlearning.utopia.service.push.impl.invoker;

import com.voxlearning.alps.annotation.common.Install;
import com.voxlearning.alps.api.bootstrap.PostFinalizeModule;
import com.voxlearning.alps.core.util.ClassUtils;
import com.voxlearning.alps.core.util.IOUtils;
import com.voxlearning.alps.http.client.execute.AbstractHttpRequestExecutor;
import com.voxlearning.alps.http.client.factory.HttpClientMethodInterceptor;
import com.voxlearning.alps.logger.LoggerFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.RequestConnControl;
import org.slf4j.Logger;
import org.springframework.aop.framework.ProxyFactory;

import java.io.Closeable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class HttpPoolingRequestExecutor {
  private static final Logger logger = LoggerFactory.getLogger(HttpPoolingRequestExecutor.class);

  private static final List<InternalHttpRequestExecutor> list = new ArrayList<>();
  private static final AtomicLong mod = new AtomicLong(0);

  static {
    init();
  }

  private static void destroy() {
    list.forEach(e -> {
      HttpClient client = e.client;
      if (client instanceof Closeable) {
        IOUtils.closeQuietly((Closeable) client);
      }
    });
    logger.info("All HTTP client(s) closed");
  }

  public static InternalHttpRequestExecutor get() {
    int m = (int) (mod.getAndIncrement() % 4);
    return list.get(m);
  }

  private static void init() {
    for (int i = 0; i < 4; i++) {
      PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
      connectionManager.setMaxTotal(100);
      connectionManager.setDefaultMaxPerRoute(10);

      HttpClientBuilder builder = HttpClients.custom()
          .setConnectionManager(connectionManager)
          .addInterceptorFirst(new RequestConnControl());

      HttpClient client = builder.build();

      HttpClient real = builder.build();

      HttpClientMethodInterceptor interceptor = new HttpClientMethodInterceptor(real);
      ProxyFactory proxyFactory = new ProxyFactory();
      proxyFactory.setInterfaces(HttpClient.class);
      proxyFactory.addAdvice(interceptor);
      HttpClient proxy = (HttpClient) proxyFactory.getProxy(ClassUtils.getDefaultClassLoader());

      list.add(new InternalHttpRequestExecutor(client, proxy));
    }
  }

  @RequiredArgsConstructor
  public static class InternalHttpRequestExecutor extends AbstractHttpRequestExecutor {

    @Getter @NonNull private final HttpClient client;
    @Getter @NonNull private final HttpClient proxy;

    @Override
    protected HttpClient getHttpClient(URI uri) {
      return proxy;
    }
  }

  @Install
  final public static class Finalization implements PostFinalizeModule {

    @Override
    public void postFinalizeModule() {
      destroy();
    }
  }
}
