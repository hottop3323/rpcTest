package remote.procedure.call.test;

import remote.procedure.call.client.Client;
import remote.procedure.call.server.HelloService;

import java.net.InetSocketAddress;

public class RPCClientTest {
    public static void main(String[] args) throws ClassNotFoundException {
        HelloService service = Client.getRemoteProxyObj(Class.forName("remote.procedure.call.server.HelloService"), new InetSocketAddress("127.0.0.1",9999));
        System.out.println(service.sayHello("zs"));
    }
}
