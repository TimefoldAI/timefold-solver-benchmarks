package ai.timefold.solver.benchmarks.examples.common.persistence;

import static ai.timefold.solver.benchmarks.examples.common.persistence.XSSFColorUtil.getXSSFColor;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.impl.score.definition.ScoreDefinition;
import ai.timefold.solver.core.impl.score.director.InnerScoreDirectorFactory;
import ai.timefold.solver.core.impl.score.director.ScoreDirectorFactory;
import ai.timefold.solver.core.impl.solver.DefaultSolverFactory;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;
import ai.timefold.solver.swing.impl.TangoColorFactory;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public abstract class AbstractXlsxSolutionFileIO<Solution_> implements SolutionFileIO<Solution_> {

    public static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("E yyyy-MM-dd", Locale.ENGLISH);
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH);
    protected static final Pattern VALID_TAG_PATTERN = Pattern
            .compile("(?U)^[\\w&\\-\\.\\/\\(\\)\\'][\\w&\\-\\.\\/\\(\\)\\' ]*[\\w&\\-\\.\\/\\(\\)\\']?$");
    protected static final Pattern VALID_NAME_PATTERN = VALID_TAG_PATTERN;
    protected static final Pattern VALID_CODE_PATTERN = Pattern.compile("(?U)^[\\w\\-\\.\\/\\(\\)]+$");
    protected static final XSSFColor UNAVAILABLE_COLOR = getXSSFColor(TangoColorFactory.ALUMINIUM_5);

    @Override
    public String getInputFileExtension() {
        return "xlsx";
    }

    public static abstract class AbstractXlsxReader<Solution_, Score_ extends Score<Score_>> {

        protected final XSSFWorkbook workbook;
        protected final ScoreDefinition<Score_> scoreDefinition;

        protected Solution_ solution;

        protected XSSFSheet currentSheet;
        protected Iterator<Row> currentRowIterator;
        protected XSSFRow currentRow;
        protected int currentRowNumber;
        protected int currentColumnNumber;

        public AbstractXlsxReader(XSSFWorkbook workbook, String solverConfigResource) {
            this.workbook = workbook;
            SolverFactory<Solution_> solverFactory = SolverFactory.createFromXmlResource(solverConfigResource);
            ScoreDirectorFactory<Solution_> scoreDirectorFactory =
                    ((DefaultSolverFactory<Solution_>) solverFactory).getScoreDirectorFactory();
            scoreDefinition = ((InnerScoreDirectorFactory<Solution_, Score_>) scoreDirectorFactory).getScoreDefinition();
        }

        public abstract Solution_ read();

        protected void readIntConstraintParameterLine(String name, Consumer<Integer> consumer, String constraintDescription) {
            nextRow();
            readHeaderCell(name);
            XSSFCell weightCell = nextCell();
            if (consumer != null) {
                if (weightCell.getCellType() != CellType.NUMERIC) {
                    throw new IllegalArgumentException(currentPosition() + ": The value ("
                            + weightCell.getStringCellValue()
                            + ") for constraint (" + name + ") must be a number and the cell type must be numeric.");
                }
                double value = weightCell.getNumericCellValue();
                if (((int) value) != value) {
                    throw new IllegalArgumentException(currentPosition() + ": The value (" + value
                            + ") for constraint (" + name + ") must be an integer.");
                }
                consumer.accept((int) value);
            } else {
                if (weightCell.getCellType() == CellType.NUMERIC
                        || !weightCell.getStringCellValue().equals("n/a")) {
                    throw new IllegalArgumentException(currentPosition() + ": The value ("
                            + weightCell.getStringCellValue()
                            + ") for constraint (" + name + ") must be an n/a.");
                }
            }
            readHeaderCell(constraintDescription);
        }

        protected void readLongConstraintParameterLine(String name, Consumer<Long> consumer, String constraintDescription) {
            nextRow();
            readHeaderCell(name);
            XSSFCell weightCell = nextCell();
            if (consumer != null) {
                if (weightCell.getCellType() != CellType.NUMERIC) {
                    throw new IllegalArgumentException(currentPosition() + ": The value ("
                            + weightCell.getStringCellValue()
                            + ") for constraint (" + name + ") must be a number and the cell type must be numeric.");
                }
                double value = weightCell.getNumericCellValue();
                if (((long) value) != value) {
                    throw new IllegalArgumentException(currentPosition() + ": The value (" + value
                            + ") for constraint (" + name + ") must be a (long) integer.");
                }
                consumer.accept((long) value);
            } else {
                if (weightCell.getCellType() == CellType.NUMERIC
                        || !weightCell.getStringCellValue().equals("n/a")) {
                    throw new IllegalArgumentException(currentPosition() + ": The value ("
                            + weightCell.getStringCellValue()
                            + ") for constraint (" + name + ") must be an n/a.");
                }
            }
            readHeaderCell(constraintDescription);
        }

        protected void readScoreConstraintHeaders() {
            nextRow(true);
            readHeaderCell("Constraint");
            readHeaderCell("Score weight");
            readHeaderCell("Description");
        }

        protected Score_ readScoreConstraintLine(String constraintName, String constraintDescription) {
            nextRow();
            readHeaderCell(constraintName);
            String scoreString = nextStringCell().getStringCellValue();
            readHeaderCell(constraintDescription);
            return scoreDefinition.parseScore(scoreString);
        }

        protected String currentPosition() {
            return "Sheet (" + currentSheet.getSheetName() + ") cell ("
                    + (currentRowNumber + 1) + CellReference.convertNumToColString(currentColumnNumber) + ")";
        }

        protected void nextSheet(String sheetName) {
            currentSheet = workbook.getSheet(sheetName);
            if (currentSheet == null) {
                throw new IllegalStateException("The workbook does not contain a sheet with name ("
                        + sheetName + ").");
            }
            currentRowIterator = currentSheet.rowIterator();
            if (currentRowIterator == null) {
                throw new IllegalStateException(currentPosition() + ": The sheet has no rows.");
            }
            currentRowNumber = -1;
        }

        protected boolean nextRow() {
            return nextRow(true);
        }

        protected boolean nextRow(boolean skipEmptyRows) {
            currentRowNumber++;
            currentColumnNumber = -1;
            if (!currentRowIterator.hasNext()) {
                currentRow = null;
                return false;
            }
            currentRow = (XSSFRow) currentRowIterator.next();
            while (skipEmptyRows && currentRowIsEmpty()) {
                if (!currentRowIterator.hasNext()) {
                    currentRow = null;
                    return false;
                }
                currentRow = (XSSFRow) currentRowIterator.next();
            }
            if (currentRow.getRowNum() != currentRowNumber) {
                if (currentRow.getRowNum() == currentRowNumber + 1) {
                    currentRowNumber++;
                } else {
                    throw new IllegalStateException(currentPosition() + ": The next row (" + currentRow.getRowNum()
                            + ") has a gap of more than 1 empty line with the previous.");
                }
            }
            return true;
        }

        protected boolean currentRowIsEmpty() {
            if (currentRow.getPhysicalNumberOfCells() == 0) {
                return true;
            }
            for (Cell cell : currentRow) {
                if (cell.getCellType() == CellType.STRING) {
                    if (!cell.getStringCellValue().isEmpty()) {
                        return false;
                    }
                } else if (cell.getCellType() != CellType.BLANK) {
                    return false;
                }
            }
            return true;
        }

        protected void readHeaderCell(String value) {
            XSSFCell cell = currentRow == null ? null : nextStringCell();
            if (cell == null || !cell.getStringCellValue().equals(value)) {
                throw new IllegalStateException(currentPosition() + ": The cell ("
                        + (cell == null ? null : cell.getStringCellValue())
                        + ") does not contain the expected value (" + value + ").");
            }
        }

        protected XSSFCell nextStringCell() {
            XSSFCell cell = nextCell();
            if (cell.getCellType() == CellType.NUMERIC) {
                throw new IllegalStateException(currentPosition() + ": The cell with value ("
                        + cell.getNumericCellValue() + ") has a numeric type but should be a string.");
            }
            return cell;
        }

        protected XSSFCell nextNumericCell() {
            XSSFCell cell = nextCell();
            if (cell.getCellType() == CellType.STRING) {
                throw new IllegalStateException(currentPosition() + ": The cell with value ("
                        + cell.getStringCellValue() + ") has a string type but should be numeric.");
            }
            return cell;
        }

        protected XSSFCell nextNumericCellOrBlank() {
            XSSFCell cell = nextCell();
            if (cell.getCellType() == CellType.BLANK
                    || (cell.getCellType() == CellType.STRING && cell.getStringCellValue().isEmpty())) {
                return null;
            }
            if (cell.getCellType() == CellType.STRING) {
                throw new IllegalStateException(currentPosition() + ": The cell with value ("
                        + cell.getStringCellValue() + ") has a string type but should be numeric.");
            }
            return cell;
        }

        protected XSSFCell nextBooleanCell() {
            XSSFCell cell = nextCell();
            if (cell.getCellType() == CellType.STRING) {
                throw new IllegalStateException(currentPosition() + ": The cell with value ("
                        + cell.getStringCellValue() + ") has a string type but should be boolean.");
            }
            if (cell.getCellType() == CellType.NUMERIC) {
                throw new IllegalStateException(currentPosition() + ": The cell with value ("
                        + cell.getNumericCellValue() + ") has a numeric type but should be a boolean.");
            }
            return cell;
        }

        protected XSSFCell nextCell() {
            currentColumnNumber++;
            XSSFCell cell = currentRow.getCell(currentColumnNumber);
            // TODO HACK to workaround the fact that LibreOffice and Excel automatically remove empty trailing cells
            if (cell == null) {
                // Return dummy cell
                return currentRow.createCell(currentColumnNumber);
            }
            return cell;
        }

        protected XSSFColor extractColor(XSSFCell cell, XSSFColor... acceptableColors) {
            XSSFCellStyle cellStyle = cell.getCellStyle();
            FillPatternType fillPattern = cellStyle.getFillPattern();
            if (fillPattern == null || fillPattern == FillPatternType.NO_FILL) {
                return null;
            }
            if (fillPattern != FillPatternType.SOLID_FOREGROUND) {
                throw new IllegalStateException(currentPosition() + ": The fill pattern (" + fillPattern
                        + ") should be either " + FillPatternType.NO_FILL
                        + " or " + FillPatternType.SOLID_FOREGROUND + ".");
            }
            XSSFColor color = cellStyle.getFillForegroundColorColor();
            for (XSSFColor acceptableColor : acceptableColors) {
                if (acceptableColor.equals(color)) {
                    return acceptableColor;
                }
            }
            throw new IllegalStateException(currentPosition() + ": The fill color (" + color
                    + ") is not one of the acceptableColors (" + Arrays.toString(acceptableColors) + ").");
        }
    }

    @Override
    public final void write(Solution_ solution, File outputSolutionFile) {
        throw new UnsupportedOperationException();
    }

}
