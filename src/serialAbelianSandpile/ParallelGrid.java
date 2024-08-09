//Copyright M.M.Kuttel 2024 CSC2002S, UCT
package serialAbelianSandpile;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

//This class is for the grid for the Abelian Sandpile cellular automaton
public class ParallelGrid extends RecursiveAction{
    private static final int THRESHOLD = 100;
    private int rows, columns;
    private int [][] grid; //grid
    private int [][] updateGrid;//grid for next time step

    public ParallelGrid(int w, int h) {
        rows = w+2; //for the "sink" border
        columns = h+2; //for the "sink" border
        grid = new int[this.rows][this.columns];
        updateGrid=new int[this.rows][this.columns];
        /* grid  initialization */
        for(int i=0; i<this.rows; i++ ) {
            for( int j=0; j<this.columns; j++ ) {
                grid[i][j]=0;
                updateGrid[i][j]=0;
            }
        }
    }

    public ParallelGrid(int[][] grid, int[][] updateGrid, int startRow, int endRow) {
        this.grid = grid;
        this.updateGrid = updateGrid;
    }

    public ParallelGrid(int[][] newGrid) {
        this(newGrid.length,newGrid[0].length); //call constructor above
        //don't copy over sink border
        for(int i=1; i<rows-1; i++ ) {
            for( int j=1; j<columns-1; j++ ) {
                this.grid[i][j]=newGrid[i-1][j-1];
            }
        }

    }
    public ParallelGrid(ParallelGrid copyGrid) {
        this(copyGrid.rows,copyGrid.columns); //call constructor above
        /* grid  initialization */
        for(int i=0; i<rows; i++ ) {
            for( int j=0; j<columns; j++ ) {
                this.grid[i][j]=copyGrid.get(i,j);
            }
        }
    }

    public int getRows() {
        return rows-2; //less the sink
    }

    public int getColumns() {
        return columns-2;//less the sink
    }


    int get(int i, int j) {
        return this.grid[i][j];
    }

    void setAll(int value) {
        //borders are always 0
        for( int i = 1; i<rows-1; i++ ) {
            for( int j = 1; j<columns-1; j++ )
                grid[i][j]=value;
        }
    }


    //for the next timestep - copy updateGrid into grid
    public void nextTimeStep() {
        for(int i=1; i<rows-1; i++ ) {
            for( int j=1; j<columns-1; j++ ) {
                this.grid[i][j]=updateGrid[i][j];
            }
        }
        System.out.println("exited nexTimeStep method");
    }

    //key method to calculate the next update grod
    @Override
    protected void compute() { //return boolean
        boolean change=false;
        if (rows < THRESHOLD) {
           update();
        }else
        {
            int mid = this.rows / 2;
            ParallelGrid left = new ParallelGrid(this.grid, this.updateGrid, 0, mid);
            ParallelGrid right = new ParallelGrid(this.grid, this.updateGrid, mid, this.rows);
            left.fork();
            right.compute();
            left.join();

        }
    }

    boolean update() {
        boolean change=false;
        //do not update border
        for( int i = 1; i<rows-1; i++ ) {
            for( int j = 1; j<columns-1; j++ ) {
                updateGrid[i][j] = (grid[i][j] % 4) +
                        (grid[i-1][j] / 4) +
                        grid[i+1][j] / 4 +
                        grid[i][j-1] / 4 +
                        grid[i][j+1] / 4;
                if (grid[i][j]!=updateGrid[i][j]) {
                    change=true;
                }
            }} //end nested for
        if (change) { nextTimeStep();}
        return change;
    }

    boolean update(int[][] updateGrid, int start, int end) {
        boolean change=false;
        //do not update border
        for( int i = start; i<end-1; i++ ) {
            for( int j = 1; j<columns-1; j++ ) {
                updateGrid[i][j] = (grid[i][j] % 4) +
                        (grid[i-1][j] / 4) +
                        grid[i+1][j] / 4 +
                        grid[i][j-1] / 4 +
                        grid[i][j+1] / 4;
                if (grid[i][j]!=updateGrid[i][j]) {
                    change=true;
                }
            }} //end nested for
        if (change) { nextTimeStep();}
        return change;
    }

    //display the grid in text format
    void printGrid( ) {
        int i,j;
        //not border is not printed
        System.out.printf("Grid:\n");
        System.out.printf("+");
        for( j=1; j<columns-1; j++ ) System.out.printf("  --");
        System.out.printf("+\n");
        for( i=1; i<rows-1; i++ ) {
            System.out.printf("|");
            for( j=1; j<columns-1; j++ ) {
                if ( grid[i][j] > 0)
                    System.out.printf("%4d", grid[i][j] );
                else
                    System.out.printf("    ");
            }
            System.out.printf("|\n");
        }
        System.out.printf("+");
        for( j=1; j<columns-1; j++ ) System.out.printf("  --");
        System.out.printf("+\n\n");
    }

    //write grid out as an image
    void gridToImage(String fileName) throws IOException {
        BufferedImage dstImage =
                new BufferedImage(rows, columns, BufferedImage.TYPE_INT_ARGB);
        //integer values from 0 to 255.
        int a=0;
        int g=0;//green
        int b=0;//blue
        int r=0;//red

        for( int i=0; i<rows; i++ ) {
            for( int j=0; j<columns; j++ ) {
                g=0;//green
                b=0;//blue
                r=0;//red

                switch (grid[i][j]) {
                    case 0:
                        break;
                    case 1:
                        g=255;
                        break;
                    case 2:
                        b=255;
                        break;
                    case 3:
                        r = 255;
                        break;
                    default:
                        break;

                }
                // Set destination pixel to mean
                // Re-assemble destination pixel.
                int dpixel = (0xff000000)
                        | (a << 24)
                        | (r << 16)
                        | (g<< 8)
                        | b;
                dstImage.setRGB(i, j, dpixel); //write it out


            }}

        File dstFile = new File(fileName);
        ImageIO.write(dstImage, "png", dstFile);
    }




}
