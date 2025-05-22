package com.siven.service;

import com.siven.entity.Employee;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

@Component
@StepScope
public class PartitionedExcelEmployeeReader implements ItemReader<Employee> {

    @Value("#{stepExecutionContext['startRow']}")
    private int startRow;

    @Value("#{stepExecutionContext['endRow']}")
    private int endRow;

    private Queue<Employee> employeeQueue = new LinkedList<>();
    private boolean initialized = false;

    @Override
    public Employee read() {
        if (!initialized) {
            initialize();
            initialized = true;
        }
        return employeeQueue.poll();
    }

    private void initialize() {
        try (InputStream is = new ClassPathResource("employees.xlsx").getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = startRow; i <= endRow && i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Employee emp = new Employee();
                emp.setEmpId(getCellValueAsString(row.getCell(0)));
                emp.setName(getCellValueAsString(row.getCell(1)));
                emp.setDepartment(getCellValueAsString(row.getCell(2)));

                String salaryStr = getCellValueAsString(row.getCell(3));
                emp.setSalary(salaryStr != null && !salaryStr.isEmpty() ? Double.parseDouble(salaryStr) : 0.0);

                employeeQueue.add(emp);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading Excel partition", e);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }
}
