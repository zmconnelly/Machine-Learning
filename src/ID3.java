
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ID3 implements Classifier {

    private final ArrayList<Instance> trainingData;
    
    private ArrayList<Integer[]> test = new ArrayList();

    public ID3(ArrayList<Instance> trainingData) {

        this.trainingData = trainingData;
        ArrayList<Integer[]> testSet = new ArrayList();
        
        Integer[] test2 = {0,0};
        Integer[] test3 = {0,0};
        Integer[] test4 = {1,1};
        Integer[] test5 = {2,1};
        Integer[] test6 = {2,1};
        Integer[] test7 = {2,0};
        Integer[] test8 = {1,1};
        Integer[] test9 = {0,0};
        Integer[] test10 = {0,1};
        Integer[] test11 = {2,1};
        Integer[] test12 = {0,1};
        Integer[] test13 = {1,1};
        Integer[] test14 = {1,1};
        Integer[] test15 = {2,0};
        
        testSet.add(test2);
        testSet.add(test3);
        testSet.add(test4);
        testSet.add(test5);
        testSet.add(test6);
        testSet.add(test7);
        testSet.add(test8);
        testSet.add(test9);
        testSet.add(test10);
        testSet.add(test11);
        testSet.add(test12);
        testSet.add(test13);
        testSet.add(test14);
        testSet.add(test15);
        System.out.println(gain(testSet));
    }

    @Override
    public int classify(ArrayList<Integer> featureVector) {

        return -1;
    }
    
    public double gain(ArrayList<Integer[]> vals){
        ArrayList<Integer> classVals = new ArrayList();
        ArrayList<Integer> attrVals = new ArrayList();
        for (Integer[] i: vals){
           attrVals.add(i[0]);
           classVals.add(i[1]);
        }
        double gain = entropy(vals.size(), countClass(classVals));
        
        return gain;
    }
    
    
    public double entropy(int total, ArrayList<Integer> portions){
        double entropy = 0;
        for(Integer i: portions){
            entropy += (0 - (((double)i/total) * (Math.log((double)i/total)/Math.log(2))));
        }
        return entropy;
    }
    
    public HashMap<Integer, Integer> countAttr(ArrayList<Integer> vals){
        HashMap<Integer, Integer> count = new HashMap();
        for(Integer i: vals){
            if(count.containsKey(i)){
                count.put(i, count.get(i) + 1);
            }
            else{
                count.put(i, 1);
            }
        }
        return count;
    }
    
//    public HashMap<Integer,Integer> countClassForAttr(ArrayList<Integer> attr, ArrayList<Integer> classes){
//        HashMap<Integer, Integer> count = new HashMap();
//        for(int i = 0; i < attr.size(); i++){
//            if(count.containsKey(attr.get(i)))
//        }
//    }
    
    public ArrayList<Integer> countClass(ArrayList<Integer> vals){
        ArrayList<Integer> res = new ArrayList();
        HashMap<Integer, Integer> temp = new HashMap();
        for(Integer i: vals){
            if(temp.containsKey(i)){
                temp.put(i, temp.get(i) + 1);
            }
            else{
                temp.put(i, 1);
            }
        }
        Iterator it = temp.entrySet().iterator();
        while(it.hasNext()){
            HashMap.Entry pair = (HashMap.Entry)it.next();
            res.add((Integer)pair.getValue());
            it.remove();
        }
        return res;
    }
}
