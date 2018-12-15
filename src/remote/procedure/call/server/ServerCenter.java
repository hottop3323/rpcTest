package remote.procedure.call.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//服务中心具体实现
public class ServerCenter implements Server {
    //map:服务端所有可供客户端访问的接口，都注册到该map中
    //key:接口的名字"HelloService"  value:真正的HelloService实现
    private  static HashMap<String,Class> serviceRegister = new HashMap();
    private static int port;//9999
    //连接池:连接池存在多个连接对象，每个连接对象都可以处理一个客户请求
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static boolean isRunning = false;
    //通过构造方法传进来一个port
    public ServerCenter(int port) {
        this.port = port;
    }

    //开启服务端服务，用Socket暴露一个端口
    @Override
    public void start(){//while(true){start();}
        ServerSocket server = null;
        try {
            server = new ServerSocket();
            server.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
        isRunning = true;   //服务已经启动
        while (true){
            //具体的服务内容:接收客户端请求，处理请求，并返回结果

            //100:1  1  1   1 ...1---->如果想让多个客户端并发执行->多线程
            System.out.println("start server...");
            //客户端每次请求一次连接(发出一次请求)，则服务端从连接池中获取一个线程对象去处理
            Socket socket = null;
            try {
                socket = server.accept();//等待客户端连接
            } catch (IOException e) {
                e.printStackTrace();
            }
            executor.execute(new ServiceTask(socket));  //启动线程，去处理客户请求
        }
    }

    @Override
    public void stop() {
        isRunning = false;
        executor.shutdown();
    }

    //服务端在操作时，需要拿到class，而不是字符串，这里第一个用Class，而不是String
    @Override
    public void register(Class service,Class serviceImpl) {
        serviceRegister.put(service.getName(),serviceImpl);

    }

    //socket客户端 - socket服务端( start()、ServiceTask )
    private static class ServiceTask implements Runnable{
        private Socket socket;
        public ServiceTask(){}

        public ServiceTask(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {//线程所有的事情
            ObjectInputStream input = null;
            ObjectOutputStream output = null;
            try {

            //接到到客户端连接及请求，处理该请求...
            input = new ObjectInputStream(socket.getInputStream());//将字节流转化对象流(序列化流)
            //因为ObjectInputStream对发送数据的顺序严格要求，因此需要参照发送的顺序，逐个接受
            String serviceName = input.readUTF();
            String methodName = input.readUTF();
            Class[] parameterTypes = (Class[]) input.readObject();//参数类型 String Integer
            Object[] arguments = (Object[]) input.readObject();//方法的参数名
            //根据客户请求，到map中找到与之对应的具体的接口
            Class ServiceClass = serviceRegister.get(serviceName); //HelloService

            Method method = ServiceClass.getMethod(methodName, parameterTypes);
            //执行该方法
            Object result = method.invoke(ServiceClass.newInstance(), arguments);//person.say(参数列表)
            //向客户端将方法执行完毕的返回值，传给客户端
            output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(result);
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        }
    }
}
