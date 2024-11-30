package dev.turtywurty.industria.util.enums;

public enum ElectricityTypes {
    BASIC("Basic","basic",100_000, 1_000),
    ADVANCED("Advanced","advanced",1_000_000, 10_000),
    ELITE("Elite","elite",1_000_000, 10_000),
    AP("Almost Perfect","almost_perfect",1_000_000, 10_000),
    PERFECT("Perfect","perfect",10_000_000, 1_000_000),
    CREATIVE("Creative","creative",Long.MAX_VALUE,Long.MAX_VALUE);

    String name;
    String id;

    private long capacity;
    private long maxTransfer;

    ElectricityTypes(String p_name,String p_id,long p_capacity,long p_energyTransferRate){
        this.name = p_name;
        this.id = p_id;
        this.capacity = p_capacity;
        this.maxTransfer = p_energyTransferRate;
    }
    public String getName(){return this.name;}
    public String getId(){return this.id;}

    public long getCapacity() {
        return this.capacity;
    }

    public long getMaxTransfer() {
        return this.maxTransfer;
    }
}
