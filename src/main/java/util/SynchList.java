package main.java.util;

import java.util.ArrayList;

public class SynchList<T>
{
    private ArrayList<T> list;
    private int id = 0;

    public SynchList(ArrayList<T> list)
    {
        System.out.println("synch constr: " + list.size());
        this.list = list;
    }

    public synchronized T getNext()
    {
        if (id == list.size())
            return null;

        return list.get(id++);
    }
}
