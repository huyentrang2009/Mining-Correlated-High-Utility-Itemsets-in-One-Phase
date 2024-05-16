import java.util.ArrayList;
import java.util.List;

/**
 * CoHUIMAlgorithm
 */
public class CoHUI_Miner {

    ArrayList<Item> Ikeep;
    double minCor = 0D;
    int minUtil = 0;
    Database D;
    public CoHUI_Miner(){}
    
    /**
     * @function: runCoHUIMiner: this function is used to run the correlated high-utility itemsets-Miner
     *      in which has some improvements comparing to its predecessor
     * @param D transactional database
     * @param minCor user-specified minimum correlation threshold
     * @param minUtil user-specified minimum utility threshold
     * @return: set of all correlated HUIs.
     */
    public ArrayList<ItemSet> runCoHUIMiner(Database D, double minCor, int minUtil){
        this.minUtil = minUtil;
        this.minCor = minCor;
        this.D = D;
        ArrayList<ItemSet> CoHUIs = new ArrayList<>(); // set of all CoHUIs
        // 1. scan database for SUP, TWU, U
        D.reOrder();         
        // 2. construct I_keep = {i in Ikeep| TWU(i) >= minUtil}
        Ikeep = new ArrayList<>();       

        for (Item it: D.distinctItem){
            if (Database.TWU.get(it.IID) >= minUtil){
                Ikeep.add(it);
            }
        }    
        Ikeep.removeIf(it -> (it.IID == 24));
    
        // 3.update SUP, U in database 
        // (1) SUP = {SUP(i) | i in IKeep}
        // (2) U(i) = {U(i) | i in IKeep} 
        // (3): Eliminate i from database D if i not in Ikeep
        boolean isInIKeep;
        ArrayList<Integer> databaseRemoveID = new ArrayList<>();

        for (int iid: Database.SUP.keySet()){
            isInIKeep = false;
            for (Item it: Ikeep){
                if (iid == it.IID){
                    isInIKeep = true;
                    break;
                }                
            }
            if (!isInIKeep){
                databaseRemoveID.add(iid);
            }
        }
        databaseRemoveID.forEach((rid) -> {
            Database.SUP.remove(rid);   // 3.1
            Database.U.remove(rid);     // 3.2
        });
        // 3.3 Eliminate i from database D if i not in Ikeep
        ArrayList<Integer> transRemoveID;
        for (Transaction T: D.transactions){
            transRemoveID = new ArrayList<>();
            for (Item it: T.items){
                if (databaseRemoveID.contains(it.IID)){
                    transRemoveID.add(it.IID);
                }
            }
            transRemoveID.forEach((remvID) -> {
                T.items.removeIf( transItemID ->
                    (transItemID.IID == remvID)
                );
            });
        }        

        // 4.1 sort Ikeep in the increasing order of SUP and 
        // 4.2 sort items of all transactions in D with respect to I keep 
        Ikeep.sort(
            new ItemComparatorOnSupport()
        );
        D.reOrder();   
        // 5. for each item X in Ikeep do
        for (Item itemX: Ikeep){
            Database dbProjectX = new Database();
            ItemSet itemSetX = new ItemSet(itemX);
            // 6. if U(X) > minUtil then 
            if (Database.U.get(itemX.IID) > minUtil){
                // 7. add X to CoHUIs
                CoHUIs.add(itemSetX);
            // 8. end if
            }
            // 9. set RU(X) = 0 
            itemSetX.RU = 0;    
            // 10. for each transaction T in D do
            for (Transaction T: D.transactions){
                Transaction newProjTrans;
                // 11. set j = 0; x_j in T; uTemp = u(T)
                int j = 0, uTemp = T.getUtility();  
                // 12. while (j < |T| AND item X is after x_j) do
                while (j < T.items.size() && T.itemIsAfterItem(T.items.get(j), itemX)){                    
                    // 13. decrement uTemp by u(xj, T)
                    uTemp -= T.items.get(j).IUtility;
                    // 14. increment j by 1
                    j += 1;                    
                    // 15. end while
                }
                // 16. if (j == len(T.items) OR item x_j is after item X) 
                // then continue
                if (j == T.items.size() - 1 || T.itemIsAfterItem(itemX, T.items.get(j))){
                    continue;                
                // 17. else if (j < len(T.items)) then calculate projected transaction
                } else if (j < T.items.size()) {
                    // 18. initial T\X = T.Get(j + 1, |T|)
                    newProjTrans = new Transaction(T.TID, T.getUtility());
                    List<Item> remItemT = T.items.subList(j + 1, T.items.size()); 
                    newProjTrans.items.addAll(remItemT);
                    // 19. PRU(T\X) = u(xj,T)
                    newProjTrans.PRU = T.items.get(j).IUtility;
                    // 20. uT(T\X) = uTemp
                    newProjTrans.TUtility = uTemp;
                    // 21. dbProjectX.add(newTran)
                    dbProjectX.addTransaction(newProjTrans);
                    // 22. Increment RU(X) by uTemp
                    itemSetX.RU += uTemp;
                    // 23. end if 
                }
                // 24. end for
            }
            // 25. SearchCoHUI(X, U(X), RU(X), dbProjectX, 1)
            ArrayList<ItemSet> newCoHUIs = SearchCoHUI(
                itemSetX, 
                D.ItemsetUtility(itemSetX), 
                itemSetX.RU, 
                dbProjectX, 
                1
            );
            CoHUIs.addAll(newCoHUIs);
            // 26. end for
        }        
        return CoHUIs;
    }

    //================================================================================
    /**
     * @func: SearchCoHUI
     * @param: 
     *      - X: prefix itemset
     *      - U(X): utility of X
     *      - RU(X): the remain utility of X
     *      - dbProjectX : projected database with X prefix
     *      - k: length of items set X.
     * @return:  CoHUIs
     */
    private ArrayList<ItemSet> SearchCoHUI(ItemSet X, int UX, int RUX, Database dbProjectX, int k){
        ArrayList<ItemSet> CoHUIs = new ArrayList<>();
        // 1. for i = k to |I keep| do 
        outerIkeep:
        for (int i = k; i < this.Ikeep.size(); i++){
            // 2. LastItem = Ikeep[i]
            // LastItem is an item that is extended from X itemset.
            Item lastItem = Ikeep.get(i);
            // 3.  X' =  X joins LastItem; X': extended from X by 'LastItem'
            ItemSet extendedX = new ItemSet();
            extendedX.ITSet.addAll(X.ITSet);
            extendedX.ITSet.add(lastItem);
            // 4. Initial U(X') = U(X); RU(X') = 0;
            // SUP(X') = 0; ULA = U(X) + RU(X)
            int UextendedX = UX;
            extendedX.RU = 0;
            extendedX.SUP = 0;
            int ULA = UX + RUX;
            // 5
            int j, uTemp;
            Database dbProjectXtend= new Database();
            for (Transaction T: dbProjectX.transactions){
                Transaction projTransXtend;
                // 6. set j = 0, xj in T
                j = 0; 
                // 7
                uTemp = T.getUtility();    
                // 8. while (j < T.len AND lastItem is after xj)
                while (j < T.items.size() && T.itemIsAfterItem(T.items.get(j), lastItem)){
                    // 9. decrement uTemp by xj util
                    uTemp -= T.items.get(j).IUtility;
                    // 10. increment j by 1
                    j += 1;
                    // 11. end while
                }
                // 12 if (j == |T| OR last item is after x_j)
                if (j == T.items.size() || T.itemIsAfterItem(lastItem, T.items.get(j))){
                    // 13. Decrement U(X') by pru(T);
                    extendedX.Utility -= T.PRU;
                    // 14. Decrement ULA by (pru(T) + uT(T))
                    ULA -= (T.PRU + T.getUtility());
                    // 15 if ULA < minUtil then return;//LA-Prune
                    if (ULA < this.minUtil){
                        // 16 Continue; if Xextend is not in T, then decrease ULA
                        // once ULA < minUtil stop the the projection with Xtend with return
                        continue outerIkeep;
                    }                    
                // 17 else
                } else {                    
                    // 18 Increment U(X') by u(xj; T)
                    UextendedX += T.items.get(j).IUtility; 
                    // 19 Increment SUP(X') by 1;
                    extendedX.SUP += 1;
                    // 20 if j < |T| then
                    if (j < T.items.size()){
                        // //Calculate projected transaction with X'
                        // 21. T\X' = T.Get(j + 1, |T|)
                        projTransXtend = new Transaction(T.TID, T.getUtility());
                        List<Item> remItemT = T.items.subList(j + 1, T.items.size()); 
                        projTransXtend.items.addAll(remItemT);
                        // 22 pru(T\X') = (pru(T) + u(xj,T))
                        projTransXtend.PRU = T.PRU + T.items.get(j).IUtility;
                        // 23 uT(T\X') = uTemp
                        projTransXtend.TUtility = uTemp;
                        // 24 dbProjectX' :add(newTran);
                        dbProjectXtend.addTransaction(projTransXtend);                        
                        // 25 Increment RU(X') by uTemp;
                        extendedX.RU += uTemp;
                    // 26 end if
                    }
                // 27 end if
                }
            // 28 end for              
            }
            // 29 if SUP(X') > 0 then
            if (extendedX.SUP > 0){
                // 30 Calculate kulc(X')//by the formula in definition 5                
                double kulcXtend = this.D.KulcCorrelation(extendedX); 
                // 31 if kulc(X') >= minCor then
                if (kulcXtend >= minCor){
                    // 32 if U(X') >=  minUtil then
                    if (UextendedX >= minUtil) {
                        // 33 CoHUIs <- X';
                        extendedX.kulc = kulcXtend;
                        extendedX.Utility = UextendedX;
                        CoHUIs.add(extendedX);
                    // 34 end if   
                    }
                    // 35 if U(X') + RU(X') >= minUtil 
                    if (UextendedX + extendedX.RU >= minUtil) {
                        //then //U-Prune                        
                        // 36 SearchCoHUI(X'; U(X'); RU(X'); dbProjectX'; k + 1)
                        ArrayList<ItemSet> newCoHUIs = SearchCoHUI(
                            extendedX, 
                            UextendedX, 
                            extendedX.RU, 
                            dbProjectXtend, 
                            k + 1                        
                        );
                        CoHUIs.addAll(newCoHUIs);
                    // 37 end if    
                    }                    
                // 38 end if                    
                }                                
            // 39 end if    
            }               
        // 40 end for
        }
        return CoHUIs;
    } 
}