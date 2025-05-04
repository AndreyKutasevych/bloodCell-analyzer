import static org.junit.jupiter.api.Assertions.*;

import org.example.bloodcellanalyzer.DisjointSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TestClass {

    private int[] disjointSet;

    @BeforeEach
    public void setUp() {
        disjointSet = new int[10];
        Arrays.fill(disjointSet, -1);
    }

    @Test
    public void testInitialFind() {
        for (int i = 0; i < disjointSet.length; i++) {
            assertEquals(i, DisjointSet.find(disjointSet, i));
        }
    }

    @Test
    public void testUnionAndFind() {
        DisjointSet.union(disjointSet, 0, 1);
        int root0 = DisjointSet.find(disjointSet, 0);
        int root1 = DisjointSet.find(disjointSet, 1);
        assertEquals(root0, root1);
    }

    @Test
    public void testUnionBySize() {
        DisjointSet.union(disjointSet, 0, 1);
        DisjointSet.union(disjointSet, 2, 3);
        DisjointSet.union(disjointSet, 0, 2);

        int root0 = DisjointSet.find(disjointSet, 0);
        int root1 = DisjointSet.find(disjointSet, 1);
        int root2 = DisjointSet.find(disjointSet, 2);
        int root3 = DisjointSet.find(disjointSet, 3);

        assertEquals(root0, root1);
        assertEquals(root0, root2);
        assertEquals(root0, root3);

        assertEquals(-4, disjointSet[root0]);
    }

    @Test
    public void testPathCompressionManual() {
        int[] set = new int[4];
        set[0] = -1;
        set[1] = 0;
        set[2] = 1;
        set[3] = 2;

        int rootBefore = set[3];

        int root = DisjointSet.find(set, 3);
        int afterFind = set[3];

        assertEquals(0, root);
        assertNotEquals(rootBefore, afterFind);
    }


}

