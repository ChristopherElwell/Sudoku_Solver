package sudoku.yourproject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import au.com.bytecode.opencsv.CSVReader;

public class Sudoku_Control {
    private static int PUZZLES_TO_SOLVE = 100000;
    public static void main(String[] args){
        String[][] data = read_puzzle(PUZZLES_TO_SOLVE);
        int sum = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < PUZZLES_TO_SOLVE; i++) {
            int[][] puzzle = make_puzzle_binary(data[i][0]);
            int[][] actual_solution = make_puzzle_binary(data[i][1]);
            int id = Integer.parseInt(data[i][2]);
            Sudoku solver = new Sudoku(puzzle);
            int[][] solver_solution = solver.run_solver();
            String[] result = check_solution(solver_solution, actual_solution, id);
            sum += Integer.parseInt(result[1]);
            //System.out.println(result[0]);
        }
        long stop = System.currentTimeMillis();
        System.out.println("\n\n\n_______________________________________\n");
        System.out.println("Puzzles Attempted:         | " + PUZZLES_TO_SOLVE);
        System.out.println("Puzzles Solved Correctly:  | " + sum);
        System.out.println("Accuracy:                  | " + String.format("%.0f",((double) sum)/PUZZLES_TO_SOLVE*100) + "%");
        System.out.println("Time Taken:                | " + (stop - start) + " ms");
        System.out.println("Time Per Puzzle:           | " + String.format("%.4f",((double) (stop - start))/(PUZZLES_TO_SOLVE)) + " ms");
        System.out.println("_______________________________________\n\n\n");
    }

    public static String[][] read_puzzle(int num_puzzles){
        String file_name = "sudoku-3m.csv";
        String[][] data = new String[num_puzzles][3];

        try (InputStream is = Sudoku.class.getClassLoader().getResourceAsStream(file_name);
             CSVReader reader = new CSVReader(new InputStreamReader(is))) {

            if (is == null) {
                throw new IllegalArgumentException("file not found! " + file_name);
            }

            String[] nextLine;
            Random random = new Random();
            int rand_start = random.nextInt(3000000);
            int index = 0;
            
            while ((nextLine = reader.readNext()) != null && index < rand_start) {    
                    index++;
            }

            index = 0;

            while ((nextLine = reader.readNext()) != null && index < num_puzzles) { 
                data[index][0] = nextLine[1];
                data[index][1] = nextLine[2];
                data[index][2] = nextLine[0];
                index++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static String[] check_solution(int[][] solver_solution, int[][] actual_solution, int id){
        String complete = "Complete";
        String correct = "Correct";
        String result_id = "1"; 
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (solver_solution[j][i] != 0 && solver_solution[j][i] != actual_solution[j][i]){
                    correct = "Incorrect";
                    result_id = "0";
                }
                if (solver_solution[j][i] == 0){
                    complete = "Incomplete";
                    result_id = "0";
                }
            }
        }
        String[] output = {id + ": " + correct + " and " + complete, result_id};
        return output;
    }

    public static int[][] make_puzzle_binary(String data) {
        
        int[][] output = new int[9][9];
        int i = 0,j = 0;

        for (char num_char : data.toCharArray()){
            if (num_char == '.'){
                output[j][i] = 0;
            } else{
                output[j][i] = (int) Math.pow(2,(num_char - '0') - 1);
            }
            if (++i >= 9){
                i = 0;
                j++;
            }
            if (j >= 9){
                break;
            }
        }    
        return output;
    }

    public static int[][] make_puzzle_decimal(String data) {
        
        int[][] output = new int[9][9];
        int i = 0,j = 0;

        for (char num_char : data.toCharArray()){
            if (num_char == '.'){
                output[j][i] = 0;
            } else{
                output[j][i] = num_char - '0';
            }
            if (++i >= 9){
                i = 0;
                j++;
            }
            if (j >= 9){
                break;
            }
        }    
        return output;
    }

    public static void print_2d_array(int[][] data, int base){
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
