/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author RaghuNandan
 */

public class Cell {
    Queue<Integer> priorityValueQueue;
    int row;    
    int col;    

    public Cell() {
    
    }
    
    
    public Cell(int t_row, int t_col, Queue<Integer> t_priorityValueList) {
        this.row=t_row;
        this.col=t_col;
        this.priorityValueQueue=new LinkedBlockingQueue<>(t_priorityValueList);
    }
           
}
