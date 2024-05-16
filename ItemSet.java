import java.util.HashSet;
import java.util.Set;

/**
 * ItemSet
 * the ItemSet plays as a set of items, which can contain results, subsets, or supersets 
 * of an itemset for the sake of CoHUI-Miner algo implementation  
 */
public class ItemSet {
    
    // Data structure: HashSet -> ensuring that there's no duplication 
    // of items in an item collection 
    public Set<Item> ITSet = new HashSet<Item>(); 
    public int Utility = 0;
    public int RU = 0;
    public int SUP = 0;
    public double kulc = 0D;
    // constructor no param
    public ItemSet(){

    }
    
    // parameterized constructor 
    public ItemSet(Item it){
        ITSet.add(it);
    }
    
    //======================================================= DEF 01
    /*
     * @func: return ItemSet's utility
     * @param: non-param
     * @return: int -> ITS utility
     */
    public int getUtility(){
        if (this.Utility != 0){
            return this.Utility;
        }
        int util = 0;
        for (Item it: ITSet){
            util += it.IUtility;
        }
        this.Utility = util;
        return util;
    }

    /*  @func: setter add an item to the itemset
        @param: an item instance 
        @return: void
    */
    public void addItem(Item item){
        ITSet.add(item);
    }

    /*  @func: setter add an item to the itemset with item ID
        @param: an item ID 
        @return: void
    */
    public void addItem(int id){
        ITSet.add(new Item(id));
    }

    // toString
    public String toString(){
        String s = "{";
        for (Item item: this.ITSet){
            s += item.toString();
        }
        s += "}";

        return String.format("%s [#LEN: %d #UTIL: %8d |#KULC: %6.5f]", s, ITSet.size(), this.Utility, this.kulc);
    }
}