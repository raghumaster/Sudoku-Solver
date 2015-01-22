package interview.sudokusolver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Java Program that solves a given sudoku puzzle
 * @author RaghuNandan & VenkatRamReddy
 */
public class Sudoku {
    //2D of size 9 x 9 for representing a sudoku puzzle
    private int[][] puzzle;
    
    //Sets for storing all the data related for solving the puzzle   
    private Set<Integer> ucRowSet;
    private Set<Integer> ucColSet;
    private Set<Integer> ucCubeSet;
    private Set<Integer> ucMinimalSet;
    private Set<Integer> completeSet;
    private Map<Integer,Integer> ucMinRankMap;
    
    //Array that keeps track of rank of each cell
    List<Integer> rankList;
    
    //Stack for keeping track of the current Cell
    private Stack<Cell> cellStack;
    //Constructor for initializing the data members
    public Sudoku(){
        //Initialize the puzzle
        puzzle = new int[9][9];
        //Initialize the puzzle with sample data from text file
        initFromFile();
        //Initialize all the instance variables
        ucRowSet  = new HashSet<>();
        ucColSet  = new HashSet<>();
        ucCubeSet  = new HashSet<>();
        ucMinimalSet  = new HashSet<>();
        completeSet  = new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9));
        ucMinRankMap = new LinkedHashMap<>();
        
        rankList = new ArrayList<>();
        
        //Initialize the stack
        cellStack = new Stack<>();
    }
    
    /*
      Main Method that instantiates and calls the member functions
    */
    public static void main(String[] args) {
        Sudoku obj = new Sudoku();
        obj.printPuzzle();
        obj.getUCMinimumPriorityMap(0,8);
        //Solve the given puzzle with Easy solver
        obj.solveEasyPuzzle();
        //If the solution is found then puzzle solved otherwise solve using hard solver
        if(obj.isSolutionFound()){
            System.out.println("Puzzle Solved");          
        }else{
            obj.solveHardPuzzle();
        }
        
    }

    
    /*
      Methods that solves hard puzzle
    */
    private void solveHardPuzzle() {
        assignRanks();
        System.out.println(rankList);
        int t_row,t_col,t_next_value,t_index;
        Cell t_current_cell;
        Queue<Integer> t_priorityValueQueue;
        List<Integer> t_priorityList;
        boolean repeatPop=false;
        for(int i=0;i<rankList.size();i++){
            //find the row and col of the current traversal index
            t_index=rankList.get(i);
            t_row=t_index/9;
            t_col=t_index%9;
            //Priority value queue
            t_priorityList=decideValue(t_row,t_col);
            if(t_priorityList==null){
                //Create an empty priority value queue
                t_priorityValueQueue=new LinkedBlockingQueue<>(); 
            }else{
                t_priorityValueQueue=new LinkedBlockingQueue<>(t_priorityList);
            }
            System.out.println("Currently Traversing..("+t_row+","+t_col+") Queue Values : "+t_priorityValueQueue);
            if(t_priorityValueQueue.size()>0){
                t_next_value=t_priorityValueQueue.remove();
                puzzle[t_row][t_col]=t_next_value;
                cellStack.push(new Cell(t_row,t_col,t_priorityValueQueue));
            }           
            else{
                if(!cellStack.empty()){
                    t_current_cell = cellStack.pop();
                    //Decrement the value of i for pop operation
                    i=i-1;
                    do{
                        t_row = t_current_cell.row;
                        t_col = t_current_cell.col;
                        if(t_current_cell.priorityValueQueue.size()>0){
                            t_next_value=t_current_cell.priorityValueQueue.remove();
                            puzzle[t_row][t_col]=t_next_value;
                            cellStack.push(t_current_cell);
                            repeatPop = false;
                        }
                        else{
                            puzzle[t_row][t_col]=0;
                            t_current_cell = cellStack.pop();
                            i=i-1;
                            repeatPop=true;
                        }
                    }while(repeatPop && !cellStack.empty());
                } 
                else{
                    System.out.println("Stack is empty : 1");
                }
                               
            }
            System.out.println("Stack Size : "+cellStack.size());
            printPuzzle();
        }
    }
    
    /*
    Function that solves easy sudoku puzzle by constantly picking the rank1 and adding them to the puzzle
    */
    private void solveEasyPuzzle() {
      List<Integer> _minList;int solCounter=0;
        do{              
            solCounter=0;
            solCounter = rankOneSolverCertainCells(solCounter);
        }while(solCounter!=0);   
    }

    /*
        Function that identifies all the rank one cells and updates the value to the puzzle
        @return - returns 0 if no rank1 cell is found
                  returns 1 if rank1 cell is found
    */
    private int rankOneSolverCertainCells(int solCounter) {
        List<Integer> _minList;
        label:for(int i=0;i<9;i++){
            for (int j=0;j<9;j++) {
                if(puzzle[i][j]==0){
                    System.out.print("("+i+","+j+") : ");
                    _minList=decideValue(i,j);
                    if(_minList.size()==1){
                        puzzle[i][j]=_minList.get(0);
                        solCounter++;
                        break label;
                    }
                    
                    for(Integer val:_minList){
                        System.out.print(val+" ");
                    }
                    System.out.println("");
                }
            }
        }
        return solCounter;
    }

    /*
        Assigns ranks to cells based on the size of PriorityList
    */
    private void assignRanks() {
        //Code for assigning ranks to each cell
        List<Integer> _minList;
        int[] rankArray = new int[81];
        for(int i=0;i<9;i++){
            for (int j=0;j<9;j++) {
                if(puzzle[i][j]==0){
                    _minList = decideValue(i,j);
                    rankArray[9*i+j]= _minList.size();
                }else{
                    rankArray[9*i+j]=0;
                }
            }
        }
        for(int rank=2;rank<9;rank++){
            for(int index=0;index<81;index++){
                if(rankArray[index]==rank){
                    rankList.add(index);
                }
            }
        }
    } 
    
    /*
     **** FURTHER DECISION FUNCTION ****
     Private method that assigns priority to minimal set by making further decision
     @param row : row index of the 2D Array
     @param col : column index of the 2D Array
     @return null : If two values in the map have rank = 1 => backtrack
             int[] of size 1 if only single value is allowed => decision is made
             int[] of size > 1 and the values sorted based on the rank; => further decision to be made
    */
    private List<Integer> decideValue(int row,int col){
        //Extract the minimumPriorityMap of the given row and col
        //MinPriorityMap contains of value-rank pairs for a given cell
        getUCMinimumPriorityMap(row,col);
        //Extract the map size
        int size = ucMinRankMap.size();
        //Create a new ArrayList
        List<Integer> priorityValueList = new ArrayList<>();
        //If the size of ucMinRankMap is 1 and it to the array and return   
        //Condition#0 : Base condition
            if(size==0){
                return null;
            }
        //Condition#1 : Base condition
            if(size==1){
                priorityValueList.add(ucMinRankMap.keySet().iterator().next());
                return priorityValueList;
            }
        //Condition#2 : Check if the map contains atleast one element with rank1
            int rankOneCount=0,valKey=0;
            //Count number of rank 1 values in the map
            for(Integer key:ucMinRankMap.keySet()){
                if(ucMinRankMap.get(key)==1){
                   rankOneCount++;
                   valKey = key;
            }}
            //Condition#2a : If the map contains exactly one element then add to the list and return
            //SUCCESS
            //Found the required value to be placed in the cell
            if(rankOneCount==1){
              priorityValueList.add(valKey);
              return priorityValueList;
            }
            //Condition#2b : If the map contains more than one value with rank 1
            //****FAILURE 
            //Do a backtrack
            if(rankOneCount>1){
                return null;
            }
        //Condition#3 : Sort the values in the map based on the rank and append them to the list
            //If the map has multiple values with different ranks
            //Sort the keys based on values and return the array
            int helperValue=0;
            for(int i=0;i<size;i++){
                int minRank=999999;
                for(Integer key:ucMinRankMap.keySet()){
                    if(ucMinRankMap.get(key)<minRank){
                        minRank=ucMinRankMap.get(key);
                        helperValue=key;
                    }
                }
                priorityValueList.add(helperValue);
                ucMinRankMap.remove(helperValue);
            }
            return priorityValueList;
    }
    
    
    /*
     **** FURTHER DECISION FUNCTION ****
     Private method that assigns priority to minimal set by making further decision
     @param row : row index of the 2D Array
     @param col : column index of the 2D Array
    */
    private void getUCMinimumPriorityMap(int row,int col){
        //Compute the minimal set for the given row and col
        getUCMinimalSet(row, col);
        //Clear the rankMap
        ucMinRankMap.clear();
        //If the size of minimal set is 1 then no further decision is required
        //BASE CONDITION 1
        if(ucMinimalSet.size()==1){
            //Add the single element to ucMinRankMap and then return
            ucMinRankMap.put(ucMinimalSet.iterator().next(),1);
            return;
        }
        //BASE CONDITION 2
        if(ucMinimalSet.size()==0){
            // ? Need to Verfiy this case **********
            return;
        }
        //If the minimal Set size is greater than 1 then priortize the list based on further decision
        //For every element in the minimal set
        for(Integer value:ucMinimalSet){
            //Counter variables for counting the priority
            int _rowCount=0,_colCount=0,_cubeCount=0,_rank,_cubeIndex,_cubeStartRow,_cubeStartCol;
            //further optimize rowSet by checking the columns from col to 8 => col and cube sets
            for (int j = 0; j < 9; j++) {
                if(puzzle[row][j]==0){
                    ucColSet.clear();
                    ucCubeSet.clear();
                    getUCColSet(row,j);
                    getUCCubeSet(row,j);
                    ucColSet.retainAll(ucCubeSet);
                    if(ucColSet.contains(value)){
                        _rowCount++;
                    }
                }
            }
            //further optimize colSet by checking the columns from col to 8 => col and cube sets
            for (int i = 0; i < 9; i++) {
                if(puzzle[i][col]==0){
                    ucRowSet.clear();
                    ucCubeSet.clear();
                    getUCRowSet(i,col);
                    getUCCubeSet(i,col);
                    ucRowSet.retainAll(ucCubeSet);
                    if(ucRowSet.contains(value)){
                        _colCount++;
                    }
                }
            }
            _cubeIndex = 3*(row/3)+(col/3);
            _cubeStartRow = 3*(_cubeIndex/3);
            _cubeStartCol = 3*(_cubeIndex%3);
            //further optimize cubeSet by checking the columns from col to 8 => col and cube sets
            for (int i = _cubeStartRow; i < _cubeStartRow+3; i++) 
            {
                for (int j = _cubeStartCol; j < _cubeStartCol+3; j++) 
                {
                    if(puzzle[i][j]==0){
                        ucColSet.clear();
                        ucRowSet.clear();
                        getUCColSet(i,j);
                        getUCRowSet(i,j);
                        ucColSet.retainAll(ucRowSet);
                        if(ucColSet.contains(value)){
                            _cubeCount++;
                        } 
                    }              
                }
            }
//            System.out.println("Min("+_rowCount+","+_colCount+","+_cubeCount+")");
            _rank = Math.min(_rowCount,Math.min(_colCount, _cubeCount));
//            System.out.println(value+" : "+_rank);
            ucMinRankMap.put(value,_rank);
        }//End of foreach loop
    }
    
    /*
     Private method that finds the intersection of ucRowSet,ucColSet and ucCubeSet
     @param row : row index of the 2D Array
     @param col : column index of the 2D Array
    */
    private void getUCMinimalSet(int row,int col){
       //Get the uncovered row,column and cube sets 
       ucMinimalSet.clear();
       getUCRowSet(row,col); 
       getUCColSet(row,col); 
       getUCCubeSet(row,col); 
       ucRowSet.retainAll(ucColSet);
       ucRowSet.retainAll(ucCubeSet);
       ucMinimalSet.addAll(ucRowSet);
    }
    
    /*
     Private method that updates the uncovered row set for a given cell
     @param row : row index of the 2D Array
     @param col : column index of the 2D Array
    */
    private void getUCRowSet(int row,int col){
        ucRowSet.addAll(completeSet);
        for(int j=0;j<9;j++){
            //Add the which are uncovered to the global row set
            if(puzzle[row][j]!=0){
                ucRowSet.remove(puzzle[row][j]);
            }
        }
    }
    
    /*
     Private method that updates the uncovered values in the given column for a given cell
     @param row : row index of the 2D Array
     @param col : column index of the 2D Array
    */
    private void getUCColSet(int row,int col){
        ucColSet.addAll(completeSet);
        for(int i=0;i<9;i++){
            if(puzzle[i][col]!=0){
                ucColSet.remove(puzzle[i][col]);
            }
        }
    }
    
    /*
     Private method that updates the uncovered values in the given cube the cell belongs
     @param row : row index of the 2D Array
     @param col : column index of the 2D Array
    */
    private void getUCCubeSet(int row,int col){
        ucCubeSet.addAll(completeSet);
        //Indentify the index of the cube to which the cell belongs
        /*
         The total puzzle can be divided into the total of 9 cubes each containing 9 cells
            0   1   2
            3   4   5
            6   7   8
        A given cell (4,5) belongs to 3*(row/3)+(col/3) => 3*1 + 1 = 4 => belongs to cube 4
        */
        //**** KOTI's Formula ****
        int _cubeIndex = 3*(row/3)+(col/3);
        //Identify the starting cell of this cube
        /*
            (4,5) => The starting row = 3*(_cubeIndex/3) = 3
                     The starting col = 3*(_cubeIndex%3) = 3
        */
        int _startRow = 3*(_cubeIndex/3);
        int _startCol = 3*(_cubeIndex%3);
        for(int i=_startRow;i<_startRow+3;i++){
            for (int j = _startCol; j < _startCol+3; j++) {
                if(puzzle[i][j]!=0){
                    ucCubeSet.remove(puzzle[i][j]);
                }               
            }
        }
    }

    /*
        Function that initializes the 2D puzzle from an input file
    */
    private void initFromFile() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("sampleSudoku.txt"));
            String str;StringTokenizer tokenize; int j,i=0;
            while((str=br.readLine())!=null){
                tokenize = new StringTokenizer(str," ");
                j=0;
                while(tokenize.hasMoreTokens()){
                    puzzle[i][j++] = Integer.parseInt(tokenize.nextToken());
                }
                i++;
            }
        } catch (Exception e) {
        }
    }
    
    /*
       Functions that prints the contents of the puzzle
    */
    private void printPuzzle() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.print(puzzle[i][j]+" ");
            }
            System.out.println("");
        }
    }
    
    /*
        Method that checks the puzzle if the solution is found
        Tester method that tests the current sudoku Puzzle
    */
    private boolean isSolutionFound(){
        //Check the rowSum
        int rowSum=0,colSum=0,cubeSum=0;
        for (int i = 0; i < 9; i++) {
            rowSum=0;
            for (int j = 0; j < 9; j++) {
                if(puzzle[i][j]==0)return false;
                else rowSum+=puzzle[i][j];
            }
            if(rowSum!=36){
                return false;
            }
        }
        //Check the rowSum
        for (int j = 0; j < 9; j++) {
            colSum=0;
            for (int i = 0; i < 9; i++) {
                if(puzzle[i][j]==0)return false;
                else colSum+=puzzle[i][j];
            }
            if(colSum!=36){
                return false;
            }
        }
        //Check the cubeSum
        for (int row = 0; row < 9; row+=3) {
            for (int col = 0; col < 9; col+=3) {
                cubeSum=0;
                int _cubeIndex = 3*(row/3)+(col/3);
                int _startRow = 3*(_cubeIndex/3);
                int _startCol = 3*(_cubeIndex%3);
                for(int i=_startRow;i<_startRow+3;i++){
                    for (int j = _startCol; j < _startCol+3; j++) {
                        cubeSum+=puzzle[_startRow][_startCol];              
                    }
                }
                if(cubeSum!=36){
                    return false;
                }
            }
        }
        return true;
    }
    
}
