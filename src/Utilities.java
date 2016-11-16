
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Utilities {

    public static int getClassificationCount(ArrayList<Instance> dataSet) {
        Set set = new HashSet<>();
        int oldSize = 0;
        for (Instance instance : dataSet) {
            set.add(instance.classification);
        }
        return set.size();
    }
}
