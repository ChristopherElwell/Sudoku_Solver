/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package sudoku.yourproject;

import java.util.ArrayList;

public class Sudoku {
    
    private static final int MAX_ITER = 1000;
    private int[][] possibilities = new int[9][9];
    private int[][] puzzle = new int[9][9];
    private int[][][] groups = new int[27][9][2];
    private static int iteration_count = 0;

    public Sudoku(int[][] puzzle_import) {
        puzzle = puzzle_import;
        iteration_count = 0;
        get_groups();
        get_inital_entries();
    } 

    public void get_groups(){
        int index = 0;
    
        for (int J = 0; J < 3; J++){
            for (int I = 0; I < 3; I++){
                for (int j = 0; j < 3; j++){
                    for (int i = 0; i < 3; i++){
                        int[] point = {i + 3 * I, j + 3 * J};
                        this.groups[index][i + 3 * j] = point;
                    }
                }
                index++;
            }
        }

        for (int i = 0; i < 9; i++){
            for (int j = 0; j < 9; j++){
                int[] point = {i,j};
                this.groups[index][j] = point;
                
            }
            index++;
        }
        for (int j = 0; j < 9; j++){
            for (int i = 0; i < 9; i++){
                int[] point = {i,j};
                this.groups[index][i] = point;
                
            }
            index++;
        }
    }

    public boolean find_missing_nums(){
        boolean cells_added = false;
        for (int[][] group : this.groups){
            int ones = 0;
            int solos = 0;
            for (int i = 0; i < 9; i++){
                ones |= solos & this.possibilities[group[i][1]][group[i][0]];
                solos ^= this.possibilities[group[i][1]][group[i][0]];
            }
            solos &= ~ones;
            for (int[] point : group){
                if ((this.possibilities[point[1]][point[0]] & solos) != 0){
                    this.puzzle[point[1]][point[0]] = this.possibilities[point[1]][point[0]] & solos;
                    this.possibilities[point[1]][point[0]] = 0;
                    for (int[] updated_point : get_affected_cells(point[0], point[1])){
                        get_possible_entries(updated_point[0],updated_point[1]);
                    }
                    cells_added = true;
                }
            }
        }
        return cells_added;
    }

    public int[][] run_solver(){
        iteration_count++;
        
        boolean cells_filled = find_missing_nums();
        if (iteration_count > MAX_ITER){
            System.out.println("MAX_ITER reached: " + iteration_count);
            return puzzle;
        }
        else if(!cells_filled && !check_if_filled(puzzle) && is_puzzle_valid()){
            return brute_force();
        } 
        else if(!cells_filled){
            return puzzle;
        } else{
            return run_solver();
        }
    }

    public boolean is_puzzle_valid(){
        for (int j = 0; j < 9; j++){
            for (int i = 0; i < 9; i++){
                if (this.possibilities[j][i] != 0){
                    return true;
                }
            }
        }
        return false;
    }

    public int[][] brute_force(){
        int[] point = {-1,-1};
        int max_options = 9;
        for (int j = 0; j < 9; j ++){
            for (int i = 0; i < 9; i++){
                if(num_bits(this.possibilities[j][i]) < max_options && num_bits(this.possibilities[j][i]) >= 1){
                    point[0] = i;
                    point[1] = j;
                    max_options = num_bits(this.possibilities[j][i]);
                }
            }
        }

        int[] options = new int[max_options];
        int pair_options_as_byte = this.possibilities[point[1]][point[0]];
        int index = 0;
        for (int i = 0; i < 9; i++) {
            if ((pair_options_as_byte >> i) % 2 == 1 && index < max_options){
                options[index] = 1 << i;
                index++;
            }
        }
        
        int[][][] choices = new int[max_options][9][9];
        for (int j = 0; j < 9; j++){
            for (int i = 0; i < 9; i++){
                for (int[][] choice : choices){
                    choice[j][i] = this.puzzle[j][i];
                }
            }
        }

        for (int i = 0; i < max_options; i++){
            choices[i][point[1]][point[0]] = options[i];            
        }


        for (int[][] choice : choices){
            Sudoku branch = new Sudoku(choice);
            int[][] result = branch.run_solver();
            if (is_correct(result)){
                return result;
            }
        }
        return this.puzzle;
    }

    public boolean is_correct(int[][] puzzle){
        for (int[][] group : this.groups){
            int checker = 0;
            for (int[] point : group){
                if (puzzle[point[1]][point[0]] == 0){
                    return false;
                }
                checker |= puzzle[point[1]][point[0]];
            }
            if (checker != 0b111111111){
                return false;
            }
        }
        return true;
    }

    public void get_inital_entries(){
        for (int j = 0; j < 9; j++){
            for(int i = 0; i < 9; i++){
                get_possible_entries(i, j);
            }
        }
    }

    public boolean check_if_filled(int[][] table){
        for (int[] row : table){
            for (int num : row){
                if (num == 0){
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<int[]> get_affected_cells(int i, int j){
        ArrayList<int[]> cells = new ArrayList<>();
        int box_x = i / 3;
        int box_y = j / 3;
        
        for (int index = 0; index < 9; index++){
            if (index != j){
                int[] point = {i, index};
                cells.add(point);
            }
            if (index != i){
                int[] point = {index, j};
                cells.add(point);
            }
        }
        
        for (int col = 0; col < 3; col++){
            for (int row = 0; row < 3; row++){
                if (col + 3 * box_x != i && row + 3 * box_y != j){
                    int[] point = {col + 3*box_x,row+3*box_y};
                    cells.add(point);
                }
            }
        }
        return cells;
    }

    public void get_possible_entries(int i, int j){
        if (this.puzzle[j][i] == 0){
            int blocked_nums = 0;
            for (int[] point: get_affected_cells(i, j)){
                blocked_nums |= this.puzzle[point[1]][point[0]];
            }
            this.possibilities[j][i] = blocked_nums ^ 0b111111111;
        }else{
            this.possibilities[j][i] = 0;
        }
    }

    public static int num_bits(int num){
        return Integer.bitCount(num);
    }

    public void print_2d_array(int[][] data, int base){
        for (int[] row: data){
            for (int num: row){
                if (base == 2){
                    System.out.print(String.format("%9s", Integer.toBinaryString(num)).replace(' ', '0') + " ");
                }
                else{
                    System.out.print(num+" ");
                }
            }
            System.out.println("");
        }
    }

    public static int[][] convert_solution(int[][] puzzle) {
        int[][] decimal_solution = new int[9][9];
        for (int i = 0; i < 9; i++){
            for (int j = 0; j < 9; j++){
                int n = 0;
                if (puzzle[j][i] == 0){
                    decimal_solution[j][i] = 0;
                } else {
                    while ((puzzle[j][i] >> n) % 2 == 0){
                        n++;
                    }
                    decimal_solution[j][i] = n + 1;
                }
            }
        }
        return decimal_solution;
    }
}