import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Database
 * - the transactional database
 * - biggest data structure in project hierachy 
 * - a transactional database has 
 *      + transactions: ArrayList<Transaction>: records of transaction
 *      + distinctItem: ArrayList<Item>: list of different items in database
 *      + ItemID: ArrayList<Integer>: a list storing items' ID of distincItem
 *      + SUP: static HashMap<Integer, Integer>: record of item support of every item in distinctItem   
 *      + TWU: static HashMap<Integer, Integer>: record of item TWU of every item in distinctItem   
 *          (TWU(item i) - transaction-weighted-utility -> the total 
 *          transaction's utility of all transaction that support item i)
 *      + itemSupComparator: private ItemComparatorOnSupport: item comparator 
 *          based on its support -> for sorting transaction item list 
 */
public class Database {

    public ArrayList<Transaction> transactions = new ArrayList<>();
    public ArrayList<Item> distinctItem = new ArrayList<>();    // items
    public ArrayList<Integer> ItemID = new ArrayList<>();
    public static HashMap<Integer, Integer> SUP = new HashMap<>(); // item and its support
    public static HashMap<Integer, Integer> TWU = new HashMap<>(); // transaction weighted utility
    public static HashMap<Integer, Integer> U = new HashMap<>(); // item util on Database
    private ItemComparatorOnSupport itemSupComparator = new ItemComparatorOnSupport();

    public Database(){}

    //======================================================= UTIL 01
    /*
     * @func: readDatabase -> read the transaction and its utility to the database
     *      - the data is stored in text file .txt
     * @param: filepath: String -> path to the data file
     * @return: void// create the transactions and add it to the database record
     */
    public void readDatabase(String filepath){
        try {
            File f = new File(filepath);
            Scanner sc = new Scanner(f);
            int uT;
            String[] input = new String[3];
            while(sc.hasNextLine()){
                input = sc.nextLine().split(":");
                uT = Integer.parseInt(input[1]);
                this.addTransaction(input[0], uT,input[2]);
            }
            sc.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //======================================================= UTIL 02
    /*
     * @func: addTransaction -> create a transaction with item list, utility coef, and items' utilites then
     *      add to database
     * @params: 
     *      - itemList: String -> the ID of items read in as a long string, each ID seperated by a blank
     *      - utilityT: int -> transaction utility
     *      - itemUtil: String -> the Utility of items read in as a long string, each utility coef seperated by a blank
     * @return: void // just add a new transactions to the database
     */
    public void addTransaction(String itemList, int utilityT, String utilityI){
        Transaction trans = new Transaction();
        trans.addItems(itemList, utilityI);
        trans.setUtility(utilityT);
        transactions.add(trans); 
    }
    
    //======================================================= UTIL 03
    /*
     * @func: addTransaction: add an transaction to database
     * @param: T: Transaction -> a trans instance
     * @return: void // just add a new transaction to the database
     */
    public void addTransaction(Transaction T){
        transactions.add(T); 
    }
    //======================================================= UTIL 04
    /*
     * @func: reOrder -> have 3 stages
     *      1. scanning distinct items
     *      2. discover items' support and its twu
     *      3. sort database's transaction by item support
     * @non-param
     * @return: void // but a MUST run// invoke after reading database 
     */
    public void reOrder(){
        this.scanDistinctItem();
        this.discoverItemUtility_Support_TWU();
        this.sortTransactionItemBySupport();
    }
    
    //======================================================= DEF 01.A
    /*
     * @func: scanDistinctItem -> mutate the attribute ArrayList distinctItem 
     *      filled with different items in database 
     * @param: non-param
     * @return: void
     */
    private void scanDistinctItem(){
        for (Transaction trans: transactions){
            for (Item it: trans.items){
                if(ItemID.contains(it.IID)){
                    continue;
                } else {
                    ItemID.add(it.IID);
                    distinctItem.add(it);
                }
            }
        }
    }

    //======================================================= DEF 02.A
    /*
     * @func:discoverItemSupportAndTWU -> mutate SUP and TWU
     *      - fill SUP with every item's support
     *      - fill TWU with every item's TWU coef
     * @param: non-param
     * @return: void
     */
    private void discoverItemUtility_Support_TWU(){
        //========= item support
        int support;
        //========= transaction-weighted-utility       
        int twu;
        //========= utility       
        int util; 
        for (int iid: ItemID){
            util = 0;
            support = 0;
            twu = 0;
            for (Transaction trans: transactions){   
                for (Item transItem: trans.items){
                    if(transItem.IID == iid){
                        util += transItem.IUtility;
                        support += 1;
                        twu += trans.getUtility();
                        break;
                    }
                } 
            }
            U.put(iid, util);
            SUP.put(iid, support);
            TWU.put(iid, twu);
        }        
    }

    //======================================================= DEF 02.B
    /*
     * @func: ItemsetTWU -> return TWU of an itemset
     * @param: X: an itemset
     * @return: TWU of X
     *                  PROPERTY 02: if TWU(X) < minUtil then no supersets of X are a HUI
     */
    public int ItemsetTWU(ItemSet X){
        int twuX = 0;
        for (Transaction T: this.transactions){
            if (ItemsetIsSupportByTransaction(T, X)){
                twuX += T.getUtility();
            }
        }
        return twuX;
    }

    /*
     * @func: ItemsetUtility -> return utility of an itemset in database D
     * @param: X: an itemset
     * @return: U(X)
     */
    public int ItemsetUtility(ItemSet X){
        int utilX = 0;
        for (Transaction T: this.transactions){
            if (ItemsetIsSupportByTransaction(T, X)){
                utilX += X.getUtility();
            }
        }
        return utilX;
    }
    
    //======================================================= DEF 03.A
    /*
     * @func: ItemsetIsSupportByTransaction -> check if a trans T support itemset X
     * @param: 
     *      T: Transaction 
     *      X: ItemSet
     * @return: boolean value 
     *      true: T does support X
     *      false: otherwise
     */
    public boolean ItemsetIsSupportByTransaction(Transaction T, ItemSet X){
        int count = 0;
        for (Item itemX: X.ITSet){
            for(Item transI: T.items){
                if(transI.IID == itemX.IID){
                    count += 1;
                    break;
                }
            }
        }
        if(count == X.ITSet.size()){
            return true;
        }
        return false;
    }
    
    //======================================================= DEF 03.B
    /*
     * @func: itemsetSupport -> scanning the database to count support of an itemset
     * @param: ItemSet X
     * @return: an integer -> number of transaction having X 
     */
    public int itemsetSupport(ItemSet X){
        int supX = 0;
        int count ;
        for (Transaction trans: transactions){
            count = 0;
            for (Item itemX: X.ITSet){
                for(Item transI: trans.items){
                    if(transI.IID == itemX.IID){
                        count += 1;
                        break;
                    }
                }
            }
            if(count == X.ITSet.size()){
                supX += 1;
            }
        }

        return supX;
    }
    
    //======================================================= DEF 04
    /*
     * @func: sortTransactionItemBySupport -> sort every trans item list by item support
     * @param: non-param
     * @return: void
     */
    private void sortTransactionItemBySupport(){        
        for (Transaction trans: transactions){
            trans.items.sort(itemSupComparator);
        }
    }

    //======================================================= DEF 05
    /*
     * @func: KulcCorrelation -> return Kulczynsky correlation of an item set
     * @param: X: ItemSet
     * @return: kulczynsky coef of X
     */
    public double KulcCorrelation(ItemSet X){
        double kulc = 0;
        double supX = (double)this.itemsetSupport(X);
        for(Item it: X.ITSet){
            kulc += (supX/SUP.get(it.IID));
            // System.out.println(SUP.get(it.IID));
        }
        return kulc/X.ITSet.size();
    }


    //*************START************************
    //*************DATABASE PROJECTION**********

    //======================================================= DEF 08
    /*
     * @func: calculate the projected transaction T on itemset X
     * @param:
     *      T: Transaction 
     *      X: ItemSet
     * @return: projected transaction of T on X
     */
    public Transaction projectedTransactionOnItemset(Transaction T, ItemSet X){
        ArrayList<Item> itemX = new ArrayList<>();
        Transaction projectedT = new Transaction(T.TID, T.getUtility()); 
        if (!ItemsetIsSupportByTransaction(T, X)){
            return projectedT;
        }
        
        itemX.addAll(X.ITSet);
        itemX.sort(itemSupComparator);
        Item last = itemX.get(itemX.size() - 1);
        int start_idx = 0;
        for (int i = 0; i < T.items.size(); i++){
            if (T.items.get(i).IID == last.IID){
                start_idx = i + 1;
                break;
            }
        }

        for (int i = start_idx; i < T.items.size(); i++){
            projectedT.addItem(T.items.get(i));
        }
        return projectedT;
    }

    //======================================================= DEF 09
    /*
     * @func: projectedDatabaseOnItemset -> return a projected database on X, which contains 
     *      projected transactions on itemset X 
     * @param:
     *      X: ItemSet
     * @return: projected database D on itemset X
     */
    public Database projectedDatabaseOnItemset(ItemSet X){
        Database projectD = new Database();
        for (Transaction T: transactions){
            if (ItemsetIsSupportByTransaction(T, X)){
                projectD.addTransaction(
                    projectedTransactionOnItemset(T, X)
                );
            }
        }
        return projectD;
    }

    //======================================================= DEF 10
    /* 
     * @func: prefixUtilityTransaction -> pru(Tj\X) = u(Tj\X): utility of itemset X on T
     * @param: 
     *      T: Transaction 
     *      X: ItemSet
     * @return: prefix utility of projected transaction T on itemset X 
     */
    public int prefixUtilityTransaction(Transaction T, ItemSet X){
        if (ItemsetIsSupportByTransaction(T, X)){
            return 0;
        }
        int PRU = 0;
        for (Item it: X.ITSet){
            PRU += it.IUtility;
        }
        T.PRU = PRU;
        return PRU;        
    }

    //======================================================= DEF 11
    /* 
     * @func: remainingUtilityTransaction -> return RU(X\T) on transaction T
     * @param: 
     *      T: Transaction 
     *      X: ItemSet
     * @return: sum of utility of items that are after itemset X in T  
     */
    public int remainingUtilityTransaction(Transaction T, ItemSet X){
        if (!ItemsetIsSupportByTransaction(T, X)){
            return 0;
        }
        int RU = 0;
        Transaction projT = projectedTransactionOnItemset(T, X);
        for (Item it: projT.items){
            RU += it.IUtility;
        }

        return RU;
    }

    //======================================================= DEF 12
    /* 
     * @func: remainingUtilityDatabase -> RU(X) in D -> sum of all RU(X\T) for all T in D
     * @param: 
     *      X: ItemSet
     * @return: remaining utility of X in database 
     */
    public int remainingUtilityDatabase(ItemSet X){
        int RUD = 0;

        for (Transaction T: this.transactions){
            RUD += remainingUtilityTransaction(T, X);
        }
        return RUD;
    }

    //*************DATABASE PROJECTION**********
    //*************END**************************    
    //======================================================= PRINTING 01
    /*
     * @func: print #[start - end] transactions info in database 
     * @param:
     *      - start: int -> start index
     *      - end: int -> end index 
     * @return void
     */
    public void printTransactions(int start, int end){
        if (end > transactions.size() || start < 0){
            return;
        }

        for (int i = start; i < end; i++){
            System.out.println(transactions.get(i));
        }
    }

    //======================================================= PRINTING 02
    // print all transaction info // non-param // return void
    public void printAllTransaction(){
        for (Transaction trs: transactions){
            System.out.println(trs);
        }
    }
}