import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
class Player6{
	int n, p;
	char[][] board;
	double timeLeft;
	int numRemaining;
	double fractionFilled;
	int maxDepth;
	long cutoffTime;
	long startTime;
	double factor;
	int regionCutoff;
	int lowerLevelCutoff;
	double lookUpTable[][][] = { {{0.02, 0.038, 0.036, 0.048, 0.069, 0.253, 0.418, 0.722 }, {0.025, 0.039, 0.12, 0.2, 0.56, 0.63, 1.4, 2.6}, {0.03, 0.038, 0.1, 0.23, 0.62, 1.0, 2.64, 6.05}},
			{{0.054, 0.11, 0.13, 0.26, 0.56, 0.78 }, {0.058, 0.13, 0.44, 0.8, 3.0, 8.5}, {0.06, 0.12, 0.5, 0.77, 4.78, 14.57}},
			{{0.09, 0.17, 0.58, 0.90 }, {0.15, 0.22, 1.31, 3.42}, {0.11, 0.28, 1.40, 3.70}},
			{{0.10, 0.22, 1.00, 3.99 }, {0.20, 0.43, 3.10, 10.50}, {0.20, 0.45, 4.05, 12.9}},
			{{0.28, 0.40, 1.70, 4.8 }, {0.35, 0.55, 4.3, 9.5}, {0.34, 0.58, 5.25, 11.00}},
			{{0.32, 0.54, 4.3, 11.2 }, {0.5, 0.9, 11.12, 27.6}, {0.66, 1.04, 16.6, 36.5}},
			{{0.3, 0.60, 4.96, 12.68 }, {0.63, 1.20, 17.50, 40}, {0.68, 1.3, 21, 47}},
			{{0.35, 0.67, 6.5, 17 }, {0.7, 1.6, 25.2, 50}, {0.8, 1.6, 27, 61}},
			{{0.45, 0.9, 10.3 }, {0.9, 1.7, 30}, {1.0, 2.3, 35}},
			{{0.39, 0.7, 4.1 }, {0.48, 0.9, 7.3}, {0.5, 1.0, 8.2}},
			{{0.4, 1.0, 6.2 }, {0.6, 1.2, 10.4}, {0.75, 1.25, 11.5}},
	};
	class Region{
		Node node;
		int row;
		int col;
		int score;
		Region(Node node, int row, int col, int score){
			this.node = node;
			this.row = row;
			this.col = col;
			this.score = score;
		}
	}
	class IncComparator implements Comparator<Region>{

		@Override
		public int compare(Region arg0, Region arg1) {
			return arg0.score - arg1.score;
		}
		
	}
	class DecComparator implements Comparator<Region>{

		@Override
		public int compare(Region arg0, Region arg1) {
			return arg1.score - arg0.score;
		}
		
	}
	class Node{
		char[][] board;
		int[] scores;
		Node(char[][] board, int[] scores){
			this.board = board;
			this.scores = scores;
		}
		int computeScore(int row, int col, char[][] temp, char symbol, char[][] intermedBoard){
			if(row == n || col == n || row == -1 || col == -1 || temp[row][col] != symbol)
				return 0;
			temp[row][col] = '*';
			intermedBoard[row][col] = '*';
			return (1 + computeScore(row, col+1, temp, symbol, intermedBoard) + computeScore(row+1, col, temp, symbol, intermedBoard) + computeScore(row-1, col, temp, symbol, intermedBoard) + computeScore(row, col-1, temp, symbol, intermedBoard));
		}
		Node successorFunction(int row, int col, int count, char[][] intermedBoard){
			char[][] temp = deepCopy(board);
			int score = computeScore(row, col, temp, temp[row][col], intermedBoard);
			applyGravity(temp);
			int[] childScores = scores.clone();
			childScores[count%2] +=score*score;
			Node child = new Node(temp, childScores);
//			child.display();
			return child;
		}
		Region[] allSuccessors(int count, char[][] intermedBoard){
			ArrayList<Region> reg = new ArrayList<>();
//			int k = 0;
//			outer:
			for(int i=0; i<n ; ++i)
				for(int j=0; j<n; ++j)
					if(intermedBoard[i][j]!='*'){
//						System.out.println(i+":"+j);
						Node child = successorFunction(i, j, count, intermedBoard);
						int score = (child.scores[0] - child.scores[1]) - (this.scores[0] - this.scores[1]);
						reg.add(new Region(child, i, j, score));
//						k++;
//						if(k > 3*numRemaining/4)
//							break outer;
					}
			if(reg.size() == 0)
				return null;
			if(count%2 == 0)
				Collections.sort(reg, new DecComparator());
			else
				Collections.sort(reg, new IncComparator());
			Region[] regs = reg.toArray(new Region[reg.size()]);
			return regs;
		}
		Node successorFunction(int row, int col, int count){
			char[][] temp = deepCopy(this.board);
			int score = computeScore(row, col, temp, temp[row][col]);
			applyGravity(temp);
			int[] childScores = scores.clone();
			childScores[count%2] +=score*score;
			Node child = new Node(temp, childScores);
			return child;
		}
		int computeScore(int row, int col, char[][] temp, char symbol){
			if(row == n || col == n || row == -1 || col == -1 || temp[row][col] != symbol)
				return 0;
			temp[row][col] = '*';
			return (1 + computeScore(row, col+1, temp, symbol) + computeScore(row+1, col, temp, symbol) + computeScore(row-1, col, temp, symbol) + computeScore(row, col-1, temp, symbol));
		}
		void applyGravity(char[][] temp){
			for(int j=0; j<n; ++j){
				int i=n-1;
				int k=i;
				while(k>=0){
					while(k>=0 && temp[k][j]=='*')
						k--;
					if(k<0)
						break;
					temp[i][j] = temp[k][j];
					i--;
					k--;
				}
				while(i>=0){
					temp[i][j]='*';
					i--;
				}
			}
		}
		int computeMinimax(){
			int count=0;
			int minimaxNode = minimax(this, count, Integer.MIN_VALUE, Integer.MAX_VALUE);
			return minimaxNode;
		}
		int minimax(Node node, int count, int alpha, int beta){
			if((count==maxDepth || System.nanoTime() > cutoffTime)){
				return (node.scores[0] - node.scores[1]);
			}
//			boolean flag = false;
			char[][] intermedBoard = deepCopy(node.board);
			if(count==0){
				Region[] children = node.allSuccessors(count, intermedBoard); 
				int limit;
				if(children == null)
					return (node.scores[0] - node.scores[1]);
				if(children.length <= (int)(factor*numRemaining) || children.length <= regionCutoff)
					limit = children.length;
				else
					limit = (int)(factor*numRemaining);
				double timeIncr;
				if(children.length > 1)
					timeIncr = timeLeft/(0.9*children.length/2);
				else
					timeIncr = timeLeft;
				maxDepth = lookUp(timeIncr);
				cutoffTime = startTime + (long)(0.99*timeIncr*Math.pow(10, 9));
//				System.out.println("Approx number of plys at level "+count+" - "+children.length);
//				System.out.println("Updated allotted Time - "+timeIncr);
//				System.out.println("Estimated time allotted - "+(timeLeft/Math.sqrt(children.length/2)));
				int k=children[0].row, l=children[0].col;
				int max = minimax(children[0].node, count+1, alpha, beta);
				for(int i=1; i<limit; ++i){
					int val = minimax(children[i].node, count+1, alpha, beta);
					if( val > max ){
						max = val;
						k = children[i].row;
						l = children[i].col;
					}
					if(max >= beta)
						return k*n+l;
					alpha = Math.max(alpha, max);
				}
//				System.out.println("Minimax is "+max+" move is "+k+":"+l);
				return k*n+l;
			}
			else if(count%2 == 0){
				int max = Integer.MIN_VALUE;
				int limit;
				Region[] children = node.allSuccessors(count, intermedBoard);
				if(children == null)
					return (node.scores[0] - node.scores[1]);
				if(children.length > lowerLevelCutoff)
					limit = lowerLevelCutoff;
				else
					limit = children.length;
//				System.out.println("Approx number of plys at level "+count+" - "+children.length);
				for(int i=0; i<limit; ++i){
					max = Math.max(max, minimax(children[i].node, count+1, alpha, beta));
					if(max >= beta)
						return max;
					alpha = Math.max(alpha, max);
				}
				return max;
			}
			else{
				int min = Integer.MAX_VALUE;
				int limit;
				Region[] children = node.allSuccessors(count, intermedBoard);
				if(children == null)
					return (node.scores[0] - node.scores[1]);
//				System.out.println("Approx number of plys at level "+count+" - "+children.length);
				if(children.length > lowerLevelCutoff)
					limit = lowerLevelCutoff;
				else
					limit = children.length;
				for(int i=0; i<limit; ++i){
					min = Math.min(min, minimax(children[i].node, count+1, alpha, beta));
					if(min <= alpha)
						return min;
					beta = Math.min(beta, min);
				}
				return min;
			}
//			else if(count%2 == 0){
//				int max = Integer.MIN_VALUE;
//				int limit = 15;
//				int k = 0;
//				outer:
//				for(int i=0; i<n; ++i)
//					for(int j=0;j<n;++j)
//						if(intermedBoard[i][j]!='*'){
////							System.out.println(i+":"+j+":"+count);
//							max = Math.max(max, minimax(node.successorFunction(i, j, count, intermedBoard), count+1, alpha, beta));
//							if(max >= beta)
//								return max;
//							alpha = Math.max(alpha, max);
//							flag=true;
//							k++;
//							if(k > limit)
//								break outer;
////							if(k > 3*numRemaining/4)
////								break outer;
//							}
//				if(flag==false)
//					return (node.scores[0] - node.scores[1]);
//				return max;
//			}
//			else{
//				int min = Integer.MAX_VALUE;
//				int limit = 15;
//				int k = 0;
//				outer:
//				for(int i=0; i<n; ++i)
//					for(int j=0;j<n;++j)
//						if(intermedBoard[i][j]!='*'){
////							System.out.println(i+":"+j+":"+count);
//							min = Math.min(min, minimax(node.successorFunction(i, j, count, intermedBoard), count+1, alpha, beta));
//							if(min <= alpha)
//								return min;
//							beta = Math.min(beta, min);
//							flag=true;
//							k++;
//							if(k > limit)
//								break outer;
////							if(k > 3*numRemaining/5)
////								break outer;
//						}
//				if(flag==false)
//					return (node.scores[0] - node.scores[1]);
//				return min;
//			}
		}
		void display(){
//			for(int i=0; i<n; ++i){
//				System.out.println();
//				for(int j=0; j<n; ++j)
//					System.out.print(board[i][j]);
//			}
			System.out.println("\nScore for player 1 - "+scores[0]+" Score for player 2 - "+scores[1]+"\n");
		}
		char[][] deepCopy(char[][] a){
			char[][] b = new char[a.length][];
			for(int i=0; i<a.length; ++i)
				b[i] = Arrays.copyOf(a[i], a[i].length);
			return b;
		}
	}
	int lookUp(double timeIncr){
		int index, pIndex;
		if(numRemaining < 37)
			index = 0;
		else if(numRemaining < 75)
			index = 1;
		else if(numRemaining < 125)
			index = 2;
		else if(numRemaining < 175)
			index = 3;
		else if(numRemaining < 225)
			index = 4;
		else if(numRemaining < 300)
			index = 5;
		else if(numRemaining < 375)
			index = 6;
		else if(numRemaining < 425)
			index = 7;
		else if(numRemaining < 500)
			index = 8;
		else if(numRemaining < 600)
			index = 9;
		else 
			index = 10;
		if(p<=4)
			pIndex = 0;
		else if(p<=7)
			pIndex = 1;
		else
			pIndex = 2;
		int depth = 2;
		int i;
//		System.out.println("Index - ");
		for(i=1; i< lookUpTable[index][pIndex].length; ++i)
			if(timeIncr < lookUpTable[index][pIndex][i])
				break;
     	if(i == lookUpTable[index][pIndex].length){
         	depth = i+1;
         	return depth;
        }
		if(timeIncr > 0.8*(lookUpTable[index][pIndex][i]))
			depth = i+2;
		else 
			depth = i+1;
		return depth;
	}
	Player6(String fileName){
		try{ 
			maxDepth = 2;
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			n = Integer.parseInt(br.readLine());
			p = Integer.parseInt(br.readLine());
			timeLeft = Double.parseDouble(br.readLine());
			numRemaining = 0;
			board = new char[n][n];
			for(int i=0; i<n; ++i){
				String str = br.readLine();
				for(int j=0; j<n; ++j){
					board[i][j] = str.charAt(j);
					if(board[i][j] != '*')
						numRemaining++;
				}
			}
			br.close();
			fractionFilled = (double)numRemaining/(n*n);
			double timeIncr = timeLeft/(0.9*0.75*numRemaining/2);
			startTime = System.nanoTime();
			cutoffTime = startTime + (long)(timeIncr*Math.pow(10, 9));
			regionCutoff = 300;
			lowerLevelCutoff = 100;
			if(numRemaining >=196)
				lowerLevelCutoff = 50;
			if(numRemaining >=500){
				regionCutoff = 100;
				lowerLevelCutoff = 25;
			}
			factor = 0.75;
//			System.out.println("Allotted Time - "+timeIncr);

		}
		catch(Exception e){
			e.printStackTrace();
		}
		solve(fileName);
	}
	void solve(String fileName){
		Node root = new Node(board, new int[2]);
//		System.out.print(n+" "+p);
//		root.display();
		int minimaxNode = root.computeMinimax();
		int x = minimaxNode/n;
		int y = minimaxNode%n;
		Node child = root.successorFunction(x, y, 0);
//		child.display();
		try{
			BufferedWriter br = new BufferedWriter(new FileWriter("output.txt"));
			char xx = (char)(y + 65);
			br.write(""+xx+(x+1)+"\n");
			for(int i=0; i<n; ++i){
				for(int j=0; j<n; ++j)
					br.write(""+child.board[i][j]);
				br.write('\n');
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
public class GamePlayer {
	public static void main(String[] args){
//		long start;
//		start = System.nanoTime();
		new Player6("input.txt");
//		System.out.println("Time - "+(System.nanoTime() - start));
	}
}

