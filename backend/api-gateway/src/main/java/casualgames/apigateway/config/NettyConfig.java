package casualgames.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class NettyConfig implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    @Value("${server.max-http-header-size:16384}")
    private int maxHttpHeaderSize;

    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        factory.addServerCustomizers(
                httpServer -> httpServer.httpRequestDecoder(
                        spec -> spec.maxHeaderSize(maxHttpHeaderSize)
                )
        );
    }
}
