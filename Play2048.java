import java.io.*;
import java.util.*;

class Perceptron {
  private double[] weights;
  private int size;
  private double rate;
  private double bias;

  public Perceptron(int size, double rate) {
    weights = new double[size+1];
    this.size = size;
    for (int i = 0; i < size; i++) {
      weights[i] = 0.0;//Math.random() / 100;
    }
    weights[size] = 0.0;//Math.random() / 100;
    this.rate = 0.05;
    bias = 0.0;
  }

  public double compute(double[] values, int action) {
    double score = bias;
    for (int i = 0; i < size; i++) {
      score += weights[i] * values[i];
    }
    score += weights[size] * 1.0 * action;
 //   System.out.println("score: " + score);
    return score;
  }

  public void train(double[] values, int action, double actual) {
    rate *= 0.95;
    double guess = compute(values, action);
    for (int i = 0; i < size; i++) {
      weights[i] += rate * values[i] * (actual - guess);
    }
    weights[size] += rate * action * (actual - guess);
    bias += (actual - guess);

    System.out.println("actual was: " + actual + ", guess was " + guess + ", now improved to " + compute(values, action));
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
  

class Board implements Comparator<Board> {
  private int[][] cells;
  private int rows;
  private int cols;
  public int score;

  private static int log2(int x) {
    int log = 1;
    while (x > 2) {
      x = x / 2;
      log++;
    }
    return log;
  }

  public int[] getBoardCounts() {
    int[] counts = new int[20];
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        if (cells[r][c] == 0) { counts[0]++; }
        else { counts[Board.log2(cells[r][c])]++; }
      }
    }
    return counts;
  }

  public boolean lessThan(Board rhs) {
    int[] counts1 = getBoardCounts();
    int[] counts2 = rhs.getBoardCounts();

    for (int i = 19; i >= 0; i--) {
      if (counts1[i] < counts2[i]) { return true; }
      else if (counts1[i] > counts2[i]) { return false; }
    }

    return false;
  }

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
    lastBigCombo = 0;
    lastMove = "blank";
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
    this.lastMove = b.lastMove;
    this.lastBigCombo = b.lastBigCombo;
  }

  private double goodness(int x, int y) {
    if (x == 0 && y == 0) { return 1.0; }
    if (x == y) { return 1.0 * x; }//Math.pow(1.5, Math.log(x)); }
    double logx = (x == 0 ? 0.0 : Math.log(x) / Math.log(2));
    double logy = (y == 0 ? 0.0 : Math.log(y) / Math.log(2));
  
    return -Math.pow(Math.abs(logx - logy), 0.65);
    //return -0.25 * (x > y ? x - y : y - x);
    
    //return Math.log(1.0*(x>y?x:y)) * (x > y ? 1.0 * y / x : 1.0 * x / y);
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

  public static Board awesomeBoard() {
    Board b = new Board();
    b.cells[1][0] = 4;
    b.cells[1][1] = 2;
    b.cells[1][2] = 2;
    b.cells[2][0] = 8;
    b.cells[2][1] = 16;
    b.cells[2][2] = 32;
    b.cells[2][3] = 64;
    b.cells[3][0] = 1024;
    b.cells[3][1] = 512;
    b.cells[3][2] = 256;
    b.cells[3][3] = 128;
    return b;
  }


  public double goodness() {
    double good = 0.0;
    double max = 0.0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        if (cells[r][c] > max) { max = cells[r][c]; }
      
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
    
    return good;
  }
    

  public Board minimax(int depth) {
    //System.out.println("find min/max depth " + depth + " of\n" + this.toString());
    if (gameState() != 1) { /*System.out.println("game state is " + gameState());*/ return this; }
    if (depth == 0) { return this; }

    if (depth % 2 == 0) {
      int bestDir = 0;
      Board best = null;
      Board copy;
      for (int i = 0; i < 4; i++) {
        copy = newDirection(i);
        if (!equals(copy)) {
          if (best == null) {    
            bestDir = i;
            best = copy;
          } else {
            Board min = copy.minimax(depth-1);
            if (!min.lessThan(best)) {
              bestDir = i;
              best = min;
            }
          }
        }
      }
     // System.out.println("bestDir: " + bestDir);
      return best;
    } else {
      Board worst = null;
      for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
          if (cells[r][c] == 0) {
            Board cand = copy(r,c,4).minimax(depth-1);
            if (worst == null || cand.lessThan(worst)) {
              worst = cand;
            }
            cand = copy(r,c,2).minimax(depth-1);
            if (worst == null || cand.lessThan(worst)) {
              worst = cand;
            }
          }
        }
      }
      return worst;
    }
  }


  public Vector<Board> nextPlayerMoves() {
    Vector<Board> nextMoves = new Vector<Board>();
    for (int i = 0; i < 4; i++) {
      Board copy = newDirection(i);
      if (!equals(copy)) {
        nextMoves.add(copy);
      }
    }
    Collections.sort(nextMoves, new Board());

    //for (int i = 0; i < nextMoves.size(); i++) {
      //System.out.println("i = " + i + " combo: " + nextMoves.get(i).lastBigCombo);
   // }
    return nextMoves;
  }

  public Vector<Board> nextRandomMoves() {
    Vector<Board> nextMoves = new Vector<Board>();
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        nextMoves.add(copy(r,c,2)); 
        nextMoves.add(copy(r,c,4)); 
      }
    }
    return nextMoves;
  }

  public double corners() {
    double score = 0.0;
    double max = 0.0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        if (cells[r][c] > max) { max = cells[r][c]; }
        double weight = r + c;
        score += weight * cells[r][c];
      }
    }
    return max;
  }

  public double openCells() {
    double count = 0.0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        if (cells[r][c] == 0) { 
          count += 1.0;
        }
      }
    }
    return count;
  }

  public double minmax(int depth, double alpha, double beta, HashMap<String, Double> seenScores) {
    if (gameState() != 1) return gameState() * 1.0;
    if (depth == 0) { return score; }

/*
    String str = toString() + (depth % 2);
    if (seenScores.containsKey(str)) {
      return seenScores.get(str);
    }
*/
    
    if (depth % 2 == 0) {
      for (Board next : nextPlayerMoves()) {
        double score = next.minmax(depth-1, alpha, beta, seenScores);
        alpha = Math.max(alpha, score);
        if (beta <= alpha) {
          break;
        }
      }
      //seenScores.put(str, alpha);
      return alpha;
    } else {
      for (Board next : nextRandomMoves()) {
        double score = next.minmax(depth-1, alpha, beta, seenScores);
        beta = Math.min(beta, score);
        if (beta <= alpha) {
          break;
        }
      }
      //seenScores.put(str, beta);
      return beta;
    }
  }

  public String bestMove() {
    double max = -1000000000.0;
    Board best = this;
    HashMap<String, Double> seenScores = new HashMap<String, Double>();
    for (Board next : nextPlayerMoves()) {
      //double score = next.corners();
      //double score = next.minmax(5, -1000000.0, 1000000, seenScores);
      double score = next.lastBigCombo;
      if (score > max) {
        max = score; 
        best = next;
      }
    }
    return best.lastMove;
  }

  private String lastMove;
  
  private double expectedGoodnessRecursive(int depth) {
    if (gameState() != 1) { return gameState(); }//return 1.0 * goodness(); }//;//gameState(); }
    if (depth == 0) { return 1.0 * goodness(); }
    
    if (depth % 2 == 0) {
      double max = 0.0;
      double g1 = 0.0, g2 = 0.0, g3 = 0.0, g4 = 0.0;
      Board copy;
    
      copy = copyUp();
      if (!equals(copy)) {
        g1 = copy.expectedGoodnessRecursive(depth-1);
      }
      copy = copyDown();
      if (!equals(copy)) {
        g2 = copy.expectedGoodnessRecursive(depth-1);
      }
      copy = copyRight();
      if (!equals(copy)) {
        g3 = copy.expectedGoodnessRecursive(depth-1);
      }
      copy = copyLeft();
      if (!equals(copy)) {
        g4 = copy.expectedGoodnessRecursive(depth-1);
      }
/*
      double g1 = copyUp().expectedGoodnessRecursive(depth-1);
      double g2 = copyDown().expectedGoodnessRecursive(depth-1);
      double g3 = copyLeft().expectedGoodnessRecursive(depth-1);
      double g4 = copyRight().expectedGoodnessRecursive(depth-1);
*/
      
      return Math.max(Math.max(g1, g2), Math.max(g3, g4));
    } else {
      double good = 0.0;
      double min = 1000000.0;
      double score;
      int count = 0;
      double denom = 0.0;
      for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
          if (cells[r][c] == 0) {
            score = copy(r,c,4).expectedGoodnessRecursive(depth-1); 
    //        System.out.println("min cand: "  + score);
            if (score < min) { min = score; }
            score = copy(r,c,2).expectedGoodnessRecursive(depth-1); 
     //       System.out.println("min cand: "  + score);
            if (score < min) { min = score; }
            count++;
          }
        }
      }
      return (count > 0 ? min : 0.0);
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
    return expectedGoodnessRecursive(7);
    //return goodness + Math.random();
  }

  public Board newDirection(int direction) {
    if (direction == 0) { return copyUp(); }
    else if (direction == 1) { return copyDown(); }
    else if (direction == 2) { return copyLeft(); }
    else { return copyRight(); }
  }
    

  public Board copy(int r, int c, int val) {
    Board copy = new Board(this);
    copy.cells[r][c] = val;
    return copy;
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

  public int lastBigCombo;

  public int compare(Board b1, Board b2) {
    if (b1.lastBigCombo == b2.lastBigCombo) { return 0; }
    return (b1.lastBigCombo < b2.lastBigCombo ? 1 : -1);
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
        if (nonZeros[i] > lastBigCombo) { lastBigCombo = nonZeros[i]; }
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
    lastBigCombo = 0;
    for (int r = 0; r < rows; r++) {
      shiftRowLeft(r);
    }
    lastMove = "left";
  }

  public void shiftRight() {
    lastBigCombo = 0;
    for (int r = 0; r < rows; r++) {
      shiftRowRight(r);
    }
    lastMove = "right";
  }

  public void shiftUp() {
    lastBigCombo = 0;
    for (int c = 0; c < cols; c++) {
      shiftColUp(c);
    }
    lastMove = "up";
  }

  public void shiftDown() {
    lastBigCombo = 0;
    for (int c = 0; c < cols; c++) {
      shiftColDown(c);
    }
    lastMove = "down";
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
    System.out.println("score: " + score);
    if (score > best) {
      best = score;
      action = "left";
      actint = 0;
    }
    score = p.compute(values, 1);//
    System.out.println("score: " + score);
    if (score > best) {
      best = score;
      action = "right";
      actint = 1;
    }
    score = p.compute(values, 2);//
    System.out.println("score: " + score);
    if (score > best) {
      best = score;
      action = "up";
      actint = 2;
    }
    score = p.compute(values, 3);//
    System.out.println("score: " + score);
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
    double learn_rate = 0.81;
    double discount = 0.5;
    BoardLearner bl = new BoardLearner();
  
/*
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
        if (as.action.equals("left")) {
          b.shiftLeft();
        } else if (as.action.equals("right")) {
          b.shiftRight();
        } else if (as.action.equals("down")) {
          b.shiftDown();
        } else {
          b.shiftUp();
        }
        int reward = b.score - oldScore;
        System.out.println("reward: " + reward);
  
        b.fillRandomCell();
        
        ActionScore as_prime = Play2048.bestAction(p, b);
        double oldValue = as.score;
        double newValue = oldValue + learn_rate * (reward + discount * as_prime.score - oldValue);
        System.out.println("old -> new: " + oldValue + " -> " + newValue);
        p.train(vals, as.actint, newValue);
        //double oldScore =score(state);
    //map.put(state, oldScore + learn_rate * (reward + discount * est - oldScore));
      }

      System.out.println("Board:\n" + b);
    }
*/
    Board b = new Board();
    b.fillRandomCell();
    b.fillRandomCell();
    b = Board.awesomeBoard();
    //Board minmax = b.minimax(6);
    //System.out.println("best minimax:\n" + minmax);
    while (b.gameState() == 1) {
      System.out.println("Board:\n" + b);
      String bestMove = b.bestMove();
      System.out.println("corner score: " + b.corners());
      System.out.println("Best move: " + bestMove);
      if (bestMove.equals("left")) {
        b.shiftLeft();
      } else if (bestMove.equals("right")) {
        b.shiftRight();
      } else if (bestMove.equals("up")) {
        b.shiftUp();
      } else if (bestMove.equals("down")) {
        b.shiftDown();
      }

 //     b = b.minimax(6);
      b.fillRandomCell();
    }
/*
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
*/
    System.out.println("Final board:\n" + b);
  }
}
