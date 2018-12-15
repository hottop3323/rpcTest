package remote.procedure.call.server;

public class HellloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "hi,"+name;
    }
}
