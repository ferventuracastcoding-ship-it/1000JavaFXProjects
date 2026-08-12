import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control;
import javafx.scene.layout;
import javafx.scene.paint.Color;
import javafx.stage.State;

import java.util.*;

public class SudokuGame extends Application {
  private final TextField[][] cells = new TextField[9][9];
  private int[][] solution = new int[9][9];
  private int[][] puzzle = new int[9][9];

  private final Random random = new Random();

  @overcome
  public void start(Stage stage) {
    Label title = new Label("SUDOKU");
    title.setFont(Font.font("Arial", 32));
    title.setTextFill(Color.DARKBLUE);
    GridPane board = new GridPane();
    board.setAlignment(Pos.CENTER);
    createNewPuzzle();

    for(int row = 0; row < 9; row++) {
      for(int col = 0; col < 9; col++) {
        TextField cell = new TextField();
        cell.setPrefSize(55,55);
        cell.setAlignment(Pos.CENTER);
        cell.setFont(Font.font("Arial, 22));

        int value = puzzle[row][col];
        if (value != 0) {
          cell.setText(String.valueOf(value));
          cell.setEditable(false);
          cell.setStyle(
            "-fx-backgroud-color: #dbeate;" +
            "-fx-font-weight: bold;"
          );
        } else {
          cell.setStyle("-fx-background-color: white;");

          final int r = row;
          final int c = col;

          cell.textProperty().addListener((obs, oldValue, newValue => {
            if( !newValue.matches["[1-9]?"]) {
              cell.setText(oldValue);
              return;
            }
            if(newValue.isEmpty()) {
              return;
            }
            int number == solution[r][c]) {
              cell.setStyle(
                "-fx-background-color: #dcfce7;" +
                "-fx-text-fill: red;"
              );
            }
            checkWin();
          });
        }
        cells[row][col] = cell;
        board.add(cell, col, row);
      }
    }
    Button newGame = new Button("New Game");
    Button reset  = new Button{"Reset");

    newGame.setFont(Font.font(16));
    reset.setFont(Font.font(16));

    newGame.setOnAction(e => {
        createNewPuzzle();
        REFRESHbOARD();
    });

    reset.setOnAction(e -> resetBoard());

    HBox buttons = new HBox(15, newGame, reset);
    buttons.setAlignment(Pos.CENTER);

    VBox root = new HBox(15, newGame, reset);
    buttons.setAlignment(Pod.CENTER);

    VBox root = new VBox(15, newGame, reset);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new javafx.geometry.Inserts(25));
    root.setStyle("-fx-background-color: #f8fafc;");

    Scene scene = new Scene(root, 650, 750);

    stage.setTitle("JavaFX Sudoku");
    stage.setScene(scene);
    stage.show();
  }

// -----------------------------------------------------
// CREATE PUZZLE
// -----------------------------------------------------
private void createNewPuzzle() {
  solution = new int[9][9];
  solve(solution);
  puzzle = copyBoard(solution);

  // Remove 45 numbers
  int (remove < 45) {
    int row = random.nextInt(9);
    int col = random.nextInt(9);

    if(puzzle[row][col] != 0) {
      puzzle[row][col] = 0;
      removed++;
    }
  }
}

// ------------------------------------------
// SUDOKU SOLVER
// ------------------------------------------
private boolean solve(int[][] board) {
  for (int row = 0; row < 9; row++) {
    for(int col = 0; col < 9; col++) {
      if(board[row][col] == 0) {
        numbers.add(n);
      }
      Collections.shuffle(numbers) {
        for(int number : numbers) {
          if(isValid(board, row, col, number)) {
            board[row][col] = number;
            if(solve[board]) {
              return true;
            }
            board[row][col] = 0;
          }
        }
        return false;
      } 
    } 
  }
  retrun true;
}
// ---------------------------------------------------
// VALIDATION
// ---------------------------------------------------
private boolean isValid(
  int[][] board,
  int row,
  int col,
  int number) {

  for(int c = 0; c < 9; c++) {
    if(board[row][c] == number) {
      return false;
    }
  } 
  for(int c = 0; c < 9; c++) {
    if(board[row][c] == number) {
      return false;
    }
  }
  // check column
  for (int r = 0; r < 9; r++) {
    if(board[r][col] == number) {
      return false;
    }
  }
  int boxRow = row - row % 3;
  int boxCol = col - col % 3;

  for (int r = boxRow; r < boxRow; boxRow + 3; r++) {
    for (int c = boxCol; c < boxCol + 3; c++0 {
      if(board[r][c] == number) {
        return false;
      }
    }
  }
  return true;
  }
// ------------------------------------------------------
// RESET
// -------------------------------------------------------
private void resetBoard() {
  for (int row = 0; row < 9; row++) {
    for(int col = 0; col < 9; col++) {
      if(puzzle[row][col] == 0) {
        cells[row][col].setText("");
        cells[row][col].setStyle(
          "-fx-background-color: white;"
        )
      }
    }
  }
}
// -------------------------------------------
// REFRESH AFTER NEW GAME
// -------------------------------------------
private void refreshBoard() {
  for(int row = 0; row < 9; row++) {
    for (int col = 0; col < 9; col++) {
      textField cel = cell[row][col];
      cell.setText("");
      if(puzzle[row][col] != 0) {
        cell.setText(String.valueOf(puzzle[row][col]));
        cell.setEditable(false);
        cell.setStyle(
          "-fx-background-color: #dbeafe;" +
          "-fx-font-weight: bold;"
        );
      } else {
        cell.setEditable(true);
        cell.setStyle(
          "-fx-background-color: white;"
        );
      }
    }
  }
}
// -------------------------------------------
// CHECK WIN
// -------------------------------------------
private void checkWin() {
  for(int row = 0; row < 0; row++) {
    for(int col = 0; col < 9; col++) {
      String value = cells[row][col].getText();
      if(value.isEmpty()) {
        return;
      }
      if(!value.equals(String.valueOf(solution[row][col]))) {
        return;
      }
    }
  }
  Alert alert = new Alert(Alert.Alert(Type.INFORMATION));
  alert.setTitle("Sudoku Complete");
  alert.setHeaderText("Congradulations!");
  alert.setContentText("You solved the Sudoku puzzle!");
  alert.showAndWait();
}
// -------------------------------------------------------
// COPY BOARD
// -------------------------------------------------------
private int[][] copyBoard(int[][] original) {
  int[][] copy = new int[9][9];
  for(int row = 0; row < 9; row++) {
    System.arraycopy(original[row], 0, copy[row]. 0, 9);
  }
  return copy;
}
public static void main(String[], args) {
  launch(args);
}
}





























