package com.siven.service;

import com.siven.entity.Employee;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@StepScope
public class ExcelEmployeeReader implements ItemReader<Employee> {

    private final Queue<Employee> employeeQueue = new ConcurrentLinkedQueue<>();
    private boolean initialized = false;

    @Override
    public Employee read() {
        if (!initialized) {
            try {
                initialize();
                initialized = true;
            } catch (IOException e) {
                throw new RuntimeException("Failed to read Excel file", e);
            }
        }
        return employeeQueue.poll();
    }

    private void initialize() throws IOException {
        InputStream is = new ClassPathResource("employees.xlsx").getInputStream();
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();
        if (rows.hasNext()) rows.next(); // skip header

        while (rows.hasNext()) {
            Row row = rows.next();
            Employee emp = new Employee();
            emp.setEmpId(getCellValueAsString(row.getCell(0)));
            emp.setName(getCellValueAsString(row.getCell(1)));
            emp.setDepartment(getCellValueAsString(row.getCell(2)));

            String salaryStr = getCellValueAsString(row.getCell(3));
            emp.setSalary(salaryStr != null && !salaryStr.isEmpty() ? Double.parseDouble(salaryStr) : 0.0);

            employeeQueue.add(emp);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    DataFormatter formatter = new DataFormatter();
                    return formatter.formatCellValue(cell);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                CellValue cellValue = evaluator.evaluate(cell);
                return getFormulaCellValueAsString(cellValue);
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private String getFormulaCellValueAsString(CellValue cellValue) {
        switch (cellValue.getCellType()) {
            case STRING:
                return cellValue.getStringValue();
            case NUMERIC:
                return String.valueOf(cellValue.getNumberValue());
            case BOOLEAN:
                return String.valueOf(cellValue.getBooleanValue());
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
