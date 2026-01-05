package com.gwl.infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.gwl.infrastructure.NettyHandler.ChatHandler;
import com.gwl.infrastructure.NettyHandler.CommandHandler;
import com.gwl.infrastructure.NettyHandler.DispatcherHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

@Component
public class NettyServer implements CommandLineRunner {
    // 注入handler
    @Autowired
    private DispatcherHandler dispatcherHandler;
    @Autowired
    private ChatHandler chatHandler;
    @Autowired
    private CommandHandler commandHandler;

    @Override
    public void run(String... args) throws Exception {
        start(8081);
    }

    public void start(int port) throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpServerCodec());
                        p.addLast(new HttpObjectAggregator(65536));
                        p.addLast(new ChunkedWriteHandler());
                        p.addLast(new WebSocketServerProtocolHandler("/ws"));
                        // 从spring注入
                        p.addLast(dispatcherHandler);
                        // p.addLast(commandHandler);
                        p.addLast(chatHandler);
                    }
                });

        b.bind(port).sync();
        System.out.println("Netty WebSocket Server started at ws://localhost:" + port + "/ws");
    }

}
