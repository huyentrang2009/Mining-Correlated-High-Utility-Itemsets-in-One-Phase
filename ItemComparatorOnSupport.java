import java.util.Comparator;
/**
 * ItemComparatorOnSupport
 * - this comparator allow java.Collections sort Item instances based on their support 
 * - item's support = the number of transaction contain that item's ID
 * - item's support is accessed to the 'Database' Object static HashMap 'SUP'
 */
public class ItemComparatorOnSupport implements Comparator<Item>{

    @Override
    public int compare(Item i1, Item i2) {
        return Database.SUP.get(i1.IID) - Database.SUP.get(i2.IID);
    }    
}