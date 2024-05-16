/**
 * Item
 * this class is the nucleus object for the transactional database
 * each instance is assigned with an ID and an Utility coefficient.
 */
public class Item {

    public int IID = -1; // item ID 
    public int IUtility = -1; // item utility
    public Item(){}
    
    // constructor with 1 param: ID - int
    public Item(int ID){
        IID = ID;
    }
    
    /* constructor with 2 params: 
        ID - int 
        utility - int
     */ 
    public Item(int ID, int util){
        IID = ID;
        IUtility = util;
    }

    // toString
    // public String toString(){
    //     return String.format("[IID: %4d, U: %3d]", IID, IUtility);
    // }
    public String toString(){
        return String.format("[IID: %4d]", IID, IUtility);
    }
    
}