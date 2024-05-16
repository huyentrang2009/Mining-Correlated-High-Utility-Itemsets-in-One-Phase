import java.util.ArrayList;
/**
 * Transaction
 * - the Transaction is an record in transactional database, each instance
 * has its own ID, an item list and transaction utility coef.  
 */
public class Transaction {
    
    static int basedTID = 0; // base ID which increases every time a new transaction initiated
    public int TID; // transaction ID
    public ArrayList<Item> items = new ArrayList<>(); // item list: ArrayList -> for sorting on item's support sake  
    public int TUtility = -1; // transactional utility
    public int PRU = 0;
    private static ItemComparatorOnSupport itemComp = new ItemComparatorOnSupport();

    // non-param constructor 
    public Transaction(){
        basedTID += 1;
        TID = basedTID;        
    }   

    /* constructor with 2 params 
     *      - id: int 
     *      - tutil: int
    */
    public Transaction(int id, int tutil){
        TID = id;
        TUtility = tutil;
    }

    //======================================================= DEF 07.A
    /*
     * @func: itemIsAfterItemset -> return boolean value if an item xj is after every 
     *      item in the itemset X 
     * @param: 
     *      - xj: Item
     *      - X: ItemSet
     * @return: boolean
     *      - true: xj is after X
     *      - false: there's at least 1 item in X is after xj
     */
    public boolean itemIsAfterItemset(Item xj, ItemSet X){
        ArrayList<Item> itemX = new ArrayList<>();
        itemX.addAll(X.ITSet);
        itemX.sort(
            itemComp
        );
        Item lastX = itemX.get(itemX.size() - 1);
        int idxLast = -1, idxItemJ = -1; 
        for (int i = 0; i < this.items.size(); i++){
            if (lastX.IID == this.items.get(i).IID){
                idxLast = i;
            }
            if (xj.IID == this.items.get(i).IID){
                idxItemJ = i;
            }            
        }

        return idxItemJ > idxLast;
    }


    //======================================================= DEF 07.B
    /* 
     * @func: itemIsAfterItem -> return true whether item 'second' is after item 'first'
     * @param: 
     *      - first: Item
     *      - second: Item
     * @return: true if 'second' is after 'first' in this transaction
     */
    public boolean itemIsAfterItem(Item first, Item second){
        int idx1 = -1, idx2 = -1; 
        for (int i = 0; i < this.items.size(); i++){
            if (first.IID == this.items.get(i).IID){
                idx1 = i;
            }
            if (second.IID == this.items.get(i).IID){
                idx2 = i;
            }            
        }

        return idx2 > idx1;
    }

    /* @func: addItems: adding a list of items and its utilities to the transaction item list
     * @params:
     *      - itemList: String -> the ID of items read in as a long string, each ID seperated by a blank
     *      - itemUtil: String -> the Utility of items read in as a long string, each utility coef seperated by a blank
     * @return: void// the item list filled up with items  
     */
    public void addItems(String itemList, String itemUtil){
        String[] spItem = itemList.split(" ");
        String[] spUtil = itemUtil.split(" ");
        Item it;
        for (int i = 0; i < spItem.length; i++){
            it = new Item(
                Integer.parseInt(spItem[i]),
                Integer.parseInt(spUtil[i])
            );
            this.items.add(it);
        }
    }

    /*  @func: addItem: add an item instance to the item list
     *  @param: 
     *      - item: Item -> an item instance
     *  @return: void// item list has one more item
     */ 
    public void addItem(Item item){
        items.add(item);
    }

    // setter: set transaction util coef to u: int
    public void setUtility(int u){
        TUtility = u;
    }
    
    // getter: get transaction util
    public int getUtility(){
        return this.TUtility;
    }

    /* @func: this function flush out console the whole item list 
     * @param: non-param
     * @return: void
     */
    public void printItems(){
        for (Item it: items){
            System.out.printf("%4s", it);
        }
    }

    // toString -> transaction brief info [ID & Util]
    public String toString(){
        return String.format("[TID: %4d, U: %4d]", TID, TUtility);
    }

    // toStringItem -> get transaction's items in detail as String
    public String toStringItem(){
        String s = "{";
        for (Item item: this.items){
            s += item.toString() + "\n";
        }
        return s + "}";
    }
}