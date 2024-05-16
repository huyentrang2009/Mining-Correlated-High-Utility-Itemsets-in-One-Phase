import java.util.ArrayList;

/**
 * DAA final project
 */
public class Main {
    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }
    
    public static void main(String[] args) {
        String path = "datasets/";
        String[] filenames = new String[] {
            "mushroom_utility.txt", // 0
            "chess_utility.txt",    // 1
            "connect_utility.txt",// 2
            "accidents_utility.txt",// 3
            "kosarak_utility.txt",  // 4
            "chainstore_utility.txt"   // 5
        };

        CoHUI_Miner runner = new CoHUI_Miner();
        Database D = new Database();
        int data_idx = 1;
        D.readDatabase(path+filenames[data_idx]);
        D.reOrder();
        System.out.println("Database: " + filenames[data_idx]);
        System.out.println("Transactions: " + D.transactions.size());
        System.out.println("No. distinct items: " + D.distinctItem.size());
        //---------------------------------------------
        long start = System.currentTimeMillis();    //time start
        Runtime runtime = Runtime.getRuntime();     //mem start
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
        //=======================
        double minCor = 0.7;
        int minUtil = 200000;
        ArrayList<ItemSet> cohuis = runner.runCoHUIMiner(D, minCor, minUtil);

        //=======================
        long end = System.currentTimeMillis();  //time end
        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory(); //mem end
        double usedMem = (usedMemoryAfter-usedMemoryBefore)/1000000.0;
        System.out.printf("Memory used: %f MB\n", usedMem);
        
        long timeTakenMilis = end - start;
        double timeTakenSec = (end - start)/1000.0;
        //---------------------------------------------
        System.out.println("Time execution (ms): " + timeTakenMilis + " miliseconds");
        System.out.println("Time execution (sec): " + timeTakenSec + " seconds");
        System.out.println("No. CoHUI found: " + cohuis.size());
        System.out.printf("Minimum Utility: %d, Minimum Correlation: %.2f\n", runner.minUtil, runner.minCor);

        cohuis.forEach((cohui) -> {
            System.out.println("COHUI " + cohui);
        });    
    }    
}