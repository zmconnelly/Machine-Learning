

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author k23v638
 */
public class ID3 implements Classifier{
    private final ArrayList<Instance> trainingData;
    private int[][] testData;//[row][column] last column is classifications
    ArrayList<Instance> pruneData;
    
    private int numberOfFeatures;
    private Node root;
    
    public ID3(ArrayList<Instance> dataset){
        this.trainingData = dataset;
        ArrayList<Instance> set1 = new ArrayList<>();
            set1.addAll(dataset.subList(0, dataset.size() / 2));
            ArrayList<Instance> set2 = new ArrayList<>();
            set2.addAll(dataset.subList(dataset.size() / 2, dataset.size()));
        testData = convertTrainingDataToData(set1);
        pruneData = set2;
        makeTree();
        pruneTree(root);
    }
    
    public int classify(ArrayList<Integer> features){
        return classify(listToArray(features));
    }
    
    private int classify(int[] features){
        return getClassFromNode(root, features);
    }
    
    private void pruneTree(Node node){
        if(node.isLeaf)
            return;
        
        double currentAccuracy = getCurrentAccuracy();
        int mostCommonClass = getMostCommonValue(getColumn(node.data[0].length - 1, node.data));
        Node temp = new Node();
        temp.isLeaf = true;
        temp.classification = mostCommonClass;
        temp.parent = node.parent;
        if(temp.parent != null){
            Node parent = temp.parent;
            Edge goalEdge = getEdgeToNode(parent, node);
            goalEdge.endNode = temp;
            double newAccuracy = getCurrentAccuracy();
            if(newAccuracy > currentAccuracy){
                pruneTree(root);
                return;
            }
            else
                goalEdge.endNode = node;
        }
        else{
            root = temp;
            double newAccuracy = getCurrentAccuracy();
            if(newAccuracy > currentAccuracy)
                return;
            else
                root = node;
        }
        
        for (Edge edge : node.edges) {
            pruneTree(edge.endNode);
        }
    }
    
    private Edge getEdgeToNode(Node fromNode, Node toNode){
        for (Edge edge : fromNode.edges) {
            if(edge.endNode == toNode)
                return edge;
        }
        return null;
    }
    
    private double getCurrentAccuracy(){
        int correctCount = 0;
        for (Instance instance : pruneData) {
            if(instance.classification == this.classify(instance.features))
                correctCount++;
        }
        return correctCount / (double) pruneData.size();
        
    }
    
    
    private int getClassFromNode(Node node, int[] features){
        if(node.isLeaf)
            return node.classification;
        for (Edge edge : node.edges) {
            if(edge.attributeValue == features[node.attrPosition])
                return getClassFromNode(edge.endNode, reduceFeatures(features, node.attrPosition));
        }
        return -1;
    }
    
    private int[] reduceFeatures(int[] features, int positionToRemove){
        int[] reducedFeatures = new int[features.length-1];
        int reducedIndex = 0;
        for (int i = 0; i < features.length; i++) {
            if(i != positionToRemove){
                reducedFeatures[reducedIndex] = features[i];
                reducedIndex++;
            }
        }
        return reducedFeatures;
    }
    
    private int[] listToArray(ArrayList<Integer> features){
        int[] array = new int[features.size()];
        
        for (int j = 0; j < features.size(); j++) {
            array[j] = features.get(j);
        }
        return array;
    }
    
    private void makeTree(){
        root = makeSubTree(testData);
    }
    
    private Node makeSubTree(int[][] localData){
        if(localData[0].length == 1){//no attributes left
            Node node = new Node();
            node.isLeaf = true;
            node.classification = getMostCommonValue(getColumn(localData[0].length - 1, localData));
            return node;
        }
        else if(oneClassification(getColumn(localData[0].length - 1, localData))){//only one classification value
            Node node = new Node();
            node.isLeaf = true;
            node.classification = localData[0][localData[0].length - 1];//just grab the classification in the first row then.
            return node;
        }
        int attrPosition = getPosOfMaxGain(localData);
        Node node = new Node();
        node.data = localData;
        node.attrPosition = attrPosition;
        makeEdges(node, localData);//will make edges for each value of the nodes attribute, the recursively call makeSubTree for the end of those edges
        return node;
    }
    
    private void makeEdges(Node parent, int[][] localData){
        int attrPosition = parent.attrPosition;
        int[] attrColumn = getColumn(attrPosition, localData);
        int[] uniqueAttrVals = getUniqueAttrVals(attrColumn);
        for (int i = 0; i < uniqueAttrVals.length; i++) {
            int uniqueAttrVal = uniqueAttrVals[i];
            Edge edge = new Edge();
            edge.attributeValue = uniqueAttrVal;
            edge.startNode = parent;
            edge.endNode = makeSubTree(reduceData(localData, attrPosition, uniqueAttrVal));
            edge.endNode.parent = parent;
            parent.edges.add(edge);
        }
    }
    
    private int[][] reduceData(int[][] beginData, int attrPosition, int attrValue){
        //remove rows without the attrValue
        //mark rows to remove
        int[][] startData = cloneData(beginData);
        int removeCount = 0;
        for (int i = 0; i < startData.length; i++) {
            if(startData[i][attrPosition] != attrValue){
                startData[i][0] = -1;
                removeCount++;
            }
        }
        
        int[][] newData = new int[startData.length-removeCount][startData[0].length-1];//removeCount LESS ROWS(should never yield 0 as we get the value from getUniqueAttrVals, and 1 less column due to removing a column
        int newDataRow = 0;
        for (int i = 0; i < startData.length; i++) {
            if(startData[i][0] == -1){
                continue;
            }
            for (int j = 0; j < startData[0].length; j++) {
                if(j < attrPosition){//ignore the attrColumn
                    newData[newDataRow][j] = startData[i][j];
                }
                else if(j > attrPosition){//ignore when j == attrPosition
                    newData[newDataRow][j-1] = startData[i][j];
                }
            }
            newDataRow++;
        }
        //remove the attrPosition column
        return newData;
    }
    
    public int[][] cloneData(int[][] startData){
        int[][] clone = new int[startData.length][startData[0].length];
        for (int i = 0; i < startData.length; i++) {
            for (int j = 0; j < startData[0].length; j++) {
                clone[i][j]= startData[i][j];
            }
        }
        return clone;
    }
    
    private int[] getUniqueAttrVals(int[] attrColumn){
        Set<Integer> set = new HashSet();
        for (int i = 0; i < attrColumn.length; i++) {
            set.add(attrColumn[i]);
        }
        int[] uniqueValues = new int[set.size()];
        int i = 0;
        for (int uniqueValue : set) {
            uniqueValues[i] = uniqueValue;
            i++;
        }
        return uniqueValues;
    }
    
    private boolean oneClassification(int[] column){
        int startClassification = column[0];
        for (int i = 1; i < column.length; i++) {
            if(column[i] != startClassification)
                return false;
        }
        return true;
    }
    
    public int getMostCommonValue(int[] a) {


    if (a == null || a.length == 0)
        return 0;


    Arrays.sort(a);


    int previous = a[0];
    int popular = a[0];
    int count = 1;
    int maxCount = 1;


    for (int i = 1; i < a.length; i++) {
        if (a[i] == previous)
            count++;
        else {
            if (count > maxCount) {
                popular = a[i-1];
                maxCount = count;
            }
            previous = a[i];
            count = 1;
        }
    }


    return count > maxCount ? a[a.length-1] : popular;


}
    
    private int getPosOfMaxGain(int[][] localData){
        //TODO
        double bestGain = 0;
        int bestPosition = 0;
        int[] classifications = getColumn(localData[0].length - 1, localData);
        for (int i = 0; i < localData[0].length-1; i++) {
            int[] curColumn = getColumn(i, localData);
            double gain = gain(curColumn, classifications);
            if(gain > bestGain){
                bestGain = gain;
                bestPosition = i;
            }
        }
        return bestPosition;
    }
    
    private double gain(int[] column, int[] classifications){
        //TODO
        double entropyOfClasses = entropy(classifications);
        double gain = entropyOfClasses;
        int[] uniqueAttrVals = getUniqueAttrVals(column);
        double attrSum = 0;
        for (int i = 0; i < uniqueAttrVals.length; i++) {
            int attrVal = uniqueAttrVals[i];
            int numberOfValueOccurences = getNumberOfOccurences(attrVal, column);
            int[] reducedClassifications = reduceClassifications(column, classifications, attrVal);
            attrSum += (numberOfValueOccurences / (double) column.length) * entropy(reducedClassifications);
        }
        gain -= attrSum;
       return gain;
    }
    
    private int[] reduceClassifications(int[] column, int[] classifications, int colVal){
        ArrayList<Integer> reduced = new ArrayList();
        for (int i = 0; i < column.length; i++) {
            if(column[i] == colVal)
                reduced.add(classifications[i]);
        }
        int[] reducedArray = new int[reduced.size()];
        int i = 0;
        for (Integer curVal : reduced) {
            reducedArray[i] = curVal;
            i++;
        }
        return reducedArray;
    }
    
    private int getNumberOfOccurences(int val, int[] column){
        int count = 0;
        for (int i = 0; i < column.length; i++) {
            if(column[i] == val)
                count++;
        }
        return count;
    }
    
    private double entropy(int[] column){
        int[] vals = getUniqueAttrVals(column);
        double sum = 0;
        for (int i = 0; i < vals.length; i++) {
            int currentVal = vals[i];
            int numberOfOccurences = getNumberOfOccurences(currentVal, column);
            double probability = numberOfOccurences / (double) column.length;
            double completeValProb = probability * Math.log10(probability) / (double)Math.log10(2);
            sum += completeValProb;
        }
        return -1*sum;
    }
    
    private int getUniqueAttrValues(int[] values){
        Set set = new HashSet<Integer>();
        for (int i = 0; i < values.length; i++) {
            set.add(values[i]);
        }
        return set.size();
    }
    
    private int[][] convertTrainingDataToData(ArrayList<Instance> trainingData) {


        Instance sample = trainingData.get(0);
        numberOfFeatures = sample.features.size();
        int[][] data = new int[trainingData.size()][numberOfFeatures + 1];


        for (int i = 0; i < trainingData.size(); i++) {
            Instance instance = trainingData.get(i);
            for (int j = 0; j < numberOfFeatures; j++) {
                data[i][j] = instance.features.get(j);
            }
            //class is last element in the row
            data[i][numberOfFeatures] = instance.classification;
        }
        return data;
    }
    
    public final int[] getColumn(int position, int[][] localData) {


        //System.out.println("Position: "+position);
        //gets column of data at the given position;
        int[] column = new int[localData.length];
        for (int i = 0; i < localData.length; i++) {
            column[i] = localData[i][position];
        }
        return column;
    }
    
    private class Node{
        ArrayList<Edge> edges = new ArrayList();
        boolean isLeaf = false;
        int classification;
        int attrPosition;
        Node parent;
        int[][] data;
        public Node(){
        
    }
        public Node(Node node){
            this.edges = node.edges;
            this.isLeaf = node.isLeaf;
            this.classification = node.classification;
            this.attrPosition = node.attrPosition;
            this.parent = node.parent;
            this.data=node.data;
        }
    }
    
    private class Edge{
        int attributeValue;
        Node endNode;
        Node startNode;
    }
    
}






