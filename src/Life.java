import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.util.ArrayList;

public class Life extends JFrame implements ActionListener, KeyListener, MouseListener {
	
	/*
	 * Controls: 
	 * Click to toggle a cell's state between alive and dead. Alive is black.
	 * Press space to toggle the running of the game of life.
	 * Selections: 
	 * Press 's' to toggle select mode. Click for first position and click for second position. In between, you can use arrows to get selection right.
	 * Press space to start moving your selection. Move it by clicking or by arrow keys. 
	 * Press space again to set down selection.
	 * Press 'd' to delete everything in the selection.
	 * Press 'f' to toggle flip mode, where arrow keys then work to flip the selection.
	 * Press 'r' to rotate the selection.
	 * Paste: 
	 * While in select mode with the selection up, press 'c' to copy the selection
	 * While not in select mode, get in select mode with copied selection with 'v'
	 * Import and Export:
	 * Press 'i' to import a file. Type the name of the file in the console. The files are held in the Builds folder. 
	 * The file copies to your copied selection, so you can paste it with 'v'. 
	 * Press 'e' to export your copied selection. 
	 */
	
	// 69 alive cells in a zigzag creates two pentadecathlons
	
	Scanner input = new Scanner(System.in);
	int winY;
	int winX;
	int cellSize;
	double wait;
	int frames;
	Timer timer;
	
	JPanel[][] cell;
	boolean[][] cellAlive; // true is alive, false is dead
	boolean[][] cellAliveOther;
	boolean[][] cellSelection; // memory while moving selection
	boolean[][] cellSelectionOther;
	boolean inputting;
	boolean selecting;
	boolean selectionUp; // this is true if you are moving the selection
	boolean flipping;
	int selectPos; // currently selecting either 1 or 2
	int[] selectPos1 = new int[2]; // in form {y, x}
	int[] selectPos2 = new int[2];
	boolean pasting;
	boolean[][] cellCopy = {{}}; // memory of copy for pasting
	String file;
	ArrayList<String> cellImport;
	int i;
	int j;
	
	int frame = 1;
	int neighbors;
	
	Color lightGreen = new Color(0, 255, 0);
	Color darkGreen = new Color(0, 80, 0);
	Color lightPurple = new Color(255, 0, 255);
	Color darkPurple = new Color(80, 0, 80);
	
	
	Life() {
		
		init(); // Allows user to input values of winY, winX, wait, and frames.
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(null);
		this.setSize(cellSize*winX + 16, cellSize*winY + 39);
		this.setTitle("Conway's Game of Life");
		this.setResizable(false);
		
		cell = new JPanel[winY][winX];
		cellAlive = new boolean[winY][winX];
		cellAliveOther = new boolean[winY][winX];
		for(int i = 0; i < winY; i++) {
			for(int j = 0; j < winX; j++) {
				cell[i][j] = new JPanel();
				cell[i][j].setBounds(cellSize*j, cellSize*i, cellSize, cellSize);
				cell[i][j].setBackground(Color.white);
				cellAlive[i][j] = false;
				cellAliveOther[i][j] = false;
				this.add(cell[i][j]);
			}
		}
		
		selecting = false;
		pasting = false;
		
		this.addMouseListener(this);
		this.addKeyListener(this);
		
		this.setVisible(true);
		inputting = true; // Click to change states of cells, space when ready to play animation.
		System.out.println("Press space when ready");
		
	}
	
	public void init() {
		
		while(true) {
			try {
				System.out.println("How many rows?");
				winY = Integer.parseInt(input.nextLine());
				if(winY <= 0) { 
					continue;
				}
				break;
			} catch(Exception e) {
				continue;
			}
		}
		while(true) {
			try {
				System.out.println("How many columns?");
				winX = Integer.parseInt(input.nextLine());
				if(winX <= 0) {
					continue;
				}
				break;
			} catch(Exception e) {
				continue;
			}
		}
		while(true) {
			try {
				System.out.println("How many pixels for each cell?");
				cellSize = Integer.parseInt(input.nextLine());
				if(cellSize <= 0) {
					continue;
				}
				break;
			} catch(Exception e) {
				continue;
			}
		}
		while(true) {
			try {
				System.out.println("How many seconds of wait?");
				wait  = Double.parseDouble(input.nextLine());
				if(wait <= 0) {
					continue;
				}
				break;
			} catch(Exception e) {
				continue;
			}
		}
		while(true) {
			try {
				System.out.println("For how many frames?");
				frames = Integer.parseInt(input.nextLine());
				if(frames <= 0) {
					continue;
				}
				break;
			} catch(Exception e) {
				continue;
			}
		}
		
	}
	
	public void colorSelection() { // this just makes the parts of the selection green and the rest black or white
		
		for(int i = 0; i < winY; i++) {
			for(int j = 0; j < winX; j++) {
				if(cellAlive[i][j]) {
					if(selectPos1[0] <= i && i <= selectPos2[0] && selectPos1[1] <= j && j <= selectPos2[1]) {
						cell[i][j].setBackground(darkGreen);
					} else {
						cell[i][j].setBackground(Color.black);
					}
				} else {
					if(selectPos1[0] <= i && i <= selectPos2[0] && selectPos1[1] <= j && j <= selectPos2[1]) {
						cell[i][j].setBackground(lightGreen);
					} else {
						cell[i][j].setBackground(Color.white);
					}
				}
			}
		}
		this.repaint();
		
	}

	public void moveSelection() { // writes over cellAlive but not cellAliveOther, and does the same as colorSelection()
		
		for(int i = 0; i < winY; i++) {
			for(int j = 0; j < winX; j++) {
				if(selectPos1[0] <= i && i <= selectPos2[0] && selectPos1[1] <= j && j <= selectPos2[1]) {
					if(cellSelection[i - selectPos1[0]][j - selectPos1[1]]) {
						cell[i][j].setBackground(darkGreen);
						cellAlive[i][j] = true;
					} else {
						cell[i][j].setBackground(lightGreen);
						cellAlive[i][j] = false;
					}
				} else {
					if(cellAliveOther[i][j]) {
						cell[i][j].setBackground(Color.black);
						cellAlive[i][j] = true;
					} else {
						cell[i][j].setBackground(Color.white);
						cellAlive[i][j] = false;
					}
				}
			}
		}
		this.repaint();
		
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		
		if(inputting && !selecting) { // this allows the user to toggle each square between alive and dead in the inputting mode
			
			i = (e.getY() - 31) / cellSize;
			j = (e.getX() - 8) / cellSize;
			
			if(cellAlive[i][j]) {
				cellAlive[i][j] = false;
				cellAliveOther[i][j] = false;
				cell[i][j].setBackground(Color.white);
			} else {
				cellAlive[i][j] = true;
				cellAliveOther[i][j] = true;
				cell[i][j].setBackground(Color.black);
			}
			
			this.repaint();
			
		} else if(inputting && selecting && !selectionUp) {  // this code allows the user to make a selection with the mouse
			
			if(selectPos == 0) { // the first point just makes one square for the selection
				selectPos1[0] = (e.getY() - 31) / cellSize;
				selectPos1[1] = (e.getX() - 8) / cellSize;
				if(cellAlive[selectPos1[0]][selectPos1[1]]) {
					cell[selectPos1[0]][selectPos1[1]].setBackground(darkGreen);
				} else {
					cell[selectPos1[0]][selectPos1[1]].setBackground(lightGreen);
				}
				selectPos = 1;
			} else if(selectPos == 1){ // the second selection makes a rectangle, but the code has to consider if the selection is made not to the right and down from the first selectPos
				selectPos2[0] = (e.getY() - 31) / cellSize;
				selectPos2[1] = (e.getX() - 8) / cellSize;
				if((e.getY() - 31) / cellSize < selectPos1[0]) {
					selectPos2[0] = selectPos1[0];
					selectPos1[0] = (e.getY() - 31) / cellSize;
				}
				if((e.getX() - 8) / cellSize < selectPos1[1]) {
					selectPos2[1] = selectPos1[1];
					selectPos1[1] = (e.getX() - 8) / cellSize;
				}
				selectPos = 2;
				colorSelection();
			}
			
		} else if(selectionUp) {
			
			if(winY - (e.getY() - 31) / cellSize >= selectPos2[0] - selectPos1[0] + 1 && winX - (e.getX() - 8) / cellSize >= selectPos2[1] - selectPos1[1] + 1) {
				selectPos2[0] += (e.getY() - 31) / cellSize - selectPos1[0];
				selectPos2[1] += (e.getX() - 8) / cellSize - selectPos1[1];
				selectPos1[0] = (e.getY() - 31) / cellSize;
				selectPos1[1] = (e.getX() - 8) / cellSize;
				moveSelection();
			}
			
		}
		
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		
		if(e.getKeyChar() == ' ' && !selecting) { // This code toggles the game of life
			if(inputting) {
				inputting = false;
				pasting = false;
				timer = new Timer((int)(1000*wait), this);
				timer.start();
			} else {
				inputting = true;
				timer.stop();
				
			}
		}
		if(e.getKeyChar() == 'v' && inputting && !selecting && cellCopy.length != 0) { // this allows you to paste copied selection by putting you in selection mode 
			selecting = true;
			selectPos = 2;
			selectPos1[0] = 0;
			selectPos1[1] = 0;
			selectPos2[0] = cellCopy.length - 1;
			selectPos2[1] = cellCopy[0].length - 1;
			selectionUp = true;
			cellSelection = new boolean[selectPos2[0] - selectPos1[0] + 1][selectPos2[1] - selectPos1[1] + 1];
			cellSelectionOther = new boolean[selectPos2[0] - selectPos1[0] + 1][selectPos2[1] - selectPos1[1] + 1];
			for(int i = selectPos1[0]; i <= selectPos2[0]; i++) {
				for(int j = selectPos1[1]; j <= selectPos2[1]; j++) {
					cellSelection[i - selectPos1[0]][j - selectPos1[1]] = cellCopy[i][j];
				}
			}
			moveSelection();
		}
		if((e.getKeyChar() == 's' && inputting) || (e.getKeyChar() == ' ' && selectionUp)) { // This code toggles selection mode. If it was in selection mode, it has to make the selection back to black and white. 
			if(!selecting) {
				selecting = true;
				selectionUp = false;
				flipping = false;
				selectPos = 0;
			} else {
				selecting = false;
				selectionUp = false;
				flipping = false;
				for(int i = 0; i < winY; i++) {
					for(int j = 0; j < winX; j++) {
						if(cellAlive[i][j]) {
							cell[i][j].setBackground(Color.black);
						} else {
							cell[i][j].setBackground(Color.white);
						}
					}
					cellAliveOther[i] = cellAlive[i].clone();
				}
				this.repaint();
			}
		}
		if(e.getKeyChar() == ' ' && selecting && !selectionUp) { // This lifts up the selection so that the arrow keys or mouse will move it around
			selectionUp = true;
			cellSelection = new boolean[selectPos2[0] - selectPos1[0] + 1][selectPos2[1] - selectPos1[1] + 1];
			cellSelectionOther = new boolean[selectPos2[0] - selectPos1[0] + 1][selectPos2[1] - selectPos1[1] + 1];
			for(int i = selectPos1[0]; i <= selectPos2[0]; i++) {
				for(int j = selectPos1[1]; j <= selectPos2[1]; j++) {
					cellAliveOther[i][j] = false;
					cellSelection[i - selectPos1[0]][j - selectPos1[1]] = cellAlive[i][j];
				}
			}
		}
		if(e.getKeyChar() == 'c' && selectionUp) { // This copies the selection
			cellCopy = new boolean[cellSelection.length][cellSelection[0].length];
			for(int i = 0; i < cellSelection.length; i++) {
				cellCopy[i] = cellSelection[i].clone();
			}
		}
		if(selecting && !selectionUp) { // This code allows the user to edit the selection with the arrow keys.
			if(e.getKeyCode() == 37) { // left arrow
				if(selectPos == 1 && selectPos1[1] > 0) {
					if(cellAlive[selectPos1[0]][selectPos1[1]]) {
						cell[selectPos1[0]][selectPos1[1]].setBackground(Color.black);
					} else {
						cell[selectPos1[0]][selectPos1[1]].setBackground(Color.white);
					}
					selectPos1[1]--;
					if(cellAlive[selectPos1[0]][selectPos1[1]]) {
						cell[selectPos1[0]][selectPos1[1]].setBackground(darkGreen);
					} else {
						cell[selectPos1[0]][selectPos1[1]].setBackground(lightGreen);
					}
				} else if(selectPos == 2 && selectPos2[1] > 0 && selectPos2[1] > selectPos1[1]) { // selectPos2 is always to the down and right of selectPos1
					selectPos2[1]--;
					colorSelection();
				}
			} else if(e.getKeyCode() == 39) { // right arrow
				if(selectPos == 1 && selectPos1[1] < winX - 1) {
					if(cellAlive[selectPos1[0]][selectPos1[1]]) {
						cell[selectPos1[0]][selectPos1[1]].setBackground(Color.black);
					} else {
						cell[selectPos1[0]][selectPos1[1]].setBackground(Color.white);
					}
					selectPos1[1]++;
					if(cellAlive[selectPos1[0]][selectPos1[1]]) {
						cell[selectPos1[0]][selectPos1[1]].setBackground(darkGreen);
					} else {
						cell[selectPos1[0]][selectPos1[1]].setBackground(lightGreen);
					}
				} else if(selectPos == 2 && selectPos2[1] < winX - 1) {
					selectPos2[1]++;
					colorSelection();
				}
			} else if(e.getKeyCode() == 38) { // up arrow
				if(selectPos == 1 && selectPos1[0] > 0) {
					if(cellAlive[selectPos1[0]][selectPos1[1]]) {	
						cell[selectPos1[0]][selectPos1[1]].setBackground(Color.black);
					} else {
						cell[selectPos1[0]][selectPos1[1]].setBackground(Color.white);
					}
					selectPos1[0]--;
					if(cellAlive[selectPos1[0]][selectPos1[1]]) {
						cell[selectPos1[0]][selectPos1[1]].setBackground(darkGreen);
					} else {
						cell[selectPos1[0]][selectPos1[1]].setBackground(lightGreen);
					}
				} else if(selectPos == 2 && selectPos2[0] > 0 && selectPos2[0] > selectPos1[0]){
					selectPos2[0]--;
					colorSelection();
				}
			} else if(e.getKeyCode() == 40) { // down arrow
				if(selectPos == 1 && selectPos1[0] < winY - 1) {
					if(cellAlive[selectPos1[0]][selectPos1[1]]) {	
						cell[selectPos1[0]][selectPos1[1]].setBackground(Color.black);
					} else {
						cell[selectPos1[0]][selectPos1[1]].setBackground(Color.white);
					}
					selectPos1[0]++;
					if(cellAlive[selectPos1[0]][selectPos1[1]]) {
						cell[selectPos1[0]][selectPos1[1]].setBackground(darkGreen);
					} else {
						cell[selectPos1[0]][selectPos1[1]].setBackground(lightGreen);
					}
				} else if(selectPos == 2 && selectPos2[0] < winY - 1) {
					selectPos2[0]++;
					colorSelection();
				}
			}
		}
		if(selectionUp && !flipping) { // This will move the entire selection. This needs to be fixed so that it also moves contents of selection. Write over cellAlive but not cellAliveNew
			if(e.getKeyCode() == 37 && selectPos1[1] > 0) { // left arrow 
				selectPos1[1]--;
				selectPos2[1]--;
				moveSelection();
			} else if(e.getKeyCode() == 39 && selectPos2[1] < winX - 1) { // right arrow
				selectPos1[1]++;
				selectPos2[1]++;
				moveSelection();
			} else if(e.getKeyCode() == 38 && selectPos1[0] > 0) { // up arrow
				selectPos1[0]--;
				selectPos2[0]--;
				moveSelection();
			} else if(e.getKeyCode() == 40 && selectPos2[0] < winY - 1) { // down arrow
				selectPos1[0]++;
				selectPos2[0]++;
				moveSelection();
			}
		}
		if(selectionUp && flipping) { // this will flip the selection. Up and down arrows flip the selection vertically and left and right arrows flip it horizontally.
			if(e.getKeyCode() == 37 || e.getKeyCode() == 39) { // horizontal flip
				for(int i = 0; i < cellSelection.length; i++) {
					cellSelectionOther[i] = cellSelection[i].clone();
				}
				for(int i = 0; i < cellSelection.length; i++) {
					for(int j = 0; j < cellSelection[0].length; j++) {
						cellSelection[i][j] = cellSelectionOther[i][cellSelection[0].length - j - 1];
					}
				}
				moveSelection();
			} else if(e.getKeyCode() == 38 || e.getKeyCode() == 40) { // vertical flip
				for(int i = 0; i < cellSelection.length; i++) {
					cellSelectionOther[i] = cellSelection[i].clone();
				}
				for(int i = 0; i < cellSelection.length; i++) {
					cellSelection[i] = cellSelectionOther[cellSelection.length - i - 1].clone();
				}
				moveSelection();
			}
		}
		if(e.getKeyChar() == 'd' && selectionUp) { // deletes selection
			for(int i = 0; i < cellSelection.length; i++) {
				for(int j = 0; j < cellSelection[0].length; j++) {
					cellSelection[i][j] = false;
				}
			}
			moveSelection();
		}
		if(e.getKeyChar() == 'f' && selectionUp) { // This toggles flip mode. Note that you have to turn it off when you are done. 
			if(flipping) {
				flipping = false;
			} else {
				flipping = true;
			}
		}
		if(e.getKeyChar() == 'r' && selectionUp && !flipping && selectPos1[0] + cellSelection[0].length - 1 < winY && selectPos1[1] + cellSelection.length - 1 < winX) { // This rotates the selection clockwise
			j = cellSelection.length;
			i = cellSelection[0].length;
			for(int y = 0; y < j; y++) {
				cellSelectionOther[y] = cellSelection[y].clone();
			}
			cellSelection = new boolean[i][j];
			for(int y = 0; y < j; y++) {
				for(int x = 0; x < i; x++) {
					cellSelection[x][j-y-1] = cellSelectionOther[y][x];
				}
			}
			cellSelectionOther = new boolean[i][j];
			selectPos2[0] = selectPos1[0] + i - 1;
			selectPos2[1] = selectPos1[1] + j - 1;
			moveSelection();
		}
		if(e.getKeyChar() == 'i' && inputting && !selecting) {
			System.out.println("What file do you want to import?");
			try {
				Scanner reader = new Scanner(new File("Builds/" + input.nextLine() + ".txt"));
				cellImport = new ArrayList<String>();
				while(reader.hasNextLine()) {
					cellImport.add(reader.nextLine());
				}
				i = cellImport.size();
				j = cellImport.get(0).length();
				if(i == 0 || j == 0) {
					throw new IllegalArgumentException("File empty");
				}
				if(i > winY || j > winX) {
					throw new IllegalArgumentException("File too big");
				}
				cellCopy = new boolean[cellImport.size()][cellImport.get(0).length()];
				for(int y = 0; y < i; y++) {
					if(cellImport.get(y).length() != j) {
						throw new IllegalArgumentException("Incorrect File Format");
					}
					for(int x = 0; x < j; x++) {
						if(cellImport.get(y).charAt(x) == '0') {
							cellCopy[y][x] = false;
						} else {
							cellCopy[y][x] = true;
						}
					}
				}
				System.out.println("File copied");
				reader.close();
			} catch(Exception error) {
				System.out.println(error);
				cellCopy = new boolean[0][0];
			} 
		}
		if(e.getKeyChar() == 'e' && inputting && !selecting && cellCopy.length != 0) {
			System.out.println("What will you name the file?");
			try {
				String name = input.nextLine();
				if (new File("Builds/" + name + ".txt").exists()) {
					System.out.println("That already exists!");
				} else if (cellSelection.length != 0 && cellSelection[0].length != 0){
					PrintWriter writer = new PrintWriter("Builds/" + name + ".txt");
					String line = "";
					for (int i = 0; i < cellSelection.length; i++) {
						line = "";
						for (int j = 0; j < cellSelection[0].length; j++) {
							if (cellSelection[i][j]) {
								line += "1";
							} else {
								line += 0;
							}
						}
						writer.println(line);
					}
					writer.close();
				}
			} catch (Exception error) {
				System.out.println(error);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == timer) {
			if(frames - 1 == frame) {
				timer.stop();
			} else {
				frame++;
				for(int y = 0; y < winY; y++) {
					for(int x = 0; x < winX; x++) {
						neighbors = 0;
						if(x > 0) {
							if(cellAlive[y][x-1]) {
								neighbors++;
							}
							if(y > 0) {
								if(cellAlive[y-1][x-1]) {
									neighbors++;
								}
							}
							if(y < winY-1) {
								if(cellAlive[y+1][x-1]) {
									neighbors++;
								}
							}
						}
						if(x < winX-1) {
							if(cellAlive[y][x+1]) {
								neighbors++;
							}
							if(y > 0) {
								if(cellAlive[y-1][x+1]) {
									neighbors++;
								}
							}
							if(y < winY-1) {
								if(cellAlive[y+1][x+1]) {
									neighbors++;
								}
							}
						}
						if(y > 0) {
							if(cellAlive[y-1][x]) {
								neighbors++;
							}
						}
						if(y < winY-1) {
							if(cellAlive[y+1][x]) {
								neighbors++;
							}
						}
						if(cellAlive[y][x]) {
							if(neighbors < 2) {
								cell[y][x].setBackground(Color.white);
								cellAliveOther[y][x] = false;
							} else if(neighbors > 3) {
								cell[y][x].setBackground(Color.white);
								cellAliveOther[y][x] = false;
							}
						} else if(neighbors == 3) {
								cell[y][x].setBackground(Color.black);
								cellAliveOther[y][x] = true;
						}
					}
				}
				for(int y = 0; y < winY; y++) {
					cellAlive[y] = cellAliveOther[y].clone();
				}
				this.repaint();
			}
		}
	}

	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}	
	
}
