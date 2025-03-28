package org.example.bloodcellanalyzer;

public class DisjointSet {
    public static int find(int[] a, int id) {
        while(a[id]!=id) id=a[id];
        return id;
    }
    public static void union(int[] a, int p, int q) {
        a[find(a,q)]=p; //The root of q is made reference p
    }
}
