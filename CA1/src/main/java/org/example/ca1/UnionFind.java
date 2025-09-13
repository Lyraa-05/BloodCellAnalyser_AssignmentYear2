package org.example.ca1;

public class UnionFind {
    private int[] parent;
    private int[] size;
    private int count;

    public UnionFind(int n) {
        parent = new int[n];
        size = new int[n];
        count = n;

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int p) {
        while (p != parent[p]) {
            parent[p] = parent[parent[p]]; //path compression
            p = parent[p];
        }
        return p;
    }

    //same root?
    public boolean connected(int p, int q) {
        return find(p) == find(q);
    }

    //merge sets
    public void union(int p, int q){
        int rootP = find(p);
        int rootQ = find(q);

        if (rootP == rootQ) return;

        //weighted union
        if (size[rootP] < size[rootQ]) {
            parent[rootP] = rootQ;
            size[rootQ] += size[rootP];
        } else {
            parent[rootQ] = rootP;
            size[rootP] += size[rootQ];
        }
        count--;
    }
    public int getCount(){
        return count;
    }
    public int getSize(int p){
        return size[find(p)];
    }
}
