package cat.nyaa.hmarket.database;


public class StoreModel {
    public String nbt;
    public int amount;
    public String owner;

    public StoreModel(String nbt, int amount, String owner) {
        this.nbt = nbt;
        this.amount = amount;
        this.owner = owner;
    }
}
