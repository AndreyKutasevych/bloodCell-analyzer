package org.example.bloodcellanalyzer;

public class DisjointSet {

    public static int find(int[] a, int id) {
        if (a[id] < 0) {
            return id;
        } else {
            return a[id] = find(a, a[id]);
        }
    }

    public static void union(int[] a, int p, int q) {
        int rootp = find(a, p);
        int rootq = find(a, q);

        if (rootp == rootq) {
            return; // Already connected
        }

        // Union by size: attach smaller tree to larger tree
        if (a[rootp] < a[rootq]) {
            a[rootp] += a[rootq];
            a[rootq] = rootp;
        } else {
            a[rootq] += a[rootp];
            a[rootp] = rootq;
        }
    }
}
