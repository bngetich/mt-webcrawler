public class PartitionRouter {

    private final int numPartitions;

    public PartitionRouter(int numPartitions) {
        this.numPartitions = numPartitions;
    }

    public int getPartition(String host){
        return Math.floorMod(host.hashCode(), numPartitions);
    }
}
