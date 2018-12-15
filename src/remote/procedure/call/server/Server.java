package remote.procedure.call.server;

import java.io.IOException;

public interface Server {
    public void start();
    public void stop();
    //注册服务
    public void register(Class service,Class serviceImpl);
    //todo
}
