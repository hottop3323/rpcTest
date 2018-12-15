package remote.procedure.call.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    //获取代表服务端接口的动态代理对象(HelloService)
    //<T>相当于Object任意类型
    //serviceName:请求的接口名
    //addr:待请求的服务端的ip、port
    //return    动态代理对象
    @SuppressWarnings("unchecked")
    public static <T> T getRemoteProxyObj(Class serviceInterface, InetSocketAddress addr){
        //newProxyInstance(a,b,c)
        /**
         * a 类加载器:需要代理那个类(例如HelloService)，就需要将HelloService的类加载器传入第一个参数
         * b 需要代理的对象，具备哪些方法--接口
         * 单继承多实现 A implements B接口、C接口
         *  String[] str = new String[]{"aaa","bb","ccc"};
         */
        return (T)Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, new InvocationHandler() {
            //proxy: 需要代理的对象    method:那个方法(sayHello())     args:参数列表
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //客户端向服务端发送请求,请求某一个具体的接口
                ObjectInputStream input = null;
                ObjectOutputStream output = null;
                try {
                    Socket socket = new Socket();
                    //socketaddress
                    socket.connect(addr);
                    socket.getInputStream();
                    output = new ObjectOutputStream(socket.getOutputStream());//发送:序列化流(对象流)
                    //接口名字、方法名 writeUTF
                    output.writeUTF(serviceInterface.getName());
                    output.writeUTF(method.getName());
                    //方法参数的类型、方法参数名字  Object
                    output.writeObject(method.getGenericParameterTypes());
                    output.writeObject(args);
                    //等待服务端处理...
                    //接收服务端处理后的返回值
                    input = new ObjectInputStream(socket.getInputStream());
                    return input.readObject(); //客户端向服务端发出请求拿到返回值
                } catch (Exception e){
                    e.printStackTrace();
                    return null;
                } finally {
                    try {
                        if (output != null) output.close();
                        if (input != null) input.close();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
