package cat.nyaa.hmarket.signshop;


public class Manager {

    private Manager() {}

    private static final class InstanceHolder {
        private static final Manager INSTANCE = new Manager();
    }

    public static Manager getInstance() {
        return InstanceHolder.INSTANCE;
    }


}
