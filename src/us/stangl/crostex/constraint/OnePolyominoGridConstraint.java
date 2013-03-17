/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.constraint;

import us.stangl.crostex.Grid;

/**
 * Constraint enforcing that a grid contain only 1 polyomino.
 * Polyomino is defined to be areas of adjacent (row or column adjacent, not diagonal) non-black cells.
 * 
 * American crosswords traditionally have this one polyomino constraint -- no isolated islands.
 * @author Alex Stangl
 */
public class OnePolyominoGridConstraint implements GridConstraint {
	/**
	 * @return whether grid has exactly one polyomino
	 */
	public boolean satisfiedBy(Grid grid) {
		// Create temp grid isomorphic (same-sized) to grid w/ -1 in blacks, 0 elsewhere
		int[][] tempGrid = new int[grid.getHeight()][grid.getWidth()];
		for (int row = 0; row < grid.getHeight(); ++row) {
			for (int col = 0; col < grid.getWidth(); ++col) {
				tempGrid[row][col] = grid.getCell(row, col).isBlack() ? -1 : 0;
			}
		}
		int firstEmptyCell = findValInGrid(tempGrid, 0);
		if (firstEmptyCell == -1)
			return false;				// all black grid! This degenerate case represents ZERO polyominos.

		floodFill(tempGrid, firstEmptyCell / tempGrid.length, firstEmptyCell % tempGrid.length, 1);
		return findValInGrid(tempGrid, 0) == -1;
	}
	
	/** search for value in grid, returning its cell-number, where [0][0] is 0, and numbered across cols, then down rows (row-major), or -1 if not found */
	private int findValInGrid(int[][] grid, int val) {
		int retval = 0;
		for (int row = 0; row < grid.length; ++row) {
			for (int col = 0; col < grid[row].length; ++col) {
				if (grid[row][col] == val)
					return retval;
				++retval;
			}
		}
		return -1;
	}
	
	/**
	 * If row, col point to empty (0) cell in grid, set it to specified val and carry over to adjacent cells.
	 */
	private void floodFill(int[][] grid, int row, int col, int val) {
		if (row >= 0 && col >= 0 && row < grid.length && col < grid[0].length && grid[row][col] == 0) {
			grid[row][col] = val;
			floodFill(grid, row, col - 1, val);
			floodFill(grid, row, col + 1, val);
			floodFill(grid, row - 1, col, val);
			floodFill(grid, row + 1, col, val);
		}
	}
	
	/*
	 * 
; grid cell contents possibilities: unfilled (blank), black, or other (i.e., filled w/ one or more letters)

(defun find-in-grid (grid pred ret)
  "apply pred to every cell in grid (row-major) and return (ret grid row col) for first true cell, or nil"
  (dotimes (row (array-dimension grid 0))
    (dotimes (col (array-dimension grid 1))
      (let ((cell (aref grid row col)))
        (if (funcall pred cell grid row col) (return-from find-in-grid (funcall ret cell grid row col))))))
  nil)

(defun find-val-in-grid (grid val)
  "return (row col) of first occurrence of val in grid, or nil if not found"
  (find-in-grid grid
                (lambda (cell grd row col) 
                  (declare (ignore grd row col))
                  (= val cell))
                (lambda (cell grd row col)
                  (declare (ignore cell grd))
                  (list row col))))

(defun flood-fill (grid row col val)
  "if row, col point to empty (0) cell in grid, set it to specified val and carry over to adjacent cells"
  (if (and (array-in-bounds-p grid row col)
           (= 0 (aref grid row col)))
      (progn
        (setf (aref grid row col) val)
        (flood-fill grid row (1- col) val)
        (flood-fill grid row (1+ col) val)
        (flood-fill grid (1- row) col val)
        (flood-fill grid (1+ row) col val))))

(defun onePolyomino (grid)
  "return T or nil reflecting whether the specified grid contains exactly 1 polyomino"
  (let ((temp-grid (make-array (array-dimensions grid))))
    ; populate tempGrid based upon grid, with -1 in fill blocks, 0 elsewhere
    (dotimes (i (array-total-size grid))
      (setf (row-major-aref temp-grid i)
            (if (= (row-major-aref grid i) 0) 0 -1)))
    (let ((first-empty-cell (find-val-in-grid temp-grid 0)))
      (if (null first-empty-cell)
          nil
          (progn
            (flood-fill temp-grid (car first-empty-cell) (cadr first-empty-cell) 1)
            (not (find-val-in-grid temp-grid 0)))))))

(defun mirror-coord (grid row col)
  "return (row col) for grid mirrored about ctr w/ 180 degree rotational symmetry"
  ; return (height - 1 - row, width - 1 - col)
  (list (- (array-dimension grid 0) 1 row) (- (array-dimension grid 1) 1 col)))

(defun symmetricp (grid)
  "return whether the specified grid has 180 degree rotational symmetry"
  ; not found-symmetry-violation
  (not (find-in-grid grid
                     (lambda (cell grd row col)
                       (let ((mirror-coord (mirror-coord grd row col)))
                         (/= cell (aref grd (car mirror-coord) (cadr mirror-coord)))))
                     (lambda (cell grd row col)
                       (declare (ignore cell grd row col))
                       t))))

(defun meets-constraintsp (grid constraints)
  "return whether the specified grid meets all the specified constraints"
  (dolist (constraint constraints)
    (unless (funcall constraint grid) (return-from meets-constraintsp nil)))
  t)

	 */

}
