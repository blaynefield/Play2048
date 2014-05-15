import java.io.*;
import java.util.*;

class Perceptron {
  private double[] weights;
  private int size;
  private double rate;

  public Perceptron(int size, double rate) {
    weights = new double[size+1];
    this.size = size;
    for (int i = 0; i < size; i++) {
      weights[i] = Math.random() / 100;
    }
    weights[size] = Math.random() / 100;
    this.rate = 0.05;
  }

  public double compute(double[] values, int action) {
    double score = 0.0;
//    System.out.println("weights: " + weights);
    for (int i = 0; i < size; i++) {
      score += weights[i] * values[i];
    }
    score += weights[size] * 1.0 * action;
 //   System.out.println("score: " + score);
    return score;
  }

  public void train(double[] values, int action, double actual) {
    rate *= 0.99;
    double guess = compute(values, action);
    for (int i = 0; i < size; i++) {
      weights[i] += rate * values[i] * (actual - guess);
    }
    weights[size] += rate * action * (actual - guess);
  }
}
    
    

class ScoreMove {
  private double score;
  private String move;
  public ScoreMove(double score, String move) {}
}

class BoardLearner {
  private HashMap<String, Double> map;
  private double learn_rate = 0.1;
  private double discount = 0.5;

  public BoardLearner() {
    map = new HashMap<String,Double>();
  }


  public double score(String str) {
    if (map.containsKey(str)) {
//      System.out.println(str + "->" + map.get(str));
      return map.get(str);
    } else {
      return Math.random() * 0.1;
    }
  }

  public void update(String state, double reward, double est) {
    double oldScore =score(state);
    map.put(state, oldScore + learn_rate * (reward + discount * est - oldScore));
  }


}
  

class Board {
  private int[][] cells;
  private int rows;
  private int cols;
  public int score;

  public double[] getBoardValues() {
    double[] values = new double[rows*cols];
    int j = 0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        values[j++] = 1.0 * cells[r][c];
      }
    }

    return values;
  }

  public Board() {
    rows = 4;
    cols = 4;
    score = 0;
    cells = new int[rows][cols];
  }

  public Board(Board b) {
    this.rows = b.rows;
    this.cols = b.cols;
    this.cells = new int[rows][cols];
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        cells[r][c] = b.cells[r][c];
      }
    }
    this.score = b.score;
  }

  private double goodness(int x, int y) {
    if (x == 0 || y == 0) {
      return 1.0;
    }
    
    return Math.log(1.0*(x>y?x:y)) * (x > y ? 1.0 * y / x : 1.0 * x / y);
  }

  public int side_side() {
    int count = 0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols - 1; c++) {
        if (cells[r][c] == cells[r][c+1]) {
          count++;
        }
      }
    }
    return count;
  }

  public int up_down() {
    int count = 0;
    for (int c = 0; c < cols; c++) {
      for (int r = 0; r < rows-1; r++) {
        if (cells[r][c] == cells[r+1][c]) {
          count++;
        }
      }
    }
    return count;
  }

  public String state() {
    return side_side() + ":" + up_down();
  }
  

  public double goodness() {
    double good = 0.0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        if (c < cols-1) {
          good += goodness(cells[r][c], cells[r][c+1]);
        }
        if (r < rows-1) {
          good += goodness(cells[r][c], cells[r+1][c]);
        }
        if (c > 0) {
          good += goodness(cells[r][c], cells[r][c-1]);
        }
        if (r > 0) {
          good += goodness(cells[r][c], cells[r-1][c]);
        }
        if (cells[r][c] == 0) {
          good += 1.0;
        }
      }
    }
    
    
  //  return 1.0 * score;
    //return gameState();
    return good;

    //return 1.0 * gameState() * good;
  }

  
  
  private double expectedGoodnessRecursive(int depth) {
    if (gameState() != 1) { return 1.0 * score; }//;//gameState(); }
    if (depth == 0) { return 1.0 * score; }
    
    if (depth % 2 == 0) {
      double g1 = copyUp().expectedGoodnessRecursive(depth-1);
      double g2 = copyDown().expectedGoodnessRecursive(depth-1);
      double g3 = copyLeft().expectedGoodnessRecursive(depth-1);
      double g4 = copyRight().expectedGoodnessRecursive(depth-1);
      
      return Math.max(Math.max(g1, g2), Math.max(g3, g4));
    } else {
      double good = 0.0;
      double min = 1000000.0;
      double score;
      int count = 0;
      for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
          if (cells[r][c] == 0) {
            count++;
            cells[r][c] = 4;
            score = expectedGoodnessRecursive(depth-1); 
            good += 0.1 * score;
            if (score < min) { min = score; }
            cells[r][c] = 2;
            score = expectedGoodnessRecursive(depth-1); 
            good += 0.9 * score;
            if (score < min) { min = score; }
            cells[r][c] = 0;
          }
        }
      }
      if (count == 0) { return 0.0; }
      else { return good / count; }
    }
  }
          
   

  public double expectedGoodness() {
/*
    double goodness = 0.0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        if (cells[r][c] == 0) {
          cells[r][c] = 2;
          goodness += 0.1 * goodness() * gameState();
          cells[r][c] = 1;
          goodness += 0.9 * goodness() * gameState();
          cells[r][c] = 0;
        }
      }
    }
*/
    return expectedGoodnessRecursive(6);
    //return goodness + Math.random();
  }
        

  public int badness() {
    int bad = 0;
    for (int r = 0; r < rows - 1; r++) {
      for (int c = 0; c < cols - 1; c++) {
        bad += Math.abs(cells[r][c] - cells[r][c+1]);
        bad += Math.abs(cells[r][c] - cells[r+1][c]);
      }
    }
    return bad;
  }

  public double expectedBadness() {
    double bad = 0.0;
    for (int r = 0; r < rows - 1; r++) {
      for (int c = 0; c < cols - 1; c++) {
        if (cells[r][c] == 0) {
          cells[r][c] = 4;
          bad += 0.1 * badness();
          cells[r][c] = 2;
          bad += 0.9 * badness();
          cells[r][c] = 0;
        }
      }
    }
    return bad;
  }

  private int[] collapse(int[] x) {
    int numNonZeros = 0;
    int[] nonZeros = new int[x.length];

    for (int i = 0; i < x.length; i++) {
      if (x[i] > 0) {
        nonZeros[numNonZeros++] = x[i];
      }
    }

    int out = 0;
    int[] result = new int[x.length];
    for (int i = 0; i < numNonZeros; ) {
      if (i < numNonZeros - 1 && (nonZeros[i] == nonZeros[i+1])) {
        result[out++] = nonZeros[i]*2;
        score += nonZeros[i]*2;
        i += 2;
      } else {
        result[out++] = nonZeros[i];
        i += 1;
      }
    }
    return result;
  }

  private void reverseRow(int row) {
    for (int c = 0; c < cols / 2; c++) {
      int tmp = cells[row][c];
      cells[row][c] = cells[row][cols-c-1];
      cells[row][cols-c-1] = tmp;
    }
  }

  private void reverseCol(int col) {
    for (int r = 0; r < rows / 2; r++) {
      int tmp = cells[r][col];
      cells[r][col] = cells[rows-r-1][col];
      cells[rows-r-1][col] = tmp;
    }
  }

  private int[] getRow(int r) {
    int[] row = new int[cols];
    for (int c = 0; c < cols; c++) {
      row[c] = cells[r][c];
    }
    return row;
  }
    
  private void setRow(int r, int[] row) {
    for (int c = 0; c < cols; c++) {
      cells[r][c] = row[c];
    }
  }

  private int[] getCol(int c) {
    int[] col = new int[rows];
    for (int r = 0; r < rows; r++) {
      col[r] = cells[r][c];
    }
    return col;
  }
    
  private void setCol(int c, int[] col) {
    for (int r = 0; r < rows; r++) {
      cells[r][c] = col[r];
    }
  }

  private void shiftRowLeft(int r) {
    setRow(r, collapse(getRow(r)));
  }

  private void shiftRowRight(int r) {
    reverseRow(r);
    setRow(r, collapse(getRow(r)));
    reverseRow(r);
  }

  private void shiftColUp(int c) {
    setCol(c, collapse(getCol(c)));
  }

  private void shiftColDown(int c) {
    reverseCol(c);
    setCol(c, collapse(getCol(c)));
    reverseCol(c);
  }

  public boolean equals(Board b) {
    if (b.rows != rows) {
      return false;
    }
    if (b.cols != cols) {
      return false;
    }
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        if (b.cells[r][c] != cells[r][c]) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean matches(int[] x, int[] y) {
    if (x.length != y.length) {
      return false;
    }
    for (int i = 0; i < x.length; i++) {
      if (x[i] != y[i]) {
        return false;
      }
    }
    return true;
  }
    
  public String toString() {
    String str = "";
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        str += "\t" + cells[r][c];
      }
      str += "\n";
    }
    return str;
  }
  
  public void shiftLeft() {
    for (int r = 0; r < rows; r++) {
      shiftRowLeft(r);
    }
  }

  public void shiftRight() {
    for (int r = 0; r < rows; r++) {
      shiftRowRight(r);
    }
  }

  public void shiftUp() {
    for (int c = 0; c < cols; c++) {
      shiftColUp(c);
    }
  }

  public void shiftDown() {
    for (int c = 0; c < cols; c++) {
      shiftColDown(c);
    }
  }
    
  public Board copyLeft() {
    Board b = new Board(this);
    b.shiftLeft();
    return b;
  }

  public Board copyRight() {
    Board b = new Board(this);
    b.shiftRight();
    return b;
  }

  public Board copyUp() {
    Board b = new Board(this);
    b.shiftUp();
    return b;
  }
  
  public Board copyDown() {
    Board b = new Board(this);
    b.shiftDown();
    return b;
  }

  public void setCell(int r, int c, int val) {
    cells[r][c] = val;
  }

  public int gameState() {
    int state = -200000;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        if (cells[r][c] == 2048) { return 200000; }
        else if (cells[r][c] == 0) { state = 1; }
        else if (r < rows-1 && cells[r][c] == cells[r+1][c]) { state = 1; }
        else if (c < cols-1 && cells[r][c] == cells[r][c+1]) { state = 1; }
      }
    }
    return state;
  }

  public void fillRandomCell() {
    int value = Math.random() > 0.1 ? 2 : 4;
    int zeros = 0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        if (cells[r][c] == 0) {
          zeros++;
        }
      }
    }
    //System.out.println("zeros: " + zeros

    int stop = (int) (Math.random() * zeros);
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        if (cells[r][c] == 0) {
          if (stop == 0) {
            cells[r][c] = value;
            return;
          } else {
            stop--;
          }
        }
      }
    }
  }
}

class ActionScore {
  public String action;
  public double score;
  public int actint;
  public ActionScore(String action, double score) {
    this.action = action;
    this.score = score;
  }
  public ActionScore(String action, double score, int actint) {
    this.action = action;
    this.score = score;
    this.actint = actint;
  }
}


public class Play2048 {
  public static ActionScore bestAction(Perceptron p, Board b) {
    double best = 0.0;
    double reward;
    String action = "left";
    double[] values = b.getBoardValues();
    double score;
    int actint = 0;

    score = p.compute(values, 0);//
    //System.out.println("score: " + score);
    if (score > best) {
      best = score;
      action = "left";
      actint = 0;
    }
    score = p.compute(values, 1);//
    //System.out.println("score: " + score);
    if (score > best) {
      best = score;
      action = "right";
      actint = 1;
    }
    score = p.compute(values, 2);//
    //System.out.println("score: " + score);
    if (score > best) {
      best = score;
      action = "up";
      actint = 2;
    }
    score = p.compute(values, 3);//
    //System.out.println("score: " + score);
    if (score > best) {
      best = score;
      action = "down";
      actint = 3;
    }
    return new ActionScore(action, best, actint);
  }
    
  public static ActionScore bestAction(BoardLearner bl, String stateString) {
    double best = 0.0;
    double reward;
    String action = "left";

    double score = bl.score(stateString + ":left");
    if (score > best) {
      best = score;
      action = "left";
    }
    score = bl.score(stateString + ":right");
    if (score > best) {
      best = score;
      action = "right";
    }
    score = bl.score(stateString + ":up");
    if (score > best) {
      best = score;
      action = "up";
    }
    score = bl.score(stateString + ":down");
    if (score > best) {
      best = score;
      action = "down";
    }
    return new ActionScore(action, best);
  }

  public static void main(String[] args) {
    double learn_rate = 0.1;
    double discount = 0.5;
    BoardLearner bl = new BoardLearner();
  
    Perceptron p = new Perceptron(16, 0.1);
    for (int j = 0; j < 100; j++) {
      Board b = new Board();
      b.fillRandomCell();
      b.fillRandomCell();
      int i = 0;
      while (i++ < 5000 && b.gameState() == 1) {
        ActionScore as = Play2048.bestAction(p, b);
        double[] vals = b.getBoardValues();

        //System.out.println("actint: " + as.actint);
  
        double rand = Math.random();
        int oldScore = b.score;
        if (rand < 0.01 || as.action.equals("left")) {
          b.shiftLeft();
        } else if (rand < 0.02 || as.action.equals("right")) {
          b.shiftRight();
        } else if (rand < 0.03 || as.action.equals("down")) {
          b.shiftDown();
        } else {
          b.shiftUp();
        }
        int reward = b.score - oldScore;
    //    System.out.println("reward: " + reward);
  
        b.fillRandomCell();
        
        ActionScore as_prime = Play2048.bestAction(p, b);
        double oldValue = as.score;
        double newValue = oldValue + learn_rate * (reward + discount * as_prime.score - oldValue);
     //   System.out.println("old -> new: " + oldValue + " -> " + newValue);
        p.train(vals, as.actint, newValue);
        //double oldScore =score(state);
    //map.put(state, oldScore + learn_rate * (reward + discount * est - oldScore));
      }

      System.out.println("Board:\n" + b);
    }
/*
    Board b = new Board();
    b.fillRandomCell();
    b.fillRandomCell();
    while (b.gameState() == 1) {
      System.out.println("Board:\n" + b);
      System.out.println("goodness of board: " + b.goodness());
      System.out.println("expected goodness of board: " + b.expectedGoodness());
    
      Board left = b.copyLeft();
      double bestScore = left.expectedGoodness();
      double score;
      String dir = "left";
      Board best = left;
      System.out.println("left score: " + bestScore);
      if (b.equals(left)) {
        bestScore = -100000000;
      }
      
      Board right = b.copyRight();
      score = right.expectedGoodness();
      System.out.println("right score: " + score);
      if (!b.equals(right) && score > bestScore) {
        best = right;
        bestScore = score;
        dir = "right";
      }

      Board up = b.copyUp();
      score = up.expectedGoodness();
      System.out.println("up score: " + score);
      if (!b.equals(up) && score > bestScore) {
        best = up;
        bestScore = score;
        dir = "up";
      }

      Board down = b.copyDown();
      score = down.expectedGoodness();
      System.out.println("down score: " + score);
      if (!b.equals(down) && score > bestScore) {
        best = down;
        bestScore = score;
        dir = "down";
      }

      System.out.println("direction: " + dir);
      b = best;
      b.fillRandomCell();
//      state = b.gameState();
    }
    System.out.println("Final board:\n" + b);
*/
  }
}
