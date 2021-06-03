import BPTree.BPTree;
import BPTree.BPTreeCreateIndex;

public class TreeLoad {
    public static void main(String[] args) {
        if(args.length!=1){
            throw  new RuntimeException("The number of arguments provided are incorrect, Please input in this format: [querytext] [pagesize]");
        }
        int pageSize = Integer.parseInt(args[0]);
        BPTree bpTree = new BPTree();
        long startTime = System.currentTimeMillis();
        BPTreeCreateIndex.constructBpIndex(pageSize, bpTree);
        long endTime = System.currentTimeMillis();
        System.out.println("Total time taken to load the tree and creation of tree.4096 in milliseconds: "+(endTime-startTime));
    }
}
