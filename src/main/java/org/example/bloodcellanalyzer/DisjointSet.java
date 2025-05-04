package org.example.bloodcellanalyzer;

public class DisjointSet {

    public static int find(int[] a, int id) {
        if (a[id] < 0) {//find method with path compression
            return id;
        } else {
            return a[id] = find(a, a[id]);//reference to the parent of parent
        }
    }

    public static void union(int[] a, int p, int q) {
        int rootP = find(a, p);
        int rootQ = find(a, q);

        if (rootP == rootQ) {
            return; // Already connected
        }

        // Union by size: attach smaller tree to larger tree
        if (a[rootP] < a[rootQ]) {//pixel value determines the set it belongs to, and its size - so if one set is smaller than another - connect it to the second one
            a[rootP] += a[rootQ];
            a[rootQ] = rootP;
        } else {
            a[rootQ] += a[rootP];
            a[rootP] = rootQ;
        }
    }
}
