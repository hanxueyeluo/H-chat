package com.easychat.websocket.netty;

import com.easychat.entity.config.AppConfig;
import com.easychat.utils.StringTools;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class NettyWebSocketStarter implements Runnable{
    private static final Logger logger= LoggerFactory.getLogger(NettyWebSocketStarter.class);
    private static EventLoopGroup bossGroup=new NioEventLoopGroup(1);
    private static EventLoopGroup workGroup=new NioEventLoopGroup();

    @Resource
    private HandlerWebsocket handlerWebsocket;

    @Resource
    private AppConfig appConfig;

    @PreDestroy
    public void close(){
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    @Override
    public void run() {
        try{
            ServerBootstrap serverBootstrap=new ServerBootstrap();
            serverBootstrap.group(bossGroup,workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class).
                    handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline=channel.pipeline();
                            //设置几个重要的处理器
                            // 对http协议支持，使用http的编码器解码器
                            pipeline.addLast(new HttpServerCodec());
                            //聚合解码 httpRequest/httpContent/lastHttpContent到fullHttpRequest
                            //保证接收的http请求的完整性
                            pipeline.addLast(new HttpObjectAggregator(64*1024));
                            //心跳
                            //readerIdleTime 读超时事件 即测试端一定事件内未接收到被测试段消息
                            //writerIdleTime 为写超时时间 及测试端一定时间内向被测试端发送消息
                            //allIdleTime 所有类型的超时时间
                            pipeline.addLast(new IdleStateHandler(60,0,0, TimeUnit.SECONDS));
                            pipeline.addLast(new HandlerHeartBeat());
                            //将http协议升级为ws协议，对websocket支持
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws",null,true,64*1024,true,true,10000L));
                            pipeline.addLast(handlerWebsocket);
                        }


                    });
            Integer wsPort = appConfig.getWsPort();
            String wsPortStr=System.getProperty("ws.port");
            if (!StringTools.isEmpty(wsPortStr)) {
                wsPort=Integer.parseInt(wsPortStr);
            }
            ChannelFuture channelFuture=serverBootstrap.bind(wsPort).sync();
            logger.info("netty启动成功,端口：{}",appConfig.getWsPort());
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            logger.error("启动netty失败",e);
        }finally {
            bossGroup.shutdownNow();
            workGroup.shutdownNow();
        }
    }
}
